package com.sqldiffer.sqlserver;

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
import com.sqldiffer.oracle.OrclTrigger;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
public class SqlServerTrigger extends Trigger {
    private static final long serialVersionUID = 1L;

    public String generateNew(Object context) {
        return getDefinition() + ";\n/";
    }

    public String generateUpd(Object context) {
        StringBuilder b = new StringBuilder();
        b.append(generateDel(context));
        b.append(generateNew(context));
        return b.toString();
    }

    public String generateDel(Object context) {
        StringBuilder b = new StringBuilder();
        b.append("\nDROP TRIGGER \"");
        b.append(getName());
        b.append("\";\n/");
        return b.toString();
    }

    public String query(Object context) {
        String spl = "SELECT Triggers.name TriggerName,Comments.Text TriggerText FROM sysobjects Triggers Inner Join sysobjects Tables On "
                + "Triggers.parent_obj = Tables.id Inner Join syscomments Comments On Triggers.id = Comments.id WHERE Triggers.xtype = 'TR' And "
                + "Tables.xtype = 'U' ORDER BY Tables.Name, Triggers.name";
        return spl;
    }

    public Object fromResult(ResultSet rs, Object context) throws Exception {
        OrclTrigger sp = new OrclTrigger();
        Object[] arr = (Object[])context;
        String schema = (String)arr[1];
        if(rs!=null) {
            sp.setName(rs.getString(1));
            sp.setTable(rs.getString(2));
            sp.setWhen(rs.getString(3).split(" ")[0]);
            sp.setAction(rs.getString(4));
            sp.setFunction(rs.getString(5));
            String funcDef = rs.getString(6);
            if(funcDef!=null) {
                funcDef = funcDef.trim().replaceFirst(schema+".", "").replaceFirst("\""+schema+"\".", "");
            }
            sp.setFunctionDef(rs.getString(6));
            sp.setDefinition(rs.getString(7));
        }
        return sp;
    }

    @Override
    public String defineQuery(Object context) {
        return null;
    }

    @Override
    public String definition(ResultSet rs) throws Exception {
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
