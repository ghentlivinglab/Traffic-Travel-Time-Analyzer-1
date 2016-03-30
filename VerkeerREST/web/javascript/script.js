// Gebruikte globale variabelen
// Worden in het begin gevraagd aan de API en ingevuld, eenmalig.
var providers = [];
var routes = [];
var events = [];

// Routes ophalen
Api.syncRoutes();



$.getJSON( "http://localhost:8080/VerkeerREST/api/route", function( json ) {
	/*console.log(json.length + " routes gevonden.");
	for(var i=0; i<json.length; i++){
		Route = { id: json[i].id, name: json[i].name, length: json[i].length };
		routes.push(Route);
	}*/
});

$(".collapse").click(togglePanel);

function togglePanel(){
	if($("#dashboard").css("left")==="250px"){
		$("#dashboard").animate({left:-550});
		$(".collapse").children().attr({"src":"images/arrow-right.png","alt":">"});
	} else{
		$("#dashboard").animate({left:250});
		$(".collapse").children().attr({"src":"images/arrow-left.png","alt":"<"});
	}

}

$("article").click(toggleGraph);

function toggleGraph(){
	$(this).find("div.graph-shadow").slideToggle();
	$(this).find("div.graph").slideToggle();
	
	if($(this).css("padding-bottom")==="0px"){
		$(this).animate({"padding-bottom":"320px"});
		$(this).find("div.arrow").delay(400).fadeToggle();
	} else{
		$(this).find("div.arrow").toggle(0);
		$(this).animate({'padding-bottom':"0px"});
	}
}

for(var i=0; i<routes.length; i++){
	console.log(routes[i].id);
}

// leest de key-value paren uit in de URL
// eg:
// bij de url http://test.com/?food=banana&drink=beer
// zal de methode getQueryVariable("food") "banana" teruggeven
function getQueryVariable(variable)
{
       var query = window.location.search.substring(1);
       var vars = query.split("&");
       for (var i=0;i<vars.length;i++) {
               var pair = vars[i].split("=");
               if(pair[0] == variable){return pair[1];}
       }
       return(false);
}

$(document).ready( function(){
	// Enkel op Windows -> scrollbar
	if (navigator.userAgent.indexOf('Mac OS X') == -1) {
		$("#dashboard .content").niceScroll({zindex:999,cursorcolor:"#CCCCCC"});
	}
});

$(window).load( function(){
	if(getQueryVariable("mapView")==="true"){
		togglePanel();
		// window.setTimeout(togglePanel,4000);
	}
});
