package com.hyw.apicommon.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 短信服务传输对象
 *
 * @author hyw
 */
@Data
public class SendSmsRequest implements Serializable {

    private static final long serialVersionUID = 8504215015474691352L;

    String phoneNum;

    String code;
}
