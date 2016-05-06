/* global Dashboard, Interval, Event, url, Mustache */

/****************************
 * global variables
 * API fills these once only
 ****************************/
var providers = [];
var routes = [];
var events = [];

// Snelheid trager dan de toegelaten snelheid, die we beschouwen als traag
var consideredSlowSpeed = 10; // bv toegalten = 50; traag = 50 - 15 => 35 km/h


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
};


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
    bindDaySelection.call(this);

    $(this).find('.popup-box').click(function (event) {
        clickedOnPopup = true;
    })

    $(this).find('.left .updated').click(function (event) {
        event.preventDefault();

        $('<div class="update-image"><img src="images/loading.gif" alt="Bezig met laden"></div>').insertBefore(this);
        $(this).remove();
        Dashboard.forceLiveReload()
    })

    
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
    url.updatePageByParams();
});

