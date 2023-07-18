package com.qzk.es.entity;

import lombok.Data;

/**
 * @Description 实体类
 * @Date 2023-07-04-10-23
 * @Author qianzhikang
 */
@Data
public class Shop {
    // id
    private Long id;
    // 名字
    private String name;
    // 地址
    private String address;
    // 人均价格 * 100
    private int price;
    // 评分 * 10
    private int score;
    // 品牌
    private String brand;
    // 纬度
    private String latitude;
    // 经度
    private String longitude;
}
