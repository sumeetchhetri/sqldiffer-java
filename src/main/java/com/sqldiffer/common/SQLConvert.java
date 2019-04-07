package com.sqldiffer.common;

import java.sql.ResultSet;

public interface SQLConvert {
    
    String query(Object context);
    
    Object fromResult(ResultSet rs, Object context) throws Exception;
    
    public static boolean equalsIgnoreNewLines(String a, String b) {
        if(a==null && b==null)return true;
        if (a == null) {
            if (b != null)
                return false;
        }
        if (b == null) {
            if (a != null)
                return false;
        }
        a = a.replace("\n", "").replace("\r", "").replaceAll("[\t ]+", "").trim();
        b = b.replace("\n", "").replace("\r", "").replaceAll("[\t ]+", "").trim();
        return a.equals(b);
    }
}
