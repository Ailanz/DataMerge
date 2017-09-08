package grabber;

import com.fasterxml.jackson.databind.JsonNode;
import org.joda.time.DateTime;

import java.util.HashMap;

public class ResultData {
    private DateTime date;
    private HashMap<String, JsonNode> data;

    public ResultData(DateTime date, HashMap<String, JsonNode> data) {
        this.date = date;
        this.data = data;
    }

    public DateTime getDate() {
        return date;
    }

    public HashMap<String, JsonNode> getData() {
        return data;
    }
}
