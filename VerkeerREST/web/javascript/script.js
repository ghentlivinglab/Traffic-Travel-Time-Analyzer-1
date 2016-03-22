// Gebruikte globale variabelen
// Worden in het begin gevraagd aan de API en ingevuld, eenmalig.
var providers = {};
var routes = {};
var events = {};

// Object prototypes die we gaan gebruiken

// TrafficData houdt snelheid en tijd in minuten bij.
// Deze worden opgeslagen in een route of andere objecten
var TrafficData = {
	speed: 0,
	time: 0,
	createdOn: 0
};

// Route bevat een id, naam en afstand. 
// Daarnaast bevat het plaats voor de TrafficData die de gemiddelden bij houdt
// En ook plaats voor TrafficData die de snelheid en tijd op dit moment bij houdt.
// Deze zullen dus telkens aangepast worden nadat we data van de api opvragen.
var Route = {
	id: 0,
	length: 0,
	name: '',
	waypoints: [],

	// avgData is een mapping van de providerId op een TrafficData object
	// We kunnen dus altijd een bepaalde gemiddelde snelheid en tijd lezen voor een bepaalde provider (of alles)
	// Bv. stel Waze heeft id = 2, dan kunnen we de laatst gesynchroniseerde avgData halen door avgData[2],
	// als die niet bestaat, dan moeten we deze nog ophalen uit de API
	avgData: {},

	// liveData is een mapping van de providerId op een TrafficData object, en bevat de meest recente metingen
	liveData: {}
};

// De provider die alle data van alle providers bundelt heeft id = -1
var Provider = {
	id: 0,
	name: ''
};

var Event = {
	id: 0,
	name: '',
	start: 0,
	end: 0
};

// Dummy object dat de communicatie met de API nabootst
// Bevat voornamelijk functies
var DummyApi = {
	syncRoutes: function() {

	}
};

// De gebruikte API zetten we op de dummy
var Api = DummyApi;

// Routes ophalen
Api.syncRoutes();
