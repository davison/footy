package org.davisononline.footy.registration

import org.davisononline.footy.core.*
import org.grails.paypal.Payment
import org.grails.paypal.PaymentItem
import org.codehaus.groovy.grails.commons.ConfigurationHolder

/**
 * flows for the redgistration and creation of players, parents and other
 * staff at the club.
 * 
 * @author darren
 */
class RegistrationController {
    
    def index = {
        redirect (action:'registerPlayer')
    }
    
    /**
     * main web flow for player registration 
     */
    def registerPlayerFlow = {

        /*
         * main form for player details, not including team
         */
        enterPlayerDetails {
            on("submit") { PlayerCommand playerCommand ->
                flow.playerCommand = playerCommand
                if (!playerCommand.validate())
                    return error()
            }.to "checkGuardianNeeded"
        }

        /*
         * if player age < [cutoff] we must have at least one
         * parent/guardian details too
         */
        checkGuardianNeeded {
            action {
                if (flow.playerCommand.age() < Person.MINOR_UNTIL && !flow.playerCommand.parentId)
                    return yes()
                else
                    return no()
            }

            on("yes") {
                flow.personCommand = new PersonCommand(
                    familyName: flow.playerCommand.familyName
                )
            }.to "enterGuardianDetails"

            // TODO: if no guardian required, must get player contact details instead
            on("no") {
                if (flow.playerCommand.parentId)
                    flow.guardian = Person.get(flow.playerCommand.parentId)
            }.to "assignTeam"
        }

        /*
         * add parent/guardian details and assign to player
         */
        enterGuardianDetails {
            on ("continue") { PersonCommand personCommand ->
                if (!handleGuardian(flow, personCommand))
                    return error()
                flow.personCommand = null
            }.to "assignTeam"

            on ("addanother") {PersonCommand personCommand ->
                if (!handleGuardian(flow, personCommand))
                    return error()
                flow.personCommand.email = ''
                flow.personCommand.givenName = ''
            }.to "enterGuardianDetails"
        }

        /*
         * select a team and enter league reg number if available
         */
        assignTeam {
            on ("continue") {
                // TODO: don't allow teams from other clubs created as part of tournament entries
                // TODO: only show teams the player is eligible for (ageband + 1)
                def team = Team.get(params.teamId)

                // create domain from flow objects
                def player = new Player (
                        dateOfBirth: flow.playerCommand.dob,
                        team: team,
                        dateJoinedClub: new Date(),
                        leagueRegistrationNumber: params.leagueRegistration ?: ''
                )
                player.person = new Person(
                        givenName: flow.playerCommand.givenName,
                        familyName: flow.playerCommand.familyName,
                        knownAsName: flow.playerCommand.knownAsName
                )

                if (!player.guardian)
                    player.guardian = flow.guardian1?.toPerson()
                else
                    player.guardian = flow.guardian

                player.secondGuardian = flow.guardian2?.toPerson()
                if (!player.save(flush: true))
                    log.error player.errors

                def payment = new Payment (
                    buyerId: player.guardian.id,
                    currency: Currency.getInstance("GBP")
                )
                payment.addToPaymentItems(
                    new PaymentItem (
                        itemName: "${player} Registration",
                        itemNumber: "${player.id}",
                        amount: ConfigurationHolder.config.org.davisononline.footy.registration.annualcost
                    )
                )
                payment.save(flush:true)

                [payment:payment, player:player]

            }.to "enterPaymentDetails"
        }

        enterPaymentDetails()
    }

    /*
     * manage 1 or 2 guardians
     */
    def handleGuardian(flow, personCommand) {
        flow.personCommand = personCommand
        if (!personCommand.validate())
            return false

        // first or second guardian?
        if (!flow.guardian1)
            flow.guardian1 = personCommand
        else
            flow.guardian2 = personCommand
    }

    /**
     * successful paypal payment made
     * @param params
     */
    def paypalSuccess = {
        def payment = Payment.findByTransactionId(params.transactionId)
        if(payment?.status == org.grails.paypal.Payment.COMPLETE) {
            render view: 'paypal/success'
        }
        else {
            flash.message = "Unable to find Entry for this transaction"
            render view: "/error"
        }
    }

    /**
     * cancelled transaction
     *
     * @param params
     * @return
     */
    def paypalCancel = {
        render view: "paypal/cancel"
    }
}

abstract class AbstractPersonCommand implements Serializable {
    String givenName
    String familyName

    static constraints = {
        givenName(nullable:false, blank:false, size:1..50)
        familyName(nullable:false, blank:false, size:1..50)
    }
}

class PlayerCommand extends AbstractPersonCommand {
    Date dob
    Long parentId
    String knownAsName

    static constraints = {
        dob(nullable: false)
        knownAsName(nullable:true, size:1..50)
    }

    /**
     * @return age at cutoff
     */
    int age() {
        // TODO: make cutoff date configurable
        def now = new Date()
        def c = 1900 + (now.month > 7 ? 1 : 0)
        def cutoff = new Date("${now.year+c}/08/31")
        Math.floor((cutoff-dob)/365.24)
    }
}

class PersonCommand extends AbstractPersonCommand {
    String email
    String phone1
    String phone2
    String address

    static constraints = {
        email(email:true, blank: false)
        phone1(blank: false)
        phone2(nullable: true)
    }

    /**
     *
     * @return a Person domain object (possibly invalid) from the command
     */
    def toPerson() {
        new Person(
                givenName: givenName,
                familyName: familyName,
                email: email,
                phone1: phone1,
                phone2: phone2,
                address: Address.parse(address)
                )
    }
}
