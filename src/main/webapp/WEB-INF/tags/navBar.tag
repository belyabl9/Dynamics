<%@ tag description="Navigation bar component" %>

<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ attribute name="activeMenuItem" %>
<%@ attribute name="browserTraining" %>

<nav class="navbar navbar-inverse navbar-fixed-top">
    <div class="container">
        <div class="navbar-header">
            <c:choose>
                <c:when test="${activeMenuItem eq 'home'}">
                    <span class="navbar-brand">Keystroke Dynamics</span>    
                </c:when>
                <c:otherwise>
                    <a href="/" class="navbar-brand">Keystroke Dynamics</a>
                </c:otherwise>
            </c:choose>
        </div>
        <c:if test="${browserTraining ne 'true'}">
            <div id="navbar" class="collapse navbar-collapse">
                <ul class="nav navbar-nav">
                    <li class="${activeMenuItem eq 'users' ? "active" : ""}">
                        <a href="/page/users">Users</a>
                    </li>
                    <li class="${activeMenuItem eq 'stat' ? "active" : ""}">
                        <a href="/page/stat">Statistics</a>
                    </li>
                    <li class="${activeMenuItem eq 'eval' ? "active" : ""}">
                        <a href="/page/eval">Evaluation</a>
                    </li>
                </ul>
                <ul class="nav navbar-nav navbar-right">
                    <li class="${activeMenuItem eq 'settings' ? "active" : ""}">
                        <a href="/page/settings">Settings</a>
                    </li>
                    <li>
                        <a href="/logout">Sign Out</a>
                    </li>
                </ul>
            </div>
        </c:if>
    </div>
</nav>