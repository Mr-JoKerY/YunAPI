package com.hyw.apicommon.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 接口库存锁定请求
 * @author hyw
 * @create 2023-05-04 13:49
 */
@Data
public class LockAvailablePiecesRequest implements Serializable {
    
    private static final long serialVersionUID = 1354230288973784689L;

    /**
     * 计费ID
     */
    private Long chargingId;

    /**
     * 锁定次数
     */
    private Long count;
}
