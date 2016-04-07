// De dashboard 'namespace' (vergelijk met static klasse) bevat methodes om het dashboard up te daten
// Aanroepen:
// Dashboard.reload();

var Dashboard = {

	mode: this.INTERVAL, // Selected mode
	provider: null, // Selected provider

	// Mogelijke dashboard standen cte's
	LIVE: 0, // Vandaag
	INTERVAL: 1, // Periode
	COMPARE_INTERVALS: 2, // Vergelijk periodes
	COMPARE_DAYS: 3, // Vergelijk dagen

	// Geselecteerde intervallen en datums. Ofwel Interval ofwel Event objecten
	selectedIntervals: [Interval.create(null, null), Interval.create(null, null)],

	// Om de veranderingen van de selectedIntervals waar te nemen NA dat de popover is gesloten
	// (hierdoor ontvangen we niet voortdurend change events) gebruiken we nog een extra
	// property die de laatst gebruikte intervallen opslaat
	lastKnownIntervals: [],

	intervalsDidChange: function() {
		// Herladen?
	},

	init: function() {
		this.provider = null;


		// TODO: TEMPORARY
		this.mode = this.INTERVAL;

		this.reload();
		Api.syncProviders(this.loadProviders, this);
	},

	loadProviders: function() {
		var str = '';
		var me = this;
		providers.forEach(function(provider){
			if (!me.provider){
				me.setProvider(provider.id);
			}
			var p = {
				id: provider.id,
				name: provider.name,
				checked: (provider.id == me.provider.id)
			};
			str += Mustache.renderTemplate("provider", p);
		});
		$('#providers').html(str);
	},

	setMode: function(mode){
		this.mode = mode;
		this.reload();
	},

	setProvider: function(providerId){
		console.log("set provider id "+providerId);
		this.provider = providers[providerId];
		this.reload();
	},
	
	// Herlaad het dashboard op de huidige stand
	reload: function() {
		if (!this.provider){
			return;
		}

		var dashboard = $('#dashboard .content');

		// Alles wissen (dit kan later weg, maar is om te voorkomen dat thisReady meerdere keren wordt uitgevoerd op dezelfde elementen)
		// Als alles juist geprogrammeerd is, dan zal het dashboard nooit leeg zijn.
		// Dus kan dit deeltje later weg. Maar voorlopig niet, zodat we deze oorzaak snel zien (dashboard zal leeg zijn). 
		// (zie ook commentaar bij thisReady hieronder)
		dashboard.html('');

		switch(this.mode){
			case Dashboard.LIVE: 
				this.reloadLive(); 
			break;
			case Dashboard.INTERVAL: 
				this.reloadInterval(); 
			break;
			case Dashboard.COMPARE_INTERVALS: 
				this.reloadCompareIntervals(); 
			break;
			case Dashboard.COMPARE_DAYS: 
				this.reloadCompareDays(); 
			break;
		}
		
		// thisReady mag NOOIT meerdere keren uitgevoerd worden op hetzelfde element (wel zelfde parent element)
		// , niet op dezelfde kinderen.
		// Het deel hierboven moet dus 100% zeker het dashboard resetten
		thisReady.call(dashboard);
	},

	// Als niet voldoende gegevens beschikbaar zijn -> loading screen
	displayLoading: function() {
		var dashboard = $('#dashboard .content');
		dashboard.html(Mustache.renderTemplate("loading", []));
	},

	displayNotImplemented: function() {
		var dashboard = $('#dashboard .content');
		dashboard.html('<p>Deze functie is nog niet geïmplementeerd</p>');
	},

	// Opent de live grafiek = grafiek in de vandaag weergave
	// element = het DOM element waarin we de grafiek willen toevoegen
	openLiveGraph: function(routeId, element, width, height) {
		var route = routes[routeId];
		var counterObject = {counter: 0, route: route, element: element, width: width, height: height}; // Referentie die we gaan meegeven
		// Deze counter voorkomt dat we openLiveGraph te snel opnieuw aanroepen als 1 van de requests klaar is
		var callback = function(){
			console.log(this.counter);
			this.counter--;
			if (this.counter == 0){
				Dashboard.openLiveGraph(this.route.id, this.element, this.width, this.height);
			}
		};

		if (!route.hasRecentAvgData(this.provider.id)){
			counterObject.counter++;
			Api.syncAvgGraph(route.id, this.provider.id, callback, counterObject);
		}
		if (!route.hasRecentLiveData(this.provider.id)){
			counterObject.counter++;
			Api.syncLiveGraph(route.id, this.provider.id, callback, counterObject);
		}
		if (counterObject.counter == 0){
			var data = {
				'Vandaag': route.liveData[this.provider.id].data,
				'Normaal': route.avgData[this.provider.id].data,
			};
			drawChart(element, data, width, height);
		}
	},

	// Genereert HTML voor live modus
	reloadLive: function() {
		if (routes.length == 0){
			Api.syncRoutes(Dashboard.reload, this);
			this.displayLoading();
			return;
		}

		var hasData = true;
		var p = this.provider.id;

		routes.forEach(function(route){
			if (!route.hasRecentAvgRepresentation(p) || !route.hasRecentLiveRepresentation(p)){
				hasData = false;
			}
		});
		if (!hasData){
			Api.syncLiveData(p, Dashboard.reload, this);
			this.displayLoading();
			return;
		}

		var dashboard = $('#dashboard .content');

		var abnormaal = '';
		var str = '';


		routes.forEach(function(route){
			var avg = route.avgData[p].representation;
			var live = route.liveData[p].representation;
			var status = route.getStatus(live, avg);

			var data = {
				id: route.id,
				name: route.name,
				description: route.description,
				status: status.text,
				color: status.color,
				live: {
					time: live.time,
					speed:  live.speed
				},
				avg: {
					time: avg.time,
					speed: avg.speed
				},
				warnings: [] // TODO: wanneer we oorzaken toevoegen moeten deze hier doorgegeven worden
			};
			if (status.color != 'green'){
				abnormaal += Mustache.renderTemplate("route", data);
			}else{
				str += Mustache.renderTemplate("route", data);
			}
		});
		var t = '';
		if (abnormaal.length > 0){
			t += '<h1>Traag verkeer</h1>'+abnormaal;

			if (str.length > 0)
				t += '<hr>';
		}
		if (str.length > 0){
			t += '<h1>Vlot verkeer</h1>'+str;
		}
		dashboard.html(t);
	},
	// Genereert HTML voor periode modus
	reloadInterval: function() {
		var dashboard = $('#dashboard .content');
		var data = {
			num: 0,
			name: this.selectedIntervals[0].getName(),
		};


		dashboard.html(Mustache.renderTemplate("period-header", data));
	},
	// Genereert HTML voor live modus
	reloadCompareIntervals: function() {
		this.displayNotImplemented();
	},
	reloadCompareDays: function() {
		this.displayNotImplemented();
	},
};

