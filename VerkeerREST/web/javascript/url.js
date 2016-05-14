/* global map, Event, Dashboard, Interval, NaN */

var url = {
    query: "",
    /****************************
     * Gets the query from the current location and stores it locally in url.query
     ***************************/
    getQuery: function () {
        this.query = window.location.search.substring(1);  // removes '?' 
    },
    /****************************
     * updates the current location with the new query parameters and adds it to the history of the browser
     ***************************/
    updateLocation: function () {
        // build new location
        var newLocation = window.location.protocol + "//" + window.location.host + window.location.pathname + (this.query.length !== 0 ? "?" + this.query : "");
        // update current location and pushes previous to the history
        history.pushState(null, document.title, newLocation);

    },
    /****************************
     * reads the key-value pairs from the location-URL
     * eg: for the URL http://test.com/?food=banana&drink=beer the method getQueryVariable("food") returns "banana"
     ***************************/
    getQueryParam: function (variable) {
        this.getQuery(); // reload query string

        var vars = this.query.split("&"); // splits the key-value pairs

        for (var i = 0; i < vars.length; i++) {
            var pair = vars[i].split("="); // splits keys from values
            if (pair[0] === variable) {
                return pair[1];
            } //returns value when requested key has been found
        }
        return false; // else returns false
    },
    /****************************
     * returns all key-value pairs in query string
     * eg: for the URL http://test.com/?food=banana&drink=beer the method getQueryVariable() returns [{name: "food", value: "banana"},{name: "drink", value: "beer"}]
     ***************************/
    getQueryParams: function () {
        this.getQuery(); // reload query string

        var vars = this.query.split("&"); // splits the key-value pairs
        var returnValue = [];
        for (var i = 0; i < vars.length; i++) {
            var pair = vars[i].split("="); // splits keys from values
            returnValue.push({"name": pair[0], "value": pair[1]});
        }
        return returnValue;
    },
    /****************************
     * adds/updates the query parameter with name and/to value
     * if no value or empty value is provided, it removes the name/value-pair
     ***************************/
    changeQueryParam: function (name, value) {
        var vars = this.query.split("&"); // splits the key-value pairs
        var variableFound = false; // check if parameter is already in use
        for (var i = 0; i < vars.length; i++) {
            var pair = vars[i].split("="); // splits key from value
            if (pair[0] === name) { // parameter is in use
                if (!value) { // there is no value or empty value provided
                    vars[i] = ""; // removes element
                } else {
                    vars[i] = pair[0] + "=" + value; // updates element
                }
                variableFound = true; // parameter is in use
            }
        }

        if (!variableFound && value) { // if parameter is not in use, add to the back
            vars.push(name + "=" + value);
        }

        this.query = ""; // reset query string
        if (vars.length > 0) { // only if there are parameters
            for (var i in vars) { // build new query string
                if (vars[i] !== "") {
                    this.query += vars[i];
                    this.query += "&";
                }
            }
        }
        this.query = this.query.slice(0, -1); // removes trailing '&'
    },
    /****************************
     * sets the given name/value-parameters in the URL
     * odd arguments are the name, even arguments are the value
     ***************************/
    setQueryParams: function () {
        this.getQuery(); // refresh local query-string
        for (var i = 0; i < arguments.length; i += 2) {
            this.changeQueryParam(arguments[i], arguments[i + 1]); // update 1 pair of parameters
        }
        this.updateLocation(); // store new values
    },
    /****************************
     * sets the given name and value in the parameters of the URL
     ***************************/
    setQueryParam: function (name, value) {
        this.getQuery();
        this.changeQueryParam(name, value);
        this.updateLocation();
    },
    /****************************
     * method to load the information from the URL query parameters and set the relevant values
     ***************************/
    updatePageByParams: function () {
        /* this.changeProviderByParam()
         * Provider settings are being loaded when all providers are loaded from the backend (Dashboard.loadProviders())
         * */

        /* this.changeViewByParam()
         * display settings are being loaded in the initialisation of the dashboard (Dashboard.init())
         * */

        /* this.changePeriodByParam() this.changeComparePeriodByParam() and this.changeDayByParam()
         * interval settings are being loade after mode/view is set and relevant data has been retrieved (Dashboard.init())
         * */

        this.showDashboardByParam(); // toggle visibility of panel
        this.changeMapByParams(); // update map-zoom and center
        this.changeFilterByParam(); // update filter
        this.changeAutoReloadByParam(); // update auto reload feature
    },
    /****************************
     * sets visibility of the dashboard panel
     ***************************/
    showDashboardByParam: function () {
        // checks if mapView or overview has to be displayed
        var showDashboard = url.getQueryParam("dashboardView") === "true"; // checks if URL contains directives
        var dashboardShown = $("#dashboard").hasClass('open'); // checks in which state the dashboard currently resides

        if (showDashboard ? !dashboardShown : dashboardShown) { // showDashboard XOR dashboardShown
            togglePanel(false); // show panel with no animation
        }
    },
    /****************************
     * sets the mode of the dashboard panel
     ***************************/
    changeViewByParam: function () {
        var view = url.getQueryParam("weergave"); // get view-settings from url
        if (view) { // only if view-settings has a value
            view = Number(view); // parse view number
            Dashboard.mode = view; // set dashboard mode to the number
            $('#mode-' + Dashboard.mode).prop("checked", true); // set the relevant radio button active
        }
    },
    /****************************
     * sets the selected provider
     ***************************/
    changeProviderByParam: function () {
        var providerName = url.getQueryParam("provider"); // get provider settings from url
        if (providerName === false) { // no value given
            return;
        }
        Dashboard.setProviderName(providerName); // activate the chosen provider
    },
    /****************************
     * sets the first period on the dashboard
     * compares with existing periods and uses one of them if the start and end date match
     * if no matching period exists, creates a new one
     ***************************/
    changePeriodByParam: function () {
        var period = url.getQueryParam("periode"); // get period-settings
        if (period) { // if the period has a value
            period = period.split(','); // split in name, start date and end date
            if (period.length === 3) { // must have only these 3 parameters
                var name = decodeURIComponent(period[0]); // decode encoded uri-string

                var from = this.createValidDate(period[1]); // generate start date
                var to = this.createValidDate(period[2]); // generate end date

                if (name && from && to) { // if valid dates and has a name -> look for event
                    var eventExists = -1;
                    for (var i = 0; i < events.length; i++) { // look for existing events and remember last matching one
                        if (events[i].start.getTime() == from.getTime() && events[i].end.getTime() == to.getTime()) {
                            eventExists = i;
                            break;
                        }
                    }
                    if (eventExists == -1) { // no existing event, create new event
                        var event = Event.create(name, from, to);
                        Dashboard.selectedIntervals[0] = event;
                    } else { // event exists, use this one
                        Dashboard.selectedIntervals[0] = events[i];
                    }

                } else if (from && to) { // no name but valid dates -> create interval
                    var interval = Interval.create(from, to); // create interval
                    Dashboard.selectedIntervals[0] = interval; // and select it

                } else { // one of the dates is incorrect
                    console.error("incorrect parameter: periode (wrong format of date)");
                    url.setQueryParam("periode"); // remove faulty parameter
                    console.info("has been removed");
                }
            } else { // wrong number of arguments
                console.error("incorrect parameter: periode (wrong number of arguments)");
                url.setQueryParam("periode"); // remove faulty parameter
                console.info("has been removed");
            }
        }
    },
    /****************************
     * sets the second period on the dashboard
     * works completely analogous to this.changePeriodByParam() 
     * documentation can thus be found in that method
     ***************************/
    changeComparePeriodByParam: function () {
        var period = url.getQueryParam("vergelijkPeriode");
        if (period) {
            period = period.split(',');
            if (period && period.length === 3) {
                var name = decodeURIComponent(period[0]);

                var from = this.createValidDate(period[1]);
                var to = this.createValidDate(period[2]);

                if (name && from && to) {
                    var eventExists = -1;
                    for (var i = 0; i < events.length; i++) {
                        if (events[i].start.getTime() === from.getTime() && events[i].end.getTime() === to.getTime()) {
                            eventExists = i;
                            break;
                        }
                    }
                    if (eventExists === -1) {
                        var event = Event.create(name, from, to);
                        Dashboard.selectedIntervals[1] = event;
                    } else if (eventExists > -1) {
                        Dashboard.selectedIntervals[1] = events[i];
                    }
                    Dashboard.intervalsDidChange();
                } else if (from && to) {
                    var interval = Interval.create(from, to);
                    Dashboard.selectedIntervals[1] = interval;
                    Dashboard.intervalsDidChange();
                } else {
                    console.error("incorrect parameter: vergelijkPeriode (wrong format of date)");
                    url.setQueryParam("vergelijkPeriode");
                    console.info("has been removed");
                }
            } else {
                console.error("incorrect parameter: vergelijkPeriode (wrong number of arguments)");
                url.setQueryParam("vergelijkPeriode");
                console.info("has been removed");
            }
        }
    },
    /****************************
     * updates map center and zoom level
     ***************************/
    changeMapByParams: function () {
        this.changeMapCenterByParam();
        this.changeMapZoomByParam();
    },
    /****************************
     * sets the center of the map
     ***************************/
    changeMapCenterByParam: function () {
        var center = url.getQueryParam("mapCenter"); // get center settings
        if (center) { // only if a value is given
            center = center.split(','); // split coordinate in components
            if (center.length === 2) { // should have 2 components
                var latitude = Number(center[0]);
                var longitude = Number(center[1]);
                if (latitude && longitude) { // set center to the latitude and longitude, only if they are correct numbers
                    map.setCenter({lat: latitude, lng: longitude});
                } else {
                    console.error("incorrect parameter: mapCenter (coords are not numbers)");
                    url.setQueryParam("mapCenter"); // remove faulty parameter
                    console.info("has been removed");
                }
            } else {
                console.error("incorrect parameter: mapCenter (wrong number of coords)");
                url.setQueryParam("mapCenter"); // remove faulty parameter
                console.info("has been removed");
            }
        }
    },
    /****************************
     * sets the zoom level of the map
     ***************************/
    changeMapZoomByParam: function () {
        var zoom = url.getQueryParam("mapZoom"); // get zoom settings
        if (zoom) { // only if value is given
            zoom = Number(zoom);
            if (!zoom) { // only if zoom is not a valid number
                zoom = zoomCurrent; // keep current zoom
                console.error("incorrect parameter: mapZoom");
                url.setQueryParam("mapZoom"); // remove faulty parameter
                console.info("has been removed");
            }
            // set current zoom to new value
            zoomCurrent = zoom;
            map.setZoom(zoom);
        }
    },
    /****************************
     * sets the filter for the dashboard
     ***************************/
    changeFilterByParam: function () {
        var value = decodeURIComponent(url.getQueryParam("filter")); // get and decode the filter settings
        if (value !== "false") { // only if a value is given
            Dashboard.filterValue = value; // set filter
            Dashboard.updateFilter(); // update filter
            $("#filter #filterInput").val(Dashboard.filterValue); // display new value in the text field
        }
    },
    /****************************
     * sets the selected day
     ***************************/
    changeDayByParam: function () {
        var value = url.getQueryParam("dag"); // get day settings
        if (value) { // only if value is given
            var date = this.createValidDate(value); // create date
            if (date) { // if date is valid
                Dashboard.setSelectedDay(date); // set the day in the dashboard
            } else {
                console.error("incorrect parameter: dag (wrong format of date)");
                url.setQueryParam("dag"); // remove faulty parameter
                console.info("has been removed");
            }
        }
    },
    /****************************
     * set auto reload of the data
     ***************************/
    changeAutoReloadByParam: function () {
        var value = url.getQueryParam("autoReload"); // get auto reload settings
        var input = $("#auto-reload"); // get the checkbox
        $(input).prop("checked", false); // unset checkbox 

        if (value) { // only if value is given
            if (value === "true") { // value has to be true
                $(input).click(); // start auto reload and set checkbox
            } else {
                console.error("incorrect parameter: autoReload (value can only be true)");
                url.setQueryParam("autoReload"); // remove faulty parameter
                console.info("has been removed");
            }
        }
    },
    /****************************
     * creates a Date object from a valid "dd/MM/yyyy"
     ***************************/
    createValidDate: function (dateString) {
        var dateString = dateString.split("/"); // split day, month and year
        // get the numeric value from the string
        var day = Number(dateString[0]);
        var month = Number(dateString[1]);
        var year = Number(dateString[2]);
        // do very basic check for the date
        if ((day < 1 || 31 < day) || (month < 1 || 12 < month) || (year === NaN)) {
            return NaN;
        }
        return new Date(year, month - 1, day); // generate date (gives NaN when impossible)
    },
    /****************************
     * adds the current intervals in the url
     ***************************/
    setIntervals: function (interval, vergelijkInterval) {
        var param = (interval.hasName ? encodeURIComponent(interval.getName()) : "") + ","
                + dateToDate(interval.start) + ","
                + dateToDate(interval.end); // generate parameter value "name,from,to"
        param = param === ",," ? "" : param; // make string empty if interval has no information

        if (Dashboard.mode === Dashboard.INTERVAL) { // if interval mode, only set first interval and remove second interval and day
            this.setQueryParams("periode", param, "vergelijkPeriode", "", "", "dag", "");

        } else if (Dashboard.mode === Dashboard.COMPARE_INTERVALS) { // if compare mode, create second interval, set both intervals and remove day
            var param2 = (vergelijkInterval.hasName ? encodeURIComponent(vergelijkInterval.getName()) : "") + ","
                    + dateToDate(vergelijkInterval.start) + ","
                    + dateToDate(vergelijkInterval.end); // generate paremeter value
            param2 = param2 === ",," ? "" : param2; // empty interval
            url.setQueryParams("periode", param, "vergelijkPeriode", param2, "dag", "");

        } else { // remove intervals when not in an interval mode
            url.setQueryParams("periode", "", "vergelijkPeriode", "");
        }
    }
};
