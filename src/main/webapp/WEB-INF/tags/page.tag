<%@ tag description="Generic Page" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@ attribute name="pageTitle" required="true" %>
<%@ attribute name="navBar" required="true" fragment="true" %>
<%@ attribute name="bodyFooter" required="true" fragment="true" %>
<%@ attribute name="jsIncludes" fragment="true" %>
<!DOCTYPE html>
<html>
<head>
    <title>${pageTitle}</title>

    <!--  <link rel="shortcut icon" href="/favicon.png" /> -->

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous" />
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous" />
    <link type="text/css" href="/styles.css" rel="stylesheet"/>

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.0/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
    <script src="/js/common.js"></script>

    <jsp:invoke fragment="jsIncludes" />
</head>
<body>
    <div class="modal hide" id="pleaseWaitDialog" data-backdrop="static" data-keyboard="false">
        <div class="modal-header">
            <h1>Processing...</h1>
        </div>
        <div class="modal-body">
            <div class="progress progress-striped active">
                <div class="bar" style="width: 100%;"></div>
            </div>
        </div>
    </div>
    <jsp:invoke fragment="navBar" />
    <div class="container" id="mainContent">
        <jsp:doBody />
    </div>
    <jsp:invoke fragment="bodyFooter" />
</body>
</html>