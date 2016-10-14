<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<t:page pageTitle="User ${data.user.name}">
    <jsp:attribute name="navBar">
        <t:navBar></t:navBar>
    </jsp:attribute>
    <jsp:attribute name="bodyFooter">
	    <t:footer></t:footer>
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script type="text/javascript" src="/js/Users.js"></script>
    </jsp:attribute>
	<jsp:body>
        <div id="statusMessage"></div>
		<form id="userDetailsForm">
			<input type="hidden" name="id" value="${data.user.id}" />
			<div class="panel panel-primary pull-left" style="width:45%">
			   <div class="panel-heading">
			       <h3 class="panel-title">
			           User Details
			       </h3>
			   </div>
			   <div class="panel-body">
			   	   <t:userDetails user="${data.user}" />
				   <a class="btn btn-primary pull-right" onclick="Users.save();">Save</a>
			   </div>
			</div>
		</form>

        <div class="panel panel-primary pull-right" style="width:35%">
            <div class="panel-heading">
                <h3 class="panel-title">
                    Statistics
                </h3>
            </div>
            <div class="panel-body">
                <div style="margin-bottom:10px;">
                    <a href="#" class="btn btn-primary" style="margin-left:10px;"
                       onclick="Users.delFeatures(${data.user.id}); return false;">
                       Clear features
                    </a>
                </div>
                <table class="table table-bordered table-hover">
                    <tr>
                        <td>
                            Mean Keypress Time, ms
                        </td>
                        <td>
                            <fmt:formatNumber type="number" maxFractionDigits="2" value="${data.user.meanKeypressTime}" />
                        </td>
                    </tr>
                    <tr>
                        <td>
                            Mean Time Between Keypresses, ms
                        </td>
                        <td>
                            <fmt:formatNumber type="number" maxFractionDigits="2" value="${data.user.meanTimeBetweenKeys}" />
                        </td>
                    </tr>
                </table>
            </div>
        </div>

		<div style="clear:both;"></div>

	</jsp:body>

</t:page>
