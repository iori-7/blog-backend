-- =============================================================================
--  大事件 (big-event) 数据库初始化脚本
--
--  表名、字段名与后端代码 (com.itheima.mapper.*) 完全一致，改代码不用改这里。
--  字符集统一 utf8mb4，MySQL 5.7 / 8.0 均可直接执行。
--  上传到服务器前请先读一遍下面的“字段长度说明”，里面有当初报错的原因。
-- =============================================================================

-- 保证导入本文件时中文（注释、'草稿' 默认值）不会变成乱码。
-- 命令行导入也请带上 --default-character-set=utf8mb4，见文件末尾。
SET NAMES utf8mb4;

-- ⚠️⚠️ 下面这行会删除服务器上已有的 big_event 库及其全部数据 ⚠️⚠️
--     如果服务器上已经有数据不能丢，请改用同目录的 big_event_upgrade.sql
DROP DATABASE IF EXISTS `big_event`;

CREATE DATABASE `big_event`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `big_event`;


-- -----------------------------------------------------------------------------
-- 用户表
-- 对应：UserMapper / User 实体 / UserServiceImpl
-- -----------------------------------------------------------------------------
CREATE TABLE `user` (
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`    varchar(20)  NOT NULL                COMMENT '用户名（注册校验 5-16 位）',
    `password`    varchar(100) NOT NULL                COMMENT '密码哈希：BCrypt 固定 60 位，兼容旧的 MD5 32 位',
    `nickname`    varchar(20)  DEFAULT NULL            COMMENT '昵称（校验 1-10 位）',
    `email`       varchar(100) DEFAULT NULL            COMMENT '邮箱',
    `user_pic`    varchar(512) DEFAULT NULL            COMMENT '头像地址（OSS 长 URL，必须给足）',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    -- 登录走 findByUserName 查询，且能防止并发注册出重复用户名
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';


-- -----------------------------------------------------------------------------
-- 分类表
-- 对应：CategoryMapper / Category 实体 / CategoryServiceImpl
-- -----------------------------------------------------------------------------
CREATE TABLE `category` (
    `id`             int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_name`  varchar(50)  NOT NULL COMMENT '分类名称',
    `category_alias` varchar(50)  NOT NULL COMMENT '分类别名',
    `create_user`    int unsigned NOT NULL COMMENT '创建人ID',
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    -- list / findById / update / delete 都带 create_user 条件，走这个索引
    KEY `idx_create_user` (`create_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章分类表';


-- -----------------------------------------------------------------------------
-- 文章表
-- 对应：ArticleMapper / Article 实体 / ArticleServiceImpl
-- -----------------------------------------------------------------------------
CREATE TABLE `article` (
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`       varchar(100) NOT NULL                COMMENT '文章标题（校验 1-10 位，留足余量）',
    `content`     mediumtext   NOT NULL                COMMENT '文章内容（长文本，绝对不能建成 varchar）',
    `cover_img`   varchar(512) NOT NULL                COMMENT '封面地址（OSS 长 URL，必须给足）',
    `state`       varchar(10)  NOT NULL DEFAULT '草稿' COMMENT '状态：只能是[已发布]或者[草稿]',
    `category_id` int unsigned NOT NULL                COMMENT '文章分类ID',
    `create_user` int unsigned NOT NULL                COMMENT '创建人ID',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    -- 列表查询固定是 where create_user=? and category_id=? and state=?，
    -- 这个复合索引能一次覆盖三种条件组合（最左前缀 create_user 必定存在）
    KEY `idx_query` (`create_user`, `category_id`, `state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章表';


-- =============================================================================
--  为什么字段要这么长？（当初部署报错的几个原因）
-- =============================================================================
--  1. password —— 最可能的报错来源
--     报错：Data too long for column 'password' at row 1
--     PasswordUtil 用的是 BCrypt，生成的哈希固定 60 个字符（形如 $2a$10$...），
--     而教程原版建表是 varchar(32)（只够 MD5 的 32 位），注册/改密时直接插入失败。
--     这里给到 varchar(100)，以后换加密算法也够用。
--
--  2. user_pic / cover_img
--     报错：Data too long for column 'user_pic'
--     OSS 返回的地址本身就长，中文文件名还会被 URL 编码成 %E4%B8%AD 这种，
--     一个中文占 9 个字符，原来的 varchar(128) 很容易撑爆。这里给到 512。
--
--  3. content
--     报错：Row size too large / Data too long for column 'content'
--     教程原版是 varchar(10000)，utf8mb4 下每字符占 4 字节，单这一列就 40000 字节，
--     逼近 MySQL 单行 65535 字节的上限，加几个字段就建表失败。
--     mediumtext 不占行内空间，最大 16MB，存富文本文章绰绰有余。
--
--  4. state
--     报错：Incorrect string value: '\xE5\xB7\xB2...' for column 'state'
--     存的是中文“已发布/草稿”，如果库或表是 latin1 字符集就存不进去。
--     本脚本库、表、列三层都显式指定了 utf8mb4，不会再有这个问题。
--
--  5. 时间
--     报错：The server time zone value '中国标准时间' is unrecognized
--     这是连接串的问题，不是建表问题，见文件末尾。
--
--  另外：这里故意没有建 article -> category 的外键。
--  代码里删除分类时没有级联处理，建了外键会导致“删分类”直接失败。
--  关联关系靠 idx_query 索引保证查询性能即可。


-- =============================================================================
--  导入命令
-- =============================================================================
--  Linux / Mac：
--    mysql -u root -p --default-character-set=utf8mb4 < big_event.sql
--
--  Windows（cmd，注意必须在 cmd 里执行，PowerShell 的 < 重定向会报错）：
--    mysql -u root -p --default-character-set=utf8mb4 < big_event.sql
--
--  Navicat / DataGrip：直接右键“运行 SQL 文件”，通常会自动识别 UTF-8。
--  如果导入后中文变成 ??? 或乱码，就是客户端字符集不对，加上
--  --default-character-set=utf8mb4 重新导入即可。
--
--  导入完成后建议顺手改掉后端 application.yml 里的连接串：
--    url: jdbc:mysql://localhost:3306/big_event?useUnicode=true&characterEncoding=utf8\
--         &useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
--  （serverTimezone 解决“中国标准时间”报错，characterEncoding 保证读写中文不乱码，
--   useSSL=false 避免服务器没配 SSL 时的连接告警）
