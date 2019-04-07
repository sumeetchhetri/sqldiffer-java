package com.sqldiffer.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sqldiffer.oracle.OrclDb;
import com.sqldiffer.postgres.PgDb;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PgDb.class, name = "PgDb"),
    @JsonSubTypes.Type(value = OrclDb.class, name = "OrclDb")
})
public abstract class Db implements Serializable, Generator {
    private static final long serialVersionUID = 1L;
    @JsonProperty("n")
    private String name;
    @JsonProperty("s")
    private String schemaName;
    @JsonProperty("d")
    private String driver;
    @JsonProperty("p")
    private List<StoredProcedure> storedProcs = new ArrayList<StoredProcedure>();
    @JsonProperty("t")
    private List<Table> tables = new ArrayList<Table>();
    @JsonProperty("v")
    private List<View> views = new ArrayList<View>();
    @JsonProperty("q")
    private Set<Sequence> sequences = new HashSet<Sequence>();
    @JsonProperty("a")
    private boolean duplicateProcNamesAllowed;
    
    public String getName() {
        return name;
    }
    public String getSchemaName() {
        return schemaName;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
    public String getDriver() {
        return driver;
    }
    public void setDriver(String driver) {
        this.driver = driver;
    }
    public Set<Sequence> getSequences() {
        return sequences;
    }
    public void setSequences(Set<Sequence> sequences) {
        this.sequences = sequences;
    }
    public boolean isDuplicateProcNamesAllowed() {
        return duplicateProcNamesAllowed;
    }
    public void setDuplicateProcNamesAllowed(boolean duplicateProcNamesAllowed) {
        this.duplicateProcNamesAllowed = duplicateProcNamesAllowed;
    }
    public List<StoredProcedure> getStoredProcs() {
        return storedProcs;
    }
    public List<Table> getTables() {
        return tables;
    }
    public List<View> getViews() {
        return views;
    }
    public void setStoredProcs(List<StoredProcedure> storedProcs) {
        this.storedProcs = storedProcs;
    }
    public void setTables(List<Table> tables) {
        this.tables = tables;
    }
    public void setViews(List<View> views) {
        this.views = views;
    }
    public abstract String generateUrl(String driver, String host, String dbName, String schema);
    public abstract String preface();
    public abstract String create();
    public abstract String connect();
    public abstract String createSchema();
}
