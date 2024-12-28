package com.explorebnb.clone.airBnbApp.advice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
public class ApiResponse<T> {
    private T data;
    private ApiError error;
    private LocalDateTime timeStamp;

    public ApiResponse(){
        this.timeStamp=LocalDateTime.now();
    }

    public ApiResponse(T data){
        this();
        this.data=data;
    }
    public ApiResponse(ApiError error){
        this();
        this.error=error;
    }
}
