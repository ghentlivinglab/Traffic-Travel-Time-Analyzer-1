// De dashboard 'namespace' (vergelijk met static klasse) bevat methodes om het dashboard up te daten
// Aanroepen:
// Dashboard.reload();

var Dashboard = {

	mode: 0,
	// Mogelijke dashboard standen cte's
	LIVE: 0, // Vandaag
	INTERVAL: 1, // Periode
	COMPARE_INTERVALS: 2, // Vergelijk periodes
	COMPARE_DAYS: 3, // Vergelijk dagen

	init: function() {
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
			console.log("syncRoutes");
			Api.syncRoutes(Dashboard.reload, this);
			this.displayLoading();
			return;
		}
		console.log("render");
		var dashboard = $('#dashboard .content');
		var str = '';
		routes.forEach(function(route){
			var data = {
				name: route.name,
				description: route.description,
				status: 'Vlot verkeer',
				live: {
					time: 12,
					speed: 78
				},
				avg: {
					time: 12,
					speed: 78
				},
				warnings: ['Ongeval']
			};
			str += Mustache.renderTemplate("route", data);
		});
		dashboard.html(str);
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

