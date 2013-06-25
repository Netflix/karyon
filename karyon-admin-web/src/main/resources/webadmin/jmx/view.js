$(document).ready(function() {
    "use strict";
    
    var counter = 0;
    
    var aoColumns;
    var multikey = ("${key}".indexOf("*") != -1);
    if (multikey) {
        aoColumns = [
                      { "sTitle": "Key",     "sWidth" : "40%" },
                      { "sTitle": "Name",    "sWidth" : "30%" },
                      { "sTitle": "Value",   "sWidth" : "30%" }
                  ];
    }
    else {
        aoColumns = [
                     { "sTitle": "Name",    "sWidth" : "50%" },
                     { "sTitle": "Value",   "sWidth" : "50%" }
                 ];
    }
    
    var bOperationsInitialized = false;
    
    var oTable = $('#attributes').dataTable( {
        "aoColumns"     : aoColumns,
        "bPaginate"     : false,
        "bLengthChange" : false,
        "bFilter"       : true,
        "bAutoWidth"    : false,
        "bStateSave"    : true,
        "bAjaxRefresh"  : true,
        "sDom"          : "H<'row'<'span3'l><f>r>t<'row'<'span6'i><p>>",
        "sAjaxSource"   : "/webadmin/jmx/{0}?jsonp=?".format("${key}"),
        "fnPostProcessServerData" : function(json, callback) {
            var aaData = [];
            if (json.attributes) {
                // This result is showing attributes for multiple JMX objects
                if (multikey) {
                    // Generate list of key name properties to remove in the case of multiple keys
                    var propsToRemove = [];
                    $.each(json.property, function(keyToRemove, valueToRemove) {
                        propsToRemove.push(keyToRemove);
                    });
                      
                    $.each(json.attributes, function(key, attrs) {
                        var newkey = JSON.stringify(Object.removeAll(keyToMap(key), propsToRemove));
                        $.each(attrs, function(name, value) {
                            aaData.push([newkey, name, value]);
                        });
                    });
                      
                }
                else {
                    $.each(json.attributes, function(key) {
                        aaData.push([key, json.attributes[key]]);
                    });
                }
            }
                
            callback({aaData : aaData});
            
            // Only show operations once (i.e. Don't show this on refresh.
            if (!bOperationsInitialized && json.operations && json.operations.length > 0) {
                $("#operations-container").show();
                bOperationsInitialized = true;
                $.each(json.operations, function(indx, obj) {
                    var html;
                    html = "<form id='jmxform{0}' action='/webadmin/jmx/${key}/{1}?jsonp=?' method='post' class='form-inline'>".format(indx, obj.name);
                    html += "<input type='submit' class='btn' value='{0}'/>".format(obj.name); 
                    html += "<input type='hidden' class='span2' name='op' value='{0}'/> ( ".format(obj.name); 
                    $.each(obj.params, function(indxParam, param) {
                        html += "{0}<input type='text' class='span2' name='{1}' placeholder='{2}'/>".format(indxParam != 0 ? "," : "", param.name, param.type);    
                    });
                    html += " ) </form>";
              
                    $("#operations").append(html);
                  
                    $('#jmxform' + indx).ajaxForm({
                        dataType: 'json',
                        success: function(json) { 
                            if (json.response) {
                                $("#result").html(json.response);
                            }
                            else {
                                $("#result").html("no response body");
                            }
                            return false;
                        }
                    }); 
                });
            }
        }
    });
    
    function keyToMap(str) {
        var map = {};
        var colon = str.indexOf(":");
        if (colon != -1) {
            var csv = str.substr(colon+1).splitCSV(",");
            $.each(csv, function(index, pair) {
                var equals = pair.indexOf("=");
                if (equals != -1) {
                    map[pair.substr(0, equals)] = pair.substr(equals+1);
                }
            });
        }
        return map;
    };
    
});