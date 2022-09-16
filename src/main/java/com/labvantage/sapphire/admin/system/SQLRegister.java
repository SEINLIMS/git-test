package com.labvantage.sapphire.admin.system;

import com.labvantage.sapphire.Cache;
import java.util.HashMap;
import org.apache.commons.codec.digest.DigestUtils;
import sapphire.ext.BaseSQLRegister;
import sapphire.xml.PropertyList;

public class SQLRegister extends BaseSQLRegister {

    private static SQLRegister sqlRegister;
    private static HashMap<String, BaseSQLRegister> dbSQLRegisters;
    private static Cache dynamicSQLMap = new Cache("Dynamic SQL Cache", 5000);

    public SQLRegister() {
    }

    public static void setSqlRegister(String databaseid, BaseSQLRegister sqlRegister) {
        if (sqlRegister == null) {
            sqlRegister = new SQLRegister();
        }

        if (dbSQLRegisters == null) {
            dbSQLRegisters = new HashMap();
        }

        dbSQLRegisters.put(databaseid, sqlRegister);
    }

    public static String getSQL(String databaseid, int sqlCode) {
        if (sqlRegister == null) {
            sqlRegister = new SQLRegister();
        }

        String sql = sqlRegister.getSQLStatement(sqlCode);
        if (sql.length() == 0) {
            BaseSQLRegister dbSQLRegister = (BaseSQLRegister) dbSQLRegisters.get(databaseid);
            if (dbSQLRegister != null) {
                sql = dbSQLRegister.getSQLStatement(sqlCode);
            }
        }

        return sql;
    }

    public static String registerDynamicSQL(Object sqlDefintion) {
        String code = sqlDefintion instanceof String ? (String) sqlDefintion
            : ((PropertyList) sqlDefintion).toJSONString(false, false);
        String dynamicCode = DigestUtils.md5Hex(code);
        dynamicSQLMap.put(dynamicCode, sqlDefintion);
        return dynamicCode;
    }

    public static Object getDynamicSQL(String dynamicCode) {
        return dynamicSQLMap.get(dynamicCode);
    }

