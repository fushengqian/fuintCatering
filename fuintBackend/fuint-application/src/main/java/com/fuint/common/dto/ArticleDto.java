package com.fuint.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文章实体类
 * Created by FSQ
 */
@Data
public class ArticleDto implements Serializable {

    /**
    * 自增ID
    */
    private Integer id;

   /**
    * 标题
    */
    private String title;

    /**
     * 简介
     */
    private String brief;

    /**
     * 所属商户
     * */
    private Integer merchantId;

    /**
     * 所属店铺
     * */
    private Integer storeId;

    /**
     * 链接地址
     */
    private String url;

    /**
     * 点击数
     */
    private Long click;

   /**
    * 图片地址
    */
    private String image;

   /**
    * 描述
    */
    private String description;

   /**
    * 创建时间
    */
    private Date createTime;

   /**
    * 更新时间
    */
    private Date updateTime;

   /**
    * 最后操作人
    */
    private String operator;

   /**
    * A：正常；D：删除
    */
    private String status;

    /**
     * 排序
     * */
    private Integer sort;
}

