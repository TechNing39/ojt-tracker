package com.ojttracker.auth;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SiteRepository siteRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final String adminPinHash;

    public AuthController(
            SiteRepository siteRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            @Value("${app.auth.admin-pin}") String adminPin) {
        this.siteRepository = siteRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.adminPinHash = passwordEncoder.encode(adminPin);
    }

    public record SiteVerifyRequest(String code, String pin) {
    }

    @PostMapping("/site/verify")
    public ResponseEntity<?> verifySite(@RequestBody SiteVerifyRequest request) {
        if (request.code() == null || request.pin() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "code, pin은 필수입니다."));
        }

        return siteRepository.findByCode(request.code())
                .filter(site -> passwordEncoder.matches(request.pin(), site.getPinHash()))
                .<ResponseEntity<?>>map(site -> ResponseEntity.ok(Map.of(
                        "token", tokenService.issueToken(Role.SITE, site.getId()),
                        "role", "SITE",
                        "siteId", site.getId(),
                        "siteName", site.getName())))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "PIN이 올바르지 않습니다.")));
    }

    public record AdminVerifyRequest(String pin) {
    }

    public record SiteSummary(Long id, String code, String name) {
    }

    @PostMapping("/admin/verify")
    public ResponseEntity<?> verifyAdmin(@RequestBody AdminVerifyRequest request) {
        if (request.pin() == null || !passwordEncoder.matches(request.pin(), adminPinHash)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "PIN이 올바르지 않습니다."));
        }

        List<SiteSummary> sites = siteRepository.findAll().stream()
                .sorted(Comparator.comparing(Site::getId))
                .map(site -> new SiteSummary(site.getId(), site.getCode(), site.getName()))
                .toList();

        return ResponseEntity.ok(Map.of(
                "token", tokenService.issueToken(Role.ADMIN, null),
                "role", "ADMIN",
                "sites", sites));
    }
}
