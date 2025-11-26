# D2 - Project Review

### Project Name: Savi Finance
### Team Name, Team Number: Fantastic Seven, 8

## Project Summary
The Consumer AI Phone Concierge is an AI-powered web application designed to save time for individuals and small businesses by automating routine phone tasks related to financial management. Each user is assigned a dedicated phone number connected to an intelligent voice agent that can answer and place calls, gather essential details (names, dates, amounts, reference numbers), and escalate to a human when necessary. After each call, the system stores transcripts and sends concise summaries via email or the web app. Users can also customize greetings, voice tone, manage their assigned number, and review call histories.

The project’s primary contribution is an end-to-end automated phone system tailored for financial tasks, such as confirming balances, due dates, and appointments. Additional contributions include operational tooling (call histories, billing CSV export, number management), per-user configuration, and a production-ready deployment workflow via Vercel and Railway. Overall, the project aims to reduce stress, improve productivity, and create clarity in financial interactions.

While the project aims to reduce routine call burdens, the justification could be stronger—particularly regarding its impact on productivity versus pre-existing solutions in a saturated AI phone agent market. Additionally, the financial-specific advantages, compliance, and privacy considerations could be elaborated further to highlight differentiation.

## Introduction & Overview of the Problem and Product
The project addresses the problem of repetitive, low-value phone calls that consume significant time, create stress, and contribute to inefficiencies in financial workflows. Traditional voicemail systems and call forwarding only pass along messages; in contrast, the AI concierge processes conversations, extracts key information, summarizes discussions, and knows when to escalate calls to a human. This structured approach ensures nothing important is missed and aligns with Savi Finance’s mission to reduce financial stress.

**Strengths:**
- Clear explanation of the problem and product purpose.
- Focus on practical benefits: saving time, reducing stress, and improving decision-making.
- Differentiation from basic automation systems by handling intent recognition and structured summaries.

**Suggestions for improvement:**
- Provide context on initial target users and common call types.
- Include measurable success criteria (e.g., reduced missed calls, faster responses).
- Highlight financial compliance, privacy measures, and technical details that make it suitable for financial use cases.
- Clarify how it differs from other AI phone agents in the market.

## Project Deployment
Deployment instructions are generally clear and easy to follow, allowing users to access the web app, log in with a test account, and explore its functionality. The app loads smoothly, and the walkthrough aids navigation to the main interface.

**Strengths:**
- Clear README instructions and demo access.
- Explanation of bypassed subscription processes and test account usage.
- Production-ready deployment using Vercel/Railway with CI workflows (Nx checks, PR reviews).

**Issues and suggestions:**
- Incoming calls were non-functional during testing.
- Some features were labeled “coming” or “incoming” without clear indications that they were incomplete.
- A troubleshooting section for setup issues (login errors, slow responses) would be helpful.
- Adding a QR code for easier mobile access could improve usability.

Overall, deployment is manageable for new users and aligns with real-world software setup expectations.

## Product Functionality
The app’s main features are intuitive, accessible, and well-organized. Key functionalities include:
- Inbound and outbound call automation.
- Agent creation and customization.
- Call transcripts and automatic email summaries.
- Clean UI with dashboards suitable for business users.

**Strengths:**
- Smooth and responsive interface.
- Layout and color scheme convey credibility for financial applications.
- Basic operational tools, such as call histories, are easy to navigate.

**Identified issues:**
- Incoming calls do not work, causing failed call attempts.
- Creating a new agent or demo calls sometimes logs the user out.
- Some features (outbound scheduling, customizable FAQs) are not yet implemented.

**Suggested improvements:**
- Add search/filter functionality for call histories.
- Implement spam call detection and categorization.
- Improve “demo mode” stability to allow safe exploration without logging out.
- Add loading indicators for transcripts and summaries to improve UX.

## Project Documentation
The documentation is generally well-written, logically structured, and provides a clear overview of the project, setup instructions, and demo access.

**Strengths:**
- Readable and approachable for new users.
- Provides both textual and video walkthroughs.
- Explains purpose, features, and technical setup clearly.

**Suggestions for improvement:**
- Explicitly indicate which features are placeholders or not yet implemented.
- Include a summary of the tech stack and deployment tools for developers.
- A simple flow diagram of the user journey could enhance clarity.

