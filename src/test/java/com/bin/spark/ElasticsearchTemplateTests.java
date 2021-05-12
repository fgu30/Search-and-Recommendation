package com.bin.spark;


import com.bin.spark.pojo.FoodieItems;
import com.bin.spark.pojo.Stu;
import com.bin.spark.service.HighlightResultMapper;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 斌~
 * 2021/4/6 11:08
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SparkApplication.class)
public class ElasticsearchTemplateTests {

//    @Autowired
//    private EsStuRepository esStuRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

//    @Test
//    public void testEs(){
//        Iterable<Stu> all = esStuRepository.findAll();
//        all.forEach(e->{
//            System.out.println(e.getName());
//        });
//    }

    /**
     * 创建索引
     */
    @Test
    public void createIndex() {
        elasticsearchTemplate.createIndex(Stu.class);
    }

    /**
     * 删除索引
     */
    @Test
    public void delIndex() {
        elasticsearchTemplate.deleteIndex(Stu.class);
    }


    /**
     * 新增文档
     */
    @Test
    public void addStuDoc() {
        Stu stu = new Stu();
        stu.setStuId(1002L);
        stu.setName("llb2");
        stu.setAge(29);
        stu.setStuMoney(1821.0);
        stu.setSign("so cool");
        stu.setDesc("I'm a student too");
        IndexQuery query = new IndexQueryBuilder().withObject(stu).build();
        elasticsearchTemplate.index(query);
    }

    /**
     * 更新文档
     */
    @Test
    public void updStuDoc() {
        Map<String, Object> mapSource = new HashMap<>();
        mapSource.put("age", 30);

        IndexRequest indexRequest = new IndexRequest();
        indexRequest.source(mapSource);

        UpdateQuery updateQuery = new UpdateQueryBuilder()
                .withClass(Stu.class)
                .withId("1001")
                .withIndexRequest(indexRequest)
                .build();
        elasticsearchTemplate.update(updateQuery);
    }

    /**
     * 查询文档
     */
    @Test
    public void getStuDoc() {
        GetQuery query = new GetQuery();
        query.setId("1001");
        Stu stu = elasticsearchTemplate.queryForObject(query, Stu.class);
        System.out.println(stu.toString());
    }

    /**
     * 删除文档
     */
    @Test
    public void delStuDoc() {
        elasticsearchTemplate.delete(Stu.class, "1002");
    }

//    -------------------------------搜索-分割线----------------------------------------

    @Test
    public void searchStuDoc() {
        //分页查询
        Pageable pageable = PageRequest.of(0, 10);

        SearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("name", "llb"))
                .withPageable(pageable)
                .build();
        AggregatedPage<Stu> stuPage = elasticsearchTemplate.queryForPage(query, Stu.class);
        System.out.println("总分页数" + stuPage.getTotalPages());
        List<Stu> stuList = stuPage.getContent();
        stuList.forEach(e -> System.out.println(e.getName()));
    }

    @Test
    public void highlightStuDoc() {
        //高亮
        String preTag = "<font color='red' >";
        String postTag = "</font>";

        //分页查询
        Pageable pageable = PageRequest.of(0, 10);

        SearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("desc", "student"))
                .withHighlightFields(new HighlightBuilder.Field("desc").preTags(preTag).postTags(postTag))
                .withPageable(pageable)
                .withSort(new FieldSortBuilder("age").order(SortOrder.ASC))
                .build();
        AggregatedPage<Stu> stuPage = elasticsearchTemplate.queryForPage(query, Stu.class,
                new HighlightResultMapper());
        System.out.println("总分页数" + stuPage.getTotalPages());
        List<Stu> stuList = stuPage.getContent();
        stuList.forEach(e -> System.out.println(e.toString()));
    }


    @Test
    public void highlightFoodieDoc() {
        //高亮
        String preTag = "<font color='red'>";
        String postTag = "</font>";
        //分页
        Pageable pageable = PageRequest.of(0, 10);
        //排序
        FieldSortBuilder price = new FieldSortBuilder("price").order(SortOrder.ASC);
        //查询构造
        String itemNameFiled = "itemName";
        SearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery(itemNameFiled, "美国"))
                .withHighlightFields(new HighlightBuilder.Field(itemNameFiled)
                        .preTags(preTag)
                        .postTags(postTag)
                )
                .withSort(price)
                .withPageable(pageable)
                .build();
        System.out.println(query.getQuery().toString());
        AggregatedPage<FoodieItems> foodiePage = elasticsearchTemplate.queryForPage(query, FoodieItems.class,
                new HighlightResultMapper());
        System.out.println("总分页数" + foodiePage.getTotalPages());
        List<FoodieItems> foodieList = foodiePage.getContent();
        foodieList.forEach(e -> System.out.println(e.toString()));
    }
}