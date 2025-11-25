package com.tarento.notesapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Table(name = "note_tags",
       uniqueConstraints = {@UniqueConstraint(name = "ux_note_tags_note_tag", columnNames = {"note_id", "tag_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"note", "tag"})
public class NoteTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many note-tags belong to one note
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false, foreignKey = @ForeignKey(name = "fk_note_tags_note"))
    private Note note;

    // Many note-tags belong to one tag
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false, foreignKey = @ForeignKey(name = "fk_note_tags_tag"))
    private Tag tag;

    // equals/hashCode: use surrogate id when present; otherwise fall back to natural key (noteId, tagId)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoteTag)) return false;
        NoteTag other = (NoteTag) o;
        if (this.id != null && other.id != null) {
            return Objects.equals(this.id, other.id);
        }
        Long nid = this.note == null ? null : this.note.getId();
        Long tid = this.tag == null ? null : this.tag.getId();
        Long onid = other.note == null ? null : other.note.getId();
        Long otid = other.tag == null ? null : other.tag.getId();
        return Objects.equals(nid, onid) && Objects.equals(tid, otid);
    }

    @Override
    public int hashCode() {
        if (this.id != null) return this.id.hashCode();
        Long nid = this.note == null ? null : this.note.getId();
        Long tid = this.tag == null ? null : this.tag.getId();
        return Objects.hash(nid, tid);
    }
}
