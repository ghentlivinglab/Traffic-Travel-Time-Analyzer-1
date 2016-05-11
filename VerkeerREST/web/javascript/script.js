/* global Dashboard, Interval, Event, url, Mustache */

/****************************
 * polyfill to make the filter work on browsers that not support the 'includes'-function of string prototypes
 * source: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String/includes#Polyfill
 ****************************/
if (!String.prototype.includes) { 
    String.prototype.includes = function (search, start) {
        'use strict';
        if (typeof start !== 'number') {
            start = 0;
        }

        if (start + search.length > this.length) {
            return false;
        } else {
            return this.indexOf(search, start) !== -1;
        }
    };
}

/****************************
 * global variables
 * API fills these once only
 ****************************/
var providers = [];
var routes = [];
var events = [];


// speeds slower than the allowed speed is considered as slow
// eg: max speed is 50; slow is 50 - 10 = 40 km/h
var consideredSlowSpeed = 10; 
// Weekdays
var weekdays = ['Maandag', 'Dinsdag', 'Woensdag', 'Donderdag', 'Vrijdag', 'Zaterdag', 'Zondag'];
var weekdays_short = ['ma', 'di', 'wo', 'do', 'vr', 'za', 'zo'];
// mapping of api days to javascript days
var weekdays_js_to_rest = [6, 0, 1, 2, 3, 4, 5];

// loading of localStorage
Event.loadLocalStorage();

/****************************
 * gets the index of an event, given by name
 ****************************/
function getEventIndex(name) {
    for (var i = 0; i < events.length; i++) {
        if (events[i].getName() === name) {
            return i;
        }
    }
    return -1;
}

// mapping of XML-entities to their characters
var entityMap = {
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': '&quot;',
    "'": '&#39;',
    "/": '&#x2F;'
};

/****************************
 * function to escape characters that are HTML-specific
 ****************************/
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
 * if false is given as an argument, there is no animation
 ****************************/
function togglePanel(animated) {
    if (typeof animated == "undefined") { // when no parameter is given, display animation
        animated = true;
    }

    if ($("#dashboard").hasClass('open')) {
        // close dashboard when open
        $("#dashboard").removeClass('open');

        if (!animated) { // do not animate transition
            $("#dashboard").css({'left': 'auto', 'margin-left': 0, 'right': 0});
        } else {
            // animate to move panel to the left
            // this part is unstable, the panel will be positioned incorrectly when the view changes width
            $("#dashboard").animate({'margin-left': -$("#dashboard").outerWidth()}, function () {
                // when panel is hidden, make css truly stable
                $("#dashboard").css({'left': 'auto', 'margin-left': 0, 'right': 0});
            });
        }

        // changes the display of the button
        $(".collapse").children().attr({"src": "images/arrow-right.png", "alt": ">"});
        url.setQueryParam("dashboardView");
    } else { // displays the panel
        openDashboard(animated);
    }
}

/****************************
 * displays the panel, with or without animation
 ****************************/
function openDashboard(animated) {
    if (!$("#dashboard").hasClass('open')) {
        // if panel is closed, open it
        $("#dashboard").addClass('open');

        if (!animated) { // do not animate
            $("#dashboard").css({'left': '100%', 'right': 'auto', 'margin-left': 0});
        } else {
            // remove 'right: 0' and use margin-left in the css so we can animate
            // again this is an unstable action if the window is resized during the transition
            $("#dashboard").css({'left': '100%', 'right': 'auto', 'margin-left': -$("#dashboard").outerWidth()});

            // animate and become stable in the end
            $("#dashboard").animate({'margin-left': 0});
        }

        // changes the display of the button
        $(".collapse").children().attr({"src": "images/arrow-left.png", "alt": "<"});
        url.setQueryParam("dashboardView", "true");
    }
}

/****************************
 * open/close popup in popupable element
 * optional parameter close to force closing (value has no importance, the popup will be closed)
 ****************************/
function togglePopup(close) {
    var anchor = $(this).find('.popup-anchor');
    if (close === undefined && !anchor.hasClass('open')) {
        anchor.addClass('open');
    } else {
        // prevent activation of Dashboard.intervalsDidChange at every click, activates only on close
        if (anchor.hasClass('open')) {
            if ($(this).hasClass('period-selection')) {
                onClosePeriodSelection.call(this);
            }
            anchor.removeClass('open');
        }
    }
}


// simulates event.stopPropagation(); without breaking the date picker (which should receive events)
var clickedOnPopup = false;

/****************************
 * executes when new content is being loaded
 * this is always the root of the changing elements
 * Wordt uitgevoerd als er nieuwe content wordt toegevoegd of herladen. Hierbij is this altijd de root element die alle aanpassingen omvat.
 * Alles bindings moeten hierop dus opnieuw uitgevoerd worden
 ****************************/
function thisReady() {
    // Propagation disablen on popup box (no closing on onclick of popupable parent)
    $(this).find('.popupable').click(function (event) {
        if (!clickedOnPopup) {
            if ($(this).hasClass('period-selection')) {
                onOpenPeriodSelection.call(this);
            }

            togglePopup.call(this);
            clickedOnPopup = true;
        }
    });

    // adds all functionalities (= binds) eg: onClick, onChange...
    // to period selections in this
    bindPeriodSelection.call(this);
    bindDaySelection.call(this);

    $(this).find('.popup-box').click(function (event) {
        clickedOnPopup = true;
    });

    $(this).find('.left .updated').click(function (event) {
        event.preventDefault();

        $('<div class="update-image"><img src="images/loading.gif" alt="Bezig met laden"></div>').insertBefore(this);
        $(this).remove();
        Dashboard.forceLiveReload();
    });


}


var autoReloadTimer; // stores the timer for auto reload
/****************************
 * manages auto reload features by reading the state of the checkbox
 * stores the timer in autoReloadTimer
 ****************************/
function autoReloadChanged(){
    var button = $("#auto-reload"); // get checkbox
    if( $(button).is(":checked") ) { // if checked, enable timer
        if( !autoReloadTimer ){ // if timer not yet running start timer to force reload and add parameter to url
            autoReloadTimer = setInterval(function(){
                Dashboard.forceLiveReload();
            }, 1000*60*5); // 1000 ms/s * 60 s/min * 5 min
            url.setQueryParam("autoReload",true);
            
        }
        
    } else if(autoReloadTimer) { // not checked but timer is running
        clearInterval(autoReloadTimer); // stip timer
        autoReloadTimer=null; // clear timer
        url.setQueryParam("autoReload",""); // remove url parameter
    }
}

/****************************
 * runs when DOM-tree is finished
 ****************************/
$(document).ready(function () {
    thisReady.call(document);

    // adds a click listener to the collapse-button
    $(".collapse").click(togglePanel);

    // closes popups if clicked on another element 
    // won't execute with a click on popup thanks to the stop propagation
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
    url.updatePageByParams();
    Dashboard.init();
});
