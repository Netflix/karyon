<script type="text/javascript">
<#include "index.js"/>
</script>

<style>
.dynatree-container {
    border: 0!important;
}

</style>

<div id="jmxview" style="width:100%; height: 100%;">
    <div class="ui-layout-center">
        <div id="jmxbeanview"></div>
    </div>
    <div class="ui-layout-west">
        <div class="toolbarcontainer form-inline">
            <label>Filter: </label>
            <div class="input-append">
                <input type="text" class="span2" id="object-filter" size="16"/>
                <button class="btn" type="button" id="filter-clear"><i class="icon-remove"></i></button>
                <button class="btn" type="button" id="filter-expand-all"><i class="icon-plus"></i></button>
                <button class="btn" type="button" id="filter-collapse-all"><i class="icon-minus"></i></button>
            </div>
        </div>
        <div id="jmxbeantree"></div>
    </div>
</div>
