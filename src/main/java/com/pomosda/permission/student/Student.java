package com.pomosda.permission.student;

import com.pomosda.permission.common.BaseEntity;
import com.pomosda.permission.parent.ParentGuardian;
import com.pomosda.permission.room.Room;
import com.pomosda.permission.schoolclass.SchoolClass;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "students")
public class Student extends BaseEntity {
    private String nis;
    private String name;

    @Enumerated(EnumType.STRING)
    private StudentGender gender;

    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_guardian_id")
    private ParentGuardian parentGuardian;
}
