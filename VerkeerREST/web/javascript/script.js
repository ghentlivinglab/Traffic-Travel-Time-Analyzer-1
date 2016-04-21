/****************************
 * global variables
 * API fills these once only
 ****************************/
var providers = [];
var routes = [];
var events = [];

// Weekdays
var weekdays = ['Maandag', 'Dinsdag', 'Woensdag', 'Donderdag', 'Vrijdag', 'Zaterdag', 'Zondag'];
var weekdays_short = ['ma', 'di', 'wo', 'do', 'vr', 'za', 'zo'];

var weekdays_js_to_rest = [6,0,1,2,3,4,5];

// Inladen van local storage
Event.loadLocalStorage();


function getEventIndex(name){
	for (var i = 0; i < events.length; i++) {
		if (events[i].getName() == name){
			return i;
		}
	}
	return -1;
}

var entityMap = {
"&": "&amp;",
"<": "&lt;",
">": "&gt;",
'"': '&quot;',
"'": '&#39;',
"/": '&#x2F;'
};

function escapeHtml(string) {
return String(string).replace(/[&<>"'\/]/g, function (s) {
  return entityMap[s];
});
}

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
	var anchor = $(this).find('.popup-anchor');
	if (close === undefined && !anchor.hasClass('open')){
		anchor.addClass('open');
	}else{
		// Voorkomen dat Dashboard.intervalsDidChange bij elke klik wordt uitgevoerd -> enkel bij sluiten
		if (anchor.hasClass('open')){
			if ($(this).hasClass('period-selection')) {
				onClosePeriodSelection.call(this);
			}
			anchor.removeClass('open');
		}
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

// simuleert event.stopPropagation(); zonder de datepicker kapot te maken (die moet namelijk ook events ontvangen!)
var clickedOnPopup = false; 

function thisReady(){
	// Propagation disablen op popup box (dat deze ook niet sluit bij onlick van de popupable parent)
	$(this).find('.popupable').click(function(event) {
		if (!clickedOnPopup){
			if ($(this).hasClass('period-selection')){
				onOpenPeriodSelection.call(this);
			}

			togglePopup.call(this);
			clickedOnPopup = true;
		}
	});

	// Alle functinaliteiten toevoegen (= binds) bv. onClick, onChange, ...
	// voor period selections in this
	bindPeriodSelection.call(this);


	$(this).find('.popup-box').click(function(event) {
		clickedOnPopup = true;
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
	thisReady.call(document);
	
	// adds a click listener to the collapse-button
	$(".collapse").click(togglePanel);

	// Popups sluiten als er naast wordt geklikt 
	// (door stop propagation zal dit nooit uitgevoerd worden als er op de popup geklikt wordt)
	$(document).click(function () {
		if (clickedOnPopup){
			clickedOnPopup = false;
			return;
		}
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
	var showDashboard = getQueryVariable("dashboardView")==="true"; // checks if URL contains directives
        var dashboardShown = $("#dashboard").css("left")==="250px"; // checks in which state the dashboard currently resides
	if( showDashboard ? !dashboardShown : dashboardShown ){
		togglePanel();
	}
});
