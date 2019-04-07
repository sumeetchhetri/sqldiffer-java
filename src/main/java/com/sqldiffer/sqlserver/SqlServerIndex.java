package com.sqldiffer.sqlserver;

import java.sql.ResultSet;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sqldiffer.common.Index;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
public class SqlServerIndex extends Index {
    private static final long serialVersionUID = 1L;

    public String generateNew(Object context) {
        StringBuilder b = new StringBuilder();
        b.append("\nCREATE INDEX ");
        b.append(getName());
        b.append(" ON ");
        b.append(getTable());
        b.append(" USING ");
        b.append(getProps().get("am"));
        b.append(" ");
        b.append(getProps().get("key_names").replaceAll("\\{\"", "(").replaceAll("\"\\}", " COLLATE pg_catalog.\"default\");\n"));
        return b.toString();
    }

    public String generateUpd(Object context) {
        StringBuilder b = new StringBuilder();
        b.append(generateDel(context));
        b.append(generateNew(context));
        return b.toString();
    }

    public String generateDel(Object context) {
        StringBuilder b = new StringBuilder();
        b.append("\nDROP INDEX ");
        b.append(getName());
        b.append(";\n");
        return b.toString();
    }

    public String query(Object context) {
        String spl = "SELECT t.name, ind.name FROM sys.indexes ind INNER JOIN sys.index_columns ic ON  ind.object_id = ic.object_id and ind.index_id = ic.index_id "
                + "INNER JOIN sys.columns col ON ic.object_id = col.object_id and ic.column_id = col.column_id INNER JOIN sys.tables t ON ind.object_id = t.object_id "
                + "WHERE ind.is_primary_key = 0 AND ind.is_unique = 0 AND ind.is_unique_constraint = 0 AND t.is_ms_shipped = 0 ORDER BY t.name, ind.name, ind.index_id, ic.index_column_id";
        return spl;
    }

    public Object fromResult(ResultSet rs, Object context) throws Exception {
        SqlServerIndex sp = new SqlServerIndex();
        if(rs!=null) {
            sp.setName(rs.getString(1));
            sp.setTable(rs.getString(3));
            sp.getProps().put("am", rs.getString(4));
            sp.getProps().put("key_names", rs.getString(6));
        }
        return sp;
    }
}
