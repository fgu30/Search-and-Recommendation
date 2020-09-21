package com.bin.spark.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * Created by 斌~
 * 2020/9/21 10:35
 */
@Component
public class CanalClient implements DisposableBean {


    private CanalConnector canalConnector;

    @Bean
    public CanalConnector getCanalConnector(){
        canalConnector = CanalConnectors.newClusterConnector(Lists.newArrayList(
                new InetSocketAddress("127.0.0.1",11111)) ,
                "example","canal","canal"
        );
        //连接
        canalConnector.connect();
        //格式{database}.{table}
        canalConnector.subscribe();
        //回滚寻找上次终端的位置
        canalConnector.rollback();
        return canalConnector;
    }

    @Override
    public void destroy() throws Exception {
        if(canalConnector!=null){
            canalConnector.disconnect();
        }
    }
}
