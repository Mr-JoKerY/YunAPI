package com.hyw.apicommon.service;

import com.hyw.apicommon.model.dto.UpdateUserInterfaceInfoRequest;

/**
 * 接口统计
 */
public interface InnerUserInterfaceInfoService {

    /**
     * 调用接口统计
     *
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);

    /**
     * 更新用户接口信息
     *
     * @param updateUserInterfaceInfoRequest
     * @return
     */
    boolean updateUserInterfaceInfo(UpdateUserInterfaceInfoRequest updateUserInterfaceInfoRequest);

}
