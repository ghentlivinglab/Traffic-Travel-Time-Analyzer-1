/**
 * Api is een globale instantie die we gebruiken om data van onze REST API te downloaden.
 * Bij elke aanvraag geven we een callback en context mee. Een callback is een functie 
 * die we uitvoeren bij het voltooien van de aanvraag. De context is het object waarop
 * we de functie moeten uitvoeren. (zie javascript documentatie Function.prototype.call)
 * https://developer.mozilla.org/nl/docs/Web/JavaScript/Reference/Global_Objects/Function/call
 *
 * Soms is het noodzakelijk om meerdere aanvragen tergelijk te starten, maar slechts een callback
 * te krijgen als deze allemaal klaar zijn. Bv. Als we een grafiek tonen waarvoor we meerdere data
 * moeten agregeren. Hiervoor starten we een Queue. (zie Api.newQueue)
 *
 * @property {Object[]} queues Bevat een lijst met alle huidige queues die bezig zijn.
 * Elk object bevat een id en een count, die aangeeft hoeveel aanvragen er nog bezig zijn
 * in deze queue.
 *
 * @property {Object} currentQueue Is niet null tussen Api.newQueue(Integer) en Api.endQueue().
 * Bevat de queue (referentie naar queues object) waarin de aanvragen moeten worden toegevoegd. 
 * Indien null worden de aanvragen niet toegevoegd aan een queue, en wordt hun callback altijd 
 * uitgevoerd.
 *
 * @property {number} queueCount Counter om telkens een uniek id te kunnen 
 * genereren voor nieuwe queues.
 *
 * @property {number} intervalDecimal Decimaal getal in uren die aangeeft om de hoeveel minuten
 * de grafiek een punt moet zetten. 
 * 
 * @type {Object}
 */
