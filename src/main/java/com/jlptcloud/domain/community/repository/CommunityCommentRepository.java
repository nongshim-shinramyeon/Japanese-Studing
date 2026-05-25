package com.jlptcloud.domain.community.repository;

import com.jlptcloud.domain.community.entity.CommunityComment;
import com.jlptcloud.domain.community.entity.CommunityPost;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

    Page<CommunityComment> findByPost(CommunityPost post, Pageable pageable);

    List<CommunityComment> findByParent(CommunityComment parent);

    void deleteByPost(CommunityPost post);
}
