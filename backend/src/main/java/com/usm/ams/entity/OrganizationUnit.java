package com.usm.ams.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.util.UUID;

@Entity
@Table(name = "organization_unit")
public class OrganizationUnit {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String type;

    @Column
    private String code;

    @Column
    private String title;

    public OrganizationUnit() {}

    public OrganizationUnit(String type, String code, String title) {
        this.type = type;
        this.code = code;
        this.title = title;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
