package com.sqldiffer.oracle;

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
public class OrclView extends View {
    private static final long serialVersionUID = 1L;

    public String generateNew(Object context) {
        StringBuilder b = new StringBuilder();
        b.append("\nCREATE VIEW \"");
        b.append(getName());
        b.append("\" AS ");
        b.append(getDefinition());
        b.append(";\n/");
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
        b.append("\nDROP VIEW \"");
        b.append(getName());
        b.append("\";\n");
        return b.toString();
    }

    public String query(Object context) {
        String spl = "select view_name,text from user_views";
        return spl;
    }

    public Object fromResult(ResultSet rs, Object context) throws Exception {
        OrclView sp = new OrclView();
        if(rs!=null) {
            sp.setName(rs.getString(1));
            sp.setType(null);
            sp.setDefinition(rs.getString(2));
        }
        return sp;
    }
}
