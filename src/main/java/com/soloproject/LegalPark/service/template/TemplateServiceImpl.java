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
        // Menambahkan semua variabel dari Map ke konteks Thymeleaf
        variables.forEach(context::setVariable);

        // Memproses template dan mengembalikan string HTML
        return templateEngine.process(templateName, context);
    }
}
