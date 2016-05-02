// Api bevat alle methodes die de communicatie met de server regelt

/****************************
 * Dummy object that mimics the API-communication
 * contains functions for the most part
 ****************************/
var Api = {
    // Counter
    queues: {},
    currentQueue: null,
    intervalDecimal: .25, // timespan in hours

    // Stel een callback uit tot meerdere requests zijn afgehandeld
    // Moet beïndigd worden met endQueue nadat alle requests (aantal count) in die queue zijn verzonden
    // Count zou later evt geautomatiseerd kunnen worden, maar is veel dup code
    newQueue: function(count) {
        if (count == 0) {
            return;
        }
        var id = Math.floor((Math.random() * 10000000));
        var queue = {
            id: id,
            count: count
        };

        console.log("Nieuwe queue " + id + " met " + count + " requests");

        this.queues[id] = queue;
        this.currentQueue = queue;
    },
    endQueue: function() {
        if (!this.currentQueue) {
            console.log("end queue, geen queue om te sluiten");
            return;
        }
        console.log("end queue " + this.currentQueue.id);
        this.currentQueue = null;
    },
    getQueue: function(id) {
        if (typeof this.queues[id] == "undefined") {
            return null;
        }
        return this.queues[id];
    },
    deleteQueue: function(id) {
        delete this.queues[id];
    },
    getQueueId: function() {
        var qid = -1;
        if (this.currentQueue) {
            qid = this.currentQueue.id;
            console.log("request toegevoegd aan queue " + qid);
        }
        return qid;
    },
    // calls callback with a delay to test the async aspect of the code
    // Als een request klaar is moet deze methode uitgevoerd worden
    // qid = queue id bij het begin van de request!
    callDelayed: function(qid, callback, context) {
        var q = this.getQueue(qid);
        if (!q) {
            //console.log("request klaar zonder queue ");
            callback.call(context);
        } else {
            q.count = Math.max(0, q.count - 1);
            if (q.count == 0) {
                console.log("request klaar (queue = " + q.id + ") queue beïndigd");

                this.deleteQueue(q.id);
                callback.call(context);
            } else {
                console.log("request klaar (queue = " + q.id + "), moet wachten op " + q.count + " request(s)");
            }
        }
    },
    syncWaypoints: function(id) {
        $.ajax({
            type: "GET",
            url: "/api/waypoints",
            data: {routeID: id},
            beforeSend: function (jqXHR, settings) {
                jqXHR.url = settings.url;
                addHeaders(jqXHR);
            },
            success: function(result, status, jqXHR) {
                if (result.result === "success") {
                    var resultdata = result.data;
                    //console.log(JSON.stringify(resultdata));
                    if (resultdata.length !== 0) { // lengte van 0 waypoints wordt eigenlijk al opgevangen in REST API door error & reason terug te geven
                        var array = [];
                        resultdata.sort(function(a, b) {
                            return a.sequence - b.sequence;
                        });
                        for (var i in resultdata) {
                            array.push({"lat": resultdata[i].latitude, "lng": resultdata[i].longitude});
                        }
                        routes[resultdata[0].routeID].waypoints = array;
                    }
                } else {
                    console.error(result.reason);
                }
            },
            error: handleAjaxError
        });
    },
    // fetches all routes of the API and places them in routes[]
    syncRoutes: function(callback, context) {
        // Bij begin van alle requests uitvoeren. 
        // Hebben deze nodig voor de callback wanneer de request klaar is.
        var qid = this.getQueueId();

        // Hier alle data van de server halen.
        // Uiteindelijk moet dit ongeveer het resultaat zijn: 
        routes = [];
        
        var me = this;
        
        $.ajax({
            type: "GET",
            url: "/api/routes",
            beforeSend: function (jqXHR, settings) {
                jqXHR.url = settings.url;
                addHeaders(jqXHR);
            },
            success: function(result, status, jqXHR) {
                if (result.result === "success") {
                    var resultdata = result.data;
                    //console.log(JSON.stringify(resultdata));
                    for (var i = 0; i < resultdata.length; i++) {
                        routes[resultdata[i].id] = Route.create(resultdata[i].id, resultdata[i].name, resultdata[i].description, resultdata[i].length);
                        Api.syncWaypoints(resultdata[i].id);
                    }
                    me.callDelayed(qid, callback, context);
                } else {
                    console.error(result.reason);
                }
            },
            error: handleAjaxError
        });
    },
    // fetches the live data (= current traffic and an average of last month(s) ) of every route
    syncLiveData: function(provider, callback, context) {
        var newCallback = function(){
            reloadMap();
            callback.call(this);
        };

        // Bij begin van alle requests uitvoeren. 
        // Hebben deze nodig voor de callback wanneer de request klaar is.
        var qid = this.getQueueId();

        // Hier alle data van de server halen.

        var me = this;
        
        $.ajax({
            type: "GET",
            url: "/api/trafficdata/live",
            data: {providerID: provider},
            beforeSend: function (jqXHR, settings) {
                jqXHR.url = settings.url;
                addHeaders(jqXHR);
            },
            success: function(result, status, jqXHR) {
                if (result.result === "success") {
                    var data = result.data;

                    routes.forEach(function(route) {
                        var rdata = data[route.id];
                        if (typeof rdata !== "undefined") {
                            var avgData = TrafficData.create(rdata.avg.speed, rdata.avg.time);
                            var liveData = TrafficData.create(rdata.live.speed, rdata.live.time);
                        } else {
                            var avgData = TrafficData.createEmpty();
                            var liveData = TrafficData.createEmpty();
                        }
                        if (route.hasAvgData(provider))
                            route.avgData[provider].representation = avgData;
                        else
                            route.avgData[provider] = TrafficGraph.create(avgData);

                        if (route.hasLiveData(provider))
                            route.liveData[provider].representation = liveData;
                        else
                            route.liveData[provider] = TrafficGraph.create(liveData);

                    });
                } else {
                    alert(result.reason);
                }
                me.callDelayed(qid, newCallback, context);
                
            },
            error: handleAjaxError
        });
    },
    // fetches the graph for average data (= averages for every hour of last month)
    // Haalt de gemiddelde grafiek op (gemiddelde van de afgelopen maand voor elk uur)
    syncAvgGraph: function(routeId, providerId, callback, context) {
        // Bij begin van alle requests uitvoeren. 
        // Hebben deze nodig voor de callback wanneer de request klaar is.
        var qid = this.getQueueId();
        console.log('start avggraph request met id '+qid);

        var route = routes[routeId];

        // Hier alle data van de server halen.
        // Uiteindelijk moet dit ongeveer het resultaat zijn: 

        var data = {};

        var from = new Date();
        from.setDate((new Date()).getDate() - 30);
        var to = new Date();

        var weekday = new Date().getDay();
        weekday = weekdays_js_to_rest[weekday];

        var me = this;

        $.ajax({
            type: "GET",
            url: "/api/trafficdata/weekday",
            data: {providerID: providerId, routeID: routeId, weekday: weekday, from: dateToRestString(from), to: dateToRestString(to)},
            beforeSend: function (jqXHR, settings) {
                jqXHR.url = settings.url;
                addHeaders(jqXHR);
            },
            success: function(result, status, jqXHR) {
                if (result.result === "success") {
                    var resultdata = result.data;

                    for (var key in resultdata[weekday]) {
                        var times = key.split(":");
                        var hour = parseInt(times[0]);
                        var minutes = parseInt(times[1]);
                        hour += (minutes / 60);
                        data[hour] = (resultdata[weekday][key]) / 60;
                    }

                    console.log('avgGraph api data:');
                    console.log(data);

                    if (route.hasAvgData(providerId)) {
                        route.avgData[providerId].setData(data);
                        me.callDelayed(qid, callback, context);
                    } else {
                        // impossible
                        console.error('Route ' + route.name + ' has no avgData for provider with id ' + providerId);
                    }
                } else {
                    alert(result.reason);
                }
            },
            error: handleAjaxError
        });
    },
    // fetches graph for today (right up to current time)
    syncLiveGraph: function(routeId, providerId, callback, context) {
    	
        // Bij begin van alle requests uitvoeren. 
        // Hebben deze nodig voor de callback wanneer de request klaar is.
        var qid = this.getQueueId();
        console.log('start live graph request met id '+qid);

        // Hier alle data van de server halen.
        // Uiteindelijk moet dit ongeveer het resultaat zijn: 

        var route = routes[routeId];
        
        // Hier alle data van de server halen.
        // Uiteindelijk moet dit ongeveer het resultaat zijn: 

        var from = new Date();
        from.setHours(0);
        from.setMinutes(0);
        from.setSeconds(0);
        var to = new Date();

        var me = this;

        $.ajax({
            type: "GET",
            url: "/api/trafficdata",
            data: {providerID: providerId, routeID: routeId, from: dateToRestString(from), to: dateToRestString(to)},
            beforeSend: function (jqXHR, settings) {
                jqXHR.url = settings.url;
                addHeaders(jqXHR);
            },
            success: function(result, status, jqXHR) {
                if (result.result === "success") {
                    var data = {};
                    var resultdata = result.data;
                    console.log(resultdata);

                    for (var key in resultdata) {
                        var time = new Date(key);
                        var hour = time.getUTCHours();
                        var minutes = time.getUTCMinutes();
                        hour += (minutes / 60);
                        if (isNaN(hour)) {
                            console.error('Unreadable date format: "' + key + '" - Make sure the REST server is running the latest version.');
                            continue;
                        }
                        data[hour] = (resultdata[key]) / 60;
                    }

                    console.log('liveGraph api data:');
                    console.log(data);

                    if (route.hasLiveData(providerId)) {
                        route.liveData[providerId].setData(data);
                        me.callDelayed(qid, callback, context);
                    } else {
                        // impossible
                        console.error('Route ' + route.name + ' heeft geen liveData voor provider met id ' + providerId);
                    }
                } else {
                    alert(result.reason);
                }
            },
            error: handleAjaxError
        });
    },
    syncProviders: function(callback, context) {
        // Bij begin van alle requests uitvoeren. 
        // Hebben deze nodig voor de callback wanneer de request klaar is.
        var qid = this.getQueueId();

        // Hier alle data van de server halen.
        // Uiteindelijk moet dit ongeveer het resultaat zijn:
        providers = [];
        
        var me = this;
        
        $.ajax({
            type: "GET",
            url: "/api/providers",
            beforeSend: function (jqXHR, settings) {
                jqXHR.url = settings.url;
                addHeaders(jqXHR);
            },
            success: function(result, status, jqXHR) {
                if (result.result === "success") {
                    var resultdata = result.data;
                    //console.log(JSON.stringify(resultdata));
                    for (var i = 0; i < resultdata.length; i++) {
                        providers[resultdata[i].id] = Provider.create(resultdata[i].id, resultdata[i].name);
                    }
                    me.callDelayed(qid, callback, context);
                } else {
                    console.error(result.reason);
                }
            },
            error: handleAjaxError
        });
    },
    // fetches the live data (= current traffic and an average of last month(s) ) of every route
    syncIntervalData: function(interval, provider, callback, context) {
        // Bij begin van alle requests uitvoeren. 
        // Hebben deze nodig voor de callback wanneer de request klaar is.
        var qid = this.getQueueId();

        var me = this;
        
        $.ajax({
            type: "GET",
            url: "/api/trafficdata/interval",
            data: {providerID: provider, from: dateToRestString(interval.start), to: dateToRestString(interval.end)},
            beforeSend: function (jqXHR, settings) {
                jqXHR.url = settings.url;
                addHeaders(jqXHR);
            },
            success: function(result, status, jqXHR) {
                if (result.result === "success") {
                    var resultdata = result.data;
                    routes.forEach(function(route) {
                        var rdata = resultdata[route.id];
                        var representation;
                        if (typeof rdata != "undefined") {
                            var days = rdata.days;
                            for (var i = 0; i < days.length; i++) {
                                days[i] = parseInt(days[i]);
                            }
                            representation = IntervalRepresentation.create(parseInt(rdata.speed), parseInt(rdata.time)/60, days);
                        } else {
                            representation = IntervalRepresentation.createEmpty();
                        }
                        var data = route.getIntervalData(interval, 7, provider);
                        if (data){
                            data.representation = representation;
                        }else{
                            route.setIntervalData(interval, 7, provider, TrafficGraph.create(representation));
                        }
                    });
                    if (routes.length == 0){
                        console.error("Routes zijn leeg! Infinte loop voorkomen.");
                        return;
                    }
                    me.callDelayed(qid, callback, context);
                } else {
                    console.error(result.reason);
                }
            },
            error: handleAjaxError
        });
        
        // Hier alle data van de server halen.
        // Uiteindelijk moet dit ongeveer het resultaat zijn: 

        /*var p = provider;
         routes.forEach(function(route){
         
         var representation = TrafficData.create(Math.floor((Math.random() * 40) + 50), Math.floor((Math.random() * 10) + 6));
         var data = route.getIntervalData(interval, 7, p);
         if (data){
         data.representation = representation;
         }else{
         route.setIntervalData(interval, 7, p, TrafficGraph.create(representation));
         }
         });*/

        // Hier alle data van de server halen
        // 
        // Callback zodra we de data hebben (moet dus in success van ajax)
        //this.callDelayed(qid, callback, context);
    },

    // fetches the acumulated data of every route
	syncIntervalGraph: function(interval, routeId, provider, callback, context) {
        var qid = this.getQueueId();

        var route = routes[routeId];

        // Hier alle data van de server halen.
        // Uiteindelijk moet dit ongeveer het resultaat zijn: 

        var me = this;

        $.ajax({
            type: "GET",
            url: "/api/trafficdata/weekday",
            data: {providerID: provider, routeID: routeId, from: dateToRestString(interval.start), to: dateToRestString(interval.end)},
            beforeSend: function (jqXHR, settings) {
                jqXHR.url = settings.url;
                addHeaders(jqXHR);
            },
            success: function(result, status, jqXHR) {
                if (result.result === "success") {
                    var resultdata = result.data;
                    for (var weekday in resultdata) {
                        var graph = route.getIntervalData(interval, weekday, provider)
                        if (!graph) {
                            graph = TrafficGraph.create(null);
                            route.setIntervalData(interval, weekday, provider, graph);
                        }

                        var data = {};
                        for (var key in resultdata[weekday]) {
                            var times = key.split(":");
                            var hour = parseInt(times[0]);
                            var minutes = parseInt(times[1]);
                            hour += (minutes / 60);
                            data[hour] = (resultdata[weekday][key]) / 60;
                        }
                        graph.data = data;
                    }
                    route.generateIntervalAvg(interval, provider);
                    me.callDelayed(qid, callback, context);
                } else {
                    console.log(result.reason);
                }
            },
            error: handleAjaxError
        });   

		/*var route = routes[routeId];

		for (var day = 0; day < 7; day++) {
			var graph = route.getIntervalData(interval, day, provider)
			if (!graph){
				graph = TrafficGraph.create(null);
				route.setIntervalData(interval, day, provider, graph);
			}
			var data = {};
			var base = 8;
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
			graph.data = data;
			//graph.representation = null;

		}

		// Gemiddelde berekenen van alle weekdagen
		route.generateIntervalAvg(interval, provider);*/
	}
};

function addHeaders(request) {
    // request.setRequestHeader("Authorization", "1235");
}

function handleAjaxError(jqXHR, textStatus, errorThrown) {
    console.log("Error while performing request for url : " + jqXHR.url + "\n" + jqXHR.status + " " + errorThrown + ". " + jqXHR.responseText);
}
