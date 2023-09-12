use my_api;
set names utf8mb4;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userName     varchar(256)                           null comment '用户昵称',
    userAccount  varchar(256)                           not null comment '账号',
    userAvatar   varchar(1024)                          null comment '用户头像',
    gender       tinyint                                null comment '性别',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user / admin',
    userPassword varchar(512)                           not null comment '密码',
    phoneNum     varchar(15)                            null comment '手机号',
    accessKey    varchar(512)                           not null comment 'access key',
    secretKey    varchar(512)                           not null comment 'secret key',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除(0-未删, 1-已删)',
    constraint uni_userAccount
        unique (userAccount)
) comment '用户';

-- 接口信息表
create table if not exists interface_info
(
    `id`                bigint              not null auto_increment comment '主键' primary key,
    `name`              varchar(256)        not null comment '接口名称',
    `methodName`        varchar(255)        not null comment '方法名称',
    `sdkClassPath`      varchar(255)        null comment '接口的客户端包名',
    `description`       varchar(256)        null comment '描述',
    `url`               varchar(512)        not null comment '接口地址',
    `requestParams`     text                null comment '请求参数',
    `parameterExample`  varchar(512)        null comment '参数示例',
    `requestHeader`     text                null comment '请求头',
    `responseHeader`    text                null comment '响应头',
    `status`            int default 0       not null comment '接口状态（0-关闭，1-开启）',
    `method`            varchar(256)        not null comment '请求类型',
    `userId`            bigint              not null comment '创建人',
    `createTime`        datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime`        datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDelete`          tinyint default 0   not null comment '是否删除(0-未删, 1-已删)'
) comment '接口信息';

-- 用户调用接口关系表
create table if not exists user_interface_info
(
    `id` bigint not null auto_increment comment '主键' primary key,
    `userId` bigint not null comment '调用用户 id',
    `interfaceInfoId` bigint not null comment '接口 id',
    `totalNum` int default 0 not null comment '总调用次数',
    `leftNum` int default 0 not null comment '剩余调用次数',
    `status` int default 0 not null comment '0-正常，1-禁用',
    `version` int null default 0 comment '乐观锁版本号',
    `createTime` datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDelete` tinyint default 0 not null comment '是否删除(0-未删, 1-已删)'
) comment '用户调用接口关系';

-- 接口计费表
create table if not exists interface_charging
(
    `id` bigint not null auto_increment comment '主键' primary key,
    `interfaceId` bigint not null comment '接口 id',
    `charging` float(255, 2) not null comment '计费规则（元/条）',
    `availablePieces` varchar(255) not null comment '接口剩余可调用次数',
    `userId` bigint not null comment '创建人',
    `createTime` datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDelete` tinyint default 0 not null comment '是否删除(0-未删, 1-已删)'
) comment '接口计费';

-- 接口订单表
create table if not exists interface_order
(
    `id` bigint not null auto_increment comment '主键' primary key,
    `interfaceId` bigint not null comment '接口 id',
    `userId` bigint not null comment '用户 id',
    `orderNumber` varchar(512) not null comment '订单编号',
    `count` bigint not null comment '购买数量',
    `charging` float(255, 2) not null comment '接口调用单价',
    `totalAmount` float(10, 2) not null comment '交易金额',
    `status` int default 0 not null comment '交易状态【0-待付款，1-已完成，2-无效订单】',
    `createTime` datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDelete` tinyint default 0 not null comment '是否删除(0-未删, 1-已删)'
) comment '接口订单';

-- 订单锁表（硬删除）
create table if not exists order_lock
(
    `id` bigint not null auto_increment comment '主键' primary key,
    `orderNumber` varchar(512) not null comment '订单编号',
    `chargingId` bigint not null comment '接口计费 id',
    `userId` bigint not null comment '用户 id',
    `lockNum` bigint not null comment '接口锁定数量',
    `lockStatus` int not null comment '接口锁定状态【0-已解锁，1-已锁定，2-扣减】',
    `createTime` datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '订单锁';

-- 支付宝订单信息表
create table if not exists alipay_info
(
    `orderNumber` varchar(512) not null comment '订单编号' primary key,
    `subject` varchar(255) not null comment '交易名称',
    `totalAmount` float(10, 2) not null comment '交易金额',
    `buyerPayAmount` float(10, 2) not null comment '买家付款金额',
    `buyerId` text not null comment '买家在支付宝的唯一id',
    `tradeNo` text not null comment '支付宝交易凭证号',
    `tradeStatus` varchar(255) not null comment '交易状态',
    `gmtPayment` datetime not null comment '买家付款时间'
) comment '支付宝订单信息';

-- 接口上传申请表
create table if not exists interface_audit
(
    `id` bigint not null auto_increment comment '主键' primary key,
    `interfaceId` bigint not null comment '接口 id',
    `userId` bigint not null comment '申请人 id',
    `approverId` bigint not null comment '审批人 id',
    `remark` varchar(255) null comment '备注',
    `auditStatus` int not null comment '审核状态【0-待审核，1-已审核】',
    `createTime` datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDelete` tinyint default 0 not null comment '是否删除(0-未删, 1-已删)'
) comment '接口上传申请';