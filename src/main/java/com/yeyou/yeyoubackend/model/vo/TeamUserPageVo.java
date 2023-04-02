package com.yeyou.yeyoubackend.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 页码信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeamUserPageVo extends PageInfoVo implements Serializable {

    private static final long serialVersionUID = -7562558214487309280L;

    private List<TeamUserVo> teamUserVoList;
}
