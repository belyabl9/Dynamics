<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<t:page pageTitle="User ${user.name}">
	<jsp:attribute name="navBar">
        <t:navBar></t:navBar>
    </jsp:attribute>
    <jsp:attribute name="bodyFooter">
	    <footer class="footer">
	      <div class="container">
	        <p class="footer-text">
	        	Keystroke Dynamics
	        	<br>
	        	Serhii Beliablia, MP-111
	        </p>
	      </div>
	    </footer>
    </jsp:attribute>
	<jsp:body>
		<form action="/page/addUser" id="userDetailsForm" method="post">
			<input type="hidden" name="id" value="New" />
			<div class="panel panel-primary" style="width:45%">
			   <div class="panel-heading">
			       <h3 class="panel-title">
			           User Details
			       </h3>
			   </div>
			   <div class="panel-body">
			   	   <t:userDetails />
				   <div class="pull-right">
				   	   <input type="submit" class="btn btn-primary" value="Add" />
				   </div>
			   </div>
			</div>
		</form>
	</jsp:body>

</t:page>
