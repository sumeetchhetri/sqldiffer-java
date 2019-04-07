package com.sqldiffer.common;

import java.io.Serializable;
import java.sql.ResultSet;
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
import com.sqldiffer.oracle.OrclStoredProcedure;
import com.sqldiffer.postgres.PgStoredProcedure;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PgStoredProcedure.class, name = "PgStoredProcedure"),
    @JsonSubTypes.Type(value = OrclStoredProcedure.class, name = "OrclStoredProcedure")
})
public abstract class StoredProcedure implements Serializable, Generator, SQLConvert {
    private static final long serialVersionUID = 1L;
    @JsonProperty("n")
    private String name;
    @JsonProperty("d")
    private String declaration;
    @JsonProperty("r")
    private String dropDeclaration;
    @JsonProperty("f")
    private String definition;
    @JsonProperty("p")
    private List<StoredProcedureParam> params = new ArrayList<StoredProcedureParam>();
    @JsonIgnore
    private int numParams = 0;
    @JsonIgnore
    private Db db;
    @JsonIgnore
    private boolean visited = false;
    
    public String getName() {
        return name;
    }
    public String getDeclaration() {
        return declaration;
    }
    public String getDropDeclaration() {
        return dropDeclaration;
    }
    public List<StoredProcedureParam> getParams() {
        return params;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDeclaration(String declaration) {
        this.declaration = declaration;
    }
    public void setDropDeclaration(String dropDeclaration) {
        this.dropDeclaration = dropDeclaration;
    }
    public void setParams(List<StoredProcedureParam> params) {
        this.params = params;
    }
    public int getNumParams() {
        return numParams;
    }
    public void setNumParams(int numParams) {
        this.numParams = numParams;
    }
    public String getDefinition() {
        return definition;
    }
    public void setDefinition(String definition) {
        this.definition = definition;
    }
    public Db getDb() {
        return db;
    }
    public void setDb(Db db) {
        this.db = db;
    }
    public boolean isVisited() {
        return visited;
    }
    public void setVisited(boolean visited) {
        this.visited = visited;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((declaration == null) ? 0 : declaration.hashCode());
        result = prime * result + ((definition == null) ? 0 : definition.hashCode());
        result = prime * result + ((dropDeclaration == null) ? 0 : dropDeclaration.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((params == null) ? 0 : params.hashCode());
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
        StoredProcedure other = (StoredProcedure) obj;
        if (declaration == null) {
            if (other.declaration != null)
                return false;
        } else if (!declaration.equals(other.declaration))
            return false;
        if (!SQLConvert.equalsIgnoreNewLines(definition, other.definition))
            return false;
        if (!SQLConvert.equalsIgnoreNewLines(dropDeclaration, other.dropDeclaration))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (params == null) {
            if (other.params != null)
                return false;
        } else if (!params.equals(other.params))
            return false;
        return true;
    }
    
    public abstract String defineQuery(Object context);
    
    public abstract String definition(ResultSet rs) throws Exception;
}
