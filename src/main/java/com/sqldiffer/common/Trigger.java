package com.sqldiffer.common;

import java.io.Serializable;
import java.sql.ResultSet;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sqldiffer.oracle.OrclTrigger;
import com.sqldiffer.postgres.PgTrigger;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PgTrigger.class, name = "PgTrigger"),
    @JsonSubTypes.Type(value = OrclTrigger.class, name = "OrclTrigger")
})
public abstract class Trigger implements Serializable, Generator, SQLConvert {
    private static final long serialVersionUID = 1L;
    @JsonProperty("n")
    private String name;
    @JsonProperty("t")
    private String table;
    @JsonProperty("w")
    private String when;
    @JsonProperty("a")
    private String action;
    @JsonProperty("f")
    private String function;
    @JsonProperty("g")
    private String functionDef;
    @JsonProperty("d")
    private String definition;
    @JsonIgnore
    private Db db;
    
    public String getName() {
        return name;
    }
    public String getTable() {
        return table;
    }
    public String getWhen() {
        return when;
    }
    public String getAction() {
        return action;
    }
    public String getDefinition() {
        return definition;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setTable(String table) {
        this.table = table;
    }
    public void setWhen(String when) {
        this.when = when;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public String getFunction() {
        return function;
    }
    public void setFunction(String function) {
        this.function = function;
    }
    public void setDefinition(String definition) {
        this.definition = definition;
    }
    public String getFunctionDef() {
        return functionDef;
    }
    public void setFunctionDef(String functionDef) {
        this.functionDef = functionDef;
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
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + ((db == null) ? 0 : db.hashCode());
        result = prime * result + ((definition == null) ? 0 : definition.hashCode());
        result = prime * result + ((function == null) ? 0 : function.hashCode());
        result = prime * result + ((functionDef == null) ? 0 : functionDef.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((table == null) ? 0 : table.hashCode());
        result = prime * result + ((when == null) ? 0 : when.hashCode());
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
        Trigger other = (Trigger) obj;
        if (action == null) {
            if (other.action != null)
                return false;
        } else if (!action.equals(other.action))
            return false;
        if (db == null) {
            if (other.db != null)
                return false;
        } else if (!db.equals(other.db))
            return false;
        if (!SQLConvert.equalsIgnoreNewLines(definition, other.definition))
            return false;
        if (!SQLConvert.equalsIgnoreNewLines(function, other.function))
            return false;
        if (functionDef == null) {
            if (other.functionDef != null)
                return false;
        } else if (!functionDef.equals(other.functionDef))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (table == null) {
            if (other.table != null)
                return false;
        } else if (!table.equals(other.table))
            return false;
        if (when == null) {
            if (other.when != null)
                return false;
        } else if (!when.equals(other.when))
            return false;
        return true;
    }
    
    public abstract String defineQuery(Object context);
    
    public abstract String definition(ResultSet rs) throws Exception;
    
    public abstract List<Trigger> mergeDuplicates(List<Trigger> triggers) throws Exception;
}