    public String getSQLStatement(int sqlCode) {
        switch (sqlCode) {
            case 10000:
                return "select w.webpageid, w.productedition from webpage w where lower(w.webpageid) like ?";
            case 10001:
                return "select propertytreeid from propertytree where propertytreetype like ? and lower( propertytreeid ) like ? order by propertytreeid";
            case 10010:
                return "select eventdeflabel from s_eventdef where s_eventdefid = ?";
            case 10020:
                return "select * from s_studysite where sstudyid = ? and activeflag='Y'";
            case 10021:
                return "select c.s_cpcohortid, c.cohortdesc, p.versionstatus from s_cpcohort c, s_studysite s, s_clinicalprotocol p  where s.s_studysiteid = ?  and s.sstudyid = c.s_clinicalprotocolid and s.clinicalprotocolrevision = c.s_clinicalprotocolrevision  and c.s_clinicalprotocolid = p.s_clinicalprotocolid and c.s_clinicalprotocolrevision = p.s_clinicalprotocolrevision  and c.s_clinicalprotocolversionid = p.s_clinicalprotocolversionid  and ( p.versionstatus='C')";
            case 10022:
                return "select * from s_subject where s_subjectid = ?";
            case 10023:
                return "select clinicalflag from s_study where s_studyid = ?";
            case 10030:
                return "select distinct levelid from s_spdetail where s_samplingplanid = ? and s_samplingplanversionid = ?";
            case 10031:
                return "select distinct levelid from s_spdetail where s_samplingplanid = ? and s_samplingplanversionid = ?";
            case 10040:
                return "select distinct sp.s_samplingplanid, sp.s_samplingplanversionid, sp.levelid from s_spdetail sp join s_batch b on sp.s_samplingplanid = b.samplingplanid and sp.s_samplingplanversionid = b.samplingplanversionid where b.s_batchid = ?";
            case 10050:
                return "SELECT keyidusablesize FROM sdc WHERE sdcid = 'LV_ClinicalProtocol'";
            case 10060:
                return "select s_studyid from s_study where studyalias = ?";
            case 10070:
                return "select addressid from address where addresstype = 'Device' order by addressid";
            case 10080:
                return "select batchstatus from s_batch where s_batchid = ?";
            case 10081:
                return "SElECT batchstatus FROM s_batch WHERE s_batchid IN ( [batchids] )";
            case 10090:
                return "select refvalueid from refvalue where ( activeflag='Y' OR activeflag is null) AND reftypeid = 'SampleProcedure' order by usersequence, refvalueid";
            case 10091:
                return "select incidentid from incident where templateflag = 'Y' and incidentcategory = 'UnPlanned' order by incidentid";
            case 10100:
                return "select departmentid, departmentdesc from department where departmentid in ( select sd.departmentid from departmentsysuser sd where sd.sysuserid = ? ) and allowtempflag = 'Y'";
            case 10101:
                return "select refvalueid, refvaluedesc from refvalue where ( activeflag='Y' OR activeflag is null) AND reftypeid = ? order by usersequence, refvalueid";
            case 10102:
                return "select refvalueid from refvalue where ( activeflag='Y' OR activeflag is null) AND reftypeid = 'SampleDeviation' order by usersequence, refvalueid";
            case 10110:
                return "select storageunitid from storageunit where linksdcid = 'PhysicalStore' and linkkeyid1 = ?";
            case 10120:
                return "select storageunitid, labelpath from storageunit where storageunitid in ( [storageunitids] )";
            case 10130:
                return "select distinct label from s_processstage ps, s_batch b WHERE ps.s_samplingplanid = b.samplingplanid AND ps.s_samplingplanversionid = b.samplingplanversionid AND ps.label IN (select distinct label from s_batchstage where batchid=?) AND b.s_batchid = ?";
            case 10140:
                return "select refvalueid, refdisplayvalue from refvalue where ( activeflag='Y' OR activeflag is null) AND reftypeid = ? order by usersequence, refvalueid";
            case 10150:
                return "select distinct keyid1 from categoryitem cat, units un where sdcid = 'Units' and categoryid in ('MassUnits','VolumeUnits') and cat.keyid1=un.unitsid";
            case 10160:
                return "select unitsid from units order by unitsid";
            case 10170:
                return "select storageunitid, linksdcid, linkkeyid1 from storageunit where storageunitid = ?";
            case 10171:
                return "select ti.trackitemid, ti.linkkeyid1 from trackitem ti, storageunit su where su.storageunittype = 'BoxPos' and su.storageunitid = ti.currentstorageunitid and ti.trackitemid in ( [trackitems] )";
            case 10172:
                return "select coalesce(refdisplayvalue, refvalueid) refdisplayvalue, refvalueid from refvalue where reftypeid = ? and (activeflag is null or activeflag = 'Y') order by usersequence";
            case 10180:
                return "select '[entity]' entity, 'LV_SampleFamily' sdcid, sstudyid, studysiteid, s_samplefamilyid, clinicalprotocolid, clinicalprotocolversionid, clinicalprotocolrevision from s_samplefamily where s_samplefamilyid = ?";
            case 10181:
                return "select '[entity]' entity, 'LV_Participant' sdcid, sstudyid, studysiteid, s_participantid, clinicalprotocolid, clinicalprotocolversionid, clinicalprotocolrevision from s_participant where s_participantid = ?";
            case 10182:
                return "select '[entity]' entity, 'LV_ParticipantEvent' sdcid, pe.s_participanteventid, p.sstudyid, p.studysiteid, p.s_participantid, p.clinicalprotocolid, p.clinicalprotocolversionid, p.clinicalprotocolrevision from s_participantevent pe, s_participant p where pe.participantid = p.s_participantid and pe.s_participanteventid = ?";
            case 10190:
                return "SELECT columnid, datatype, coalesce(columnlabel, columnid) collabel FROM syscolumn WHERE tableid = ? AND datatype IN ( 'N','R','C','D' ) order by collabel";
            case 10191:
                return "SELECT attributeid, datatype, coalesce(attributetitle, attributeid) attributetitle FROM sdcattributedef WHERE sdcid = ? order by attributetitle";
            case 10200:
                return "SELECT arraylayoutzone FROM arraylayoutzone WHERE arraylayoutid = ? and arraylayoutversionid = ? order by usersequence";
            case 10201:
                return "select r.managecontainerinventoryflag,t.qtyunits from reagentlot r,trackitem t where r.reagentlotid = ? and t.trackitemid = ? and r.reagentlotid = t.linkkeyid1 and t.linksdcid = 'LV_ReagentLot'";
            case 10202:
                return "select * from reagenttype where reagenttypeid=? and reagenttypeversionid=?";
            case 10203:
                return "select * from reagenttype where reagenttypeid=? and reagenttypeversionid=coalesce(NULLIF(?,'') ,(select rt1.reagenttypeversionid from reagenttype rt1 where  rt1.reagenttypeid=reagenttype.reagenttypeid and rt1.versionstatus='C'),(select cast(max(cast(rt2.reagenttypeversionid as integer)) as varchar(40)) from reagenttype rt2 where  rt2.reagenttypeid=reagenttype.reagenttypeid and rt2.versionstatus='P'))";
            case 10204:
                return "select paramid,paramtype from arraymethodparamitem where arraymethodid = ? and levelflag = 'I'";
            case 10205:
                return "select workitemid from workitem where lower( workitemid )=?";
            case 10206:
                return "select paramid from param where lower( paramid )=?";
            case 10300:
                return "INSERT INTO search ( searchid, searchdt, searchtypeflag, sysuserid, enteredquery, queryclass, executedquery, searchtime, processtime, totaltime, hits, limit ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
            case 10301:
                return "INSERT INTO search ( searchid, searchdt, searchtypeflag, sysuserid, enteredquery, term, searchtime, processtime, totaltime, hits ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
            case 10320:
                return "select bulletin.bulletinid, bulletin.bulletintext, bulletin.priorityflag, bulletin.bulletindesc, bulletin.url, bulletin.source, bulletin.createdt, bulletinsysuser.readflag from bulletin, bulletinsysuser where bulletinsysuser.deletedflag='N' and bulletin.bulletinid = bulletinsysuser.bulletinid and    bulletinsysuser.sysuserid=? order by bulletin.createdt desc";
            case 10330:
                return "SELECT attachmentdesc, attachmentnum FROM sdiattachment WHERE sdcid = 'SDINote' AND keyid1 = ?";
            case 10340:
                return "SELECT trackitem.trackitemid, trackitem.linksdcid, trackitem.linkkeyid1, trackitem.linkkeyid2, trackitem.linkkeyid3, trackitem.currentstorageunitid, trackitem.custodialuserid, trackitem.custodytakendt,        storageunit.storageunitlabel, storageunit.labelpath FROM   trackitem LEFT OUTER JOIN storageunit ON trackitem.currentstorageunitid = storageunit.storageunitid WHERE  trackitem.trackitemid = ?";
            case 10341:
                return "SELECT trackitem.trackitemid, trackitem.linksdcid, trackitem.linkkeyid1, trackitem.linkkeyid2, trackitem.linkkeyid3, trackitem.currentstorageunitid, trackitem.custodialuserid, trackitem.custodytakendt,        storageunit.storageunitlabel, storageunit.labelpath FROM   trackitem LEFT OUTER JOIN storageunit ON trackitem.currentstorageunitid = storageunit.storageunitid WHERE  trackitem.linksdcid = ? AND trackitem.linkkeyid1 = ?";
            case 10342:
                return "SELECT trackitem.trackitemid, trackitem.linksdcid, trackitem.linkkeyid1, trackitem.linkkeyid2, trackitem.linkkeyid3, trackitem.currentstorageunitid, trackitem.custodialuserid, trackitem.custodytakendt,        storageunit.storageunitlabel, storageunit.labelpath FROM   trackitem LEFT OUTER JOIN storageunit ON trackitem.currentstorageunitid = storageunit.storageunitid WHERE  trackitem.linksdcid = ? AND trackitem.linkkeyid1 = ? AND trackitem.linkkeyid2 = ?";
            case 10343:
                return "SELECT trackitem.trackitemid, trackitem.linksdcid, trackitem.linkkeyid1, trackitem.linkkeyid2, trackitem.linkkeyid3, trackitem.currentstorageunitid, trackitem.custodialuserid, trackitem.custodytakendt,        storageunit.storageunitlabel, storageunit.labelpath FROM   trackitem LEFT OUTER JOIN storageunit ON trackitem.currentstorageunitid = storageunit.storageunitid WHERE  trackitem.linksdcid = ? AND trackitem.linkkeyid1 = ? AND trackitem.linkkeyid2 = ? AND trackitem.linkkeyid3 = ?";
            case 10344:
                return "SElECT distinct productid,productversionid FROM s_sample WHERE s_sampleid IN ( [sampleids] )";
            case 10345:
                return "select * from reagentlotstage where reagentlotid=? order by usersequence";
            case 10346:
                return "select paramid,paramtype from paramlistitem where paramlistid=? and paramlistversionid=coalesce(NULLIF(?,'') ,paramlistversionid) and variantid=? order by usersequence";
            case 20000:
                return "Select ag.columnid, ag.adhocqueryargdesc label, ag.groupflag, ag.operator,  ag.value, ag.usersequence, ag.argrelationflag, a.grouprelationflag, a.shareableflag, ag.notes from adhocqueryarg ag inner join adhocquery a on ag.adhocqueryid=a.adhocqueryid and a.basedonsdcid='[sdcid]' where ag.adhocqueryid='[adhocqueryid]' order by ag.groupflag, ag.usersequence";
            case 20001:
                return "SELECT actionid from action where lower( actionid ) like lower( '%[searchText]%' ) and ( activeflag is null or activeflag != 'N')";
            case 20002:
                return "SELECT sdcid, operationid FROM sdcoperation WHERE sdcid in ( [sdcIds] ) order by sdcid, usersequence";
            case 20003:
                return "select sdcid from sdc";
            case 20004:
                return "select columnid, columnlabel from syscolumn, sdc where syscolumn.tableid=sdc.tableid and sdcid='[sdcid]' order by columnid";
            case 20005:
                return "select columnid, columnlabel from syscolumn where tableid='[tableid]' order by columnid";
            case 20006:
                return "select actionid, propertyid from actionproperty where actionid in ([actionidlist]) and propertytypeflag='O'";
            case 20007:
                return "select * from paramlistitem where paramlistid='[paramlistid]' AND paramlistversionid='[paramlistversionid]' AND variantid='[variantid]' AND paramid='[paramid]' AND paramtype='[paramtype]'";
            case 20008:
                return "select unitsid as col0, 'A' from units where unitsid='[displayunits]' or unitsid in (select tounits from unitconversion where unitsid='[displayunits]') union select distinct limittypeid as col0, 'B' from sdidataitemlimits where sdcid='Sample' and keyid1='[keyid1]' and paramlistid='[paramlistid]' and paramlistversionid='[paramlistversionid]' and variantid='[variantid]' and paramid='[paramid]' and paramtype='[paramtype]' and sdidataitemlimits.limittypeid not in (select limittypeid from limitrulelimittype where rejectvalueflag='Y') order by 2,1";
            case 20009:
                return "select unitsid as col0, 'A' from units union select distinct limittypeid as col0, 'B' from sdidataitemlimits where sdcid='Sample' and keyid1='[keyid1]' and paramlistid='[paramlistid]' and paramlistversionid='[paramlistversionid]' and variantid='[variantid]' and paramid='[paramid]' and paramtype='[paramtype]' and sdidataitemlimits.limittypeid not in (select limittypeid from limitrulelimittype where rejectvalueflag='Y') order by 2,1";
            case 20010:
                return "select webpageid from webpage where webpageid in ( '[sdcid]Lookup', 'LV_[sdcid]Lookup' )";
            case 20011:
                return "SELECT * FROM statsmonitorvalue WHERE capturemode='[capturemode]' and statsmonitorgroupid='[statsmonitorgroupid]' AND statsmonitoritemid='[statsmonitoritemid]' AND hostname='[hostname]' order by hostname, capturedt";
            case 20012:
                return "SELECT * FROM statsmonitorvalue WHERE capturemode='[capturemode]' and statsmonitorgroupid='[statsmonitorgroupid]' AND statsmonitoritemid='[statsmonitoritemid]' order by hostname, capturedt";
            case 20013:
                return "SELECT * FROM statsmonitorvalue WHERE hostname='[hostname]' and statsmonitorgroupid='RESTART' order by capturedt";
            case 20014:
                return "select distinct t.instrumenttypeid from instrumenttype t, instrumenttypefield f where t.instrumenttypeid=f.instrumenttypeid and instrumentfieldid='[instrumentfieldid]' order by 1";
            case 20015:
                return "select instrumenttypeid from instrumenttype order by 1";
            case 20016:
                return "SELECT {fn concat( {fn concat( syscolumn.tableid, '_' )}, columnid )} FROM syscolumn WHERE syscolumn.tableid = '[sqlcode_tablename]' ORDER BY 1";
            case 20017:
                return "SELECT {fn concat( {fn concat( syscolumn.tableid, '_' )}, columnid )} FROM syscolumn, sdc WHERE syscolumn.tableid = sdc.tableid AND sdcid = '[sqlcode_sdcid]' ORDER BY 1";
            case 20018:
                return "SELECT sdcid FROM sdc WHERE sdctype <> 'D' ORDER BY 1";
            case 20019:
                return "SELECT * FROM trackitem where trackitemid=?";
            case 20020:
                return "select s_questionid FROM s_clinicalprotocolquestion where s_clinicalprotocolid = ? ORDER BY s_questionid desc";
            case 20021:
                return "select s_questionid FROM s_studyquestion where s_studyid = ? ORDER BY s_questionid desc";
            case 20022:
                return "SELECT valuetree FROM gizmodefuseroverride WHERE sysuserid=? AND gizmodefid=?";
            case 20023:
                return "INSERT INTO gizmodefuseroverride (valuetree, sysuserid, gizmodefid) VALUES (?,?,?)";
            case 20024:
                return "UPDATE gizmodefuseroverride SET valuetree=? WHERE sysuserid=? AND gizmodefid=?";
            case 20025:
                return "DELETE gizmodefuseroverride WHERE sysuserid=? AND gizmodefid=?";
            case 20026:
                return "DELETE gizmodefuseroverride WHERE gizmodefid=?";
            case 20030:
                return "UPDATE webpagelogtitle SET title=? WHERE webpagelogid=?";
            case 20031:
                return "select workitemid,workitemversionid,workitemdesc from workitem where workitemid =?";
            case 20032:
                return "select specid,specversionid,specdesc from spec where specid =?";
            case 20033:
                return "SELECT ACTIVITYWORKSDI.ACTIVITYID, SDIDATA.SDIDATAID FROM ACTIVITYWORKSDI,SDIDATA WHERE ACTIVITYWORKSDI.WORKSDCID='DataSet' AND ACTIVITYWORKSDI.WORKKEYID1 = SDIDATA.SDIDATAID AND SDIDATA.SDIDATAID IN ([ids])";
            case 20034:
                return "SELECT ACTIVITYWORKSDI.ACTIVITYID, ACTIVITYWORKSDI.WORKKEYID1 SDIWORKITEMID FROM ACTIVITYWORKSDI WHERE ACTIVITYWORKSDI.WORKSDCID = 'SDIWorkItem' AND ACTIVITYWORKSDI.WORKKEYID1 IN ([ids]) UNION SELECT ACTIVITYWORKSDI.ACTIVITYID, SDIWORKITEM.SDIWORKITEMID SDIWORKITEMID FROM ACTIVITYWORKSDI,SDIWORKITEM, SDIDATA WHERE SDIDATA.SDCID = SDIWORKITEM.SDCID AND SDIDATA.KEYID1 = SDIWORKITEM.KEYID1 AND SDIDATA.KEYID2 = SDIWORKITEM.KEYID2 AND SDIDATA.KEYID3 = SDIWORKITEM.KEYID3 AND SDIDATA.SOURCEWORKITEMID = SDIWORKITEM.WORKITEMID AND SDIDATA.SOURCEWORKITEMINSTANCE = SDIWORKITEM.WORKITEMINSTANCE AND ACTIVITYWORKSDI.WORKKEYID1 = SDIDATA.SDIDATAID AND ACTIVITYWORKSDI.WORKSDCID='DataSet' AND SDIWORKITEM.SDIWORKITEMID IN ([ids])";
            case 20035:
                return "SELECT ACTIVITYWORKSDI.ACTIVITYID, ACTIVITYWORKSDI.WORKKEYID1 SAMPLEID FROM ACTIVITYWORKSDI WHERE ACTIVITYWORKSDI.WORKSDCID = 'Sample' AND ACTIVITYWORKSDI.WORKKEYID1 IN ([ids]) UNION SELECT ACTIVITYWORKSDI.ACTIVITYID, SDIWORKITEM.KEYID1 SAMPLEID FROM ACTIVITYWORKSDI, SDIWORKITEM WHERE ACTIVITYWORKSDI.WORKSDCID='SDIWorkItem' AND ACTIVITYWORKSDI.WORKKEYID1 = SDIWORKITEM.SDIWORKITEMID AND SDIWORKITEM.SDCID = 'Sample' AND SDIWORKITEM.KEYID1 IN ([ids]) UNION SELECT ACTIVITYWORKSDI.ACTIVITYID, SDIDATA.KEYID1 SAMPLEID FROM ACTIVITYWORKSDI, SDIDATA WHERE ACTIVITYWORKSDI.WORKSDCID='DataSet' AND ACTIVITYWORKSDI.WORKKEYID1 = SDIDATA.SDIDATAID AND SDIDATA.SDCID = 'Sample' AND SDIDATA.KEYID1 IN ([ids])";
            case 20036:
                return "SELECT ACTIVITYWORKSDI.ACTIVITYID, ACTIVITYWORKSDI.WORKKEYID1 SAMPLEID, S_SAMPLE.BATCHID BATCHID FROM ACTIVITYWORKSDI, S_SAMPLE WHERE ACTIVITYWORKSDI.WORKSDCID='Sample' AND ACTIVITYWORKSDI.WORKKEYID1 = S_SAMPLE.S_SAMPLEID AND S_SAMPLE.BATCHID IN ([ids]) UNION SELECT ACTIVITYWORKSDI.ACTIVITYID, SDIWORKITEM.KEYID1 SAMPLEID, S_SAMPLE.BATCHID BATCHID FROM ACTIVITYWORKSDI, S_SAMPLE, SDIWORKITEM WHERE ACTIVITYWORKSDI.WORKSDCID='SDIWorkItem' AND ACTIVITYWORKSDI.WORKKEYID1=SDIWORKITEM.SDIWORKITEMID AND SDIWORKITEM.SDCID = 'Sample' AND SDIWORKITEM.KEYID1=S_SAMPLE.S_SAMPLEID AND  S_SAMPLE.BATCHID IN ([ids]) UNION SELECT ACTIVITYWORKSDI.ACTIVITYID, SDIDATA.KEYID1 SAMPLEID, S_SAMPLE.BATCHID BATCHID FROM ACTIVITYWORKSDI, S_SAMPLE, SDIDATA WHERE ACTIVITYWORKSDI.WORKSDCID='DataSet' AND ACTIVITYWORKSDI.WORKKEYID1 = SDIDATA.SDIDATAID AND SDIDATA.SDCID = 'Sample' AND SDIDATA.KEYID1= S_SAMPLE.S_SAMPLEID AND  S_SAMPLE.BATCHID IN ([ids])";
            case 20037:
                return "SELECT ACTIVITYWORKSDI.ACTIVITYID, S_SAMPLE.S_SAMPLEID SAMPLEID, S_SAMPLE.MONITORGROUPID MONITORGROUPID FROM ACTIVITYWORKSDI, S_SAMPLE WHERE ACTIVITYWORKSDI.WORKSDCID='Sample' AND ACTIVITYWORKSDI.WORKKEYID1 = S_SAMPLE.S_SAMPLEID AND S_SAMPLE.MONITORGROUPID IN ([ids]) UNION SELECT ACTIVITYWORKSDI.ACTIVITYID, SDIWORKITEM.KEYID1 SAMPLEID, S_SAMPLE.MONITORGROUPID MONITORGROUPID FROM ACTIVITYWORKSDI, S_SAMPLE, SDIWORKITEM WHERE ACTIVITYWORKSDI.WORKSDCID='SDIWorkItem' AND ACTIVITYWORKSDI.WORKKEYID1=SDIWORKITEM.SDIWORKITEMID AND SDIWORKITEM.SDCID = 'Sample' AND SDIWORKITEM.KEYID1=S_SAMPLE.S_SAMPLEID AND S_SAMPLE.MONITORGROUPID IN ([ids]) UNION SELECT ACTIVITYWORKSDI.ACTIVITYID, SDIDATA.KEYID1 SAMPLEID, S_SAMPLE.MONITORGROUPID MONITORGROUPID FROM ACTIVITYWORKSDI, S_SAMPLE, SDIDATA WHERE ACTIVITYWORKSDI.WORKSDCID='DataSet' AND ACTIVITYWORKSDI.WORKKEYID1 = SDIDATA.SDIDATAID AND SDIDATA.SDCID = 'Sample' AND SDIDATA.KEYID1= S_SAMPLE.S_SAMPLEID AND S_SAMPLE.MONITORGROUPID IN ([ids])";
            case 20038:
                return "SELECT ACTIVITYWORKSDI.ACTIVITYID, S_SAMPLE.S_SAMPLEID SAMPLEID, S_SAMPLE.REQUESTID REQUESTID FROM ACTIVITYWORKSDI, S_SAMPLE WHERE ACTIVITYWORKSDI.WORKSDCID='Sample' AND ACTIVITYWORKSDI.WORKKEYID1= S_SAMPLE.S_SAMPLEID AND S_SAMPLE.REQUESTID IN ([ids]) UNION SELECT ACTIVITYWORKSDI.ACTIVITYID, SDIWORKITEM.KEYID1 SAMPLEID, S_SAMPLE.REQUESTID REQUESTID FROM ACTIVITYWORKSDI, S_SAMPLE, SDIWORKITEM WHERE ACTIVITYWORKSDI.WORKSDCID='SDIWorkItem' AND ACTIVITYWORKSDI.WORKKEYID1=SDIWORKITEM.SDIWORKITEMID AND SDIWORKITEM.SDCID = 'Sample' AND SDIWORKITEM.KEYID1=S_SAMPLE.S_SAMPLEID AND S_SAMPLE.REQUESTID IN ([ids]) UNION SELECT ACTIVITYWORKSDI.ACTIVITYID, SDIDATA.KEYID1 SAMPLEID, S_SAMPLE.REQUESTID REQUESTID FROM ACTIVITYWORKSDI, S_SAMPLE, SDIDATA WHERE ACTIVITYWORKSDI.WORKSDCID='DataSet' AND ACTIVITYWORKSDI.WORKKEYID1 = SDIDATA.SDIDATAID AND SDIDATA.SDCID = 'Sample' AND SDIDATA.KEYID1= S_SAMPLE.S_SAMPLEID AND  S_SAMPLE.REQUESTID IN ([ids])";
            case 20039:
                return "SELECT S_SAMPLEID,WAPSTATUS FROM S_SAMPLE WHERE S_SAMPLEID IN ([ids]) AND WAPSTATUS='Pending'";
            case 20040:
                return "SELECT SDIWORKITEMID,WAPSTATUS FROM SDIWORKITEM WHERE SDIWORKITEMID IN ([ids]) AND WAPSTATUS='Pending'";
            case 20041:
                return "SELECT SDIDATAID, WAPSTATUS FROM SDIDATA WHERE SDIDATAID IN ([ids]) AND WAPSTATUS='Pending'";
            case 20042:
                return "SELECT ACTIVITYWORKSDI.ACTIVITYID, S_SAMPLE.S_SAMPLEID SAMPLEID, S_SAMPLE.STUDYID STUDYID  FROM ACTIVITYWORKSDI, S_SAMPLE WHERE ACTIVITYWORKSDI.WORKSDCID='Sample' AND ACTIVITYWORKSDI.WORKKEYID1= S_SAMPLE.S_SAMPLEID AND S_SAMPLE.STUDYID IN ([ids]) UNION SELECT ACTIVITYWORKSDI.ACTIVITYID, SDIWORKITEM.KEYID1 SAMPLEID, S_SAMPLE.STUDYID STUDYID FROM ACTIVITYWORKSDI, S_SAMPLE, SDIWORKITEM WHERE ACTIVITYWORKSDI.WORKSDCID='SDIWorkItem' AND ACTIVITYWORKSDI.WORKKEYID1=SDIWORKITEM.SDIWORKITEMID AND SDIWORKITEM.SDCID = 'Sample' AND SDIWORKITEM.KEYID1=S_SAMPLE.S_SAMPLEID AND S_SAMPLE.STUDYID IN ([ids]) UNION SELECT ACTIVITYWORKSDI.ACTIVITYID, SDIDATA.KEYID1 SAMPLEID, S_SAMPLE.STUDYID STUDYID FROM ACTIVITYWORKSDI, S_SAMPLE, SDIDATA WHERE ACTIVITYWORKSDI.WORKSDCID='DataSet' AND ACTIVITYWORKSDI.WORKKEYID1 = SDIDATA.SDIDATAID AND SDIDATA.SDCID = 'Sample' AND SDIDATA.KEYID1= S_SAMPLE.S_SAMPLEID AND S_SAMPLE.STUDYID IN ([ids])";
            case 20043:
                return "SELECT ACTIVITYWORKSDI.ACTIVITYID, S_SAMPLE.S_SAMPLEID SAMPLEID, S_SAMPLE.SSTUDYID SSTUDYID  FROM ACTIVITYWORKSDI, S_SAMPLE WHERE ACTIVITYWORKSDI.WORKSDCID='Sample' AND ACTIVITYWORKSDI.WORKKEYID1= S_SAMPLE.S_SAMPLEID AND S_SAMPLE.SSTUDYID IN ([ids]) UNION SELECT ACTIVITYWORKSDI.ACTIVITYID, SDIWORKITEM.KEYID1 SAMPLEID, S_SAMPLE.SSTUDYID SSTUDYID FROM ACTIVITYWORKSDI, S_SAMPLE, SDIWORKITEM WHERE ACTIVITYWORKSDI.WORKSDCID='SDIWorkItem' AND ACTIVITYWORKSDI.WORKKEYID1=SDIWORKITEM.SDIWORKITEMID AND SDIWORKITEM.SDCID = 'Sample' AND SDIWORKITEM.KEYID1=S_SAMPLE.S_SAMPLEID AND S_SAMPLE.SSTUDYID IN ([ids]) UNION SELECT ACTIVITYWORKSDI.ACTIVITYID, SDIDATA.KEYID1 SAMPLEID, S_SAMPLE.SSTUDYID SSTUDYID FROM ACTIVITYWORKSDI, S_SAMPLE, SDIDATA WHERE ACTIVITYWORKSDI.WORKSDCID='DataSet' AND ACTIVITYWORKSDI.WORKKEYID1 = SDIDATA.SDIDATAID AND SDIDATA.SDCID = 'Sample' AND SDIDATA.KEYID1= S_SAMPLE.S_SAMPLEID AND S_SAMPLE.SSTUDYID IN ([ids])";
            case 20044:
                return "SELECT reportid FROM report WHERE sdcidvalue = ?";
            case 20050:
                return "SELECT issueid, createby FROM issue where issuestatus IN ('In Review','Initial')";
            case 20051:
                return "select count(1) numberofdepartmentshifts from departmentshift where shiftid = ? and departmentid=?";
            case 20052:
                return "select collectorpropertytreeid, collectorextendnodeid from instrumentmodel where instrumentmodelid=? and instrumenttypeid=?";
            case 20053:
                return "select locationlabel from S_SAMPLEPOINT where s_samplepointid=?";
            case 20054:
                return "select productid,productversionid from s_sample where s_sampleid=?";
            case 20055:
                return "select worksheetid from worksheetsdi where sdcid = 'QCBatch' and keyid1 = ?";
            case 20056:
                return "select sii.keyid1,sii.itemkeyid1 paramlistid,sii.itemkeyid2 paramlistversionid,sii.itemkeyid3 variantid,sii.iteminstance dataset from sdiworkitem si,sdiworkitemitem sii where si.sdcid=sii.sdcid and si.keyid1=sii.keyid1 and si.keyid2=sii.keyid2 and si.keyid3=sii.keyid3 and si.workitemid=sii.workitemid and si.workiteminstance=sii.workiteminstance and sdiworkitemid in ([sdiworkitemid])";
            case 20057:
                return "UPDATE sdiattachment SET lockedflag=?,lockedby=? WHERE sdcid=? AND keyid1=? AND keyid2=? AND keyid3=? AND attachmentnum=?";
            case 20058:
                return "select * from paramlist where PARAMLISTID = ? and PARAMLISTVERSIONID= ? and VARIANTID= ?";
            case 20059:
                return "select controlledflag from query where queryid = ? and basedonid = ?";
            case 20060:
                return "select r.s_requestid from s_request r where r.s_requestid in ([ids]) and r.TEMPLATEFLAG!='Y' and not exists (select 1 from sdiapproval s where s.keyid1=r.s_requestid and s.approvalfunction='Acceptance')";
            case 20061:
                return "select s_batchid from s_batch where s_batchid in ([ids]) and (batchstatus='Preliminary Release' or exists (select keyid1 from sdiapproval where sdcid='Batch' and keyid1 = s_batch.s_batchid) )  ";
            case 20062:
                return "select basedepartment from sysuser where sysuserid=?";
            case 20063:
                return "SELECT 'Department' as sdcid, departmentid as keyid1 FROM portaldepartment WHERE portalid = ? UNION SELECT 'LV_App' as sdcid, appid as keyid1 FROM portalapp WHERE portalid = ? ORDER BY sdcid, keyid1";
            case 20064:
                return "SELECT 'User' as sdcid, dsu.sysuserid as keyid1, s.logonname as desccol FROM departmentsysuser dsu, sysuser s WHERE s.sysuserid = dsu.sysuserid AND dsu.departmentid = ? UNION SELECT 'LV_App' as sdcid, appid as keyid1, '' as desccol FROM appdepartment WHERE departmentid = ? ORDER BY sdcid, desccol, keyid1";
            case 200151:
                return "select instrumentfieldid from instrumenttypefield where instrumenttypeid=? order by usersequence";
            case 200152:
                return "select disctinct instrumentfieldid from instrumentfield order by 1";
            case 1000000:
                return "delete from sysconfig where 1=2";
            case 1000001:
                return "select * from sysconfig";
            case 1000002:
                return "SELECT localeid, localedesc FROM locale ORDER BY localeid";
            case 1000004:
                return "SELECT s.s_prodvariantid, s.prodvariantruleid, s.lasttransitiondt, s.currentstateid, r.initialstateid FROM s_prodvariant s, s_prodvariantrule r  WHERE s.s_prodvariantid = ? and r.s_prodvariantruleid = ?";
            case 1000005:
                return "SELECT sp.scheduleplanid, sp.scheduleplandesc FROM  scheduleplan sp, study_scheduleplan st WHERE st.studyid = ? AND sp.scheduleplanid = st.scheduleplanid ORDER BY scheduleplanid";
            case 1000006:
                return "SELECT sc.scheduleconditionid, sc.conditionlabel FROM  schedulecondition sc, study_scheduleplan st  WHERE st.studyid = ? AND st.scheduleplanid = ? AND sc.scheduleplanid = st.scheduleplanid ORDER BY scheduleconditionid";
            case 1000007:
                return "SELECT s.studyid, sc.scheduleconditionid, sc.conditionlabel, sp.scheduleplanid, sp.scheduleplandesc  FROM  schedulecondition sc, scheduleplan sp, scheduleplanitem spi, s_sample s  WHERE sc.scheduleplanid = spi.scheduleplanid AND sc.scheduleconditionid = spi.scheduleconditionid  AND sp.scheduleplanid = spi.scheduleplanid AND spi.scheduleplanid = s.eventplan  AND spi.scheduleplanitemid = s.eventplanitem AND s.s_sampleid = ?";
            case 1000008:
                return "select basedonid, argid, arginto, argdesc, argtype, sdcid, reftypeid, weblookupurl, argdata from queryarg where queryid = ?";
            case 1000009:
                return "select refvalueid,refvaluedesc from refvalue where ( activeflag='Y' OR activeflag is null) AND reftypeid='ArrayZones' order by usersequence, refvalueid";
            case 1000010:
                return "select maxvolume, maxvolumeunits from arraytype at join arraylayout al on al.arraylayoutid = ? and al.arraylayoutversionid = ? and  al.arraytypeid = at.arraytypeid and al.arraytypeversionid = at.arraytypeversionid";
            case 1000011:
                return "select maxvolume, maxvolumeunits from arraytype where arraytypeid = ? and arraytypeversionid = ?";
            case 1000012:
                return "SELECT s_sampletypeid FROM s_sampletype WHERE subtypeflag = 'Y' AND parentsampletypeid = ?";
            case 1000013:
                return "SELECT s_sampletypeid FROM s_sampletype WHERE subtypeflag = 'Y'";
            case 1000014:
                return "SELECT parentsampletypeid AS parents FROM s_sampletype WHERE subtypeflag = 'Y' AND s_sampletypeid = ?";
            case 1000015:
                return "SELECT s_sampletypeid AS parents FROM s_sampletype WHERE ( subtypeflag IS NULL OR subtypeflag != 'Y' )";
            case 1000016:
                return "SELECT distinct p.productmodeflag, spd.levelid from s_product p  LEFT OUTER JOIN s_spdetail spd ON p.embeddedsamplingplanid = spd.s_samplingplanid AND p.embeddedsamplingplanversionid = spd.s_samplingplanversionid  WHERE p.s_productid = ? AND p.s_productversionid = ? AND p.formulationprojectid IS NULL";
            case 1000017:
                return "SELECT expectedbatchsize, expectedbatchsizeunits, sampletypeid, formulationprojectid, formulationiterationflag from s_product  WHERE s_productid = ? AND s_productversionid = ?";
            case 1000018:
                return "SELECT instrumentmodelid,instrumentdesc from instrument WHERE instrumentid = ?";
            case 1000019:
                return "SELECT unmanagedflag from instrumentmodel WHERE instrumentmodelid = ?";
            case 1000020:
                return "SELECT unmanagedflag from instrumenttype WHERE instrumenttypeid = ?";
            case 1000021:
                return "SELECT unmanagedflag from reagenttype WHERE reagenttypeid=? and reagenttypeversionid=coalesce(NULLIF(?,'') ,(select rt1.reagenttypeversionid from reagenttype rt1 where  rt1.reagenttypeid=reagenttype.reagenttypeid and rt1.versionstatus='C'),(select cast(max(cast(rt2.reagenttypeversionid as integer)) as varchar(40)) from reagenttype rt2 where  rt2.reagenttypeid=reagenttype.reagenttypeid and rt2.versionstatus='P'))";
            default:
                return "";
        }
    }
}
