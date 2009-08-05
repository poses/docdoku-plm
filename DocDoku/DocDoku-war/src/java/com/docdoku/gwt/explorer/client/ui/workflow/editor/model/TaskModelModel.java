/*
 * TaskModelModel.java
 * 
 * Copyright (c) 2009 Docdoku. All rights reserved.
 * 
 * This file is part of Docdoku.
 * 
 * Docdoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Docdoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Docdoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.gwt.explorer.client.ui.workflow.editor.model;

import com.docdoku.gwt.explorer.client.data.ExplorerConstants;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.common.TaskModelDTO;
import com.docdoku.gwt.explorer.common.UserDTO;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;
import java.util.List;

/**
 * TaskModelModel is part of the new new layer for model role in WorkflowEditor
 * TaskModelModel provides access to data contained in a TaskModelDTO
 * Whenever 
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class TaskModelModel {

    private TaskModelDTO data ;
    private List<TaskModelModelListener> observers ;


    /**
     * Build a TaskModelModel using data as sources
     * @param data
     */
    public TaskModelModel(TaskModelDTO data) {
        this.data = data;
        observers = new ArrayList<TaskModelModelListener>() ;
    }

    /**
     * Build a TaskModelModel creating a new data source
     * 
     */
    public TaskModelModel(String workspaceId){
        data = new TaskModelDTO() ;
        data.setInstructions("");
        data.setTaskName(ServiceLocator.getInstance().getExplorerI18NConstants().taskName());
        observers = new ArrayList<TaskModelModelListener>();
        data.setResponsible(ExplorerConstants.getInstance().getUser());
    }

    public TaskModelDTO getData() {
        return data;

    }

    public void setData(TaskModelDTO data) {
        this.data = data;
        fireChange();
    }

    public void setTaskName(String taskName) {
        data.setTaskName(taskName);
        fireChange();
    }

    public void setResponsible(UserDTO responsible) {
        data.setResponsible(responsible);
        fireChange();
    }

    public void setInstructions(String instructions) {
        data.setInstructions(instructions);
        fireChange();
    }



    public String getTaskName() {
        return data.getTaskName();
    }

    public UserDTO getResponsible() {
        return data.getResponsible();
    }

    public String getInstructions() {
        return data.getInstructions();
    }

    public void addListener (TaskModelModelListener l){
        observers.add(l);
    }

    public void removeListener(TaskModelModelListener l){
        observers.remove(l);
    }

    public void removeAllListeners(){
        observers.clear();
    }

    private void fireChange(){
        TaskModelEvent event = new TaskModelEvent(this);
        for (TaskModelModelListener listener : observers) {
            listener.onTaskModelModelChanged(event);
        }
    }


    /**
     * 
     * @deprecated
     */
    @Deprecated
    private void fetchAuthor(){
        AsyncCallback<UserDTO> callback = new AsyncCallback<UserDTO>() {

            public void onFailure(Throwable caught) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void onSuccess(UserDTO result) {
                data.setResponsible(result);
            }
        } ;

//        ServiceLocator.getInstance().getExplorerService().whoAmI(, callback);
    }

}
