//package com.bin.spark.canal;
//
//import com.alibaba.otter.canal.client.CanalConnector;
//import com.alibaba.otter.canal.protocol.CanalEntry;
//import com.alibaba.otter.canal.protocol.Message;
//import com.bin.spark.mapper.ShopModelMapper;
//import com.google.protobuf.InvalidProtocolBufferException;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.elasticsearch.action.delete.DeleteRequest;
//import org.elasticsearch.action.index.IndexRequest;
//import org.elasticsearch.client.RequestOptions;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * @author mac
// * canal 渠道数据处理
// */
//@Component
//@Slf4j
//public class CanalScheduling implements Runnable, ApplicationContextAware {
//
//    private ApplicationContext applicationContext;
//
//    @Autowired
//    private ShopModelMapper shopModelMapper;
//
//    @Resource
//    private CanalConnector canalConnector;
//
//    @Qualifier("highLevelClient")
//    @Autowired
//    private RestHighLevelClient restHighLevelClient;
//
//    /**
//     * 100毫秒 执行一次
//     */
//    @Override
//    @Scheduled(fixedDelay = 100)
//    public void run() {
//        long batchId = -1;
//        try{
//            int batchSize = 1000;
//            Message message = canalConnector.getWithoutAck(batchSize);
//            batchId = message.getId();
//            List<CanalEntry.Entry> entries = message.getEntries();
//            if(batchId != -1 && entries.size() > 0){
//                entries.forEach(entry -> {
//                    if(entry.getEntryType() == CanalEntry.EntryType.ROWDATA){
//                        // 解析处理
//                        publishCanalEvent(entry);
//                    }
//                });
//            }
//            canalConnector.ack(batchId);
//        }catch(Exception e){
//            e.printStackTrace();
//            canalConnector.rollback(batchId);
//        }
//    }
//
//    /**
//     * 将 binlog 中的一条（entry），
//     * 解析成受影响的记录（change），再逐条解析受影响的记录（change），
//     * 将记录（rowData）的数据结构从 List 转成 Map，
//     * 完了交给 indexES 方式索引进 ElasticSearch；
//     * @param entry binlog 中的一条；
//     */
//    private void publishCanalEvent(CanalEntry.Entry entry){
//        CanalEntry.EventType eventType = entry.getHeader().getEventType();
//        String database = entry.getHeader().getSchemaName();
//        String table = entry.getHeader().getTableName();
//        CanalEntry.RowChange change = null;
//        try {
//            change = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
//        } catch (InvalidProtocolBufferException e) {
//            e.printStackTrace();
//            return;
//        }
//        for (CanalEntry.RowData rowData :  change.getRowDatasList()){
//            List<CanalEntry.Column> columns = new ArrayList<>();
//            if(eventType.equals(CanalEntry.EventType.DELETE)){
//               columns = rowData.getBeforeColumnsList();
//            }else{
//                columns = rowData.getAfterColumnsList();
//            }
//            String primaryKey = "id";
//            CanalEntry.Column idColumn = columns.stream().filter(column -> column.getIsKey()
//                    && primaryKey.equals(column.getName())).findFirst().orElse(null);
//            Map<String,Object> dataMap = parseColumnsToMap(columns);
//            try{
//                indexES(dataMap, database, table,eventType);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
////        change.getRowDatasList().forEach(rowData -> {
////            List<CanalEntry.Column> columns = rowData.getAfterColumnsList();
////            String primaryKey = "id";
////            CanalEntry.Column idColumn = columns.stream().filter(column -> column.getIsKey()
////                    && primaryKey.equals(column.getName())).findFirst().orElse(null);
////            Map<String,Object> dataMap = parseColumnsToMap(columns);
////            try{
////                indexES(dataMap, database, table,eventType.toString());
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
////        });
//    }
//
//    private Map<String,Object> parseColumnsToMap(List<CanalEntry.Column> columns){
//        Map<String,Object> jsonMap = new HashMap<>();
//        columns.forEach(column -> {
//            if(column == null){
//                return;
//            }
//            jsonMap.put(column.getName(), column.getValue());
//        });
//        return jsonMap;
//    }
//
//    private void indexES(Map<String,Object> dataMap, String database, String table, CanalEntry.EventType eventType) throws IOException {
//        if(!StringUtils.equals("spark_db", database)){
//            return;
//        }
//        if(eventType.equals(CanalEntry.EventType.DELETE)){
//            log.info("DELETE：{}",dataMap.toString());
//            DeleteRequest deleteRequest = new DeleteRequest("shop");
//            deleteRequest.id(dataMap.get("id").toString());
//            deleteRequest.type("_doc");
//            restHighLevelClient.delete(deleteRequest,RequestOptions.DEFAULT);
//        }else{
//            // result 查出来的记录是全字段，不像 canal.adapter 只能查出更改的字段；
//            List<Map<String,Object>> result = new ArrayList<>();
//            if(StringUtils.equals("seller", table)) {
//                result = shopModelMapper.buildEsQuery(new Integer((String)dataMap.get("id")), null, null);
//            } else if (StringUtils.equals("category", table)){
//                result = shopModelMapper.buildEsQuery(null, new Integer((String)dataMap.get("id")), null);
//            } else if (StringUtils.equals("shop", table)){
//                result = shopModelMapper.buildEsQuery(null, null, new Integer((String)dataMap.get("id")));
//            } else {
//                return;
//            }
//            // 调用 ES API 将 MySQL 中变化的数据索引进 ElasticSearch
//            for(Map<String,Object> map : result){
//                IndexRequest indexRequest = new IndexRequest("shop");
//                indexRequest.id(String.valueOf(map.get("id")));
//                indexRequest.source(map);
//                indexRequest.type("_doc");
//                restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
//            }
//        }
//    }
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.applicationContext = applicationContext;
//    }
//
//}