/****************************
 * object prototypes in use
 ****************************/

/****************************
 * trafficData maintains speed and travel time
 * could be a representation of a specific moment, day, period, mondays in a period, event, ...
 * will be stored in a route or other object
 ****************************/
var TrafficData = {
	speed: 0,
	time: 0,

	// Percentage boven de normale omstandigheden, in het laatste half uur ofzo (bepaalt door server)
	expceptional: 0,

	timestamp: null, // Tijd die bij deze snelheid en tijd hoort op de polling server
	createdOn: null, // Tijd waarop dit object werd aangemaakt <-> timestamp!
	empty: false,
	
	create: function(speed, time, timestamp) { // Constructor
		var obj = Object.create(this);
		obj.speed = speed;
		obj.time = time;
		obj.timestamp =  new Date(timestamp);
		obj.createdOn = new Date();
		return obj;
	},

	createEmpty: function() {
		var obj = Object.create(this);
		obj.createdOn = new Date();
		obj.empty = true;
		return obj;
	},

	toString: function (){
		if (this.empty) {
			return '';
		}
		return Math.floor(this.time*10)/10+' min. '+Math.floor(this.speed)+' km/h';
	},

	toSpeedString: function() {
		if (this.empty) {
			return '';
		}
		return Math.floor(this.speed)+' km/h';
	},
	toTimeString: function() {
		if (this.empty) {
			return '';
		}
		return Math.floor(this.time*10)/10+' minuten';
	}
};


/****************************
 * IntervalData maintains % slow traffic per weekday and the total
 * will be stored in a TrafficGraph object
 ****************************/
var IntervalRepresentation = {
	speed: 0,
	time: 0,
	unusualPercentage: 0,
	unusual: null,
	empty: false,

	createdOn: null, // instance of Date
	
	create: function(speed, time, unusualPercentage, unusual) { // Constructor
		var obj = Object.create(this);
		obj.unusual = unusual.slice();
		obj.speed = speed;
		obj.time = time;
		obj.unusualPercentage = unusualPercentage;

		obj.createdOn = new Date();
		return obj;
	},
	createEmpty: function() {
		var obj = Object.create(this);
		obj.empty = true;
		obj.unusual = [];

		obj.createdOn = new Date();
		return obj;
	},

	toString: function() {
		if (this.empty){
			return '';
		}
		return Math.floor(this.time)+' min. '+this.speed+' km/h';
	},

	toSpeedString: function() {
		if (this.empty) {
			return '';
		}
		return Math.floor(this.speed)+' km/h';
	},
	toTimeString: function() {
		if (this.empty) {
			return '';
		}
		return Math.floor(this.time*10)/10+' minuten';
	}
};

/****************************
 * object which keeps a TrafficData per time interval and day
 * also maintains a TrafficData which represents the whole graph
 * In de vandaag weergave is dit de laatste bekende 
 * In de dag weergave (bv 12/08/2015) houden we het gemiddelde van de dag bij
 ****************************/
var TrafficGraph = {
	interval: 0.25, // every 15 minutes
	createdOn: null,  // instance of Date
	representation: null, // instance of TrafficData or IntervalRepresentation

	// will contain the TrafficaData per time interval and day
	data: {
		/* eg.
		6: instanceof TrafficData,
		6.25: instanceof TrafficData,
		6.5: instanceof TrafficData,
		6.75: instanceof TrafficData,
		7: instanceof TrafficData,
		7.25: instanceof TrafficData,
		...
		24: instanceof TrafficData
		*/
	},

	// will contain the AVERAGE TrafficaData per time interval and day
	avgData: {

	},
	
	// setter for data, changes the creation date
	setData: function(data){
		this.data = data;
		this.createdOn = new Date()
	},
	setAvgData: function(data){
		this.avgData = data;
	},
	create: function(representation, data, avgData) {
		var obj = Object.create(TrafficGraph);
		obj.representation = representation;
		obj.data = null;
		obj.avgData = null;

		// dont pass data if it's not defined
		if (data !== undefined){
			obj.data = data;
			obj.createdOn = new Date();
		}

		// dont pass data if it's not defined
		if (avgData !== undefined){
			obj.avgData = avgData;
		}
		
		return obj;
	}
};

/****************************
 * Route contains an id, name and distance
 * also contains location for TrafficData that keeps averages 
 * and contains location for TrafficData with live speed and time
 * will be updated with every API-call
 ****************************/
