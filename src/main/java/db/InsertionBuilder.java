package db;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class InsertionBuilder {

    private TableBuilder tableBuilder = null;
    private List<Pair<String, TableBuilder.FIELD_TYPE>> columnNames = new LinkedList<>();
    private List<Map<String, String>> params = new LinkedList<>();

    private InsertionBuilder() {
    }

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
        HashMap<String, String> map = new HashMap<>();
        map.put("SYMBOL", "ABC");
        map.put("HIGH", "32");
        map.put("LOW", "12");
        map.put("OPEN", "32");
        map.put("CLOSE", "12");
        map.put("VOLUME", "1000");
        map.put("DATE", LocalDate.now().toString());
        InsertionBuilder insert = aBuilder()
                .withTableBuilder(stockPriceTableBuilder)
                .withParams(map);
        System.out.println(insert.execute());
    }

    public static InsertionBuilder aBuilder() {
        return new InsertionBuilder();
    }

    public InsertionBuilder withTableBuilder(TableBuilder tableBuilder) {
        this.tableBuilder = tableBuilder;
        this.tableBuilder.getColumns().forEach(s -> columnNames.add(Pair.of(s.getKey(), s.getValue())));
        return this;
    }

    //Each Param represents a single row
    public InsertionBuilder withParams(Map<String, String> param) {
        params.add(param);
        return this;
    }

    public String execute() {
        StringBuilder sb = new StringBuilder();
        sb.append("insert or replace into " + tableBuilder.getTableName() + " (");
        sb.append(StringUtils.join(columnNames.stream().map(s -> s.getKey()).toArray(), ',').replace('[', ' ').replace(']', ' ') + ") values ");
        params.forEach(p -> {
            sb.append('(');
            columnNames.forEach(s -> {
                if (s.getRight() == TableBuilder.FIELD_TYPE.NUMERIC) {
                    sb.append(p.get(s.getKey()));
                } else if (s.getRight() == TableBuilder.FIELD_TYPE.TEXT) {
                    sb.append("'" + p.get(s.getKey()) + "'");
                }
                sb.append(',');
            });
            sb.deleteCharAt(sb.length() - 1);
            sb.append(')').append(',');
        });
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private boolean validate(List<Pair<String, String>> param) {
        return true;
    }
}