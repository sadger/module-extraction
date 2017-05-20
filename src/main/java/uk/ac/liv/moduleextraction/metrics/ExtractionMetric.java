package uk.ac.liv.moduleextraction.metrics;


public class ExtractionMetric {


    public enum ExtractionType{
        AMEX,
        STAR,
        HYBRID,
        N_DEPLETING
    }

    private final ExtractionType type;
    private final Integer moduleSize;
    private final Long timeTaken;
    private final Long syntacticChecks;
    private final Long qbfChecks;
    private final Long separabilityAxioms;

    private ExtractionMetric(ExtractionType type, Integer moduleSize, Long timeTaken, Long syntacticChecks, Long qbfChecks, Long separabilityAxioms){
        this.type = type;
        this.moduleSize = moduleSize;
        this.timeTaken = timeTaken;
        this.syntacticChecks = syntacticChecks;
        this.qbfChecks = qbfChecks;
        this.separabilityAxioms = separabilityAxioms;
    }

    public int getModuleSize() {
        return moduleSize;
    }

    public ExtractionType getType() {
        return type;
    }

    public long getQbfChecks() {
        return qbfChecks;
    }

    public long getSeparabilityAxiomCount() {
        return separabilityAxioms;
    }

    public long getSyntacticChecks() {
        return syntacticChecks;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    @Override
    public String toString() {
        StringBuilder build = new StringBuilder();
        build.append("Extraction Type: " + type.toString() + "\n");
        build.append("Module size: " + moduleSize + "\n");
        build.append("Time Taken: " + timeTaken + "\n");
        build.append("QBF Checks: " + qbfChecks + "\n");
        build.append("Syntactic Checks: " + syntacticChecks + "\n");
        build.append("Separability Axioms Found:" + separabilityAxioms + "\n");
        return build.toString();
    }

    public static class MetricBuilder{
        //Use boxed types to allow for null on missing rather than 0
        private ExtractionType type = null;
        private Integer moduleSize = null;
        private Long timeTaken = null;
        private Long syntacticChecks = null;
        private Long qbfChecks = null;
        private Long separabilityAxioms = null;

        public MetricBuilder(ExtractionType type){
            this.type = type;
        }

        public MetricBuilder moduleSize(final int size){
            this.moduleSize = size;
            return this;
        }

        public MetricBuilder timeTaken(final long timeTaken){
            this.timeTaken = timeTaken;
            return this;
        }

        public MetricBuilder syntacticChecks(final long checks){
            this.syntacticChecks = checks;
            return this;
        }

        public MetricBuilder qbfChecks(final long checks){
            this.qbfChecks = checks;
            return this;
        }

        public MetricBuilder separabilityCausingAxioms(final long axioms){
            this.separabilityAxioms = axioms;
            return this;
        }


        public ExtractionMetric createMetric(){
            return new ExtractionMetric(type,moduleSize,timeTaken,syntacticChecks,qbfChecks,separabilityAxioms);
        }
    }

    public static void main(String[] args) {
        MetricBuilder m = new MetricBuilder(ExtractionType.AMEX);
        System.out.println(m.createMetric());
//
        MetricBuilder r = new MetricBuilder(ExtractionType.AMEX).syntacticChecks(10).moduleSize(11);
        System.out.println(r.createMetric());
    }
}
