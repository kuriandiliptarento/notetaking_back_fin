package com.tarento.notesapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"folder", "noteTags"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String content;  // <--- Renamed

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_notes_folder"))
    private Folder folder;

    // @ManyToMany(fetch = FetchType.LAZY)
    // @JoinTable(
    //         name = "note_tags",
    //         joinColumns = @JoinColumn(name = "note_id",
    //                 foreignKey = @ForeignKey(name = "fk_note_tags_note")),
    //         inverseJoinColumns = @JoinColumn(name = "tag_id",
    //                 foreignKey = @ForeignKey(name = "fk_note_tags_tag"))
    // )
    // private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NoteTag> noteTags = new HashSet<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public void addTag(Tag tag) {
        NoteTag nt = new NoteTag();
        nt.setNote(this);
        nt.setTag(tag);
        this.noteTags.add(nt);
        tag.getNoteTags().add(nt);
    }

    public void removeTag(Tag tag) {
        this.noteTags.removeIf(nt -> {
            if (nt.getTag().equals(tag)) {
                tag.getNoteTags().remove(nt);
                nt.setNote(null);
                nt.setTag(null);
                return true;
            }
            return false;
        });
    }
}
