package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.eln.gwt.server.AddWorksheetActivity;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItem;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFactory;
import com.labvantage.sapphire.services.SapphireConnection;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIList;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class BaseGenerateWorksheet extends BaseELNAction {

    protected SapphireConnection sapphireConnection;
    private M18NUtil m18NUtil;
    protected DataSet template;
    private DataSet templatesections;
    private DataSet templateitems;
    protected PropertyList templateoptions;
    private ActionBlock ab = new ActionBlock();
    private int sectionSequence = 0;
    protected DataSet repeatset;
    protected DataSet workitemrepeatset;
    protected int repeatsetRow;
    String currentSDIWorkItemId = "";
    String currentSDIWorkItemInstance = "";
    private HashMap<String, PropertyList> worksheetSDIs = new HashMap();
    protected String templateid;
    protected String templateversionid;
    protected String workbookid;
    protected String workbookversionid;
    protected String workitemid;
    protected String workitemversionid;
    protected String authorflag = "C";
    protected String authorid;
    protected String sdiworkitemid;
    protected DataSet sdiworkitem;
    protected DataSet sdiworkitemitemworkitems;
    protected DataSet sdiworkitemitemparamlists;
    protected int sdiperworksheet = 100;
    boolean preview;
    protected String metadata_id;
    protected String metadata_value;
    protected String limsdata_sdcid;
    protected String limsdata_keyid1;
    protected String limsdata_keyid2;
    protected String limsdata_keyid3;

    public BaseGenerateWorksheet() {
    }

    public void processAction(PropertyList properties) throws SapphireException {
        String sysUserid = this.connectionInfo.getSysuserId();
        if (sysUserid == null || "".equals(sysUserid)) {
            sysUserid = "(system)";
        }

        if ("(system)".equals(sysUserid)) {
            throw new SapphireException(this.getTranslationProcessor()
                .translate("Action failed. LES Worksheet Generation can not access Current User."));
        } else {
            this.m18NUtil = new M18NUtil(this.connectionInfo);
            this.sapphireConnection = new SapphireConnection(this.database.getConnection(),
                this.connectionInfo);
            this.templateid = properties.getProperty("templateid");
            this.templateversionid = properties.getProperty("templateversionid");
            this.workbookid = properties.getProperty("workbookid");
            this.workbookversionid = properties.getProperty("workbookversionid");
            this.authorid = properties.getProperty("authorid");
            this.preview = properties.getProperty("preview").equals("Y");
            this.sdiperworksheet = Integer.parseInt(
                properties.getProperty("maxsdiperworksheet", "-9999"));
            this.metadata_id = properties.getProperty("metadata_id");
            this.metadata_value = properties.getProperty("metadata_value");
            this.limsdata_sdcid = properties.getProperty("limsdata_sdcid");
            this.limsdata_keyid1 = properties.getProperty("limsdata_keyid1");
            this.limsdata_keyid2 = properties.getProperty("limsdata_keyid2");
            this.limsdata_keyid3 = properties.getProperty("limsdata_keyid3");
        }
    }

    protected void loadTemplate(String sdcid, String keyid1, String keyid2, String keyid3,
        String rule) throws SapphireException {
        if (!this.preview) {
            DataSet sdiworksheetrule;
            if ((this.templateid == null || this.templateid.length() == 0) && rule != null
                && rule.length() > 0) {
                sdiworksheetrule = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT * FROM sdiworksheetrule WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND worksheetrule = ?",
                    new Object[]{sdcid, keyid1, keyid2, keyid3, rule});
            } else {
                String currentversionid = resolveVersion(this.getQueryProcessor(), this.templateid,
                    "", "worksheet");
                sdiworksheetrule = this.getQueryProcessor().getPreparedSqlDataSet(
                    "SELECT * FROM sdiworksheetrule WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND worksheetid = ?",
                    new Object[]{sdcid, keyid1, keyid2, keyid3, this.templateid});
                if (sdiworksheetrule.size() > 1 && rule != null && rule.length() > 0) {
                    HashMap filter = new HashMap();
                    filter.put("worksheetrule", rule);
                    sdiworksheetrule = sdiworksheetrule.getFilteredDataSet(filter);
                }

                int i = sdiworksheetrule.size() - 1;

                while (true) {
                    if (i < 0) {
                        if (sdiworksheetrule.size() == 1
                            && sdiworksheetrule.getValue(0, "worksheetversionid").length() == 0) {
                            sdiworksheetrule.setValue(0, "worksheetversionid", currentversionid);
                        }
                        break;
                    }

                    if (sdiworksheetrule.getValue(i, "worksheetversionid").length() > 0
                        && !this.templateversionid.equals(
                        sdiworksheetrule.getValue(i, "worksheetversionid"))
                        || sdiworksheetrule.getValue(i, "worksheetversionid").length() == 0
                        && this.templateversionid.length() > 0 && !this.templateversionid.equals(
                        currentversionid)) {
                        sdiworksheetrule.deleteRow(i);
                    }

                    --i;
                }
            }

            if (sdiworksheetrule.size() != 1) {
                throw new SapphireException("Worksheet template not found");
            }

            this.templateid = sdiworksheetrule.getValue(0, "worksheetid");
            this.templateversionid = resolveVersion(this.getQueryProcessor(), this.templateid,
                sdiworksheetrule.getValue(0, "worksheetversionid"), "worksheet");
            if (this.workbookid.length() == 0) {
                this.workbookid = sdiworksheetrule.getValue(0, "workbookid");
                this.workbookversionid = resolveVersion(this.getQueryProcessor(), this.workbookid,
                    sdiworksheetrule.getValue(0, "workbookversionid"), "workbook");
            }

            if (this.workbookid.length() == 0) {
                String[] userworkbook = getUserWorkbook(this.sapphireConnection.getSysuserId(),
                    this.database, this.getActionProcessor(),
                    new ConfigurationProcessor(this.sapphireConnection.getConnectionId()), true);
                this.workbookid = userworkbook[0];
                this.workbookversionid = userworkbook[1];
            }

            if (this.workbookversionid.length() == 0) {
                this.workbookversionid = "1";
            }

            this.authorflag = sdiworksheetrule.getValue(0, "authorflag", "C");
            this.sdiperworksheet = Integer.parseInt(
                sdiworksheetrule.getValue(0, "maxsdiperworksheet", "-9999"));
        }

        SDIData templateData = this.loadWorksheet(this.templateid, this.templateversionid,
            new PropertyList());
        this.template = templateData.getDataset("primary");
        SDIData sectionData = templateData.getSDIData("sections");
        this.templatesections = sectionData.getDataset("primary");
        SDIData itemData = templateData.getSDIData("items");
        this.templateitems = itemData.getDataset("primary");

        try {
            this.templateoptions = new PropertyList(
                new JSONObject(this.template.getValue(0, "options")));
        } catch (Exception var11) {
            this.templateoptions = new PropertyList();
            this.logger.error("Faile to parse worksheet options", var11);
        }

    }

    protected void startWorksheet(String defaultworksheetname) throws SapphireException {
        PropertyList substitutions = new PropertyList();
        if (this.metadata_id.length() > 0 && this.metadata_value.length() > 0) {
            String[] metadata_ids = StringUtil.split(this.metadata_id, ";");
            String[] metadata_values = StringUtil.split(this.metadata_value, ";");

            for (int i = 0; i < metadata_values.length; ++i) {
                substitutions.setProperty(metadata_ids[i], metadata_values[i]);
            }
        }

        substitutions.setProperty("templateid", this.template.getValue(0, "worksheetid"));
        substitutions.setProperty("templateversionid",
            this.template.getValue(0, "worksheetversionid"));
        substitutions.setProperty("workbookid", this.workbookid);
        substitutions.setProperty("workbookversionid", this.workbookversionid);
        substitutions.setProperty("templatename", this.template.getValue(0, "worksheetname"));
        substitutions.setProperty("workitemid", this.workitemid);
        substitutions.setProperty("workitemversionid", this.workitemversionid);
        PropertyList extra = this.getWorksheetNameSubstitutions();
        if (extra != null) {
            substitutions.putAll(extra);
        }

        String worksheetname = resolveWorksheetName(this.sapphireConnection,
            this.getSequenceProcessor(),
            this.templateoptions.getProperty("worksheetnametemplate", defaultworksheetname),
            substitutions, (DataSet) null);
        PropertyList wsProps = new PropertyList();
        wsProps.setProperty("sdcid", "LV_Worksheet");
        wsProps.setProperty("worksheetversionid", "1");
        wsProps.setProperty("worksheetdesc", worksheetname);
        wsProps.setProperty("worksheetname", worksheetname);
        wsProps.setProperty("authorid", this.authorid.length() > 0 ? this.authorid
            : (this.authorflag.equals("C") ? this.connectionInfo.getSysuserId() : "(null)"));
        wsProps.setProperty("authordt",
            wsProps.getProperty("authorid").equals("(null)") ? "(null)" : "now");
        wsProps.setProperty("workbookid", this.workbookid);
        wsProps.setProperty("workbookversionid", this.workbookversionid);
        wsProps.setProperty("worksheetstatus", "Pending");
        wsProps.setProperty("coreflag", "N");
        wsProps.setProperty("templateflag", "N");
        wsProps.setProperty("templatetypeflag", "(null)");
        wsProps.setProperty("templateprivacyflag", "(null)");
        wsProps.setProperty("templatekeyid1", this.template.getValue(0, "worksheetid"));
        wsProps.setProperty("templatekeyid2", this.template.getValue(0, "worksheetversionid"));
        wsProps.setProperty("templateid", this.template.getValue(0, "worksheetid"));
        wsProps.setProperty("templateversionid", this.template.getValue(0, "worksheetversionid"));
        wsProps.setProperty("copyattachment", "Y");
        wsProps.setProperty("excludeworksheetsdi", "Y");
        wsProps.setProperty("excludeworksheetcontributor", "Y");
        wsProps.setProperty("excludeworksheetactivitylog", "Y");
        wsProps.setProperty("worksheet_action", "Y");
        this.ab.setAction("AddWorksheet", "AddSDI", "1", wsProps);
        PropertyList wssActivityProps = new PropertyList();
        wssActivityProps.setProperty("worksheetid", "[$G{AddWorksheet.newkeyid1}]");
        wssActivityProps.setProperty("worksheetversionid", "[$G{AddWorksheet.newkeyid2}]");
        wssActivityProps.setProperty("targetsdcid", "LV_Worksheet");
        wssActivityProps.setProperty("targetkeyid1", "[$G{AddWorksheet.newkeyid1}]");
        wssActivityProps.setProperty("targetkeyid2", "[$G{AddWorksheet.newkeyid2}]");
        wssActivityProps.setProperty("activitytype", "Add");
        wssActivityProps.setProperty("activitylog", "Start Worksheet Generation");
        this.ab.setActionClass("EditSectionsActivityLog", AddWorksheetActivity.class.getName(),
            wssActivityProps);
    }

    protected void addWorksheetSDIs(String sdcid, String keyid1, String keyid2, String keyid3)
        throws SapphireException {
        PropertyList wssdiProps = new PropertyList();
        wssdiProps.setProperty("worksheetid", "[$G{AddWorksheet.newkeyid1}]");
        wssdiProps.setProperty("worksheetversionid", "[$G{AddWorksheet.newkeyid2}]");
        wssdiProps.setProperty("sdcid", sdcid);
        wssdiProps.setProperty("keyid1", keyid1);
        wssdiProps.setProperty("keyid2", keyid2);
        wssdiProps.setProperty("keyid3", keyid3);
        this.ab.setActionClass("AddWorksheetSDI_" + sdcid, AddWorksheetSDI.class.getName(),
            wssdiProps);
        this.worksheetSDIs.put(sdcid, wssdiProps);
    }

    protected void generateSections() throws SapphireException {
        for (int sectionrow = 0; sectionrow < this.templatesections.size();
            sectionrow = this.generateSection(sectionrow, false, "")[0]) {
        }

        this.getActionProcessor().processActionBlock(this.ab);
    }

    private int[] generateSection(int sectionrow, boolean bypassRepeat, String parentid)
        throws SapphireException {
        PropertyList wssOptions = null;

        try {
            wssOptions = new PropertyList(
                new JSONObject(this.templatesections.getString(sectionrow, "options")));
        } catch (JSONException var17) {
            throw new SapphireException("Failed to parse worksheet section options");
        }

        String repeat = wssOptions.getProperty("generatesectionrepeat");
        if (!bypassRepeat && repeat.length() > 0) {
            int start = sectionrow;
            int startlevel = this.templatesections.getInt(sectionrow, "sectionlevel");
            int subsections = 0;
            HashSet<String> done = new HashSet();
            if (!repeat.equals("SDIWorkItem_AllWorkItem") && !repeat.equals(
                "SDIWorkItem_WorkItem")) {
                this.repeatset = this.getRepeatSet(repeat, parentid);
            } else {
                this.workitemrepeatset = this.getRepeatSet(repeat, parentid);
                this.repeatset = this.workitemrepeatset;
            }

            if (this.repeatset != null) {
                for (int i = 0; i < this.repeatset.size(); ++i) {
                    String key = this.getRepeatKey(repeat, i);
                    if (!done.contains(key)) {
                        this.repeatsetRow = i;
                        String sdiworkitemid =
                            repeat.equals("SDIWorkItem_WorkItem") ? this.repeatset.getValue(i,
                                "itemkeyid1") : "";
                        String sdiworkiteminstance =
                            repeat.equals("SDIWorkItem_WorkItem") ? this.repeatset.getValue(i,
                                "iteminstance") : "";
                        if (sdiworkitemid.length() > 0 && sdiworkiteminstance.length() > 0) {
                            this.currentSDIWorkItemId = sdiworkitemid;
                            this.currentSDIWorkItemInstance = sdiworkiteminstance;
                        }

                        if (repeat.equals("SDIWorkItem_AllWorkItem")) {
                            this.workitemid = this.repeatset.getValue(i, "workitemid");
                            this.workitemversionid = this.repeatset.getValue(i,
                                "workitemversionid");
                        }

                        sectionrow = this.generateSection(sectionrow, true, sdiworkitemid)[0];

                        while (this.templatesections.getInt(sectionrow, "sectionlevel")
                            > startlevel) {
                            int[] ret = this.generateSection(sectionrow, false, sdiworkitemid);
                            sectionrow = ret[0];
                            if (i == 0) {
                                subsections += ret[1];
                            }
                        }

                        sectionrow = start;
                        done.add(key);
                        if (repeat.equals("SDIWorkItem_AllWorkItem") || repeat.equals(
                            "SDIWorkItem_WorkItem")) {
                            this.repeatset = this.workitemrepeatset;
                        }
                    }
                }
            }

            sectionrow += subsections + 1;
            return new int[]{sectionrow, subsections + 1};
        } else {
            PropertyList wssProps = new PropertyList();
            wssProps.setProperty("sdcid", "LV_WorksheetSection");
            wssProps.setProperty("worksheetsectionversionid", "1");
            wssProps.setProperty("worksheetid", "[$G{AddWorksheet.newkeyid1}]");
            wssProps.setProperty("worksheetversionid", "[$G{AddWorksheet.newkeyid2}]");
            wssProps.setProperty("worksheetsectiondesc", StringUtil.replaceAll(
                this.resolveSubstitutions(
                    this.templatesections.getValue(sectionrow, "worksheetsectiondesc")), ";", ","));
            wssProps.setProperty("sectionstatus", "InProgress");
            wssProps.setProperty("sectionlevel",
                this.templatesections.getValue(sectionrow, "sectionlevel"));
            wssProps.setProperty("availabilityflag", "Y");
            wssProps.setProperty("usersequence", String.valueOf(this.sectionSequence));
            wssProps.setProperty("templatekeyid1",
                this.templatesections.getValue(sectionrow, "worksheetsectionid"));
            wssProps.setProperty("templatekeyid2",
                this.templatesections.getValue(sectionrow, "worksheetsectionversionid"));
            wssProps.setProperty("templateid", this.template.getValue(0, "worksheetid"));
            wssProps.setProperty("templateversionid",
                this.template.getValue(0, "worksheetversionid"));
            wssProps.setProperty("copyattachment", "Y");
            wssProps.setProperty("worksheet_action", "Y");
            this.ab.setAction("AddWorksheetSection_" + this.sectionSequence, "AddSDI", "1",
                wssProps);
            this.templatesections.setString(sectionrow, "__sectionavailabilityflag",
                wssProps.getProperty("availabilityflag"));
            this.templatesections.setString(sectionrow, "__itemavailability",
                wssOptions.getProperty("itemavailability", "A"));
            HashMap filter = new HashMap();
            filter.put("worksheetsectionid",
                this.templatesections.getValue(sectionrow, "worksheetsectionid"));
            filter.put("worksheetsectionversionid",
                this.templatesections.getValue(sectionrow, "worksheetsectionversionid"));
            DataSet sectionitems = this.templateitems.getFilteredDataSet(filter);
            String sectionAvailabilityFlag = this.templatesections.getValue(sectionrow,
                "__sectionavailabilityflag", "Y");
            String itemAvailability = this.templatesections.getValue(sectionrow,
                "__itemavailability", "A");

            for (int j = 0; j < sectionitems.size(); ++j) {
                WorksheetItem worksheetItem = WorksheetItemFactory.getInstance(
                    this.sapphireConnection, (DBUtil) this.database, (HashMap) sectionitems.get(j));
                PropertyList config = new PropertyList();
                config.setPropertyList(
                    this.resolveSubstitutions(sectionitems.getClob(j, "config", "")));
                if (this.currentSDIWorkItemId.length() > 0 && config.getProperty("workitemid")
                    .equals(this.currentSDIWorkItemId)) {
                    config.setProperty("workiteminstance", this.currentSDIWorkItemInstance);
                }

                PropertyList wsiProps = new PropertyList();
                wsiProps.setProperty("sdcid", "LV_WorksheetItem");
                wsiProps.setProperty("worksheetitemversionid", "1");
                wsiProps.setProperty("worksheetid", "[$G{AddWorksheet.newkeyid1}]");
                wsiProps.setProperty("worksheetversionid", "[$G{AddWorksheet.newkeyid2}]");
                wsiProps.setProperty("worksheetsectionid",
                    "[$G{AddWorksheetSection_" + this.sectionSequence + ".newkeyid1}]");
                wsiProps.setProperty("worksheetsectionversionid",
                    "[$G{AddWorksheetSection_" + this.sectionSequence + ".newkeyid2}]");
                wsiProps.setProperty("itemstatus", "InProgress");
                wsiProps.setProperty("availabilityflag", j == 0 ? sectionAvailabilityFlag
                    : (itemAvailability.equals("S") ? "N"
                        : (itemAvailability.equals("R") ? "Y" : sectionAvailabilityFlag)));
                wsiProps.setProperty("config", config.toXMLString());
                if (sectionitems.getValue(j, "propertytreeid").equals("RichTextControl")) {
                    wsiProps.setProperty("contents",
                        this.resolveSubstitutions(sectionitems.getValue(j, "contents")));
                }

                wsiProps.setProperty("usersequence", String.valueOf(j));
                wsiProps.setProperty("templatekeyid1", sectionitems.getValue(j, "worksheetitemid"));
                wsiProps.setProperty("templatekeyid2",
                    sectionitems.getValue(j, "worksheetitemversionid"));
                wsiProps.setProperty("templateid", this.template.getValue(0, "worksheetid"));
                wsiProps.setProperty("templateversionid",
                    this.template.getValue(0, "worksheetversionid"));
                wsiProps.setProperty("excludeworksheetitemsdi", "Y");
                wsiProps.setProperty("copyattachment", "Y");
                wsiProps.setProperty("worksheet_action", "Y");
                this.ab.setBlockProperty("WSI_templateid_" + this.sectionSequence + "_" + j,
                    sectionitems.getValue(j, "worksheetitemid"));
                this.ab.setAction("AddWorksheetItem_" + this.sectionSequence + "_" + j, "AddSDI",
                    "1", wsiProps);
                String defaultsdc = worksheetItem.getWorksheetItemOptions()
                    .getOption("defaultsdcid");
                if (worksheetItem.getWorksheetItemOptions().getOption("supportssdis").equals("Y")
                    && defaultsdc.length() > 0 && this.worksheetSDIs.get(defaultsdc) != null
                    && config.getProperty("source").equalsIgnoreCase("control")) {
                    PropertyList wsisdiProps = ((PropertyList) this.worksheetSDIs.get(
                        defaultsdc)).copy();
                    if (wsisdiProps != null) {
                        wsisdiProps = wsisdiProps.copy();
                        wsisdiProps.setProperty("worksheetitemid",
                            "[$G{AddWorksheetItem_" + this.sectionSequence + "_" + j
                                + ".newkeyid1}]");
                        wsisdiProps.setProperty("worksheetitemversionid",
                            "[$G{AddWorksheetItem_" + this.sectionSequence + "_" + j
                                + ".newkeyid2}]");
                        this.ab.setActionClass(
                            "AddWorksheetItemSDI_" + this.sectionSequence + "_" + j,
                            AddWorksheetItemSDI.class.getName(), wsisdiProps);
                    }
                }
            }

            ++this.sectionSequence;
            ++sectionrow;
            return new int[]{sectionrow, 1};
        }
    }

    protected DataSet getRepeatSet(String repeat, String parentid) {
        if (repeat.equals("SDIWorkItem_WorkItem")) {
            if (this.sdiworkitemitemworkitems != null) {
                this.sdiworkitemitemworkitems.sort("keyid1, keyid2, keyid3, usersequence");
                return this.sdiworkitemitemworkitems;
            }

            new DataSet();
        } else if (repeat.equals("SDIWorkItem_AllWorkItem")) {
            if (this.sdiworkitem != null) {
                this.sdiworkitem.sort("usersequence");
                return this.sdiworkitem;
            }

            new DataSet();
        } else if (repeat.equals("SDIWorkItem_ParamList")) {
            DataSet repeatSet = null;
            if (this.sdiworkitemitemparamlists != null) {
                HashMap plFilterMap = new HashMap();
                if (parentid.length() > 0) {
                    plFilterMap.put("workitemid", parentid);
                    repeatSet = this.sdiworkitemitemparamlists.getFilteredDataSet(plFilterMap);
                } else {
                    plFilterMap.put("workitemid", this.workitemid);
                    repeatSet = this.sdiworkitemitemparamlists.getFilteredDataSet(plFilterMap);
                }

                repeatSet.sort("keyid1, keyid2, keyid3, usersequence");
            }

            return repeatSet;
        }

        return null;
    }

    protected String getRepeatKey(String repeat, int repeatRow) {
        if (!repeat.equals("SDIWorkItem_WorkItem") && !repeat.equals("SDIWorkItem_ParamList")) {
            return repeat.equals("SDIWorkItem_AllWorkItem") ?
                this.repeatset.getValue(repeatRow, "workitemid") + ";" + this.repeatset.getValue(
                    repeatRow, "workitemversionid") : null;
        } else {
            return this.repeatset.getValue(repeatRow, "itemkeyid1") + ";" + this.repeatset.getValue(
                repeatRow, "itemkeyid2") + ";" + this.repeatset.getValue(repeatRow, "itemkeyid3");
        }
    }

    protected String[] finalizeWorksheet() throws SapphireException {
        String worksheetid = this.ab.getActionProperty("AddWorksheet", "newkeyid1");
        String worksheetversionid = this.ab.getActionProperty("AddWorksheet", "newkeyid2");
        DataSet sdiattributes = new DataSet();
        DataSet attributecontrols = this.getQueryProcessor().getPreparedSqlDataSet(
            "SELECT worksheetitemid, worksheetitemversionid, config FROM worksheetitem WHERE propertytreeid = 'AttributesControl' AND worksheetid = ? AND worksheetversionid = ?",
            new Object[]{worksheetid, worksheetversionid}, true);

        for (int i = 0; i < attributecontrols.size(); ++i) {
            PropertyList config = new PropertyList();
            config.setPropertyList(attributecontrols.getClob(i, "config", ""));
            if (config.getProperty("attributemode").equalsIgnoreCase("worksheet")) {
                DataSet worksheetitemattributes = this.getAttributeControlAttributes(config);
                if (worksheetitemattributes != null) {
                    BaseSDIAttributeAction.coreCopyDownAttributes(sdiattributes,
                        worksheetitemattributes,
                        this.getSDCProcessor().getPropertyList("LV_WorksheetItem"),
                        attributecontrols.getValue(i, "worksheetitemid"),
                        attributecontrols.getValue(i, "worksheetitemversionid"), "(null)",
                        (HashMap) null, this.m18NUtil, this.logger);
                }
            }
        }

        if (sdiattributes.size() > 0) {
            DataSetUtil.insert(this.database, sdiattributes, "sdiattribute");
        }

        DataSet worksheetitemparams;
        if (this.metadata_id.length() > 0 && this.metadata_value.length() > 0) {
            worksheetitemparams = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT attributeid FROM sdiattribute WHERE sdcid='LV_Worksheet' AND keyid1=? AND keyid2=?",
                new String[]{worksheetid, worksheetversionid});
            StringBuffer addMetaData_id = new StringBuffer();
            StringBuffer addMetaData_value = new StringBuffer();
            String[] metadata_ids = StringUtil.split(this.metadata_id, ";");
            String[] metadata_values = StringUtil.split(this.metadata_value, ";");
            int count = 0;

            for (int i = 0; i < metadata_values.length; ++i) {
                if (worksheetitemparams.findRow("attributeid", metadata_ids[i]) >= 0) {
                    addMetaData_id.append(";").append(metadata_ids[i]);
                    addMetaData_value.append(";").append(metadata_values[i]);
                    ++count;
                }
            }

            if (addMetaData_id.length() > 0) {
                PropertyList addMetadataValues = new PropertyList();
                addMetadataValues.setProperty("sdcid", "LV_Worksheet");
                addMetadataValues.setProperty("keyid1", worksheetid);
                addMetadataValues.setProperty("keyid2", worksheetversionid);
                addMetadataValues.setProperty("attributeid", addMetaData_id.substring(1));
                addMetadataValues.setProperty("value", addMetaData_value.substring(1));
                addMetadataValues.setProperty("attributesdcid",
                    StringUtil.repeat(";LV_Worksheet", count).substring(1));
                addMetadataValues.setProperty("attributeinstance",
                    StringUtil.repeat(";1", count).substring(1));
                this.getActionProcessor().processAction("EditSDIAttribute", "1", addMetadataValues);
            }
        }

        if (this.limsdata_sdcid.length() > 0 && this.limsdata_keyid1.length() > 0) {
            worksheetitemparams = new DataSet();
            worksheetitemparams.addColumnValues("sdcid", 0, this.limsdata_sdcid, ";");
            worksheetitemparams.addColumnValues("keyid1", 0, this.limsdata_keyid1, ";");
            worksheetitemparams.addColumnValues("keyid2", 0, this.limsdata_keyid2, ";");
            worksheetitemparams.addColumnValues("keyid3", 0, this.limsdata_keyid3, ";");
            worksheetitemparams.padColumns();
            worksheetitemparams.sort("sdcid");
            ArrayList<DataSet> limsdataBySDC = worksheetitemparams.getGroupedDataSets("sdcid");
            Iterator var17 = limsdataBySDC.iterator();

            while (var17.hasNext()) {
                DataSet limsdataForSDC = (DataSet) var17.next();
                String sdcid = limsdataForSDC.getValue(0, "sdcid");
                PropertyList addwsi = new PropertyList();
                addwsi.setProperty("worksheetid", worksheetid);
                addwsi.setProperty("worksheetversionid", worksheetversionid);
                addwsi.setProperty("sdcid", sdcid);
                addwsi.setProperty("keyid1", limsdataForSDC.getColumnValues("keyid1", ";"));
                addwsi.setProperty("keyid2", limsdataForSDC.getColumnValues("keyid2", ";"));
                addwsi.setProperty("keyid3", limsdataForSDC.getColumnValues("keyid3", ";"));
                this.getActionProcessor()
                    .processActionClass(AddWorksheetSDI.class.getName(), addwsi);
            }
        }

        worksheetitemparams = this.getQueryProcessor().getPreparedSqlDataSet(
            "SELECT worksheetitemparam.* FROM worksheetitemparam, worksheetitem WHERE worksheetitemparam.worksheetitemid = worksheetitem.worksheetitemid AND worksheetitemparam.worksheetitemversionid = worksheetitem.worksheetitemversionid   AND worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ?",
            new Object[]{worksheetid, worksheetversionid});

        for (int i = 0; i < worksheetitemparams.size(); ++i) {
            String value = this.getSubstitution(worksheetitemparams.getValue(i, "paramname"));
            if (value.length() > 0) {
                worksheetitemparams.setValue(i, "paramvalue", value);
            }
        }

        DataSetUtil.update(this.database, worksheetitemparams, "worksheetitemparam",
            new String[]{"worksheetitemid", "worksheetitemversionid", "paramname"});
        return new String[]{worksheetid, worksheetversionid};
    }

    protected DataSet getAttributeControlAttributes(PropertyList config) {
        return null;
    }

    protected DataSet getWorkItemAttributeControlAttributes(PropertyList config) {
        String attributeid = config.getProperty("attributeid");
        String worksheetcontext = config.getProperty("worksheetcontext");
        String sourcerelation = config.getProperty("sourcerelation", "WorkItem");
        String workitemid = config.getProperty("workitemid");
        String workiteminstance = config.getProperty("workiteminstance");
        String sdiWIVersion = "";
        if (this.sdiworkitem != null && this.sdiworkitem.getRowCount() > 0) {
            HashMap find = new HashMap();
            find.put("workitemid", workitemid);
            if (workiteminstance.length() > 0) {
                try {
                    find.put("workiteminstance", new BigDecimal(workiteminstance));
                } catch (Exception var14) {
                }
            }

            int r = this.sdiworkitem.findRow(find);
            if (r > -1) {
                sdiWIVersion = this.sdiworkitem.getValue(r, "workitemversionid");
            }
        }

        String paramlistid = config.getProperty("paramlistid");
        String paramlistversionid = config.getProperty("paramlistversionid");
        String variantid = config.getProperty("variantid");
        StringBuffer sql = new StringBuffer();
        ArrayList params = new ArrayList();
        sql.append(
            "SELECT * FROM sdiattribute WHERE sdcid = 'WorkItem' AND keyid1 = ? AND keyid2 = ? AND attributesdcid = ? ");
        params.add(workitemid);
        params.add(sdiWIVersion.length() > 0 ? sdiWIVersion : this.workitemversionid);
        params.add("LV_WorksheetItem");
        if (sourcerelation.equalsIgnoreCase("ParamList")) {
            sql.append(
                " AND copydowncontext = ( SELECT workitemitemid FROM workitemitem WHERE workitemid = ? and workitemversionid = ? AND sdcid = 'ParamList' AND keyid1 = ? AND ( keyid2 = ? OR keyid2 = 'C') AND keyid3 = ? ) ");
            params.add(workitemid);
            params.add(sdiWIVersion.length() > 0 ? sdiWIVersion : this.workitemversionid);
            params.add(paramlistid);
            params.add(paramlistversionid);
            params.add(variantid);
        } else {
            sql.append(" AND copydowncontext IS NULL ");
        }

        if (attributeid.length() > 0) {
            sql.append(" AND attributeid = ?");
            params.add(attributeid);
        }

        if (worksheetcontext.length() > 0) {
            sql.append(" AND worksheetcontext = ?");
            params.add(worksheetcontext);
        }

        DataSet ds = this.getQueryProcessor()
            .getPreparedSqlDataSet(sql.toString(), params.toArray());
        return ds;
    }

    protected PropertyList getWorksheetNameSubstitutions() {
        return null;
    }

    private String resolveSubstitutions(String value) throws SapphireException {
        if (value != null && value.length() > 0 && value.contains("$S{") && value.contains("}")) {
            String[] tokens = StringUtil.getTokens(value, "$S{", "}", false);
            if (tokens.length > 0) {
                String[] var3 = tokens;
                int var4 = tokens.length;

                for (int var5 = 0; var5 < var4; ++var5) {
                    String token = var3[var5];
                    String replaceWith = "";
                    if (replaceWith.length() == 0) {
                        replaceWith = this.getSubstitution(token);
                    }

                    if (replaceWith != null && replaceWith.length() > 0) {
                        value = StringUtil.replaceAll(value, "$S{" + token + "}", replaceWith);
                    }
                }
            }

            return value;
        } else {
            return value;
        }
    }

    protected String getSubstitution(String token) {
        return "";
    }

    protected DataSet loadWorkItem(String sdiworkitemid) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        DataSet workitems = this.getQueryProcessor().getPreparedSqlDataSet(
            "SELECT DISTINCT workitem.workitemid, workitem.workitemversionid FROM sdiworkitem, workitem WHERE sdiworkitem.workitemid = workitem.workitemid AND sdiworkitem.workitemversionid = workitem.workitemversionid AND sdiworkitemid IN ("
                + safeSQL.addIn(sdiworkitemid, ";") + ")", safeSQL.getValues());
        if (workitems.size() == 1) {
            this.workitemid = workitems.getValue(0, "workitemid");
            this.workitemversionid = workitems.getValue(0, "workitemversionid");
            return workitems;
        } else {
            throw new SapphireException(
                (workitems.size() > 1 ? "Multiple workitems found" : "No workitem found")
                    + " for the passed in sdiworkitemid: " + sdiworkitemid);
        }
    }

    protected DataSet loadWorkItems(String sdiworkitemid) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        DataSet workitems = this.getQueryProcessor().getPreparedSqlDataSet(
            "SELECT DISTINCT workitem.workitemid, workitem.workitemversionid FROM sdiworkitem, workitem WHERE sdiworkitem.workitemid = workitem.workitemid AND sdiworkitem.workitemversionid = workitem.workitemversionid AND sdiworkitemid IN ("
                + safeSQL.addIn(sdiworkitemid, ";") + ")", safeSQL.getValues());
        return workitems;
    }

    protected void loadWorkItemSamples(String sdiworkitemid) throws SapphireException {
        this.sdiworkitemid = sdiworkitemid;
        SDIList sdiList = new SDIList();
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        List<String> list = this.getSDIWIIDListBySupportedLimit(sdiworkitemid);
        Iterator var6 = list.iterator();

        while (var6.hasNext()) {
            String sdiwiid = (String) var6.next();
            sql.setLength(0);
            safeSQL.reset();
            sql.append(this.database.isOracle() ?
                "SELECT  sdcid, keyid1, keyid2, keyid3 FROM sdiworkitem, TABLE (LV_OrderTab ("
                    + safeSQL.addVar(sdiwiid) + ")) t "
                : "SELECT sdcid, keyid1, keyid2, keyid3 FROM sdiworkitem, LV_OrderTab ("
                    + safeSQL.addVar(sdiwiid) + ",default,default,default) t ");
            sql.append(" WHERE sdiworkitem.sdiworkitemid = t.id_value ORDER BY t.seq_value");
            this.database.createPreparedResultSet(sql.toString(), safeSQL.getValues());

            while (this.database.getNext()) {
                sdiList.setSdcid(this.database.getValue("sdcid"));
                sdiList.addSDI(this.database.getValue("keyid1"), this.database.getValue("keyid2"),
                    this.database.getValue("keyid3"));
            }
        }

        if (sdiList.size() > 0) {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDIList(sdiList);
            sdiRequest.setRequestItem("primary");
            sdiRequest.setRequestItem("sdiworkitem");
            sdiRequest.setRequestItem("sdiworkitemitem");
            sdiRequest.setRequestItem("datarelation");
            sdiRequest.setRequestItem("workitemrelation");
            sdiRequest.setRequestItem("attribute");
            sdiRequest.setExtendedDataTypes(true);
            SDIData workitemData = this.getSDIProcessor().getSDIData(sdiRequest);
            this.sdiworkitem = workitemData.getDataset("sdiworkitem");
            this.sdiworkitem.sort("workitemtypeflag");
            HashMap filterMap = new HashMap();
            filterMap.put("itemsdcid", "WorkItem");
            this.sdiworkitemitemworkitems = workitemData.getDataset("sdiworkitemitem")
                .getFilteredDataSet(filterMap);
            filterMap.put("itemsdcid", "ParamList");
            this.sdiworkitemitemparamlists = workitemData.getDataset("sdiworkitemitem")
                .getFilteredDataSet(filterMap);
            this.addWorksheetSDIs(sdiList.getSdcid(), sdiList.getKeyid1(), sdiList.getKeyid2(),
                sdiList.getKeyid3());
            this.addWorksheetSDIs("SDIWorkItem", sdiworkitemid, "", "");
        } else {
            throw new SapphireException(
                "Parent SDIs not found for sdiworkitemid: " + sdiworkitemid);
        }
    }

    protected void loadWorkOrderSamples(String workorderid) throws SapphireException {
        SDIList sdiList = new SDIList();
        this.database.createPreparedResultSet(
            "SELECT s_sampleid from s_sample where workorderid = ?", new String[]{workorderid});

        while (this.database.getNext()) {
            sdiList.setSdcid("Sample");
            sdiList.addSDI(this.database.getValue("s_sampleid"), "(null)", "(null)");
        }

        if (sdiList.size() > 0) {
            this.addWorksheetSDIs(sdiList.getSdcid(), sdiList.getKeyid1(), sdiList.getKeyid2(),
                sdiList.getKeyid3());
        }

    }

    protected void setTemplateSectionsItems(SDIData templateData) {
        this.template = templateData.getDataset("primary");
        SDIData sectionData = templateData.getSDIData("sections");
        this.templatesections = sectionData.getDataset("primary");
        SDIData itemData = templateData.getSDIData("items");
        this.templateitems = itemData.getDataset("primary");

        try {
            this.templateoptions = new PropertyList(
                new JSONObject(this.template.getValue(0, "options")));
        } catch (Exception var5) {
            this.templateoptions = new PropertyList();
            this.logger.error("Faile to parse worksheet options", var5);
        }

    }

    public List<String> getSDIWIIDListBySupportedLimit(String sdiworkitemid) {
        ArrayList<String> arrayList = new ArrayList();

        int lastIndx;
        for (int limit = 4000; sdiworkitemid.length() > limit;
            sdiworkitemid = sdiworkitemid.substring(lastIndx + 1)) {
            String temp = sdiworkitemid.substring(0, limit);
            lastIndx = temp.lastIndexOf(";");
            temp = sdiworkitemid.substring(0, lastIndx);
            arrayList.add(temp);
        }

        arrayList.add(sdiworkitemid);
        return arrayList;
    }

    protected void renewActionBlock() {
        this.ab = new ActionBlock();
    }
}
