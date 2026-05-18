package benchmark.sources;

import java.util.function.DoubleUnaryOperator;

public class TernarySearch {

    public static double ternarySearch(
            double left,
            double right,
            DoubleUnaryOperator f,
            double eps,
            int maxIter
    ) {

        for (int i = 0; i < maxIter; i++) {

            double m1 = left + (right - left) / 3.0;
            double m2 = right - (right - left) / 3.0;
            double m = (m1 + m2) / 2.0;

            if (Math.abs(f.applyAsDouble(m)) < eps) {
                return f.applyAsDouble(m);
            }

            if (f.applyAsDouble(m1) < f.applyAsDouble(m2)) {
                right = m2;
            } else {
                left = m1;
            }
        }

        return f.applyAsDouble((left + right) / 2.0);
    }
    
}
