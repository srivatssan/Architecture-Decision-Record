package com.example.ADR.util;
import com.example.ADR.model.ADR;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class AdrPdfGenerator {

    public static byte[] generatePdfFromTemplate(ADR adr) throws IOException {
        // 1. Load template
        String template;
        InputStream inputStream = new ClassPathResource("templates/adr-pdf-template.html").getInputStream();
        template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);


        // 2. Prepare data map
        Map<String, String> values = new HashMap<>();
        values.put("title", adr.getTitle());
        values.put("status", adr.getStatus());
        values.put("componentId", adr.getComponentId());
        values.put("impactArea", adr.getImpactArea());
        values.put("authorEmail", adr.getAuthorEmail());
        values.put("createdAt", adr.getCreatedAt() != null ? adr.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
        values.put("tags", adr.getTags());
        values.put("linkedStories", adr.getLinkedStories());
        values.put("context", adr.getContext());
        values.put("decision", adr.getDecision());
        values.put("consequences", adr.getConsequences());
        values.put("dispositionStatus", adr.getDispositionStatus());
        values.put("dispositionTargetDate", adr.getDispositionTargetDate() != null ? adr.getDispositionTargetDate().toString() : "");
        values.put("intakeId", adr.getArchitectureIntakeId());
        values.put("ConfluenceLink", adr.getConfluenceLink());
        values.put("proofLink", adr.getProofLink());
        values.put("generatedAt", java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        // 3. Replace placeholders
        String html = new StringSubstitutor(values).replace(template);

        // 4. Render PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String baseUri = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .build()
                .toUriString();

        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, baseUri);  // <-- this is important
        builder.toStream(outputStream);
        builder.run();

        return outputStream.toByteArray();
    }
}
