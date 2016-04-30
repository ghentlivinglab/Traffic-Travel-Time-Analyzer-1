/* global Dashboard, Interval, Event, url */

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

var weekdays_js_to_rest = [6, 0, 1, 2, 3, 4, 5];

// Inladen van local storage
Event.loadLocalStorage();


function getEventIndex(name) {
    for (var i = 0; i < events.length; i++) {
        if (events[i].getName() === name) {
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
    var template = document.getElementById("template-" + template_name).innerHTML;
    Mustache.parse(template); // Speeding things up in the future
    return Mustache.render(template, view);
}


/****************************
 * toggles the display of the overview panel
 ****************************/
function togglePanel() {
    if ($("#dashboard").hasClass('open')) {
        // Als het dashboard open is, sluiten we het terug
        $("#dashboard").removeClass('open');

        // Animeer dat het naar links opschuift, dit is niet stabiel.
        // Als de breedte van het dashboard wijzigt tijdens het animeren
        // zal het op het einde niet op de juiste plaats staan.
        $("#dashboard").animate({'margin-left': -$("#dashboard").outerWidth()}, function () {
            // Als het terug gesloten is, zorgen we dat de CSS terug 'stabiel' is
            $("#dashboard").css({'left': 'auto', 'margin-left': 0, 'right': 0});
        });

        // changes the display of the button
        $(".collapse").children().attr({"src": "images/arrow-right.png", "alt": ">"});
        url.setQueryParam("dashboardView");
    } else {
        // Als het dashboard gesloten is, openen we het
        $("#dashboard").addClass('open');

        // We verwijderen right: 0 en stappen over op margin-left, zodat we die kunnen animeren
        // (right en left tergelijk animeren kan niet zonder jump)
        // De gebruiker merkt hier niets van, het scherm blijft exact hetzelfde.
        // We zijn wel niet meer 'stabiel', wijzigingen in de breedte van het dashboard zullen de animatie
        // beïnvloeden, maar dat blijft heel erg beperkt
        $("#dashboard").css({'left': '100%', 'right': 'auto', 'margin-left': -$("#dashboard").outerWidth()});

        // Animeren, op het einde hiervan zijn we terug 'stabiel'
        $("#dashboard").animate({'margin-left': 0});

        // changes the display of the button
        $(".collapse").children().attr({"src": "images/arrow-left.png", "alt": "<"});
        url.setQueryParam("dashboardView","true");
    }

}

/****************************
 * Open / sluit popup in popupable element
 * optionele parameter close om sluiten te forceren (maakt niet uit welke waarde die krijgt, zodra gegeven -> sluiten)
 ****************************/
function togglePopup(close) {
    var anchor = $(this).find('.popup-anchor');
    if (close === undefined && !anchor.hasClass('open')) {
        anchor.addClass('open');
    } else {
        // Voorkomen dat Dashboard.intervalsDidChange bij elke klik wordt uitgevoerd -> enkel bij sluiten
        if (anchor.hasClass('open')) {
            if ($(this).hasClass('period-selection')) {
                onClosePeriodSelection.call(this);
            }
            anchor.removeClass('open');
        }
    }
}

/****************************
 * Wordt uitgevoerd als er nieuwe content wordt toegevoegd of herladen. Hierbij is this altijd de root element die alle aanpassingen omvat.
 * Alles bindings moeten hierop dus opnieuw uitgevoerd worden
 ****************************/

// simuleert event.stopPropagation(); zonder de datepicker kapot te maken (die moet namelijk ook events ontvangen!)
var clickedOnPopup = false;

function thisReady() {
    // Propagation disablen op popup box (dat deze ook niet sluit bij onlick van de popupable parent)
    $(this).find('.popupable').click(function (event) {
        if (!clickedOnPopup) {
            if ($(this).hasClass('period-selection')) {
                onOpenPeriodSelection.call(this);
            }

            togglePopup.call(this);
            clickedOnPopup = true;
        }
    });

    // Alle functinaliteiten toevoegen (= binds) bv. onClick, onChange, ...
    // voor period selections in this
    bindPeriodSelection.call(this);


    $(this).find('.popup-box').click(function (event) {
        clickedOnPopup = true;
    });
}

/****************************
 * runs when DOM-tree is finished
 ****************************/
$(document).ready(function () {
    thisReady.call(document);

    // adds a click listener to the collapse-button
    $(".collapse").click(togglePanel);

    // Popups sluiten als er naast wordt geklikt 
    // (door stop propagation zal dit nooit uitgevoerd worden als er op de popup geklikt wordt)
    $(document).click(function () {
        if (clickedOnPopup) {
            clickedOnPopup = false;
            return;
        }
        $('.popupable').each(function () {
            togglePopup.call(this, true);
        });
    });

    // initialises dashboard
    Dashboard.init();
});

/****************************
 * runs when DOM-tree is finished and all objects from it are loaded
 ****************************/
$(window).load(function () {
    updateViewByURLParams();
    $("[name=dashboard]").on('click',function(event){
        url.setQueryParam("weergave",this.id.split('-')[1]);
    });
    $("[name=provider]").on('click',changeURLParamProvider);
});

function changeURLParamProvider(){
    var providers = $("[name=provider]");
    var indices = [];
    for(var i=0;i<providers.length;i++){
        if($(providers[i]).is(':checked')){
            indices.push(i);
        }
    }
    url.setQueryParam("providers","["+indices+"]");
}

function updateViewByURLParams(){
    URLParamsShowDashboard();
    URLParamsChangeView();
    URLParamsChangeProvider();
    URLParamsChangePeriod();
    URLParamsChangeComparePeriod();
    URLParamsChangeMap();
}

function URLParamsShowDashboard(){
    // checks if mapView or overview has to be displayed
    var showDashboard = url.getQueryParam("dashboardView") === "true"; // checks if URL contains directives
    var dashboardShown = $("#dashboard").hasClass('open'); // checks in which state the dashboard currently resides
    
    if (showDashboard ? !dashboardShown : dashboardShown) { // showDashboard XOR dashboardShown
        togglePanel();
    }
}

function URLParamsChangeView(){
    var view = Number(url.getQueryParam("weergave"));
    if(!view){
        view = 0;
        console.error("incorrect parameter: weergave");
        url.setQueryParam("weergave");
        console.error("has been removed");
    }
    $("#mode-"+view).click();
}

function URLParamsChangeProvider(){
    try{
        var URLProviders = JSON.parse(url.getQueryParam("providers"));
    } catch (error){
        var URLProviders = [0];
        console.error("incorrect parameter: providers");
        url.setQueryParam("providers");
        console.error("has been removed");
    }
        var providers = $("[name=provider]");
        URLProviders = ( URLProviders === NaN ? [0] : URLProviders);
        for(var i=0;i<URLProviders.length;i++){
            $($(providers[URLProviders[i]])).prop("checked",true);
        }
}

function URLParamsChangeMap(){
    var center = url.getQueryParam("mapCenter");
    if(center){
        center = center.split(',');
        if(center.length===2){
            var latitude = Number(center[0]);
            var longitude = Number(center[1]);
            if( latitude && longitude ){
                map.setCenter({lat:latitude,lng:longitude});
           } else{
                console.error("incorrect parameter: mapCenter (coords are not numbers)");
                url.setQueryParam("mapCenter");
                console.error("has been removed");
           }
        } else{
            console.error("incorrect parameter: mapCenter (wrong number of coords)");
            url.setQueryParam("mapCenter");
            console.error("has been removed");
        }
    }
    var zoom = url.getQueryParam("mapZoom");
    if(zoom){
        zoom = Number(zoom);
        if(!zoom){
            zoom = zoomCurrent;
            console.error("incorrect parameter: mapZoom");
            url.setQueryParam("mapZoom");
            console.error("has been removed");
        }
        zoomCurrent=zoom;
        map.setZoom(zoom);
    }
}

function URLParamsChangePeriod(){
    var period = url.getQueryParam("periode");
    if(period){
        period=period.split(',');
        if(period.length===3){
            var name = decodeURIComponent(period[0]);

            var from = createValidDate(period[1]);
            var to = createValidDate(period[2]);

            if ( name && from && to ) {
                var event = Event.create(name,from,to);
                Dashboard.selectedIntervals[0] = event;
                if(!url.getQueryParam("vergelijkPeriode")){
                    Dashboard.intervalsDidChange();
                }
            } else if(from && to) {
                var interval = Interval.create(from,to);
                Dashboard.selectedIntervals[0] = interval;
                if(!url.getQueryParam("vergelijkPeriode")){
                    Dashboard.intervalsDidChange();
                }
            } else {
                console.error("incorrect parameter: periode (wrong format of date)");
                url.setQueryParam("periode");
                console.error("has been removed");
            }
        } else {
            console.error("incorrect parameter: periode (wrong number of arguments)");
            url.setQueryParam("periode");
            console.error("has been removed");
        }
    }
}

function URLParamsChangeComparePeriod(){
    var period = url.getQueryParam("vergelijkPeriode");
    if(period){
        period=period.split(',');
        if(period && period.length===3){
            var name = decodeURIComponent(period[0]);

            var from = createValidDate(period[1]);
            var to = createValidDate(period[2]);

            if ( name && from && to ) {
                var event = Event.create(name,from,to);
                Dashboard.selectedIntervals[1] = event;
                Dashboard.intervalsDidChange();
            } else if(from && to) {
                var interval = Interval.create(from,to);
                Dashboard.selectedIntervals[1] = interval;
                Dashboard.intervalsDidChange();
            } else {
                console.error("incorrect parameter: vergelijkPeriode (wrong format of date)");
                url.setQueryParam("vergelijkPeriode");
                console.error("has been removed");
            }
        } else {
            console.error("incorrect parameter: vergelijkPeriode (wrong number of arguments)");
            url.setQueryParam("vergelijkPeriode");
            console.error("has been removed");
        }
    }
}

function createValidDate(dateString){
    var dateString = dateString.split("/");
    var day = Number(dateString[0]);
    var month = Number(dateString[1]);
    var year = Number(dateString[2]);
    if( (day < 1 || 31 < day ) || ( month < 1 || 12 < month ) || (year===NaN)){
        return NaN;
    }
    return new Date(year, month - 1, day);
}
