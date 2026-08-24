package org.example.thuvienso.Module;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "idBook", columnDefinition = "VARCHAR(36) COMMENT 'Id sách vật lý'")
    String idBook;

    @Column(name = "bookCode", columnDefinition = "VARCHAR(64) COMMENT 'Mã sách nội bộ / mã vạch'", unique = true)
    String bookCode;

    @Column(name = "title", columnDefinition = "VARCHAR(256) COMMENT 'Tên sách'")
    @NotNull(message = "INVALID_KEY")
    String title;

    @Column(name = "description", columnDefinition = "VARCHAR(256) COMMENT 'Mô tả sách'")
    String description;

    @Column(name = "author", columnDefinition = "VARCHAR(256) COMMENT 'Tác giả'")
    String author;

    @Column(name = "publisher", columnDefinition = "VARCHAR(256) COMMENT 'Nhà xuất bản'")
    String publisher;

    @Column(name = "publishYear", columnDefinition = "INT COMMENT 'Năm xuất bản'")
    Integer publishYear;

    @Column(name = "shelfLocation", columnDefinition = "VARCHAR(64) COMMENT 'Vị trí kệ sách'")
    String shelfLocation;

    @Column(name = "totalCopies", columnDefinition = "INT DEFAULT 0 COMMENT 'Tổng số bản in'")
    Integer totalCopies;

    @Column(name = "availableCopies", columnDefinition = "INT DEFAULT 0 COMMENT 'Số bản còn có thể mượn'")
    Integer availableCopies;

    @Column(name = "thumbnail", columnDefinition = "VARCHAR(256) COMMENT 'Ảnh bìa sách'")
    String thumbnail;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idCategory")
    CategoryEntity categoryEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idDocument")
    DocumentEntity documentEntity;
}