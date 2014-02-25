/**
 * Created with IntelliJ IDEA.
 * User: lrollus
 * Date: 20/02/14
 * Time: 16:52
 * To change this template use File | Settings | File Templates.
 */

var JobTemplatePanel = SideBarPanel.extend({
    tagName: "div",
    currentAnnotation : null,
    currentInterval : null,
    /**
     * ExplorerTabs constructor
     * @param options
     */
    initialize: function (options) {
        this.browseImageView = options.browseImageView;
    },
    /**
     * Grab the layout and call ask for render
     */
    render: function () {
        var self = this;
        require([
            "text!application/templates/explorer/JobTemplatePanel.tpl.html"
        ], function (tpl) {
            self.doLayout(tpl);
        });
        return this;
    },
    refresh: function() {
        alert("refresh");
        this.doLayout();
    },
    changeAnnotation : function(idAnnotation) {
        var self = this;
        self.currentAnnotation = idAnnotation;
        var panel = $('#jobTemplatePanel' + self.model.get('id'));
        panel.find(".jobTemplateInfo").empty();
        panel.find(".jobTemplateInfo").append('<img src="'+window.location.origin+'/api/annotation/'+idAnnotation+ '/crop.png?max_size=128&draw=true" /><br/>');
        panel.find(".jobTemplateInfo").append("Annotation " + idAnnotation + "<br/>");

        panel.find(".jobTemplateInfo").css("border-color","#47a447");

    },
    linkTemplateToAnntation : function() {
        var self = this;

        var jobTemplate = $('input[name=groupJobTemplate'+self.model.id+']:checked').val();

        new JobTemplateAnnotationModel({annotationIdent: self.currentAnnotation, jobTemplate:jobTemplate}).save({}, {
                success: function (model, response) {

                    var job = new JobModel({ id : model.get('job').id});
                    $.post(job.executeUrl())
                        .done(function() {
                            window.app.view.message("Job", "Job running!", "success");
                            self.printJobStatus( model.get('job').id);
                        })
                        .fail(function() { console.log("error"); })
                        .always(function() { console.log("finished"); });
                },
                error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Job", json.errors, "error");
                }
            }
        );
    },
    printJobStatus : function(idJob) {
        var self = this;
        var panel = $('#jobTemplatePanel' + self.model.get('id'));
        var algoView = new ProjectDashboardAlgos({model:{id:-1}});
        console.log("printJobStatus");
        var refreshData = function () {
            var selectRunElem = panel.find(".jobTemplateStatus");
            new JobModel({ id: idJob}).fetch({
                success: function (model, response) {
                    selectRunElem.empty();
                    var item = algoView.getStatusElement(model,100);
                    selectRunElem.append(item);
                    if(model.get('status')=="3" && self.currentInterval!=null) {
                        panel.find(".jobTemplateStatus").css("border-color","#47a447");
                        clearInterval(self.currentInterval);
                        self.currentInterval = null
                    }

                }
            });
        };
        refreshData();
        if(self.currentInterval!=null) {
            clearInterval(self.currentInterval);
        }
        self.currentInterval = setInterval(refreshData, 2000);
        $(window).bind('hashchange', function () {
            clearInterval(self.currentInterval);
        });



    },

    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout: function (tpl) {
        var self = this;
        var panel = $('#jobTemplatePanel' + self.model.get('id'));
        var content =_.template(tpl, {});
        panel.html(content);
        var elContent1 = panel.find(".JobTemplateContent1");
        var sourceEvent1 = panel.find(".toggle-content1");
        this.initToggle(panel, elContent1, sourceEvent1, "JobTemplateContent1");
        var list = panel.find(".jobTemplateList");
        list.empty();

        new JobTemplateCollection({project: self.model.get('project')}).fetch({
            success: function (collection, response) {

                if(collection.size()==0) {
                    panel.hide();
                } else {
                    //get all project id/name
                    var softwares = {};
                    collection.each(function(jobTemplate) {
                        softwares[jobTemplate.get('software')]=jobTemplate.get('softwareName');
                    });

                    //create div for each software
                    for (var prop in softwares) {
                        if (softwares.hasOwnProperty(prop)) {
                            list.append('<ul style="padding:10px;" class="'+prop+'">'+softwares[prop]+'</ul>');
                        }
                    }

                    //during each, add the template under the good software

                    collection.each(function(jobTemplate) {
                        var str = '<li><input type="radio" name="groupJobTemplate'+self.model.get('id')+'" value="'+jobTemplate.get('id')+'"> '+jobTemplate.get('name')+'</li>';
                        list.find("."+jobTemplate.get('software')).append(str);
                    });
                    panel.find("input").click(function() {
                        panel.find(".jobTemplateList").css("border-color","#47a447");
                    });
                }
            }
        });

        panel.find("button.Launch").click(function() {
            panel.find(".jobTemplateAction").css("border-color","#47a447");
            panel.find(".jobTemplateStatus").css("border-color","#5E5E5E");

            self.linkTemplateToAnntation();
        })

    }
});
