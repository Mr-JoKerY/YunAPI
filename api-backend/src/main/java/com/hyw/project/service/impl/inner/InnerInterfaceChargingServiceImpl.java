package com.hyw.project.service.impl.inner;

import com.hyw.apicommon.model.dto.LockAvailablePiecesRequest;
import com.hyw.apicommon.model.dto.UnLockAvailablePiecesRequest;
import com.hyw.apicommon.service.InnerInterfaceChargingService;
import com.hyw.project.service.InterfaceChargingService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * @author hyw
 * @create 2023-05-03 16:21
 */
@DubboService
public class InnerInterfaceChargingServiceImpl implements InnerInterfaceChargingService {
    
    @Resource
    private InterfaceChargingService interfaceChargingService;

    @Override
    public boolean checkInventory(Long id) {
        return interfaceChargingService.checkInventory(id);
    }

    @Override
    public boolean lockAvailablePieces(LockAvailablePiecesRequest lockAvailablePiecesRequest) {
        return interfaceChargingService.lockAvailablePieces(lockAvailablePiecesRequest);
    }

    @Override
    public boolean unLockAvailablePieces(UnLockAvailablePiecesRequest unLockAvailablePiecesRequest) {
        return interfaceChargingService.unLockAvailablePieces(unLockAvailablePiecesRequest);
    }
}
