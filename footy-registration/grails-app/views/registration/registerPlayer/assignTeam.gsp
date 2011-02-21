<%@ page import="org.davisononline.footy.core.*" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <g:set var="entityName" value="${message(code: 'club.label', default: 'Club')}" />
        <title><g:message code="default.paymentdetails.label" default="Assign Team" /></title>
    </head>
    <body>
        <div class="dialog">
            <g:form action="registerPlayer"> 
                <p>
                    If the player is already assigned to a team and has a 
                    league registration number, you can enter those here.
                </p>
        
                <div class="buttons">
                    <span class="button"><g:submitButton name="submit" class="save" value="${message(code: 'default.button.continue.label', default: 'Continue')}" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
        