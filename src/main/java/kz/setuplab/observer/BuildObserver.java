package kz.setuplab.observer;

public interface BuildObserver {
    void onBuildChanged(BuildEvent event);
}