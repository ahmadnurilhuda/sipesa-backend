package com.pomosda.permission.schoolclass;

import com.pomosda.permission.academicyear.AcademicYear;
import com.pomosda.permission.common.BaseEntity;
import com.pomosda.permission.user.User;
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
@Table(name = "school_classes")
public class SchoolClass extends BaseEntity {
    private String name;
    private String schoolYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homeroom_teacher_id")
    private User homeroomTeacher;
}
