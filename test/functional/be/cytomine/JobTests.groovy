package be.cytomine

import be.cytomine.processing.Job
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.JobAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.processing.SoftwareProject
import be.cytomine.security.UserJob
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.processing.JobData
import be.cytomine.processing.JobDataBinaryValue

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class JobTests extends functionaltestplugin.FunctionalTestCase {

    void testListJobWithCredential() {
        def result = JobAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListJobBySoftwareWithCredential() {
        Job job = BasicInstance.createOrGetBasicJob()
        def result = JobAPI.listBySoftware(job.software.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListJobBySoftwareAndProjectWithCredential() {
        Job job = BasicInstance.createOrGetBasicJob()
        def result = JobAPI.listBySoftwareAndProject(job.software.id,job.project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD,false)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListJobBySoftwareAndProjectWithCredentialLight() {
        Job job = BasicInstance.createOrGetBasicJob()
        def result = JobAPI.listBySoftwareAndProject(job.software.id,job.project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD,true)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testShowJobWithCredential() {
        def result = JobAPI.show(BasicInstance.createOrGetBasicJob().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testAddJobCorrect() {
        def jobToAdd = BasicInstance.getBasicJobNotExist()
        def result = JobAPI.create(jobToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        int idJob = result.data.id
  
        result = JobAPI.show(idJob, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testAddJobWithBadSoftware() {
        Job jobToAdd = BasicInstance.createOrGetBasicJob()
        Job jobToEdit = Job.get(jobToAdd.id)
        def jsonJob = jobToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJob)
        jsonUpdate.software = -99
        jsonJob = jsonUpdate.encodeAsJSON()
        def result = JobAPI.update(jobToAdd.id, jsonJob, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testUpdateJobCorrect() {
        Job jobToAdd = BasicInstance.createOrGetBasicJob()
        def result = JobAPI.update(jobToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testUpdateJobNotExist() {
        Job jobWithNewName = BasicInstance.getBasicJobNotExist()
        jobWithNewName.save(flush: true)
        Job jobToEdit = Job.get(jobWithNewName.id)
        def jsonJob = jobToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJob)
        jsonUpdate.id = -99
        jsonJob = jsonUpdate.encodeAsJSON()
        def result = JobAPI.update(-99, jsonJob, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testUpdateJobWithBadSoftware() {
        Job jobToAdd = BasicInstance.createOrGetBasicJob()
        Job jobToEdit = Job.get(jobToAdd.id)
        def jsonJob = jobToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJob)
        jsonUpdate.software = -99
        jsonJob = jsonUpdate.encodeAsJSON()
        def result = JobAPI.update(jobToAdd.id, jsonJob, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testDeleteJob() {
        def jobToDelete = BasicInstance.getBasicJobNotExist()
        assert jobToDelete.save(flush: true)!= null
        def id = jobToDelete.id
        def result = JobAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        def showResult = JobAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, showResult.code)
    }

    void testDeleteJobNotExist() {
        def result = JobAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testDeleteAllJobDataJobNotExist() {
        def result = JobAPI.deleteAllJobData(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testDeleteAllJobData() {
        //create a job
        Job job = BasicInstance.getBasicJobNotExist()
        BasicInstance.checkDomain(job)
        BasicInstance.saveDomain(job)
        BasicInstance.createSoftwareProject(job.software,job.project)

        UserJob userJob = BasicInstance.getBasicUserJobNotExist()
        userJob.job = job
        userJob.user = BasicInstance.getNewUser()
        BasicInstance.checkDomain(userJob)
        BasicInstance.saveDomain(userJob)

        //add algo-annotation for this job
        AlgoAnnotation a1 = BasicInstance.getBasicAlgoAnnotationNotExist()
        a1.project = job.project
        a1.user = userJob
        BasicInstance.checkDomain(a1)
        BasicInstance.saveDomain(a1)

        //add algo-annotation-term for this job
        AlgoAnnotationTerm at1 = BasicInstance.getBasicAlgoAnnotationTermNotExist()
        at1.project = job.project
        at1.annotationIdent = a1.id
        at1.annotationClassName = a1.class.getName()
        at1.userJob = userJob
        BasicInstance.checkDomain(at1)
        BasicInstance.saveDomain(at1)

        //add job data
        JobData data1 = BasicInstance.getBasicJobDataNotExist()
        data1.job = job
        BasicInstance.checkDomain(data1)
        BasicInstance.saveDomain(data1)

//        JobDataBinaryValue dataValue1 = new JobDataBinaryValue(data: "toto".bytes,jobData:data1)
//        BasicInstance.checkDomain(dataValue1)
//        BasicInstance.saveDomain(dataValue1)

        Infos.addUserRight(userJob.user,job.project)

        //count data = 1-1
        assert AlgoAnnotationTerm.findAllByUserJobInList(UserJob.findAllByJob(job)).size() == 1
        assert AlgoAnnotation.findAllByUserInList(UserJob.findAllByJob(job)).size() == 1
        assert JobData.findAllByJob(job).size() == 1
//        assert JobDataBinaryValue.findAllByJobDataInList(JobData.findAllByJob(job)).size() == 1

        //delete all job data
        def result = JobAPI.deleteAllJobData(job.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        //count data = 0-0
        assert AlgoAnnotationTerm.findAllByUserJobInList(UserJob.findAllByJob(job)).size() == 0
        assert AlgoAnnotation.findAllByUserInList(UserJob.findAllByJob(job)).size() == 0
        assert JobData.findAllByJob(job).size() == 0
 //       assert JobDataBinaryValue.findAllByJobDataInList(JobData.findAllByJob(job)).size() == 0
    }

    void testDeleteAllJobDataWithReviewedAnnotations() {
        //create a job
        Job job = BasicInstance.getBasicJobNotExist()
        BasicInstance.checkDomain(job)
        BasicInstance.saveDomain(job)
        BasicInstance.createSoftwareProject(job.software,job.project)

        UserJob userJob = BasicInstance.getBasicUserJobNotExist()
        userJob.job = job
        userJob.user = BasicInstance.getNewUser()
        BasicInstance.checkDomain(userJob)
        BasicInstance.saveDomain(userJob)

        //add algo-annotation for this job
        AlgoAnnotation a1 = BasicInstance.getBasicAlgoAnnotationNotExist()
        a1.project = job.project
        a1.user = userJob
        BasicInstance.checkDomain(a1)
        BasicInstance.saveDomain(a1)

        //add algo-annotation-term for this job
        AlgoAnnotationTerm at1 = BasicInstance.getBasicAlgoAnnotationTermNotExist()
        at1.project = job.project
        at1.annotationIdent = a1.id
        at1.annotationClassName = a1.class.getName()
        at1.userJob = userJob
        BasicInstance.checkDomain(at1)
        BasicInstance.saveDomain(at1)

        Infos.addUserRight(userJob.user,job.project)

        //add reviewed annotation
        ReviewedAnnotation reviewed = BasicInstance.getBasicReviewedAnnotationNotExist()
        reviewed.project = job.project
        reviewed.parentIdent = a1.id
        reviewed.parentClassName = a1.class.getName()
        BasicInstance.checkDomain(reviewed)
        BasicInstance.saveDomain(reviewed)

        //count data = 1-1
        assert AlgoAnnotationTerm.findAllByUserJobInList(UserJob.findAllByJob(job)).size() == 1
        assert AlgoAnnotation.findAllByUserInList(UserJob.findAllByJob(job)).size() == 1

        //delete all job data
        def result = JobAPI.deleteAllJobData(job.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }


}
