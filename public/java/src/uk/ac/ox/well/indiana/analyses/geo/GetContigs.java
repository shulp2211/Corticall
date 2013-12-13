package uk.ac.ox.well.indiana.analyses.geo;

import uk.ac.ox.well.indiana.tools.Module;
import uk.ac.ox.well.indiana.utils.arguments.Argument;
import uk.ac.ox.well.indiana.utils.arguments.Output;
import uk.ac.ox.well.indiana.utils.assembly.CortexGraphWalker;
import uk.ac.ox.well.indiana.utils.io.cortex.CortexGraph;
import uk.ac.ox.well.indiana.utils.io.cortex.CortexKmer;
import uk.ac.ox.well.indiana.utils.io.cortex.CortexMap;
import uk.ac.ox.well.indiana.utils.io.cortex.CortexRecord;

import java.io.PrintStream;
import java.util.*;

public class GetContigs extends Module {
    @Argument(fullName="diagnosticKmers", shortName="dk", doc="Diagnostic kmers")
    public HashSet<CortexKmer> DIAGNOSTIC_KMERS;

    @Argument(fullName="cortexGraph", shortName="cg", doc="Cortex graph")
    public CortexMap CORTEX_MAP;

    @Output
    public PrintStream out;

    @Override
    public void execute() {
        CortexGraphWalker cgw = new CortexGraphWalker(CORTEX_MAP);

        for (int color = 0; color < CORTEX_MAP.getGraph().getNumColors(); color++) {
            String sampleName = CORTEX_MAP.getGraph().getColor(color).getSampleName();

            Map<CortexKmer, Set<CortexKmer>> contigs = cgw.buildContigs(color, DIAGNOSTIC_KMERS);

            int index = 1;
            for (CortexKmer contig : contigs.keySet()) {
                out.println(">" + sampleName + "." + index);
                out.println(contig);

                index++;
            }
        }
    }
}
