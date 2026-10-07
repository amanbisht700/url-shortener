package com.example.shortener;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LinkService {
    private static final String ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final LinkRepository repo;

    public LinkService(LinkRepository repo) { this.repo = repo; }

    @Transactional
    public ShortLink create(String url, String alias, Integer expiryDays) {
        validateUrl(url);
        ShortLink link = new ShortLink();
        link.setOriginalUrl(url);
        if (expiryDays != null && expiryDays > 0) {
            link.setExpiresAt(Instant.now().plus(expiryDays, ChronoUnit.DAYS));
        }
        if (alias != null && !alias.isBlank()) {
            if (!alias.matches("[a-zA-Z0-9_-]{3,30}"))
                throw new IllegalArgumentException("Alias must be 3-30 letters, numbers, - or _");
            if (repo.existsByCode(alias))
                throw new IllegalStateException("That alias is taken. Try another one.");
            link.setCode(alias);
            return repo.save(link);
        }
        link.setCode(UUID.randomUUID().toString().substring(0, 12)); // placeholder until we have the ID
        repo.save(link);
        link.setCode(toBase62(link.getId() + 100_000)); // Base62 of the DB id: short and collision-free
        return link;
    }

    @Transactional
    public Optional<ShortLink> resolve(String code) {
        Optional<ShortLink> found = repo.findByCode(code)
                .filter(l -> l.getExpiresAt() == null || l.getExpiresAt().isAfter(Instant.now()));
        found.ifPresent(l -> repo.incrementClicks(l.getId()));
        return found;
    }

    public List<ShortLink> recent() { return repo.findTop20ByOrderByCreatedAtDesc(); }

    static String toBase62(long n) {
        StringBuilder sb = new StringBuilder();
        do { sb.append(ALPHABET.charAt((int) (n % 62))); n /= 62; } while (n > 0);
        return sb.reverse().toString();
    }

    private void validateUrl(String url) {
        try {
            URI u = URI.create(url == null ? "" : url.trim());
            if (u.getHost() == null || !(u.getScheme().equals("http") || u.getScheme().equals("https")))
                throw new IllegalArgumentException();
        } catch (Exception e) {
            throw new IllegalArgumentException("Enter a full link starting with http:// or https://");
        }
    }
}
