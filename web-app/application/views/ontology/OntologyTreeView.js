/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 13/04/11
 * Time: 14:15
 * To change this template use File | Settings | File Templates.
 */
var OntologyTreeView = Backbone.View.extend({
       tagName : "div",

       //template : _.template($('#project-view-tpl').html()),
       initialize: function(options) {
          this.tree = null;
          this.activeEvent = true;
          this.browseImageView = options.browseImageView;
          this.idAnnotation  = null;
       },
       showColors : function() {
          $(this.el).find('.tree').dynatree("getRoot").visit(function(node){

             if (node.children != null) return; //title is ok

             var title = node.data.title
             var color = node.data.color
             var htmlNode = "{{title}} <span style='background-color:{{color}}'>&nbsp;&nbsp;</span>"
             var nodeTpl = _.template(htmlNode, {title : title, color : color});


             node.setTitle(nodeTpl);
          });
       },
       render : function () {
          var self = this;
          require(["text!application/templates/explorer/OntologyTreeWrapper.tpl.html"], function(tpl) {
             self.doLayout(tpl);
          });
          return this;
       },
       doLayout: function(tpl) {
          $(this.el).html(_.template(tpl,{}));
          this.tree = $(this.el).find('.tree');
          var self = this;

          $(this.el).find('.tree').dynatree({
                 checkbox: true,
                 selectMode: 3,
                 expand : true,
                 onExpand : function() { console.log("expanding/collapsing");},
                 children: this.model.toJSON(),
                 onSelect: function(select, node) {

                    if(!self.activeEvent) return;
                    if (self.idAnnotation == null) return; // nothing to do

                    if (node.isSelected()) {
                       self.linkTerm(node.data.key);
                       console.log("Link term with annotation " + node.data.key);
                    } else if (!node.isSelected()) {
                       console.log("UnLink term with annotation " + node.data.key);
                       self.unlinkTerm(node.data.key);
                    }


                 },
                 onDblClick: function(node, event) {
                    node.toggleSelect();
                 },

                 // The following options are only required, if we have more than one tree on one page:
                 initId: "treeData"+this.model.id,
                 cookieId: "dynatree-Cb"+this.model.id,
                 idPrefix: "dynatree-Cb"+this.model.id+"-"
              });

          self.showColors();
          //expand all nodes
          $(this.el).find('.tree').dynatree("getRoot").visit(function(node){
             node.expand(true);
          });

          var ontologyPanelWidth = $(this.el).width();
          var ontologyPanelHeight = $(this.el).height();
          $(this.el).draggable({
                 start: function(event, ui) {
                    ontologyPanelWidth = $(self.el).width();
                    ontologyPanelHeight = $(self.el).height();
                 },
                 drag: function(event, ui) {
                    $(this).css("width", ontologyPanelWidth);
                    $(this).css("height", ontologyPanelHeight);
                 }
              });
          return this;
       },
       clear : function() {
          this.activeEvent = false;
          $(this.el).find('.tree').dynatree("getRoot").visit(function(node){
             node.select(false);
          });
          this.activeEvent = true;
       },
       clearAnnotation : function() {
          this.idAnnotation = null;
       },
       check : function(idTerm) {
          var self = this;
          self.activeEvent = false;
          (this.el).find('.tree').dynatree("getRoot").visit(function(node){
             if (node.data.key == idTerm) node.select(true);
          });
          self.activeEvent = true;
       },
       uncheck : function(idTerm) {
          var self = this;
          self.activeEvent = false;
          (this.el).find('.tree').dynatree("getRoot").visit(function(node){
             if (node.data.key == idTerm) node.select(false);
          });
          self.activeEvent = true;
       },
       refresh: function(idAnnotation) {

          var self = this;

          this.idAnnotation = idAnnotation;
          console.log("refresh: idAnnotation="+self.idAnnotation);
          var refreshTree = function(model , response) {
             console.log("refresh tree with:"+model.length + " elements");
             self.clear();
             self.activeEvent = false;
             console.log("self.activeEvent f="+self.activeEvent);

             console.log("check correct term");

             model.each(function(term) {
                console.log("term:" + term.get('name'));
                self.check(term.get("id"));
             });

             self.activeEvent = true;
             console.log("self.activeEvent t="+self.activeEvent);
          }


          new AnnotationTermCollection({idAnnotation:idAnnotation}).fetch({success:refreshTree});
       },
       getTermsChecked : function() {
          var terms = [];
          (this.el).find('.tree').dynatree("getRoot").visit(function(node){
             if (node.isSelected()) terms.push(node.data.key);
          });
          console.log("Selected terms ? : " + terms.length);

          return terms;
       },
       linkTerm : function(idTerm) {
          var self = this;
          new AnnotationTermModel({annotation : this.idAnnotation, term : idTerm}).save({annotation : this.idAnnotation, term : idTerm},
              {
                 success: function (model, response) {
                    window.app.view.message("Annotation Term", response.message, "");
                    self.browseImageView.reloadAnnotation(self.idAnnotation);
                    self.browseImageView.refreshAnnotationTabs(idTerm);
                 },
                 error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Annotation-Term", json.errors, "");
                 }
              }
          );
       },
       unlinkTerm : function(idTerm) {
          var self = this;
          new AnnotationTermModel({annotation : this.idAnnotation, term : idTerm}).destroy(
              {
                 success: function (model, response) {
                    console.log(response);
                    window.app.view.message("Annotation Term", response.message, "");
                    self.browseImageView.reloadAnnotation(self.idAnnotation);
                    self.browseImageView.refreshAnnotationTabs(idTerm);
                 },
                 error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Annotation-Term", json.errors, "");
                 }
              }

          );
       }

    });