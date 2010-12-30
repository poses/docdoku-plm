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

public class ButtonMenu extends JMenuItem {
    private JLabel mStatusLabel;

    public ButtonMenu(JLabel pStatusLabel) {
        mStatusLabel = pStatusLabel;
    }

    @Override
    public void menuSelectionChanged(boolean pIsIncluded) {
        if (pIsIncluded) {
            Action action = getAction();
            if (action != null) {
                Object message = action.getValue(Action.LONG_DESCRIPTION);
                mStatusLabel.setText(message.toString());
            }
        } else
            mStatusLabel.setText(" ");

        super.menuSelectionChanged(pIsIncluded);
    }

}