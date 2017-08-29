<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<t:page pageTitle="Users">
	<jsp:attribute name="navBar">
        <t:navBar activeMenuItem="users"></t:navBar>
    </jsp:attribute>
    <jsp:attribute name="bodyFooter">
	    <t:footer></t:footer>
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <link rel="stylesheet" type="text/css" href="//cdn.datatables.net/1.10.12/css/jquery.dataTables.css">

        <script src="/js/Users.js"></script>
        <script type="text/javascript" charset="utf8" src="//cdn.datatables.net/1.10.12/js/jquery.dataTables.js"></script>
    </jsp:attribute>
    <jsp:body>

        <script>
            $(document).ready( function () {
            	Users.initUsersList();
            });
        </script>
        
        <div id="statusMessage"></div>
		<div class="panel panel-primary">
            <div class="panel-heading clearfix">
              <h3 class="pull-left">Users</h3>
              <div class="pull-right">
			      <a href="/page/addUser" style="float:right;" class="btn btn-primary text-right">
                      Create a new user
                  </a>
              </div>
            </div>

            <div class="panel-body">

				<table id="usersTable" class="table table-bordered table-hover">
					<thead>
						<tr>
							<th data-toggle="tooltip" title="Admin (A) or regular user (U)">User Type</th>
							<th>Full Name</th>
							<th>Login</th>
							<th>Authentications</th>
                            <th>Actions</th>
						</tr>
					</thead>
				</table>
			</div>
        </div>

	</jsp:body>
</t:page>