package com.sqldiffer.common;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sqldiffer.oracle.OrclSequence;
import com.sqldiffer.postgres.PgSequence;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PgSequence.class, name = "PgSequence"),
    @JsonSubTypes.Type(value = OrclSequence.class, name = "OrclSequence")
})
public abstract class Sequence implements Serializable, Generator, SQLConvert {
    private static final long serialVersionUID = 1L;
    @JsonProperty("n")
    private String name;
    @JsonProperty("t")
    private String tableName;
    @JsonProperty("c")
    private String colName;
    @JsonProperty("d")
    private String defVal;
    @JsonProperty("e")
    private String cycle;
    @JsonProperty("f")
    private BigDecimal min;
    @JsonProperty("g")
    private BigDecimal max;
    @JsonProperty("h")
    private BigDecimal inc;
    @JsonIgnore
    private Db db;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getTableName() {
        return tableName;
    }
    public String getColName() {
        return colName;
    }
    public String getDefVal() {
        return defVal;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public void setColName(String colName) {
        this.colName = colName;
    }
    public void setDefVal(String defVal) {
        this.defVal = defVal;
    }
    public BigDecimal getMin() {
        return min;
    }
    public BigDecimal getMax() {
        return max;
    }
    public BigDecimal getInc() {
        return inc;
    }
    public void setMin(BigDecimal min) {
        this.min = min;
    }
    public void setMax(BigDecimal max) {
        this.max = max;
    }
    public void setInc(BigDecimal inc) {
        this.inc = inc;
    }
    public Db getDb() {
        return db;
    }
    public void setDb(Db db) {
        this.db = db;
    }
    public String getCycle() {
        return cycle;
    }
    public void setCycle(String cycle) {
        this.cycle = cycle;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((colName == null) ? 0 : colName.hashCode());
        result = prime * result + ((defVal == null) ? 0 : defVal.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
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
        Sequence other = (Sequence) obj;
        /*if (colName == null) {
            if (other.colName != null)
                return false;
        } else if (!colName.equals(other.colName))
            return false;*/
        /*if (defVal == null) {
            if (other.defVal != null)
                return false;
        } else if (!defVal.equals(other.defVal))
            return false;*/
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        /*if (tableName == null) {
            if (other.tableName != null)
                return false;
        } else if (!tableName.equals(other.tableName))
            return false;*/
        return true;
    }
}
