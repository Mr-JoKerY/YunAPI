package com.hyw.apicommon.service;

import com.hyw.apicommon.model.entity.InterfaceInfo;

/**
 * 接口服务
 */
public interface InnerInterfaceInfoService {

    /**
     * 从数据库中查询模拟接口是否存在（请求路径、请求方法、请求参数）
     */
    InterfaceInfo getInterfaceInfo(String url, String method);

    /**
     * 根据ID获取接口信息
     * @param id
     * @return
     */
    InterfaceInfo getInterfaceInfoById(Long id);
}
