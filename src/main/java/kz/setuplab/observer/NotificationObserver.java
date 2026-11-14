package kz.setuplab.observer;

import kz.setuplab.model.PcBuild;

public class NotificationObserver implements BuildObserver {

    @Override
    public void onBuildChanged(PcBuild build) {
        System.out.println(
                "Build updated: " + build.getName()
        );
    }
}