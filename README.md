# Orali Money for Android

> _Note:_ This document is intended to be relatively short. Be concise and precise. Assume the reader has no prior knowledge of your application and is non-technical. 

---

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

## Description about the project
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
1. **Installation**  
   - Download the APK file from GitHub or the partner’s distribution link.  
   - Install on an Android device (Android 10+).  

2. **Authentication**  
   - Log in using device-native security (PIN, fingerprint, or Face ID).  

3. **Using core features**  
   - **Send Money:** Tap the “Send” icon → choose recipient (contact/QR) → enter amount with coin/bar pictographs → confirm.  
   - **Receive Money:** Balance automatically updates with a clear visual indicator.  
   - **Request Money:** Tap the “Request” icon → enter amount visually → share request → receive confirmation when accepted.  
   - **Pending Transactions:** Open “Pending” menu to see in-progress transfers. Cancel or wait until they complete/expire.  
   - **Transaction History:** Tap “History” → scroll through chronological, visual records of transfers.  

---

## Development requirements
**Technical prerequisites:**
- **OS:** Android 10+  
- **Language/Frameworks:** Kotlin, Jetpack Compose  
- **Build System:** Gradle (Android Gradle Plugin 8.x)  
- **Dependencies:** AndroidX, Compose UI, Taler client APIs, OIM graphic library  


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
- **Testing:** Each PR is built locally and on GitHub Actions (continuous integration). The Gradle build ensures the code compiles, unit tests pass, and the APK is generated successfully.  
- **Deployment steps:**  
  1. Merge into `main`.  
  2. Gradle generates a signed APK / Android App Bundle.  
  3. The APK is tested on emulators (Pixel 6, Android 13) and physical devices.  
  4. Once validated, the APK is shared with the partner via GitHub Releases or direct distribution.  

### Justification
- **Branching model** keeps `main` stable while allowing rapid development in `wip` and `feature` branches.  
- **Pull-request reviews** enforce accountability, reduce bugs, and ensure accessibility/OIM principles are respected.  
- **Gradle** provides a reliable, industry-standard build system for Android projects, supporting CI/CD integration.  
- **GitHub Actions** automates testing and builds, ensuring issues are caught early.  
- **Partner access to GitHub** guarantees transparency and collaboration with external developers (e.g., Iván for Android, Marc for iOS).  

This workflow balances structure with flexibility, making it easy to track progress, avoid conflicts, and deliver a reliable app.

---

## Coding Standards and Guidelines
We follow **Kotlin style guidelines** and **Google Android best practices**, enforced by `ktlint`.  

- camelCase for variables/methods  
- PascalCase for classes  
- Clear comments and documentation where functionality is non-obvious  

---

**Setup instructions:**
```bash
git clone https://github.com/csc301-2025-f/project-17-orali-money-for-android.git
cd project-17-orali-money-for-android
./gradlew build
./gradlew installDebug

