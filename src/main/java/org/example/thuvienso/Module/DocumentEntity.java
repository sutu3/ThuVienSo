package org.example.thuvienso.Module;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.example.thuvienso.Enum.StatusDocument;
import org.example.thuvienso.Enum.TypeDocument;

import java.util.List;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "idDocument",columnDefinition = "VARCHAR(36) COMMENT 'Id của tài liệu'")
    String idDocument;

    @Column(name = "content",columnDefinition = "TEXT COMMENT 'nội dung tài liệu'")
    String content;

    @Column(name = "title",columnDefinition = "VARCHAR(256) COMMENT 'tiêu đề tài liệu'")
    String title;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) COMMENT 'Trạng thái của tài liệu'", nullable = false)
    @NotNull(message = "Trạng thái không được null")
    StatusDocument status;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) COMMENT 'Loại của tài liệu'", nullable = false)
    @NotNull(message = "Loại tài liệu không được null")
    TypeDocument typeDocument;

    @Column(name = "thumbnail",columnDefinition = "VARCHAR(256) COMMENT 'tiêu đề thu nhỏ'")
    String thumbnail;
    @ManyToOne
    @JoinColumn(name = "idCategory",nullable = false)
    CategoryEntity categoryEntity;
    @ManyToMany(mappedBy = "documents")
    List<CollectionEntity> collections;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idFolder")
    FolderEntity folderEntity;

    @OneToMany(mappedBy = "documentEntity")
    List<FileEntity> fileEntity;
}