var Route = {
	id: 0,
	length: 0,
	name: '',
	speedLimit: 30,
	description: '',
	waypoints: [], // Array of google.maps.LatLng

	// Constructor with optional argument waypoints
	create: function(id, name, description, length, speedLimit, waypoints) {
		var obj = Object.create(this);
		obj.id = id;
		obj.length = length;
		obj.name = name;
		obj.description = description;
		obj.speedLimit = speedLimit;

		// create new references (otherwise we edit the same references for every object)
		obj.avgData = {};
		obj.liveData = {};
		obj.dayData = {};
		obj.intervalData = {};

		// Waypoints has to be an Array
		if (waypoints !== undefined && waypoints instanceof Array){
			obj.waypoints = waypoints.slice();
		}else{
			obj.waypoints = []; // Belangrijk! (referentie probleem anders)
		}
		return obj;
	},

	/**
	 * Maak een route op basis van data uit de localStorage
	 * 
	 * @param  {[type]}
	 * @return {[type]}
	 */
	createFromStorage: function(data) {
		return Route.create(data.id, data.name, data.description, data.length, data.speedLimit, data.waypoints);
	},

	/**
	 * Sla alle routes op in de localStorage, maar zonder hun data.
	 * Enkel de algemene data zoals id, length, .. en waypoints
	 */
	saveRoutes: function() {
		var routesCopy = [];
		for (var i in routes) {
			var route = routes[i];
			var copy = Route.create(route.id, route.name, route.description, route.length, route.speedLimit, route.waypoints);
			routesCopy.push(copy);
		}

		localStorage.setItem('routes', JSON.stringify(routesCopy));
	},

	/**
	 * Haal routes op uit de local storage.
	 * Returnt false indien mislukt.
	 */
	loadRoutes: function() {
		var retrievedObject = localStorage.getItem('routes');
		try{
			var parsed = JSON.parse(retrievedObject);
			if (Array.isArray(parsed)){
				// Na het parsen zijn alle methodes verdwenen. Deze voegen we terug toe
				routes = [];

				for (var i = 0; i < parsed.length; i++) {
					var parse = parsed[i];
					var route = Route.createFromStorage(parse);
					routes[route.id] = route;
				}
			}
			return routes.length > 0;
		} catch (e) {
			return false;
		}
	},

	getDescription: function () {
		return this.description;
	},
	getLength: function () {
		return Math.floor(this.length / 100)/10 + " km";
	},

	getColor: function(representation) {
		if (representation.empty){
			return 'gray';
		}
		if (representation.speed/this.speedLimit < 0.5){
			return 'red';
		}
		if (representation.speed/this.speedLimit < 0.7){
			return 'orange';
		}
		return 'green';
	},

	getUnusualColor: function(representation) {
		if (representation.empty){
			return 'gray';
		}
		if (representation.unusualPercentage < 40){
			return 'red';
		}
		if (representation.unusualPercentage > 60){
			return 'green';
		}
		return 'orange';
	},

	getStatusFor: function(representation) {
		if (representation.unusualPercentage < 40){
			return {
				name: 'Trager dan gemiddeld',
				index: 10
			};
		}
		if (representation.unusualPercentage > 60){
			return {
				name: 'Sneller dan gemiddeld',
				index: 5
			};
		}
		return {
				name: 'Gelijk met gemiddelde',
				index: 0
			};
	},

	// Returnt status voor live situatie indien geen paramters gegeven
	// Geeft anders een status voor de opgegeven representatie(s)
	getStatus: function(liveData,  avgData){
		if (liveData.empty || avgData.empty) {
			return {
				text: 'Niet beschikbaar',
				color: 'gray'
			};
		}
		if (liveData.speed < 20){
			return {
				text: 'Stilstaand verkeer',
				color: 'red'
			};
		}

		if (liveData.speed < 30 || (avgData.speed > 50 && liveData.speed < 50)){
			return {
				text: 'Traag verkeer',
				color: 'orange'
			};
		}

		return {
			text: 'Vlot verkeer',
			color: 'green'
		};
	},

	// Geeft terug of de live situatie normaal is of niet
	isExceptional: function(providerId) {
		if (!this.hasAvgData(providerId)) {
			return false;
		}
		if (!this.hasLiveDataRepresentation(providerId)) {
			return false;
		}

		var liveData = this.liveData[providerId].representation;
		var avgData = this.avgData[providerId].representation;

		return liveData.speed < this.speedLimit-10 && liveData.speed < avgData.speed * 0.6;
	},

	// avgData is een mapping van de providerId op een TrafficGraph object
	// We kunnen dus altijd een bepaalde gemiddelde snelheid en tijd lezen voor een bepaalde provider (of alles)
	// Bv. stel Waze heeft id = 2, dan kunnen we de laatst gesynchroniseerde avgData halen door avgData[2],
	// als die niet bestaat, dan moeten we deze nog ophalen uit de API
	// Bv. avgData[providerId] -> TrafficGraph
	hasAvgData: function(providerId) {
		return typeof this.avgData[providerId] != "undefined";
	},
	hasRecentAvgRepresentation: function(providerId) {
		if (!this.hasAvgData(providerId)){
			return false;
		}
		if ((new Date) - this.avgData[providerId].representation.createdOn > 5*60*1000) { // Als ouder dan 5 minuten -> false
			return false;
		}
		return true;
	},
	hasRecentAvgData: function(providerId) {
		if (!this.hasAvgData(providerId) && Object.keys(this.avgData[providerId].data).length > 0){
			return false;
		}
		if ((new Date) - this.avgData[providerId].createdOn > 5*60*1000) { // Als ouder dan 5 minuten -> false
			return false;
		}
		return true;
	},
	avgData: {

	},

	// LiveData is a mapping of a providerId on a TrafficGraph object that contains the most recent measurements
	// eg: liveData[providerId] -> TrafficGraph
	hasLiveData: function(providerId) {
		return typeof this.liveData[providerId] != "undefined";
	},

	hasLiveDataRepresentation: function(providerId) {
		return typeof this.liveData[providerId] != "undefined" && this.liveData[providerId].representation;
	},

	hasRecentLiveRepresentation: function(providerId) {
		if (!this.hasLiveData(providerId) || !this.liveData[providerId].representation){
			return false;
		}
		if ((new Date) - this.liveData[providerId].representation.createdOn > 5*60*1000) { // Als ouder dan 5 minuten -> false
			return false;
		}
		return true;
	},
	hasRecentLiveData: function(providerId) {
		if (!this.hasLiveData(providerId) || !this.liveData[providerId].data){
			return false;
		}
		if ((new Date) - this.liveData[providerId].createdOn > 5*60*1000) { // Als ouder dan 5 minuten -> false
			return false;
		}
		return true;
	},
	liveData: {

	},

	getDayData: function(day, providerId) {
		var str = dateToDate(day);
		if (typeof this.dayData[str] == "undefined"){
			return null; // undefined
		}
		if (typeof this.dayData[str][providerId] == "undefined"){
			return null; // undefined
		}
		return this.dayData[str][providerId];
	},

	// returns trafficGraph for interval, or null
	setDayData: function(day, providerId, value) {
		var str = dateToDate(day);
		if (typeof this.dayData[str] == "undefined"){
			this.dayData[str] = {};
		}
		this.dayData[str][providerId] = value;
	},
	// Information per day is stored here (in a graph)
	// eg: 4 july 2015
	// dayData["04/07/2015"][providerId] -> TrafficGraph object
	dayData: {
		
	},

	// returns trafficGraph for interval, or null
	getIntervalData: function(interval, day, providerId) {
		var str = interval.toString();
		if (typeof this.intervalData[str] == "undefined"){
			return null; // undefined
		}
		if (typeof this.intervalData[str][day] == "undefined"){
			return null; // undefined
		}
		if (typeof this.intervalData[str][day][providerId] == "undefined"){
			return null; // undefined
		}
		return this.intervalData[str][day][providerId];
	},

	// returns trafficGraph for interval, or null
	setIntervalData: function(interval, day, providerId, value) {
		var str = interval.toString();
		if (typeof this.intervalData[str] == "undefined"){
			this.intervalData[str] = {};
		}
		if (typeof this.intervalData[str][day] == "undefined"){
			this.intervalData[str][day] = {};
		}
		this.intervalData[str][day][providerId] = value;
	},

	// calculates the average of all weekdays (not given by api)
	// stores it in IntervalData under day=7 (all weekdays)
	generateIntervalAvg: function(interval, providerId) {
		var data = {};
		for (var day = 0; day < 7; day++) {
			var graph = this.getIntervalData(interval, day, providerId);
			if (!graph){
				continue;
			}
			for (var time in graph.data) {
				
				if (!data[time]) {

					data[time] = {
						value: 0,
						avgValue: 0,
						count: 0,
						avgCount: 0
					};
				}
				data[time].value += graph.data[time];
				data[time].count ++;
			}
			for (var time in graph.avgData) {
				
				if (!data[time]) {

					data[time] = {
						value: 0,
						avgValue: 0,
						count: 0,
						avgCount: 0
					};
				}
				data[time].avgValue += graph.avgData[time];
				data[time].avgCount ++;
			}
		}

		var result = {};
		var avgResult = {};
		for (var time in data) {
			if (data[time].count > 0) {
				result[time] = data[time].value / data[time].count;
			}
		}
		for (var time in data) {
			if (data[time].avgCount > 0) {
				avgResult[time] = data[time].avgValue / data[time].avgCount;
			}
		}

		var graph = this.getIntervalData(interval, 7, providerId)
		if (!graph){
			graph = TrafficGraph.create(null);
			this.setIntervalData(interval, 7, providerId, graph);
		}
		graph.data = result;
		graph.avgData = avgResult;
	},

	// returns representation for given interval, or null if non-existant
	getIntervalDataRepresentation: function(interval, day, providerId) {
		if (!this.getIntervalData(interval, day, providerId)){
			return null;
		}
		return this.getIntervalData(interval, day, providerId).representation;
	},
	
	// maintains graphs for each day (0-6) or all days (7)
	// eg intervalData["04/07/2015T12:00>08/07/2015T14:00"][0][providerId] -> TrafficGraph
	// met de 1e parameter de toString van een interval of event object (geven hetzelfde weer voor hetzelfde datum en uur bereik)
	intervalData: {

	}
};

