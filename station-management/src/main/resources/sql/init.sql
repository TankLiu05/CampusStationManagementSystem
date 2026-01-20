-- 初始化数据库脚本（MySQL）
-- 根据当前 JPA 实体结构创建表，并插入一个默认超级管理员及其角色范围

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS wuliu CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE wuliu;

DROP TABLE IF EXISTS admin_role_scope;
DROP TABLE IF EXISTS parcel_route;
DROP TABLE IF EXISTS parcel;
DROP TABLE IF EXISTS location;
DROP TABLE IF EXISTS notice;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_admin;

CREATE TABLE sys_admin (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  username     VARCHAR(50)  NOT NULL,
  password     VARCHAR(100) NOT NULL,
  phone        VARCHAR(20)          DEFAULT NULL,
  email        VARCHAR(100)         DEFAULT NULL,
  status       TINYINT      NOT NULL,
  role         VARCHAR(20)  NOT NULL,
  create_time  DATETIME(6)  NOT NULL,
  update_time  DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_admin_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_user (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  username     VARCHAR(50)  NOT NULL,
  password     VARCHAR(100) NOT NULL,
  phone        VARCHAR(20)          DEFAULT NULL,
  email        VARCHAR(100)         DEFAULT NULL,
  status       TINYINT      NOT NULL,
  role         VARCHAR(20)          DEFAULT NULL,
  create_time  DATETIME(6)  NOT NULL,
  update_time  DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notice (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  title        VARCHAR(200) NOT NULL,
  content      TEXT         NOT NULL,
  creator_id   BIGINT               DEFAULT NULL,
  creator_name VARCHAR(50)          DEFAULT NULL,
  create_time  DATETIME(6) NOT NULL,
  update_time  DATETIME(6) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE location (
  id             BIGINT       NOT NULL AUTO_INCREMENT,
  user_id        BIGINT       NOT NULL,
  username       VARCHAR(50)  NOT NULL,
  phone          VARCHAR(20)          DEFAULT NULL,
  province       VARCHAR(50)          DEFAULT NULL,
  city           VARCHAR(50)          DEFAULT NULL,
  street         VARCHAR(100)         DEFAULT NULL,
  detail_address VARCHAR(255)         DEFAULT NULL,
  create_time    DATETIME(6)  NOT NULL,
  update_time    DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  KEY idx_location_user_id (user_id),
  CONSTRAINT fk_location_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE parcel (
  id             BIGINT       NOT NULL AUTO_INCREMENT,
  tracking_number VARCHAR(50) NOT NULL,
  company        VARCHAR(50)          DEFAULT NULL,
  receiver_id    BIGINT               DEFAULT NULL,
  receiver_name  VARCHAR(50)          DEFAULT NULL,
  receiver_phone VARCHAR(20)          DEFAULT NULL,
  origin         VARCHAR(100)         DEFAULT NULL,
  destination    VARCHAR(100)         DEFAULT NULL,
  location       VARCHAR(100)         DEFAULT NULL,
  pickup_code    VARCHAR(6)           DEFAULT NULL,
  status         INT                  DEFAULT 0,
  is_signed      INT                  DEFAULT 0,
  create_time    DATETIME(6) NOT NULL,
  update_time    DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_parcel_receiver_id (receiver_id),
  CONSTRAINT fk_parcel_receiver FOREIGN KEY (receiver_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE parcel_route (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  tracking_number VARCHAR(50)  NOT NULL,
  current_station VARCHAR(100) NOT NULL,
  next_station    VARCHAR(100)         DEFAULT NULL,
  eta_next_station DATETIME(6)        DEFAULT NULL,
  eta_delivered   DATETIME(6)         DEFAULT NULL,
  create_time     DATETIME(6) NOT NULL,
  update_time     DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_route_tracking_number (tracking_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE admin_role_scope (
  id             BIGINT       NOT NULL AUTO_INCREMENT,
  admin_id       BIGINT       NOT NULL,
  parent_admin_id BIGINT              DEFAULT NULL,
  role           VARCHAR(20)  NOT NULL,
  province       VARCHAR(50)          DEFAULT NULL,
  city           VARCHAR(50)          DEFAULT NULL,
  station_id     BIGINT               DEFAULT NULL,
  create_time    DATETIME(6) NOT NULL,
  update_time    DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_scope_admin_id (admin_id),
  KEY idx_scope_parent_admin_id (parent_admin_id),
  CONSTRAINT fk_scope_admin FOREIGN KEY (admin_id) REFERENCES sys_admin (id),
  CONSTRAINT fk_scope_parent_admin FOREIGN KEY (parent_admin_id) REFERENCES sys_admin (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO sys_admin (username, password, phone, email, status, role, create_time, update_time)
VALUES ('superadmin', '123456', NULL, NULL, 1, 'SUPERADMIN', NOW(6), NOW(6));

INSERT INTO admin_role_scope (admin_id, parent_admin_id, role, province, city, station_id, create_time, update_time)
SELECT id, NULL, 'SUPERADMIN', NULL, NULL, NULL, NOW(6), NOW(6)
FROM sys_admin
WHERE username = 'superadmin'
LIMIT 1;

