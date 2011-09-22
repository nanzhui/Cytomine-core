package be.cytomine.api

import be.cytomine.project.Project
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.Term

class StatsController extends RestController {

     def statUserAnnotations = {
        Project project = Project.read(params.id)
        if(project == null) responseNotFound("Project", params.id)
        def terms = Term.findAllByOntology(project.getOntology())
        def nbAnnotationsByUserAndTerms = AnnotationTerm.createCriteria().list {
            inList("term", terms)
            join("annotation")
            createAlias("annotation", "a")
            projections {
                groupProperty("a.user.id")
                groupProperty("term.id")
                count("term")
            }
        }

        Map<Long, Object> result = new HashMap<Long, Object>()
        project.users().each { user->
            def item = [:]
            item.id = user.id
            item.key = user.firstname + " " + user.lastname
            item.terms = []
            terms.each { term ->
                def t = [:]
                t.id = term.id
                t.name = term.name
                t.color = term.color
                t.value = 0
                item.terms << t
            }
            result.put(user.id, item)
        }
        nbAnnotationsByUserAndTerms.each { stat->
            def item = result.get(stat[0])
            item.terms.each {
                if (it.id == stat[1]) {
                    it.value = stat[2]
                }
            }
        }
        responseSuccess(result.values())
    }

    def statUser = {
        Project project = Project.read(params.id)
        if(project==null) { responseNotFound("Project", params.id) }
        def userAnnotations = Annotation.createCriteria().list {
            inList("image", project.imagesinstance())
            join("user")  //right join possible ? it will be sufficient
            projections {
                countDistinct('id')
                groupProperty("user.id")
            }
        }
        Map<Long, Object> result = new HashMap<Long, Object>()
        project.users().each { user->
            def item = [:]
            item.id = user.id
            item.key = user.firstname + " " + user.lastname
            item.value = 0
            result.put(item.id, item)
        }
        userAnnotations.each { item ->
            result.get(item[1]).value = item[0]
        }

        responseSuccess(result.values())
    }

    def statTerm = {
        Project project = Project.read(params.id)
        if(project == null) responseNotFound("Project", params.id)


        def terms = project.ontology.terms()
        def annotations = project.annotations()
        def stats = [:]
        def color = [:]
        def list = []

        //init list
        terms.each{ term ->
            if(!term.hasChildren()) {
                stats[term.name] = 0
                color[term.name] = term.color
            }
        }

        //compute stat
        annotations.each{ annotation ->
            def termOfAnnotation = annotation.terms()
            termOfAnnotation.each{ term ->
                if(term.ontology.id==project.ontology.id && !term.hasChildren())
                    stats[term.name] = stats[term.name]+1
            }
        }
        stats.each{
            println "Item: $it"
            list << ["key":it.key,"value":it.value,"color":color.get(it.key)]
        }
        responseSuccess(list)
    }
}