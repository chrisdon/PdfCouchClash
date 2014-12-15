package com.fail.pdfcouchclash;

import android.content.Context;

import com.couchbase.lite.Database;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.View;

import java.util.Map;

/**
 * Created by cdonnelly on 15/12/2014.
 */
public class DBUtils {

    /**
     * Create a html view and register its map function:
     *
     * @param database
     */
    public static void createHtmlView(Database database, final Context context) {
        View htmls = database.getView(context.getString(R.string.view_name_html));
        htmls.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                String type = (String) document.get(context.getString(R.string.doc_type));
                String resource_type = (String) document.get(context.getString(R.string.doc_resource_type));
                if (type.equals(context.getString(R.string.doc_type_resource)) && resource_type.equals(context.getString(R.string.doc_resource_type_html))) {
                    String _id = (String) document.get(context.getString(R.string.doc_id));
                    Map<String, Object> metadata = (Map<String, Object>) document.get(context.getString(R.string.doc_metadata));
                    Map<String, Object> erp = (Map<String, Object>) metadata.get(context.getString(R.string.doc_metadata_erp));
                    Object ID = erp.get(context.getString(R.string.doc_metadata_erp_ID));
                    emitter.emit(ID.toString(), _id);
                }
            }
        }, "1.0");
    }
}
