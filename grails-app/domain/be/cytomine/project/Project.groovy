package be.cytomine.project

import grails.converters.JSON
import be.cytomine.SequenceDomain
import be.cytomine.rest.UrlApi
import be.cytomine.ontology.Ontology
import be.cytomine.image.ImageInstance

import be.cytomine.ontology.Annotation
import org.perf4j.StopWatch
import org.perf4j.LoggingStopWatch
import be.cytomine.image.AbstractImage
import be.cytomine.security.UserGroup

class Project extends SequenceDomain {

    String name
    Ontology ontology
    Long countAnnotations
    Long countImages

    static hasMany = [projectGroup:ProjectGroup]

    static constraints = {
        name ( maxSize : 100, unique : true, blank : false)
        countAnnotations(nullable:true)
        countImages(nullable:true)
    }

    String toString() {
        name
    }

    def imagesinstance() {
        ImageInstance.findAllByProject(this)
    }

    def countImageInstance() {
      countImages
        //ImageInstance.countByProject(this)
    }

    def abstractimages() {
        ImageInstance.createCriteria().list {
            eq("project", this)
            projections {
                groupProperty("baseImage")
            }
        }
    }

    def annotations() {
        Annotation.findAllByImageInList(this.imagesinstance())
    }

    def countAnnotations() {
      countAnnotations
        //def images = this.imagesinstance()
        //images.size() > 0 ? Annotation.countByImageInList(images) : 0
    }

    def slides() {
        ImageInstance.createCriteria().list {
            join 'slide'
            projections {
                groupProperty('slide')
            }
            eq("project", this)
        }
    }

    def countSlides () {
        def query = ImageInstance.createCriteria().list {
            join 'slide'
            projections {
                countDistinct('slide.id')
            }
            eq("project", this)
        }
        query[0]
    }

    def groups() {
        projectGroup.collect{ it.group }
    }

    def users() {
        UserGroup.findAllByGroupInList(this.groups()).collect { it.user }
    }

    static Project createFromData(jsonProject) {
        def project = new Project()
        getFromData(project,jsonProject)
    }

    static Project getFromData(project,jsonProject) {
        String name = jsonProject.name.toString()
        /*println "jsonProject.name=" + jsonProject.name
 println "jsonProject.name==null" + (jsonProject.name==null)
 println "jsonProject.name.type" + jsonProject.name.class
 println "jsonProject.name.equals(null)" + jsonProject.name.equals("null")
 println "jsonProject.name.toString().equals(null)" + jsonProject.name.toString().equals("null")
 println "isNull(String key) " + jsonProject.isNull(jsonProject.name)
 println "isNull(String key) " + jsonProject.isNull(name)
 println "isNull(String key) " + jsonProject.isNull("name")  */
        if(!name.equals("null"))
            project.name = jsonProject.name.toUpperCase()
        else throw new IllegalArgumentException("Project name cannot be null")
        if (jsonProject.ontology)
            project.ontology = Ontology.read(jsonProject.ontology)
        return project;
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + Project.class
        JSON.registerObjectMarshaller(Project) {
            StopWatch stopWatch = new LoggingStopWatch();
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['name'] = it.name
            returnArray['ontology'] = it.ontology? it.ontology.id : null
            returnArray['ontologyName'] = it.ontology? it.ontology.name : null
            returnArray['ontologyURL'] = UrlApi.getOntologyURLWithOntologyId(it.ontology?.id)
            returnArray['abstractimageURL'] = UrlApi.getAbstractImageURLWithProjectId(it.id)
            returnArray['imageinstanceURL'] = UrlApi.getImageInstanceURLWithProjectId(it.id)
            returnArray['termURL'] = UrlApi.getTermsURLWithOntologyId(it.ontology?.id)
            returnArray['userURL'] = UrlApi.getUsersURLWithProjectId(it.id)



            StopWatch stopWatchUsers = new LoggingStopWatch();
            returnArray['users'] = it.users().collect { it.id }
            stopWatchUsers.stop("Project.registerMarshaller.users");
            StopWatch stopWatchSlides = new LoggingStopWatch();
            try {returnArray['numberOfSlides'] = it.countSlides()}catch(Exception e){returnArray['numberOfSlides']=-1}
            stopWatchSlides.stop("Project.registerMarshaller.slides");
            StopWatch stopWatchImages = new LoggingStopWatch();
            try {returnArray['numberOfImages'] = it.countImageInstance()}catch(Exception e){returnArray['numberOfImages']=-1}
            stopWatchImages.stop("Project.registerMarshaller.images");
            StopWatch stopWatchAnnotations = new LoggingStopWatch();
            try {returnArray['numberOfAnnotations'] = it.countAnnotations()}catch(Exception e){e.printStackTrace();returnArray['numberOfAnnotations']=-1}
            //try {returnArray['numberOfAnnotations'] = it.countAnnotations()}catch(Exception e){e.printStackTrace();returnArray['numberOfAnnotations']=-1}
            stopWatchAnnotations.stop("Project.registerMarshaller.annotations");
            returnArray['created'] = it.created? it.created.time.toString() : null
            returnArray['updated'] = it.updated? it.updated.time.toString() : null
            stopWatch.stop("Project.registerMarshaller");
            return returnArray
        }
    }
}
