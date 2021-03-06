define([
    "common-objects/models/part",
    "text!templates/part_list_item.html",
    "i18n!localization/nls/product-management-strings"
], function (
    Part,
    template,
    i18n
    ) {
    var PartListItemView = Backbone.View.extend({

        template: Mustache.compile(template),

        events:{
            "click input[type=checkbox]":"selectionChanged",
            "click td.part_number":"toPartModal",
            "click td.part-revision-share i":"sharePart"
        },

        tagName:"tr",

        initialize: function () {
            _.bindAll(this);
            this._isChecked = false ;
            this.listenTo(this.model,"change",this.render);
        },

        render:function(){
            this.$el.html(this.template({model:this.model,i18n:i18n}));
            this.$checkbox = this.$("input[type=checkbox]");
            if(this.isChecked()){
                this.check();
                this.trigger("selectionChanged",this);
            }
            this.bindUserPopover();
            this.trigger("rendered",this);
            return this;
        },

        selectionChanged:function(){
            this._isChecked = this.$checkbox.prop("checked");
            this.trigger("selectionChanged",this);
        },

        isChecked:function(){
            return this._isChecked;
        },

        check:function(){
            this.$checkbox.prop("checked", true);
            this._isChecked = true;
        },

        unCheck:function(){
            this.$checkbox.prop("checked", false);
            this._isChecked = false;
        },

        toPartModal:function(){
            var self = this ;
            require(['common-objects/views/part/part_modal_view'], function(PartModalView) {
                var model = new Part({partKey:self.model.getNumber() + "-" + self.model.getVersion()});
                model.fetch().success(function(){
                    new PartModalView({
                        model: model
                    }).show();
                });
            });
        },

        bindUserPopover:function(){
            this.$(".author-popover").userPopover(this.model.getAuthorLogin(),this.model.getNumber(),"left");
            if(this.model.isCheckout()){
                this.$(".checkout-user-popover").userPopover(this.model.getCheckOutUserLogin(),this.model.getNumber(),"left");
            }
        },

        sharePart:function(){
            var that = this;
            require(["common-objects/views/share/share_entity"],function(ShareView){
                var shareView = new ShareView({model:that.model,entityType:"parts"});
                $("body").append(shareView.render().el);
                shareView.openModal();
            });
        }

    });

    return PartListItemView;

});
