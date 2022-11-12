/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.dungeon.roomedit.valueedit;

import kr.syeyoung.dungeonsguide.dungeon.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.MIntegerSelectionButton;
import kr.syeyoung.dungeonsguide.gui.elements.MLabel;
import kr.syeyoung.dungeonsguide.gui.elements.MLabelAndElement;

import java.awt.*;

public class ValueEditInteger extends MPanel implements ValueEdit<Integer> {
    private Parameter parameter;


    public ValueEditInteger(final Parameter parameter2) {
        this.parameter = parameter2;
        {
            MLabel label = new MLabel() {
                @Override
                public String getText() {
                    return parameter.getPreviousData().toString();
                }
            };
            MLabelAndElement mLabelAndElement = new MLabelAndElement("Prev", label);
            mLabelAndElement.setBounds(new Rectangle(0, 0, getBounds().width, 20));
            add(mLabelAndElement);
        }
        {
            int newData = (Integer) parameter.getNewData();
            final MIntegerSelectionButton textField = new MIntegerSelectionButton(newData);
            textField.setOnUpdate(new Runnable() {
                @Override
                public void run() {
                    parameter.setNewData(textField.getData());
                }
            });
            MLabelAndElement mLabelAndElement = new MLabelAndElement("New", textField);
            mLabelAndElement.setBounds(new Rectangle(0, 20, getBounds().width, 20));
            add(mLabelAndElement);
        }
    }

    @Override
    public void renderWorld(float partialTicks) {

    }

    @Override
    public void onBoundsUpdate() {
        for (MPanel panel : getChildComponents()) {
            panel.setSize(new Dimension(getBounds().width, 20));
        }
    }

    @Override
    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(0, 0, parentWidth, parentHeight));
    }

    public static class Generator implements ValueEditCreator<ValueEditInteger> {

        @Override
        public ValueEditInteger createValueEdit(Parameter parameter) {
            return new ValueEditInteger(parameter);
        }

        @Override
        public Object createDefaultValue(Parameter parameter) {
            return 0;
        }

        @Override
        public Object cloneObj(Object object) {
            return object;
        }
    }
}
