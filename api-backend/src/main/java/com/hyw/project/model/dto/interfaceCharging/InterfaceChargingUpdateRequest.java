package com.hyw.project.model.dto.interfaceCharging;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求
 *
 * @author hyw
 */
@Data
public class InterfaceChargingUpdateRequest implements Serializable {

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

    private static final long serialVersionUID = 1L;
}