package com.yeyou.yeyoubackend.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 传输Id
 */
@Data
public class IdRequest implements Serializable {

    private static final long serialVersionUID = 7484008117737623129L;
    /**
     * id
     */
    private Long id;
}
