package com.fuint.framework.pagination;

import java.io.Serializable;
import java.util.Map;

/**
 * 分页实体对象
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public class PaginationRequest implements Serializable {

    private static final long serialVersionUID = -344484321130132260L;

    /**
     * 当前页码
     */
    private int currentPage;
    /**
     * 每页大小
     */
    private int pageSize;
    /**
     * 排序字段
     */
    private String[] sortColumn;
    /**
     * 排序类型
     */
    private String sortType;
    /**
     * 分页查询参数
     */
    private Map<String, Object> searchParams;

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String[] getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = new String[]{sortColumn};
    }

    public void setSortColumn(String[] sortColumns) {
        this.sortColumn = sortColumns;
    }

    public String getSortType() {
        return sortType;
    }

    public void setSortType(String sortType) {
        this.sortType = sortType;
    }

    public Map<String, Object> getSearchParams() {
        return searchParams;
    }

    public void setSearchParams(Map<String, Object> searchParams) {
        this.searchParams = searchParams;
    }
}
