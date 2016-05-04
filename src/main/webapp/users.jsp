<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="styles.css">
<title>Users</title>
</head>
<body>

	<script type="text/javascript">
		function goToUser(userId) {
			window.location = 'users?id=' + userId;
		}
	</script>

	<div class="header">
		<h1>Users</h1>
		<div style="text-align: left; margin: 5px 0px 20px 15px; float: left;">
			<form action="/Dynamics">
				<input type="submit" value="Main Page" />
			</form>
		</div>
		<div style="text-align: left; margin: 5px 0px 20px 15px; float: left;">
			<form action="users">
				<input type="hidden" name="id" value="New" />
				<input type="submit" value="Create a new user" />
			</form>
		</div>
	</div>

	<div class="content">
		<table style="border: 1px solid black;">
			<tr>
				<th>#</th>
				<th>Name</th>
				<th>Login</th>
				<th>Password</th>
				<th>Sessions Count</th>
			</tr>
			
			<c:forEach items="${users}" var="user" varStatus="loop">
			   <tr class="sel-row" onclick="goToUser(${user.id});">
			   	   <td>
			   	       ${loop.index + 1}
			   	   </td>
			   	   <td>
				   	   ${user.name}
				   </td>
				   <td>
				       ${user.login}
				   </td>
				   <td>
				       ${user.password}
				   </td>
				   <td class="rightAlign">
				       ${user.sessionsCount}
				   </td>
			   </tr>
			</c:forEach>
		</table>

	</div>
	
	<div id="footer" class="footer">
		Keystroke Dynamics<br>Serhii Beliablia, MP-111
	</div>

</body>
</html>