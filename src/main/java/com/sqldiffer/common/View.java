package com.sqldiffer.common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sqldiffer.oracle.OrclView;
import com.sqldiffer.postgres.PgView;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PgView.class, name = "PgView"),
    @JsonSubTypes.Type(value = OrclView.class, name = "OrclView")
})
public abstract class View implements Serializable, Generator, SQLConvert {
    private static final long serialVersionUID = 1L;
    @JsonProperty("n")
    private String name;
    @JsonProperty("t")
    private String type;
    @JsonProperty("d")
    private String definition;
    @JsonIgnore
    private Integer weight = 0;
    @JsonIgnore
    private Set<String> relatedViewNames = new HashSet<String>();
    @JsonIgnore
    private Db db;
    
    public String getName() {
        return name;
    }
    public String getType() {
        return type;
    }
    public String getDefinition() {
        return definition;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setDefinition(String definition) {
        this.definition = definition;
    }
    public Integer getWeight() {
        return weight;
    }
    public void setWeight(Integer weight) {
        this.weight = weight;
    }
    public Set<String> getRelatedViewNames() {
        return relatedViewNames;
    }
    public void setRelatedViewNames(Set<String> relatedViewNames) {
        this.relatedViewNames = relatedViewNames;
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
        result = prime * result + ((definition == null) ? 0 : definition.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        View other = (View) obj;
        if (!SQLConvert.equalsIgnoreNewLines(definition, other.definition))
            return false;
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
