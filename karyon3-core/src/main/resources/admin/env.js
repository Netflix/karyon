$(document).ready(function(){
    var hash = window.location.href.replace(/^.*?#/,'').split('/');
    var host = hash[1];

    $.get("http://" + host + "/env", function(envMap) {
        $.get('env.template', function (template) {
            var rows = [];
            for (var key in envMap) {
              if (envMap.hasOwnProperty(key)) {
                rows.push({ 'name' : key, 'value' : envMap[key] });
              }
            }

            var rendered = Mustache.render(template, {'rows': rows});
            $('#main-content').html(rendered);
            $('#data-table').DataTable();
        });
    });
});