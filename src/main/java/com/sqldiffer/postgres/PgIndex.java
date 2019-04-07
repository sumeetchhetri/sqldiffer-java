package com.sqldiffer.postgres;

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
public class PgIndex extends Index {
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
        String spl = "SELECT i.relname as indname, i.relowner as indowner, idx.indrelid::regclass, am.amname as indam, "
                + "idx.indkey, ARRAY( SELECT pg_get_indexdef(idx.indexrelid, k + 1, true) FROM generate_subscripts(idx.indkey, 1) as k ORDER BY k )::text as indkey_names, "
                + "idx.indexprs IS NOT NULL as indexprs, idx.indpred IS NOT NULL as indpred FROM pg_index as idx JOIN pg_class as i ON i.oid = idx.indexrelid "
                + "AND idx.indkey::text = '0' JOIN pg_am as am ON i.relam = am.oid JOIN pg_namespace as ns ON ns.oid = i.relnamespace AND ns.nspname = ANY(current_schemas(false));";
        return spl;
    }

    public Object fromResult(ResultSet rs, Object context) throws Exception {
        PgIndex sp = new PgIndex();
        if(rs!=null) {
            sp.setName(rs.getString(1));
            sp.setTable(rs.getString(3));
            sp.getProps().put("am", rs.getString(4));
            sp.getProps().put("key_names", rs.getString(6));
        }
        return sp;
    }
}
