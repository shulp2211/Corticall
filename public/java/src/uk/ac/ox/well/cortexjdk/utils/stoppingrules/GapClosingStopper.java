package uk.ac.ox.well.cortexjdk.utils.stoppingrules;

import uk.ac.ox.well.cortexjdk.utils.traversal.CortexEdge;
import uk.ac.ox.well.cortexjdk.utils.traversal.CortexVertex;
import uk.ac.ox.well.cortexjdk.utils.traversal.TraversalState;

/**
 * Created by kiran on 02/06/2017.
 */
public class GapClosingStopper extends AbstractTraversalStoppingRule<CortexVertex, CortexEdge> {
    @Override
    public boolean hasTraversalSucceeded(TraversalState<CortexVertex> s) {
        //return s.getPreviousGraph().containsVertex(s.getCurrentVertex());
        return false;
    }

    @Override
    public boolean hasTraversalFailed(TraversalState<CortexVertex> s) {
        return s.getCurrentJunctionDepth() > 5 || s.getNumAdjacentEdges() == 0;
    }
}
