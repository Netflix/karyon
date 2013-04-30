$(document).ready(function() {
    "use strict";
    
    $('#jmxview').layout({
        center: {
            resizable:              false
        },
        west: {
            size:                    350,
            minSize:                 350
        }
    });
    
    $.ui.dynatree.nodedatadefaults["icon"] = false;
    
    var oTree = $("#jmxbeantree").dynatree( {
        onActivate: function(node) {
            if (node.data.noLink == false) {
                $("#jmxbeanview").load("/admin/jmx/mbean?" + $.param({id: "${id}", "key": node.data.key}));
            }
        },
        selectMode: 1,
        initAjax: {
            url:      "/webadmin/jmx",
            cache:    false,
            dataType: "json",
            data:     { key: "root" }
        },
        persist: false,
        onPostInit: function(isReloading, isError) {
            this.reactivate();
            
            expandAll();
            collapseAll();
        }
    });
    
    // Traverse the entire tree and hide nodes that do not match the query
    $("#object-filter").keyup(function() {
        filterNode(oTree.dynatree("getRoot"), $(this).val().toLowerCase());
    });
    
    $("#filter-clear").click(function() {
        $("#object-filter").val("");
        expandAll();
    });
    
    $("#filter-expand-all").click(function() {
        $("#object-filter").val("");
        expandAll();
    });
    
    $("#filter-collapse-all").click(function() {
        $("#object-filter").val("");
        collapseAll();
    });
    
    function expandAll() {
        oTree.dynatree("getRoot").visit(function(node){
            node.expand(true);
            $(node.li).show(); 
        });
    }
    
    function collapseAll() {
        oTree.dynatree("getRoot").visit(function(node){
            node.expand(false);
        });
    }
    
    function filterNode(node, text) {
        var show = false;
        if (node.data.key != null)
            show = !text || text.length == 0 || node.data.key.toLowerCase().indexOf(text) != -1;
        
        if (node.hasChildren()) {
            $.each(node.getChildren(), function(index, child) {
                show = filterNode(child, text) || show;
            });
            
            if (show != node.isExpanded) {
                try {
                    node.expand(show);
                }
                catch (err) {
                    console.log("Failed to process : " + node.data.key + " " + err);
                }
            };
            if (show) {
                $(node.li).show(); 
            }
            else {
                $(node.li).hide(); 
            }
        }
        
        return show;
    }
});
