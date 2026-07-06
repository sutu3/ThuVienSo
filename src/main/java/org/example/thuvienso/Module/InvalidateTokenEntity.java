package org.example.thuvienso.Module;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvalidateTokenEntity
{
    @Id
    String id;
    @Column(columnDefinition = "DATE  COMMENT 'thời gian hết hạn'",nullable = false)
    Date expiryTime;
}
