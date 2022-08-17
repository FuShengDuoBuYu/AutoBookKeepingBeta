package Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

}