Overall, the documentation supports effective onboarding and testing while remaining professional and comprehensive.

## Miscellaneous Feedback
- The project demonstrates thoughtful integration of technology to address real human needs.
- Potential future enhancements include accessibility features (text-to-speech speed control, voice customization) and metrics for user impact (average call time saved, satisfaction scores).
- Emphasis on financial sector compliance and privacy would strengthen credibility.
- Overall, the project is creative, purposeful, and has strong potential for growth.

---

# Amish
## D2 - Project Review

Please fill in the template below. Some suggestions have been provided to help you understand how to answer each segment.

Before submission, please replace the suggestions (in italics) with your answers. You can keep the prompts though.

### Project Name: _Enter project name_
### Team Name, Team Number: _Enter team name and number who the project belongs to_

### Project Summary:
_Provide a short summary of the project. Capture details like what problem it aims to solve and what are the key contributions of the project._

The Consumer AI Phone Concierge by the Savi Finance team is designed to reduce the time and stress individuals and small businesses spend on repetitive phone calls. Instead of manually confirming balances, due dates, or basic business information, users get a dedicated phone number linked to an AI voice agent. This agent can handle inbound calls, make outbound calls to gather details, escalate when needed, and provide clear summaries of each interaction. By automating these routine tasks, the project directly supports Savi Finance’s mission of easing financial stress and making money management more intentional.

### Introduction & Overview of the problem and the product
_* Were you able to understand clearly the problem that the project is trying to solve?_

_* Were you able to get an overview of what the project is about?_

_* Were you able to differentiate what the project does and how it differs from existing or prior work?_

_* Any points that would have helped you to get more information on this subject?_

The README clearly explained the problem the project is trying to solve, which is saving time on repetitive phone calls and reducing stress for both individuals and small businesses. The overview of the product was straightforward, and I understood how the AI phone concierge connects to Savi Finance’s goal of making money management simpler and more intentional. I also found it easy to see how this project differs from typical budgeting apps, since it focuses on automating everyday phone tasks rather than just providing planning or tracking tools.

*One point that could have made the overview even stronger would be a short summary section at the top to tie everything together, since the README is quite detailed and can feel long at first glance.* Overall, the documentation gave a clear picture of both the problem and the product.

### Project Deployment
_* Were you able to follow the instructions and install the application on your device?_

_* Did the application run without any errors?_

_* Any points that would have helped to improve this aspect?_

The Savi Finance team’s web app was deployed on Vercel and worked smoothly during testing. The provided link led directly to the authorization screen and the application ran without errors or crashes. The team did a great job ensuring that the app was easy to access and explore, with clear instructions in the README. I also appreciated how the README explained both the functionality (inbound/outbound calling, transcripts, customization) and the development workflow. *One possible improvement would be to add a QR code for quicker access on mobile devices*, but overall the project was accessible, well-structured, and aligned with the partner’s goals.

### Product functionality
_* How easy and accessible were the key features of the application?_

_* Highlight any bugs or major issues that you could identify_

_* Is the UI of the application representative of a product similar to this project?_

_* What additional functionalities or improvements could result in a better product?_

The app was easy to set up and use. Logging in, subscribing, and creating an agent all worked smoothly with no errors. Navigating the main page was also straightforward, and the UI felt professional and aligned with what you’d expect from a product like this. *The main issue was with inbound calls, which disconnected instantly, and there was also a bug where clicking a voice agent in the user profile sent me back to the authorization page.* Most of the advanced features like managing agents, customizing FAQs, outbound calls, and after-call emails are still incoming, but once those are implemented and the current bugs are fixed, the product will feel much more complete.

### Project Documentation
_* Was the project readme easy to follow and gives all the necessary details about the project?_

_* For a new user (such as yourself), how approachable and easy is it to read and follow the readme?_

_* Are there technical or major writing errors that prevents a good understanding or leads to confusion?_

_* Do you think there are gaps in the documentation or points that could have improved its quality?_

The README was clear and straightforward, with all the necessary details about the partner, the problem being addressed, and how to set up and use the application. As a new user, I found it approachable because the step-by-step instructions made the setup process easy to follow. The overview also did a good job of explaining the main features and workflow, which helped me understand the project without confusion.

