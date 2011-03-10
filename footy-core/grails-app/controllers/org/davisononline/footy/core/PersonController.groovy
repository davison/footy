package org.davisononline.footy.core

/**
 * admin controller for Person and Player operations
 */
//@Seured([CLUB_ADMIN])
class PersonController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def listperson = {
        params.max = Math.min(params.max ? params.int('max') : 25, 100)
        if (!params.sort) params.sort = 'familyName'
        def l = Person.findAllEligibleParent(params)
        [personInstanceList: l, personInstanceTotal: l.size()]
    }

    def listplayer = {
        params.max = Math.min(params.max ? params.int('max') : 25, 100)
        def l = Player.list(params)
        [playerInstanceList: l, playerInstanceTotal: l.size()]
    }

    def create = {
        def personInstance = new Person()
        personInstance.properties = params
        render(view: 'edit', model:[personInstance: personInstance])
    }

    def save = { PersonCommand cmd ->
        def p = cmd.toPerson()
        if (!p.validate()) {
            cmd.errors = p.errors
            render(view: "edit", model: [personCommand: p])
        }
        else {
            p.save(flush: true)
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'person.label', default: 'Person'), p])}"
            redirect(action: "listperson")
        }
    }

    def edit = {
        def personInstance = Person.get(params.id)
        if (!personInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [personCommand: personInstance]
        }
    }

    def editplayer = {
        // TODO: implement editplayer
        render "editplayer NOT IMPLEMENTED YET"
    }

    def update = { PersonCommand cmd ->
        def personInstance = Person.get(params.id)
        if (personInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (personInstance.version > version) {
                    
                    personInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'person.label', default: 'Person')] as Object[], "Another user has updated this Person while you were editing")
                    render(view: "edit", model: [personInstance: personInstance])
                    return
                }
            }
            
            def submitted = cmd.toPerson()
            personInstance.properties = submitted.properties
            if (!personInstance.hasErrors() && personInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'person.label', default: ''), personInstance])}"
                redirect(action: "listperson")
            }
            else {
                render(view: "edit", model: [personCommand: personInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), params.id])}"
            redirect(action: "listperson")
        }
    }

    def delete = {
        def personInstance = Person.get(params.id)
        if (personInstance) {
            try {
                personInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'person.label', default: 'Person'), params.id])}"
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'person.label', default: 'Person'), params.id])}"
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), params.id])}"
        }
        redirect(action: "listperson")
    }
}