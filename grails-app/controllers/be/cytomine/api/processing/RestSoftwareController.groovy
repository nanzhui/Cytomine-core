package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.Software
import grails.plugins.springsecurity.Secured
import grails.converters.JSON

class RestSoftwareController extends RestController {

    def softwareService

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def list = {
        responseSuccess(softwareService.list())
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def show = {
        Software software = softwareService.read(params.long('id'))
        if (software) responseSuccess(software)
        else responseNotFound("Software", params.id)
    }

    def add = {
        add(softwareService, request.JSON)
    }

    def update = {
        log.info "update software controller"
        update(softwareService, request.JSON)
    }

    def delete = {
        delete(softwareService, JSON.parse("{id : $params.id}"))
    }
}
