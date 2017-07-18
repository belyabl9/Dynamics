<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<t:page pageTitle="Keystroke Dynamics">
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
		<div class="panel panel-primary">
            <div class="panel-heading">
              <h3 class="panel-title">Project Information</h3>
            </div>
            <div class="panel-body">
				This project is aimed to investigate current approaches for creating keystroke biometric systems
            </div>
        </div>
    </jsp:body>
</t:page>
