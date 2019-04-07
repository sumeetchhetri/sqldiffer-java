package com.sqldiffer.common;

public interface Generator {
    String generateNew(Object context);
    String generateUpd(Object context);
    String generateDel(Object context);
}
