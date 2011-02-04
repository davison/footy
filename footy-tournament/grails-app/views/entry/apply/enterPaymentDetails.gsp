<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title><g:message code="enter.payment.heading" default="Enter Payment" /></title>
    </head>

    <body>
        <p>
            You are entering the following team(s) into <strong>${entryInstance.tournament.name}</strong>
        </p>
        <ul>
            <g:each var="team" in="${entryInstance.teams}">
            <li>${team.club.name} ${team}</li>
            </g:each>
        </ul>
        <p>
            <g:set var="numTeams" value="${entryInstance.teams.size()}"/>
            You have <strong>${numTeams}</strong> team${numTeams > 1 ? 's' : ''} (@ <g:formatNumber number="${entryInstance.tournament.costPerTeam}" type="currency" currencyCode="GBP" /> each)  
            <strong>Total: <g:formatNumber number="${numTeams*entryInstance.tournament.costPerTeam}" type="currency" currencyCode="GBP" /></strong>
        </p>
        <table>
        <tbody>
            <tr class="prop">
            <td  class="name">
                <img src="${resource(dir:'images', file:'paypal_logo.gif')}" alt="PayPal"/>
            </td>
            <td  class="value image">
                <g:form controller="paypal" action="uploadCart">
                <input type="image" 
                    src="https://www.paypal.com/en_US/i/btn/btn_buynow_LG.gif"
                    alt="PayPal - The safer, easier way to pay online!"/>
                <img alt="" border=0" src="https://www.paypal.com/en_US/i/scr/pixel.gif" width="1" height="1"/>
                </g:form>
            </td>
            </tr>
        </tbody>
        </table>
        <p>
            You will receive email confirmation as soon as payment clears (which is
            normally immediate in the case of PayPal)
        </p>
    </body>
</html>

