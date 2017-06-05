package uk.ac.ox.well.indiana.utils.stoppingconditions;

import org.jgrapht.DirectedGraph;
import uk.ac.ox.well.indiana.utils.io.cortex.graph.CortexGraph;
import uk.ac.ox.well.indiana.utils.io.cortex.graph.CortexRecord;
import uk.ac.ox.well.indiana.utils.traversal.CortexEdge;
import uk.ac.ox.well.indiana.utils.traversal.CortexVertex;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by kiran on 08/05/2017.
 */
public class NahrStopper extends AbstractTraversalStopper<CortexVertex, CortexEdge> {

    @Override
    public boolean hasTraversalSucceeded(CortexRecord cr, DirectedGraph<CortexVertex, CortexEdge> g, int junctions, int size, int edges, int childColor, Set<Integer> parentColors) {
        return false;
    }

    @Override
    public boolean hasTraversalFailed(CortexRecord cr, DirectedGraph<CortexVertex, CortexEdge> g, int junctions, int size, int edges, int childColor, Set<Integer> parentColors) {
        return false;
    }

    private boolean foundNovels = false;
    private int distanceFromLastNovel = 0;

    @Override
    public boolean hasTraversalSucceeded(CortexVertex cv, boolean goForward, int traversalColor, Set<Integer> joiningColors, int currentTraversalDepth, int currentGraphSize, int numAdjacentEdges, boolean childrenAlreadyTraversed, DirectedGraph<CortexVertex, CortexEdge> previousGraph, CortexGraph rois) {
        if (foundNovels) {
            distanceFromLastNovel++;
        }

        if (rois.findRecord(cv.getCr().getCortexKmer()) != null) {
            foundNovels = true;
            distanceFromLastNovel++;
        }

        return foundNovels && (distanceFromLastNovel >= 1000 || currentTraversalDepth >= 5 || numAdjacentEdges == 0 || childrenAlreadyTraversed);
    }

    @Override
    public boolean hasTraversalFailed(CortexVertex cv, boolean goForward, int traversalColor, Set<Integer> joiningColors, int currentTraversalDepth, int currentGraphSize, int numAdjacentEdges, boolean childrenAlreadyTraversed, DirectedGraph<CortexVertex, CortexEdge> previousGraph, CortexGraph rois) {
        return !foundNovels && (currentGraphSize >= 1000 || currentTraversalDepth >= 2 || numAdjacentEdges == 0);
    }
}
