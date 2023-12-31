package com.hyw.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hyw.apicommon.exception.ThrowUtils;
import com.hyw.apicommon.model.dto.UpdateUserInterfaceInfoRequest;
import com.hyw.apicommon.model.entity.User;
import com.hyw.project.model.entity.UserInterfaceInfo;
import com.hyw.project.annotation.AuthCheck;
import com.hyw.apicommon.common.BaseResponse;
import com.hyw.apicommon.common.DeleteRequest;
import com.hyw.apicommon.common.ErrorCode;
import com.hyw.apicommon.common.ResultUtils;
import com.hyw.apicommon.constant.CommonConstant;
import com.hyw.apicommon.constant.UserConstant;
import com.hyw.apicommon.exception.BusinessException;
import com.hyw.project.model.dto.userInterfaceInfo.UserInterfaceInfoAddRequest;
import com.hyw.project.model.dto.userInterfaceInfo.UserInterfaceInfoQueryRequest;
import com.hyw.project.model.dto.userInterfaceInfo.UserInterfaceInfoUpdateRequest;
import com.hyw.project.model.entity.InterfaceCharging;
import com.hyw.project.model.vo.UserInterfaceInfoVO;
import com.hyw.project.service.InterfaceChargingService;
import com.hyw.project.service.UserInterfaceInfoService;
import com.hyw.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户接口信息接口
 *
 * @author hyw
 */
@RestController
@RequestMapping("/userInterfaceInfo")
@Slf4j
public class UserInterfaceInfoController {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private InterfaceChargingService interfaceChargingService;

    // region 增删改查

    /**
     * 创建
     *
     * @param userInterfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUserInterfaceInfo(@RequestBody UserInterfaceInfoAddRequest userInterfaceInfoAddRequest, HttpServletRequest request) {
        if (userInterfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoAddRequest, userInterfaceInfo);
        // 校验
        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        userInterfaceInfo.setUserId(loginUser.getId());
        boolean result = userInterfaceInfoService.save(userInterfaceInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newUserInterfaceInfoId = userInterfaceInfo.getId();
        return ResultUtils.success(newUserInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUserInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        UserInterfaceInfo oldUserInterfaceInfo = userInterfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldUserInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldUserInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = userInterfaceInfoService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 更新
     *
     * @param userInterfaceInfoUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserInterfaceInfo(@RequestBody UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest,
                                                         HttpServletRequest request) {
        if (userInterfaceInfoUpdateRequest == null || userInterfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoUpdateRequest, userInterfaceInfo);
        // 参数校验
        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, false);
        User user = userService.getLoginUser(request);
        long id = userInterfaceInfoUpdateRequest.getId();
        // 判断是否存在
        UserInterfaceInfo oldUserInterfaceInfo = userInterfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldUserInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可修改
        if (!oldUserInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = userInterfaceInfoService.updateById(userInterfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserInterfaceInfo> getUserInterfaceInfoById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getById(id);
        return ResultUtils.success(userInterfaceInfo);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param userInterfaceInfoQueryRequest
     * @return
     */
    @GetMapping("/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<UserInterfaceInfo>> listUserInterfaceInfo(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest) {
        UserInterfaceInfo userInterfaceInfoQuery = new UserInterfaceInfo();
        if (userInterfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(userInterfaceInfoQueryRequest, userInterfaceInfoQuery);
        }
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>(userInterfaceInfoQuery);
        List<UserInterfaceInfo> userInterfaceInfoList = userInterfaceInfoService.list(queryWrapper);
        return ResultUtils.success(userInterfaceInfoList);
    }

    /**
     * 分页获取列表
     *
     * @param userInterfaceInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserInterfaceInfo>> listUserInterfaceInfoByPage(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest, HttpServletRequest request) {
        if (userInterfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfoQuery = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoQueryRequest, userInterfaceInfoQuery);
        long current = userInterfaceInfoQueryRequest.getCurrent();
        long size = userInterfaceInfoQueryRequest.getPageSize();
        String sortField = userInterfaceInfoQueryRequest.getSortField();
        String sortOrder = userInterfaceInfoQueryRequest.getSortOrder();
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>(userInterfaceInfoQuery);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<UserInterfaceInfo> userInterfaceInfoPage = userInterfaceInfoService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(userInterfaceInfoPage);
    }

    /**
     * 获取当前用户创建的资源列表
     *
     * @param userId
     * @param request
     * @return
     */
    @GetMapping("/my/list")
    public BaseResponse<List<UserInterfaceInfoVO>> listUserInterfaceInfoByUserId(@RequestParam Long userId, HttpServletRequest request) {
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<UserInterfaceInfoVO> userInterfaceInfoVOList = userInterfaceInfoService.listUserInterfaceInfoByUserId(userId, loginUser);
        return ResultUtils.success(userInterfaceInfoVOList);
    }

    /**
     * 获取接口的免费调用次数
     *
     * @param updateUserInterfaceInfoRequest
     * @param request
     * @return
     */
    @PostMapping("/get/free")
    public BaseResponse<Boolean> getFreeInterfaceCount(@RequestBody UpdateUserInterfaceInfoRequest updateUserInterfaceInfoRequest, HttpServletRequest request) {
        Long interfaceId = updateUserInterfaceInfoRequest.getInterfaceId();
        Long userId = updateUserInterfaceInfoRequest.getUserId();
        Long lockNum = updateUserInterfaceInfoRequest.getLockNum();
        ThrowUtils.throwIf(interfaceId == null || userId == null || lockNum == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(lockNum > 100, ErrorCode.OPERATION_ERROR, "该接口调用的免费次数不足！");

        synchronized (userId) {
            // 检查是否为登录用户
            User loginUser = userService.getLoginUser(request);
            ThrowUtils.throwIf(!userId.equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
            // 检查该接口是否付费
            long interfaceCharging = interfaceChargingService.count(new QueryWrapper<InterfaceCharging>().eq("interfaceId", interfaceId));
            ThrowUtils.throwIf(interfaceCharging > 0, ErrorCode.PARAMS_ERROR, "抱歉，该接口需付费调用!");
            // 检查该接口的剩余调用次数
            UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getOne(new QueryWrapper<UserInterfaceInfo>().eq("userId", userId).eq("interfaceInfoId", interfaceId));
            ThrowUtils.throwIf(userInterfaceInfo != null && userInterfaceInfo.getLeftNum() >= 1000, ErrorCode.OPERATION_ERROR, "抱歉，您获取的次数太多了");

            boolean result = userInterfaceInfoService.updateUserInterfaceInfo(updateUserInterfaceInfoRequest);
            return ResultUtils.success(result);
        }
    }

    // endregion

}
