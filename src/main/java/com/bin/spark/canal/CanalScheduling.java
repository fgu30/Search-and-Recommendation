package com.bin.spark.canal;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.bin.spark.mapper.ShopModelMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by 斌~
 * 2020/9/21 11:13
 */
@Component
public class CanalScheduling implements Runnable,ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Resource
    private CanalConnector canalConnector;

    @Autowired
    private ShopModelMapper shopModelMapper;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    @Scheduled(fixedDelay = 100)
    public void run() {
        long batchId = -1;
        try{
            //一次拉取多少消息
            int batchSize = 1000;
            Message message = canalConnector.getWithoutAck(batchSize);
             batchId = message.getId();
            List<CanalEntry.Entry> entries = message.getEntries();
            if(batchId!=-1 && entries.size()>0){
                entries.forEach(entry -> {
                    if(entry.getEntryType()== CanalEntry.EntryType.ROWDATA){
                        //解析处理
                        publishCanalEvent(entry);
                    }
                });
            }
            canalConnector.ack(batchId);
         }catch (Exception e){
            e.printStackTrace();
            canalConnector.rollback(batchId);
        }
    }

    private void publishCanalEvent(CanalEntry.Entry entry){
        CanalEntry.EventType entryType = entry.getHeader().getEventType();
        String dataBase  = entry.getHeader().getSchemaName();
        String table  = entry.getHeader().getTableName();
        CanalEntry.RowChange change = null;
        try {
            change = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return;
        }
        change.getRowDatasList().forEach(rowData -> {
            List <CanalEntry.Column> columns = rowData.getAfterColumnsList();
            String primaryKey ="id";
            CanalEntry.Column idColumn = columns.stream().filter(e-> e.getIsKey() && primaryKey.equals(e.getName()))
                    .findFirst().orElse(null);
            Map<String, Object> dataMap = parseColumnsToMap(columns);
            try {
                indexES(dataMap,dataBase,table);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    private Map<String, Object> parseColumnsToMap(List<CanalEntry.Column> columns){
        Map<String, Object> jsonMap = new HashMap<>();
        columns.forEach(column->{
            if(column == null){
                return;
            }
            jsonMap.put(column.getName(),column.getValue());
        });
        return jsonMap;
    }

    private void indexES (Map<String, Object> dataMap,String dataBase,String table) throws IOException {
        if(!StringUtils.equals("spark_db",dataBase)){
            return;
        }
        List<Map<String,Object>> result = new ArrayList<>();
        if(StringUtils.equals("seller",table)){
            result = shopModelMapper.buildEsQuery(Integer.parseInt(dataMap.get("id").toString()),null,null);
        }else if(StringUtils.equals("category",table)){
            result = shopModelMapper.buildEsQuery(null,Integer.parseInt(dataMap.get("id").toString()),null);
        }else if(StringUtils.equals("shop",table)){
            result = shopModelMapper.buildEsQuery(null,null,Integer.parseInt(dataMap.get("id").toString()));
        }else{
            return;
        }
        for(Map<String,Object> map:result){
            IndexRequest indexRequest = new IndexRequest("shop");
            indexRequest.id(map.get("id").toString());
            indexRequest.source(map);
            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        }
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }
}
