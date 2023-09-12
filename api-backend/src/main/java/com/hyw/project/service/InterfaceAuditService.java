package com.hyw.project.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hyw.apicommon.model.entity.User;
import com.hyw.project.model.dto.interfaceAudit.InterfaceAuditQueryRequest;
import com.hyw.project.model.dto.interfaceInfo.InterfaceInfoAddRequest;
import com.hyw.project.model.entity.InterfaceAudit;
import com.hyw.project.model.vo.InterfaceAuditVO;

/**
* @author hyw
* @description 针对表【interface_audit】的数据库操作Service
* @createDate 2023-06-12 22:26:29
*/
public interface InterfaceAuditService extends IService<InterfaceAudit> {

    /**
     * 添加接口审批
     * @param interfaceInfoAddRequest
     * @param loginUser
     * @return
     */
    Long addInterfaceAudit(InterfaceInfoAddRequest interfaceInfoAddRequest, User loginUser);

    /**
     * 审核接口通过
     * @param interfaceAuditId
     * @param remark
     * @param loginUser
     * @return
     */
    boolean auditInterface(Long interfaceAuditId, String remark, User loginUser);

    /**
     * 获取查询条件
     *
     * @param interfaceAuditQueryRequest
     * @return
     */
    QueryWrapper<InterfaceAudit> getQueryWrapper(InterfaceAuditQueryRequest interfaceAuditQueryRequest);

    /**
     * 分页获取接口审核封装
     *
     * @param auditPage
     * @return
     */
    Page<InterfaceAuditVO> getInterfaceAuditVOPage(Page<InterfaceAudit> auditPage);
}
