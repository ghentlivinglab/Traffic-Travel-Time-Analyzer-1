// Gebruikte globale variabelen
// Worden in het begin gevraagd aan de API en ingevuld, eenmalig.
var providers = [];
var routes = [];
var events = [];

// Kleine uitbreiding op mustache
// Om een  template te renderen die in onze script tags gedefineerd staan
// Gebeurt met Mustache
// renderTemplate('route', {name: "...", ...});
Mustache.renderTemplate = function (template_name, view) {
	var template = document.getElementById("template-"+template_name).innerHTML;
	Mustache.parse(template); // Speeding things up in the future
 	return Mustache.render(template, view);
}

function togglePanel(){
	if($("#dashboard").css("left")==="250px"){
		$("#dashboard").animate({left:-550});
		$(".collapse").children().attr({"src":"images/arrow-right.png","alt":">"});
	} else{
		$("#dashboard").animate({left:250});
		$(".collapse").children().attr({"src":"images/arrow-left.png","alt":"<"});
	}

}




function toggleGraph(){
	var box = $(this).find('.graph-box');
	if(box.length == 0 || !box.is(":visible")){
		// Grafiek toevoegen
		// TODO: data doorgeven (moet ook in deze functie enzo)

		if (box.length == 0){
			$(this).append('<div class="graph-box"><div class="arrow"></div><div class="graph-shadow"></div><div class="graph"><div class="graph-content"><img class="loading" src="images/loading.gif" alt="Bezig met laden"></div></div></div>');
			box = $(this).find('.graph-box');
			console.log('grafiek toegevoegd '+box);
		}

		var graphContent = box.find('.graph-content');

		// Small hack to find the right width and height for the chart
		// Prevent duplicate code here -> no hardcoded height, only in css files
		box.css({ visibility: "hidden", display: "block" });
		var width = graphContent.outerWidth();
		var height = graphContent.outerHeight();
		box.css({ visibility: "", display: "" });

		Dashboard.openLiveGraph($(this).attr('data-route'), graphContent[0], width, height);

		box.slideDown('fast', function() {
			
		});

	} else{
		box.slideUp('fast', function() {
			// Zouden we eigenlijk kunnen houden, maar de grafieken zijn nogal zwaar voor een browser
			// Het is dus beter om ze te verwijderen en opnieuw aan te maken wanneer nodig
			// De data die ze bevatten is toch gecached in de objecten
			box.remove();
		});
	}
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
	$("article").click(toggleGraph);
	$(".collapse").click(togglePanel);

	Dashboard.init();
});

$(window).load( function(){
	if(getQueryVariable("mapView")==="true"){
		togglePanel();
	}
});
