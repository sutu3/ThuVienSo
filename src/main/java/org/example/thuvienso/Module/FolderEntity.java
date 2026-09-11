package org.example.thuvienso.Module;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.example.thuvienso.Enum.FolderVisibility;

import java.util.List;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FolderEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "idFolder",columnDefinition = "VARCHAR(36) COMMENT 'Id của thư mục'")
    String idFolder;

    @Column(name = "folderName",columnDefinition = "VARCHAR(256) COMMENT 'tên thư mục'")
    String folderName;

    @Column(name = "description",columnDefinition = "VARCHAR(256) COMMENT 'mô tả'")
    String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parentFolderId")
    FolderEntity parentFolder;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", columnDefinition = "VARCHAR(20) DEFAULT 'PUBLIC' COMMENT 'Phạm vi thư mục'")
    FolderVisibility visibility;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idOwner")   // null = thư mục public dùng chung
    AccountEntity owner;

    @OneToMany(mappedBy = "parentFolder")
    List<FolderEntity> childFolder;

    @OneToMany(mappedBy = "folderEntity")
    List<DocumentEntity> documentEntity;
}
