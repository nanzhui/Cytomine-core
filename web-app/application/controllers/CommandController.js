var CommandController = Backbone.Controller.extend({
    undo : function() {
        var self = this;
        $.post('command/undo.json', {}, function(data) {
            console.log("data:");
            console.log(data);
             _.each(data, function(undoElem){
                  console.log(undoElem);
                  window.app.view.message("Undo", undoElem.message, "");
                  self.dispatch(undoElem.callback);
             });

        }, "json");

    },

    redo : function () {
        var self = this;
        $.post('command/redo.json', {}, function(data) {
                console.log("data:");
                console.log(data);
                 _.each(data, function(redoElem){
                      console.log(redoElem);
                      window.app.view.message("Redo", redoElem.message, "");
                      self.dispatch(redoElem.callback);
                 });
        }, "json");

    },

    dispatch : function(callback) {
        if (!callback) return; //nothing to do

        //Annotations
        if (callback.method == "be.cytomine.AddAnnotationCommand") {
            var tab = _.detect(window.app.controllers.browse.tabs.images, function(object) {
                return object.idImage == callback.imageID;
            });
            var image = tab.browImageView;
            if (image == undefined) return; //tab is closed
            image.getUserLayer().annotationAdded(callback.annotationID);
        } else if (callback.method == "be.cytomine.EditAnnotationCommand") {
            var tab = _.detect(window.app.controllers.browse.tabs.images, function(object) {
                return object.idImage == callback.imageID;
            });
            var image = tab.browImageView;
            if (image == undefined) return; //tab is closed
            image.getUserLayer().annotationUpdated(callback.annotationID);
        } else if (callback.method == "be.cytomine.DeleteAnnotationCommand") {
            var tab = _.detect(window.app.controllers.browse.tabs.images, function(object) {
                return object.idImage == callback.imageID;
            });
            var image = tab.browImageView;
            if (image == undefined) return; //tab is closed
            image.getUserLayer().annotationRemoved(callback.annotationID);
        } else if (callback.method == "be.cytomine.AddAnnotationTermCommand") {
            var tab = _.detect(window.app.controllers.browse.tabs.images, function(object) {
                return object.idImage == callback.imageID;
            });
            var image = tab.browImageView;
            if (image == undefined) return; //tab is closed
            image.getUserLayer().termAdded(callback.annotationID,callback.termID);
        } else if (callback.method == "be.cytomine.DeleteAnnotationTermCommand") {
            var tab = _.detect(window.app.controllers.browse.tabs.images, function(object) {
                return object.idImage == callback.imageID;
            });
            var image = tab.browImageView;
            if (image == undefined) return; //tab is closed
            image.getUserLayer().termRemoved(callback.annotationID,callback.termID);
        }
    }
});