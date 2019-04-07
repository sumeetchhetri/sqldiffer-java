package com.sqldiffer.oracle;

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
public class OrclSequence extends Sequence {
    private static final long serialVersionUID = 1L;

    public String generateNew(Object context) {
        StringBuilder b = new StringBuilder();
        b.append("\nCREATE SEQUENCE \"");
        b.append(getName());
        b.append("\";\n/");
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
        b.append("\nDROP SEQUENCE \"");
        b.append(getName());
        b.append("\";\n/");
        return b.toString();
    }

    public String query(Object context) {
        String spl = "SELECT sequence_name,min_value,max_value,increment_by,cycle_flag FROM user_sequences";
        return spl;
    }

    @SuppressWarnings("unchecked")
    public Object fromResult(ResultSet rs, Object context) throws Exception {
        Map<String, Sequence> uniqs = (Map<String, Sequence>)context;
        OrclSequence sp = new OrclSequence();
        sp.setName(rs.getString(1));
        sp.setMin(rs.getBigDecimal(2));
        sp.setMax(rs.getBigDecimal(3));
        sp.setInc(rs.getBigDecimal(4));
        sp.setCycle(rs.getString(5));
        uniqs.put(sp.getName(), sp);
        return sp;
    }
}
