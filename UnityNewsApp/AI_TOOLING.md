# AI Tooling Write-Up

## Tools Used

I used OpenAI Codex as a coding assistant throughout the project. I used it frequently for:

- Exploring the assignment and turning it into an implementation plan.
- Creating and reviewing the two-app architecture.
- Implementing Kotlin/Compose/Hilt/Room/AIDL code.
- Writing and updating tests.
- Improving documentation and README files.
- Running Gradle verification commands and inspecting failures.

I treated the AI as a pair-programming assistant, not as an unchecked source of truth. I reviewed the generated code, corrected architectural direction when needed, and verified changes with tests/builds.

## Where AI Accelerated The Work

AI helped quickly compare architectural options for inter-app communication. We considered HTTP-like local servers, ContentProvider, broadcasts, and Android native IPC. AIDL was selected because it gives a typed, explicit Android IPC contract and fits the assignment's “backend app” concept without pretending the backend is a real network server.

AI also helped generate repetitive but important structure: module READMEs, KDoc comments, repository/use-case wiring, and focused unit tests. This saved time while still keeping the code reviewable and consistent.

## Where AI Was Wrong Or Needed Correction

At one point the presentation layer had use cases defined but the ViewModel injected the repository directly. That was off-architecture for the clean layering I wanted. I corrected the dependency direction so the ViewModel injects use cases, and the use cases depend on the repository interface.

AI also initially leaned toward refreshing articles immediately when filters changed. The assignment specifically asks for an Apply/Save action that applies filters together, so I changed the flow to keep draft criteria separate from applied criteria and refresh only when the user taps Apply.

## What I Would Do Differently Next Time

I would write the architecture note earlier, before implementing most of the code. The design became clearer as the code evolved, but having the architecture/security/scalability decisions written first would make review and implementation even smoother.

For a production version, I would also decide earlier whether the article stream should be list-based or paged. This assignment does not require pagination, but a real large-scale news product should design pagination and cache eviction from day one.
