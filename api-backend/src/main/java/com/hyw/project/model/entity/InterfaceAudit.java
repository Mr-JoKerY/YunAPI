package com.hyw.project.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 接口上传申请
 * @TableName interface_audit
 */
@TableName(value ="interface_audit")
@Data
public class InterfaceAudit implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
     * 审批状态【0-待审核，1-已审核】
     */
    private Integer auditStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除(0-未删, 1-已删)
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}