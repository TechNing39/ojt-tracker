package com.ojttracker.auth;

public enum SiteCode {
    JUNGGYE("중계"),
    SANGBONG("상봉"),
    BANGHAK("방학"),
    SUYU("수유"),
    MIA("미아");

    private final String koreanName;

    SiteCode(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }
}
