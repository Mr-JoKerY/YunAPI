package com.hyw.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hyw.apicommon.common.BaseResponse;
import com.hyw.apicommon.common.ErrorCode;
import com.hyw.apicommon.common.ResultUtils;
import com.hyw.apicommon.constant.UserConstant;
import com.hyw.apicommon.exception.BusinessException;
import com.hyw.apicommon.exception.ThrowUtils;
import com.hyw.apicommon.model.entity.User;
import com.hyw.project.annotation.AuthCheck;
import com.hyw.project.model.dto.interfaceAudit.InterfaceAuditQueryRequest;
import com.hyw.project.model.dto.interfaceAudit.InterfaceAuditRequest;
import com.hyw.project.model.dto.interfaceInfo.InterfaceInfoAddRequest;
import com.hyw.project.model.entity.InterfaceAudit;
import com.hyw.project.model.vo.InterfaceAuditVO;
import com.hyw.project.service.InterfaceAuditService;
import com.hyw.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 接口审批管理
 *
 * @author hyw
 */
@RestController
@RequestMapping("/interfaceAudit")
@Slf4j
public class InterfaceAuditController {

    @Resource
    private InterfaceAuditService interfaceAuditService;

    @Resource
    private UserService userService;

    /**
     * 用户添加接口
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceAudit(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Long newInterfaceAuditId = interfaceAuditService.addInterfaceAudit(interfaceInfoAddRequest, loginUser);
        return ResultUtils.success(newInterfaceAuditId);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param interfaceAuditQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<InterfaceAuditVO>> listInterfaceAuditByPage(@RequestBody InterfaceAuditQueryRequest interfaceAuditQueryRequest) {
        if (interfaceAuditQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = interfaceAuditQueryRequest.getCurrent();
        long size = interfaceAuditQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<InterfaceAudit> auditPage = interfaceAuditService.page(new Page<>(current, size), interfaceAuditService.getQueryWrapper(interfaceAuditQueryRequest));
        return ResultUtils.success(interfaceAuditService.getInterfaceAuditVOPage(auditPage));
    }

    /**
     * 审核接口成功
     *
     * @param interfaceAuditRequest
     * @param request
     * @return
     */
    @PostMapping("/confirm")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> auditInterface(@RequestBody InterfaceAuditRequest interfaceAuditRequest, HttpServletRequest request) {
        if (interfaceAuditRequest == null || interfaceAuditRequest.getInterfaceAuditId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前登录用户
        final User loginUser = userService.getLoginUser(request);
        Long interfaceAuditId = interfaceAuditRequest.getInterfaceAuditId();
        String remark = interfaceAuditRequest.getRemark();
        boolean result = interfaceAuditService.auditInterface(interfaceAuditId, remark, loginUser);
        return ResultUtils.success(result);
    }

}
