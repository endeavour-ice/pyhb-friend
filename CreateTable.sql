create table chat_record
(
    id          varchar(255)                       not null
        primary key,
    user_id     varchar(255)                       null comment '用户id
',
    friend_id   varchar(255)                       null comment '好友id',
    has_read    int(1)   default 0                 null comment '是否已读 0 未读',
    create_time datetime default CURRENT_TIMESTAMP not null,
    is_delete   tinyint  default 0                 not null comment '是否删除',
    message     varchar(1024) charset utf8mb4      null comment '消息',
    send_time   datetime default CURRENT_TIMESTAMP not null comment '发送的时间'
)
    comment '聊天记录表';
create table comment_reply
(
    id            bigint auto_increment comment 'id'
        primary key,
    post_id       bigint                             not null comment '帖子id',
    comment_id    bigint                             not null comment '回复的id',
    user_id       bigint                             not null comment '创建用户 id',
    reply_content varchar(1204) charset utf8mb4      null comment '评论内容',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint  default 0                 not null comment '是否删除'
)
    comment '回复评论表';
create table post
(
    id             bigint auto_increment comment 'id'
        primary key,
    user_id        bigint                             not null comment '创建用户 id',
    content        text                               null comment '内容',
    tags         varchar(255)                              null comment '标签id',
    review_status  int(20)  default 0                 not null comment '状态（0-待审核, 1-通过, 2-拒绝）',
    review_message varchar(512)                       null comment '审核信息',
    view_num       int(20)  default 0                 not null comment '浏览数',
    thumb_num      int(20)  default 0                 not null comment '点赞数',
    create_time    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete      tinyint  default 0                 not null comment '是否删除'
)
    comment '帖子';
create table post_comment
(
    id          bigint auto_increment comment 'id'
        primary key,
    post_id     bigint                             not null comment '帖子id',
    user_id     bigint                             not null comment '评论用户 id',
    content     varchar(1204) charset utf8mb4      null comment '评论内容',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint  default 0                 not null comment '是否删除'
)
    comment '评论表';
create table post_thumb
(
    id          bigint auto_increment comment 'id'
        primary key,
    post_id     bigint                             not null comment '帖子 id',
    user_id     bigint                             not null comment '创建用户 id',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '帖子点赞记录';
create table post_collect
(
    id          bigint auto_increment comment 'id'
        primary key,
    post_id     bigint                             not null comment '帖子 id',
    user_id     bigint                             not null comment '创建用户 id',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '帖子收藏记录';
create table read_team_chat
(
    id          varchar(255)                       not null
        primary key,
    user_id     varchar(255)                       null comment '用户id',
    team_id     varchar(255)                       null comment '队伍id',
    has_read    int      default 0                 null comment '未读的信息条数',
    create_time datetime default CURRENT_TIMESTAMP not null,
    is_delete   tinyint  default 0                 not null comment '是否删除'
)
    comment '队伍聊天记录表';
create table team
(
    id          bigint                             not null comment 'id'
        primary key,
    name        varchar(256)                       null comment '队伍的名称',
    user_id     bigint                             null comment '用户id',
    description varchar(1024)                      null comment '描述',
    max_num     bigint   default 1                 not null comment '最大人数',
    password    varchar(32)                        null comment '密码',
    status      varchar(256)                       null comment '状态',
    expire_time datetime                           null comment '创建队伍的时间',
    is_delete   tinyint  default 0                 not null comment '是否删除',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
    avatar_url  varchar(255)                       null comment '队伍头像'
)
    comment '队伍表';
create table team_chat_record
(
    id          varchar(255)                       not null
        primary key,
    user_id     varchar(255)                       null comment '用户id',
    team_id     varchar(255)                       null comment '队伍id',
    message     varchar(1024) charset utf8mb4      null comment '消息',
    has_read    int(1)   default 0                 null comment '是否已读 0 未读',
    create_time datetime default CURRENT_TIMESTAMP not null,
    is_delete   tinyint  default 0                 not null comment '是否删除'
)
    comment '队伍聊天记录表';
create table user
(
    id           bigint                             not null comment 'id'
        primary key,
    username     varchar(256)                       null comment '用户名',
    user_account varchar(256)                       null comment '登陆账号',
    avatar_url   varchar(1024)                      null comment '用户头像',
    gender       varchar(10)                        null comment '性别',
    password     varchar(32)                        null comment '密码',
    tel          varchar(256)                       null comment '手机号',
    email        varchar(256)                       null comment '邮箱',
    profile      varchar(255)                       null comment '个人简介',
    planet_code  varchar(512)                       null comment '编号',
    tags         varchar(1024)                      null comment '标签列表',
    user_status  int      default 0                 not null comment '用户状态',
    role         int      default 0                 not null comment '用户角色 0 - 普通用户 1 - 管理员',
    is_delete    tinyint  default 0                 not null comment '是否删除',
    create_time  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间'
)
    comment '用户表';

create index name
    on user (user_account);

create index t
    on user (tags);
create table user_friend
(
    id          varchar(255)                         not null comment 'id'
        primary key,
    user_id     varchar(255)                         null comment '用户id',
    friend_id  varchar(255)                         null comment '朋友id',
    comments    varchar(255)                         null comment '朋友备注',
    create_time datetime   default CURRENT_TIMESTAMP null comment '添加好友日期',
    is_delete   tinyint(1) default 0                 not null
);
create table user_friend_req
(
    id          varchar(255)                       not null
        primary key,
    from_userid varchar(255)                       null comment '请求用户id',
    to_userid   varchar(255)                       null comment '被请求好友用户',
    message     varchar(255)                       null comment '发送的消息',
    user_status int(1)   default 0                 not null comment '消息是否已处理 0 未处理',
    create_time datetime default CURRENT_TIMESTAMP null
);
create table user_label
(
    id          bigint(22)                         not null
        primary key,
    label_type  varchar(255)                       null comment '标签类型',
    label       varchar(255)                       null comment '标签',
    is_delete   tinyint  default 0                 not null comment '是否删除',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
    tag_num     int      default 0                 not null comment '标签使用数'
)
    comment '标签表';
create table user_notice
(
    id          varchar(22)                        not null
        primary key,
    notice      text                               null comment '发布的公告',
    region      tinyint                            null comment '发布的位置',
    is_delete   tinyint  default 0                 not null comment '是否删除',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间'
)
    comment '公告表';
create table user_team
(
    id          bigint                             not null comment 'id'
        primary key,
    name        varchar(256)                       null comment '队伍的名称',
    user_id     bigint                             null comment '用户id',
    team_id     bigint                             null comment '队伍id',
    join_time   datetime                           null comment '加入队伍时间',
    is_delete   tinyint  default 0                 not null comment '是否删除',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间'
)
    comment '队伍表';

create index team
    on user_team (user_id, team_id);