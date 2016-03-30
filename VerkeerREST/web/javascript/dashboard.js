// De dashboard 'namespace' (vergelijk met static klasse) bevat methodes om het dashboard up te daten
// Aanroepen:
// Dashboard.reload();

var Dashboard = {

	mode: 0, // Selected mode
	provider: -1, // Selected provider

	// Mogelijke dashboard standen cte's
	LIVE: 0, // Vandaag
	INTERVAL: 1, // Periode
	COMPARE_INTERVALS: 2, // Vergelijk periodes
	COMPARE_DAYS: 3, // Vergelijk dagen

	init: function() {
		this.reload();
	},

	setMode: function(mode){
		this.mode = mode;
		this.reload();
	},

	// Herlaad het dashboard op de huidige stand
	reload: function() {
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

	// Genereert HTML voor live modus
	reloadLive: function() {
		if (routes.length == 0){
			console.log("sync routes");
			Api.syncRoutes(Dashboard.reload, this);
			this.displayLoading();
			return;
		}

		var hasData = true;
		var p = this.provider;

		routes.forEach(function(route){
			if (!route.hasRecentAvgRepresentation(p) || !route.hasRecentLiveRepresentation(p)){
				hasData = false;
			}
		});
		if (!hasData){
			console.log("sync live data");
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
				warnings: ['Ongeval']
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
		this.displayNotImplemented();
	},
	// Genereert HTML voor live modus
	reloadCompareIntervals: function() {
		this.displayNotImplemented();
	},
	reloadCompareDays: function() {
		this.displayNotImplemented();
	},
};