/****************************
 *
 ****************************/
var Provider = {
	id: 0,
	name: '',
	create: function(id, name) {
		var obj = Object.create(this);
		obj.id = id;
		obj.name = name;
		return obj;
	},
	getUrlString: function() {
		// Spaties verwijderen (niet leesbaar in url met %20)
		return this.name.replace(/\s+/g, '');
	},

	/**
	 * Maak een Provider op basis van data uit de localStorage
	 * 
	 * @param  {[type]}
	 * @return {[type]}
	 */
	createFromStorage: function(data) {
		return Provider.create(data.id, data.name);
	},

	/**
	 * Sla alle Providers op in de localStorage, maar zonder hun data.
	 * Enkel de algemene data zoals id, length, .. en waypoints
	 */
	saveProviders: function() {
		var providersCopy = [];
		for (var i in routes) {
			var provider = providers[i];
			var copy = Provider.create(provider.id, provider.name);
			providersCopy.push(copy);
		}

		localStorage.setItem('providers', JSON.stringify(providersCopy));
	},

	/**
	 * Haal Providers op uit de local storage.
	 * Returnt false indien mislukt.
	 */
	loadProviders: function() {
		var retrievedObject = localStorage.getItem('providers');
		try{
			var parsed = JSON.parse(retrievedObject);
			if (Array.isArray(parsed)){
				// Na het parsen zijn alle methodes verdwenen. Deze voegen we terug toe
				providers = [];

				for (var i = 0; i < parsed.length; i++) {
					var parse = parsed[i];
					var provider = Provider.createFromStorage(parse);
					providers[Provider.id] = provider;
				}
			}
			return providers.length > 0;
		} catch (e) {
			return false;
		}
	}

};

