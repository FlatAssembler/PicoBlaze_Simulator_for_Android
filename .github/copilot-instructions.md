# Copilot instructions for PicoBlaze_Simulator_for_Android

This file gives repository-specific guidance to Copilot sessions: how to build/test, high-level architecture, and important conventions to respect when changing or extending the code.

---

Build, test and lint commands

- Build full project (Unix): ./gradlew build
- Build full project (Windows): gradlew.bat build
- Assemble debug APK: ./gradlew assembleDebug  (Windows: gradlew.bat assembleDebug)
- Install debug APK to connected device/emulator: ./gradlew installDebug
- Run unit tests (module-level): ./gradlew :app:testDebugUnitTest
- Run a single JUnit unit test (example):
  ./gradlew :app:testDebugUnitTest --tests "hr.ferit.teo_samarzija.picoblaze_simulator.ExampleUnitTest"
  (Windows: use gradlew.bat)
- Run instrumentation (connected) tests: ./gradlew connectedAndroidTest
- Run Android lint for module: ./gradlew :app:lintDebug
- Clean: ./gradlew clean

Notes:
- Use the included gradlew / gradlew.bat wrappers to guarantee the Gradle version defined by the project.

---

High-level architecture (big picture)

- Purpose: an Android port of the PicoBlaze simulator where the assembler and UI are implemented in JavaScript (served from app/src/main/assets) and the emulator core is implemented in Java/Kotlin.

- UI layer (WebView): MainActivity loads file:///android_asset/MainActivityWebView.html. The HTML/JS assets implement the assembler, syntax highlighter and the simulation UI. Key assets: assembler.js, parser.js, tokenizer.js, preprocessor.js, SimulationWebView.html, MainActivityWebView.html and supporting JS files in app/src/main/assets.

- Java/Kotlin bridge: WebAppInterface.java is registered into the WebView as "PicoBlaze" (webView.addJavascriptInterface). JavaScript calls PicoBlaze.* methods to send assembled machine code, set instructions/line numbers, manipulate breakpoints, and query simulator state. MainActivity triggers the "sendMachineCodeToJava()" JS function via webView.evaluateJavascript(...) to request the assembler to push results to Java.

- Emulator core: Simulator.kt contains the emulation loop and runtime state (memory, registers, PC, flags, terminal I/O). It is a singleton accessed by WebAppInterface methods for runtime queries and control (start/pause/reset). The emulator uses a 4096-word program memory and follows PicoBlaze conventions (PC modulo 4096, register banking, I/O ports for UART and switches).

- Program model: AssembledProgram.java is the central in-Java singleton storing instructions[], lineNumbers[], assemblyCode, breakpoints and forbiddenBreakpoints. The JavaScript assembler writes into this model using the WebAppInterface methods (setInstructionAtAddress, setLineNumberAtAddress, setAssemblyCode, flushTheTerminal, etc.).

- Flow summary: User edits assembly in the WebView -> JS assembles -> JS writes machine-code and metadata into Java via the PicoBlaze interface -> MainActivity navigates to the machine-code or simulation screen -> Simulator executes instructions and updates the WebView via evaluateJavascript callbacks.

---

Key conventions and important patterns

- Interop points (do not rename lightly):
  - JS -> Java interface name: "PicoBlaze" (created in MainActivity). JS code expects these methods to exist exactly as implemented in WebAppInterface.java.
  - Trigger to transfer assembled code: evaluateJavascript("sendMachineCodeToJava()") in MainActivity and simulation flow. Keep the JS function name unless refactoring both sides.

- Machine-code representation:
  - Instructions are stored as 18-bit integers in AssembledProgram.instructions (size 4096). Line numbers are kept in AssembledProgram.lineNumbers, and breakpoints are tracked by source line number (breakpoints) and forbidden breakpoints by address (forbiddenBreakpoints).
  - PC arithmetic uses PC % 4096. Be consistent when adding/control-flow changes.

- Terminal and I/O:
  - Terminal I/O and UART ports are emulated in Simulator.kt. Ports 2/3/4 are used for UART RX/TX/reset semantics; port 0 is mapped to hardware switches. WebAppInterface exposes getTerminalOutput(), setTerminalInput(), setSwitches(int).

- Threading and WebView evaluation:
  - Simulator posts UI updates to the WebView thread using WebView.post(...) and evaluateJavascript(...) from the UI thread. Avoid blocking the UI thread when making Simulator changes; use the existing timer/task patterns.

- Tests and tasks:
  - Unit tests are standard JUnit tests under app/src/test/java. Instrumentation tests (if any) are under app/src/androidTest.

- Editing caution:
  - Simulator.kt was largely AI-generated and converted from Java; it may contain non-idiomatic Kotlin patterns. When modifying the emulator core, keep behavior-preserving changes and add unit tests where possible.
  - Many assets are plain JS files that the WebView expects to be present and to expose specific global functions and element IDs (e.g., playPauseImage, UART_output). If renaming elements or functions in HTML/JS, update Java calls that interact with them.

---

Where to look first when working on a feature

- MainActivity.java — WebView setup and high-level entry points (assemble(), startSimulating(), moveToMachineCode(), moveToSimulation()).
- WebAppInterface.java — JS-Java bridge: methods the JS calls and helpers for managing the assembled program and simulator state.
- AssembledProgram.java — canonical in-memory representation of an assembled program, breakpoints, and assembly output.
- Simulator.kt — emulation loop, memory/registers, I/O handling.
- app/src/main/assets/* — assembler and UI HTML/JS used by WebView.

---

Other repository notes

- No CONTRIBUTING.md or other assistant config files detected; this file should be the authoritative Copilot guidance for repository-specific patterns.

---

If you'd like, add MCP server configuration suggestions (e.g., an Android emulator-based testing server) — ask and it can be added.

