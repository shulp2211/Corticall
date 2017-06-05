package uk.ac.ox.well.indiana.utils.traversal;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import uk.ac.ox.well.indiana.utils.io.cortex.graph.CortexKmer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kiran on 03/06/2017.
 */
public class PathFinder {
    private Graph<CortexVertex, CortexEdge> g;

    public PathFinder(Graph<CortexVertex, CortexEdge> graph, int color) {
        g = new DefaultDirectedGraph<>(CortexEdge.class);

        for (CortexEdge e : graph.edgeSet()) {
            if (e.getColor() == color) {
                CortexVertex s = graph.getEdgeSource(e);
                CortexVertex t = graph.getEdgeTarget(e);

                g.addVertex(s);
                g.addVertex(t);
                g.addEdge(s, t, e);
            }
        }
    }

    public GraphPath<CortexVertex, CortexEdge> getPathFinder(CortexVertex startVertex, CortexVertex endVertex) {
        return getPathFinder(startVertex, endVertex, null, true);
    }

    public GraphPath<CortexVertex, CortexEdge> getPathFinder(CortexVertex startVertex, CortexVertex endVertex, CortexKmer constraint, boolean accept) {
        KShortestPaths<CortexVertex, CortexEdge> ksp = new KShortestPaths<>(g, 10);

        List<GraphPath<CortexVertex, CortexEdge>> pathsUnfiltered = ksp.getPaths(startVertex, endVertex);
        List<GraphPath<CortexVertex, CortexEdge>> pathsFiltered;

        if (constraint == null) {
            pathsFiltered = pathsUnfiltered;
        } else {
            pathsFiltered = new ArrayList<>();

            for (GraphPath<CortexVertex, CortexEdge> gp : pathsUnfiltered) {
                boolean constraintFound = false;

                for (CortexVertex cv : gp.getVertexList()) {
                    CortexKmer ck = new CortexKmer(cv.getSk());

                    if (ck.equals(constraint)) {
                        constraintFound = true;
                        break;
                    }
                }

                if ((constraintFound && accept) || (!constraintFound && !accept)) {
                    pathsFiltered.add(gp);
                }
            }
        }

        if (pathsFiltered == null || pathsFiltered.size() == 0) {
            return null;
        }

        return pathsFiltered.get(0);
    }
}
