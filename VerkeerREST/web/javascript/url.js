/* global map, Event, Dashboard, Interval, NaN */

var url = {
    query: "",
    /****************************
     * 
     * 
     ***************************/
    getQuery: function () {
        this.query = window.location.search.substring(1);  // removes '?'
    },
    /****************************
     * 
     * 
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
        return false;
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
    setQueryParams: function () {
        this.getQuery();
        for (var i = 0; i < arguments.length; i += 2) {
            this.changeQueryParam(arguments[i], arguments[i + 1]);
        }
        this.updateLocation();
    },
    setQueryParam: function (name, value) {
        this.getQuery();
        this.changeQueryParam(name, value);
        this.updateLocation();
    },
    updatePageByParams: function () {
        // Provider hier niet inladen! Pas nadat we alle providers hebben ingeladen
        // => Gebeurt nu in Dashboard loadProviders(), een functie die aangeroepen wordt
        // Nadat we alle providers hebben ingeladen in providers

        this.showDashboardByParam();
        this.changeMapByParams();
        this.changeFilterByParam();
        this.changeViewByParam();
        this.changeDayByParam();

        // Onderstaande functies moeten nog gecontrolleerd worden
        this.changePeriodByParam();
        this.changeComparePeriodByParam();
    },
    showDashboardByParam: function () {
        // checks if mapView or overview has to be displayed
        var showDashboard = url.getQueryParam("dashboardView") === "true"; // checks if URL contains directives
        var dashboardShown = $("#dashboard").hasClass('open'); // checks in which state the dashboard currently resides

        if (showDashboard ? !dashboardShown : dashboardShown) { // showDashboard XOR dashboardShown
            togglePanel();
        }
    },
    changeViewByParam: function () {
        var view = url.getQueryParam("weergave");
        if (view) {
            view = Number(view);
            if (!view && view !== 0) {
                view = 0;
                console.error("incorrect parameter: weergave");
                url.setQueryParam("weergave");
                console.error("has been removed");
            }
            $("#mode-" + view).click();
        }
    },
    changeProviderByParam: function () {
        var providerName = url.getQueryParam("provider");
        if (providerName === false) {
            return;
        }
        Dashboard.setProviderName(providerName);
    },
    changePeriodByParam: function () {
        var period = url.getQueryParam("periode");
        if (period) {
            period = period.split(',');
            if (period.length === 3) {
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
                    var event;
                    if (eventExists === -1) {
                        event = Event.create(name, from, to);
                        Dashboard.selectedIntervals[1] = event;
                    } else if (eventExists > -1) {
                        Dashboard.selectedIntervals[0] = events[i];
                    }
                    if (!url.getQueryParam("vergelijkPeriode")) {
                        Dashboard.intervalsDidChange();
                    }
                } else if (from && to) {
                    var interval = Interval.create(from, to);
                    Dashboard.selectedIntervals[0] = interval;
                    if (!url.getQueryParam("vergelijkPeriode")) {
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
    },
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
                    var event;
                    if (eventExists === -1) {
                        event = Event.create(name, from, to);
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
                    console.error("has been removed");
                }
            } else {
                console.error("incorrect parameter: vergelijkPeriode (wrong number of arguments)");
                url.setQueryParam("vergelijkPeriode");
                console.error("has been removed");
            }
        }
    },
    changeMapByParams: function () {
        this.changeMapCenterByParam();
        this.changeMapZoomByParam();
    },
    changeMapCenterByParam: function () {
        var center = url.getQueryParam("mapCenter");
        if (center) {
            center = center.split(',');
            if (center.length === 2) {
                var latitude = Number(center[0]);
                var longitude = Number(center[1]);
                if (latitude && longitude) {
                    map.setCenter({lat: latitude, lng: longitude});
                } else {
                    console.error("incorrect parameter: mapCenter (coords are not numbers)");
                    url.setQueryParam("mapCenter");
                    console.error("has been removed");
                }
            } else {
                console.error("incorrect parameter: mapCenter (wrong number of coords)");
                url.setQueryParam("mapCenter");
                console.error("has been removed");
            }
        }
    },
    changeMapZoomByParam: function () {
        var zoom = url.getQueryParam("mapZoom");
        if (zoom) {
            zoom = Number(zoom);
            if (!zoom) {
                zoom = zoomCurrent;
                console.error("incorrect parameter: mapZoom");
                url.setQueryParam("mapZoom");
                console.error("has been removed");
            }
            zoomCurrent = zoom;
            map.setZoom(zoom);
        }
    },
    changeFilterByParam: function () {
        var value = decodeURIComponent(url.getQueryParam("filter"));
        if (value !== "false") {
            Dashboard.filterValue = value;
            Dashboard.updateFilter();
            $("#filter #filterInput").val(Dashboard.filterValue);
        }
    },
    changeDayByParam: function () {
        var value = url.getQueryParam("dag");
        if (value) {
            var date = this.createValidDate(value);
            if (date) {
                Dashboard.setSelectedDay(date);
            } else {
                console.error("incorrect parameter: dag (wrong format of date)");
                url.setQueryParam("dag");
                console.error("has been removed");
            }
        }
    },
    createValidDate: function (dateString) {
        var dateString = dateString.split("/");
        var day = Number(dateString[0]);
        var month = Number(dateString[1]);
        var year = Number(dateString[2]);
        if ((day < 1 || 31 < day) || (month < 1 || 12 < month) || (year === NaN)) {
            return NaN;
        }
        return new Date(year, month - 1, day);
    },
    setIntervals: function (interval, vergelijkInterval) {
        var param = (interval.hasName ? encodeURIComponent(interval.getName()) : "") + ","
                + dateToDate(interval.start) + ","
                + dateToDate(interval.end);
        param = param === ",," ? "" : param;

        if (Dashboard.mode === Dashboard.INTERVAL) {
            this.setQueryParams("periode", param, "vergelijkPeriode", "", "", "dag", "");

        } else if (Dashboard.mode === Dashboard.COMPARE_INTERVALS) {
            var param2 = (vergelijkInterval.hasName ? encodeURIComponent(vergelijkInterval.getName()) : "") + ","
                    + dateToDate(vergelijkInterval.start) + ","
                    + dateToDate(vergelijkInterval.end);
            param2 = param2 === ",," ? "" : param2;
            url.setQueryParams("periode", param, "vergelijkPeriode", param2, "dag", "");

        } else {
            url.setQueryParams("periode", "", "vergelijkPeriode", "");
        }
    }
};
