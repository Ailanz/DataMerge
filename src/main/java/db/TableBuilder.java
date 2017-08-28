package db;


import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class TableBuilder {
    private TableBuilder(){}

    public enum FIELD_TYPE {
        TEXT,
        NUMERIC
    }

    private String tableName = "";

    private Set<String> primaryKey = new LinkedHashSet<>();
    private HashMap<String, FIELD_TYPE> columns = new HashMap<>();

    public static void main(String args[]){

        /*
         "'SYMBOL' TEXT," +
            "'HIGH' NUMERIC," +
            "'LOW' NUMERIC," +
            "'OPEN' NUMERIC," +
            "'CLOSE' NUMERIC," +
            "'VOLUME' NUMERIC," +
            "'DATE' TEXT," +
            "PRIMARY KEY(SYMBOL,DATE)" +
         */
        TableBuilder stockPriceTableBuilder = TableBuilder.aBuilder().withTableName("StockPrice")
                .withColumn("SYMBOL", TableBuilder.FIELD_TYPE.TEXT)
                .withColumn("HIGH", TableBuilder.FIELD_TYPE.NUMERIC)
                .withColumn("LOW", TableBuilder.FIELD_TYPE.NUMERIC)
                .withColumn("OPEN", TableBuilder.FIELD_TYPE.NUMERIC)
                .withColumn("CLOSE", TableBuilder.FIELD_TYPE.NUMERIC)
                .withColumn("VOLUME", TableBuilder.FIELD_TYPE.NUMERIC)
                .withColumn("DATE", TableBuilder.FIELD_TYPE.TEXT)
                .withprimaryKeys("SYMBOL", "DATE");
        System.out.println(stockPriceTableBuilder.generateQuery());
    }

    public static TableBuilder aBuilder(){
        return new TableBuilder();
    }

    public TableBuilder withTableName(String name){
        tableName = name;
        return this;
    }

    public TableBuilder withColumn(String fieldName, FIELD_TYPE type){
        return withColumn(fieldName, type, false);
    }

    public TableBuilder withColumn(String fieldName, FIELD_TYPE type, boolean isPrimaryKey){
        columns.put(fieldName, type);
        if(isPrimaryKey) {
            primaryKey.add(fieldName);
        }
        return this;
    }

    public TableBuilder withprimaryKeys(String... keys){
        primaryKey.addAll(Arrays.asList(keys));
        return this;
    }

    public String generateQuery(){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("CREATE TABLE '%s' (", tableName));
        columns.entrySet().forEach(s -> sb.append(String.format("'%s' %s,", s.getKey(), s.getValue().name())));
        sb.append(String.format(" PRIMARY KEY(%s));", StringUtils.join(primaryKey).replace('[',' ').replace(']',' ')));
        return sb.toString();
    }

}
