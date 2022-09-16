package com.labvantage.sapphire.modules.eln.gwt.server;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.actions.eln.AddWorksheet;
import com.labvantage.sapphire.actions.eln.AddWorksheetContributor;
import com.labvantage.sapphire.actions.eln.AddWorksheetItem;
import com.labvantage.sapphire.actions.eln.AddWorksheetItemRef;
import com.labvantage.sapphire.actions.eln.AddWorksheetItemSDI;
import com.labvantage.sapphire.actions.eln.AddWorksheetSDI;
import com.labvantage.sapphire.actions.eln.AddWorksheetSection;
import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.actions.eln.DeleteWorksheetContributor;
import com.labvantage.sapphire.actions.eln.DeleteWorksheetItem;
import com.labvantage.sapphire.actions.eln.DeleteWorksheetItemSDI;
import com.labvantage.sapphire.actions.eln.DeleteWorksheetSDI;
import com.labvantage.sapphire.actions.eln.DeleteWorksheetSection;
import com.labvantage.sapphire.actions.eln.EditWorksheet;
import com.labvantage.sapphire.actions.eln.EditWorksheetItem;
import com.labvantage.sapphire.actions.eln.EditWorksheetItemParams;
import com.labvantage.sapphire.actions.eln.EditWorksheetSection;
import com.labvantage.sapphire.actions.eln.GenerateQCBatchWorksheet;
import com.labvantage.sapphire.actions.eln.GenerateReagentWorksheet;
import com.labvantage.sapphire.actions.eln.GenerateSampleWorksheet;
import com.labvantage.sapphire.actions.eln.GenerateTestMethodWorksheet;
import com.labvantage.sapphire.actions.eln.GenerateWorkorderWorksheet;
import com.labvantage.sapphire.actions.eln.SetWorksheetItemConfig;
import com.labvantage.sapphire.actions.eln.SetWorksheetItemContent;
import com.labvantage.sapphire.actions.eln.SetWorksheetItemStatus;
import com.labvantage.sapphire.actions.eln.SetWorksheetSectionStatus;
import com.labvantage.sapphire.actions.eln.SetWorksheetStatus;
import com.labvantage.sapphire.actions.sdi.BaseSDIAction;
import com.labvantage.sapphire.actions.sdi.DeleteSDINote;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.eln.Worksheet;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItem;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFactory;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.modules.search.SearchDocument;
import com.labvantage.sapphire.modules.search.SearchRequest;
import com.labvantage.sapphire.modules.search.SearchResults;
import com.labvantage.sapphire.modules.search.Searcher;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandResponse;
import com.labvantage.sapphire.pageelements.gwt.server.command.JSONableMap;
import com.labvantage.sapphire.pageelements.gwt.server.command.StandardCommandRequest;
import com.labvantage.sapphire.pageelements.gwt.shared.ELNConstants;
import com.labvantage.sapphire.pageelements.maint.EditorStyleUtil;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.DataAccessService;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import org.json.JSONException;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.ActionBlock;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIList;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ELNRequest extends StandardCommandRequest implements ELNConstants {

    public ELNRequest() {
    }

    protected boolean processCommand(String command, CommandRequest commandRequest,
        CommandResponse commandResponse) throws SapphireException {
        super.processCommand(command, commandRequest, commandResponse);
        DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());

        boolean var5;
        try {
            dbu.setConnection(this.sapphireConnection);
            if (command.equals("lwsm")) {
                this.loadWSM(commandRequest, commandResponse, dbu);
            } else if (command.equals("lwb")) {
                this.loadWorkbook(commandRequest, commandResponse, dbu);
            } else if (command.equals("lwssdis")) {
                this.loadWorksheetSDIs(commandRequest, commandResponse, dbu);
            } else if (command.equals("lcws")) {
                this.loadCopyWorksheet(commandRequest, commandResponse, dbu);
            } else if (command.equals("lwss")) {
                this.loadWorksheets(commandRequest, commandResponse, dbu);
            } else if (command.equals("lc")) {
                this.loadContributors(commandRequest, commandResponse, dbu);
            } else if (command.equals("ac")) {
                this.addContributor(commandRequest, commandResponse, dbu);
            } else if (command.equals("dc")) {
                this.deleteContributor(commandRequest, commandResponse, dbu);
            } else if (command.equals("lf")) {
                this.loadFields(commandRequest, commandResponse, dbu);
            } else if (command.equals("latt")) {
                this.loadAttachments(commandRequest, commandResponse, dbu);
            } else if (command.equals("la")) {
                this.loadAttributes(commandRequest, commandResponse, dbu);
            } else if (command.equals("subv")) {
                this.substituteValues(commandRequest, commandResponse, dbu);
            } else if (command.equals("aws")) {
                this.addWorksheet(commandRequest, commandResponse, dbu);
            } else if (command.equals("awsv")) {
                this.addWorksheetVersion(commandRequest, commandResponse, dbu);
            } else if (command.equals("apvwsv")) {
                this.approveWorksheetVersion(commandRequest, commandResponse, dbu);
            } else if (command.equals("dwssdi")) {
                this.deleteWorksheetSDI(commandRequest, commandResponse, dbu);
            } else if (command.equals("wsnc")) {
                this.worksheetNameCheck(commandRequest, commandResponse, dbu);
            } else if (command.equals("ews")) {
                this.editWorksheet(commandRequest, commandResponse, dbu);
            } else if (command.equals("swss")) {
                this.setWorksheetStatus(commandRequest, commandResponse, dbu);
            } else if (command.equals("ltd")) {
                this.loadTemplateDetails(commandRequest, commandResponse, dbu);
            } else if (command.equals("sat")) {
                this.saveAsTemplate(commandRequest, commandResponse, dbu);
            } else if (command.equals("awss")) {
                this.addSection(commandRequest, commandResponse, dbu);
            } else if (command.equals("ewss")) {
                this.editSection(commandRequest, commandResponse, dbu);
            } else if (command.equals("mwss")) {
                this.moveSection(commandRequest, commandResponse, dbu);
            } else if (command.equals("swsss")) {
                this.setSectionStatus(commandRequest, commandResponse, dbu);
            } else if (command.equals("dwss")) {
                this.deleteSection(commandRequest, commandResponse, dbu);
            } else if (command.equals("lwsi")) {
                this.loadWorksheetItem(commandRequest, commandResponse, dbu);
            } else if (command.equals("lwsid")) {
                this.loadWorksheetItemDiff(commandRequest, commandResponse, dbu);
            } else if (command.equals("lwiinc")) {
                this.loadWorksheetItemIncludes(commandRequest, commandResponse, dbu);
            } else if (command.equals("lwsia")) {
                this.loadItemAudit(commandRequest, commandResponse, dbu);
            } else if (command.equals("lwsisdis")) {
                this.loadItemSDIs(commandRequest, commandResponse, dbu);
            } else if (command.equals("lswi")) {
                this.loadSDIWorksheetItem(commandRequest, commandResponse, dbu);
            } else if (command.equals("awsi")) {
                this.addItem(commandRequest, commandResponse, dbu);
            } else if (command.equals("awssdi")) {
                this.addSDI(commandRequest, commandResponse, dbu);
            } else if (command.equals("dwsisdi")) {
                this.deleteItemSDI(commandRequest, commandResponse, dbu);
            } else if (command.equals("awsiref")) {
                this.addItemRef(commandRequest, commandResponse, dbu);
            } else if (command.equals("ewsi")) {
                this.editItem(commandRequest, commandResponse, dbu);
            } else if (command.equals("mwsi")) {
                this.moveItem(commandRequest, commandResponse, dbu);
            } else if (command.equals("swsis")) {
                this.setItemStatus(commandRequest, commandResponse, dbu);
            } else if (command.equals("ceo")) {
                this.canExecuteOperation(commandRequest, commandResponse, dbu);
            } else if (command.equals("sewsic")) {
                this.startEditContent(commandRequest, commandResponse, dbu);
            } else if (command.equals("eewsic")) {
                this.endEditContent(commandRequest, commandResponse, dbu);
            } else if (command.equals("rwsic")) {
                this.revertItemContent(commandRequest, commandResponse, dbu);
            } else if (command.equals("cewsic")) {
                this.cancelEditContent(commandRequest, commandResponse, dbu);
            } else if (command.equals("dwsi")) {
                this.deleteItem(commandRequest, commandResponse, dbu);
            } else if (command.equals("lid")) {
                this.loadItemDetails(commandRequest, commandResponse, dbu);
            } else if (command.equals("lao")) {
                this.loadAddOptions(commandRequest, commandResponse, dbu);
            } else if (command.equals("lr")) {
                this.loadReferences(commandRequest, commandResponse, dbu);
            } else if (command.equals("lal")) {
                this.loadActivityLog(commandRequest, commandResponse, dbu);
            } else if (command.equals("loga")) {
                this.logActivity(commandRequest, commandResponse, dbu);
            } else if (command.equals("lic")) {
                this.loadConfig(commandRequest, commandResponse, dbu);
            } else if (command.equals("sc")) {
                this.saveConfig(commandRequest, commandResponse, dbu);
            } else if (command.equals("lo")) {
                this.loadOptions(commandRequest, commandResponse, dbu);
            } else if (command.equals("so")) {
                this.saveOptions(commandRequest, commandResponse, dbu);
            } else if (command.equals("sup")) {
                this.saveUserPrivs(commandRequest, commandResponse, dbu);
            } else if (command.equals("asa")) {
                this.addSDIAttributes(commandRequest, commandResponse, dbu);
            } else if (command.equals("dsa")) {
                this.deleteSDIAttributes(commandRequest, commandResponse, dbu);
            } else if (command.equals("sp")) {
                this.saveParameters(commandRequest, commandResponse, dbu);
            } else if (command.equals("vssa")) {
                this.validateSDIAttributes(commandRequest, commandResponse, dbu);
            } else if (command.equals("ssa")) {
                this.saveSDIAttributes(commandRequest, commandResponse, dbu);
            } else if (command.equals("asatt")) {
                this.addSDIAttachment(commandRequest, commandResponse, dbu);
            } else if (command.equals("esatt")) {
                this.editSDIAttachment(commandRequest, commandResponse, dbu);
            } else if (command.equals("dsatt")) {
                this.deleteSDIAttachment(commandRequest, commandResponse, dbu);
            } else if (command.equals("rd")) {
                this.resetDetails(commandRequest, commandResponse, dbu);
            } else if (command.equals("eab")) {
                this.executeActionBlock(commandRequest, commandResponse, dbu);
            } else if (command.equals("chknote")) {
                this.checkFollowupNote(commandRequest, commandResponse);
            }

            var5 = true;
        } finally {
            dbu.releaseConnection();
        }

        return var5;
    }

    private void substituteValues(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil dbu) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");
        String worksheetitemid = commandRequest.getString("worksheetitemid");
        String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
        String value = commandRequest.getString("value");
        value = this.doSub(value, "worksheetid", worksheetid);
        value = this.doSub(value, "worksheetversionid", worksheetversionid);
        value = this.doSub(value, "worksheetitemid", worksheetitemid);
        value = this.doSub(value, "worksheetitemversionid", worksheetitemversionid);
        DataSet attributes;
        int i;
        String attributeid;
        String datatype;
        String columnid;
        if (value.contains("[metadata.") || value.contains("$S{metadata.")) {
            attributes = this.loadAttributes("LV_WorksheetItem", worksheetitemid,
                worksheetitemversionid);

            for (i = 0; i < attributes.size(); ++i) {
                attributeid = attributes.getValue(i, "attributeid");
                datatype = attributes.getValue(i, "datatype");
                columnid = !datatype.equals("D") && !datatype.equals("O") ? (datatype.equals("N")
                    ? "numericvalue" : "textvalue") : "datevalue";
                value = this.doSub(value, "metadata." + attributeid,
                    attributes.getValue(i, columnid, attributes.getValue(i, "default" + columnid)));
            }
        }

        if (value.contains("[metadata.") || value.contains("$S{metadata.")) {
            attributes = this.loadAttributes("LV_Worksheet", worksheetid, worksheetversionid);

            for (i = 0; i < attributes.size(); ++i) {
                attributeid = attributes.getValue(i, "attributeid");
                datatype = attributes.getValue(i, "datatype");
                columnid = !datatype.equals("D") && !datatype.equals("O") ? (datatype.equals("N")
                    ? "numericvalue" : "textvalue") : "datevalue";
                value = this.doSub(value, "metadata." + attributeid,
                    attributes.getValue(i, columnid, attributes.getValue(i, "default" + columnid)));
            }
        }

        commandResponse.set("value", value);
    }

    private String doSub(String value, String param, String newvalue) {
        value = StringUtil.replaceAll(value, "$S{" + param + "}", newvalue == null ? "" : newvalue);
        value = StringUtil.replaceAll(value, "[" + param + "]", newvalue == null ? "" : newvalue);
        return value;
    }

    private void loadWSM(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            this.getMyWorkbook(commandResponse, database);
            commandResponse.set("workbooksexist", "Y");

            try {
                commandResponse.set("searchenabled",
                    Indexer.getInstance(this.sapphireConnection.getDatabaseId()).isSearching() ? "Y"
                        : "N");
            } catch (Exception var18) {
                commandResponse.set("searchenabled", "N");
            }

            database.createPreparedResultSet(
                "SELECT count(*) count FROM worksheet WHERE templateflag = 'Y' AND templatetypeflag = ?",
                new Object[]{"W"});
            database.getNext();
            if (database.getInt("count") == 0) {
                PropertyList templateProps = new PropertyList();
                templateProps.setProperty("worksheetname", "Blank");
                templateProps.setProperty("templateflag", "Y");
                this.getActionProcessor()
                    .processActionClass(AddWorksheet.class.getName(), templateProps);
            }

            String queryid = commandRequest.getString("queryid");
            String generatetype = commandRequest.getString("generatetype");
            String worksheetid = commandRequest.getString("worksheetid");
            if (queryid.length() > 0) {
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid("LV_Worksheet");
                sdiRequest.setQueryid(queryid);
                String[] queryparams = new String[12];

                for (int i = 0; i < queryparams.length; ++i) {
                    queryparams[i] = commandRequest.getString("param" + (i + 1));
                }

                sdiRequest.setQueryParams(queryparams);
                sdiRequest.setRequestItem("primary");
                SDIData worksheetData = this.getSDIProcessor().getSDIData(sdiRequest);
                if (worksheetData != null && worksheetData.getDataset("primary") != null) {
                    DataSet worksheets = worksheetData.getDataset("primary");
                    commandResponse.set("worksheetid",
                        worksheets.getColumnValues("worksheetid", ";"));
                    commandResponse.set("worksheetversionid",
                        worksheets.getColumnValues("worksheetversionid", ";"));
                    commandResponse.set("worksheetinputlist", "Y");
                }
            } else if (generatetype.length() > 0) {
                String templateid = commandRequest.getString("generatetemplateid");
                String templateversionid = commandRequest.getString("generatetemplateversionid");
                if (templateid.length() > 0 && templateversionid.length() > 0) {
                    String workbookid = "";
                    String workbookdesc = "WSP-LESPreviews";
                    database.createPreparedResultSet(
                        "SELECT workbookid FROM workbook WHERE workbookdesc = ?",
                        new Object[]{workbookdesc});
                    if (database.getNext()) {
                        workbookid = database.getValue("workbookid");
                    }

                    if (workbookid.length() == 0) {
                        PropertyList addWorkbook = new PropertyList();
                        addWorkbook.setProperty("sdcid", "LV_Workbook");
                        addWorkbook.setProperty("keyid2", "1");
                        addWorkbook.setProperty("workbookdesc", workbookdesc);
                        addWorkbook.setProperty("workbookstatus", "InProgress");
                        this.getActionProcessor().processAction("AddSDI", "1", addWorkbook);
                        workbookid = addWorkbook.getProperty("newkeyid1");
                    }

                    StringBuffer wsid = new StringBuffer();
                    StringBuffer wsver = new StringBuffer();
                    database.createPreparedResultSet(
                        "SELECT worksheetid, worksheetversionid FROM worksheet WHERE worksheetdesc like 'WSP-%' and createdt < ?",
                        new Object[]{(new DateTimeUtil()).getTimestamp("now-1d")});

                    while (database.getNext()) {
                        wsid.append(";").append(database.getValue("worksheetid"));
                        wsver.append(";").append(database.getValue("worksheetversionid"));
                    }

                    PropertyList genWorksheet;
                    if (wsid.length() > 0) {
                        genWorksheet = new PropertyList();
                        genWorksheet.setProperty("sdcid", "LV_Worksheet");
                        genWorksheet.setProperty("keyid1", wsid.substring(1));
                        genWorksheet.setProperty("keyid2", wsver.substring(1));
                        this.getActionProcessor().processAction("DeleteSDI", "1", genWorksheet);
                    }

                    genWorksheet = new PropertyList();
                    genWorksheet.setProperty("workbookid", workbookid);
                    genWorksheet.setProperty("workbookversionid", "1");
                    genWorksheet.setProperty("templateid", templateid);
                    genWorksheet.setProperty("templateversionid", templateversionid);
                    genWorksheet.setProperty("preview", "Y");
                    String workorder;
                    if (!generatetype.equalsIgnoreCase("WorkItemWorksheet")) {
                        if (generatetype.equalsIgnoreCase("SampleWorksheet")) {
                            workorder = commandRequest.getString("generatepreviewkeyid1");
                            if (workorder.length() == 0) {
                                throw new SapphireException(
                                    "No samples specified when generating a sample worksheet.");
                            }

                            genWorksheet.setProperty("sampleid", workorder);
                            this.getActionProcessor()
                                .processActionClass(GenerateSampleWorksheet.class.getName(),
                                    genWorksheet);
                        } else if (generatetype.equalsIgnoreCase("qcbatch")) {
                            workorder = commandRequest.getString("generatepreviewkeyid1");
                            if (workorder.length() == 0) {
                                throw new SapphireException(
                                    "No qcbatch specified when generating a qcbatch worksheet.");
                            }

                            genWorksheet.setProperty("qcbatchid", workorder);
                            this.getActionProcessor()
                                .processActionClass(GenerateQCBatchWorksheet.class.getName(),
                                    genWorksheet);
                        } else if (generatetype.equalsIgnoreCase("consumable")) {
                            workorder = commandRequest.getString("generatepreviewkeyid1");
                            if (workorder.length() == 0) {
                                throw new SapphireException(
                                    "No Consumable Lot specified when generating a Consumable worksheet.");
                            }

                            genWorksheet.setProperty("reagentlotid", workorder);
                            this.getActionProcessor()
                                .processActionClass(GenerateReagentWorksheet.class.getName(),
                                    genWorksheet);
                        } else {
                            if (!generatetype.equalsIgnoreCase("workorder")) {
                                throw new SapphireException(
                                    "Unrecognized generator type: " + generatetype);
                            }

                            workorder = commandRequest.getString("generatepreviewkeyid1");
                            if (workorder.length() == 0) {
                                throw new SapphireException(
                                    "No WorkOrder specified when generating an Instrument Certification worksheet.");
                            }

                            genWorksheet.setProperty("workorderid", workorder);
                            this.getActionProcessor()
                                .processActionClass(GenerateWorkorderWorksheet.class.getName(),
                                    genWorksheet);
                        }
                    } else {
                        workorder = commandRequest.getString("generatepreviewkeyid1");
                        String workitemid = commandRequest.getString("generatekeyid1");
                        SafeSQL safeSQL = new SafeSQL();
                        database.createPreparedResultSet(
                            "SELECT sdiworkitemid FROM sdiworkitem WHERE sdcid = 'Sample' AND keyid1 IN ("
                                + safeSQL.addIn(workorder, ";")
                                + ") AND keyid2 = '(null)' AND keyid3 = '(null)' AND workitemid="
                                + safeSQL.addVar(workitemid) + " AND workiteminstance = 1",
                            safeSQL.getValues());
                        StringBuffer sdiworkitemid = new StringBuffer();

                        while (database.getNext()) {
                            sdiworkitemid.append(";").append(database.getValue("sdiworkitemid"));
                        }

                        if (sdiworkitemid.length() == 0) {
                            throw new SapphireException(
                                "No workitem specified when generating a workitem worksheet.");
                        }

                        genWorksheet.setProperty("sdiworkitemid", sdiworkitemid.substring(1));
                        this.getActionProcessor()
                            .processActionClass(GenerateTestMethodWorksheet.class.getName(),
                                genWorksheet);
                    }

                    commandResponse.set("worksheetid", genWorksheet.getProperty("worksheetid"));
                    commandResponse.set("worksheetversionid",
                        genWorksheet.getProperty("worksheetversionid"));
                }
            } else if (worksheetid.length() > 0) {
                commandResponse.set("worksheetid", worksheetid);
                commandResponse.set("worksheetversionid",
                    commandRequest.getString("worksheetversionid"));
                commandResponse.set("worksheetinputlist", "Y");
            }
        } catch (Exception var19) {
            commandResponse.setStatusFail(
                "Failed to load Worksheet Manager. Reason: " + var19.getMessage(), var19);
        }

    }

    private void loadWorkbook(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String[] myworkbookid = this.getMyWorkbook(commandResponse, database);
            String workbookid = commandRequest.getString("workbookid", myworkbookid[0]);
            String workbookversionid = commandRequest.getString("workbookversionid",
                myworkbookid.length > 1 ? myworkbookid[1] : "1");
            commandResponse.set("workbookid", workbookid);
            commandResponse.set("workbookversionid", workbookversionid);
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid("LV_Workbook");
            sdiRequest.setKeyid1List(workbookid);
            sdiRequest.setKeyid2List(workbookversionid);
            sdiRequest.setRequestItem("primary");
            SDIData workbookData = this.getSDIProcessor().getSDIData(sdiRequest);
            if (workbookData == null || workbookData.getDataset("primary") == null
                || workbookData.getDataset("primary").size() != 1) {
                throw new Exception("Workbook not found");
            }

            commandResponse.set("workbook", workbookData.getDataset("primary"));
            if (workbookData.getDataset("primary").getValue(0, "worksheettemplatesflag", "A")
                .equals("A")) {
                SDIRequest sdiRequestWorksheets = new SDIRequest();
                sdiRequestWorksheets.setSDCid("LV_Worksheet");
                sdiRequestWorksheets.setQueryFrom("worksheet");
                sdiRequestWorksheets.setQueryWhere(
                    "templatetypeflag = 'W' AND ( lesflag = 'N' OR lesflag IS NULL ) AND ( ( templateprivacyflag = 'G' AND versionstatus IN ( "
                        + this.getGlobalVersionStatusList()
                        + ") ) OR templateprivacyflag IS NULL OR ( templateprivacyflag = 'O' AND versionstatus IN ('P', 'A', 'C') AND authorid = '"
                        + this.sapphireConnection.getSysuserId() + "' ) )");
                sdiRequestWorksheets.setRequestItem("primary");
                sdiRequestWorksheets.setQueryOrderBy("worksheetname");
                SDIData worksheetData = this.getSDIProcessor().getSDIData(sdiRequestWorksheets);
                DataSet templates = worksheetData.getDataset("primary");

                for (int i = 0; i < templates.size(); ++i) {
                    if (templates.getValue(i, "worksheetversionid").length() == 0) {
                        templates.setValue(i, "worksheetversionid", "C");
                        DataSet names = this.getQueryProcessor().getPreparedSqlDataSet(
                            "SELECT worksheetversionid, worksheetname FROM worksheet WHERE worksheetid = ? AND ( versionstatus = 'P' OR versionstatus = 'C' ) ORDER BY versionstatus, cast ( worksheetversionid as integer ) DESC",
                            new Object[]{templates.getValue(i, "worksheetid")});
                        if (names.size() > 0) {
                            templates.setValue(i, "worksheetname",
                                names.getValue(0, "worksheetname"));
                        }
                    }
                }

                commandResponse.set("workbooktemplates", templates);
            } else {
                commandResponse.set("workbooktemplates", this.loadWorkbookTemplates(
                    workbookData.getDataset("primary").getValue(0, "workbookid"),
                    workbookData.getDataset("primary").getValue(0, "workbookversionid"), "W", ""));
            }
        } catch (Exception var14) {
            commandResponse.setStatusFail(
                "Failed to load workbook '" + commandRequest.getString("workbookid") + "'. Reason: "
                    + var14.getMessage(), var14);
        }

    }

    private String[] getMyWorkbook(CommandResponse commandResponse, DBUtil database)
        throws SapphireException {
        String[] myworkbookid = BaseELNAction.getUserWorkbook(
            this.sapphireConnection.getSysuserId(), database, this.getActionProcessor(),
            new ConfigurationProcessor(this.sapphireConnection.getConnectionId()), true);
        DataSet myworkbook = myworkbookid.length >= 1 ? this.getQueryProcessor()
            .getPreparedSqlDataSet(
                "SELECT workbookdesc FROM workbook WHERE workbookid = ? AND workbookversionid = ?",
                new Object[]{myworkbookid[0], myworkbookid.length > 1 ? myworkbookid[1] : "1"})
            : null;
        commandResponse.set("myworkbookid", myworkbookid[0]);
        commandResponse.set("myworkbookversionid", myworkbookid.length > 1 ? myworkbookid[1] : "1");
        commandResponse.set("myworkbookdesc",
            myworkbook != null && myworkbook.size() == 1 ? myworkbook.getValue(0, "workbookdesc")
                : "");
        return myworkbookid;
    }

    private DataSet loadWorkbookTemplates(String workbookid, String workbookversionid,
        String templatetype, String propertytreeid) {
        DataSet templates;
        if (templatetype.equals("I")) {
            templates = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT workbooktemplate.worksheetid, workbooktemplate.worksheetversionid, worksheet.worksheetname, worksheet.templateprivacyflag FROM worksheetitem, workbooktemplate LEFT OUTER JOIN worksheet ON workbooktemplate.worksheetid = worksheet.worksheetid AND workbooktemplate.worksheetversionid = worksheet.worksheetversionid WHERE workbooktemplate.workbookid = ? AND workbooktemplate.workbookversionid = ? AND workbooktemplate.worksheetid = worksheetitem.worksheetid AND typeflag = ? AND propertytreeid = ? AND versionstatus IN ('P', 'A', 'C') ORDER BY workbooktemplate.usersequence, worksheet.worksheetname",
                new Object[]{workbookid, workbookversionid, templatetype, propertytreeid});
        } else {
            templates = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT workbooktemplate.worksheetid, workbooktemplate.worksheetversionid, worksheet.worksheetname FROM workbooktemplate LEFT OUTER JOIN worksheet ON workbooktemplate.worksheetid = worksheet.worksheetid AND workbooktemplate.worksheetversionid = worksheet.worksheetversionid  AND versionstatus IN ('P', 'A', 'C') WHERE workbooktemplate.workbookid = ? AND workbooktemplate.workbookversionid = ? AND typeflag = ? ORDER BY workbooktemplate.usersequence, worksheet.worksheetname",
                new Object[]{workbookid, workbookversionid, templatetype});
        }

        for (int i = 0; i < templates.size(); ++i) {
            if (templates.getValue(i, "worksheetversionid").length() == 0) {
                templates.setValue(i, "worksheetversionid", "C");
                DataSet names = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT worksheetversionid, worksheetname FROM worksheet WHERE worksheetid = ? AND ( versionstatus = 'P' OR versionstatus = 'C' ) ORDER BY versionstatus, cast ( worksheetversionid as integer ) DESC",
                    new Object[]{templates.getValue(i, "worksheetid")});
                if (names.size() > 0) {
                    templates.setValue(i, "worksheetname", names.getValue(0, "worksheetname"));
                }
            }
        }

        return templates;
    }

    private void loadCopyWorksheet(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");

        try {
            SDIRequest wsRequest = new SDIRequest();
            wsRequest.setSDIList("LV_Worksheet", worksheetid, worksheetversionid, "");
            wsRequest.setRequestItem("primary");
            wsRequest.setExtendedDataTypes(true);
            SDIData worksheetData = this.getSDIProcessor().getSDIData(wsRequest);
            DataSet worksheet = worksheetData != null ? worksheetData.getDataset("primary") : null;
            if (worksheet == null || worksheet.size() != 1) {
                throw new SapphireException("Worksheet not found");
            }

            commandResponse.set("worksheet", worksheet);
            DataSet workbook = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT * FROM workbook WHERE workbookid = ? AND workbookversionid = ?",
                new Object[]{worksheet.getValue(0, "workbookid"),
                    worksheet.getValue(0, "workbookversionid")}, true);
            commandResponse.set("workbook", workbook);
            DataSet template = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT worksheetname FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ?",
                new Object[]{worksheet.getValue(0, "templateid"),
                    worksheet.getValue(0, "templateversionid")}, true);
            commandResponse.set("templatename",
                template.size() == 1 ? template.getValue(0, "templatename") : "");
            this.getMyWorkbook(commandResponse, database);
        } catch (Exception var11) {
            commandResponse.setStatusFail(
                "Failed to load copy worksheet " + BaseELNAction.getIdVersionText(worksheetid,
                    worksheetversionid) + ". Reason: " + var11.getMessage(), var11);
        }

    }

    private void loadWorksheets(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        String workbookid = commandRequest.getString("workbookid");
        String workbookversionid = commandRequest.getString("workbookversionid");
        String query = StringUtil.replaceAll(commandRequest.getString("query"), "'", "'");
        String like = query.toLowerCase();
        like = query.startsWith("+") ? query.substring(1) : query;
        like = !like.endsWith("*") && !like.endsWith("~") ? like
            : like.substring(0, like.length() - 1);
        int limit = Integer.parseInt(commandRequest.getString("limit"));

        try {
            String union = "SELECT worksheetid FROM worksheet WHERE Lower( worksheetname ) like '%"
                + SafeSQL.encodeForSQL(like, this.sapphireConnection.isOracle())
                + "%' UNION SELECT  worksheet.worksheetid FROM worksheet, worksheetsection WHERE worksheet.workbookid = '"
                + SafeSQL.encodeForSQL(workbookid, this.sapphireConnection.isOracle())
                + "' AND worksheet.workbookversionid = '" + SafeSQL.encodeForSQL(workbookversionid,
                this.sapphireConnection.isOracle())
                + "' AND worksheet.worksheetid = worksheetsection.worksheetid AND worksheet.worksheetversionid = worksheetsection.worksheetversionid AND Lower( worksheetsectiondesc ) like '%"
                + SafeSQL.encodeForSQL(like, this.sapphireConnection.isOracle())
                + "%' UNION SELECT  worksheet.worksheetid FROM worksheet, worksheetitem WHERE worksheet.workbookid = '"
                + SafeSQL.encodeForSQL(workbookid, this.sapphireConnection.isOracle())
                + "' AND worksheet.workbookversionid = '" + SafeSQL.encodeForSQL(workbookversionid,
                this.sapphireConnection.isOracle())
                + "' AND worksheet.worksheetid = worksheetitem.worksheetid AND worksheet.worksheetversionid = worksheetitem.worksheetversionid AND Lower( worksheetitemdesc ) like '%"
                + SafeSQL.encodeForSQL(like, this.sapphireConnection.isOracle()) + "%' ";
            if (commandRequest.getBoolean("loadinputworksheets")) {
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid("LV_Worksheet");
                sdiRequest.setKeyid1List(commandRequest.getString("inputworksheetid"));
                sdiRequest.setKeyid2List(commandRequest.getString("inputworksheetversionid"));
                sdiRequest.setRequestItem(
                    "primary[worksheetid, worksheetversionid, worksheetname, worksheetstatus, authorid, authordt, "
                        + Worksheet.getNoteStatusClause("LV_Worksheet", "worksheet", "worksheetid",
                        "worksheetversionid") + "]");
                sdiRequest.setQueryWhere(like.length() > 0 ? "worksheetid IN (" + union + ")" : "");
                SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
                if (sdiData != null && sdiData.getDataset("primary") != null) {
                    commandResponse.set("inputworksheets", sdiData.getDataset("primary"));
                } else {
                    commandResponse.set("inputworksheets", new DataSet());
                }
            }

            if (commandRequest.getBoolean("loadworkbookworksheets")) {
                String sql =
                    "SELECT worksheetid, worksheetversionid, worksheetname, worksheetstatus, authorid, authordt, "
                        + Worksheet.getNoteStatusClause("LV_Worksheet", "worksheet", "worksheetid",
                        "worksheetversionid")
                        + " FROM worksheet WHERE workbookid = ? AND workbookversionid = ? ";
                if (like.length() > 0) {
                    sql = sql + "AND worksheetid IN (" + union + ")";
                }

                DataSet worksheets = this.getQueryProcessor().getPreparedSqlDataSet(
                    database.isOracle() ?
                        "SELECT DISTINCT worksheetid, worksheetversionid, worksheetname, worksheetstatus, authorid, authordt, notestatus FROM ("
                            + sql + " ORDER BY authordt DESC) WHERE rownum < " + (limit + 1)
                        : "SELECT DISTINCT TOP " + limit
                            + " worksheetid, worksheetversionid, worksheetname, worksheetstatus, authorid, authordt, notestatus FROM ("
                            + sql + ") unionview ORDER BY authordt DESC",
                    new Object[]{workbookid, workbookversionid});
                int count = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT count(*) \"count\" FROM worksheet WHERE workbookid = ? AND workbookversionid = ? AND ( templateflag = 'N' OR templateflag IS NULL )",
                    new Object[]{workbookid, workbookversionid}).getInt(0, "count");
                commandResponse.set("workbookworksheets", worksheets);
                commandResponse.set("moreworkbookworksheets", count > limit ? "Y" : "N");
            }

            DataSet worksheets;
            if (commandRequest.getBoolean("loadrecentworksheets")) {
                worksheets = this.loadMRUList(
                    commandRequest.getString("__hostwebpageid") + "_recent_worksheets", like,
                    limit);
                commandResponse.set("recentworksheets", worksheets);
            }

            if (commandRequest.getBoolean("loadmatchingworksheets")) {
                worksheets = new DataSet();
                if (query.length() > 0) {
                    Searcher searcher = new Searcher(this.sapphireConnection);
                    SearchRequest searchRequest = new SearchRequest(query);
                    searchRequest.setSdcid("LV_Worksheet");
                    searchRequest.setShowTemplates(false);
                    SearchResults results = searcher.getSearchResults(searchRequest);
                    List<SearchDocument> searchDocuments = results.getSearchDocuments();

                    for (int i = 0; i < searchDocuments.size() && i < limit; ++i) {
                        SearchDocument searchDocument = (SearchDocument) searchDocuments.get(i);
                        int row;
                        if (!searchDocument.getType().equals("NOTE") && !searchDocument.getType()
                            .equals("ATTACHMENT")) {
                            row = worksheets.addRow();
                            worksheets.setString(row, "worksheetid",
                                searchDocument.getValue("worksheetid"));
                            worksheets.setString(row, "worksheetversionid",
                                searchDocument.getValue("worksheetversionid"));
                            worksheets.setString(row, "worksheetname",
                                searchDocument.getValue("worksheetname"));
                            worksheets.setString(row, "worksheetstatus",
                                searchDocument.getValue("worksheetstatus"));
                            worksheets.setString(row, "authorid",
                                searchDocument.getValue("authorid"));
                            worksheets.setString(row, "authordt",
                                searchDocument.getValue("authordt"));
                        } else {
                            row = worksheets.addRow();
                            if (searchDocument.getParentSdcid().equals("LV_Worksheet")) {
                                worksheets.setString(row, "worksheetid",
                                    searchDocument.getParentKeyid1());
                                worksheets.setString(row, "worksheetversionid",
                                    searchDocument.getParentKeyid2());
                                worksheets.setString(row, "worksheetname",
                                    searchDocument.getValue("worksheetname"));
                                worksheets.setString(row, "worksheetstatus",
                                    searchDocument.getValue("worksheetstatus"));
                                worksheets.setString(row, "authorid",
                                    searchDocument.getValue("authorid"));
                                worksheets.setString(row, "authordt",
                                    searchDocument.getValue("authordt"));
                            } else if (searchDocument.getSdcid().equals("LV_Worksheet")) {
                                worksheets.setString(row, "worksheetid",
                                    searchDocument.getKeyid1());
                                worksheets.setString(row, "worksheetversionid",
                                    searchDocument.getKeyid2());
                                worksheets.setString(row, "worksheetname",
                                    searchDocument.getValue("worksheetname"));
                                worksheets.setString(row, "worksheetstatus",
                                    searchDocument.getValue("worksheetstatus"));
                                worksheets.setString(row, "authorid",
                                    searchDocument.getValue("authorid"));
                                worksheets.setString(row, "authordt",
                                    searchDocument.getValue("authordt"));
                            }
                        }
                    }
                }

                commandResponse.set("matchingworksheets", worksheets);
            }

            PropertyListCollection worksheetqueries = commandRequest.getCollection(
                "worksheetqueries");
            if (worksheetqueries != null) {
                for (int i = 0; i < worksheetqueries.size(); ++i) {
                    PropertyList worksheetquery = worksheetqueries.getPropertyList(i);
                    if (worksheetquery.getProperty("show").equals("Y")) {
                        SDIRequest sdiRequest = new SDIRequest();
                        sdiRequest.setSDCid("LV_Worksheet");
                        sdiRequest.setQueryid(worksheetquery.getProperty("queryid"));
                        PropertyListCollection queryparams = worksheetquery.getCollection(
                            "queryparams");
                        if (queryparams != null && queryparams.size() > 0) {
                            String[] params = new String[queryparams.size()];

                            for (int j = 0; j < queryparams.size(); ++j) {
                                params[i] = StringUtil.replaceAll(
                                    queryparams.getPropertyList(j).getProperty("paramvalue"),
                                    "[currentuser]", this.sapphireConnection.getSysuserId());
                            }

                            sdiRequest.setQueryParams(params);
                        }

                        sdiRequest.setRequestItem(
                            "primary[worksheetid, worksheetversionid, worksheetname, worksheetstatus, authorid, authordt, "
                                + Worksheet.getNoteStatusClause("LV_Worksheet", "worksheet",
                                "worksheetid", "worksheetversionid") + "]");
                        SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
                        if (sdiData != null && sdiData.getDataset("primary") != null) {
                            commandResponse.set(worksheetquery.getProperty("queryid"),
                                sdiData.getDataset("primary"));
                        } else {
                            commandResponse.set(worksheetquery.getProperty("queryid"),
                                new DataSet());
                        }
                    }
                }
            }
        } catch (Exception var18) {
            commandResponse.setStatusFail(
                "Failed to load worksheets with query '" + query + "'. Reason: "
                    + var18.getMessage(), var18);
        }

    }

    private void loadWorksheetSDIs(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");

        try {
            PropertyListCollection worksheetsdcs = commandRequest.getCollection("worksheetsdcs");
            if (worksheetsdcs == null || worksheetsdcs.size() == 0) {
                worksheetsdcs = new PropertyListCollection();
            }

            DataSet sdcs = commandRequest.getBoolean("groupbyitem") ? this.getQueryProcessor()
                .getPreparedSqlDataSet(
                    "SELECT DISTINCT wsi.worksheetitemid, wsi.worksheetitemversionid, worksheetsdi.sdcid FROM worksheetsdi LEFT OUTER JOIN   ( SELECT worksheetitem.worksheetitemid, worksheetitem.worksheetitemversionid, worksheetitem.worksheetid, worksheetitem.worksheetversionid, sdcid, keyid1     FROM worksheetitem, worksheetitemsdi     WHERE worksheetitem.worksheetitemid = worksheetitemsdi.worksheetitemid AND worksheetitem.worksheetitemversionid = worksheetitemsdi.worksheetitemversionid AND worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ? ) wsi     ON worksheetsdi.worksheetid = wsi.worksheetid AND worksheetsdi.worksheetversionid = wsi.worksheetversionid AND        worksheetsdi.sdcid = wsi.sdcid AND worksheetsdi.keyid1 = wsi.keyid1 WHERE worksheetsdi.worksheetid = ? AND worksheetsdi.worksheetversionid = ? ORDER BY 1, 2",
                    new Object[]{worksheetid, worksheetversionid, worksheetid, worksheetversionid})
                : this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT DISTINCT sdcid FROM worksheetsdi WHERE worksheetid = ? AND worksheetversionid = ? ORDER BY 1",
                    new Object[]{worksheetid, worksheetversionid});

            int i;
            for (i = 0; i < sdcs.size(); ++i) {
                String sdcid = sdcs.getValue(i, "sdcid");
                PropertyList sdcProps = this.getSDCProcessor().getProperties(sdcid);
                PropertyList sdc = worksheetsdcs.find("sdcid", sdcid);
                if (sdc == null) {
                    PropertyList worksheetsdc = new PropertyList();
                    int keycols = Integer.parseInt(sdcProps.getProperty("keycolumns"));

                    try {
                        worksheetsdc.setJSONString(
                            "{sdcid: '" + sdcid + "', title: '" + StringUtil.initCaps(
                                sdcProps.getProperty("plural")) + "', columns: [   {columnid: '"
                                + sdcProps.getProperty("keycolid1") + "', title: 'Id'}," + (
                                keycols >= 2 ? "   {columnid: '" + sdcProps.getProperty("keycolid2")
                                    + "', title: 'Ver'}," : "") + (keycols >= 3 ? "   {columnid: '"
                                + sdcProps.getProperty("keycolid3") + "', title: 'Var'}," : "")
                                + "   {columnid: '" + sdcProps.getProperty("desccol")
                                + "', title: 'Description'}]}");
                    } catch (JSONException var21) {
                        throw new SapphireException(
                            "Failed to create default column list for worksheetsdis", var21);
                    }

                    worksheetsdcs.add(worksheetsdc);
                } else {
                    sdc.setProperty("title", StringUtil.initCaps(sdcProps.getProperty("plural")));
                }
            }

            String worksheetitemid;
            if (!commandRequest.getBoolean("groupbyitem")) {
                for (i = 0; i < worksheetsdcs.size(); ++i) {
                    PropertyList worksheetsdc = worksheetsdcs.getPropertyList(i);
                    String sdcid = worksheetsdc.getProperty("sdcid");
                    DataSet worksheetsdis = this.loadWorksheetSDIs(worksheetid, worksheetversionid,
                        sdcid, worksheetsdc);
                    commandResponse.set(sdcid + "_worksheetsdi", worksheetsdis);
                }
            } else {
                commandResponse.set("itemgroups", sdcs);
                for (i = 0; i < sdcs.size(); ++i) {
                    String sdcid = sdcs.getValue(i, "sdcid");
                    worksheetitemid = sdcs.getValue(i, "worksheetitemid");
                    String worksheetitemversionid = sdcs.getValue(i, "worksheetitemversionid");
                    PropertyList worksheetsdc = worksheetsdcs.find("sdcid", sdcid);
                    PropertyListCollection columns = worksheetsdc.getCollection("columns");
                    PropertyList sdcProps = this.getSDCProcessor().getProperties(sdcid);
                    String tableid = sdcProps.getProperty("tableid");
                    int keycols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
                    StringBuffer cols = new StringBuffer();
                    boolean isDTypeSDC = "D".equalsIgnoreCase(sdcProps.getProperty("sdctype"));
                    if (columns != null) {
                        for (int j = 0; j < columns.size(); ++j) {
                            cols.append(",").append(tableid).append(".")
                                .append(columns.getPropertyList(j).getProperty("columnid"));
                        }
                    }

                    DataSet worksheetsdis;
                    String sql;
                    if (worksheetitemid.length() > 0) {
                        sql =
                            "SELECT worksheetitemsdi.worksheetitemid, worksheetitemsdi.worksheetitemversionid, "
                                + tableid + "." + sdcProps.getProperty("keycolid1") + ", " + tableid
                                + "." + (isDTypeSDC ? "keyid1"
                                : sdcProps.getProperty("keycolid1") + " keyid1 ") + (keycols >= 2 ?
                                ", " + tableid + "." + sdcProps.getProperty("keycolid2") + (
                                    isDTypeSDC ? "" : " keyid2") : " ") + (keycols >= 3 ? ", "
                                + tableid + "." + sdcProps.getProperty("keycolid3") + (isDTypeSDC
                                ? "" : " keyid3") : " ") + (cols.length() > 0 ? ", "
                                + cols.substring(1) + " " : "") + "FROM worksheetitemsdi, "
                                + tableid
                                + " WHERE worksheetitemsdi.worksheetitemid = ? AND worksheetitemsdi.worksheetitemversionid = ? AND worksheetitemsdi.sdcid = ? AND worksheetitemsdi.keyid1 = "
                                + tableid + "." + sdcProps.getProperty("keycolid1") + (keycols >= 2
                                ? " AND worksheetitemsdi.keyid2 = " + tableid + "."
                                + sdcProps.getProperty("keycolid2") : "") + (keycols >= 3 ?
                                " AND worksheetitemsdi.keyid3 = " + tableid + "."
                                    + sdcProps.getProperty("keycolid3") : "") + " ORDER BY 1" + (
                                keycols >= 2 ? ", 2" : "") + (keycols >= 3 ? ", 3" : "");
                        worksheetsdis = this.getQueryProcessor().getPreparedSqlDataSet(sql,
                            new Object[]{worksheetitemid, worksheetitemversionid, sdcid});
                        commandResponse.set(worksheetitemid + "_" + sdcid, worksheetsdis);
                    } else {
                        sql = "SELECT null, null, " + tableid + "." + sdcProps.getProperty(
                            "keycolid1") + ", " + tableid + "." + sdcProps.getProperty("keycolid1")
                            + " keyid1 " + (keycols >= 2 ? ", " + tableid + "."
                            + sdcProps.getProperty("keycolid2") + " keyid2 " : " ") + (keycols >= 3
                            ? ", " + tableid + "." + sdcProps.getProperty("keycolid3") + " keyid3 "
                            : " ") + (cols.length() > 0 ? ", " + cols.substring(1) + " " : "")
                            + "FROM worksheetsdi, " + tableid
                            + " WHERE worksheetsdi.worksheetid = ? AND worksheetsdi.worksheetversionid = ? AND worksheetsdi.sdcid = ? AND worksheetsdi.keyid1 = "
                            + tableid + "." + sdcProps.getProperty("keycolid1") + (keycols >= 2 ?
                            " AND worksheetsdi.keyid2 = " + tableid + "." + sdcProps.getProperty(
                                "keycolid2") : "") + (keycols >= 3 ? " AND worksheetsdi.keyid3 = "
                            + tableid + "." + sdcProps.getProperty("keycolid3") : "") + " AND " + (
                            keycols == 1 ? "worksheetsdi.keyid1" : (keycols == 2
                                ? "{fn concat( worksheetsdi.keyid1, worksheetsdi.keyid2 )}"
                                : "{fn concat( {fn concat( worksheetsdi.keyid1, worksheetsdi.keyid2 )}, worksheetsdi.keyid3 )}"))
                            + " NOT IN ( SELECT " + (keycols == 1 ? "worksheetitemsdi.keyid1"
                            : (keycols == 2
                                ? "{fn concat( worksheetitemsdi.keyid1, worksheetitemsdi.keyid2 )}"
                                : "{fn concat( {fn concat( worksheetitemsdi.keyid1, worksheetitemsdi.keyid2 )}, worksheetitemsdi.keyid3 )}"))
                            + " FROM worksheetitem, worksheetitemsdi  WHERE worksheetitem.worksheetitemid = worksheetitemsdi.worksheetitemid AND worksheetitem.worksheetitemversionid = worksheetitemsdi.worksheetitemversionid    AND worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ? ) ORDER BY 1"
                            + (keycols >= 2 ? ", 2" : "") + (keycols >= 3 ? ", 3" : "");
                        worksheetsdis = this.getQueryProcessor().getPreparedSqlDataSet(sql,
                            new Object[]{worksheetid, worksheetversionid, sdcid, worksheetid,
                                worksheetversionid});
                        commandResponse.set("null_" + sdcid, worksheetsdis);
                    }
                }
            }

            commandResponse.set("worksheetsdcs", worksheetsdcs);
        } catch (Exception var22) {
            commandResponse.setStatusFail(
                "Failed to load SDIs for worksheet " + BaseELNAction.getIdVersionText(worksheetid,
                    worksheetversionid) + ". Reason: " + var22.getMessage(), var22);
        }

    }

    private DataSet loadWorksheetSDIs(String worksheetid, String worksheetversionid, String sdcid,
        PropertyList sdc) {
        return this.loadWorksheetSDIs(worksheetid, worksheetversionid, sdcid, sdc,
            (PropertyList) null, (String) null, (String) null, (String) null);
    }

    private DataSet loadWorksheetSDIs(String worksheetid, String worksheetversionid, String sdcid,
        PropertyList sdc, PropertyList filter, String keyid1, String keyid2, String keyid3) {
        PropertyListCollection columns = sdc != null ? sdc.getCollection("columns") : null;
        PropertyList sdcProps = this.getSDCProcessor().getProperties(sdcid);
        String tableid = sdcProps.getProperty("tableid");
        int keycols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
        StringBuffer cols = new StringBuffer();
        if (columns != null) {
            for (int j = 0; j < columns.size(); ++j) {
                cols.append(",").append(tableid).append(".")
                    .append(columns.getPropertyList(j).getProperty("columnid"));
            }
        }

        String filterWhere = this.getFilterWhere(tableid, filter);
        String keyidWhere =
            keyid1 != null && keyid1.length() != 0 ? tableid + "." + sdcProps.getProperty(
                "keycolid1") + "='" + keyid1 + "'" : "";
        keyidWhere =
            keyidWhere + (keycols >= 2 && keyid2 != null && keyid2.length() > 0 ? " AND " + tableid
                + "." + sdcProps.getProperty("keycolid2") + "='" + keyid2 + "'" : "");
        keyidWhere =
            keyidWhere + (keycols >= 3 && keyid3 != null && keyid3.length() > 0 ? " AND " + tableid
                + "." + sdcProps.getProperty("keycolid3") + "='" + keyid3 + "'" : "");
        String sql =
            "SELECT " + tableid + "." + sdcProps.getProperty("keycolid1") + ", " + tableid + "."
                + sdcProps.getProperty("keycolid1") + " keyid1 " + (keycols >= 2 ? ", " + tableid
                + "." + sdcProps.getProperty("keycolid2") + " keyid2 " : " ") + (keycols >= 3 ? ", "
                + tableid + "." + sdcProps.getProperty("keycolid3") + " keyid3 " : " ") + (
                cols.length() > 0 ? ", " + cols.substring(1) + " " : "") + "FROM worksheetsdi, "
                + tableid
                + " WHERE worksheetsdi.worksheetid = ? AND worksheetsdi.worksheetversionid = ? AND worksheetsdi.sdcid = ? AND worksheetsdi.keyid1 = "
                + tableid + "." + sdcProps.getProperty("keycolid1") + (keycols >= 2 ?
                " AND worksheetsdi.keyid2 = " + tableid + "." + sdcProps.getProperty("keycolid2")
                : "") + (keycols >= 3 ? " AND worksheetsdi.keyid3 = " + tableid + "."
                + sdcProps.getProperty("keycolid3") : "") + " " + (filterWhere.length() > 0 ?
                " AND " + filterWhere : "") + (keyidWhere.length() > 0 ? " AND " + keyidWhere : "")
                + " ORDER BY 1" + (keycols >= 2 ? ", 2" : "") + (keycols >= 3 ? ", 3" : "");
        return this.getQueryProcessor()
            .getPreparedSqlDataSet(sql, new Object[]{worksheetid, worksheetversionid, sdcid});
    }

    private DataSet loadItemSDIs(String worksheetitemid, String worksheetitemversionid,
        String sdcid, PropertyList sdc, PropertyList filter, String keyid1, String keyid2,
        String keyid3) {
        PropertyListCollection columns = sdc != null ? sdc.getCollection("columns") : null;
        PropertyList sdcProps = this.getSDCProcessor().getProperties(sdcid);
        String tableid = sdcProps.getProperty("tableid");
        int keycols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
        StringBuffer cols = new StringBuffer();
        if (columns != null) {
            for (int j = 0; j < columns.size(); ++j) {
                cols.append(",").append(tableid).append(".")
                    .append(columns.getPropertyList(j).getProperty("columnid"));
            }
        }

        String filterWhere = this.getFilterWhere(tableid, filter);
        String keyidWhere =
            keyid1 != null && keyid1.length() != 0 ? tableid + "." + sdcProps.getProperty(
                "keycolid1") + "='" + keyid1 + "'" : "";
        keyidWhere =
            keyidWhere + (keycols >= 2 && keyid2 != null && keyid2.length() > 0 ? " AND " + tableid
                + "." + sdcProps.getProperty("keycolid2") + "='" + keyid2 + "'" : "");
        keyidWhere =
            keyidWhere + (keycols >= 3 && keyid3 != null && keyid3.length() > 0 ? " AND " + tableid
                + "." + sdcProps.getProperty("keycolid3") + "='" + keyid3 + "'" : "");
        String sql = "SELECT " + (cols.length() > 0 ? cols.substring(1) + ", " : "") + tableid + "."
            + sdcProps.getProperty("keycolid1") + " keyid1 " + (keycols >= 2 ? ", " + tableid + "."
            + sdcProps.getProperty("keycolid2") + " keyid2 " : " ") + (keycols >= 3 ? ", " + tableid
            + "." + sdcProps.getProperty("keycolid3") + " keyid3 " : " ")
            + "FROM worksheetitemsdi, " + tableid
            + " WHERE worksheetitemsdi.worksheetitemid = ? AND worksheetitemsdi.worksheetitemversionid = ? AND worksheetitemsdi.sdcid = ? AND worksheetitemsdi.keyid1 = "
            + tableid + "." + sdcProps.getProperty("keycolid1") + (keycols >= 2 ?
            " AND worksheetitemsdi.keyid2 = " + tableid + "." + sdcProps.getProperty("keycolid2")
            : "") + (keycols >= 3 ? " AND worksheetitemsdi.keyid3 = " + tableid + "."
            + sdcProps.getProperty("keycolid3") : "") + " " + (filterWhere.length() > 0 ? " AND "
            + filterWhere : "") + (keyidWhere.length() > 0 ? " AND " + keyidWhere : "")
            + " ORDER BY 1";
        return this.getQueryProcessor().getPreparedSqlDataSet(sql,
            new Object[]{worksheetitemid, worksheetitemversionid, sdcid});
    }

    private String getFilterWhere(String tableid, PropertyList filter) {
        if (filter != null && filter.size() != 0) {
            String columnid = filter.getProperty("columnid");
            boolean in = filter.getProperty("operator", "in").equalsIgnoreCase("in");
            String valuelist = filter.getProperty("valuelist");
            if (columnid.length() != 0 && valuelist.length() != 0) {
                if (!valuelist.startsWith("'")) {
                    if (valuelist.contains(";")) {
                        valuelist = "'" + StringUtil.replaceAll(valuelist, ";", "','") + "'";
                    } else {
                        valuelist = "'" + valuelist + "'";
                    }
                }

                return " " + tableid + "." + columnid + (in ? " in " : " not in ") + "(" + valuelist
                    + ") ";
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    private void loadContributors(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");

        try {
            DataSet contributors = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT 'A' type, authorid contributorid, sysuserdesc, sysuserid, defaultdepartment departmentid FROM worksheet, sysuser WHERE worksheet.authorid = sysuser.sysuserid AND worksheetid = ? AND worksheetversionid = ? UNION SELECT 'C' type, contributorid, sysuserdesc, sysuserid, defaultdepartment departmentid FROM worksheetcontributor, sysuser WHERE worksheetcontributor.contributorid = sysuser.sysuserid AND worksheetid = ? AND worksheetversionid = ? AND nominatedflag = 'Y' UNION SELECT DISTINCT 'L' type, activityby contributorid, sysuserdesc, sysuser.sysuserid, departmentid FROM worksheetactivitylog, sysuser LEFT OUTER JOIN departmentsysuser ON departmentsysuser.sysuserid = sysuser.sysuserid AND    departmentsysuser.departmentid IN (SELECT departmentid FROM departmentsysuser WHERE sysuserid IN (SELECT authorid FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ? ) ) WHERE worksheetactivitylog.activityby = sysuser.sysuserid AND worksheetid = ? AND worksheetversionid = ? ORDER BY sysuserdesc, sysuserid, type",
                new Object[]{worksheetid, worksheetversionid, worksheetid, worksheetversionid,
                    worksheetid, worksheetversionid, worksheetid, worksheetversionid});

            for (int i = 0; i < contributors.size(); ++i) {
                String sysuserid = contributors.getValue(i, "sysuserid");

                while (i + 1 < contributors.size() && contributors.getValue(i + 1, "sysuserid")
                    .equals(sysuserid)) {
                    contributors.deleteRow(i + 1);
                }
            }

            if (commandRequest.getBoolean("loadhistory")) {
                StringBuffer union = new StringBuffer();
                SafeSQL safeSQL = new SafeSQL();

                for (int i = 0; i < contributors.size(); ++i) {
                    union.append(" UNION ").append(
                            "SELECT activityby, activitydt, activitytype, activitylog, targetsdcid, targetkeyid1, targetkeyid2 FROM (SELECT ")
                        .append(this.sapphireConnection.isSqlServer() ? "TOP 5" : "").append(
                            " activityby, activitydt, activitytype, activitylog, targetsdcid, targetkeyid1, targetkeyid2 FROM worksheetactivitylog WHERE worksheetid = ")
                        .append(safeSQL.addVar(worksheetid)).append(" AND worksheetversionid = ")
                        .append(safeSQL.addVar(worksheetversionid)).append(" AND activityby = ")
                        .append(safeSQL.addVar(contributors.getValue(i, "contributorid"))).append(
                            " AND activitytype NOT IN (" + safeSQL.addIn("Open")
                                + ") ORDER BY activitydt DESC ) wsal ")
                        .append(this.sapphireConnection.isOracle() ? "WHERE rownum < 6" : "");
                }

                if (contributors.size() > 0) {
                    DataSet activity = this.getQueryProcessor().getPreparedSqlDataSet(
                        union.substring(7) + " ORDER BY activityby, activitydt DESC",
                        safeSQL.getValues());
                    commandResponse.set("activity", activity);
                }
            }

            commandResponse.set("contributors", contributors);
        } catch (Exception var10) {
            commandResponse.setStatusFail(
                "Failed to load contributors for worksheet " + BaseELNAction.getIdVersionText(
                    worksheetid, worksheetversionid) + ". Reason: " + var10.getMessage(), var10);
        }

    }

    private void loadFields(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");

        try {
            DataSet fields = commandRequest.getBoolean("groupbyitem") ? this.getQueryProcessor()
                .getPreparedSqlDataSet(
                    "SELECT worksheetitemdesc, worksheetitemfield.worksheetitemid, worksheetitemfield.worksheetitemversionid, fieldname, fieldinstance, fieldtitle, enteredtext, displayvalue FROM worksheetsection, worksheetitem, worksheetitemfield WHERE worksheetsection.worksheetsectionid = worksheetitem.worksheetsectionid AND worksheetsection.worksheetsectionversionid = worksheetitem.worksheetsectionversionid   AND worksheetitemfield.worksheetitemid = worksheetitem.worksheetitemid AND worksheetitemfield.worksheetitemversionid = worksheetitem.worksheetitemversionid   AND worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ? ORDER BY worksheetsection.usersequence, worksheetitem.usersequence, worksheetitemfield.usersequence",
                    new Object[]{worksheetid, worksheetversionid}) : this.getQueryProcessor()
                .getPreparedSqlDataSet(
                    "SELECT worksheetitemdesc, worksheetitemfield.worksheetitemid, worksheetitemfield.worksheetitemversionid, fieldname, fieldinstance, fieldtitle, enteredtext, displayvalue FROM worksheetitemfield, worksheetitem WHERE worksheetitemfield.worksheetitemid = worksheetitem.worksheetitemid AND worksheetitemfield.worksheetitemversionid = worksheetitem.worksheetitemversionid   AND worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ? ORDER BY fieldname, worksheetitemfield.worksheetitemid, fieldinstance",
                    new Object[]{worksheetid, worksheetversionid});
            commandResponse.set("fields", fields);
        } catch (Exception var7) {
            commandResponse.setStatusFail(
                "Failed to load fields for worksheet " + BaseELNAction.getIdVersionText(worksheetid,
                    worksheetversionid) + ". Reason: " + var7.getMessage(), var7);
        }

    }

    private void loadAttachments(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");

        try {
            boolean groupbyitem = commandRequest.getBoolean("groupbyitem");
            DataSet attachments = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT 'W' \"type\", worksheetid \"id\", worksheetversionid \"version\", sdiattachment.sdcid, sdiattachment.keyid1, sdiattachment.keyid2, sdiattachment.keyid3, sdiattachment.attachmentnum, sdiattachment.usersequence,        sdiattachment.attachmentdesc, worksheet.worksheetname, -1 \"wssseq\", 0 \"wsiseq\" FROM sdiattachment, worksheet WHERE sdiattachment.sdcid = 'LV_Worksheet'   AND sdiattachment.keyid1 = worksheet.worksheetid AND sdiattachment.keyid2 = worksheet.worksheetversionid  AND worksheet.worksheetid = ? AND worksheet.worksheetversionid = ? UNION SELECT 'S' \"type\", worksheetsection.worksheetid \"id\", worksheetsection.worksheetversionid \"version\", sdiattachment.sdcid, sdiattachment.keyid1, sdiattachment.keyid2, sdiattachment.keyid3, sdiattachment.attachmentnum, sdiattachment.usersequence,        sdiattachment.attachmentdesc, worksheetsection.worksheetsectiondesc, worksheetsection.usersequence \"wssseq\", -1 \"wsiseq\" FROM sdiattachment, worksheetsection WHERE sdiattachment.sdcid = 'LV_WorksheetSection'   AND sdiattachment.keyid1 = worksheetsection.worksheetsectionid AND sdiattachment.keyid2 = worksheetsection.worksheetsectionversionid  AND worksheetsection.worksheetid = ? AND worksheetsection.worksheetversionid = ? UNION SELECT 'I' \"type\", worksheetitem.worksheetid \"id\", worksheetitem.worksheetversionid \"version\", sdiattachment.sdcid, sdiattachment.keyid1, sdiattachment.keyid2, sdiattachment.keyid3, sdiattachment.attachmentnum, sdiattachment.usersequence,        sdiattachment.attachmentdesc, worksheetitem.worksheetitemdesc, worksheetsection.usersequence \"wssseq\", worksheetitem.usersequence \"wsiseq\" FROM sdiattachment, worksheetitem, worksheetsection WHERE sdiattachment.sdcid = 'LV_WorksheetItem'   AND sdiattachment.keyid1 = worksheetitem.worksheetitemid AND sdiattachment.keyid2 = worksheetitem.worksheetitemversionid   AND worksheetitem.worksheetsectionid = worksheetsection.worksheetsectionid AND worksheetitem.worksheetsectionversionid = worksheetsection.worksheetsectionversionid   AND worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ? AND ( attachmentuse IS NULL OR attachmentuse <> 'HTMLEditor' ) ORDER BY \"wssseq\", \"wsiseq\", "
                    + (groupbyitem ? "usersequence, attachmentnum" : "attachmentdesc"),
                new Object[]{worksheetid, worksheetversionid, worksheetid, worksheetversionid,
                    worksheetid, worksheetversionid});
            commandResponse.set("attachments", attachments);
        } catch (Exception var8) {
            commandResponse.setStatusFail(
                "Failed to load attachments for worksheet " + BaseELNAction.getIdVersionText(
                    worksheetid, worksheetversionid) + ". Reason: " + var8.getMessage(), var8);
        }

    }

    private void loadAttributes(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");

        try {
            DataSet attributes = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT 'W' \"type\", -1, -1, worksheetid \"id\", worksheetversionid \"version\", sdiattribute.attributeid, sdiattribute.attributeinstance, sdiattribute.attributesdcid, sdiattribute.editorstyleid, sdiattribute.datatype, sdiattribute.textvalue, sdiattribute.numericvalue, sdiattribute.datevalue, sdiattribute.defaulttextvalue, sdiattribute.defaultnumericvalue, sdiattribute.defaultdatevalue, sdiattribute.mandatoryflag, sdiattribute.updateableflag, sdcattributedef.attributetitle FROM sdiattribute, sdcattributedef, worksheet WHERE sdiattribute.sdcid = sdcattributedef.sdcid AND sdiattribute.attributeid = sdcattributedef.attributeid AND sdiattribute.sdcid = 'LV_Worksheet'   AND sdiattribute.keyid1 = worksheet.worksheetid AND sdiattribute.keyid2 = worksheet.worksheetversionid   AND worksheet.worksheetid = ? AND worksheet.worksheetversionid = ? UNION SELECT 'S' \"type\", worksheetsection.usersequence, 0, worksheetsectionid \"id\", worksheetsectionversionid \"version\", sdiattribute.attributeid, sdiattribute.attributeinstance, sdiattribute.attributesdcid, sdiattribute.editorstyleid, sdiattribute.datatype, sdiattribute.textvalue, sdiattribute.numericvalue, sdiattribute.datevalue, sdiattribute.defaulttextvalue, sdiattribute.defaultnumericvalue, sdiattribute.defaultdatevalue, sdiattribute.mandatoryflag, sdiattribute.updateableflag, sdcattributedef.attributetitle FROM sdiattribute, sdcattributedef, worksheetsection WHERE sdiattribute.sdcid = sdcattributedef.sdcid AND sdiattribute.attributeid = sdcattributedef.attributeid AND sdiattribute.sdcid = 'LV_WorksheetSection'   AND sdiattribute.keyid1 = worksheetsection.worksheetsectionid AND sdiattribute.keyid2 = worksheetsection.worksheetsectionversionid   AND worksheetsection.worksheetid = ? AND worksheetsection.worksheetversionid = ? UNION SELECT 'I' \"type\", worksheetsection.usersequence, worksheetitem.usersequence, worksheetitemid \"id\", worksheetitemversionid \"version\", sdiattribute.attributeid, sdiattribute.attributeinstance, sdiattribute.attributesdcid, sdiattribute.editorstyleid, sdiattribute.datatype, sdiattribute.textvalue, sdiattribute.numericvalue, sdiattribute.datevalue, sdiattribute.defaulttextvalue, sdiattribute.defaultnumericvalue, sdiattribute.defaultdatevalue, sdiattribute.mandatoryflag, sdiattribute.updateableflag, sdcattributedef.attributetitle FROM sdiattribute, sdcattributedef, worksheetitem, worksheetsection WHERE sdiattribute.sdcid = sdcattributedef.sdcid AND sdiattribute.attributeid = sdcattributedef.attributeid AND sdiattribute.sdcid = 'LV_WorksheetItem'   AND sdiattribute.keyid1 = worksheetitem.worksheetitemid AND sdiattribute.keyid2 = worksheetitem.worksheetitemversionid   AND worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ?   AND worksheetitem.worksheetsectionid = worksheetsection.worksheetsectionid   AND worksheetitem.worksheetsectionversionid = worksheetsection.worksheetsectionversionid ORDER BY "
                    + (commandRequest.getBoolean("groupbyitem") ? "2, 3, 1 DESC, attributeid"
                    : "attributeid, 2, 3"),
                new Object[]{worksheetid, worksheetversionid, worksheetid, worksheetversionid,
                    worksheetid, worksheetversionid}, true);
            this.formatAttributes(attributes);
            commandResponse.set("attributes", attributes);
        } catch (Exception var7) {
            commandResponse.setStatusFail(
                "Failed to load attributes for worksheet " + BaseELNAction.getIdVersionText(
                    worksheetid, worksheetversionid) + ". Reason: " + var7.getMessage(), var7);
        }

    }

    private void addWorksheet(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            PropertyList actionProps = commandRequest.getStringPropertyList();
            actionProps.setProperty("authorid", this.sapphireConnection.getSysuserId());
            actionProps.setProperty("worksheetstatus", "InProgress");
            DataSet attributes = commandRequest.getDataSet("attributes");
            if (actionProps.getProperty("worksheetname").equals("(Auto)")) {
                DataSet template = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT options FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ?",
                    new Object[]{actionProps.getProperty("templateid"),
                        actionProps.getProperty("templateversionid")}, true);
                PropertyList options = new PropertyList();
                options.setPropertyList(template.getClob(0, "options", ""));
                actionProps.setProperty("worksheetname",
                    BaseELNAction.resolveWorksheetName(this.sapphireConnection,
                        this.getSequenceProcessor(), options.getProperty("worksheetnametemplate"),
                        actionProps, attributes));
            }

            this.getActionProcessor().processActionClass(AddWorksheet.class.getName(), actionProps);
            String worksheetid = actionProps.getProperty("worksheetid");
            String worksheetversionid = actionProps.getProperty("worksheetversionid");
            if (attributes != null && attributes.size() > 0) {
                this.saveSDIAttributes(worksheetid, worksheetversionid, "", "", "", "", attributes,
                    false);
            }

            if (commandRequest.getBoolean("loadworksheet")) {
                commandRequest.set("worksheetid", worksheetid);
                commandRequest.set("worksheetversionid", worksheetversionid);
                Worksheet worksheet = new Worksheet(this.sapphireConnection);
                worksheet.open(worksheetid, worksheetversionid, commandRequest, commandResponse);
            } else {
                commandResponse.set("worksheetid", worksheetid);
                commandResponse.set("worksheetversionid", worksheetversionid);
            }
        } catch (Exception var9) {
            commandResponse.setStatusFail(
                "Failed to create worksheet. Reason: " + var9.getMessage(), var9);
        }

    }

    private void addWorksheetVersion(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String templateid = commandRequest.getString("templateid");
            PropertyList actionProps = commandRequest.getStringPropertyList();
            database.createPreparedResultSet("SELECT " + (this.connectionInfo.isOracle()
                ? " nvl( max( to_number( worksheetversionid ) ), 0 )"
                : " isnull( max( cast( worksheetversionid AS Integer ) ), 0 )")
                + " as version FROM worksheet WHERE worksheetid = ?", new Object[]{templateid});
            database.getNext();
            actionProps.setProperty("worksheetversionid",
                String.valueOf(database.getInt("version") + 1));
            actionProps.setProperty("authorid", this.sapphireConnection.getSysuserId());
            actionProps.setProperty("worksheetstatus", "InProgress");
            actionProps.setProperty("newtemplateid", templateid);
            actionProps.setProperty("newversion", "Y");
            this.getActionProcessor().processActionClass(AddWorksheet.class.getName(), actionProps);
            String worksheetid = actionProps.getProperty("worksheetid");
            String worksheetversionid = actionProps.getProperty("worksheetversionid");
            if (commandRequest.getBoolean("loadworksheet")) {
                commandRequest.set("worksheetid", worksheetid);
                commandRequest.set("worksheetversionid", worksheetversionid);
                Worksheet worksheet = new Worksheet(this.sapphireConnection);
                worksheet.open(worksheetid, worksheetversionid, commandRequest, commandResponse);
            } else {
                commandResponse.set("worksheetid", worksheetid);
                commandResponse.set("worksheetversionid", worksheetversionid);
            }
        } catch (Exception var9) {
            commandResponse.setStatusFail(
                "Failed to create worksheet version. Reason: " + var9.getMessage(), var9);
        }

    }

    private void approveWorksheetVersion(CommandRequest commandRequest,
        CommandResponse commandResponse, DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            database.executePreparedUpdate(
                "UPDATE worksheet SET versionstatus = 'A' WHERE worksheetid = ? AND versionstatus = 'C'",
                new Object[]{worksheetid});
            database.executePreparedUpdate(
                "UPDATE worksheet SET versionstatus = 'C' WHERE worksheetid = ? AND worksheetversionid = ?",
                new Object[]{worksheetid, worksheetversionid});
            commandResponse.set("worksheetid", worksheetid);
            commandResponse.set("worksheetversionid", worksheetversionid);
        } catch (Exception var6) {
            commandResponse.setStatusFail(
                "Failed to approve worksheet version. Reason: " + var6.getMessage(), var6);
        }

    }

    private void deleteWorksheetSDI(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            SDIList sdiList = commandRequest.getSDIList("sdilist");
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            actionProps.setProperty("worksheetversionid",
                commandRequest.getString("worksheetversionid"));
            if (commandRequest.getBoolean("deleteunassigned")) {
                actionProps.setProperty("deleteunassigned", "Y");
                actionProps.setProperty("sdcid", commandRequest.getString("sdcid"));
            } else {
                actionProps.setProperty("sdcid", sdiList.getSdcid());
                actionProps.setProperty("keyid1", sdiList.getKeyid1());
                actionProps.setProperty("keyid2", sdiList.getKeyid2());
                actionProps.setProperty("keyid3", sdiList.getKeyid3());
            }

            this.getActionProcessor()
                .processActionClass(DeleteWorksheetSDI.class.getName(), actionProps);
            commandResponse.set("worksheetitemid", actionProps.getProperty("worksheetitemid"));
            commandResponse.set("worksheetitemversionid",
                actionProps.getProperty("worksheetitemversionid"));
        } catch (Exception var6) {
            commandResponse.setStatusFail(
                "Failed to delete worksheet SDIs. Reason: " + var6.getMessage(), var6);
        }

    }

    private void worksheetNameCheck(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetname = commandRequest.getString("worksheetname");
            String workbookid = commandRequest.getString("workbookid");
            String workbookversionid = commandRequest.getString("workbookversionid");
            String templateid = commandRequest.getString("templateid");
            String templatetype = commandRequest.getString("templatetype");
            String templateprivacyflag = commandRequest.getString("templateprivacyflag");
            String wstemplateid = commandRequest.getString("sourcetemplateid");
            String wstemplateversionid = commandRequest.getString("sourcetemplateversionid");
            if (worksheetname.equals("(Auto)")) {
                commandResponse.set("exists", "N");
            } else if (templatetype.length() == 0) {
                String policyNode = "Sapphire Custom";
                if (wstemplateid.length() > 0) {
                    policyNode = Worksheet.getPolicyNode(this.getQueryProcessor(), wstemplateid,
                        wstemplateversionid);
                }

                PropertyList policy = this.getConfigurationProcessor()
                    .getPolicy("ELNPolicy", policyNode);
                boolean globalUniqueness = "Global".equalsIgnoreCase(
                    policy.getProperty("worksheetnameuniqueness", "Global"));
                if (globalUniqueness) {
                    database.createPreparedResultSet(
                        "SELECT worksheetid, worksheetversionid, worksheetname FROM worksheet WHERE worksheetname = ? AND ( templateflag = 'N' OR templateflag IS NULL )",
                        new Object[]{worksheetname});
                } else {
                    database.createPreparedResultSet(
                        "SELECT worksheetid, worksheetversionid, worksheetname FROM worksheet WHERE worksheetname = ? AND workbookid = ? and workbookversionid = ? AND ( templateflag = 'N' OR templateflag IS NULL )",
                        new Object[]{worksheetname, workbookid, workbookversionid});
                }

                commandResponse.set("exists", database.getNext() ? "Y" : "N");
                commandResponse.set("existstype", "name");
            } else if (templateprivacyflag.equals("O")) {
                database.createPreparedResultSet(
                    "SELECT worksheetid, worksheetversionid, worksheetname FROM worksheet WHERE worksheetname = ? AND templateflag = 'Y' AND templatetypeflag = ? AND templateprivacyflag = 'O' AND authorid = ?",
                    new Object[]{worksheetname, templatetype,
                        this.sapphireConnection.getSysuserId()});
                commandResponse.set("exists", database.getNext() ? "Y" : "N");
                commandResponse.set("existstype", "name");
            } else if (BaseSDIAction.isValidKey(templateid)) {
                database.createPreparedResultSet(
                    "SELECT worksheetid, worksheetversionid, worksheetname FROM worksheet WHERE worksheetid = ?",
                    new Object[]{templateid + "_" + (
                        Configuration.isDevmode(this.connectionInfo.getDatabaseId()) ? "LV" : "")
                        + templatetype});
                if (database.getNext()) {
                    commandResponse.set("exists", "Y");
                    commandResponse.set("existstype", "id");
                } else {
                    database.createPreparedResultSet(
                        "SELECT worksheetid, worksheetversionid, worksheetname FROM worksheet WHERE worksheetname = ? AND templateflag = 'Y' AND templatetypeflag = ? AND ( templateprivacyflag = 'G' OR templateprivacyflag IS NULL )",
                        new Object[]{worksheetname, templatetype});
                    commandResponse.set("exists", database.getNext() ? "Y" : "N");
                    commandResponse.set("existstype", "name");
                }
            } else {
                commandResponse.set("exists", "Y");
                commandResponse.set("existstype", "id");
                commandResponse.set("invalid", "Y");
            }
        } catch (Exception var15) {
            commandResponse.setStatusFail(
                "Failed to check worksheet name. Reason: " + var15.getMessage(), var15);
        }

    }

    private void editWorksheet(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            PropertyList editProps = commandRequest.getStringPropertyList();
            editProps.setProperty("elnrequest", "Y");
            this.getActionProcessor().processActionClass(EditWorksheet.class.getName(), editProps);
            commandResponse.set("worksheetdesc", commandRequest.getString("worksheetdesc"));
        } catch (Exception var5) {
            commandResponse.setStatusFail("Failed to edit worksheet. Reason: " + var5.getMessage(),
                var5);
        }

    }

    private void setWorksheetStatus(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            if (commandRequest.getString("status").equals("Reject") || commandRequest.getString(
                "status").equals("Approve") || commandRequest.getString("status")
                .equals("Complete")) {
                DataSet notes = BaseELNAction.getWorksheetNotes(this.getQueryProcessor(),
                    commandRequest.getString("worksheetid"),
                    commandRequest.getString("worksheetversionid"),
                    "followupflag = 'Y' AND resolvedflag = 'N'");
                if (commandRequest.getString("status").equals("Reject")) {
                    if (notes.size() == 0) {
                        commandResponse.set("nofollowups", "Y");
                        return;
                    }
                } else if (notes.size() > 0) {
                    commandResponse.set("unresolvedfollowups", "Y");
                    return;
                }
            }

            PropertyList actionProps = commandRequest.getStringPropertyList();
            this.getActionProcessor()
                .processActionClass(SetWorksheetStatus.class.getName(), actionProps);
        } catch (SapphireException var5) {
            commandResponse.addErrorHandler(this.getActionProcessor().getErrorHandler());
        } catch (Exception var6) {
            commandResponse.setStatusFail(
                "Failed to set worksheet status. Reason: " + var6.getMessage(), var6);
        }

    }

    private void loadTemplateDetails(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        String templateid = commandRequest.getString("templateid");
        String templateversionid = commandRequest.getString("templateversionid");

        try {
            if (templateversionid.length() == 0 || templateversionid.equalsIgnoreCase("C")) {
                templateversionid = BaseELNAction.resolveVersion(this.getQueryProcessor(),
                    templateid, templateversionid, "worksheet");
            }

            DataSet template = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT options FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ?",
                new Object[]{templateid, templateversionid}, true);
            PropertyList options = new PropertyList();
            options.setPropertyList(template.getClob(0, "options", ""));
            commandResponse.set("options", options);
            DataSet attributes = this.loadAttributes("LV_Worksheet", templateid, templateversionid);
            commandResponse.set("attributes", attributes);
            commandResponse.set("editorstyles",
                this.getEditorStyles(attributes.getColumnValues("editorstyleid", ";")));
        } catch (Exception var9) {
            commandResponse.setStatusFail(
                "Failed to load details for template '" + BaseELNAction.getIdVersionText(templateid,
                    templateversionid) + "'. Reason: " + var9.getMessage(), var9);
        }

    }

    private void saveAsTemplate(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("workbookid", "(null)");
            actionProps.setProperty("workbookversionid", "(null)");
            actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            actionProps.setProperty("worksheetversionid",
                commandRequest.getString("worksheetversionid"));
            actionProps.setProperty("worksheetsectionid",
                commandRequest.getString("worksheetsectionid"));
            actionProps.setProperty("worksheetsectionversionid",
                commandRequest.getString("worksheetsectionversionid"));
            actionProps.setProperty("worksheetitemid", commandRequest.getString("worksheetitemid"));
            actionProps.setProperty("worksheetitemversionid",
                commandRequest.getString("worksheetitemversionid"));
            actionProps.setProperty("newtemplateid", commandRequest.getString("newtemplateid"));
            actionProps.setProperty("worksheetname", commandRequest.getString("worksheetname"));
            actionProps.setProperty("templateflag", "Y");
            actionProps.setProperty("templatetypeflag",
                commandRequest.getString("templatetypeflag"));
            actionProps.setProperty("templateprivacyflag",
                commandRequest.getString("templateprivacyflag", "O"));
            actionProps.setProperty("newtemplatemode", commandRequest.getString("newtemplatemode"));
            PropertyList options = commandRequest.getPropertyList("options");
            if (options != null) {
                actionProps.putAll(options);
            }

            this.getActionProcessor()
                .processActionClass(CopyWorksheet.class.getName(), actionProps);
            commandResponse.set("worksheetid", actionProps.getProperty("worksheetid"));
            commandResponse.set("worksheetversionid",
                actionProps.getProperty("worksheetversionid"));
        } catch (Exception var6) {
            commandResponse.setStatusFail("Failed to save worksheet. Reason: " + var6.getMessage(),
                var6);
        }

    }

    private void addSection(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            actionProps.setProperty("worksheetversionid",
                commandRequest.getString("worksheetversionid"));
            actionProps.setProperty("sectiondesc",
                commandRequest.getString("worksheetsectiondesc"));
            actionProps.setProperty("sectionlevel", commandRequest.getString("sectionlevel"));
            actionProps.setProperty("templateid", commandRequest.getString("templateid"));
            actionProps.setProperty("templateversionid",
                commandRequest.getString("templateversionid"));
            actionProps.setProperty("sectiontemplateid",
                commandRequest.getString("sectiontemplateid"));
            actionProps.setProperty("sectiontemplateversionid",
                commandRequest.getString("sectiontemplateversionid"));
            actionProps.setProperty("fromworksheetid", commandRequest.getString("fromworksheetid"));
            actionProps.setProperty("fromworksheetversionid",
                commandRequest.getString("fromworksheetversionid"));
            String beforesectionid = commandRequest.getString("beforesectionid");
            int usersequence = -1;
            if (beforesectionid.length() > 0) {
                usersequence = this.getQueryProcessor().getPreparedCount(
                    "SELECT usersequence FROM worksheetsection WHERE worksheetid=? AND worksheetsectionid=?",
                    new String[]{commandRequest.getString("worksheetid"), beforesectionid});
            }

            actionProps.setProperty("usersequence", "" + usersequence);
            String aftersectionid = commandRequest.getString("aftersectionid");
            int afterusersequence = -1;
            if (aftersectionid.length() > 0) {
                afterusersequence = this.getQueryProcessor().getPreparedCount(
                    "SELECT usersequence FROM worksheetsection WHERE worksheetid=? AND worksheetsectionid=?",
                    new String[]{commandRequest.getString("worksheetid"), aftersectionid});
            }

            actionProps.setProperty("usersequence", "" + usersequence);
            actionProps.setProperty("afterusersequence", "" + afterusersequence);
            this.getActionProcessor()
                .processActionClass(AddWorksheetSection.class.getName(), actionProps);
            String[] sectionid = StringUtil.split(actionProps.getProperty("worksheetsectionid"),
                ";");
            String[] sectionversion = StringUtil.split(
                actionProps.getProperty("worksheetsectionversionid"), ";");
            StringBuffer where = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();

            for (int i = 0; i < sectionid.length; ++i) {
                where.append(" OR ").append("(worksheetsectionid=")
                    .append(safeSQL.addVar(sectionid[i])).append(" AND worksheetsectionversionid=")
                    .append(safeSQL.addVar(sectionversion[i])).append(")");
            }

            DataSet sections = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT * FROM worksheetsection WHERE " + where.substring(4)
                    + " ORDER BY usersequence", safeSQL.getValues(), true);

            for (int i = 0; i < sections.size(); ++i) {
                sections.setValue(i, "options",
                    (new PropertyList(sections.getValue(i, "options"))).toJSONString());
            }

            commandResponse.set("worksheetsections", sections);
            if (commandRequest.getString("templateid").length() > 0
                || commandRequest.getString("sectiontemplateid").length() > 0) {
                DataSet sectionitems = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT * FROM worksheetitem WHERE " + where.substring(4)
                        + " ORDER BY usersequence", safeSQL.getValues(), true);

                for (int i = 0; i < sectionitems.size(); ++i) {
                    WorksheetItem worksheetItem = WorksheetItemFactory.getRenderingInstance(
                        this.sapphireConnection, (HashMap) sectionitems.get(i),
                        commandRequest.getString("width"));
                    sectionitems.setString(i, "elementid", worksheetItem.getElementId());
                    sectionitems.addColumn("itemtype", 0);
                    sectionitems.setValue(i, "itemtype", worksheetItem.getClientRenderer());
                    sectionitems.setValue(i, "html", worksheetItem.getViewHTML());
                    sectionitems.setString(i, "availabilitystatus",
                        worksheetItem.getAvailability());
                    PropertyList options = worksheetItem.getWorksheetItemOptions().toPropertyList();
                    sectionitems.setValue(i, "options", options.toJSONString());
                    sectionitems.setString(i, "option_itemcompletion",
                        options.getProperty("itemcompletion", "N"));
                }

                if (commandRequest.getString("templateid").length() > 0) {
                    updateMRUList(
                        new ConfigurationProcessor(this.sapphireConnection.getConnectionId()),
                        this.sapphireConnection.getSysuserId(),
                        commandRequest.getString("__hostwebpageid") + "_recent_section_templates",
                        commandRequest.getString("templateid") + ";" + commandRequest.getString(
                            "templateversionid"));
                } else if (commandRequest.getString("sectiontemplateid").length() > 0) {
                    updateMRUList(
                        new ConfigurationProcessor(this.sapphireConnection.getConnectionId()),
                        this.sapphireConnection.getSysuserId(),
                        commandRequest.getString("__hostwebpageid") + "_recent_copy_worksheets",
                        commandRequest.getString("fromworksheetid") + ";"
                            + commandRequest.getString("fromworksheetversionid"));
                }

                commandResponse.set("worksheetitems", sectionitems);
            }
        } catch (Exception var18) {
            commandResponse.setStatusFail(
                "Failed to create worksheet section. Reason: " + var18.getMessage(), var18);
        }

    }

    private void editSection(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            this.getActionProcessor().processActionClass(EditWorksheetSection.class.getName(),
                commandRequest.getStringPropertyList());
            commandResponse.set("worksheetsectiondesc",
                commandRequest.getString("worksheetsectiondesc"));
        } catch (Exception var5) {
            commandResponse.setStatusFail(
                "Failed to edit worksheet section. Reason: " + var5.getMessage(), var5);
        }

    }

    private void moveSection(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            DataSet sections = commandRequest.getDataSet("worksheetsections");
            int sectionrow = Integer.parseInt(commandRequest.getString("sectionrow"));
            String worksheetid = sections.getValue(sectionrow, "worksheetid");
            String worksheetversionid = sections.getValue(sectionrow, "worksheetversionid");
            String worksheetsectionid = sections.getValue(sectionrow, "worksheetsectionid");
            String worksheetsectionversionid = sections.getValue(sectionrow,
                "worksheetsectionversionid");
            boolean up = commandRequest.getBoolean("up");
            int newseq = 0;
            int currseq;
            if (up && sectionrow > 1) {
                currseq = Integer.parseInt(sections.getValue(sectionrow, "usersequence"));
                newseq = Integer.parseInt(sections.getValue(sectionrow - 1, "usersequence"));
                database.executePreparedUpdate(
                    "UPDATE worksheetsection SET usersequence = ? WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?",
                    new Object[]{newseq, worksheetsectionid, worksheetsectionversionid});
                database.executePreparedUpdate(
                    "UPDATE worksheetsection SET usersequence = ? WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?",
                    new Object[]{currseq, sections.getValue(sectionrow - 1, "worksheetsectionid"),
                        sections.getValue(sectionrow - 1, "worksheetsectionversionid")});
                sections.setValue(sectionrow - 1, "usersequence", String.valueOf(currseq));
                sections.setValue(sectionrow, "usersequence", String.valueOf(newseq));
                sections.add(sectionrow - 1, sections.remove(sectionrow));
            } else if (!up && sectionrow < sections.size() - 1) {
                currseq = Integer.parseInt(sections.getValue(sectionrow, "usersequence"));
                newseq = Integer.parseInt(sections.getValue(sectionrow + 1, "usersequence"));
                database.executePreparedUpdate(
                    "UPDATE worksheetsection SET usersequence = ? WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?",
                    new Object[]{newseq, worksheetsectionid, worksheetsectionversionid});
                database.executePreparedUpdate(
                    "UPDATE worksheetsection SET usersequence = ? WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?",
                    new Object[]{currseq, sections.getValue(sectionrow + 1, "worksheetsectionid"),
                        sections.getValue(sectionrow + 1, "worksheetsectionversionid")});
                sections.setValue(sectionrow + 1, "usersequence", String.valueOf(currseq));
                sections.setValue(sectionrow, "usersequence", String.valueOf(newseq));
                sections.add(sectionrow + 1, sections.remove(sectionrow));
            }

            PropertyList activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("targetsdcid", "LV_WorksheetSection");
            activityProps.setProperty("targetkeyid1", worksheetsectionid);
            activityProps.setProperty("targetkeyid2", worksheetsectionversionid);
            activityProps.setProperty("activitytype", "Edit");
            activityProps.setProperty("activitylog",
                "Moved section " + (up ? "up" : "down") + " to sequence " + newseq);
            this.getActionProcessor()
                .processActionClass(AddWorksheetActivity.class.getName(), activityProps);
            commandResponse.set("worksheetsections", sections);
        } catch (Exception var13) {
            commandResponse.setStatusFail(
                "Failed to move worksheet section. Reason: " + var13.getMessage(), var13);
        }

    }

    private void setSectionStatus(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            if (commandRequest.getString("status").equals("Reject") || commandRequest.getString(
                "status").equals("Approve") || commandRequest.getString("status")
                .equals("Complete")) {
                DataSet notes = BaseELNAction.getWorksheetSectionNotes(this.getQueryProcessor(),
                    database, commandRequest.getString("worksheetid"),
                    commandRequest.getString("worksheetversionid"),
                    commandRequest.getString("worksheetsectionid"),
                    commandRequest.getString("worksheetsectionversionid"),
                    "followupflag = 'Y' AND resolvedflag = 'N'", true);
                if (commandRequest.getString("status").equals("Reject")) {
                    if (notes.size() == 0) {
                        commandResponse.set("nofollowups", "Y");
                        return;
                    }
                } else if (notes.size() > 0) {
                    commandResponse.set("unresolvedfollowups", "Y");
                    return;
                }
            }

            PropertyList actionProps = commandRequest.getStringPropertyList();
            actionProps.setProperty("htmlerror", "Y");
            this.getActionProcessor()
                .processActionClass(SetWorksheetSectionStatus.class.getName(), actionProps);
            commandResponse.set("setstatusworksheetitemid",
                actionProps.getProperty("setstatusworksheetitemid"));
            commandResponse.set("setstatusworksheetitemversionid",
                actionProps.getProperty("setstatusworksheetitemversionid"));
            commandResponse.set("setstatusvalue", actionProps.getProperty("setstatusvalue"));
            commandResponse.set("availableworksheetitemid",
                actionProps.getProperty("availableworksheetitemid"));
            commandResponse.set("availableworksheetitemversionid",
                actionProps.getProperty("availableworksheetitemversionid"));
            commandResponse.set("availabilityflag", actionProps.getProperty("availabilityflag"));
            PropertyList loadSectionDetails = new PropertyList();
            loadSectionDetails.setProperty("worksheetid", actionProps.getProperty("worksheetid"));
            loadSectionDetails.setProperty("worksheetversionid",
                actionProps.getProperty("worksheetversionid"));
            loadSectionDetails.setProperty("worksheetsectionid",
                actionProps.getProperty("worksheetsectionid"));
            loadSectionDetails.setProperty("worksheetsectionversionid",
                actionProps.getProperty("worksheetsectionversionid"));
            this.getActionProcessor()
                .processActionClass(LoadSection.class.getName(), loadSectionDetails);
            SDIData sectionData = (SDIData) loadSectionDetails.get("section");
            commandResponse.set("sectiondata", sectionData.getDataset("primary"));
            if (commandRequest.getBoolean("refreshavailability")) {
                PropertyList loadProps = new PropertyList();
                loadProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
                loadProps.setProperty("worksheetversionid",
                    commandRequest.getString("worksheetversionid"));
                loadProps.setProperty("loadforavailability", "Y");
                this.getActionProcessor()
                    .processActionClass(LoadWorksheet.class.getName(), loadProps);
                SDIData worksheetData = (SDIData) loadProps.get("worksheet");
                commandResponse.set("worksheetsections",
                    worksheetData.getSDIData("sections").getDataset("primary"));
                commandResponse.set("worksheetitems",
                    worksheetData.getSDIData("items").getDataset("primary"));
            }
        } catch (SapphireException var9) {
            commandResponse.addErrorHandler(this.getActionProcessor().getErrorHandler());
        } catch (Exception var10) {
            commandResponse.setStatusFail(
                "Failed to set worksheet section status. Reason: " + var10.getMessage(), var10);
        }

    }

    private void checkFollowupNote(CommandRequest commandRequest, CommandResponse commandResponse) {
        try {
            if (commandRequest.getString("checkfollowupnote").equals("Y")) {
                DataSet notes = BaseELNAction.getWorksheetNotes(this.getQueryProcessor(),
                    commandRequest.getString("worksheetid"),
                    commandRequest.getString("worksheetversionid"),
                    "followupflag = 'Y' AND resolvedflag = 'N'");
                if (notes.size() == 0) {
                    commandResponse.set("nofollowups", "Y");
                    return;
                }
            }
        } catch (Exception var4) {
            commandResponse.addErrorHandler(var4.getMessage());
        }

    }

    private void deleteSection(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "LV_WorksheetSection");
            actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            actionProps.setProperty("worksheetversionid",
                commandRequest.getString("worksheetversionid"));
            actionProps.setProperty("worksheetsectionid",
                commandRequest.getString("worksheetsectionid"));
            actionProps.setProperty("worksheetsectionversionid",
                commandRequest.getString("worksheetsectionversionid"));
            this.getActionProcessor()
                .processActionClass(DeleteWorksheetSection.class.getName(), actionProps);
        } catch (Exception var5) {
            commandResponse.setStatusFail(
                "Failed to delete worksheet section. Reason: " + var5.getMessage(), var5);
        }

    }

    private void loadWorksheetItem(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            boolean dockmode = commandRequest.getString("dockmode").equals("Y");
            int auditsequence = -1;

            try {
                auditsequence = Integer.parseInt(commandRequest.getString("auditsequence", "-1"));
            } catch (Exception var10) {
            }

            DataSet item = auditsequence == -1 ? this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT * FROM worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ?",
                new Object[]{worksheetitemid, worksheetitemversionid}, true)
                : this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT * FROM a_worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ? AND auditsequence = ?",
                    new Object[]{worksheetitemid, worksheetitemversionid, auditsequence}, true);
            WorksheetItem worksheetItem = WorksheetItemFactory.getRenderingInstance(
                this.sapphireConnection, (HashMap) item.get(0), commandRequest.getString("width"));
            worksheetItem.setTemplate(commandRequest.getBoolean("template"));
            item.setString(0, "elementid", worksheetItem.getElementId());
            if (auditsequence > -1 && !worksheetItem.getWorksheetItemOptions()
                .getOption("supportshistory", "N").equals("Y")) {
                item.setValue(0, "html", this.getTranslationProcessor()
                    .translate("Historical data not available for this control"));
            } else if (item.getValue(0, "itemstatus", "InProgress").equals("InProgress")
                || item.getValue(0, "html").length() == 0) {
                item.setValue(0, "html", dockmode ? worksheetItem.getDockViewHTML(
                    commandRequest.getString("dockpanelid") + "_") : worksheetItem.getViewHTML());
                item.setString(0, "availabilitystatus", worksheetItem.getAvailability());
            }

            item.setValue(0, "options",
                worksheetItem.getWorksheetItemOptions().toPropertyList().toJSONString());
            commandResponse.set("worksheetitem", item);
        } catch (Exception var11) {
            commandResponse.setStatusFail(
                "Failed to load worksheet item. Reason: " + var11.getMessage(), var11);
        }

    }

    private void loadWorksheetItemDiff(CommandRequest commandRequest,
        CommandResponse commandResponse, DBUtil database) {
        String worksheetitemid = commandRequest.getString("worksheetitemid");
        String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
        String prefix = commandRequest.getString("prefix");

        try {
            int auditsequence = Integer.parseInt(commandRequest.getString("auditsequence", "-1"));
            int priorauditsequence = Integer.parseInt(
                commandRequest.getString("priorauditsequence", "-1"));
            DataSet current = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT * FROM a_worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ? AND auditsequence = ?",
                new Object[]{worksheetitemid, worksheetitemversionid, auditsequence}, true);
            DataSet prior = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT contents FROM a_worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ? AND auditsequence = ?",
                new Object[]{worksheetitemid, worksheetitemversionid, priorauditsequence}, true);
            if (current.size() == 1 && prior.size() == 1) {
                WorksheetItem worksheetItem = WorksheetItemFactory.getInstance(
                    this.sapphireConnection, database, (HashMap) current.get(0));
                String diffhtml = worksheetItem.getDiffHTML(current.getValue(0, "contents"),
                    prior.getValue(0, "contents"), prefix);
                commandResponse.set("diffhtml", diffhtml);
            } else {
                commandResponse.setStatusFail("Failed to load diff information");
            }
        } catch (Exception var13) {
            commandResponse.setStatusFail(
                "Failed to load diff information. Reason: " + var13.getMessage(), var13);
        }

    }

    private void loadWorksheetItemIncludes(CommandRequest commandRequest,
        CommandResponse commandResponse, DBUtil database) {
        try {
            PropertyListCollection cssincludes = new PropertyListCollection();
            PropertyListCollection scriptincludes = new PropertyListCollection();
            BaseELNAction.getWorksheetItemIncludes(this.sapphireConnection,
                this.getQueryProcessor(), cssincludes, scriptincludes);
            StringBuffer includes = new StringBuffer();

            for (int i = 0; i < cssincludes.size(); ++i) {
                includes.append("<link rel=\"stylesheet\" href=\"")
                    .append(cssincludes.getPropertyList(i).getProperty("cssfile"))
                    .append("\" type=\"text/css\"/>");
            }

            commandResponse.set("includes", includes.toString());
        } catch (Exception var8) {
            commandResponse.setStatusFail(
                "Failed to load worksheet item includes. Reason: " + var8.getMessage(), var8);
        }

    }

    private void addItem(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String propertytreeid = commandRequest.getString("propertytreeid");
            String templateid = commandRequest.getString("templateid");
            String templateversionid = commandRequest.getString("templateversionid");
            String itemtemplateid = commandRequest.getString("itemtemplateid");
            String itemtemplateversionid = commandRequest.getString("itemtemplateversionid");
            DataSet items = commandRequest.getDataSet("worksheetitems");
            int itemrow = Integer.parseInt(commandRequest.getString("itemrow"));
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            actionProps.setProperty("worksheetversionid",
                commandRequest.getString("worksheetversionid"));
            actionProps.setProperty("worksheetsectionid",
                commandRequest.getString("worksheetsectionid"));
            actionProps.setProperty("worksheetsectionversionid",
                commandRequest.getString("worksheetsectionversionid"));
            actionProps.setProperty("propertytreeid", propertytreeid);
            actionProps.setProperty("sourcenodeid", commandRequest.getString("sourcenodeid"));
            actionProps.setProperty("sdcid", commandRequest.getString("sdcid"));
            actionProps.setProperty("keyid1", commandRequest.getString("keyid1"));
            actionProps.setProperty("templateid", templateid);
            actionProps.setProperty("templateversionid", templateversionid);
            actionProps.setProperty("fromworksheetid", commandRequest.getString("fromworksheetid"));
            actionProps.setProperty("fromworksheetversionid",
                commandRequest.getString("fromworksheetversionid"));
            actionProps.setProperty("itemtemplateid", itemtemplateid);
            actionProps.setProperty("itemtemplateversionid", itemtemplateversionid);
            actionProps.setProperty("keyid2", commandRequest.getString("keyid2"));
            actionProps.setProperty("keyid3", commandRequest.getString("keyid3"));
            actionProps.setProperty("usersequence", commandRequest.getString("itemrow"));
            this.getActionProcessor()
                .processActionClass(AddWorksheetItem.class.getName(), actionProps);
            String worksheetitemid = actionProps.getProperty("worksheetitemid");
            String worksheetitemversionid = actionProps.getProperty("worksheetitemversionid");
            items.setValue(itemrow, "worksheetitemid", worksheetitemid);
            items.setValue(itemrow, "worksheetitemversionid", worksheetitemversionid);
            items.setValue(itemrow, "worksheetid", commandRequest.getString("worksheetid"));
            items.setValue(itemrow, "worksheetversionid",
                commandRequest.getString("worksheetversionid"));
            items.setValue(itemrow, "worksheetsectionid",
                commandRequest.getString("worksheetsectionid"));
            items.setValue(itemrow, "worksheetsectionversionid",
                commandRequest.getString("worksheetsectionversionid"));
            items.setValue(itemrow, "propertytreeid", commandRequest.getString("propertytreeid"));
            items.setValue(itemrow, "usersequence", String.valueOf(itemrow));
            items.setValue(itemrow, "createby", this.sapphireConnection.getSysuserId());
            if (actionProps.getProperty("usersequenceupdate", "N").equals("Y")) {
                int seq = itemrow + 1;

                for (int i = itemrow + 1; i < items.size(); ++i) {
                    items.setValue(i, "usersequence", String.valueOf(seq));
                    ++seq;
                }
            }

            DataSet item = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT * FROM worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ?",
                new Object[]{worksheetitemid, worksheetitemversionid}, true);
            WorksheetItem worksheetItem = WorksheetItemFactory.getRenderingInstance(
                this.sapphireConnection, (HashMap) item.get(0), commandRequest.getString("width"));
            worksheetItem.setTemplate(commandRequest.getBoolean("template"));
            items.setString(itemrow, "elementid", worksheetItem.getElementId());
            items.setValue(itemrow, "html", templateid.length() == 0 && itemtemplateid.length() == 0
                ? worksheetItem.getEditorHTML() : worksheetItem.getViewHTML());
            items.setString(itemrow, "availabilitystatus", worksheetItem.getAvailability());
            items.setValue(itemrow, "contents", worksheetItem.getDefaultContents());
            items.setValue(itemrow, "options",
                worksheetItem.getWorksheetItemOptions().toPropertyList().toJSONString());
            items.setValue(itemrow, "worksheetitemdesc", item.getValue(0, "worksheetitemdesc"));
            items.setValue(itemrow, "captionflag", item.getValue(0, "captionflag"));
            if (templateid.length() > 0) {
                updateMRUList(new ConfigurationProcessor(this.sapphireConnection.getConnectionId()),
                    this.sapphireConnection.getSysuserId(),
                    commandRequest.getString("__hostwebpageid") + "_recent_" + item.getValue(0,
                        "propertytreeid") + "_templates", templateid + ";" + templateversionid);
            } else if (itemtemplateid.length() > 0) {
                updateMRUList(new ConfigurationProcessor(this.sapphireConnection.getConnectionId()),
                    this.sapphireConnection.getSysuserId(),
                    commandRequest.getString("__hostwebpageid") + "_recent_copy_worksheets",
                    commandRequest.getString("fromworksheetid") + ";" + commandRequest.getString(
                        "fromworksheetversionid"));
            }

            commandResponse.set("worksheetitems", items);
            commandResponse.set("itemrow", String.valueOf(itemrow));
            commandResponse.set("itemtype", worksheetItem.getClientRenderer());
        } catch (Exception var16) {
            commandResponse.setStatusFail(
                "Failed to add worksheet item. Reason: " + var16.getMessage(), var16);
        }

    }

    private void editItem(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            this.getActionProcessor().processActionClass(EditWorksheetItem.class.getName(),
                commandRequest.getStringPropertyList());
        } catch (Exception var5) {
            commandResponse.setStatusFail(
                "Failed to edit worksheet item. Reason: " + var5.getMessage(), var5);
        }

    }

    private void moveItem(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            DataSet items = commandRequest.getDataSet("worksheetitems");
            int itemrow = Integer.parseInt(commandRequest.getString("itemrow"));
            String worksheetid = items.getValue(itemrow, "worksheetid");
            String worksheetversionid = items.getValue(itemrow, "worksheetversionid");
            String worksheetitemid = items.getValue(itemrow, "worksheetitemid");
            String worksheetitemversionid = items.getValue(itemrow, "worksheetitemversionid");
            String currentsectionid = items.getValue(itemrow, "worksheetsectionid");
            String currentsectionversionid = items.getValue(itemrow, "worksheetsectionversionid");
            boolean up = commandRequest.getBoolean("up");
            int newseq = 0;
            String nextsectionid;
            String nextsectionversionid;
            DataSet nextitems;
            int newrow;
            int usersequence;
            int currseq;
            if (up) {
                if (itemrow > 0) {
                    currseq = Integer.parseInt(items.getValue(itemrow, "usersequence"));
                    newseq = Integer.parseInt(items.getValue(itemrow - 1, "usersequence"));
                    database.executePreparedUpdate(
                        "UPDATE worksheetitem SET usersequence = ? WHERE worksheetitemid = ? AND worksheetitemversionid = ?",
                        new Object[]{newseq, worksheetitemid, worksheetitemversionid});
                    database.executePreparedUpdate(
                        "UPDATE worksheetitem SET usersequence = ? WHERE worksheetitemid = ? AND worksheetitemversionid = ?",
                        new Object[]{currseq, items.getValue(itemrow - 1, "worksheetitemid"),
                            items.getValue(itemrow - 1, "worksheetitemversionid")});
                    items.setValue(itemrow - 1, "usersequence", String.valueOf(currseq));
                    items.setValue(itemrow, "usersequence", String.valueOf(newseq));
                    items.add(itemrow - 1, items.remove(itemrow));
                } else {
                    nextsectionid = commandRequest.getString("priorsectionid");
                    nextsectionversionid = commandRequest.getString("priorsectionversionid");
                    nextitems = commandRequest.getDataSet("priorsectionitems");
                    newseq = nextitems.size() == 0 ? 0 : Integer.parseInt(
                        nextitems.getString(nextitems.size() - 1, "usersequence")) + 1;
                    database.executePreparedUpdate(
                        "UPDATE worksheetitem SET usersequence=?, worksheetsectionid=?, worksheetsectionversionid=? WHERE worksheetitemid=? AND worksheetitemversionid=?",
                        new Object[]{newseq, nextsectionid, nextsectionversionid, worksheetitemid,
                            worksheetitemversionid});
                    database.executePreparedUpdate(
                        "UPDATE worksheetitem SET usersequence=usersequence-1 WHERE worksheetsectionid=? AND worksheetsectionversionid=?",
                        new Object[]{currentsectionid, currentsectionversionid});
                    database.executePreparedUpdate(
                        "UPDATE worksheetitem SET usersequence=0 WHERE worksheetsectionid=? AND worksheetsectionversionid=? AND usersequence=-1",
                        new Object[]{currentsectionid, currentsectionversionid});
                    nextitems.copyRow(items, itemrow, 1);
                    newrow = nextitems.size() - 1;
                    nextitems.setString(newrow, "usersequence", "" + newseq);
                    nextitems.setString(newrow, "worksheetsectionid", nextsectionid);
                    nextitems.setString(newrow, "worksheetsectionversionid", nextsectionversionid);
                    items.deleteRow(itemrow);
                    usersequence = 0;

                    while (true) {
                        if (usersequence >= items.size()) {
                            commandResponse.set("priorsectionkey",
                                nextsectionid + ";" + nextsectionversionid);
                            commandResponse.set("priorworksheetitems", nextitems);
                            break;
                        }

                        items.setString(usersequence, "usersequence",
                            "" + (Integer.parseInt(items.getString(usersequence, "usersequence"))
                                - 1));
                        ++usersequence;
                    }
                }
            } else if (!up) {
                if (itemrow < items.size() - 1) {
                    currseq = Integer.parseInt(items.getValue(itemrow, "usersequence"));
                    newseq = Integer.parseInt(items.getValue(itemrow + 1, "usersequence"));
                    database.executePreparedUpdate(
                        "UPDATE worksheetitem SET usersequence = ? WHERE worksheetitemid = ? AND worksheetitemversionid = ?",
                        new Object[]{newseq, worksheetitemid, worksheetitemversionid});
                    database.executePreparedUpdate(
                        "UPDATE worksheetitem SET usersequence = ? WHERE worksheetitemid = ? AND worksheetitemversionid = ?",
                        new Object[]{currseq, items.getValue(itemrow + 1, "worksheetitemid"),
                            items.getValue(itemrow + 1, "worksheetitemversionid")});
                    items.setValue(itemrow + 1, "usersequence", String.valueOf(currseq));
                    items.setValue(itemrow, "usersequence", String.valueOf(newseq));
                    items.add(itemrow + 1, items.remove(itemrow));
                } else {
                    nextsectionid = commandRequest.getString("nextsectionid");
                    nextsectionversionid = commandRequest.getString("nextsectionversionid");
                    nextitems = commandRequest.getDataSet("nextsectionitems");
                    newseq = 0;
                    database.executePreparedUpdate(
                        "UPDATE worksheetitem SET usersequence=usersequence+1 WHERE worksheetsectionid=? AND worksheetsectionversionid=?",
                        new Object[]{currentsectionid, currentsectionversionid});
                    database.executePreparedUpdate(
                        "UPDATE worksheetitem SET usersequence=0, worksheetsectionid=?, worksheetsectionversionid=? WHERE worksheetitemid=? AND worksheetitemversionid=?",
                        new Object[]{nextsectionid, nextsectionversionid, worksheetitemid,
                            worksheetitemversionid});

                    for (newrow = 0; newrow < nextitems.size(); ++newrow) {
                        usersequence = Integer.parseInt(
                            nextitems.getString(newrow, "usersequence"));
                        usersequence = usersequence == -1 ? 0 : usersequence;
                        nextitems.setString(newrow, "usersequence", "" + (usersequence + 1));
                    }

                    nextitems.copyRow(items, itemrow, 1);
                    newrow = nextitems.size() - 1;
                    nextitems.setString(newrow, "usersequence", "0");
                    nextitems.setString(newrow, "worksheetsectionid", nextsectionid);
                    nextitems.setString(newrow, "worksheetsectionversionid", nextsectionversionid);
                    nextitems.sort("usersequence");
                    items.deleteRow(itemrow);
                    commandResponse.set("nextsectionkey",
                        nextsectionid + ";" + nextsectionversionid);
                    commandResponse.set("nextworksheetitems", nextitems);
                }
            }

            PropertyList activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("targetsdcid", "LV_WorksheetItem");
            activityProps.setProperty("targetkeyid1", worksheetitemid);
            activityProps.setProperty("targetkeyid2", worksheetitemversionid);
            activityProps.setProperty("activitytype", "Edit");
            activityProps.setProperty("activitylog",
                "Moved item " + (up ? "up" : "down") + " to sequence " + newseq);
            this.getActionProcessor()
                .processActionClass(AddWorksheetActivity.class.getName(), activityProps);
            commandResponse.set("worksheetitems", items);
        } catch (Exception var19) {
            commandResponse.setStatusFail(
                "Failed to move worksheet item. Reason: " + var19.getMessage(), var19);
        }

    }

    private void setItemStatus(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            PropertyList actionProps = commandRequest.getStringPropertyList();
            actionProps.setProperty("htmlerror", "Y");
            this.getActionProcessor()
                .processActionClass(SetWorksheetItemStatus.class.getName(), actionProps);
            commandResponse.set("availableworksheetitemid",
                actionProps.getProperty("availableworksheetitemid"));
            commandResponse.set("availableworksheetitemversionid",
                actionProps.getProperty("availableworksheetitemversionid"));
            commandResponse.set("availabilityflag", actionProps.getProperty("availabilityflag"));
            if (commandRequest.getBoolean("refreshavailability")) {
                PropertyList loadProps = new PropertyList();
                loadProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
                loadProps.setProperty("worksheetversionid",
                    commandRequest.getString("worksheetversionid"));
                loadProps.setProperty("loadforavailability", "Y");
                this.getActionProcessor()
                    .processActionClass(LoadWorksheet.class.getName(), loadProps);
                SDIData worksheetData = (SDIData) loadProps.get("worksheet");
                commandResponse.set("worksheetsections",
                    worksheetData.getSDIData("sections").getDataset("primary"));
                commandResponse.set("worksheetitems",
                    worksheetData.getSDIData("items").getDataset("primary"));
            }
        } catch (SapphireException var7) {
            commandResponse.addErrorHandler(this.getActionProcessor().getErrorHandler());
        } catch (Exception var8) {
            commandResponse.setStatusFail(
                "Failed to set worksheet item status. Reason: " + var8.getMessage(), var8);
        }

    }

    private void canExecuteOperation(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            String lockedby = "";
            RSet rset;
            if (commandRequest.getString("lockworksheet", "N").equals("N")) {
                rset = das.createLockedRSet("LV_WorksheetItem", worksheetitemid,
                    worksheetitemversionid, "(null)", "LA");
                database.createPreparedResultSet(
                    "SELECT rsetitems.sysuserid \"__lockedby\" FROM worksheetitem, rsetitems WHERE rsetitems.rsetid = ? AND rsetitems.sdcid = ? AND rsetitems.keyid1 = worksheetitem.worksheetitemid AND rsetitems.keyid2 = worksheetitem.worksheetitemversionid",
                    new Object[]{rset.getRsetid(), "LV_WorksheetItem"});
                if (database.getNext()) {
                    lockedby = database.getValue("__lockedby");
                }

                das.clearRSet(rset);
            } else {
                rset = das.createLockedRSet("LV_Worksheet", worksheetid, worksheetversionid,
                    "(null)", "LA");
                database.createPreparedResultSet(
                    "SELECT rsetitems.sysuserid \"__lockedby\" FROM worksheet, rsetitems WHERE rsetitems.rsetid = ? AND rsetitems.sdcid = ? AND rsetitems.keyid1 = worksheet.worksheetid AND rsetitems.keyid2 = worksheet.worksheetversionid",
                    new Object[]{rset.getRsetid(), "LV_Worksheet"});
                if (database.getNext()) {
                    lockedby = database.getValue("__lockedby");
                }

                das.clearRSet(rset);
            }

            commandResponse.set("lockedby", lockedby);
        } catch (Exception var11) {
            commandResponse.setStatusFail(
                "Failed to check if an operation can execute. Reason: " + var11.getMessage(),
                var11);
        }

    }

    private void startEditContent(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            String rsetid = "";
            DataSet item;
            if (commandRequest.getString("lockworksheet", "N").equals("N")) {
                DataAccessService das = new DataAccessService(this.sapphireConnection);
                RSet rset = das.createLockedRSet("LV_WorksheetItem", worksheetitemid,
                    worksheetitemversionid, "(null)", "LA");
                item = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT worksheetitem.*, rsetitems.lockstate \"__lockstate\", rsetitems.sysuserid \"__lockedby\" FROM worksheetitem, rsetitems WHERE rsetitems.rsetid = ? AND rsetitems.sdcid = ? AND rsetitems.keyid1 = worksheetitem.worksheetitemid AND rsetitems.keyid2 = worksheetitem.worksheetitemversionid",
                    new Object[]{rset.getRsetid(), "LV_WorksheetItem"}, true);
                rsetid = rset.getRsetid();
                commandResponse.set("lockedby", item.getValue(0, "__lockedby"));
            } else {
                item = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT worksheetitem.*, '0' \"__lockstate\", '' \"__lockedby\" FROM worksheetitem WHERE worksheetitem.worksheetitemid = ? AND worksheetitem.worksheetitemversionid = ?",
                    new Object[]{worksheetitemid, worksheetitemversionid}, true);
            }

            WorksheetItem worksheetItem = WorksheetItemFactory.getRenderingInstance(
                this.sapphireConnection, (HashMap) item.get(0), commandRequest.getString("width"));
            worksheetItem.setTemplate(commandRequest.getBoolean("template"));
            commandResponse.set("html", worksheetItem.getEditorHTML());
            commandResponse.set("contents", worksheetItem.getContents());
            commandResponse.set("options",
                worksheetItem.getWorksheetItemOptions().toPropertyList());
            commandResponse.set("rsetid",
                rsetid.length() > 0 ? rsetid + ";" + worksheetItem.getEditRSet()
                    : worksheetItem.getEditRSet());
        } catch (Exception var10) {
            commandResponse.setStatusFail(
                "Failed to start editing worksheet item. Reason: " + var10.getMessage(), var10);
        }

    }

    private void endEditContent(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            PropertyList actionProps = commandRequest.getStringPropertyList();
            actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            actionProps.setProperty("worksheetversionid",
                commandRequest.getString("worksheetversionid"));
            actionProps.setProperty("worksheetitemid", commandRequest.getString("worksheetitemid"));
            actionProps.setProperty("worksheetitemversionid",
                commandRequest.getString("worksheetitemversionid"));
            actionProps.setProperty("contents", commandRequest.getString("contents"));
            actionProps.setProperty("template", commandRequest.getBoolean("template") ? "Y" : "N");
            actionProps.setProperty("activitylog", commandRequest.getString("activitylog"));
            this.getActionProcessor()
                .processActionClass(SetWorksheetItemContent.class.getName(), actionProps);
            WorksheetItem worksheetItem = (WorksheetItem) actionProps.get("worksheetitem");
            if (commandRequest.getBoolean("complete")) {
                actionProps.setProperty("status", "Complete");
                actionProps.setProperty("htmlerror", "Y");
                this.getActionProcessor()
                    .processActionClass(SetWorksheetItemStatus.class.getName(), actionProps);
            }

            commandResponse.set("viewerhtml", worksheetItem.getViewHTML());
            String[] worksheetitem = worksheetItem.getDependentWorksheetItems();
            commandResponse.set("dependentworksheetitemid", worksheetitem[0]);
            commandResponse.set("dependentworksheetitemversionid", worksheetitem[1]);
            if (commandRequest.getBoolean("complete") && commandRequest.getBoolean(
                "refreshavailability")) {
                PropertyList loadProps = new PropertyList();
                loadProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
                loadProps.setProperty("worksheetversionid",
                    commandRequest.getString("worksheetversionid"));
                loadProps.setProperty("loadforavailability", "Y");
                this.getActionProcessor()
                    .processActionClass(LoadWorksheet.class.getName(), loadProps);
                SDIData worksheetData = (SDIData) loadProps.get("worksheet");
                commandResponse.set("worksheetsections",
                    worksheetData.getSDIData("sections").getDataset("primary"));
                commandResponse.set("worksheetitems",
                    worksheetData.getSDIData("items").getDataset("primary"));
            }

            DataAccessService das = new DataAccessService(this.sapphireConnection);
            String[] rsets = StringUtil.split(commandRequest.getString("rsetid"), ";");

            for (int i = 0; i < rsets.length; ++i) {
                if (rsets[i].length() > 0) {
                    das.clearRSet(new RSet(rsets[i]));
                }
            }
        } catch (SapphireException var10) {
            commandResponse.addErrorHandler(this.getActionProcessor().getErrorHandler());
        } catch (Exception var11) {
            commandResponse.setStatusFail(
                "Failed to end editing worksheet item. Reason: " + var11.getMessage(), var11);
        }

    }

    private void revertItemContent(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            int auditsequence = -1;

            try {
                auditsequence = Integer.parseInt(commandRequest.getString("auditsequence", "-1"));
            } catch (Exception var7) {
            }

            if (auditsequence >= 0) {
                DataSet item = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT contents FROM a_worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ? AND auditsequence = ?",
                    new Object[]{commandRequest.getString("worksheetitemid"),
                        commandRequest.getString("worksheetitemversionid"), auditsequence}, true);
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
                actionProps.setProperty("worksheetversionid",
                    commandRequest.getString("worksheetversionid"));
                actionProps.setProperty("worksheetitemid",
                    commandRequest.getString("worksheetitemid"));
                actionProps.setProperty("worksheetitemversionid",
                    commandRequest.getString("worksheetitemversionid"));
                actionProps.setProperty("contents", item.getValue(0, "contents"));
                actionProps.setProperty("template",
                    commandRequest.getBoolean("template") ? "Y" : "N");
                actionProps.setProperty("activitylog",
                    "Reverted back to audit-sequence " + auditsequence);
                this.getActionProcessor()
                    .processActionClass(SetWorksheetItemContent.class.getName(), actionProps);
            }
        } catch (SapphireException var8) {
            commandResponse.addErrorHandler(this.getActionProcessor().getErrorHandler());
        } catch (Exception var9) {
            commandResponse.setStatusFail(
                "Failed to revert worksheet item content. Reason: " + var9.getMessage(), var9);
        }

    }

    private void cancelEditContent(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            boolean initialedit = commandRequest.getBoolean("initialedit");
            if (initialedit) {
                this.deleteItem(commandRequest, commandResponse, database);
            } else {
                DataSet item = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT * FROM worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ?",
                    new Object[]{worksheetitemid, worksheetitemversionid}, true);
                WorksheetItem worksheetItem = WorksheetItemFactory.getRenderingInstance(
                    this.sapphireConnection, (HashMap) item.get(0),
                    commandRequest.getString("width"));
                commandResponse.set("viewerhtml", worksheetItem.getViewHTML());
            }

            DataAccessService das = new DataAccessService(this.sapphireConnection);
            String[] rsets = StringUtil.split(commandRequest.getString("rsetid"), ";");

            for (int i = 0; i < rsets.length; ++i) {
                if (rsets[i].length() > 0) {
                    das.clearRSet(new RSet(rsets[i]));
                }
            }
        } catch (Exception var10) {
            commandResponse.setStatusFail(
                "Failed to end editing worksheet item. Reason: " + var10.getMessage(), var10);
        }

    }

    private void deleteItem(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            RSet rset = das.createLockedRSet("LV_WorksheetItem", worksheetitemid,
                worksheetitemversionid, "(null)", "LA");
            DataSet item = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT rsetitems.lockstate \"__lockstate\", rsetitems.sysuserid \"__lockedby\" FROM worksheetitem, rsetitems WHERE rsetitems.rsetid = ? AND rsetitems.sdcid = ? AND rsetitems.keyid1 = worksheetitem.worksheetitemid AND rsetitems.keyid2 = worksheetitem.worksheetitemversionid",
                new Object[]{rset.getRsetid(), "LV_WorksheetItem"}, true);
            das.clearRSet(rset);
            if (item.getValue(0, "__lockedby").length() == 0) {
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("sdcid", "LV_WorksheetItem");
                actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
                actionProps.setProperty("worksheetversionid",
                    commandRequest.getString("worksheetversionid"));
                actionProps.setProperty("worksheetitemid", worksheetitemid);
                actionProps.setProperty("worksheetitemversionid", worksheetitemversionid);
                this.getActionProcessor()
                    .processActionClass(DeleteWorksheetItem.class.getName(), actionProps);
            } else {
                commandResponse.set("lockedby", item.getValue(0, "__lockedby"));
            }
        } catch (Exception var10) {
            commandResponse.setStatusFail(
                "Failed to delete worksheet item. Reason: " + var10.getMessage(), var10);
        }

    }

    private void loadItemDetails(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetsectionid = commandRequest.getString("worksheetsectionid");
            String worksheetsectionversionid = commandRequest.getString(
                "worksheetsectionversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            boolean template = commandRequest.getBoolean("template");
            boolean loadAttributes = false;
            boolean loadAttachments = false;
            DataSet itemdata = null;
            DataSet attributedata = null;
            DataSet attachmentdata = null;
            String loadsdcid;
            String loadkeyid1;
            String loadkeyid2;
            PropertyList options;
            if (worksheetitemid.length() > 0) {
                options = null;
                itemdata = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT worksheetitem.*, worksheetsection.options \"wssoptions\", ( SELECT worksheetname FROM worksheet template WHERE template.worksheetid = worksheetitem.templateid AND template.worksheetversionid = worksheetitem.templateversionid ) \"templatename\" FROM worksheetitem, worksheetsection WHERE worksheetitem.worksheetsectionid = worksheetsection.worksheetsectionid AND worksheetitemid = ? AND worksheetitemversionid = ?",
                    new Object[]{worksheetitemid, worksheetitemversionid}, true);
                WorksheetItem worksheetItem = WorksheetItemFactory.getInstance(
                    this.sapphireConnection, database, (HashMap) itemdata.get(0));
                options = worksheetItem.getWorksheetItemOptions().toPropertyList();
                itemdata.setString(0, "elementid", worksheetItem.getElementId());
                commandResponse.set("options", options);
                commandResponse.set("metadata", this.getMetaData(worksheetItem.getMetaData()));
                itemdata.setValue(0, "options", "");
                itemdata.setValue(0, "config", "");
                itemdata.setValue(0, "html", "");
                PropertyList wssOptions = new PropertyList();
                wssOptions.setPropertyList(itemdata.getClob(0, "wssoptions", ""));
                commandResponse.set("wssoptions", wssOptions);
                loadAttributes = template
                    || options.getProperty("allowitemattributes", "Y").equals("Y")
                    && options.getProperty("disableitemattributes", "N").equals("N");
                loadAttachments = template
                    || options.getProperty("allowitemattachments", "Y").equals("Y")
                    && options.getProperty("disableitemattachments", "N").equals("N");
                loadsdcid = "LV_WorksheetItem";
                loadkeyid1 = worksheetitemid;
                loadkeyid2 = worksheetitemversionid;
                DataSet params = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT * FROM worksheetitemparam WHERE worksheetitemid = ? AND worksheetitemversionid = ?",
                    new Object[]{worksheetitemid, worksheetitemversionid});
                commandResponse.set("params", params);
                StringBuffer editorstyleid = new StringBuffer();
                int i = 0;

                while (true) {
                    if (i >= params.size()) {
                        if (editorstyleid.length() > 0) {
                            commandResponse.set("parameditorstyles",
                                this.getEditorStyles(editorstyleid.substring(1)));
                        }
                        break;
                    }

                    editorstyleid.append(";").append(params.getValue(i, "parameditorstyleid"));
                    ++i;
                }
            } else if (worksheetsectionid.length() > 0) {
                itemdata = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT worksheetsection.*, ( SELECT worksheetname FROM worksheet template WHERE template.worksheetid = worksheetsection.templateid AND template.worksheetversionid = worksheetsection.templateversionid ) \"templatename\" FROM worksheetsection WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?",
                    new Object[]{worksheetsectionid, worksheetsectionversionid}, true);
                options = new PropertyList();
                options.setPropertyList(itemdata.getClob(0, "options", ""));
                commandResponse.set("options", options);
                itemdata.setValue(0, "options", "");
                loadAttributes =
                    template || options.getProperty("allowsectionattributes", "Y").equals("Y");
                loadAttachments =
                    template || options.getProperty("allowsectionattachments", "Y").equals("Y");
                loadsdcid = "LV_WorksheetSection";
                loadkeyid1 = worksheetsectionid;
                loadkeyid2 = worksheetsectionversionid;
            } else {
                itemdata = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT worksheet.*,  ( SELECT worksheetname FROM worksheet template WHERE template.worksheetid = worksheet.templateid AND template.worksheetversionid = worksheet.templateversionid ) \"templatename\" FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ?",
                    new Object[]{worksheetid, worksheetversionid}, true);
                options = new PropertyList();
                options.setPropertyList(itemdata.getClob(0, "options", ""));
                options.setProperty("lesonly", itemdata.getValue(0, "lesflag"));
                options.setProperty("blockflag", itemdata.getValue(0, "blockflag"));
                options.setProperty("blocksdcid", itemdata.getValue(0, "blocksdcid"));
                commandResponse.set("options", options);
                itemdata.setValue(0, "options", "");
                loadAttributes =
                    template || options.getProperty("allowworksheetattributes", "Y").equals("Y");
                loadAttachments =
                    template || options.getProperty("allowworksheetattachments", "Y").equals("Y");
                loadsdcid = "LV_Worksheet";
                loadkeyid1 = worksheetid;
                loadkeyid2 = worksheetversionid;
            }

            if (loadAttributes) {
                commandResponse.set("allowattributes", "Y");
                attributedata = this.loadAttributes(loadsdcid, loadkeyid1, loadkeyid2);
                JSONableMap editorStyles = this.getEditorStyles(
                    attributedata.getColumnValues("editorstyleid", ";"));
                commandResponse.set("attributeeditorstyles", editorStyles);
                this.preprocessAttributes(attributedata, editorStyles);
            }

            if (loadAttachments) {
                commandResponse.set("allowattachments", "Y");
                attachmentdata = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT attachmentdesc, attachmentnum FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND ( attachmentuse IS NULL OR attachmentuse <> 'HTMLEditor' ) ORDER BY attachmentnum",
                    new Object[]{loadsdcid, loadkeyid1, loadkeyid2});
            }

            commandResponse.set("itemdata", itemdata != null ? itemdata : new DataSet());
            commandResponse.set("attributedata",
                attributedata != null ? attributedata : new DataSet());
            commandResponse.set("attachmentdata",
                attachmentdata != null ? attachmentdata : new DataSet());
        } catch (Exception var25) {
            commandResponse.setStatusFail(
                "Failed to load worksheet item data. Reason: " + var25.getMessage(), var25);
        }

    }

    private void preprocessAttributes(DataSet attributedata, JSONableMap editorStyles) {
        EditorStyleUtil esu = new EditorStyleUtil(this.connectionInfo.getConnectionId());
        if (attributedata != null && attributedata.size() > 0 && editorStyles != null) {
            for (int i = 0; i < attributedata.size(); ++i) {
                String editorstyleid = attributedata.getValue(i, "editorstyleid");
                String displayvalue = null;
                if (editorstyleid.length() > 0) {
                    PropertyList def = (PropertyList) editorStyles.get(editorstyleid);
                    if (def != null) {
                        String datatype = attributedata.getValue(i, "datatype");
                        if (datatype.equals("S")) {
                            String value = attributedata.getValue(i, "textvalue");
                            displayvalue = esu.getStringDisplayValue(value, def);
                        }
                    }
                }

                if (displayvalue != null && displayvalue.length() > 0) {
                    attributedata.setString(i, "_converteddisplayvalue", displayvalue);
                }
            }
        }

    }

    private DataSet loadAttributes(String sdcid, String keyid1, String keyid2) {
        DataSet attributes = this.getQueryProcessor().getPreparedSqlDataSet(
            "SELECT sdiattribute.attributeid, sdiattribute.attributeinstance, sdiattribute.attributesdcid, sdiattribute.editorstyleid, sdiattribute.datatype, sdiattribute.textvalue, sdiattribute.numericvalue, sdiattribute.datevalue, sdiattribute.clobvalue, sdiattribute.defaulttextvalue, sdiattribute.defaultnumericvalue, sdiattribute.defaultdatevalue, sdiattribute.mandatoryflag, sdiattribute.updateableflag, sdcattributedef.attributetitle FROM sdiattribute, sdcattributedef WHERE sdiattribute.sdcid = sdcattributedef.sdcid AND sdiattribute.attributeid = sdcattributedef.attributeid AND sdiattribute.sdcid = ? AND sdiattribute.keyid1 = ? AND sdiattribute.keyid2 = ?",
            new Object[]{sdcid, keyid1, keyid2}, true);
        this.formatAttributes(attributes);
        return attributes;
    }

    private void formatAttributes(DataSet attributes) {
        M18NUtil m18NUtil = new M18NUtil(this.sapphireConnection);
        attributes.addColumn("displayvalue", 0);
        attributes.addColumn("defaultdisplayvalue", 0);

        for (int i = 0; i < attributes.size(); ++i) {
            DateFormat df;
            if (attributes.getValue(i, "datatype").equals("D")) {
                df = m18NUtil.getDefaultDateFormat();
                attributes.setValue(i, "displayvalue",
                    attributes.getCalendar(i, "datevalue") != null ? df.format(
                        attributes.getCalendar(i, "datevalue").getTime()) : "");
                attributes.setValue(i, "defaultdisplayvalue",
                    attributes.getCalendar(i, "defaultdatevalue") != null ? df.format(
                        attributes.getCalendar(i, "defaultdatevalue").getTime()) : "");
            } else if (attributes.getValue(i, "datatype").equals("O")) {
                df = m18NUtil.getDefaultDateOnlyFormat();
                attributes.setValue(i, "displayvalue",
                    attributes.getCalendar(i, "datevalue") != null ? df.format(
                        attributes.getCalendar(i, "datevalue").getTime()) : "");
                attributes.setValue(i, "defaultdisplayvalue",
                    attributes.getCalendar(i, "defaultdatevalue") != null ? df.format(
                        attributes.getCalendar(i, "defaultdatevalue").getTime()) : "");
            } else if (attributes.getValue(i, "datatype").equals("N")) {
                attributes.setValue(i, "displayvalue", attributes.getValue(i, "numericvalue"));
                attributes.setValue(i, "defaultdisplayvalue",
                    attributes.getValue(i, "defaultnumericvalue"));
            } else {
                attributes.setValue(i, "displayvalue", attributes.getValue(i, "textvalue"));
                attributes.setValue(i, "defaultdisplayvalue",
                    attributes.getValue(i, "defaulttextvalue"));
            }
        }

    }

    private void loadItemAudit(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            SafeSQL safeSQL = new SafeSQL();
            DataSet auditdata = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT wal.activitydt, sysuser.sysuserdesc, wal.targetauditseq   FROM worksheetactivitylog wal LEFT OUTER JOIN sysuser ON sysuser.sysuserid=activityby  WHERE activitytype in ( 'SetContent', 'SetConfig' ) AND wal.worksheetid="
                    + safeSQL.addVar(worksheetid) + " AND worksheetversionid=" + safeSQL.addVar(
                    worksheetversionid) + " AND targetsdcid=" + safeSQL.addVar("LV_WorksheetItem")
                    + " AND targetkeyid1=" + safeSQL.addVar(worksheetitemid) + " AND targetkeyid2="
                    + safeSQL.addVar(worksheetitemversionid) + " ORDER BY wal.targetauditseq desc",
                safeSQL.getValues());
            commandResponse.set("auditdata", auditdata != null ? auditdata : new DataSet());
        } catch (Exception var10) {
            commandResponse.setStatusFail(
                "Failed to load worksheet item audit data. Reason: " + var10.getMessage(), var10);
        }

    }

    private DataSet getMetaData(LinkedHashMap<String, String> metadata) {
        DataSet dataSet = new DataSet();
        if (metadata != null) {
            Iterator<String> iterator = metadata.keySet().iterator();

            while (iterator.hasNext()) {
                String title = (String) iterator.next();
                int row = dataSet.addRow();
                dataSet.setString(row, "title", title);
                dataSet.setString(row, "value", (String) metadata.get(title));
            }
        }

        return dataSet;
    }

    private void loadSDIWorksheetItem(CommandRequest commandRequest,
        CommandResponse commandResponse, DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String sdcid = commandRequest.getString("sdcid");
            String keyid1 = commandRequest.getString("keyid1");
            String keyid2 = commandRequest.getString("keyid2");
            String keyid3 = commandRequest.getString("keyid3");
            DataSet worksheetItems = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT worksheetitem.worksheetitemid, worksheetitem.worksheetitemversionid FROM worksheetitem, worksheetitemsdi WHERE worksheetitem.worksheetitemid = worksheetitemsdi.worksheetitemid AND worksheetitem.worksheetitemversionid = worksheetitemsdi.worksheetitemversionid   AND worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ?   AND sdcid = ? AND keyid1 = ?"
                    + (keyid2.length() > 0 ? " AND keyid2 = ?" : "") + (keyid3.length() > 0
                    ? " AND keyid3 = ?" : ""),
                keyid3.length() > 0 ? new Object[]{worksheetid, worksheetversionid, sdcid, keyid1,
                    keyid2, keyid3}
                    : (keyid2.length() > 0 ? new Object[]{worksheetid, worksheetversionid, sdcid,
                        keyid1, keyid2}
                        : new Object[]{worksheetid, worksheetversionid, sdcid, keyid1}));
            commandResponse.set("sdiworksheetitems", worksheetItems);
        } catch (Exception var11) {
            commandResponse.setStatusFail(
                "Failed to load worksheet items for SDI. Reason: " + var11.getMessage(), var11);
        }

    }

    private void loadItemSDIs(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            String sdcid = commandRequest.getString("sdcid");
            PropertyList operation = commandRequest.getPropertyList("operation");
            String sdcOperation = operation.getProperty("sdcoperation");
            String keyid1 = commandRequest.getString("keyid1");
            String keyid2 = commandRequest.getString("keyid2");
            String keyid3 = commandRequest.getString("keyid3");
            PropertyList sdcProps = commandRequest.getPropertyList("sdcprops");
            PropertyList filter = operation.getPropertyListNotNull("sdifilter");
            DataSet sdis =
                worksheetid.length() > 0 ? this.loadWorksheetSDIs(worksheetid, worksheetversionid,
                    sdcid, sdcProps, filter, keyid1, keyid2, keyid3)
                    : this.loadItemSDIs(worksheetitemid, worksheetitemversionid, sdcid, sdcProps,
                        filter, keyid1, keyid2, keyid3);
            if (sdcOperation.length() > 0 && sdis.size() > 0 && !this.hasSecurityAccess(sdcid, sdis,
                sdcOperation)) {
                commandResponse.setStatusFail(
                    "This operation is not permitted on the " + this.getSDCProcessor()
                        .getProperty(sdcid, "plural") + " in this control");
            }

            commandResponse.set("worksheetitemsdis", sdis);
            commandResponse.set("plural", this.getSDCProcessor().getProperty(sdcid, "plural"));
        } catch (Exception var17) {
            commandResponse.setStatusFail(
                "Failed to load worksheet item SDIs. Reason: " + var17.getMessage(), var17);
        }

    }

    private boolean hasSecurityAccess(String sdcid, DataSet sdis, String sdcOperation)
        throws SapphireException {
        boolean hasAccess = true;

        try {
            String securityMode = this.getSDCProcessor().getProperty(sdcid, "accesscontrolledflag");
            int keyCount = Integer.parseInt(
                this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
            PropertyList pl = new PropertyList();
            pl.setProperty("sdcid", sdcid);
            pl.setProperty("operation", sdcOperation);
            pl.setProperty("keyid1", sdis.getColumnValues("keyid1", ";"));
            pl.setProperty("keyid2", keyCount > 1 ? sdis.getColumnValues("keyid2", ";") : "");
            pl.setProperty("keyid3", keyCount > 2 ? sdis.getColumnValues("keyid3", ";") : "");
            boolean check = false;
            if (!"D".equalsIgnoreCase(securityMode) && !"SDIWorkItem".equalsIgnoreCase(sdcid)
                && !"B".equalsIgnoreCase(securityMode)) {
                if ("S".equalsIgnoreCase(securityMode)) {
                    this.getActionProcessor()
                        .processActionClass("com.labvantage.sapphire.actions.ddt.SDISecurityCheck",
                            pl);
                    check = true;
                }
            } else {
                this.getActionProcessor().processActionClass(
                    "com.labvantage.sapphire.actions.ddt.DepartmentalSecurityCheck", pl);
                check = true;
            }

            if (check) {
                String outoperation = pl.getProperty("operation");
                String failedsdis = pl.getProperty("failedsdis");
                String passedsdis = pl.getProperty("passedsdis");
                if (failedsdis.length() > 0) {
                    hasAccess = false;
                }

                this.logDebug("outoperation = " + outoperation);
                this.logDebug("failedsdis = " + failedsdis);
                this.logDebug("passedsdis = " + passedsdis);
            }

            return hasAccess;
        } catch (Exception var12) {
            throw new SapphireException(
                "Failed to check SDC Operation. Reason: " + ErrorUtil.extractMessageFromException(
                    var12, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), var12);
        }
    }

    private void addSDI(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            String source = commandRequest.getString("source");
            SDIList sdiList = commandRequest.getSDIList("sdilist");
            if (commandRequest.getBoolean("connectortypeassdc")) {
                database.createPreparedResultSet(
                    "SELECT connectortypesdcid FROM connectortype WHERE connectortypeid = ?",
                    new Object[]{sdiList.getSdcid()});
                if (!database.getNext()) {
                    throw new SapphireException(
                        "Failed to resolve SDC from connectortypeid '" + sdiList.getSdcid()
                            + "' when adding SDIs to worksheet " + BaseELNAction.getIdVersionText(
                            worksheetid, worksheetversionid));
                }

                sdiList.setSdcid(database.getValue("connectortypesdcid"));
            }

            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            actionProps.setProperty("worksheetversionid",
                commandRequest.getString("worksheetversionid"));
            actionProps.setProperty("sdcid", sdiList.getSdcid());
            actionProps.setProperty("keyid1", sdiList.getKeyid1());
            actionProps.setProperty("keyid2", sdiList.getKeyid2());
            actionProps.setProperty("keyid3", sdiList.getKeyid3());
            if (source.equalsIgnoreCase("Control")) {
                actionProps.setProperty("worksheetitemid", worksheetitemid);
                actionProps.setProperty("worksheetitemversionid", worksheetitemversionid);
                this.getActionProcessor()
                    .processActionClass(AddWorksheetItemSDI.class.getName(), actionProps);
            } else {
                this.getActionProcessor()
                    .processActionClass(AddWorksheetSDI.class.getName(), actionProps);
            }

            DataSet dependents = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT DISTINCT worksheetitemid, worksheetitemversionid FROM worksheetitemparam WHERE valuesdcid = ? AND valuekeyid1 = ? AND valuekeyid2 = ? AND valuetype = 'sdilist' AND valuelabel = 'keyid1'",
                new Object[]{"LV_WorksheetItem", worksheetitemid, worksheetitemversionid});
            if (dependents.size() > 0) {
                commandResponse.set("dependentworksheetitemid",
                    dependents.getColumnValues("worksheetitemid", ";"));
                commandResponse.set("dependentworksheetitemversionid",
                    dependents.getColumnValues("worksheetitemversionid", ";"));
            }
        } catch (Exception var12) {
            commandResponse.setStatusFail(
                "Failed to add worksheet item SDIs. Reason: " + var12.getMessage(), var12);
        }

    }

    private void deleteItemSDI(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            SDIList sdiList = commandRequest.getSDIList("sdilist");
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            actionProps.setProperty("worksheetversionid",
                commandRequest.getString("worksheetversionid"));
            actionProps.setProperty("worksheetitemid", worksheetitemid);
            actionProps.setProperty("worksheetitemversionid", worksheetitemversionid);
            if (sdiList != null && sdiList.getSdcid().length() > 0) {
                actionProps.setProperty("sdcid", sdiList.getSdcid());
                if (sdiList.size() > 0) {
                    actionProps.setProperty("keyid1", sdiList.getKeyid1());
                    actionProps.setProperty("keyid2", sdiList.getKeyid2());
                    actionProps.setProperty("keyid3", sdiList.getKeyid3());
                }
            }

            this.getActionProcessor()
                .processActionClass(DeleteWorksheetItemSDI.class.getName(), actionProps);
            commandResponse.set("worksheetitemid", actionProps.getProperty("worksheetitemid"));
            commandResponse.set("worksheetitemversionid",
                actionProps.getProperty("worksheetitemversionid"));
            DataSet dependents = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT DISTINCT worksheetitemid, worksheetitemversionid FROM worksheetitemparam WHERE valuesdcid = ? AND valuekeyid1 = ? AND valuekeyid2 = ? AND valuetype = 'sdilist' AND valuelabel = 'keyid1'",
                new Object[]{"LV_WorksheetItem", worksheetitemid, worksheetitemversionid});
            if (dependents.size() > 0) {
                commandResponse.set("dependentworksheetitemid",
                    dependents.getColumnValues("worksheetitemid", ";"));
                commandResponse.set("dependentworksheetitemversionid",
                    dependents.getColumnValues("worksheetitemversionid", ";"));
            }
        } catch (Exception var9) {
            commandResponse.setStatusFail(
                "Failed to delete worksheet item SDIs. Reason: " + var9.getMessage(), var9);
        }

    }

    private void addItemRef(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            this.getActionProcessor().processActionClass(AddWorksheetItemRef.class.getName(),
                commandRequest.getStringPropertyList());
        } catch (Exception var5) {
            commandResponse.setStatusFail(
                "Failed to add worksheet item reference. Reason: " + var5.getMessage(), var5);
        }

    }

    private void loadAddOptions(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String workbookid = commandRequest.getString("workbookid");
            String workbookversionid = commandRequest.getString("workbookversionid");
            String templatetype = commandRequest.getString("templatetype");
            String propertytreeid = commandRequest.getString("propertytreeid");
            String templatecategoryid = commandRequest.getString("templatecategoryid");
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            int limit = 10;
            boolean workbookTemplates = false;
            DataSet templates;
            String mruPropertyId;
            if (templatetype.equals("C")) {
                templates = new DataSet();
                mruPropertyId =
                    commandRequest.getString("__hostwebpageid") + "_recent_copy_worksheets";
            } else {
                if (workbookTemplates) {
                    templates = this.loadWorkbookTemplates(workbookid, workbookversionid,
                        templatetype, propertytreeid);
                } else if (templatetype.equals("I")) {
                    templates = this.getQueryProcessor().getPreparedSqlDataSet(database.isOracle() ?
                            "SELECT worksheetid, worksheetversionid, worksheetname, templateprivacyflag  FROM ( SELECT worksheet.worksheetid, worksheet.worksheetversionid, worksheet.worksheetname, worksheet.templateprivacyflag FROM worksheet, worksheetitem  WHERE worksheet.worksheetid = worksheetitem.worksheetid AND worksheet.worksheetversionid = worksheetitem.worksheetversionid AND templatetypeflag = ? AND propertytreeid = ? AND "
                                + (templatecategoryid.length() > 0 ?
                                "worksheet.worksheetid IN (SELECT categoryitem.keyid1 FROM categoryitem WHERE categoryitem.sdcid='LV_Worksheet' AND categoryitem.categoryid='"
                                    + templatecategoryid + "') AND " : "")
                                + "( ( templateprivacyflag = 'G' AND worksheet.versionstatus IN ("
                                + this.getGlobalVersionStatusList()
                                + ") ) OR templateprivacyflag IS NULL OR ( templateprivacyflag = 'O' AND worksheet.versionstatus IN ('P', 'A', 'C') AND authorid = ? ) )  ORDER BY 3)  WHERE rownum < "
                                + (limit + 2) : "SELECT TOP " + (limit + 1)
                            + " worksheet.worksheetid, worksheet.worksheetversionid, worksheet.worksheetname, worksheet.templateprivacyflag  FROM worksheet, worksheetitem  WHERE worksheet.worksheetid = worksheetitem.worksheetid AND worksheet.worksheetversionid = worksheetitem.worksheetversionid AND templatetypeflag = ? AND propertytreeid = ? AND "
                            + (templatecategoryid.length() > 0 ?
                            "worksheet.worksheetid IN (SELECT categoryitem.keyid1 FROM categoryitem WHERE categoryitem.sdcid='LV_Worksheet' AND categoryitem.categoryid='"
                                + templatecategoryid + "') AND " : "")
                            + "( ( templateprivacyflag = 'G' AND worksheet.versionstatus IN ("
                            + this.getGlobalVersionStatusList()
                            + ") ) OR templateprivacyflag IS NULL OR ( templateprivacyflag = 'O' AND worksheet.versionstatus IN ('P', 'A', 'C') AND authorid = ? ) )  ORDER BY 3",
                        new Object[]{templatetype, propertytreeid,
                            this.sapphireConnection.getSysuserId()});
                } else if (templatetype.equals("S")) {
                    templates = this.getQueryProcessor().getPreparedSqlDataSet(database.isOracle() ?
                            "SELECT worksheetid, worksheetversionid, worksheetname, templateprivacyflag FROM  (SELECT worksheetid, worksheetversionid, worksheetname,templateprivacyflag  FROM worksheet WHERE templatetypeflag = ? AND  ( ( templateprivacyflag = 'G' AND worksheet.versionstatus IN ("
                                + this.getGlobalVersionStatusList()
                                + ") ) OR templateprivacyflag IS NULL OR ( templateprivacyflag = 'O' AND worksheet.versionstatus IN ('P', 'A', 'C') AND authorid = ? ) )  ORDER BY 3)  WHERE rownum < "
                                + (limit + 2) : "SELECT TOP " + (limit + 1)
                            + " worksheetid, worksheetversionid, worksheetname, templateprivacyflag FROM worksheet WHERE templatetypeflag = ? AND  ( ( templateprivacyflag = 'G' AND worksheet.versionstatus IN ( "
                            + this.getGlobalVersionStatusList()
                            + ") ) OR templateprivacyflag IS NULL OR ( templateprivacyflag = 'O' AND worksheet.versionstatus IN ('P', 'A', 'C') AND authorid = ? ) )  ORDER BY 3",
                        new Object[]{templatetype, this.sapphireConnection.getSysuserId()});
                } else {
                    templates = new DataSet();
                }

                mruPropertyId = commandRequest.getString("__hostwebpageid") + "_recent_" + (
                    templatetype.equals("S") ? "section" : propertytreeid) + "_templates";
            }

            DataSet recents = this.loadMRUList(mruPropertyId, "", 5);
            commandResponse.set("recentitems", recents);
            commandResponse.set("templates", templates);
            if (templates.size() > limit) {
                commandResponse.set("moretemplates", "Y");
                templates.deleteRow(templates.size() - 1);
            }
        } catch (Exception var16) {
            commandResponse.setStatusFail(
                "Failed to load worksheet item add options. Reason: " + var16.getMessage(), var16);
        }

    }

    private void loadReferences(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            DataSet externalrefs = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT worksheet.worksheetid, worksheet.worksheetversionid, worksheet.worksheetname, refsdcid, refkeyid1, refkeyid2, reffunction, worksheetitemreference.createby, worksheetitemreference.createdt FROM worksheet, worksheetitem, worksheetitemreference WHERE worksheetitemreference.worksheetitemid = worksheetitem.worksheetitemid AND worksheetitemreference.worksheetitemversionid = worksheetitem.worksheetitemversionid AND       worksheetitem.worksheetid = worksheet.worksheetid AND worksheetitem.worksheetversionid = worksheet.worksheetversionid AND       refworksheetid = ? AND refworksheetversionid = ? ORDER BY worksheetitemreference.createdt",
                new Object[]{worksheetid, worksheetversionid}, true);
            commandResponse.set("externalrefs", externalrefs);
            DataSet internalrefs = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT worksheetitem.worksheetitemid, worksheetitem.worksheetitemversionid, refworksheetid, refworksheetversionid, worksheet.worksheetname, refsdcid, refkeyid1, refkeyid2, reffunction, worksheetitemreference.createby, worksheetitemreference.createdt FROM worksheet, worksheetitem, worksheetitemreference WHERE worksheetitemreference.worksheetitemid = worksheetitem.worksheetitemid AND worksheetitemreference.worksheetitemversionid = worksheetitem.worksheetitemversionid AND       worksheetitemreference.refworksheetid = worksheet.worksheetid AND worksheetitemreference.refworksheetversionid = worksheet.worksheetversionid AND       worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ? ORDER BY worksheetitemreference.createdt",
                new Object[]{worksheetid, worksheetversionid}, true);
            commandResponse.set("internalrefs", internalrefs);
        } catch (Exception var8) {
            commandResponse.setStatusFail(
                "Failed to load worksheet item references. Reason: " + var8.getMessage(), var8);
        }

    }

    private void loadActivityLog(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            DateTimeUtil dtu = new DateTimeUtil();
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String targetsdcid = commandRequest.getString("targetsdcid", "");
            String targetkeyid1 = commandRequest.getString("targetkeyid1", "");
            String targetkeyid2 = commandRequest.getString("targetkeyid2", "1");
            ArrayList bindVars = new ArrayList();
            bindVars.add(worksheetid);
            bindVars.add(worksheetversionid);
            String extraSelect = "";
            String extraFrom = "";
            if (targetsdcid.length() > 0 && targetkeyid1.length() > 0
                && targetkeyid2.length() > 0) {
                if (targetsdcid.equals("LV_Worksheet")) {
                    extraSelect = ",tracelog.reason";
                    extraFrom = " LEFT OUTER JOIN a_worksheet on activitytype='SetStatus' AND a_worksheet.worksheetid=targetkeyid1 AND a_worksheet.worksheetversionid=targetkeyid2 AND a_worksheet.auditsequence=targetauditseq LEFT OUTER JOIN tracelog ON a_worksheet.tracelogid=tracelog.tracelogid ";
                } else if (targetsdcid.equals("LV_WorksheetSection")) {
                    extraSelect = ",tracelog.reason";
                    extraFrom = " LEFT OUTER JOIN a_worksheetsection on activitytype='SetStatus' AND a_worksheetsection.worksheetsectionid=targetkeyid1 AND a_worksheetsection.worksheetsectionversionid=targetkeyid2 AND a_worksheetsection.auditsequence=targetauditseq LEFT OUTER JOIN tracelog ON a_worksheetsection.tracelogid=tracelog.tracelogid ";
                } else {
                    extraSelect = ",tracelog.reason";
                    extraFrom = " LEFT OUTER JOIN a_worksheetitem on activitytype='SetStatus' AND a_worksheetitem.worksheetitemid=targetkeyid1 AND a_worksheetitem.worksheetitemversionid=targetkeyid2 AND a_worksheetitem.auditsequence=targetauditseq LEFT OUTER JOIN tracelog ON a_worksheetitem.tracelogid=tracelog.tracelogid ";
                }
            }

            StringBuffer sql = new StringBuffer(
                "SELECT worksheetactivitylog.activityby, worksheetactivitylog.activitydt, worksheetactivitylog.activitytype, sysuser.sysuserdesc, CASE WHEN targetsdcid = 'LV_Worksheet' THEN 'Worksheet' WHEN targetsdcid = 'LV_WorksheetSection' THEN 'Section' WHEN targetsdcid = 'LV_WorksheetItem' THEN 'Control' ELSE 'Unknown' END AS targetsdcid, CASE WHEN targetsdcid = 'LV_Worksheet' THEN     (SELECT worksheetname FROM worksheet WHERE worksheetid = targetkeyid1 AND worksheetversionid=targetkeyid2) WHEN targetsdcid = 'LV_WorksheetSection' THEN     (SELECT worksheetsectiondesc FROM worksheetsection WHERE worksheetsectionid = targetkeyid1 AND worksheetsectionversionid=targetkeyid2) WHEN targetsdcid = 'LV_WorksheetItem' THEN     (SELECT Coalesce (NullIf (worksheetitemdesc,''),propertytreeid) FROM worksheetitem WHERE worksheetitemid = targetkeyid1 AND worksheetitemversionid=targetkeyid2)   ELSE 'Unknown' END AS targetname, worksheetactivitylog.activitylog "
                    + extraSelect
                    + " FROM worksheetactivitylog LEFT OUTER JOIN sysuser ON sysuser.sysuserid=activityby "
                    + extraFrom);
            sql.append(
                " WHERE worksheetactivitylog.worksheetid = ? AND worksheetactivitylog.worksheetversionid = ?");
            if (commandRequest.contains("activityby")
                && commandRequest.getString("activityby").length() > 0) {
                sql.append(" AND worksheetactivitylog.activityby = ? ");
                bindVars.add(commandRequest.getString("activityby"));
            }

            if (commandRequest.contains("activitytype")
                && commandRequest.getString("activitytype").length() > 0) {
                sql.append(" AND worksheetactivitylog.activitytype = ? ");
                bindVars.add(commandRequest.getString("activitytype"));
            }

            if (commandRequest.contains("activitydt_start")
                && commandRequest.getString("activitydt_start").length() > 0) {
                sql.append(" AND worksheetactivitylog.activitydt >= ? ");
                bindVars.add(dtu.getTimestamp(commandRequest.getString("activitydt_start")));
            }

            if (commandRequest.contains("activitydt_end")
                && commandRequest.getString("activitydt_end").length() > 0) {
                sql.append(" AND worksheetactivitylog.activitydt <= ? ");
                bindVars.add(dtu.getTimestamp(commandRequest.getString("activitydt_end")));
            }

            if (targetsdcid.length() > 0) {
                sql.append(" AND targetsdcid = ? ");
                bindVars.add(targetsdcid);
            }

            if (targetkeyid1.length() > 0) {
                sql.append(" AND targetkeyid1 = ? ");
                bindVars.add(targetkeyid1);
            }

            if (targetkeyid2.length() > 0) {
                sql.append(" AND targetkeyid2 = ? ");
                bindVars.add(targetkeyid2);
            }

            DataSet log = this.getQueryProcessor().getPreparedSqlDataSet(sql
                    + " ORDER BY worksheetactivitylog.activitydt DESC, worksheetactivitylog.activitylogid DESC ",
                bindVars.toArray(new Object[bindVars.size()]), true);
            commandResponse.set("activitylog", log);
            if (targetsdcid.equals("LV_Worksheet")) {
                commandResponse.set("istemplate", this.getQueryProcessor().getPreparedCount(
                    "SELECT count(*) FROM worksheet WHERE worksheetid=? AND worksheetversionid=? AND templateflag='Y'",
                    new String[]{targetkeyid1, targetkeyid2}) > 0 ? "Y" : "N");
            }
        } catch (Exception var15) {
            commandResponse.setStatusFail(
                "Failed to load worksheet item config. Reason: " + var15.getMessage(), var15);
        }

    }

    private void logActivity(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            PropertyList activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            activityProps.setProperty("worksheetversionid",
                commandRequest.getString("worksheetversionid"));
            activityProps.setProperty("targetsdcid", commandRequest.getString("targetsdcid"));
            activityProps.setProperty("targetkeyid1", commandRequest.getString("targetkeyid1"));
            activityProps.setProperty("targetkeyid2", commandRequest.getString("targetkeyid2"));
            activityProps.setProperty("activitytype", commandRequest.getString("activitytype"));
            activityProps.setProperty("activitylog", commandRequest.getString("activitylog"));
            this.getActionProcessor()
                .processActionClass(AddWorksheetActivity.class.getName(), activityProps);
        } catch (Exception var5) {
            commandResponse.setStatusFail("Failed to log activity. Reason: " + var5.getMessage(),
                var5);
        }

    }

    private void loadConfig(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            DataSet item = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT config, propertytreeid FROM worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ?",
                new Object[]{worksheetitemid, worksheetitemversionid}, true);
            PropertyList config = new PropertyList();
            config.setPropertyList(item.getClob(0, "config"));
            commandResponse.set("config", config);
        } catch (Exception var8) {
            commandResponse.setStatusFail(
                "Failed to load worksheet item config. Reason: " + var8.getMessage(), var8);
        }

    }

    private void saveConfig(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            String propertyid = commandRequest.getString("propertyid");
            String propertyvalue = this.unescapeChars(commandRequest.getString("propertyvalue"));
            PropertyList config = commandRequest.getPropertyList("config");
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            actionProps.setProperty("worksheetversionid",
                commandRequest.getString("worksheetversionid"));
            actionProps.setProperty("templateflag", commandRequest.getString("templateflag"));
            actionProps.setProperty("worksheetitemid", worksheetitemid);
            actionProps.setProperty("worksheetitemversionid", worksheetitemversionid);
            actionProps.setProperty("propertyid", propertyid);
            actionProps.setProperty("propertyvalue", propertyvalue);
            actionProps.setProperty("config", config != null ? config.toXMLString() : "");
            this.getActionProcessor()
                .processActionClass(SetWorksheetItemConfig.class.getName(), actionProps);
            if (commandRequest.getBoolean("loadviewer")) {
                WorksheetItem worksheetItem = (WorksheetItem) actionProps.get("worksheetitem");
                commandResponse.set("viewerhtml", worksheetItem.getViewHTML());
            }
        } catch (Exception var11) {
            commandResponse.setStatusFail(
                "Failed to save worksheet item config. Reason: " + var11.getMessage(), var11);
        }

    }

    private void loadOptions(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetsectionid = commandRequest.getString("worksheetsectionid");
            String worksheetsectionversionid = commandRequest.getString(
                "worksheetsectionversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            PropertyList options = new PropertyList();
            DataSet data;
            if (worksheetitemid.length() > 0) {
                data = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT options FROM worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ?",
                    new Object[]{worksheetitemid, worksheetitemversionid}, true);
                options.setPropertyList(data.getClob(0, "options"));
            } else if (worksheetsectionid.length() > 0) {
                data = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT options FROM worksheetsection WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?",
                    new Object[]{worksheetsectionid, worksheetsectionversionid}, true);
                options.setPropertyList(data.getClob(0, "options"));
            } else {
                data = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT options, lesflag, blockflag, blocksdcid FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ?",
                    new Object[]{worksheetid, worksheetversionid}, true);
                options.setPropertyList(data.getClob(0, "options"));
                options.setProperty("lesonly", data.getValue(0, "lesflag"));
                options.setProperty("blockflag", data.getValue(0, "blockflag"));
                options.setProperty("blocksdcid", data.getValue(0, "blocksdcid"));
            }

            commandResponse.set("options", options);
        } catch (Exception var12) {
            commandResponse.setStatusFail(
                "Failed to load worksheet item options. Reason: " + var12.getMessage(), var12);
        }

    }

    private void saveOptions(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetsectionid = commandRequest.getString("worksheetsectionid");
            String worksheetsectionversionid = commandRequest.getString(
                "worksheetsectionversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            PropertyList options = commandRequest.getPropertyList("options");
            String optionsXML = options.toXMLString();
            boolean apply = commandRequest.getBoolean("apply");
            ActionBlock ab = new ActionBlock();
            if (worksheetitemid.length() > 0) {
                if (apply) {
                    database.createPreparedResultSet(
                        "SELECT worksheetitemid, worksheetitemversionid FROM worksheetitem WHERE worksheetid = ? AND worksheetversionid = ?",
                        new Object[]{worksheetid, worksheetversionid});

                    while (database.getNext()) {
                        PropertyList editProps = new PropertyList();
                        editProps.setProperty("worksheetid", worksheetid);
                        editProps.setProperty("worksheetversionid", worksheetversionid);
                        editProps.setProperty("worksheetitemid",
                            database.getValue("worksheetitemid"));
                        editProps.setProperty("worksheetitemversionid",
                            database.getValue("worksheetitemversionid"));
                        editProps.setProperty("options", optionsXML);
                        ab.setActionClass("EditItemOptions" + database.getValue("worksheetitemid")
                                + database.getValue("worksheetitemversionid"),
                            EditWorksheetItem.class.getName(), editProps);
                    }
                } else {
                    PropertyList editProps = new PropertyList();
                    editProps.setProperty("worksheetid", worksheetid);
                    editProps.setProperty("worksheetversionid", worksheetversionid);
                    editProps.setProperty("worksheetitemid", worksheetitemid);
                    editProps.setProperty("worksheetitemversionid", worksheetitemversionid);
                    editProps.setProperty("options", optionsXML);
                    ab.setActionClass("EditItemOptions", EditWorksheetItem.class.getName(),
                        editProps);
                }
            } else if (worksheetsectionid.length() > 0) {
                resolveSectionSDIApprovals(this.getQueryProcessor(), database, worksheetid,
                    worksheetversionid, worksheetsectionid, worksheetsectionversionid, options, ab);
                if (!apply) {
                    PropertyList editProps = new PropertyList();
                    editProps.setProperty("worksheetid", worksheetid);
                    editProps.setProperty("worksheetversionid", worksheetversionid);
                    editProps.setProperty("worksheetsectionid", worksheetsectionid);
                    editProps.setProperty("worksheetsectionversionid", worksheetsectionversionid);
                    editProps.setProperty("options", optionsXML);
                    ab.setActionClass("EditSectionOptions", EditWorksheetSection.class.getName(),
                        editProps);
                } else {
                    boolean firstPass = true;
                    int level = 0;
                    database.createPreparedResultSet(
                        "SELECT worksheetsectionid, worksheetsectionversionid, sectionlevel, worksheetsectiondesc FROM worksheetsection WHERE worksheetid = ? AND worksheetversionid = ?   AND usersequence >= ( SELECT usersequence FROM worksheetsection WHERE worksheetsectionid = ? AND worksheetsectionversionid = ? ) ORDER BY usersequence",
                        new Object[]{worksheetid, worksheetversionid, worksheetsectionid,
                            worksheetsectionversionid});

                    for (; database.getNext(); firstPass = false) {
                        if (firstPass || database.getInt("sectionlevel") > level) {
                            PropertyList editProps = new PropertyList();
                            editProps.setProperty("worksheetid", worksheetid);
                            editProps.setProperty("worksheetversionid", worksheetversionid);
                            editProps.setProperty("worksheetsectionid",
                                database.getValue("worksheetsectionid"));
                            editProps.setProperty("worksheetsectionversionid",
                                database.getValue("worksheetsectionversionid"));
                            editProps.setProperty("options", optionsXML);
                            ab.setActionClass(
                                "EditSectionOptions" + database.getValue("worksheetsectionid")
                                    + database.getValue("worksheetsectionversionid"),
                                EditWorksheetSection.class.getName(), editProps);
                        }

                        if (firstPass) {
                            level = database.getInt("sectionlevel");
                        }
                    }
                }
            } else {
                PropertyList editProps = new PropertyList();
                editProps.setProperty("worksheetid", worksheetid);
                editProps.setProperty("worksheetversionid", worksheetversionid);
                PropertyListCollection worksheetsdcs = options.getCollection("worksheetsdcs");
                PropertyList sectionProps;
                if (worksheetid != null) {
                    for (int i = 0; i < worksheetsdcs.size(); ++i) {
                        sectionProps = worksheetsdcs.getPropertyList(i);
                        String sdcid = sectionProps.getProperty("sdcid");
                        sectionProps.setProperty("title", StringUtil.initCaps(
                            this.getSDCProcessor().getProperty(sdcid, "plural")));
                    }
                }

                editProps.setProperty("options", optionsXML);
                editProps.setProperty("lesflag", options.getProperty("lesonly", "N"));
                editProps.setProperty("blockflag", options.getProperty("blockflag", "N"));
                editProps.setProperty("blocksdcid",
                    options.getProperty("blockflag").equals("Y") ? options.getProperty("blocksdcid")
                        : "");
                editProps.setProperty("elnrequest", "Y");
                ab.setActionClass("EditWorksheetOptions", EditWorksheet.class.getName(), editProps);
                resolveWorksheetSDIApprovals(database, worksheetid, worksheetversionid, options,
                    ab);
                if (apply) {
                    String sectionOptionsXML = options.getPropertyList("sectiondefaults")
                        .toXMLString();
                    database.createPreparedResultSet(
                        "SELECT worksheetsectionid, worksheetsectionversionid FROM worksheetsection WHERE worksheetid = ? AND worksheetversionid = ? ",
                        new Object[]{worksheetid, worksheetversionid});

                    while (database.getNext()) {
                        sectionProps = new PropertyList();
                        sectionProps.setProperty("worksheetid", worksheetid);
                        sectionProps.setProperty("worksheetversionid", worksheetversionid);
                        sectionProps.setProperty("worksheetsectionid",
                            database.getValue("worksheetsectionid"));
                        sectionProps.setProperty("worksheetsectionversionid",
                            database.getValue("worksheetsectionversionid"));
                        sectionProps.setProperty("options", sectionOptionsXML);
                        ab.setActionClass(
                            "EditSectionOptions" + database.getValue("worksheetsectionid")
                                + database.getValue("worksheetsectionversionid"),
                            EditWorksheetSection.class.getName(), sectionProps);
                    }

                    String itemOptionsXML = options.getPropertyList("itemdefaults").toXMLString();
                    database.createPreparedResultSet(
                        "SELECT worksheetitemid, worksheetitemversionid FROM worksheetitem WHERE worksheetid = ? AND worksheetversionid = ?",
                        new Object[]{worksheetid, worksheetversionid});

                    while (database.getNext()) {
                        PropertyList itemProps = new PropertyList();
                        itemProps.setProperty("worksheetid", worksheetid);
                        itemProps.setProperty("worksheetversionid", worksheetversionid);
                        itemProps.setProperty("worksheetitemid",
                            database.getValue("worksheetitemid"));
                        itemProps.setProperty("worksheetitemversionid",
                            database.getValue("worksheetitemversionid"));
                        itemProps.setProperty("options", itemOptionsXML);
                        ab.setActionClass("EditItemOptions" + database.getValue("worksheetitemid")
                                + database.getValue("worksheetitemversionid"),
                            EditWorksheetItem.class.getName(), itemProps);
                    }
                }
            }

            this.getActionProcessor().processActionBlock(ab);
        } catch (Exception var19) {
            commandResponse.setStatusFail("Failed to save options. Reason: " + var19.getMessage(),
                var19);
        }

    }

    public static void resolveSectionSDIApprovals(QueryProcessor qp, DBAccess database,
        String worksheetid, String worksheetversionid, String worksheetsectionid,
        String worksheetsectionversionid, PropertyList options, ActionBlock ab)
        throws SapphireException {
        int deleteApprovalType = 0;
        if (options.getProperty("sectioncompletion").equals("A")
            && options.getProperty("sectionapprovaltype").length() > 0) {
            checkNestedSectionApprovalTypes(qp, worksheetid, worksheetversionid, worksheetsectionid,
                worksheetsectionversionid);
            database.createPreparedResultSet(
                "SELECT approvaltypeid FROM sdiapproval WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ?",
                new Object[]{"LV_WorksheetSection", worksheetsectionid, worksheetsectionversionid});
            boolean exists = false;
            if (database.getNext()) {
                String approvaltypeid = database.getValue("approvaltypeid");
                if (approvaltypeid.equals(options.getProperty("sectionapprovaltype"))) {
                    exists = true;
                } else {
                    PropertyList delApprovalProps = new PropertyList();
                    delApprovalProps.put("sdcid", "LV_WorksheetSection");
                    delApprovalProps.put("keyid1", worksheetsectionid);
                    delApprovalProps.put("keyid2", worksheetsectionversionid);
                    delApprovalProps.put("approvaltypeid", approvaltypeid);
                    ab.setAction("DeleteSectionApproval" + deleteApprovalType++,
                        "DeleteSDIApproval", "1", delApprovalProps);
                }
            }

            if (!exists) {
                PropertyList addApprovalProps = new PropertyList();
                addApprovalProps.put("sdcid", "LV_WorksheetSection");
                addApprovalProps.put("keyid1", worksheetsectionid);
                addApprovalProps.put("keyid2", worksheetsectionversionid);
                addApprovalProps.put("approvaltypeid", options.getProperty("sectionapprovaltype"));
                ab.setAction("AddSectionApproval", "AddSDIApproval", "1", addApprovalProps);
            }
        } else {
            database.createPreparedResultSet(
                "SELECT approvaltypeid FROM sdiapproval WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ?",
                new Object[]{"LV_Worksheet", worksheetid, worksheetversionid});
            if (database.getNext()) {
                PropertyList delApprovalProps = new PropertyList();
                delApprovalProps.put("sdcid", "LV_WorksheetSection");
                delApprovalProps.put("keyid1", worksheetsectionid);
                delApprovalProps.put("keyid2", worksheetsectionversionid);
                delApprovalProps.put("approvaltypeid", database.getValue("approvaltypeid"));
                ab.setAction("DeleteSectionApproval" + deleteApprovalType++, "DeleteSDIApproval",
                    "1", delApprovalProps);
            }
        }

    }

    public static void resolveWorksheetSDIApprovals(DBAccess database, String worksheetid,
        String worksheetversionid, PropertyList options, ActionBlock ab) throws SapphireException {
        int deleteApprovalType = 0;
        if (options.getProperty("worksheetcompletion").equals("A")
            && options.getProperty("worksheetapprovaltype").length() > 0) {
            database.createPreparedResultSet(
                "SELECT approvaltypeid FROM sdiapproval WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ?",
                new Object[]{"LV_Worksheet", worksheetid, worksheetversionid});
            boolean exists = false;
            if (database.getNext()) {
                String approvaltypeid = database.getValue("approvaltypeid");
                if (approvaltypeid.equals(options.getProperty("worksheetapprovaltype"))) {
                    exists = true;
                } else {
                    PropertyList delApprovalProps = new PropertyList();
                    delApprovalProps.put("sdcid", "LV_Worksheet");
                    delApprovalProps.put("keyid1", worksheetid);
                    delApprovalProps.put("keyid2", worksheetversionid);
                    delApprovalProps.put("approvaltypeid", approvaltypeid);
                    ab.setAction("DeleteWorksheetApproval" + deleteApprovalType++,
                        "DeleteSDIApproval", "1", delApprovalProps);
                }
            }

            if (!exists) {
                PropertyList addApprovalProps = new PropertyList();
                addApprovalProps.put("sdcid", "LV_Worksheet");
                addApprovalProps.put("keyid1", worksheetid);
                addApprovalProps.put("keyid2", worksheetversionid);
                addApprovalProps.put("approvaltypeid",
                    options.getProperty("worksheetapprovaltype"));
                ab.setAction("AddWorksheetApproval", "AddSDIApproval", "1", addApprovalProps);
            }
        } else {
            database.createPreparedResultSet(
                "SELECT approvaltypeid FROM sdiapproval WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ?",
                new Object[]{"LV_Worksheet", worksheetid, worksheetversionid});
            if (database.getNext()) {
                PropertyList delApprovalProps = new PropertyList();
                delApprovalProps.put("sdcid", "LV_Worksheet");
                delApprovalProps.put("keyid1", worksheetid);
                delApprovalProps.put("keyid2", worksheetversionid);
                delApprovalProps.put("approvaltypeid", database.getValue("approvaltypeid"));
                ab.setAction("DeleteWorksheetApproval" + deleteApprovalType++, "DeleteSDIApproval",
                    "1", delApprovalProps);
            }
        }

    }

    private static void checkNestedSectionApprovalTypes(QueryProcessor qp, String worksheetid,
        String worksheetversionid, String worksheetsectionid, String worksheetsectionversionid)
        throws SapphireException {
        DataSet sections = qp.getPreparedSqlDataSet(
            "SELECT worksheetsectionid, worksheetsectionversionid, sectionlevel, options FROM worksheetsection WHERE worksheetid=? AND worksheetversionid=? ORDER BY usersequence",
            new String[]{worksheetid, worksheetversionid}, true);
        HashMap find = new HashMap();
        find.put("worksheetsectionid", worksheetsectionid);
        find.put("worksheetsectionversionid", worksheetsectionversionid);
        int row = sections.findRow(find);
        if (row >= 0) {
            int startlevel = sections.getInt(row, "sectionlevel");
            boolean nestedSectionError = false;
            boolean parentSectionError = false;

            int i;
            int level;
            String suboptions;
            PropertyList suboptionsPL;
            for (i = row + 1; i < sections.size(); ++i) {
                level = sections.getInt(i, "sectionlevel");
                if (level <= startlevel) {
                    break;
                }

                suboptions = sections.getValue(i, "options").trim();
                if (suboptions.startsWith("<propertylist")) {
                    suboptionsPL = new PropertyList();
                    suboptionsPL.setPropertyList(suboptions);
                    if (!suboptionsPL.getProperty("sectioncompletion").equals("N")) {
                        nestedSectionError = true;
                    }
                }
            }

            for (i = row - 1; i >= 0; --i) {
                level = sections.getInt(i, "sectionlevel");
                if (level >= startlevel) {
                    break;
                }

                suboptions = sections.getValue(i, "options").trim();
                if (suboptions.startsWith("<propertylist")) {
                    suboptionsPL = new PropertyList();
                    suboptionsPL.setPropertyList(suboptions);
                    if (!suboptionsPL.getProperty("sectioncompletion").equals("N")) {
                        parentSectionError = true;
                    }
                }
            }

            if (nestedSectionError) {
                throw new SapphireException(
                    "A sub-section has already been marked as requiring Completion");
            }

            if (parentSectionError) {
                throw new SapphireException(
                    "A parent section has already been marked as requiring Completion");
            }
        }

    }

    private void saveUserPrivs(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            PropertyList editProps = new PropertyList();
            editProps.setProperty("worksheetid", worksheetid);
            editProps.setProperty("worksheetversionid", worksheetversionid);
            editProps.setProperty("userprivs", commandRequest.getString("userprivs"));
            editProps.setProperty("elnrequest", "Y");
            this.getActionProcessor().processActionClass(EditWorksheet.class.getName(), editProps);
        } catch (Exception var7) {
            commandResponse.setStatusFail("Failed to save user privs. Reason: " + var7.getMessage(),
                var7);
        }

    }

    private void saveParameters(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        String worksheetitemid = commandRequest.getString("worksheetitemid");
        String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");

        try {
            DataSet params = commandRequest.getDataSet("params");
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            actionProps.setProperty("worksheetversionid",
                commandRequest.getString("worksheetversionid"));
            actionProps.setProperty("worksheetitemid", worksheetitemid);
            actionProps.setProperty("worksheetitemversionid", worksheetitemversionid);
            actionProps.setProperty("paramname", params.getColumnValues("paramname", ";"));
            actionProps.setProperty("paramtitle", params.getColumnValues("paramtitle", ";"));
            actionProps.setProperty("parameditorstyleid",
                params.getColumnValues("parameditorstyleid", ";"));
            actionProps.setProperty("paramvalue", params.getColumnValues("paramvalue", ";"));
            actionProps.setProperty("valuesdcid", params.getColumnValues("valuesdcid", ";"));
            actionProps.setProperty("valuekeyid1", params.getColumnValues("valuekeyid1", ";"));
            actionProps.setProperty("valuekeyid2", params.getColumnValues("valuekeyid2", ";"));
            actionProps.setProperty("valuetype", params.getColumnValues("valuetype", ";"));
            actionProps.setProperty("valuelabel", params.getColumnValues("valuelabel", ";"));
            this.getActionProcessor()
                .processActionClass(EditWorksheetItemParams.class.getName(), actionProps);
        } catch (Exception var8) {
            commandResponse.setStatusFail(
                "Failed to save parameters for worksheet item " + BaseELNAction.getIdVersionText(
                    worksheetitemid, worksheetitemversionid) + ". Reason: " + var8.getMessage(),
                var8);
        }

    }

    private void addSDIAttributes(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetsectionid = commandRequest.getString("worksheetsectionid");
            String worksheetsectionversionid = commandRequest.getString(
                "worksheetsectionversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            String attributes = commandRequest.getString("attributes");
            String type = commandRequest.getString("type");
            ActionBlock ab = new ActionBlock();
            PropertyList actionProps = new PropertyList();
            if (worksheetitemid.length() > 0) {
                actionProps.setProperty("sdcid", "LV_WorksheetItem");
                actionProps.setProperty("attributesdcid", "LV_WorksheetItem");
                actionProps.setProperty("keyid1", worksheetitemid);
                actionProps.setProperty("keyid2", worksheetitemversionid);
            } else if (worksheetsectionid.length() > 0) {
                actionProps.setProperty("sdcid", "LV_WorksheetSection");
                actionProps.setProperty("attributesdcid", "LV_WorksheetSection");
                actionProps.setProperty("keyid1", worksheetsectionid);
                actionProps.setProperty("keyid2", worksheetsectionversionid);
            } else {
                actionProps.setProperty("sdcid", "LV_Worksheet");
                actionProps.setProperty("attributesdcid", "LV_Worksheet");
                actionProps.setProperty("keyid1", worksheetid);
                actionProps.setProperty("keyid2", worksheetversionid);
            }

            actionProps.setProperty("type", type);
            actionProps.setProperty("attributeid", attributes);
            ab.setAction("AddAttributes", "AddSDIAttribute", "1", actionProps);
            PropertyList activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("targetsdcid", actionProps.getProperty("sdcid"));
            activityProps.setProperty("targetkeyid1", actionProps.getProperty("keyid1"));
            activityProps.setProperty("targetkeyid2", actionProps.getProperty("keyid2"));
            activityProps.setProperty("activitytype", "Add");
            activityProps.setProperty("activitylog", "Added metadata " + attributes);
            ab.setActionClass("ActivityLog", AddWorksheetActivity.class.getName(), activityProps);
            this.getActionProcessor().processActionBlock(ab);
        } catch (Exception var15) {
            commandResponse.setStatusFail("Failed to add attributes. Reason: " + var15.getMessage(),
                var15);
        }

    }

    private void deleteSDIAttributes(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            ActionBlock ab = new ActionBlock();
            PropertyList deleteProps = new PropertyList();
            deleteProps.setProperty("sdcid", commandRequest.getString("sdcid"));
            deleteProps.setProperty("keyid1", commandRequest.getString("keyid1"));
            deleteProps.setProperty("keyid2", commandRequest.getString("keyid2"));
            deleteProps.setProperty("attributeid", commandRequest.getString("attributeid"));
            deleteProps.setProperty("attributesdcid", commandRequest.getString("attributesdcid"));
            deleteProps.setProperty("attributeinstance",
                commandRequest.getString("attributeinstance"));
            ab.setAction("DeleteAttribute", "DeleteSDIAttribute", "1", deleteProps);
            PropertyList activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("targetsdcid", commandRequest.getString("sdcid"));
            activityProps.setProperty("targetkeyid1", commandRequest.getString("keyid1"));
            activityProps.setProperty("targetkeyid2", commandRequest.getString("keyid2"));
            activityProps.setProperty("activitytype", "Delete");
            activityProps.setProperty("activitylog",
                "Deleted attribute: " + commandRequest.getString("attributeid"));
            ab.setActionClass("ActivityLog", AddWorksheetActivity.class.getName(), activityProps);
            this.getActionProcessor().processActionBlock(ab);
        } catch (Exception var9) {
            commandResponse.setStatusFail(
                "Failed to delete attribute. Reason: " + var9.getMessage(), var9);
        }

    }

    private void validateSDIAttributes(CommandRequest commandRequest,
        CommandResponse commandResponse, DBUtil database) {
        try {
            DataSet attributes = commandRequest.getDataSet("attributes");
            DateTimeUtil dtu = new DateTimeUtil(this.sapphireConnection);

            for (int i = 0; i < attributes.size(); ++i) {
                HashMap trans = new HashMap();
                trans.put("title", attributes.getValue(i, "attributetitle"));
                String value = attributes.getValue(i, "value");
                if (value.length() > 0) {
                    if (attributes.getValue(i, "datatype").equals("N")) {
                        try {
                            new BigDecimal(value);
                        } catch (Exception var10) {
                            attributes.setString(i, "error", this.getTranslationProcessor()
                                .translate("Value for '[title]' is not a valid number", trans));
                        }
                    } else if (attributes.getValue(i, "datatype").equals("D")
                        || attributes.getValue(i, "datatype").equals("O")) {
                        Calendar cal = dtu.getCalendar(value);
                        if (cal == null) {
                            attributes.setString(i, "error", this.getTranslationProcessor()
                                .translate("Value for '[title]' is not a valid date", trans));
                        }
                    }
                }
            }

            commandResponse.set("attributes", attributes);
        } catch (Exception var11) {
            commandResponse.setStatusFail("Failed to add attributes. Reason: " + var11.getMessage(),
                var11);
        }

    }

    private void saveSDIAttributes(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetsectionid = commandRequest.getString("worksheetsectionid");
            String worksheetsectionversionid = commandRequest.getString(
                "worksheetsectionversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            DataSet attributes = commandRequest.getDataSet("attributes");
            boolean template = commandRequest.getBoolean("template");
            DataSet saveAttributes;
            DataSet deleteAttributes;
            if (attributes.getColumnType("_delete") == -1) {
                saveAttributes = attributes;
                deleteAttributes = null;
            } else {
                HashMap filter = new HashMap();
                filter.put("_delete", "Y");
                deleteAttributes = attributes.getFilteredDataSet(filter);
                saveAttributes = attributes.getFilteredDataSet(filter, true);
            }

            String priorOptions = "";
            if (worksheetitemid.length() > 0) {
                priorOptions = this.getQueryProcessor().getPreparedSqlDataSet(
                        "SELECT options FROM worksheetitem WHERE worksheetitemid=? AND worksheetitemversionid=?",
                        new String[]{worksheetitemid, worksheetitemversionid}, true)
                    .getValue(0, "options");
            } else if (worksheetsectionid.length() > 0) {
                priorOptions = this.getQueryProcessor().getPreparedSqlDataSet(
                        "SELECT options FROM worksheetsection WHERE worksheetsectionid=? AND worksheetsectionversionid=?",
                        new String[]{worksheetsectionid, worksheetsectionversionid}, true)
                    .getValue(0, "options");
            } else {
                priorOptions = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT options FROM worksheet WHERE worksheetid=? AND worksheetversionid=?",
                    new String[]{worksheetid, worksheetversionid}, true).getValue(0, "options");
            }

            if (saveAttributes.size() > 0) {
                PropertyList actionProps = this.saveSDIAttributes(worksheetid, worksheetversionid,
                    worksheetsectionid, worksheetsectionversionid, worksheetitemid,
                    worksheetitemversionid, saveAttributes, template);
                DataSet dependents = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT DISTINCT worksheetitemid, worksheetitemversionid FROM worksheetitemparam WHERE valuesdcid = ? AND valuekeyid1 = ? AND valuekeyid2 = ? AND valuetype = 'metadata' AND valuelabel IN ('"
                        + StringUtil.replaceAll(actionProps.getProperty("attributeid"), ";", "','")
                        + "')", new Object[]{actionProps.getProperty("sdcid"),
                        actionProps.getProperty("keyid1"), actionProps.getProperty("keyid2")});
                if (dependents.size() > 0) {
                    commandResponse.set("dependentworksheetitemid",
                        dependents.getColumnValues("worksheetitemid", ";"));
                    commandResponse.set("dependentworksheetitemversionid",
                        dependents.getColumnValues("worksheetitemversionid", ";"));
                }
            }

            if (deleteAttributes != null && deleteAttributes.size() > 0) {
                ActionBlock ab = new ActionBlock();
                PropertyList deleteProps = new PropertyList();
                if (worksheetitemid.length() > 0) {
                    deleteProps.setProperty("sdcid", "LV_WorksheetItem");
                    deleteProps.setProperty("keyid1", worksheetitemid);
                    deleteProps.setProperty("keyid2", worksheetitemversionid);
                } else if (worksheetsectionid.length() > 0) {
                    deleteProps.setProperty("sdcid", "LV_WorksheetSection");
                    deleteProps.setProperty("keyid1", worksheetsectionid);
                    deleteProps.setProperty("keyid2", worksheetsectionversionid);
                } else {
                    deleteProps.setProperty("sdcid", "LV_Worksheet");
                    deleteProps.setProperty("keyid1", worksheetid);
                    deleteProps.setProperty("keyid2", worksheetversionid);
                }

                deleteProps.setProperty("attributeid",
                    deleteAttributes.getColumnValues("attributeid", ";"));
                deleteProps.setProperty("attributesdcid",
                    deleteAttributes.getColumnValues("attributesdcid", ";"));
                deleteProps.setProperty("attributeinstance",
                    deleteAttributes.getColumnValues("attributeinstance", ";"));
                ab.setAction("DeleteAttribute", "DeleteSDIAttribute", "1", deleteProps);
                PropertyList activityProps = new PropertyList();
                activityProps.setProperty("worksheetid", worksheetid);
                activityProps.setProperty("worksheetversionid", worksheetversionid);
                activityProps.setProperty("targetsdcid", deleteProps.getProperty("sdcid"));
                activityProps.setProperty("targetkeyid1", deleteProps.getProperty("keyid1"));
                activityProps.setProperty("targetkeyid2", deleteProps.getProperty("keyid2"));
                activityProps.setProperty("activitytype", "Delete");
                activityProps.setProperty("activitylog",
                    "Deleted attribute: " + commandRequest.getString("attributeid"));
                ab.setActionClass("ActivityLog", AddWorksheetActivity.class.getName(),
                    activityProps);
                this.getActionProcessor().processActionBlock(ab);
            }

            String afterOptions = "";
            if (worksheetitemid.length() > 0) {
                afterOptions = this.getQueryProcessor().getPreparedSqlDataSet(
                        "SELECT options FROM worksheetitem WHERE worksheetitemid=? AND worksheetitemversionid=?",
                        new String[]{worksheetitemid, worksheetitemversionid}, true)
                    .getValue(0, "options");
            } else if (worksheetsectionid.length() > 0) {
                afterOptions = this.getQueryProcessor().getPreparedSqlDataSet(
                        "SELECT options FROM worksheetsection WHERE worksheetsectionid=? AND worksheetsectionversionid=?",
                        new String[]{worksheetsectionid, worksheetsectionversionid}, true)
                    .getValue(0, "options");
            } else {
                afterOptions = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT options FROM worksheet WHERE worksheetid=? AND worksheetversionid=?",
                    new String[]{worksheetid, worksheetversionid}, true).getValue(0, "options");
            }

            if (!priorOptions.equals(afterOptions)) {
                commandResponse.set("_refreshworksheet", "Y");
            }
        } catch (Exception var18) {
            commandResponse.setStatusFail("Failed to add attributes. Reason: " + var18.getMessage(),
                var18);
        }

    }

    private PropertyList saveSDIAttributes(String worksheetid, String worksheetversionid,
        String worksheetsectionid, String worksheetsectionversionid, String worksheetitemid,
        String worksheetitemversionid, DataSet attributes, boolean template)
        throws ActionException {
        ActionBlock ab = new ActionBlock();
        PropertyList actionProps = new PropertyList();
        if (worksheetitemid.length() > 0) {
            actionProps.setProperty("sdcid", "LV_WorksheetItem");
            actionProps.setProperty("keyid1", worksheetitemid);
            actionProps.setProperty("keyid2", worksheetitemversionid);
        } else if (worksheetsectionid.length() > 0) {
            actionProps.setProperty("sdcid", "LV_WorksheetSection");
            actionProps.setProperty("keyid1", worksheetsectionid);
            actionProps.setProperty("keyid2", worksheetsectionversionid);
        } else {
            actionProps.setProperty("sdcid", "LV_Worksheet");
            actionProps.setProperty("keyid1", worksheetid);
            actionProps.setProperty("keyid2", worksheetversionid);
        }

        actionProps.setProperty("attributeid", attributes.getColumnValues("attributeid", ";"));
        actionProps.setProperty("attributeinstance",
            attributes.getColumnValues("attributeinstance", ";"));
        actionProps.setProperty("attributesdcid",
            attributes.getColumnValues("attributesdcid", ";"));
        actionProps.setProperty(template ? "defaultvalue" : "value",
            attributes.getColumnValues("value", ";"));
        actionProps.setProperty("mandatory", attributes.getColumnValues("mandatoryflag", ";"));
        actionProps.setProperty("updatable", attributes.getColumnValues("updateableflag", ";"));
        ab.setAction("EditAttributes", "EditSDIAttribute", "1", actionProps);
        PropertyList activityProps = new PropertyList();
        activityProps.setProperty("worksheetid", worksheetid);
        activityProps.setProperty("worksheetversionid", worksheetversionid);
        activityProps.setProperty("targetsdcid", actionProps.getProperty("sdcid"));
        activityProps.setProperty("targetkeyid1", actionProps.getProperty("keyid1"));
        activityProps.setProperty("targetkeyid2", actionProps.getProperty("keyid2"));
        activityProps.setProperty("activitytype", "Edit");
        StringBuilder log = new StringBuilder();

        for (int i = 0; i < attributes.size(); ++i) {
            String attributeid = attributes.getValue(i, "attributeid") + (
                attributes.getValue(i, "attributeinstance").equals("1") ? ""
                    : " (" + attributes.getValue(i, "attributeinstance") + ")");
            log.append(", ");
            if (attributes.getValue(i, "_delete").equals("Y")) {
                log.append(attributeid + " Deleted");
            } else {
                log.append(attributeid + "=" + attributes.getValue(i, "value"));
            }
        }

        activityProps.setProperty("activitylog",
            "Edited Metadata: " + (log.length() > 0 ? log.substring(2) : ""));
        ab.setActionClass("ActivityLog", AddWorksheetActivity.class.getName(), activityProps);
        this.getActionProcessor().processActionBlock(ab);
        return actionProps;
    }

    private void addSDIAttachment(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetsectionid = commandRequest.getString("worksheetsectionid");
            String worksheetsectionversionid = commandRequest.getString(
                "worksheetsectionversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            String attachmentdesc = commandRequest.getString("attachmentdesc");
            PropertyList activityProps = new PropertyList();
            if (worksheetitemid.length() > 0) {
                activityProps.setProperty("targetsdcid", "LV_WorksheetItem");
                activityProps.setProperty("targetkeyid1", worksheetitemid);
                activityProps.setProperty("targetkeyid2", worksheetitemversionid);
            } else if (worksheetsectionid.length() > 0) {
                activityProps.setProperty("targetsdcid", "LV_WorksheetSection");
                activityProps.setProperty("targetkeyid1", worksheetsectionid);
                activityProps.setProperty("targetkeyid2", worksheetsectionversionid);
            } else {
                activityProps.setProperty("targetsdcid", "LV_Worksheet");
                activityProps.setProperty("targetkeyid1", worksheetid);
                activityProps.setProperty("targetkeyid2", worksheetversionid);
            }

            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("activitytype", "Add");
            activityProps.setProperty("activitylog", "Added attachment: " + attachmentdesc);
            this.getActionProcessor()
                .processActionClass(AddWorksheetActivity.class.getName(), activityProps);
        } catch (Exception var12) {
            commandResponse.setStatusFail(
                "Failed to add attachment (log). Reason: " + var12.getMessage(), var12);
        }

    }

    private void editSDIAttachment(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetsectionid = commandRequest.getString("worksheetsectionid");
            String worksheetsectionversionid = commandRequest.getString(
                "worksheetsectionversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            int attachmentnum = Integer.parseInt(commandRequest.getString("attachmentnum"));
            String attachmentdesc = commandRequest.getString("attachmentdesc");
            PropertyList activityProps = new PropertyList();
            Object[] update;
            if (worksheetitemid.length() > 0) {
                activityProps.setProperty("targetsdcid", "LV_WorksheetItem");
                activityProps.setProperty("targetkeyid1", worksheetitemid);
                activityProps.setProperty("targetkeyid2", worksheetitemversionid);
                update = new Object[]{attachmentdesc, "LV_WorksheetItem", worksheetitemid,
                    worksheetitemversionid, attachmentnum};
            } else if (worksheetsectionid.length() > 0) {
                activityProps.setProperty("targetsdcid", "LV_WorksheetSection");
                activityProps.setProperty("targetkeyid1", worksheetsectionid);
                activityProps.setProperty("targetkeyid2", worksheetsectionversionid);
                update = new Object[]{attachmentdesc, "LV_WorksheetSection", worksheetsectionid,
                    worksheetsectionversionid, attachmentnum};
            } else {
                activityProps.setProperty("targetsdcid", "LV_Worksheet");
                activityProps.setProperty("targetkeyid1", worksheetid);
                activityProps.setProperty("targetkeyid2", worksheetversionid);
                update = new Object[]{attachmentdesc, "LV_Worksheet", worksheetid,
                    worksheetversionid, attachmentnum};
            }

            database.executePreparedUpdate(
                "UPDATE sdiattachment SET attachmentdesc = ? WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = '(null)' AND attachmentnum = ?",
                update);
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("activitytype", "Edit");
            activityProps.setProperty("activitylog",
                "Edited attachment description: " + attachmentdesc);
            this.getActionProcessor()
                .processActionClass(AddWorksheetActivity.class.getName(), activityProps);
        } catch (Exception var14) {
            commandResponse.setStatusFail(
                "Failed to edit attachments. Reason: " + var14.getMessage(), var14);
        }

    }

    private void deleteSDIAttachment(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            ActionBlock ab = new ActionBlock();
            PropertyList deleteProps = new PropertyList();
            deleteProps.setProperty("sdcid", commandRequest.getString("sdcid"));
            deleteProps.setProperty("keyid1", commandRequest.getString("keyid1"));
            deleteProps.setProperty("keyid2", commandRequest.getString("keyid2"));
            if (commandRequest.getString("attachmentnum").length() > 0) {
                deleteProps.setProperty("attachmentnum", commandRequest.getString("attachmentnum"));
            }

            ab.setAction("DeleteAttachment", "DeleteSDIAttachment", "1", deleteProps);
            PropertyList activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("targetsdcid", commandRequest.getString("sdcid"));
            activityProps.setProperty("targetkeyid1", commandRequest.getString("keyid1"));
            activityProps.setProperty("targetkeyid2", commandRequest.getString("keyid2"));
            activityProps.setProperty("activitytype", "Delete");
            if (commandRequest.getString("attachmentnum").length() > 0) {
                activityProps.setProperty("activitylog",
                    "Deleted attachment: " + commandRequest.getString("attachmentnum"));
            } else {
                activityProps.setProperty("activitylog", "Deleted all attachments");
            }

            ab.setActionClass("ActivityLog", AddWorksheetActivity.class.getName(), activityProps);
            this.getActionProcessor().processActionBlock(ab);
        } catch (Exception var9) {
            commandResponse.setStatusFail(
                "Failed to delete attachments. Reason: " + var9.getMessage(), var9);
        }

    }

    private void addContributor(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");

        try {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("worksheetid", worksheetid);
            actionProps.setProperty("worksheetversionid", worksheetversionid);
            actionProps.setProperty("contributorid", commandRequest.getString("contributorid"));
            this.getActionProcessor()
                .processActionClass(AddWorksheetContributor.class.getName(), actionProps);
        } catch (Exception var7) {
            commandResponse.setStatusFail(
                "Failed to add contributor to worksheet " + BaseELNAction.getIdVersionText(
                    worksheetid, worksheetversionid) + ". Reason: " + var7.getMessage(), var7);
        }

    }

    private void deleteContributor(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");

        try {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("worksheetid", worksheetid);
            actionProps.setProperty("worksheetversionid", worksheetversionid);
            actionProps.setProperty("contributorid", commandRequest.getString("contributorid"));
            this.getActionProcessor()
                .processActionClass(DeleteWorksheetContributor.class.getName(), actionProps);
        } catch (Exception var7) {
            commandResponse.setStatusFail(
                "Failed to delete contributor from worksheet " + BaseELNAction.getIdVersionText(
                    worksheetid, worksheetversionid) + ". Reason: " + var7.getMessage(), var7);
        }

    }

    private void resetDetails(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");
        String worksheetsectionid = commandRequest.getString("worksheetsectionid");
        String worksheetsectionversionid = commandRequest.getString("worksheetsectionversionid");
        String worksheetitemid = commandRequest.getString("worksheetitemid");
        String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
        boolean resetMetadata = commandRequest.getBoolean("resetmetadata");
        boolean resetMetadataValues = commandRequest.getBoolean("resetmetadatavalues");
        boolean resetNotes = commandRequest.getBoolean("resetnotes");
        boolean resetAttachments = commandRequest.getBoolean("resetattachments");
        boolean resetLIMSData = commandRequest.getBoolean("resetlimsdata");
        boolean applyAll = commandRequest.getBoolean("applyall");
        boolean applySubsections = commandRequest.getBoolean("applysubsections");
        boolean applyControls = commandRequest.getBoolean("applycontrols");
        boolean template = commandRequest.getBoolean("template");
        boolean controlTemplate = commandRequest.getBoolean("controltemplate");

        try {
            ActionBlock ab = new ActionBlock();
            if (worksheetitemid.length() > 0) {
                this.resetDetails(worksheetid, worksheetversionid, "LV_WorksheetItem",
                    worksheetitemid, worksheetitemversionid, resetMetadata, resetMetadataValues,
                    resetNotes, resetAttachments, resetLIMSData, template, controlTemplate, ab);
            } else {
                if (worksheetsectionid.length() > 0) {
                    this.resetDetails(worksheetid, worksheetversionid, "LV_WorksheetSection",
                        worksheetsectionid, worksheetsectionversionid, resetMetadata,
                        resetMetadataValues, resetNotes, resetAttachments, resetLIMSData, template,
                        controlTemplate, ab);
                    if (applyControls) {
                        DataSet items = this.getQueryProcessor().getPreparedSqlDataSet(
                            "SELECT worksheetitemid, worksheetitemversionid FROM worksheetitem WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?",
                            new Object[]{worksheetsectionid, worksheetsectionversionid});

                        for (int i = 0; i < items.size(); ++i) {
                            this.resetDetails(worksheetid, worksheetversionid, "LV_WorksheetItem",
                                items.getValue(i, "worksheetitemid"),
                                items.getValue(i, "worksheetitemversionid"), resetMetadata,
                                resetMetadataValues, resetNotes, resetAttachments, resetLIMSData,
                                template, controlTemplate, ab);
                        }
                    }

                    if (applySubsections) {
                        int level = Integer.parseInt(commandRequest.getString("sectionlevel", "1"));
                        DataSet sections = this.getQueryProcessor().getPreparedSqlDataSet(
                            "SELECT worksheetsectionid, worksheetsectionversionid, sectionlevel FROM worksheetsection WHERE worksheetid = ? AND worksheetversionid = ? AND usersequence > ( SELECT usersequence FROM worksheetsection WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?) ORDER BY usersequence",
                            new Object[]{worksheetid, worksheetversionid, worksheetsectionid,
                                worksheetsectionversionid});

                        for (int i = 0;
                            i < sections.size() && sections.getInt(i, "sectionlevel") > level;
                            ++i) {
                            this.resetDetails(worksheetid, worksheetversionid,
                                "LV_WorksheetSection", sections.getValue(i, "worksheetsectionid"),
                                sections.getValue(i, "worksheetsectionversionid"), resetMetadata,
                                resetMetadataValues, resetNotes, resetAttachments, resetLIMSData,
                                template, controlTemplate, ab);
                            if (applyControls) {
                                DataSet items = this.getQueryProcessor().getPreparedSqlDataSet(
                                    "SELECT worksheetitemid, worksheetitemversionid FROM worksheetitem WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?",
                                    new Object[]{sections.getString(i, "worksheetsectionid"),
                                        sections.getString(i, "worksheetsectionversionid")});

                                for (int j = 0; j < items.size(); ++j) {
                                    this.resetDetails(worksheetid, worksheetversionid,
                                        "LV_WorksheetItem", items.getValue(j, "worksheetitemid"),
                                        items.getValue(j, "worksheetitemversionid"), resetMetadata,
                                        resetMetadataValues, resetNotes, resetAttachments,
                                        resetLIMSData, template, controlTemplate, ab);
                                }
                            }
                        }
                    }
                } else {
                    this.resetDetails(worksheetid, worksheetversionid, "LV_Worksheet", worksheetid,
                        worksheetversionid, resetMetadata, resetMetadataValues, resetNotes,
                        resetAttachments, resetLIMSData, template, controlTemplate, ab);
                    if (applyAll) {
                        DataSet sections = this.getQueryProcessor().getPreparedSqlDataSet(
                            "SELECT worksheetsectionid, worksheetsectionversionid FROM worksheetsection WHERE worksheetid = ? AND worksheetversionid = ?",
                            new Object[]{worksheetid, worksheetversionid});

                        for (int i = 0; i < sections.size(); ++i) {
                            this.resetDetails(worksheetid, worksheetversionid,
                                "LV_WorksheetSection", sections.getValue(i, "worksheetsectionid"),
                                sections.getValue(i, "worksheetsectionversionid"), resetMetadata,
                                resetMetadataValues, resetNotes, resetAttachments, resetLIMSData,
                                template, controlTemplate, ab);
                        }

                        sections = this.getQueryProcessor().getPreparedSqlDataSet(
                            "SELECT worksheetitemid, worksheetitemversionid FROM worksheetitem WHERE worksheetid = ? AND worksheetversionid = ?",
                            new Object[]{worksheetid, worksheetversionid});

                        for (int i = 0; i < sections.size(); ++i) {
                            this.resetDetails(worksheetid, worksheetversionid, "LV_WorksheetItem",
                                sections.getValue(i, "worksheetitemid"),
                                sections.getValue(i, "worksheetitemversionid"), resetMetadata,
                                resetMetadataValues, resetNotes, resetAttachments, resetLIMSData,
                                template, controlTemplate, ab);
                        }
                    }
                }
            }

            this.getActionProcessor().processActionBlock(ab);
        } catch (Exception var26) {
            commandResponse.setStatusFail(
                "Failed to reset details for worksheet " + BaseELNAction.getIdVersionText(
                    worksheetid, worksheetversionid) + ". Reason: " + var26.getMessage(), var26);
        }

    }

    private void resetDetails(String worksheetid, String worksheetversionid, String itemsdcid,
        String itemid, String itemversionid, boolean resetMetadata, boolean resetMetadataValues,
        boolean resetNotes, boolean resetAttachments, boolean resetLIMSData, boolean template,
        boolean controlTemplate, ActionBlock ab) throws SapphireException {
        DataSet notes;
        int i;
        PropertyList noteProps;
        PropertyList activityProps;
        if (resetMetadata || resetMetadataValues) {
            notes = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT attributeid, attributeinstance FROM sdiattribute WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ?",
                new Object[]{itemsdcid, itemid, itemversionid});

            for (i = 0; i < notes.size(); ++i) {
                noteProps = new PropertyList();
                noteProps.setProperty("sdcid", itemsdcid);
                noteProps.setProperty("keyid1", itemid);
                noteProps.setProperty("keyid2", itemversionid);
                noteProps.setProperty("attributesdcid", itemsdcid);
                noteProps.setProperty("attributeid", notes.getValue(i, "attributeid"));
                noteProps.setProperty("attributeinstance", notes.getValue(i, "attributeinstance"));
                noteProps.setProperty(template ? "defaultvalue" : "value", "");
                ab.setAction("Attributes_" + itemid + notes.getValue(i, "attributeid"),
                    resetMetadata ? "DeleteSDIAttribute" : "EditSDIAttribute",
                    resetMetadata ? "1" : "1", noteProps);
            }

            activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("targetsdcid", itemsdcid);
            activityProps.setProperty("targetkeyid1", itemid);
            activityProps.setProperty("targetkeyid2", itemversionid);
            activityProps.setProperty("activitytype", "Delete");
            activityProps.setProperty("activitylog",
                resetMetadata ? "Deleted all metadata" : "Reset all metadata values");
            ab.setActionClass("AttributeActivityLog_" + itemid,
                AddWorksheetActivity.class.getName(), activityProps);
        }

        if (resetNotes) {
            notes = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT notenum FROM sdinote WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ?",
                new Object[]{itemsdcid, itemid, itemversionid});

            for (i = 0; i < notes.size(); ++i) {
                noteProps = new PropertyList();
                noteProps.setProperty("sdcid", itemsdcid);
                noteProps.setProperty("keyid1", itemid);
                noteProps.setProperty("keyid2", itemversionid);
                noteProps.setProperty("notenum", notes.getValue(i, "notenum"));
                ab.setActionClass("Notes_" + itemid + notes.getValue(i, "notenum"),
                    DeleteSDINote.class.getName(), noteProps);
            }

            activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("targetsdcid", itemsdcid);
            activityProps.setProperty("targetkeyid1", itemid);
            activityProps.setProperty("targetkeyid2", itemversionid);
            activityProps.setProperty("activitytype", "Delete");
            activityProps.setProperty("activitylog", "Deleted all notes");
            ab.setActionClass("NotesActivityLog_" + itemid, AddWorksheetActivity.class.getName(),
                activityProps);
        }

        PropertyList limsProps;
        if (resetAttachments && !itemsdcid.equals("LV_WorksheetSection")) {
            limsProps = new PropertyList();
            limsProps.setProperty("sdcid", itemsdcid);
            limsProps.setProperty("keyid1", itemid);
            limsProps.setProperty("keyid2", itemversionid);
            ab.setAction("Attachments_" + itemid, "DeleteSDIAttachment", "1", limsProps);
            activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("targetsdcid", itemsdcid);
            activityProps.setProperty("targetkeyid1", itemid);
            activityProps.setProperty("targetkeyid2", itemversionid);
            activityProps.setProperty("activitytype", "Delete");
            activityProps.setProperty("activitylog", "Deleted all attachments");
            ab.setActionClass("AttachmentsActivityLog_" + itemid,
                AddWorksheetActivity.class.getName(), activityProps);
        }

        if (resetLIMSData && !itemsdcid.equals("LV_WorksheetSection")) {
            limsProps = new PropertyList();
            limsProps.setProperty("worksheetid", worksheetid);
            limsProps.setProperty("worksheetversionid", worksheetversionid);
            if (!controlTemplate && !itemsdcid.equals("LV_Worksheet")) {
                limsProps.setProperty("worksheetitemid", itemid);
                limsProps.setProperty("worksheetitemversionid", itemversionid);
                ab.setActionClass("LIMSData_" + itemid, DeleteWorksheetItemSDI.class.getName(),
                    limsProps);
            } else {
                ab.setActionClass("LIMSData_" + itemid, DeleteWorksheetSDI.class.getName(),
                    limsProps);
            }
        }

    }

    private void executeActionBlock(CommandRequest commandRequest, CommandResponse commandResponse,
        DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");
        String worksheetitemid = commandRequest.getString("worksheetitemid");
        String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
        String keyid1 = commandRequest.getString("keyid1");
        String keyid2 = commandRequest.getString("keyid2");
        String keyid3 = commandRequest.getString("keyid3");
        PropertyList operation = commandRequest.getPropertyList("operation");
        String sdcOperation = operation.getProperty("sdcoperation");
        PropertyList filter = operation.getPropertyListNotNull("sdifilter");

        try {
            String actionblock = commandRequest.getString("actionblock");
            actionblock = StringUtil.replaceAll(actionblock, "[worksheetid]", worksheetid);
            actionblock = StringUtil.replaceAll(actionblock, "[worksheetversionid]",
                worksheetversionid);
            actionblock = StringUtil.replaceAll(actionblock, "[worksheetitemid]", worksheetitemid);
            actionblock = StringUtil.replaceAll(actionblock, "[worksheetitemversionid]",
                worksheetitemversionid);
            DataSet item = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT * FROM worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ?",
                new Object[]{worksheetitemid, worksheetitemversionid}, true);
            WorksheetItem worksheetItem = WorksheetItemFactory.getInstance(this.sapphireConnection,
                database, (HashMap) item.get(0));
            boolean cont = true;
            if (worksheetItem.getWorksheetItemOptions().getOption("supportssdis").equals("Y")) {
                String sdcid = worksheetItem.getWorksheetItemOptions().getOption("defaultsdcid");
                actionblock = StringUtil.replaceAll(actionblock, "[sdcid]", sdcid);
                PropertyList config = worksheetItem.getConfig();
                String source = commandRequest.getString("source",
                    config.getProperty("source", "Control"));
                DataSet sdiList = null;
                if (source.equalsIgnoreCase("worksheet")) {
                    sdiList = this.loadWorksheetSDIs(worksheetid, worksheetversionid, sdcid,
                        (PropertyList) null, filter, keyid1, keyid2, keyid3);
                } else {
                    sdiList = this.loadItemSDIs(worksheetitemid, worksheetitemversionid, sdcid,
                        (PropertyList) null, filter, keyid1, keyid2, keyid3);
                }

                if (sdcOperation.length() > 0 && sdiList.size() > 0 && !this.hasSecurityAccess(
                    sdcid, sdiList, sdcOperation)) {
                    commandResponse.setStatusFail(
                        "This operation is not permitted on the " + this.getSDCProcessor()
                            .getProperty(sdcid, "plural") + " in this control");
                    cont = false;
                }

                actionblock = StringUtil.replaceAll(actionblock, "[keyid1]",
                    sdiList.getColumnValues("keyid1", ";"));
                actionblock = StringUtil.replaceAll(actionblock, "[keyid2]",
                    sdiList.getColumnValues("keyid2", ";"));
                actionblock = StringUtil.replaceAll(actionblock, "[keyid3]",
                    sdiList.getColumnValues("keyid3", ";"));
            }

            if (cont) {
                ActionBlock ab = new ActionBlock(actionblock);
                this.getActionProcessor().processActionBlock(ab);
            }
        } catch (Exception var22) {
            commandResponse.setStatusFail("Failed to execute action block for worksheet item "
                + BaseELNAction.getIdVersionText(worksheetitemid, worksheetitemversionid)
                + ". Reason: " + var22.getMessage(), var22);
        }

    }

    public static void updateMRUList(ConfigurationProcessor configurationProcessor,
        String sysuserid, String propertyid, String key) throws SapphireException {
        String mruList = configurationProcessor.getProfileProperty(sysuserid, propertyid);
        int pos = mruList.indexOf(key);
        if (pos == -1) {
            mruList = key + (mruList.length() > 0 ? "|" + mruList : "");
        } else if (pos > 0) {
            int pos2 = mruList.indexOf("|", pos + 1);
            mruList =
                key + "|" + mruList.substring(0, pos - 1) + (pos2 > -1 ? mruList.substring(pos2)
                    : "");
        }

        if (mruList.length() > 200) {
            mruList = mruList.substring(0, mruList.lastIndexOf("|"));
        }

        configurationProcessor.setProfileProperty(sysuserid, propertyid, mruList);
    }

    private DataSet loadMRUList(String propertyid, String query, int limit)
        throws SapphireException {
        ConfigurationProcessor cp = new ConfigurationProcessor(
            this.sapphireConnection.getConnectionId());
        String[] recent = StringUtil.split(
            cp.getProfileProperty(this.sapphireConnection.getSysuserId(), propertyid), "|");
        DataSet recents = new DataSet();
        StringBuffer where = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        if (recent.length > 0 && recent[0].length() > 0) {
            for (int i = 0; i < recent.length; ++i) {
                String[] parts = StringUtil.split(recent[i], ";");
                if (parts.length == 2) {
                    where.append(" OR ").append("(worksheetid=").append(safeSQL.addVar(parts[0]))
                        .append(" AND worksheetversionid=").append(safeSQL.addVar(parts[1]))
                        .append(")");
                }
            }

            DataSet temp = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT worksheetid, worksheetversionid, worksheetname, worksheetstatus, authorid, authordt, templateprivacyflag, "
                    + Worksheet.getNoteStatusClause("LV_Worksheet", "worksheet", "worksheetid",
                    "worksheetversionid")
                    + " FROM worksheet WHERE ( ( templateprivacyflag = 'G' AND versionstatus IN ("
                    + this.getGlobalVersionStatusList()
                    + " ) ) OR templateprivacyflag IS NULL OR ( templateprivacyflag = 'O' AND worksheet.versionstatus IN ('P', 'A', 'C') ) )  AND ("
                    + where.substring(4) + ")" + (query.length() > 0 ?
                    " AND Lower( worksheetname ) like " + safeSQL.addVar(
                        "'%" + query.toLowerCase() + "%'") : ""), safeSQL.getValues());
            HashMap findMap = new HashMap();

            for (int i = 0; i < recent.length && recents.size() <= limit; ++i) {
                String[] parts = StringUtil.split(recent[i], ";");
                if (parts.length == 2) {
                    findMap.put("worksheetid", parts[0]);
                    findMap.put("worksheetversionid", parts[1]);
                    int findRow = temp.findRow(findMap);
                    if (findRow != -1) {
                        recents.copyRow(temp, findRow, 1);
                    }
                }
            }
        }

        return recents;
    }

    private String getGlobalVersionStatusList() {
        try {
            boolean isELNAdmin = this.sapphireConnection.getRoleList().contains("ELNAdmin");
            PropertyList policy = this.getConfigurationProcessor()
                .getPolicy("ELNPolicy", "Sapphire Custom");
            PropertyList globalTemplates = policy.getPropertyListNotNull("globaltemplates");
            boolean requiresApproval = globalTemplates.getProperty("requiresapproval", "N")
                .equals("Y");
            return !isELNAdmin && requiresApproval ? "'C'" : "'C', 'P', 'A'";
        } catch (SapphireException var5) {
            this.logError("Failed to load ELN policy: " + var5.getMessage(), var5);
            return "'C', 'P'";
        }
    }
}
