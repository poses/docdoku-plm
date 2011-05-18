/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

package com.docdoku.client.ui.common;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CloseButton extends JButton {

    public CloseButton(final Window pWindow, String pLabel) {
        super(pLabel);
        Image img = Toolkit.getDefaultToolkit().getImage(CloseButton.class.getResource("/com/docdoku/client/resources/icons/stop.png"));
        ImageIcon closeIcon = new ImageIcon(img);
        setIcon(closeIcon);
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                pWindow.dispose();
            }
        });
    }
}