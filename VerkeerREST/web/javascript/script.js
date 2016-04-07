/****************************
 * global variables
 * API fills these once only
 ****************************/
var providers = [];
var routes = [];
var events = [];

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
				Dashboard.intervalsDidChange();
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
 * Converteer functies (formaat is voor achter de schermen, niet voor display!) Niet aanpassen aub
 ****************************/

function dateToDate(date){
	if (!date){
		return '';
	}
	var day = date.getDate();
	var month = date.getMonth();
	var year = date.getFullYear();
	return day+"/"+month+"/"+year;
}
function dateToTime(date){
	if (!date){
		return '';
	}
	var hours = date.getHours();
	var minutes = date.getMinutes();
	return hours+":"+minutes;
}

// Vult automatisch de periode popup in, o.a. net voor het openen of na het aanpassen van het geselecteerde interval.
//  Hierbij is this = class .period-selection
var ignoreChangePickadate = false;
function fillPeriodSelectionInputs(){
	var interval = Dashboard.selectedIntervals[$(this).attr('data-num')];

	// Popup inhoud juist zetten

	// TODO: Alle events toevoegen

	// Edit properties juist zetten
	var fromDate = $(this).find('.from.datepicker');
	var fromTime = $(this).find('.from.timepicker');
	var toDate = $(this).find('.to.datepicker');
	var toTime = $(this).find('.to.timepicker');

	ignoreChangePickadate = true;
	if (interval.start){
		fromDate.pickadate('picker').set('select', interval.start);
		fromTime.pickatime('picker').set('select', interval.start);
	}
	else {
		fromDate.pickadate('picker').clear();
		fromTime.pickatime('picker').clear();
	}

	if (interval.end){
		toDate.pickadate('picker').set('select', interval.end);
		toTime.pickatime('picker').set('select', interval.end);
	}
	else {
		toDate.pickadate('picker').clear();
		toTime.pickatime('picker').clear();
	}
	ignoreChangePickadate = false;

	$(this).find('.view-edit .name').val(interval.getName());

	// Heeft een naam?
	if (interval.hasName) {
		$(this).find('.view-edit').removeClass('no-name');
	}else{
		$(this).find('.view-edit').addClass('no-name');
	}

}

// Haalt de waarde van de inputs uit de popup en slaat deze op in het juiste interval element (1 of 2) van dashoboard
function savePeriodSelectionInputs() {
	var interval = Dashboard.selectedIntervals[$(this).attr('data-num')];

	if (interval.hasName){
		interval.setName($(this).find('.view-edit .name').val());
	}

	var fromDate = $(this).find('.from.datepicker').pickadate('picker').get('select');
	var fromTime = $(this).find('.from.timepicker').pickatime('picker').get('select');
	var toDate = $(this).find('.to.datepicker').pickadate('picker').get('select');
	var toTime = $(this).find('.to.timepicker').pickatime('picker').get('select');

	if (fromDate && fromTime){
		interval.setStart(new Date(fromDate.year, fromDate.month, fromDate.date, fromTime.hour, fromTime.mins));
	} else if(fromDate) {
		interval.setStart(new Date(fromDate.year, fromDate.month, fromDate.date));
	}

	if (toDate && toTime){
		interval.setEnd(new Date(toDate.year, toDate.month, toDate.date, toTime.hour, toTime.mins));
	} else if(toDate) {
		// TODO: Dit werkt niet (wordt genegeerd en tijd wordt 0)
		interval.setEnd(new Date(toDate.year, toDate.month, toDate.date, 23, 59));
	}

}

// Zet de name van de input
function updatePeriodSelectionName() {
	var interval = Dashboard.selectedIntervals[$(this).attr('data-num')];

	// Aanpassen
	var name = $(this).children('.name');
	name.text(interval.getName());
}

// Vult de events aan
function fillPeriodSelectionEvents(){
	var container = $(this).find('.popup-period-items');
	var str = '';

	for (var i = 0; i < events.length; i++) {
		var event = events[i];
		str += '<div class="item" data-index="' + i + '"><span>'+escapeHtml(event.getName())+'</span><div class="edit-period"></div></div>';
	}

	container.html(str);

	container.find('.item span').click(function(e) {
		e.preventDefault();
		var popup = $(this).parents('.period-selection');
		var event = events[$(this).parent().attr('data-index')];
		if (event){
			Dashboard.selectedIntervals[popup.attr('data-num')] = event;
			updatePeriodSelectionName.call(popup[0]);
			togglePopup.call(popup[0], true);
		}
	});
	container.find('.item .edit-period').click(function(e) {
		e.preventDefault();
		var popup = $(this).parents('.period-selection');
		var event = events[$(this).parent().attr('data-index')];
		if (event){
			Dashboard.selectedIntervals[popup.attr('data-num')] = event;
			updatePeriodSelectionName.call(popup[0]);
			fillPeriodSelectionInputs.call(popup[0]);
			popup.addClass('edit');
		}
	});
}

