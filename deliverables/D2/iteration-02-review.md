# Team 9 - FooBaz

## Iteration 1 - Review & Retrospect

 * When: 2025-10-21
 * Where: Google Meets, synchronous

## Process - Reflection


#### Q1. What worked well

- Constantly Updated Lucid UML Diagrams  
Maintaining and regularly updating our **Lucid UML diagrams** worked extremely well. It kept everyone aligned on the system architecture, helped us plan integrations, and made it easier to visualize dependencies between classes. This practice reduced confusion when connecting different modules and made integration smoother.

- Clear Ownership and Tiered User Story Management  
Each team member was responsible for **one user story**, while a few team leads managed **two user stories each under the project manager**. This tiered structure encouraged accountability and strong coordination. It also made it easier for the project manager to monitor progress and identify bottlenecks early.

- Bug Tracking via Dedicated Discord Channel  
Having a **dedicated Discord channel** for bugs proved highly effective. Team members reported bugs as soon as they were found, explained how they were fixed, and discussed potential causes. This real-time tracking prevented repeated issues and acted as a lightweight bug log without redundant documentation.

- Multi-Platform Communication (WhatsApp + Discord)  
Using both **WhatsApp and Discord** worked well for different purposes. WhatsApp for quick updates and scheduling, and Discord for technical collaboration. This dual setup made communication efficient and context-appropriate.

- Version tracking and avoiding merge conflicts
  We have been very careful about how we manage push/pull requests and so far it has been working very smoothly

#### Q2. What did not work well

List **process-related** (i.e. team organization and how you work) decisions and actions that did not work well.

### 1. Inconsistent Use of GitHub Issues  
We used **GitHub Issues** for task tracking, but inconsistently. Since priorities and features often shifted, issues weren’t always updated or closed properly. This made it hard to gauge actual progress and occasionally caused confusion about who was responsible for what.

### 2. Fragmented Gradle Dependencies and Asset Management  
Initially, each developer used separate **Gradle dependencies** and stored assets individually, with plans to merge later. This caused **integration problems, version mismatches, and duplicated files**. Unifying dependencies and asset structure earlier would have prevented these issues.

### 3. Delayed Integration Testing  
Integration testing was **postponed until multiple modules were completed**, leading to complex debugging later. Conducting smaller, more frequent tests throughout development would have caught issues earlier and reduced last-minute fixes.

### 4. Informal Task Tracking Outside Version Control  
Some smaller tasks were tracked only through **chat messages** rather than GitHub or shared documentation. While convenient, this caused occasional overlap or missed responsibilities. A structured workflow where all updates are logged in version control would have improved coordination.

### 5. Unclear partner roles, and lack of communication
We have a primary partner and a secondary partner which created conflicts in how we were designing our codebase, how the secondary partner wants it designed, and how our primary partner wants it designed. For example, we were approved on a repository that was five years out of date, causing two weeks delay for reintegrations, and less time was available for testing and polishing our work.  

#### Q3(a). Planned changes

List any **process-related** (i.e. team organization and/or how you work) changes you are planning to make (if there are any)

- The team lead will be more attentive on checking up and following up with team members
- We will move away from frequent meetings to more informal "status updates" with meetings twice a week
- The current hierarchy in regards to our primary partner needs clarification; we will ensure that we know exactly who to report to in our next meeting with the partner
- Our next steps need to be firmly agreed upon with the partner before we move on with building code


#### Q3(b). Integration & Next steps
Briefly explain how you integrated the previously developed individuals components as one product (i.e. How did you split the work across your team) and how was the work integrated together.

We split work by user stories (Send, Receive, Transactions, GUI) and assigned them to different members. Each part was developed on separate branches, then merged into the main project, with integration coordinated through UML diagrams and meetings to resolve any conflicts (though none arose).

Our next steps are trying to integrate what we have with 1) our main patener's goals and 2) our secondary partner's existing code base. We will therefore focus on integrating our code into their existing architecture.

## Product - Review

#### Q4. How was your product demo?
Our demo went well and we delivered a working demonstration of the user stories. However, our main partner was not able to attend due to scheduling conflicts.We are currently facing challenges with integrating our solution into our other partner's existing codebase, and trying to deliver on our main partener's goals. We are actively working to address these integration issues through several approaches: conducting code audits, holding frequent technical sync meetings with their development team, and exploring alternatives which minimize changes to their existing systems. Despite these challenges, the feedback from attendees were positive, and we remain committed to finding a viable integration path forward. We've scheduled a follow-up session with our main partner to review the demo recording and discuss next steps for the integration work.
