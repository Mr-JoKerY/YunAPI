package com.hyw.project.model.dto.interfaceAudit;

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
public class InterfaceAuditQueryRequest extends PageRequest implements Serializable {
    
    private static final long serialVersionUID = 4489941418648280203L;

    /**
     * 接口ID
     */
    private Long interfaceId;

    /**
     * 申请人ID
     */
    private Long userId;
    
    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 审批状态
     */
    private Integer auditStatus;

}