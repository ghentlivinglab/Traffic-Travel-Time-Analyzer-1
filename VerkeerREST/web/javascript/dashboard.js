/* global Mustache, Interval, Api, url, events, providers, routes, weekdays */

// De dashboard 'namespace' (vergelijk met static klasse) bevat methodes om het dashboard up te daten
// Aanroepen:
// Dashboard.reload();

var Dashboard = {

	mode: null, // Selected mode
	provider: null, // Selected provider

	// Eeen lijst met extra providers die geselecteerd zijn bij een grafiek
	// Wordt telkens gereset als een grafiek wordt geopened
	extraProviders: [],

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
		console.log("dashboard.init");
		this.provider = null;
		this.loadSelectedIntervals();
		this.lastKnownIntervals = [Interval.copy(this.selectedIntervals[0]), Interval.copy(this.selectedIntervals[1])];
		this.mode = this.LIVE;
		if (localStorage.getItem('mode') !== null){
			this.mode = parseInt(localStorage.getItem('mode'));
			$('#mode-'+this.mode).prop("checked", true);
		}
		url.changeViewByParam();

		// Aanroepen als de mode juist staat
		url.changePeriodByParam();
		url.changeComparePeriodByParam();

		if (localStorage.getItem('selectedDay') !== null){
			this.selectedDay = new Date(localStorage.getItem('selectedDay'));
		}
		url.changeDayByParam();

		this.reload();

		if (!Provider.loadProviders()) {
			Api.syncProviders(this.loadProviders, this);
		} else {
			this.loadProviders();
		}
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
			url.setIntervals(this.selectedIntervals[0],this.selectedIntervals[1]);
			this.reload();
		}
		this.saveSelectedIntervals();
	},
	setSelectedDay: function(date){
		if (!this.selectedDay || this.selectedDay.getTime() !== date.getTime()) {
			this.selectedDay = date;
			localStorage.setItem('selectedDay', date.toJSON());
			this.dayDidChange();
		}
	},
	dayDidChange: function() {
		this.reload();
                url.setQueryParams("dag",dateToDate(this.selectedDay));
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

		url.changeProviderByParam();

		if (!this.provider && localStorage.getItem('provider') !== null){
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

		url.setQueryParam("weergave",this.mode);
                url.setIntervals(this.selectedIntervals[0],this.selectedIntervals[1]);
                if(this.mode === Dashboard.DAY){
                    url.setQueryParam("dag",dateToDate(this.selectedDay));
                } else {
                    url.setQueryParam("dag","");
                }

		openDashboard();
	},

	setProviderName: function(providerName) {
		console.log("set provider name "+providerName);
		for (var id in providers) {
			var provider = providers[id];
			if (provider.getUrlString().toLowerCase() == providerName.toLowerCase()) {
				this.setProvider(id);
				return true;
			}
		}
		return false;
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
            	// Noodzakelijk, omdat de geselecteerde provider nooit in extraProviders mag zitten
            	Dashboard.removeExtraProvider(this.provider.id);

                if (this.mode != this.LIVE) {
                	var p = this.provider.id;
                	var hasData = this.routesDoHaveData(function(route) {
						return route.hasRecentAvgRepresentation(p) && route.hasRecentLiveRepresentation(p);
					});

					if (!hasData){
						Api.syncLiveData(p, function() {}, this); // Dashboard niet reloaden -> niet live
					} else {
						reloadMap();
					}
	            } else {
					reloadMap();
				}

				this.reload();

				// Waarom name? -> veel duidelijker voor de gebruiker
				url.setQueryParam("provider",this.provider.getUrlString());
            }
		} else {
			console.error('No provider found with id '+providerId);
		}
	},

	forceLiveReload: function() {
		Api.syncLiveData(this.provider.id, Dashboard.reload, this);
	},

	// Herlaad het dashboard op de huidige stand
	reload: function() {
		if (!this.provider){
			return;
		}
		if (routes.length == 0){
			// Laden uit local storage
			if (!Route.loadRoutes()) {
				// Nog niet in local storage -> ophalen
				Api.syncRoutes(Dashboard.reload, this);
				this.displayLoading();
				return;
			}
		}

		if (!this.initialSync){
			this.initialSync = true;
			//Api.syncLiveData(this.provider.id, Dashboard.reload, this);
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

	// Geeft een HTML string terug om extra providers te selecteren bij een grafiek
	// Met de providers die nog niet geselecteerd zijn
	extraProvidersList: function() {
		var str = '';
		for(var i in providers) {
			var provider = providers[i];
			if (provider.id != this.provider.id && !this.isExtraProvider(provider.id)) {
				str += '<a class="extra-provider" href="#" onclick="addExtraProvider.call(this);" data-provider="'+ provider.id +'">'+provider.name+'</a>';
			}
		}

		var extraStr = '';
		for(var i in this.extraProviders) {
			var provider = providers[this.extraProviders[i]];
			extraStr += '<a class="extra-provider remove" href="#" onclick="removeExtraProvider.call(this);" data-provider="'+ provider.id +'">'+provider.name+'</a>';
		}

		return '<div class="select-extra-providers"><div class="description">Extra providers tonen</div>'+ str + extraStr + '</div>';
	},

	// Geeft aan of een provider id gemarkeerd is als extra provider in een grafiek
	// True / false
	isExtraProvider: function(providerid) {
		for(var j in this.extraProviders) {
			if (this.extraProviders[j] == providerid) {
				return true;
			}
		}
		return false;
	},

	removeExtraProvider: function(providerId) {
		for (var i = 0; i < this.extraProviders.length; i++) {
	        if (this.extraProviders[i] == providerId) {
	            this.extraProviders.splice(i, 1);
	            break;
	        }
	    }
	},

	// Extraprovider bevat een provider die moet worden toegevoegd (= toevoegen provider na openen grafiek)
	// aan extraProviders. Indien niet opgegeven: geen actie (= openen nieuwe grafiek)
	// Indien null: extraProviders reset (= providers wissen)
	openGraph: function(routeId, element, width, height, extraProvider) {
		if (typeof extraProvider != "undefined") {
			if (extraProvider) {
				if (!this.isExtraProvider(extraProvider) && extraProvider != this.provider.id) {
					this.extraProviders.push(extraProvider);
				}
			} else {
				this.extraProviders = [];
			}
		}

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

		// kopie maken
		var extraProviders = this.extraProviders.slice();

		extraProviders.unshift(this.provider.id);
		var missing = [];

		for (var i = 0; i < extraProviders.length; i++) {
			var providerId = extraProviders[i];
			if (!route.hasRecentLiveData(providerId)) {
				missing.push(providerId);
			}
		}

		if (missing.length == 0){
			var data = {};

			for (var i = 0; i < extraProviders.length; i++) {
				var providerId = extraProviders[i];
				var provider = providers[providerId];
				data[provider.name] = route.liveData[providerId].data;
				data[provider.name + ' gemiddelde'] = route.liveData[providerId].avgData;
			}
			// Onderaan lijst toevoegen voor extraProviders
			$(element).parent().find('.extra-content').html(this.extraProvidersList());

			drawChart(element, data, width, height);
		}else{
			Api.newQueue(missing.length);
			for (var i = 0; i < missing.length; i++) {
				var providerId = missing[i];
				Api.syncLiveGraph(route.id, providerId, callback, this);
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

		// kopie maken
		var extraProviders = this.extraProviders.slice();


		var data = {};

		extraProviders.unshift(this.provider.id);
		var missing = [];

		// weekdagen enkel toevoegen bij 1 provider
		var end = 7;
		if (extraProviders.length > 1) {
			end = -1;
		}

		for (var i = 0; i < extraProviders.length; i++) {
			var providerId = extraProviders[i];
			var provider = providers[providerId];
			var okay = true;

			// Kleine hack: we beginnen altijd bij 7, daarna 1, 2 ...
			// dit doen we om het gemiddelde altijd eerst te tonen, zonder dup code
			for (var j = -1; j <= end; j++) {
				var day = j;
				if (day == -1) {
					day = 7;
				}

				var graph = route.getIntervalData(interval, day, providerId);
				if (!graph || !graph.data){
					okay = false;
					break;
				}else{
					var pre = '';
					if (extraProviders.length > 1) {
						pre = provider.name + ' - ';
					}

					if (day == 7) {
						data[pre + "Alle dagen"] = graph.data;
						data[pre + "Gemiddelde"] = graph.avgData;
					} else {
						data[weekdays[day]] = graph.data;
					}
				}	
			}

			if (!okay) {
				missing.push(providerId);
			}
		}

		if (missing.length > 0) {
			Api.newQueue(missing.length);
			for (var i = 0; i < missing.length; i++) {
				var providerId = missing[i];
				Api.syncIntervalGraph(interval, routeId, providerId, callback, this);
			}
			Api.endQueue();
			return;
		}

		$(element).parent().find('.extra-content').html(this.extraProvidersList());
		drawChart(element, data, width, height, 2 * extraProviders.length);
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

		var extraProviders = this.extraProviders.slice();
		var data = {};

		extraProviders.unshift(this.provider.id);
		var missing = [];

		for (var i = 0; i < extraProviders.length; i++) {
			var providerId = extraProviders[i];
			var provider = providers[providerId];

			var graph = route.getDayData(day, providerId);

			if (graph !== null && graph.data !== null) {
				var pre = '';
				if (extraProviders.length > 1) {
					pre = provider.name + ' - ';
				}

				data[pre + dateToDate(day)] = graph.data;
				data[pre + "Gemiddelde"] = graph.avgData;
			} else {
				missing.push(providerId);
			}

		}
		if (missing.length > 0) {
			Api.newQueue(missing.length);
			for (var i = 0; i < missing.length; i++) {
				var providerId = missing[i];
				Api.syncDayGraph(day, routeId, providerId, callback, this);
			}
			Api.endQueue();
			return;
		}

		$(element).parent().find('.extra-content').html(this.extraProvidersList());
		drawChart(element, data, width, height, extraProviders.length * 2);
	},
	// Opent de grafiek horende bij 2 intervallen
	openCompareGraph: function(interval0, interval1, routeId, element, width, height) {
		var route = routes[routeId];

		var callback = function(){
			Dashboard.openCompareGraph(interval0, interval1, routeId, element, width, height);
		};

		var extraProviders = this.extraProviders.slice();
		var data = {};

		extraProviders.unshift(this.provider.id);
		var missing0 = [];
		var missing1 = [];

		for (var i = 0; i < extraProviders.length; i++) {
			var providerId = extraProviders[i];
			var provider = providers[providerId];

			var pre = '';
			if (extraProviders.length > 1) {
				pre = provider.name + ' - ';
			}

			var graph = route.getIntervalData(interval0, 7, providerId);
			if (!graph || !graph.data){
				missing0.push(providerId);
			}else{
				data[pre + interval0.getName()] = graph.data;
			}

			graph = route.getIntervalData(interval1, 7, providerId);
			if (!graph || !graph.data){
				missing1.push(providerId);
			}else{
				data[pre + interval1.getName()] = graph.data;
			}	
		}

		if (missing0.length > 0 || missing1.length > 0) {
			Api.newQueue(missing0.length + missing1.length);

			for (var i = 0; i < missing0.length; i++) {
				var providerId = missing0[i];
				Api.syncIntervalGraph(interval0, routeId, providerId, callback, this);
			}
			for (var i = 0; i < missing1.length; i++) {
				var providerId = missing1[i];
				Api.syncIntervalGraph(interval1, routeId, providerId, callback, this);
			}
			Api.endQueue();

			return;
		}

		$(element).parent().find('.extra-content').html(this.extraProvidersList());
		drawChart(element, data, width, height, extraProviders.length * 2);
	},

	//syncIntervalGraph
	// Genereert HTML voor live modus
	reloadLive: function() {
		var p = this.provider.id;

		var hasData = this.routesDoHaveData(function(route) {
			return route.hasRecentAvgRepresentation(p) && route.hasRecentLiveRepresentation(p);
		});

		var dashboard = $('#dashboard .content');

		if (!hasData){
			Api.syncLiveData(p, Dashboard.reload, this);
			var str = this.renderHeader("live", {});
			str += Mustache.renderTemplate("loading", []);
			dashboard.html(str);
			return;
		}

		//Bepaald hoe oud deze live data is.
		var lastupdated = new Date();
		for(var i=0; i<routes.length; ++i){
			if (typeof routes[i] == "undefined" || !routes[i]) {
				continue;
			}
			if(routes[i].hasRecentLiveRepresentation(p) && routes[i].liveData[p].representation.createdOn < lastupdated){
				lastupdated = routes[i].liveData[p].representation.timestamp;
			}
		}

		var today = new Date();

		var lu_str = "";
		// Als het vandaag is: hoeveel minuten geleden
		if (today.getFullYear() === lastupdated.getFullYear() &&
		    today.getMonth() === lastupdated.getMonth() &&
		    today.getDate() === lastupdated.getDate()) {

			var diffMins = Math.round(((( today - lastupdated ) / 1000 ) / 60 ) );
			if (diffMins > 0){ 
                if(diffMins < 60) {
                    lu_str = diffMins + " minuten geleden";
                } else {
                    lu_str = Math.floor(diffMins/60) + " uur " + diffMins%60 + " minuten geleden";
                }
			}
	    } else {
	    	// Today op gisteren zetten, en kijken of het gisteren was
	    	today.setDate(today.getDate() - 1);
		    if (today.getFullYear() === lastupdated.getFullYear() &&
			    today.getMonth() === lastupdated.getMonth() &&
			    today.getDate() === lastupdated.getDate()) {

			    lu_str = "Gisteren om "+ dateToTimeString(lastupdated);
		    } else {
		    	lu_str = dateToString(lastupdated);
		    }
	    }

	    var str = this.renderHeader("live", {updated: lu_str});


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
        	if (route.isExceptional(p)) {
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
	},
	// Genereert HTML voor periode modus
	reloadDay: function() {
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
        	if (route.getUnusualColor(dayData) == 'red') {
        		return -dayData.unusualPercentage;
        	}
        	return dayData.unusualPercentage;
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
        	if (route.getUnusualColor(dayData) == 'red') {
        		return -dayData.unusualPercentage;
        	}
        	return dayData.unusualPercentage;
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
    	return '<header>'+ Mustache.renderTemplate("header-"+headerName, data) + '</header>';
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

