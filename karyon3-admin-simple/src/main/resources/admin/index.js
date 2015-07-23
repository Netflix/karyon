$.urlParam = function(name){
    var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
    if (results==null){
       return null;
    }
    else{
       return results[1] || 0;
    }
}

$(document).ready(function(){
    var serverId = $.urlParam("serverId");
    var resourceId = $.urlParam("resourceId");
    
    var serverUrl = "http://" + serverId + ":8077";
    console.log(serverUrl);
    $.get(serverUrl + "/meta", function(data) {
        $(".navbar-brand").text(data.appname);
    });
        
    $.get(serverUrl + "/resources", function(data) {
        var activeResource = $.urlParam("resourceId");
        console.log("Active: " + activeResource);
        var navbar = $(".nav.navbar-nav");
        for (var index in data) {
            var res = data[index];
            var li = "<li class='" + ((res == activeResource) ? 'active' : '') + "'><a href='?serverId=" + serverId + "&resourceId="+res +"'>" + res + "</a></li>";
            navbar.append(li);
        }
        
        $(".jumbotron").append("<iframe class='pageview' frameborder='0' src='/" + activeResource + ".html'></iframe>");
        
    });
});
