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
                    for (var i = 0; i < resultdata.length; i++) {
                        routes[resultdata[i].id] = Route.create(resultdata[i].id, resultdata[i].name, resultdata[i].description, resultdata[i].length, resultdata[i].speedLimit);
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
        loadingMap(true);

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
                            var avgData = TrafficData.create(rdata.avg.speed, rdata.avg.time, stringToDate(rdata.live.createdOn));
                            var liveData = TrafficData.create(rdata.live.speed, rdata.live.time, stringToDate(rdata.live.createdOn));
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
                    console.error(result.reason);
                }
                me.callDelayed(qid, newCallback, context);
                
            },
            error: handleAjaxError
        });
    },

    // fetches graph for today (right up to current time)
    syncLiveGraph: function(routeId, providerId, callback, context) {
    	
        // Bij begin van alle requests uitvoeren. 
        // Hebben deze nodig voor de callback wanneer de request klaar is.
        var qid = this.getQueueId();

        // Hier alle data van de server halen.
        // Uiteindelijk moet dit ongeveer het resultaat zijn: 

        var route = routes[routeId];
        
        // Hier alle data van de server halen.
        // Uiteindelijk moet dit ongeveer het resultaat zijn: 

        var from = new Date();
        if (route.hasLiveData(providerId)) {
            from = new Date(route.liveData[providerId].representation.timestamp.getTime());
        }

        
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
                    var avgData = {};
                    var resultdata = result.data;

                    for (var key in resultdata) {
                        var time = new Date(key);
                        var hour = time.getUTCHours();
                        var minutes = time.getUTCMinutes();
                        hour += (minutes / 60);

                        if (isNaN(hour)) {
                            console.error('Unreadable date format: "' + key + '" - Make sure the REST server is running the latest version.');
                            continue;
                        }
                        data[hour] = (resultdata[key].traveltime) / 60;
                        avgData[hour] = (resultdata[key].average) / 60;
                    }

                    if (route.hasLiveData(providerId)) {
                        route.liveData[providerId].setData(data);
                        route.liveData[providerId].setAvgData(avgData);
                        me.callDelayed(qid, callback, context);
                    } else {
                        // impossible
                        console.error('Route ' + route.name + ' heeft geen liveData voor provider met id ' + providerId);
                    }
                } else {
                    console.error(result.reason);
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
            data: {providerID: provider, from: dateToRestString(interval.start), to: dateToRestString(interval.end), slowSpeed: consideredSlowSpeed},
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
                            var unusual = rdata.unusual;
                            var arr = [];
                            if (typeof unusual != "undefined" && unusual) {
                                for (var i = 0; i < unusual.length; i++) {
                                    arr[i] = stringToDate(unusual[i]);
                                }
                            }
                            
                            representation = IntervalRepresentation.create(parseInt(rdata.speed), parseInt(rdata.time)/60, parseInt(rdata.slow), arr);
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
        
    },

    syncDayData: function(day, provider, callback, context) {
        // Bij begin van alle requests uitvoeren. 
        // Hebben deze nodig voor de callback wanneer de request klaar is.
        var qid = this.getQueueId();

        var me = this;

        var start = new Date(day.getTime());
        start.setHours(0,0,0,0);

        var end = new Date(day.getTime());
        end.setHours(23,59,59,999);
        
        // Optimalisatie mogelijk: apart API request voor 1 dag maken
        $.ajax({
            type: "GET",
            url: "/api/trafficdata/interval",
            data: {providerID: provider, from: dateToRestString(start), to: dateToRestString(end), slowSpeed: consideredSlowSpeed},
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
                           var unusual = rdata.unusual;
                            var arr = [];
                            if (typeof unusual != "undefined" && unusual) {
                                for (var i = 0; i < unusual.length; i++) {
                                    arr[i] = stringToDate(unusual[i]);
                                }
                            }
                            
                            representation = IntervalRepresentation.create(parseInt(rdata.speed), parseInt(rdata.time)/60, parseInt(rdata.slow), arr);
                        } else {
                            representation = IntervalRepresentation.createEmpty();
                        }
                        var data = route.getDayData(day, provider);
                        if (data){
                            data.representation = representation;
                        }else{
                            route.setDayData(day, provider, TrafficGraph.create(representation));
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
                        var avgData = {};
                        for (var key in resultdata[weekday]) {
                            var times = key.split(":");
                            var hour = parseInt(times[0]);
                            var minutes = parseInt(times[1]);
                            hour += (minutes / 60);
                            data[hour] = (resultdata[weekday][key].traveltime) / 60;
                            avgData[hour] = (resultdata[weekday][key].average) / 60;
                        }
                        graph.data = data;
                        graph.avgData = avgData;
                    }
                    route.generateIntervalAvg(interval, provider);
                    me.callDelayed(qid, callback, context);
                } else {
                    console.error(result.reason);
                }
            },
            error: handleAjaxError
        });   

	},

    syncDayGraph: function(day, routeId, provider, callback, context) {
        var qid = this.getQueueId();

        var route = routes[routeId];

        var start = new Date(day.getTime());
        start.setHours(0,0,0,0);

        var end = new Date(day.getTime());
        end.setHours(23,59,59,999);

        var me = this;

        // Optimalisatie mogelijk: apart API request voor 1 dag maken
        $.ajax({
            type: "GET",
            url: "/api/trafficdata/weekday",
            data: {providerID: provider, routeID: routeId, from: dateToRestString(start), to: dateToRestString(end)},
            beforeSend: function (jqXHR, settings) {
                jqXHR.url = settings.url;
                addHeaders(jqXHR);
            },
            success: function(result, status, jqXHR) {
                if (result.result === "success") {
                    var resultdata = result.data;

                    var graph = route.getDayData(day, provider);
                    if (!graph) {
                        graph = TrafficGraph.create(null);
                        route.setDayData(day, provider, graph);
                    }

                    // Hier krijgen we data per dag van de week doorgestuurd
                    // Die is eigenlijk overbodig.
                    // Maar hiervan geeft slechts één dag data terug die we nodig hebben.
                    
                    // Leeg zetten voor als we geen data tegen komen
                    // Dit vermijdt een infinite loop
                    graph.data = {};
                    graph.avgData = {};
                    for (var weekday in resultdata) {
                        var data = {};
                        var avgData = {};
                        for (var key in resultdata[weekday]) {
                            var times = key.split(":");
                            var hour = parseInt(times[0]);
                            var minutes = parseInt(times[1]);
                            hour += (minutes / 60);
                            data[hour] = resultdata[weekday][key].traveltime / 60;
                            avgData[hour] = (resultdata[weekday][key].average) / 60;
                        }
                        if (Object.keys(data).length > 0) {
                            // Deze dag bevat data
                            graph.data = data;
                            graph.avgData = avgData;
                            break;
                        }
                    }

                    me.callDelayed(qid, callback, context);
                } else {
                    console.error(result.reason);
                }
            },
            error: handleAjaxError
        });   

    }
};

function addHeaders(request) {
    request.setRequestHeader("x-api-key", "6qKKfkX7u2lmJqxd8RrpLk7m");
}

function handleAjaxError(jqXHR, textStatus, errorThrown) {
    console.error("Error while performing request for url : " + jqXHR.url + "\n" + jqXHR.status + " " + errorThrown + ". " + jqXHR.responseText);
}

