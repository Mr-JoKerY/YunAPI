package com.hyw.apiorder.model.dto;

import com.hyw.apicommon.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * @author hyw
 */
@Data
public class OrderQueryRequest extends PageRequest implements Serializable {

    private Integer type;

    private Long userId;

}
