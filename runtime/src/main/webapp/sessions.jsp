<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<t:page pageTitle="Sessions">
    <jsp:attribute name="navBar">
        <t:navBar></t:navBar>
    </jsp:attribute>
    <jsp:attribute name="bodyFooter">
        <t:footer></t:footer>
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script src="/js/Sessions.js"></script>
    </jsp:attribute>
    <jsp:body>
    
        <div id="statusMessage"></div>
        <div class="panel panel-primary">
            <div class="panel-heading">
              <h3>User "${data.user.login}" -> Sessions</h3>
            </div>

            <div class="panel-body">
                 <c:forEach items="${data.sessions}" var="session" varStatus="loop">
                     <div class="panel panel-primary">
                         <div class="panel-heading clearfix">
                           <span class="pull-left">
                               <h4>Session ${loop.index + 1} - ${session.date}</h4>
                           </span>
                           <span class="pull-right">
                               <a href="#" onclick="Sessions.del(${data.user.id}, ${session.id})">
                                   <img src="/images/delete.png" />
                               </a>
                           </span>
                           
                         </div>
                         <div class="panel-body">
                             <ul>
                                 <c:forEach items="${session.features}" var="feature">
                                     <li style="height:20px;">${feature}</li>
                                 </c:forEach>
                             </ul>
                         </div>
                     </div>
                 </c:forEach>
            </div>
        </div>

    </jsp:body>
</t:page>