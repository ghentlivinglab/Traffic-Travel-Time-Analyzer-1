// Gebruikte globale variabelen
// Worden in het begin gevraagd aan de API en ingevuld, eenmalig.
var providers = {};
var routes = [];
var events = {};

// Object prototypes die we gaan gebruiken

// TrafficData houdt snelheid en tijd in minuten bij.
// Kan bij alles horen: een moment, een dag, een periode, enkel bv maandagen van een periode, een evenement...
// Deze worden opgeslagen in een route of andere objecten
var TrafficData = {
	speed: 0,
	time: 0,
	createdOn: 0 // Aangemaakt op (om geldigheid te kunnen controleren)
};


// Object die trafficdata bijhoudt per tijdsinterval per dag
// Houdt ook 1 TrafficData bij die representatief is voor de hele grafiek
// In de vandaag weergave is dit de laatste bekende 
// In de dag weergave (bv 12/08/2015) houden we het gemiddelde van de dag bij
var TrafficGraph {
	start: 6,
	end: 24,
	interval: 0.25, // kwartier
	createdOn: 0, 
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
	}
}

// Route bevat een id, naam en afstand. 
// Daarnaast bevat het plaats voor de TrafficData die de gemiddelden bij houdt
// En ook plaats voor TrafficData die de snelheid en tijd op dit moment bij houdt.
// Deze zullen dus telkens aangepast worden nadat we data van de api opvragen.
var Route = {
	id: 0,
	length: 0,
	name: '',
	waypoints: [],

	// avgData is een mapping van de providerId op een TrafficGraph object
	// We kunnen dus altijd een bepaalde gemiddelde snelheid en tijd lezen voor een bepaalde provider (of alles)
	// Bv. stel Waze heeft id = 2, dan kunnen we de laatst gesynchroniseerde avgData halen door avgData[2],
	// als die niet bestaat, dan moeten we deze nog ophalen uit de API
	// Bv. avgData[providerId] -> TrafficGraph
	avgData: {

	},

	// liveData is een mapping van de providerId op een TrafficGraph object, en bevat de meest recente metingen
	// Bv. liveData[providerId] -> TrafficGraph
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
	}
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


$.getJSON( "http://localhost:8080/VerkeerREST/api/route", function( json ) {
	/*console.log(json.length + " routes gevonden.");
	for(var i=0; i<json.length; i++){
		Route = { id: json[i].id, name: json[i].name, length: json[i].length };
		routes.push(Route);
	}*/
});

$(".collapse").click(function(){
	$("#dashboard").toggleClass("expand_collapse");

})

$("article").click(function(){
	$(this).find("div.arrow").toggleClass("hidden");
	$(this).find("div.graph-shadow").toggleClass("hidden");
	$(this).find("div.graph").toggleClass("hidden");
	$(this).css("height","68px");
})

for(var i=0; i<routes.length; i++){
	console.log(routes[i].id);
}