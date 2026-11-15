package kz.setuplab.observer;

public class NotificationObserver implements BuildObserver {

    @Override
    public void onBuildChanged(BuildEvent event) {

        switch (event.type()) {
            case CREATED ->
                    System.out.println(
                            "Build created: " + event.buildId()
                    );

            case UPDATED ->
                    System.out.println(
                            "Build updated: " + event.buildId()
                    );

            case DELETED ->
                    System.out.println(
                            "Build deleted: " + event.buildId()
                    );
        }
    }
}g