// Uitgevoerd als de period selection popup wordt gesloten
// Hierna moeten we de change handler van het dashboard aanroepen
function onClosePeriodSelection() {

}

// Uitgevoerd net voor de period selection popup wordt geopend
function onOpenPeriodSelection() {
	fillPeriodSelectionInputs.call(this);
	// Terug naar niet edit weergave, indien we daarin waren
	$(this).removeClass('edit');

	// Als het niet om een event gaat -> aanpassen knop niet tonen.
	// (een interval (<-> event) zit namelijk niet in het geheugen)
	var interval = Dashboard.selectedIntervals[$(this).attr('data-num')];

	$(this).removeClass('new');

	if (interval.hasName) {
		$(this).removeClass('no-event');
	} else {
		$(this).addClass('no-event');
		if (!interval.isValid()){
			$(this).addClass('new');
		}
	}

	fillPeriodSelectionEvents.call(this);
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

	// Bindings van period selection!
	$(this).find('.period-selection .view-selection .edit-button').click(function(e) {
		e.preventDefault();
		$(this).parents('.period-selection').addClass('edit');
	});
	$(this).find('.period-selection .view-selection .new-button').click(function(e) {
		e.preventDefault();
		var popup = $(this).parents('.period-selection');

		// Als we al een custom bereik hebben, deze niet vernietigen
		if (Dashboard.selectedIntervals[popup.attr('data-num')].hasName){
			Dashboard.selectedIntervals[popup.attr('data-num')] = Interval.create(null, null);
		}

		fillPeriodSelectionInputs.call(popup[0]);
		updatePeriodSelectionName.call(popup[0]);
		popup.addClass('edit');
	});

	// Bindings van de edit knoppen
	$(this).find('.period-selection .view-edit .add').click(function() {
		var popup = $(this).parents('.period-selection');
		var event = Event.new(Dashboard.selectedIntervals[popup.attr('data-num')]);
		if (event){
			// Als gelukt
			savePeriodSelectionInputs.call(popup[0]);
			Dashboard.selectedIntervals[popup.attr('data-num')] = event;
			fillPeriodSelectionInputs.call(popup[0]);
			updatePeriodSelectionName.call(popup[0]);
			$(this).parent().parent().removeClass('no-name');
			$(this).parent().parent().children('.name').focus();
			$(this).parent().parent().children('.name').select();
		}
	});
	$(this).find('.period-selection .view-edit .delete').click(function() {
		var popup = $(this).parents('.period-selection');

		Dashboard.selectedIntervals[popup.attr('data-num')] = Dashboard.selectedIntervals[popup.attr('data-num')].delete();
		$(this).parent().parent().addClass('no-name');

		updatePeriodSelectionName.call(popup[0]);
	});

	$(this).find('.period-selection .view-edit .close').click(function() {
		var popup = $(this).parents('.period-selection');
		togglePopup.call(popup[0], true);
	});

	$(this).find('.period-selection .view-edit .name').keyup(function() {
		var popup = $(this).parents('.period-selection');
		savePeriodSelectionInputs.call(popup[0]);
		updatePeriodSelectionName.call(popup[0]);
	});

	$(this).find('.period-selection input.inline').change(function() {
		if (ignoreChangePickadate){
			return;
		}
		var popup = $(this).parents('.period-selection');
		savePeriodSelectionInputs.call(popup[0]);
		updatePeriodSelectionName.call(popup[0]);
	});


	$(this).find('.popup-box').click(function(event) {
		clickedOnPopup = true;
	});

	// Scrollbalken
	if (navigator.userAgent.indexOf('Mac OS X') == -1) {
		$(this).find(".popup-scroll").niceScroll({zindex:999,cursorcolor:"#CCCCCC"});
	}

	// Date en time pickers
	$(this).find('.datepicker').pickadate({
		selectMonths: true,
		selectYears: true,
		monthsFull: ['Januari', 'Februari', 'Maart', 'April', 'Mei', 'Juni', 'Juli', 'Augustus', 'September', 'October', 'November', 'December'],
		monthsShort: ['Jan', 'Feb', 'Mar', 'Apr', 'Mei', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
		weekdaysFull: ['Zondag', 'Maandag', 'Dinsdag', 'Woensdag', 'Donderdag', 'Vrijdag', 'Zaterdag'],
		weekdaysShort: ['Zo', 'Ma', 'Di', 'Wo', 'Do', 'Vr', 'Za'],

		// Buttons
		today: 'Vandaag',
		clear: 'Wis',
		close: 'Sluiten',

		// Accessibility labels
		labelMonthNext: 'Volgende maand',
		labelMonthPrev: 'Vorige maand',
		labelMonthSelect: 'Selecteer een maand',
		labelYearSelect: 'Selecteer een jaar',
		format: 'dd/mm/yyyy',
		formatSubmit: 'dd/m/yyyy',
	});
	$(this).find('.timepicker').pickatime({format: 'H:i'});


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
	if(getQueryVariable("mapView")==="true"){
		togglePanel();
	}
});
