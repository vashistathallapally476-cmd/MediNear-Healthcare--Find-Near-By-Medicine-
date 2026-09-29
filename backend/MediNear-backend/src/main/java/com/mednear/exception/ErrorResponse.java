package com.mednear.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private int                 status;
    private String              error;
    private String              message;
    private Map<String, String> fieldErrors;   // populated on validation failure
    private LocalDateTime       timestamp;

    public ErrorResponse(int status, String error, String message) {
        this.status    = status;
        this.error     = error;
        this.message   = message;
        this.timestamp = LocalDateTime.now();
    }

    public int                 getStatus()      { return status; }
    public String              getError()       { return error; }
    public String              getMessage()     { return message; }
    public Map<String, String> getFieldErrors() { return fieldErrors; }
    public void setFieldErrors(Map<String, String> v) { this.fieldErrors = v; }
    public LocalDateTime       getTimestamp()   { return timestamp; }
}
