import com.alibaba.fastjson.JSON;
import com.qzk.es.Application;
import com.qzk.es.entity.ShopDto;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @Description DSL查询语句的使用demo
 * @Date 2023-07-14-08-58
 * @Author qianzhikang
 */
@SpringBootTest(classes = Application.class)
public class DSLQueryDemo {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * 全文查询
     */
    @Test
    void fullQuery() throws IOException {
        // 1.准备Request
        SearchRequest request = new SearchRequest("shop");
        // 2.准备DSL
        request.source()
                .query(QueryBuilders.matchQuery("all", "苹果"));
        // 3.发送请求
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        System.out.println(response);
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsString());
        }
    }


    /**
     * 精确查询：词条查询
     */
    @Test
    void termQuery() throws IOException {
        // 1.准备Request
        SearchRequest request = new SearchRequest("shop");

        // 2.准备DSL
        request.source()
                // term 词条查询，分数为45的
                .query(QueryBuilders.termQuery("score", "45"));

        // 3.发送请求
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        // 解析结果
        handleResponse(response);
    }


    /**
     * 范围查询
     */
    @Test
    void rangQuery() throws IOException {
        // 1.准备Request
        SearchRequest request = new SearchRequest("shop");

        // 2.准备DSL
        request.source()
                // term 词条查询，分数为45的
                .query(QueryBuilders.rangeQuery("price").gte(10000).lte(25000));

        // 3.发送请求
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        // 解析结果
        handleResponse(response);
    }


    /**
     * 调整文档算分排名查询
     */
    @Test
    void functionScoreQuery() throws IOException {
        FunctionScoreQueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(
                QueryBuilders.matchQuery("brand", "苹果"),      // 原始查询条件
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                QueryBuilders.termQuery("isAd", true),     // 过滤条件
                                ScoreFunctionBuilders.weightFactorFunction(5)           // 算分权重
                        )
                }
        );
        functionScoreQuery.boostMode(CombineFunction.MULTIPLY);         // 加权模式


        // 准备Request
        SearchRequest request = new SearchRequest("shop");
        request.source().query(functionScoreQuery);

        // 查询，处理结果
        SearchResponse res = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        handleResponse(res);
    }

    /**
     * 布尔查询
     */
    @Test
    void boolQuery() throws IOException {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 名字必须包含"苹果"
        boolQuery.must(QueryBuilders.termQuery("name", "苹果"));
        // 评分应该大于"65"
        boolQuery.should(QueryBuilders.rangeQuery("score").gte(65));
        // 价格必须大于等于30000
        boolQuery.filter(QueryBuilders.rangeQuery("price")
                .gte(30000));

        // 准备Request
        SearchRequest request = new SearchRequest("shop");
        request.source().query(boolQuery);

        // 查询，处理结果
        SearchResponse res = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        handleResponse(res);
    }


    /**
     * 地理位置查询
     */
    @Test
    void geoQuery() throws IOException {
        // 指定距离查询
        GeoDistanceQueryBuilder location = QueryBuilders
                // 文档中地理位置的字段
                .geoDistanceQuery("location")
                // 指定圆心
                .point(31.2497, 120.3925)
                // 指定距离
                .distance("3km");
        // 准备Request
        SearchRequest request = new SearchRequest("shop");
        request.source().query(location);
        // 查询，处理结果
        SearchResponse res = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        handleResponse(res);
    }


    /**
     * 地理位置排序查询
     */
    @Test
    void geoQuerySort() throws IOException {

        GeoDistanceSortBuilder location = SortBuilders
                // 距离排序，参数为 文档中经纬度字段 和 圆心位置
                .geoDistanceSort("location", new GeoPoint(31.2497, 120.3925))
                // 距离升序
                .order(SortOrder.ASC)
                // 单位km
                .unit(DistanceUnit.KILOMETERS);

        // 准备Request
        SearchRequest request = new SearchRequest("shop");
        request.source().sort(location);
        // 查询，处理结果
        SearchResponse res = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        handleResponse(res);
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
