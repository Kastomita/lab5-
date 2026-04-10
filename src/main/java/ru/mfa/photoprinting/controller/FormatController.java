package ru.mfa.photoprinting.controller;

import ru.mfa.photoprinting.model.Format;
import ru.mfa.photoprinting.service.FormatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/formats")
public class FormatController {

    private final FormatService formatService;

    public FormatController(FormatService formatService) {
        this.formatService = formatService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Format>> getAllFormats() {
        return ResponseEntity.ok(formatService.getAllFormats());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Format> getFormatById(@PathVariable Long id) {
        Format format = formatService.getFormatById(id);
        return format != null ? ResponseEntity.ok(format) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Format> createFormat(@RequestBody Format format) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formatService.createFormat(format));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Format> updateFormat(@PathVariable Long id, @RequestBody Format format) {
        Format updated = formatService.updateFormat(id, format);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFormat(@PathVariable Long id) {
        formatService.deleteFormat(id);
        return ResponseEntity.noContent().build();
    }
}