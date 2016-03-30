// Object prototypes die we gaan gebruiken

// TrafficData houdt snelheid en tijd in minuten bij.
// Kan bij alles horen: een moment, een dag, een periode, enkel bv maandagen van een periode, een evenement...
// Deze worden opgeslagen in een route of andere objecten
var TrafficData = {
	speed: 0,
	time: 0,
	createdOn: null, // Type = Date
	create: function(speed, time) {
		var obj = Object.create(this);
		obj.speed = speed;
		obj.time = time;
		obj.createdOn = new Date();
		return obj;
	}
};


// Object die trafficdata bijhoudt per tijdsinterval per dag
// Houdt ook 1 TrafficData bij die representatief is voor de hele grafiek
// In de vandaag weergave is dit de laatste bekende 
// In de dag weergave (bv 12/08/2015) houden we het gemiddelde van de dag bij
var TrafficGraph = {
	start: 6,
	end: 24,
	interval: 0.25, // kwartier
	createdOn: null, 
	representation: null, // instanceof TrafficData

	// Houdt de data bij
	data: {
		/* Bv.
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
	create: function(representation, data) {
		var obj = Object.create(TrafficGraph);
		obj.representation = representation;

		// Als data niet gedefinieerd: niet doorgeven
		if (data !== undefined){
			// TODO: Hoe zijn we er zeker van dat de structuur die wordt doorgegeven klopt?
			obj.data = data;
		}

		obj.createdOn = new Date();

		return obj;
	}
};

// Route bevat een id, naam en afstand. 
// Daarnaast bevat het plaats voor de TrafficData die de gemiddelden bij houdt
// En ook plaats voor TrafficData die de snelheid en tijd op dit moment bij houdt.
// Deze zullen dus telkens aangepast worden nadat we data van de api opvragen.
var Route = {
	id: 0,
	length: 0,
	name: '',
	description: '',
	waypoints: [], // Array van google.maps.LatLng

	// Geeft status object terug op basis van de liveData en avgData
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
	avgData: {

	},

	// liveData is een mapping van de providerId op een TrafficGraph object, en bevat de meest recente metingen
	// Bv. liveData[providerId] -> TrafficGraph
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
	liveData: {

	},

	// Data per dag wordt hiet opgeslagen (in grafiekvorm)
	// Bv voor 4 juli 2015
	// dayData["04/07/2015"][providerId] -> TrafficGraph object
	dayData: {

	},

	// Houdt de grafieken bij:
	// Kan zijn voor maandag, dinsdag, woensdag, ... zondag (0-6) Maandag = 0, zondag = 6
	// bv. data[maandag][providerId] -> TrafficGraph
	eventData: {

	},

	// Constructor, waypoints is een optioneel argument
	create: function(id, name, description, length, waypoints) {
		var obj = Object.create(this);
		obj.id = id;
		obj.length = length;
		obj.name = name;
		obj.description = description;

		// Nieuwe referenties maken (anders passen we dezelfde referenties aan voor alle objeten)
		obj.avgData = {};
		obj.liveData = {};
		obj.dayData = {};
		obj.eventData = {};

		// Waypoints moet een array zijn
		if (waypoints !== undefined && waypoints instanceof Array){
			obj.waypoints = waypoints;
		}
		return obj;
	}
};

// De provider die alle data van alle providers bundelt heeft id = -1
var Provider = {
	id: 0,
	name: '',
	create: function(id, name) {
		var obj = Object.create(this);
		obj.id = id;
		return obj;
	}
};

var Event = {
	id: 0,
	name: '',
	start: null, // Object van Date. Zie http://www.w3schools.com/jsref/jsref_obj_date.asp
	end: null, // Object Date
	create: function(id, name, start, end){
		// Prototype kopieëren
		var obj = Object.create(this);
		obj.id = id;
		obj.name = name;
		obj.start = start;
		obj.end = end;
		return obj;
	}
};