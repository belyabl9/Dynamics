<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="domain.User" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="styles.css">
<title>User "${user.name}"</title>
</head>
<body>

	<script type="text/javascript">

	</script>

	<div class="header">
		<h1>User Info</h1>
		<div style="text-align: left; margin: 5px 0px 20px 15px; float: left;">
			<form action="/Dynamics">
				<input type="submit" value="Main Page" />
			</form>
		</div>
		<div style="text-align: left;margin: 5px 0px 20px 15px; float: left;">
			<form action="users">
				<input type="submit" value="Back to Users" />
			</form>
		</div>
	</div>

	<div class="content">
		<form action="users?action=save" method="post">
			<div 
				style="width:280px;"
				class="${not empty user.id ? 'float-left' : ''}"
			>
				<fieldset class="fieldset">
					<legend>User Details</legend>
					<input 
						type="hidden"
						name="id"
						value="${not empty user.id ? user.id : 'New'}"
					/>
					<div class="field">
						<label class="label">Name</label>
						<input type="text" name="firstName" value="${user.firstName}"></input>
					</div>
					<div class="field">
						<label class="label">Surname</label>
						<input type="text" name="surname" value="${user.surname}"></input>
					</div>
					<div class="field">
						<label class="label">Login</label>
						<input type="text" name="login" value="${user.login}"></input>
					</div>
					<div class="field">
						<label class="label">Password</label>
						<input type="text" name="password" value="${user.password}"></input>
					</div>
					<div class="field rightAlign">
						<input 
							type="submit"
							value="<%= request.getParameter("id").equals("New") ? "Add" : "Save" %>" 
						/>
					</div>
				</fieldset>
		</form>
			</div>
<% if (!request.getParameter("id").equals("New")) { %>
			<div class="user-actions float-left">
				<fieldset class="fieldset">
					<legend>Actions</legend>
					<form action="users?action=del" method="post">
						<input 
							type="hidden"
							name="id"
							value="${user.id}"
						/>
						<input 
							class="block action-button"
							type="submit"
							value="Delete"
						/>
					</form>
					
					<form action="users?action=deleteSessions" method="post">
						<input 
							type="hidden"
							name="id"
							value="${user.id}"
						/>
						<input 
							class="block action-button"
							type="submit"
							value="Clear sessions"
						/>
					</form>
				</fieldset>
			</div>
			<div class="statistics float-left">
				<fieldset class="fieldset">
					<legend>Statistics</legend>
					<table>
						<tr>
							<td>
								Mean Keypress Time, ms
							</td>
							<td>
								<fmt:formatNumber type="number" maxFractionDigits="2" value="${user.meanKeypressTime}" />
							</td>
						</tr>
						<tr>
							<td>
								Mean Time Between Keypresses, ms
							</td>
							<td>
								<fmt:formatNumber type="number" maxFractionDigits="2" value="${user.meanTimeBetweenKeys}" />
							</td>
						</tr>
					</table>
				</fieldset>
			</div>
			<div style="clear:both;"></div>
<% } %>
		
	</div>
	
	<div id="footer" class="footer">
		Keystroke Dynamics<br>Serhii Beliablia, MP-111
	</div>

</body>
</html>