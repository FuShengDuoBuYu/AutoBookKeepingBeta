package Util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StringUtil {

    public static String list2String(List<String> stringList){
        return String.join(",", stringList);
    }


    public static List<String> string2List(String str){
        if(str == null || str .equals("")){
            return new ArrayList<>();
        }
        return new ArrayList<String>(Arrays.asList(str.split(",")));
    }

    public static Map<String,Object> Json2Map(String content){
        content = content.trim();
        Map<String, Object> result = new HashMap<>();
        try {

            if (content.charAt(0) == '[') {

                JSONArray jsonArray = new JSONArray(content);
                for (int i = 0; i < jsonArray.length(); i++) {

                    Object value = jsonArray.get(i);
                    if (value instanceof JSONArray || value instanceof JSONObject) {

                        result.put(i + "", Json2Map(value.toString().trim()));
                    } else {

                        result.put(i + "", jsonArray.getString(i));
                    }
                }
            } else if (content.charAt(0) == '{'){

                JSONObject jsonObject = new JSONObject(content);
                Iterator<String> iterator = jsonObject.keys();
                while (iterator.hasNext()) {

                    String key = iterator.next();
                    Object value = jsonObject.get(key);
                    if (value instanceof JSONArray || value instanceof JSONObject) {
                        Log.d("json", "Json2Map: " + value.toString());
                        result.put(key, value.toString().trim());
                    } else {
                        result.put(key, value.toString().trim());
                    }
                }
            }else {

                Log.e("异常", "json2Map: 字符串格式错误");
            }
        } catch (JSONException e) {

            Log.e("异常", "json2Map: ", e);
            result = null;
        }
        return result;
    }
}
