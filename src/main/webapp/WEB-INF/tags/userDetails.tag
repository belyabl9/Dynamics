<%@ tag description="User Details" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ attribute name="user" type="com.m1namoto.domain.User" %>

<div class="form-group row">
	<label for="test" class="control-label col-sm-3">User Type</label>
	<div class="col-sm-9">
		<c:choose>
			<c:when test="${empty user}">
				<select id="test" name="userType" class="form-control">
					<option ${user.userType eq 'ADMIN'   ? 'selected' : '' } value="0">Admin</option>
					<option ${user.userType eq 'REGULAR' ? 'selected' : '' } value="1">Regular User</option>
				</select>
			</c:when>
			<c:otherwise>
				${user.userType eq 'ADMIN' ? 'Admin' : 'Regular user'}
			</c:otherwise>
		</c:choose>
	</div>
</div>
<div class="form-group row">
	<label class="col-form-label col-sm-3">Name</label>
	<div class="col-sm-9">
		<input type="text" class="form-control" name="firstName" placeholder="Enter the name" value="${user.firstName}" required />
	</div>
</div>
<div class="form-group row">
	<label class="col-form-label col-sm-3">Surname</label>
	<div class="col-sm-9">
		<input type="text" class="form-control" name="surname" placeholder="Enter the surname" value="${user.surname}" required />
	</div>
</div>
<div class="form-group row">
	<label class="col-form-label col-sm-3">Login</label>
	<div class="col-sm-9">
		<input type="text" class="form-control" name="login" placeholder="Enter the login" value="${user.login}" required />
	</div>
</div>
<c:if test="${empty user}">
	<div class="form-group row">
		<label class="col-form-label col-sm-3">Password</label>
		<div class="col-sm-9">
			<input type="password" class="form-control" name="password" placeholder="Enter the password" value="${user.password}" required />
		</div>
	</div>
</c:if>

