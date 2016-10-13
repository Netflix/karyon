<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.5/d3.min.js" charset="utf-8"></script>
<link href="/baseserver/res/css/d3.flameGraph.css" rel="stylesheet" type="text/css" ></link>
<script type="text/javascript" language="javascript" src="/baseserver/res/js/d3.flameGraph.js" charset="utf-8"></script>
<script type="text/javascript" language="javascript" src="/baseserver/res/js/d3.tip.js" charset="utf-8"></script>

<script type="text/javascript">
<#include "index.js"/>
</script>

<style>
.link {
  fill: none;
  stroke: #666;
  stroke-width: 1.5px;
}
.link.binding {
    fill: none;
    stroke-width: 1.5px;
    stroke-dasharray: 5,5;
}
.node circle {
  fill: #ccc;
  stroke: #ccc;
  stroke-width: 1.5px;
}
.node.Interface circle {
}
.node.Implementation circle {
  fill: #00f;
  stroke: #00f;
}
.node.Instance circle {
  fill: #f00;
  stroke: #f00;
}
text {
  font: 10px sans-serif;
  pointer-events: none;
}
</style>
