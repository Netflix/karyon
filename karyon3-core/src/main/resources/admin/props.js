$(document).ready(function(){
    var hash = window.location.href.replace(/^.*?#/,'').split('/');
    var host = hash[1];
    var tab = hash[2];
        
    $.get("http://" + host + "/props", function(propMap) {
        $.get('props.template', function (template) {
            var props = [];
            $.each(propMap.props, function(key, value) {
               props.push({ 'name' : key, 'value' : value });
            });
            
            var rendered = Mustache.render(template, {'rows': props});
            $('#main-content').html(rendered);
            $('#data-table').DataTable();
        });
    });
});
