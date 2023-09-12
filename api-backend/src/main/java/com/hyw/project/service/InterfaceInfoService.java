package com.hyw.project.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hyw.apicommon.model.entity.InterfaceInfo;
import com.hyw.apicommon.model.entity.User;
import com.hyw.project.model.dto.interfaceInfo.InterfaceInfoInvokeRequest;
import com.hyw.project.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.hyw.apicommon.model.vo.InterfaceInfoVO;

/**
 * @author hyw
 * @description 针对表【interface_info(接口信息)】的数据库操作Service
 * @createDate 2023-05-25 15:51:15
 */
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    /**
     * 校验
     *
     * @param interfaceInfo
     * @param add           是否为创建校验
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);

    /**
     * 上线接口
     *
     * @param id
     * @return
     */
    Boolean onlineInterfaceInfo(long id);

    /**
     * 下线接口
     *
     * @param id
     * @return
     */
    Boolean offlineInterfaceInfo(long id);

    /**
     * 获取接口信息封装
     *
     * @param interfaceInfo
     * @return
     */
    InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo);

    /**
     * 获取查询条件
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest);

    /**
     * 通用客户端调用接口
     * @param interfaceInfo
     * @param userRequestParams
     * @param loginUser
     * @return
     */
    Object invokeInterface(InterfaceInfo interfaceInfo, String userRequestParams, User loginUser);
}
