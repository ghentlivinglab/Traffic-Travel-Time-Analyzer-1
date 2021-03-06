/****************************
 * Converteer functies (formaat is voor achter de schermen, niet voor display!) Niet aanpassen aub
 ****************************/

// Date naar een leesbare string (zonder tijd) voor mensen
function dateToDate(date){
	if (!date){
		return '';
	}
	var day = date.getDate();
	var month = date.getMonth()+1;
	var year = date.getFullYear();
	return day+"/"+month+"/"+year;
}

// Date naar een leesbare string voor mensen
function dateToString(date){
	if (!date){
		return '';
	}
	var day = pad(date.getDate());
	var month = pad(date.getMonth()+1);
	var year = pad(date.getFullYear());
        
    var hours = pad(date.getHours());
    var minutes = pad(date.getMinutes());
    
	return year+"-"+month+"-"+day+" om "+hours+":"+minutes;
}

function dateToTimeString(date) {
	if (!date){
		return '';
	}
    var hours = pad(date.getHours());
    var minutes = pad(date.getMinutes());
    
	return hours+":"+minutes;
}

// Date naar leesbare string voor onze REST api
// Gebruik hier dateToString niet omdat deze functie haar output niet mag veranderen moesten we dateToString wijzigen
function dateToRestString(date){
	if (!date){
		return '';
	}
	var day = pad(date.getDate());
	var month = pad(date.getMonth()+1);
	var year = pad(date.getFullYear());
        
    var hours = pad(date.getHours());
    var minutes = pad(date.getMinutes());
    var seconds = pad(date.getSeconds());
	return year+"-"+month+"-"+day+" "+hours+":"+minutes+":"+seconds;
}

function pad(str){
    str = '' + str;
    if(str.length < 2){
        return "0" + str;
    }
    return str;
}

// Date.parse functie is heel erg inconsistent en afgeraden door Mozilla foundation
function stringToDate(dateString) {
	var reggie = /(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})/;
	if (!reggie.test(dateString)){
		console.warn("Invalid date parsed: "+dateString);
		return new Date();
	}
	var dateArray = reggie.exec(dateString); 
	var dateObject = new Date(
	    (+dateArray[1]),
	    (+dateArray[2])-1, // Careful, month starts at 0!
	    (+dateArray[3]),
	    (+dateArray[4]),
	    (+dateArray[5]),
	    (+dateArray[6])
	);
	return dateObject;
}

// Vult automatisch de periode popup in, o.a. net voor het openen of na het aanpassen van het geselecteerde interval.
//  Hierbij is this = class .period-selection
var ignoreChangePickadate = false;
function fillPeriodSelectionInputs(){
	var interval = Dashboard.selectedIntervals[$(this).attr('data-num')];

	// Popup inhoud juist zetten

	// Edit properties juist zetten
	var fromDate = $(this).find('.from.datepicker');
	var toDate = $(this).find('.to.datepicker');

	ignoreChangePickadate = true;
	if (interval.start){
		fromDate.pickadate('picker').set('select', interval.start);
	}
	else {
		fromDate.pickadate('picker').clear();
	}

	if (interval.end){
		toDate.pickadate('picker').set('select', interval.end);
	}
	else {
		toDate.pickadate('picker').clear();
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
	var toDate = $(this).find('.to.datepicker').pickadate('picker').get('select');

	interval.setStart(new Date(fromDate.year, fromDate.month, fromDate.date));
	interval.setEnd(new Date(toDate.year, toDate.month, toDate.date));
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
		// Als we op de knop klikken om een 'event' aan te passsen (3 puntjes)

		var popup = $(this).parents('.period-selection');

		// Event zoeken dat bij dit item hoort
		var event = events[$(this).parent().attr('data-index')];
		
		if (event){
			// Als het event bestaat:
			// Het toevoegen als geselecteerd itnerval
			Dashboard.selectedIntervals[popup.attr('data-num')] = event;

			// Naam van het selectiebereik aanpassen
			updatePeriodSelectionName.call(popup[0]);

			// De inputs aanpassen aan het geselecteerde event
			fillPeriodSelectionInputs.call(popup[0]);

			// Naar edit modus gaan, zodat we deze inputs ook zien
			popup.addClass('edit');
		}
	});
}

// Uitgevoerd als de period selection popup wordt gesloten
// Hierna moeten we de change handler van het dashboard aanroepen
function onClosePeriodSelection() {
	Dashboard.intervalsDidChange();
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

function bindPeriodSelection() {
	// Bindings van period selection!
	$(this).find('.period-selection .view-selection .edit-button').click(function() {
		$(this).parents('.period-selection').addClass('edit');
	});
	$(this).find('.period-selection .view-selection .new-button').click(function() {
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
		format: 'dd/mm/yyyy'
	});
}

function bindDaySelection() {
	var w = $(this).find('#day-selection');
	if (w.length == 0) {
		return;
	}

	w.change(function() {
		if (ignoreChangePickadate) {
			// we hebben het programatisch aangepast
			// voorkomen infinite loop
			return
		}

		var day = w.pickadate('picker').get('select');
		if (day) {
			var date = new Date(day.year, day.month, day.date);
    		Dashboard.setSelectedDay(date);
    	} else {
    		Dashboard.setSelectedDay(null);
    	}
	});

	// Selectie juist zetten voor de datepicker (deze leest value attribuut niet in)
	if (Dashboard.selectedDay !== null) {
		// infinite loop voorkomen
		ignoreChangePickadate = true;
		w.pickadate('picker').set('select', Dashboard.selectedDay);
		ignoreChangePickadate = false;
	}
}

