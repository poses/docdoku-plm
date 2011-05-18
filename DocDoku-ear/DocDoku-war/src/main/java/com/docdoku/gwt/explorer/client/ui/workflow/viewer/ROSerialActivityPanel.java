package com.docdoku.gwt.explorer.client.ui.workflow.viewer;

import com.docdoku.gwt.explorer.shared.SerialActivityDTO;
import com.docdoku.gwt.explorer.shared.TaskDTO;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class ROSerialActivityPanel extends ROActivityPanel {

    private InteractiveTaskPanel interactive = null;

    public ROSerialActivityPanel(SerialActivityDTO model, String visitorName, boolean currentActivity, int step, TaskListener l) {
        HorizontalPanel mainPanel = new HorizontalPanel();
        int i = 0;
        boolean found = false;
        for (TaskDTO t : model.getTasks()) {
            ROTaskPanel taskPanel = null;
            if (!model.isStopped() && !found && t.getWorker().getName().equals(visitorName) && currentActivity && (t.getStatus() == TaskDTO.Status.NOT_STARTED || t.getStatus() == TaskDTO.Status.IN_PROGRESS)) {
                InteractiveTaskPanel tmp = new InteractiveTaskPanel(t, true, step, i);
                tmp.addTaskListener(l);
                taskPanel = tmp;
                found = true;
                interactive = tmp;
            } else {
                if (!found && (t.getStatus() == TaskDTO.Status.NOT_STARTED || t.getStatus() == TaskDTO.Status.IN_PROGRESS)){
                    found =true ;
                }
                taskPanel = new ROTaskPanel(t);
            }

            mainPanel.add(taskPanel);
            i++;
        }
        initWidget(mainPanel);
    }

    public void hideAllPopups(){
        if (interactive != null){
            interactive.hideOptions();
        }
    }

}