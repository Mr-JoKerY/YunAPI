package com.hyw.apiclientsdk.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author hyw
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ApiRequest {

    private String method;

    private String path;

    private String params;

}
