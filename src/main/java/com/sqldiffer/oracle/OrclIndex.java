package com.sqldiffer.oracle;

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
public class OrclIndex extends Index {
    private static final long serialVersionUID = 1L;

    public String generateNew(Object context) {
        return getDefinition() + ";\n/";
    }

    public String generateUpd(Object context) {
        StringBuilder b = new StringBuilder();
        b.append(generateDel(context));
        b.append(generateNew(context));
        return b.toString();
    }

    public String generateDel(Object context) {
        StringBuilder b = new StringBuilder();
        b.append("\nDROP INDEX \"");
        b.append(getName());
        b.append("\";\n/");
        return b.toString();
    }

    public String query(Object context) {
        String spl = "SELECT index_name, table_name, dbms_metadata.get_ddl('INDEX', index_name) from user_indexes";
        return spl;
    }

    public Object fromResult(ResultSet rs, Object context) throws Exception {
        OrclIndex sp = new OrclIndex();
        if(rs!=null) {
            sp.setName(rs.getString(1));
            sp.setTable(rs.getString(2));
            sp.setDefinition(rs.getString(3));
        }
        return sp;
    }
}
