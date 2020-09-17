package com.bin.spark.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 斌~
 * @version 1.0
 * @date 2020/9/17 11:14 下午
 */
@Configuration
public class ElasticsearchRestClient {

    @Value("${es.host}")
    String elasticsearchHost;

    @Value("${es.port}")
    Integer elasticsearchPort;

    @Value("${es.scheme}")
    String elasticsearchScheme;

    @Bean(name="highLevelClient")
    public RestHighLevelClient highLevelClient(){
        HttpHost httpHost = new HttpHost(elasticsearchHost,elasticsearchPort,elasticsearchScheme);
        return new RestHighLevelClient(RestClient.builder(httpHost));
    }
}
