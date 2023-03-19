package com.yeyou.yeyoubackend.model.enums;

public enum TeamStatusEnum {

    PUBLIC("公开",0),
    PRIVATE("私有", 1),
    SECRET("加密", 2),
    SECKILL("争夺", 3);
    private String text;
    private int val;

    public static TeamStatusEnum getTeamStatusEnum(Integer val){
        if(val==null) return null;
        TeamStatusEnum[] values = values();
        for (TeamStatusEnum value : values) {
            if(value.getVal()==val) return value;
        }
        return null;
    }

    TeamStatusEnum(String text, int val) {
        this.text = text;
        this.val = val;
    }

    public String getText() {
        return text;
    }

    public int getVal() {
        return val;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setVal(int val) {
        this.val = val;
    }
}
