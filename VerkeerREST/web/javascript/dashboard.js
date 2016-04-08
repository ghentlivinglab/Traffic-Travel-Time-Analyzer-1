// De dashboard 'namespace' (vergelijk met static klasse) bevat methodes om het dashboard up te daten
// Aanroepen:
// Dashboard.reload();

var Dashboard = {

	mode: null, // Selected mode
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


	init: function() {
		this.provider = null;
		this.lastKnownIntervals = [Interval.copy(this.selectedIntervals[0]), Interval.copy(this.selectedIntervals[1])];

		this.mode = this.LIVE;

		this.reload();
		Api.syncProviders(this.loadProviders, this);
	},

	intervalsDidChange: function() {
		var changed = false;
		for (var i = 0; i < this.selectedIntervals.length; i++) {
			var sel = this.selectedIntervals[i];
			var lk = this.lastKnownIntervals[i];

			if (!lk.equals(sel)){
				changed = true;
			}

			this.lastKnownIntervals[i] = Interval.copy(sel);
		}

		if (changed) {
			this.reload();
		}
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
		//var counterObject = {counter: 0, route: route, element: element, width: width, height: height}; // Referentie die we gaan meegeven
		// Deze counter voorkomt dat we openLiveGraph te snel opnieuw aanroepen als 1 van de requests klaar is
		var callback = function(){
			Dashboard.openLiveGraph(routeId, element, width, height);
		};
		var c = 0;

		if (!route.hasRecentAvgData(this.provider.id)){
			c++;
		}
		if (!route.hasRecentLiveData(this.provider.id)){
			c++;
		}
		if (c == 0){
			var data = {
				'Vandaag': route.liveData[this.provider.id].data,
				'Normaal': route.avgData[this.provider.id].data,
			};
			drawChart(element, data, width, height);
		}else{
			Api.newQueue(c);
			if (!route.hasRecentAvgData(this.provider.id)){
				Api.syncAvgGraph(route.id, this.provider.id, callback, this);
			}
			if (!route.hasRecentLiveData(this.provider.id)){
				Api.syncLiveGraph(route.id, this.provider.id, callback, this);
			}
			Api.endQueue();
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
		var str = '';

		// Dit stuk code sorteert de resultaten van alle routes en voegt ze toe aan de html
		// Met de juiste Mustache template
		var dataArr = [];
		routes.forEach(function(route){
			var avg = route.avgData[p].representation;
			var live = route.liveData[p].representation;
			var status = route.getStatus(live, avg);

			// Data voor in de template
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
			dataArr.push(data);
		});

		dataArr.sort(function(a, b) {
			// Nog sorteren op status op eerste plaats toeveogen hier
			return a.live.speed/a.avg.speed - b.live.speed/b.avg.speed;
		});

		// Juiste subtitels (en evt lijn) ondertussen toevoegen
		var lastStatus = '';
		dataArr.forEach(function (data){
			if (lastStatus != data.status){
				if (lastStatus != ''){
					str += '<hr>';
				}
				lastStatus = data.status;
				str += "<h1>"+lastStatus+"</h1>";
			}
			str += Mustache.renderTemplate("route", data);
		});

		dashboard.html(str);
	},
	// Genereert HTML voor periode modus
	reloadInterval: function() {
		var dashboard = $('#dashboard .content');
		var interval = this.selectedIntervals[0];
		var data = {
			num: 0,
			name: this.selectedIntervals[0].getName(),
		};
		var period_selection = Mustache.renderTemplate("period-selection", data);

		var str = Mustache.renderTemplate("period-header", { 'period-selection': period_selection});

		
		// Opgegeven interval checken
		if (interval.isEmpty()){
			str += "<p>Selecteer een reeds opgeslagen periode of kies zelf een bereik.</p>";
			dashboard.html(str);
			return;
		}else{
			if (interval.isValid()){
				// Hebben we alle benodigde data? 
				// Dat is: de representatie van elke periode + het gemiddelde van de afgelopen maand
				var hasData = true;
				var hasAvg = true;

				var p = this.provider.id;

				routes.forEach(function(route){
					if (!route.getIntervalDataRepresentation(interval, 7, p)){
						hasData = false;
					}
					if (!route.hasRecentAvgRepresentation(p)){
						hasAvg = false;
					}
				});

				// Stuk code die we nodig hebben om de Api queue te laten werken
				// Deze houdt bij hoeveel requests er nog voltooid moeten worden voor we
				// een callback krijgen. 
				// we krijgen dus geen callback per request, maar enkel als ze allemaal klaar zijn
				var c = 0;
				if (!hasData){
					c++;
				}
				if (!hasAvg){
					c++;
				}

				if (c != 0){
					Api.newQueue(c);
					if (!hasData)
						Api.syncIntervalData(interval, p, Dashboard.reload, this);
					if (!hasAvg)
						Api.syncLiveData(p, Dashboard.reload, this);
					Api.endQueue();

					str += Mustache.renderTemplate("loading", []);
					dashboard.html(str);
					return;
				}
			}else{
				str += "<p>Het opgegeven bereik is niet volledig/ongeldig.</p>";
				dashboard.html(str);
				return;
			}
		}
		// Als alles in orde is: resultaat tonen

		str += "<p>Resultaat voor periode: "+ dateToDate(interval.start) +" tot "+dateToDate(interval.end) +"</p>";
					
		// Dit stuk code sorteert de resultaten van alle routes en voegt ze toe aan de html
		// Met de juiste Mustache template
		var dataArr = [];
		routes.forEach(function(route){
			var representation = route.getIntervalDataRepresentation(interval, 7, p);
			var avg = route.avgData[p].representation;
			var status = route.getStatus(representation, avg);

			var data = {
				id: route.id,
				name: route.name,
				description: route.description,
				status: status.text,
				color: status.color,
				live: {
					time: representation.time,
					speed:  representation.speed
				},
				avg: {
					time: avg.time,
					speed: avg.speed
				},
				warnings: ['Geen waarschuwingen'] // TODO: wanneer we oorzaken toevoegen moeten deze hier doorgegeven worden
			};

			dataArr.push(data);
		});

		dataArr.sort(function(a, b) {
			// Nog sorteren op status op eerste plaats toeveogen hier
			return a.live.speed/a.avg.speed - b.live.speed/b.avg.speed;
		});

		// Juiste subtitels (en evt lijn) ondertussen toevoegen
		var lastStatus = '';
		dataArr.forEach(function (data){
			if (lastStatus != data.status){
				if (lastStatus != ''){
					str += '<hr>';
				}
				lastStatus = data.status;
				str += "<h1>"+lastStatus+"</h1>";
			}
			str += Mustache.renderTemplate("route", data);
		});

		dashboard.html(str);
	},
	// Genereert HTML voor live modus
	reloadCompareIntervals: function() {
		this.displayNotImplemented();
	},
	reloadCompareDays: function() {
		this.displayNotImplemented();
	},
};

