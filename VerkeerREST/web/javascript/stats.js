// Deze code genereert de demo chart, deze moeten we dus aanpassen als we echte data hebben

google.charts.load("current", {packages:["corechart"]});

// Opent de bijpassende grafiek voor dit element (= this)
function toggleGraph(){
	var box = $(this).find('.graph-box');
	if(box.length == 0 || !box.is(":visible")){
		// Grafiek toevoegen
		// TODO: data doorgeven (moet ook in deze functie enzo)

		if (box.length == 0){
			$(this).append('<div class="graph-box"><div class="arrow"></div><div class="graph-shadow"></div><div class="graph"><div class="graph-content"><img class="loading" src="images/loading.gif" alt="Bezig met laden"></div></div></div>');
			box = $(this).find('.graph-box');
			console.log('grafiek toegevoegd '+box);
		}

		var graphContent = box.find('.graph-content');

		// Small hack to find the right width and height for the chart
		// Prevent duplicate code here -> no hardcoded height, only in css files
		box.css({ visibility: "hidden", display: "block" });
		var width = graphContent.outerWidth();
		var height = graphContent.outerHeight();
		box.css({ visibility: "", display: "" });

		// Momenteel voeg ik gewoon de live grafiek toe, maar dit moet berekend gebeuren
		// Later kan dit ook een periode, periodevergelijk, of dag/dag grafiek worden
		// Zal uit de html moeten worden afgeleid (extra data- attributen toevoegen)
		Dashboard.openLiveGraph($(this).attr('data-route'), graphContent[0], width, height);

		box.slideDown('fast', function() {
			
		});

	} else{
		box.slideUp('fast', function() {
			// Zouden we eigenlijk kunnen houden, maar de grafieken zijn nogal zwaar voor een browser
			// Het is dus beter om ze te verwijderen en opnieuw aan te maken wanneer nodig
			// De data die ze bevatten is toch gecached in de objecten
			box.remove();
		});
	}
}

// Converteert 6.25 naar 6:15
function floatToHour(i){
	var hour = Math.floor(i)%24;
	var minutes = ("00"+(i-hour)*60).slice(-2); // Minuten padden zodat het altijd 2 lang is
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
	var arr = [
	      ['Tijdstip'],
	];

	// Dit stuk converteert het data object naar hetgene google verwacht (zie hun documentatie hiervoor)

	for (var key in data) {
			arr[0].push(key);
	}

	for (var i = 6; i <= 24; i+= Api.intervalDecimal) {
		var a = [floatToHour(i)];

		for (var key in data) {
			a.push(data[key][i]);
		}
		arr.push(a);
	}

	// data is nu omgezt naar [[yas, title1, title2], [xval, yval1, yval2], ...] formaat
	// nu kunnen we dit omzetten naar de DataTable die google verwacht met eigen google functie
	var d = google.visualization.arrayToDataTable
	    (arr);

	// Options van de grafiek
	// TODO: opties staan nu gewoon op die van de live grafiek, moet meegestuurd worden!

	var options = {
		legend: 'top',
		fontName: 'Roboto',
		width: width,
		height: height,
		chartArea:{left:80,top:60,right:80, bottom: 50},
		series: {
			0: { color: '#63A7FF', 'lineWidth': 2, 'curveType': 'function', pointSize: 0},
			1: { color: '#A8A8A8', 'lineWidth': 2, 'lineDashStyle':  [4, 4], 'curveType': 'function' },
			2: { labelInLegend: null, color: '#63A7FF', 'lineWidth': 0, 'curveType': 'none', pointSize: 6}
		},
		vAxis: {
		    baselineColor: '#A8A8A8'

		},
		hAxis: {
		    baselineColor: '#A8A8A8',
		    textStyle: {
				color: '#5E5E5E',
				italic: false
			}
		},
		vAxis: {
		    baselineColor: '#A8A8A8',
		    textStyle: {
				color: '#5E5E5E',
				italic: false
			}
		}
	};

	var chart = new google.visualization.LineChart(element);
	chart.draw(d, options);
}