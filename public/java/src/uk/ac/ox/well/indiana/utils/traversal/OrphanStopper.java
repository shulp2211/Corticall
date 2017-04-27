package uk.ac.ox.well.indiana.utils.traversal;

import org.jgrapht.DirectedGraph;
import uk.ac.ox.well.indiana.utils.io.cortex.graph.CortexRecord;

import java.util.Set;

/**
 * Created by kiran on 26/04/2017.
 */
public class OrphanStopper extends AbstractTraversalStopper<AnnotatedVertex, AnnotatedEdge> {
    public boolean hasTraversalSucceeded(CortexRecord cr, DirectedGraph<AnnotatedVertex, AnnotatedEdge> g, int depth, int size, int edges, int childColor, Set<Integer> parentColors) {
        // We should accept this branch if we make it to the end of our traversal and there are no more edges to navigate

        boolean hasNoIncomingEdges = cr.getInDegree(childColor) == 0;
        boolean hasNoOutgoingEdges = cr.getOutDegree(childColor) == 0;

        return hasNoIncomingEdges || hasNoOutgoingEdges;
    }

    @Override
    public boolean hasTraversalFailed(CortexRecord cr, DirectedGraph<AnnotatedVertex, AnnotatedEdge> g, int depth, int size, int edges, int childColor, Set<Integer> parentColors) {
        // We should reject this branch if we ever reconnect with the parental colors.

        boolean reunion = false;
        for (int c : parentColors) {
            reunion |= cr.getCoverage(c) > 0;
        }

        return reunion;
    }

    @Override
    public int maxJunctionsAllowed() {
        return 0;
    }
}
