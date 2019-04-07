package com.sqldiffer.oracle;

import java.sql.ResultSet;

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
public class OrclStoredProcedureParam extends StoredProcedureParam {
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
        Object[] args = (Object[])context;
        return "SELECT outer.* FROM (SELECT position,argument_name,data_type,data_length,data_precision,in_out,object_name,rownum rn "
                + "FROM user_arguments) outer where outer.rn >= " + args[0] + " and outer.rn < " + args[1];
    }

    public Object fromResult(ResultSet rs, Object context) throws Exception {
        OrclStoredProcedureParam pgsa = new OrclStoredProcedureParam();
        pgsa.setPosition(rs.getInt(1));
        pgsa.setName(rs.getString(2));
        pgsa.setType(rs.getString(3));
        if(rs.getBigDecimal(4)!=null) {
            pgsa.setType(pgsa.getType()+"("+rs.getBigDecimal(4).longValue());
            if(rs.getBigDecimal(5)!=null) {
                pgsa.setType(pgsa.getType()+","+rs.getBigDecimal(5).longValue());
            }
            pgsa.setType(pgsa.getType()+")");
        }
        pgsa.setMode(rs.getString(6));
        pgsa.setProcName(rs.getString(7));
        return pgsa;
    }
}
