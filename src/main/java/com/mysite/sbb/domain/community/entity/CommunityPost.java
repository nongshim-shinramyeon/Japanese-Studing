package com.mysite.sbb.domain.community.entity;

import com.mysite.sbb.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class CommunityPost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String authorName;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 3000)
    private String content;

    @Column(nullable = false, length = 120, updatable = false)
    private String ownerKey;

    protected CommunityPost() {
    }

    public CommunityPost(String authorName, String title, String content, String ownerKey) {
        this.authorName = authorName;
        this.title = title;
        this.content = content;
        this.ownerKey = ownerKey;
    }

    public void update(String authorName, String title, String content) {
        this.authorName = authorName;
        this.title = title;
        this.content = content;
    }

    public boolean isOwnedBy(String ownerKey) {
        return this.ownerKey.equals(ownerKey);
    }

    public Long getId() {
        return id;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getOwnerKey() {
        return ownerKey;
    }
}
