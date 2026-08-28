package org.example.thuvienso.Module;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(
        name = "reading_history",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reading_account_book",
                columnNames = {"idAccount", "idBook"}
        )
)
public class ReadingHistoryEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "idReadingHistory", columnDefinition = "VARCHAR(36) COMMENT 'Id lịch sử đọc'")
    String idReadingHistory;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idBook", nullable = false)
    BookEntity book;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idAccount", nullable = false)
    AccountEntity account;

    @Column(name = "lastReadAt", columnDefinition = "DATETIME COMMENT 'Lần đọc gần nhất'")
    LocalDateTime lastReadAt;
}