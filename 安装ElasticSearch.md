# 安装ElasticSearch

## 安装之前

首先设置`max_map_count`，默认为65530es会启动不起来
1.查看max_map_count的值

```sh
cat /proc/sys/vm/max_map_count
```

2.重新设置`max_map_count`的值

```sh
sysctl -w vm.max_map_count=262144
```

3.创建虚拟网络供容器使用

```sh
docker network create es-net
```

## 拉取镜像并启动

1.拉取镜像

```sh
docker pull elasticsearch:7.12.1
```

2.创建容器并启动

```sh
docker run -d \
    --name es \
    -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
    -e "discovery.type=single-node" \
    -v es-data:/usr/share/elasticsearch/data \
    -v es-plugins:/usr/share/elasticsearch/plugins \
    --privileged \
    --network es-net \
    -p 9200:9200 \
    -p 9300:9300 \
elasticsearch:7.12.1
```

命令解释：

- `-e "cluster.name=es-docker-cluster"`：设置集群名称
- `-e "http.host=0.0.0.0"`：监听的地址，可以外网访问
- `-e "ES_JAVA_OPTS=-Xms512m -Xmx512m"`：内存大小
- `-e "discovery.type=single-node"`：非集群模式
- `-v es-data:/usr/share/elasticsearch/data`：挂载逻辑卷，绑定es的数据目录
- `-v es-plugins:/usr/share/elasticsearch/plugins`：挂载逻辑卷，绑定es的插件目录
- `--privileged`：授予逻辑卷访问权
- `--network es-net` ：加入一个名为es-net的网络中
- `-p 9200:9200`：端口映射配置

## 测试连接

访问：http://服务器IP:9200/ 进行测试

成功则显示如下：

![image-20230411150617647](https://pic-go.oss-cn-shanghai.aliyuncs.com/typora-img/202304111506758.png)

# 安装kibana

## 拉取镜像

```sh
docker pull kibana:7.12.1
```

## 创建容器并启动

```sh
docker run -d \
--name kibana \
-e ELASTICSEARCH_HOSTS=http://es:9200 \
--network=es-net \
-p 5601:5601  \
kibana:7.12.1
```

命令解释：

- `--network es-net` ：加入一个名为es-net的网络中，与elasticsearch在同一个网络中
- `-e ELASTICSEARCH_HOSTS=http://es:9200"`：设置elasticsearch的地址，因为kibana已经与elasticsearch在一个网络，因此可以用容器名直接访问elasticsearch
- `-p 5601:5601`：端口映射配置

访问： http://ip:5601/ 即可进入控制台![image-20230411151401374](https://pic-go.oss-cn-shanghai.aliyuncs.com/typora-img/202304111514410.png)

# 安装中文分词器IK

## 安装

**1.查看数据卷挂载目录**

```sh
docker volume inspect es-plugins
```

![image-20230704095051386](https://pic-go.oss-cn-shanghai.aliyuncs.com/typora-img/202307040950419.png)

**2.下载ik分词器**

传送门：https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.12.1/elasticsearch-analysis-ik-7.12.1.zip

**3.上传**

上传到上文的挂载目录`/var/lib/docker/volumes/es-plugins/_data`

**4.新建文件夹ik并解压**

```sh
# 进入挂载目录
cd /var/lib/docker/volumes/es-plugins/_data
# 新建文件夹ik
mkdir ik
# 移动zip文件
mv elasticsearch-analysis-ik-7.12.1.zip ik
# 解压缩
unzip elasticsearch-analysis-ik-7.12.1.zip
```

**5.重启容器**

```sh
docker restart es
```



## 测试

```sh
# 查看es日志
docker logs -f es
```

![image-20230411153106074](https://pic-go.oss-cn-shanghai.aliyuncs.com/typora-img/202304111531114.png)

成功加载ik插件

## ik_smart 模式

粗粒度划分

```json
POST /_analyze
{
  "text": "为世界上所有的美好而战！",
  "analyzer": "ik_smart"
}
```

![image-20230411153323338](https://pic-go.oss-cn-shanghai.aliyuncs.com/typora-img/202304111533375.png)



## ik_max_word 模式

最细粒度划分

```json
POST /_analyze
{
  "text": "为世界上所有的美好而战！",
  "analyzer": "ik_max_word"
}
```

![image-20230411153604096](https://pic-go.oss-cn-shanghai.aliyuncs.com/typora-img/202304111536133.png)