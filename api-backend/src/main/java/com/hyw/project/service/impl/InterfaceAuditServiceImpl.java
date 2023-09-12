package com.hyw.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hyw.apicommon.common.ErrorCode;
import com.hyw.apicommon.constant.CommonConstant;
import com.hyw.apicommon.exception.BusinessException;
import com.hyw.apicommon.exception.ThrowUtils;
import com.hyw.apicommon.model.entity.InterfaceInfo;
import com.hyw.apicommon.model.entity.User;
import com.hyw.apicommon.utils.SqlUtils;
import com.hyw.project.mapper.InterfaceAuditMapper;
import com.hyw.project.model.dto.interfaceAudit.InterfaceAuditQueryRequest;
import com.hyw.project.model.dto.interfaceInfo.InterfaceInfoAddRequest;
import com.hyw.project.model.entity.InterfaceAudit;
import com.hyw.project.model.entity.InterfaceCharging;
import com.hyw.project.model.enums.InterfaceInfoAuditStatusEnum;
import com.hyw.project.model.enums.InterfaceInfoStatusEnum;
import com.hyw.project.model.vo.InterfaceAuditVO;
import com.hyw.project.service.InterfaceAuditService;
import com.hyw.project.service.InterfaceChargingService;
import com.hyw.project.service.InterfaceInfoService;
import com.hyw.project.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author hyw
 * @description 针对表【interface_audit】的数据库操作Service实现
 * @createDate 2023-06-12 22:26:29
 */
