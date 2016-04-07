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
	createdOn: null, // instance of Date
	
	create: function(speed, time) { // Constructor
		var obj = Object.create(this);
		obj.speed = speed;
		obj.time = time;
		obj.createdOn = new Date();
		return obj;
	}
};

/****************************
 * object which keeps a TrafficData per time interval and day
 * also maintains a TrafficData which represents the whole graph
 * TODO: translate lines below
 * In de vandaag weergave is dit de laatste bekende 
 * In de dag weergave (bv 12/08/2015) houden we het gemiddelde van de dag bij
 ****************************/
var TrafficGraph = {
	start: 6,
	end: 24,
	interval: 0.25, // every 15 minutes
	createdOn: null,  // instance of Date
	representation: null, // instance of TrafficData

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
	
	// setter for data, changes the creation date
	setData: function(data){
		this.data = data;
		this.createdOn = new Date()
	},
	create: function(representation, data) {
		var obj = Object.create(TrafficGraph);
		obj.representation = representation;
		obj.data = {};

		// dont pass data if it's not defined
		if (data !== undefined){
			// TODO: Hoe zijn we er zeker van dat de structuur die wordt doorgegeven klopt?
			obj.data = data;
			obj.createdOn = new Date();
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
	description: '',
	waypoints: [], // Array of google.maps.LatLng

	// returns status object, based on liveData and avgData
	getStatus: function(liveData, avgData){
		if (liveData.speed < avgData.speed*0.7){
			return {
				text: 'Stilstaand verkeer',
				color: 'red'
			};
		}
		if (liveData.speed < avgData.speed*0.9){
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
	// TODO: translate lines below
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
	hasRecentLiveRepresentation: function(providerId) {
		if (!this.hasLiveData(providerId)){
			return false;
		}
		if ((new Date) - this.liveData[providerId].representation.createdOn > 5*60*1000) { // Als ouder dan 5 minuten -> false
			return false;
		}
		return true;
	},
	hasRecentLiveData: function(providerId) {
		if (!this.hasLiveData(providerId) && Object.keys(this.liveData[providerId].data).length > 0){
			return false;
		}
		if ((new Date) - this.liveData[providerId].createdOn > 5*60*1000) { // Als ouder dan 5 minuten -> false
			return false;
		}
		return true;
	},
	liveData: {

	},

	// TODO: translate
	// Data per dag wordt hiet opgeslagen (in grafiekvorm)
	// Bv voor 4 juli 2015
	// dayData["04/07/2015"][providerId] -> TrafficGraph object
	dayData: {

	},

	// TODO: translate
	// Houdt de grafieken bij:
	// Kan zijn voor maandag, dinsdag, woensdag, ... zondag (0-6) Maandag = 0, zondag = 6
	// bv. eventData["04/07/2015T12:00>08/07/2015T14:00"][maandag][providerId] -> TrafficGraph
	eventData: {

	},

	// Constructor with optional argument waypoints
	create: function(id, name, description, length, waypoints) {
		var obj = Object.create(this);
		obj.id = id;
		obj.length = length;
		obj.name = name;
		obj.description = description;

		// create new references (otherwise we edit the same references for every object)
		obj.avgData = {};
		obj.liveData = {};
		obj.dayData = {};
		obj.eventData = {};

		// Waypoints has to be an Array
		if (waypoints !== undefined && waypoints instanceof Array){
			obj.waypoints = waypoints;
		}
		return obj;
	}
};

/****************************
 *
 * note: provider that has data of every provider has an id-value of -1
 ****************************/
var Provider = {
	id: 0,
	name: '',
	create: function(id, name) {
		var obj = Object.create(this);
		obj.id = id;
		obj.name = name;
		return obj;
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

		// TODO: Hier code om het aan server toe te voegen (als id niet opgegeven)

		// TODO: Toevoegen aan localstorage

		return obj;
	},
	isValid: function() {
		// TODO: additional checks
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

	delete: function() {
		// Delete reference from server
		// Delete reference from localstorage
		// Delete reference from memory
		var index = getEventIndex(this.name);
		if (index != -1){
			events.splice(index, 1);
			this.saveLocalStorage();
		}
		console.log("nieuw delete: "+this.start+" > "+this.end);

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
		var obj = Object.create(this);
		obj.start = start;
		obj.end = end;
		return obj;
	},
	isValid: function() {
		// TODO: additional checks
		return typeof this.end != "undefined" && typeof this.start != "undefined" && this.start && this.end;
	},
	getName: function() {
		if (!this.isValid()){
			return 'Selecteer een periode'; 
		}
		// TODO: aanpassen
		return dateToDate(this.start)+' > '+dateToDate(this.end); 
	},
	setStart: function(start) {
		this.start = start;
	},
	setEnd: function(end) {
		this.end = end;
	}
};
