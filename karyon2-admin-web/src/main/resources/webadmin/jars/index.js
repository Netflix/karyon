$(document).ready(function() {
    "use strict";
    
    $("#status-error").html("");

    var source = "${ajax_base}/jars";

    var oTable = $('#jars-table').dataTable( {
        "aoColumns": [
            { "sTitle": "Jar"          , "mDataProp" : "name",                  "sDefaultContent": "", "sWidth" : "30%" },
            { "sTitle": "Created By"   , "mDataProp" : "createdBy",             "sDefaultContent": "", "sWidth" : "10%" },
            { "sTitle": "Build date"   , "mDataProp" : "buildDate",             "sDefaultContent": "", "sWidth" : "6%" },
            { "sTitle": "Build number" , "mDataProp" : "buildNumber",           "sDefaultContent": "", "sWidth" : "5%" },
            { "sTitle": "Built by"     , "mDataProp" : "builtBy",               "sDefaultContent": "", "sWidth" : "5%" }
        ],
        "sAjaxSource": source,
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
                        var items = [];
                        $.each(json.data, function(i, obj) {
                            items.push({
                                'id' : obj.id,
                                'name' : obj.jar,
                                'createdBy' : obj.createdBy,
                                'buildDate' : obj.buildDate,
                                'buildNumber' : obj.buildNumber,
                                'builtBy' : obj.builtBy
                            });
                        });

                        fnCallback({"aaData":items});
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
        "bFilter"         : true,
        "fnInfoCallback": function( oSettings, iStart, iEnd, iMax, iTotal, sPre ) {
            $("#status-visible").html(iTotal);
            $("#status-total").html(iMax);
            return "";
        }
    });

    $(".bse-filter").val("");
    $("#jars-table_filter").hide();
    
    $(".bse-filter").die("keyup").live("keyup", function() {
        $('#jars-table').dataTable().fnFilter($(".bse-filter").val(), null, false, true);
    });
        
    $(".bse-refresh").die("click").live("click", function() {
        $('#jars-table').dataTable().fnReloadAjax();
    });    
    
});