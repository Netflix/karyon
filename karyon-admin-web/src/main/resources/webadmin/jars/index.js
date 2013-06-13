$(document).ready(function() {
    "use strict";
    
    $("#status-error").html("");
    
<#assign source = "/webadmin/jars">

    var oTable = $('#jars-table').dataTable( {
        "aoColumns": [
            { "sTitle": "Jar"         , "mDataProp" : "name",                  "sDefaultContent": "", "sWidth" : "30%" },
            { "sTitle": "Owner"     , "mDataProp" : "libraryOwner",          "sDefaultContent": "", "sWidth" : "10%" },
            { "sTitle": "Version"     , "mDataProp" : "implementationVersion", "sDefaultContent": "", "sWidth" : "10%" }
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
                        var items = [];
                        $.each(json.data, function(i, obj) {
                            items.push({
                                'name' : obj.jar,
                                'libraryOwner' : obj.createdBy,
                                'implementationVersion' : obj.manifestVersion
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