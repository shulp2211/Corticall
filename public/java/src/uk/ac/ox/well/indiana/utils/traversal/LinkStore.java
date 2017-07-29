package uk.ac.ox.well.indiana.utils.traversal;

import uk.ac.ox.well.indiana.utils.io.cortex.links.CortexJunctionsRecord;
import uk.ac.ox.well.indiana.utils.io.cortex.links.CortexLinksRecord;
import uk.ac.ox.well.indiana.utils.sequence.SequenceUtils;

import java.util.*;

/**
 * Created by kiran on 24/07/2017.
 */
public class LinkStore {
    private Map<String, Integer> linkAges = new TreeMap<>();
    private Map<String, Integer> linkPos = new TreeMap<>();

    public void add(String curKmer, CortexLinksRecord clr, boolean goForward) {
        boolean recordOrientationMatchesKmer = clr.getKmerAsString().equals(curKmer);

        List<CortexJunctionsRecord> junctions = new ArrayList<>();
        junctions.addAll(clr.getJunctions());

        for (CortexJunctionsRecord cjr : junctions) {
            boolean linkGoesForward = recordOrientationMatchesKmer ? cjr.isForward() : !cjr.isForward();
            String junctionList = linkGoesForward ? cjr.getJunctions() : SequenceUtils.complement(cjr.getJunctions());

            if (linkGoesForward == goForward && !linkAges.containsKey(junctionList)) {
                linkAges.put(junctionList, 0);
                linkPos.put(junctionList, 0);
            }
        }
    }

    public void incrementAge() {
        for (String junctionList : linkAges.keySet()) {
            linkAges.put(junctionList, linkAges.get(junctionList) + 1);
        }
    }

    public void incrementPositions(String choice) {
        Set<String> toRemove = new HashSet<>();

        for (String junctionList : linkPos.keySet()) {
            int pos = linkPos.get(junctionList);

            if (pos + 1 >= junctionList.length() || !junctionList.substring(pos, pos + 1).equals(choice)) {
                toRemove.add(junctionList);
            } else if (junctionList.substring(pos, pos + 1).equals(choice)) {
                linkPos.put(junctionList, pos + 1);
            }
        }

        for (String junctionList : toRemove) {
            linkAges.remove(junctionList);
            linkPos.remove(junctionList);
        }
    }

    public String getOldestLink() {
        int age = Integer.MIN_VALUE;
        String oldestLink = null;

        for (String junctionList : linkAges.keySet()) {
            if (linkAges.get(junctionList) > age) {
                age = linkAges.get(junctionList);
                oldestLink = junctionList;
            } else if (linkAges.get(junctionList) == age) {
                if (oldestLink != null && junctionList.charAt(linkPos.get(junctionList)) == oldestLink.charAt(linkPos.get(oldestLink))) {
                    if (junctionList.length() > oldestLink.length()) {
                        oldestLink = junctionList;
                    }
                } else {
                    oldestLink = null;
                }
            }
        }

        return oldestLink;
    }

    public String getNextJunctionChoice() {
        String junctionList = getOldestLink();
        String choice = null;

        if (junctionList != null) {
            int pos = linkPos.get(junctionList);

            choice = junctionList.substring(pos, pos + 1);

            incrementPositions(choice);
        }

        return choice;
    }

    public boolean isActive() {
        return linkPos.size() > 0;
    }
}
