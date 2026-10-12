-- =============================================================================
--  大事件 (big-event) 数据库升级脚本 —— 已有数据、不想重建库表时用这个
--
--  作用：把教程默认的旧表结构，改成能装下 BCrypt 密码、长 OSS 地址、
--        长文章内容、中文字符集的结构。只改结构，不动数据。
--
--  和 big_event.sql 的区别：这个脚本不会 DROP 任何东西，数据全部保留。
--  执行前仍然建议先备份：
--      mysqldump -u root -p --default-character-set=utf8mb4 big_event > backup.sql
-- =============================================================================

SET NAMES utf8mb4;

USE `big_event`;


-- -----------------------------------------------------------------------------
-- 第 1 步：库和表的字符集统一成 utf8mb4
-- 解决报错：Incorrect string value: '\xE5\xB7\xB2...' for column 'state'
-- （“已发布/草稿”是中文，latin1 或 utf8mb3 存不下）
-- -----------------------------------------------------------------------------
ALTER DATABASE `big_event` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

ALTER TABLE `user`     CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `category` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `article`  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;


-- -----------------------------------------------------------------------------
-- 第 2 步（最关键）：password 加长到 100
-- 解决报错：Data too long for column 'password' at row 1
-- BCrypt 的哈希固定 60 个字符，旧的 varchar(32) 只够 MD5，注册和改密都会失败。
-- -----------------------------------------------------------------------------
ALTER TABLE `user`
    MODIFY COLUMN `password` varchar(100) NOT NULL COMMENT '密码哈希：BCrypt 60 位 / 兼容 MD5 32 位',
    MODIFY COLUMN `username` varchar(20)  NOT NULL COMMENT '用户名',
    MODIFY COLUMN `nickname` varchar(20)  DEFAULT NULL COMMENT '昵称',
    MODIFY COLUMN `email`    varchar(100) DEFAULT NULL COMMENT '邮箱',
    MODIFY COLUMN `user_pic` varchar(512) DEFAULT NULL COMMENT '头像地址';


-- -----------------------------------------------------------------------------
-- 第 3 步：分类名/别名加长（代码里 @NotEmpty 没限长度，前端能填多少就存多少）
-- -----------------------------------------------------------------------------
ALTER TABLE `category`
    MODIFY COLUMN `category_name`  varchar(50) NOT NULL COMMENT '分类名称',
    MODIFY COLUMN `category_alias` varchar(50) NOT NULL COMMENT '分类别名';


-- -----------------------------------------------------------------------------
-- 第 4 步：文章表三处加长
--   content   varchar(n) -> mediumtext  解决：Row size too large / Data too long
--   cover_img 加长到 512                解决：Data too long（OSS 长 URL）
--   state     加长到 10                 中文状态值 + utf8mb4
-- -----------------------------------------------------------------------------
ALTER TABLE `article`
    MODIFY COLUMN `title`     varchar(100) NOT NULL                COMMENT '文章标题',
    MODIFY COLUMN `content`   mediumtext   NOT NULL                COMMENT '文章内容',
    MODIFY COLUMN `cover_img` varchar(512) NOT NULL                COMMENT '封面地址',
    MODIFY COLUMN `state`     varchar(10)  NOT NULL DEFAULT '草稿' COMMENT '状态：已发布|草稿';


-- -----------------------------------------------------------------------------
-- 第 5 步：补索引（可选，数据量小时影响不大，加上查询更稳）
--
-- 注意：MySQL 不支持 ADD INDEX IF NOT EXISTS，如果这里报
--      ERROR 1061 (42000): Duplicate key name 'xxx'
-- 说明索引已经存在，忽略这条错误继续执行即可。
-- -----------------------------------------------------------------------------
ALTER TABLE `user`    ADD UNIQUE KEY `uk_username` (`username`);
ALTER TABLE `article` ADD KEY `idx_query` (`create_user`, `category_id`, `state`);
ALTER TABLE `category` ADD KEY `idx_create_user` (`create_user`);

-- 加唯一索引时如果报
--      ERROR 1062 (23000): Duplicate entry '...' for key 'uk_username'
-- 说明表里已经有重名用户了（旧数据没做唯一约束），需要先手动去重：
--      SELECT username, COUNT(*) FROM `user` GROUP BY username HAVING COUNT(*) > 1;
--      -- 确认要保留哪一条后，删掉多余的再重新执行这条 ALTER


-- =============================================================================
--  执行完之后
-- =============================================================================
--  1. 后端 application.yml 里的连接串建议补上参数：
--       url: jdbc:mysql://localhost:3306/big_event?useUnicode=true&characterEncoding=utf8\
--            &useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
--     解决报错：The server time zone value '中国标准时间' is unrecognized
--
--  2. 旧的 MD5 密码不用管。PasswordUtil 里做了兼容：老用户下次登录时
--     会先按 MD5 校验通过，然后自动把密码原地升级成 BCrypt。
--     但要注意 —— varchar(32) 没加长的话，这个升级动作本身就会插入失败，
--     所以第 2 步必须先执行。
