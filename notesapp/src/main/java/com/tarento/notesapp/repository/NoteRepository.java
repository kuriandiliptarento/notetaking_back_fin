package com.tarento.notesapp.repository;

import com.tarento.notesapp.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByFolder_Id(Long folderId);

    // find all notes for a user (via folder -> user relation)
    List<Note> findByFolder_User_Id(Long userId);

    List<Note> findByNoteTags_Tag_IdAndFolder_User_Id(Long tagId, Long userId);
}
