(function($) {

    var hash = window.location.href.replace(/^.*?#/,'').split('/');
    var host = hash[0];
    var tab = hash[1];

    var lifecycleStateLabel = {
       'Starting' : 'label-primary',
       'Started'  : 'label-success',
       'Running'  : 'label-success',
       'Stopping' : 'label-default',
       'Stopped'  : 'label-default',
       'Failed'   : 'label-danger',
    };

    var healthStateLabel = {
        'Starting'      : 'label-primary',
        'Healthy'       : 'label-success',
        'Unhealthy'     : 'label-danger',
        'OutOfService'  : 'label-warning',
    };

    function updateSidebar(sammy, context, h, t) {
        var changed = false;
        if (h !== host) {
            context.log('host changed from ' + host + ' to ' + h);
            host = h;
            changed = true;
        }

        if (t !== tab) {
            context.log('tab changed from ' + tab + ' to ' + t);
            tab = t;
            changed = true;
        }

        if (changed) {
            sammy.load('http://' + host + ':8077/resources')
                 .then(function (json) {
                     var items = JSON.parse(json);
                     items.push('appinfo');
                     items.sort();
                     var tabs = items.map(function (name) {
                     return {
                        'name':  name,
                        'class': (name === tab) ? 'active' : 'non-active',
                        'href':  '#/' + host + '/' + name
                     };
                 });
                 $.get('sidebar.template', function (template) {
                     var rendered = Mustache.render(template, {'tabs': tabs});
                     $('#main-sidebar').html(rendered);
                 });
            });
        }
    }
    
    function updateHeader(sammy, context, h, t) {
        $.get('http://' + host + ':8077/guice-lifecycle', function (lifecycle) { 
             $('#header-lifecycle-state').html(lifecycle.state);
             $('#header-lifecycle-state').removeClass().addClass("label " + lifecycleStateLabel[lifecycle.state]);
         });
             
        $.get('http://' + host + ':8077/health', function (health) { 
             $('#header-health-state').html(health.state);
             $('#header-health-state').removeClass().addClass("label " + healthStateLabel[health.state]);
         });
         
        $.get('http://' + host + ':8077/meta', function (meta) { 
            $('#header-appname').html(meta.appId);
            $('#header-region').html(meta.region);
            $('#header-serverId').html(meta.serverId);
         });
    }

    function toList(obj) {
        var items = [];
        for (k in obj) {
            items.push({'key': k, 'value': obj[k]});
        }
        return items;
    }

    function fixNames(obj) {
        var props = {};
        for (k in obj) {
            props[k.replace(/[.]/g, "_")] = obj[k];
        }
        return props;
    }

    function toPath(tab) {
        return (tab === "appinfo") ? 'props' : tab;
    }

    var app = $.sammy('#main-content', function() {
    
        this.post('#/', function(context) {
            window.location = '#/' + this.params['host'];
        });
    
        this.get('#/:host', function(context) {
            window.location = '#/' + this.params['host'] + '/appinfo';
        });
    
        this.get('#/:host/:tab', function(context) {
            var params = this.params;
            updateSidebar(this, context, params['host'], params['tab']);
            updateHeader(this, context, params['host'], params['tab']);
            this.load(
                   params['tab'] + ".html", 
                   {
                       error : function(response) {
                           $.get('http://' + params['host'] + ':8077/' + toPath(params['tab']), function(json) { 
                               var code = JSON.stringify(json, null, 2);
                               $.get('code.template', function (template) {
                                   var rendered = Mustache.render(template, {'lang': 'json', 'code': code});
                                   $('#main-content').html(rendered);
                                   $('#main-content').each(function (i, block) {
                                   hljs.highlightBlock(block);
                                   });
                               });
                           })
                           .fail(function() {
                               $('#main-content').html("Error loading page " + params['tab']);
                           });
                       }
                   }
                )
                .then(function(html) {
                   $('#main-content').html(html);
                });
        });
    
        this.get('#/:host/:tab/:query', function(context) {
            var params = this.params;
            updateSidebar(this, context, params['host'], params['tab']);
            console.log('query [' + params['query'] + ']');
            this.load('http://' + params['host'] + ':8077/' + params['tab'])
                .then(function (json) {
                    var obj = JSON.parse(json);
                    showCode(obj);
                });
            });
        });
      
        $(function() {
            app.run('#/');
        });  
    }
)(jQuery);