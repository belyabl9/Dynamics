<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
 <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<t:page pageTitle="Statistics per user">
	<jsp:attribute name="navBar">
        <t:navBar activeMenuItem="stat"></t:navBar>
	</jsp:attribute>
	<jsp:attribute name="bodyFooter">
        <t:footer></t:footer>
	</jsp:attribute>
	<jsp:attribute name="jsIncludes">
		<script src="/js/Statistics.js"></script>
        <script src="https://code.highcharts.com/highcharts.js"></script>
        <script src="https://code.highcharts.com/modules/exporting.js"></script>
	</jsp:attribute>
	
	<jsp:body>
	
 <script>
 
 Statistics.fetchKeypressPlotData();

 </script>

        <div class="panel panel-primary">
            <div class="panel-heading clearfix">
                <h3>Statistics</h3>
            </div>
            <div class="panel-body">
                <table class="table table-bordered table-hover">
                    <thead>
                        <th>User</th>
                        <th>Hold features</th>
                        <th>Release-press features</th>
                        <th>Mean hold press time, ms</th>
                        <th>Mean release-press time, ms</th>
                    </thead>
                    <tbody>
                        <c:forEach items="${data.users}" var="user">
                            <tr>
                                <td>${user.name}</td>
                                <td>${fn:length(user.releasePressFeatures)}</td>
                                <td>${fn:length(user.holdFeatures)}</td>
                                <td>${user.meanKeypressTime}</td>
                                <td>${user.meanTimeBetweenKeys}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

    <div id="plot" style="min-width: 800px; height: 600px; max-width: 1000px; margin: 0 auto"></div>

	</jsp:body>

</t:page>
