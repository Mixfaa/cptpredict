package com.mixfa.cptpredict.service.impl;

import com.mixfa.cptpredict.misc.CommandExecutor;
import com.mixfa.cptpredict.misc.PythonCommands;
import com.mixfa.cptpredict.misc.Utils;
import com.mixfa.cptpredict.model.VMBenchmarkResult;
import com.mixfa.cptpredict.model.benchmark.BenchmarkAppResult;
import com.mixfa.cptpredict.model.benchmark.IPCBenchmarkApp;
import com.mixfa.cptpredict.service.VMBenchmarker;
import dev.toonformat.jtoon.JToon;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.scp.client.ScpClient;
import org.apache.sshd.scp.client.ScpClientCreator;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class VMBenchmarkerImpl implements VMBenchmarker {
    private static final String BENCHMARKS_DIR = "./benchmarks/";
    private static final String BENCHMARKS_DIR_GLOBAL = "benchmarks/";

    private static final List<IPCBenchmarkApp> BENCHMARKS = List.of(
            new IPCBenchmarkApp("ipc_bench1", IPCBenchmarkApp.Type.CPU, 42612519311.0),
            new IPCBenchmarkApp("ipc_bench2", IPCBenchmarkApp.Type.CPU, 31266231637.0),
            new IPCBenchmarkApp("ipc_bench3", IPCBenchmarkApp.Type.CPU, 20047469552.0),
            new IPCBenchmarkApp("ipc_bench4", IPCBenchmarkApp.Type.RAM, 14053396507.0),
            new IPCBenchmarkApp("ipc_bench5", IPCBenchmarkApp.Type.RAM, 1227000595.0),
            new IPCBenchmarkApp("ipc_bench6", IPCBenchmarkApp.Type.RAM, 22368686340.0),
            new IPCBenchmarkApp("ipc_bench7", IPCBenchmarkApp.Type.RAM, 9462805294.0),
            new IPCBenchmarkApp("ipc_bench8", IPCBenchmarkApp.Type.DISK, 1075639543.0),
            new IPCBenchmarkApp("ipc_bench9", IPCBenchmarkApp.Type.DISK, 122448109.0)
    );


    private static final String FREQ_BENCHMARK = "freq-benchmark";
    private static final String RAM_BENCHMARK = "ram_benchmark";
//    private static final String FREQ_BENCHMARK_WIN_AMD64 = "freq-benchmark-x86_64-pc-windows-gnu.exe";
//    private static final String FREQ_BENCHMARK_WIN_ARM64 = "freq-benchmark-aarch64-pc-windows-gnullvm.exe";
//    private static final String FREQ_BENCHMARK_LINUX_AMD64 = "freq-benchmark-x86_64-unknown-linux-gnu";
//    private static final String FREQ_BENCHMARK_LINUX_ARM64 = "freq-benchmark-aarch64-unknown-linux-gnu";

//    private static boolean isWindows(String os) {
//        return os.contains("windows");
//    }

    private static boolean isLinux(String os) {
        return os.contains("linux");
    }
//
//    private static boolean isArm64(String arch) {
//        return arch.equals("arm64");
//    }
//
//    private static boolean isAmd64(String arch) {
//        return arch.equals("amd64") || arch.equals("x86_64");
//    }

    public static String getCpuName(boolean isLinux, PythonCommands pythonCommands, CommandExecutor commandExecutor) throws Exception {
        var cmd = isLinux ? pythonCommands.getCpuNameLinux() : pythonCommands.getCpuNameWindows();
        return commandExecutor.executeCommand(cmd);
    }

    private static Map<String, Object> runBenchmarkOld(String benchmarkExecutable, CommandExecutor commandExecutor) throws Exception {
        var dataStr = commandExecutor.executeCommand(benchmarkExecutable);

        try {
            return (Map<String, Object>) JToon.decode(dataStr);
        } catch (Exception e) {
            throw new Exception("Parsing error", e);
        }
    }

    private static double runIpcBenchmark(IPCBenchmarkApp benchmarkApp, String os, String arch, CommandExecutor commandExecutor, String benchmarkDir) throws Exception {
        var output = commandExecutor.executeCommand(benchmarkDir + benchmarkApp.executableName(os, arch));
        return Double.parseDouble(output);
    }

    private static long runAvailableRamBenchmark(String benchmarkDir, String os, String arch, CommandExecutor commandExecutor) throws Exception {
        var availableRam = commandExecutor.executeCommand(benchmarkDir + Utils.executableName(RAM_BENCHMARK, os, arch));
        return Long.parseLong(availableRam.trim());
    }

    private static VMBenchmarkResult benchmarkMachine(CommandExecutor commandExecutor, PythonCommands pythonCommands, String benchmarksDir) throws Exception {
        var osArch = getOsArchSSH(pythonCommands, commandExecutor);

        var os = osArch.getFirst();
        var arch = osArch.getSecond();

        var cpuName = getCpuName(isLinux(os), pythonCommands, commandExecutor);

        var availableRam = runAvailableRamBenchmark(benchmarksDir, os, arch, commandExecutor);
        var freqData = runBenchmarkOld(benchmarksDir + Utils.executableName(FREQ_BENCHMARK, os, arch), commandExecutor);

        var cores = freqData.size();
        var frequencies = new double[cores];
        freqData.forEach((name, value) -> {
            var coreNumber = Integer.parseInt(name.substring("Core-".length()));
            var coreFreqKHz = Double.parseDouble(String.valueOf(value)) / 1000.0; // convert to khz

            frequencies[coreNumber] = coreFreqKHz;
        });

        var results = new ArrayList<BenchmarkAppResult>(); //new BenchmarkAppResult[BENCHMARKS.size()];

        var cycles = 3;
        for (IPCBenchmarkApp benchmark : BENCHMARKS) {
            var totalTime = 0.0;
            for (int j = 0; j < cycles; j++) {
                totalTime += runIpcBenchmark(benchmark, os, arch, commandExecutor, benchmarksDir);
            }
            var time = totalTime / cycles;
            if (time == 0) time = 1; // are you running on nuke?
            var instrPerMs = benchmark.testedInstructions() / time;

            results.add(new BenchmarkAppResult(benchmark, instrPerMs));
        }
        return new VMBenchmarkResult(cpuName, cores, availableRam, frequencies, results.toArray(BenchmarkAppResult[]::new));
    }

    @Override
    public VMBenchmarkResult benchmarkLocalMachine() throws Exception {
        var processExecutor = CommandExecutor.LocalMachineExecutor.instance();
        var pythonCommands = PythonCommands.tryCreate(processExecutor);
        return benchmarkMachine(processExecutor, pythonCommands, BENCHMARKS_DIR);
    }

    private static Pair<String, String> getOsArchSSH(PythonCommands pythonCommands, CommandExecutor commandExecutor) throws Exception {
        var osArch = commandExecutor.executeCommand(pythonCommands.getOsArchCmd())
                .toLowerCase().trim().split(":");
        if (osArch.length != 2)
            throw new RuntimeException("Invalid OS arch result: " + Arrays.toString(osArch));
        return Pair.of(osArch[0], osArch[1]);
    }

    private static List<Path> makeBenchmarksList(String os, String arch) {
        var benchmarks = BENCHMARKS.stream()
                .map(benchmarkApp -> Paths.get(BENCHMARKS_DIR_GLOBAL, benchmarkApp.executableName(os, arch)))
                .collect(Collectors.toCollection(ArrayList::new));

        benchmarks.add(Path.of(BENCHMARKS_DIR_GLOBAL + Utils.executableName(FREQ_BENCHMARK, os, arch)));
        benchmarks.add(Path.of(BENCHMARKS_DIR_GLOBAL + Utils.executableName(RAM_BENCHMARK, os, arch)));

        return benchmarks;
    }

    @Override
    public VMBenchmarkResult benchmarkSSH(String host, String user, String password, int port) throws Exception {
        try (SshClient client = SshClient.setUpDefaultClient()) {
            client.start();

            try (ClientSession session = client.connect(user, host, port).verify(10, TimeUnit.SECONDS).getSession()) {
                session.addPasswordIdentity(password);
                session.auth().verify(10, TimeUnit.SECONDS);

                var processExecutor = new CommandExecutor.SSHSessionExecutor(session);
                var pythonCommands = PythonCommands.tryCreate(processExecutor);

                var osArch = getOsArchSSH(pythonCommands, processExecutor);
                var os = osArch.getFirst();
                var arch = osArch.getSecond();

                var listOfFiles = makeBenchmarksList(os, arch);


                // transfer files
                var scpClientCreator = ScpClientCreator.instance();
                var scpClient = scpClientCreator.createScpClient(session);

                final var tempDir = "tempdir/";

                try {
                    session.executeRemoteCommand(pythonCommands.removeDir(tempDir));
                    session.executeRemoteCommand(pythonCommands.makeDir(tempDir));

                    for (var filepath : listOfFiles) {
                        var remotePath = tempDir + filepath.getFileName().toString();
                        scpClient.upload(filepath, remotePath, ScpClient.Option.PreserveAttributes);

                        if (isLinux(os))
                            session.executeRemoteCommand("chmod +x " + remotePath);
                    }

                    return benchmarkMachine(processExecutor, pythonCommands, tempDir);
                } catch (Exception e) {
                    throw e;
                } finally {
                    session.executeRemoteCommand(pythonCommands.removeDir(tempDir));
                }
            } finally {
                client.stop();
            }
        }
    }
}

