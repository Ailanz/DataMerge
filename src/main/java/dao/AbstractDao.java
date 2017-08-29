package dao;
import db.TableBuilder;

import java.util.Map;

/**
 * Created by Ailan on 8/28/2017.
 */
public interface AbstractDao {
    Map<String, String> getParams();
}
