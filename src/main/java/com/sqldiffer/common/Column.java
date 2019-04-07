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
import com.sqldiffer.oracle.OrclColumn;
import com.sqldiffer.postgres.PgColumn;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PgColumn.class, name = "PgColumn"),
    @JsonSubTypes.Type(value = OrclColumn.class, name = "OrclColumn")
})
public abstract class Column implements Serializable, Generator, SQLConvert {
    private static final long serialVersionUID = 1L;
    @JsonProperty("p")
    private int pos;
    @JsonProperty("n")
    private String name;
    @JsonProperty("l")
    private boolean notnull;
    @JsonProperty("t")
    private String type;
    @JsonProperty("k")
    private boolean pkey;
    @JsonProperty("u")
    private boolean uniq;
    @JsonProperty("f1")
    private String fkey1;
    @JsonProperty("fn1")
    private String fkeyfnum1;
    @JsonProperty("f2")
    private String fkey2;
    @JsonProperty("fn2")
    private String fkeyfnum2;
    @JsonProperty("d")
    private String defVal;
    @JsonIgnore
    private Table table;
    public int getPos() {
        return pos;
    }
    public String getName() {
        return name;
    }
    public boolean isNotnull() {
        return notnull;
    }
    public String getType() {
        return type;
    }
    public boolean isPkey() {
        return pkey;
    }
    public boolean isUniq() {
        return uniq;
    }
    public String getFkey1() {
        return fkey1;
    }
    public String getFkeyfnum1() {
        return fkeyfnum1;
    }
    public String getFkey2() {
        return fkey2;
    }
    public String getFkeyfnum2() {
        return fkeyfnum2;
    }
    public String getDefVal() {
        return defVal;
    }
    public void setPos(int pos) {
        this.pos = pos;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setNotnull(boolean notnull) {
        this.notnull = notnull;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setPkey(boolean pkey) {
        this.pkey = pkey;
    }
    public void setUniq(boolean uniq) {
        this.uniq = uniq;
    }
    public void setFkey1(String fkey1) {
        this.fkey1 = fkey1;
    }
    public void setFkeyfnum1(String fkeyfnum1) {
        this.fkeyfnum1 = fkeyfnum1;
    }
    public void setFkey2(String fkey2) {
        this.fkey2 = fkey2;
    }
    public void setFkeyfnum2(String fkeyfnum2) {
        this.fkeyfnum2 = fkeyfnum2;
    }
    public void setDefVal(String defVal) {
        this.defVal = defVal;
    }
    public Table getTable() {
        return table;
    }
    public void setTable(Table table) {
        this.table = table;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((defVal == null) ? 0 : defVal.hashCode());
        result = prime * result + ((fkey1 == null) ? 0 : fkey1.hashCode());
        result = prime * result + ((fkey2 == null) ? 0 : fkey2.hashCode());
        result = prime * result + ((fkeyfnum1 == null) ? 0 : fkeyfnum1.hashCode());
        result = prime * result + ((fkeyfnum2 == null) ? 0 : fkeyfnum2.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (notnull ? 1231 : 1237);
        result = prime * result + (pkey ? 1231 : 1237);
        result = prime * result + pos;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + (uniq ? 1231 : 1237);
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
        Column other = (Column) obj;
        if (defVal == null) {
            if (other.defVal != null)
                return false;
        } else if (!defVal.equalsIgnoreCase(other.defVal))
            return false;
        /*if (notnull != other.notnull)
            return false;
        if (pkey != other.pkey)
            return false;
        if (pos != other.pos)
            return false;
        if (uniq != other.uniq)
            return false;
        if (fkey1 == null) {
            if (other.fkey1 != null)
                return false;
        } else if (!fkey1.equals(other.fkey1))
            return false;
        if (fkey2 == null) {
            if (other.fkey2 != null)
                return false;
        } else if (!fkey2.equals(other.fkey2))
            return false;
        if (fkeyfnum1 == null) {
            if (other.fkeyfnum1 != null)
                return false;
        } else if (!fkeyfnum1.equals(other.fkeyfnum1))
            return false;
        if (fkeyfnum2 == null) {
            if (other.fkeyfnum2 != null)
                return false;
        } else if (!fkeyfnum2.equals(other.fkeyfnum2))
            return false;*/
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
