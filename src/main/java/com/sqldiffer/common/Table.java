package com.sqldiffer.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sqldiffer.oracle.OrclTable;
import com.sqldiffer.postgres.PgTable;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PgTable.class, name = "PgTable"),
    @JsonSubTypes.Type(value = OrclTable.class, name = "OrclTable")
})
public abstract class Table implements Serializable, Generator, SQLConvert {
    private static final long serialVersionUID = 1L;
    @JsonProperty("n")
    private String name;
    @JsonProperty("c")
    private List<Column> columns = new ArrayList<Column>();
    @JsonProperty("t")
    private List<Trigger> triggers = new ArrayList<Trigger>();
    @JsonProperty("i")
    private List<Index> indexes = new ArrayList<Index>();
    @JsonProperty("o")
    private List<Constraint> constraints = new ArrayList<Constraint>();
    @JsonProperty("p")
    private boolean isTemp;
    @JsonIgnore
    private Db db;
    
    public String getName() {
        //if(db!=null && db.getSchemaName()!=null) {
        //   return "\"" + db.getSchemaName() + "\".\"" + name + "\"";
        //}
        return name;
    }
    public List<Column> getColumns() {
        return columns;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }
    public List<Trigger> getTriggers() {
        return triggers;
    }
    public List<Index> getIndexes() {
        return indexes;
    }
    public void setTriggers(List<Trigger> triggers) {
        this.triggers = triggers;
    }
    public void setIndexes(List<Index> indexes) {
        this.indexes = indexes;
    }
    public List<Constraint> getConstraints() {
        return constraints;
    }
    public void setConstraints(List<Constraint> constraints) {
        this.constraints = constraints;
    }
    public boolean isTemp() {
        return isTemp;
    }
    public void setTemp(boolean isTemp) {
        this.isTemp = isTemp;
    }
    public Db getDb() {
        return db;
    }
    public void setDb(Db db) {
        this.db = db;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((columns == null) ? 0 : columns.hashCode());
        result = prime * result + ((constraints == null) ? 0 : constraints.hashCode());
        result = prime * result + ((indexes == null) ? 0 : indexes.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((triggers == null) ? 0 : triggers.hashCode());
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
        Table other = (Table) obj;
        if (columns == null) {
            if (other.columns != null)
                return false;
        } else if (!columns.equals(other.columns))
            return false;
        if (constraints == null) {
            if (other.constraints != null)
                return false;
        } else if (!constraints.equals(other.constraints))
            return false;
        if (indexes == null) {
            if (other.indexes != null)
                return false;
        } else if (!indexes.equals(other.indexes))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (triggers == null) {
            if (other.triggers != null)
                return false;
        } else if (!triggers.equals(other.triggers))
            return false;
        return true;
    }
}
