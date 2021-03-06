define(["text!templates/local_versioned_file.html", "views/loader_view",  "commander", "storage"], function(template, Loader, Commander, Storage) {

    var LocalVersionedFileView = Backbone.View.extend({

        className: "versionedFile",

        template: Handlebars.compile(template),

        events: {
            "click .icon-signout"   : "checkin",
            "click .icon-signin"    : "checkout",
            "click .icon-refresh"   : "render",
            "click .icon-download"  : "get",
            "click .icon-undo"      : "undoCheckout"
        },

        render:function() {
            var status = this.model.getStatus();
            status.checkoutDateParsed = moment(status.checkoutDate).format("YYYY-MM-DD HH:MM:ss");
            status.isCheckedOutByMe = this.isCheckoutByConnectedUser(status);
            status.iteration = _.last(status.iterations);

            this.$el.html(this.template({model: this.model, status: status}));

            if (status.lastModified && this.model.getMTime() > status.lastModified) {
                this.$el.addClass("modified");
            }

            return this;
        },

        loader:function() {
            this.$el.html(new Loader());
        },

        isCheckoutByConnectedUser:function(status) {
            return status.checkoutUser == APP_GLOBAL.GLOBAL_CONF.user;
        },

        checkin:function() {
            this.loader();
            var self = this;
            Commander.checkin(this.model, function() {
               Commander.getStatusForFile(self.model.getFullPath(), function(pStatus) {
                   var status = JSON.parse(pStatus);
                   self.model.setStatus(status);
                   self.render();
               });
            });
        },

        checkout:function() {
            this.loader();
            var self = this;
            Commander.checkout(this.model, function() {
                Commander.getStatusForPart(self.model, function(pStatus) {
                    var status = JSON.parse(pStatus);
                    self.model.setStatus(status);
                    self.render();
                });
            });
        },

        undoCheckout:function() {
            this.loader();
            var self = this;
            Commander.undoCheckout(this.model, function() {
                Commander.getStatusForFile(self.model.getFullPath(), function(pStatus) {
                    var status = JSON.parse(pStatus);
                    self.model.setStatus(status);
                    self.render();
                });
            });
        },

        get:function() {
            this.loader();
            var self = this;
            Commander.get(this.model.getFullPath(), function() {
                Commander.getStatusForFile(self.model.getFullPath(), function(pStatus) {
                    var status = JSON.parse(pStatus);
                    self.model.setStatus(status);
                    self.render();
                });
            });
        }
    });

    return LocalVersionedFileView;
});