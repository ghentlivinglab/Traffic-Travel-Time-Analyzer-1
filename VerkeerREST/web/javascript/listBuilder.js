/**
    De listbuilder bouwt een lijst om de gegevens van routes voor te stellen.
*/

var ListBuilder = {
    leftGetter: null,
    rightGetter: null,
    leftRepresentation: null,
    rightRepresentation: null,
    sortFunction: null,
    statusFunction: null,

    // Mogelijke representaties die gebruikt kunnen worden (doorgeven in setLeft / setRight)

    // Default representatie toont snelheid en tijd met balk
    // Werkt voor zowel TrafficData als IntervalRepresentation objecten
    DEFAULT_REPRESENTATION: function(route, representation) {
        return Mustache.renderTemplate("listbuilder-normal", {
            'speed': representation.toSpeedString(),
            'time': representation.toTimeString(),
            'color': route.getColor(representation)
        });
    },

    // Default representatie toont snelheid en tijd met balk
    // Werkt voor zowel TrafficData als IntervalRepresentation objecten
    AVERAGE_REPRESENTATION: function(route, representation) {
        return Mustache.renderTemplate("listbuilder-average", {
            'speed': representation.toSpeedString(),
            'time': representation.toTimeString(),
            'color': route.getColor(representation)
        });
    },

    INTERVAL_REPRESENTATION: function(route, representation) {
        return Mustache.renderTemplate("listbuilder-normal", {
            'speed': representation.toSpeedString(),
            'time': representation.toTimeString(),
            'color': route.getColor(representation)
        });
    },

    DAY_REPRESENTATION: function(route, representation) {
        return Mustache.renderTemplate("listbuilder-normal", {
            'speed': representation.toSpeedString(),
            'time': representation.toTimeString(),
            'color': route.getColor(representation)
        });
    },

    UNAVAILABLE_REPRESENTATION: function() {
        return '<p>Niet beschikbaar</p>';
    },

    create: function() {
        return Object.create(ListBuilder);
    },

    // Zet het linker gedeelte
    setLeft: function(representationType, getter) {
        this.leftGetter = getter;
        this.leftRepresentation = representationType;
    },

    // Zet het linker gedeelte
    setRight: function(representationType, getter) {
        this.rightGetter = getter;
        this.rightRepresentation = representationType;
    },

    /** Functie opgeven die de status van een route bepaald en haar index.
     Deze status wordt gebruikt om de routes te groeperen. 
     Status met hogere index komt het meest omhoog.
    statusFunction heeft 3 parameters: route, de representatie van left (of null), en representatie van right (of null)
     en de statusFunctie moet een object terug geven in de vorm van:
     {
        index: 100,
        name: 'Uitzonderlijk verkeer'
     }
     */
    setStatusFunction: function(statusFunction) {
        this.statusFunction = statusFunction;
    },

    // geeft een index terug die bepaald hoe hoog de route moet staan in het overizcht
    // hoe hoger, hoe meer omhoog
    // bv: function(route, obj1, obj2) { return -obj1.speed; } sorteert op snelheid, traagste eerst
    // sortFunction heeft 3 parameters: route, de representatie van left (of null), en representatie van right (of null)
    // left en right representatie zullen nooit null zijn als of setLeft of setRight is uitgevoerd
    setSortIndexFunction: function(sortFunction) {
        this.sortFunction = sortFunction;
    },

    render: function() {
        var dataArr = [];

        for (var i = routes.length - 1; i >= 0; i--) {
            var route = routes[i];

            if (typeof route == "undefined") {
                continue;
            }

            var data = {
                id: route.id,
                name: route.name,
                description: route.getDescription(),
                length: route.getLength(),
                status: {index: -1000, name: 'Undefined'},
                statusIndex: 0,
                routeIndex: -10000000,
            };

            var left = '';
            var right = '';

            var leftObject = null;
            var rightObject = null;
            var unavailable = false;

            if (this.leftGetter !== null && this.leftRepresentation !== null) {
                leftObject = this.leftGetter(route);
                if (!leftObject || leftObject.empty) {
                    unavailable = true;
                }
            }

            if (this.rightGetter !== null && this.rightRepresentation !== null) {
                rightObject = this.rightGetter(route);
                if (!rightObject || leftObject.empty) {
                    unavailable = true;
                }
            }

            if (unavailable) {
                left = this.UNAVAILABLE_REPRESENTATION();
                right = '';
                data.status = {index: -100000000000, name: "Niet beschikbaar"};
            } else {
                if (leftObject !== null) {
                    left = this.leftRepresentation(route, leftObject);

                    if (this.sortFunction !== null) {
                        data.routeIndex = this.sortFunction(route, leftObject, rightObject);
                    }
                }
                if (rightObject !== null) {
                    right = this.rightRepresentation(route, rightObject);
                }
                if (this.statusFunction !== null) {
                    data.status = this.statusFunction(route, leftObject, rightObject);
                }
            }

            data.left = left;
            data.right = right;

            dataArr.push(data);
        }

        // Sorteer op basis van routeIndex en statusIndex
        // Van groot naar klein
        dataArr.sort(function(a, b) {
            if (a.status.index == b.status.index) {
                return b.routeIndex - a.routeIndex;
            }
            return b.status.index - a.status.index;
        });

        var lastStatus = '';
        var str = '';
        dataArr.forEach(function (data){
            if (lastStatus != data.status.name){
                if (lastStatus != ''){
                    str += '<hr>';
                }
                lastStatus = data.status.name;
                str += "<h1>"+lastStatus+"</h1>";
            }
            str += Mustache.renderTemplate("listbuilder", data);
        });
        return str;
    }
};