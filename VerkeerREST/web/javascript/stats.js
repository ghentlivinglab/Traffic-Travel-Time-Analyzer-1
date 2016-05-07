// Deze code genereert de demo chart, deze moeten we dus aanpassen als we echte data hebben

google.charts.load("current", {packages:["corechart"]});

// Opent de bijpassende grafiek voor dit element (= this)
function toggleGraph(){
	var box = $(this).find('.graph-animation-box');
	if(box.length == 0 || !box.is(":visible")){
		// Grafiek toevoegen
		// TODO: data doorgeven (moet ook in deze functie enzo)

		if (box.length == 0) {
			$(this).append('<div class="graph-animation-box"><div class="graph-box"><div class="arrow"></div><div class="graph-shadow"></div><div class="graph"><div class="graph-loading"><img class="loading" src="images/loading.gif" alt="Bezig met laden"></div><div class="graph-content"><div class="google-graph"></div><div class="extra-content"></div></div></div></div></div>');
			box = $(this).find('.graph-animation-box');
			box.click(function(e) {
				e.stopPropagation();
			});

            // Animatie starten voor het loading screen
            box.css({ 'display': 'block'});

            var graphContent = box.find('.google-graph');
            var graph_width = box.outerWidth();
            var graph_height = 350;
            var route = $(this).attr('data-route');

            var graph = box.find('.graph');
            var height = box.outerHeight();
            graph.css({ 'height': '0px'});

            // We laden pas nadat de animatie klaar is, om lag te voorkomen
            // Optimalisatie zou kunnen zijn om wel het downloaden, maar niet het weergeven al te starten
            graph.animate({'height': height}, 'fast', function() {
                Dashboard.openGraph(route, graphContent[0], graph_width, graph_height);
            });
            
		} else {
    		var graphContent = box.find('.graph-content');

    		var width = box.parent().outerWidth();
    		var height = 350;

    		Dashboard.openGraph($(this).attr('data-route'), graphContent[0], width, height);
        }
	} else{
		box.slideUp('fast', function() {
			box.remove();
		});
	}
}

function addExtraProvider() {
    var box = $(this).parents('.graph-animation-box');
    var route = $(this).parents('article').attr('data-route');
    var graphContent = box.find('.google-graph');

    var providerId = $(this).attr('data-provider');
    var graph_width = graphContent.outerWidth();
    var graph_height = graphContent.outerHeight();

    Dashboard.openGraph(route, graphContent[0], graph_width, graph_height, providerId);
}

function removeExtraProvider() {
    var box = $(this).parents('.graph-animation-box');
    var route = $(this).parents('article').attr('data-route');
    var graphContent = box.find('.google-graph');

    var providerId = $(this).attr('data-provider');
    var graph_width = graphContent.outerWidth();
    var graph_height = graphContent.outerHeight();

    Dashboard.removeExtraProvider(providerId);
    Dashboard.openGraph(route, graphContent[0], graph_width, graph_height);
}

// Converteert 6.25 naar 6:15
function floatToHour(i){
	var hour = Math.floor(i);
	var minutes = ("00"+(i-hour)*60).slice(-2); // Minuten padden zodat het altijd 2 lang is
	hour = hour%24; // Moet erna om minuten te laten werken
	return hour+':'+minutes;
}

