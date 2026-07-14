package com.pomosda.permission.academicyear;

import com.pomosda.permission.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "academic_years")
public class AcademicYear extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String period;

    @Column(nullable = false)
    private boolean active;
}
