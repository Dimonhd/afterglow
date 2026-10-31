package com.afterglow.physics;

/** Решение СЛАУ A·x = b методом Гаусса с выбором главного элемента. */
public final class LinearSolver {
    private LinearSolver() {}

    /**
     * Решает систему. Матрица и вектор изменяются на месте.
     * @return решение x либо null, если матрица вырождена.
     */
    public static double[] solve(double[][] a, double[] b) {
        int n = b.length;
        for (int col = 0; col < n; col++) {
            int pivot = col;
            double best = Math.abs(a[col][col]);
            for (int r = col + 1; r < n; r++) {
                double v = Math.abs(a[r][col]);
                if (v > best) { best = v; pivot = r; }
            }
            if (best < 1e-18) return null;
            if (pivot != col) {
                double[] t = a[pivot]; a[pivot] = a[col]; a[col] = t;
                double tb = b[pivot]; b[pivot] = b[col]; b[col] = tb;
            }
            double diag = a[col][col];
            for (int r = col + 1; r < n; r++) {
                double f = a[r][col] / diag;
                if (f == 0.0) continue;
                for (int c = col; c < n; c++) a[r][c] -= f * a[col][c];
                b[r] -= f * b[col];
            }
        }
        double[] x = new double[n];
        for (int r = n - 1; r >= 0; r--) {
            double s = b[r];
            for (int c = r + 1; c < n; c++) s -= a[r][c] * x[c];
            x[r] = s / a[r][r];
        }
        return x;
    }
}
