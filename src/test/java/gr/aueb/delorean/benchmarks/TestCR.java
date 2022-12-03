package gr.aueb.delorean.benchmarks;

import gr.aueb.delorean.util.TimeSeries;
import gr.aueb.delorean.pmcmr.PMCMRCompressor;
import gr.aueb.delorean.pmcmr.PMCMRDecompressor;
import gr.aueb.delorean.pmcmr.PMCMREncoder;
import gr.aueb.delorean.pmcmr.PMCMRSegment;
import gr.aueb.delorean.simpiece.SimPieceCompressor;
import gr.aueb.delorean.simpiece.SimPieceDecompressor;
import gr.aueb.delorean.simpiece.SimPieceEncoder;
import gr.aueb.delorean.simpiece.SimPieceSegment;
import gr.aueb.delorean.util.TimeSeriesReader;
import gr.aueb.delorean.swingfilter.SwingFilterCompressor;
import gr.aueb.delorean.swingfilter.SwingFilterDecompressor;
import gr.aueb.delorean.swingfilter.SwingFilterEncoder;
import gr.aueb.delorean.swingfilter.SwingFilterSegment;
import gr.aueb.delorean.util.Point;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCR {
    private long PMCMR(List<Point> ts, double epsilon) {
        List<PMCMRSegment> segments = PMCMRCompressor.filter(ts, epsilon);

        byte[] binary = PMCMREncoder.getBinary(segments);
        long compressedSize = binary.length;
        segments = PMCMREncoder.readBinary(binary);

        PMCMRDecompressor d = new PMCMRDecompressor(segments);
        for (Point point : ts) {
            Double decompressedValue = d.readValue();
            assertEquals(
                    point.getValue(),
                    decompressedValue,
                    1.1 * epsilon,
                    "Value did not match for timestamp " + point.getTimestamp()
            );
        }

        return compressedSize;
    }


    private long Swing(List<Point> ts, double epsilon) {
        List<SwingFilterSegment> segments = SwingFilterCompressor.filter(ts, epsilon);

        byte[] binary = SwingFilterEncoder.getBinary(segments);
        long compressedSize = binary.length;
        segments = SwingFilterEncoder.readBinary(binary);

        SwingFilterDecompressor d = new SwingFilterDecompressor(segments);
        for (Point point : ts) {
            double decompressedValue = d.readValue();
            assertEquals(
                    point.getValue(),
                    decompressedValue,
                    1.1 * epsilon,
                    "Value did not match for timestamp " + point.getTimestamp()
            );
        }

        return compressedSize;
    }


    private long SimPiece(List<Point> ts, double epsilon) {
        List<SimPieceSegment> segments = SimPieceCompressor.filter(ts, epsilon);
        segments = SimPieceCompressor.mergeSegments(segments);

        byte[] binary = SimPieceEncoder.getBinary(epsilon, segments);
        long compressedSize = binary.length;
        segments = SimPieceEncoder.readBinary(binary);

        SimPieceDecompressor simPieceDecompressor = new SimPieceDecompressor(segments);
        for (Point point : ts) {
            double decompressedValue = simPieceDecompressor.readValue();
            assertEquals(
                    point.getValue(),
                    decompressedValue,
                    1.1 * epsilon,
                    "Value did not match for timestamp " + point.getTimestamp()
            );
        }

        return compressedSize;
    }


    @Test
    public void TestCR() {
        double epsilonStart = 0.005;
        double epsilonStep = 0.005;
        double epsilonEnd = 0.05;

        String delimiter = ",";
        final String[] filenames = {
                "/Lightning.csv.gz",
                "/Wafer.csv.gz",
                "/MoteStrain.csv.gz",
                "/Cricket.csv.gz",
                "/FaceFour.csv.gz",
        };

        for (String filename : filenames) {
            System.out.println(filename);
            TimeSeries ts = TimeSeriesReader.getTimeSeries(getClass().getResourceAsStream(filename), delimiter);

            System.out.println("PMCMR");
            for (double epsilonPct = epsilonStart; epsilonPct <= epsilonEnd; epsilonPct += epsilonStep)
                System.out.printf("Epsilon: %.2f%%\tCompression Ratio: %.3f\n",
                        epsilonPct * 100, (double) ts.size / PMCMR(ts.data, ts.range * epsilonPct));

            System.out.println("Swing");
            for (double epsilonPct = epsilonStart; epsilonPct <= epsilonEnd; epsilonPct += epsilonStep)
                System.out.printf("Epsilon: %.2f%%\tCompression Ratio: %.3f\n",
                        epsilonPct * 100, (double) ts.size / Swing(ts.data, ts.range * epsilonPct));

            System.out.println("Sim-Piece");
            for (double epsilonPct = epsilonStart; epsilonPct <= epsilonEnd; epsilonPct += epsilonStep)
                System.out.printf("Epsilon: %.2f%%\tCompression Ratio: %.3f\n",
                        epsilonPct * 100, (double) ts.size / SimPiece(ts.data, ts.range * epsilonPct));

            System.out.println();
        }
    }
}
