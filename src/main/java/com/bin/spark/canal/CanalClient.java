//package com.bin.spark.canal;
//
//import com.alibaba.google.common.collect.Lists;
//import com.alibaba.otter.canal.client.CanalConnector;
//import com.alibaba.otter.canal.client.CanalConnectors;
//import org.springframework.beans.factory.DisposableBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.stereotype.Component;
//
//import java.net.InetSocketAddress;
//
///**
// * @author mac
// * canal客户端接入
// */
//@Component
//public class CanalClient implements DisposableBean{
//
//    private CanalConnector canalConnector;
//
//    @Bean
//    public CanalConnector getCanalConnector(){
//        canalConnector = CanalConnectors.newClusterConnector(Lists.newArrayList(
//                new InetSocketAddress("127.0.0.1", 11111)),
//                "example",
//                "canal",
//                "canal"
//        );
//        canalConnector.connect();
//        // 指定filter，格式{database}.{table}，不传参数就是 subscribe 所有的内容
////        canalConnector.subscribe();
//        canalConnector.subscribe(".*\\..*");
//        // 回滚寻找上次中断的位置
//        canalConnector.rollback();
//        return canalConnector;
//    }
//
//    /**
//     * 容器销毁时调用
//     * @throws Exception
//     */
//    @Override
//    public void destroy() throws Exception {
//        if(canalConnector != null){
//            canalConnector.disconnect();
//        }
//    }
//}
