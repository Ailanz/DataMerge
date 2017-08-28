package db;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class InsertionBuilder {

    private TableBuilder tableBuilder = null;
    private List<String> columnNames = null;
    private List<List<Pair<String, String>>> params = new LinkedList<>();

    private InsertionBuilder() {
    }

    public static InsertionBuilder aBuilder() {
        return new InsertionBuilder();
    }

    public InsertionBuilder withTableBuilder(TableBuilder tableBuilder) {
        this.tableBuilder = tableBuilder;
        this.tableBuilder.getColumns().forEach(s -> columnNames.add(s.getKey()));
        return this;
    }

    //Each Param represents a single row
    public InsertionBuilder withParams(List<Pair<String, String>> param) {
        params.add(param);
        return this;
    }

    public String execute(){
        StringBuilder sb = new StringBuilder();
        sb.append("insert into " + tableBuilder.getTableName() + "  (");
        sb.append(StringUtils.join(columnNames).replace('[', ' ').replace(']', ' ') + ") values " );
        //refer to sqlite
        return null;
    }

    private boolean validate(List<Pair<String, String>> param) {
        return true;
    }
}