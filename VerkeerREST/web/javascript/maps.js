/* global url */

/****************************
 * initialisers to set scope
 ****************************/
var colors = []; // will be filled with color for each route
var infowindow; // the info window for extra info about a route
var markers = []; // will be filled with the markers for events
var lines = {}; // will be filled with line-object for each route
var map; // object for the map

/****************************
 * general settings
 ****************************/
// map settings
var mapCenter = {"lat": 51.076317, "lng": 3.7096717};
var zoomCurrent = 12;
// colors
var normalTrafficColor = '#39D930';
var mediumTrafficColor = '#e67e22';
var heavyTrafficColor = '#C10037';
var unknownColor = '#CCCCCC';
var selectedColor = '#3333AA';
// line settings
var zoomThreshold = 14;
var zoomedInWeight = 2;
var zoomedOutWeight = 4;
var hoverWeight = zoomedOutWeight;
// event settings
var eventImage = "images/warning.png";

/****************************
 * initialiser for the map
 ****************************/
function initMap() {
    map = new google.maps.Map(document.getElementById('map'), {// generate map in #map
        "center": mapCenter, // center so all routes are visible
        "zoom": zoomCurrent, // zoom so all routes are visible
        "mapTypeId": google.maps.MapTypeId.TERRAIN, // set default to terrain
        "mapTypeControl": true, // allow mapTypeControl
        "mapTypeControlOptions": {
            "mapTypeIds": [google.maps.MapTypeId.TERRAIN, google.maps.MapTypeId.SATELLITE, google.maps.MapTypeId.HYBRID], // allow terrain and sattelite (with or without labels)
        },
        "streetViewControl": false, // disable street view
        "styles": [{
                featureType: "poi",
                stylers: [
                    {zIndex: 0}
                ]
            }]
    });

    map.addListener('click', function (event) { // close info window with click on map
        if (infowindow != null) {
            infowindow.close();
            infowindow = null;
            generateLines();
        }
    });
    map.addListener('zoom_changed', zoomChanged); // change line weight on different zoom levels
    map.idleAdder = map.addListener('idle',addIdleListener); // adds a listener to the map that executes when it becomes idle
}

/****************************
 * this adds a function to the map that generates the url parameters for the map-center and zoom level when the onIdle of the map is fired
 * this setup is needed because the default value should not be stored in the url (no changes to the view == no parameters needed)
 ****************************/
function addIdleListener(event){
    map.addListener('idle', function(event){
        var center = map.getCenter();
        url.setQueryParam("mapCenter",center.lat()+","+center.lng());
        url.setQueryParam("mapZoom",map.getZoom());
    });
    
    google.maps.event.removeListener(map.idleAdder); // removes the idleListener-adder
}

/****************************
 * reloads the map to update the data
 ****************************/
function reloadMap() {
    loadingMap(true); // show loading icon
    generateLines(); // generates the data for all routes
    updateColors(); // generate new colors for the lines
    loadingMap(false); // hide loading icon
}

/****************************
 * shows/hides the loading icon on the map based on boolean (true/false respectively)
 ****************************/
function loadingMap(boolean) {
    var overlay = $('#map-overlay');
    var content = $(overlay).find("#content");

    if (boolean) { // show when loading
        content.fadeIn();
        overlay.show();
    } else { // hide when not loading
        overlay.hide();
        content.fadeOut();
    }
}

/****************************
 * method called when zoom level of the map is changed
 ****************************/
function zoomChanged(event) {
    zoomCurrent = this.getZoom();
    var weight = getWeight(); // get the weight of the lines for this zoom level
    for (var i in lines) {
        lines[i].setOptions({strokeWeight: weight}); // set new weight on all lines
    }
}

/****************************
 * creates new line objects to show on the map
 * will remove old lines if they are present
 ****************************/
function generateLines() {
    if (lines.length !== 0) { // lines already exist
        deleteLines(); // remove them all
    }
    var weight = getWeight();
    for (var i in routes) { // for each route
        var line = new google.maps.Polyline({// create new line-object
            path: routes[i].waypoints, // path of current route
            strokeColor: colors[i], // color of current route
            strokeOpacity: 1.0, // non-transparent
            strokeWeight: weight, // default weight
            zIndex: getZIndex(colors[i]) // puts heavy traffic in front, followed by medium and normal traffic
        });

        google.maps.event.addListener(line, 'click', lineClicked); // shows info window on click
        google.maps.event.addListener(line, 'mouseover', lineHover); // makes line bigger on hover
        google.maps.event.addListener(line, 'mouseout', lineOut); // resets line to original width

        line.setMap(map); // add line to map
        line["id"] = i; // assign id to map, this is the index in the lines array
        lines[i] = line; // add line to lines-array
    }
}

/****************************
 * removes all lines from the map and deletes them
 ****************************/
function deleteLines() {
    for (var i in lines) {
        lines[i].setMap(null); // remove line from map
    }
    lines = []; // delete all lines
}

/****************************
 * generate the correct color for each route
 ****************************/
