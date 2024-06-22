package com.wised.auth.helper;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.util.Map;

/**
 * Represents an email template that can be customized with replacement parameters.
 */
public class EmailTemplate {

    private String template;

    /**
     * Initializes an EmailTemplate object by loading the template content from a file.
     * @param customTemplate The name of the template file to load.
     */
    public EmailTemplate(String customTemplate) {
        System.out.println(customTemplate);
        try {
            // Load the template content from the specified file
            this.template = loadTemplate(customTemplate);
        } catch (Exception e) {
            // If an error occurs while loading the template, store an error message in the template
            this.template = e.getMessage();
        }
    }

    /**
     * Load the template content from a file.
     * @param customTemplate The name of the template file to load.
     * @return The content of the template as a string.
     * @throws Exception If the template file cannot be read.
     */
    private String loadTemplate(String customTemplate) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(customTemplate).getFile());
        String content = "Empty"; // Default content if template file cannot be read

        try {
            // Read the template file into a string
            content = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new Exception("Could not read template = " + customTemplate);
        }

        return content;
    }

    /**
     * Get the template content with replacement parameters applied.
     * @param replacements A map of replacement parameters where keys are placeholders in the template and values are replacements.
     * @return The template content with replacements.
     */
    public String getTemplate(Map<String, String> replacements) {
        String customizedTemplate = this.template;

        // Replace placeholders in the template with values from the replacements map
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            customizedTemplate = customizedTemplate.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        return customizedTemplate;
    }
}
