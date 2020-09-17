package com.bin.spark.config;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpPost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
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
public class ElasticSearchRestClient {

    @Value("${elasticsearch.ip}")
    String ipAddress;

    @Bean(name = "highLevelClient")
    public RestHighLevelClient highLevelClient(){
        String[] address = ipAddress.split(":");
        String ip =address[0];
        int host = Integer.parseInt(address[1]);

        HttpHost httpHost = new HttpHost(ip,host,"http");
        return new RestHighLevelClient(RestClient.builder(new HttpHost[]{httpHost}));
    }

}
