
package kz.setuplab.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuildEventPublisherTest {

    private BuildEventPublisher publisher;
    private List<BuildEvent> receivedEvents;
    private BuildObserver observer;

    @BeforeEach
    void setUp() {
        publisher = new BuildEventPublisher();
        receivedEvents = new ArrayList<>();

        observer = receivedEvents::add;

        publisher.addObserver(observer);
    }

    @Test
    void observerReceivesCreatedEvent() {
        BuildEvent event = new BuildEvent(
                BuildEventType.CREATED,
                "build-1"
        );

        publisher.notifyObservers(event);

        assertEquals(1, receivedEvents.size());
        assertEquals(
                BuildEventType.CREATED,
                receivedEvents.get(0).type()
        );
        assertEquals(
                "build-1",
                receivedEvents.get(0).buildId()
        );
    }

    @Test
    void observerReceivesUpdatedEvent() {
        publisher.notifyObservers(
                new BuildEvent(
                        BuildEventType.UPDATED,
                        "build-2"
                )
        );

        assertEquals(1, receivedEvents.size());
        assertEquals(
                BuildEventType.UPDATED,
                receivedEvents.get(0).type()
        );
    }

    @Test
    void observerReceivesDeletedEvent() {
        publisher.notifyObservers(
                new BuildEvent(
                        BuildEventType.DELETED,
                        "build-3"
                )
        );

        assertEquals(1, receivedEvents.size());
        assertEquals(
                BuildEventType.DELETED,
                receivedEvents.get(0).type()
        );
    }

    @Test
    void removedObserverDoesNotReceiveEvents() {
        publisher.removeObserver(observer);

        publisher.notifyObservers(
                new BuildEvent(
                        BuildEventType.CREATED,
                        "build-4"
                )
        );

        assertTrue(receivedEvents.isEmpty());
    }

    @Test
    void duplicateObserverIsNotRegisteredTwice() {
        publisher.addObserver(observer);

        publisher.notifyObservers(
                new BuildEvent(
                        BuildEventType.CREATED,
                        "build-5"
                )
        );

        assertEquals(1, receivedEvents.size());
    }

    @Test
    void nullObserverIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> publisher.addObserver(null)
        );
    }
}
