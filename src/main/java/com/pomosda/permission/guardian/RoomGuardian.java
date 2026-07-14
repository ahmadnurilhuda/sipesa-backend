package com.pomosda.permission.guardian;

import com.pomosda.permission.academicyear.AcademicYear;
import com.pomosda.permission.common.BaseEntity;
import com.pomosda.permission.pengurus.Pengurus;
import com.pomosda.permission.room.Room;
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
@Table(name = "room_guardians")
public class RoomGuardian extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pengurus_id", nullable = false)
    private Pengurus pengurus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;
}
