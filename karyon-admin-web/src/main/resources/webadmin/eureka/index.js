$(document).ready(function() {
    "use strict";

    $("#status-error").html("");

    <#assign source = "/webadmin/eureka">

    var oTable = $('#discovery-table').dataTable( {
        "aoColumns": [
            {"sTitle" : "Application", "mDataProp" : "application"},
            {"sTitle" : "Instance Id", "mDataProp" : "id"},
            {"sTitle" : "Status", "mDataProp" : "status"},
            {"sTitle" : "IP Address", "mDataProp" : "ipAddress"},
            {"sTitle" : "Hostname", "mDataProp" : "hostName"}
        ],
        "sAjaxSource": "${source}",
        "fnServerData": function ( sSource, aoData, fnCallback ) {
            $.getJSON(sSource)
                .success(function(json) {
                    $("#status-error").html("");
                    $("#status-lastupdate").html(new Date().format());
                    if (json.error) {
                        $("#status-error").html(json.error.message);
                        $("#status-error").addClass("status-error");
                    }
                    else {
                        $("#status-error").removeClass("status-error");
                        fnCallback({"aaData": json.data});
                    }
                })
                .error(function(jqXHR, textStatus, errorThrown) {
                    $("#status-error").html(textStatus + ": " + errorThrown);
                    $("#status-error").addClass("status-error");
                });
        },
        "bPaginate"       : false,
        "bLengthChange"   : false,
        "bAutoWidth"      : false,
        "bFilter"         : false,
        "fnInfoCallback": function( oSettings, iStart, iEnd, iMax, iTotal, sPre ) {
            $("#status-visible").html(iTotal);
            $("#status-total").html(iMax);
            return "";
        }
    });

    $(".bse-filter").val("");

    $(".bse-filter").die("keyup").live("keyup", function() {
        $('#discovery-table').dataTable().fnFilter($(".bse-filter").val(), null, false, true);
    });

    $(".bse-refresh").die("click").live("click", function() {
        $('#discovery-table').dataTable().fnReloadAjax();
    });
});