package com.hy.yqdoc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RequestToMethodItem {

    public String requestUrl;
    public String[] requestType;
    public String controllerName;
    public String controllerComment;
    public String methodName;
    public List<MethodParam> methodParams;
    public String description;
    public String contentType;

}
