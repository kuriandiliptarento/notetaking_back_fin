package com.tarento.notesapp.repository;

import com.tarento.notesapp.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByFolder_Id(Long folderId);

    // find all notes for a user (via folder -> user relation)
    List<Note> findByFolder_User_Id(Long userId);

    List<Note> findFullByFolder_User_Id(Long userId);

    List<Note> findByNoteTags_Tag_IdAndFolder_User_Id(Long tagId, Long userId);

    /* ---------- Multi-tag queries (new) ---------- */

    // OR mode: notes having ANY of the tag IDs
    @Query("""
        SELECT DISTINCT n
        FROM Note n
        JOIN n.noteTags nt
        JOIN nt.tag t
        WHERE n.folder.user.id = :userId
          AND t.id IN :tagIds
        """)
    List<Note> findNotesByAnyTag(
            @Param("userId") Long userId,
            @Param("tagIds") List<Long> tagIds
    );

    // AND mode: notes having ALL tag IDs (group + having)
    @Query("""
        SELECT n
        FROM Note n
        JOIN n.noteTags nt
        JOIN nt.tag t
        WHERE n.folder.user.id = :userId
          AND t.id IN :tagIds
        GROUP BY n.id
        HAVING COUNT(DISTINCT t.id) = :tagCount
        """)
    List<Note> findNotesByAllTags(
            @Param("userId") Long userId,
            @Param("tagIds") List<Long> tagIds,
            @Param("tagCount") long tagCount
    );
}
