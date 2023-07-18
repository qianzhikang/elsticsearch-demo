package com.qzk.es.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description dto
 * @Date 2023-07-04-14-09
 * @Author qianzhikang
 */
@Data
@NoArgsConstructor
public class ShopDto {
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
    // 经度，纬度
    private String location;

    public ShopDto(Shop shop) {
        this.id = shop.getId();
        this.name = shop.getName();
        this.address = shop.getAddress();
        this.price = shop.getPrice();
        this.score = shop.getScore();
        this.brand = shop.getBrand();
        this.location = shop.getLatitude() + "," + shop.getLongitude();
    }
}
