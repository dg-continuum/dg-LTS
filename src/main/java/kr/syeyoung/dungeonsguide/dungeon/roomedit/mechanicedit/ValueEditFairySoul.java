package kr.syeyoung.dungeonsguide.dungeon.roomedit.mechanicedit;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.impl.DungeonFairySoul;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.valueedit.ValueEdit;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.valueedit.ValueEditCreator;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.MLabel;
import kr.syeyoung.dungeonsguide.gui.elements.MLabelAndElement;
import kr.syeyoung.dungeonsguide.gui.elements.MTextField;
import kr.syeyoung.dungeonsguide.gui.elements.MValue;
import kr.syeyoung.dungeonsguide.utils.TextUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;

public class ValueEditFairySoul extends MPanel implements ValueEdit<DungeonFairySoul> {
    // scroll pane
    // just create
    // add set
    private final DungeonFairySoul dungeonSecret;
    private final MLabel label;
    private final MValue<OffsetPoint> value;
    private final MTextField preRequisite;
    private final MLabelAndElement preRequisite2;
    private Parameter parameter;

    public ValueEditFairySoul(final Parameter parameter2) {
        this.parameter = parameter2;
        this.dungeonSecret = (DungeonFairySoul) parameter2.getNewData();


        label = new MLabel();
        label.setText("FairySoul Point");
        label.setAlignment(MLabel.Alignment.LEFT);
        add(label);

        value = new MValue(dungeonSecret.getSecretPoint(), Collections.emptyList());
        add(value);

        preRequisite = new MTextField() {
            @Override
            public void edit(String str) {
                dungeonSecret.setPreRequisite(Arrays.asList(str.split(",")));
            }
        };
        preRequisite.setText(TextUtils.join(dungeonSecret.getPreRequisite(), ","));
        preRequisite2 = new MLabelAndElement("Req.", preRequisite);
        preRequisite2.setBounds(new Rectangle(0, 40, getBounds().width, 20));
        add(preRequisite2);
    }

    @Override
    public void onBoundsUpdate() {
        label.setBounds(new Rectangle(0, 0, getBounds().width, 20));
        value.setBounds(new Rectangle(0, 20, getBounds().width, 20));
        preRequisite2.setBounds(new Rectangle(0, 40, getBounds().width, 20));
    }

    @Override
    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public void renderWorld(float partialTicks) {
        dungeonSecret.highlight(new Color(0, 255, 0, 50), parameter.getName(), EditingContext.getEditingContext().getRoom(), partialTicks);
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(0, 0, parentWidth, parentHeight));
    }

    public static class Generator implements ValueEditCreator<ValueEditFairySoul> {

        @Override
        public ValueEditFairySoul createValueEdit(Parameter parameter) {
            return new ValueEditFairySoul(parameter);
        }

        @Override
        public Object createDefaultValue(Parameter parameter) {
            return new DungeonFairySoul();
        }

        @Override
        public Object cloneObj(Object object) {
            return ((DungeonFairySoul) object).clone();
        }
    }
}
