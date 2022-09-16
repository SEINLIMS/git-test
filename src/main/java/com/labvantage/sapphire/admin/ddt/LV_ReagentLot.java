package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.DeleteSDI;
import com.labvantage.sapphire.modules.reagent.ReagentUtil;
import com.labvantage.sapphire.util.samplingplan.SamplingPlanUtil;
import com.labvantage.sapphire.util.sdiapproval.ApprovalUtil;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_ReagentLot extends BaseSDCRules {

    final String LABVANTAGE_CVS_ID = "$Revision: 82801 $";
    private String ruleid = "ReagentLotRule";
    public static final String SDCID = "LV_ReagentLot";
    public static final String KEYID1 = "keyid1";
    private static final String CONTAINERTRACKING_TOGETHER = "T";
    private static final String TRACKITEM_QTYCURRENTTYPE_U = "U";
    private static final String TRACKITEM_QTYCURRENTTYPE_C = "C";
    private static final String REORDEREDSCOPE_PERTYPE = "T";
    private static final String REORDEREDSCOPE_PERLOT = "L";
    private static final String PROPERTY_STATUS_INITIAL = "Initial";
    private static final String PROPERTY_STATUS_PENDINGAPPROVAL = "PendingApproval";
    private static final String PROPERTY_STATUS_ACTIVE = "Active";
    private static final String PROPERTY_STATUS_INACTIVE = "Inactive";
    private static final String PROPERTY_STATUS_REJECTED = "Rejected";
    private static final String PROPERTY_STATUS_EXPIRED = "Expired";
    private static final String PROPERTY_STATUS_CANCELLED = "Cancelled";
    private static final String COLUMN_TARGETCONCPARAMID = "targetconcentrationparamid";
    private static final String COLUMN_TARGETCONCPARAMTYPE = "targetconcentrationparamtype";
    private static final String COLUMN_TARGETCONC = "targetconcentration";
    private static final String COLUMN_ACTUALCONC = "actualconcentration";
    private static final String COLUMN_ACTIVEMATERIAL = "activematerialid";
    private static final String COLUMN_PERCENTOFTARGETCONC = "percentoftargetconcentration";

    public LV_ReagentLot() {
    }

    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.enterReagentPostAddDetail(sdiData, actionProps);
        this.addQualitySample(primary);
        this.setTrackItemStatus(primary);
        this.setCertificationStatus(primary);
        this.setApprovalStatus(primary);
    }

    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.setDefaultValues(primary);
        this.setReagentDates(primary);
        this.setReorderedFlag(primary);
        this.setReagentStatus(primary, (DataSet) null);
        this.setUnitType(primary, true);
        this.setConcentration(primary);
    }

    private void setUnitType(DataSet primary, boolean add) {
        if (!primary.isValidColumn("amountinitialunitstype")) {
            primary.addColumn("amountinitialunitstype", 0);
        }

        for (int i = 0; i < primary.size(); ++i) {
            if (add || this.hasPrimaryValueChanged(primary, i, "amountinitialunits")) {
                if ("(Containers)".equals(primary.getValue(i, "amountinitialunits"))
                    || add && "C".equals(primary.getValue(i, "amountinitialunitstype"))) {
                    primary.setValue(i, "amountinitialunits", "");
                    primary.setValue(i, "amountinitialunitstype", "C");
                } else {
                    primary.setValue(i, "amountinitialunitstype",
                        StringUtil.getLen(primary.getValue(i, "amountinitialunits")) > 0L ? "U"
                            : "");
                }
            }
        }

    }

    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        SDIData beforeImage = this.getBeforeEditImage();
        DataSet oldPrimary = beforeImage.getDataset("primary");
        this.setReagentStatus(primary, oldPrimary);
        this.setReagentDates(primary);
        this.setUnitType(primary, false);
    }

    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.addQualitySample(primary);
        this.callReagentEvent(sdiData);
        this.setTrackItemStatus(primary);
        this.setCertificationStatus(primary);
        this.setApprovalStatus(primary);
        this.setExpiryDate(primary);
        this.cancelQualitySample(primary);
    }

    private void cancelQualitySample(DataSet primary) throws SapphireException {
        PreparedStatement psReagentQualitySample = this.database.prepareStatement(
            "getReagentQualitySample", "select s_sampleid from s_sample where reagentlotid=?");

        try {
            for (int i = 0; i < primary.size(); ++i) {
                String reagentlotstatus = primary.getValue(i, "reagentstatus", "");
                String reagentlotid = primary.getValue(i, "reagentlotid", "");
                if (this.hasPrimaryValueChanged(primary, i, "reagentstatus")
                    && reagentlotstatus.equalsIgnoreCase("Cancelled")) {
                    psReagentQualitySample.setString(1, reagentlotid);
                    DataSet dsReagentQualitySample = new DataSet(
                        psReagentQualitySample.executeQuery());
                    if (dsReagentQualitySample.size() > 0) {
                        String sampleids = dsReagentQualitySample.getColumnValues("s_sampleid",
                            ";");
                        PropertyList sampleProps = new PropertyList();
                        sampleProps.setProperty("sdcid", "Sample");
                        sampleProps.setProperty("keyid1", sampleids);
                        sampleProps.setProperty("samplestatus", "Cancelled");
                        this.getActionProcessor().processAction("EditSDI", "1", sampleProps);
                    }
                }
            }
        } catch (Exception var12) {
            throw new SapphireException("Unable to cancel Reagent Quality Sample: " + var12);
        } finally {
            this.database.closeStatement("getReagentQualitySample");
        }

    }

    private void setExpiryDate(DataSet primary) throws ActionException {
        HashMap map = new HashMap();

        for (int i = 0; i < primary.size(); ++i) {
            if (this.hasPrimaryValueChanged(primary, i, "expirydt")) {
                map.put(primary.getValue(i, "reagentlotid"), primary.getValue(i, "expirydt"));
            }
        }

        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("select t.trackitemid, rl.reagentlotid");
        sql.append(" from trackitem t, reagentlot rl");
        sql.append(" where t.linksdcid = 'LV_ReagentLot'");
        sql.append(" and t.linkkeyid1 = rl.reagentlotid");
        sql.append(" and rl.contentflag = 'K'");
        sql.append(" and rl.reagentlotid in (")
            .append(safeSQL.addIn(OpalUtil.toDelimitedString(map.keySet(), "','"))).append(")");
        DataSet ds = this.getQueryProcessor()
            .getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            ds.addColumn("expirydt", 0);

            for (int i = 0; i < ds.size(); ++i) {
                ds.setValue(i, "expirydt", (String) map.get(ds.getValue(i, "reagentlotid")));
            }

            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "TrackItemSDC");
            props.setProperty("keyid1", ds.getColumnValues("trackitemid", ";"));
            props.setProperty("expirydt", ds.getColumnValues("expirydt", ";"));
            this.getActionProcessor().processAction("EditSDI", "1", props);
        }

    }

    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sql =
            "SELECT trackitemid FROM trackitem WHERE linksdcid='LV_ReagentLot' AND linkkeyid1 IN (SELECT keyid1 FROM rsetitems WHERE rsetid = "
                + safeSQL.addVar(rsetid) + " )";
        DataSet linkedtrackitemDS = this.getQueryProcessor()
            .getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (linkedtrackitemDS != null && linkedtrackitemDS.size() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "TrackItemSDC");
            props.setProperty("keyid1", linkedtrackitemDS.getColumnValues("trackitemid", ";"));
            this.getActionProcessor().processActionClass(DeleteSDI.class.getName(), props);
        }

        sql = "DELETE FROM sdidatarelation WHERE tosdcid='LV_ReagentLot' AND tokeyid1 IN (SELECT keyid1 FROM rsetitems WHERE rsetid = ? )";
        this.database.executePreparedUpdate(sql, new Object[]{rsetid});
        sql = "DELETE FROM s_sdicertification WHERE resourcesdcid='LV_ReagentLot' AND resourcekeyid1 IN (SELECT keyid1 FROM rsetitems WHERE rsetid = ? ) AND certificationtype = 'Reagent Certification'";
        this.database.executePreparedUpdate(sql, new Object[]{rsetid});
    }

    private void setDefaultValues(DataSet primary) throws SapphireException {
        for (int i = 0; i < primary.size(); ++i) {
            String reagentTypeid = primary.getString(i, "reagenttypeid");
            String reagentTypeVersionid = primary.getString(i, "reagenttypeversionid");
            if (reagentTypeid != null && reagentTypeVersionid != null) {
                this.database = ReagentUtil.getReagentTypeDetail(this.database, reagentTypeid,
                    reagentTypeVersionid);
            } else {
                this.setError(this.ruleid, "VALIDATION",
                    "Please specify the Reagent Type to create the Reagent Lot");
            }

            String amountInitial = primary.getValue(i, "amountinitial",
                this.database.getValue("amountexpected"));
            String amountinitialUnits = primary.getValue(i, "amountinitialunits",
                this.database.getValue("amountexpectedunits"));
            String amountinitialUnitsType = primary.getValue(i, "amountinitialunitstype",
                this.database.getValue("amountexpectedunitstype"));
            String noOfContainer = primary.getValue(i, "containersinitial",
                primary.isValidColumn("containersinitial") ? ""
                    : this.database.getValue("containersexpected"));
            String mixedBy = primary.getValue(i, "mixedby",
                this.getConnectionInfo().getSysuserId());
            String mixedDt = primary.getValue(i, "mixeddt", "");
            String receivedBy = primary.getValue(i, "receivedby",
                this.getConnectionInfo().getSysuserId());
            String receivedDt = primary.getValue(i, "receiveddt", "");
            String purchasedFlag = primary.getValue(i, "purchasedflag", "N");
            String trackitemExpiryPeriodReqFlag = this.database.getValue(
                "trackitemexpiryperiodreqflag");
            String qualitySampleReqFlag =
                this.database.getValue("qualitysamplereqflag").trim().length() == 0 ? "N"
                    : this.database.getValue("qualitysamplereqflag");
            String parentLotid = primary.getValue(i, "parentid", "");
            String containertypeid = primary.getValue(i, "containertypeid",
                this.database.getValue("containertypeid"));
            String containerTrackingFlag = primary.getValue(i, "containertrackingflag",
                this.database.getValue("containertrackingflag"));
            String contentflag = primary.getValue(i, "contentflag",
                this.database.getValue("contentflag"));
            if (!primary.isValidColumn("amountinitial")) {
                primary.addColumn("amountinitial", 1);
            }

            primary.setValue(i, "amountinitial", String.valueOf(amountInitial));
            if (!primary.isValidColumn("amountinitialunits")) {
                primary.addColumn("amountinitialunits", 0);
            }

            primary.setValue(i, "amountinitialunits", amountinitialUnits);
            if (!primary.isValidColumn("amountinitialunitstype")) {
                primary.addColumn("amountinitialunitstype", 0);
            }

            primary.setValue(i, "amountinitialunitstype", amountinitialUnitsType);
            if (!primary.isValidColumn("containersinitial")) {
                primary.addColumn("containersinitial", 1);
            }

            primary.setValue(i, "containersinitial", String.valueOf(noOfContainer));
            if (purchasedFlag.equalsIgnoreCase("Y")) {
                if (!primary.isValidColumn("receivedby")) {
                    primary.addColumn("receivedby", 0);
                }

                primary.setValue(i, "receivedby", receivedBy);
                if (!primary.isValidColumn("receiveddt")) {
                    primary.addColumn("receiveddt", 2);
                }

                if (receivedDt.length() == 0) {
                    primary.setValue(i, "receiveddt", "NOW");
                }
            } else {
                if (!primary.isValidColumn("mixedby")) {
                    primary.addColumn("mixedby", 0);
                }

                primary.setValue(i, "mixedby", mixedBy);
                if (!primary.isValidColumn("mixeddt")) {
                    primary.addColumn("mixeddt", 2);
                }

                if (mixedDt.length() == 0) {
                    primary.setValue(i, "mixeddt", "NOW");
                }
            }

            if (!primary.isValidColumn("purchasedflag")) {
                primary.addColumn("purchasedflag", 0);
            }

            primary.setValue(i, "purchasedflag", purchasedFlag);
            if (!primary.isValidColumn("trackitemexpiryperiodreqflag")) {
                primary.addColumn("trackitemexpiryperiodreqflag", 0);
            }

            primary.setValue(i, "trackitemexpiryperiodreqflag", trackitemExpiryPeriodReqFlag);
            if (!primary.isValidColumn("qualitysamplereqflag")) {
                primary.addColumn("qualitysamplereqflag", 0);
            }

            primary.setValue(i, "qualitysamplereqflag", qualitySampleReqFlag);
            if (!primary.isValidColumn("parentid")) {
                primary.addColumn("parentid", 0);
            }

            primary.setValue(i, "parentid", parentLotid);
            if (!primary.isValidColumn("containertypeid")) {
                primary.addColumn("containertypeid", 0);
            }

            primary.setValue(i, "containertypeid", containertypeid);
            if (!primary.isValidColumn("containertrackingflag")) {
                primary.addColumn("containertrackingflag", 0);
            }

            primary.setValue(i, "containertrackingflag", containerTrackingFlag);
            if (!primary.isValidColumn("contentflag")) {
                primary.addColumn("contentflag", 0);
            }

            primary.setValue(i, "contentflag", contentflag);
        }

    }

    private void enterReagentPostAddDetail(SDIData sdiData, PropertyList actionProps)
        throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        boolean addFromTemplate = actionProps.containsKey("templateid")
            && actionProps.getProperty("templateid", "").length() > 0;

        for (int i = 0; i < primary.size(); ++i) {
            boolean isTemplate =
                addFromTemplate || primary.getString(i, "templateflag", "N").equalsIgnoreCase("Y");
            String reagentLotid = primary.getString(i, "reagentlotid");
            String reagentTypeid = primary.getString(i, "reagenttypeid");
            String containerTypeid = primary.getValue(i, "containertypeid");
            String reagentTypeVersionid = primary.getString(i, "reagenttypeversionid");
            double amountInitial = primary.getDouble(i, "amountinitial", 0.0);
            String amountinitialUnits = primary.getValue(i, "amountinitialunits");
            int noOfContainer = primary.getInt(i, "containersinitial", 0);
            boolean isVirtual = "V".equalsIgnoreCase(primary.getValue(i, "contentflag", "R"));
            double containerSize = 0.0;
            String containersizeUnit = "";
            if (containerTypeid.length() > 0) {
                String sqlContainerSize = "SELECT sizevalue,sizeunits FROM containertype WHERE containertypeid=?";
                this.database.createPreparedResultSet(sqlContainerSize,
                    new Object[]{containerTypeid});
                if (this.database.getNext()) {
                    containerSize = this.database.getBigDecimal("sizevalue") == null ? 0.0
                        : this.database.getBigDecimal("sizevalue").doubleValue();
                    containersizeUnit = this.database.getValue("sizeunits");
                }
            }

            if (reagentTypeid != null && reagentTypeVersionid != null) {
                DataSet ds = ReagentUtil.getReagentTypeDetail(this.getQueryProcessor(),
                    reagentTypeid, reagentTypeVersionid);
                String paramlistids = ds.getValue(0, "paramlistid");
                String paramlistversionids = ds.getValue(0, "paramlistversionid", "C");
                String variantids = ds.getValue(0, "variantid");
                double expectedAmount = ds.getBigDecimal(0, "amountexpected") == null ? 0.0
                    : ds.getBigDecimal(0, "amountexpected").doubleValue();
                String expectedAmountUnit = ds.getValue(0, "amountexpectedunits");
                boolean isUnmanaged = "Y".equalsIgnoreCase(ds.getValue(0, "unmanagedflag"));
                boolean isUnitMaches = amountinitialUnits.equalsIgnoreCase(expectedAmountUnit);
                boolean isContainerUnitMatches = amountinitialUnits.equalsIgnoreCase(
                    containersizeUnit);
                String approvalTypeid = ds.getValue(0, "approvaltypeid");
                int containersExpected = ds.getInt(0, "containersexpected");
                String contentflag = ds.getValue(0, "contentflag", "R");
                String sqlReciepe = "SELECT * FROM reagenttyperecipe WHERE reagenttypeid=? AND reagenttypeversionid=? order by usersequence";
                this.database.createPreparedResultSet("reagenttype", sqlReciepe,
                    new Object[]{reagentTypeid, reagentTypeVersionid});
                double mulFactor = 0.0;
                double unitConvertContainerSize;
                if (isContainerUnitMatches) {
                    unitConvertContainerSize = containerSize;
                } else {
                    unitConvertContainerSize = ReagentUtil.getConvertedValue(containerSize,
                        containersizeUnit, amountinitialUnits, this.database, false);
                }

                try {
                    if (this.database.getNext("reagenttype")) {
                        if (isUnitMaches && expectedAmount > 0.0) {
                            mulFactor = amountInitial / expectedAmount;
                        } else if (amountInitial > 0.0 && expectedAmount > 0.0) {
                            mulFactor =
                                ReagentUtil.getConvertedValue(amountInitial, amountinitialUnits,
                                    expectedAmountUnit, this.database) / expectedAmount;
                        } else if (expectedAmount == 0.0 && noOfContainer > 0
                            && containersExpected > 0) {
                            mulFactor = (double) (noOfContainer / containersExpected);
                        } else if (expectedAmount == 0.0 && amountInitial > 0.0
                            && containersExpected > 0) {
                            if (isContainerUnitMatches) {
                                if (containerSize > 0.0) {
                                    noOfContainer = (int) (amountInitial / containerSize);
                                    if (amountInitial % containerSize > 0.0) {
                                        ++noOfContainer;
                                    }
                                } else {
                                    ++noOfContainer;
                                }
                            } else {
                                amountInitial = ReagentUtil.getConvertedValue(amountInitial,
                                    amountinitialUnits, containersizeUnit, this.database, false);
                                if (containerSize > 0.0) {
                                    noOfContainer = (int) (amountInitial / containerSize);
                                    if (amountInitial % containerSize > 0.0) {
                                        ++noOfContainer;
                                    }
                                } else {
                                    ++noOfContainer;
                                }
                            }

                            mulFactor = (double) (noOfContainer / containersExpected);
                        } else if (amountInitial == 0.0 && noOfContainer == 0) {
                            mulFactor = 1.0;
                        }
                    }
                } catch (Exception var42) {
                    throw new SapphireException("Error in Calculating the Multiplication Factor",
                        var42);
                } finally {
                    this.database.closeResultSet("reagenttype");
                }

                String var37 = primary.getValue(i, "parentid");
                boolean isClonedReagent = var37.trim().length() > 0;
                if (!isTemplate && !isVirtual) {
                    this.addReagentLotRecipe(mulFactor, isClonedReagent, reagentLotid,
                        reagentTypeid, reagentTypeVersionid);
                }

                PropertyList trackitemProps = this.getTrackitemInfo(primary, i,
                    ds.getValue(0, "maxfreezethawcount", ""),
                    ds.getValue(0, "warnfreezethawcount", ""));
                trackitemProps.setProperty("intialcontainer", String.valueOf(noOfContainer));
                trackitemProps.setProperty("amountInitial", String.valueOf(amountInitial));
                trackitemProps.setProperty("containerSize", String.valueOf(containerSize));
                trackitemProps.setProperty("unitConvertContainerSize",
                    String.valueOf(unitConvertContainerSize));
                trackitemProps.setProperty("containersizeUnit", containersizeUnit);
                trackitemProps.setProperty("amountinitialUnits", amountinitialUnits);
                if (!isClonedReagent && !isUnmanaged
                    && Integer.parseInt(trackitemProps.getProperty("nooftrackitem")) > 0) {
                    this.addTrackItem(trackitemProps, contentflag);
                }

                this.addReagentAttributes(paramlistids, paramlistversionids, variantids,
                    reagentLotid);
                if (approvalTypeid.trim().length() > 0 && !isTemplate) {
                    this.addReagentSDIApproval(approvalTypeid, reagentLotid);
                }

                this.addReagentCertification(reagentLotid);
                if (!isTemplate) {
                    this.addReagentLotStage(reagentLotid, reagentTypeid, reagentTypeVersionid);
                }
            } else {
                this.setError(this.ruleid, "VALIDATION",
                    "Please specify the Reagent Type to create the Reagent Lot");
            }
        }

    }

    private void addTrackItem(PropertyList props, String contentflag) throws SapphireException {
        try {
            char decimalSeparator = FormatUtil.getInstance(this.connectionInfo)
                .getDecimalSeparator();
            int noOfContainer = Integer.parseInt(props.getProperty("nooftrackitem"));
            int initialContainer = Integer.parseInt(props.getProperty("intialcontainer"));
            double amountInitial = Double.parseDouble(props.getProperty("amountInitial"));
            double containerSize = Double.parseDouble(props.getProperty("containerSize"));
            double convertedContainerSize = Double.parseDouble(
                props.getProperty("unitConvertContainerSize"));
            String containerUnit = props.getProperty("containersizeUnit");
            String amountUnit = props.getProperty("amountinitialUnits");
            boolean unitMatches = amountUnit.equals(containerUnit);
            if (initialContainer == 0 && amountInitial > 0.0 && noOfContainer > 1
                && amountUnit.trim().length() > 0) {
                for (int i = 0; i < noOfContainer; ++i) {
                    PropertyList trackitemProps = new PropertyList();
                    trackitemProps.setProperty("sdcid", "TrackItemSDC");
                    trackitemProps.setProperty("linksdcid", "LV_ReagentLot");
                    trackitemProps.setProperty("linkkeyid1", props.getProperty("reagentlotid"));
                    if (i == noOfContainer - 1) {
                        int scale = ReagentUtil.getMaxScale(
                            props.getProperty("amountInitial", "") + ";" + props.getProperty(
                                "containerSize"), decimalSeparator);
                        double amount = (new BigDecimal(
                            amountInitial - convertedContainerSize * (double) i)).setScale(scale, 4)
                            .doubleValue();
                        if (!unitMatches) {
                            amount = ReagentUtil.getConvertedValue(amount, amountUnit,
                                containerUnit, this.database, false);
                        }

                        trackitemProps.setProperty("qtycurrent",
                            String.valueOf(amount).replace('.', decimalSeparator));
                    } else {
                        trackitemProps.setProperty("qtycurrent",
                            String.valueOf(containerSize).replace('.', decimalSeparator));
                    }

                    trackitemProps.setProperty("qtycurrenttype",
                        props.getProperty("qtycurrenttype"));
                    trackitemProps.setProperty("qtyunits", props.getProperty("qtyunit"));
                    trackitemProps.setProperty("copies", "1");
                    trackitemProps.setProperty("containertypeid",
                        props.getProperty("containertypeid"));
                    trackitemProps.setProperty("expirydt",
                        props.getProperty("trackitemexpirydate"));
                    trackitemProps.setProperty("trackitemstatus", "Invalid");
                    this.getActionProcessor()
                        .processActionClass(AddSDI.class.getName(), trackitemProps);
                }
            } else {
                PropertyList trackitemProps = new PropertyList();
                trackitemProps.setProperty("sdcid", "TrackItemSDC");
                trackitemProps.setProperty("linksdcid", "LV_ReagentLot");
                trackitemProps.setProperty("linkkeyid1", props.getProperty("reagentlotid"));
                trackitemProps.setProperty("qtycurrent",
                    props.getProperty("qtycurrent", "0.0").replace('.', decimalSeparator));
                trackitemProps.setProperty("qtycurrenttype", props.getProperty("qtycurrenttype"));
                trackitemProps.setProperty("qtyunits", props.getProperty("qtyunit"));
                trackitemProps.setProperty("copies", props.getProperty("nooftrackitem"));
                trackitemProps.setProperty("containertypeid", props.getProperty("containertypeid"));
                trackitemProps.setProperty("expirydt", props.getProperty("trackitemexpirydate"));
                trackitemProps.setProperty("trackitemstatus", "Invalid");
                this.getActionProcessor()
                    .processActionClass(AddSDI.class.getName(), trackitemProps);
            }

        } catch (Exception var20) {
            throw new SapphireException("DB_ACTION_FAILED", "Unable to Add Trackitem" + var20);
        }
    }

    private void addQualitySample(DataSet primary) throws SapphireException {
        PreparedStatement sampleCount = this.database.prepareStatement("getsamplecount",
            "select s_sampleid from s_sample where reagentlotid=?");

        try {
            for (int i = 0; i < primary.size(); ++i) {
                String reagentLotid = primary.getValue(i, "reagentLotid");
                sampleCount.setString(1, reagentLotid);
                if (!sampleCount.executeQuery().next()) {
                    String reagentTypeid = primary.getString(i, "reagenttypeid");
                    String reagentTypeVersionid = primary.getString(i, "reagenttypeversionid");
                    if (reagentTypeid != null && reagentTypeVersionid != null) {
                        this.database = ReagentUtil.getReagentTypeDetail(this.database,
                            reagentTypeid, reagentTypeVersionid);
                        String qualitySampleTemplateid = this.database.getValue("qualitysampleid");
                        boolean isQualitySampleReq = "Y".equalsIgnoreCase(
                            primary.getValue(i, "qualitysamplereqflag"));
                        if (isQualitySampleReq) {
                            try {
                                PropertyList sampleProps = new PropertyList();
                                sampleProps.setProperty("reagentlotid", reagentLotid);
                                sampleProps.setProperty("sampletemplateid",
                                    qualitySampleTemplateid);
                                sampleProps.setProperty("samplestatus", "Initial");
                                this.getActionProcessor()
                                    .processAction("AddReagentSample", "1", sampleProps);
                            } catch (Exception var14) {
                                throw new SapphireException("DB_ACTION_FAILED",
                                    "Not able to add Reagent Quality Sample", var14);
                            }
                        }
                    }
                }
            }
        } catch (Exception var15) {
            this.logger.error("Error in executing method addQualitySample", var15);
            throw new SapphireException(ErrorUtil.extractMessageFromException(var15,
                ErrorUtil.isUserAdmin(this.getConnectionId())), var15);
        } finally {
            this.database.closeStatement("getsamplecount");
        }

    }

    private void setReagentDates(DataSet primary) throws SapphireException {
        for (int i = 0; i < primary.size(); ++i) {
            String reagentLotid = primary.getString(i, "reagentlotid");
            String reagentTypeid = primary.getString(i, "reagenttypeid");
            String reagentTypeVersionid = primary.getString(i, "reagenttypeversionid");
            String expiryDt = primary.getValue(i, "expirydt");
            primary.getValue(i, "expirywarningdt");
            String reorderedDate = primary.getValue(i, "reordereddt");
            String trackItemExpiryPeriodReq = primary.getValue(i, "trackitemexpiryperiodreqflag");
            if (reagentTypeid != null && reagentTypeVersionid != null) {
                DateTimeUtil dtu = new DateTimeUtil(this.connectionInfo);
                Calendar calculatedExpiryDt = Calendar.getInstance();
                this.database = ReagentUtil.getReagentTypeDetail(this.database, reagentTypeid,
                    reagentTypeVersionid);
                BigDecimal expiryPeriod = this.database.getBigDecimal("expiryperiod");
                String expiryPeriodUnit = this.database.getValue("expiryperiodunits");
                String expiryBasedOn = this.database.getValue("expirybasedon");
                Calendar expDatebasedOn = primary.getCalendar(i, expiryBasedOn);
                if (expDatebasedOn == null) {
                    expDatebasedOn = DateTimeUtil.getNowCalendar();
                }

                if (expiryPeriodUnit != null && expiryPeriodUnit.length() > 0
                    && expDatebasedOn != null && expiryPeriod != null
                    && expiryPeriod.doubleValue() > 0.0) {
                    calculatedExpiryDt = DateTimeUtil.getOffsetDate(expDatebasedOn,
                        expiryPeriodUnit, expiryPeriod);
                }

                if (expiryPeriod != null && expiryPeriod.doubleValue() > 0.0
                    && expiryDt.trim().length() == 0 && (
                    this.hasPrimaryValueChanged(primary, i, "mixeddt")
                        || this.hasPrimaryValueChanged(primary, i, "receiveddt"))) {
                    if (!primary.isValidColumn("expirydt")) {
                        primary.addColumn("expirydt", 2);
                    }

                    primary.setDate(i, "expirydt", calculatedExpiryDt);
                }

                expiryDt = primary.getValue(i, "expirydt");
                Calendar calculatedExpiryWarningDate = Calendar.getInstance();
                BigDecimal expiryWarningPeriod = this.database.getBigDecimal("expirywarningperiod");
                String expiryWarningPeriodUnit = this.database.getValue("expirywarningperiodunits");
                if (expiryWarningPeriod != null && expiryWarningPeriod.doubleValue() > 0.0) {
                    if (expiryWarningPeriodUnit != null && expiryWarningPeriodUnit.length() > 0
                        && expiryDt != null && expiryWarningPeriod.doubleValue() > 0.0) {
                        calculatedExpiryWarningDate = DateTimeUtil.getOffsetDate(
                            dtu.getCalendar(expiryDt), expiryWarningPeriodUnit,
                            expiryWarningPeriod.negate());
                    }

                    if (this.hasPrimaryValueChanged(primary, i, "expirydt")) {
                        if (!primary.isValidColumn("expirywarningdt")) {
                            primary.addColumn("expirywarningdt", 2);
                        }

                        primary.setDate(i, "expirywarningdt", calculatedExpiryWarningDate);
                    }
                }

                Calendar calculatedReorderedDate = Calendar.getInstance();
                BigDecimal expiryReorderPeriod = this.database.getBigDecimal("expiryreorderperiod");
                String expiryReorderPeriodUnit = this.database.getValue("expiryreorderperiodunits");
                if (expiryReorderPeriod != null && expiryReorderPeriod.doubleValue() > 0.0) {
                    if (expiryReorderPeriodUnit != null && expiryReorderPeriodUnit.length() > 0
                        && expiryDt != null && expiryReorderPeriod.doubleValue() > 0.0) {
                        calculatedReorderedDate = DateTimeUtil.getOffsetDate(
                            dtu.getCalendar(expiryDt), expiryReorderPeriodUnit,
                            expiryReorderPeriod.negate());
                    }

                    if (reorderedDate.trim().length() == 0 || this.hasPrimaryValueChanged(primary,
                        i, "expirydt")) {
                        if (!primary.isValidColumn("reordereddt")) {
                            primary.addColumn("reordereddt", 2);
                        }

                        primary.setDate(i, "reordereddt", calculatedReorderedDate);
                    }
                }

                Calendar calculatedGracePeriodDate = Calendar.getInstance();
                BigDecimal gracePeriod = this.database.getBigDecimal("expirygraceperiod");
                String gracePeriodUnit = this.database.getValue("expirygraceperiodunits");
                if (gracePeriod != null && gracePeriod.doubleValue() > 0.0) {
                    if (gracePeriodUnit != null && gracePeriodUnit.length() > 0 && expiryDt != null
                        && gracePeriod.doubleValue() > 0.0) {
                        calculatedGracePeriodDate = DateTimeUtil.getOffsetDate(
                            dtu.getCalendar(expiryDt), gracePeriodUnit, gracePeriod);
                    }

                    if (!primary.isValidColumn("graceperioddt")) {
                        primary.addColumn("graceperioddt", 2);
                    }

                    primary.setDate(i, "graceperioddt", calculatedGracePeriodDate);
                }

                if ("Y".equalsIgnoreCase(trackItemExpiryPeriodReq)
                    && expiryDt.trim().length() > 0) {
                    String sqlTrackitem = "SELECT trackitemid,expirydt FROM trackitem WHERE linksdcid='LV_ReagentLot' AND linkkeyid1=?";
                    this.database.createPreparedResultSet(sqlTrackitem, new Object[]{reagentLotid});

                    while (this.database.getNext()) {
                        String trackitemid = this.database.getValue("trackitemid");
                        if (this.database.getString("expirydt") == null) {
                            PropertyList trackitemProps = new PropertyList();
                            trackitemProps.setProperty("sdcid", "TrackItemSDC");
                            trackitemProps.setProperty("keyid1", trackitemid);
                            trackitemProps.setProperty("linksdcid", "LV_ReagentLot");
                            trackitemProps.setProperty("linkkeyid1", reagentLotid);
                            trackitemProps.setProperty("expirydt", calculatedExpiryDt.toString());

                            try {
                                this.getActionProcessor()
                                    .processAction("EditSDI", "1", trackitemProps);
                            } catch (Exception var29) {
                                throw new SapphireException("DB_ACTION_FAILED",
                                    "Not able to Edit Trackitem", var29);
                            }
                        }
                    }
                }
            }
        }

    }

    private void addReagentAttributes(String paramlistids, String paramlistversionids,
        String variantids, String reagentLotid) throws SapphireException {
        PropertyList addDataSetProps = new PropertyList();
        addDataSetProps.setProperty("sdcid", "LV_ReagentLot");
        addDataSetProps.setProperty("addnewonly", "N");
        if (paramlistids.trim().length() > 0) {
            addDataSetProps.setProperty("keyid1", reagentLotid);
            addDataSetProps.setProperty("paramlistid", paramlistids);
            addDataSetProps.setProperty("paramlistversionid", paramlistversionids);
            addDataSetProps.setProperty("variantid", variantids);

            try {
                this.getActionProcessor().processAction("AddDataSet", "1", addDataSetProps);
            } catch (Exception var7) {
                throw new SapphireException("DB_ACTION_FAILED",
                    "Not able to Add Reagent Attributes", var7);
            }
        }

    }

    private void addReagentSDIApproval(String approvalTypeid, String reagentLotid)
        throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_ReagentLot");
        props.setProperty("keyid1", reagentLotid);
        props.setProperty("approvaltypeid", approvalTypeid);
        props.setProperty("addsteps", "Y");
        props.setProperty("ready", "N");

        try {
            this.getActionProcessor().processAction("AddSDIApproval", "1", props);
        } catch (Exception var5) {
            throw new SapphireException("DB_ACTION_FAILED",
                "Not able to Add Reagent Approvals" + var5);
        }
    }

    private void addReagentCertification(String reagentLotid) throws SapphireException {
        DataSet dsinsert = new DataSet(this.connectionInfo);
        dsinsert.addColumnValues("resourcesdcid", 0, "LV_ReagentLot", ";");
        dsinsert.addColumnValues("resourcekeyid1", 0, reagentLotid, ";");
        dsinsert.addColumnValues("resourcekeyid2", 0, "(null)", ";");
        dsinsert.addColumnValues("resourcekeyid3", 0, "(null)", ";");
        dsinsert.addColumnValues("certifiedforsdcid", 0, "*", ";");
        dsinsert.addColumnValues("certifiedforkeyid1", 0, "*", ";");
        dsinsert.addColumnValues("certifiedforkeyid2", 0, "*", ";");
        dsinsert.addColumnValues("certifiedforkeyid3", 0, "*", ";");
        dsinsert.addColumnValues("certificationtype", 0, "Reagent Certification", ";");
        dsinsert.addColumnValues("certificationstatus", 0, "Invalid", ";");

        try {
            DataSetUtil.insert(this.database, dsinsert, "s_sdicertification");
        } catch (Exception var4) {
            throw new SapphireException("Not able to Add Reagent Certification" + var4);
        }
    }

    private void addReagentLotRecipe(double mulFactor, boolean isClonedReagent, String reagentLotid,
        String reagentTypeid, String reagentTypeVersionid) throws SapphireException {
        if (!isClonedReagent) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT * FROM reagenttyperecipe");
            sql.append(" WHERE reagenttypeid=").append(safeSQL.addVar(reagentTypeid)).append("");
            sql.append(" AND reagenttypeversionid=").append(safeSQL.addVar(reagentTypeVersionid))
                .append("");
            sql.append(" order by usersequence");
            DataSet ds = this.getQueryProcessor()
                .getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            char decimalSeparator = FormatUtil.getInstance(this.connectionInfo)
                .getDecimalSeparator();
            PropertyList policy = this.getConfigurationProcessor()
                .getPolicy("ConsumablePolicy", "Sapphire Custom");
            boolean autopopulateuseamount = "Y".equals(
                policy.getProperty("autopopulateuseamount", "Y"));

            for (int i = 0; i < ds.size(); ++i) {
                String recipeitemtype = ds.getValue(i, "recipeitemtype");
                PropertyList reagentRecipeActionProps = new PropertyList();
                reagentRecipeActionProps.setProperty("sdcid", "LV_ReagentLot");
                reagentRecipeActionProps.setProperty("linkid", "ReagentLotRecipe");
                reagentRecipeActionProps.setProperty("keyid1", reagentLotid);
                reagentRecipeActionProps.setProperty("reagentlotrecipeitemid",
                    ds.getValue(i, "reagenttyperecipeitemid"));
                reagentRecipeActionProps.setProperty("recipeitemtype", recipeitemtype);
                reagentRecipeActionProps.setProperty("includereagenttypeid",
                    ds.getValue(i, "includereagenttypeid"));
                reagentRecipeActionProps.setProperty("includereagenttypeversionid",
                    ds.getValue(i, "includereagenttypeversionid"));
                reagentRecipeActionProps.setProperty("originalreagenttypeid",
                    ds.getValue(i, "includereagenttypeid"));
                reagentRecipeActionProps.setProperty("originalreagenttypeversionid",
                    ds.getValue(i, "includereagenttypeversionid"));
                reagentRecipeActionProps.setProperty("filltovolumeflag",
                    ds.getValue(i, "filltovolumeflag"));
                reagentRecipeActionProps.setProperty("reagentlotstageid",
                    ds.getValue(i, "reagenttypestageid"));
                boolean isInstrument = "Instrument".equalsIgnoreCase(recipeitemtype);
                int scale = 0;
                if (!isInstrument) {
                    scale = 3;
                } else {
                    reagentRecipeActionProps.setProperty("instrumenttype",
                        ds.getValue(i, "instrumenttype"));
                }

                String amountStr = "";
                String amountTextStr = ds.getValue(i, "amounttext", "").trim();
                int actaulScale = this.getScale(amountTextStr);
                if (actaulScale > scale) {
                    scale = actaulScale;
                }

                BigDecimal value;
                if (ds.getValue(i, "amount", "").trim().length() > 0) {
                    BigDecimal getBigDecimalAmout = ds.getBigDecimal(i, "amount",
                        new BigDecimal("0.0"));
                    value = (new BigDecimal(getBigDecimalAmout.doubleValue() * mulFactor)).setScale(
                        scale, 4);
                    amountStr = value.toString();
                }

                if (amountTextStr.length() > 0 && mulFactor != 1.0) {
                    FormatUtil formatUtil = FormatUtil.getInstance(this.connectionInfo);
                    value = formatUtil.parseBigDecimal(amountTextStr);
                    BigDecimal amounttext = (new BigDecimal(
                        value.doubleValue() * mulFactor)).setScale(scale, 4);
                    amountTextStr = amounttext.toString();
                    if (actaulScale != scale) {
                        amountTextStr = this.removeLastZeros(amountTextStr, actaulScale);
                    }
                }

                try {
                    if (autopopulateuseamount) {
                        reagentRecipeActionProps.setProperty("amounttext",
                            amountTextStr.replace('.', decimalSeparator));
                        reagentRecipeActionProps.setProperty("amount",
                            amountStr.replace('.', decimalSeparator));
                        reagentRecipeActionProps.setProperty("amountunits",
                            ds.getValue(i, "amountunits"));
                        reagentRecipeActionProps.setProperty("amountunitstype",
                            ds.getValue(i, "amountunitstype"));
                    }

                    reagentRecipeActionProps.setProperty("amountrecommendedtext",
                        amountTextStr.replace('.', decimalSeparator));
                    reagentRecipeActionProps.setProperty("amountrecommended",
                        amountStr.replace('.', decimalSeparator));
                    reagentRecipeActionProps.setProperty("amountadjusted",
                        amountStr.replace('.', decimalSeparator));
                    reagentRecipeActionProps.setProperty("amountrecommendedunits",
                        ds.getValue(i, "amountunits"));
                    reagentRecipeActionProps.setProperty("amountrecommendedunitstype",
                        ds.getValue(i, "amountunitstype"));
                    reagentRecipeActionProps.setProperty("usersequence", String.valueOf(i + 1));
                    this.getActionProcessor()
                        .processAction("AddSDIDetail", "1", reagentRecipeActionProps);
                } catch (Exception var24) {
                    throw new SapphireException("DB_ACTION_FAILED",
                        "Not able to Add Reagent Recipe", var24);
                }
            }
        }

    }

    private int getScale(String amountTextStr) {
        amountTextStr = this.getEnglishLocaleValue(amountTextStr);
        String[] amountTextStrArr = StringUtil.split(amountTextStr, ".");
        return amountTextStrArr.length > 1 ? amountTextStrArr[1].length() : 0;
    }

    private String removeLastZeros(String amountTextStr, int actaulScale) {
        String temp = amountTextStr;
        amountTextStr = this.getEnglishLocaleValue(amountTextStr);
        int scale = this.getScale(amountTextStr);
        if (scale > actaulScale) {
            for (int i = amountTextStr.length(); i > 1; --i) {
                if (amountTextStr.charAt(i - 1) != '0') {
                    if (amountTextStr.charAt(i - 1) == '.') {
                        temp = temp.substring(0, temp.length() - 1);
                    }
                    break;
                }

                temp = temp.substring(0, temp.length() - 1);
                --scale;
                if (actaulScale != 0 && scale == actaulScale) {
                    break;
                }
            }
        }

        return temp;
    }

    private String getEnglishLocaleValue(String amountTextStr) {
        char decimalSeparator = FormatUtil.getInstance(this.connectionInfo).getDecimalSeparator();
        amountTextStr = amountTextStr.replace(decimalSeparator, '.');
        amountTextStr = amountTextStr.replace(',', '.');
        return amountTextStr;
    }

    private void setReagentStatus(DataSet primary, DataSet oldPrimary) throws SapphireException {
        for (int i = 0; i < primary.size(); ++i) {
            String reagentTypeid = primary.getString(i, "reagenttypeid");
            String reagentTypeVersionid = primary.getString(i, "reagenttypeversionid");
            String qualitySampleRequired = primary.getValue(i, "qualitysamplereqflag", "");
            String finalReagentStatus = primary.getValue(i, "reagentstatus", "");
            if (!primary.isValidColumn("reagentstatus") && oldPrimary != null) {
                finalReagentStatus = oldPrimary.getValue(i, "reagentstatus", "");
            }

            if (reagentTypeid != null && reagentTypeVersionid != null && (
                "Initial".equalsIgnoreCase(finalReagentStatus)
                    || finalReagentStatus.length() == 0)) {
                this.database = ReagentUtil.getReagentTypeDetail(this.database, reagentTypeid,
                    reagentTypeVersionid);
                String approvalTypeid = this.database.getValue("approvaltypeid");
                if (approvalTypeid.trim().length() == 0 && "Y".equalsIgnoreCase(
                    qualitySampleRequired)) {
                    finalReagentStatus = "Initial";
                } else if (approvalTypeid.trim().length() == 0 && "N".equalsIgnoreCase(
                    qualitySampleRequired)) {
                    finalReagentStatus = "Active";
                } else if (approvalTypeid.trim().length() > 0 && "N".equalsIgnoreCase(
                    qualitySampleRequired)) {
                    finalReagentStatus = "PendingApproval";
                } else if (approvalTypeid.trim().length() > 0 && "Y".equalsIgnoreCase(
                    qualitySampleRequired)) {
                    finalReagentStatus = "Initial";
                }

                if (!primary.isValidColumn("reagentstatus")) {
                    primary.addColumn("reagentstatus", 0);
                }

                primary.setString(i, "reagentstatus", finalReagentStatus);
            }
        }

    }

    private void setCertificationStatus(DataSet primary) throws SapphireException {
        for (int i = 0; i < primary.size(); ++i) {
            String reagentlotid = primary.getString(i, "reagentlotid");
            String reagentStatus = primary.getString(i, "reagentstatus");
            if (reagentStatus == null) {
                return;
            }

            String oldReagentStatus = this.getOldPrimaryValue(primary, i, "reagentstatus");
            if (!reagentStatus.equalsIgnoreCase(oldReagentStatus)) {
                String certificationStatus = "Invalid";
                if ("Active".equalsIgnoreCase(reagentStatus)) {
                    certificationStatus = "Valid";
                }

                String sqlCertification =
                    "UPDATE s_sdicertification set certificationstatus='" + certificationStatus
                        + "' WHERE resourcesdcid='LV_ReagentLot' AND resourcekeyid1=?";
                this.database.executePreparedUpdate(sqlCertification, new Object[]{reagentlotid});
            }
        }

    }

    private void setApprovalStatus(DataSet primary) throws SapphireException {
        for (int i = 0; i < primary.size(); ++i) {
            String reagentlotid = primary.getString(i, "reagentlotid");
            String reagentStatus = primary.getString(i, "reagentstatus");
            if (reagentStatus == null) {
                return;
            }

            String oldReagentStatus = this.getOldPrimaryValue(primary, i, "reagentstatus");
            if (!reagentStatus.equalsIgnoreCase(oldReagentStatus)
                && "PendingApproval".equalsIgnoreCase(reagentStatus)) {
                try {
                    PropertyList actionProps = new PropertyList();
                    actionProps.setProperty("sdcid", "LV_ReagentLot");
                    actionProps.setProperty("keyid1", reagentlotid);
                    actionProps.setProperty("ready", "Y");
                    this.getActionProcessor().processAction("ResetSDIApproval", "1", actionProps);
                } catch (Exception var7) {
                    throw new SapphireException("Approval can not be set for ready" + var7);
                }
            }
        }

    }

    private void setTrackItemStatus(DataSet primary) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();

        for (int i = 0; i < primary.size(); ++i) {
            String reagentlotid = primary.getString(i, "reagentlotid");
            String reagentStatus = primary.getString(i, "reagentstatus");
            String oldReagentStatus = this.getOldPrimaryValue(primary, i, "reagentstatus");
            String expirydt = "";
            String trackitemStatus = "";
            if (reagentStatus == null) {
                return;
            }

            if (!reagentStatus.equalsIgnoreCase(oldReagentStatus)) {
                if ("Active".equalsIgnoreCase(reagentStatus)) {
                    trackitemStatus = "Valid";
                } else if ("Expired".equalsIgnoreCase(reagentStatus)) {
                    trackitemStatus = "Expired";
                    expirydt = primary.getValue(i, "expirydt", "");
                } else if ("Inactive".equalsIgnoreCase(reagentStatus)
                    || "Rejected".equalsIgnoreCase(reagentStatus)) {
                    trackitemStatus = "Invalid";
                }

                safeSQL.reset();
                String sql =
                    "SELECT trackitemid,expirydt FROM trackitem WHERE linksdcid='LV_ReagentLot' AND linkkeyid1="
                        + safeSQL.addVar(reagentlotid)
                        + " AND trackitemstatus not in ('Expired','Depleted','Disposed')";
                DataSet ds = this.getQueryProcessor()
                    .getPreparedSqlDataSet(sql, safeSQL.getValues());
                String trackitemids = ds.getColumnValues("trackitemid", ";");
                if (trackitemids.trim().length() > 0 && trackitemStatus.trim().length() > 0) {
                    try {
                        PropertyList trackitemStatusProps = new PropertyList();
                        trackitemStatusProps.setProperty("sdcid", "TrackItemSDC");
                        trackitemStatusProps.setProperty("keyid1", trackitemids);
                        trackitemStatusProps.setProperty("trackitemstatus", trackitemStatus);
                        if (expirydt != null && expirydt.trim().length() > 0) {
                            String strTrackitemexpirydt = "";
                            StringBuffer trackitemExpirydts = new StringBuffer();

                            for (int rowid = 0; rowid < ds.size(); ++rowid) {
                                strTrackitemexpirydt = ds.getValue(rowid, "expirydt", "");
                                if (strTrackitemexpirydt.trim().length() == 0) {
                                    trackitemExpirydts.append(";").append(expirydt);
                                } else {
                                    trackitemExpirydts.append(";").append(strTrackitemexpirydt);
                                }
                            }

                            if (trackitemExpirydts.length() > 0) {
                                trackitemStatusProps.setProperty("expirydt",
                                    trackitemExpirydts.substring(1));
                            }
                        }

                        this.getActionProcessor()
                            .processAction("EditSDI", "1", trackitemStatusProps);
                    } catch (Exception var16) {
                        throw new SapphireException(
                            "Unable to set Trackitem Status Status " + var16);
                    }
                }
            }
        }

    }

    public void postApprove(DataSet dsApproval) {
        try {
            DataSet approvedDS = ApprovalUtil.getSDIApprovalFlags(this.database, dsApproval);
            DataSet dsProp = new DataSet();

            for (int i = 0; i < approvedDS.size(); ++i) {
                String keyid1 = approvedDS.getValue(i, "keyid1");
                String approvalFlag = approvedDS.getValue(i, "approvalflag");
                String reagentStatus =
                    "Pass".equalsIgnoreCase(approvalFlag) ? "Active" : "Rejected";
                int newRow = dsProp.addRow();
                String dispositionStatus =
                    "Pass".equalsIgnoreCase(approvalFlag) ? "Passed" : "Failed";
                dsProp.setString(newRow, "disposition", dispositionStatus);
                dsProp.setString(newRow, "keyid1", approvedDS.getValue(i, "keyid1"));
                String approvalStatus = "";
                if ("Active".equalsIgnoreCase(reagentStatus)) {
                    approvalStatus = "passed";
                } else if ("Rejected".equalsIgnoreCase(reagentStatus)) {
                    approvalStatus = "failed";
                }

                PropertyList reagentStatusProps = new PropertyList();
                reagentStatusProps.setProperty("reagentlotid", keyid1);
                reagentStatusProps.setProperty("approvalstatus", approvalStatus);
                this.getActionProcessor()
                    .processAction("AdvanceReagentStatus", "1", reagentStatusProps);
            }

            if (dsProp.size() > 0) {
                String sdcId = this.getSdcid();
                ActionProcessor actionProcessor = this.getActionProcessor();
                PropertyList props = new PropertyList();
                props.put("sdcid", sdcId);
                props.put("keyid1", dsProp.getColumnValues("keyid1", ";"));
                props.put("disposition", dsProp.getColumnValues("disposition", ";"));
                actionProcessor.processAction("EditSDI", "1", props);
            }
        } catch (Exception var12) {
            Logger.logInfo("Exception occured in post approve rule :" + var12.getMessage());
        }

    }

    public PropertyList getTrackitemInfo(DataSet primary, int i, String maxfreezethawcount,
        String warnfreezethawcount) throws SapphireException {
        PropertyList trackitemProps = new PropertyList();
        char decimalSeparator = FormatUtil.getInstance(this.connectionInfo).getDecimalSeparator();
        String reagentLotid = primary.getString(i, "reagentlotid");
        String containerTypeid = primary.getValue(i, "containertypeid");
        int noOfContainer = primary.getInt(i, "containersinitial", 0);
        String amountInitialUnitsType = primary.getValue(i, "amountinitialunitstype");
        String trackItemExpiryPeriodReq = primary.getValue(i, "trackitemexpiryperiodreqflag");
        String containerTrackMethod = primary.getValue(i, "containertrackingflag");
        BigDecimal amountInitialBigDecimal = primary.getBigDecimal(i, "amountinitial",
            new BigDecimal(0.0));
        double amountInitial = amountInitialBigDecimal.doubleValue();
        String amountinitialUnits = primary.getValue(i, "amountinitialunits");
        String sqlContainerSize = "SELECT sizevalue,sizeunits FROM containertype WHERE containertypeid=?";
        this.database.createPreparedResultSet(sqlContainerSize, new Object[]{containerTypeid});
        double containerSize = 0.0;
        BigDecimal containerSizeBigDecimal = new BigDecimal(containerSize);
        String containerSizeUnit = "";
        if (this.database.getNext()) {
            containerSizeBigDecimal =
                this.database.getBigDecimal("sizevalue") == null ? containerSizeBigDecimal
                    : this.database.getBigDecimal("sizevalue");
            containerSize = containerSizeBigDecimal.doubleValue();
            containerSizeUnit = this.database.getValue("sizeunits");
        }

        if (noOfContainer == 0 && "C".equalsIgnoreCase(amountInitialUnitsType)) {
            noOfContainer = (int) Math.ceil(amountInitial);
        }

        if (!amountinitialUnits.equals(containerSizeUnit) && containerSizeUnit.length() > 0) {
            amountInitial = ReagentUtil.getConvertedValue(amountInitial, amountinitialUnits,
                containerSizeUnit, this.database, false);
        }

        if (noOfContainer == 0 && containerSize > 0.0) {
            BigDecimal[] divideAndRemainder = amountInitialBigDecimal.divideAndRemainder(
                containerSizeBigDecimal);
            noOfContainer = divideAndRemainder[0].intValue();
            if (divideAndRemainder[1].doubleValue() > 0.0) {
                ++noOfContainer;
            }
        }

        String expiryDt = primary.getValue(i, "expirydt");
        String trackitemExpiryDate = "";
        if ("Y".equalsIgnoreCase(trackItemExpiryPeriodReq)) {
            trackitemExpiryDate = expiryDt;
        }

        int noOfTrackitem = 0;
        double qtyCurrent = 0.0;
        String qtyUnit = "";
        String qtyCurrentType = "";
        if (containerSize == 0.0 && noOfContainer == 0) {
            noOfTrackitem = 1;
            qtyCurrent = amountInitial;
            qtyUnit = amountinitialUnits;
            qtyCurrentType = amountInitialUnitsType;
        } else if (containerSize == 0.0 && noOfContainer > 0) {
            if ("T".equalsIgnoreCase(containerTrackMethod)) {
                qtyCurrent = (double) noOfContainer;
                noOfTrackitem = 1;
            } else {
                qtyCurrent = 1.0;
                noOfTrackitem = noOfContainer;
            }

            qtyUnit = "";
            qtyCurrentType = "C";
        } else if (amountInitial == 0.0 && containerSize > 0.0 && noOfContainer > 0
            && amountInitialUnitsType.trim().length() == 0) {
            if ("T".equalsIgnoreCase(containerTrackMethod)) {
                qtyCurrent = (double) noOfContainer;
                noOfTrackitem = 1;
            } else {
                qtyCurrent = 1.0;
                noOfTrackitem = noOfContainer;
            }

            qtyUnit = "";
            qtyCurrentType = "C";
        } else if (amountInitial == 0.0 && containerSize > 0.0 && noOfContainer > 0
            && "C".equalsIgnoreCase(amountInitialUnitsType)) {
            if ("T".equalsIgnoreCase(containerTrackMethod)) {
                qtyCurrent = (double) noOfContainer;
                noOfTrackitem = 1;
            } else {
                qtyCurrent = 1.0;
                noOfTrackitem = noOfContainer;
            }

            qtyUnit = "";
            qtyCurrentType = "C";
        } else if (amountInitial == 0.0 && containerSize > 0.0 && noOfContainer > 0
            && "U".equalsIgnoreCase(amountInitialUnitsType)) {
            if ("T".equalsIgnoreCase(containerTrackMethod)) {
                qtyCurrent = (double) noOfContainer * containerSize;
                noOfTrackitem = 1;
            } else {
                qtyCurrent = containerSize;
                noOfTrackitem = noOfContainer;
            }

            qtyUnit = containerSizeUnit;
            qtyCurrentType = "U";
        } else if (amountInitial > 0.0 && containerSize > 0.0 && noOfContainer > 0
            && "C".equalsIgnoreCase(amountInitialUnitsType)) {
            if ("T".equalsIgnoreCase(containerTrackMethod)) {
                qtyCurrent = (double) noOfContainer;
                noOfTrackitem = 1;
            } else {
                qtyCurrent = 1.0;
                noOfTrackitem = noOfContainer;
            }

            qtyUnit = "";
            qtyCurrentType = "C";
        } else if (amountInitial > 0.0 && containerSize > 0.0 && noOfContainer > 0
            && "U".equalsIgnoreCase(amountInitialUnitsType)) {
            if ("T".equalsIgnoreCase(containerTrackMethod)) {
                qtyCurrent = amountInitial;
                noOfTrackitem = 1;
            } else {
                int scale = ReagentUtil.getMaxScale(
                    amountInitialBigDecimal.toString() + ";" + containerSizeBigDecimal.toString(),
                    decimalSeparator, 3);
                qtyCurrent = Double.parseDouble(
                    (new BigDecimal(amountInitial / (double) noOfContainer)).setScale(scale, 4)
                        .toString());
                noOfTrackitem = noOfContainer;
            }

            qtyUnit = containerSizeUnit;
            qtyCurrentType = "U";
        }

        trackitemProps.setProperty("reagentlotid", reagentLotid);
        trackitemProps.setProperty("noofcontainer", String.valueOf(noOfContainer));
        trackitemProps.setProperty("containertypeid", containerTypeid);
        trackitemProps.setProperty("nooftrackitem", String.valueOf(noOfTrackitem));
        trackitemProps.setProperty("trackitemexpirydate", trackitemExpiryDate);
        trackitemProps.setProperty("qtycurrent", String.valueOf(qtyCurrent));
        trackitemProps.setProperty("qtyunit", qtyUnit);
        trackitemProps.setProperty("qtycurrenttype", qtyCurrentType);
        trackitemProps.setProperty("maxfreezethawcount", maxfreezethawcount);
        trackitemProps.setProperty("warnfreezethawcount", warnfreezethawcount);
        return trackitemProps;
    }

    private void setReorderedFlag(DataSet primary) throws SapphireException {
        String reorderedFlag = "N";

        for (int i = 0; i < primary.size(); ++i) {
            String reagentTypeid = primary.getString(i, "reagenttypeid");
            String reagentTypeVersionid = primary.getString(i, "reagenttypeversionid");
            if (reagentTypeid != null && reagentTypeVersionid != null) {
                this.database = ReagentUtil.getReagentTypeDetail(this.database, reagentTypeid,
                    reagentTypeVersionid);
                String reorderThresholdScopeFlag = this.database.getValue(
                    "reorderthresholdscopeflag");
                String sqlReagentLot = "SELECT reorderedflag FROM reagentlot WHERE reagenttypeid=? AND reagenttypeversionid=?";
                this.database.createPreparedResultSet(sqlReagentLot,
                    new Object[]{reagentTypeid, reagentTypeVersionid});
                if (this.database.getNext()) {
                    reorderedFlag = this.database.getString("reorderedflag");
                }

                if ("T".equalsIgnoreCase(reorderThresholdScopeFlag)) {
                    if (!primary.isValidColumn("reorderedflag")) {
                        primary.addColumn("reorderedflag", 0);
                    }

                    primary.setValue(i, "reorderedflag", reorderedFlag);
                } else if ("L".equalsIgnoreCase(reorderThresholdScopeFlag)) {
                    if (!primary.isValidColumn("reorderedflag")) {
                        primary.addColumn("reorderedflag", 0);
                    }

                    primary.setValue(i, "reorderedflag", "N");
                }
            }
        }

    }

    public void callReagentEvent(SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        DataSet reagentInformation = new DataSet();
        reagentInformation.addColumn("reagentlotid", 0);
        reagentInformation.addColumn("reagenttypeid", 0);
        reagentInformation.addColumn("reagenttypeversionid", 0);
        reagentInformation.addColumn("notificationaction", 0);
        reagentInformation.addColumn("notificationactionversionid", 0);
        reagentInformation.addColumn("expirynotificationflagchanged", 0);
        reagentInformation.addColumn("expirywarningnotificationflagchanged", 0);
        reagentInformation.addColumn("expiryreordernotificationflagchanged", 0);
        reagentInformation.addColumn("expirydt", 2);
        reagentInformation.addColumn("reordereddt", 0);

        String expirynotificationflagchanged;
        String expirywarningnotificationflagchanged;
        String expiryreordernotificationflagchanged;
        String notificationActionId;
        String notificationActionVersionId;
        String reorderedDt;
        for (int i = 0; i < primary.size(); ++i) {
            reagentInformation.addRow();
            String lotId = primary.getString(i, "reagentlotid");
            expirynotificationflagchanged = this.getOldPrimaryValue(primary, i, "reagenttypeid");
            expirywarningnotificationflagchanged = this.getOldPrimaryValue(primary, i,
                "reagenttypeversionid");
            expiryreordernotificationflagchanged = this.getOldPrimaryValue(primary, i, "expirydt");
            notificationActionId = primary.getString(i, "expirynotifyflag", "");
            notificationActionVersionId = this.getOldPrimaryValue(primary, i, "expirynotifyflag");
            reorderedDt = primary.getString(i, "expirywarningnotifyflag", "");
            String oldPrmExpWarnNotifyFlag = this.getOldPrimaryValue(primary, i,
                "expirywarningnotifyflag");
            String prmExpReorderNotifyFlag = primary.getString(i, "expiryreorderedflag", "");
            String oldPrmExpReorderNotifyFlag = this.getOldPrimaryValue(primary, i,
                "expiryreorderedflag");
            String notifyActionDetails = this.getNotifyActionDetails(lotId,
                expirynotificationflagchanged, expirywarningnotificationflagchanged);
            String[] notificationActionDetails = StringUtil.split(notifyActionDetails, ";");
            reagentInformation.setValue(i, "reagentlotid", lotId);
            reagentInformation.setValue(i, "reagenttypeid", expirynotificationflagchanged);
            reagentInformation.setValue(i, "reagenttypeversionid",
                expirywarningnotificationflagchanged);
            reagentInformation.setValue(i, "expirydt", expiryreordernotificationflagchanged);
            if (notificationActionDetails.length > 1) {
                reagentInformation.setValue(i, "notificationaction", notificationActionDetails[0]);
                reagentInformation.setValue(i, "notificationactionversionid",
                    notificationActionDetails[1]);
            }

            if (notificationActionDetails.length > 2) {
                reagentInformation.setValue(i, "reordereddt", notificationActionDetails[2]);
            }

            if (notificationActionId.length() > 0) {
                reagentInformation.setValue(i, "expirynotificationflagchanged",
                    notificationActionId.equals(notificationActionVersionId) ? "N" : "Y");
            } else {
                reagentInformation.setValue(i, "expirynotificationflagchanged", "N");
            }

            if (reorderedDt.length() > 0) {
                reagentInformation.setValue(i, "expirywarningnotificationflagchanged",
                    reorderedDt.equals(oldPrmExpWarnNotifyFlag) ? "N" : "Y");
            } else {
                reagentInformation.setValue(i, "expirywarningnotificationflagchanged", "N");
            }

            if (prmExpReorderNotifyFlag.length() > 0) {
                reagentInformation.setValue(i, "expiryreordernotificationflagchanged",
                    prmExpReorderNotifyFlag.equals(oldPrmExpReorderNotifyFlag) ? "N" : "Y");
            } else {
                reagentInformation.setValue(i, "expiryreordernotificationflagchanged", "N");
            }
        }

        PropertyList properties = new PropertyList();

        for (int j = 0; j < reagentInformation.getRowCount(); ++j) {
            expirynotificationflagchanged = reagentInformation.getValue(j,
                "expirynotificationflagchanged");
            expirywarningnotificationflagchanged = reagentInformation.getValue(j,
                "expirywarningnotificationflagchanged");
            expiryreordernotificationflagchanged = reagentInformation.getValue(j,
                "expiryreordernotificationflagchanged");
            notificationActionId = reagentInformation.getValue(j, "notificationaction");
            notificationActionVersionId = reagentInformation.getValue(j,
                "notificationactionversionid");
            reorderedDt = reagentInformation.getValue(j, "reordereddt");
            properties.put("keyid1", reagentInformation.getValue(j, "reagentlotid"));
            properties.put("reagenttypeid", reagentInformation.getValue(j, "reagenttypeid"));
            properties.put("reagenttypeversionid",
                reagentInformation.getValue(j, "reagenttypeversionid"));
            if ("Y".equals(expirynotificationflagchanged)) {
                properties.put("eventtype", "ExpiryNotification");
                properties.put("expirydate", reagentInformation.getValue(j, "expirydt"));
                this.getActionProcessor()
                    .processAction(notificationActionId, notificationActionVersionId, properties);
            }

            if ("Y".equals(expirywarningnotificationflagchanged)) {
                properties.put("eventtype", "ExpiryWarningNotification");
                properties.put("expirydate", reagentInformation.getValue(j, "expirydt"));
                this.getActionProcessor()
                    .processAction(notificationActionId, notificationActionVersionId, properties);
            }

            if ("Y".equals(expiryreordernotificationflagchanged)) {
                properties.put("eventtype", "ExpiryReorderNotification");
                properties.put("reorderdate", reorderedDt);
                this.getActionProcessor()
                    .processAction(notificationActionId, notificationActionVersionId, properties);
            }

            properties.clear();
        }

    }

    private String getNotifyActionDetails(String reagentLotId, String reagentTypeId,
        String reagentTypeVersionId) throws SapphireException {
        String sql = "SELECT notificationaction, notificationactionversionid, reordereddt FROM reagenttype, reagentlot  WHERE reagentlot.reagenttypeid = reagenttype.reagenttypeid AND  reagentlot.reagenttypeversionid = reagenttype.reagenttypeversionid AND  reagentlotid = ? AND  reagenttype.reagenttypeid = ? AND  reagenttype.reagenttypeversionid = ? ";
        String actionId = "";
        String actionVersionId = "";
        String reorderedDt = "";
        this.database.createPreparedResultSet(sql,
            new Object[]{reagentLotId, reagentTypeId, reagentTypeVersionId});
        if (this.database.getNext()) {
            actionId = this.database.getValue("notificationaction");
            actionVersionId = this.database.getValue("notificationactionversionid");
            reorderedDt = this.database.getValue("reordereddt");
        }

        return actionId + ";" + actionVersionId + ";" + reorderedDt;
    }

    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet currentReagentLotRecipe = sdiData.getDataset("reagentlotrecipe");
        if (currentReagentLotRecipe != null && currentReagentLotRecipe.size() > 0) {
            for (int i = 0; i < currentReagentLotRecipe.size(); ++i) {
                if (currentReagentLotRecipe.getString(i, "originalreagenttypeid", "").length()
                    == 0) {
                    currentReagentLotRecipe.setString(i, "originalreagenttypeid",
                        currentReagentLotRecipe.getString(i, "includereagenttypeid", ""));
                    currentReagentLotRecipe.setValue(i, "originalreagenttypeversionid",
                        currentReagentLotRecipe.getValue(i, "includereagenttypeversionid", ""));
                }
            }
        }

    }

    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (actionProps != null && actionProps.get("trackitemid") != null
            && actionProps.get("amount") != null && actionProps.get("amountunits") != null
            && actionProps.get("amountunitstype") != null) {
            char decimalSeparator = FormatUtil.getInstance(this.connectionInfo)
                .getDecimalSeparator();
            String currTIIdList = actionProps.get("trackitemid").toString();
            String[] allTIIds = StringUtil.split(currTIIdList, ";");
            String currUseAmountList = actionProps.getProperty("amount", "0.0")
                .replace(decimalSeparator, '.');
            String[] allUseAmounts = StringUtil.split(currUseAmountList, ";");
            String currUseAmountUnitList = actionProps.get("amountunits").toString();
            String[] allUseAmountUnits = StringUtil.split(currUseAmountUnitList, ";");
            String currUseAmountUnitsTypes = actionProps.get("amountunitstype").toString();
            String[] allUseAmountUnitsTypes = StringUtil.split(currUseAmountUnitsTypes, ";");
            String reagentLotIdList = actionProps.get("reagentlotid") == null ? ""
                : actionProps.get("reagentlotid").toString();
            String[] allReagentLotIds = StringUtil.split(reagentLotIdList, ";");
            DataSet reagentLotDS = this.getReagentLotDS(reagentLotIdList);
            String paramTrackItemIds = "";
            String paramQtys = "";
            String paramQtyUnits = "";
            String paramQtyUnitTypes = "";
            HashMap currKeys = new HashMap();
            StringBuffer unmanagedTIs = new StringBuffer();

            for (int currTINum = 0; currTINum < allTIIds.length; ++currTINum) {
                String currTIId = allTIIds[currTINum];
                if (!currTIId.equals("(null)")) {
                    if (currTIId.length() > 0) {
                        unmanagedTIs.append(";").append(currTIId);
                    }

                    String currUseAmount = allUseAmounts[currTINum];
                    if (currUseAmount != null && currUseAmount.length() != 0
                        && !currUseAmount.equalsIgnoreCase("(null)")) {
                        String virtualTI = "";
                        if (reagentLotDS != null && reagentLotDS.size() > 0) {
                            currKeys.put("reagentlotid", allReagentLotIds[currTINum]);
                            int findRow = reagentLotDS.findRow(currKeys);
                            if (findRow > -1) {
                                virtualTI = reagentLotDS.getString(findRow, "trackitemid");
                            }
                        }

                        String currUseAmountUnit = allUseAmountUnits[currTINum];
                        if (currUseAmountUnit == null || currUseAmountUnit.length() == 0
                            || currUseAmountUnit.equalsIgnoreCase("(null)")) {
                            currUseAmountUnit = "";
                        }

                        String currUseAmountUnitsType = allUseAmountUnitsTypes[currTINum];
                        if (currUseAmountUnitsType == null || currUseAmountUnitsType.length() == 0
                            || currUseAmountUnitsType.equalsIgnoreCase("(null)")) {
                            currUseAmountUnitsType = "";
                        }

                        if (currTIId.length() > 0) {
                            if (paramTrackItemIds.length() == 0) {
                                paramTrackItemIds =
                                    currTIId + this.addVirtualTI(virtualTI, virtualTI);
                                paramQtys =
                                    this.negate(currUseAmount) + this.addVirtualTI(virtualTI,
                                        currUseAmount);
                                paramQtyUnits = currUseAmountUnit + this.addVirtualTI(virtualTI,
                                    currUseAmountUnit);
                                paramQtyUnitTypes =
                                    currUseAmountUnitsType + this.addVirtualTI(virtualTI,
                                        currUseAmountUnitsType);
                            } else {
                                paramTrackItemIds =
                                    paramTrackItemIds + ";" + currTIId + this.addVirtualTI(
                                        virtualTI, virtualTI);
                                paramQtys = paramQtys + ";" + this.negate(currUseAmount)
                                    + this.addVirtualTI(virtualTI, currUseAmount);
                                paramQtyUnits =
                                    paramQtyUnits + ";" + currUseAmountUnit + this.addVirtualTI(
                                        virtualTI, currUseAmountUnit);
                                paramQtyUnitTypes = paramQtyUnitTypes + ";" + currUseAmountUnitsType
                                    + this.addVirtualTI(virtualTI, currUseAmountUnitsType);
                            }
                        }

                        currKeys.clear();
                    }
                }
            }

            if (paramTrackItemIds.length() > 0) {
                HashMap trackItemProperties = new HashMap();
                trackItemProperties.put("trackitemid", paramTrackItemIds);
                trackItemProperties.put("quantity", paramQtys);
                trackItemProperties.put("quantityunit", paramQtyUnits);
                trackItemProperties.put("quantitytype", paramQtyUnitTypes);

                try {
                    this.getActionProcessor()
                        .processAction("AdjustTrackItemInv", "1", trackItemProperties);
                } catch (SapphireException var27) {
                    this.setError(this.ruleid, "FAILURE",
                        "Failed to adjust container amount. Check the use amount and units.");
                    return;
                }
            }

            if (unmanagedTIs.length() > 0) {
                ReagentUtil.updateUsedDetailsOfUnManagedTrackItemInv(unmanagedTIs.substring(1),
                    this.getQueryProcessor(), this.getActionProcessor());
            }

        }
    }

    public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.setUsedAmount(sdiData, actionProps);
    }

    private void setUsedAmount(SDIData sdiData, PropertyList actionProps) {
        String currUseAmountList = actionProps.getProperty("amount", "0.0");
        String currUseAmountListTemp = "";
        if (currUseAmountList.contains("|")) {
            SDIData beforeImage = this.getBeforeEditImage();
            DataSet oldReagentLotRecipe = beforeImage.getDataset("reagentlotrecipe");
            DataSet currentReagentLotRecipe = sdiData.getDataset("reagentlotrecipe");
            String currTIIds = actionProps.get("trackitemid").toString();
            String reagentLotIdList = actionProps.get("reagentlotid").toString();
            String reagentLotRecipeItemIdList = actionProps.get("reagentlotrecipeitemid")
                .toString();
            String[] allTIIds = StringUtil.split(currTIIds, ";");
            String[] allUseAmounts = StringUtil.split(currUseAmountList, ";");
            String[] allReagentLotIds = StringUtil.split(reagentLotIdList, ";");
            String[] allReagentLotRecipeItemIds = StringUtil.split(reagentLotRecipeItemIdList, ";");
            HashMap currKeys = new HashMap();

            for (int noOfTIs = 0; noOfTIs < allTIIds.length; ++noOfTIs) {
                String currUseAmount = allUseAmounts[noOfTIs];
                if (currUseAmount.contains("|")) {
                    currKeys.put("reagentlotid", allReagentLotIds[noOfTIs]);
                    currKeys.put("reagentlotrecipeitemid", allReagentLotRecipeItemIds[noOfTIs]);
                    int filteredRowNumber = oldReagentLotRecipe.findRow(currKeys);
                    int filteredRowNumberInPrimaary = currentReagentLotRecipe.findRow(currKeys);
                    String[] currUseAmountArr = StringUtil.split(currUseAmount, "|");
                    currUseAmountListTemp = currUseAmountListTemp + ";" + currUseAmountArr[0];
                    currentReagentLotRecipe.setValue(filteredRowNumberInPrimaary, "amount",
                        currUseAmountArr[0]);
                    if (this.isPreviousvalueChange(oldReagentLotRecipe, filteredRowNumber,
                        currUseAmountArr[1])) {
                        oldReagentLotRecipe.setValue(filteredRowNumber, "amount",
                            currUseAmountArr[1]);
                        if (currUseAmountArr.length > 3) {
                            oldReagentLotRecipe.setValue(filteredRowNumber, "amountunits",
                                currUseAmountArr[2]);
                            oldReagentLotRecipe.setValue(filteredRowNumber, "amountunitstype",
                                currUseAmountArr[3]);
                        }
                    }
                } else {
                    currUseAmountListTemp = currUseAmountListTemp + ";" + currUseAmount;
                }
            }

            actionProps.setProperty("amount", currUseAmountListTemp.substring(1));
        }

    }

    private boolean isPreviousvalueChange(DataSet oldReagentLotRecipe, int row,
        String newPreviousValue) {
        boolean change = false;
        String previousValue = oldReagentLotRecipe.getValue(row, "amount", "0.0");
        char decimalSeparator = FormatUtil.getInstance(this.connectionInfo).getDecimalSeparator();
        if (Double.parseDouble(previousValue.replace(decimalSeparator, '.')) != Double.parseDouble(
            newPreviousValue.replace(decimalSeparator, '.'))) {
            change = true;
        }

        return change;
    }

    private DataSet getReagentLotDS(String reagentLotIdList) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append(
            "select rl.reagentlotid,ti.trackitemid from reagentlot rl,trackitem ti where rl.contentflag='V' and ti.linkkeyid1=rl.reagentlotid ");
        sql.append(" and rl.reagentlotid in (")
            .append(safeSQL.addIn(StringUtil.replaceAll(reagentLotIdList, ";", "','"))).append(")");
        return this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    private String addVirtualTI(String virtualTI, String value) {
        return virtualTI.length() > 0 ? ";" + value : "";
    }

    public void postEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        SDIData beforeImage = this.getBeforeEditImage();
        DataSet oldPrimary = beforeImage.getDataset("reagentlotrecipe");
        if (actionProps != null && actionProps.get("trackitemid") != null
            && actionProps.get("amount") != null && actionProps.get("amountunits") != null
            && actionProps.get("amountunitstype") != null && actionProps.get("reagentlotid") != null
            && actionProps.get("reagentlotrecipeitemid") != null) {
            char decimalSeparator = FormatUtil.getInstance(this.connectionInfo)
                .getDecimalSeparator();
            String currTIIds = actionProps.get("trackitemid").toString();
            String[] allTIIds = StringUtil.split(currTIIds, ";");
            String currUseAmountList = actionProps.getProperty("amount", "0.0")
                .replace(decimalSeparator, '.');
            String[] allUseAmounts = StringUtil.split(currUseAmountList, ";");
            String currUseAmountUnitList = actionProps.get("amountunits").toString();
            String[] allUseAmountUnits = StringUtil.split(currUseAmountUnitList, ";");
            String currUseAmountUnitsTypeList = actionProps.get("amountunitstype").toString();
            String[] allUseAmountUnitsTypes = StringUtil.split(currUseAmountUnitsTypeList, ";");
            String reagentLotIdList = actionProps.get("reagentlotid").toString();
            String[] allReagentLotIds = StringUtil.split(reagentLotIdList, ";");
            DataSet reagentLotDS = this.getReagentLotDS(reagentLotIdList);
            String reagentLotRecipeItemIdList = actionProps.get("reagentlotrecipeitemid")
                .toString();
            String[] allReagentLotRecipeItemIds = StringUtil.split(reagentLotRecipeItemIdList, ";");
            HashMap currKeys = new HashMap();
            StringBuffer trackitemids = new StringBuffer();
            StringBuffer quantities = new StringBuffer();
            StringBuffer quantityunits = new StringBuffer();
            StringBuffer quantityunittypes = new StringBuffer();
            StringBuffer unmanagedTIs = new StringBuffer();

            for (int noOfTIs = 0; noOfTIs < allTIIds.length; ++noOfTIs) {
                String currTIId = allTIIds[noOfTIs];
                currKeys.put("reagentlotid", allReagentLotIds[noOfTIs]);
                int findRow = reagentLotDS.findRow(currKeys);
                String virtualTI = "";
                if (findRow > -1) {
                    virtualTI = reagentLotDS.getString(findRow, "trackitemid");
                }

                currKeys.put("reagentlotrecipeitemid", allReagentLotRecipeItemIds[noOfTIs]);
                int filteredRowNumber = oldPrimary.findRow(currKeys);
                String preUseReagentTIId = oldPrimary.getValue(filteredRowNumber, "trackitemid");
                if (preUseReagentTIId == null) {
                    preUseReagentTIId = "";
                }

                String preUseAmount = "0.0";
                String preUseAmountUnits = "";
                String preUseAmountUnitsType = "";
                if (preUseReagentTIId.length() > 0) {
                    preUseAmount = oldPrimary.getBigDecimal(filteredRowNumber, "amount",
                        new BigDecimal("0.0")).toString();
                    preUseAmountUnits = oldPrimary.getValue(filteredRowNumber, "amountunits");
                    preUseAmountUnitsType = oldPrimary.getValue(filteredRowNumber,
                        "amountunitstype");
                    if (preUseAmount == null || preUseAmount.length() == 0
                        || preUseAmount.equalsIgnoreCase("(null)")) {
                        preUseAmount = "0.0";
                    }

                    if (preUseAmountUnits == null || preUseAmountUnits.equalsIgnoreCase("(null)")) {
                        preUseAmountUnits = "";
                    }

                    if (preUseAmountUnitsType == null || preUseAmountUnitsType.equalsIgnoreCase(
                        "(null)")) {
                        preUseAmountUnitsType = "";
                    }
                }

                if (currTIId.equals("(null)")) {
                    if (preUseReagentTIId.length() > 0) {
                        trackitemids.append(";").append(preUseReagentTIId);
                        quantities.append(";").append(preUseAmount);
                        quantityunits.append(";").append(preUseAmountUnits);
                        quantityunittypes.append(";").append(preUseAmountUnitsType);
                    }
                } else {
                    String currUseAmount = allUseAmounts[noOfTIs];
                    String currUseAmountUnit = allUseAmountUnits[noOfTIs];
                    String currUseAmountUnitsType = allUseAmountUnitsTypes[noOfTIs];
                    if (currUseAmount == null || currUseAmount.length() == 0
                        || currUseAmount.equalsIgnoreCase("(null)")) {
                        currUseAmount = "0.0";
                    }

                    if (currUseAmountUnit == null || currUseAmountUnit.equalsIgnoreCase("(null)")) {
                        currUseAmountUnit = "";
                    }

                    if (currUseAmountUnitsType == null || currUseAmountUnitsType.equalsIgnoreCase(
                        "(null)")) {
                        currUseAmountUnitsType = "";
                    }

                    if (preUseReagentTIId.length() == 0) {
                        if (currTIId.length() > 0) {
                            trackitemids.append(";")
                                .append(currTIId + this.addVirtualTI(virtualTI, virtualTI));
                            quantities.append(";").append(
                                "" + this.negate(currUseAmount) + this.addVirtualTI(virtualTI,
                                    currUseAmount));
                            quantityunits.append(";").append(
                                currUseAmountUnit + this.addVirtualTI(virtualTI,
                                    currUseAmountUnit));
                            quantityunittypes.append(";").append(
                                currUseAmountUnitsType + this.addVirtualTI(virtualTI,
                                    currUseAmountUnitsType));
                            unmanagedTIs.append(";").append(currTIId);
                        }
                    } else if (currTIId.length() > 0) {
                        if (!preUseReagentTIId.equals(currTIId)) {
                            trackitemids.append(";").append(
                                preUseReagentTIId + ";" + currTIId + this.addVirtualTI(virtualTI,
                                    virtualTI + ";" + virtualTI));
                            quantities.append(";").append(
                                preUseAmount + ";" + this.negate(currUseAmount) + this.addVirtualTI(
                                    virtualTI, this.negate(preUseAmount) + ";" + currUseAmount));
                            quantityunits.append(";").append(
                                preUseAmountUnits + ";" + currUseAmountUnit + this.addVirtualTI(
                                    virtualTI, currUseAmountUnit + ";" + preUseAmountUnits));
                            quantityunittypes.append(";").append(
                                preUseAmountUnitsType + ";" + currUseAmountUnitsType
                                    + this.addVirtualTI(virtualTI,
                                    currUseAmountUnitsType + ";" + preUseAmountUnitsType));
                            unmanagedTIs.append(";").append(currTIId);
                        } else {
                            double dpreUseAmt = Double.parseDouble(preUseAmount);
                            double dcurrUseAmt = Double.parseDouble(currUseAmount);
                            if (dpreUseAmt == dcurrUseAmt && preUseAmountUnits.equalsIgnoreCase(
                                currUseAmountUnit) && preUseAmountUnitsType.equalsIgnoreCase(
                                currUseAmountUnitsType)) {
                                continue;
                            }

                            if (dpreUseAmt != 0.0) {
                                trackitemids.append(";").append(currTIId);
                                quantities.append(";").append(preUseAmount);
                                quantityunits.append(";").append(preUseAmountUnits);
                                quantityunittypes.append(";").append(preUseAmountUnitsType);
                            }

                            if (dcurrUseAmt != 0.0) {
                                trackitemids.append(";").append(currTIId);
                                quantities.append(";").append(this.negate(currUseAmount));
                                quantityunits.append(";").append(currUseAmountUnit);
                                quantityunittypes.append(";").append(currUseAmountUnitsType);
                            }

                            if (virtualTI.length() > 0) {
                                if (dcurrUseAmt != 0.0) {
                                    trackitemids.append(";").append(virtualTI);
                                    quantities.append(";").append(currUseAmount);
                                    quantityunits.append(";").append(currUseAmountUnit);
                                    quantityunittypes.append(";").append(currUseAmountUnitsType);
                                }

                                if (dpreUseAmt != 0.0) {
                                    trackitemids.append(";").append(virtualTI);
                                    quantities.append(";").append(this.negate(preUseAmount));
                                    quantityunits.append(";").append(preUseAmountUnits);
                                    quantityunittypes.append(";").append(preUseAmountUnitsType);
                                }
                            }
                        }
                    }

                    currKeys.clear();
                }
            }

            if (trackitemids.length() > 0) {
                this.adjustTrackItemAmount(trackitemids.substring(1), quantities.substring(1),
                    quantityunits.substring(1), quantityunittypes.substring(1));
            }

            if (unmanagedTIs.length() > 0) {
                ReagentUtil.updateUsedDetailsOfUnManagedTrackItemInv(unmanagedTIs.substring(1),
                    this.getQueryProcessor(), this.getActionProcessor());
            }

        }
    }

    private void adjustTrackItemAmount(String trackitemids, String quantities, String quantityunits,
        String quantityunittypes) {
        PropertyList trackItemProperties = new PropertyList();
        trackItemProperties.setProperty("trackitemid", trackitemids);
        trackItemProperties.setProperty("quantity", quantities);
        trackItemProperties.setProperty("quantityunit", quantityunits);
        trackItemProperties.setProperty("quantitytype", quantityunittypes);

        try {
            this.getActionProcessor().processAction("AdjustTrackItemInv", "1", trackItemProperties);
        } catch (SapphireException var7) {
            this.setError(this.ruleid, "FAILURE",
                "Failed to adjust container amount. Check the use amount and units.");
        }
    }

    public boolean requiresBeforeEditImage() {
        return true;
    }

    public boolean requiresBeforeEditDetailImage() {
        return true;
    }

    private String negate(String amount) {
        if (amount != null && amount.length() != 0) {
            double amt = Double.parseDouble(amount);
            amt = 0.0 - amt;
            return "" + amt;
        } else {
            return "0.0";
        }
    }

    private void processSamplingPlan(DataSet primary) throws SapphireException {
        ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.getConnectionId());
        String prodVariantType = "ReagentTypeSupplier";
        PropertyList pl = SamplingPlanUtil.getPolicyPropertyList(this.getSdcid(), configProcessor);
        if (pl != null) {
            String applyEvent = pl.getProperty("applyevent", "");
            StringBuffer appSamplingPlanKeyIds = new StringBuffer();
            StringBuffer evalProdVarKeyIds = new StringBuffer();

            String disposition;
            for (int i = 0; i < primary.getRowCount(); ++i) {
                if (this.hasPrimaryValueChanged(primary, i, "reagentstatus")) {
                    disposition = primary.getValue(i, "reagentstatus", "");
                    if (disposition.length() > 0 && "Initial".equalsIgnoreCase(disposition)
                        && applyEvent.equalsIgnoreCase("On Creation")) {
                        appSamplingPlanKeyIds.append(";")
                            .append(primary.getValue(i, "reagentlotid"));
                    }
                }

                if (this.hasPrimaryValueChanged(primary, i, "disposition")) {
                    disposition = primary.getValue(i, "disposition", "");
                    if ("passed".equalsIgnoreCase(disposition) || "failed".equalsIgnoreCase(
                        disposition)) {
                        evalProdVarKeyIds.append(";").append(primary.getValue(i, "reagentlotid"));
                    }
                }
            }

            PropertyList props;
            if (appSamplingPlanKeyIds.length() > 0) {
                props = new PropertyList();
                props.setProperty("sdcid", this.getSdcid());
                props.setProperty("keyid1", appSamplingPlanKeyIds.substring(1));
                props.setProperty("prodvarianttype", prodVariantType);
                this.getActionProcessor().processAction("ApplySamplingPlan", "1", props);
                disposition = props.getProperty("return_message");
                if (!disposition.equalsIgnoreCase("success")) {
                    this.setError("ProcessSamplingPlan", "INFORMATION", disposition);
                }
            }

            if (evalProdVarKeyIds.length() > 0) {
                props = new PropertyList();
                props.setProperty("sdcid", this.getSdcid());
                props.setProperty("keyid1", evalProdVarKeyIds.substring(1));
                props.setProperty("prodvarianttype", prodVariantType);
                this.getActionProcessor().processAction("EvalProdVariantState", "1", props);
            }
        }

    }

    private void addReagentLotStage(String reagentLotid, String reagentTypeid,
        String reagentTypeVersionid) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT * FROM reagenttypestage");
        sql.append(" WHERE reagenttypeid=").append(safeSQL.addVar(reagentTypeid));
        sql.append(" AND reagenttypeversionid=").append(safeSQL.addVar(reagentTypeVersionid));
        sql.append(" order by usersequence");
        DataSet ds = this.getQueryProcessor()
            .getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        PropertyList reagentStageProps = new PropertyList();
        reagentStageProps.setProperty("sdcid", "LV_ReagentLot");
        reagentStageProps.setProperty("linkid", "ReagentLot Stage");
        reagentStageProps.setProperty("keyid1", reagentLotid);
        StringBuffer stageids = new StringBuffer();
        StringBuffer labels = new StringBuffer();
        StringBuffer descs = new StringBuffer();
        StringBuffer usersequences = new StringBuffer();

        for (int i = 0; i < ds.size(); ++i) {
            String reagenttypestageid = ds.getValue(i, "reagenttypestageid");
            String stagelabel = ds.getValue(i, "stagelabel");
            String stagedescription = ds.getValue(i, "stagedescription");
            stageids.append(";").append(reagenttypestageid);
            labels.append(";").append(stagelabel);
            descs.append(";").append(stagedescription);
            usersequences.append(";").append(String.valueOf(i + 1));
        }

        if (stageids.length() > 0) {
            reagentStageProps.setProperty("reagentlotstageid", stageids.substring(1));
            reagentStageProps.setProperty("stagelabel", labels.substring(1));
            reagentStageProps.setProperty("stagedescription", descs.substring(1));
            reagentStageProps.setProperty("usersequence", usersequences.substring(1));

            try {
                this.getActionProcessor().processAction("AddSDIDetail", "1", reagentStageProps);
            } catch (Exception var16) {
                throw new SapphireException("DB_ACTION_FAILED", "Not able to Add Reagent Stage",
                    var16);
            }
        }

    }

    private void setConcentration(DataSet primary) throws SapphireException {
        QueryProcessor qp = this.getQueryProcessor();
        SafeSQL safeSQL = new SafeSQL();

        for (int i = 0; i < primary.size(); ++i) {
            safeSQL.reset();
            String reagenttypid = primary.getString(i, "reagenttypeid", "");
            String reagentTypVersionid = primary.getString(i, "reagenttypeversionid", "");
            String sql =
                "select targetconcentrationparamid,targetconcentrationparamtype,targetconcentration,activematerialid from reagenttype where reagenttypeid = "
                    + safeSQL.addVar(reagenttypid) + " and reagenttypeversionid=" + safeSQL.addVar(
                    reagentTypVersionid);
            DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                primary.setString(i, "targetconcentrationparamid",
                    ds.getString(0, "targetconcentrationparamid", ""));
                primary.setString(i, "targetconcentrationparamtype",
                    ds.getString(0, "targetconcentrationparamtype", ""));
                primary.setValue(i, "targetconcentration",
                    ds.getValue(0, "targetconcentration", ""));
                primary.setString(i, "activematerialid", ds.getString(0, "activematerialid", ""));
            }
        }

    }

    public void postDataEntry(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet sdidataitem = sdiData.getDataset("dataitem");
        if (sdidataitem != null && sdidataitem.size() > 0) {
            QueryProcessor qp = this.getQueryProcessor();
            SafeSQL safeSQL = new SafeSQL();
            String reagentlot = sdidataitem.getString(0, "keyid1");
            String sql =
                "select reagentlotid,targetconcentrationparamid,targetconcentrationparamtype,targetconcentration from reagentlot where reagentlotid = "
                    + safeSQL.addVar(reagentlot);
            DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            String targetconcparamid = "";
            String targetconcparamtype = "";
            if (ds != null && ds.size() > 0) {
                targetconcparamid = ds.getString(0, "targetconcentrationparamid", "");
                targetconcparamtype = ds.getString(0, "targetconcentrationparamtype", "");
                if (targetconcparamid.length() > 0) {
                    HashMap map = new HashMap();
                    map.put("paramid", targetconcparamid);
                    map.put("paramtype", targetconcparamtype);
                    int findRowIDx = sdidataitem.findRow(map);
                    if (findRowIDx >= 0) {
                        String targetconc = ds.getValue(0, "targetconcentration", "");
                        String actualValue = sdidataitem.getValue(findRowIDx, "transformvalue", "");
                        this.updateConcentration(targetconc, actualValue, reagentlot);
                    }
                }
            }
        }

    }

    private void updateConcentration(String targetconc, String actualValue, String reagentlot)
        throws SapphireException {
        FormatUtil formatUtil = FormatUtil.getInstance(this.connectionInfo);
        if (actualValue.length() > 0) {
            actualValue = actualValue.replace('.', formatUtil.getDecimalSeparator());
            PropertyList sampleProps = new PropertyList();
            sampleProps.setProperty("sdcid", "LV_ReagentLot");
            sampleProps.setProperty("keyid1", reagentlot);
            sampleProps.setProperty("actualconcentration", actualValue);
            if (targetconc.length() > 0) {
                targetconc = targetconc.replace('.', formatUtil.getDecimalSeparator());
                double poc = formatUtil.parseBigDecimal(actualValue).doubleValue()
                    / formatUtil.parseBigDecimal(targetconc).doubleValue() * 100.0;
                BigDecimal bd = BigDecimal.valueOf(poc);
                bd = bd.setScale(2, 4);
                String strValue = this.removeLastZerosAferDecimal(bd.toString(),
                    formatUtil.getDecimalSeparator());
                sampleProps.setProperty("percentoftargetconcentration", strValue);
            }

            this.getActionProcessor().processAction("EditSDI", "1", sampleProps);
        }

    }

    private String removeLastZerosAferDecimal(String amountTextStr, char decimalSeperator) {
        String temp = amountTextStr;
        if (amountTextStr.indexOf(46) >= 0 || amountTextStr.indexOf(decimalSeperator) >= 0) {
            for (int i = amountTextStr.length(); i > 1; --i) {
                if (amountTextStr.charAt(i - 1) != '0') {
                    if (amountTextStr.charAt(i - 1) == '.'
                        || amountTextStr.charAt(i - 1) == decimalSeperator) {
                        temp = temp.substring(0, temp.length() - 1);
                    }
                    break;
                }

                temp = temp.substring(0, temp.length() - 1);
            }
        }

        return temp;
    }
}
