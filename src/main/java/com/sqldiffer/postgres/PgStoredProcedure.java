package com.sqldiffer.postgres;

import java.sql.ResultSet;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sqldiffer.common.StoredProcedure;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
public class PgStoredProcedure extends StoredProcedure {
    private static final long serialVersionUID = 1L;

    public String generateNew(Object context) {
        StringBuilder spcb = new StringBuilder();
        spcb.append("\n");
        spcb.append(getDefinition());
        spcb.append(";\n");
        return spcb.toString();
    }

    public String generateUpd(Object context) {
        StringBuilder spcb = new StringBuilder();
        spcb.append(getDropDeclaration());
        spcb.append(getDefinition());
        return spcb.toString();
    }

    public String generateDel(Object context) {
        StringBuilder spcb = new StringBuilder();
        spcb.append("\n");
        spcb.append(getDropDeclaration());
        spcb.append(";\n");
        return spcb.toString();
    }

    public String query(Object context) {
        return "SELECT DISTINCT quote_ident(p.proname) as function, " +
                "'CREATE OR REPLACE FUNCTION ' || p.proname  || '(' || pg_catalog.pg_get_function_arguments(p.oid) || ');\'," + 
                "'DROP FUNCTION ' || p.oid::regprocedure, " + 
                "pronargs " +
                "FROM pg_catalog.pg_proc p " +
                "JOIN pg_catalog.pg_namespace n ON n.oid = p.pronamespace " + 
                "WHERE n.nspname = ANY (current_schemas(false)) " +  
                "and p.proname not like 'pgpool_%' " + 
                "and p.proname not like 'pcp_%' " +
                "and p.proname not like 'dblink%' " +
                "and p.proname not like 'uuid_%' " +
                "and p.proname not like 'pg%' " +
                "and pg_catalog.pg_get_function_result(p.oid) <> 'trigger'";
    }

    public Object fromResult(ResultSet rs, Object context) throws Exception {
        PgStoredProcedure sp = new PgStoredProcedure();
        if(rs!=null) {
            sp.setName(rs.getString(1));
            sp.setDeclaration(rs.getString(2));
            sp.setDropDeclaration(rs.getString(3));
            sp.setNumParams(rs.getInt(4));
        }
        return sp;
    }

    @Override
    public String defineQuery(Object context) {
        return "select pg_get_functiondef(pg_proc.oid) from pg_proc LEFT JOIN pg_namespace n ON n.oid = pg_proc.pronamespace "
                + "where nspname = ANY (current_schemas(false)) and proname = '"+getName()+"' and pronargs = " + getNumParams();
    }

    @Override
    public String definition(ResultSet rs) throws Exception {
        if(rs!=null) {
            String tmp = rs.getString(1);
            if(tmp.toUpperCase().startsWith("CREATE OR REPLACE FUNCTION "+getDb().getSchemaName().toUpperCase()+".")) {
                String eprf = "CREATE OR REPLACE FUNCTION "+getDb().getSchemaName().toUpperCase()+".";
                String prf = "CREATE OR REPLACE FUNCTION ";
                tmp = prf + tmp.substring(eprf.length());
            } else if(tmp.toUpperCase().startsWith("CREATE FUNCTION "+getDb().getSchemaName().toUpperCase()+".")) {
                String eprf = "CREATE FUNCTION "+getDb().getSchemaName().toUpperCase()+".";
                String prf = "CREATE FUNCTION ";
                tmp = prf + tmp.substring(eprf.length());
            }
            return tmp.replace("\r\n", "\n");
        }
        return null;
    }
}
