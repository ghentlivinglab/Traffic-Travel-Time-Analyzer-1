/****************************
 * Dummy object that mimics the API-communication
 * contains functions for the most part
 ****************************/
var DummyApi = {
	delay: 0.2, // Fake delay die gebruikt zal worden
	interval: 15, // timespan in minutes used in graphs
	intervalDecimal: .25, // timespan in hours

	// Counter
	queues: {},
	currentQueue: null,

	// Stel een callback uit tot meerdere requests zijn afgehandeld
	// Moet beïndigd worden met endQueue nadat alle requests (aantal count) in die queue zijn verzonden
	// Count zou later evt geautomatiseerd kunnen worden, maar is veel dup code
	newQueue: function(count) {
		if (count == 0){
			return;
		}
		var id = Math.floor((Math.random() * 10000000));
		var queue = {
			id: id,
			count: count
		};

		console.log("Nieuwe queue "+id+" met "+count+" requests");

		this.queues[id] = queue;
		this.currentQueue = queue;
	},

	endQueue: function() {
		console.log("end queue "+this.currentQueue.id);
		this.currentQueue = null;
	},

	getQueue: function(id) {
		if (typeof this.queues[id] == "undefined"){
			return null;
		}
		return this.queues[id];
	},

	deleteQueue: function(id) {
		delete this.queues[id];
	},
	// calls callback with a delay to test the async aspect of the code
	callDelayed: function(callback, context){
		// Id opslaan
		var qid = -1;
		if (this.currentQueue){
			qid = this.currentQueue.id;
			console.log("request toegeveogd aan queue "+qid);
		}
		var me = this;

		setTimeout(function() {
			var q = me.getQueue(qid);
			if (!q){
				console.log("request klaar zonder queue ");
				callback.call(context);
			}else{
				q.count = Math.max(0, q.count - 1);
				if (q.count == 0){
					console.log("request klaar (queue = "+q.id+") queue beïndigd");

					me.deleteQueue(q.id);
					callback.call(context);
				} else {
					console.log("request klaar (queue = "+q.id+"), moet wachten op "+q.count+" request(s)");
				}
			}

		}, this.delay*1000+Math.floor((Math.random() * 2000)));
	},

	// fetches all routes of the API and places them in routes[]
	syncRoutes: function(callback, context) {
		routes = [];
		routes[2] = Route.create(2, 'Route 1', 'Van E40 tot X', 2854);
		routes[3] = Route.create(3, 'Route 2', 'Van E40 tot X', 2254);
		routes[4] = Route.create(4, 'Route 3', 'Van E40 tot X', 1254);
		routes[5] = Route.create(5, 'Route 4', 'Van E40 tot X', 6783);
		routes[6] = Route.create(6, 'Route 5', 'Van E40 tot X', 234);
		routes[7] = Route.create(7, 'Route 6', 'Van E40 tot X', 2823);
		routes[8] = Route.create(8, 'Route 7', 'Van E40 tot X', 3854);
		routes[9] = Route.create(9, 'Route 8', 'Van E40 tot X', 4854);
		routes[10] = Route.create(10, 'Route 9', 'Van E40 tot X', 1858);
		routes[11] = Route.create(11, 'Route 10', 'Van E40 tot X', 1858);
		routes[12] = Route.create(12, 'Route 11', 'Van E40 tot X', 1858);
		routes[13] = Route.create(13, 'Route 12', 'Van E40 tot X', 1858);
		routes[14] = Route.create(14, 'Route 13', 'Van E40 tot X', 1858);
		
		// Callback 
		this.callDelayed(callback, context);
	},

	// fetches the live data (= current traffic and an average of last month(s) ) of every route
	syncLiveData: function(provider, callback, context) {
		var p = provider;
		routes.forEach(function(route){
		
			var avgData = TrafficData.create(Math.floor((Math.random() * 40) + 50), Math.floor((Math.random() * 10) + 6));
			var liveData = TrafficData.create(Math.floor((Math.random() * 40) + 50), Math.floor((Math.random() * 10) + 6));

			if (route.hasAvgData(p)){
				route.avgData[p].representation = avgData;
			}else{
				route.avgData[p] = TrafficGraph.create(avgData);
			}

			if (route.hasLiveData(p)){
				route.liveData[p].representation = liveData;
			}else{
				route.liveData[p] = TrafficGraph.create(liveData);
			}
		});

		this.callDelayed(callback, context);
	},

	// fetches the graph for average data (= averages for every hour of last month)
	// Haalt de gemiddelde grafiek op (gemiddelde van de afgelopen maand voor elk uur)
	syncAvgGraph: function(routeId, providerId, callback, context){
		var route = routes[routeId];

		var base = 8;
		var data = {};

		for (var i = 6; i <= 24; i+=this.intervalDecimal) {
			if (i > 7 && i < 10 || i > 16 && i < 18){
				base += Math.random();
			}
			if (i > 18){
				base -= Math.random();
			}
			if (base > 5){
				base += Math.random() * 1 - 0.7;
			}else{
				base += Math.random() * 2;
			}
			data[i] = base;
		}

		if (route.hasAvgData(providerId)){
			route.avgData[providerId].setData(data);
		}else{
			// impossible
			console.error('Route '+route.name+' heeft geen avgData voor provider met id '+providerId);
		}

		this.callDelayed(callback, context);
	},

	// fetches graph for today (right up to current time)
	syncLiveGraph: function(routeId, providerId, callback, context){
		var route = routes[routeId];

		var base = 8;
		var data = {};
		var currentTime = (new Date()).getHours() + (new Date()).getMinutes()/60;
		for (var i = 6; i <= 24; i+=this.intervalDecimal) {
			if (i > 7 && i < 10 || i > 16 && i < 18){
				base += Math.random();
			}
			if (i > 18){
				base -= Math.random();
			}
			if (base > 5){
				base += Math.random() * 1 - 0.7;
			}else{
				base += Math.random() * 2;
			}
			if (i > currentTime){
				break;
			}
			data[i] = base;
		}

		if (route.hasLiveData(providerId)){
			route.liveData[providerId].setData(data);
		}else{
			// impossible
			console.error('Route '+route.name+' heeft geen liveData voor provider met id '+providerId);
		}

		this.callDelayed(callback, context);
	},

	//
	syncProviders: function(callback, context) {
		providers = [];
		providers[0] = Provider.create(0, 'Alles');
		providers[1] = Provider.create(1, 'Google');
		providers[2] = Provider.create(2, 'Waze');
		providers[3] = Provider.create(3, 'Here');
		providers[4] = Provider.create(4, 'TomTom');
		providers[5] = Provider.create(5, 'Coyote');

		// Callback 
		this.callDelayed(callback, context);
	},

	// fetches the live data (= current traffic and an average of last month(s) ) of every route
	syncIntervalData: function(interval, provider, callback, context) {
		var p = provider;
		routes.forEach(function(route){
		
			var representation = TrafficData.create(Math.floor((Math.random() * 40) + 50), Math.floor((Math.random() * 10) + 6));
			var data = route.getIntervalData(interval, 7, p);
			if (data){
				data.representation = representation;
			}else{
				route.setIntervalData(interval, 7, p, TrafficGraph.create(representation));
			}
		});

		this.callDelayed(callback, context);
	},


};