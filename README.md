# WDRollCall
Simple Android application for testing the capabilities and development time of an application communicating over Wifi-Direct P2P.

This project is a **work in progress** as of 17/04/2017

### What the application tries to accomplish:

1. let the user create a simple group just by simple name.
2. let multiple users connect to a single group just to be members.
3. let the user that created/owns a group do a roll-call of its members.*
4. let each member respond to his group roll-call.
5. Every communication between Group-Owners and Group-Members should be across Wifi-Direct P2P (Bonjour Service) communication.
6. have full validations for all sorts of situation that can present during communication or runtime in general aka validation / request popups
7. Specify a clear way of Creating/Requesting/Discovering/Connecting Bonjour Services using simple steps, without wrapping everything in classes (when not necessary).

(*) The roll-call here **is** an -_are you ready?_- confirmation **is not** a -_are you here?_-

### Pending to develop

1. ~~Creating a Bonjour Service using the user specified Group Name~~
2. ~~Creating a Service Request for all service types~~
3. ~~Discovering all Available Services~~
4. Connecting to one single service
5. Fluent message exchange (Group Owner <-> Member) with a single service.
6. Do Roll Call functionality itself through messages.
7. ~~Separate UI for Group Owner - Member~~
8. ~~UI refresh button for restarting Service Creation and Service Request/Dicovery~~
9. UI Log for storing only important debug information of transaction (using Toast that much is a health hazard).
10. Filter Available services so user can only see and select services from the same application.
11. Add Wifi-off notice and turn on dialog.
12. New UI for when User is inside the Group itself not just selecting which to join.
13. Research a way to force end the communication with a service requester.
