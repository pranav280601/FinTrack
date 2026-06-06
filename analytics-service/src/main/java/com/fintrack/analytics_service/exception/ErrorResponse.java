package com.fintrack.analytics_service.exception;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
    
    public int status;
    public String message;
    public String details;
    public LocalDateTime timestamp;

}
