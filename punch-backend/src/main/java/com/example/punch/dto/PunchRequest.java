package com.example.punch.dto;

public class PunchRequest {
    private String employeeId;
    private String punchType;
    
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getPunchType() { return punchType; }
    public void setPunchType(String punchType) { this.punchType = punchType; }
}
