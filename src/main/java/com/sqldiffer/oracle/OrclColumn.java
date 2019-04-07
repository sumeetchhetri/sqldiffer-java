package com.sqldiffer.oracle;

import java.sql.ResultSet;
import java.util.Map;

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
public class OrclColumn extends Column {
    private static final long serialVersionUID = 1L;
    
    static final String RESERVED_KEYWORDS = "date|time|level|mode|comment|number";
    
    public String generateNew(Object context) {
        StringBuilder b = new StringBuilder();
        b.append("\nALTER TABLE \"");
        b.append(getTable().getName());
        b.append("\" ADD ");
        b.append("\"");
        b.append(getName());
        b.append("\"");
        b.append(" ");
        b.append(getType());
        if(isNotnull()) {
            //b.append(" NOT NULL ");
        }
        if(getDefVal()!=null) {
            b.append(" DEFAULT ");
            b.append(getDefVal());
        }
        b.append(";\n");
        return b.toString();
    }
    
    public String generateUpd(Object context) {
        StringBuilder b = new StringBuilder();
        Column dstcol = (Column)context;
        if(dstcol!=null && !dstcol.getType().equals(getType())) {
            b.append("\nALTER TABLE \"");
            b.append(getTable().getName());
            b.append("\" MODIFY ");
        	b.append("\"");
            b.append(getName());
            b.append("\"");
            b.append(" ");
            b.append(getType());
            b.append(";\n/");
        } else if(dstcol!=null && getDefVal()!=null && !getDefVal().equals(dstcol.getDefVal())) {
            b.append("\nALTER TABLE \"");
            b.append(getTable().getName());
            b.append("\" MODIFY ");
            b.append("\"");
            b.append(getName());
            b.append("\"");
            b.append(" DEFAULT ");
            b.append(getDefVal());
            b.append(";\n/");
        } else if(dstcol!=null && dstcol.getDefVal()!=null && !dstcol.getDefVal().equals(getDefVal())) {
            b.append("\nALTER TABLE \"");
            b.append(getTable().getName());
            b.append("\" MODIFY ");
            b.append("\"");
            b.append(getName());
            b.append("\"");
            if("null".equalsIgnoreCase(getDefVal())) {
                b.append(" NOT NULL ENABLE ");
            } else {
                b.append(" DROP DEFAULT ");
                b.append(getDefVal()); 
            }
            b.append(";\n/");
        }
        return b.toString();
    }
    public String generateDel(Object context) {
        StringBuilder b = new StringBuilder();
        b.append("\nALTER TABLE \"");
        b.append(getTable().getName());
        b.append("\" DROP COLUMN ");
        b.append("\"");
        b.append(getName());
        b.append("\"");
        b.append(";\n/");
        return b.toString();
    }
    public String query(Object context) {
        Object[] args = (Object[])context;
        String spl = "SELECT outer.* FROM (SELECT column_id,column_name,nullable,data_type,data_length,data_precision,data_scale,data_default,table_name,rownum rn "
                + "from user_tab_columns) outer where outer.rn >= " + args[0] + " and outer.rn < " + args[1];
        return spl;
    }
    @SuppressWarnings("unchecked")
    public Object fromResult(ResultSet rs, Object context) throws Exception {
        OrclColumn col = new OrclColumn();
        String defval = rs.getString(8);//for oracle https://blog.jooq.org/2015/12/30/oracle-long-and-long-raw-causing-stream-has-already-been-closed-exception/
        String tblname = rs.getString(9);
        Object[] arr = (Object[])context;
        Map<String, Table> tbls = (Map<String, Table>)arr[0];
        Table tsp = tbls.get(tblname);
        if(tsp==null)return null;
        boolean exists = false;
        for (Column pcol :tsp.getColumns()) {
            OrclColumn ecol = (OrclColumn)pcol;
            if(ecol.getName().equals(rs.getString(2))) {
                col = ecol;
                exists = true;
                break;
            }
        }
        col.setPos(rs.getInt(1));
        col.setName(rs.getString(2));
        col.setNotnull(!rs.getString(3).equals("Y"));
        col.setType(rs.getString(4));
        if(rs.getBigDecimal(6)!=null && col.getType().indexOf("(")==-1) {
            col.setType(col.getType()+"("+rs.getBigDecimal(6).longValue());
            if(rs.getBigDecimal(7)!=null) {
                col.setType(col.getType()+","+rs.getBigDecimal(7).longValue());
            }
            col.setType(col.getType()+")");
        } else if(rs.getBigDecimal(5)!=null && col.getType().indexOf("(")==-1 && col.getType().toLowerCase().contains("char")) {
            col.setType(col.getType()+"("+rs.getBigDecimal(5).longValue());
            col.setType(col.getType()+")");
        }
        if(defval!=null) {
            defval = defval.trim();
            if(defval.indexOf(".\"NEXTVAL\"")!=-1 || defval.indexOf(".\"nextval\"")!=-1) {
                defval = defval.replace("\"", "");
            }
        }
        col.setDefVal(defval);
        col.setTable(tsp);
        if(!exists)tsp.getColumns().add(col);
        return tsp;
    }
}
