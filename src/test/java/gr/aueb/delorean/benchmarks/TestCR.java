package gr.aueb.delorean.benchmarks;

import gr.aueb.delorean.pmcmr.PMCMR;
import gr.aueb.delorean.simpiece.SimPiece;
import gr.aueb.delorean.swingfilter.SwingFilter;
import gr.aueb.delorean.util.Point;
import gr.aueb.delorean.util.TimeSeries;
import gr.aueb.delorean.util.TimeSeriesReader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCR {
    private long PMCMR(List<Point> ts, double epsilon) {
        PMCMR pmcmr = new PMCMR(ts, epsilon);
        byte[] binary = pmcmr.toByteArray();

        long compressedSize = binary.length;

        pmcmr = new PMCMR(binary);
        List<Point> tsDecompressed = pmcmr.decompress();
        for (int i = 0; i < ts.size(); i++) {
            assertEquals(
                    ts.get(i).getValue(),
                    tsDecompressed.get(i).getValue(),
                    1.1 * epsilon,
                    "Value did not match for timestamp " + ts.get(i).getTimestamp()
            );
        }

        return compressedSize;
    }


    private long Swing(List<Point> ts, double epsilon) {
        SwingFilter swingFilter = new SwingFilter(ts, epsilon);
        byte[] binary = swingFilter.toByteArray();

        long compressedSize = binary.length;

        swingFilter = new SwingFilter(binary);
        List<Point> tsDecompressed = swingFilter.decompress();
        for (int i = 0; i < ts.size(); i++) {
            assertEquals(
                    ts.get(i).getValue(),
                    tsDecompressed.get(i).getValue(),
                    1.1 * epsilon,
                    "Value did not match for timestamp " + ts.get(i).getTimestamp()
            );
        }

        return compressedSize;
    }


    private long SimPiece(List<Point> ts, double epsilon) {
        SimPiece simPiece = new SimPiece(ts, epsilon);
        byte[] binary = simPiece.toByteArray();

        long compressedSize = binary.length;

        simPiece = new SimPiece(binary);
        List<Point> tsDecompressed = simPiece.decompress();
        for (int i = 0; i < ts.size(); i++) {
            assertEquals(
                    ts.get(i).getValue(),
                    tsDecompressed.get(i).getValue(),
                    1.1 * epsilon,
                    "Value did not match for timestamp " + ts.get(i).getTimestamp()
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
                "/Cricket.csv.gz",
                "/FaceFour.csv.gz",
                "/Lightning.csv.gz",
                "/MoteStrain.csv.gz",
                "/Wafer.csv.gz",
                "/WindSpeed.csv.gz",
                "/WindDirection.csv.gz",
                "/Pressure.csv.gz"
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
