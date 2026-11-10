package io.github.yak33.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.yak33.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体（对应表 sys_user）
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /** 用户ID */
    @TableId(value = "id", type = IdType.INPUT)
    private String userId;

    /** 登录账号 */
    private String userName;

    /** 用户昵称 */
    private String nickName;

    private String email;

    private String phonenumber;

    /** 性别（0男 1女 2未知） */
    private String sex;

    private String avatar;

    /** BCrypt 密码哈希 */
    private String password;

    /** 状态（0正常 1停用） */
    private String status;

    /** 删除标志（0存在 2删除），@TableLogic 由 MP 自动追加条件 */
    @TableLogic
    private String delFlag;

    private String remark;
}
