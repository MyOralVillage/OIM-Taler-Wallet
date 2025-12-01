# Orali Money for Android

## Please note that...

This is an **EXPERIMENTAL IMPLEMENTATION** of the [the GNU Taler Android wallet](https://git.taler.net/taler-android.git/):

Under no circumstances should it be used for real financial transactions, sensitive financial information, nor as a supplement to any existing
GNU Taler Android libraries. **USE AT YOUR OWN RISK, IT MAY BREAK EXISTING GNU TALER ANDROID BUILDS!**

See the [Taler manual](https://docs.taler.net) and [Taler wallet](https://www.taler.net/en/wallet.html)
for more information.


## Contents

- [About](#about)
- [Features](#features)
- [Development Prerequisites](#development-prerequisites)
- [Installation](#installation)
- [Design Rationale](#design-rationale)
- [Automated Testing and Deployment](#automated-testing-and-deployment)
- [Coding Guidelines](#coding-guidelines)
- [Contact](#contact)
- [License](#license)

## About

Orali Money is a collaboration between [GNU Taler](https://www.taler.net/en/index.html) and [My Oral Village](https://myoralvillage.org/) to create a mobile wallet that empowers people traditionally excluded from financial systems.

This repository is responsible for the Android version of Orali Money which has two interfaces: a standard interface adopted from Taler and an interface built on [OIM (Oral Information Management)](https://myoralvillage.org/our-model-oim/) principles, allowing for illiterate and innumerate adults to interact with the application.

## Features

1. Sending Money
2. Receiving Money
3. Transaction History (Column)
4. Transaction History (River)

## Development Prerequisites

- OS: Android 8+
- Language/Frameworks: Kotlin, Jetpack Compose
- Build System: Gradle (Android Gradle Plugin 9.x)
- Dependencies: AndroidX, Compose UI, OIM Graphic Library

## Installation 
### **For non developers**
[Please navigate to our  most recent release page and download one of the debug APKs.](https://github.com/csc301-2025-f/project-9-Orali-Project-for-Android/releases/)

- x86_64 (for Windows emulation)
- arm64 (for native Android phones or MacOS emulation)
- armeabi/x86 (for deprecated Android phones; *this is rarely needed*)

The OIM mode for Android is still in pre-release, therefore there is no official signed release on FDroid/Play store/Nightly as of now. Since full apk optimizations are not run for debugs, the APK sizes are on the larger side (around 40mb).   

### 1. Clone the Repository

<details>
    <summary> By HTTPS </summary>

#### HTTPS

```bash
git clone https://github.com/csc301-2025-f/project-9-Orali-Project-for-Android.git
cd project-9-Orali-Project-for-Android
```

Note that you may need to authenticate with your GitHub credentials.
</details>

<details>
    <summary> By SSH </summary>

#### SSH

```bash
git clone git@github.com:csc301-2025-f/project-9-Orali-Project-for-Android.git
cd project-9-Orali-Project-for-Android
```
</details>

### 2. Build the Project

Ensure the Java Development Kit 17 (JDK 17) is installed before running the Gradle commands below.

<details>
    <summary> Windows (Command Prompt and Powershell) </summary>

#### Windows (Command Prompt)

```cmd
gradlew.bat :wallet:build
```

#### Windows (Powershell)

```powershell
.\gradlew.bat :wallet:build
```
</details>


<details>
    <summary> Linux and Mac </summary>

#### Linux / Mac
```bash
./gradlew :wallet:build
```
</details>

## Design Rationale

This section explains why OIM was built the way it was. The choices here come from a mix of technical constraints, user research in Sierra Leone, hardware realities in the target communities, and restrictions imposed by the backend environment. Each decision reflects a balance between clarity, usability, and practicality.

<details>
    <summary> Concurrency, Performance, and Real-World Devices </summary>

### 1. Concurrency, Performance, and Real-World Devices

OIM is designed for environments where devices are modest and resources are limited. The goal was to build something that behaves the same way on a modern phone and on older devices such as **Tecno Spark 7**, **Itel A56**, **Tecno Pop series**, **Samsung J2**, **Samsung A10**, and similar models that are still heavily used across West Africa. Many of these phones run with 1 GB or 2 GB of RAM, limited CPU power, and storage that becomes slow when the device fills up. These constraints shaped the entire architecture.

#### Minimal Threading for Predictability

The app uses a mostly single thread model, because the dataset is small and does not justify multiple threads. A simple architecture works better when the hardware is not powerful.

* It avoids complicated coordination between coroutines.
* Lifecycle events are easier to reason about.
* UI updates always occur in a predictable sequence.
* There is less chance of race conditions or partial updates.
* Debugging becomes straightforward because logic runs in a single flow.

This approach reduces surprises and ensures that the interface feels stable even on phones that struggle under heavier workloads.

#### How Older Phones Influence the Design

In the communities where OIM will be used, users often rely on slow devices that are several years old. These devices typically have:

* limited RAM
* slower multi core processors
* weak GPU pipelines
* slow eMMC storage
* fragile thermal performance

Running several asynchronous tasks at once can cause stutters or frame drops. Even lightweight background work can cause delays if the device is already under strain from Android services or Google Play components. Keeping OIM lightweight avoids these issues and keeps the experience smooth.

#### Safe Use of Background Work

Some operations still run off the main thread, such as small parsing tasks or loading local note images. However:

* these are short lived
* they never involve heavy computation
* they avoid adding pressure to the system

This maintains consistent rendering and protects the user from slowdowns.

</details>

<details>
    <summary> The River View and Metaphor Driven Visualization </summary>

### 2. The River View and Metaphor Driven Visualization

The river view is one of the most distinctive features of OIM. It comes from extensive research performed by our partners in Sierra Leone who studied how individuals with lower levels of literacy or numeracy understand money. Traditional interfaces filled with numbers, charts, or lists create unnecessary barriers. Highly visual metaphors remove those barriers.

#### Why a River

Money behaves like something that moves, divides, or accumulates. A river already communicates this naturally. It lets users understand financial activity by drawing on everyday environmental knowledge.

* A river can gain water.
* It can lose water to streams or branches.
* It can form pools or lakes.
* It changes shape over time.

This makes the metaphor powerful and easy to grasp.

#### Farms as Incoming Money

Incoming transactions are represented as farms. Farms are associated with growth, harvest, and gain. When money arrives, the landscape grows, and the user receives something valuable. This framing helps users understand positive inflow without requiring them to read numerical labels.

* farm size scales with transaction amount
* placement shows when the transaction occurred
* tapping opens the exact transaction

#### Lakes as Outgoing Money

Outgoing amounts create lakes. A lake is formed by water leaving the main river and gathering elsewhere. This mirrors the idea of money being sent away.

* lake size grows with the transaction
* the shape and placement match the timeline
* each lake opens its corresponding transaction

#### Visual Interaction

To support different literacy levels, the river view avoids textual dates. Instead, symbolic date markers appear above each item so users can see approximate timing without needing to read. Every element on the river can be tapped so the visual metaphor stays connected to real financial data. This ensures that the visual storytelling always reflects actual transaction history.

</details>

<details>
    <summary> Landscape Only Layout </summary>

### 3. Landscape Only Layout

OIM does not support portrait mode, and this is intentional. The river metaphor needs horizontal space to breathe. On narrow portrait screens, the metaphor collapses and the UI becomes cramped.

#### Why Landscape Is Necessary

* Farms, lakes, and the river require width for clear spacing.
* A horizontal layout offers long uninterrupted movement.
* Tap areas stay large and easy for users to hit.
* The banknote stacks and money related visuals remain readable.
* Layouts look consistent across cheap and expensive phones.

Because everything in the app is designed around spatial storytelling, portrait mode would break the core experience.

#### Switching Behavior

The main wallet app remains in portrait. Only when the user enters OIM does the device rotate into landscape. Leaving OIM returns the device to portrait. This separation keeps the main app simple while giving OIM the space it needs.

</details>

<details>
    <summary> Backend Limitations and On Device Processing </summary>

### 4. Backend Limitations and On Device Processing

The backend that powers OIM is managed by partner organizations and cannot be modified. The OIM team could not change any schema, add metadata, or restructure responses. This imposed several limitations.

#### What Could Not Be Changed

* backend response format
* transaction structure
* available fields
* metadata support
* categorization
* additional endpoints

Since the backend could not be touched, the app performs all computation for the river view on the device itself.

#### What the Device Computes

* transaction grouping
* scaling
* layout logic
* metaphor mapping
* date and time translation
* amount based sizing

The river view works fully offline using only the minimal transaction data provided. This allows total independence from backend capabilities while still maintaining visual richness.

</details>

<details>
    <summary> Why the App Does Not Use AI </summary>

### 5. Why the App Does Not Use AI

There were several reasons why AI was intentionally excluded, even though it could help with categorization and insights.

#### No Backend Compute

The backend environment does not have the CPU or GPU capacity to run even small AI models. There was no space to deploy transformers or even lightweight models.

#### Running AI on the Device Is Not Practical

Most users rely on low budget devices that cannot support quantized neural networks. Running inference on these devices would lead to:

* slow interfaces
* memory exhaustion
* thermal issues
* battery drain

The risk outweighed the benefit.

#### No AI Ready Data

Because backend endpoints cannot be changed, the app receives no metadata about:

* transaction type
* category
* merchant
* labels
* description structure

AI systems need structured data to generate meaningful insights. Without it, they would perform poorly.

#### Stability Comes First

The project prioritizes predictable behavior. Heavy AI computation contradicts that goal. Simplicity makes the application more reliable.

</details>

<details>
    <summary> Architecture and Code Structure </summary>

### 6. Architecture and Code Structure

The app uses an architecture that emphasizes clarity and maintainability.

#### Separation of Responsibilities

* The MainViewModel manages state and data.
* Screens display the UI and contain no business logic.
* Composable components are reusable and lightweight.

This creates a clean, understandable flow for future contributors.

#### Reusable UI Components

The app uses unified components across multiple screens.

Examples include:

* stacked note renderer
* notes gallery overlay
* amount parser
* transaction mapping utilities

Whenever assets or logic change, these updates propagate everywhere automatically.

#### Resource Mapping

All currency, note denominations, and transaction purposes map through a single place. This prevents errors and simplifies updates.

#### Mock Database

During development, the biggest structural limitation was the fact that the backend environment could not be consistently accessed, modified, or extended by the OIM engineering team. The partner organizations controlled the database schema, API endpoints, migration schedule, and infrastructure uptime. This created a situation where development needed to proceed at full speed without depending on backend availability.

Because of this, OIM required a **fully standalone, schema-accurate mock database** that could act as a drop-in replacement for the real database during development. The motivation for this choice was much deeper than simply wanting “offline testing”. It emerged from a combination of practical, operational, and architectural needs.

##### Ensuring Development Continuity

The backend environment was frequently inaccessible due to:

* limited uptime during certain hours
* shared testing environments with other partner teams
* network instability when working from field locations
* maintenance windows and database resets outside our control

Without a local mock database, UI development would have stalled for days at a time. The mock DB ensured that:

* developers could continue working when the real backend was offline
* UI prototypes were testable even during infrastructure outages
* QA could reproduce cases without needing to simulate server resets
* regressions could be caught without relying on backend data

This decision protected the development timeline and allowed continuous iteration.

##### A Controlled and Repeatable Data Environment

The mock database also provided a stable sandbox. The real backend could not guarantee:

* consistent test data
* stable transaction IDs
* reproducible states for payment flows
* fixed balances for predictable screenshots

The mock DB made it possible to create:

* deterministic test scenarios
* predictable transaction histories
* consistent wallet states
* reliable edge-case reproduction (zero balance, large inflows, rapid sends, etc.)

This was essential for building and debugging features like the river view, note stacking animations, and consolidated purpose selection.

##### Compatibility With the Real Schema

The mock database was engineered to **mirror the real schema exactly**, including:

* field names
* field types
* table structures
* indexing assumptions
* timestamps
* transaction direction conventions

This ensured that once the backend became available:

* integration required almost no changes
* switching from mock → real DB was a one-line swap
* every piece of UI logic already matched the real data model
* no mass refactoring was necessary

The mock DB therefore served as a long-term investment in stability, not just a temporary convenience.

##### Enabling Visual Feature Development Without Backend Support

Several features in OIM, such as:

* the river view
* scaling farms and lakes
* stacked note consolidation
* amount mapping into denominations
* date icon assignment

all required **derived** or **aggregated** data that the backend did not provide.

The mock DB allowed local generation of:

* synthetic transaction histories
* large sequences of inflows and outflows
* stress test cases
* unusual patterns (e.g., clustered sends, missing purposes, extreme amounts)

Without this, it would have been impossible to test the visual design under realistic workloads.

##### Protecting the Backend From Accidental Load

During development, UI screens refresh state frequently, especially when testing animation loops or rapid interactions. Running these tests directly against the real backend could have:

* created unnecessary load
* polluted production-adjacent logs
* generated accidental transactions
* triggered rate-limit or throttle protections
* introduced audit noise for partner teams

The mock DB acted as a safety barrier, absorbing all local experimentation and ensuring the backend remained stable and clean.

##### Future Integration Safety

Because the mock DB is **structurally identical** to the partner database, any future backend improvements or migrations will only require:

* updating a single mapping layer
* reusing all existing UI logic
* leaving the composables and ViewModel untouched

This makes the architecture long-lived and maintainable.
</details>

<details>
    <summary> Visual Choices and Currency Representation </summary>

### 7. Visual Choices and Currency Representation

Some UI decisions came directly from partner research in Sierra Leone. These choices reflect what users in the region already understand.

#### Partner Driven Design

Partners provided representations for:

* common note combinations such as forty being shown as twenty stacked on twenty
* how values should be grouped visually
* how amounts should scale
* how certain payment concepts should be represented

These decisions came from observing real user behavior in the field.

#### Why TESTKUDOS Uses SLE Notes

No assets were supplied for TESTKUDOS. Using generic placeholders would confuse users and break the visual language. SLE notes already fit the user’s mental model and the app’s visual structure. Reusing SLE notes created a consistent experience without introducing noise or ambiguity.

</details>

## Automated Testing and Deployment

This repository does not have automated testing. The Taler wallet depends on legacy code to run, and large scale unit testing was out of the scope of this project.

Orali Money contains developer releases on this repository and will have official releases on the F-Droid store.

## Coding Guidelines

We follow [Kotlin style guidelines](https://developer.android.com/kotlin/style-guide) and [Android Best Practices](https://developer.android.com/topic/architecture). In addition, we use [ktlint](https://github.com/pinterest/ktlint).

## Contact

Our partners are **My Oral Village** and **Taler Systems**:

- Brett Matthews (Founder, My Oral Village) – [Primary Partner Contact]
- David Myhre (Director of Partnerships, My Oral Village)
- Iván Ávalos (Android Developer, Taler Systems)
- Marc Stibane (iOS Developer, Taler Systems)

My Oral Village is a nonprofit dedicated to creating financial tools for illiterate and innumerate populations worldwide.

Taler Systems is the developer of GNU Taler, an open-source, privacy-preserving payment system.

## License

- This project is licensed under the [GNU General Public License](https://www.gnu.org/licenses/gpl-3.0.en.html) either version 3.0 or (at your option) any later version.  
