package org.example.thuvienso.Module;

import jakarta.persistence.*;
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
@Table(
        name = "favorite",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_favorite_account_book",
                columnNames = {"idAccount", "idBook"}
        )
)
public class FavoriteEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "idFavorite", columnDefinition = "VARCHAR(36) COMMENT 'Id sách yêu thích'")
    String idFavorite;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idBook", nullable = false)
    BookEntity book;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idAccount", nullable = false)
    AccountEntity account;
}