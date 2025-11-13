package com.tarento.notesapp.repository;

import com.tarento.notesapp.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    // Find the special root folder for a user
    Optional<Folder> findByUser_IdAndRootTrue(Long userId);

    // Find children of a given parent folder id
    List<Folder> findByParentFolder_Id(Long parentFolderId);

    // Find top-level folders for a user (children of the user's root)
    List<Folder> findByUser_IdAndParentFolder_Id(Long userId, Long parentFolderId);

    // Uniqueness checks (use parentFolder id)
    boolean existsByUser_IdAndNameAndParentFolderIsNull(Long userId, String name); // legacy; discouraged
    boolean existsByUser_IdAndNameAndParentFolder_Id(Long userId, String name, Long parentFolderId);
}
