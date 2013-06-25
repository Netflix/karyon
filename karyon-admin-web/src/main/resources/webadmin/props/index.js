$(document).ready(function() {
    "use strict";
    
    $("#status-error").html("");
    
    <#assign list_source = "/webadmin/props">

    var oTable = $('#props-table').dataTable( {
        "aoColumns": [
            { "sTitle": "Name", "sWidth" : "30%", "sDefaultContent" : "-" },
            { "sTitle": "Value", "sWidth" : "70%", "sDefaultContent" : "-"}
        ],
        "sAjaxSource" : "${list_source}",
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
                        $.each(json.data, function(index, obj) {
                            items.push([obj.name, obj.value]);
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
        "bDestroy"        : true,
        "bFilter"         : true,
        "bAutoWidth"      : false,
        "fnInfoCallback": function( oSettings, iStart, iEnd, iMax, iTotal, sPre ) {
            $("#status-visible").html(iTotal);
            $("#status-total").html(iMax);
            return "";
        }
    });
    
    $(".bse-filter").val("");
    $("#props-table_filter").hide();
    
    $(".bse-filter").die("keyup").live("keyup", function() {
        $('#props-table').dataTable().fnFilter($(".bse-filter").val(), null, false, true);
    });
        
    $(".bse-refresh").die("click").live("click", function() {
        $('#props-table').dataTable().fnReloadAjax();
    });    
});