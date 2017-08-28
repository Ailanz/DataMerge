package db;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TableBuilder {
    private TableBuilder() {
    }

    public enum FIELD_TYPE {
        TEXT,
        NUMERIC
    }

    private String tableName = "";

    private Set<String> primaryKey = new LinkedHashSet<>();
    private List<Pair<String, FIELD_TYPE>> columns = new LinkedList<>();

    public static void main(String args[]) {

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

    public static TableBuilder aBuilder() {
        return new TableBuilder();
    }

    public TableBuilder withTableName(String name) {
        tableName = name;
        return this;
    }

    public TableBuilder withColumn(String fieldName, FIELD_TYPE type) {
        return withColumn(fieldName, type, false);
    }

    public TableBuilder withColumn(String fieldName, FIELD_TYPE type, boolean isPrimaryKey) {
        columns.add(Pair.of(fieldName, type));
        if (isPrimaryKey) {
            primaryKey.add(fieldName);
        }
        return this;
    }

    public TableBuilder withprimaryKeys(String... keys) {
        primaryKey.addAll(Arrays.asList(keys));
        return this;
    }

    public String generateQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("CREATE TABLE '%s' (", tableName));
        columns.forEach(s -> sb.append(String.format("'%s' %s,", s.getKey(), s.getValue().name())));
        sb.append(String.format(" PRIMARY KEY(%s));", StringUtils.join(primaryKey).replace('[', ' ').replace(']', ' ')));
        return sb.toString();
    }

}
