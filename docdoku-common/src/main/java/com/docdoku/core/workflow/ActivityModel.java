/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU Affero General Public License for more details.  
 *  
 * You should have received a copy of the GNU Affero General Public License  
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.core.workflow;

import com.docdoku.core.common.User;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Defines common attributes and behaviors for activities model.
 * 
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@Table(name="ACTIVITYMODEL")
@javax.persistence.IdClass(com.docdoku.core.workflow.ActivityModelKey.class)
@XmlSeeAlso({SerialActivityModel.class, ParallelActivityModel.class})
@Inheritance()
@Entity
public abstract class ActivityModel implements Serializable, Cloneable {

    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name="WORKFLOWMODEL_ID", referencedColumnName="ID"),
        @JoinColumn(name="WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    protected WorkflowModel workflowModel;   
    
    @OneToMany(mappedBy = "activityModel", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @OrderBy("num")
    protected List<TaskModel> taskModels=new LinkedList<TaskModel>();
    
    @javax.persistence.Id
    protected int step;
    
    @javax.persistence.Column(name = "WORKFLOWMODEL_ID", length=50, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String workflowModelId="";
    
    @javax.persistence.Column(name = "WORKSPACE_ID", length=50, nullable = false, insertable = false, updatable = false)
    @javax.persistence.Id
    private String workspaceId="";

    @ManyToOne(optional = true,fetch=FetchType.EAGER)
    @JoinTable (
            name="ACTIVITYMODEL_RELAUNCH",
            joinColumns={
                    @JoinColumn(name="ACTIVITYMODEL_STEP", referencedColumnName="STEP"),
                    @JoinColumn(name="WORKFLOWMODEL_ID", referencedColumnName="WORKFLOWMODEL_ID"),
                    @JoinColumn(name="WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
            },
            inverseJoinColumns={
                    @JoinColumn(name="RELAUNCHACTIVITYMODEL_STEP", referencedColumnName="STEP"),
                    @JoinColumn(name="RELAUNCHWORKFLOWMODEL_ID", referencedColumnName="WORKFLOWMODEL_ID"),
                    @JoinColumn(name="RELAUNCHWORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
            }
    )
    private ActivityModel relaunchActivity;
    
    protected String lifeCycleState;

    public ActivityModel(){
    
    }
    
    public ActivityModel(WorkflowModel pWorkflowModel, int pStep, List<TaskModel> pTaskModels, String pLifeCycleState){
        setWorkflowModel(pWorkflowModel);
        taskModels=pTaskModels;
        step=pStep;
        lifeCycleState=pLifeCycleState;
    }
    
    
    public void setWorkflowModel(WorkflowModel pWorkflowModel){
        workflowModel=pWorkflowModel;
        workflowModelId=workflowModel.getId();
        workspaceId=workflowModel.getWorkspaceId();
    }
    
    public int getStep(){
        return step;
    }
    
    public void setStep(int pStep){
        step=pStep;
    }
    
    public String getLifeCycleState(){
        return lifeCycleState;
    }

    
    public void setTaskModels(List<TaskModel> taskModels) {
        this.taskModels = taskModels;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getWorkflowModelId() {
        return workflowModelId;
    }

    @XmlTransient
    public WorkflowModel getWorkflowModel() {
        return workflowModel;
    }

    public void setLifeCycleState(String pLifeCycleState){
        lifeCycleState=pLifeCycleState;
    }
    
    public void addTaskModel(TaskModel pTaskModel) {
        taskModels.add(pTaskModel);
        int index = taskModels.size()-1;
        pTaskModel.setNum(index);
    }
    
    public TaskModel removeTaskModel(int pOrder) {
        TaskModel taskModel = taskModels.remove(pOrder);
        for(int i=pOrder;i<taskModels.size();i++){
            taskModels.get(i).setNum(i);
        }
        return taskModel;
    }
    
    public void removeTaskModel(TaskModel pTaskModel) {
        int index = taskModels.indexOf(pTaskModel);
        removeTaskModel(index);
    }

    public List<TaskModel> getTaskModels() {
        return taskModels;
    }

    @XmlTransient
    public ActivityModel getRelaunchActivity() {
        return relaunchActivity;
    }

    public void setRelaunchActivity(ActivityModel relaunchActivity) {
        this.relaunchActivity = relaunchActivity;
    }

    /**
     * perform a deep clone operation
     */
    @Override
    public ActivityModel clone() {
        ActivityModel clone = null;
        try {
            clone = (ActivityModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        //perform a deep copy
        List<TaskModel> clonedTaskModels = new LinkedList<TaskModel>();
        for (TaskModel taskModel : taskModels) {
            TaskModel clonedTaskModel=taskModel.clone();
            clonedTaskModel.setActivityModel(clone);
            clonedTaskModels.add(clonedTaskModel);
        }
        clone.taskModels = clonedTaskModels;
        return clone;
    }

    public abstract Activity createActivity(Map<Role, User> roleUserMap);

}
