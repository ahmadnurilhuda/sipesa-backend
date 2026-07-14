package com.pomosda.permission.parent;

import com.pomosda.permission.common.BaseEntity;
import com.pomosda.permission.user.User;
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
@Table(name = "parent_guardians")
public class ParentGuardian extends BaseEntity {
    private String name;
    private String phone;
    private String address;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
