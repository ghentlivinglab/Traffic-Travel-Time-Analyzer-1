// Dummy object dat de communicatie met de API nabootst
// Bevat voornamelijk functies

var DummyApi = {
	delay: 1, // Fake delay die gebruikt zal worden
	interval: 15, // minute interval in graphs
	intervalDecimal: 0.25, // (60/interval)

	// Roept callback aan met delay ingesteld in object -> om async te testen
	callDelayed: function(callback, context){
		setTimeout(function() {
			callback.call(context);
		}, this.delay*1000);
	},

	// Deze functie haalt alle routes van de api en plaatst deze in routes[]
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

		// Callback 
		this.callDelayed(callback, context);
	},

	// Haalt de livedata (= drukte nu en gemiddelde drukte van afgelopen maand(en)) van alle routes op 
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
			// dit kan niet
			console.error('Route '+route.name+' heeft geen avgData voor provider met id '+providerId);
		}

		this.callDelayed(callback, context);
	},

	// Haalt de grafiek van vandaag op (tot dit tijdstip)
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
			// dit kan niet
			console.error('Route '+route.name+' heeft geen liveData voor provider met id '+providerId);
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