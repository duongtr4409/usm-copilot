package com.usm.ams.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "enrollments")
public class Enrollment {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "class_unit_id", nullable = false)
    private UUID classUnitId;

    @ManyToOne
    @JoinColumn(name = "student_profile_id", nullable = false)
    private StudentProfile studentProfile;

    @Column(nullable = false)
    private String status = "ENROLLED";

    @Column(name = "enrolled_at", nullable = false)
    private OffsetDateTime enrolledAt = OffsetDateTime.now();

    public Enrollment() {}

    public Enrollment(UUID classUnitId, StudentProfile studentProfile) {
        this.classUnitId = classUnitId;
        this.studentProfile = studentProfile;
        this.enrolledAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getClassUnitId() { return classUnitId; }
    public void setClassUnitId(UUID classUnitId) { this.classUnitId = classUnitId; }

    public StudentProfile getStudentProfile() { return studentProfile; }
    public void setStudentProfile(StudentProfile studentProfile) { this.studentProfile = studentProfile; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(OffsetDateTime enrolledAt) { this.enrolledAt = enrolledAt; }
}
