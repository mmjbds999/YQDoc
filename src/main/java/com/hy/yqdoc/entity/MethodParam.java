package com.hy.yqdoc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MethodParam {

    private String name;
    private Class<?> type;
    private String comment;

}
