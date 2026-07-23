package com.ojttracker.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 기동 시마다 5개 지점을 env var의 PIN으로 upsert한다.
 * PIN을 바꾸려면 env var를 바꾸고 재배포만 하면 되므로 별도 관리 화면이 필요 없다.
 */
@Component
@Order(1)
public class SiteSeeder implements ApplicationRunner {

    private final SiteRepository siteRepository;
    private final PasswordEncoder passwordEncoder;
    private final String junggyePin;
    private final String sangbongPin;
    private final String banghakPin;
    private final String suyuPin;
    private final String miaPin;

    public SiteSeeder(
            SiteRepository siteRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.auth.site-pin.junggye}") String junggyePin,
            @Value("${app.auth.site-pin.sangbong}") String sangbongPin,
            @Value("${app.auth.site-pin.banghak}") String banghakPin,
            @Value("${app.auth.site-pin.suyu}") String suyuPin,
            @Value("${app.auth.site-pin.mia}") String miaPin) {
        this.siteRepository = siteRepository;
        this.passwordEncoder = passwordEncoder;
        this.junggyePin = junggyePin;
        this.sangbongPin = sangbongPin;
        this.banghakPin = banghakPin;
        this.suyuPin = suyuPin;
        this.miaPin = miaPin;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed(SiteCode.JUNGGYE, junggyePin);
        seed(SiteCode.SANGBONG, sangbongPin);
        seed(SiteCode.BANGHAK, banghakPin);
        seed(SiteCode.SUYU, suyuPin);
        seed(SiteCode.MIA, miaPin);
    }

    private void seed(SiteCode code, String pin) {
        String pinHash = passwordEncoder.encode(pin);
        siteRepository
                .findByCode(code.name())
                .map(site -> {
                    site.setName(code.getKoreanName());
                    site.setPinHash(pinHash);
                    return siteRepository.save(site);
                })
                .orElseGet(() -> siteRepository.save(new Site(code.name(), code.getKoreanName(), pinHash)));
    }
}
