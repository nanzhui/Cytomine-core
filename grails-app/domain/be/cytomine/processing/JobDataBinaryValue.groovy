package be.cytomine.processing

import be.cytomine.CytomineDomain

class JobDataBinaryValue extends CytomineDomain {
    //jobdata and JobDataBinaryValue are split because we need lazy-loading for data load!

    byte[] data

    static belongsTo = [jobData: JobData]

    static constraints = {
        data(nullable: true)
    }
    static mapping = {
        id generator: "assigned"
    }

    public beforeValidate() {
        println "beforeValidate"
        super.beforeValidate()
        println "this.id="+this.id
    }
}
