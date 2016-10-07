$(document).ready(function(){
    var flamegraph = d3.flameGraph()
        .width(1280)
        .height(340)
        .label(function(d) {
            return d.name + " " + d.value + " msec";
        });

    d3.json("${ajax_base}/guice/metrics", function(error, data) {
        if (error) {
        	console.warn(error);
        } else {
	        d3.select(".middle-center")
	            .datum(data)
	            .call(flamegraph);
        }
    });
});