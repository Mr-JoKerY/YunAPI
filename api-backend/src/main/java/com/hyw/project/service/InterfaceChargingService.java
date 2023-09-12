package com.hyw.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hyw.apicommon.model.dto.LockAvailablePiecesRequest;
import com.hyw.apicommon.model.dto.UnLockAvailablePiecesRequest;
import com.hyw.project.model.entity.InterfaceCharging;

/**
* @author hyw
* @description 针对表【interface_charging】的数据库操作Service
* @createDate 2023-04-30 20:44:10
*/
public interface InterfaceChargingService extends IService<InterfaceCharging> {

    /**
     * 检查某个接口的库存是否充足
     * @param id
     * @return
     */
    boolean checkInventory(Long id);

    /**
     * 锁定库存
     * @param lockAvailablePiecesRequest
     * @return
     */
    boolean lockAvailablePieces(LockAvailablePiecesRequest lockAvailablePiecesRequest);

    /**
     * 解锁库存
     * @param unLockAvailablePiecesRequest
     * @return
     */
    boolean unLockAvailablePieces(UnLockAvailablePiecesRequest unLockAvailablePiecesRequest);
}
