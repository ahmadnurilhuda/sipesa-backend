package com.pomosda.permission.guardian;

import com.pomosda.permission.academicyear.AcademicYear;
import com.pomosda.permission.common.BaseEntity;
import com.pomosda.permission.pengurus.Pengurus;
import com.pomosda.permission.schoolclass.SchoolClass;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "class_guardians")
public class ClassGuardian extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pengurus_id", nullable = false)
    private Pengurus pengurus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;
}