/****************************
 * Event is an interval saved in memory, localstorage and on the server
 ****************************/
var Event = {
	// id wordt enkel gebruikt voor communicatie met de server
	id: -1, // Van de server gehaald, anders -1 (niet gesynct)
	name: '', // Naam moet altijd uniek zijn
	start: null, // instance of Date
	end: null, // instance of Date
	hasName: true,

	// Nieuw event van interval element
	new: function(interval) {
		if (interval.isValid()){
			return this.create(interval.getName(), interval.start, interval.end)
		}else{
			return this.create('Interval', interval.start, interval.end)
		}
	},

	createFromStorage: function(object) {
		// Komt rechtstreeks uit localstorage. Geen checks op doen. Events zou leeg moeten zijn.
		var obj = Object.create(Event);
		obj.name = object.name;

		// javascript JSON houdt date bij in string formaat -> hieronder omzetten in date objecten
		obj.start = new Date(object.start);
		obj.end = new Date(object.end);

		return obj;
	},

	create: function(name, start, end){
		// copy Prototype
		var obj = Object.create(Event);
		obj.start = start;
		obj.end = end;
		obj.name = null;

		// Toevoegen aan events, en naam aanvullen als deze al in gebruik is:
		obj.setName(name);

		return obj;
	},
	isValid: function() {
		return typeof this.end != "undefined" && typeof this.start != "undefined" && this.start && this.end;
	},
	setStart: function(start) {
		this.start = start;
		this.saveLocalStorage();
	},

	setEnd: function(end) {
		this.end = end;
		this.saveLocalStorage();
	},

	// iteration is enkel voor de recursieve werking, en is een optionele parameter
	setName: function(name, iteration) {
		var n = name;
		if (typeof iteration == 'undefined') {
			iteration = 1;
		} else {
			n += '-'+iteration;
		}

		if (this.name && n == this.name){
			return;
		}

		if (getEventIndex(n) != -1) {
			// Nope!
			iteration++;
			this.setName(name, iteration);
			return;
		}
		var index = getEventIndex(this.name);
		if (index != -1){
			events[index] = this;
		}else{
			events.push(this);
		}

		this.name = n;
		this.saveLocalStorage();
	},
	getName: function() {
		return this.name;
	},

	isEmpty: function() {
		return false;
	},

	delete: function() {
		// Delete reference from server
		// Delete reference from localstorage
		// Delete reference from memory
		var index = getEventIndex(this.name);
		if (index != -1){
			events.splice(index, 1);
			this.saveLocalStorage();
		}

		// Replace reference with a new interval object
		return Interval.create(this.start, this.end);
	},

	saveLocalStorage: function() {
		localStorage.setItem('events', JSON.stringify(events));
	},

	loadLocalStorage: function() {	
		// Retrieve the object from storage
		try{
			var retrievedObject = localStorage.getItem('events');
			var parsed = JSON.parse(retrievedObject);
			if (Array.isArray(parsed)){
				// Na het parsen zijn alle methodes verdwenen. Deze voegen we terug toe
				events = [];

				for (var i = 0; i < parsed.length; i++) {
					var parse = parsed[i];
					events.push(this.createFromStorage(parse));
				}
			}
		} catch (e) {
			// Fout in storage -> vervangen die handel
			this.saveLocalStorage();
		}
	},

	toString: function() {
		return dateToDate(this.start)+' > '+dateToDate(this.end); 
	}
};

