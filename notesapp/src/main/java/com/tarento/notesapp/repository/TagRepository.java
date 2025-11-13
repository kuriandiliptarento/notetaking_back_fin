package com.tarento.notesapp.repository;

import com.tarento.notesapp.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    // all tags for a user
    List<Tag> findByUser_Id(Long userId);

    // uniqueness checks
    boolean existsByUser_IdAndName(Long userId, String name);
}
