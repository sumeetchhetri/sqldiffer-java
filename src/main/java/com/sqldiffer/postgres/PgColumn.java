package com.sqldiffer.postgres;

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
public class PgColumn extends Column {
    private static final long serialVersionUID = 1L;
    
    public String generateNew(Object context) {
        StringBuilder b = new StringBuilder();
        b.append("\nALTER TABLE ");
        b.append(getTable().getName());
        b.append(" ADD COLUMN ");
        if(getName().matches("^[a-zA-Z_][a-zA-Z0-9_]*")) {
            b.append(getName());
        } else {
            b.append("\"");
            b.append(getName());
            b.append("\"");
        }
        b.append(" ");
        b.append(getType());
        if(isNotnull()) {
            b.append(" NOT NULL ");
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
            b.append("\nALTER TABLE ");
            b.append(getTable().getName());
            b.append(" ALTER COLUMN ");
            if(getName().matches("^[a-zA-Z_][a-zA-Z0-9_]*")) {
                b.append(getName());
            } else {
                b.append("\"");
                b.append(getName());
                b.append("\"");
            }
            b.append(" TYPE ");
            b.append(getType());
            b.append(" USING ");
            b.append(getName());
            String t = getType();
            if(getType().indexOf("(")!=-1) {
                t = getType().substring(0, getType().indexOf("("));
            }
            b.append("::");
            b.append(t);
            b.append(";\n");
        } else if(dstcol!=null && getDefVal()!=null && !getDefVal().equals(dstcol.getDefVal())) {
            b.append("\nALTER TABLE ");
            b.append(getTable().getName());
            b.append(" ALTER COLUMN ");
            if(getName().matches("^[a-zA-Z_][a-zA-Z0-9_]*")) {
                b.append(getName());
            } else {
                b.append("\"");
                b.append(getName());
                b.append("\"");
            }
            b.append(" SET DEFAULT ");
            b.append(getDefVal());
            b.append(";\n");
        } else if(dstcol!=null && dstcol.getDefVal()!=null && !dstcol.getDefVal().equals(getDefVal())) {
            b.append("\nALTER TABLE ");
            b.append(getTable().getName());
            b.append(" ALTER COLUMN ");
            if(getName().matches("^[a-zA-Z_][a-zA-Z0-9_]*")) {
                b.append(getName());
            } else {
                b.append("\"");
                b.append(getName());
                b.append("\"");
            }
            b.append(" DROP DEFAULT ");
            b.append(getDefVal());
            b.append(";\n");
        }
        return b.toString();
    }
    public String generateDel(Object context) {
        StringBuilder b = new StringBuilder();
        b.append("\nALTER TABLE ");
        b.append(getTable().getName());
        b.append(" DROP COLUMN ");
        if(getName().matches("^[a-zA-Z_][a-zA-Z0-9_]*")) {
            b.append(getName());
        } else {
            b.append("\"");
            b.append(getName());
            b.append("\"");
        }
        b.append(";\n");
        return b.toString();
    }
    public String query(Object context) {
        Object[] arr = (Object[])context;
        String spl = "SELECT DISTINCT f.attnum AS number, f.attname AS name, f.attnotnull AS notnull, pg_catalog.format_type(f.atttypid,f.atttypmod) AS type, "
                + "CASE WHEN p.contype = 'p' THEN 't' ELSE 'f' END AS primarykey, CASE WHEN p.contype = 'u' THEN 't' ELSE 'f' END AS uniquekey, "
                + "CASE WHEN p.contype = 'f' THEN g.relname END AS foreignkey, CASE WHEN p.contype = 'f' THEN p.confkey END AS foreignkey_fieldnum, "
                + "CASE WHEN p.contype = 'f' THEN g.relname END AS foreignkey, CASE WHEN p.contype = 'f' THEN p.conkey END AS foreignkey_connnum, "
                + "CASE WHEN f.atthasdef = 't' THEN d.adsrc END AS default, p.contype, c.relname FROM pg_attribute f JOIN pg_class c ON c.oid = f.attrelid "
                + "JOIN pg_type t ON t.oid = f.atttypid LEFT JOIN pg_attrdef d ON d.adrelid = c.oid AND d.adnum = f.attnum "
                + "LEFT JOIN pg_namespace n ON n.oid = c.relnamespace LEFT JOIN pg_constraint p ON p.conrelid = c.oid AND f.attnum = ANY (p.conkey) "
                + "LEFT JOIN pg_class AS g ON p.confrelid = g.oid  WHERE c.relkind = 'r'::char AND n.nspname = ANY (current_schemas(false))"
                + "AND f.attnum > 0 ORDER BY c.relname, f.attname, number limit " + arr[1] + " offset " + arr[0];
        return spl;
    }
    @SuppressWarnings("unchecked")
    public Object fromResult(ResultSet rs, Object context) throws Exception {
        PgColumn col = new PgColumn();
        String tblname = rs.getString(13);
        Object[] arr = (Object[])context;
        Map<String, Table> tbls = (Map<String, Table>)arr[0];
        Table tsp = tbls.get(tblname);
        if(tsp==null)return null;
        boolean exists = false;
        for (Column pcol :tsp.getColumns()) {
            PgColumn ecol = (PgColumn)pcol;
            if(ecol.getName().equals(rs.getString(2))) {
                col = ecol;
                exists = true;
                break;
            }
        }
        col.setPos(rs.getInt(1));
        col.setName(rs.getString(2));
        col.setNotnull(rs.getBoolean(3));
        col.setType(rs.getString(4));
        if(rs.getString(12)!=null && rs.getString(12).equals("p")) {
            col.setPkey(rs.getBoolean(5));
        }
        if(rs.getString(12)!=null && rs.getString(12).equals("u")) {
            col.setUniq(rs.getBoolean(6));
        }
        col.setFkey1(rs.getString(7));
        col.setFkeyfnum1(rs.getString(8));
        col.setFkey2(rs.getString(9));
        col.setFkeyfnum2(rs.getString(10));
        col.setDefVal(rs.getString(11));
        col.setTable(tsp);
        if(!exists)tsp.getColumns().add(col);
        return tsp;
    }
}
