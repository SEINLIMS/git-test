package com.labvantage.sapphire.modules.connectionlog;

import com.labvantage.sapphire.DateTimeUtil;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class ConnectionLogOptionsAjaxGetter extends BaseAjaxRequest {

    public static final String DELIMITER = ";";
    private boolean isMss = false;

    public ConnectionLogOptionsAjaxGetter() {
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response,
        ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response,
            "top.wizard_iframe.reportOptions_AjaxCallback");
        String option = ajaxResponse.getRequestParameter("option");
        String sysusers = ajaxResponse.getRequestParameter("sysusers");
        String fromdate = ajaxResponse.getRequestParameter("fromdate");
        String todate = ajaxResponse.getRequestParameter("todate");
        String client = ajaxResponse.getRequestParameter("clientinfo");
        String connection = ajaxResponse.getRequestParameter("connection");
        String connectiontype = ajaxResponse.getRequestParameter("connectiontype");
        String sortby = ajaxResponse.getRequestParameter("sortby");
        this.isMss = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid())
            .getDbms().equals("MSS");
        if (fromdate == null) {
            fromdate = "";
        }

        if (todate == null) {
            todate = "";
        }

        DateTimeUtil dtu;
        Calendar date;
        SimpleDateFormat sdf;
        if (fromdate != null && fromdate.length() > 0) {
            dtu = new DateTimeUtil(this.getConnectionProcessor().getConnectionInfo(connection));
            date = dtu.getCalendar(fromdate);
            sdf = new SimpleDateFormat("MM/dd/yy hh:mm:ss aa");
            fromdate = sdf.format(date.getTime());
        }

        if (todate != null && todate.length() > 0) {
            dtu = new DateTimeUtil(this.getConnectionProcessor().getConnectionInfo(connection));
            date = dtu.getCalendar(todate);
            sdf = new SimpleDateFormat("MM/dd/yy hh:mm:ss aa");
            todate = sdf.format(date.getTime());
        }

        String whereclause = "";
        if (option != null && option.length() > 0) {
            if (option.equals("active")) {
                whereclause = whereclause
                    + " connectioncleareddt is null AND connectionlog.connectionid IN ( SELECT connectionid FROM connection )";
            } else if (option.equals("recent")) {
            }
        }

        if (sysusers != null && sysusers.length() > 0) {
            if (whereclause.length() > 0) {
                whereclause = whereclause + " AND ";
            }

            whereclause =
                whereclause + " connectionlog.sysuserid = '" + SafeSQL.encodeForSQL(sysusers,
                    !this.isMss) + "'";
        }

        if (connection != null && connection.length() > 0) {
            if (whereclause.length() > 0) {
                whereclause = whereclause + " AND ";
            }

            whereclause =
                whereclause + " connectionlog.connectionid like '%" + SafeSQL.encodeForSQL(
                    connection, !this.isMss) + "%'";
        }

        if (connectiontype != null && connectiontype.length() > 0) {
            if (whereclause.length() > 0) {
                whereclause = whereclause + " AND ";
            }

            whereclause =
                whereclause + " connectionlog.connectiontypeflag = '" + SafeSQL.encodeForSQL(
                    connectiontype, !this.isMss) + "'";
        }

        if (client != null && client.length() > 0) {
            if (whereclause.length() > 0) {
                whereclause = whereclause + " AND ";
            }

            whereclause = whereclause + " ( clientipaddress  like '%" + SafeSQL.encodeForSQL(client,
                !this.isMss) + "%' or clienthostname like '%" + SafeSQL.encodeForSQL(client,
                !this.isMss) + "%' ) ";
        }

        if (option.equals("recent")) {
            if (!this.isMss) {
                whereclause = whereclause + " connectioncreateddt > (sysdate-7) ";
            } else {
                whereclause = whereclause + " connectioncreateddt > (getdate()-7) ";
            }
        }

        if (fromdate.length() > 0 && todate.length() > 0) {
            if (whereclause.length() > 0) {
                whereclause = whereclause + " AND ";
            }

            if (!this.isMss) {
                whereclause = whereclause + " TRUNC(ConnectionCreatedDt) between TRUNC(TO_DATE ('"
                    + SafeSQL.encodeForSQL(fromdate, !this.isMss)
                    + "', 'mm/dd/yy hh:mi:ss am'))  and TRUNC(TO_DATE ('" + SafeSQL.encodeForSQL(
                    todate, !this.isMss) + "', 'mm/dd/yy hh:mi:ss am') ) ";
            } else {
                whereclause = whereclause
                    + " connectioncreateddt between CONVERT(CHAR(10), CONVERT(DATETIME,'" + fromdate
                    + "'), 102) and CONVERT(CHAR(10), CONVERT(DATETIME,'" + SafeSQL.encodeForSQL(
                    todate, !this.isMss) + "')+1, 102)";
            }
        } else if (fromdate.length() > 0) {
            if (whereclause.length() > 0) {
                whereclause = whereclause + " AND ";
            }

            if (!this.isMss) {
                whereclause = whereclause + " TRUNC(ConnectionCreatedDt) > TRUNC(TO_DATE ('"
                    + SafeSQL.encodeForSQL(fromdate, !this.isMss) + "', 'mm/dd/yy hh:mi:ss am'))";
            } else {
                whereclause =
                    whereclause + " ConnectionCreatedDt > CONVERT(CHAR(10), CONVERT(DATETIME,'"
                        + SafeSQL.encodeForSQL(fromdate, !this.isMss) + "'), 102)";
            }
        } else if (todate.length() > 0) {
            if (whereclause.length() > 0) {
                whereclause = whereclause + " AND ";
            }

            if (!this.isMss) {
                whereclause = whereclause + " TRUNC(ConnectionCreatedDt) < TRUNC(TO_DATE ('"
                    + SafeSQL.encodeForSQL(todate, !this.isMss) + "', 'mm/dd/yy hh:mi:ss am') ) ";
            } else {
                whereclause =
                    whereclause + " ConnectionCreatedDt < CONVERT(CHAR(10), CONVERT(DATETIME,'"
                        + SafeSQL.encodeForSQL(todate, !this.isMss) + "'), 102)";
            }
        }

        ajaxResponse.addCallbackArgument("tablehtml",
            this.getHtml(option, whereclause, sortby, fromdate, todate, client, sysusers,
                connectiontype, connection));
        ajaxResponse.print();
    }

    private String getHtml(String option, String whereclause, String sortby, String fromdate,
        String todate, String client, String sysuser, String connectiontype, String connectionid) {
        if (whereclause.length() > 0) {
            whereclause = " WHERE " + whereclause;
        }

        String totalsql = "SELECT count(*) itemcount FROM connectionlog " + whereclause;
        int totalitems = this.getQueryProcessor().getSqlDataSet(totalsql).getInt(0, "itemcount", 0);
        StringBuilder buffer = new StringBuilder();
        String outerwhere = "";
        if (!this.isMss) {
            outerwhere = " WHERE rownum < 1001";
        }

        if (sortby == null || sortby.length() == 0) {
            sortby = "ConnectionCreatedDt desc";
        }

        String sql;
        if (this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getDbms()
            .equals("MSS")) {
            sql =
                "SELECT TOP 1000 * FROM connectionlog LEFT OUTER JOIN connection ON connectionlog.connectionid = connection.connectionid "
                    + whereclause + " ORDER BY " + sortby;
        } else {
            sql =
                "SELECT * FROM ( SELECT * FROM connectionlog LEFT OUTER JOIN connection ON connectionlog.connectionid = connection.connectionid "
                    + whereclause + " ORDER BY " + sortby + " ) " + outerwhere;
        }

        DataSet oldds = this.getQueryProcessor().getSqlDataSet(sql);
        if (oldds != null) {
            if (option.equals("active")) {
                buffer.append(totalitems == oldds.getRowCount() ? "<font color=\"black\">"
                    + this.getTranslationProcessor().translate("Total active connections found:")
                    + totalitems + "</font>"
                    : "<font color=\"red\">" + this.getTranslationProcessor()
                        .translate("Displaying top") + " " + oldds.getRowCount() + " "
                        + this.getTranslationProcessor().translate(" active connections of total:")
                        + " " + totalitems + "</font>");
            } else if (option.equals("recent")) {
                buffer.append(totalitems == oldds.getRowCount() ? "<font color=\"black\">"
                    + this.getTranslationProcessor()
                    .translate("Total connections made during the last 7 days:") + " " + totalitems
                    + "</font>" : "<font color=\"red\">" + this.getTranslationProcessor()
                    .translate("Displaying top") + " " + oldds.getRowCount() + " "
                    + this.getTranslationProcessor()
                    .translate("connections from the last 7 days of total:") + totalitems
                    + "</font>");
            } else {
                buffer.append(
                    "<table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\"><tr><td width=\"120\" valign=\"top\" style=\"border-right:double gray\">");
                buffer.append(this.getSearchHtml(fromdate, todate, client, sysuser, connectiontype,
                    connectionid, sortby));
                buffer.append("</td><td valign=\"top\" style=\"padding:5px\">");
                buffer.append(totalitems == oldds.getRowCount() ? "<b><font color=\"black\">"
                    + this.getTranslationProcessor().translate("Total connections found:") + " "
                    + totalitems + "</font></b>"
                    : "<b><font color=\"red\">" + this.getTranslationProcessor()
                        .translate("Displaying top") + " " + oldds.getRowCount() + " "
                        + this.getTranslationProcessor().translate("connections of total:") + " "
                        + totalitems + "</font></b>");
            }

            buffer.append(
                "<table id=\"list_list\" class=\"list_table\" cellpadding=\"2\" cellspacing=\"0\"> ");
            buffer.append("<thead class=\"listtablehead\">");
            buffer.append("<tr class=\"listtablehead\">");
            buffer.append(
                "<th align=\"left\" class=\"list_tableheadcell\">&nbsp;<input type='checkbox' onchange='triggerSelectAll(\""
                    + option + "\");' /></th>");
            buffer.append("<th  class=\"list_tableheadcell\"></th>");
            buffer.append(
                "<th  align=\"left\" class=\"list_tableheadcell\">" + this.getTranslationProcessor()
                    .translate("Connectionid") + "</th>");
            buffer.append("<th   align=\"left\" class=\"list_tableheadcell\">"
                + this.getTranslationProcessor().translate("User") + "</th>");
            buffer.append("<th   align=\"left\" class=\"list_tableheadcell\">"
                + this.getTranslationProcessor().translate("Job Type") + "</th>");
            buffer.append("<th   align=\"left\" class=\"list_tableheadcell\">"
                + this.getTranslationProcessor().translate("Login") + "</th>");
            if (!option.equals("active")) {
                buffer.append("<th   align=\"left\" class=\"list_tableheadcell\">"
                    + this.getTranslationProcessor().translate("Logout") + "</th>");
            } else {
                buffer.append("<th   align=\"left\" class=\"list_tableheadcell\">"
                    + this.getTranslationProcessor().translate("Last Accessed Date") + "</th>");
            }

            buffer.append("<th  align=\"left\"  class=\"list_tableheadcell\">"
                + this.getTranslationProcessor().translate("Server") + "</th>");
            buffer.append("<th   align=\"left\" class=\"list_tableheadcell\">"
                + this.getTranslationProcessor().translate("Client") + "</th>");
            buffer.append("<th   align=\"left\" class=\"list_tableheadcell\">"
                + this.getTranslationProcessor().translate("Device ID") + "</th>");
            buffer.append("<th   align=\"left\" class=\"list_tableheadcell\">"
                + this.getTranslationProcessor().translate("Browser") + "</th>");
            buffer.append("</tr>");
            buffer.append("</thead>");
            DataSet ds = new DataSet();

            int i;
            for (i = 0; i < oldds.getRowCount(); ++i) {
                if (this.getConnectionId().equals(oldds.getString(i, "connectionid"))) {
                    ds.copyRow(oldds, i, 1);
                    break;
                }
            }

            for (i = 0; i < oldds.getRowCount(); ++i) {
                if (!this.getConnectionId().equals(oldds.getString(i, "connectionid"))) {
                    ds.copyRow(oldds, i, 1);
                }
            }

            for (i = 0; i < ds.getRowCount(); ++i) {
                String style = "font-style:normal;font-weight:bold;color:blue";
                String color = "blue";
                if (this.getConnectionId().equals(ds.getString(i, "connectionid"))) {
                    style = "font-style:normal;font-weight:bold;";
                    color = "green";
                }

                if (i % 2 == 0) {
                    buffer.append("<tr name=\"list_tablerow\" class=\"list_tableroweven\"> ");
                } else {
                    buffer.append("<tr name=\"list_tablerow\" class=\"list_tablerowodd\"> ");
                }

                if (ds.getValue(i, "ConnectionClearedDt") != null
                    && ds.getValue(i, "ConnectionClearedDt").length() != 0) {
                    style = "font-style:normal;";
                    buffer.append("<td class=\"list_tablebodycell\"><input name=\"selector\" id=\""
                        + ds.getString(i, "connectionid") + "\" type=\"checkbox\" disabled></td>");
                    color = "black";
                } else if (color.equals("green")) {
                    buffer.append("<td class=\"list_tablebodycell\"><input name=\"selector\" id=\""
                        + ds.getString(i, "connectionid") + "\" type=\"checkbox\" disabled></td>");
                } else {
                    buffer.append("<td class=\"list_tablebodycell\"><input name=\"selector\" id=\""
                        + ds.getString(i, "connectionid") + "\" type=\"checkbox\" class='" + option
                        + "'></td>");
                }

                buffer.append("<TD class=\"list_tablebodycell\" style=" + style + ">"
                    + this.getConnectionTypeIcon(ds.getString(i, "connectiontypeflag"))
                    + "</font></td>");
                buffer.append(
                    "<TD class=\"list_tablebodycell\" style=" + style + "><font color=\"" + color
                        + "\">" + ds.getString(i, "connectionid", "&nbsp;") + "</font></td>");
                buffer.append(
                    "<TD class=\"list_tablebodycell\" style=" + style + "><font color=\"" + color
                        + "\">" + ds.getString(i, "sysuserid", "&nbsp;") + "</font></td>");
                buffer.append(
                    "<TD class=\"list_tablebodycell\" style=" + style + "><font color=\"" + color
                        + "\">" + ds.getString(i, "jobtypeid", "&nbsp;") + "</font></td>");
                buffer.append(
                    "<TD class=\"list_tablebodycell\" style=" + style + "><font color=\"" + color
                        + "\">" + ds.getValue(i, "connectioncreateddt", "&nbsp;") + "</font></td>");
                String logoffdt;
                String deletedt;
                if (!option.equals("active")) {
                    logoffdt = ds.getValue(i, "connectioncleareddt", "");
                    if (logoffdt.length() == 0) {
                        deletedt = ds.getValue(i, "deletedt", "");
                        if (deletedt.length() > 0) {
                            logoffdt = "( Pending )";
                        } else {
                            logoffdt = "&nbsp;";
                        }
                    }

                    buffer.append(
                        "<TD class=\"list_tablebodycell\" style=" + style + "><font color=\""
                            + color + "\">" + logoffdt + "</font></td>");
                } else {
                    logoffdt = ds.getValue(i, "lastaccesseddt", "");
                    buffer.append(
                        "<TD class=\"list_tablebodycell\" style=" + style + "><font color=\""
                            + color + "\">" + logoffdt + "</font></td>");
                }

                logoffdt = ds.getString(i, "serverhostname", "");
                deletedt = ds.getString(i, "serveripaddress", "");
                if (deletedt.length() > 0 && !deletedt.equals(logoffdt)) {
                    logoffdt = logoffdt + "( " + deletedt + " )";
                }

                buffer.append(
                    "<TD class=\"list_tablebodycell\" style=" + style + "><font color=\"" + color
                        + "\">" + logoffdt + "</font></td>");
                String clienthost = ds.getString(i, "clienthostname", "");
                String clientip = ds.getString(i, "clientipaddress", "");
                if (clientip.length() > 0 && !clientip.equals(clienthost)) {
                    clienthost = clienthost + "( " + clientip + " )";
                }

                buffer.append(
                    "<TD class=\"list_tablebodycell\" style=" + style + "><font color=\"" + color
                        + "\">" + clienthost + "</font></td>");
                buffer.append(
                    "<TD class=\"list_tablebodycell\" style=" + style + "><font color=\"" + color
                        + "\">" + this.getTranslationProcessor()
                        .translate(ds.getString(i, "deviceid", "&nbsp;")) + "</font></td>");
                buffer.append(
                    "<TD class=\"list_tablebodycell\" style=" + style + "><font color=\"" + color
                        + "\">" + ds.getString(i, "clientbrowser", "&nbsp;") + "</font></td>");
                buffer.append("</TR>");
            }

            buffer.append("</TABLE>");
        }

        if (option.equals("active")) {
            buffer.append("</td></tr></table>");
        }

        return buffer.toString();
    }

    private StringBuffer getSearchHtml(String fromdate, String todate, String client,
        String sysuser, String connectiontype, String connectionid, String sortby) {
        StringBuffer buffer = new StringBuffer();
        String sql = "SELECT sysuserid, sysuserdesc FROM sysuser ORDER by sysuserdesc";
        DataSet sysusers = this.getQueryProcessor().getSqlDataSet(sql);
        String sysusershtml = "<SELECT id='sysusers' STYLE=\"width: 100px\"> >\n";
        if (sysuser.length() == 0) {
            sysusershtml = sysusershtml + "<OPTION value='' selected>All</OPTION>";
        } else {
            sysusershtml = sysusershtml + "<OPTION value=''>All</OPTION>";
        }

        for (int i = 0; i < sysusers.getRowCount(); ++i) {
            if (sysusers.getString(i, "sysuserid").equals(sysuser)) {
                sysusershtml = sysusershtml + "<OPTION value='" + sysusers.getString(i, "sysuserid")
                    + "' selected>" + sysusers.getString(i, "sysuserdesc",
                    sysusers.getString(i, "sysuserid")) + "</option>\n";
            } else {
                sysusershtml =
                    sysusershtml + "<OPTION value='" + sysusers.getString(i, "sysuserid") + "'>"
                        + sysusers.getString(i, "sysuserdesc", sysusers.getString(i, "sysuserid"))
                        + "</option>\n";
            }
        }

        sysusershtml = sysusershtml + "</SELECT>\n";
        StringBuffer daterangehtml = new StringBuffer();
        daterangehtml.append(
            "<tr><td>" + this.getTranslationProcessor().translate("Start Date") + "</td><tr>");
        daterangehtml.append(
            "<tr><td><input type=\"text\" name=\"fromdate\" id=\"fromdate\" value=\"" + fromdate
                + "\" size=\"12\" />\n<a href=\"/Lookup a date\" onClick=\"lookupdate( 'fromdate','','','','','O' );return false\" tabindex=\"0\"><img title=\"Lookup a date\" border=\"0\" src=\"WEB-CORE/elements/images/lookup_date.gif\"></a>");
        daterangehtml.append("</td></tr>");
        daterangehtml.append(
            "<tr><td>" + this.getTranslationProcessor().translate("End Date") + "</td></tr>");
        daterangehtml.append("<tr><td>");
        daterangehtml.append("<input type=\"text\" name=\"todate\" id=\"todate\" value=\"" + todate
            + "\" size=\"12\" />\n<a href=\"/Lookup a date\" onClick=\"lookupdate( 'todate','','','','','O' );return false\" tabindex=\"0\"><img title=\"Lookup a date\" border=\"0\" src=\"WEB-CORE/elements/images/lookup_date.gif\"></a>");
        daterangehtml.append("</td></tr>");
        buffer.append("<table cellspacing=\"0\">");
        buffer.append(daterangehtml + "<tr><td>" + this.getTranslationProcessor().translate("User")
            + "</td></tr><tr><td>" + sysusershtml + "</td></tr><tr><td>"
            + this.getTranslationProcessor().translate("Client")
            + "</td></tr><tr><td><input type='text' id=\"clientinfo\" value=\"" + client
            + "\"/></td></tr><tr><td>" + this.getTranslationProcessor().translate("Connection ID")
            + "</td></tr><tr><td><input type='text' id=\"connectionid\" value=\"" + connectionid
            + "\"/></td></tr><tr><td>" + this.getTranslationProcessor().translate("Connection Type")
            + "</td></tr><tr><td><select id=\"connectiontype\"><option value=\"B\"" + (
            connectiontype.equals("B") ? "selected" : "") + ">Browser</option>\n<option value=\"W\""
            + (connectiontype.equals("W") ? "selected" : "")
            + ">Web Service</option>\n<option value=\"R\"" + (connectiontype.equals("R")
            ? "selected" : "") + ">Remote Java</option>\n<option value=\"S\"" + (
            connectiontype.equals("S") ? "selected" : "")
            + ">External Servlet</option>\n<option value=\"\"" + (connectiontype.equals("")
            ? "selected" : "") + ">All</option>\n</select></td></tr>");
        buffer.append("<tr><td>" + this.getTranslationProcessor().translate("Sort By")
            + "</td></tr><tr><td><select id=\"sortby\">\n<option value=\"ConnectionCreatedDt desc\""
            + (sortby.equals("ConnectionCreatedDt desc") ? "selected" : "") + ">"
            + this.getTranslationProcessor().translate("Login Date (Desc)")
            + "</option>\n<option value=\"ConnectionCreatedDt asc\"" + (
            sortby.equals("ConnectionCreatedDt asc") ? "selected" : "") + ">"
            + this.getTranslationProcessor().translate("Login Date (Asc)")
            + "</option>\n<option value=\"ConnectionClearedDt desc\"" + (
            sortby.equals("ConnectionClearedDt desc") ? "selected" : "") + ">"
            + this.getTranslationProcessor().translate("Logoff Date (Desc)")
            + "</option>\n<option value=\"ConnectionClearedDt asc\"" + (
            sortby.equals("ConnectionClearedDt asc") ? "selected" : "") + ">"
            + this.getTranslationProcessor().translate("Logoff Date (Asc)")
            + "</option>\n<option value=\"connectionlog.sysuserid asc\"" + (
            sortby.equals("connectionlog.sysuserid asc") ? "selected" : "") + ">"
            + this.getTranslationProcessor().translate("User")
            + "</option>\n<option value=\"jobtypeid asc\"" + (sortby.equals("jobtypeid asc")
            ? "selected" : "") + ">Job Type</option>\n<option value=\"clientbrowser asc\"" + (
            sortby.equals("clientbrowser asc") ? "selected" : "")
            + ">Browser</option>\n</select></td></tr>");
        buffer.append("<tr><td><br><input type=\"button\" id=\"refreshbutton\" value=\""
            + this.getTranslationProcessor().translate("Search")
            + "\" onclick=\"updateOption( 'query' )\"/></td></tr>");
        buffer.append("</table>");
        return buffer;
    }

    private String getConnectionTypeIcon(String connectiontypeflag) {
        if ("R".equals(connectiontypeflag)) {
            return "<IMG src=\"WEB-CORE/images/png/RemoteClient.png\" alt=\"Remote Java\" title=\"Remote Java\"/>";
        } else if ("W".equals(connectiontypeflag)) {
            return "<IMG src=\"WEB-CORE/images/png/WebService.png\" alt=\"Web Service Client\" title=\"Web Service Client\"/>";
        } else if ("B".equals(connectiontypeflag)) {
            return "<IMG src=\"WEB-CORE/images/png/WebBrowser.png\" alt=\"Web Browser\" title=\"Web Browser\"/>";
        } else if ("S".equals(connectiontypeflag)) {
            return "<IMG src=\"WEB-CORE/images/png/Ship.png\" alt=\"Servlet\" title=\"Servlet\"/>";
        } else {
            return "P".equals(connectiontypeflag)
                ? "<IMG src=\"WEB-CORE/images/png/StorageTank.png\" alt=\"Portal\" title=\"Portal\"/>"
                : "<IMG src=\"WEB-CORE/images/png/Help.png\" alt=\"Unknown\" title=\"Unknown\"/>";
        }
    }
}
