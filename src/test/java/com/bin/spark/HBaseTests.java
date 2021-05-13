package com.bin.spark;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 斌~
 * 2021/4/6 11:08
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SparkApplication.class)
public class HBaseTests {

    private Connection connection = null;
    private Table table = null;
    private Admin admin = null;
    private String tableName = "pk_hbase_java_api";


    @Before
    public void setUp() {
        Configuration configuration = new Configuration();
            configuration.set("hbase.rootdir", "hdfs://hadoop000:8020/hbase");
        configuration.set("hbase.zookeeper.quorum", "hadoop000:2181");
        try {
            connection = ConnectionFactory.createConnection(configuration);
            admin = connection.getAdmin();
            Assert.assertNotNull(connection);
            Assert.assertNotNull(admin);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getConnection() {

    }

    /**
     * 创建表
     */
    @Test
    public void createTable() throws Exception {
        TableName table = TableName.valueOf(tableName);
        if (admin.tableExists(table)) {
            System.out.println(tableName + " 已经存在...");
        } else {
            HTableDescriptor descriptor = new HTableDescriptor(table);
            descriptor.addFamily(new HColumnDescriptor("info"));
            descriptor.addFamily(new HColumnDescriptor("address"));
            admin.createTable(descriptor);
            System.out.println(tableName + " 创建成功...");
        }
    }

    /**
     * 查询表
     */
    @Test
    public void queryTableInfos() throws Exception {
        HTableDescriptor[] tables = admin.listTables();
        if (tables.length > 0) {
            for (HTableDescriptor table : tables) {
                System.out.println(table.getNameAsString());

                HColumnDescriptor[] columnDescriptors = table.getColumnFamilies();
                for (HColumnDescriptor hColumnDescriptor : columnDescriptors) {
                    System.out.println("\t" + hColumnDescriptor.getNameAsString());
                }
            }
        }
    }


    @Test
    public void testPut() throws Exception {
        table = connection.getTable(TableName.valueOf(tableName));

//        Put put = new Put(Bytes.toBytes("pk"));
//
//        // 通过PUT设置要添加数据的cf、qualifier、value
//        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("age"), Bytes.toBytes("28"));
//        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("birthday"), Bytes.toBytes("xxxx"));
//        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("company"), Bytes.toBytes("imooc"));
//
//        put.addColumn(Bytes.toBytes("address"), Bytes.toBytes("country"), Bytes.toBytes("CN"));
//        put.addColumn(Bytes.toBytes("address"), Bytes.toBytes("province"), Bytes.toBytes("GUANGDONG"));
//        put.addColumn(Bytes.toBytes("address"), Bytes.toBytes("city"), Bytes.toBytes("shenzhen"));
//
//
//        // 将数据put到HBase中去
//        table.put(put);


        List<Put> puts = new ArrayList<>();

        Put put1 = new Put(Bytes.toBytes("jepson"));
        put1.addColumn(Bytes.toBytes("info"), Bytes.toBytes("age"), Bytes.toBytes("18"));
        put1.addColumn(Bytes.toBytes("info"), Bytes.toBytes("birthday"), Bytes.toBytes("xxxx"));
        put1.addColumn(Bytes.toBytes("info"), Bytes.toBytes("company"), Bytes.toBytes("apple"));
        put1.addColumn(Bytes.toBytes("address"), Bytes.toBytes("country"), Bytes.toBytes("CN"));
        put1.addColumn(Bytes.toBytes("address"), Bytes.toBytes("province"), Bytes.toBytes("SHANGHAI"));
        put1.addColumn(Bytes.toBytes("address"), Bytes.toBytes("city"), Bytes.toBytes("SHANGHAI"));


        Put put2 = new Put(Bytes.toBytes("xingxing"));
        put2.addColumn(Bytes.toBytes("info"), Bytes.toBytes("age"), Bytes.toBytes("19"));
        put2.addColumn(Bytes.toBytes("info"), Bytes.toBytes("birthday"), Bytes.toBytes("xxxx"));
        put2.addColumn(Bytes.toBytes("info"), Bytes.toBytes("company"), Bytes.toBytes("PDD"));
        put2.addColumn(Bytes.toBytes("address"), Bytes.toBytes("country"), Bytes.toBytes("CN"));
        put2.addColumn(Bytes.toBytes("address"), Bytes.toBytes("province"), Bytes.toBytes("SHANGHAI"));
        put2.addColumn(Bytes.toBytes("address"), Bytes.toBytes("city"), Bytes.toBytes("SHANGHAI"));


        puts.add(put1);
        puts.add(put2);

        table.put(puts);
    }


    @Test
    public void testUpdate() throws Exception {
        table = connection.getTable(TableName.valueOf(tableName));

        Put put = new Put(Bytes.toBytes("xingxing"));
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("age"), Bytes.toBytes("20"));


        table.put(put);

    }


    @Test
    public void testGet01() throws Exception {
//        table = connection.getTable(TableName.valueOf(tableName));
        table = connection.getTable(TableName.valueOf("access_20190130"));

        Get get = new Get("20190130_1433814004".getBytes());
        //get.addColumn(Bytes.toBytes("info"), Bytes.toBytes("age"));

        Result result = table.get(get);
        printResult(result);
    }


    @Test
    public void testScan01() throws Exception {
        table = connection.getTable(TableName.valueOf(tableName));

        Scan scan = new Scan();
//        scan.addFamily(Bytes.toBytes("info"));
        scan.addColumn(Bytes.toBytes("info"), Bytes.toBytes("company"));

        //Scan scan = new Scan(Bytes.toBytes("jepson")); // >=
        //Scan scan = new Scan(new Get(Bytes.toBytes("jepson")));
        //Scan scan = new Scan(Bytes.toBytes("jepson"),Bytes.toBytes("xingxing")); // [)
        ResultScanner rs = table.getScanner(scan);

//        ResultScanner rs = table.getScanner(Bytes.toBytes("info"), Bytes.toBytes("company"));
        for(Result result : rs){
            printResult(result);
            System.out.println("~~~~~~~~~~~~~");
        }

    }

    @Test
    public void testFilter() throws Exception {
        table = connection.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan();
//        String reg = "^*ing";
//        Filter filter = new RowFilter(CompareFilter.CompareOp.EQUAL, new RegexStringComparator(reg));
//        scan.setFilter(filter);


//        Filter filter = new PrefixFilter(Bytes.toBytes("p"));
//        scan.setFilter(filter);

        FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ONE);
        Filter filter1 = new PrefixFilter("p".getBytes());
        Filter filter2 = new PrefixFilter("j".getBytes());

        filters.addFilter(filter1);
        filters.addFilter(filter2);
        scan.setFilter(filters);

        ResultScanner rs = table.getScanner(scan);
        for(Result result : rs){
            printResult(result);
            System.out.println("~~~~~~~~~~~~~");
        }
    }

    private void printResult(Result result) {
        for (Cell cell : result.rawCells()) {
            System.out.println(Bytes.toString(result.getRow()) + "\t "
                    + Bytes.toString(CellUtil.cloneFamily(cell)) + "\t"
                    + Bytes.toString(CellUtil.cloneQualifier(cell)) + "\t"
                    + Bytes.toString(CellUtil.cloneValue(cell)) + "\t"
                    + cell.getTimestamp());
        }
    }


    @After
    public void tearDown() {
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}