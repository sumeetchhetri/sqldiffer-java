package com.sqldiffer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqldiffer.common.Column;
import com.sqldiffer.common.Constraint;
import com.sqldiffer.common.Db;
import com.sqldiffer.common.Index;
import com.sqldiffer.common.Sequence;
import com.sqldiffer.common.StoredProcedure;
import com.sqldiffer.common.StoredProcedureParam;
import com.sqldiffer.common.Table;
import com.sqldiffer.common.Trigger;
import com.sqldiffer.common.View;
import com.sqldiffer.oracle.OrclColumn;
import com.sqldiffer.oracle.OrclConstraint;
import com.sqldiffer.oracle.OrclDb;
import com.sqldiffer.oracle.OrclIndex;
import com.sqldiffer.oracle.OrclSequence;
import com.sqldiffer.oracle.OrclStoredProcedure;
import com.sqldiffer.oracle.OrclStoredProcedureParam;
import com.sqldiffer.oracle.OrclTable;
import com.sqldiffer.oracle.OrclTrigger;
import com.sqldiffer.oracle.OrclView;
import com.sqldiffer.postgres.PgColumn;
import com.sqldiffer.postgres.PgConstraint;
import com.sqldiffer.postgres.PgDb;
import com.sqldiffer.postgres.PgIndex;
import com.sqldiffer.postgres.PgSequence;
import com.sqldiffer.postgres.PgStoredProcedure;
import com.sqldiffer.postgres.PgStoredProcedureParam;
import com.sqldiffer.postgres.PgTable;
import com.sqldiffer.postgres.PgTrigger;
import com.sqldiffer.postgres.PgView;

@SuppressWarnings("unchecked")
public class SQLDiffer {

    private static final File REPO_DIR = new File("tmp/repo");
    private static final File REPO_TAB_DIR = new File("tmp/repo/tab");
    private static final File REPO_SPC_DIR = new File("tmp/repo/spc");
    private static final File REPO_TRG_DIR = new File("tmp/repo/trg");
    private static final File REPO_MSC_DIR = new File("tmp/repo/msc");

