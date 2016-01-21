package uk.ac.ox.well.indiana.commands.gg;

import org.jgrapht.DirectedGraph;
import uk.ac.ox.well.indiana.commands.Module;
import uk.ac.ox.well.indiana.utils.arguments.Argument;
import uk.ac.ox.well.indiana.utils.arguments.Output;
import uk.ac.ox.well.indiana.utils.io.cortex.graph.CortexGraph;
import uk.ac.ox.well.indiana.utils.io.cortex.graph.CortexKmer;
import uk.ac.ox.well.indiana.utils.io.cortex.graph.CortexRecord;
import uk.ac.ox.well.indiana.utils.io.utils.LineReader;
import uk.ac.ox.well.indiana.utils.sequence.CortexUtils;

import java.io.File;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

public class FilterNovels extends Module {
    @Argument(fullName="clean", shortName="c", doc="Graph")
    public CortexGraph CLEAN;

    @Argument(fullName="novelKmers", shortName="n", doc="Novel kmers")
    public CortexGraph NOVEL_KMERS;

    @Argument(fullName="contaminants", shortName="x", doc="Contaminants")
    public CortexGraph REJECTED_KMERS;

    @Argument(fullName="lowerThreshold", shortName="l", doc="Lower coverage threshold")
    public String LOWER_THRESHOLD = "10";

    @Argument(fullName="upperThreshold", shortName="u", doc="Upper coverage threshold")
    public String UPPER_THRESHOLD = "100";

    @Output
    public PrintStream out;

    private int loadThreshold(String threshold) {
        File f = new File(threshold);
        if (f.exists()) {
            LineReader lr = new LineReader(f);
            threshold = lr.getNextRecord();
        }

        return Integer.valueOf(threshold);
    }

    @Override
    public void execute() {
        int upperThreshold = loadThreshold(UPPER_THRESHOLD);
        int lowerThreshold = loadThreshold(LOWER_THRESHOLD);

        log.info("Filtering novel kmers...");
        log.info("  {} kmers to start with", NOVEL_KMERS.getNumRecords());

        log.info("Examining coverage...");
        Set<CortexKmer> coverageOutliers = new HashSet<CortexKmer>();
        for (CortexRecord cr : NOVEL_KMERS) {
            if (cr.getCoverage(0) < lowerThreshold || cr.getCoverage(0) > upperThreshold) {
                coverageOutliers.add(cr.getCortexKmer());
            }
        }
        log.info("  {} kmers outside coverage limits {} and {}", coverageOutliers.size(), lowerThreshold, upperThreshold);

        Set<CortexKmer> contaminatingKmers = new HashSet<CortexKmer>();

        log.info("Exploring contaminants...");
        for (CortexRecord cr : REJECTED_KMERS) {
            if (!coverageOutliers.contains(cr.getCortexKmer()) && !contaminatingKmers.contains(cr.getCortexKmer())) {
                DirectedGraph<AnnotatedVertex, AnnotatedEdge> dfs = CortexUtils.dfs(CLEAN, null, cr.getKmerAsString(), 0, null, ContaminantStopper.class);

                for (AnnotatedVertex rv : dfs.vertexSet()) {
                    CortexRecord rr = CLEAN.findRecord(new CortexKmer(rv.getKmer()));

                    if (rr.getCoverage(0) > 0 && rr.getCoverage(1) == 0 && rr.getCoverage(2) == 0) {
                        contaminatingKmers.add(rr.getCortexKmer());
                    }
                }

                contaminatingKmers.add(cr.getCortexKmer());
            }
        }
        log.info("  {} contaminants found", contaminatingKmers.size());

        Set<CortexKmer> orphanedKmers = new HashSet<CortexKmer>();

        log.info("Finding orphans...");
        for (CortexRecord cr : NOVEL_KMERS) {
            if (!contaminatingKmers.contains(cr.getCortexKmer()) && !orphanedKmers.contains(cr.getCortexKmer())) {
                DirectedGraph<AnnotatedVertex, AnnotatedEdge> dfs = CortexUtils.dfs(CLEAN, null, cr.getKmerAsString(), 0, null, ChildTraversalStopper.class);

                Set<CortexKmer> novelKmers = new HashSet<CortexKmer>();
                boolean isOrphaned = true;

                for (AnnotatedVertex av : dfs.vertexSet()) {
                    CortexKmer ak = new CortexKmer(av.getKmer());
                    CortexRecord ar = CLEAN.findRecord(ak);

                    if (ar != null) {
                        if (ar.getCoverage(0) > 0 && ar.getCoverage(1) == 0 && ar.getCoverage(2) == 0) {
                            novelKmers.add(ak);
                        } else if (ar.getCoverage(1) > 0 || ar.getCoverage(2) > 0) {
                            isOrphaned = false;
                            break;
                        }
                    }
                }

                if (isOrphaned) {
                    orphanedKmers.addAll(novelKmers);
                }
            }
        }
        log.info("  {} orphaned kmers", orphanedKmers.size());

        int count = 0;
        for (CortexRecord cr : NOVEL_KMERS) {
            if (!coverageOutliers.contains(cr.getCortexKmer()) && !contaminatingKmers.contains(cr.getCortexKmer()) && !orphanedKmers.contains(cr.getCortexKmer())) {
                out.println(">" + count);
                out.println(cr.getKmerAsString());

                count++;
            }
        }

        log.info("Before: {}", NOVEL_KMERS.getNumRecords());
        log.info("After: {}", count);
    }
}
