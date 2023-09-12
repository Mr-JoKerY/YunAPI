package com.hyw.project.model.dto.interfaceCharging;

import com.hyw.apicommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
 * @author hyw
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InterfaceChargingQueryRequest extends PageRequest implements Serializable {

    /**
     * 主键
     */
    private Long id;

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

    /**
     * 创建人
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}