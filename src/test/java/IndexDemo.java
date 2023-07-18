import com.qzk.es.Application;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @Description 索引映射操作
 * @Date 2023-07-04-10-07
 * @Author qianzhikang
 */
@SpringBootTest(classes = Application.class)
public class IndexDemo {

    @Resource
    private RestHighLevelClient restHighLevelClient;


    private final String INDEX_JSON = "{\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"name\": {\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_max_word\",\n" +
            "        \"copy_to\": \"all\"\n" +
            "      },\n" +
            "      \"address\": {\n" +
            "        \"type\": \"keyword\",\n" +
            "        \"index\": false\n" +
            "      },\n" +
            "      \"price\": {\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"score\": {\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"brand\": {\n" +
            "        \"type\": \"keyword\",\n" +
            "        \"copy_to\": \"all\"\n" +
            "      },\n" +
            "      \"location\": {\n" +
            "        \"type\": \"geo_point\"\n" +
            "      },\n" +
            "      \"all\":{\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_max_word\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    /**
     * 创建索引库和映射
     */
    @Test
    void createIndexDemo() throws IOException {
        // 创建索引库创建请求
        CreateIndexRequest request = new CreateIndexRequest("shop");
        // 构建mapping映射
        request.source(INDEX_JSON, XContentType.JSON);
        // 执行
        restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
    }

    /**
     * 删除索引库
     */
    @Test
    void deleteIndexDemo() throws IOException {
        // 创建索引库删除请求
        DeleteIndexRequest request = new DeleteIndexRequest("shop");
        // 执行
        restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
    }

    /**
     * 判断索引库是否存在
     * 本质就是Get查询
     */
    @Test
    void GetIndexDemo() throws IOException {
        // 创建索引库查询请求
        GetIndexRequest request = new GetIndexRequest("shop");
        // 执行，使用exists查询是否存在，返回布尔值
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists ? "已存在" : "不存在");
    }
}

