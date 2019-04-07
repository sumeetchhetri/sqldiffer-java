package com.sqldiffer.postgres;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sqldiffer.common.Trigger;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
public class PgTrigger extends Trigger {
    private static final long serialVersionUID = 1L;

    public String generateNew(Object context) {
        StringBuilder b = new StringBuilder();
        b.append("\n");
        b.append(getFunctionDef());
        b.append(";\nCREATE TRIGGER ");
        b.append(getName());
        b.append(" ");
        b.append(getWhen());
        b.append(" ");
        b.append(getAction());
        b.append(" ON ");
        b.append(getTable());
        b.append(" FOR EACH ROW ");
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
        b.append("\nDROP TRIGGER IF EXISTS ");
        b.append(getName());
        b.append(" ON ");
        b.append(getTable());
        b.append(";\nDROP FUNCTION IF EXISTS ");
        b.append(getFunction());
        b.append("();\n");
        return b.toString();
    }

    public String query(Object context) {
        String spl = "select trigger_name, event_object_table, action_timing, event_manipulation, action_statement from information_schema.triggers "
                + "where trigger_schema = ANY (current_schemas(false))";
        return spl;
    }

    public Object fromResult(ResultSet rs, Object context) throws Exception {
        PgTrigger sp = new PgTrigger();
        if(rs!=null) {
            sp.setName(rs.getString(1));
            sp.setTable(rs.getString(2));
            sp.setWhen(rs.getString(3));
            sp.setAction(rs.getString(4));
            sp.setDefinition(rs.getString(5));
            if(sp.getDefinition().toLowerCase().startsWith("execute procedure ")) {
                sp.setFunction(sp.getDefinition().toLowerCase().replaceFirst("execute procedure ", "").replace("()", ""));
            }
        }
        return sp;
    }

    @Override
    public String defineQuery(Object context) {
        return "select pg_get_functiondef(pg_proc.oid) from pg_proc LEFT JOIN pg_namespace n ON n.oid = pg_proc.pronamespace "
                + "where nspname = ANY (current_schemas(false)) and proname = '"+getFunction()+"' and pg_catalog.pg_get_function_result(pg_proc.oid) = 'trigger'";
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

    @Override
    public List<Trigger> mergeDuplicates(List<Trigger> triggers) throws Exception {
        List<Trigger> utriggers = new ArrayList<Trigger>();
        Map<String, List<Trigger>> utrgs = new HashMap<String, List<Trigger>>();
        for (Trigger trigger : triggers) {
            if(!utrgs.containsKey(trigger.getName()+trigger.getWhen()+trigger.getDefinition())) {
                utrgs.put(trigger.getName()+trigger.getWhen()+trigger.getDefinition(), new ArrayList<Trigger>());
            }
            utrgs.get(trigger.getName()+trigger.getWhen()+trigger.getDefinition()).add(trigger);
        }
        for (List<Trigger> trgs : utrgs.values()) {
            List<String> actions = new ArrayList<String>();
            for (Trigger t : trgs) {
                actions.add(t.getAction());
            }
            Trigger tgt = trgs.get(0);
            tgt.setAction(StringUtils.join(actions, " OR "));
            utriggers.add(tgt);
        }
        return utriggers;
    }
}
