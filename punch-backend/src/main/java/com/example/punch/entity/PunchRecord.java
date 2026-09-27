package com.example.punch.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "punch_record")
public class PunchRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "employee_id", nullable = false, length = 8)
    private String employeeId;
    @Column(name = "punch_type", nullable = false)
    private Long punchType;
    @Column(name = "punch_time", nullable = false)
    private LocalDateTime punchTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public Long getPunchType() { return punchType; }
    public void setPunchType(Long punchType) { this.punchType = punchType; }
    public LocalDateTime getPunchTime() { return punchTime; }
    public void setPunchTime(LocalDateTime punchTime) { this.punchTime = punchTime; }
}
