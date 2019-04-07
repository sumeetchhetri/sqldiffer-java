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
import com.sqldiffer.oracle.OrclConstraint;
import com.sqldiffer.postgres.PgConstraint;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PgConstraint.class, name = "PgConstraint"),
    @JsonSubTypes.Type(value = OrclConstraint.class, name = "OrclConstraint")
})
public abstract class Constraint implements Serializable, Generator, SQLConvert {
    private static final long serialVersionUID = 1L;
    @JsonProperty("n")
    private String name;
    @JsonProperty("d")
    private String definition;
    @JsonProperty("t")
    private char type;
    @JsonProperty("c")
    private List<String> columns = new ArrayList<String>();
    @JsonIgnore
    private Table table;
    
    public String getName() {
        //if(table!=null && table.getDb()!=null && table.getDb().getSchemaName()!=null) {
        //    return "\"" + table.getDb().getSchemaName() + "\".\"" + name + "\"";
        //}
        return name;
    }
    public String getDefinition() {
        return definition;
    }
    public Table getTable() {
        return table;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDefinition(String definition) {
        this.definition = definition;
    }
    public char getType() {
        return type;
    }
    public void setType(char type) {
        this.type = type;
    }
    public List<String> getColumns() {
        return columns;
    }
    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
    public void setTable(Table table) {
        this.table = table;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((definition == null) ? 0 : definition.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        Constraint other = (Constraint) obj;
        if (definition == null) {
            if (other.definition != null)
                return false;
        } else if (!definition.equals(other.definition))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}
