package org.davisononline.footy.tournament

/**
 * @author darren
 */
class TournamentController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    /**
     * redir to list view
     */
    def index = {
        redirect(action: "list", params: params)
    }

    /**
     * sorts by start date, oldest last
     */
    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        if (!params.sort) {
            params.sort = "startDate"
            params.order = "desc"
        } 
        [tournamentInstanceList: Tournament.list(params), tournamentInstanceTotal: Tournament.count()]
    }

    /**
     * create
     */
    def create = {
        def tournamentInstance = new Tournament()
        tournamentInstance.properties = params
        render(view: "edit", model: [tournamentInstance: tournamentInstance])
    }

    /**
     * save
     */
    def save = {
        def tournamentInstance = new Tournament(params)
        if (tournamentInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'tournament.label', default: 'Tournament'), tournamentInstance.name])}"
            redirect(action: "list")
        }
        else {
            render(view: "edit", model: [tournamentInstance: tournamentInstance])
        }
    }
    
    /**
     * display entries in order to see payment status and which teams
     * were registered together
     */
    def entryList = {
        def t = checkInstance(params)
        [entries: t.entries]
    }

    /**
     * show tournament details and includes a teamList sorted by age band
     * gathered from all the entries
     */
    def show = {
        def t = checkInstance(params)
        def teamList = t.teamsEntered().sort { a,b-> 
            a.ageBand == b.ageBand? 0 : a.ageBand < b.ageBand ? -1 : 1 
        }
        render (view: 'show', model: [tournamentInstance: t, teamList: teamList])
    }

    /**
     * edit details
     */
    def edit = {
        def t = checkInstance(params)
        // can't edit if entries already made
        if (t?.entries.size() > 0) {
            flash.message = "${message(code: 'org.davisononline.footy.tournament.entriesexist', default: 'Entries already exist - cannot edit tournament')}"
            redirect(action: "list", params: params)
        }
        else
            render (view: 'edit', model:[tournamentInstance: t])
    }
    
    /**
     * update details
     */
    def update = {
        def tournamentInstance = checkInstance(params)
        if (params.version) {
            def version = params.version.toLong()
            if (tournamentInstance.version > version) {
                
                tournamentInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'tournament.label', default: 'Tournament')] as Object[], "Another user has updated this Tournament while you were editing")
                render(view: "edit", model: [tournamentInstance: tournamentInstance])
                return
            }
        }
        tournamentInstance.properties = params
        if (!tournamentInstance.hasErrors() && tournamentInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.updated.message', args: [message(code: 'tournament.label', default: 'Tournament'), tournamentInstance.name])}"
            redirect(action: "list")
        }
        else {
            render(view: "edit", model: [tournamentInstance: tournamentInstance])
        }
    }

    /**
     * delete tournament
     */
    def delete = {
        def tournamentInstance = checkInstance(params)
        try {
            tournamentInstance.delete(flush: true)
            flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'tournament.label', default: 'Tournament'), tournamentInstance.name])}"
            redirect(action: "list")
        }
        catch (org.springframework.dao.DataIntegrityViolationException e) {
            flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'tournament.label', default: 'Tournament'), params.id])}"
            redirect(action: "show", id: params.id)
        }
    }

    private checkInstance(params) {
        def t = Tournament.get(params.id)
        if (!t) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'tournament.label', default: 'Tournament'), params.id])}"
            redirect(action: "list")
        }
        return t
    }
}
