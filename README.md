# *Please note that...* 

## this repo extends the GNU Taler Android Repository

This git repository contains **UNTESTED** code for the GNU Taler Android wallet app,
non app-specific GNU Taler Android libraries, and an implementation of the *OIM Salome* pictographic library. (**verify official name**).

This is an **EXPERIMENTAL IMPLEMENTATION** of the GNU Taler Android wallet:

    https://git.taler.net/taler-android.git

See the [Taler developer manual](https://docs.taler.net/developers-manual.html#build-apps-from-source).
for more information.

## this version is *PURLEY A PROOF OF CONCEPT!* 

Under no circumstances should it be
used for financial transactions, sensitive financial information, nor as a supplement to any existing
GNU Taler Android libraries. **USE AT YOUR OWN RISK, IT *WILL* BREAK EXISTING GNU TALER ANDROID BUILDS!**

## this version has known issues:

- User preferences (selected scope, action button state) are stored in memory only
  + Preferences will be lost when app restarts
  + This is a temporary workaround - proper persistence will be added in future versions
- Not compatible with GNU Taler Android 
  + list issues here 
  + and more issues
  + proposed solutions?
- Gradle version (possible upgrade?)
- Database is hardcoded 
- idk list more?
- idk what else to add lmao

---
# Building and Structure

## Setup instructions

### 1. Clone the Repository

#### HTTPS Method
```bash
git clone https://github.com/csc301-2025-f/project-9-Orali-Project-for-Android.git
cd project-9-Orali-Project-for-Android
```

#### SSH Method
```bash
git clone git@github.com:csc301-2025-f/project-9-Orali-Project-for-Android.git
cd project-9-Orali-Project-for-Android
```

### 2. Build the Project

#### Linux / Mac
```bash
./gradlew :wallet:build
```

#### Windows (Command Prompt)
```cmd
gradlew.bat :wallet:build
```

#### Windows (PowerShell)
```powershell
.\gradlew.bat :wallet:build
```

### Notes
- For SSH method, ensure you have SSH keys configured with GitHub
- For HTTPS method, you may need to authenticate with your GitHub credentials
- Make sure you have Java Development Kit 17 (JDK 17) installed before running Gradle commands

## Project Structure

* [**taler-kotlin-android**](/taler-kotlin-android) - a refactored version of the taler-koltin-android library
* [**transaction-database**](/transaction-database) - a complete, locally saved SQLite database for transaction histories
* [**wallet**](/wallet) - the GNU Taler wallet Android app with OIM UI and transaction history lookup

---
# Orali Money for Android

## Partner Intro
Our partners are **My Oral Village** and **Taler Systems**.

**Contacts:**
- Brett Matthews (Founder, My Oral Village) – [Primary Partner Contact]
- David Myhre (Director of Partnerships, My Oral Village)
- Iván Ávalos (Android Developer, Taler Systems)
- Marc Stibane (iOS Developer, Taler Systems)

**About the organizations:**  
My Oral Village is a nonprofit dedicated to creating financial tools for illiterate and innumerate populations worldwide.  
Taler Systems is the developer of GNU Taler, an open-source, privacy-preserving payment system.  
Together, they are collaborating to build Orali Money: a mobile wallet that empowers people traditionally excluded from financial systems.

---

## Description of the project
**Orali Money for Android** is a mobile wallet designed to make money management accessible for illiterate and innumerate adults.

It enables users to send, receive, and request money through a clear, icon-based interface.  
The core value lies in its **Oral Information Management (OIM)** system, which replaces text-heavy interfaces with intuitive visuals.

**Problem:** Nearly one billion people are excluded from digital finance because they cannot read numbers or text.  
Orali Money provides a safe, inclusive financial tool that addresses this barrier.

---

## Key Features
- **Send Money** – Transfer money securely using icons and colors, with minimal text.
- **Receive Money** – Users get clear notifications when funds arrive, shown visually in their balance.
- **Request Money** – Ask others for money using simple icons, coin stacks, or bar visuals.
- **Pending Transactions** – Keep track of transfers that haven’t yet been “picked up,” with the ability to cancel or wait until they expire.
- **Transaction History** – Chronological and visual record of past activity, designed for easy comprehension.
- **Shareability** – Literate users can easily share the app with loved ones, who can learn to use it in under a week.
- **Error Handling** – Errors are conveyed with clear icons and audio cues.
- **Onboarding/Demo** – First-time users are guided through a walkthrough demo transaction.

---

## Instructions

*TODO* 

---

## Development requirements
**Technical prerequisites:**
- **OS:** Android 8++
- **Language/Frameworks:** Kotlin, Jetpack Compose
- **Build System:** Gradle (Android Gradle Plugin 9.x)
- **Dependencies:** AndroidX, Compose UI, OIM graphic library

---
## Deployment and GitHub Workflow

### Branching Strategy
- **main** – contains only stable, production-ready code. Nothing is merged into `main` without peer review and partner approval.
- **wip_<feature>** – work-in-progress feature branches (e.g., `wip_D1`, `wip_send-money`). Used for ongoing development.
- **feature/<name>** – branches created by team members to implement specific features (e.g., `feature/request-money`).
- **fix/<issue>** – branches for bug fixes.

This structure ensures code is always traceable and organized by purpose.

### Workflow
1. A developer creates a new branch off of `wip_<feature>` or `main` depending on the scope of work.
2. Code is committed locally and pushed to the corresponding branch on GitHub.
3. Once ready, the developer opens a **pull request (PR)**:
  - PRs are made from `feature/*` or `fix/*` into `wip_<feature>` branches.
  - After internal review, a PR from `wip_<feature>` into `main` is created.
4. **Review process:**
  - At least one teammate reviews the PR for correctness, readability, and alignment with project goals.
  - Larger changes (UI flows, OIM compliance) require two reviewers, ideally including the partner liaison.
5. **Merging:**
  - Once approved, the branch is merged into its target.
  - Only designated leads  merge into `main`.

### Deployment Process
- **Build Tool:** Gradle (Android Gradle Plugin). Used for dependency management, modular builds, and APK generation.
- **Testing:** Each PR is built locally and on GitHub Actions (continuous integration). 
- The Gradle build ensures the code compiles, unit tests pass, and the APK is generated successfully.
- **Deployment steps:**
  1. Merge into `main`.
  2. Gradle generates a signed APK / Android App Bundle.
  3. The APK is tested on emulators (Pixel 6, Android 16) and physical devices.
  4. Once validated, the APK is shared with the partner via GitHub Releases or direct distribution.

### Justification
- **Branching model** keeps `main` stable while allowing rapid development in `wip` and `feature` branches.
- **Pull-request reviews** enforce accountability, reduce bugs, and ensure accessibility/OIM principles are respected.
- **Gradle** provides a reliable, industry-standard build system for Android projects, supporting CI/CD integration.
- **GitHub Actions** automates testing and builds, ensuring issues are caught early.
- **Partner access to GitHub** guarantees transparency and collaboration with external developers(e.g., Iván for Android, Marc for iOS).

This workflow balances structure with flexibility, making it easy to track progress, avoid conflicts, and deliver a reliable app.

---

## Coding Standards and Guidelines
We follow **Kotlin style guidelines** and **Google Android best practices**, enforced by `ktlint`.

- camelCase for variables/methods
- PascalCase for classes
- Clear comments and documentation where functionality is non-obvious



