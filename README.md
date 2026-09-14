# 🚀 VM Performance & Execution Time Predictor

[![Java](https://img.shields.io/badge/Java-25%2B-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![Vaadin](https://img.shields.io/badge/Vaadin-Latest-blue.svg)](https://vaadin.com/)
[![Rust](https://img.shields.io/badge/Rust-1.70%2B-black.svg)](https://www.rust-lang.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

An analytical software system for **predicting application execution time and evaluating virtual machine (VM) performance** across various cloud provider architectures without needing full-scale application execution on target environments.

---

## 📌 Overview

In cloud computing environments, choosing the right virtual machine (VM) configuration for resource-intensive workloads is a major challenge. Static theoretical models like traditional **CPI (Cycles Per Instruction)** or $O(N)$ asymptotic complexity fail to account for hypervisor overhead, memory latency, cache contention, and multi-tenant resource sharing ("noisy neighbor" effect). Empirical benchmarks (e.g., SPEC CPU) provide generalized scores, while hardware profilers (Intel VTune, Linux Perf) operate only retrospectively.

This system solves the problem by combining **Dynamic Binary Instrumentation (Intel PIN)**, **low-overhead micro-benchmarking in Rust & x86-64 Assembly**, and a **weighted IPC analytical mathematical model** across CPU, RAM, and Disk I/O subsystems.

---

## ✨ Key Features

- 🔬 **Dynamic Program Profiling (Intel PIN):** Analyzes compiled binary executables (no source code required) to measure executed instruction counts $N_i(N)$, cache misses / RAM accesses $N_{\text{ram}}(N)$, and Disk I/O bytes $N_{\text{disk}}(N)$ as functions of input data size $N$.
- 📈 **Automated Complexity Curve Fitting (AIC):** Fits non-linear models ranging from $O(1)$ to $O(N^3)$ and fractional dependencies using non-linear least squares. Automatically selects the optimal model using the **Akaike Information Criterion (AIC)** to prevent overfitting.
- ⚡ **Agentless Remote Benchmarking (SSH/SCP):** Executes micro-benchmarks on target remote VMs via secure SSH/SCP connections to evaluate target system IPC across CPU, Memory, and Storage subsystems.
- ⏱️ **Precision vCPU Frequency Calibration:** Utilizes inline **x86-64 Assembly** embedded in Rust to measure actual effective vCPU execution frequency $F_{\text{target}}$, stripping away hypervisor scheduling noise.
- 📊 **Subsystem-Weighted IPC Prediction Model:** Dynamically assigns computational weights ($W_{\text{cpu}}, W_{\text{ram}}, W_{\text{disk}}$) to reflect application resource intensity and projects execution time on target configurations.
- 💰 **Cloud Cost Optimization:** Integrates provider pricing policies to evaluate performance-to-cost ratios, recommending the most cost-effective VM tier.
- 💻 **Single-User Monolithic Web UI:** Modern, responsive dashboard built with **Vaadin** and **Spring Boot**, supporting hybrid persistence (Local JSON or optional MongoDB).

---

## 🛠️ Tech Stack

| Component | Technology / Library | Purpose |
|---|---|---|
| **Core Application** | Java 25 | Backend architecture, analytical computation layer, and service orchestrator |
| **Framework** | Spring Boot 3.x | Dependency Injection, application configuration, Spring Profiles |
| **Frontend / Web UI** | Vaadin | Full-stack Java web UI framework (Server-Side Rendering) |
| **Benchmarking Suite** | Rust + x86-64 Assembly | Zero-GC, low-overhead micro-benchmarks & CPU frequency calibration |
| **Instrumentation** | Intel PIN (`inscount`, `allcache`) | Dynamic binary instrumentation for instruction and memory event collection |
| **Regression & Analytics** | Apache Commons Math | Non-linear least-squares fitting and AIC model evaluation |
| **Data Persistence** | Hybrid (JSON / MongoDB / Spring Data) | Local file storage by default; configurable remote MongoDB integration |
| **Remote Orchestration** | SSH & SCP (JSch) | Agentless deployment and execution of benchmarks on remote VMs |
| **Build & Test** | Maven, JUnit 5, Mockito | Project build automation and unit/integration testing |

---

## ⚙️ How It Works

```
+------------------+      1. PIN Profiling      +-----------------------+
| Compiled Binary  | -------------------------> |  Input-to-Instruction |
|  (Program-under- |                            |  Functions Ni(N), etc.|
|      Test)       |                            +-----------------------+
+------------------+                                        |
                                                            v
+------------------+      2. SSH Micro-bench    +-----------------------+
|  Remote/Local VM | -------------------------> | Subsystem IPC Profiles|
|  Configurations  |   (Rust + x86-64 ASM)      |  (CPU, RAM, Disk I/O) |
+------------------+                            +-----------------------+
                                                            |
                                                            v
                                                +-----------------------+
                                                | Weighted IPC Model &  |
                                                | Time Prediction Engine|
                                                +-----------------------+
                                                            |
                                                            v
                                                +-----------------------+
                                                |  Predicted Execution  |
                                                |  Time & Cost Matrix   |
                                                +-----------------------+
```

### 1. Application Profiling Phase
The system uses Intel PIN tools (`inscount`, `allcache`) to observe the execution of target binaries across different dataset sizes ($N$). It measures:
- $N_i(N)$: Total instruction count
- $N_{\text{ram}}(N)$: Memory accesses and cache miss counts
- $N_{\text{disk}}(N)$: Disk bytes read/written

### 2. Micro-Benchmarking Phase
A suite of 9 specialized Rust micro-benchmarks is executed locally or deployed remotely via SSH/SCP to measure subsystem-specific IPC performance on target VMs:
- **CPU IPC:** Floating-point and integer intensive loop iterations.
- **RAM IPC:** Pointer-chasing and cache-churning access patterns.
- **DISK IPC:** Sequential and random block reads/writes.
- **vCPU Frequency Calibration:** Inline assembly calculates effective cycles per second ($F_{\text{target}}$).

### 3. Mathematical Analytical Engine
1. **Subsystem Weight Calculation:**
   $$\text{IPC}_{\text{prog, target}} = \text{IPC}_{\text{prog, test}} \times \left( \frac{\text{IPC}_{\text{target}}}{\text{IPC}_{\text{test}}} \right)$$
2. **Time Prediction Formula:**
   $$T_{\text{pred}} = \frac{N_i(N)}{F_{\text{target}} \times \text{IPC}_{\text{prog, target}}}$$

---

## 📈 Accuracy & Validation Results

Extensive experimental testing on real workloads (e.g., Monte Carlo simulations, memory churn algorithms) yielded strong correlation between predicted and real execution times:

| Workload Type | Target Environment | Input Size ($N$) | Predicted Time | Actual Time | Relative Error ($\varepsilon$) |
|---|---|---|---|---|---|
| **CPU-Bound** (Monte Carlo) | WSL Ubuntu (4 vCPU) | $20 \times 10^9$ | 356 s | 360 s | **1.11%** |
| **CPU-Bound** (Monte Carlo) | VirtualBox Debian (1 vCPU) | $20 \times 10^9$ | 608 s | 599 s | **1.50%** |
| **Memory-Bound** (Churn) | WSL Ubuntu (4 vCPU) | 10,000 | 358 s | ~362 s | **~1.1%** |
| **Memory-Bound** (Churn) | VirtualBox Debian (1 vCPU) | 10,000 | 634 s | ~642 s | **~1.2%** |

- **CPU-bound tasks:** ~1–3% error
- **Memory-bound tasks:** ~3–6% error
- **Disk I/O-bound tasks:** ~5–8% error (due to cloud storage latency variance)

---

## 🚀 Getting Started

### Prerequisites
- **JDK 25** or higher
- **Maven 3.8+**
- **Intel PIN Toolkit** (for dynamic program profiling)
- **Rust toolchain** (`cargo`, `rustc`) (for compiling micro-benchmarks)
- **SSH client** enabled (for remote VM benchmarking)

### Building the Application
```bash
# Clone the repository
git clone https://github.com/your-username/vm-performance-predictor.git
cd vm-performance-predictor

# Build Rust micro-benchmarks
cd native/benchmarks
cargo build --release
cd ../..

# Build Spring Boot / Vaadin application
mvn clean package -DskipTests
```

### Running the Application
```bash
java -jar target/vm-performance-predictor-1.0.0.jar
```
Open your browser and navigate to `http://localhost:8080`.

---

 
