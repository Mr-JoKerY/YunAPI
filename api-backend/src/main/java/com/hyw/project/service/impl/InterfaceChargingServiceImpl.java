package com.hyw.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hyw.apicommon.common.ErrorCode;
import com.hyw.apicommon.exception.BusinessException;
import com.hyw.apicommon.exception.ThrowUtils;
import com.hyw.apicommon.model.dto.LockAvailablePiecesRequest;
import com.hyw.apicommon.model.dto.UnLockAvailablePiecesRequest;
import com.hyw.project.mapper.InterfaceChargingMapper;
import com.hyw.project.model.entity.InterfaceCharging;
import com.hyw.project.service.InterfaceChargingService;
import org.springframework.stereotype.Service;

/**
 * @author hyw
 * @description 针对表【interface_charging】的数据库操作Service实现
 * @createDate 2023-04-30 20:44:10
 */
@Service
public class InterfaceChargingServiceImpl extends ServiceImpl<InterfaceChargingMapper, InterfaceCharging> implements InterfaceChargingService {

    /**
     * 检查某个接口的库存是否充足
     * @param id
     * @return
     */
    @Override
    public boolean checkInventory(Long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceCharging interfaceCharging = this.getById(id);
        return Integer.parseInt(interfaceCharging.getAvailablePieces()) >= 0;
    }

    /**
     * 锁定库存
     * @param lockAvailablePiecesRequest
     * @return
     */
    @Override
    public boolean lockAvailablePieces(LockAvailablePiecesRequest lockAvailablePiecesRequest) {
        Long chargingId = lockAvailablePiecesRequest.getChargingId();
        Long count = lockAvailablePiecesRequest.getCount();
        ThrowUtils.throwIf(null == chargingId || null == count || count < 0, ErrorCode.PARAMS_ERROR);

        InterfaceCharging interfaceCharging = this.getById(chargingId);
        ThrowUtils.throwIf(interfaceCharging == null, ErrorCode.NOT_FOUND_ERROR);
        String availablePieces = interfaceCharging.getAvailablePieces();
        long current = Long.parseLong(availablePieces) - count;
        ThrowUtils.throwIf(current < 0, ErrorCode.OPERATION_ERROR, "接口库存不足!");

        InterfaceCharging updateInterfaceCharging = new InterfaceCharging();
        updateInterfaceCharging.setId(chargingId);
        updateInterfaceCharging.setAvailablePieces(String.valueOf(current));
        return this.updateById(updateInterfaceCharging);
    }

    /**
     * 解锁库存
     * @param unLockAvailablePiecesRequest
     * @return
     */
    @Override
    public boolean unLockAvailablePieces(UnLockAvailablePiecesRequest unLockAvailablePiecesRequest) {
        Long interfaceId = unLockAvailablePiecesRequest.getInterfaceId();
        Long count = unLockAvailablePiecesRequest.getCount();
        ThrowUtils.throwIf(null == interfaceId || null == count || count < 0, ErrorCode.PARAMS_ERROR);

        UpdateWrapper<InterfaceCharging> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("interfaceId", interfaceId).setSql("availablePieces = availablePieces + " + count);
        return this.update(updateWrapper);
    }
}




