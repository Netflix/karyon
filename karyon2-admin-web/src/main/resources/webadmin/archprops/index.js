$(document).ready(function () {
    "use strict";

    $("#status-error").html("");
    var source = "${ajax_base}/archprops";

    $('#props-table').dataTable({
        "aoColumns": [
            { "sTitle": "Name", "sWidth" : "30%", "sDefaultContent" : "-" },
            { "sTitle": "Value", "sWidth" : "70%", "sDefaultContent" : "-"}
        ],
        "sAjaxSource"    : source,
        "fnServerData"   : function (sSource, aoData, fnCallback) {
            $.getJSON(sSource, aoData, function (json) {
                $("#status-lastupdate").html(new Date().format());
                if (json.iTotalDisplayRecords) {
                    $("#status-visible").html(json.iTotalDisplayRecords);
                }
                if (json.iTotalRecords) {
                    $("#status-total").html(json.iTotalRecords);
                }
                fnCallback(json);
            });
        },
        "bLengthChange"  : false,
        "bServerSide"    : true,
        "bProcessing"    : true,
        "sPaginationType": "bootstrap",
        "iDisplayLength" : 50,
        "bDestroy"       : true,
        "bFilter"        : true,
        "bAutoWidth"     : false,
        "fnInfoCallback" : function (oSettings, iStart, iEnd, iMax, iTotal, sPre) {
            $("#status-visible").html(iTotal);
            $("#status-total").html(iMax);
            return "";
        }
    });

    $(".bse-filter").val("");
    $("#props-table_filter").hide();

    $(".bse-filter").die("keyup").live("keyup", function () {
        $('#props-table').dataTable().fnFilter($(".bse-filter").val(), null, false, true);
    });

    $(".bse-refresh").die("click").live("click", function () {
        $('#props-table').dataTable().fnReloadAjax();
    });
});