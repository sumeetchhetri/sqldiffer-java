package com.sqldiffer.postgres;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sqldiffer.common.StoredProcedureParam;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
public class PgStoredProcedureParam extends StoredProcedureParam {
    private static final long serialVersionUID = 1L;

    public String generateNew(Object context) {
        return null;
    }

    public String generateUpd(Object context) {
        return null;
    }

    public String generateDel(Object context) {
        return null;
    }

    public String query(Object context) {
        String spName = (String)((Object[])context)[0];
        Integer spNumPars = (Integer)((Object[])context)[1];
        String spl = "SELECT pg_catalog.pg_get_function_result(p.oid)," +
                     "pg_catalog.pg_get_function_arguments(p.oid)" +
                     "FROM pg_catalog.pg_proc p " +
                     "JOIN pg_catalog.pg_namespace n ON n.oid = p.pronamespace " +
                     "WHERE n.nspname not like 'pg%' " +
                     "and n.nspname not like 'information_%'  " +
                     "and p.proname = '"+spName+"' and p.pronargs = " + spNumPars;
        return spl;
    }

    public Object fromResult(ResultSet rs, Object context) throws Exception {
        List<StoredProcedureParam> columns = new ArrayList<StoredProcedureParam>();
        String[] ar = rs.getString(2).split(",");
        for (String a: ar) {
            if(a.trim().isEmpty())continue;
            PgStoredProcedureParam pgsa = new PgStoredProcedureParam();
            pgsa.setMode("IN");
            if(a.toLowerCase().startsWith("out ")) {
                a = a.substring(4).trim();
                pgsa.setMode("OUT");
            } else if(a.toLowerCase().startsWith("inout ")) {
                a = a.substring(6).trim();
                pgsa.setMode("INOUT");
            } else {
                a = a.trim();
            }
            String pnm = a.indexOf(" ")!=-1?a.substring(0, a.indexOf(" ")):"";
            a = a.substring(a.indexOf(" ")+1);
            pgsa.setName(pnm);
            pgsa.setPosition(columns.size()+1);
            
            String type = a;
            if(a.toLowerCase().indexOf(" default ")!=-1) {
                type = a.substring(0, a.toLowerCase().indexOf(" default "));
                a = a.substring(a.toLowerCase().indexOf(" default ")+9);
                pgsa.setDefVal(a);
            }
            pgsa.setType(type);
            columns.add(pgsa);
        }
        return columns;
    }
}
