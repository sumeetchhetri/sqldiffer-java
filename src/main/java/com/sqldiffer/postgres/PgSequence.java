package com.sqldiffer.postgres;

import java.sql.ResultSet;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sqldiffer.common.Sequence;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
public class PgSequence extends Sequence {
    private static final long serialVersionUID = 1L;

    public String generateNew(Object context) {
        StringBuilder b = new StringBuilder();
        b.append("\nCREATE SEQUENCE ");
        b.append(getName());
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
        b.append("\nDROP SEQUENCE ");
        b.append(getName());
        b.append(";\n");
        return b.toString();
    }

    public String query(Object context) {
        String spl = "SELECT table_name,column_default,column_name,null as sequence_name,null,null,null,null FROM information_schema.columns WHERE column_default LIKE 'nextval%' and " + 
                     "table_schema = ANY (current_schemas(false)) UNION SELECT null,null,null,sequence_name,minimum_value,maximum_value,increment,cycle_option " + 
                     "FROM information_schema.sequences where sequence_schema = ANY (current_schemas(false)) order by sequence_name";
        return spl;
    }

    @SuppressWarnings("unchecked")
    public Object fromResult(ResultSet rs, Object context) throws Exception {
        Map<String, Sequence> uniqs = (Map<String, Sequence>)context;
        PgSequence sp = new PgSequence();
        if(rs!=null) {
            if(rs.getString(1)!=null) {
                String sqname = rs.getString(2).trim().substring(9).replace("::regclass)", "").replace("'", "");
                if(uniqs.containsKey(sqname)) {
                    sp = (PgSequence)uniqs.get(sqname);
                    sp.setTableName(rs.getString(1));
                    sp.setName(sqname);
                    sp.setColName(rs.getString(3));
                    sp.setDefVal(rs.getString(2));
                }
            } else {
                sp.setName(rs.getString(4));
                sp.setMin(rs.getBigDecimal(5));
                sp.setMax(rs.getBigDecimal(6));
                sp.setInc(rs.getBigDecimal(7));
                sp.setCycle(rs.getString(8));
                uniqs.put(rs.getString(4), sp);
            }
        }
        return sp;
    }
}
