package kz.setuplab.observer;

import kz.setuplab.model.PcBuild;
import java.util.ArrayList;
import java.util.List;

public class BuildEventPublisher {

    private final List<BuildObserver> observers =
            new ArrayList<>();

    public void addObserver(BuildObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(BuildObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(PcBuild build) {
        for (BuildObserver observer : observers) {
            observer.onBuildChanged(build);
        }
    }
}