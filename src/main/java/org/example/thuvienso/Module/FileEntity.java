package org.example.thuvienso.Module;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.example.thuvienso.Enum.StatusDocument;
import org.example.thuvienso.Enum.TypeFile;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "idFile",columnDefinition = "VARCHAR(36) COMMENT 'Id của tập tin'")
    String idFile;

    @Column(name = "fileName",columnDefinition = "VARCHAR(256) COMMENT 'tên tập tin'")
    String fileName;

    @Column(name = "partFile",columnDefinition = "VARCHAR(256) COMMENT 'Đường dẫn tập tin'")
    String partFile;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) COMMENT 'loại file'", nullable = false)
    @NotNull(message = "Loại file không được null")
    TypeFile typeFile;

    @Column(name = "thumbnail",columnDefinition = "VARCHAR(256) COMMENT 'Tập tin thu nhỏ'")
    String thumbnail;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idDocument")
    DocumentEntity documentEntity;
}
