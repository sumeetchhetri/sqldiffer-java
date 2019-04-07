package com.sqldiffer.postgres;

import java.sql.ResultSet;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sqldiffer.common.View;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
public class PgView extends View {
    private static final long serialVersionUID = 1L;

    public String generateNew(Object context) {
        StringBuilder b = new StringBuilder();
        b.append("\nCREATE VIEW ");
        b.append(getName());
        b.append(" AS ");
        b.append(getDefinition());
        b.append("\n");
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
        b.append("\nDROP VIEW ");
        b.append(getName());
        b.append(";\n");
        return b.toString();
    }

    public String query(Object context) {
        String spl = "select table_name,view_definition from INFORMATION_SCHEMA.views WHERE table_schema = ANY (current_schemas(false))";
        return spl;
    }

    public Object fromResult(ResultSet rs, Object context) throws Exception {
        PgView sp = new PgView();
        if(rs!=null) {
            sp.setName(rs.getString(1));
            sp.setType(null);
            sp.setDefinition(rs.getString(2));
        }
        return sp;
    }
}
