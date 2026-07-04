package com.tianji.aigc.application.agent.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageDTO<T> {

    private Long total;

    private Long pages;

    private Integer pageNo;

    private Integer pageSize;

    private List<T> list;
}
