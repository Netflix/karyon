<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.5/d3.min.js" charset="utf-8"></script>

<#macro body>
<script type="text/javascript">
<#include "home.js"/>
<#if homeScriptResources??>
    <#list homeScriptResources as resource>
        <#include resource />
    </#list>
</#if>
</script>

    <#import "/layout/bootstrap/macros.ftl" as nf/>

<style>

  option.instance-UP {
    color: green;
  }

  .button-bar {
    padding: 4px;
  }

  #bse-filter {
    margin-top: 4px;
    margin-right: 10px;
    margin-bottom: 0px;
    padding: 4px;
  }

  .dataTable {
    table-layout: fixed;
    margin-top: 15px;
  }

</style>
<!------------ Toolbar/Menu ------------>
<div class="middle-north">
  <div class="subnav">
    <!-- KEEP THESE ALPHABETICALLY SORTED -->
    <div>
      <ul class="nav nav-pills left">
          <#if adminPages??>
              <#list adminPages as adminPage>
                  <#if adminPage.isVisible()>
                      <#if adminPage.name??>
                        <li><a href="#" id="submenu-${adminPage.pageId}"><span>${adminPage.name}</span></a></li>
                      <#else>
                        <li><a href="#" id="submenu-${adminPage.pageId}"><span>${adminPage.pageId}</span></a></li>
                      </#if>
                  </#if>
              </#list>
          </#if>
      </ul>
    </div>
    <div class="right">
      <input type='text' id="bse-filter" class='bse-filter' placeholder="filter"/>
    </div>
    <div class="right button-bar">
      <button class='bse-refresh btn btn-primary'>Refresh</button>
      <button class='btn btn-primary' id="machine-readable">Machine Readable</button>
    </div>
  </div>
</div>

<!------------ Contents ------------>
<div class="middle-center-wrapper">
  <div class="middle-center">
  </div>
</div>

<!------------ Status ------------>
<div class="middle-south status-footer form-inline">
  <label for="status-error">Error: </label> <span id="status-error">&nbsp;</span>
  <label for="status-visible">Visible: </label> <span id="status-visible">&nbsp;</span>
  <label for="status-total">Total: </label> <span id="status-total">&nbsp;</span>
  <label for="status-lastupdate">Last updated: </label><span id="status-lastupdate">&nbsp;</span>
</div>
</#macro>
