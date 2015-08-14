$(document).ready(function(){
    var hash = window.location.href.replace(/^.*?#/,'').split('/');
    var host = hash[1];
    var tab = hash[2];
        
    $.get("http://" + host + ":8077/jars", function(jars) {
        $.get('jars.template', function (template) {
            var rendered = Mustache.render(template, {'rows': jars});
            $('#main-content').html(rendered);
            $('#data-table').DataTable();
        });
    });
});
