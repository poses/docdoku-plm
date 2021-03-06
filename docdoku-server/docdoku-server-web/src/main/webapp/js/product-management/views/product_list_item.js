define([
    "text!templates/product_list_item.html",
    "i18n!localization/nls/product-management-strings"
], function (
    template,
    i18n
    ) {
    var ProductListItemView = Backbone.View.extend({

        template: Mustache.compile(template),

        events:{
            "click input[type=checkbox]":"selectionChanged",
            "click td.product_id":"openDetailsView"
        },

        tagName:"tr",

        initialize: function () {
            this._isChecked = false ;
        },

        render:function(){
            this.$el.html(this.template({model:this.model, i18n:i18n}));
            this.$checkbox = this.$("input[type=checkbox]");
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

        openDetailsView:function(){
            var that = this ;
            require(["views/product_details_view"],function(ProductDetailsView){
                var pdv = new ProductDetailsView({model:that.model});
                $("body").append(pdv.render().el);
                pdv.openModal();
            });
        }

    });

    return ProductListItemView;

});
