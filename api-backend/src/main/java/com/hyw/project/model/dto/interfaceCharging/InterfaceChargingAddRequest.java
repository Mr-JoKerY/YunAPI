package com.hyw.project.model.dto.interfaceCharging;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 */
@Data
public class InterfaceChargingAddRequest implements Serializable {

    /**
     * 接口id
     */
    private Long interfaceId;

    /**
     * 计费规则（元/条）
     */
    private Double charging;

    /**
     * 接口剩余可调用次数
     */
    private String availablePieces;

    private static final long serialVersionUID = 1L;
}