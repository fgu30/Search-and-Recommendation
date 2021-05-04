package com.bin.spark.service;

/**
 * @author 斌~
 * @version 1.0
 * @date 2021/5/3 下午4:46
 */

import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *@author mac
 * @业务描述： 用于对es搜索结果再加工
 */
public class HighlightResultMapper implements SearchResultMapper {
    @Override
    public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> clazz, Pageable pageable) {
        long totalHits = searchResponse.getHits().getTotalHits();
        List<T> list = new ArrayList<>();
        SearchHits hits = searchResponse.getHits();
        if (hits.getHits().length > 0) {
            for (SearchHit searchHit : hits) {
                Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                T item = JSON.parseObject(searchHit.getSourceAsString(), clazz);
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (highlightFields.containsKey(field.getName())) {
                        try {
                            field.set(item, highlightFields.get(field.getName()).fragments()[0].toString());
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
                list.add(item);
            }
        }
        return new AggregatedPageImpl<>(list, pageable, totalHits);
    }

    @Override
    public <T> T mapSearchHit(SearchHit searchHit, Class<T> aClass) {
        System.out.println("mapSearchHit here");
        return null;
    }
}