There were no major technical or writing errors that interrupted understanding. *The only thing that could improve the documentation is making it slightly more concise, since the length might feel overwhelming at first.* Overall, the README provided all the details I needed to test the app successfully.

### Misc.
_* Are there any other points that you want to comment on / give your feedback?_

_* These could be points that were not captured above, but you think will help to improve the product quality._

The overall flow of the application was smooth, and the deployment link made it simple to access and test without extra setup. The README also balanced technical details with user instructions well, which made it approachable. *One thing I noticed is that the UI felt a bit “AI-ish” because of the shadows and iconography, which gave it more of a machine-generated feel. Using a different design style or more human-centered iconography could make the product feel warmer and more relatable for users*. Along with fixing small bugs and rolling out the planned features, this kind of design tweak would help polish the overall experience.

---
# Anish
## Project Summary
The Consumer AI Phone Concierge project introduces an AI-powered assistant that handles routine phone tasks for individuals and 
small businesses. It provides each user with a dedicated phone number and a smart voice agent that can answer and make calls,
gather essential information such as names, dates, and amounts, and send short summaries by email with clear next steps. The project addresses
the problem of people spending too much time on repetitive phone calls that
add little value and contribute to stress and lost productivity. Its goal is to simplify
communication, reduce missed calls, and support better financial management by connecting automation to real human needs.

## Introduction & Overview of the Problem and the Product
The project focuses on a common problem that affects both individuals and small businesses. People spend a surprising amount of time
on routine phone calls that do not create much value yet still cause stress and distraction. Many hours are lost each week confirming balances,
checking due dates, or verifying appointments, which can lead to unnecessary frustration and financial strain. The AI Phone Concierge 
aims to solve this problem by giving every user a dedicated phone number linked to an intelligent voice assistant that can answer and make calls
on their behalf. The assistant listens carefully, collects important details such as names, dates, and amounts, and sends a short summary by
email with clear next steps. When an issue requires personal attention, the agent knows when to escalate the call to a human so that nothing important is missed.

What makes this project stand out is how it goes beyond simple automation. Traditional voicemail systems and call forwarding 
only pass along messages, leaving the user to sift through information later. The AI concierge, on the other hand, understands intent,
processes the conversation, and extracts information in a structured way. It can summarize what was discussed, keep a record of the call,
and help users act faster. The focus on reducing financial stress and improving everyday decision-making also connects it closely 
to Savi Finance’s broader goal of aligning money management with personal goals. This is not just about saving time but about 
creating clarity and peace of mind.

To make the overview even stronger, the project could include more context on who the first users will be and which types of calls they handle most often.
It would also be helpful to outline what kind of information the system captures, how privacy and consent are managed, and what success looks like in
measurable terms such as reduced missed calls or faster response times. With those details, the project would communicate not only what it does but
also how it makes a meaningful difference in people’s daily lives.

## Project Deployment
The deployment instructions are clear and detailed. Access to the demo site is well described, and the explanation of how to log in with a test
account makes setup simple for anyone testing the system. The web application loads smoothly, and the walkthrough helps users reach the main
interface quickly. The bypassed subscription process is explained clearly, which helps prevent confusion during testing. It would be helpful to
include a short troubleshooting section for common setup issues such as login errors or slow response times. A note clarifying that each account is
linked to one Twilio number would also make this aspect more transparent. Overall, deployment feels manageable for a new user and reflects
a real-world software setup.

## Product Functionality
The main features are accessible and intuitive. The interface is clean and presents the key sections, such as inbound and outbound call history,
in a way that feels natural to explore. Creating an agent and viewing transcripts works smoothly, and the automatic email summaries add a professional touch.
The layout and color choices give the impression of a credible financial tool. There are still a few features listed as “coming soon,”
such as outbound scheduling and customizable FAQs, which are understandable at this stage of development. It would strengthen the product to expand the settings
panel and add small indicators to show when a call summary or transcript is loading. Overall, the experience shows polish and potential for real users.

