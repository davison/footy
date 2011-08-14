package org.davisononline.footy.core

/**
 * figures out the best home page to display.  UrlMappings should set this
 * controller against the '/' uri
 */
class HomeController {

    static final String FOOTY_CMS = "footy-cms"


    def pluginManager

    def springSecurityService

    /**
     * default action
     */
    def index = {
        if (springSecurityService.loggedIn) {
            def user = SecUser.findByUsername(springSecurityService.authentication.name)
            def person = user ? Person.findByUser(user) : null
            def teams = []
            if (person) {
                teams = Team.findAllByManager(person)
                //teams << Team.findByCoachesInList([person])
            }
            render view: '/admin', model: [person:person, teams:teams]
            return
        }
        
        if (pluginManager.hasGrailsPlugin(FOOTY_CMS))
            redirect uri: '/content/index'

        render view: '/index'
    }
}
