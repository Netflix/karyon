$(document).ready(function() {
    "use strict";

    $("#status-error").html("");

    <#assign source = "/webadmin/env">

    var oTable = $('#env-table').dataTable( {
        "aoColumns": [
            { "sTitle" : "Name", 'sWidth' : '50%'  },
            { "sTitle" : "Value", 'sWidth' : '50%' }
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
                        $.each(json.data, function(i, pair) {
                            items.push([pair.name, pair.value]);
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
        "fnInfoCallback"  : function( oSettings, iStart, iEnd, iMax, iTotal, sPre ) {
            $("#status-visible").html(iTotal);
            $("#status-total").html(iMax);
            return "";
        }
    });

    $(".bse-filter").val("");
    $("#env-table_filter").hide();

    $(".bse-filter").die("keyup").live("keyup", function() {
        $('#env-table').dataTable().fnFilter($(".bse-filter").val(), null, false, true);
    });

    $(".bse-refresh").die("click").live("click", function() {
        $('#env-table').dataTable().fnReloadAjax();
    });

});