package com.hyw.apicommon.service;

import com.hyw.apicommon.model.dto.UnLockAvailablePiecesRequest;
import com.hyw.apicommon.model.dto.LockAvailablePiecesRequest;

/**
 * @author hyw
 * @create 2023-05-03 16:22
 */
public interface InnerInterfaceChargingService {

    /**
     * 检查某个接口的库存是否充足
     * @param id
     * @return
     */
    boolean checkInventory(Long id);

    /**
     * 锁定某个接口的库存
     * @param lockAvailablePiecesRequest
     * @return
     */
    boolean lockAvailablePieces(LockAvailablePiecesRequest lockAvailablePiecesRequest);

    /**
     * 解锁某个接口的库存
     * @param unLockAvailablePiecesRequest
     * @return
     */
    boolean unLockAvailablePieces(UnLockAvailablePiecesRequest unLockAvailablePiecesRequest);
}
