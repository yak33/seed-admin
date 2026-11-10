package io.github.yak33.common.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import io.github.yak33.common.satoken.utils.LoginHelper;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * 审计字段自动填充：create_by/create_time/update_by/update_time
 *
 * @author ZHANGCHAO 2026/11/09
 */
public class InjectionMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        String userId = LoginHelper.getUserIdOrNull();
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createBy", String.class, userId);
        this.strictInsertFill(metaObject, "updateBy", String.class, userId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        String userId = LoginHelper.getUserIdOrNull();
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", String.class, userId);
    }
}
