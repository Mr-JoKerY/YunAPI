package com.hyw.apiinterface.entity;

import lombok.Data;

@Data
public class CommonResponse<T> {

    private String code;

    private String msg;

    private T data;
}
