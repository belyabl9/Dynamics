<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<t:page pageTitle="Keystroke Dynamics">
	<jsp:attribute name="navBar">
        <t:navBar browserTraining="true"></t:navBar>
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
    <jsp:attribute name="jsIncludes">
        <script src="/js/Dynamics.js"></script>
    </jsp:attribute>
    <jsp:body>
    	
    	<script type="text/javascript">
    	$(document).ready(function() {
    		Dynamics.init();
    	});
    		
    	</script>
    
    	<div id="statusMessage"></div>
		<div class="panel panel-primary">
            <div class="panel-heading">
              <h3 class="panel-title">Keystroke Dynamics</h3>
            </div>
            <div class="panel-body">
				<ul class="nav nav-tabs">
				  <li style="background-color: #d2d3e1;">
					  <a class="active" data-toggle="tab" href="#auth">Authentication</a>
				  </li>
				  <li style="background-color: #d2d3e1;">
					  <a data-toggle="tab" href="#reg">Registration</a>
				  </li>
				</ul>
				
				<div class="tab-content">
				  <div id="auth" class="tab-pane fade in active">
				  	<form id="authForm">
					  <div class="panel panel-primary" style="width:45%">
						   <div class="panel-heading">
						       <h3 class="panel-title">
						           Authentication
						       </h3>
						   </div>
					  		<div class="panel-body">
								<div class="form-group row">
									<label class="col-form-label col-sm-3">Login</label>
									<div class="col-sm-9">
										<input type="text" class="form-control" name="login" placeholder="Enter the login" value="${user.login}" required></input>
									</div>
								</div>
								<div class="form-group row">
									<label class="col-form-label col-sm-3">Password</label>
									<div class="col-sm-9">
										<input type="password" class="form-control" name="password" placeholder="Enter the password" value="${user.password}" required></input>
									</div>
								</div>
							    <div class="form-group row" style="display: none">
									<label class="col-form-label col-sm-3">Learning text</label>
									<div class="col-sm-9">
										<textarea class="form-control" name="learningText"></textarea>
									</div>
							    </div>
							   <div class="pull-right">
							   	   <button class="btn btn-primary">Sign in</button>
							   </div>
						   	</div>
					    </div>
					    </form>
				  </div>
				  <div id="reg" class="tab-pane fade">
					<form id="registrationForm">
						<input type="hidden" name="id" value="New" />
						<div class="panel panel-primary" style="width:45%">
						   <div class="panel-heading">
						       <h3 class="panel-title">
						           User Details
						       </h3>
						   </div>
						   <div class="panel-body">
						   		<input type="hidden" name="userType" value="1"></input>
								<div class="form-group row">
									<label class="col-form-label col-sm-3">Name</label>
									<div class="col-sm-9">
										<input type="text" class="form-control" name="firstName" placeholder="Enter the name" value="${user.firstName}" required></input>
									</div>
								</div>
								<div class="form-group row">
									<label class="col-form-label col-sm-3">Surname</label>
									<div class="col-sm-9">
										<input type="text" class="form-control" name="surname" placeholder="Enter the surname" value="${user.surname}" required></input>
									</div>
								</div>
								<div class="form-group row">
									<label class="col-form-label col-sm-3">Login</label>
									<div class="col-sm-9">
										<input type="text" class="form-control" name="login" placeholder="Enter the login" value="${user.login}" required></input>
									</div>
								</div>
								<div class="form-group row">
									<label class="col-form-label col-sm-3">Password</label>
									<div class="col-sm-9">
										<input type="password" class="form-control" name="password" placeholder="Enter the password" value="${user.password}" required></input>
									</div>
								</div>
							   <div class="form-group row">
									<label class="col-form-label col-sm-3">Learning text example</label>
									<div class="col-sm-9">
										<textarea class="form-control" style="overflow: hidden;" required readonly>
											pack my bags with five dozen liquor jugs
										</textarea>
									</div>
							   </div>
							   <div class="form-group row">
									<label class="col-form-label col-sm-3">Learning text</label>
									<div class="col-sm-9">
										<textarea class="form-control" name="learningText" required></textarea>
									</div>
							   </div>
							   <div class="pull-right">
							   	   <button class="btn btn-primary">Register</button>
							   </div>
						   </div>
						</div>
					</form>
				  </div>
				</div>
            </div>
        </div>
    </jsp:body>
</t:page>