package kz.setuplab.observer;

public record BuildEvent(
        BuildEventType type,
        String buildId
) {}