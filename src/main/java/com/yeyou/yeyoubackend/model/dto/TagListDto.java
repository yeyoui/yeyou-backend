package com.yeyou.yeyoubackend.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class TagListDto {
    private String text;
    private Long id;
    private List<TagChildren> children;

}

