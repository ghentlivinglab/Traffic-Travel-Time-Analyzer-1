// Deze code genereert de demo chart, deze moeten we dus aanpassen als we echte data hebben

google.charts.load("current", {packages:["corechart"]});

// Opent de bijpassende grafiek voor dit element (= this)
function toggleGraph(){
	var box = $(this).find('.graph-animation-box');
	if(box.length == 0 || !box.is(":visible")){
		// Grafiek toevoegen
		// TODO: data doorgeven (moet ook in deze functie enzo)

		if (box.length == 0) {
			$(this).append('<div class="graph-animation-box"><div class="graph-box"><div class="arrow"></div><div class="graph-shadow"></div><div class="graph"><div class="graph-loading"><img class="loading" src="images/loading.gif" alt="Bezig met laden"></div><div class="graph-content"></div></div></div></div>');
			box = $(this).find('.graph-animation-box');
			box.click(function(e) {
				e.stopPropagation();
			});

            // Animatie starten voor het loading screen
            box.css({ 'display': 'block'});

            var graphContent = box.find('.graph-content');
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
        var graph = box.find('.graph');
		box.slideUp('slow', function() {
			// Zouden we eigenlijk kunnen houden, maar de grafieken zijn nogal zwaar voor een browser
			// Het is dus beter om ze te verwijderen en opnieuw aan te maken wanneer nodig
			// De data die ze bevatten is toch gecached in de objecten
			box.remove();
		});
	}
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
function drawChart(element, data, width, height, dotted) {
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

	if (typeof dotted == "undefined"){
		dotted = false;
	}

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

    console.log('voor omzetten '+Date.now());
	// data is nu omgezt naar [[yas, title1, title2], [xval, yval1, yval2], ...] formaat
	// nu kunnen we dit omzetten naar de DataTable die google verwacht met eigen google functie
	var d = google.visualization.arrayToDataTable
	    (arr);

    console.log('na omzetten '+Date.now());

	var colors = ['#800024', '#C10037', '#FC4D7E', '#005AC1', '#51A2FF', '#A8A8A8', '#D2D2D2'];

	// Options van de grafiek
	// TODO: opties staan nu gewoon op die van de live grafiek, moet meegestuurd worden!
	var defSettings = { color: '#63A7FF', 'lineWidth': 2, 'curveType': 'function', pointSize: 0, calc: function () {
                            return null;
                        }};
	var avgSettings = { color: '#A8A8A8', 'lineWidth': 2, 'lineDashStyle':  [4, 4], 'curveType': 'function' };

	var series = {};
	var legend = { position: 'top', alignment: 'start' };
	var padding = {left:90,top:50,right:20, bottom: 70};
	if (dotted) {
		series = {0: defSettings, 1: avgSettings};
	}else{
		if (Object.keys(data).length > 2){
			legend = { position: 'right', alignment: 'start' };
			padding = {left:90,top:20,right:140, bottom: 70};
		}else{
			colors = ['#800024', '#005AC1'];
		}

		for (var i = 0; i < arr[0].length-1; i++) {
			// Dupliceren
			var obj = JSON.parse(JSON.stringify(defSettings));
			obj.color = colors[i];

			// weekend in stippellijnen
			if (i >= 5) {
				obj.lineDashStyle = [4, 4];
			}
			series[i] = obj;
		}
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

    console.log('aanmaken ' +Date.now());
	var chart = new google.visualization.LineChart(element);
    console.log('na aanmaken '+Date.now());

	var columns = [];
    for (var i = 0; i < d.getNumberOfColumns(); i++) {
        columns.push(i);
    }

    console.log('voor drawen '+Date.now());
    if (columns.length > 3){
        for (var col = 2; col < columns.length; col++) {
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
    console.log('na drawen '+Date.now());

    
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
    

    var box = $(element).parents('.graph-box');

    $(element).css({'display': 'block'});
    var height = $(element).outerHeight() ;

    // Dit stuk voorkomt lag tijdens de animatie door het renderen van de grafiek
    setTimeout(function(){
        $(element).parents('.graph').stop().animate({'height': height}, 'fast', function() {
            console.log('animation done');
            box.find('.graph-loading').remove();
            $(element).css({'visibility': 'visible'});
            $(this).css({height: 'auto'});
        });
    }, 100);
}