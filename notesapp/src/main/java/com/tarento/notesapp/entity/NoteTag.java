package com.tarento.notesapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "note_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NoteTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // Many note-tags belong to one note
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false, foreignKey = @ForeignKey(name = "fk_note_tags_note"))
    @OnDelete(action = OnDeleteAction.CASCADE)   // instructs Hibernate to add ON DELETE CASCADE on FK
    private Note note;

    // Many note-tags belong to one tag
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false, foreignKey = @ForeignKey(name = "fk_note_tags_tag"))
    @OnDelete(action = OnDeleteAction.CASCADE)   // instructs Hibernate to add ON DELETE CASCADE on FK
    private Tag tag;
}