/****************************
 * Interval for temporary usage, max 1 reference
 ****************************/
var Interval = {
	start: null, // instance of Date
	end: null, // instance of Date
	hasName: false,
	create: function(start, end){
		// copy Prototype
		var obj = Object.create(Interval);
		obj.start = start;
		obj.end = end;
		return obj;
	},
	createFromStorage: function(object) {
		// Komt rechtstreeks uit localstorage. Geen checks op doen. Events zou leeg moeten zijn.
		var obj = Object.create(Interval);
		// javascript JSON houdt date bij in string formaat -> hieronder omzetten in date objecten
		obj.start = new Date(object.start);
		obj.end = new Date(object.end);

		return obj;
	},
	isValid: function() {
		return typeof this.end != "undefined" && typeof this.start != "undefined" && this.start && this.end;
	},
	isEmpty: function() {
		return (typeof this.end == "undefined" || !this.end) && typeof (this.start == "undefined" || !this.start);
	},
	getName: function() {
		if (!this.isValid()){
			return 'Selecteer een periode'; 
		}
		return dateToDate(this.start)+' > '+dateToDate(this.end); 
	},
	copy: function(object) {
		var obj = Object.create(Interval);

		if (!object.start || typeof object.start == "undefined"){
			obj.start = null;
		}else{
			obj.start = new Date(object.start.valueOf());
		}

		if (!object.end || typeof object.end == "undefined"){
			obj.end = null;
		}else{
			obj.end = new Date(object.end.valueOf());
		}
		return obj;
	},
	equals: function(interval) {
		if (!this.isValid()){
			if (!interval.isValid()){
				return true;
			}else{
				return false;
			}
		}
		if (!interval.isValid()){
			return false;
		}

		return this.start.getTime() == interval.start.getTime() && this.end.getTime() == interval.end.getTime();
	},
	setStart: function(start) {
		this.start = start;
	},
	setEnd: function(end) {
		this.end = end;
	},

	toString: function() {
		return dateToString(this.start)+' > '+dateToString(this.end); 
	}
};
