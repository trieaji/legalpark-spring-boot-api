package com.soloproject.LegalPark.service.template;

import java.util.Map;

public interface ITemplateService {
    /**
     * Memproses template HTML dengan data yang diberikan..
     */
    String processEmailTemplate(String templateName, Map<String, Object> variables);
}
