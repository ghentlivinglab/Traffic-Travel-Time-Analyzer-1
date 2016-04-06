/****************************
 * global variables
 * API fills these once only
 ****************************/
var providers = [];
var routes = [];
var events = [];

/****************************
 * Small extension of Mustache
 * renders a template from a script-tag
 * renderTemplate('route', {name: "...", ...});
 ****************************/
Mustache.renderTemplate = function (template_name, view) {
	var template = document.getElementById("template-"+template_name).innerHTML;
	Mustache.parse(template); // Speeding things up in the future
 	return Mustache.render(template, view);
}


/****************************
 * toggles the display of the overview panel
 ****************************/
function togglePanel(){
	if($("#dashboard").css("left")==="250px"){ // panel is shown
		$("#dashboard").animate({left:-550}); // hides panel to the left
		$(".collapse").children().attr({"src":"images/arrow-right.png","alt":">"}); // changes the display of the button
	} else{ // panel is not shown
		$("#dashboard").animate({left:250}); // shows panel
		$(".collapse").children().attr({"src":"images/arrow-left.png","alt":"<"}); // changes the display of the button
	}

}

/****************************
 * Open / sluit popup in popupable element
 * optionele parameter close om sluiten te forceren (maakt niet uit welke waarde die krijgt, zodra gegeven -> sluiten)
 ****************************/
function togglePopup(close){
	console.log("togglePopup");
	var anchor = $(this).find('.popup-anchor');
	if (close === undefined && !anchor.hasClass('open')){
		anchor.addClass('open');
	}else{
		anchor.removeClass('open');
	}
}

/****************************
 * reads the key-value pairs from the location-URL
 * eg: for the URL http://test.com/?food=banana&drink=beer the method getQueryVariable("food") returns "banana"
 ****************************/
function getQueryVariable(variable){
       var query = window.location.search.substring(1); // removes '?'
       var vars = query.split("&"); // splits the key-value pairs
       for (var i=0;i<vars.length;i++) {
               var pair = vars[i].split("="); // splits keys from values
               if(pair[0] == variable){return pair[1];} //returns value when requested key has been found
       }
       return false; // key not found
}

/****************************
 * Wordt uitgevoerd als er nieuwe content wordt toegevoegd of herladen. Hierbij is this altijd de root element die alle aanpassingen omvat.
 * Alles bindings moeten hierop dus opnieuw uitgevoerd worden
 ****************************/
function thisReady(){
	// Propagation disablen op popup box (dat deze ook niet sluit bij onlick van de popupable parent)
	$(this).find('.popupable').click(function(event) {
		togglePopup.call(this);
		event.stopPropagation();
	});

	$(this).find('.popup-box').click(function(event) {
		console.log("Popup prevented closing");
		event.stopPropagation();
	});

	// Scrollbalken
	if (navigator.userAgent.indexOf('Mac OS X') == -1) {
		$(this).find(".popup-scroll").niceScroll({zindex:999,cursorcolor:"#CCCCCC"});
	}
}

/****************************
 * runs when DOM-tree is finished
 ****************************/
$(document).ready( function(){
	
	// runs the nice-scrollbar script on non-Mac devices
	if (navigator.userAgent.indexOf('Mac OS X') == -1) {
		$("#dashboard .content").niceScroll({zindex:999,cursorcolor:"#CCCCCC"});
	}

	thisReady.call(document);
	
	// adds a click listener to the collapse-button
	$(".collapse").click(togglePanel);

	// Popups sluiten als er naast wordt geklikt 
	// (door stop propagation zal dit nooit uitgevoerd worden als er op de popup geklikt wordt)
	$(document).click(function () {
		$('.popupable').each(function() {
			togglePopup.call(this, true);
		});
	});

	// initialises dashboard
	Dashboard.init();
});

/****************************
 * runs when DOM-tree is finished and all objects from it are loaded
 ****************************/
$(window).load( function(){
	
	// checks if mapView or overview has to be displayed
	if(getQueryVariable("mapView")==="true"){
		togglePanel();
	}
});