// Voegt een grafiek toe in element, met data, width en height
// Data bevat formaat:
/*
	{
		'Vandaag': { // Title 
			6: 123, // Tijdstip als decimaal getal -> data
			6.25: 351,
			...
		},
		...
	}

*/
function drawChart(element, data, width, height) {
	for (var key in data) {

		// lege keys negeren we
		if (!data[key] || Object.keys(data[key]).length == 0){
			delete data[key];
            continue;
		}
        var count = 0;
        for (var i = 6; i <= 24; i+= Api.intervalDecimal) {
            if (typeof data[key][i] != 'undefined' && data[key][i]) {
                count++;
                break;
            }
        }
        if (count == 0){
            delete data[key];
        }
	}
	if (Object.keys(data).length == 0){
		$(element).html('<p>Geen data beschikbaar</p>');
		return;
	}

	var arr = [
	      ['Tijdstip'],
	];

	var minimum = 0;

	// Dit stuk converteert het data object naar hetgene google verwacht (zie hun documentatie hiervoor)

	for (var key in data) {
		// lege keys negeren we
		arr[0].push(key);
	}
         console.log(data);
	for (var i = 6; i <= 24; i+= Api.intervalDecimal) {

		var a = [floatToHour(i)];

		for (var key in data) {

            a.push(data[key][i]);
            if (typeof data[key][i] != 'undefined' && data[key][i] && (minimum == 0 || data[key][i] < minimum)) {
            	minimum = data[key][i];
            }
		}
		arr.push(a);
	}
        console.log(arr);

	// data is nu omgezt naar [[yas, title1, title2], [xval, yval1, yval2], ...] formaat
	// nu kunnen we dit omzetten naar de DataTable die google verwacht met eigen google functie
	var d = google.visualization.arrayToDataTable
	    (arr);

	// Options van de grafiek
	var defSettings = {'lineWidth': 2, 'curveType': 'function', pointSize: 0, calc: function () {
                            return null;
                        }};
	var avgSettings = {'lineWidth': 2, 'lineDashStyle':  [4, 4], 'curveType': 'function' };

	var series = {};
	var legend = { position: 'top', alignment: 'start' };
	var padding = {left:90,top:50,right:20, bottom: 70};

	if (Object.keys(data).length > 2){
		legend = { position: 'right', alignment: 'start' };
		padding = {left:90,top:20,right:140, bottom: 70};
	}

	for (var i = 0; i < arr[0].length-1; i++) {
		// Dupliceren
        var name = arr[0][i+1];
        if (name.match(/Gemiddelde/gi)) {
            var obj = JSON.parse(JSON.stringify(avgSettings));
        } else {
            var obj = JSON.parse(JSON.stringify(defSettings));
        }

		series[i] = obj;
	}

	var series_copy = JSON.parse(JSON.stringify(series));
	var options = {
		legend: legend,
		fontName: 'Roboto',
		width: width,
		height: height,
		chartArea:padding,
		series: series,
		hAxis: {
		    baselineColor: '#A8A8A8',
		   	title: "Tijdstip",
		    textStyle: {
				color: '#5E5E5E',
				italic: false
			}
		},
		vAxis: {
		    baselineColor: '#A8A8A8',
		    title: "Reistijd (minuten)",
		    slantedText:true,
		    slantedTextAngle:90,

		    textStyle: {
				color: '#5E5E5E',
				italic: false
			},
			minValue: Math.max(0, Math.floor(minimum)),
	        minorGridlines: {
	        	count: 1
	        }
		}
	};

	var chart = new google.visualization.LineChart(element);

	var columns = [];
    for (var i = 0; i < d.getNumberOfColumns(); i++) {
        columns.push(i);
    }

    if (columns.length > 5){
        for (var col = 5; col < columns.length; col++) {
            if (columns[col] == col) {
                // hide the data series
                columns[col] = {
                    label: d.getColumnLabel(col),
                    type: d.getColumnType(col),
                    calc: function () {
                        return null;
                    }
                };
                
                // grey out the legend entry
                series[col - 1].color = '#CCCCCC';
            }
        }
        var view = new google.visualization.DataView(d);
        view.setColumns(columns);
        chart.draw(view, options);
    }else{
        chart.draw(d, options);
    }
    
    google.visualization.events.addListener(chart, 'select', function () {
        var sel = chart.getSelection();
        // if selection length is 0, we deselected an element
        if (sel.length > 0) {
            // if row is undefined, we clicked on the legend
            if (sel[0].row === null) {
            	console.log("clicked");
                var col = sel[0].column;
                if (columns[col] == col) {
                    // hide the data series
                    columns[col] = {
                        label: d.getColumnLabel(col),
                        type: d.getColumnType(col),
                        calc: function () {
                            return null;
                        }
                    };
                    
                    // grey out the legend entry
                    series[col - 1].color = '#CCCCCC';
                }
                else {
                    // show the data series
                    columns[col] = col;
                    series[col - 1].color = series_copy[col - 1].color;
                }
                var view = new google.visualization.DataView(d);
                view.setColumns(columns);
                chart.draw(view, options);
            }
        }
    });
    
    var graphContent = $(element).parent();
    if (graphContent.css('display') != 'block') {
        var box = $(element).parents('.graph-box');

        graphContent.css({'display': 'block'});
        var height = graphContent.outerHeight() ;

        // Dit stuk voorkomt lag tijdens de animatie door het renderen van de grafiek
        setTimeout(function(){
            $(element).parents('.graph').stop().animate({'height': height}, 'fast', function() {
                box.find('.graph-loading').remove();
                graphContent.css({'visibility': 'visible'});
                graphContent.css({'display': 'none'});
                graphContent.fadeIn('fast', function() {
                    $(element).parents('.graph').css({height: 'auto'});
                });
            });
        }, 100);
    }
}