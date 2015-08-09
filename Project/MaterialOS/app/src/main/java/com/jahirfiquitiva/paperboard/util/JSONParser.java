package com.jahirfiquitiva.paperboard.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class JSONParser {

    public static JSONObject getJSONfromURL(String url) {
        try {
            //TODO: What is up with this deprecated code?
            HttpClient cl = new DefaultHttpClient();
            HttpResponse response = cl.execute(new HttpGet(url));
            if (response.getStatusLine().getStatusCode() == 200) {
                final String data = EntityUtils.toString(response.getEntity());
                return new JSONObject(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
