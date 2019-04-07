package com.sqldiffer.oracle;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sqldiffer.common.Constraint;
import com.sqldiffer.common.Table;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
public class OrclConstraint extends Constraint {
    private static final long serialVersionUID = 1L;
    
    Map<String, OrclConstraint> _c = new HashMap<String, OrclConstraint>();
    
    public String generateNew(Object context) {
        StringBuilder b = new StringBuilder();
        if(getType()=='P' || getType()=='U') {
            b.append("\nALTER TABLE \"");
            b.append(getTable().getName());
            b.append("\" ADD CONSTRAINT \"");
            b.append(getName());
            b.append("\" ");
            if(getType()=='P') {
                String cols = "PRIMARY KEY (";
                Set<String> u = new HashSet<String>();
                for (String c : getColumns()) {
                    if(!u.contains(c)) {
                        cols += c + ",";
                    }
                    u.add(c);
                }
                if(cols.charAt(cols.length()-1)==',') {
                    cols = cols.substring(0, cols.length()-1);
                }
                cols += ")";
                b.append(cols);
            } else if(getType()=='U') {
                b.append("UNIQUE (");
                b.append(getColumns().get(0));
                b.append(")");
            }
            if(StringUtils.isNotBlank(getDefinition())) {
                b.append(getDefinition());
            }
            b.append(";\n/");
        }
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
        if(getType()=='P' || getType()=='U') {
            b.append("\nALTER TABLE \"");
            b.append(getTable().getName());
            b.append("\" DROP CONSTRAINT \"");
            b.append(getName());
            b.append("\";\n/");
        }
        return b.toString();
    }
    public String query(Object context) {
        Object[] args = (Object[])context;
        String spl = "SELECT outer.* FROM (select cons.constraint_name, cons.search_condition, cons.constraint_type, cols.column_name, cols.position, cols.table_name,rownum rn " +
                "FROM user_constraints cons INNER join user_cons_columns cols ON cons.constraint_name = cols.constraint_name " + 
                "AND cons.owner = cols.owner AND cons.status = 'ENABLED' ORDER BY cols.table_name, cols.position) outer where outer.rn >= " + args[0] + " and outer.rn < " + args[1];
        return spl;
    }
    @SuppressWarnings("unchecked")
    public Object fromResult(ResultSet rs, Object context) throws Exception {
        OrclConstraint col = new OrclConstraint();
        String defval = rs.getString(2);//for oracle https://blog.jooq.org/2015/12/30/oracle-long-and-long-raw-causing-stream-has-already-been-closed-exception/
        String tblname = rs.getString(6);
        Map<String, Table> tbls = (Map<String, Table>)context;
        Table tsp = tbls.get(tblname);
        if(tsp==null)return null;
        boolean exists = false;
        for (Constraint pcol :tsp.getConstraints()) {
            OrclConstraint ecol = (OrclConstraint)pcol;
            if(ecol.getName().equals(rs.getString(1))) {
                col = ecol;
                exists = true;
                break;
            }
        }
        if(_c.containsKey(rs.getString(1))) {
            col = _c.get(rs.getString(1));
        } else {
            _c.put(rs.getString(1), col);
        }
        col.setName(rs.getString(1));
        col.setDefinition(defval);
        col.setType(rs.getString(3).charAt(0));
        col.getColumns().add(rs.getString(4));
        col.setTable(tsp);
        if(!exists)tsp.getConstraints().add(col);
        return tsp;
    }
}
