package org.example.thuvienso.Module;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.example.thuvienso.Enum.TypeCollection;
import org.example.thuvienso.Enum.TypeDocument;

import java.util.List;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CollectionEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "idCollection",columnDefinition = "VARCHAR(36) COMMENT 'Id của bộ sưu tập'")
    String idCollection;

    @Column(name = "collectionName",columnDefinition = "VARCHAR(256) COMMENT 'tên bộ sưu tập'")
    String collectionName;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) COMMENT 'Loại của bộ sưu tập'", nullable = false)
    @NotNull(message = "Loại bộ sưu tập không được null")
    TypeCollection typeCollection;

    @ManyToMany
    @JoinTable(
            name = "collection_document",

            joinColumns = @JoinColumn(
                    name = "idCollection"
            ),

            inverseJoinColumns = @JoinColumn(
                    name = "idDocument"
            )
    )
    List<DocumentEntity> documents;
}
