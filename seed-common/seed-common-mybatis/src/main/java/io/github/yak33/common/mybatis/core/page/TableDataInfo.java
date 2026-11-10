package io.github.yak33.common.mybatis.core.page;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页响应：与 R 平级直接返回（不带 code 包装）
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Data
public class TableDataInfo<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 当前页数据 */
    private List<T> rows;

    public static <T> TableDataInfo<T> build(long total, List<T> rows) {
        TableDataInfo<T> info = new TableDataInfo<>();
        info.setTotal(total);
        info.setRows(rows);
        return info;
    }
}
