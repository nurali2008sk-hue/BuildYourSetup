
package kz.setuplab.observer;

import kz.setuplab.model.PcBuild;
import kz.setuplab.facade.SetupFacade;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class SavedBuildsObserver implements BuildObserver {

    private final SetupFacade facade;
    private final Consumer<List<PcBuild>> onRefresh;

    public SavedBuildsObserver(
            SetupFacade facade,
            Consumer<List<PcBuild>> onRefresh
    ) {
        this.facade = facade;
        this.onRefresh = onRefresh;
    }

    @Override
    public void onBuildChanged(BuildEvent event) {
        try {
            List<PcBuild> builds = facade.getSavedBuilds();
            onRefresh.accept(builds);
        } catch (IOException exception) {
            System.err.println(
                    "Failed to refresh saved builds: "
                            + exception.getMessage()
            );
        }
    }
}
