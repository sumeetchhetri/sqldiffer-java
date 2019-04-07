package com.sqldiffer.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sqldiffer.oracle.OrclIndex;
import com.sqldiffer.postgres.PgIndex;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PgIndex.class, name = "PgIndex"),
    @JsonSubTypes.Type(value = OrclIndex.class, name = "OrclIndex")
})
public abstract class Index implements Serializable, Generator, SQLConvert {
    private static final long serialVersionUID = 1L;
    @JsonProperty("n")
    private String name;
    @JsonProperty("t")
    private String table;
    @JsonProperty("p")
    private Map<String, String> props = new HashMap<String, String>();
    @JsonProperty("d")
    private String definition;
    @JsonIgnore
    private Table tableO;
    
    public String getName() {
        //if(tableO!=null && tableO.getDb()!=null && tableO.getDb().getSchemaName()!=null) {
        //    return "\"" + tableO.getDb().getSchemaName() + "\".\"" + name + "\"";
        //}
        return name;
    }
    public String getTable() {
        return table;
    }
    public Map<String, String> getProps() {
        return props;
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
    public void setProps(Map<String, String> props) {
        this.props = props;
    }
    public void setDefinition(String definition) {
        this.definition = definition;
    }
    public Table getTableO() {
        return tableO;
    }
    public void setTableO(Table tableO) {
        this.tableO = tableO;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((definition == null) ? 0 : definition.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((props == null) ? 0 : props.hashCode());
        result = prime * result + ((table == null) ? 0 : table.hashCode());
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
        Index other = (Index) obj;
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
        if (props == null) {
            if (other.props != null)
                return false;
        } else if (!props.equals(other.props))
            return false;
        if (table == null) {
            if (other.table != null)
                return false;
        } else if (!table.equals(other.table))
            return false;
        return true;
    }
}
