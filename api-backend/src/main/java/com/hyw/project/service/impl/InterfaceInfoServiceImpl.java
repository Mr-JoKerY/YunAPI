package com.hyw.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hyw.apiclientsdk.client.CommonApiClient;
import com.hyw.apiclientsdk.common.ApiRequest;
import com.hyw.apiclientsdk.common.ApiResponse;
import com.hyw.apicommon.common.ErrorCode;
import com.hyw.apicommon.constant.CommonConstant;
import com.hyw.apicommon.exception.BusinessException;
import com.hyw.apicommon.exception.ThrowUtils;
import com.hyw.apicommon.model.entity.InterfaceInfo;
import com.hyw.apicommon.model.entity.User;
import com.hyw.apicommon.model.vo.InterfaceInfoVO;
import com.hyw.project.mapper.InterfaceChargingMapper;
import com.hyw.project.mapper.InterfaceInfoMapper;
import com.hyw.project.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.hyw.project.model.entity.InterfaceCharging;
import com.hyw.project.model.enums.InterfaceInfoStatusEnum;
import com.hyw.project.service.InterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author hyw
* @description 针对表【interface_info(接口信息)】的数据库操作Service实现
* @createDate 2023-05-25 15:51:15
*/
@Slf4j
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
    implements InterfaceInfoService {

    @Resource
    private InterfaceChargingMapper interfaceChargingMapper;

    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = interfaceInfo.getName();
        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isAnyBlank(name)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        if (StringUtils.isNotBlank(name) && name.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
        }
    }

    @Override
    public Boolean onlineInterfaceInfo(long id) {
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = this.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);

        // todo 测试该接口是否可以调用，这里作为模拟，只调用了clint的接口
//        com.hyw.apiclientsdk.model.User user = new com.hyw.apiclientsdk.model.User();
//        user.setUsername("test");
//        String username = apiClient.getUsernameByPost(user);
//        if (StringUtils.isBlank(username)) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口验证失败");
//        }

        // 接口状态设为上线
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.ONLINE.getValue());
        return this.updateById(interfaceInfo);
    }

    @Override
    public Boolean offlineInterfaceInfo(long id) {
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = this.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);

        // 接口状态设为关闭
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
        return this.updateById(interfaceInfo);
    }

    /**
     * 获取接口信息封装
     *
     * @param interfaceInfo
     * @return
     */
    @Override
    public InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo) {
        InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
        BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
        // 关联查询接口计费信息
        QueryWrapper<InterfaceCharging> interfaceChargingQueryWrapper = new QueryWrapper<>();
        interfaceChargingQueryWrapper.eq("interfaceId", interfaceInfo.getId());
        InterfaceCharging interfaceCharging = interfaceChargingMapper.selectOne(interfaceChargingQueryWrapper);
        if (interfaceCharging != null) {
            interfaceInfoVO.setChargingId(interfaceCharging.getId());
            interfaceInfoVO.setCharging(interfaceCharging.getCharging());
            interfaceInfoVO.setAvailablePieces(interfaceCharging.getAvailablePieces());
        }
        return interfaceInfoVO;
    }

    /**
     * 获取查询条件
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        String name = interfaceInfoQuery.getName();
        String description = interfaceInfoQuery.getDescription();

        // name，description 需支持模糊搜索
        interfaceInfoQuery.setName(null);
        interfaceInfoQuery.setDescription(null);

        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        // 待审核的接口不予以展示
        queryWrapper.ne("status", InterfaceInfoStatusEnum.PENDING.getValue());
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    /**
     * 通用客户端调用接口
     * @param interfaceInfo
     * @param userRequestParams
     * @param loginUser
     * @return
     */
    @Override
    public Object invokeInterface(InterfaceInfo interfaceInfo, String userRequestParams, User loginUser) {
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();

        // 客户端调用接口
        CommonApiClient apiClient = new CommonApiClient(accessKey, secretKey);
        ApiRequest request = new ApiRequest(interfaceInfo.getMethod(), interfaceInfo.getUrl(), userRequestParams);
        ApiResponse response = apiClient.sendRequest(request);
        log.info("调用接口：" + interfaceInfo.getName());
        return response.getResult();
    }

}




