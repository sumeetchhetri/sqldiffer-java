package com.sqldiffer.oracle;

import java.sql.ResultSet;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sqldiffer.common.SQLConvert;
import com.sqldiffer.common.StoredProcedure;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
public class OrclStoredProcedure extends StoredProcedure {
    private static final long serialVersionUID = 1L;

    public String generateNew(Object context) {
        StringBuilder spcb = new StringBuilder();
        spcb.append("\n");
        spcb.append(getDefinition());
        spcb.append("\n/");
        return spcb.toString();
    }

    public String generateUpd(Object context) {
        StringBuilder spcb = new StringBuilder();
        //spcb.append(getDropDeclaration());
        spcb.append(getDefinition());
        return spcb.toString();
    }

    public String generateDel(Object context) {
        StringBuilder spcb = new StringBuilder();
        spcb.append("\n");
        spcb.append(getDropDeclaration());
        spcb.append(";\n/");
        return spcb.toString();
    }

    public String query(Object context) {
        return "SELECT object_name,dbms_metadata.get_ddl(object_type, object_name) FROM user_objects WHERE object_type IN ('FUNCTION','PROCEDURE','PACKAGE')";
    }

    public Object fromResult(ResultSet rs, Object context) throws Exception {
        OrclStoredProcedure sp = new OrclStoredProcedure();
        if(rs!=null) {
            sp.setName(rs.getString(1));
            sp.setDefinition(rs.getString(2).trim());
            sp.setDropDeclaration("DROP PROCEDURE " + sp.getName());
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
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StoredProcedure other = (StoredProcedure) obj;
        if (getDeclaration() == null) {
            if (other.getDeclaration() != null)
                return false;
        } else if (!getDeclaration().equals(other.getDeclaration()))
            return false;
        if (!SQLConvert.equalsIgnoreNewLines(getDefinition(), other.getDefinition()))
            return false;
        if (!SQLConvert.equalsIgnoreNewLines(getDropDeclaration(), other.getDropDeclaration()))
            return false;
        if (getName() == null) {
            if (other.getName() != null)
                return false;
        } else if (!getName().equals(other.getName()))
            return false;
        /*if (getParams() == null) {
            if (other.getParams() != null)
                return false;
        } else if (!getParams().equals(other.getParams()))
            return false;*/
        return true;
    }
}
