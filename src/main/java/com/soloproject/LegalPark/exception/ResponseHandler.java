package com.soloproject.LegalPark.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseHandler {
    private Integer code;
    private HttpStatus status;
    private String message;
    private Object data;
    private Object error;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }



    
    public static ResponseEntity<Object> generateResponseSuccess(Object responseObj) {
        var data = new ResponseHandler();
        data.setCode(HttpStatus.OK.value());
        data.setData(responseObj);
        data.setMessage("success");
        data.setStatus(HttpStatus.OK);

        return ResponseEntity.status(HttpStatus.OK).body(data);
    }



    public static ResponseEntity<Object> generateResponseSuccess(HttpStatus status, String message, Object data) {
        var response = new ResponseHandler();
        response.setCode(status.value());
        response.setStatus(status);
        response.setMessage(message);
        response.setData(data);
        response.setError(null);

        return ResponseEntity.status(status).body(response);
    }



    public static ResponseEntity<Object> generateResponseError(HttpStatus status,Object error,String message) {
        var data = new ResponseHandler();
        data.setCode(status.value());
        data.setStatus(status);
        data.setMessage(message);
        data.setError(error);

        return ResponseEntity.status(status).body(data);
    }
}
