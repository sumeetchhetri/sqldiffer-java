package com.sqldiffer.postgres;

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
public class PgTable extends Table {
    private static final long serialVersionUID = 1L;
    
    public String generateNew(Object context) {
        StringBuilder b = new StringBuilder();
        b.append("\nCREATE TABLE ");
        b.append(getName());
        b.append("(");
        //String pkeycols = "";
        for (Column c : getColumns()) {
            b.append("\n\t");
            if(c.getName().matches("^[a-zA-Z_][a-zA-Z0-9_]*")) {
                b.append(c.getName());
            } else {
                b.append("\"");
                b.append(c.getName());
                b.append("\"");
            }
            b.append(" ");
            b.append(c.getType());
            b.append(" ");
            if(c.isNotnull()) { 
                b.append("NOT NULL ");
            }
            if(c.getDefVal()!=null && !c.getDefVal().trim().isEmpty()) { 
                b.append("DEFAULT ");
                b.append(c.getDefVal());
            }
            b.append(",");
            //if(c.isPkey()) {
            //    pkeycols += c.getName() + ",";
            //}
        }
        /*if(pkeycols!=null && !pkeycols.isEmpty()) {
            if(pkeycols.charAt(pkeycols.length()-1)==',') {
                pkeycols = pkeycols.substring(0, pkeycols.length()-1);
            }
            b.append("\n\tCONSTRAINT pk_");
            b.append(getName());
            b.append(" PRIMARY KEY (");
            b.append(pkeycols);
            b.append("),");
        }
        for (Column c : getColumns()) {
            if(c.isUniq()) {
                b.append("\n\tCONSTRAINT ");
                b.append(getName());
                b.append("_");
                b.append(c.getName());
                b.append(" UNIQUE (");
                b.append(c.getName());
                b.append("),");
            }
        }*/
        if(b.charAt(b.length()-1)==',') {
            b.deleteCharAt(b.length()-1);
        }
        b.append("\n) WITH (OIDS=FALSE);\n");
        return b.toString();
    }

    public String generateUpd(Object context) {
        return null;
    }

    public String generateDel(Object context) {
        StringBuilder b = new StringBuilder();
        b.append("\nDROP TABLE ");
        b.append(getName());
        b.append(";\n");
        return b.toString();
    }

    public String query(Object context) {
        String spl = "SELECT table_name FROM information_schema.tables WHERE table_schema = ANY (current_schemas(false)) AND table_type='BASE TABLE'";
        return spl;
    }

    public Object fromResult(ResultSet rs, Object context) throws Exception {
        PgTable sp = new PgTable();
        if(rs!=null) {
            sp.setName(rs.getString(1));
        }
        return sp;
    }
}
