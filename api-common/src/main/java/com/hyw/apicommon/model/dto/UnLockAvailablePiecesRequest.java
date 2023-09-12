package com.hyw.apicommon.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 接口库存解锁请求
 * @author hyw
 * @create 2023-05-04 13:49
 */
@Data
public class UnLockAvailablePiecesRequest implements Serializable {
    
    private static final long serialVersionUID = 1354230288973784689L;

    /**
     * 接口ID
     */
    private Long interfaceId;

    /**
     * 锁定次数
     */
    private Long count;
}
