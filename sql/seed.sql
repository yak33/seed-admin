-- ============================================================
-- seed 骨架初始化脚本（MySQL 5.7+）
-- 五张 RBAC 表统一使用 id VARCHAR(36)，新数据由后端生成 UUID
-- 包含：5 张系统表、初始 admin/123456、super/common 角色、动态菜单、按钮权限
-- ============================================================

CREATE DATABASE IF NOT EXISTS seed_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE seed_admin;

DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id           VARCHAR(36)  NOT NULL COMMENT '用户ID（UUID）',
    user_name    VARCHAR(30)  NOT NULL COMMENT '登录账号',
    nick_name    VARCHAR(30)  NOT NULL COMMENT '用户昵称',
    email        VARCHAR(50)  DEFAULT '' COMMENT '邮箱',
    phonenumber  VARCHAR(11)  DEFAULT '' COMMENT '手机号',
    sex          CHAR(1)      DEFAULT '0' COMMENT '性别（0男 1女 2未知）',
    avatar       VARCHAR(255) DEFAULT '' COMMENT '头像地址',
    password     VARCHAR(100) DEFAULT '' COMMENT 'BCrypt 密码哈希',
    status       CHAR(1)      DEFAULT '0' COMMENT '状态（0正常 1停用）',
    del_flag     CHAR(1)      DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
    create_by    VARCHAR(36)  DEFAULT NULL COMMENT '创建者',
    create_time  DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_by    VARCHAR(36)  DEFAULT NULL COMMENT '更新者',
    update_time  DATETIME     DEFAULT NULL COMMENT '更新时间',
    remark       VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_name (user_name)
) ENGINE = InnoDB COMMENT = '用户表';

DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id          VARCHAR(36)  NOT NULL COMMENT '角色ID（UUID）',
    role_name   VARCHAR(30)  NOT NULL COMMENT '角色名称',
    role_key    VARCHAR(100) NOT NULL COMMENT '角色权限串（super 为超管标识）',
    role_sort   INT          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    status      CHAR(1)      DEFAULT '0' COMMENT '状态（0正常 1停用）',
    del_flag    CHAR(1)      DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
    create_by   VARCHAR(36)  DEFAULT NULL COMMENT '创建者',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_by   VARCHAR(36)  DEFAULT NULL COMMENT '更新者',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    remark      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE = InnoDB COMMENT = '角色表';

DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id          VARCHAR(36)  NOT NULL COMMENT '菜单ID（UUID）',
    menu_name   VARCHAR(50)  NOT NULL COMMENT '菜单标题',
    route_name  VARCHAR(100) DEFAULT NULL COMMENT '前端路由 name（全局唯一）',
    parent_id   VARCHAR(36)  DEFAULT '0' COMMENT '父菜单ID（0 为根）',
    order_num   INT          DEFAULT 0 COMMENT '显示顺序',
    path        VARCHAR(200) DEFAULT '' COMMENT '路由地址（目录 /xxx，页面相对路径）',
    component   VARCHAR(255) DEFAULT NULL COMMENT '组件路径（相对 src/views）',
    menu_type   CHAR(1)      DEFAULT '' COMMENT '类型（M目录 C菜单 F按钮）',
    visible     CHAR(1)      DEFAULT '0' COMMENT '显示状态（0显示 1隐藏）',
    status      CHAR(1)      DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
    perms       VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    icon        VARCHAR(100) DEFAULT '#' COMMENT '图标（iconify 名称）',
    create_by   VARCHAR(36)  DEFAULT NULL COMMENT '创建者',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_by   VARCHAR(36)  DEFAULT NULL COMMENT '更新者',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_route_name (route_name),
    UNIQUE KEY uk_menu_parent_name (parent_id, menu_name)
) ENGINE = InnoDB COMMENT = '菜单表';

DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id          VARCHAR(36) NOT NULL COMMENT '关联ID（UUID）',
    user_id     VARCHAR(36) NOT NULL COMMENT '用户ID',
    role_id     VARCHAR(36) NOT NULL COMMENT '角色ID',
    create_by   VARCHAR(36) DEFAULT NULL COMMENT '创建者',
    create_time DATETIME    DEFAULT NULL COMMENT '创建时间',
    update_by   VARCHAR(36) DEFAULT NULL COMMENT '更新者',
    update_time DATETIME    DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE = InnoDB COMMENT = '用户角色关联表';

DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id          VARCHAR(36) NOT NULL COMMENT '关联ID（UUID）',
    role_id     VARCHAR(36) NOT NULL COMMENT '角色ID',
    menu_id     VARCHAR(36) NOT NULL COMMENT '菜单ID',
    create_by   VARCHAR(36) DEFAULT NULL COMMENT '创建者',
    create_time DATETIME    DEFAULT NULL COMMENT '创建时间',
    update_by   VARCHAR(36) DEFAULT NULL COMMENT '更新者',
    update_time DATETIME    DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE = InnoDB COMMENT = '角色菜单关联表';

