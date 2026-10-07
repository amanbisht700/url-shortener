package com.example.shortener;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class LinkController {
    record CreateRequest(String url, String alias, Integer expiryDays) {}
    record LinkView(String code, String shortUrl, String originalUrl, long clicks,
                    Instant createdAt, Instant expiresAt) {}

    private final LinkService service;
    public LinkController(LinkService service) { this.service = service; }

    @PostMapping("/api/links")
    public ResponseEntity<LinkView> create(@RequestBody CreateRequest req) {
        ShortLink l = service.create(req.url(), req.alias(), req.expiryDays());
        return ResponseEntity.status(HttpStatus.CREATED).body(view(l));
    }

    @GetMapping("/api/links")
    public List<LinkView> list() {
        return service.recent().stream().map(this::view).toList();
    }

    // Regex excludes dots, so /index.html and other static files are not captured
    @GetMapping("/{code:[a-zA-Z0-9_-]{3,30}}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        return service.resolve(code)
                .map(l -> ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(l.getOriginalUrl())).<Void>build())
                .orElse(ResponseEntity.notFound().build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    private LinkView view(ShortLink l) {
        String base = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return new LinkView(l.getCode(), base + "/" + l.getCode(), l.getOriginalUrl(),
                l.getClicks(), l.getCreatedAt(), l.getExpiresAt());
    }
}
