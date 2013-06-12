<script type="text/javascript">
<#include "index.js"/>
</script>

<style>
.middle-center {
    padding: 4px;
}

#property-edit-template {
    width: 60em;
}

#property-edit-template textarea[name='value'] {
    width: 40em;
    height: 20em;
}

#property-edit-template input[name='name'] {
    width: 40em;
}

#sources div.name {
    width: 18em;
    float: left;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    -o-text-overflow: ellipsis;
}

#sources div.value {
    width: 18em;
    float: left;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    -o-text-overflow: ellipsis;
}

#sources .source:nth-child(even)  {
    background: #eee;
}

#sources .source:nth-child(odd)  {
}

#sources .source:first-child  {
    background: #a44;
}

#sources .source:first-child div.name {
    color: white;
}

#sources .source:first-child div.value {
    color: white;
}

.sources {
    border-left: 1px solid #DDD;
    width: 40em;
}

.source {
    padding: 4px;
}

#property-create {
    margin-bottom: 10px;
}
</style>

<#import "/layout/bootstrap/bootstrap.ftl" as bs/>

<table cellpadding="0" cellspacing="0" border="0" class="table table-striped table-condensed table-bordered fixed break-wrap" id="props-table" style="width:100%"></table>


