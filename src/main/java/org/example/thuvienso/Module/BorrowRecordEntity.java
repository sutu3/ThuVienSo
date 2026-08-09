package org.example.thuvienso.Module;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.example.thuvienso.Enum.BorrowStatus;

import java.time.LocalDate;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BorrowRecordEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "idBorrow", columnDefinition = "VARCHAR(36) COMMENT 'Id phiếu mượn'")
    String idBorrow;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idBook", nullable = false)
    BookEntity book;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idAccount", nullable = false)
    AccountEntity account;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) COMMENT 'Trạng thái phiếu mượn'", nullable = false)
    @NotNull(message = "INVALID_KEY")
    BorrowStatus status;

    @Column(name = "borrowDate", columnDefinition = "DATE COMMENT 'Ngày mượn thực tế'")
    LocalDate borrowDate;

    @Column(name = "dueDate", columnDefinition = "DATE COMMENT 'Ngày hẹn trả'")
    LocalDate dueDate;

    @Column(name = "returnDate", columnDefinition = "DATE COMMENT 'Ngày trả thực tế'")
    LocalDate returnDate;

    @Column(name = "note", columnDefinition = "VARCHAR(512) COMMENT 'Ghi chú'")
    String note;
}