package com.soloproject.LegalPark.service.template;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
public class TemplateServiceImpl implements ITemplateService{
    @Autowired
    private SpringTemplateEngine templateEngine;

    @Override
    public String processEmailTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        // Adding all variables from Map to the Thymeleaf context
        variables.forEach(context::setVariable);

        // Processing templates and returning HTML strings
        return templateEngine.process(templateName, context);
    }
}
