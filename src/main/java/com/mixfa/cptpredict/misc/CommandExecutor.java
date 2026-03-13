package com.mixfa.cptpredict.misc;

import org.apache.sshd.client.session.ClientSession;

public interface CommandExecutor {
    String executeCommand(String command) throws Exception;

    record SSHSessionExecutor(ClientSession session) implements CommandExecutor {
        @Override
        public String executeCommand(String command) throws Exception {
            return session.executeRemoteCommand(command);
        }
    }

    record LocalMachineExecutor() implements CommandExecutor {
        private static final LocalMachineExecutor instance = new LocalMachineExecutor();

        public static LocalMachineExecutor instance() {
            return instance;
        }

        @Override
        public String executeCommand(String command) throws Exception {
            var process = new ProcessBuilder(command.split(" ")).start();
            process.waitFor();
            return new String(process.getInputStream().readAllBytes());
        }
    }
}
