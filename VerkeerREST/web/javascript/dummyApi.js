// Dummy object dat de communicatie met de API nabootst
// Bevat voornamelijk functies

var DummyApi = {
	delay: 1, // Fake delay die gebruikt zal worden

	// Roept callback aan met delay ingesteld in object -> om async te testen
	callDelayed: function(callback, context){
		setTimeout(function() {
			callback.call(context);
		}, this.delay*1000);
	},

	syncRoutes: function(callback, context) {
		routes = [];
		routes.push(Route.create(2, 'Route 1', 'Van E40 tot X', 2854));
		routes.push(Route.create(3, 'Route 2', 'Van E40 tot X', 2254));
		routes.push(Route.create(4, 'Route 3', 'Van E40 tot X', 1254));
		routes.push(Route.create(5, 'Route 4', 'Van E40 tot X', 6783));
		routes.push(Route.create(6, 'Route 5', 'Van E40 tot X', 234));
		routes.push(Route.create(7, 'Route 6', 'Van E40 tot X', 2823));
		routes.push(Route.create(8, 'Route 7', 'Van E40 tot X', 3854));
		routes.push(Route.create(9, 'Route 8', 'Van E40 tot X', 4854));
		routes.push(Route.create(10, 'Route 9', 'Van E40 tot X', 1858));

		// Callback 
		this.callDelayed(callback, context);
	},

	// Haalt de livedata van alle routes op 
	syncLiveData: function(provider, callback, context) {
		var p = provider;
		for (var i = 0; i < routes.length; i++) {
			var route = routes[i];
		
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
		}

		this.callDelayed(callback, context);
	},

	syncProviders: function(callback, context) {
		providers = [];
		providers.push(Provider.create(-1, 'Alles'));
		providers.push(Provider.create(1, 'Google'));
		providers.push(Provider.create(2, 'Waze'));
		providers.push(Provider.create(3, 'Here'));
		providers.push(Provider.create(4, 'TomTom'));
		providers.push(Provider.create(5, 'Coyote'));

		// Callback 
		this.callDelayed(callback, context);
	}
};