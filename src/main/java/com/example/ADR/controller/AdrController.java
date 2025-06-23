package com.example.ADR.controller;

import com.example.ADR.model.ADR;
import com.example.ADR.repository.AdrRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.util.MimeTypeUtils;
import java.util.stream.Collectors;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/adrs")
@CrossOrigin(origins = "*")
public class AdrController {

    private final AdrRepository adrRepository;

    public AdrController(AdrRepository adrRepository) {
        this.adrRepository = adrRepository;
    }
    private String safe(Object value) {
        return value != null ? value.toString() : "None";
    }

    @GetMapping
    public List<ADR> getAllAdrs() {
        return adrRepository.findAll();
    }

    @GetMapping("/recent")
    public List<ADR> getRecentAdrs() {
        List<ADR> list = adrRepository.findTop10ByOrderByCreatedAtDesc();
        System.out.println("Fetched ADRs: " + list.size());
        return adrRepository.findTop10ByOrderByCreatedAtDesc();
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getAdrById(@PathVariable("id") Long id) {
        Optional<ADR> optionalAdr = adrRepository.findById(id);
        if (optionalAdr.isPresent()) {
            return ResponseEntity.ok(optionalAdr.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ADR not found");
        }
    }
    @PostMapping
    public ResponseEntity<?> createAdr(@RequestBody ADR adr) {
        // Defensive null check
        String role = adr.getUserRole() != null ? adr.getUserRole().toUpperCase() : "GENERAL";
        System.out.println("User Role from UI: "+role);

        // Only allow ADMIN or ARCHITECT
        if (!role.equals("ADMIN") && !role.equals("ARCHITECT")) {
            System.out.println("Access denied: only Admins or Architects can create ADRs");
            return ResponseEntity.status(403).body("Access denied: only Admins or Architects can create ADRs");
        }
        System.out.println("Moving past the forbidden check logic");
        try {
            if (adr.getCreatedAt() == null) {
                adr.setCreatedAt(LocalDate.now().atStartOfDay());
            }
            ADR savedAdr = adrRepository.save(adr);
            return ResponseEntity.ok(savedAdr);
        } catch (Exception e) {
            System.out.println("Entered the catch block. Something is wrong.");
            return ResponseEntity.status(500).body("Failed to save ADR: " + e.getMessage());
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<String> updateAdr(@PathVariable("id") Long id, @RequestBody Map<String, Object> request) {
        Optional<ADR> existingOpt = adrRepository.findById(id);
        if (existingOpt.isPresent()) {
            ADR existing = existingOpt.get();
            String role = (String) request.get("userRole");

            if (role == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing user role");
            }

            if (role.equals("ARCHITECT") || role.equals("ADMIN")) {
                existing.setTitle((String) request.get("title"));
                existing.setStatus((String) request.get("status"));
                existing.setComponentId((String) request.get("componentId"));
                existing.setImpactArea((String) request.get("impactArea"));
                existing.setDecision((String) request.get("decision"));
            }

            if (role.equals("ADMIN")) {
                existing.setDispositionStatus((String) request.get("dispositionStatus"));
                String dateStr = (String) request.get("dispositionTargetDate");
                if (dateStr != null) {
                    try {
                        existing.setDispositionTargetDate(LocalDate.parse(dateStr));
                    } catch (DateTimeParseException e) {
                        return ResponseEntity.badRequest().body("Invalid disposition target date format");
                    }
                }
            }

            adrRepository.save(existing);
            return ResponseEntity.ok("ADR updated");
        } else {
            return ResponseEntity.notFound().build();
        }

    }
    @DeleteMapping("/api/adrs/{id}")
    public ResponseEntity<String> deleteAdr(
            @PathVariable Long id,
            @RequestHeader("userRole") String userRole) {

        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only ADMIN can delete ADRs.");
        }

        Optional<ADR> optionalADR = adrRepository.findById(id);
        if (optionalADR.isPresent()) {
            ADR adr = optionalADR.get();
            adr.setDeleted(true); // Soft delete
            adrRepository.save(adr);

            return ResponseEntity.ok("ADR marked as deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ADR not found.");
        }

    }
    @GetMapping("/search")
    public ResponseEntity<List<ADR>> searchAdrs(
            @RequestParam(name = "label", required = false) String label,
            @RequestParam(name = "value", required = false) String value,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate){

    List<ADR> results;

        try {
            results = adrRepository.findAllNotDeleted().stream()
                    .filter(adr -> {
                        boolean match = true;

                        if (label != null && value != null) {
                            switch (label) {
                                case "componentId":
                                    match &= adr.getComponentId() != null && adr.getComponentId().toLowerCase().contains(value.toLowerCase());
                                    break;
                                case "impactArea":
                                    match &= adr.getImpactArea() != null && adr.getImpactArea().toLowerCase().contains(value.toLowerCase());
                                    break;
                                case "authorEmail":
                                    match &= adr.getAuthorEmail() != null && adr.getAuthorEmail().toLowerCase().contains(value.toLowerCase());
                                    break;
                                case "status":
                                    match &= adr.getStatus() != null && adr.getStatus().toLowerCase().contains(value.toLowerCase());
                                    break;
                                case "tags":
                                    match &= adr.getTags() != null && adr.getTags().toLowerCase().contains(value.toLowerCase());
                                    break;
                                case "intakeId":
                                    match &= adr.getArchitectureIntakeId() != null && adr.getArchitectureIntakeId().toLowerCase().contains(value.toLowerCase());
                                    break;
                                default:
                                    match = false;
                            }
                        }

                        if (startDate != null) {
                            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
                            match &= adr.getCreatedAt() != null && !adr.getCreatedAt().isBefore(start);
                        }

                        if (endDate != null) {
                            LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
                            match &= adr.getCreatedAt() != null && !adr.getCreatedAt().isAfter(end);
                        }

                        return match;
                    })
                    .toList();

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }

        return ResponseEntity.ok(results);
    }
    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<byte[]> exportAdrToPdf(
            @PathVariable("id") Long id,
            @RequestHeader("userRole") String userRole,
            HttpServletRequest request) {

        // Access control
        if (!"ADMIN".equalsIgnoreCase(userRole) && !"ARCHITECT".equalsIgnoreCase(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        Optional<ADR> optionalAdr = adrRepository.findById(id);
        if (optionalAdr.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        ADR adr = optionalAdr.get();

        try {
            // Load HTML template
            ClassPathResource templateResource = new ClassPathResource("templates/adr-pdf-template.html");
            String template;
            try (InputStream inputStream = templateResource.getInputStream()) {
                template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }

            // Prepare variables
            Map<String, String> valuesMap = new HashMap<>();
            valuesMap.put("title", safe(adr.getTitle()));
            valuesMap.put("status", safe(adr.getStatus()));
            valuesMap.put("componentId", safe(adr.getComponentId()));
            valuesMap.put("impactArea", safe(adr.getImpactArea()));
            valuesMap.put("authorEmail", safe(adr.getAuthorEmail()));
            valuesMap.put("createdAt", safe(adr.getCreatedAt()));
            valuesMap.put("tags", safe(adr.getTags()));
            valuesMap.put("linkedStories", safe(adr.getLinkedStories()));
            valuesMap.put("context", safe(adr.getContext()));
            valuesMap.put("decision", safe(adr.getDecision()));
            valuesMap.put("consequences", safe(adr.getConsequences()));
            valuesMap.put("dispositionStatus", safe(adr.getDispositionStatus()));
            valuesMap.put("dispositionTargetDate", safe(adr.getDispositionTargetDate()));
            valuesMap.put("architectureIntakeId", safe(adr.getArchitectureIntakeId()));
            //System.out.println("Value of Architecture Intkae ID: "+safe(adr.getArchitectureIntakeId()));
            valuesMap.put("loopLink", safe(adr.getConfluenceLink()));
            valuesMap.put("proofLink", safe(adr.getProofLink()));
            valuesMap.put("timestamp", safe(LocalDateTime.now()));

            StringSubstitutor substitutor = new StringSubstitutor(valuesMap, "{{", "}}");
            String filledHtml = substitutor.replace(template);
            substitutor.setEnableSubstitutionInVariables(true);

            // Base URI to resolve logo image
            String baseUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/static/")
                    .build()
                    .toUriString();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(filledHtml, baseUri);
            builder.toStream(outputStream);
            builder.run();

            byte[] pdfBytes = outputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("ADR_" + adr.getId() + ".pdf")
                    .build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    @GetMapping("/export/json-bulk")
    public ResponseEntity<byte[]> exportFilteredAdrsAsJson(
            @RequestParam(name = "label", required = false) String label,
            @RequestParam(name = "value", required = false) String value,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestHeader("userRole") String userRole) {

        if (!"ADMIN".equalsIgnoreCase(userRole) && !"ARCHITECT".equalsIgnoreCase(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        List<ADR> filteredAdrs = filterAdrs(label, value, startDate, endDate);

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            byte[] jsonBytes = mapper.writeValueAsBytes(filteredAdrs);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("Filtered_ADRs.json")
                    .build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(jsonBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @GetMapping("/export/pdf-bulk")
    public ResponseEntity<byte[]> exportFilteredAdrsAsPdf(
            @RequestParam(name = "label", required = false) String label,
            @RequestParam(name = "value", required = false) String value,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestHeader("userRole") String userRole) {

        if (!"ADMIN".equalsIgnoreCase(userRole) && !"ARCHITECT".equalsIgnoreCase(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        List<ADR> filteredAdrs = filterAdrs(label, value, startDate, endDate);

        try {
            ClassPathResource templateResource = new ClassPathResource("templates/adr-table-template.html");
            String template;
            try (InputStream inputStream = templateResource.getInputStream()) {
                template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }

            //StringBuilder tableBody = new StringBuilder();
            StringBuilder rowsBuilder = new StringBuilder();
            for (ADR adr : filteredAdrs) {
                rowsBuilder.append("<tr>")
                        .append("<td>").append(safe(adr.getTitle())).append("</td>")
                        .append("<td>").append(safe(adr.getStatus())).append("</td>")
                        .append("<td>").append(safe(adr.getComponentId())).append("</td>")
                        .append("<td>").append(safe(adr.getImpactArea())).append("</td>")
                        .append("<td>").append(safe(adr.getAuthorEmail())).append("</td>")
                        .append("<td>").append(safe(adr.getCreatedAt())).append("</td>")
                        .append("</tr>");
            }
            // Substitution values
            Map<String, String> valuesMap = new HashMap<>();
            valuesMap.put("rows", rowsBuilder.toString());
            valuesMap.put("timestamp", LocalDateTime.now().toString());

            StringSubstitutor substitutor = new StringSubstitutor(valuesMap, "{{", "}}");
            substitutor.setEnableSubstitutionInVariables(true);
            String filledHtml = substitutor.replace(template);

           // String finalHtml = template.replace("{{tableRows}}", tableBody.toString());

            String baseUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/static/")
                    .build()
                    .toUriString();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(filledHtml, baseUri);
            builder.toStream(outputStream);
            builder.run();

            byte[] pdfBytes = outputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("Filtered_ADRs.pdf")
                    .build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    private List<ADR> filterAdrs(String label, String value, String startDate, String endDate) {
        return adrRepository.findAllNotDeleted().stream()
                .filter(adr -> {
                    boolean match = true;
                    if (label != null && value != null) {
                        switch (label) {
                            case "componentId":
                                match &= adr.getComponentId() != null && adr.getComponentId().toLowerCase().contains(value.toLowerCase());
                                break;
                            case "impactArea":
                                match &= adr.getImpactArea() != null && adr.getImpactArea().toLowerCase().contains(value.toLowerCase());
                                break;
                            case "authorEmail":
                                match &= adr.getAuthorEmail() != null && adr.getAuthorEmail().toLowerCase().contains(value.toLowerCase());
                                break;
                            case "status":
                                match &= adr.getStatus() != null && adr.getStatus().toLowerCase().contains(value.toLowerCase());
                                break;
                            case "tags":
                                match &= adr.getTags() != null && adr.getTags().toLowerCase().contains(value.toLowerCase());
                                break;
                            case "intakeId":
                                match &= adr.getArchitectureIntakeId() != null && adr.getArchitectureIntakeId().toLowerCase().contains(value.toLowerCase());
                                break;
                        }
                    }

                    if (startDate != null) {
                        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
                        match &= adr.getCreatedAt() != null && !adr.getCreatedAt().isBefore(start);
                    }

                    if (endDate != null) {
                        LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
                        match &= adr.getCreatedAt() != null && !adr.getCreatedAt().isAfter(end);
                    }

                    return match;
                })
                .collect(Collectors.toList());
    }





}
