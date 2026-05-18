package benchmark.tests;

import benchmark.sources.GoldenSectionSearch;
import benchmark.sources.TernarySearch;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;

public class OptimizationBenchmark {

    static class Result {

        double x;
        double fx;

        int iterations;

        long avgTimeNs;
        long medianTimeNs;

        double sdTimeNs;

        public Result(
                double x,
                double fx,
                int iterations,
                long avgTimeNs,
                long medianTimeNs,
                double sdTimeNs
        ) {
            this.x = x;
            this.fx = fx;
            this.iterations = iterations;
            this.avgTimeNs = avgTimeNs;
            this.medianTimeNs = medianTimeNs;
            this.sdTimeNs = sdTimeNs;
        }
    }

    static class FunctionInfo {

        String className;
        DoubleUnaryOperator function;

        public FunctionInfo(
                String className,
                DoubleUnaryOperator function
        ) {
            this.className = className;
            this.function = function;
        }
    }

    @FunctionalInterface
    interface OptimizationAlgorithm {

        double run(
                double left,
                double right,
                DoubleUnaryOperator f,
                double eps,
                int maxIter
        );
    }

    public static Result benchmarkAlgorithm(
            OptimizationAlgorithm algorithm,
            double left,
            double right,
            DoubleUnaryOperator f,
            double eps,
            int maxIter,
            int repeat,
            double shrinkFactor
    ) {

        long[] times = new long[repeat];

        double fx = 0;

        // Warm-up
        for (int i = 0; i < 1000; i++) {

            algorithm.run(
                    left,
                    right,
                    f,
                    eps,
                    maxIter
            );
        }

        // Benchmark
        for (int i = 0; i < repeat; i++) {

            long start = System.nanoTime();

            fx = algorithm.run(
                    left,
                    right,
                    f,
                    eps,
                    maxIter
            );

            long end = System.nanoTime();

            times[i] = end - start;
        }

        Arrays.sort(times);

        // Average
        long sum = 0;

        for (long t : times) {
            sum += t;
        }

        long avg = sum / repeat;

        // Median
        long median;

        if (repeat % 2 == 0) {

            median =
                    (times[repeat / 2]
                            + times[repeat / 2 - 1]) / 2;

        } else {

            median = times[repeat / 2];
        }

        // Standard deviation
        double variance = 0;

        for (long t : times) {
            variance += Math.pow(t - avg, 2);
        }

        variance /= repeat;

        double sd = Math.sqrt(variance);

        // Estimated iterations
        int iterations = 0;

        double range = right - left;

        while (range > eps) {

            range /= shrinkFactor;

            iterations++;
        }

        double x = (left + right) / 2.0;

        return new Result(
                x,
                fx,
                iterations,
                avg,
                median,
                sd
        );
    }

    public static FunctionInfo generateFunction(
            Random rd,
            int type
    ) {

        double a = rd.nextDouble() * 10 + 1;

        double b = rd.nextDouble() * 20 - 10;

        double c = rd.nextDouble() * 50 - 25;

        switch (type) {

            case 0:
                return new FunctionInfo(
                        "Quadratic",
                        x -> a * Math.pow(x - b, 2) + c
                );

            case 1:
                return new FunctionInfo(
                        "Absolute",
                        x -> a * Math.abs(x - b) + c
                );

            case 2:
                return new FunctionInfo(
                        "Exponential",
                        x -> a * Math.exp(
                                0.1
                                        * Math.pow(
                                        x - b,
                                        2
                                )
                        ) + c
                );

            default:
                return new FunctionInfo(
                        "LogCosh",
                        x -> Math.log(
                                Math.cosh(
                                        a
                                                * (x - b)
                                                / 5.0
                                )
                        ) + c
                );
        }
    }

    public static void main(String[] args) {

        Locale.setDefault(Locale.US);

        Random rd = new Random();

        double[] epsList = {
                1e-2,
                1e-4,
                1e-6,
                1e-8,
                1e-10
        };

        int totalTests = 10000;

        int repeat = 50;

        int maxIter = 1000;

        try (

                PrintWriter writer =
                        new PrintWriter(
                                new FileWriter(
                                        "benchmark.csv"
                                )
                        )

        ) {

            writer.println(
                    "algorithm,"
                            + "test_id,"
                            + "function_class,"
                            + "eps,"
                            + "iterations,"
                            + "avg_time_ns,"
                            + "median_time_ns,"
                            + "sd_time_ns,"
                            + "approx_x,"
                            + "approx_fx"
            );

            // Global warm-up
            for (int i = 0; i < 10000; i++) {

                TernarySearch.ternarySearch(
                        -100,
                        100,
                        x -> x * x,
                        1e-6,
                        100
                );

                GoldenSectionSearch
                        .goldenSectionSearch(
                                -100,
                                100,
                                x -> x * x,
                                1e-6,
                                100
                        );
            }

            for (int test = 1;
                 test <= totalTests;
                 test++) {

                int functionType =
                        rd.nextInt(4);

                FunctionInfo info =
                        generateFunction(
                                rd,
                                functionType
                        );

                for (double eps : epsList) {

                    // =====================
                    // TERNARY SEARCH
                    // =====================

                    Result ternaryResult =
                            benchmarkAlgorithm(
                                    TernarySearch::ternarySearch,
                                    -100,
                                    100,
                                    info.function,
                                    eps,
                                    maxIter,
                                    repeat,
                                    1.5
                            );

                    writer.printf(
                            "%s,%d,%s,%.0e,%d,%d,%d,%.4f,%.12f,%.12f%n",
                            "Ternary",
                            test,
                            info.className,
                            eps,
                            ternaryResult.iterations,
                            ternaryResult.avgTimeNs,
                            ternaryResult.medianTimeNs,
                            ternaryResult.sdTimeNs,
                            ternaryResult.x,
                            ternaryResult.fx
                    );

                    // =====================
                    // GOLDEN SECTION
                    // =====================

                    Result goldenResult =
                            benchmarkAlgorithm(
                                    GoldenSectionSearch
                                            ::goldenSectionSearch,
                                    -100,
                                    100,
                                    info.function,
                                    eps,
                                    maxIter,
                                    repeat,
                                    1.6180339887
                            );

                    writer.printf(
                            "%s,%d,%s,%.0e,%d,%d,%d,%.4f,%.12f,%.12f%n",
                            "GoldenSection",
                            test,
                            info.className,
                            eps,
                            goldenResult.iterations,
                            goldenResult.avgTimeNs,
                            goldenResult.medianTimeNs,
                            goldenResult.sdTimeNs,
                            goldenResult.x,
                            goldenResult.fx
                    );
                }

                if (test % 100 == 0) {

                    System.out.println(
                            "Completed: "
                                    + test
                                    + "/"
                                    + totalTests
                    );
                }
            }

            System.out.println("Done!");

            System.out.println(
                    "Saved to benchmark.csv"
            );

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}
