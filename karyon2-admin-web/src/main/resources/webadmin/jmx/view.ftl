<script type="text/javascript">
<#include "view.js"/>
</script>

<#import "/layout/main/macros.ftl" as nf/>

<style>

.ui-layout-center {
    background: #efefef;
}

.jmx-mbean h3 {
    margin: 10px;
}

.mini-layout {
    padding: 4px;
}

#operations form {
    margin-bottom: 10px;
}

#operations input {
    margin: 0px 4px;
}

#operations {
    font-size:20px;
}

</style>

<div class="jmx-mbean">
    <h3>${key}</h3>
    <h3>Attributes</h3>
    <div class="mini-layout">
        <table cellpadding="0" cellspacing="0" border="0" class="table table-striped table-condensed table-bordered" id="attributes" style="width:100%"></table>
    </div>
    
    <div id="operations-container" class="hide">
        <h3>Operations</h3>
        <div class="mini-layout" id="operations">
        </div>
      
        <h3>Results</h3>
        <div class="mini-layout" id="result">
        </div>
    </div>
</div>