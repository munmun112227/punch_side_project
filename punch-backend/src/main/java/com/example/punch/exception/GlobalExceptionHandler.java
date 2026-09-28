package com.example.punch.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        String code = ex.getMessage();
        log.error("Catching RuntimeException with code: {}", code);
        Map<String, Object> body = new HashMap<>();
        HttpStatus status;
        String message;

        switch (code) {
            case "001":
                status = HttpStatus.BAD_REQUEST;
                message = "未知的員編；錯誤碼: 001";
                break;
            case "003":
                status = HttpStatus.BAD_REQUEST;
                message = "目前查無打卡資料；錯誤碼: 003";
                break;
            case "801":
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                message = "打卡需求產生例外狀況，請通知系統管理員；錯誤碼: 801";
                break;
            case "901":
            default:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                message = "打卡需求產生例外狀況，請通知系統管理員；錯誤碼: 901";
                break;
        }

        body.put("error", message);
        return new ResponseEntity<>(body, status);
    }
}
