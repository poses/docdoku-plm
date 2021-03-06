define([
    "i18n!localization/nls/document-management-strings",
    "common-objects/models/activity_model",
    "text!templates/activity_model_editor.html",
    "common-objects/models/task_model"
], function (
    i18n,
    ActivityModel,
    template,
    TaskModel
    ) {
    var ActivityModelEditorView = Backbone.View.extend({

        tagName: "li",
        className: "activity-section",

        events: {
            "click button.add-task" : "addTaskAction",
            "click button.switch-activity" : "switchActivityAction",
            "click button.delete-activity" : "deleteActivityAction",
            "change input.activity-state":  "lifeCycleStateChanged",
            "change input.tasksToComplete":  "tasksToCompleteChanged",
            "change select.relaunchActivitySelector":"changeRelaunchActivityStep"
        },

        initialize: function () {

            this.subviews = [];

            var switchModeTitle;
            switch(this.model.get("type")){
                case "SERIAL":
                    switchModeTitle = i18n.GOTO_PARALLEL_MODE;
                    break;
                case "PARALLEL":
                    switchModeTitle = i18n.GOTO_SERIAL_MODE;
                    break;
            }

            this.template = Mustache.render(template,{cid: this.model.cid, activity: this.model.attributes, switchModeTitle: switchModeTitle, i18n: i18n});

            this.model.attributes.taskModels.bind('add', this.addOneTask, this);
            this.model.attributes.taskModels.bind('remove', this.removeOneTask, this);

            this.on("activities-order:changed",this.populateRelaunchActivitySelector);

        },

        addAllTask: function() {
            this.model.attributes.taskModels.each(this.addOneTask, this);
        },

        addOneTask: function(taskModel) {
            var self = this;

            this.updateMaxTasksToComplete();

            require(["views/task_model_editor"], function(TaskModelEditorView) {
                var taskModelEditorView = new TaskModelEditorView({model: taskModel, roles: self.options.roles});
                self.subviews.push(taskModelEditorView);
                taskModelEditorView.render();
                self.tasksUL.append(taskModelEditorView.el);
            });
        },

        removeOneTask: function(){
            this.updateMaxTasksToComplete();

            var cntTasks = this.model.get("taskModels").length;

            if(this.inputTasksToComplete.val() > cntTasks){
                this.inputTasksToComplete.val(cntTasks);
                this.tasksToCompleteChanged();
            }
        },

        updateMaxTasksToComplete: function(){
            this.inputTasksToComplete.attr({
                MAX: this.model.get("taskModels").length
            });
        },

        addTaskAction: function(){
            this.inputTasksToComplete.val(parseInt(this.inputTasksToComplete.val(),10)+1);
            this.tasksToCompleteChanged();

            this.model.attributes.taskModels.add(new TaskModel());
            return false;
        },

        switchActivityAction: function(){
            switch(this.model.get("type")){
                case "SERIAL":
                    this.model.set({
                        type: "PARALLEL"
                    });
                    this.activityDiv.removeClass("SERIAL");
                    this.activityDiv.addClass("PARALLEL");
                    this.buttonSwitchActivity.attr({title:i18n.GOTO_SERIAL_MODE});
                    break;
                case "PARALLEL":
                    this.model.set({
                        type: "SERIAL"
                    });
                    this.activityDiv.removeClass("PARALLEL");
                    this.activityDiv.addClass("SERIAL");
                    this.buttonSwitchActivity.attr({title:i18n.GOTO_PARALLEL_MODE});
                    break;
            }
            return false;
        },

        deleteActivityAction: function(){
            this.model.collection.remove(this.model);
            this.unbindAllEvents();
            this.remove();
        },

        tasksToCompleteChanged: function(){
            this.model.set({
                tasksToComplete: this.inputTasksToComplete.val()
            });
        },

        taskPositionChanged: function(oldPosition, newPosition){
            var taskModel = this.model.attributes.taskModels.at(oldPosition);
            this.model.attributes.taskModels.remove(taskModel, {silent:true});
            this.model.attributes.taskModels.add(taskModel, {silent:true, at:newPosition});
        },

        lifeCycleStateChanged: function(){
            this.model.set({
                lifeCycleState: this.inputLifeCycleState.val()
            });
        },

        render: function() {
            this.$el.html(this.template);

            this.bindDomElements();

            this.addAllTask();

            this.populateRelaunchActivitySelector();

            return this;
        },

        bindDomElements: function(){
            var self = this;

            this.activityDiv = this.$("div.activity");

            this.buttonSwitchActivity = this.$('button.switch-activity');

            this.inputLifeCycleState = this.$('input.activity-state');

            this.inputTasksToComplete = this.$('input.tasksToComplete');

            this.relaunchActivitySelector = this.$(".relaunchActivitySelector");

            this.relaunchActivitySelectorWrapper = this.$(".relaunchActivitySelector-wrapper");

            this.tasksUL = this.$("ul.task-list");
            this.tasksUL.sortable({
                handle: "i.icon-reorder",
                tolerance: "pointer",
                start: function(event, ui) {
                    ui.item.oldPosition = ui.item.index();
                },
                stop: function(event, ui) {
                    self.taskPositionChanged(ui.item.oldPosition, ui.item.index());
                }
            });
        },

        populateRelaunchActivitySelector:function(){
            var that = this;
            this.relaunchActivitySelector.empty();
            this.relaunchActivitySelector.append("<option value='-1'></option>");
            var stepCount = 0;

            var modelIndex = this.model.collection.indexOf(this.model);

            this.model.collection.each(function(activity){
                var activityIndex = activity.collection.indexOf(activity);
                if(activityIndex < modelIndex){
                    that.relaunchActivitySelector.append("<option value='"+activityIndex+"'>"+activity.get("lifeCycleState")+"</option>");
                    stepCount ++ ;
                }
            });

            if(!stepCount){
                this.relaunchActivitySelectorWrapper.hide();
            }else{
                this.relaunchActivitySelectorWrapper.show();
            }

            if(this.model.get("relaunchStep") != null){
                this.relaunchActivitySelector.val(this.model.get("relaunchStep"));
            }

        },

        unbindAllEvents: function(){
            _.each(this.subviews, function(subview){
                subview.unbindAllEvents();
            });
            this.undelegateEvents();
        },

        changeRelaunchActivityStep:function(e){
            if(e.target.value == -1){
                this.model.set("relaunchStep",null);
            }else{
                this.model.set("relaunchStep",e.target.value);
            }
        }

    });
    return ActivityModelEditorView;
});