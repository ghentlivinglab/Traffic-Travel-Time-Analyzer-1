/* global Mustache, Interval, Api, url, events, providers, routes, weekdays */

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
	selectedDay: null,
	filterValue: "",

	init: function() {
		this.provider = null;
		this.loadSelectedIntervals();
		this.lastKnownIntervals = [Interval.copy(this.selectedIntervals[0]), Interval.copy(this.selectedIntervals[1])];

		this.mode = this.LIVE;
		if (localStorage.getItem('mode') !== null){
			this.mode = parseInt(localStorage.getItem('mode'));
			$('#mode-'+this.mode).prop("checked", true);
		}

		if (localStorage.getItem('selectedDay') !== null){
			this.selectedDay = new Date(localStorage.getItem('selectedDay'));
		}

		this.reload();
		Api.syncProviders(this.loadProviders, this);
	},
	intervalsDidChange: function() {
                var interval = this.selectedIntervals[0];
                var param = (interval.hasName ? encodeURIComponent(interval.getName()) : "") + "," + dateToDate(interval.start) + "," + dateToDate(interval.end);
                if(this.mode===Dashboard.INTERVAL){
                    url.setQueryParams("periode",param,"vergelijkPeriode","","","dag","");
                }
                
                var interval2 = this.selectedIntervals[1];
                if (this.mode===Dashboard.COMPARE_INTERVALS) {
                    var param2 = (interval2.hasName ? encodeURIComponent(interval2.getName()) : "") + "," + dateToDate(interval2.start) + "," + dateToDate(interval2.end);
                    url.setQueryParams("periode",param,"vergelijkPeriode",param2,"dag","");
                }
                
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
	setSelectedDay: function(date){
		this.selectedDay = date;
		localStorage.setItem('selectedDay', date.toJSON());
		this.dayDidChange();
	},
	dayDidChange: function() {
		this.reload();
                url.setQueryParams("periode","","vergelijkPeriode","","dag",dateToDate(this.selectedDay));
	},

	filterChanged: function(){
        var filterInput = $("#filterInput");
        this.filterValue = filterInput.val();
        url.setQueryParam("filter",encodeURIComponent(this.filterValue));

        this.updateFilter();
    },

    updateFilter: function() {
    	$('.route').each(function() {
        	var routeId = $(this).attr('data-route');
        	if (typeof routeId != "undefined") {
        		var route = routes[routeId];
        		if (Dashboard.routeSatisfiesFilter(route)) {
        			$(this).show();
        		} else {
        			$(this).hide();
        		}
        	}
        });
    },

	saveSelectedIntervals: function() {
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
	
	loadSelectedIntervals: function() {
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
			case Dashboard.DAY: 
				this.reloadDay(); 
			break;
			default:
				this.displayNotImplemented();
			break;
		}
		
		this.updateFilter();
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
		if (this.mode == this.DAY){
			this.openDayGraph(this.selectedDay, routeId, element, width, height);
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
				'Gemiddelde': route.avgData[this.provider.id].data,
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
			var graph = route.getIntervalData(interval, day, this.provider.id);
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
	// Opent de grafiek horende bij 1 interval (met weekdagen etc)
	openDayGraph: function(day, routeId, element, width, height) {
		if (typeof day == "undefined" || day === null) {
			return;
		}

		var route = routes[routeId];

		var callback = function(){
			Dashboard.openDayGraph(day, routeId, element, width, height);
		};

		var graph = route.getDayData(day, this.provider.id);
		if (graph === null || graph.data === null){
			Api.syncDayGraph(day, routeId, this.provider.id, callback, this);
			return;
		}
		var data = {};
		data[dateToDate(day)] = graph.data;
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

		var graph = route.getIntervalData(interval0, 7, this.provider.id);
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
		var p = this.provider.id;

		var hasData = this.routesDoHaveData(function(route) {
			return route.hasRecentAvgRepresentation(p) && route.hasRecentLiveRepresentation(p);
		});

		var dashboard = $('#dashboard .content');
		var str = this.renderHeader("live", {});

		if (!hasData){
			Api.syncLiveData(p, Dashboard.reload, this);
			str += Mustache.renderTemplate("loading", []);
			dashboard.html(str);
			return;
		}

        var builder = ListBuilder.create();
        builder.setLeft(ListBuilder.DEFAULT_REPRESENTATION, function(route) {
        	if (!route.hasRecentLiveRepresentation(p)) {
        		return null;
        	}
        	return route.liveData[p].representation;
        });

        builder.setRight(ListBuilder.AVERAGE_REPRESENTATION, function(route) {
        	if (!route.hasRecentAvgRepresentation(p)) {
        		return null;
        	}
        	return route.avgData[p].representation;
        });

        builder.setStatusFunction(function(route, liveData, avgData) {
        	if (route.isExceptional()) {
        		return {
        			name: 'Abnormaal verkeer',
        			index: 10
        		};
        	}
        	return {
    			name: 'Gewoonlijk verkeer',
    			index: 0
    		};
        });

        builder.setSortIndexFunction(function(route, liveData, avgData) {
        	return route.speedLimit / Math.max(0.01,liveData.speed);
        });

		str += builder.render();
		dashboard.html(str);


		//Bepaald hoe oud deze live data is.
		var lastupdated = routes[1].liveData[5].representation.timestamp;
		for(var i=1; i<routes.length; ++i){
			if(routes[i].liveData[5].representation.createdOn > lastupdated){
				lastupdated = routes[i].liveData[5].representation.timestamp;
			}
		}
		var diffMins = Math.round(((( new Date()-lastupdated ) / 1000 ) / 60 ) );
		var lu_str = "(updated " + diffMins + " minutes ago)"
		$('#lu').html(lu_str);
	},
	// Genereert HTML voor periode modus
	reloadDay: function() {
		console.log("reload day");
		var dashboard = $('#dashboard .content');
		var day = this.selectedDay;

		// Opgegeven interval checken
		if (day === null || typeof day == "undefined"){

			var str = this.renderHeader("day", {day: ''});
			str += "<p>Selecteer een dag.</p>";
			dashboard.html(str);
			return;
		} else {
			var dayString = dateToDate(day);
			var str = this.renderHeader("day", {day: dayString});

			// Hebben we alle benodigde data? 
			// Dat is: de representatie van elke periode + het gemiddelde van de afgelopen maand
			var p = this.provider.id;
			var hasData = this.routesDoHaveData(function(route) {
				return route.getDayData(day, p) !== null;
			});

			if (!hasData){
				Api.syncDayData(day, p, Dashboard.reload, this);

				str += Mustache.renderTemplate("loading", []);
				dashboard.html(str);
				return;
			}
		}
		// Als alles in orde is: resultaat tonen

		str += "<p>Resultaat voor dag: "+ dayString +"</p>";

		var builder = ListBuilder.create();
        builder.setLeft(ListBuilder.DAY_REPRESENTATION, function(route) {
        	if (route.getDayData(day, p) === null) {
        		return null;
        	}
        	return route.getDayData(day, p).representation;
        });

        builder.setStatusFunction(function(route, dayData) {
        	return route.getStatusFor(dayData);
        });

        builder.setSortIndexFunction(function(route, dayData) {
        	return -dayData.speed;
        });

		str += builder.render();
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

		var str = this.renderHeader("period", { 'period-selection': period_selection});
		
		// Opgegeven interval checken
		if (interval.isEmpty()){
			str += "<p>Selecteer een reeds opgeslagen periode of kies zelf een bereik.</p>";
			dashboard.html(str);
			return;
		}else{
			if (interval.isValid()){
				// Hebben we alle benodigde data? 
				// Dat is: de representatie van elke periode + het gemiddelde van de afgelopen maand
				var p = this.provider.id;
				var hasData = this.routesDoHaveData(function(route) {
					return route.getIntervalDataRepresentation(interval, 7, p);
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

		var builder = ListBuilder.create();
        builder.setLeft(ListBuilder.INTERVAL_REPRESENTATION, function(route) {
        	return route.getIntervalDataRepresentation(interval, 7, p);
        });
        builder.setRight(ListBuilder.UNUSUAL_REPRESENTATION, function(route) {
        	return route.getIntervalDataRepresentation(interval, 7, p);
        });

        builder.setStatusFunction(function(route, dayData) {
        	return route.getStatusFor(dayData);
        });

        builder.setSortIndexFunction(function(route, dayData) {
        	return -dayData.speed;
        });

		str += builder.render();
		dashboard.html(str);
	},
	// Genereert HTML voor live modus
	reloadCompareIntervals: function() {
		var dashboard = $('#dashboard .content');
		var interval0 = this.selectedIntervals[0];
		var data = {
			num: 0,
			name: interval0.getName()
		};
		var period_selection0 = Mustache.renderTemplate("period-selection", data);

		var interval1 = this.selectedIntervals[1];
		var data = {
			num: 1,
			name: interval1.getName()
		};
		var period_selection1 = Mustache.renderTemplate("period-selection", data);

		var str = this.renderHeader('compare', { 'period-selection0': period_selection0, 'period-selection1': period_selection1})

		if (interval0.isEmpty() || interval1.isEmpty()){
			str += "<p>Selecteer twee reeds opgeslagen periodes of kies zelf een bereik.</p>";
			dashboard.html(str);
			return;
		}else{
			if (interval0.isValid() && interval1.isValid()){
				// Hebben we alle benodigde data? 
				// Dat is: de representatie van elke periode + het gemiddelde van de afgelopen maand
				var p = this.provider.id;

				var hasData0 = this.routesDoHaveData(function(route) {
					return route.getIntervalDataRepresentation(interval0, 7, p);
				});
				var hasData1 = this.routesDoHaveData(function(route) {
					return route.getIntervalDataRepresentation(interval1, 7, p);
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

		var builder = ListBuilder.create();
        builder.setLeft(ListBuilder.INTERVAL_REPRESENTATION, function(route) {
        	return route.getIntervalDataRepresentation(interval0, 7, p);
        });

        builder.setRight(ListBuilder.INTERVAL_REPRESENTATION, function(route) {
        	return route.getIntervalDataRepresentation(interval1, 7, p);
        });

        builder.setStatusFunction(function(route, representation0, representation1) {
        	var diff = representation0.speed - representation1.speed;
			var t = {
				name: 'Slechter',
				index: 0
			};
			if (diff > 0){
				t = {
					name: 'Verbeterd',
					index: 2
				};
			}
			if (Math.abs(diff) < 5){
				t = {
					name: 'Gelijk',
					index: 1
				};
			}
        	return t;
        });

        builder.setSortIndexFunction(function(route, representation0, representation1) {
        	var diff = representation0.speed - representation1.speed;
        	return diff;
        });

		str += builder.render();
		dashboard.html(str);
	},

    routeSatisfiesFilter: function(route){
        var filter = this.filterValue.trim().split(" ");
        for(var i = 0;i<filter.length;i++){
            if(route.name.toLowerCase().includes(filter[i].toLowerCase()) 
                    || route.getDescription().toLowerCase().includes(filter[i].toLowerCase())){
                return true;
            }
        }
        return false;
    },
    selectFilterInput: function(currentPos){
        var input = $("#dashboard #filterInput");
        input[0].selectionStart = input[0].selectionEnd = currentPos;
        input.focus();
    },

    renderHeader: function(headerName, data) {
    	data.search = Mustache.renderTemplate("search", {filter: this.filterValue});
    	return Mustache.renderTemplate("header-"+headerName, data);
    },


    // Functies die hergebruikt moeten worden, en dup code voorkomen

    /**
		Controleert voor elke route of een conditie waar is. En returnt 
		true als voor ten minste 1 route deze conditie waar is. Conditie 
		wordt doorgegeven als een functie in het argument. Deze functie
		wordt uitgeroepen als een route (dus this = route die gecontrolleerd
		 moet worden)
		arguments:
		 * check: function(route){...}
		return: boolean true/false
    */
    routesDoHaveData: function(check) {
    	if (typeof check != "function") {
    		console.error("Dashboard.routesDoHaveData expects a function for parameter 'check'");
    		return false;
    	}
		for (var i = routes.length - 1; i >= 0; i--) {
			var route = routes[i];
			if (typeof route == "undefined") {
				continue;
			}
			if (check(route)){
				return true;
			}
		}
		return false;
    }

};

