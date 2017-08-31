<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<t:page pageTitle="Settings">
    <jsp:attribute name="navBar">
        <t:navBar activeMenuItem="settings"></t:navBar>
    </jsp:attribute>
    <jsp:attribute name="bodyFooter">
        <t:footer></t:footer>
    </jsp:attribute>
    <jsp:attribute name="jsIncludes">
        <script src="/js/Settings.js"></script>
    </jsp:attribute>
    <jsp:body>

        <script type="text/javascript">
            $(document).ready(function() {
                Settings.init();
            });

        </script>

        <div id="statusMessage"></div>
        <div class="panel panel-primary" style="width: 35%;">
            <div class="panel-heading">
              <h3>Settings</h3>
            </div>

            <div class="panel-body" style="display: flex;">

                <form id="settingsForm" style="width: 100%;">
                    <div class="form-group" style="display: flex;">
                        <label>Save requests</label>
                        <input type="hidden" id="save_requests" name="save_requests" value="false" />
                        <input type="checkbox" id="save_requests_inp" style="margin-left: auto;"
                            ${data.settings.save_requests eq 'true' ? 'checked' : '' }>
                        </input>
                    </div>
                    <div class="form-group" style="display: flex;">
                        <label>Update biometric template</label>
                        <input type="hidden" id="update_template" name="update_template" value="true" />
                        <input type="checkbox" id="update_template_inp" style="margin-left: auto;"
                            ${data.settings.update_template eq 'true' ? 'checked' : '' }>
                        </input>
                    </div>
                    <div class="form-group" style="display: flex;">
                        <label>Similarity threshold, [0-1]</label>
                        <input type="number" id="threshold" name="threshold"
                            value="${data.settings.threshold}"
                            required step="0.01" min="0" max="1"
                            style="margin-left: auto;">
                        </input>
                    </div>
                    <div class="form-group" style="display: flex;">
                      <label>Number of records in the template</label>
                      <input type="number" id="learningRate" name="learning_rate"
                          value="${data.settings.learning_rate}"
                          required min="1" max="100"
                          style="margin-left: auto;">
                      </input>
                    </div>

                    <button class="btn btn-default pull-right">
                        Save
                    </button>
                </form>
            </div>
        </div>

    </jsp:body>
</t:page>