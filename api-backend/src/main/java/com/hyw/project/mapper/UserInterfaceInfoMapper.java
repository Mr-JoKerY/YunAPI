package com.hyw.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hyw.project.model.entity.UserInterfaceInfo;

import java.util.List;

/**
* @author hyw
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Mapper
* @createDate 2023-06-01 21:38:28
* @Entity generator.domain.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {

    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);
}




