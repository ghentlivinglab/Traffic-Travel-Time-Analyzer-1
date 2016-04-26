// De dashboard 'namespace' (vergelijk met static klasse) bevat methodes om het dashboard up te daten
// Aanroepen:
// Dashboard.reload();

var Dashboard = {

	mode: null, // Selected mode
	provider: null, // Selected provider

	// Mogelijke dashboard standen cte's
	LIVE: 0, // Vandaag
	INTERVAL: 2, // Periode
	COMPARE_INTERVALS: 3, // Vergelijk periodes
	DAY: 1, // Vergelijk dagen

	// Geselecteerde intervallen en datums. Ofwel Interval ofwel Event objecten
	selectedIntervals: [Interval.create(null, null), Interval.create(null, null)],

	// Om de veranderingen van de selectedIntervals waar te nemen NA dat de popover is gesloten
	// (hierdoor ontvangen we niet voortdurend change events) gebruiken we nog een extra
	// property die de laatst gebruikte intervallen opslaat
	lastKnownIntervals: [],
	initialSync: false,


	init: function() {
		this.provider = null;
		this.loadSelectedIntervals();
		this.lastKnownIntervals = [Interval.copy(this.selectedIntervals[0]), Interval.copy(this.selectedIntervals[1])];

		this.mode = this.LIVE;
		if (localStorage.getItem('mode') !== null){
			this.mode = parseInt(localStorage.getItem('mode'));
			$('#mode-'+this.mode).prop("checked", true);
		}

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
		this.saveSelectedIntervals();
	},

	saveSelectedIntervals: function () {
		var selected_intervals = {}; // number of selection -> object data
		var selected_events = {}; // name -> number of selection
		for (var num in this.selectedIntervals){
			var interval = this.selectedIntervals[num];
			if (this.selectedIntervals[num].hasName){
				if (typeof selected_events[interval.name] == "undefined"){
					selected_events[interval.name] = [];
				}
				selected_events[interval.name].push(num);
			} else {
				selected_intervals[num] = interval;
			}
		}
		localStorage.setItem('selected_intervals', JSON.stringify(selected_intervals));
		localStorage.setItem('selected_events', JSON.stringify(selected_events));
	},
	loadSelectedIntervals: function () {
		try {
			var selected_intervals = JSON.parse(localStorage.getItem('selected_intervals')); // number of selection -> object data
			var selected_events = JSON.parse(localStorage.getItem('selected_events')); // name -> number of selection
			
			for (var num in selected_intervals){
				var interval = Interval.createFromStorage(selected_intervals[num]);
				// dates juist zetten
				this.selectedIntervals[num] = interval;
			}

			for (var name in selected_events){
				var nums = selected_events[name];

				var index = getEventIndex(name);
				if (index != -1){
					for (var i = 0; i < nums.length; i++) {
						var num = nums[i];
						this.selectedIntervals[num] = events[index];
					}
				}
			}

		}catch (e) {
			// niets doen
		}
	},

	loadProviders: function() {
		var str = '';

		if (localStorage.getItem('provider') !== null){
			this.setProvider(localStorage.getItem('provider'));
		}

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
		localStorage.setItem('mode', this.mode);
		this.reload();
	},

	setProvider: function(providerId){
		if (typeof providers[providerId] != "undefined"){
			var reload = false;
			if (!this.provider || this.provider.id != providerId) {
				reload = true;
			}
			this.provider = providers[providerId];
			localStorage.setItem('provider', this.provider.id);
			if (reload) {
				this.reload();
			}
		} else {
			console.error('No provider found with id '+providerId);
		}
	},
	
	// Herlaad het dashboard op de huidige stand
	reload: function() {
		if (!this.provider){
			return;
		}
		if (routes.length == 0){
			Api.syncRoutes(Dashboard.reload, this);
			this.displayLoading();
			return;
		}

		if (!this.initialSync){
			this.initialSync = true;
			Api.syncLiveData(this.provider.id, Dashboard.reload, this);
		}
		var dashboard = $('#dashboard .content');

		// Alles wissen (dit kan later weg, maar is om te voorkomen dat thisReady meerdere keren wordt uitgevoerd op dezelfde elementen)
		// Als alles juist geprogrammeerd is, dan zal het dashboard nooit leeg zijn.
		// Dus kan dit deeltje later weg. Maar voorlopig niet, zodat we deze oorzaak snel zien (dashboard zal leeg zijn). 
		// (zie ook commentaar bij thisReady hieronder)
		dashboard.html('');
                url.setQueryParam("Weergave",this.mode);
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
			default:
				this.displayNotImplemented();
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

	openGraph: function(routeId, element, width, height) {
		if (this.mode == this.LIVE){
			this.openLiveGraph(routeId, element, width, height);
		}
		if (this.mode == this.INTERVAL){
			this.openIntervalGraph(this.selectedIntervals[0], routeId, element, width, height);
		}
		if (this.mode == this.COMPARE_INTERVALS){
			this.openCompareGraph(this.selectedIntervals[0], this.selectedIntervals[1], routeId, element, width, height);
		}

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
			drawChart(element, data, width, height, true);
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
	// Opent de grafiek horende bij 1 interval (met weekdagen etc)
	openIntervalGraph: function(interval, routeId, element, width, height) {
		var route = routes[routeId];

		var callback = function(){
			Dashboard.openIntervalGraph(interval, routeId, element, width, height);
		};

		var okay = true;
		var data = {};
		for (var day = 0; day < 7; day++) {
			var graph = route.getIntervalData(interval, day, this.provider.id)
			if (!graph || !graph.data){
				okay = false;
				break;
			}else{
				data[weekdays[day]] = graph.data;
			}	
		}

		if (!okay) {
			Api.syncIntervalGraph(interval, routeId, this.provider.id, callback, this);
			return;
		}

		drawChart(element, data, width, height);
	},

	// Opent de grafiek horende bij 2 intervallen
	openCompareGraph: function(interval0, interval1, routeId, element, width, height) {
		var route = routes[routeId];

		var callback = function(){
			Dashboard.openCompareGraph(interval0, interval1, routeId, element, width, height);
		};

		var okay0 = true;
		var data = {};
		var c = 0;

		var graph = route.getIntervalData(interval0, 7, this.provider.id)
		if (!graph || !graph.data){
			okay0 = false;
			c++;
		}else{
			data[interval0.getName()] = graph.data;
		}

		graph = route.getIntervalData(interval1, 7, this.provider.id)
		if (!graph || !graph.data){
			okay1 = false;
			c++;
		}else{
			data[interval1.getName()] = graph.data;
		}	

		if (c > 0) {
			Api.newQueue(c);

			if (!okay0)
				Api.syncIntervalGraph(interval0, routeId, this.provider.id, callback, this);

			if (!okay1)
				Api.syncIntervalGraph(interval1, routeId, this.provider.id, callback, this);

			Api.endQueue();

			return;
		}

		drawChart(element, data, width, height);
	},

	//syncIntervalGraph

	// Genereert HTML voor live modus
	reloadLive: function() {
		var hasData = false;
		var p = this.provider.id;

		routes.forEach(function(route){
			if (route.hasRecentAvgRepresentation(p) && route.hasRecentLiveRepresentation(p)){
				hasData = true;
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

			if (!route.hasRecentAvgRepresentation(p) || !route.hasRecentLiveRepresentation(p)){
				var data = {
					id: route.id,
					name: route.name,
					description: route.getDescription(),
					length: route.getLength(),
					status: '',
					color: '',
					score: 100000000, // Voor sorteren
					title: 'Niet beschikbaar',
					subtitle: 'Geen data van deze provider over deze route',
					warnings: [] // TODO: wanneer we oorzaken toevoegen moeten deze hier doorgegeven worden
				};
				dataArr.push(data);
				return;
			}

			var avg = route.avgData[p].representation;
			var live = route.liveData[p].representation;
			var status = route.getStatus(live, avg);

			// Data voor in de template
			var data = {
				id: route.id,
				name: route.name,
				description: route.getDescription(),
				length: route.getLength(),
				status: status.text,
				color: status.color,
				score: live.speed, // Voor sorteren
				title: live.toString(),
				subtitle: avg.toString(),
				warnings: route.getWarnings(live, avg) // TODO: wanneer we oorzaken toevoegen moeten deze hier doorgegeven worden
			};
			dataArr.push(data);
		});

		dataArr.sort(function(a, b) {
			if (a.status != b.status){
				if (a.color == "red"){
					return -1;
				}
				if (b.color == "red"){
					return 1;
				}
				if (a.color == "orange"){
					return -1;
				}
				if (b.color == "orange"){
					return 1;
				}
				if (a.color == "green"){
					return -1;
				}
				if (b.color == "green"){
					return 1;
				}
			}
			// Nog sorteren op status op eerste plaats toeveogen hier
			return a.score - b.score;
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
				var hasData = false;

				var p = this.provider.id;

				routes.forEach(function(route){
					if (route.getIntervalDataRepresentation(interval, 7, p)){
						hasData = true;
						// TODO: Loop kan hier eig stoppen (omzetten in for loop)
					}
				});

				if (!hasData){
					Api.syncIntervalData(interval, p, Dashboard.reload, this);

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
			if (!route.getIntervalDataRepresentation(interval, 7, p) || route.getIntervalDataRepresentation(interval, 7, p).empty){
				var data = {
					id: route.id,
					name: route.name,
					description: route.getDescription(),
					length: route.getLength(),
					status: 'Niet beschikbaar',
					color: 'gray',
					score: 10000, // Voor sorteren
					title: '',
					subtitle: 'Geen data van deze provider over deze route',
					warnings: [] // TODO: wanneer we oorzaken toevoegen moeten deze hier doorgegeven worden
				};
				dataArr.push(data);
				return;
			}
			var representation = route.getIntervalDataRepresentation(interval, 7, p);
			var status = representation.getStatus();

			var data = {
				id: route.id,
				name: route.name,
				description: route.getDescription(),
				length: route.getLength(),
				status: status.text,
				color: status.color,
				score: representation.speed, // Voor sorteren
				title: representation.toString(),
				subtitle: representation.getSubtitle(),
				warnings: [] // TODO: wanneer we oorzaken toevoegen moeten deze hier doorgegeven worden
			};

			dataArr.push(data);
		});

		dataArr.sort(function(a, b) {
			// Nog sorteren op status op eerste plaats toeveogen hier
			return a.score - b.score;
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
		var dashboard = $('#dashboard .content');
		var interval0 = this.selectedIntervals[0];
		var data = {
			num: 0,
			name: interval0.getName(),
		};
		var period_selection0 = Mustache.renderTemplate("period-selection", data);

		var interval1 = this.selectedIntervals[1];
		var data = {
			num: 1,
			name: interval1.getName(),
		};
		var period_selection1 = Mustache.renderTemplate("period-selection", data);


		var str = Mustache.renderTemplate("compare-header", { 'period-selection0': period_selection0, 'period-selection1': period_selection1});

		if (interval0.isEmpty() || interval1.isEmpty()){
			str += "<p>Selecteer twee reeds opgeslagen periodes of kies zelf een bereik.</p>";
			dashboard.html(str);
			return;
		}else{
			if (interval0.isValid() && interval1.isValid()){
				// Hebben we alle benodigde data? 
				// Dat is: de representatie van elke periode + het gemiddelde van de afgelopen maand
				var hasData0 = false;
				var hasData1 = false;

				var p = this.provider.id;

				routes.forEach(function(route){
					if (route.getIntervalDataRepresentation(interval0, 7, p)){
						hasData0 = true;
					}
					if (route.getIntervalDataRepresentation(interval1, 7, p)){
						hasData1 = true;
					}
				});

				var c = 0;
				if (!hasData0){
					c++;
				}
				if (!hasData1){
					c++;
				}

				if (c > 0){
					Api.newQueue(c);

					if (!hasData0)
						Api.syncIntervalData(interval0, p, Dashboard.reload, this);

					if (!hasData1)
						Api.syncIntervalData(interval1, p, Dashboard.reload, this);

					Api.endQueue();

					str += Mustache.renderTemplate("loading", []);
					dashboard.html(str);
					return;
				}
			}else{
				str += "<p>De opgegeven bereiken zijn niet volledig/ongeldig.</p>";
				dashboard.html(str);
				return;
			}
		}
		str += "<p>Resultaat voor periode: "+ dateToDate(interval0.start) +" tot "+dateToDate(interval0.end) +' en '+ dateToDate(interval1.start) +" tot "+dateToDate(interval1.end) +"</p>";

		var dataArr = [];

		routes.forEach(function(route){
			if (!route.getIntervalDataRepresentation(interval0, 7, p) || !route.getIntervalDataRepresentation(interval1, 7, p) || route.getIntervalDataRepresentation(interval1, 7, p).empty || route.getIntervalDataRepresentation(interval0, 7, p).empty){
				var data = {
					id: route.id,
					name: route.name,
					description: route.getDescription(),
					length: route.getLength(),
					status: 'Niet beschikbaar',
					score: 10000000,
					first: {
						status: 'Niet beschikbaar',
						color:  'gray',
						title: '',
						subtitle: 'Deze route is niet beschikbaar in deze provider.',
					},
					second: {
						status: '',
						color: '',
						title: '',
						subtitle: '',
					}
				};

				dataArr.push(data);
				return;
			}

			var representation0 = route.getIntervalDataRepresentation(interval0, 7, p);
			var representation1 = route.getIntervalDataRepresentation(interval1, 7, p);

			var status0 = representation0.getStatus();
			var status1 = representation1.getStatus();

			var diff = representation0.speed - representation1.speed;

			var t = 'Slechter';
			if (diff > 0){
				t = 'Verbeterd';
			}
			if (Math.abs(diff) < 5){
				t = 'Gelijk';
			}
			

			var data = {
				id: route.id,
				name: route.name,
				description: route.getDescription(),
				length: route.getLength(),
				status: t,
				statusScore: diff,
				score: diff, // Sorteren op grootste verschillen
				first: {
					status: status0.text,
					color: status0.color,
					title: representation0.toString(),
					subtitle: representation0.getSubtitle(),
				},
				second: {
					status: status1.text,
					color: status1.color,
					title: representation1.toString(),
					subtitle: representation1.getSubtitle(),
				}
			};

			dataArr.push(data);
		});

		dataArr.sort(function(a, b) {
			if (a.status == b.status) {
				return a.score - b.score;
			}
			// Nog sorteren op status op eerste plaats toeveogen hier
			return a.score - b.score;
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
			str += Mustache.renderTemplate("compare", data);
		});

		dashboard.html(str);
	},
	reloadCompareDays: function() {
		this.displayNotImplemented();
	},
};

