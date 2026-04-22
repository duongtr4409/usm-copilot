package com.usm.ams.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "staff")
public class Staff {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "uuid")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "account_id")
    private UserAccount account;

    @Column(name = "staff_number")
    private String staffNumber;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column
    private String position;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private OrganizationUnit unit;

    @Column(columnDefinition = "jsonb")
    private String contact;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Staff() {}

    public Staff(String staffNumber, String fullName, String position, OrganizationUnit unit, String contact) {
        this.staffNumber = staffNumber;
        this.fullName = fullName;
        this.position = position;
        this.unit = unit;
        this.contact = contact;
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UserAccount getAccount() { return account; }
    public void setAccount(UserAccount account) { this.account = account; }

    public String getStaffNumber() { return staffNumber; }
    public void setStaffNumber(String staffNumber) { this.staffNumber = staffNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public OrganizationUnit getUnit() { return unit; }
    public void setUnit(OrganizationUnit unit) { this.unit = unit; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
