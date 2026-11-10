package io.github.yak33.common.mybatis.core.page;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.yak33.common.core.exception.ServiceException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分页查询入参：Controller 直接以 query string 绑定
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Data
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 500;

    @Min(value = 1, message = "页码最小为 1")
    private Integer pageNum;

    @Min(value = 1, message = "每页条数最小为 1")
    @Max(value = MAX_PAGE_SIZE, message = "每页条数最大为 {max}")
    private Integer pageSize;

    /**
     * 构造 MP 分页对象；页码或条数非法时直接抛业务异常而不是静默纠正，
     * 让调用方尽早发现传参问题
     */
    public <T> Page<T> build() {
        if (pageNum == null || pageSize == null) {
            throw new ServiceException("分页参数不完整：pageNum=" + pageNum + ", pageSize=" + pageSize);
        }
        return Page.of(pageNum, pageSize);
    }

    public Integer getPageNumOrDefault() {
        return pageNum == null ? DEFAULT_PAGE_NUM : pageNum;
    }

    public Integer getPageSizeOrDefault() {
        return pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
    }
}