## Project Documentation
The documentation is well written and easy to follow. The structure is logical and reads like a real-world README, explaining both the purpose
of the app and the technical setup. The steps are listed clearly, and the inclusion of access details and demo login credentials makes it easy for reviewers
to test. There are no major writing or technical errors that create confusion. It might help to add a short section summarizing
the tech stack and deployment tools so developers understand the backend and frontend connection at a glance. A short flow diagram
of the user journey could also make the setup more visual. Overall, the documentation is informative, professional,
and well aligned with the project’s goals.

## Miscellaneous
The project has a thoughtful concept that connects technology to a real human need. The focus on reducing stress and saving
time makes the product meaningful rather than purely technical. Future iterations could include metrics such as average call time saved or user
satisfaction to highlight impact. Accessibility features such as text-to-speech speed control or voice customization could 
also make the product more inclusive. Overall, this is a creative and well-executed project with a strong sense of purpose and a clear vision for growth.

---
# Fares
## D2 - Project Review

Please fill in the template below. Some suggestions have been provided to help you understand how to answer each segment.

Before submission, please replace the suggestions (in italics) with your answers. You can keep the prompts though.

### Project Name: _Enter project name_
### Team Name, Team Number: _Enter team name and number who the project belongs to_

### Project Summary:
_Provide a short summary of the project. Capture details like what problem it aims to solve and what are the key contributions of the project._

The Consumer AI Phone Concierge automates routine phone calls for individuals and small businesses, focusing on financial tasks like confirming balances, due dates, and appointments. Users get a dedicated phone number linked to an AI agent that can handle calls, extract key information, escalate to a human if necessary, and provide concise summaries. The main contribution is reducing the time and stress spent on repetitive calls while providing accurate, structured information for better financial management.

### Introduction & Overview of the problem and the product
_* Were you able to understand clearly the problem that the project is trying to solve?_

_* Were you able to get an overview of what the project is about?_

_* Were you able to differentiate what the project does and how it differs from existing or prior work?_

_* Any points that would have helped you to get more information on this subject?_

The README made it clear that the project’s purpose is to minimize wasted time on repetitive phone calls and improve productivity. I understood how the AI concierge works, and the distinction from typical budgeting or tracking apps was clear: this focuses on handling the phone-based workflow rather than passive data tracking.  

*The overview could have been stronger if a few example call scenarios were highlighted, showing the AI’s capabilities versus traditional voicemail or human assistants.* Overall, it was easy to understand the problem and the solution.

### Project Deployment
_* Were you able to follow the instructions and install the application on your device?_

_* Did the application run without any errors?_

_* Any points that would have helped to improve this aspect?_

The deployment instructions were clear, and accessing the web app was straightforward. The app ran smoothly in the browser, and the test account worked without issue.  

*One suggestion is to include a “troubleshooting” section for any login errors or features that are temporarily unavailable.* This would help new users avoid confusion during setup.

### Product functionality
_* How easy and accessible were the key features of the application?_

_* Highlight any bugs or major issues that you could identify_

_* Is the UI of the application representative of a product similar to this project?_

_* What additional functionalities or improvements could result in a better product?_

Key features like agent creation, call summaries, and dashboard navigation were intuitive and accessible. The UI felt professional and aligned with the financial domain.  

**Bugs/issues:**  
- Incoming calls did not connect during testing.  
- Clicking certain options in the profile sometimes redirected unexpectedly.  

**Suggestions:**  
- Implement search and filtering for call history.  
- Stabilize demo calls and test environments.  
- Add visual indicators for in-progress calls or transcript generation.  

### Project Documentation
_* Was the project readme easy to follow and gives all the necessary details about the project?_

_* For a new user (such as yourself), how approachable and easy is it to read and follow the readme?_

_* Are there technical or major writing errors that prevents a good understanding or leads to confusion?_

_* Do you think there are gaps in the documentation or points that could have improved its quality?_

The README was clear, detailed, and approachable. The combination of text instructions and walkthroughs helped a new user understand the deployment and features.  

*One area for improvement would be to explicitly mark which features are still incoming or incomplete, to avoid confusion.*

### Misc.
_* Are there any other points that you want to comment on / give your feedback?_

_* These could be points that were not captured above, but you think will help to improve the product quality._

