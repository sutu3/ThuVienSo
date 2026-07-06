package org.example.thuvienso.Module;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "idCategory",columnDefinition = "VARCHAR(36) COMMENT 'Id của thể loại'")
    String idCategory;

    @Column(name = "categoryName",columnDefinition = "VARCHAR(256) COMMENT 'tên thể loại'")
    String categoryName;
    @OneToMany(mappedBy="categoryEntity")
    List<DocumentEntity> documentEntity;

}
