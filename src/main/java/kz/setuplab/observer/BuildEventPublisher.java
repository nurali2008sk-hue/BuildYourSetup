package kz.setuplab.observer;

import java.util.ArrayList;
import java.util.List;

public class BuildEventPublisher {

    private final List<BuildObserver> observers =
            new ArrayList<>();

    public void addObserver(BuildObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException(
                    "Observer cannot be null."
            );
        }

        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(BuildObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(BuildEvent event) {
        for (BuildObserver observer : List.copyOf(observers)) {
            observer.onBuildChanged(event);
        }
    }
}