package org.example.thuvienso.Dto.Response.Statistic;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CountByKeyResponse {
    String key;
    long value;
}