INSERT INTO sys_user (id, user_name, nick_name, password, status, del_flag, create_by, create_time, update_by, update_time, remark)
VALUES ('1', 'admin', '超级管理员', '$2a$10$Gv7sqWalNT1wdYzHfx1RQu9i52k5tf1edh6M4by7WmFGwu/wXqWPG', '0', '0', '1', NOW(), '1', NOW(), '内置超管，不可删除');

INSERT INTO sys_role (id, role_name, role_key, role_sort, status, del_flag, create_by, create_time, update_by, update_time, remark) VALUES
('1', '超级管理员', 'super',  1, '0', '0', '1', NOW(), '1', NOW(), '内置超管角色，不通过管理接口分配'),
('2', '普通角色',   'common', 2, '0', '0', '1', NOW(), '1', NOW(), '仅可访问工作台和个人中心');

INSERT INTO sys_user_role (id, user_id, role_id, create_by, create_time, update_by, update_time)
VALUES ('ur-admin-super', '1', '1', '1', NOW(), '1', NOW());

INSERT INTO sys_menu (id, menu_name, route_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time) VALUES
('1',   '仪表盘',   'Dashboard',  '0',   1,  '/dashboard', NULL,                          'M', '0', '0', NULL,                'lucide:layout-dashboard', '1', NOW(), '1', NOW()),
('101', '工作台',   'Workspace',  '1',   1,  'workspace',  '/dashboard/workspace/index', 'C', '0', '0', NULL,                'carbon:workspace',       '1', NOW(), '1', NOW()),
('102', '个人中心', 'Profile',    '1',   99, 'profile',    '/_core/profile/index',       'C', '1', '0', NULL,                'lucide:user',            '1', NOW(), '1', NOW()),
('2',   '系统管理', 'System',     '0',   10, '/system',    NULL,                          'M', '0', '0', NULL,                'lucide:settings',        '1', NOW(), '1', NOW()),
('201', '用户管理', 'SystemUser', '2',   1,  'user',       '/system/user/index',         'C', '0', '0', 'system:user:list',   'lucide:users',           '1', NOW(), '1', NOW()),
('2011','用户新增', NULL,         '201', 1,  '',           NULL,                          'F', '0', '0', 'system:user:add',    '#',                      '1', NOW(), '1', NOW()),
('2012','用户修改', NULL,         '201', 2,  '',           NULL,                          'F', '0', '0', 'system:user:edit',   '#',                      '1', NOW(), '1', NOW()),
('2013','用户删除', NULL,         '201', 3,  '',           NULL,                          'F', '0', '0', 'system:user:remove', '#',                      '1', NOW(), '1', NOW()),
('202', '角色管理', 'SystemRole', '2',   2,  'role',       '/system/role/index',         'C', '0', '0', 'system:role:list',   'lucide:shield',          '1', NOW(), '1', NOW()),
('2021','角色新增', NULL,         '202', 1,  '',           NULL,                          'F', '0', '0', 'system:role:add',    '#',                      '1', NOW(), '1', NOW()),
('2022','角色修改', NULL,         '202', 2,  '',           NULL,                          'F', '0', '0', 'system:role:edit',   '#',                      '1', NOW(), '1', NOW()),
('2023','角色删除', NULL,         '202', 3,  '',           NULL,                          'F', '0', '0', 'system:role:remove', '#',                      '1', NOW(), '1', NOW()),
('203', '菜单管理', 'SystemMenu', '2',   3,  'menu',       '/system/menu/index',         'C', '0', '0', 'system:menu:list',   'lucide:menu',            '1', NOW(), '1', NOW()),
('2031','菜单新增', NULL,         '203', 1,  '',           NULL,                          'F', '0', '0', 'system:menu:add',    '#',                      '1', NOW(), '1', NOW()),
('2032','菜单修改', NULL,         '203', 2,  '',           NULL,                          'F', '0', '0', 'system:menu:edit',   '#',                      '1', NOW(), '1', NOW()),
('2033','菜单删除', NULL,         '203', 3,  '',           NULL,                          'F', '0', '0', 'system:menu:remove', '#',                      '1', NOW(), '1', NOW());

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time) VALUES
('rm-common-dashboard', '2', '1', '1', NOW(), '1', NOW()),
('rm-common-workspace', '2', '101', '1', NOW(), '1', NOW()),
('rm-common-profile', '2', '102', '1', NOW(), '1', NOW());
