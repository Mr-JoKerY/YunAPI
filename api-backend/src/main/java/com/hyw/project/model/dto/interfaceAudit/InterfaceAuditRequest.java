package com.hyw.project.model.dto.interfaceAudit;

import lombok.Data;

import java.io.Serializable;

/**
 * 审批请求
 *
 * @author hyw
 */
@Data
public class InterfaceAuditRequest implements Serializable {

    private static final long serialVersionUID = 4489941418648280203L;

    /**
     * 接口审批 id
     */
    private Long interfaceAuditId;

    /**
     * 备注
     */
    private String remark;

}