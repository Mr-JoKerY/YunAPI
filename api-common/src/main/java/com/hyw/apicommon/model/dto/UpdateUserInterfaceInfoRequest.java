package com.hyw.apicommon.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author hyw
 * @create 2023-05-06 13:46
 */
@Data
public class UpdateUserInterfaceInfoRequest implements Serializable {

    private static final long serialVersionUID = 1472097902521779075L;

    private Long userId;

    private Long interfaceId;

    private Long lockNum;
}
