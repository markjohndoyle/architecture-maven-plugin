package org.mark.maven.amp.graph.jgrapht;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.ComponentAttributeProvider;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.DefaultAttribute;
import org.jgrapht.io.IntegerComponentNameProvider;
import org.mark.maven.amp.graph.DirectedComponentGraph;
import org.mark.maven.amp.model.Component;
import org.mark.maven.amp.model.ComponentInstability;

import static org.jgrapht.io.AttributeType.STRING;

public final class JGraphComponentGraph implements DirectedComponentGraph
{
    private static final DecimalFormat TWO_DP = new DecimalFormat("#.###");
    private final ComponentNameProvider<Component> vertexIDProvider = new IntegerComponentNameProvider<>();
    private static final ComponentNameProvider<DefaultEdge> NULL_EDGE_LABEL = edge -> "";

    private final Map<Component, ComponentInstability> compInsts;
    private final Graph<Component, DefaultEdge> compGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
    private Path dotPath;


    private static final class CompInstabiltyProvider implements ComponentNameProvider<Component>
    {
        private final Map<Component, ComponentInstability> compInsts;

        public CompInstabiltyProvider(Map<Component, ComponentInstability> componentInstabilities)
        {
            this.compInsts = componentInstabilities;
        }

        @Override
        public String getName(Component component)
        {
            return component.getName() + System.lineSeparator() + "I = " + TWO_DP.format(compInsts.get(component).getInstabilityValue());
        }
    }

    private static final class CompInstabiltyEdgeProvider implements ComponentNameProvider<DefaultEdge>
    {
        private final Map<Component, ComponentInstability> compInsts;
        private final Graph<Component, DefaultEdge> graph;


        public CompInstabiltyEdgeProvider(Graph<Component, DefaultEdge> graph,
                                             Map<Component, ComponentInstability> componentInstabilities)
        {
            this.compInsts = componentInstabilities;
            this.graph = graph;
        }

        @Override
        public String getName(DefaultEdge edge)
        {
            ComponentInstability sourceInstability = compInsts.get(graph.getEdgeSource(edge));
            ComponentInstability targetInstability = compInsts.get(graph.getEdgeTarget(edge));
            if(sourceInstability.getInstabilityValue() < targetInstability.getInstabilityValue())
            {
                return "⚠ dep flow";
            }
            return "✓";
        }
    }

    private static final class CompInstabilityEdgeAttrProvider implements ComponentAttributeProvider<DefaultEdge>
    {
        private final Map<Component, ComponentInstability> compInsts;
        private final Graph<Component, DefaultEdge> graph;

        public CompInstabilityEdgeAttrProvider(Graph<Component, DefaultEdge> graph,
                                             Map<Component, ComponentInstability> componentInstabilities)
        {
            this.compInsts = componentInstabilities;
            this.graph = graph;
        }

        @Override
        public Map<String, Attribute> getComponentAttributes(DefaultEdge edge)
        {
            Map<String, Attribute> edgeAttributes = new HashMap<>();
            ComponentInstability sourceInstability = compInsts.get(graph.getEdgeSource(edge));
            ComponentInstability targetInstability = compInsts.get(graph.getEdgeTarget(edge));
            if(sourceInstability.getInstabilityValue() < targetInstability.getInstabilityValue())
            {
                edgeAttributes.put("color", new DefaultAttribute<>("red", STRING));
            }
            else {
                edgeAttributes.put("color", new DefaultAttribute<>("green", STRING));
            }
            return edgeAttributes;
        }
    }


    /**
     * @param componentInstabilities
     * @throws IOException
     * @throws
     */
    public JGraphComponentGraph(Map<Component, ComponentInstability> componentInstabilities) throws IOException
    {
        this.compInsts = componentInstabilities;
        createDotFile();
        Collection<ComponentInstability> instabilities = componentInstabilities.values();
        instabilities.forEach(compInst -> {
            compGraph.addVertex(compInst.getComp());
            compInst.getOutCompDeps().forEach(targetComp -> {
                if(!compGraph.containsVertex(targetComp)) {
                    compGraph.addVertex(targetComp);
                }
                compGraph.addEdge(compInst.getComp(), targetComp);
            });
        });
    }

    public void getDependencyPaths()
    {
        Set<Component> leaves = compGraph.vertexSet().stream().
                                              filter(v -> Graphs.vertexHasSuccessors(compGraph, v)).
                                              collect(Collectors.toSet());

        for(Component vertex : compGraph.vertexSet())
        {
            // if we aren't a leaf
            if(!leaves.contains(vertex))
            {
                for(Component leaf : leaves)
                {
                    AllDirectedPaths<Component, DefaultEdge> allPaths = new AllDirectedPaths<>(compGraph);
                    List<GraphPath<Component, DefaultEdge>> depPaths = allPaths.getAllPaths(vertex, leaf, true, null);
                    if(!depPaths.isEmpty())
                    {
                    }
                }
            }
        }
    }

    public List<GraphPath<Component, DefaultEdge>>
    gatherBadFlowPaths(List<GraphPath<Component, DefaultEdge>> depPaths)
    {
        List<GraphPath<Component, DefaultEdge>> badPaths = new ArrayList<>();
        for(GraphPath<Component, DefaultEdge> path : depPaths)
        {
            List<DefaultEdge> edgesInPath = path.getEdgeList();
            for(DefaultEdge edge : edgesInPath)
            {
                ComponentInstability sourceInstability = compInsts.get(compGraph.getEdgeSource(edge));
                ComponentInstability targetInstability = compInsts.get(compGraph.getEdgeTarget(edge));
                if(sourceInstability.getInstabilityValue() < targetInstability.getInstabilityValue())
                {
                    badPaths.add(path);
                }
            }
        }
        return badPaths;
    }

    @Override
    public void exportDot() throws IOException
    {
        DOTExporter<Component, DefaultEdge> dotter =
                        new DOTExporter<>(vertexIDProvider,
                                          new CompInstabiltyProvider(compInsts),
                                          new CompInstabiltyEdgeProvider(compGraph, compInsts),
                                          null,
                                          new CompInstabilityEdgeAttrProvider(compGraph, compInsts),
                                          null);
        dotter.putGraphAttribute("labelloc", "t");
        dotter.putGraphAttribute("label", "InstabilityDependencyFlow");
        dotter.putGraphAttribute("fontsize", "30");

        dotter.exportGraph(compGraph, new FileWriter(Files.createFile(dotPath).toFile()));
    }

    private void createDotFile() throws IOException
    {
        dotPath = Paths.get("components.dot");
        Files.deleteIfExists(dotPath);
    }

}
