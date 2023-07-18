import com.alibaba.fastjson.JSON;
import com.qzk.es.Application;
import com.qzk.es.entity.ShopDto;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * @Description 查询结果处理
 * @Date 2023-07-17-11-06
 * @Author qianzhikang
 */
@SpringBootTest(classes = Application.class)
public class ResponseResolveDemo {


    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * 排序、分页
     */
    @Test
    void pageAndSort() throws IOException {
        // 页码，每页大小
        int page = 1, size = 2;
        // 1.准备Request
        SearchRequest request = new SearchRequest("shop");
        // 2.准备DSL
        // 2.1.query
        request.source().query(QueryBuilders.matchAllQuery());
        // 2.2.排序 sort
        request.source().sort("price", SortOrder.DESC);
        // 2.3.分页 from、size
        request.source().from((page - 1) * size).size(size);
        // 3.发送请求
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        // 4.解析响应
        handleResponse(response);
    }

    /**
     * 高亮搜索关键字
     */
    @Test
    void highlightField() throws IOException {
        SearchRequest request = new SearchRequest("shop");
        request.source().query(QueryBuilders.matchQuery("all", "苹果"));
        // 高亮检索配置
        request.source().highlighter(new HighlightBuilder()
                // 那些字段需要匹配高亮
                .field("name").field("brand")
                // 是否只高亮显示指定的字段，由于此例中，查询的是复合字段all，所以只能为false
                .requireFieldMatch(false));
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        // 处理高亮
        handleHighlightResp(response);
    }


    private void handleHighlightResp(SearchResponse response) {
        // 文档数组
        SearchHit[] hits = response.getHits().getHits();
        // 遍历
        for (SearchHit hit : hits) {
            // 获取文档source
            String json = hit.getSourceAsString();
            // 反序列化
            ShopDto shopDto = JSON.parseObject(json, ShopDto.class);

            // 处理高亮关键字
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (!CollectionUtils.isEmpty(highlightFields)) {
                // 处理name高亮
                HighlightField nameField = highlightFields.get("name");
                if (nameField != null) {
                    // 获取高亮值
                    String name = nameField.getFragments()[0].string();
                    // 覆盖非高亮结果
                    shopDto.setName(name);
                }

                // 处理brand高亮
                HighlightField brandField = highlightFields.get("brand");
                if (brandField != null) {
                    // 获取高亮值
                    String brand = brandField.getFragments()[0].string();
                    // 覆盖非高亮结果
                    shopDto.setBrand(brand);
                }
            }

            System.out.println("shop = " + shopDto);
        }
    }


    private void handleResponse(SearchResponse response) {
        // 4.解析响应
        SearchHits searchHits = response.getHits();
        // 4.1.获取总条数
        long total = searchHits.getTotalHits().value;
        System.out.println("共搜索到" + total + "条数据");
        // 4.2.文档数组
        SearchHit[] hits = searchHits.getHits();
        // 4.3.遍历
        for (SearchHit hit : hits) {
            System.out.println(hit.getScore() + " 分");
            // 获取文档source
            String json = hit.getSourceAsString();
            // 反序列化
            ShopDto shopDto = JSON.parseObject(json, ShopDto.class);
            System.out.println("shop = " + shopDto);
        }
    }
}

