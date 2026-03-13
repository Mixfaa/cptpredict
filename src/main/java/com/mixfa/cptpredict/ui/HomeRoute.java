package com.mixfa.cptpredict.ui;

import com.mixfa.cptpredict.service.VMBenchmarker;
import com.mixfa.cptpredict.service.repo.RepoHolder;
import com.vaadin.flow.router.Route;

@Route("/")
public class HomeRoute extends VMConfigManagerRoute {
    public HomeRoute(RepoHolder repoHolder, VMBenchmarker vmBenchmarker) {
        super(repoHolder, vmBenchmarker);
    }
}
