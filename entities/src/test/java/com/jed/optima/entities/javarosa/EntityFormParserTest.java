package com.jed.optima.entities.javarosa;

import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.instance.TreeElement;
import org.junit.Test;
import com.jed.optima.entities.javarosa.spec.EntityAction;
import com.jed.optima.entities.javarosa.spec.EntityFormParser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static com.jed.optima.entities.javarosa.spec.FormEntityElement.ATTRIBUTE_CREATE;
import static com.jed.optima.entities.javarosa.spec.FormEntityElement.ATTRIBUTE_UPDATE;
import static com.jed.optima.entities.javarosa.spec.FormEntityElement.ELEMENT_ENTITY;
import static com.jed.optima.entities.javarosa.spec.FormEntityElement.ELEMENT_LABEL;

public class EntityFormParserTest {

    @Test
    public void parseAction_findsCreateWithTrueString() {
        TreeElement entityElement = new TreeElement(ELEMENT_ENTITY);
        entityElement.setAttribute(null, ATTRIBUTE_CREATE, "true");

        EntityAction action = EntityFormParser.parseAction(entityElement);
        assertThat(action, equalTo(EntityAction.CREATE));
    }

    @Test
    public void parseAction_findsUpdateWithTrueString() {
        TreeElement entityElement = new TreeElement(ELEMENT_ENTITY);
        entityElement.setAttribute(null, ATTRIBUTE_UPDATE, "true");

        EntityAction dataset = EntityFormParser.parseAction(entityElement);
        assertThat(dataset, equalTo(EntityAction.UPDATE));
    }

    @Test
    public void parseLabel_whenLabelIsAnInt_convertsToString() {
        TreeElement labelElement = new TreeElement(ELEMENT_LABEL);
        labelElement.setAnswer(new IntegerData(0));
        TreeElement entityElement = new TreeElement(ELEMENT_ENTITY);
        entityElement.addChild(labelElement);

        String label = EntityFormParser.parseLabel(entityElement);
        assertThat(label, equalTo("0"));
    }
}
