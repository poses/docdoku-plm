 define([
    "common-objects/collections/security/workspace_user_memberships",
    "common-objects/collections/security/workspace_user_group_memberships",
    "common-objects/views/security/membership_item",
    "common-objects/models/security/acl_user_entry",
    "common-objects/models/security/acl_user_group_entry",
    "common-objects/views/security/acl_item",
    "common-objects/models/security/admin",
    "text!common-objects/templates/security/acl_edit.html",
    "i18n!localization/nls/security-strings"
], function (
    WorkspaceUserMemberships,
    WorkspaceUserGroupMemberships,
    MembershipItemView,
    ACLUserEntry,
    ACLUserGroupEntry,
    ACLItemView,
    Admin,
    template,
    i18n
) {
    var ACLEditView = Backbone.View.extend({

        events:{
            "hidden #acl_edit_modal":"destroy",
            "submit form":"onSubmit"
        },

        template: Mustache.compile(template),

        initialize: function() {
            _.bindAll(this);
            this.useACL = false;
            this.acl = this.options.acl;
            this.aclUserEntries = [];
            this.aclUserGroupEntries = [];
        },

        setTitle:function(title){
            this.title = title;
        },

        openModal: function() {
            this.$modal.modal('show');
        },

        closeModal: function() {
            this.$modal.modal('hide');
            this.remove();
        },

        bindDomElements:function(){
            this.$modal = this.$('#acl_edit_modal');
            this.$usersAcls = this.$("#users-acl-entries");
            this.$userGroupsAcls = this.$("#groups-acl-entries");
            this.$usingAcl = this.$(".using-acl");
            this.$aclSwitch = this.$(".acl-switch");
        },

        render:function(){

            var that = this ;

            this.$el.html(this.template({i18n:i18n, title:this.title}));

            this.bindDomElements();

            this.admin = new Admin();
            this.admin.fetch({reset:true,success:function(){

                that.useACL = (that.acl != null);
                if(!that.useACL){
                    that.$usingAcl.addClass("hide");
                }

                if(that.acl == null){
                    that.onNoAclGiven();
                }else{
                    for(var userLogin in that.acl.userEntries){
                        var userAclView = new ACLItemView({model:new ACLUserEntry({userLogin : userLogin, permission :that.acl.userEntries[userLogin]}), editMode:that.options.editMode  && userLogin != that.admin.getLogin() && userLogin != APP_CONFIG.login}).render();
                        that.$usersAcls.append(userAclView.$el);
                        that.aclUserEntries.push(userAclView.model);
                    }

                    for(var groupId in that.acl.groupEntries){
                        var groupAclView = new ACLItemView({model:new ACLUserGroupEntry({groupId : groupId, permission :that.acl.groupEntries[groupId]}), editMode:that.options.editMode}).render();
                        that.$userGroupsAcls.append(groupAclView.$el);
                        that.aclUserGroupEntries.push(groupAclView.model);
                    }
                }

                that.$aclSwitch.bootstrapSwitch();
                that.$aclSwitch.bootstrapSwitch('setState', that.useACL);
                that.$aclSwitch.on('switch-change', function (e, data) {
                    that.useACL = !that.useACL;
                    that.$usingAcl.toggleClass("hide");
                });

            }});

            return this;
        },


        onNoAclGiven:function(){
            this.loadWorkspaceMembership();
        },

        loadWorkspaceMembership:function(){
            this.userMemberships = new WorkspaceUserMemberships();
            this.userGroupMemberships = new WorkspaceUserGroupMemberships();
            this.listenToOnce(this.userMemberships,"reset",this.onUserMembershipsReset);
            this.listenToOnce(this.userGroupMemberships,"reset",this.onUserGroupMembershipsReset);
            this.userMemberships.fetch({reset:true});
            this.userGroupMemberships.fetch({reset:true});
        },

        onUserMembershipsReset:function(){
            var that = this ;
            this.userMemberships.each(function(userMembership){
                var view = new ACLItemView({model:new ACLUserEntry({userLogin : userMembership.key(), permission :userMembership.getPermission()}), editMode:that.options.editMode && userMembership.key() != that.admin.getLogin() && userMembership.key() != APP_CONFIG.login}).render();
                that.$usersAcls.append(view.$el);
                that.aclUserEntries.push(view.model);
            });
        },

        onUserGroupMembershipsReset:function(){
            var that = this ;
            this.userGroupMemberships.each(function(userGroupMembership){
                var view = new ACLItemView({model:new ACLUserGroupEntry({groupId : userGroupMembership.key(), permission :userGroupMembership.getPermission()}), editMode:that.options.editMode}).render();
                that.$userGroupsAcls.append(view.$el);
                that.aclUserGroupEntries.push(view.model);
            });
        },

        toList:function(){
            if(!this.useACL){
                return null;
            }
            var dto = {};
            dto.userEntries = {};
            dto.groupEntries = {};
            _(this.aclUserEntries).each(function(aclEntry){
                dto.userEntries[aclEntry.key()]=aclEntry.getPermission();
            });
            _(this.aclUserGroupEntries).each(function(aclEntry){
                dto.groupEntries[aclEntry.key()]=aclEntry.getPermission();
            });
            return dto;
        },

        onSubmit:function(e){
            this.trigger("acl:update");
            e.preventDefault();
            e.stopPropagation();
            return false ;
        }
    });

    return ACLEditView;
});
