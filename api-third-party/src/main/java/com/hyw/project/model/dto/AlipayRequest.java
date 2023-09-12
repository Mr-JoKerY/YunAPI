package com.hyw.project.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author hyw
 */
@Data
public class AlipayRequest implements Serializable {

    private static final long serialVersionUID = -8597630489529830444L;

    private String outTradeNo;
    private String subject;
    private double totalAmount;
}
