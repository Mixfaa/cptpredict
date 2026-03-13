package com.mixfa.cptpredict.service;

import com.mixfa.cptpredict.model.VMBenchmarkResult;

public interface VMBenchmarker {
    VMBenchmarkResult benchmarkLocalMachine() throws Exception;

    VMBenchmarkResult benchmarkSSH(String host, String user, String password, int port) throws Exception;
}
