# BuildYourSetup

**BuildYourSetup** is a Java desktop application for configuring PC builds. Users can select components, receive budget-aware recommendations, add optional services, save and manage builds, and export configurations to TXT or HTML.

This project was developed as a team final project to demonstrate the practical use of **six object-oriented design patterns** in one application.

## Team Members

| Member | GitHub | Main Responsibilities |
| --- | --- | --- |
| 1 | [nurali2008sk-hue](https://github.com/nurali2008sk-hue) | Builder and Strategy; PC build creation and recommendation logic |
| 2 | [mansurahmetov33-gif](https://github.com/mansurahmetov33-gif) | Facade and Observer; saved-build management, editing, notifications, and GUI refresh |
| 3 | [gulkapi](https://github.com/gulkapi) | Decorator and Factory Method; optional build services and TXT/HTML export |

## Features

- Browse the available PC component catalog.
- Create PC configurations through the desktop interface.
- Generate compatible, budget-aware recommendations using **Economy** or **Performance** selection strategies.
- Add optional **assembly**, **warranty**, and **OS installation** services and calculate the resulting price.
- Save PC builds to a local JSON file.
- View, reload, edit, and delete saved builds.
- Refresh the saved-builds interface when build changes are published.
- Export a configuration to **TXT** or **HTML**.

## Technologies

- **Java 21**
- **Java Swing** for the graphical user interface
- **Maven** for build and dependency management
- **Gson 2.11.0** for JSON persistence
- **JUnit 5.11.4** for automated tests
- **PlantUML** for UML diagrams

## Design Patterns

### 1. Builder

**Purpose:** Construct a `PcBuild` with its name, identifier, and selected components.

**Implementation:** `PcBuild.Builder` in `kz.setuplab.model.PcBuild`.

### 2. Strategy

**Purpose:** Switch between different ways of choosing a recommended build.

**Implementation:** `BuildSelectionStrategy`, `EconomyStrategy`, and `PerformanceStrategy` in `kz.setuplab.strategy`. The recommendation service uses a selection strategy to choose from candidate builds.

### 3. Facade

**Purpose:** Provide a single entry point for common build operations instead of exposing multiple services and repositories directly to the UI.

**Implementation:** `SetupFacade` in `kz.setuplab.facade`, coordinating recommendation, compatibility checking, persistence, and build-change notifications.

### 4. Observer

**Purpose:** Notify interested objects after a build is created, updated, or deleted.

**Implementation:** `BuildObserver`, `BuildEvent`, `BuildEventPublisher`, `SavedBuildsObserver`, and `NotificationObserver` in `kz.setuplab.observer`.

### 5. Decorator

**Purpose:** Add optional services and their costs without changing the base PC build model.

**Implementation:** `PricedBuild`, `BasePricedBuild`, `BuildServiceDecorator`, `AssemblyDecorator`, `WarrantyDecorator`, and `OsInstallationDecorator` in `kz.setuplab.decorator`.

### 6. Factory Method

**Purpose:** Create the appropriate exporter for each supported output format.

**Implementation:** `ExportFactory`, `TxtExportFactory`, `HtmlExportFactory`, `BuildExporter`, `TxtExporter`, and `HtmlExporter` in `kz.setuplab.export`.

## Project Structure

```text
BuildYourSetup/
├── pom.xml
├── docs/
│   ├── sequence-diagram.puml
│   └── class-diagram.puml
├── data/
│   ├── builds.json
│   └── build-services.json
└── src/
    ├── main/java/kz/setuplab/
    │   ├── Main.java
    │   ├── data/
    │   ├── decorator/
    │   ├── export/
    │   ├── facade/
    │   ├── model/
    │   ├── observer/
    │   ├── repository/
    │   ├── service/
    │   ├── strategy/
    │   └── ui/
    └── test/java/kz/setuplab/
        ├── decorator/
        ├── export/
        └── observer/
```

The diagram paths above describe the intended final repository layout. Ensure `docs/class-diagram.puml` has been committed and pushed. JSON data files are created or updated during application use and may not both be present in a fresh checkout.

## Requirements

- JDK **21 or newer**
- Apache Maven, or an IDE with Maven support (such as IntelliJ IDEA)

## How to Run

1. Clone the repository:

   ```bash
   git clone https://github.com/nurali2008sk-hue/BuildYourSetup.git
   cd BuildYourSetup
   ```

2. Start the Swing application using Maven:

   ```bash
   mvn compile exec:java
   ```

   Alternatively, open the Maven project in IntelliJ IDEA and run `kz.setuplab.Main`.

3. Use the interface to browse components, create or recommend a build, select optional services, save configurations, manage saved builds, and export results.

## Running Tests

Run all JUnit tests with:

```bash
mvn clean test
```

The repository includes tests for the Facade/Observer behavior, decorators, and export factories. Run the command on the latest combined version before submission.

## Data Storage

The application stores builds locally using JSON:

- `data/builds.json` — saved PC configurations, managed by `JsonBuildRepository`.
- `data/build-services.json` — selected optional services associated with builds, managed by `BuildServiceRepository`.

These files are local application data, not a database server. Their contents can change when the application is used.

## UML Diagrams

- [`docs/sequence-diagram.puml`](docs/sequence-diagram.puml) — sequence of interactions during build editing and notification.
- [`docs/class-diagram.puml`](docs/class-diagram.puml) — class relationships and design patterns (add this file to the repository if it has not yet been pushed).

Open `.puml` files with a PlantUML viewer or the PlantUML IntelliJ plugin.

## Notes

BuildYourSetup is an educational project demonstrating design-pattern integration, Java desktop UI development, automated testing, and local JSON persistence.