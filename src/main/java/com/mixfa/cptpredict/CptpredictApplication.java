package com.mixfa.cptpredict;

import com.mixfa.cptpredict.service.impl.VMBenchmarkerImpl;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.component.page.Push;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration;

@SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
})
@Push
@ColorScheme(ColorScheme.Value.DARK)
public class CptpredictApplication implements AppShellConfigurator {


    public static void main(String[] args) throws Exception {
//        var cpu_freq = 5_100_000.0;
//        var ipc = Stream.of(
//                14502160,
//                10058603,
//                8873391,
//                4222253,
//                886789,
//                15107084,
//                6058374,
//                13198,
//                232391
//        ).map(it -> it / cpu_freq).mapToDouble(Double::doubleValue);
//
//        var app_ipc = (471425777866.0 / 11932.0) / cpu_freq;
//
//        System.out.println("CPU Freq: " + cpu_freq);
//        System.out.println("App IPC: " + app_ipc);
//
//        var target_machine_cpu_freq = 5_100_000.0;
//
//        var target_machine_ipc = Stream.of(
//                14502160,
//                10058603,
//                8873391,
//                4222253,
//                886789,
//                15107084,
//                6058374,
//                13198,
//                232391
//        ).map(it -> it / target_machine_cpu_freq).mapToDouble(Double::doubleValue);
//
//        System.out.println("______________");
//        System.out.println("Target machine CPU Freq: " + target_machine_cpu_freq);
////        target_machine_ipc.forEach(System.out::println);
//
//        var ipc_avg = ipc.average().getAsDouble();
//        var target_machine_ipc_avg = target_machine_ipc.average().getAsDouble();
//
//        var app_target_ipc = (target_machine_ipc_avg * app_ipc) / ipc_avg;
//
//        System.out.println(app_target_ipc);
//
//        var cpu_func = BigOAnalysis.analyze(
//                new double[]{
//                        1.0,
//                        2.0,
//                        3.0,
//                        4.0,
//                        5.0,
//                        6.0,
//                        7.0,
//                        10.0,
//                        15.0,
//                        20.0,
//                        100.0,
//                },
//                new double[]{
//                        4727362137.0,
//                        9441487545.0,
//                        14155612951.0,
//                        18869738329.0,
//                        23583863765.0,
//                        28297989324.0,
//                        33012114809.0,
//                        47154491005.0,
//                        70725118054.0,
//                        94295745053.0,
//                        471425777866.0,
//                }
//        ).get();
//        var n = 138;
//        var cpu_instructions = cpu_func.getFunction().applyAsDouble(n);
//
//        System.out.println("cpu instr: " + cpu_instructions);
//
//        var cpu_msec = cpu_instructions / (target_machine_cpu_freq * app_target_ipc);
//
//        System.out.println("cpu msec: " + cpu_msec);
//        System.out.println(DurationFormatUtils.formatDurationWords(
//                (long) cpu_msec, true, true
//        ));

//        new VMBenchmarkerImpl()
//                .benchmarkSSH("172.26.89.33", "mixfa", "semnadcat", 22);

//        var impl = new VMBenchmarkerImpl();
//        System.out.println(impl.benchmarkLocalMachine());
//        System.out.println(impl.benchmarkSSH("localhost", "mixfa", "semnadcat", 22));
//        SpringApplication.run(CptpredictApplication.class, args);
    }
}