var Api = {
    queues: {},
    currentQueue: null,
    queueCount: 1,
    intervalDecimal: .25,

    /**
     * Start een nieuwe Queue, zodat de callback enkel zal worden uitgevoerd als alle requests uit deze
     * queue zijn afgehandeld. Alle aanvragen die gestart worden tussen Api.newQueue en Api.endQueue
     * behoren tot deze queue.
     * 
     * @param  {number} Aantal requests die moet worden voltooid voor de callback zal worden aangroepen.
     * Neem hiervoor telkens het aantal aanvragen die je zal starten tussen Api.newQueue en Api.endQueue
     */
    newQueue: function(count) {
        if (count == 0) {
            return;
        }
        this.queueCount++;

        var id = this.queueCount;
        var queue = {
            id: id,
            count: count
        };

        this.queues[id] = queue;
        this.currentQueue = queue;
    },

    /**
     * Aanvragen gestart na endQueue worden niet meer toegevoegd aan de queue.
     */
    endQueue: function() {
        if (!this.currentQueue) {
            return;
        }
        console.log("end queue " + this.currentQueue.id);
        this.currentQueue = null;
    },

    /**
     * @param  {number} Id van de gevraagde Queue.
     * @return {Object} Het queue object dat bij dit id hoort, of null.
     */
    getQueue: function(id) {
        if (typeof this.queues[id] == "undefined") {
            return null;
        }
        return this.queues[id];
    },

    /**
     * @param  {number} Id van de te verwijderen Queue.
     */
    deleteQueue: function(id) {
        delete this.queues[id];
    },

    /**
     * @return {number} Het id van de huidige queue (indien tussen Api.newQueue en Api.endQueue) of -1
     * indien in geen queue.
     */
    getQueueId: function() {
        var qid = -1;
        if (this.currentQueue) {
            qid = this.currentQueue.id;
        }
        return qid;
    },

    // calls callback with a delay to test the async aspect of the code
    // Als een request klaar is moet deze methode uitgevoerd worden
    // qid = queue id bij het begin van de request!

    /**
     * Roep deze functie aan als een aanvraag klaar is. Deze zal er voor zorgen dat de callback
     * al dan niet wordt aangroepen op basis van het queue systeem.
     * 
     * @param  {number} Het queue id waartoe de aanvraag behoort (of -1 indien geen queue). 
     * Vraag dit id op bij het starten van de aanvraag.
     * @param  {Function} De callback die moet worden uitgevoerd als de queue leeg is of als er geen queue is.
     * @param  {Object} Het object waarop de callback moet worden uitgevoerd.
     */
    callDelayed: function(qid, callback, context) {
        var q = this.getQueue(qid);
        if (!q) {
            callback.call(context);
        } else {
            q.count = Math.max(0, q.count - 1);
            if (q.count == 0) {
                this.deleteQueue(q.id);
                callback.call(context);
            }
        }
    },

    /**
     * Fetches all routes of the API and places them in routes[]
     * 
     * @param {Function} callback De functie uit te voeren indien geslaagd en queue leeg is.
     * In een queue wordt enkel de callback van de laatst geslaagde aanvraag aangroepen (dus maximum 1).
     * @param {Object} context Object die de callback moet uitvoeren
     */
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
                Api.addHeaders(jqXHR);
            },
            success: function(result, status, jqXHR) {
                if (result.result === "success") {
                    var resultdata = result.data;

                    // Nu alle waypoints van alle routes downloaden.
                    // Als dit ook geslaagd is, dan roepen we de callback aan van de syncRoutes.
                    var waypointsCallback = function() {
                        me.callDelayed(qid, callback, context);
                    }

                    // Hierna alle waypoints van alle routes opslaan
                    Api.newQueue(resultdata.length);
                    for (var i = 0; i < resultdata.length; i++) {
                        routes[resultdata[i].id] = Route.create(resultdata[i].id, resultdata[i].name, resultdata[i].description, resultdata[i].length, resultdata[i].speedLimit);
                        Api.syncWaypoints(resultdata[i].id, waypointsCallback, Api);
                    }
                    Api.endQueue();
                    
                } else {
                    console.error(result.reason);
                }
            },
            error: Api.handleAjaxError
        });
    },

    /**
     * Haalt de waypoints op van een route en plaatst deze in het overeenkomstige route object in routes[]
     * 
     * @param  {number} routeId waarvoor we de waypoints willen ophalen.
     * @param {Function} callback De functie uit te voeren indien geslaagd en queue leeg is.
     * In een queue wordt enkel de callback van de laatst geslaagde aanvraag aangroepen (dus maximum 1).
     * @param {Object} context Object die de callback moet uitvoeren
     */
    syncWaypoints: function(id, callback, context) {
        // Queue id opvragen, om aan Api.callDelayed door te geven
        var qid = this.getQueueId();

        var me = this;

        $.ajax({
            type: "GET",
            url: "/api/waypoints",
            data: {routeID: id},
            beforeSend: function (jqXHR, settings) {
                jqXHR.url = settings.url;
                Api.addHeaders(jqXHR);
            },
            success: function(result, status, jqXHR) {
                if (result.result === "success") {
                    var resultdata = result.data;

                    // lengte van 0 waypoints wordt eigenlijk al opgevangen in REST API door error & reason terug te geven
                    if (resultdata.length != 0) {
                        var array = [];
                        resultdata.sort(function(a, b) {
                            return a.sequence - b.sequence;
                        });
                        for (var i in resultdata) {
                            array.push({"lat": resultdata[i].latitude, "lng": resultdata[i].longitude});
                        }
                        routes[resultdata[0].routeID].waypoints = array;
                        me.callDelayed(qid, callback, context);
                    }
                } else {
                    console.error(result.reason);
                }
            },
            error: Api.handleAjaxError
        });
    },

    /**
     * Haalt alle mogelijke providers van de server en slaat ze op in providers[]
     *
     * @param {Function} callback De functie uit te voeren indien geslaagd en queue leeg is.
     * In een queue wordt enkel de callback van de laatst geslaagde aanvraag aangroepen (dus maximum 1).
     * @param {Object} context Object die de callback moet uitvoeren
     */
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
                Api.addHeaders(jqXHR);
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
            error: Api.handleAjaxError
        });
    },

    // fetches the live data (= current traffic and an average of last month(s) ) of every route

     /**
     * fetches the live data (= current traffic and an average of last month(s) ) of every route
     * 
     * @param  {number} provider waarvan we data willen opvragen
     * @param {Function} callback De functie uit te voeren indien geslaagd en queue leeg is.
     * In een queue wordt enkel de callback van de laatst geslaagde aanvraag aangroepen (dus maximum 1).
     * @param {Object} context Object die de callback moet uitvoeren
     */
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
                Api.addHeaders(jqXHR);
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
            error: Api.handleAjaxError
        });
    },

    /**
     * fetches graph for today (right up to current time)
     *
     * @param {number} routeId Id van de route waarvoor we de grafiek willen tonen
     * @param  {number} provider waarvan we data willen opvragen
     * @param {Function} callback De functie uit te voeren indien geslaagd en queue leeg is.
     * In een queue wordt enkel de callback van de laatst geslaagde aanvraag aangroepen (dus maximum 1).
     * @param {Object} context Object die de callback moet uitvoeren
     */
    syncLiveGraph: function(routeId, providerId, callback, context) {
        // Bij begin van alle requests uitvoeren. 
        // Hebben deze nodig voor de callback wanneer de request klaar is.
        var qid = this.getQueueId();

        var route = routes[routeId];

        var from = new Date();

        if (route.hasLiveDataRepresentation(providerId)) {
            from = new Date(route.liveData[providerId].representation.timestamp.getTime());
        } else {
            // Kleine trick voor het laden van de grafiek zonder enige representation
            // Bij het toevoegen van een provider aan een grafiek
            // Deze meot dan dezelfde dag zijn als die al werd getoond
             if (route.hasLiveDataRepresentation(Dashboard.provider.id)) {
                from = new Date(route.liveData[Dashboard.provider.id].representation.timestamp.getTime());
            } 
        }
        var to = new Date(from.valueOf());

        
        // Vanaf gisteren - zelfde tijdstip
        from.setDate(from.getDate() - 1);

        var me = this;

        $.ajax({
            type: "GET",
            url: "/api/trafficdata",
            data: {providerID: providerId, routeID: routeId, from: dateToRestString(from), to: dateToRestString(to), interval: this.intervalDecimal*60},
            beforeSend: function (jqXHR, settings) {
                jqXHR.url = settings.url;
                Api.addHeaders(jqXHR);
            },
            success: function(result, status, jqXHR) {
                if (result.result === "success") {
                    var data = {};
                    var avgData = {};
                    var resultdata = result.data;

                    for (var key in resultdata) {
                        var time = stringToDate(key);
                        var hour = time.getHours();
                        var minutes = time.getMinutes();
                        hour += (minutes / 60);

                        if (isNaN(hour)) {
                            console.error('Unreadable date format: "' + key + '" - Make sure the REST server is running the latest version.');
                            continue;
                        }
                        // Enkel die van vandaag tonen
                        if (time.getDate() == to.getDate()) {
                            data[hour] = (resultdata[key].traveltime) / 60;
                        }
                        // Gemiddelde toont ook de gemiddeldes van gisteren als voorspelling
                        avgData[hour] = (resultdata[key].average) / 60;
                    }

                    if (!route.hasLiveData(providerId)) {
                        route.liveData[providerId] = TrafficGraph.create(null);
                    }
                    route.liveData[providerId].intervalDecimal = me.intervalDecimal;
                    route.liveData[providerId].setData(data);
                    route.liveData[providerId].setAvgData(avgData);
                    me.callDelayed(qid, callback, context);
                } else {
                    console.error(result.reason);
                }
            },
            error: Api.handleAjaxError
        });
    },

    /**
     * Fetches the interval data of every route
     *
     * @param {Interval} interval waarvoor we data willen ophalen
     * @param  {number} provider id waarvan we data willen opvragen
     * @param {Function} callback De functie uit te voeren indien geslaagd en queue leeg is.
     * In een queue wordt enkel de callback van de laatst geslaagde aanvraag aangroepen (dus maximum 1).
     * @param {Object} context Object die de callback moet uitvoeren
     */
    syncIntervalData: function(interval, provider, callback, context) {
        // Bij begin van alle requests uitvoeren. 
        // Hebben deze nodig voor de callback wanneer de request klaar is.
        var qid = this.getQueueId();

        var me = this;

        var end = new Date(interval.end.getTime());
        // volgende dag nemen om 0:00
        end.setDate(end.getDate() + 1);
        
        $.ajax({
            type: "GET",
            url: "/api/trafficdata/interval",
            data: {providerID: provider, from: dateToRestString(interval.start), to: dateToRestString(end), slowSpeed: consideredSlowSpeed},
            beforeSend: function (jqXHR, settings) {
                jqXHR.url = settings.url;
                Api.addHeaders(jqXHR);
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
            error: Api.handleAjaxError
        });
        
    },

    /**
     * Fetches the day data of every route
     *
     * @param {Interval} interval waarvoor we data willen ophalen
     * @param  {number} provider id waarvan we data willen opvragen
     * @param {Function} callback De functie uit te voeren indien geslaagd en queue leeg is.
     * In een queue wordt enkel de callback van de laatst geslaagde aanvraag aangroepen (dus maximum 1).
     * @param {Object} context Object die de callback moet uitvoeren
     */
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
                Api.addHeaders(jqXHR);
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
            error: Api.handleAjaxError
        });
        
    },

    /**
     *  Fetches the acumulated interval graph of this route
     *
     * @param {Interval} interval waarvoor we data willen ophalen
     * @param {number} routeid waarvoor we data willen ophalen
     * @param  {number} provider id waarvan we data willen opvragen
     * @param {Function} callback De functie uit te voeren indien geslaagd en queue leeg is.
     * In een queue wordt enkel de callback van de laatst geslaagde aanvraag aangroepen (dus maximum 1).
     * @param {Object} context Object die de callback moet uitvoeren
     */
    syncIntervalGraph: function(interval, routeId, provider, callback, context) {
        // Bij begin van alle requests uitvoeren. 
        // Hebben deze nodig voor de callback wanneer de request klaar is.
        var qid = this.getQueueId();

        var route = routes[routeId];

        var me = this;

        var end = new Date(interval.end.getTime());
        // volgende dag nemen om 0:00
        end.setDate(end.getDate() + 1);

        $.ajax({
            type: "GET",
            url: "/api/trafficdata/weekday",
            data: {providerID: provider, routeID: routeId, from: dateToRestString(interval.start), to: dateToRestString(end), interval: this.intervalDecimal*60},
            beforeSend: function (jqXHR, settings) {
                jqXHR.url = settings.url;
                Api.addHeaders(jqXHR);
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
                        graph.intervalDecimal = me.intervalDecimal;
                        graph.data = data;
                        graph.avgData = avgData;
                    }
                    route.generateIntervalAvg(interval, provider);
                    me.callDelayed(qid, callback, context);
                } else {
                    console.error(result.reason);
                }
            },
            error: Api.handleAjaxError
        });   

    },

    /**
     *  Fetches the day graph of this route
     *
     * @param {Date} day waarvoor we data willen ophalen
     * @param {number} routeid waarvoor we data willen ophalen
     * @param  {number} provider id waarvan we data willen opvragen
     * @param {Function} callback De functie uit te voeren indien geslaagd en queue leeg is.
     * In een queue wordt enkel de callback van de laatst geslaagde aanvraag aangroepen (dus maximum 1).
     * @param {Object} context Object die de callback moet uitvoeren
     */
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
            data: {providerID: provider, routeID: routeId, from: dateToRestString(start), to: dateToRestString(end), interval: this.intervalDecimal*60},
            beforeSend: function (jqXHR, settings) {
                jqXHR.url = settings.url;
                Api.addHeaders(jqXHR);
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
                    graph.intervalDecimal = me.intervalDecimal;

                    me.callDelayed(qid, callback, context);
                } else {
                    console.error(result.reason);
                }
            },
            error: Api.handleAjaxError
        });   
    },

    /**
     *  Voegt de noodzakelijk api key toe aan een aanvraag zijn headers. 
     *  Deze is momenteel hardcoded.
     *
     * @param {jqXHR} request jQuery request waarop toe te passen
     */
    addHeaders: function(request) {
        request.setRequestHeader("x-api-key", "6qKKfkX7u2lmJqxd8RrpLk7m");
    },

    /**
     *  Handel een error af van een request.
     *
     * @param {jqXHR} jqXHR jQuery request
     * @param {jqXHR} textStatus 
     * @param {jqXHR} errorThrown
     */
    handleAjaxError: function(jqXHR, textStatus, errorThrown) {
        console.error("Error while performing request for url : " + jqXHR.url + "\n" + jqXHR.status + " " + errorThrown + ". " + jqXHR.responseText);
    }
};