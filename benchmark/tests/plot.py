import os
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

# =========================
# I/O DIRECTORY
# =========================

INPUT_DIR = "benchmark/tests"
OUTPUT_DIR = "benchmark/tests/summary"

os.makedirs(INPUT_DIR, exist_ok=True)
os.makedirs(OUTPUT_DIR, exist_ok=True)

# =========================
# READ CSV
# =========================

df = pd.read_csv(
    os.path.join(
        INPUT_DIR,
        "benchmark.csv"
    )
)

# =========================
# SUMMARY
# =========================

summary = (
    df.groupby(
        ["algorithm", "function_class", "eps"]
    )
    .agg(
        avg_runtime=("avg_time_ns", "mean"),
        median_runtime=("median_time_ns", "mean"),
        avg_sd=("sd_time_ns", "mean"),
        avg_iterations=("iterations", "mean"),
    )
    .reset_index()
)

print(summary)

# =========================
# SAVE SUMMARY CSV
# =========================

summary.to_csv(
    os.path.join(
        OUTPUT_DIR,
        "benchmark_summary.csv"
    ),
    index=False
)

# =========================
# EPS LABEL
# =========================

summary["eps_label"] = (
    summary["eps"]
    .apply(lambda x: f"{x:.0e}")
)

# =========================
# AVG RUNTIME PLOT
# =========================

plt.figure(figsize=(12, 7))

for algorithm in summary["algorithm"].unique():

    for func in summary["function_class"].unique():

        subset = summary[
            (summary["algorithm"] == algorithm)
            &
            (summary["function_class"] == func)
        ]

        plt.plot(
            subset["eps_label"],
            subset["avg_runtime"],
            marker="o",
            label=f"{algorithm} - {func}"
        )

plt.xlabel("Epsilon")
plt.ylabel("Average Runtime (ns)")

plt.title(
    "Average Runtime vs Epsilon"
)

plt.legend(
    bbox_to_anchor=(1.05, 1),
    loc="upper left"
)

plt.grid(True)

plt.tight_layout()

plt.savefig(
    os.path.join(
        OUTPUT_DIR,
        "avg_runtime_vs_eps.png"
    ),
    dpi=300,
    bbox_inches="tight"
)

plt.close()

# =========================
# ITERATIONS PLOT
# =========================

plt.figure(figsize=(12, 7))

for algorithm in summary["algorithm"].unique():

    for func in summary["function_class"].unique():

        subset = summary[
            (summary["algorithm"] == algorithm)
            &
            (summary["function_class"] == func)
        ]

        plt.plot(
            subset["eps_label"],
            subset["avg_iterations"],
            marker="o",
            label=f"{algorithm} - {func}"
        )

plt.xlabel("Epsilon")
plt.ylabel("Average Iterations")

plt.title(
    "Iterations vs Epsilon"
)

plt.legend(
    bbox_to_anchor=(1.05, 1),
    loc="upper left"
)

plt.grid(True)

plt.tight_layout()

plt.savefig(
    os.path.join(
        OUTPUT_DIR,
        "iterations_vs_eps.png"
    ),
    dpi=300,
    bbox_inches="tight"
)

plt.close()

# =========================
# STABILITY PLOT
# =========================

plt.figure(figsize=(12, 7))

for algorithm in summary["algorithm"].unique():

    for func in summary["function_class"].unique():

        subset = summary[
            (summary["algorithm"] == algorithm)
            &
            (summary["function_class"] == func)
        ]

        plt.plot(
            subset["eps_label"],
            subset["avg_sd"],
            marker="o",
            label=f"{algorithm} - {func}"
        )

plt.xlabel("Epsilon")
plt.ylabel("Standard Deviation (ns)")

plt.title(
    "Runtime Stability vs Epsilon"
)

plt.legend(
    bbox_to_anchor=(1.05, 1),
    loc="upper left"
)

plt.grid(True)

plt.tight_layout()

plt.savefig(
    os.path.join(
        OUTPUT_DIR,
        "runtime_stability.png"
    ),
    dpi=300,
    bbox_inches="tight"
)

plt.close()

# =========================
# OVERALL RUNTIME BAR CHART
# =========================

overall = (
    df.groupby(
        ["algorithm", "function_class"]
    )
    .agg(
        overall_runtime=("avg_time_ns", "mean")
    )
    .reset_index()
)

plt.figure(figsize=(12, 7))

labels = (
    overall["algorithm"]
    + "\n"
    + overall["function_class"]
)

plt.bar(
    labels,
    overall["overall_runtime"]
)

plt.xlabel("Algorithm + Function Class")

plt.ylabel("Average Runtime (ns)")

plt.title(
    "Overall Runtime Comparison"
)

plt.xticks(rotation=45)

plt.tight_layout()

plt.savefig(
    os.path.join(
        OUTPUT_DIR,
        "overall_runtime.png"
    ),
    dpi=300,
    bbox_inches="tight"
)

plt.close()

# =========================
# ALGORITHM COMPARISON CSV
# =========================

algorithm_summary = (
    df.groupby("algorithm")
    .agg(
        avg_runtime=("avg_time_ns", "mean"),
        median_runtime=("median_time_ns", "mean"),
        avg_sd=("sd_time_ns", "mean"),
        avg_iterations=("iterations", "mean"),
    )
    .reset_index()
)

algorithm_summary.to_csv(
    os.path.join(
        OUTPUT_DIR,
        "algorithm_summary.csv"
    ),
    index=False
)

print("Done!")
print(
    f"All outputs saved to: {OUTPUT_DIR}"
)
