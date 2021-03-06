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

package com.docdoku.client.actions;

import com.docdoku.client.ui.workflow.EditParallelActivityModelDialog;
import com.docdoku.core.workflow.ParallelActivityModel;
import com.docdoku.core.workflow.WorkflowModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.docdoku.client.ui.workflow.EditableParallelActivityModelCanvas;
import com.docdoku.client.ui.workflow.WorkflowModelFrame;

import javax.swing.*;

public class EditParallelActivityModelActionListener implements ActionListener {


    public void actionPerformed(ActionEvent pAE) {
        final EditableParallelActivityModelCanvas canvas = (EditableParallelActivityModelCanvas) pAE.getSource();
        final WorkflowModelFrame owner = (WorkflowModelFrame) SwingUtilities.getAncestorOfClass(WorkflowModelFrame.class, canvas);
        //clone the object in case the user cancels the action
        final ParallelActivityModel clonedActivityModel = canvas.getParalleActivityModel().clone();
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                EditParallelActivityModelDialog source = (EditParallelActivityModelDialog) pAE.getSource();
                int tasksToComplete = source.getNumberOfNeededCompletedTasks();
                clonedActivityModel.setTasksToComplete(tasksToComplete);
                WorkflowModel model = owner.getWorkflowModel();
                int step = canvas.getParalleActivityModel().getStep();
                model.setActivityModel(step,clonedActivityModel);
                canvas.setParallelActivityModel(clonedActivityModel);
                canvas.refresh();
            }
        };

        new EditParallelActivityModelDialog(owner, clonedActivityModel, new CreateTaskActionListener(), new EditTaskActionListener(), action);
    }
}