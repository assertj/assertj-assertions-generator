package org.assertj.assertions.generator;

import java.util.HashMap;

/**
 * Created by Alexander Bischof on 25.05.15.
 */
public class TemplateRegistry extends HashMap<Template.Type, Template> {

    public void register(Template template) {
        checkTemplateParameter(template);

        super.put(template.getType(), template);
    }

    public Template getTemplate(Template.Type templateType) {
        return super.get(templateType);
    }

    private static void checkTemplateParameter(Template assertionClassTemplate) {
        if (assertionClassTemplate == null) {
            throw new NullPointerException("Expecting a non null Template");
        }
        if (assertionClassTemplate.getContent() == null) {
            throw new NullPointerException("Expecting a non null content in the Template");
        }
    }
}
