package com.hyw.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hyw.apicommon.model.dto.UpdateUserInterfaceInfoRequest;
import com.hyw.apicommon.model.entity.User;
import com.hyw.apicommon.model.vo.InterfaceInfoVO;
import com.hyw.project.model.entity.UserInterfaceInfo;
import com.hyw.project.model.vo.UserInterfaceInfoVO;

import java.util.List;

/**
* @author hyw
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
* @createDate 2023-06-01 21:38:28
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    /**
     * 校验
     * @param userInterfaceInfo
     * @param add 是否为创建校验
     */
    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);

    /**
     * 调用接口统计
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);

    /**
     * 回滚调用接口统计的数据
     *
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean recoverInvokeCount(long interfaceInfoId, long userId);

    /**
     * 校验用户是否有调用次数
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean haveInvokeCount(long interfaceInfoId, long userId);

    /**
     * 根据Id获取某个用户拥有的接口信息
     * @param userId
     * @param loginUser
     * @return
     */
    List<UserInterfaceInfoVO> listUserInterfaceInfoByUserId(Long userId, User loginUser);

    /**
     * 更新用户接口信息
     * @param updateUserInterfaceInfoRequest
     * @return
     */
    boolean updateUserInterfaceInfo(UpdateUserInterfaceInfoRequest updateUserInterfaceInfoRequest);

    /**
     * 获取调用次数前limit的接口信息
     * @param limit
     * @return
     */
    List<InterfaceInfoVO> interfaceInvokeTopAnalysis(int limit);
}
