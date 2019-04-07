package com.sqldiffer.postgres;

import java.sql.ResultSet;
import java.util.Map;

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
public class PgConstraint extends Constraint {
    private static final long serialVersionUID = 1L;
    
    public String generateNew(Object context) {
        StringBuilder b = new StringBuilder();
        b.append("\nALTER TABLE ");
        b.append(getTable().getName());
        b.append(" ADD CONSTRAINT ");
        b.append(getName());
        b.append(" ");
        b.append(getDefinition());
        b.append(";\n");
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
        b.append("\nALTER TABLE ");
        b.append(getTable().getName());
        b.append(" DROP CONSTRAINT ");
        b.append(getName());
        b.append(";\n");
        return b.toString();
    }
    public String query(Object context) {
        Object[] arr = (Object[])context;
        String spl = "select conname, pg_get_constraintdef(c.oid), conrelid::regclass::varchar as relname "
                + "from pg_constraint c "
                + "join pg_namespace n ON n.oid = c.connamespace "
                + "where contype in ('f', 'p','c','u') and n.nspname = ANY (current_schemas(false)) "
                + "order by conname, relname limit " + arr[1] + " offset " + arr[0];
        return spl;
    }
    @SuppressWarnings("unchecked")
    public Object fromResult(ResultSet rs, Object context) throws Exception {
        PgConstraint col = new PgConstraint();
        String tblname = rs.getString(3);
        Map<String, Table> tbls = (Map<String, Table>)context;
        Table tsp = tbls.get(tblname);
        if(tsp==null)return null;
        col.setName(rs.getString(1));
        col.setDefinition(rs.getString(2));
        col.setTable(tsp);
        tsp.getConstraints().add(col);
        return tsp;
    }
}
