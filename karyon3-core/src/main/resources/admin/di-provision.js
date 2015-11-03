$(document).ready(function(){
    var hash = window.location.href.replace(/^.*?#/,'').split('/');
    var host = hash[1];
    var tab = hash[2];
        
    var flamegraph = d3.flameGraph()
        .width(1280)
        .height(340)
        .label(function(d) {
            return d.name + " " + d.value + " msec";
        });

    d3.json("http://" + host + "/di-provision", function(error, data) {
        if (error) 
            return console.warn(error);
        d3.select("#main-content-body")
            .datum(data)
            .call(flamegraph);
    });
});