    @SuppressWarnings("unused")
    private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(repository.resolve(objectId));
            RevTree tree = walk.parseTree(commit.getTree().getId());
            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }
            walk.dispose();
            return treeParser;
        }
    }

    @SuppressWarnings("unused")
    private static void addDeletedFilesToIndexIfNeeded(Git git, String filePattern) throws GitAPIException {
        Set<String> deletedFiles = git.status().call().getMissing();
        if (deletedFiles.isEmpty()) {
            return;
        }
        if (!deletedFiles.isEmpty()) {
            RmCommand rmCommand = git.rm();
            deletedFiles.forEach(rmCommand::addFilepattern);
            rmCommand.call();
        }
    }

    public static void maino(String[] args) throws Exception {
        DbConvDetails det = new DbConvDetails();
        det.driver = "oracle";
        det.host = "risl.ohumhealthcare.com:1521";
        det.dbName = "orclpdb";
        det.user = "test1";
        det.pass = "abc";
        det.schemaName = "test1";
        det.fileName = "docit_schema-01.json";
        det.parallel = true;
        //generateSchema(det);

        det = new DbConvDetails();
        det.driver = "oracle";
        det.host = "risl.ohumhealthcare.com:1521";
        det.dbName = "orclpdb";
        det.user = "test2";
        det.pass = "abc";
        det.schemaName = "test2";
        det.fileName = "docit_schema-02.json";
        det.parallel = true;
        //generateSchema(det);

        compareDiff("source.json", "target.json", false, "oracle", "orclpdb", "IHMSDOCIT", false, "diff_sql", null, true);
        System.out.println(new Date());
    }

    public static void main(String[] args) throws Exception {
        DbConvDetails det = new DbConvDetails();
        det.driver = "postgres";
        det.host = "10.10.5.31:5432";
        det.dbName = "docit_uat_ccm_dev";
        det.user = "ccm_dev_admin";
        det.pass = "ccmdev#321";
        det.schemaName = "public";
        det.fileName = "docit_schema-01.json";
        det.parallel = true;
        generateSchema(det);

        det = new DbConvDetails();
        det.driver = "postgres";
        det.host = "10.10.5.31:5432";
        det.dbName = "docit_uat_ccm";
        det.user = "ccm_admin";
        det.pass = "ccm@0909";
        det.schemaName = "public";
        det.fileName = "docit_schema-02.json";
        det.parallel = true;
        generateSchema(det);

        compareDiff("docit_schema-01.json", "docit_schema-02.json", false, "postgres", "docit_qa_team", "public", false, "diff_sql", null, true);
        System.out.println(new Date());
    }

    public static void mainr(String[] args) throws Exception {
        try {
            if (args.length > 0) {
                if (args[0].trim().equalsIgnoreCase("schema")) {
                    if (args.length >= 6) {
                        String driver = args[1].trim();
                        String host = args[2].trim();
                        String dbName = args[3].trim();
                        String user = args[4].trim();
                        String pass = args[5].trim();
                        String schemaName = (args.length>6 && !args[6].trim().isEmpty()) ? args[6].trim() : "public";
                        String fileName = (args.length>7 && !args[7].trim().isEmpty()) ? args[7].trim() : "schema_" + System.nanoTime() + ".json";
                        if (!driver.toLowerCase().matches("postgres") && !driver.toLowerCase().matches("oracle")) {
                            throw new RuntimeException("Invalid database type specified, only one of (postgres, oracle) allowed");
                        }
                        if (host.isEmpty()) {
                            throw new RuntimeException("Invalid host specified");
                        }
                        if (dbName.isEmpty()) {
                            throw new RuntimeException("Invalid database name specified");
                        }
                        if (user.isEmpty()) {
                            throw new RuntimeException("Invalid user name specified");
                        }
                        DbConvDetails det = new DbConvDetails();
                        det.driver = driver;
                        det.host = host;
                        det.dbName = dbName;
                        det.user = user;
                        det.pass = pass;
                        det.schemaName = schemaName.toUpperCase();
                        det.fileName = fileName;
                        det.parallel = true;
                        generateSchema(det);
                    } else {
                        throw new RuntimeException("Provide parameters as follows - schema postgres localhost db user passwd public test.json");
                    }
                } else if (args[0].trim().equalsIgnoreCase("diff")) {
                    if (args.length >= 9) {
                        String f1 = args[1].trim();
                        String f2 = args[2].trim();
                        if (!args[3].trim().toLowerCase().matches("true|false|yes|no|1|0")) {
                            throw new RuntimeException("Provide a valid value for whether a single diff file is required or not valid values include one of (true|false|yes|no|1|0)");
                        }
                        boolean singleDiffFile = args[3].trim().toLowerCase().matches("true|yes|1");
                        String tgtdriver = args[4].trim();
                        String tgtdb = args[5].trim();
                        String tgtschm = !args[6].trim().isEmpty() ? args[6].trim() : "public";

                        boolean isReverse = true;
                        if (!args[7].trim().toLowerCase().matches("true|false|yes|no|1|0")) {
                            throw new RuntimeException("Provide a valid value for whether a reverse diff file is required or not valid values include one of (true|false|yes|no|1|0)");
                        } else {
                            isReverse = args[7].trim().toLowerCase().matches("true|yes|1");
                        }

                        String difffileName = (args.length > 8 && !args[8].trim().isEmpty()) ? args[8].trim() : (f1.substring(0, f1.lastIndexOf(".")) + f2.substring(0, f2.lastIndexOf(".")));
                        String type = (args.length > 9 && !args[9].trim().isEmpty()) ? args[9].trim() : null;
                        boolean isDiffNeeded = (args.length > 10 && !args[10].trim().isEmpty() && args[10].trim().toLowerCase().matches("1|yes|true|y")) ? true  : false;

                        if (f1.isEmpty()) {
                            throw new RuntimeException("Invalid source schema filename specified");
                        }
                        if (f2.isEmpty()) {
                            throw new RuntimeException("Invalid target schema filename specified");
                        }
                        if (!tgtdriver.toLowerCase().matches("postgres") && !tgtdriver.toLowerCase().matches("oracle")) {
                            throw new RuntimeException("Invalid target database type specified, only one of (postgres, oracle) allowed");
                        }
                        if (tgtdb.toLowerCase().isEmpty()) {
                            throw new RuntimeException("Invalid target database name specified");
                        }
                        if (isReverse) {
                            compareDiff(f1, f2, singleDiffFile, tgtdriver, tgtdb, tgtschm, true, difffileName, type, isDiffNeeded);
                            compareDiff(f1, f2, singleDiffFile, tgtdriver, tgtdb, tgtschm, false, difffileName, type, isDiffNeeded);
                        } else {
                            compareDiff(f1, f2, singleDiffFile, tgtdriver, tgtdb, tgtschm, false, difffileName, type, isDiffNeeded);
                        }
                    } else {
                        throw new RuntimeException("Provide parameters as follows - diff a.json b.json true postgres/oracle db public true true");
                    }
                }
            } else {
                throw new RuntimeException("Generate a schema using command - schema postgres/oracle localhost db user passwd public test.json\n"
                        + "Create a diff/reverse-diff using command - diff a.json b.json true postgres/oracle db public true");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void compareDiff(String f1, String f2, boolean singleDiffFile, String tgtdriver, String tgtdb, String tgtschm, boolean isReverse, String difffileName, String type, boolean isDiffNeeded) throws Exception {
        Db dbsrc = null, dbdst = null;
        if (StringUtils.isBlank(type)) {
            type = "t:0,s:0,v:0,r:0,f:0,m:0";
        }

        /*int tfm = -1;
        if (type.indexOf("t,") != -1 || type.indexOf("t:0") != -1 || type.indexOf("t:1,") != -1) {
            if (type.indexOf("t:1,") != -1) {
                tfm = 1;
            } else {
                tfm = 0;
            }
        }
        int sfm = -1;
        if (type.indexOf("s,") != -1 || type.indexOf("s:0") != -1 || type.indexOf("s:1,") != -1) {
            if (type.indexOf("s:1,") != -1) {
                sfm = 1;
            } else {
                sfm = 0;
            }
        }
        int vfm = -1;
        if (type.indexOf("v,") != -1 || type.indexOf("v:0") != -1 || type.indexOf("v:1,") != -1) {
            if (type.indexOf("v:1,") != -1) {
                vfm = 1;
            } else {
                vfm = 0;
            }
        }
        int rfm = -1;
        if (type.indexOf("r,") != -1 || type.indexOf("r:0") != -1 || type.indexOf("r:1,") != -1) {
            if (type.indexOf("r:1,") != -1) {
                rfm = 1;
            } else {
                rfm = 0;
            }
        }
        int ffm = -1;
        if (type.indexOf("f,") != -1 || type.indexOf("f:0") != -1 || type.indexOf("f:1,") != -1) {
            if (type.indexOf("f:1,") != -1) {
                ffm = 1;
            } else {
                ffm = 0;
            }
        }
        int mfm = -1;
        if (type.indexOf("m,") != -1 || type.indexOf("m:0") != -1 || type.indexOf("m:1,") != -1) {
            if (type.indexOf("m:1,") != -1) {
                mfm = 1;
            } else {
                mfm = 0;
            }
        }*/

        try {
            dbsrc = new ObjectMapper().readValue(new File(f1), PgDb.class);
        } catch (Exception e) {
            dbsrc = new ObjectMapper().readValue(new File(f1), OrclDb.class);
        }
        if ("postgres".equals(tgtdriver)) {
            dbdst = new PgDb();
            if (new File(f2).exists()) {
                dbdst = new ObjectMapper().readValue(new File(f2), PgDb.class);
            }
        } else if ("oracle".equals(tgtdriver)) {
            dbdst = new OrclDb();
            if (new File(f2).exists()) {
                dbdst = new ObjectMapper().readValue(new File(f2), OrclDb.class);
            }
        }

        StringBuilder dbb = new StringBuilder();
        StringBuilder seqb = new StringBuilder();
        StringBuilder tabb = new StringBuilder();
        StringBuilder spcb = new StringBuilder();
        StringBuilder trgb = new StringBuilder();
        StringBuilder mscb = new StringBuilder();

        if (!singleDiffFile) {
            dbb.append(dbdst.preface());
            seqb.append(dbdst.preface());
            tabb.append(dbdst.preface());
            spcb.append(dbdst.preface());
            trgb.append(dbdst.preface());
            mscb.append(dbdst.preface());
        }

        if(isDiffNeeded) {
            FileUtils.deleteDirectory(REPO_DIR);
            REPO_DIR.mkdirs();
            REPO_TAB_DIR.mkdirs();
            REPO_SPC_DIR.mkdirs();
            REPO_TRG_DIR.mkdirs();
            REPO_MSC_DIR.mkdirs();
        }

        Git git = null;
        RevCommit c1 = null, c2 = null;

        if(isDiffNeeded) {
            FileUtils.deleteDirectory(new File(REPO_DIR, ".git"));
            git = Git.init().setDirectory(REPO_DIR).call();
        }

        for (Table tsrc : dbsrc.getTables()) {
            tsrc.setDb(dbdst);
            for (Column csrc : tsrc.getColumns()) {
                csrc.setTable(tsrc);
            }
            for (Constraint csrc : tsrc.getConstraints()) {
                csrc.setTable(tsrc);
            }
            for (Trigger csrc : tsrc.getTriggers()) {
                if(isDiffNeeded) {
                    FileUtils.write(new File(REPO_TRG_DIR, csrc.getName().replaceAll("[\"']", "")+".sql"), csrc.generateNew(null));
                }
            }
            for (Index csrc : tsrc.getIndexes()) {
                csrc.setTableO(tsrc);
            }
            if(isDiffNeeded) {
                FileUtils.write(new File(REPO_TAB_DIR, tsrc.getName().replaceAll("[\"']", "")+".sql"), tsrc.generateNew(null));
            }
        }
        for (StoredProcedure psrc : dbsrc.getStoredProcs()) {
            psrc.setDb(dbdst);
            if(isDiffNeeded) {
                FileUtils.write(new File(REPO_SPC_DIR, psrc.getName().replaceAll("[\"']", "")+".sql"), psrc.generateNew(null));
            }
        }
        if(isDiffNeeded) {
            git.add().addFilepattern(".").call();
            c1 = git.commit().setMessage("COMMIT-SRC").call();
            git.push();
            FileUtils.deleteDirectory(new File(REPO_DIR, "msc"));
            FileUtils.deleteDirectory(new File(REPO_DIR, "spc"));
            FileUtils.deleteDirectory(new File(REPO_DIR, "tab"));
            FileUtils.deleteDirectory(new File(REPO_DIR, "trg"));
            REPO_TAB_DIR.mkdirs();
            REPO_SPC_DIR.mkdirs();
            REPO_TRG_DIR.mkdirs();
            REPO_MSC_DIR.mkdirs();
            //addDeletedFilesToIndexIfNeeded(git, ".");
        }


        for (Table tdst : dbdst.getTables()) {
            tdst.setDb(dbdst);
            for (Column csrc : tdst.getColumns()) {
                csrc.setTable(tdst);
            }
            for (Constraint csrc : tdst.getConstraints()) {
                csrc.setTable(tdst);
            }
            for (Trigger csrc : tdst.getTriggers()) {
                if(isDiffNeeded) {
                    FileUtils.write(new File(REPO_TRG_DIR, csrc.getName().replaceAll("[\"']", "")+".sql"), csrc.generateNew(null));
                }
            }
            for (Index csrc : tdst.getIndexes()) {
                csrc.setTableO(tdst);
            }
            if(isDiffNeeded) {
                FileUtils.write(new File(REPO_TAB_DIR, tdst.getName().replaceAll("[\"']", "")+".sql"), tdst.generateNew(null));
            }
        }
        for (StoredProcedure psrc : dbdst.getStoredProcs()) {
            psrc.setDb(dbdst);
            if(isDiffNeeded) {
                FileUtils.write(new File(REPO_SPC_DIR, psrc.getName().replaceAll("[\"']", "")+".sql"), psrc.generateNew(null));
            }
        }

        if(isDiffNeeded) {
            git.add().addFilepattern(".").call();
            c2 = git.commit().setMessage("COMMIT-DST").call();
            git.push();

            System.out.println("Printing diff between tree: " + c1.getName() + " and " + c2.getName());

            // finally get the list of changed files
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            /*List<DiffEntry> diffs= git.diff()
                    .setNewTree(prepareTreeParser(r, c1.getName()))
                    .setOldTree(prepareTreeParser(r, c2.getName()))
                    .setOutputStream(baos).call();
            for (DiffEntry entry : diffs) {
                System.out.println("Entry: " + entry);
            }*/
            String diff = new String(baos.toByteArray(), "UTF-8");
            String indexhtml = FileUtils.readFileToString(new File("templates/index.html"));
            indexhtml = indexhtml.replace("$$(DIFF_TEXT)$$", diff);

            FileUtils.copyDirectory(new File("templates"), new File("report"));
            FileUtils.write(new File("report/index.html"), indexhtml);
        }

        for (View vsrc : dbsrc.getViews()) {
            vsrc.setDb(dbdst);
        }
        for (View vsrc : dbdst.getViews()) {
            vsrc.setDb(dbdst);
        }

        for (Sequence vsrc : dbsrc.getSequences()) {
            vsrc.setDb(dbdst);
        }
        for (Sequence vsrc : dbdst.getSequences()) {
            vsrc.setDb(dbdst);
        }

        if (dbsrc != null && dbdst != null) {
            for (Table tsrc : dbsrc.getTables()) {
                Table tdst = null;
                for (Table tmpb : dbdst.getTables()) {
                    if (tsrc.getName().equals(tmpb.getName())) {
                        tdst = tmpb;
                        break;
                    }
                }
                if (tdst == null) {
                    if (!isReverse) {
                        tabb.append(tsrc.generateNew(null));
                    } else {
                        tabb.append(tsrc.generateDel(null));
                    }
                    for (Index csrc : tsrc.getIndexes()) {
                        if (!isReverse) {
                            mscb.append(csrc.generateNew(null));
                        } else {
                            mscb.append(csrc.generateDel(null));
                        }
                    }
                    for (Constraint csrc : tsrc.getConstraints()) {
                        if (!isReverse) {
                            mscb.append(csrc.generateNew(null));
                        } else {
                            mscb.append(csrc.generateDel(null));
                        }
                    }
                } else if (!tsrc.equals(tdst)) {
                    for (Column csrc : tsrc.getColumns()) {
                        Column cdst = null;
                        for (Column tmp : tdst.getColumns()) {
                            if (csrc.getName().equals(tmp.getName())) {
                                cdst = tmp;
                                break;
                            }
                        }
                        if (cdst == null) {
                            if (!isReverse) {
                                tabb.append(csrc.generateNew(null));
                            } else {
                                tabb.append(csrc.generateDel(null));
                            }
                        } else if (!csrc.equals(cdst)) {
                            if (!isReverse) {
                                tabb.append(csrc.generateUpd(cdst));
                            } else {
                                tabb.append(cdst.generateUpd(csrc));
                            }
                        }
                    }
                    for (Column cdst : tdst.getColumns()) {
                        Column csrc = null;
                        for (Column tmpb : tsrc.getColumns()) {
                            if (tmpb.getName().equals(cdst.getName())) {
                                csrc = tmpb;
                                break;
                            }
                        }
                        if(csrc==null) {
                            if (!isReverse) {
                                tabb.append(cdst.generateDel(null));
                            } else {
                                tabb.append(cdst.generateNew(null));
                            }
                        }
                    }

                    Map<String, Boolean> donem = new HashMap<String, Boolean>();
                    for (Constraint csrc : tsrc.getConstraints()) {
                        Constraint cdst = null;
                        for (Constraint tmp : tdst.getConstraints()) {
                            if (csrc.getName().equals(tmp.getName())) {
                                cdst = tmp;
                                break;
                            }
                        }
                        if (cdst == null) {
                            if (!isReverse) {
                                if (!donem.containsKey(csrc.getDefinition())) {
                                    donem.put(csrc.getDefinition(), true);
                                    mscb.append(csrc.generateNew(null));
                                }
                            } else {
                                mscb.append(csrc.generateDel(null));
                            }
                        } else if (!csrc.equals(cdst)) {
                            if (!isReverse) {
                                if (!donem.containsKey(csrc.getDefinition())) {
                                    donem.put(csrc.getDefinition(), true);
                                    mscb.append(cdst.generateDel(null));
                                    mscb.append(csrc.generateNew(null));
                                }
                            } else {
                                if (!donem.containsKey(cdst.getDefinition())) {
                                    donem.put(cdst.getDefinition(), true);
                                    mscb.append(csrc.generateDel(null));
                                    mscb.append(cdst.generateNew(null));
                                }
                            }
                        }
                    }
                    for (Constraint cdst : tdst.getConstraints()) {
                        Constraint csrc = null;
                        for (Constraint tmpb : tsrc.getConstraints()) {
                            if (tmpb.getName().equals(cdst.getName())) {
                                csrc = tmpb;
                                break;
                            }
                        }
                        if(csrc==null) {
                            if (!isReverse) {
                                mscb.append(cdst.generateDel(null));
                            } else {
                                mscb.append(cdst.generateNew(null));
                            }
                        }
                    }

                    for (Index csrc : tsrc.getIndexes()) {
                        Index cdst = null;
                        for (Index tmp : tdst.getIndexes()) {
                            if (csrc.getName().equals(tmp.getName())) {
                                cdst = tmp;
                                break;
                            }
                        }
                        if (cdst == null) {
                            if (!isReverse) {
                                mscb.append(csrc.generateNew(null));
                            } else {
                                mscb.append(csrc.generateDel(null));
                            }
                        } else if (!csrc.equals(cdst)) {
                            if (!isReverse) {
                                mscb.append(cdst.generateDel(null));
                                mscb.append(csrc.generateNew(null));
                            } else {
                                mscb.append(csrc.generateDel(null));
                                mscb.append(cdst.generateNew(null));
                            }
                        }
                    }
                    for (Index cdst : tdst.getIndexes()) {
                        Index csrc = null;
                        for (Index tmpb : tsrc.getIndexes()) {
                            if (tmpb.getName().equals(cdst.getName())) {
                                csrc = tmpb;
                                break;
                            }
                        }
                        if(csrc==null) {
                            if (!isReverse) {
                                mscb.append(cdst.generateDel(null));
                            } else {
                                mscb.append(cdst.generateNew(null));
                            }
                        }
                    }
                }
            }
            for (Table tdst : dbdst.getTables()) {
                Table tsrc = null;
                for (Table tmpb : dbsrc.getTables()) {
                    if (tmpb.getName().equals(tdst.getName())) {
                        tsrc = tmpb;
                        break;
                    }
                }
                if(tsrc==null) {
                    if (!isReverse) {
                        tabb.append(tdst.generateDel(null));
                    } else {
                        tabb.append(tdst.generateNew(null));
                    }
                }
            }

            // Handle StoredProcedures
            for (StoredProcedure psrc : dbsrc.getStoredProcs()) {
                StoredProcedure pdst = null;
                for (StoredProcedure tmpb : dbdst.getStoredProcs()) {
                    if (psrc.getName().equals(tmpb.getName())) {
                        pdst = tmpb;
                        if (("oracle".equals(tgtdriver) || (!"oracle".equals(tgtdriver) && psrc.getParams().size() == pdst.getParams().size())) && !pdst.isVisited() && !psrc.isVisited()) {
                            psrc.setVisited(true);
                            pdst.setVisited(true);
                            break;
                        }
                    } else {
                        pdst = null;
                    }
                }
                if (pdst == null) {
                    if (!isReverse) {
                        spcb.append(psrc.generateNew(null));
                    } else {
                        spcb.append(psrc.generateDel(null));
                    }
                } else if (!psrc.equals(pdst)) {
                    if (!isReverse) {
                        spcb.append(pdst.generateDel(null));
                        spcb.append(psrc.generateNew(null));
                    } else {
                        spcb.append(psrc.generateDel(null));
                        spcb.append(pdst.generateNew(null));
                    }
                }
            }
            for (StoredProcedure tdst : dbdst.getStoredProcs()) {
                StoredProcedure tsrc = null;
                for (StoredProcedure tmpb : dbsrc.getStoredProcs()) {
                    if (tmpb.getName().equals(tdst.getName())) {
                        tsrc = tmpb;
                        break;
                    }
                }
                if(tsrc==null) {
                    if (!isReverse) {
                        spcb.append(tdst.generateDel(null));
                    } else {
                        spcb.append(tdst.generateNew(null));
                    }
                }
            }

            // Handle Views
            List<View> delViews = new ArrayList<View>();
            List<View> addViews = new ArrayList<View>();
            for (View vsrc : dbsrc.getViews()) {
                View vdst = null;
                for (View tmpb : dbdst.getViews()) {
                    if (vsrc.getName().equals(tmpb.getName())) {
                        vdst = tmpb;
                        break;
                    }
                }
                if (vdst == null) {
                    if (!isReverse) {
                        // spcb.append(vsrc.generateNew(null));
                        addViews.add(vsrc);
                    } else {
                        // spcb.append(vsrc.generateDel(null));
                        delViews.add(vsrc);
                    }
                } else if (!vsrc.equals(vdst)) {
                    if (!isReverse) {
                        // spcb.append(vdst.generateDel(null));
                        // spcb.append(vsrc.generateNew(null));
                        delViews.add(vdst);
                        addViews.add(vsrc);
                    } else {
                        // spcb.append(vsrc.generateDel(null));
                        // spcb.append(vdst.generateNew(null));
                        delViews.add(vsrc);
                        addViews.add(vdst);
                    }
                }
            }
            for (View vwc : delViews) {
                for (View vw : delViews) {
                    Pattern vwpt = Pattern.compile("[\t ,]+" + vw.getName() + "[\t ,]+", Pattern.CASE_INSENSITIVE);
                    if (vwpt.matcher(vwc.getDefinition()).find()) {
                        vw.setWeight(vw.getWeight() + 1);
                    }
                }
            }
            Collections.sort(delViews, new Comparator<View>() {
                public int compare(View o1, View o2) {
                    int cmp = o1.getWeight().compareTo(o2.getWeight());
                    if (cmp == 0) {
                        if (o1.getRelatedViewNames().contains(o2.getName())) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                    return cmp;
                }
            });
            Collections.reverse(delViews);
            for (View vwc : addViews) {
                for (View vw : addViews) {
                    Pattern vwpt = Pattern.compile("[\t ,]+" + vw.getName() + "[\t ,]+", Pattern.CASE_INSENSITIVE);
                    if (vwpt.matcher(vwc.getDefinition()).find()) {
                        vw.setWeight(vw.getWeight() + 1);
                    }
                }
            }
            Collections.sort(addViews, new Comparator<View>() {
                public int compare(View o1, View o2) {
                    int cmp = o1.getWeight().compareTo(o2.getWeight());
                    if (cmp == 0) {
                        if (o1.getRelatedViewNames().contains(o2.getName())) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                    return cmp;
                }
            });
            Collections.reverse(addViews);
            for (View vwc : delViews) {
                tabb.append(vwc.generateDel(null));
            }
            for (View vwc : addViews) {
                tabb.append(vwc.generateNew(null));
            }

            // Handle Sequences
            for (Sequence ssrc : dbsrc.getSequences()) {
                Sequence sdst = null;
                for (Sequence tmpb : dbdst.getSequences()) {
                    if (ssrc.getName().equals(tmpb.getName())) {
                        sdst = tmpb;
                        if (ssrc.equals(sdst)) {
                            break;
                        }
                    }
                }
                if (sdst == null) {
                    if (!isReverse) {
                        seqb.append(ssrc.generateNew(null));
                    } else {
                        seqb.append(ssrc.generateDel(null));
                    }
                } else if (!ssrc.equals(sdst)) {
                    if (!isReverse) {
                        seqb.append(sdst.generateDel(null));
                        seqb.append(ssrc.generateNew(null));
                    } else {
                        seqb.append(ssrc.generateDel(null));
                        seqb.append(sdst.generateNew(null));
                    }
                }
            }
            for (Sequence tdst : dbdst.getSequences()) {
                Sequence tsrc = null;
                for (Sequence tmpb : dbsrc.getSequences()) {
                    if (tmpb.getName().equals(tdst.getName())) {
                        tsrc = tmpb;
                        break;
                    }
                }
                if(tsrc==null) {
                    if (!isReverse) {
                        seqb.append(tdst.generateDel(null));
                    } else {
                        seqb.append(tdst.generateNew(null));
                    }
                }
            }

            // Handle Triggers
            for (Table tsrc : dbsrc.getTables()) {
                Table tdst = null;
                for (Table tmpb : dbdst.getTables()) {
                    if (tsrc.getName().equals(tmpb.getName())) {
                        tdst = tmpb;
                        break;
                    }
                }
                if (tdst == null) {
                    for (Trigger csrc : tsrc.getTriggers()) {
                        if (!isReverse) {
                            trgb.append(csrc.generateNew(null));
                        } else {
                            trgb.append(csrc.generateDel(null));
                        }
                    }
                } else {
                    for (Trigger csrc : tsrc.getTriggers()) {
                        Trigger cdst = null;
                        for (Trigger tmp : tdst.getTriggers()) {
                            if (csrc.getName().equals(tmp.getName())) {
                                cdst = tmp;
                                if (csrc.equals(cdst)) {
                                    break;
                                }
                            }
                        }
                        if (cdst == null) {
                            if (!isReverse) {
                                trgb.append(csrc.generateNew(null));
                            } else {
                                trgb.append(csrc.generateDel(null));
                            }
                        } else if (!csrc.equals(cdst)) {
                            if (!isReverse) {
                                trgb.append(cdst.generateDel(null));
                                trgb.append(csrc.generateNew(null));
                            } else {
                                trgb.append(csrc.generateDel(null));
                                trgb.append(cdst.generateNew(null));
                            }
                        }
                    }
                    for (Trigger cdst : tdst.getTriggers()) {
                        Trigger csrc = null;
                        for (Trigger tmpb : tsrc.getTriggers()) {
                            if (tmpb.getName().equals(cdst.getName())) {
                                csrc = tmpb;
                                break;
                            }
                        }
                        if(csrc==null) {
                            if (!isReverse) {
                                trgb.append(cdst.generateDel(null));
                            } else {
                                trgb.append(cdst.generateNew(null));
                            }
                        }
                    }
                }
            }
        }

        if (!isReverse) {
            if (dbdst.getName() == null) {
                dbdst.setName(tgtdb);
                dbb.append(dbdst.create());
            }
            if (dbdst.getSchemaName() == null) {
                dbdst.setSchemaName(tgtschm);
                dbb.append(dbdst.connect());
                dbb.append(dbdst.createSchema());
            }
        }

        if (!singleDiffFile) {
            new File(difffileName + "_db" + (isReverse ? "_r" : "") + ".sql").delete();
            new File(difffileName + "_seq" + (isReverse ? "_r" : "") + ".sql").delete();
            new File(difffileName + "_tab" + (isReverse ? "_r" : "") + ".sql").delete();
            new File(difffileName + "_spc" + (isReverse ? "_r" : "") + ".sql").delete();
            new File(difffileName + "_trg" + (isReverse ? "_r" : "") + ".sql").delete();
            new File(difffileName + "_msc" + (isReverse ? "_r" : "") + ".sql").delete();

            FileUtils.write(new File(difffileName + "_db" + (isReverse ? "_r" : "") + ".sql"), dbb.toString());
            FileUtils.write(new File(difffileName + "_seq" + (isReverse ? "_r" : "") + ".sql"), seqb.toString());
            FileUtils.write(new File(difffileName + "_tab" + (isReverse ? "_r" : "") + ".sql"), tabb.toString());
            FileUtils.write(new File(difffileName + "_spc" + (isReverse ? "_r" : "") + ".sql"), spcb.toString());
            FileUtils.write(new File(difffileName + "_trg" + (isReverse ? "_r" : "") + ".sql"), trgb.toString());
            FileUtils.write(new File(difffileName + "_msc" + (isReverse ? "_r" : "") + ".sql"), mscb.toString());
        } else {
            dbb.append(seqb.toString());
            dbb.append(tabb.toString());
            dbb.append(spcb.toString());
            dbb.append(trgb.toString());
            dbb.append(mscb.toString());

            new File(difffileName + (isReverse ? "_r" : "") + ".sql").delete();
            FileUtils.write(new File(difffileName + (isReverse ? "_r" : "") + ".sql"), dbb.toString());
        }
    }

    private static class DbConvDetails {
        String driver;
        String host;
        String dbName;
        String user;
        String pass;
        String schemaName;
        String fileName;
        boolean parallel = false;
        Db db = null;
        StoredProcedure sp = null;
        StoredProcedureParam spp = null;
        Table tb = null;
        Column cl = null;
        Constraint cn = null;
        Trigger tr = null;
        Index ix = null;
        View vw = null;
        Sequence sq = null;
        @SuppressWarnings("unused")
        boolean isDiffNeeded = false;
        Map<String, StoredProcedure> procs = new ConcurrentHashMap<String, StoredProcedure>();
        Map<String, Boolean> procsp = new ConcurrentHashMap<String, Boolean>();
        Map<String, Table> tbls = new ConcurrentHashMap<String, Table>();
        Map<String, Boolean> tblsp = new ConcurrentHashMap<String, Boolean>();
        Map<String, List<Trigger>> trgtbls = new ConcurrentHashMap<String, List<Trigger>>();
        Map<String, List<Index>> indtbls = new ConcurrentHashMap<String, List<Index>>();
    }

    public static void generateSchema(final DbConvDetails det) throws Exception {
        if ("postgres".equalsIgnoreCase(det.driver)) {
            Class.forName("org.postgresql.Driver");
            det.db = new PgDb();
            det.sp = new PgStoredProcedure();
            det.spp = new PgStoredProcedureParam();
            det.tb = new PgTable();
            det.cl = new PgColumn();
            det.cn = new PgConstraint();
            det.tr = new PgTrigger();
            det.ix = new PgIndex();
            det.vw = new PgView();
            det.sq = new PgSequence();
            det.isDiffNeeded = true;

            det.db.setDriver(det.driver);
            det.db.setDuplicateProcNamesAllowed(true);
        } else if ("oracle".equalsIgnoreCase(det.driver)) {
            Class.forName("oracle.jdbc.OracleDriver");
            det.db = new OrclDb();
            det.sp = new OrclStoredProcedure();
            det.spp = new OrclStoredProcedureParam();
            det.tb = new OrclTable();
            det.cl = new OrclColumn();
            det.cn = new OrclConstraint();
            det.tr = new OrclTrigger();
            det.ix = new OrclIndex();
            det.vw = new OrclView();
            det.sq = new OrclSequence();

            det.db.setDriver(det.driver);
        }

        det.schemaName = det.schemaName.toUpperCase();
        det.dbName = det.dbName;
        det.db.setName(det.dbName);
        det.db.setSchemaName(det.schemaName);

        final String url = det.db.generateUrl(det.driver, det.host, det.dbName, det.schemaName);
        System.out.println("Creating schema files for " + url + "@" + det.user + "@" + det.pass);

        Runnable r1 = () -> objectifyStoredProcedures(det, url);
        Runnable r2 = () -> objectifyViews(det, url);
        Runnable r3 = () -> objectifyTriggers(det, url);
        Runnable r4 = () -> objectifyIndexes(det, url);
        Runnable r5 = () -> objectifyTables(det, url);
        Runnable r6 = () -> objectifySequences(det, url);

        if(det.parallel) {
            Thread t1 = new Thread(r1);
            Thread t2 = new Thread(r2);
            Thread t3 = new Thread(r3);
            Thread t4 = new Thread(r4);
            Thread t5 = new Thread(r5);
            Thread t6 = new Thread(r6);
            t1.start();
            t2.start();
            t3.start();
            t4.start();
            t5.start();
            t6.start();
            t1.join();
            t2.join();
            t3.join();
            t4.join();
            t5.join();
            t6.join();
        } else {
            r1.run();
            r2.run();
            r3.run();
            r4.run();
            r5.run();
            r6.run();
        }

        mergeTriggersIndexesWithTables(det);

        System.out.println("=================Objectifying complete=====================\n\n");
        FileUtils.write(new File(det.fileName), new ObjectMapper().writeValueAsString(det.db));
    }

    private static void objectifyStoredProcedures(DbConvDetails det, String url) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, det.user, det.pass);
            String spl = det.sp.query(null);
            Statement pps = conn.createStatement();
            ResultSet pprs = pps.executeQuery(spl);
            while (pprs.next()) {
                StoredProcedure tsp = (StoredProcedure) det.sp.fromResult(pprs, null);
                if(tsp.getDeclaration()!=null) {
                    tsp.setDeclaration(tsp.getDeclaration().replace("\""+det.schemaName+"\".", ""));
                    tsp.setDeclaration(tsp.getDeclaration().replace(det.schemaName+".", ""));
                }
                if(tsp.getDropDeclaration()!=null) {
                    tsp.setDropDeclaration(tsp.getDropDeclaration().replace("\""+det.schemaName+"\".", ""));
                    tsp.setDropDeclaration(tsp.getDropDeclaration().replace(det.schemaName+".", ""));
                }
                if(tsp.getDefinition()!=null) {
                    tsp.setDefinition(tsp.getDefinition().replace("\""+det.schemaName+"\".", ""));
                    tsp.setDefinition(tsp.getDefinition().replace(det.schemaName+".", ""));
                }
                tsp.setDb(det.db);
                det.db.getStoredProcs().add(tsp);
                if (!det.db.isDuplicateProcNamesAllowed()) {
                    det.procs.put(tsp.getName(), tsp);
                }
            }
            pprs.close();
            pps.close();
            System.out.println("Total store procedures = " + det.db.getStoredProcs().size());

            int spParamCount = 0;
            if (det.db.isDuplicateProcNamesAllowed()) {
                for (StoredProcedure tsp : det.db.getStoredProcs()) {
                    System.out.println("Objectifying procedure " + tsp.getName());

                    spl = det.spp.query(new Object[] {tsp.getName(), tsp.getNumParams()});
                    pps = conn.createStatement();
                    pprs = pps.executeQuery(spl);
                    while (pprs.next()) {
                        spParamCount++;
                        Object tmp = det.spp.fromResult(pprs, null);
                        if (tmp != null) {
                            if (tmp instanceof List) {
                                List<StoredProcedureParam> pars = (List<StoredProcedureParam>) tmp;
                                tsp.getParams().addAll(pars);
                            } else {
                                StoredProcedureParam pars = (StoredProcedureParam) tmp;
                                tsp.getParams().add(pars);
                            }
                        }
                    }
                    pprs.close();
                    pps.close();

                    spl = tsp.defineQuery(tsp.getName());
                    if (spl != null) {
                        pps = conn.createStatement();
                        pprs = pps.executeQuery(spl);
                        while (pprs.next()) {
                            String def = (String) tsp.definition(pprs);
                            tsp.setDefinition(def);
                        }
                        pprs.close();
                        pps.close();
                    }
                }
            } else {
                int start = 0, size = 1000;
                while (true) {
                    int count = 0;
                    spl = det.spp.query(new Object[] {start, start + size});
                    pps = conn.createStatement();
                    pprs = pps.executeQuery(spl);
                    while (pprs.next()) {
                        spParamCount++;
                        count++;
                        Object tmp = det.spp.fromResult(pprs, null);
                        if (tmp != null) {
                            if (tmp instanceof List) {
                                List<StoredProcedureParam> pars = (List<StoredProcedureParam>) tmp;
                                for (StoredProcedureParam p : pars) {
                                    StoredProcedure proc = det.procs.get(p.getProcName());
                                    proc.getParams().add(p);
                                    if (!det.procsp.containsKey(p.getProcName())) {
                                        System.out.println("Objectifying procedure " + p.getProcName());
                                        det.procsp.put(p.getProcName(), true);
                                    }
                                }
                            } else {
                                StoredProcedureParam p = (StoredProcedureParam) tmp;
                                StoredProcedure proc = det.procs.get(p.getProcName());
                                proc.getParams().add(p);
                                if (!det.procsp.containsKey(p.getProcName())) {
                                    System.out.println("Objectifying procedure " + p.getProcName());
                                    det.procsp.put(p.getProcName(), true);
                                }
                            }
                        }
                    }
                    pprs.close();
                    pps.close();
                    if (count == 0)
                        break;
                    start += count;
                }
            }
            System.out.println("Total store procedure parameters = " + spParamCount);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(conn!=null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    private static void objectifyViews(DbConvDetails det, String url) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, det.user, det.pass);
            String spl = det.vw.query(null);
            Statement pps = conn.createStatement();
            ResultSet pprs = pps.executeQuery(spl);
            while (pprs.next()) {
                View tsp = (View) det.vw.fromResult(pprs, null);
                if(tsp.getDefinition()!=null) {
                    tsp.setDefinition(tsp.getDefinition().replace("\""+det.schemaName+"\".", ""));
                    tsp.setDefinition(tsp.getDefinition().replace(det.schemaName+".", ""));
                }
                System.out.println("Objectifying view " + tsp.getName());
                det.db.getViews().add(tsp);
            }
            pprs.close();
            pps.close();
            System.out.println("Total views = " + det.db.getViews().size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(conn!=null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    private static void objectifyTriggers(DbConvDetails det, String url) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, det.user, det.pass);
            int trgCount = 0;
            String spl = det.tr.query(null);
            Statement pps = conn.createStatement();
            ResultSet pprs = pps.executeQuery(spl);
            while (pprs.next()) {
                trgCount++;
                Trigger tsp = (Trigger) det.tr.fromResult(pprs, new Object[] {det.tbls, det.db.getSchemaName()});
                tsp.setDefinition(tsp.getDefinition().replace("\""+det.schemaName+"\".", ""));
                tsp.setDefinition(tsp.getDefinition().replace(det.schemaName+".", ""));
                if(tsp.getFunctionDef()!=null) {
                    tsp.setFunctionDef(tsp.getFunctionDef().replace("\""+det.schemaName+"\".", ""));
                    tsp.setFunctionDef(tsp.getFunctionDef().replace(det.schemaName+".", ""));
                }
                tsp.setDb(det.db);
                System.out.println("Objectifying trigger " + tsp.getName());

                spl = tsp.defineQuery(null);
                if (spl != null) {
                    Statement pps1 = conn.createStatement();
                    ResultSet pprs1 = pps1.executeQuery(spl);
                    while (pprs1.next()) {
                        String def = (String) tsp.definition(pprs1);
                        tsp.setFunctionDef(def);
                    }
                    pprs1.close();
                    pps1.close();
                }

                if (!det.trgtbls.containsKey(tsp.getTable())) {
                    det.trgtbls.put(tsp.getTable(), new ArrayList<Trigger>());
                }
                det.trgtbls.get(tsp.getTable()).add(tsp);
            }
            pprs.close();
            pps.close();
            System.out.println("Total triggers = " + trgCount);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(conn!=null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    private static void objectifyIndexes(DbConvDetails det, String url) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, det.user, det.pass);
            int indxCount = 0;
            String spl = det.ix.query(null);
            Statement pps = conn.createStatement();
            ResultSet pprs = pps.executeQuery(spl);
            while (pprs.next()) {
                indxCount++;
                Index tsp = (Index) det.ix.fromResult(pprs, null);
                if(tsp.getDefinition()!=null) {
                    tsp.setDefinition(tsp.getDefinition().replace("\""+det.schemaName+"\".", ""));
                    tsp.setDefinition(tsp.getDefinition().replace(det.schemaName+".", ""));
                }
                System.out.println("Objectifying index " + tsp.getName());
                if (!det.indtbls.containsKey(tsp.getTable())) {
                    det.indtbls.put(tsp.getTable(), new ArrayList<Index>());
                }
                det.indtbls.get(tsp.getTable()).add(tsp);
            }
            pprs.close();
            pps.close();
            System.out.println("Total indexes = " + indxCount);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(conn!=null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    private static void mergeTriggersIndexesWithTables(DbConvDetails det) throws Exception {
        for (Table tb : det.db.getTables()) {
            if (det.trgtbls.containsKey(tb.getName())) {
                tb.setTriggers(det.trgtbls.get(tb.getName()));
                tb.setTriggers(det.tr.mergeDuplicates(tb.getTriggers()));
            }
            if (det.indtbls.containsKey(tb.getName())) {
                tb.setIndexes(det.indtbls.get(tb.getName()));
            }
        }
    }

    private static void objectifyTables(DbConvDetails det, String url) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, det.user, det.pass);
            String spl = det.tb.query(det.db.getSchemaName());
            Statement pps = conn.createStatement();
            ResultSet pprs = pps.executeQuery(spl);
            while (pprs.next()) {
                Table tsp = (Table) det.tb.fromResult(pprs, null);
                det.db.getTables().add(tsp);
                det.tbls.put(tsp.getName(), tsp);
            }
            pprs.close();
            pps.close();
            System.out.println("Total tables = " + det.db.getTables().size());

            int columnCount = 0;
            int start = 0, size = 1000;
            while (true) {
                int count = 0;
                spl = det.cl.query(new Object[] {start, start + size});
                pps = conn.createStatement();
                pprs = pps.executeQuery(spl);
                while (pprs.next()) {
                    count++;
                    Table tsp = (Table) det.cl.fromResult(pprs, new Object[] {det.tbls, det.db.getSchemaName()});
                    if(tsp!=null) {
                        columnCount++;
                    }
                    if (tsp != null && !det.tblsp.containsKey(tsp.getName())) {
                        System.out.println("Objectifying table " + tsp.getName());
                        det.tblsp.put(tsp.getName(), true);
                    }
                }
                pprs.close();
                pps.close();
                if (count == 0)
                    break;
                start += count;
            }

            System.out.println("Total table columns = " + columnCount);

            int constraintCount = 0;
            start = 0;
            size = 1000;
            while (true) {
                int count = 0;
                spl = det.cn.query(new Object[] {start, start + size});
                pps = conn.createStatement();
                pprs = pps.executeQuery(spl);
                while (pprs.next()) {
                    count++;
                    Table tsp = (Table) det.cn.fromResult(pprs, det.tbls);
                    if(tsp!=null) {
                        constraintCount++;
                    }
                    if (tsp != null && !det.tblsp.containsKey(tsp.getName())) {
                        System.out.println("Objectifying table " + tsp.getName());
                        det.tblsp.put(tsp.getName(), true);
                    }
                }
                pprs.close();
                pps.close();
                if (count == 0)
                    break;
                start += count;
            }
            System.out.println("Total table constraints = " + constraintCount);

            for (Table tdst : det.db.getTables()) {
                for (Column c : tdst.getColumns()) {
                    if(c.getDefVal()!=null) {
                        c.setDefVal(c.getDefVal().replace("\""+det.schemaName+"\".", ""));
                        c.setDefVal(c.getDefVal().replace(det.schemaName+".", ""));
                    }
                }
                for (Constraint c : tdst.getConstraints()) {
                    if(c.getDefinition()!=null) {
                        c.setDefinition(c.getDefinition().replace("\""+det.schemaName+"\".", ""));
                        c.setDefinition(c.getDefinition().replace(det.schemaName+".", ""));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(conn!=null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    private static void objectifySequences(DbConvDetails det, String url) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, det.user, det.pass);
            String spl = det.sq.query(null);
            Statement pps = conn.createStatement();
            ResultSet pprs = pps.executeQuery(spl);
            Map<String, Sequence> uniqs = new HashMap<String, Sequence>();
            while (pprs.next()) {
                Sequence tsp = (Sequence) det.sq.fromResult(pprs, uniqs);
                System.out.println("Objectifying sequence " + tsp.getName());
            }
            det.db.getSequences().addAll(uniqs.values());
            pprs.close();
            pps.close();
            System.out.println("Total table sequences = " + det.db.getSequences().size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(conn!=null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }
}
