package com.sqldiffer.common;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sqldiffer.oracle.OrclStoredProcedureParam;
import com.sqldiffer.postgres.PgStoredProcedureParam;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PgStoredProcedureParam.class, name = "PgStoredProcedureParam"),
    @JsonSubTypes.Type(value = OrclStoredProcedureParam.class, name = "OrclStoredProcedureParam")
})
public abstract class StoredProcedureParam implements Serializable, Generator, SQLConvert {
    private static final long serialVersionUID = 1L;
    @JsonProperty("n")
    private String name;
    @JsonProperty("t")
    private String type;
    @JsonProperty("m")
    private String mode;
    @JsonProperty("p")
    private int position;
    @JsonProperty("d")
    private String defVal;
    @JsonIgnore
    private String procName;
    
    public String getName() {
        return name;
    }
    public String getType() {
        return type;
    }
    public String getMode() {
        return mode;
    }
    public int getPosition() {
        return position;
    }
    public String getDefVal() {
        return defVal;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setMode(String mode) {
        this.mode = mode;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    public void setDefVal(String defVal) {
        this.defVal = defVal;
    }
    public String getProcName() {
        return procName;
    }
    public void setProcName(String procName) {
        this.procName = procName;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((defVal == null) ? 0 : defVal.hashCode());
        result = prime * result + ((mode == null) ? 0 : mode.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + position;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StoredProcedureParam other = (StoredProcedureParam) obj;
        if (defVal == null) {
            if (other.defVal != null)
                return false;
        } else if (!defVal.equals(other.defVal))
            return false;
        if (mode == null) {
            if (other.mode != null)
                return false;
        } else if (!mode.equals(other.mode))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (position != other.position)
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
