var Statistics = (function() {

	return {
	   
		getRandomColor: function() {
			    var letters = '0123456789ABCDEF';
			    var color = '#';
			    for (var i = 0; i < 6; i++ ) {
			        color += letters[Math.floor(Math.random() * 16)];
			    }
			    return color;
		},
		
		prepareSeries: function(stats) {
			var self = this; 
			var series = [];
			 
			$.each(stats, function(ind, userStat) {
				 var sample = {
				     name: userStat.user.name,
				     color: self.getRandomColor(),
				     data: []
				 };
				 
				 $.each(userStat.features, function(i, feature) {
					 if (feature.value > 0) {
				         sample.data.push([ feature.code, feature.value ]);
					 }
				 });
				 series.push(sample);	 
			 });
			 
			 return series;
		},

		createChart: function(series) {
			 $(function () {
				    Highcharts.chart('plot', {
				        chart: {
				            type: 'scatter',
				            zoomType: 'xy'
				        },
				        title: {
				            text: 'Keypress time per code'
				        },
				        xAxis: {
				            title: {
				                enabled: true,
				                text: 'Code'
				            },
				            startOnTick: true,
				            endOnTick: true,
				            showLastLabel: true
				        },
				        yAxis: {
				            title: {
				                text: 'Keypress time (ms)'
				            }
				        },
				        legend: {
				            layout: 'vertical',
				            align: 'left',
				            verticalAlign: 'top',
				            x: 60,
				            y: 0,
				            floating: true,
				            backgroundColor: (Highcharts.theme && Highcharts.theme.legendBackgroundColor) || '#FFFFFF',
				            borderWidth: 1
				        },
				        plotOptions: {
				            scatter: {
				                marker: {
				                    radius: 5,
				                    states: {
				                        hover: {
				                            enabled: true,
				                            lineColor: 'rgb(100,100,100)'
				                        }
				                    }
				                },
				                states: {
				                    hover: {
				                        marker: {
				                            enabled: false
				                        }
				                    }
				                },
				                tooltip: {
				                    headerFormat: '<b>{series.name}</b><br>',
				                    pointFormat: 'Code: {point.x}; Value: {point.y} ms'
				                }
				            }
				        },
				        series: series
				    });
				});
		},
		
		fetchKeypressPlotData: function() {
			var self = this;
	        	$.ajax({
	        		url: '/ajax/keypressPlotData',
	                success: function(data) {
	                	var responseObj = JSON.parse(data.statisticsJson);
	                	var series = self.prepareSeries(responseObj);
	                	self.createChart(series);
	                },
	                error: function() {
	                	console.error("Can not fetch key press features data for plot");
	                }
	        	});
		}
	};
})();