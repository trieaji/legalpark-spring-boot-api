package com.soloproject.LegalPark.service.template;

import java.util.Map;

public interface ITemplateService {
    /**
     * Processing HTML templates with the given data.
     */
    String processEmailTemplate(String templateName, Map<String, Object> variables);
}