function updateColors() {
    var providerId = Dashboard.provider.id;
    for (var i in routes) {
        // check if route data is available
        if (!routes[i].hasLiveDataRepresentation(providerId)) {
            continue;
        }
        var colorStatus = routes[i].getColor(routes[i].liveData[providerId].representation); // get the new color
        switch (colorStatus) {
            case 'red':
                colors[i] = heavyTrafficColor; // heavy traffic
                lines[i].setOptions({strokeColor: heavyTrafficColor, zIndex: getZIndex(heavyTrafficColor)});
                break;
            case 'orange':
                colors[i] = mediumTrafficColor; // medium traffic
                lines[i].setOptions({strokeColor: mediumTrafficColor, zIndex: getZIndex(mediumTrafficColor)});
                break;
            case 'grey':
                colors[i] = unknownColor;
                lines[i].setOptions({strokeColor: mediumTrafficColor, zIndex: getZIndex(unknownColor)});
            default:
                colors[i] = normalTrafficColor; // normal traffic
                lines[i].setOptions({strokeColor: normalTrafficColor, zIndex: getZIndex(normalTrafficColor)});
                break;
        }
    }
}

/****************************
 * returns z-index based on traffic type
 * needs color update before using this method for correct information
 ****************************/
function getZIndex(color) {
    var z_index = 0; // normal traffic is most to the back
    if (color === heavyTrafficColor) { // heavy traffic in front
        z_index = 2;
    } else if (color === mediumTrafficColor) { // medium traffic inbetween
        z_index = 1;
    }
    return z_index;
}

/****************************
 * returns the current line weight
 ****************************/
function getWeight() {
    if (zoomThreshold < zoomCurrent) { // zoomed in
        return zoomedInWeight;
    }
    return zoomedOutWeight; // zoomed out
}

/****************************
 * generates an info window and adds it to the map
 * requires a location and a message to display
 ****************************/
function createInfoWindow(latLng, message) {
    if (infowindow != null) { // close old window if one exists
        infowindow.close();
    }
    infowindow = new google.maps.InfoWindow({// create info window
        content: message,
        position: latLng // create window on given location
    });
    infowindow.open(map); // show info window
    return infowindow;
}

/****************************
 * method called when a line is clicked
 * shows an info window about the route
 ****************************/
function lineClicked(event) {
    if (infowindow != null) {// if an infoWindow is already shown
        updateColors(); // change colors
    }
    // get the current time that is needed for the current route and format it
    var currentTime = Number(routes[this['id']].liveData[Dashboard.provider.id].representation.time);
    var currentMinutes = Math.floor(currentTime);
    var currentSeconds = currentTime - currentMinutes;
    currentMinutes += (currentSeconds < .5) ? 0 : 1;
    // get the average time that is needed for the current route and format it
    var averageTime = Number(routes[this['id']].avgData[Dashboard.provider.id].representation.time);
    var averageMinutes = Math.floor(averageTime);
    var averageSeconds = averageTime - averageMinutes;
    averageMinutes += (averageSeconds < .5) ? 0 : 1;
    // get the length of the current route
    var distance = routes[this['id']].length;
    
    // generate the contents of the info window
    var message = '<content id="infoWindow">'
            + '<h1>' + routes[this['id']].name + ' </h1>'
            + '<h2>' + routes[this['id']].description + '</h2>'
            + '<p class="infoWindowCurrentTime">Huidige reistijd: </p>'
            + '<p class="infoWindowCurrentTime value">' + currentMinutes + ' minuten</p>'
            + '<p class="infoWindowAverageTime">Gemiddelde reistijd: </p>'
            + '<p class="infoWindowAverageTime value">' + averageMinutes + ' minuten</p>'
            + '<p class="infoWindowRouteLenght">Lengte van traject:</p>'
            + '<p class="infoWindowRouteLenght value">' + Math.round(distance/10)/100 + ' kilometer</p>'
            + '</content>';
    // generate the info window on the clicked location
    createInfoWindow(event["latLng"], message);
    
    // add accent to line
    this.setOptions({strokeWeight: hoverWeight, zIndex: 3, strokeColor: selectedColor});
    // add the close event to the 'x'-button
    google.maps.event.addListener(infowindow,'closeclick',closeInfoWindow);
}

/****************************
 * closes the current infowindow and regenerates the lines
 ****************************/
function closeInfoWindow(){
    infowindow = null;
    generateLines();
}

/****************************
 * set line bolder on hover
 ****************************/
function lineHover(event) {
    if (infowindow == null) { // if no infoWindow is shown
        this.setOptions({strokeWeight: hoverWeight, zIndex: 3, strokeColor: selectedColor}); // add accent to line
    }
}

/****************************
 * revert line to original width
 ****************************/
function lineOut(event) {
    if (infowindow == null) { // if no infoWindow is shown
        this.setOptions({strokeColor: colors[this['id']], zIndex: getZIndex(colors[this['id']]), strokeWeight: getWeight()}); // return line to default display
    }
}

/****************************
 * generates a traffic event and adds it to the map
 * stores it in the markers array
 ****************************/
function createMarker(latLng) {
    var marker = new google.maps.Marker({
        "position": latLng,
        "map": map,
        "icon": eventImage,
    }); // create marker on given location

    google.maps.event.addListener(marker, 'click', markerClicked); // shows info window on click
    markers.push(marker); // store marker in markers
    return marker;
}

/****************************
 * removes all events from the map and deletes them
 ****************************/
function deletemarkers() {
    for (var i = 0; i < markers.length; i++) {
        markers[i].setMap(null); // remove from map
    }
    markers = []; // deletes all events
}

/****************************
 * method called when marker is clicked
 ****************************/
function markerClicked(event) {
    createInfoWindow(event["latLng"], "test");
}
