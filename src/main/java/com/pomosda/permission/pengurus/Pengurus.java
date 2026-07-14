package com.pomosda.permission.pengurus;

import com.pomosda.permission.common.BaseEntity;
import com.pomosda.permission.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pengurus")
public class Pengurus extends BaseEntity {
    @Column(nullable = false)
    private String name;

    private String nip;

    @Column(unique = true)
    private String phone;

    @Column(nullable = false)
    private String position;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
