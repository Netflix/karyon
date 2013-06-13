$(document).ready(function() {
    "use strict";

    $("div.outer-center").layout({
        defaults: {
            resizable : false,
            slideable : false,
            spacing_open: 0,
            spacing_closed: 0
        },
        north:  { paneSelector : ".middle-north"  },
        south:  { paneSelector : ".middle-south"  },
        center: { paneSelector : ".middle-center-wrapper" }
    });

    var init = $.getHashParams();
    var state = {};


    $(".nav a").click( function(e) {
        e.preventDefault();
        $.modifyHashParams({"view": $(this).attr("id").substring("submenu-".length)});
    });

    $("#machine-readable").click(function(e) {
        e.preventDefault();
        window.open("/webadmin/" + $.getHashParams()["view"]);
    });


    /**
     * Load page elements based on the location hash
     */
    function applyHashChange(prev, curr) {
        if (!curr.view)
            curr.view = "env";
        if (curr.view == state.view && curr.app == state.app && curr.inst == state.inst)
            return;

        $("#bc-app").text(curr.app ? curr.app : "");
        $("#bc-instance").text(curr.inst ? curr.inst : "");
        $("#bc-view").text($("#submenu-{0} span".format(curr.view)).text());

        populateContents(curr.view, curr.inst);
    }

    /**
     * Load the view for the currently select instance
     */
    function populateContents(view, inst) {
        state.view = view;
        if (!state.view) {
            state.view = "env";
        }
        state.inst = inst;
        var $view = $(".middle-center");
        if (view) {
            var url = "/admin/{0}".format(state.view);
            $(".nav li").removeClass("active");
            $("#submenu-{0}".format(state.view)).parent().addClass("active");

            $view.ajaxBusy();
            $view.html("");
            $view.load(url, function() {
                $view.ajaxBusy('remove');
            });
        } else {
        $view.html("Select an instance or valid view");
        }
    }

    $.listenHashChange(applyHashChange);
});