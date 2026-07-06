package org.example.thuvienso.Dto.Request;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.example.thuvienso.Enum.TypeCollection;
import org.example.thuvienso.Module.BaseEntity;
import org.example.thuvienso.Module.DocumentEntity;

import java.util.List;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CollectionRequest {

    String collectionName;

    String typeCollection;

}
