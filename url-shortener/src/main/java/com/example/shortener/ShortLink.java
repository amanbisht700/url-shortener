package com.example.shortener;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(indexes = @Index(name = "idx_code", columnList = "code", unique = true))
public class ShortLink {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 30)
    private String code;
    @Column(nullable = false, length = 2048)
    private String originalUrl;
    private long clicks;
    private Instant createdAt = Instant.now();
    private Instant expiresAt;

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String u) { this.originalUrl = u; }
    public long getClicks() { return clicks; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant e) { this.expiresAt = e; }
}
