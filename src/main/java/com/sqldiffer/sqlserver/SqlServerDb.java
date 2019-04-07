package com.sqldiffer.sqlserver;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sqldiffer.common.Db;

/**
 * @author Sumeet Chhetri<br/>
 *
 */

@JsonAutoDetect(getterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY, isGetterVisibility=Visibility.NONE)
@JsonInclude(Include.NON_NULL)
public class SqlServerDb extends Db {
    private static final long serialVersionUID = 1L;
    
    public String generateUrl(String driver, String host, String dbName, String schema) {
        return "jdbc:postgresql://"+host+"/"+dbName+"?searchpath="+schema+"&currentSchema="+schema;
    }

    public String generateNew(Object context) {
        return null;
    }

    public String generateUpd(Object context) {
        return null;
    }

    public String generateDel(Object context) {
        return null;
    }

    @Override
    public String preface() {
        StringBuilder b = new StringBuilder();
        b.append("--");
        b.append("\n-- PostgreSQL database dump");
        b.append("\n--");
        b.append("\nSET statement_timeout = 0;");
        b.append("\nSET lock_timeout = 0;");
        b.append("\nSET idle_in_transaction_session_timeout = 0;");
        b.append("\nSET client_encoding = 'SQL_ASCII';");
        b.append("\nSET standard_conforming_strings = on;");
        b.append("\nSET check_function_bodies = false;");
        b.append("\nSET client_min_messages = warning;");
        b.append("\nSET row_security = off;");
        b.append("\n--");
        b.append("\nSET search_path = "+getSchemaName()+", pg_catalog;\n\n");
        return b.toString();
    }

    @Override
    public String create() {
        StringBuilder b = new StringBuilder();
        b.append("\nCREATE DATABASE ");
        b.append(getName());
        b.append(";\n");
        return b.toString();
    }

    @Override
    public String connect() {
        StringBuilder b = new StringBuilder();
        b.append("\n\\connect ");
        b.append(getName());
        b.append(";\n");
        return b.toString();
    }

    @Override
    public String createSchema() {
        StringBuilder b = new StringBuilder();
        b.append("\nCREATE SCHEMA ");
        b.append(getSchemaName());
        b.append(";\n");
        return b.toString();
    }
}