@Service
public class InterfaceAuditServiceImpl extends ServiceImpl<InterfaceAuditMapper, InterfaceAudit> implements InterfaceAuditService {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private InterfaceChargingService interfaceChargingService;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addInterfaceAudit(InterfaceInfoAddRequest interfaceInfoAddRequest, User loginUser) {
        // 插入接口信息记录
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        interfaceInfo.setUserId(loginUser.getId());
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.PENDING.getValue());
        boolean saveInfo = interfaceInfoService.save(interfaceInfo);
        // 插入接口审批记录
        InterfaceAudit interfaceAudit = new InterfaceAudit();
        interfaceAudit.setInterfaceId(interfaceInfo.getId());
        interfaceAudit.setUserId(loginUser.getId());
        boolean saveAudit = this.save(interfaceAudit);
        // 判断接口是否收费，插入收费信息
        if (interfaceInfoAddRequest.isNeedCharge()) {
            InterfaceCharging interfaceCharging = new InterfaceCharging();
            interfaceCharging.setInterfaceId(interfaceInfo.getId());
            interfaceCharging.setCharging(interfaceInfoAddRequest.getCharging());
            interfaceCharging.setAvailablePieces(interfaceInfoAddRequest.getAvailablePieces());
            interfaceCharging.setUserId(loginUser.getId());
            boolean saveCharging = interfaceChargingService.save(interfaceCharging);
            ThrowUtils.throwIf(!saveCharging, ErrorCode.OPERATION_ERROR);
        }
        ThrowUtils.throwIf(!saveInfo || !saveAudit, ErrorCode.OPERATION_ERROR);
        return interfaceAudit.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditInterface(Long interfaceAuditId, String remark, User loginUser) {
        // 1. 根据id查询接口审核表、接口信息表
        InterfaceAudit interfaceAudit = this.getById(interfaceAuditId);
        if (interfaceAudit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(interfaceAudit.getInterfaceId());
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 2. 判断该记录是否已完成审核
        Long auditId = interfaceAudit.getId();
        synchronized (String.valueOf(auditId).intern()) {
            if (interfaceAudit.getAuditStatus().equals(InterfaceInfoAuditStatusEnum.FINISH.getValue())) {
                return false;
            }
            // 2.1 增加审批人、备注字段，修改审核状态为审核完毕
            interfaceAudit.setRemark(remark);
            interfaceAudit.setApproverId(loginUser.getId());
            interfaceAudit.setAuditStatus(InterfaceInfoAuditStatusEnum.FINISH.getValue());
            boolean updateAudit = this.updateById(interfaceAudit);
            // 2.2 修改接口信息表接口状态为关闭
            interfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
            boolean updateInfo = interfaceInfoService.updateById(interfaceInfo);
            // 3. 判断修改情况
            ThrowUtils.throwIf(!updateInfo || !updateAudit, ErrorCode.OPERATION_ERROR);
            return true;
        }
    }

    /**
     * 获取查询包装类
     *
     * @param interfaceAuditQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<InterfaceAudit> getQueryWrapper(InterfaceAuditQueryRequest interfaceAuditQueryRequest) {
        if (interfaceAuditQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long interfaceId = interfaceAuditQueryRequest.getInterfaceId();
        Long userId = interfaceAuditQueryRequest.getUserId();
        Long approverId = interfaceAuditQueryRequest.getApproverId();
        String remark = interfaceAuditQueryRequest.getRemark();
        Integer auditStatus = interfaceAuditQueryRequest.getAuditStatus();
        String sortField = interfaceAuditQueryRequest.getSortField();
        String sortOrder = interfaceAuditQueryRequest.getSortOrder();
        // 排除不合法的状态
        ThrowUtils.throwIf(!InterfaceInfoAuditStatusEnum.getValues().contains(auditStatus), ErrorCode.PARAMS_ERROR);

        QueryWrapper<InterfaceAudit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjectUtils.isNotEmpty(interfaceId), "interfaceId", interfaceId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(approverId), "approverId", approverId);
        queryWrapper.like(StringUtils.isNotBlank(remark), "remark", remark);
        queryWrapper.eq(auditStatus != null, "auditStatus", auditStatus);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    /**
     * 分页获取接口审核封装
     *
     * @param auditPage
     * @return
     */
    @Override
    public Page<InterfaceAuditVO> getInterfaceAuditVOPage(Page<InterfaceAudit> auditPage) {
        List<InterfaceAudit> auditList = auditPage.getRecords();
        Page<InterfaceAuditVO> auditVOPage = new Page<>(auditPage.getCurrent(), auditPage.getSize(), auditPage.getTotal());
        if (CollectionUtils.isEmpty(auditList)) {
            return auditVOPage;
        }

        // 关联查询审批人、申请人和接口信息
        Set<Long> approverIdSet = auditList.stream().map(InterfaceAudit::getApproverId).collect(Collectors.toSet());
        Map<Long, List<User>> approverUserIdUserListMap = userService.listByIds(approverIdSet).stream().collect(Collectors.groupingBy(User::getId));

        Set<Long> createUserIdSet = auditList.stream().map(InterfaceAudit::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> createUserIdUserListMap = userService.listByIds(createUserIdSet).stream().collect(Collectors.groupingBy(User::getId));

        Set<Long> interfaceIdSet = auditList.stream().map(InterfaceAudit::getInterfaceId).collect(Collectors.toSet());
        Map<Long, List<InterfaceInfo>> interfaceIdInterfaceListMap = interfaceInfoService.listByIds(interfaceIdSet).stream().collect(Collectors.groupingBy(InterfaceInfo::getId));
        // 填充信息
        List<InterfaceAuditVO> interfaceAuditVOList = auditList.stream().map(interfaceAudit -> {
            InterfaceAuditVO interfaceAuditVO = new InterfaceAuditVO();
            BeanUtils.copyProperties(interfaceAudit, interfaceAuditVO);

            Long approverId = interfaceAudit.getApproverId();
            User approver = null;
            if (approverUserIdUserListMap.containsKey(approverId)) {
                approver = approverUserIdUserListMap.get(approverId).get(0);
            }
            interfaceAuditVO.setApproverAccount(approver.getUserAccount());

            Long createUserId = interfaceAudit.getUserId();
            User createUser = null;
            if (createUserIdUserListMap.containsKey(createUserId)) {
                createUser = createUserIdUserListMap.get(createUserId).get(0);
            }
            interfaceAuditVO.setUserAccount(createUser.getUserAccount());

            Long interfaceId = interfaceAudit.getInterfaceId();
            InterfaceInfo interfaceInfo = null;
            if (interfaceIdInterfaceListMap.containsKey(interfaceId)) {
                interfaceInfo = interfaceIdInterfaceListMap.get(interfaceId).get(0);
            }
            interfaceAuditVO.setInterfaceInfo(interfaceInfo);

            return interfaceAuditVO;
        }).collect(Collectors.toList());
        auditVOPage.setRecords(interfaceAuditVOList);
        return auditVOPage;
    }
}




