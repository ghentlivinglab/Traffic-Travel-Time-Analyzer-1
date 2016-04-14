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
    },
    // calls callback with a delay to test the async aspect of the code
    // Als een request klaar is moet deze methode uitgevoerd worden
    // qid = queue id bij het begin van de request!
    callDelayed: function(qid, callback, context) {
        var q = this.getQueue(qid);
        if (!q) {
            console.log("request klaar zonder queue ");
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
    // fetches all routes of the API and places them in routes[]
    syncRoutes: function(callback, context) {
        // Bij begin van alle requests uitvoeren. 
        // Hebben deze nodig voor de callback wanneer de request klaar is.
        var qid = this.getQueueId();

        // Hier alle data van de server halen.
        // Uiteindelijk moet dit ongeveer het resultaat zijn: 
        routes = [];
        var me = this;
        $.getJSON("http://localhost:8080/VerkeerREST/api/routes", function(result) {
            for (var i = 0; i < result.length; i++) {
                routes[result[i].id] = Route.create(result[i].id, result[i].name, result[i].description, result[i].length);
            }
            me.callDelayed(qid, callback, context);
        });


        /*routes = [];
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
         routes[14] = Route.create(14, 'Route 13', 'Van E40 tot X', 1858);*/

        // Hier alle data van de server halen

        // Callback zodra we de data hebben (moet dus in success van ajax)

    },
    // fetches the live data (= current traffic and an average of last month(s) ) of every route
    syncLiveData: function(provider, callback, context) {
        // Bij begin van alle requests uitvoeren. 
        // Hebben deze nodig voor de callback wanneer de request klaar is.
        var qid = this.getQueueId();

        // Hier alle data van de server halen.

        var me = this;
        $.getJSON("http://localhost:8080/VerkeerREST/api/trafficdata/live?providerID=" + provider, function(result) {
            if (result.result === "success") {
                var data = result.data;

                routes.forEach(function(route) {
                    var rdata = data[route.id];
                    console.log(JSON.stringify(rdata));
                    if (typeof rdata !== "undefined") {
                        var avgData = TrafficData.create(rdata.avg.speed, rdata.avg.time);
                        var liveData = TrafficData.create(rdata.live.speed, rdata.live.time);
                    } else {
                        var avgData = TrafficData.create('', '');
                        var liveData = TrafficData.create('', '');
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
            me.callDelayed(qid, callback, context);
        }).fail(function() {
            console.log("something went wrong loading the live data.");
        });
    },
    // fetches the graph for average data (= averages for every hour of last month)
    // Haalt de gemiddelde grafiek op (gemiddelde van de afgelopen maand voor elk uur)
    syncAvgGraph: function(routeId, providerId, callback, context) {
        // Bij begin van alle requests uitvoeren. 
        // Hebben deze nodig voor de callback wanneer de request klaar is.
        var qid = this.getQueueId();

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

        $.getJSON("/VerkeerREST/api/trafficdata/weekday?providerID=" + providerId + "&routeID=" + routeId + "&weekday=" + weekday + "&from=" + dateToRestString(from) + "&to=" + dateToRestString(to), function(result) {
            if (result.result === "success") {
                var resultdata = result.data;

                for (var key in resultdata[weekday]) {
                    var times = key.split(":");
                    var hour = parseInt(times[0]);
                    var minutes = parseInt(times[1]);
                    hour += (minutes / 60);
                    data[hour] = (resultdata[weekday][key]) / 60;
                }

                if (route.hasAvgData(providerId)) {
                    route.avgData[providerId].setData(data);
                    me.callDelayed(qid, callback, context);
                } else {
                    // impossible
                    console.error('Route ' + route.name + ' has no avgData for provider with id ' + providerId);
                }
                console.log(data);
                

            } else {
                alert(result.reason);
            }
        }).fail(function() {
            console.log("something went wrong loading the live data.");
        });

        // Callback zodra we de data hebben (moet dus in success van ajax)


        /*var route = routes[routeId];
         
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
         /*
         if (route.hasAvgData(providerId)){
         route.avgData[providerId].setData(data);
         }else{
         // impossible
         console.error('Route '+route.name+' heeft geen avgData voor provider met id '+providerId);
         }*/

        // Hier alle data van de server halen

        // Callback zodra we de data hebben (moet dus in success van ajax)
        //this.callDelayed(qid, callback, context);
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

        var data = {};

        var from = new Date();
        from.setHours(0);
        from.setMinutes(0);
        from.setSeconds(0);
        var to = new Date();

        var me = this;

        $.getJSON("/VerkeerREST/api/trafficdata?providerID=" + providerId + "&routeID=" + routeId + "&from=" + dateToRestString(from) + "&to=" + dateToRestString(to), function(result) {
            if (result.result === "success") {
                var resultdata = result.data;

                for (var key in resultdata) {
                    var time = new Date(key);
                    var hour = time.getHours();
                    var minutes = time.getMinutes();
                    hour += (minutes / 60);
                    data[hour] = (resultdata[key]) / 60;
                }

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
        }).fail(function() {
            console.log("something went wrong loading the live data.");
        });
        

        /*
        var base = 8;
        var data = {};
        var currentTime = (new Date()).getHours() + (new Date()).getMinutes() / 60;
        for (var i = 6; i <= 24; i += this.intervalDecimal) {
            if (i > 7 && i < 10 || i > 16 && i < 18) {
                base += Math.random();
            }
            if (i > 18) {
                base -= Math.random();
            }
            if (base > 5) {
                base += Math.random() * 1 - 0.7;
            } else {
                base += Math.random() * 2;
            }
            if (i > currentTime) {
                break;
            }
            data[i] = base;
        }

        if (route.hasLiveData(providerId)) {
            route.liveData[providerId].setData(data);
        } else {
            // impossible
            console.error('Route ' + route.name + ' heeft geen liveData voor provider met id ' + providerId);
        }

        // Hier alle data van de server halen

        // Callback zodra we de data hebben (moet dus in success van ajax)
        this.callDelayed(qid, callback, context);
        */
    },
    //
    syncProviders: function(callback, context) {
        // Bij begin van alle requests uitvoeren. 
        // Hebben deze nodig voor de callback wanneer de request klaar is.
        var qid = this.getQueueId();

        // Hier alle data van de server halen.
        // Uiteindelijk moet dit ongeveer het resultaat zijn:
        providers = [];
        var me = this;
        providers[0] = Provider.create(0, 'Alles');
        $.getJSON("http://localhost:8080/VerkeerREST/api/providers", function(result) {
            console.log(JSON.stringify(result));
            for (var i = 0; i < result.length; i++) {
                providers[result[i].id] = Provider.create(result[i].id, result[i].name);
            }
            me.callDelayed(qid, callback, context);
        });
        /*providers = [];
         providers[0] = Provider.create(0, 'Alles');
         providers[1] = Provider.create(1, 'Google');
         providers[2] = Provider.create(2, 'Waze');
         providers[3] = Provider.create(3, 'Here');
         providers[4] = Provider.create(4, 'TomTom');
         providers[5] = Provider.create(5, 'Coyote');*/

        // Hier alle data van de server halen

        // Callback zodra we de data hebben (moet dus in success van ajax)

    },
    // fetches the live data (= current traffic and an average of last month(s) ) of every route
    syncIntervalData: function(interval, provider, callback, context) {
        // Bij begin van alle requests uitvoeren. 
        // Hebben deze nodig voor de callback wanneer de request klaar is.
        var qid = this.getQueueId();

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

        // Callback zodra we de data hebben (moet dus in success van ajax)
        //this.callDelayed(qid, callback, context);
    },
};

// Gebruiken nu voorlopig de dummy

// De gebruikte API zetten we op de dummy
//var Api = Api;
