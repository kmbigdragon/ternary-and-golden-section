package benchmark.sources;

import java.util.function.DoubleUnaryOperator;

public class GoldenSectionSearch {

    public static double goldenSectionSearch(
            double left,
            double right,
            DoubleUnaryOperator f,
            double eps,
            int maxIter
    ) {

        final double phi = (1 + Math.sqrt(5)) / 2.0;

        double m1 = right - (right - left) / phi;
        double m2 = left + (right - left) / phi;

        double f1 = f.applyAsDouble(m1);
        double f2 = f.applyAsDouble(m2);

        for (int i = 0; i < maxIter; i++) {

            if (Math.abs(right - left) < eps) {
                break;
            }

            if (f1 < f2) {

                right = m2;

                m2 = m1;
                f2 = f1;

                m1 = right - (right - left) / phi;
                f1 = f.applyAsDouble(m1);

            } else {

                left = m1;

                m1 = m2;
                f1 = f2;

                m2 = left + (right - left) / phi;
                f2 = f.applyAsDouble(m2);
            }
        }

        double x = (left + right) / 2.0;

        return f.applyAsDouble(x);
    }

}
