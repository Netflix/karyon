$(document).ready(function () {
    "use strict";

    $("#status-error").html("");

    var source = "${ajax_base}/guice/keys";

    $.get(source, function(json) {
    	var code = JSON.stringify(json, null, 2);
    	$("#guice-keys").text(code);
    });
});