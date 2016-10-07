$(document).ready(function(){
    $.get("${ajax_base}/guice/keys", function(graph) {
        var nodes = {};
        var links = [];
        
        graph.forEach(function(from) {
           nodes[from.name] = from;
           from.dependencies.forEach(function(to) {
               links.push({source: from.name, target: to, type: "dependency"});
           });
           from.boundTo.forEach(function(to) {
               links.push({source: from.name, target: to, type: "binding"});
           });
        });
        
        // Compute the distinct nodes from the links.
        links.forEach(function(link) {
          link.source = nodes[link.source] || (nodes[link.source] = {name: link.source});
          link.target = nodes[link.target] || (nodes[link.target] = {name: link.target});
        });
        
        var width = 1500,
            height = 1500;
        
        var force = d3.layout.force()
            .nodes(d3.values(nodes))
            .links(links)
            .size([width, height])
            .linkDistance(60)
            .charge(-300)
            .on("tick", tick)
            .start();
        
        var svg = d3.select(".middle-center").append("svg")
            .attr("width", width)
            .attr("height", height);
        
        // build the arrow.
        svg.append("svg:defs").selectAll("marker")
            .data(["end"])      // Different link/path types can be defined here
          .enter().append("svg:marker")    // This section adds in the arrows
            .attr("id", String)
            .attr("viewBox", "0 -5 10 10")
            .attr("refX", 15)
            .attr("refY", -1.5)
            .attr("markerWidth", 6)
            .attr("markerHeight", 6)
            .attr("orient", "auto")
          .append("svg:path")
            .attr("d", "M0,-5L10,0L0,5");
            
        var link = svg.selectAll(".link")
            .data(force.links())
          .enter().append("line")
            .attr("class", function(d) { return "link " + d.type; })
            .attr("marker-end", "url(#end)");
        
        var node = svg.selectAll(".node")
            .data(force.nodes())
          .enter().append("g")
            .attr("class", function(d) { return "node " + d.type; })
            .on("mouseover", mouseover)
            .on("mouseout", mouseout)
            .call(force.drag);
        
        node.append("circle")
            .attr("r", 8);
        
        node.append("text")
            .attr("x", 12)
            .attr("dy", ".35em")
            .text(function(d) { return d.name; });
        
        function tick() {
          link
              .attr("x1", function(d) { return d.source.x; })
              .attr("y1", function(d) { return d.source.y; })
              .attr("x2", function(d) { return d.target.x; })
              .attr("y2", function(d) { return d.target.y; });
        
          node
              .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
        }
        
        function mouseover() {
          d3.select(this).select("circle").transition()
              .duration(750)
              .attr("r", 16);
        }
        
        function mouseout() {
          d3.select(this).select("circle").transition()
              .duration(750)
              .attr("r", 8);
        }
    });
});