The product demonstrates thoughtful automation and could greatly reduce repetitive work. Further improvements in UI design, completion of all planned features, and stronger differentiation from other AI agents would make it even more effective and polished.

---

# Nathan

### Project Summary
This webapp is meant to provide small buisness and non buisness consumers. It looks to save time by automating phone calls 
related to financial tasks such as answering and receiving phone calls, human escalation when needed, and human 
understandable summaries of the class. The users will then be able to check dues, balances, financial advisment appointments,
and when loans are due. Currently, they have per-user configs and settings, inbound and outbound call automation, utilities
such as billing tables, call logs, a CI workflow using Nx, Vercel, Railway, and a demo web app. By automating these calls with an AI agent, the product aims to reduce these inefficiencies 
and ultimately lower financial stress for users.

As for the justifications behind the project, I don't find the ones presented convincing. 
Most of the stress from a consumer point of view is  trying to get in touch with a human, nor do I think this 
significantly improves productivity since a human is still needed as a backup. All this really does is 
cut down on labour costs such that buisnesses can fire call center employees. Additionally, the market is incredibly 
saturated with "AI solutions" for just about everything and I fail to see how this is much different from pre-existing 
solutions already in use by financial instutitions. 

### Project Deployment:
The instructions were very clear and the app was very easy to navigate, and I think they put in good work on this end. 
However, I encountered issues when attempting to access incoming calls; the number provided did not work. 
Additionally, some features which were not implenented just said "coming/incoming" without clear 
indications that these were works in progress. The app and documentation would have greatly benifited from
making it more explicit which features were implemented and which features were still works in progress. 

### Product functionality:
The general UI and layout are very well made and the dashboards provide good layout for buissness users. 
However, when a demo call to the agent is made the app crashes and immediatley logs you out. Additionally, 
attempting to create a new agent logs the user out of the demo as well. 

I think this app could benifit from a more refined "demo mode" where users can learn how to use the web app and
demo calling agents. Additionally filtering specific numbers and scanning for spam calls is a necessity to make
this app functional. 

### Project Documentation

The README was well written and gave a good overview of the issue and the product. I found the overall app to be quite
simple and straightforward and was quite easy to use as someone who was not familiar with it. 

---

# Zaki 
## D2 - Project Review

Please fill in the template below. Some suggestions have been provided to help you understand how to answer each segment.

Before submission, please replace the suggestions (in italics) with your answers. You can keep the prompts though.

### Project Name: _Enter project name_
### Team Name, Team Number: _Enter team name and number who the project belongs to_

### Project Summary:
_Provide a short summary of the project. Capture details like what problem it aims to solve and what are the key contributions of the project._

The project is a web app that serves as a AI Phone Agent for small financial service businesses and consumers, 
that aims to save people time with automating tasks related to phone calls related to financial aspects. 
The project aims to answer and make phone calls, escalate to a human when needed and summarize the calls, 
with the phone goals aim being to confirm balances, due dates, appointments. The current contributions are the end-to-end
call automation (inbound/outbound), per-user configuration (greeting, voice tone, FAQs), 
operational tooling (call histories, billing CSV export, number management), and a production-minded deployment 
and CI workflow (Vercel/Railway, Nx checks, PR reviews) via a live, demo-gated web app.

### Introduction & Overview of the problem and the product:
_* Were you able to understand clearly the problem that the project is trying to solve?_
 Yes. It is  clearly targeted to save the time wasted on routine phone calls that are common within financial 
 businesses and consumers, such as confirming due dates/balances, tasks that cause long hold times, 
 repeated info entry, and error-prone note-taking,  and ties it to reducing financial stress for  them by automating 
 these calls with an AI agent.
_* Were you able to get an overview of what the project is about?_
Yes. It’s a dedicated-number AI phone agent that answers inbound calls and places outbound calls on the user’s
behalf. It also authenticates where appropriate, gathers key facts (names, dates, amounts, reference numbers),
and escalates to a human when needed. After each call it stores transcripts and delivers concise summaries
via the web app and email. Users can also customize greetings/voice tone, manage their assigned number, 
review call histories. There is also a plan to add features for configuring FAQs, outbound scheduling, and billing exports

