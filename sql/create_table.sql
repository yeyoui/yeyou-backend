create
database if not exists yeyou;

use
yeyou;

-- 用户表
drop table if exists `user`;
create table user
(
    username     varchar(256) null comment '用户昵称',
    id           bigint auto_increment comment 'id'
        primary key,
    userAccount  varchar(256) null comment '账号',
    avatarUrl    varchar(1024)  null comment '用户头像' default 'http://yeapi.top:80/icons/8bd669ae-65cc-4ade-9043-3c6746e32213.png',
    gender       tinyint null comment '性别',
    userPassword varchar(512)       not null comment '密码',
    phone        varchar(128) null comment '电话',
    email        varchar(512) null comment '邮箱',
    userStatus   int      default 0 not null comment '状态 0 - 正常',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete     tinyint  default 0 not null comment '是否删除',
    userRole     int      default 0 not null comment '用户角色 0 - 普通用户 1 - 管理员  2 - 优质用户',
    userCode   varchar(512) null comment '用户编号',
    tags         varchar(1024) null comment '标签 json 列表'
) comment '用户';

-- 队伍表
drop table if exists team;
create table team
(
    id          bigint auto_increment comment 'id' primary key,
    name        varchar(256)       not null comment '队伍名称',
    description varchar(1024) null comment '描述',
    maxNum      int      default 1 not null comment '最大人数',
    expireTime  datetime null   comment '过期时间',
    userId      bigint not null comment '创建者id',
    leaderId    bigint not null comment '队长 id',
    memberNum     int   default 1 comment  '成员数(默认1)',
    status      int      default 0 not null comment '0 - 公开，1 - 私有，2 - 加密',
    password    varchar(512) null comment '密码',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete    tinyint  default 0 not null comment '是否删除'
) comment '队伍';

-- 用户队伍关系
drop table if exists user_team;
create table user_team
(
    id         bigint auto_increment comment 'id'
        primary key,
    userId     bigint comment '用户id',
    teamId     bigint comment '队伍id',
    joinTime   datetime default CURRENT_TIMESTAMP comment '加入时间',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0 not null comment '是否删除'
) comment '用户队伍关系';

create table team_seckill
(
    id         bigint auto_increment comment 'id' primary key,
    teamId     bigint                             not null comment '队伍id',
    joinNum    int                                not null comment '本次可加入人数',
    beginTime  datetime                           not null comment '开始时间',
    endTime    datetime                           not null comment '结束时间',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '队伍抢夺表';


-- 标签表（可以不创建，因为标签字段已经放到了用户表中）
create table tag
(
    id         bigint auto_increment comment 'id'
        primary key,
    tagName    varchar(256) null comment '标签名称',
    userId     bigint null comment '用户 id',
    parentId   bigint null comment '父标签 id',
    isParent   tinyint null comment '0 - 不是, 1 - 父标签',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0 not null comment '是否删除'
#     constraint uniIdx_tagName
#         unique (tagName)
) comment '标签';

create index idx_userId
    on tag (userId);

drop table if exists post;
create table post
(
    id         bigint auto_increment primary key comment 'id',
    title       varchar(256) not null comment '标题',
    content     text not null comment '文章内容',
    tags        varchar(1024) null comment '标签 json 列表(数组)',
    userId   bigint not null comment '作者ID',
    thumbNum int default 0 comment '点赞数',
    favourNum int default 0 comment '收藏数',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0 not null comment '是否删除',
    index idx_userId(userId)
)comment '帖子' collate utf8mb4_unicode_ci;

drop table if exists post_thumb;
create table post_thumb
(
    id         bigint auto_increment primary key  comment 'id',
    postId   bigint not null comment '帖子id',
    userId   bigint not null comment '用户id',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId(postId),
    index idx_userId(userId)
)comment '帖子点赞表';

drop table if exists post_favour;
create table post_favour
(
    id         bigint auto_increment primary key  comment 'id',
    postId   bigint not null comment '帖子id',
    userId   bigint not null comment '用户id',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId(postId),
    index idx_userId(userId)
)comment '帖子收藏表';
