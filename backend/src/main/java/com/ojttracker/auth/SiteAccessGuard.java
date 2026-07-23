package com.ojttracker.auth;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class SiteAccessGuard {

    private final SiteRepository siteRepository;

    public SiteAccessGuard(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    /**
     * 목록 조회/생성처럼 siteId를 쿼리 파라미터로 받는 엔드포인트에서 사용.
     * ADMIN은 siteId를 반드시 지정해야 하고, SITE는 자기 소속 사이트만 조회할 수 있다.
     */
    public Long resolveSiteId(TokenPrincipal principal, Long requestedSiteId) {
        if (principal.isAdmin()) {
            if (requestedSiteId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "siteId는 필수입니다.");
            }
            if (!siteRepository.existsById(requestedSiteId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 사이트입니다.");
            }
            return requestedSiteId;
        }

        if (requestedSiteId != null && !requestedSiteId.equals(principal.siteId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "다른 사이트에 접근할 수 없습니다.");
        }
        return principal.siteId();
    }
}
