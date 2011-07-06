/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.gwt.explorer.client.ui.workflow.viewer;

import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ExplorerConstants;
import com.docdoku.gwt.explorer.client.ui.doc.DocMainPanel;
import com.docdoku.gwt.explorer.client.ui.workflow.viewer.TaskChangeEvent.Type;
import com.docdoku.gwt.explorer.shared.MasterDocumentDTO;
import com.docdoku.gwt.explorer.shared.WorkflowDTO;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 *
 * @author Emmanuel Nhan
 */
public class WorkflowGlassPanel extends PopupPanel implements ClickHandler, TaskListener {

    public final static double RATIO = 0.85;
    // note : this is a scroll size experimentaly build with Firefox & may not fit other browsers
    private final static int SCROLL_SIZE = 15;
    private WorkflowViewer viewer;
    private ScrollPanel scroll;
    private WorkflowDTO workflow;
    private String visitorName;
    private DocMainPanel parentPanel;
    private Action approveCommand;
    private Action rejectCommand;

    public WorkflowGlassPanel(DocMainPanel owner) {
        super(true);
        setGlassEnabled(true);
        parentPanel = owner;
        viewer = null;
        scroll = null;


    }

    public void setWorkflow(WorkflowDTO workflow) {
        this.workflow = workflow;
        this.visitorName = ExplorerConstants.getInstance().getUser().getName();
        viewer = new WorkflowViewer(workflow, visitorName, this);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        showViewer();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        if (scroll != null) {
            scroll.removeFromParent();
        }
        if (viewer.isAttached()) {
            viewer.removeFromParent();
        }

    }

    @Override
    public void onClick(ClickEvent event) {
        this.removeFromParent();
    }

    private void callApprove(int activityStep, int taskStep, String comment) {
        approveCommand.execute(workflow.getWorkspaceId(), workflow.getId(), activityStep, taskStep, comment, this);
    }

    private void callReject(int activityStep, int taskStep, String comment) {
        rejectCommand.execute(workflow.getWorkspaceId(), workflow.getId(), activityStep, taskStep, comment, this);
    }

    public void onTaskStatusChange(TaskChangeEvent event) {
        if (event.getType() == Type.APPROVE) {
            callApprove(event.getActivity(), event.getStep(), event.getComment());
        } else {
            callReject(event.getActivity(), event.getStep(), event.getComment());

        }
    }

    private void showViewer() {
        if (scroll != null) {
            scroll.removeFromParent();
        }
        RootPanel.get().add(viewer);
        // very dirty workaround to get widget size !
        int width = viewer.getOffsetWidth();
        int height = viewer.getOffsetHeight();
        viewer.setVisible(false);
        if (width <= Window.getClientWidth() * RATIO && height <= Window.getClientHeight() * RATIO) {
            viewer.setVisible(true);
            RootPanel.get().setWidgetPosition(viewer, Window.getClientWidth() / 2 - viewer.getOffsetWidth() / 2, Window.getClientHeight() / 2 - viewer.getOffsetHeight() / 2);

        } else if (width <= Window.getClientWidth() * RATIO && height > Window.getClientHeight() * RATIO) {
            // width ok, but not height :
            scroll = new ScrollPanel(viewer);
            scroll.setHeight((Window.getClientHeight() * RATIO) + "px");
            scroll.setWidth(width + "px");
            RootPanel.get().remove(viewer);
            viewer.setVisible(true);
            RootPanel.get().add(scroll);
            RootPanel.get().setWidgetPosition(scroll, Window.getClientWidth() / 2 - scroll.getOffsetWidth() / 2, Window.getClientHeight() / 2 - scroll.getOffsetHeight() / 2);
        } else if (width > Window.getClientWidth() * RATIO && height <= Window.getClientHeight() * RATIO) {
            // height ok, but not width :
            scroll = new ScrollPanel(viewer);
            scroll.setHeight((height + SCROLL_SIZE) + "px");
            scroll.setWidth((Window.getClientWidth() * RATIO) + "px");
            RootPanel.get().remove(viewer);
            viewer.setVisible(true);
            RootPanel.get().add(scroll);
            RootPanel.get().setWidgetPosition(scroll, Window.getClientWidth() / 2 - scroll.getOffsetWidth() / 2, Window.getClientHeight() / 2 - scroll.getOffsetHeight() / 2);
        } else {
            scroll = new ScrollPanel(viewer);
            // this viewer is realy too large ... or client is on EEEPC !!
            scroll.setHeight((Window.getClientHeight() * RATIO) + "px");
            scroll.setWidth((Window.getClientWidth() * RATIO) + "px");
            RootPanel.get().remove(viewer);
            viewer.setVisible(true);
            RootPanel.get().add(scroll);
            RootPanel.get().setWidgetPosition(scroll, Window.getClientWidth() / 2 - scroll.getOffsetWidth() / 2, Window.getClientHeight() / 2 - scroll.getOffsetHeight() / 2);
        }

    }

    public void updateAfterAcceptOrReject(MasterDocumentDTO result) {
        viewer.hideAllPopups();
        viewer.removeFromParent();
        parentPanel.setLifeCycleState(result.getLifeCycleState());
        setWorkflow(result.getWorkflow());
        showViewer();
    }

    public void setApproveAction(Action command) {
        approveCommand = command;
    }

    public void setRejectAction(Action command) {
        rejectCommand = command;
    }
}
