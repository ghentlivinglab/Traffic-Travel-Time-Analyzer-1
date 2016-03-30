// Deze code genereert de demo chart, deze moeten we dus aanpassen als we echte data hebben

google.charts.load("current", {packages:["corechart"]});
//google.charts.setOnLoadCallback(drawChart);

// 6.25 -> 6:15
function floatToHour(i){
	var hour = Math.floor(i);
	var minutes = ("00"+(i-hour)*60).slice(-2); // Minuten padden zodat het altijd 2 lang is
	if (hour == 24){
		hour = 0;
	}
	return hour+':'+minutes;
}

// Data bevat formaat:
/*
	{
		'Vandaag': {
			6: 123,
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

	for (var key in data) {
			arr[0].push(key);
	}
	console.log(data);

	for (var i = 6; i <= 24; i+= Api.intervalDecimal) {
		var a = [floatToHour(i)];

		for (var key in data) {
			a.push(data[key][i]);
		}
		arr.push(a);
	}
	console.log(arr);
	var d = google.visualization.arrayToDataTable
	    (arr);

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