<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<t:page pageTitle="Classifier evaluation">
    <jsp:attribute name="navBar">
        <t:navBar activeMenuItem="eval"></t:navBar>
    </jsp:attribute>
    <jsp:attribute name="bodyFooter">
        <t:footer></t:footer>
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script type="text/javascript" src="/js/Evaluation.js"></script>
    </jsp:attribute>
    
    <jsp:body>

    <div class="container">
        <div class="panel panel-primary col-md-9" style="padding:0;">
            <div class="panel-heading clearfix">
                <h3>Classifier evaluation</h3>
            </div>
            <div class="panel-body">
                <div class="form-group form-inline">
                   <div class="col-form-label">
                       <h4>String for evaluation</h4>
                   </div>
                   <div>
                       <input type="text" required class="form-control" id="password" name="password" /></input>
                       <button class="btn btn-primary" onclick="Evaluation.evaluate(); return false;">Evaluate</button>
                   </div>
                </div>
                <div class="form-group">
                   <div class="col-form-label">
                       <h4>Configuration</h4>
                   </div>
                   <div>
                       <textarea id="configuration" style="white-space:pre" readonly="readonly" class="form-control" rows="10" /></textarea>
                   </div>
                </div>
                <div class="form-group">
                   <div class="col-form-label">
                       <h4>Evaluation results</h4>
                   </div>
                   <div>
                       <textarea id="results" readonly="readonly" class="form-control" rows="10" /></textarea>
                   </div>
                   
                </div>
            </div>
        </div>
    </div>

    </jsp:body>

</t:page>
