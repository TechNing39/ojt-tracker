package com.ojttracker.checklist;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
public class ChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    // MySQL(TiDB)에서 Hibernate 기본값인 native ENUM 컬럼 대신 varchar로 저장 — ddl-auto=update는 ENUM 값 목록을 갱신하지 않아 카테고리 추가 시 저장이 실패하기 때문
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private Category category;

    private Instant createdAt;

    private Integer sortOrder;

    private Long siteId;

    protected ChecklistItem() {
    }

    public ChecklistItem(String title, Category category, Long siteId) {
        this.title = title;
        this.category = category;
        this.siteId = siteId;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getSiteId() {
        return siteId;
    }

    public String getTitle() {
        return title;
    }

    public Category getCategory() {
        return category;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
