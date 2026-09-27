package com.example.punch.service;

import com.example.punch.dto.EncryptedRequest;
import com.example.punch.dto.PunchRequest;
import com.example.punch.entity.Employee;
import com.example.punch.entity.PunchRecord;
import com.example.punch.repository.EmployeeRepository;
import com.example.punch.repository.PunchRecordRepository;
import com.example.punch.util.RsaCryptoUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PunchServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PunchRecordRepository punchRecordRepository;

    @Mock
    private RsaCryptoUtil rsaCryptoUtil;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PunchService punchService;

    private EncryptedRequest encryptedRequest;
    private PunchRequest punchRequest;
    private Employee employee;

    @BeforeEach
    void setUp() {
        encryptedRequest = new EncryptedRequest();
        encryptedRequest.setData("encrypted-data");

        punchRequest = new PunchRequest();
        punchRequest.setEmployeeId("12345678");
        punchRequest.setPunchType("1");

        employee = new Employee();
        employee.setEmployeeId("12345678");
        employee.setEmployeeName("Test User");
    }

    @Test
    void processPunch_Success() throws Exception {
        // Arrange
        when(rsaCryptoUtil.decrypt(anyString())).thenReturn("decrypted-json");
        when(objectMapper.readValue("decrypted-json", PunchRequest.class)).thenReturn(punchRequest);
        when(employeeRepository.findByEmployeeId("12345678")).thenReturn(Optional.of(employee));
        
        PunchRecord savedRecord = new PunchRecord();
        savedRecord.setEmployeeId("12345678");
        savedRecord.setPunchType(1L);
        when(punchRecordRepository.save(any(PunchRecord.class))).thenReturn(savedRecord);

        // Act
        PunchRecord result = punchService.processPunch(encryptedRequest);

        // Assert
        assertNotNull(result);
        assertEquals("12345678", result.getEmployeeId());
        assertEquals(1L, result.getPunchType());
        verify(punchRecordRepository, times(1)).save(any(PunchRecord.class));
    }

    @Test
    void processPunch_UnknownEmployee_ThrowsException() throws Exception {
        // Arrange
        when(rsaCryptoUtil.decrypt(anyString())).thenReturn("decrypted-json");
        when(objectMapper.readValue("decrypted-json", PunchRequest.class)).thenReturn(punchRequest);
        when(employeeRepository.findByEmployeeId("12345678")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            punchService.processPunch(encryptedRequest);
        });
        assertEquals("001", exception.getMessage());
    }
}
