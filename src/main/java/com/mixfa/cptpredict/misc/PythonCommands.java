package com.mixfa.cptpredict.misc;


public final class PythonCommands {

    private PythonCommands(String pythonCommand) {
        PYTHON_CMD = pythonCommand;
    }

    private final String PYTHON_CMD;
    private static final PythonCommands INSTANCE_WINDOWS = new PythonCommands("python");
    private static final PythonCommands INSTANCE_LINUX = new PythonCommands("python3");
    private static final PythonCommands[] INSTANCES = {INSTANCE_LINUX, INSTANCE_WINDOWS};

    public static PythonCommands tryCreate(CommandExecutor commandExecutor) {
        for (PythonCommands instance : INSTANCES) {
            try {
                commandExecutor.executeCommand(instance.version());
                return instance;
            } catch (Exception e) {

            }
        }
        throw new RuntimeException("Python not installed");
    }

    public String version() {
        return PYTHON_CMD + " --version";
    }

    public String getOsArchCmd() {
        return PYTHON_CMD + " -c \"import platform; info = platform.uname(); print(f'{info.system}:{info.machine}')\"";
    }

    public String getCpuNameLinux() {
        return PYTHON_CMD + " -c \"import subprocess; cmd = 'grep -m1 \\\"model name\\\" /proc/cpuinfo'; out = subprocess.check_output(cmd, shell=True).decode().strip(); print(out.split(':')[1].strip())\"";
    }

    public String getCpuNameWindows() {
        return PYTHON_CMD + " -c \"import subprocess;command = 'wmic cpu get name';cpu_name = subprocess.check_output(command, shell=True).decode().split('\\n')[1].strip();print(cpu_name);\"";
    }

    public String makeDir(String path) {
        return String.format(
                "%s -c \"import os; os.makedirs('%s', exist_ok=True);\"",
                PYTHON_CMD,
                path
        );
    }

    public String removeDir(String path) {
        return String.format(
                "%s -c \"import shutil; shutil.rmtree('%s', ignore_errors=True)\"",
                PYTHON_CMD,
                path
        );
    }
}
