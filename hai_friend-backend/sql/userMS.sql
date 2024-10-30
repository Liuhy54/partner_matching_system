-- auto-generated definition
create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    username     varchar(256) charset utf8mb4       null comment '用户昵称',
    userAccount  varchar(256) charset utf8mb4       null comment '账号',
    avatarUrl    varchar(1024) charset utf8mb4      null comment '头像',
    gender       tinyint                            null comment '性别',
    userPassword varchar(256) charset utf8mb4       not null comment '密码',
    phone        varchar(128) charset utf8mb4       null comment '电话',
    email        varchar(512) charset utf8mb4       null comment '邮箱',
    userStatus   int      default 0                 null comment '状态 0 - 正常',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 null comment '删除',
    userRole     int      default 0                 not null comment '用户角色：0 - 普通用户 1 - 管理员',
    planetCode   varchar(512)                       null comment '编号',
    tags         varchar(1024)                      null comment '标签 json 列表'
)
    comment '用户' collate = utf16_bin;



-- auto-generated definition
create table tag
(
    id         bigint auto_increment comment 'id'
        primary key,
    tagName    varchar(256) charset utf8mb4       null comment '标签名称',
    userId     bigint                             null comment '用户id',
    parentId   bigint                             null comment '父级id',
    isParent   tinyint                            null comment '0-不是父标签 1-是父标签',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 null comment '删除',
    constraint unildx_tagName
        unique (tagName)
)
    comment '标签' collate = utf16_bin;

create index idx_userId
    on tag (userId);
