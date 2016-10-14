<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<t:page pageTitle="Statistics per user">
	<jsp:attribute name="navBar">
        <t:navBar activeMenuItem="stat"></t:navBar>
	</jsp:attribute>
	<jsp:attribute name="bodyFooter">
        <t:footer></t:footer>
	</jsp:attribute>
	<jsp:attribute name="jsIncludes">
		<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
	</jsp:attribute>
	
	<jsp:body>
	
		<script type="text/javascript">
		
			var usersStat = ${data.statisticsJson};
		
			function drawChart(info) {
				var data = new google.visualization.DataTable();
			      
			      for (var i = 0; i < info.labels.length; i++) {
			    	  var label = info.labels[i];
			    	  data.addColumn(label.type, label.name);
			      }
		
			      for (var i = 0; i < info.dataset.length; i++) {
			    	  var values = info.dataset[i];
			    	  data.addRow(values);
			      }
		
			      var options = {
			        height: 700,
			        hAxis: {
			          title: info.hAxisTitle,
			          gridlines: { count: info.dataset.length }
			        },
			        vAxis: {
			          title: info.vAxisTitle
			        },
			        series: {
			          1: { curveType: 'function' }
			        }
			      };
		
			      var chart = new google.visualization.LineChart(document.getElementById(info.renderTo));
			      chart.draw(data, options);
			}
			
			function getDataset(usersStat, feature) {
				var dataset = [],
					structure = [],
					maxLength = 0;
				
				for (var i = 0; i < usersStat.length; i++) {
					var userStat = usersStat[i],
						list = userStat[feature];
				
					structure[i] = list;
					
					if (list.length > maxLength) {
						maxLength = list.length;
					} 
				}
				
				for (var i = 0; i < maxLength; i++) {
					var part = [i];
					for (var j = 0; j < structure.length; j++) {
						part[j+1] = structure[j][i] || null;
					}
					dataset[i] = part;
				}
				
				return dataset;
			}
			
			window.onload = function() {
		
				var labels = [{ name: 'Experiment', type: 'number' }],
					dataset1 = getDataset(usersStat, 'keypressList'),
					dataset2 = getDataset(usersStat, 'betweenKeypressList');
				
				for (var i = 0; i < usersStat.length; i++) {
					labels[i+1] = { name: usersStat[i].user.name, type: 'number' };
				}
				
				google.charts.load('current', {packages: ['corechart', 'line']});
				google.charts.setOnLoadCallback(function() {
					drawChart({
						labels: labels,
						dataset: dataset1,
						hAxisTitle: 'Experiment',
						vAxisTitle: 'Keypress Time, ms',
						renderTo: 'keypressChartDiv'
					});
					drawChart({
						labels: labels,
						dataset: dataset2,
						hAxisTitle: 'Experiment',
						vAxisTitle: 'Time Between Keypresses, ms',
						renderTo: 'betweenKeypressChartDiv'
					});
				});
			}
				
		</script>
	
		<div id="content" class="content">
		    <div id="keypressChartDiv"></div>
	    	<div id="betweenKeypressChartDiv"></div>
	    	<div></div>
		</div>
	</jsp:body>

</t:page>
