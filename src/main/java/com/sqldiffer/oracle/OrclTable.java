package com.sqldiffer.oracle;

import java.sql.ResultSet;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sqldiffer.common.Column;
import com.sqldiffer.common.Table;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
public class OrclTable extends Table {
    private static final long serialVersionUID = 1L;
    
    public String generateNew(Object context) {
        StringBuilder b = new StringBuilder();
        if(!isTemp()) {
            b.append("\nCREATE TABLE \"");
        } else {
            b.append("\nCREATE GLOBAL TEMPORARY TABLE \"");
        }
        b.append(getName());
        b.append("\" (");
        //String pkeycols = "";
        for (Column c : getColumns()) {
            b.append("\n\t");
            /*if(c.getName().matches("^[a-zA-Z_][a-zA-Z0-9_]*")) {
                b.append(c.getName());
            } else {*/
                b.append("\"");
                b.append(c.getName());
                b.append("\"");
            //}
            b.append(" ");
            b.append(c.getType());
            b.append(" ");
            if(c.isNotnull()) { 
                //b.append("NOT NULL ");
            }
            if(c.getDefVal()!=null && !c.getDefVal().trim().isEmpty()) { 
                b.append("DEFAULT ");
                b.append(c.getDefVal());
            }
            b.append(",");
        }
        if(b.charAt(b.length()-1)==',') {
            b.deleteCharAt(b.length()-1);
        }
        if(!isTemp()) {
            b.append("\n);\n/");
        } else {
            b.append("\n) ON COMMIT DELETE ROWS;\n/"); 
        }
        return b.toString();
    }

    public String generateUpd(Object context) {
        return null;
    }

    public String generateDel(Object context) {
        StringBuilder b = new StringBuilder();
        b.append("\nDROP TABLE \"");
        b.append(getName());
        b.append("\";\n/");
        return b.toString();
    }

    public String query(Object context) {
        String spl = "SELECT table_name,temporary from user_tables";
        return spl;
    }

    public Object fromResult(ResultSet rs, Object context) throws Exception {
        OrclTable sp = new OrclTable();
        if(rs!=null) {
            sp.setName(rs.getString(1));
            sp.setTemp(rs.getString(2).equals("Y"));
        }
        return sp;
    }
}
