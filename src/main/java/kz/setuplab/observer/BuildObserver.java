package kz.setuplab.observer;

import kz.setuplab.model.PcBuild;

public interface BuildObserver {

    void onBuildChanged(PcBuild build);

}