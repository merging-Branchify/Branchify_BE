package com.merging.branchify.dto;

import lombok.Getter;

@Getter
public class ResponseDto<T> {
    private T data;
    private String message;
    private int status;

    public ResponseDto() {}

    // 생성자
    public ResponseDto(T data, String message, int status) {
        this.data = data;
        this.message = message;
        this.status = status;
    }

    public static <T> ResponseDto<T> success(T data) {
        return new ResponseDto<>(data, "success", 200);
    }


    public void setData(T data) {
        this.data = data;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}