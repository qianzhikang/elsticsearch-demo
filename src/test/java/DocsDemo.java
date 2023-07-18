import com.alibaba.fastjson.JSON;
import com.qzk.es.Application;
import com.qzk.es.entity.Shop;
import com.qzk.es.entity.ShopDto;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description 文档操作
 * @Date 2023-07-04-14-05
 * @Author qianzhikang
 */
@SpringBootTest(classes = Application.class)
public class DocsDemo {
    @Resource
    private RestHighLevelClient restHighLevelClient;


    ShopDto init() {
        Shop shop = new Shop();
        shop.setId(10100001L);
        shop.setBrand("nobrand");
        shop.setPrice(10000);
        shop.setScore(45);
        shop.setName("noname");
        shop.setAddress("local");
        shop.setLatitude("31.2497");
        shop.setLongitude("120.3925");
        return new ShopDto(shop);
    }


    List<ShopDto> initList() {
        Shop shop1 = new Shop();
        shop1.setId(10100001L);
        shop1.setBrand("苹果");
        shop1.setPrice(10000);
        shop1.setScore(45);
        shop1.setName("苹果");
        shop1.setAddress("local");
        shop1.setLatitude("31.2497");
        shop1.setLongitude("120.3925");

        Shop shop2 = new Shop();
        shop2.setId(10100002L);
        shop2.setBrand("菠萝");
        shop2.setPrice(20000);
        shop2.setScore(55);
        shop2.setName("菠萝");
        shop2.setAddress("local");
        shop2.setLatitude("33.2497");
        shop2.setLongitude("120.3925");

        Shop shop3 = new Shop();
        shop3.setId(10100003L);
        shop3.setBrand("苹果");
        shop3.setPrice(30000);
        shop3.setScore(65);
        shop3.setName("苹果2店");
        shop3.setAddress("local");
        shop3.setLatitude("32.2497");
        shop3.setLongitude("120.3925");

        List<ShopDto> shopDtos = new ArrayList<>();
        shopDtos.add(new ShopDto(shop1));
        shopDtos.add(new ShopDto(shop2));
        shopDtos.add(new ShopDto(shop3));


        return shopDtos;

    }

    /**
     * 新增文档
     */
    @Test
    void insertDoc() throws IOException {
        // 初始化 shop
        ShopDto shopDto = init();
        // 转为json
        String json = JSON.toJSONString(shopDto);
        // 创建新增请求对象，指定索引库和文档id
        IndexRequest request = new IndexRequest("shop").id(shopDto.getId().toString());
        // 配置json文档
        request.source(json, XContentType.JSON);
        // 执行新增
        restHighLevelClient.index(request, RequestOptions.DEFAULT);
    }


    /**
     * 批量新增
     */
    @Test
    void bulkDocs() throws IOException {
        // 初始化一个shop的list集合
        List<ShopDto> shopDtos = initList();
        // 创建批量请求
        BulkRequest request = new BulkRequest();
        // 准备参数，添加多个新增的Request
        shopDtos.forEach(item -> request.add(new IndexRequest("shop")
                .id(item.getId().toString()).source(JSON.toJSONString(item), XContentType.JSON)));
        // 执行批量处理
        restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
    }


    /**
     * 简单查询文档
     */
    @Test
    void getDocsById() throws IOException {
        // 创建查询请求
        GetRequest request = new GetRequest("shop", "10100001");
        // 执行，获取结果
        GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
        // 解析结果
        String json = response.getSourceAsString();

        System.out.println(json);
    }


    /**
     * 删除文档
     */
    @Test
    void deleteDoc() throws IOException {
        // 准备Request
        DeleteRequest request = new DeleteRequest("shop", "10100003");
        // 执行
        restHighLevelClient.delete(request, RequestOptions.DEFAULT);
    }

    /**
     * 修改文档
     */
    @Test
    void UpdateDoc() throws IOException {
        // 构建增量修改的请求
        UpdateRequest request = new UpdateRequest("shop", "10100003");
        // 需要修改的k-v对
        //request.doc("name","newname","price",50000);
        request.doc("isAd",true);
        // 执行更新
        restHighLevelClient.update(request,RequestOptions.DEFAULT);
    }


}
