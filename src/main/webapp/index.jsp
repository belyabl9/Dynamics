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
<title>Keystroke Dynamics</title>
</head>
<body>

	<script type="text/javascript">

	</script>

	<div class="header">
		<h1>Main Page</h1>
	</div>

	<div class="content">
		<form action="users">
			<div>
				<input class="action-button" type="submit" value="Users" />
			</div>		
		</form>
		
		<form action="stat">
			<div>
				<input class="action-button" type="submit" value="Statistics" />
			</div>
		</form>
	</div>
	
	<div id="footer" class="footer">
		Keystroke Dynamics<br>Serhii Beliablia, MP-111
	</div>

</body>
</html>