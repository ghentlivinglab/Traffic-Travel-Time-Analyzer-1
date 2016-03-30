// Deze code genereert de demo chart, deze moeten we dus aanpassen als we echte data hebben

google.charts.load("current", {packages:["corechart"]});
//google.charts.setOnLoadCallback(drawChart);
function drawChart(element, width, height) {
	var arr = [
	      ['Tijdstip', 'Vandaag', 'Normaal', 'Uren'],
	];


	// Random genereren (tijdelijk!)
	var base = 8;
	var base2 = 8;
	for (var i = 6; i <= 24; i+=0.25) {
		if (i > 7 && i < 10 || i > 16 && i < 18){
			base += Math.random();
			base2 += Math.random();
		}
		if (i > 18){
			base -= Math.random();
			base2 -= Math.random();
		}
		if (base > 6){
			base += Math.random() * 1 - 0.7;
		}else{
			base += Math.random() * 2;
		}
		if (base2 > 5){
			base2 += Math.random() * 1 - 0.7;
		}else{
			base2 += Math.random() * 2;
		}
		if (i > 14){
			base = null;
		}
		var hour = Math.floor(i);
		var minutes = ("00"+(i-hour)*60).slice(-2); // Minuten padden zodat het altijd 2 lang is
		if (hour == 24){
			hour = 0;
		}
		if (hour == i){
			arr.push([hour+":"+minutes, base, base2, base]);
		}else{
			arr.push([hour+":"+minutes, base, base2, null]);
		}
	}

	var data = google.visualization.arrayToDataTable
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
	chart.draw(data, options);
}