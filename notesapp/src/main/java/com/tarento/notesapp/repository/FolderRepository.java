package com.tarento.notesapp.repository;

import com.tarento.notesapp.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    // root folders (parent is NULL) for a user
    List<Folder> findByUserIdAndParentFolderIsNull(Long userId);

    // children of a parent folder
    List<Folder> findByParentFolder_Id(Long parentFolderId);

    // uniqueness checks
    boolean existsByUserIdAndNameAndParentFolderIsNull(Long userId, String name);

    boolean existsByUserIdAndNameAndParentFolder_Id(Long userId, String name, Long parentFolderId);
}