_* Were you able to differentiate what the project does and how it differs from existing or prior work?_
Partially. I understand that this project's main point of differentiation is that it's aligned towards financial businesses and consumers, and tied into the Savi Finance fintech company. However, I believe there should be a sharper contrast presented as to how this is better for the target market, than say, another mature AI Phone Agent on the market, as with some research, there are quite some many AI Phone Agent businesses. Basically, I want to see more of the difference between this and the preexsiting solutions for the target audience.

_* Any points that would have helped you to get more information on this subject?_
I think having some points talking about how it is specific towards finance would be good. Like maybe some technical details or something about the implementation that helps it be better for financial use cases. Another point would be talking about compliance and privacy in the financial sector; how does this app handle compliance, data privacy, etc. with regards to the financial industry specifically, as I know there are certain regulatory aspects to the financial sector especially with confidential information.

### Project Deployment:
_* Were you able to follow the instructions and install the application on your device?_
Yes, it was easy to follow the instructions and access the application. The instructions were very clear, 
actionable, and made sense as to direct you as to where to go.
_* Did the application run without any errors?_
For the most part. However, I was unable to use the incoming calls feature. I tried to call the number and it did not work. 

_* Any points that would have helped to improve this aspect?_
It would have been better if the instructions made it more clear that certain features were not implemented, 
because it just says (incoming) or (coming) next to the ones that aren't ready yet which could be unclear
to some people, as the they are still present in the instructions workflow for D2.
### Product functionality:
_* How easy and accessible were the key features of the application?_
The application was very easy to use and simple. The buttons are clear, and visually distinct, so it is easy to navigate the application's UI. The app is also smooth and lag-free. I was able to see major features such as transcripts, and see the call history, which were the main features they talked about. So overall, it was quite easy to use. As for accessibility, the icons are visually distinct and easy to see, so it is quite good on that aspect as well.
_* Highlight any bugs or major issues that you could identify_
The major bugs I identified;
Firstly, incoming calls do not work. I tried to call the agent from my phone, however it did not work and the
call just ended instantaneously. Next, clicking create agent to possibly create another agent actually just logs you out
of the demo and resets it.
_* Is the UI of the application representative of a product similar to this project?_
Yes, the UI is very streamlined and makes a lot of sense for a AI phone agent product. It has clear 
dashboards that make sense for a business, as to how they would manage this application, clear settings,
and everything is organized. So thinking from the perspective of a financial business, the UI is
ideal and it is basically easy to access the important things I would need as a business.

_* What additional functionalities or improvements could result in a better product?_

I think one important additional feature would be to add some sort of filtering or search system to the application. 
Right now, you have to scroll through all the call history , for both incoming and outgoing, to just
find one specific call. This could get really messy with the length of use of the project. I believe there should be some sort of
filtering system, like for example, a user could sort by call length, to see important calls. Sort by certain phone numbers
that may be critically important to that business. 
Another tool could be handling spam calls. Many financial businesses and consumers probably deal with scam calls, or fraud, so there should be way to screen for that in the incoming calls, and move them to a different section for "Potential Spam Detected". It would also help contribute to the overall goal of the project to reduce stress among the target market of financial businesses and users.
### Project Documentation:
_* Was the project readme easy to follow and gives all the necessary details about the project?_
Yes, it is easy to follow and contains all the necessary details required for use. The overview was also well
written and clearly gave a good introduction to the project. It also contains clear information on what is implemented
and what isn't, however, the README could be more explicit and clear in showing what features are not implemented. 

_* For a new user (such as yourself), how approachable and easy is it to read and follow the readme?_
It is very approachable and simple. The text instructions are very clear and easy, and they also paired it 
with a video tutorial that follows the instructions, so it's really easy to follow along with the video and use the application.

_* Are there technical or major writing errors that prevents a good understanding or leads to confusion?_
No, there were no major technical or writing errors that confused me.

_* Do you think there are gaps in the documentation or points that could have improved its quality?_
I think the documentation should make it more explicit as to what is not implemented in the app currently and is just a 
placeholder, because it did not seem so clear to me, as it just had some small text next to it saying (incoming). 
Which someone may miss easily.
## Misc.:
_* Are there any other points that you want to comment on / give your feedback?_
N/A
