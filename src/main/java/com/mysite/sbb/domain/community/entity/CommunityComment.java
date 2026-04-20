package com.mysite.sbb.domain.community.entity;

import com.mysite.sbb.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class CommunityComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private CommunityPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CommunityComment parent;

    @Column(nullable = false, length = 80)
    private String authorName;

    @Column(nullable = false, length = 1200)
    private String content;

    protected CommunityComment() {
    }

    public CommunityComment(CommunityPost post, CommunityComment parent, String authorName, String content) {
        this.post = post;
        this.parent = parent;
        this.authorName = authorName;
        this.content = content;
    }

    public void update(String authorName, String content) {
        this.authorName = authorName;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public CommunityPost getPost() {
        return post;
    }

    public CommunityComment getParent() {
        return parent;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getContent() {
        return content;
    }
}
