package grabber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Ailan on 9/2/2017.
 */
public class UrlHelper {
    static final ObjectMapper mapper = new ObjectMapper();

    protected static JsonNode getJsonNode(String url) {
        return getJsonNode(url, 3);
    }

    static {
        System.setProperty("sun.net.client.defaultConnectTimeout", "30000");
        System.setProperty("sun.net.client.defaultReadTimeout", "30000");
    }

    protected static JsonNode getJsonNode(String url, int retry) {
        URL jsonUrl = null;
        try {
            jsonUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        for (int i = 0; i < retry; i++) {  //retry = 3
            try {
                JsonNode node = AlphaVantageBuilder.mapper.readValue(jsonUrl, JsonNode.class);
                if (node.size() != 0) {
                    return node;
                }
            } catch (IOException e) {
                System.err.println("Retrying... " + jsonUrl.toString());
            } catch (RuntimeException r) {
            }
        }

        System.err.println("Giving Up... " + jsonUrl.toString());

        return null;
    }
}
