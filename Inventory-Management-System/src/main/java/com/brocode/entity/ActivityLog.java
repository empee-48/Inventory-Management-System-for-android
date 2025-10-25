package com.brocode.entity;

import com.brocode.utils.Activity;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class ActivityLog extends BaseEntity {
    private Activity activity;
    private String description;
}
