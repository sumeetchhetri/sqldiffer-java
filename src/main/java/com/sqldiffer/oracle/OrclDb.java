package com.sqldiffer.oracle;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sqldiffer.common.Db;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
public class OrclDb extends Db {
    private static final long serialVersionUID = 1L;
    public String generateUrl(String driver, String host, String dbName, String schema) {
        return "jdbc:oracle:thin:@//"+host+"/"+dbName;
    }

    public String generateNew(Object context) {
        return null;
    }

    public String generateUpd(Object context) {
        return null;
    }

    public String generateDel(Object context) {
        return null;
    }

    @Override
    public String preface() {
        StringBuilder b = new StringBuilder();
        return b.toString();
    }

    @Override
    public String create() {
        StringBuilder b = new StringBuilder();
        b.append("\nCREATE DATABASE ");
        b.append(getName());
        b.append(";\n/");
        return b.toString();
    }

    @Override
    public String connect() {
        StringBuilder b = new StringBuilder();
        return b.toString();
    }

    @Override
    public String createSchema() {
        StringBuilder b = new StringBuilder();
        return b.toString();
    }
}
