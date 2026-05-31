package com.lola.cloudguard.rest;

import com.lola.cloudguard.domain.ScanContext;
import com.lola.cloudguard.domain.ScanResult;
import com.lola.cloudguard.domain.SecurityGroup;
import com.lola.cloudguard.domain.Violation;
import com.lola.cloudguard.parser.SecurityGroupYamlParser;
import com.lola.cloudguard.scan.PolicyScanner;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/scans")
public class ScanController {
    private final PolicyScanner scanner;
    private final ScanStore scanStore;

    public ScanController(PolicyScanner scanner, ScanStore scanStore) {
        this.scanner = scanner;
        this.scanStore = scanStore;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScanResult create(@RequestBody ScanSubmission submission) {
        ScanSubmission safeSubmission = submission == null ? new ScanSubmission("api", null, List.of()) : submission;
        ScanResult result = scanner.scan(
                safeSubmission.securityGroups(),
                new ScanContext(safeSubmission.source() == null ? "api" : safeSubmission.source(), safeSubmission.environment())
        );
        return scanStore.save(result);
    }

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public ScanResult upload(@RequestBody UploadRequest request) {
        try {
            SecurityGroupYamlParser parser = new SecurityGroupYamlParser();
            List<SecurityGroup> securityGroups = parser.parse(request.content(), request.filename());
            
            ScanResult result = scanner.scan(
                    securityGroups,
                    new ScanContext(request.source() == null ? "web-upload" : request.source(), null)
            );
            return scanStore.save(result);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Failed to parse file: " + e.getMessage()
            );
        }
    }

    @GetMapping("/{id}")
    public ScanResult get(@PathVariable UUID id) {
        return scanStore.find(id).orElseThrow(() -> notFound(id));
    }

    @GetMapping("/{id}/violations")
    public List<Violation> violations(@PathVariable UUID id) {
        return get(id).violations();
    }

    private ResponseStatusException notFound(UUID id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Scan not found: " + id);
    }
}
