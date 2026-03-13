package com.mixfa.cptpredict.misc;


public final class PythonCommands {
    private PythonCommands() {

    }

    public static String getOsArchCmd() {
        return "python -c \"import platform; info = platform.uname(); print(f'{info.system}:{info.machine}')\"";
    }

    public static String getCpuNameLinux() {
        return "python -c \"import subprocess; cmd = 'grep -m1 \\\"model name\\\" /proc/cpuinfo'; out = subprocess.check_output(cmd, shell=True).decode().strip(); print(out.split(':')[1].strip())\"";
    }

    public static String getCpuNameWindows() {
        return "python -c \"import subprocess;command = 'wmic cpu get name';cpu_name = subprocess.check_output(command, shell=True).decode().split('\\n')[1].strip();print(cpu_name);\"";
    }

    public static String removeDir(String path) {
        return String.format(
                "python -c \"import os; os.makedirs('%s', exist_ok=True);\"",
                path
        );
    }

    public static String makeDir(String path) {
        return String.format(
                "python -c \"import shutil; shutil.rmtree('%s')\"",
                path
        );
    }
}
