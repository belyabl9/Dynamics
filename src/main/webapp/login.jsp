<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page session="false" %>
<t:page pageTitle="Sign In">
	<jsp:attribute name="navBar">
	</jsp:attribute>
	<jsp:attribute name="bodyFooter">
	</jsp:attribute>
	<jsp:body>

      <form class="form-signin" method="post" action="/auth">
        <h2 class="form-signin-heading">
        	Please sign in
        </h2>
        <input type="text" name="login" class="form-control" style="margin-top:5px;" placeholder="Login" required autofocus>
        <input type="password" name="password" class="form-control" style="margin-top:5px;" placeholder="Password" required>
        <button class="btn btn-lg btn-primary btn-block" style="margin-top:10px;" type="submit">
        	Sign in
        </button>
      </form>

	</jsp:body>
</t:page>