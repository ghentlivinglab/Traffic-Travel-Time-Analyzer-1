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
        var newLocation = window.location.protocol + "//" + window.location.host + window.location.pathname +(this.query.length!==0?"?"+ this.query:"");
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
    setQueryParams:function(){
        this.getQuery();
        for(var i=0;i<arguments.length;i+=2){
            this.changeQueryParam(arguments[i],arguments[i+1]);
        }
        this.updateLocation();
    },
    setQueryParam:function(name,value){
        this.getQuery();
        this.changeQueryParam(name,value);
        this.updateLocation();
    }
}
