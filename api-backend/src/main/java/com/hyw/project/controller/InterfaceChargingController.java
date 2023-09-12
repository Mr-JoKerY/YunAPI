package com.hyw.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hyw.apicommon.common.BaseResponse;
import com.hyw.apicommon.common.DeleteRequest;
import com.hyw.apicommon.common.ErrorCode;
import com.hyw.apicommon.common.ResultUtils;
import com.hyw.apicommon.constant.CommonConstant;
import com.hyw.apicommon.constant.UserConstant;
import com.hyw.apicommon.exception.BusinessException;
import com.hyw.apicommon.exception.ThrowUtils;
import com.hyw.apicommon.model.entity.User;
import com.hyw.apicommon.utils.SqlUtils;
import com.hyw.project.annotation.AuthCheck;
import com.hyw.project.model.dto.interfaceCharging.InterfaceChargingAddRequest;
import com.hyw.project.model.dto.interfaceCharging.InterfaceChargingQueryRequest;
import com.hyw.project.model.dto.interfaceCharging.InterfaceChargingUpdateRequest;
import com.hyw.project.model.entity.InterfaceCharging;
import com.hyw.project.service.InterfaceChargingService;
import com.hyw.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 接口计费管理
 *
 * @author hyw
 */
@RestController
@RequestMapping("/interfaceCharging")
@Slf4j
public class InterfaceChargingController {

    @Resource
    private InterfaceChargingService interfaceChargingService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建
     *
     * @param interfaceChargingAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceCharging(@RequestBody InterfaceChargingAddRequest interfaceChargingAddRequest, HttpServletRequest request) {
        if (interfaceChargingAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceCharging interfaceCharging = new InterfaceCharging();
        BeanUtils.copyProperties(interfaceChargingAddRequest, interfaceCharging);
        // 可对 interfaceCharging 增加一步校验
        User loginUser = userService.getLoginUser(request);
        interfaceCharging.setUserId(loginUser.getId());
        boolean result = interfaceChargingService.save(interfaceCharging);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(interfaceCharging.getId());
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceCharging(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceCharging oldInterfaceCharging = interfaceChargingService.getById(id);
        ThrowUtils.throwIf(oldInterfaceCharging == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldInterfaceCharging.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = interfaceChargingService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 更新（仅管理员）
     *
     * @param interfaceChargingUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateInterfaceCharging(@RequestBody InterfaceChargingUpdateRequest interfaceChargingUpdateRequest) {
        if (interfaceChargingUpdateRequest == null || interfaceChargingUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceCharging interfaceCharging = new InterfaceCharging();
        BeanUtils.copyProperties(interfaceChargingUpdateRequest, interfaceCharging);
        // 参数校验
//        interfaceChargingService.validInterfaceCharging(interfaceCharging, false);
        long id = interfaceChargingUpdateRequest.getId();
        // 判断是否存在
        InterfaceCharging oldInterfaceCharging = interfaceChargingService.getById(id);
        ThrowUtils.throwIf(oldInterfaceCharging == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = interfaceChargingService.updateById(interfaceCharging);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<InterfaceCharging> getInterfaceChargingById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceCharging interfaceCharging = interfaceChargingService.getById(id);
        ThrowUtils.throwIf(interfaceCharging == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(interfaceCharging);
    }

    /**
     * 分页获取列表
     *
     * @param interfaceChargingQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<InterfaceCharging>> listInterfaceCharging(@RequestBody InterfaceChargingQueryRequest interfaceChargingQueryRequest) {
        if (interfaceChargingQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = interfaceChargingQueryRequest.getCurrent();
        long size = interfaceChargingQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<InterfaceCharging> interfaceChargingPage = interfaceChargingService.page(new Page<>(current, size), getQueryWrapper(interfaceChargingQueryRequest));
        return ResultUtils.success(interfaceChargingPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param interfaceChargingQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<InterfaceCharging>> listInterfaceChargingByPage(@RequestBody InterfaceChargingQueryRequest interfaceChargingQueryRequest, HttpServletRequest request) {
        if (interfaceChargingQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        interfaceChargingQueryRequest.setUserId(loginUser.getId());
        long current = interfaceChargingQueryRequest.getCurrent();
        long size = interfaceChargingQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<InterfaceCharging> interfaceChargingPage = interfaceChargingService.page(new Page<>(current, size), getQueryWrapper(interfaceChargingQueryRequest));
        return ResultUtils.success(interfaceChargingPage);
    }

    /**
     * 获取查询包装类
     *
     * @param interfaceChargingQueryRequest
     * @return
     */
    private QueryWrapper<InterfaceCharging> getQueryWrapper(InterfaceChargingQueryRequest interfaceChargingQueryRequest) {
        QueryWrapper<InterfaceCharging> queryWrapper = new QueryWrapper<>();
        if (interfaceChargingQueryRequest == null) {
            return queryWrapper;
        }
        Long id = interfaceChargingQueryRequest.getId();
        Long interfaceId = interfaceChargingQueryRequest.getInterfaceId();
        Double charging = interfaceChargingQueryRequest.getCharging();
        String availablePieces = interfaceChargingQueryRequest.getAvailablePieces();
        Long userId = interfaceChargingQueryRequest.getUserId();
        String sortField = interfaceChargingQueryRequest.getSortField();
        String sortOrder = interfaceChargingQueryRequest.getSortOrder();

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(interfaceId), "interfaceId", interfaceId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(charging), "charging", charging);
        queryWrapper.eq(StringUtils.isNotBlank(availablePieces), "availablePieces", availablePieces);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    // endregion

}
