# Product Requirements Document --- Safety Incident Reporting System

**Status:** Revised draft\
**Date:** 10 November 2026\
**Basis:** Agreed PRD decisions and subsequent clarifications.
Unresolved matters are listed in §16.

------------------------------------------------------------------------

## 1. Product Intent

The product lets Inspectors record and submit safety incident reports,
routes submitted reports through the responsible project Manager for
approval, gives Admin/HSE Engineers visibility of approved reports
across projects, and tracks approved reports through resolution and
closure.

Intent statements for traceability:

-   **I-1:** Keep the violations of one incident together in one report,
    without mixing incidents.
-   **I-2:** Route submitted reports through Manager approval before
    they become approved reports.
-   **I-3:** Show reports according to role and project responsibility.
-   **I-4:** Track approved reports through the statuses Open and
    Closed.
-   **I-5:** Allow Admin/HSE Engineers to delete eligible reports and
    restore deleted reports while the project remains open; permanently
    remove project records after the retention period.
-   **I-6:** Keep the product within the agreed scope.

## 2. Scope

### 2.1 In Scope

-   Inspectors creating and submitting reports containing one or more
    violations belonging to the same incident.
-   Validation of the project named on the report form and prevention of
    duplicate violations.
-   Manager approval or rejection of submitted reports.
-   Role- and project-based report visibility.
-   The report lifecycle: Submitted/Pending Approval, Approved/Open,
    Rejected/Discarded, Closed, and Deleted.
-   Corrective-action recording by Admin/HSE Engineer, viewable by the
    relevant Manager.
-   Deletion of eligible Closed reports by Admin/HSE Engineer, access to
    a Deleted tab by Admin/HSE Engineer only, and restoration while the
    project remains open.
-   Project creation, Manager-email assignment, and project closure by
    Admin/HSE Engineer.
-   Automatic closure of Open reports when an Admin/HSE Engineer
    confirms project closure after the system warns that Open reports
    exist.
-   Read-only access to project records during the three-month
    post-closure retention period, subject to role permissions.
-   Login by company-issued email and password; interface determined by
    the logged-in user's role.

### 2.2 Out of Scope

-   Communication between users inside the system, including the
    Manager's rejection email to the Inspector.
-   Creating or managing user accounts, teams, Inspector-to-Manager
    assignments, or passwords (provided by the company).
-   Draft or saved-incomplete reports.
-   Editing submitted reports.
-   Reopening Closed reports.
-   Evidence or proof required to close a report.
-   Escalation or time limits for pending approval.
-   Combining violations of different incidents in one report.
-   Statuses other than Open and Closed for approved reports.
-   System-generated notifications, except for messages displayed by the
    interface.

## 3. Glossary

  -----------------------------------------------------------------------
  Term                                Meaning
  ----------------------------------- -----------------------------------
  **Incident**                        An accident or an inspection
                                      finding to which one or more safety
                                      violations belong.

  **Safety violation**                A safety issue recorded in a
                                      report.

  **Report**                          The unified record of the
                                      violations of one incident. One
                                      incident may have several reports.

  **Submitted**                       The Inspector has submitted a
                                      completed report to the system.

  **Pending approval**                A submitted report awaiting the
                                      project's Manager decision. It has
                                      no Open/Closed status.

  **Approved**                        A pending report accepted by the
                                      Manager. Approval changes its
                                      status to Open.

  **Rejected**                        A pending report rejected by the
                                      Manager. It is discarded and cannot
                                      undergo any further report action
                                      or transition.

  **Open**                            An approved report that has not
                                      been closed.

  **Closed**                          An approved report marked closed by
                                      an Admin/HSE Engineer, or
                                      automatically closed as part of
                                      confirmed project closure. It is
                                      final.

  **Deleted report**                  An eligible Closed report removed
                                      from active report lists by an
                                      Admin/HSE Engineer and placed in
                                      the Deleted tab. It may be restored
                                      while its project remains open.

  **Resolved**                        Proper action has been taken
                                      against the violation. Whether it
                                      is resolved is judged and
                                      communicated outside the system.

  **Corrective action**               An action addressing the reported
                                      violation. It is not a report
                                      status.

  **Inspector**                       A member of an inspection team
                                      under a Manager; creates and
                                      submits reports.

  **Manager**                         The one person designated for a
                                      project; approves or rejects its
                                      pending reports and views reports
                                      for that project.

  **Admin/HSE Engineer**              One role ("Admin" and "HSE
                                      Engineer" are the same). Higher
                                      authority than Manager; oversees
                                      all projects. Several people may
                                      hold this role.

  **Project**                         The unit handled by one Manager and
                                      overseen by Admin/HSE Engineers.

  **Project closure**                 The action that ends a project. It
                                      prevents further report submissions
                                      and report changes. It starts the
                                      three-month retention period.

  **Account Administration**          The separate company team that
                                      handles account, password, and role
                                      problems. It is not the Admin role
                                      and has no role in the software.
  -----------------------------------------------------------------------

## 4. Users and Authority

### 4.1 Permission Matrix

  -----------------------------------------------------------------------
  Action            Inspector         Manager           Admin/HSE
                                                        Engineer
  ----------------- ----------------- ----------------- -----------------
  Create and submit Yes               No                No
  a report                                              

  View own pending  Yes               No                No
  reports                                               

  View pending      No                Yes               **No**
  reports of                                            
  assigned project                                      

  Approve or reject No                Yes, own project  **No**
  a pending report                    only              

  View active Open  Own reports only  Own project       All projects
  and Closed                                            
  reports while                                         
  project is open                                       

  View own Closed   Yes               ---               ---
  reports after                                         
  project closure                                       

  View project      Own Closed        Yes, read-only    Yes, read-only
  reports during    reports only                        
  post-closure                                          
  retention                                             

  Close an Open     No                No                Yes
  report while                                          
  project is open                                       

  Record corrective No                No                Yes
  action while                                          
  project is open                                       

  View corrective   See OQ-03         Yes, own project  Yes
  action                                                

  Delete a pending  No                No                **No**
  report                                                

  Delete an Open    No                No                No; it must first
  report                                                be Closed

  Delete a Closed   No                No                Yes
  report while                                          
  project is open                                       

  Open the Deleted  No                No                Yes
  tab                                                   

  Restore a deleted No                No                Yes
  report while                                          
  project is open                                       

  Delete, restore,  No                No                No
  or otherwise                                          
  change reports                                        
  after project                                         
  closure                                               

  Create a project, No                No                Yes
  assign its                                            
  Manager email, or                                     
  close a project                                       
  -----------------------------------------------------------------------

**Permission notes:**

1.  Admin/HSE Engineers do not receive or view pending reports and
    cannot approve, reject, or delete them.
2.  A Manager can view but cannot change an individual Closed report.
    After project closure, the Manager has read-only access to the
    project's reports during the retention period.
3.  Inspectors can view only their own reports. After project closure,
    they can still view their own Closed reports during the retention
    period; they do not gain access to the project's other reports or
    the Deleted tab.
4.  Only Admin/HSE Engineers can access the Deleted tab. Managers and
    Inspectors cannot view deleted reports.
5.  Once a project is closed, neither Managers nor Admin/HSE Engineers
    can change its reports. This includes deleting or restoring reports.
6.  The project's currently designated Manager receives the project's
    Manager permissions, including access to its existing reports and
    pending approvals.

## 5. Functional Requirements

### 5.1 Report Creation and Submission

  -----------------------------------------------------------------------
  ID                                  Requirement
  ----------------------------------- -----------------------------------
  **FR-01**                           The system shall allow an Inspector
                                      to create a report containing one
                                      or more violations belonging to the
                                      same incident.

  **FR-02**                           The system shall not accept a
                                      report with no violations.

  **FR-03**                           The report form shall include a
                                      text field naming the project to
                                      which the report relates.

  **FR-04**                           The system shall accept a report
                                      only if the named project exists,
                                      has a Manager designated, has not
                                      been closed, and is handled by the
                                      submitting Inspector's Manager.
                                      Otherwise, it shall show an
                                      appropriate message and shall not
                                      accept the report.

  **FR-05**                           The system shall not accept a
                                      report that combines violations
                                      belonging to different incidents.
                                      The mechanism for identifying an
                                      incident is subject to OQ-01 and
                                      OQ-02.

  **FR-06**                           The system shall not accept a
                                      violation that has the same
                                      incident and identical description
                                      text as a violation in an existing
                                      report that has not been deleted,
                                      and shall show a message. Deleted
                                      reports are excluded from this
                                      duplicate check.

  **FR-07**                           The system shall allow a completed,
                                      valid form to be submitted. A
                                      successfully submitted report shall
                                      enter Pending Approval and shall
                                      have no Open/Closed status. The
                                      system shall not provide a draft or
                                      save-incomplete function.

  **FR-08**                           The system shall show a message
                                      when a session has expired, shall
                                      not create a report from an expired
                                      session, and shall create only one
                                      report if submission is triggered
                                      more than once.
  -----------------------------------------------------------------------

### 5.2 Manager Approval and Rejection

  -----------------------------------------------------------------------
  ID                                  Requirement
  ----------------------------------- -----------------------------------
  **FR-09**                           The system shall allow a Manager to
                                      view pending reports for the
                                      project they currently handle.

  **FR-10**                           The system shall allow the
                                      project's Manager to approve a
                                      pending report. Approval shall
                                      change the report's status to Open.

  **FR-11**                           The system shall allow the
                                      project's Manager to reject a
                                      pending report. Rejection shall
                                      discard the report; it shall not
                                      become Open or Closed and shall not
                                      remain available in report lists.

  **FR-12**                           The system shall not show pending
                                      or rejected reports to Admin/HSE
                                      Engineers.

  **FR-13**                           The system shall prevent a rejected
                                      report from being approved, opened,
                                      closed, deleted, or otherwise
                                      transitioned. A rejected report is
                                      discarded and is not recoverable
                                      through the Deleted tab.
  -----------------------------------------------------------------------

### 5.3 Report Visibility and Status

  -----------------------------------------------------------------------
  ID                                  Requirement
  ----------------------------------- -----------------------------------
  **FR-14**                           The system shall show an Inspector
                                      only their own reports: pending
                                      reports and approved reports with
                                      Open or Closed status, subject to
                                      the post-project-closure visibility
                                      rule.

  **FR-15**                           The system shall show a Manager the
                                      pending, Open, and Closed reports
                                      of the project they currently
                                      handle, and no reports belonging to
                                      other projects.

  **FR-16**                           The system shall show an Admin/HSE
                                      Engineer all approved, non-deleted
                                      Open and Closed reports across all
                                      projects. It shall not show pending
                                      or rejected reports.

  **FR-17**                           The system shall use only Open and
                                      Closed as statuses for approved
                                      reports. Pending Approval and
                                      Rejected are lifecycle states, not
                                      report statuses.
  -----------------------------------------------------------------------

### 5.4 Closure and Corrective Action

  -----------------------------------------------------------------------
  ID                                  Requirement
  ----------------------------------- -----------------------------------
  **FR-18**                           The system shall allow an Admin/HSE
                                      Engineer to close an Open report
                                      while its project is open.

  **FR-19**                           The system shall not allow a Closed
                                      report to be edited or reopened.

  **FR-20**                           The system shall allow an Admin/HSE
                                      Engineer to record corrective
                                      action for a report, subject to the
                                      unresolved edit/delete rules in
                                      OQ-03.

  **FR-21**                           The system shall make recorded
                                      corrective action viewable by the
                                      Manager of the report's project.

  **FR-22**                           The system shall not allow report
                                      changes, including closing,
                                      deleting, or restoring reports,
                                      after the project has been closed.
                                      Open reports that are automatically
                                      closed as part of project closure
                                      are the exception to this rule and
                                      shall be closed by the
                                      project-closure operation itself.
  -----------------------------------------------------------------------

### 5.5 Deletion and Restoration

  -----------------------------------------------------------------------
  ID                                  Requirement
  ----------------------------------- -----------------------------------
  **FR-23**                           The system shall allow only an
                                      Admin/HSE Engineer to delete a
                                      Closed report while its project
                                      remains open.

  **FR-24**                           The system shall prevent an
                                      Admin/HSE Engineer from deleting a
                                      Pending Approval, Rejected, or Open
                                      report. An Open report must first
                                      be closed before it can be deleted.
                                      Pending and Rejected reports cannot
                                      be deleted.

  **FR-25**                           Deletion shall remove the report
                                      from active report lists and move
                                      it to the Deleted tab. Deletion
                                      shall not itself change the
                                      report's Closed status.

  **FR-26**                           The system shall provide a Deleted
                                      tab accessible only to Admin/HSE
                                      Engineers.

  **FR-27**                           The system shall allow an Admin/HSE
                                      Engineer to restore a deleted
                                      report only while its project
                                      remains open. A restored report
                                      shall return to the active Closed
                                      reports and shall no longer appear
                                      in the Deleted tab.

  **FR-28**                           The system shall prevent Inspectors
                                      and Managers from viewing,
                                      deleting, or restoring deleted
                                      reports.

  **FR-29**                           The system shall prevent deletion
                                      and restoration after project
                                      closure. Deleted reports shall
                                      remain in the Deleted tab for
                                      Admin/HSE Engineers during the
                                      retention period, but shall be
                                      read-only and cannot be restored.
  -----------------------------------------------------------------------

### 5.6 Project Management and Closure

  -----------------------------------------------------------------------
  ID                                  Requirement
  ----------------------------------- -----------------------------------
  **FR-30**                           The system shall allow an Admin/HSE
                                      Engineer to create a project.

  **FR-31**                           The system shall allow an Admin/HSE
                                      Engineer to add a Manager's email
                                      to a project after it is created
                                      and shall refuse a second Manager
                                      on the same project.

  **FR-32**                           When an Admin/HSE Engineer attempts
                                      to close a project that has one or
                                      more Open reports, the system shall
                                      display an appropriate
                                      warning/error message and prompt
                                      the Admin/HSE Engineer to confirm
                                      whether to proceed with project
                                      closure.

  **FR-33**                           If the Admin/HSE Engineer confirms
                                      project closure under FR-32, the
                                      system shall automatically change
                                      all Open reports belonging to that
                                      project to Closed and then close
                                      the project.

  **FR-34**                           If the Admin/HSE Engineer does not
                                      confirm project closure under
                                      FR-32, the project shall remain
                                      open and no reports shall be
                                      automatically closed.

  **FR-35**                           The system shall not close a
                                      project while it has Pending
                                      Approval reports. It shall inform
                                      the Admin/HSE Engineer that pending
                                      reports must be resolved before
                                      project closure. This requirement
                                      preserves the previously agreed
                                      rule; confirm in OQ-06 whether it
                                      remains intended alongside the new
                                      automatic closure rule for Open
                                      reports.

  **FR-36**                           On project closure, the system
                                      shall prevent new reports from
                                      being submitted against that
                                      project and shall prevent Managers
                                      and Admin/HSE Engineers from
                                      changing its reports.

  **FR-37**                           During the three-month retention
                                      period after project closure,
                                      Admin/HSE Engineers and the
                                      project's Manager shall have
                                      read-only access to the project's
                                      reports. Admin/HSE Engineers shall
                                      also be able to view its Deleted
                                      tab, but shall not restore or
                                      otherwise change any report.

  **FR-38**                           During the three-month retention
                                      period after project closure,
                                      Inspectors shall be able to view
                                      their own Closed reports for that
                                      project. They shall not view other
                                      users' reports or the Deleted tab.

  **FR-39**                           The system shall permanently remove
                                      all reports belonging to a project,
                                      including active Closed reports and
                                      reports in the Deleted tab, when
                                      the three-month retention period
                                      after project closure expires.
  -----------------------------------------------------------------------

### 5.7 Access and Interface

  -----------------------------------------------------------------------
  ID                                  Requirement
  ----------------------------------- -----------------------------------
  **FR-40**                           The system shall show each
                                      logged-in user the interface for
                                      their role, determined from the
                                      login email and company-provided
                                      role information.

  **FR-41**                           The system shall enforce the
                                      permissions in §4 for every view
                                      and operation, including direct
                                      attempts to access a report or
                                      perform an action outside the
                                      user's permissions.
  -----------------------------------------------------------------------

## 6. Non-Functional Requirements

  -----------------------------------------------------------------------
  ID                                  Requirement
  ----------------------------------- -----------------------------------
  **NFR-01 Security**                 Users shall log in with a
                                      company-issued email and password.
                                      The system shall deny every request
                                      to view or perform an action that
                                      the permission matrix in §4 does
                                      not allow. Account creation,
                                      password reset, and lockout are
                                      handled by Account Administration
                                      outside the software.

  **NFR-02 Performance**              At least 95% of applicable
                                      requests, including list views,
                                      shall complete within 2 seconds
                                      under the volumes defined in OQ-08.

  **NFR-03 Retention**                All reports belonging to a project,
                                      including active and deleted
                                      reports, shall be retained for
                                      three months after project closure
                                      and permanently removed when that
                                      period expires.

  **NFR-04 Maintainability**          The code shall be modular and
                                      documented so that a new feature
                                      can be added within the relevant
                                      module without changing unrelated
                                      modules.
  -----------------------------------------------------------------------

## 7. Business Rules

  -----------------------------------------------------------------------
  ID                                  Rule
  ----------------------------------- -----------------------------------
  **BR-01**                           A report contains violations of one
                                      incident only. The same violation
                                      shall not be reported twice, using
                                      the duplicate rule in FR-06.

  **BR-02**                           One incident may have several
                                      reports. A violation found after a
                                      report has been submitted goes in a
                                      new report.

  **BR-03**                           A submitted report is Pending
                                      Approval and has no Open/Closed
                                      status. Manager approval changes it
                                      to Open.

  **BR-04**                           The Manager is the sole approver
                                      for their project. There is no time
                                      limit or escalation; a pending
                                      report may wait indefinitely and
                                      Admin/HSE Engineers cannot view or
                                      act on it.

  **BR-05**                           Rejection discards the report. The
                                      Manager communicates the reason to
                                      the Inspector outside the system. A
                                      rejected report cannot be approved,
                                      opened, closed, deleted, or
                                      restored.

  **BR-06**                           A submitted report cannot be edited
                                      by anyone.

  **BR-07**                           An Admin/HSE Engineer may close an
                                      Open report while the project is
                                      open once it is considered
                                      resolved. No evidence is required.
                                      Closed is final.

  **BR-08**                           Only an Admin/HSE Engineer may
                                      delete a Closed report, and only
                                      while its project is open. Open
                                      reports must be closed first.
                                      Pending and Rejected reports cannot
                                      be deleted.

  **BR-09**                           Deletion removes a Closed report
                                      from active lists and places it in
                                      the Deleted tab. Only Admin/HSE
                                      Engineers may access that tab.

  **BR-10**                           An Admin/HSE Engineer may restore a
                                      deleted report while its project is
                                      open. Restoration returns it to the
                                      active Closed reports. No role may
                                      restore a report after project
                                      closure.

  **BR-11**                           When an Admin/HSE Engineer confirms
                                      project closure after being warned
                                      that Open reports exist, all Open
                                      reports in that project are
                                      automatically changed to Closed as
                                      part of the closure operation.

  **BR-12**                           A project cannot be closed while it
                                      has Pending Approval reports,
                                      unless this rule is changed through
                                      the open question in OQ-06.

  **BR-13**                           Once a project is closed, Managers
                                      and Admin/HSE Engineers have
                                      read-only access during the
                                      retention period. Inspectors may
                                      view only their own Closed reports.
                                      No report may be deleted, restored,
                                      or otherwise changed.

  **BR-14**                           All reports belonging to a closed
                                      project, including active and
                                      deleted reports, are permanently
                                      removed three months after project
                                      closure.

  **BR-15**                           Each project has exactly one
                                      Manager. No report can be filed on
                                      a project until its Manager email
                                      is added. Manager access follows
                                      the project's currently designated
                                      Manager, who holds the access of
                                      the previous Manager, including
                                      pending approvals.

  **BR-16**                           Admin and HSE Engineer are one role
                                      with higher authority than Manager,
                                      overseeing all projects.

  **BR-17**                           Corrective action is an action
                                      recorded by an Admin/HSE Engineer;
                                      it is not a status.

  **BR-18**                           The following happen outside the
                                      system: the Manager's rejection
                                      reason/communication to the
                                      Inspector, notice to the Admin/HSE
                                      Engineer that a matter is resolved,
                                      and account problems raised with
                                      Account Administration.

  **BR-19**                           Roles, teams, accounts, and
                                      Inspector-to-Manager assignments
                                      are decided by the company before
                                      use.
  -----------------------------------------------------------------------

## 8. Constraints

  -----------------------------------------------------------------------
  ID                                  Constraint
  ----------------------------------- -----------------------------------
  **CON-01**                          The product shall not include
                                      in-system communication between
                                      users.

  **CON-02**                          Admin and HSE Engineer shall be
                                      treated as one role.

  **CON-03**                          No technology, framework, database,
                                      hosting, or architecture is
                                      prescribed.

  **CON-04**                          The software relies on
                                      company-provided accounts and
                                      designations; it has no user or
                                      team management.

  **CON-05**                          The software does not verify that
                                      an entered Manager email belongs to
                                      a real Manager and defines no rules
                                      for later changes to it.

  **CON-06**                          The software does not judge or
                                      evidence resolution; closing an
                                      individual report is an Admin/HSE
                                      Engineer decision. Project closure
                                      may automatically close all Open
                                      reports after explicit
                                      confirmation.
  -----------------------------------------------------------------------

## 9. Main Flows

### 9.1 Submit and Approve a Report

1.  An Inspector logs in and sees the Inspector interface.
2.  The Inspector fills in the project name and one or more violations
    belonging to one incident.
3.  The system validates the form, project, and duplicate-violation
    rules.
4.  The Inspector submits the completed report.
5.  The report enters Pending Approval and has no Open/Closed status.
6.  The project's Manager sees the pending report. Admin/HSE Engineers
    do not.
7.  The Manager either:
    -   **Approves:** the report becomes Open; or
    -   **Rejects:** the report is discarded and cannot undergo any
        further report action or transition. The Manager communicates
        the reason outside the system.
8.  Approved reports become visible according to the permission matrix.

### 9.2 Resolve and Close a Report

1.  An Admin/HSE Engineer views an Open report.
2.  The Admin/HSE Engineer records corrective action, if applicable.
3.  Once informed outside the system that the matter is resolved, an
    Admin/HSE Engineer closes the report.
4.  The report becomes Closed and cannot be edited or reopened.
5.  A Closed report may be deleted by an Admin/HSE Engineer while its
    project remains open.

### 9.3 Delete and Restore a Report

1.  An Admin/HSE Engineer selects a Closed report while its project is
    open.
2.  The Admin/HSE Engineer deletes it.
3.  The report leaves active lists and appears in the Deleted tab.
4.  Only Admin/HSE Engineers can view the Deleted tab.
5.  While the project remains open, an Admin/HSE Engineer may restore
    the report. It returns to the active Closed reports.
6.  Once the project is closed, deletion and restoration are disabled.
    The Deleted tab is read-only during retention.

### 9.4 Close a Project

1.  An Admin/HSE Engineer initiates project closure.
2.  If Open reports exist, the system displays a warning/error message
    and asks the Admin/HSE Engineer whether to proceed.
3.  If the Admin/HSE Engineer declines, the project remains open and no
    reports change.
4.  If the Admin/HSE Engineer confirms, the system automatically closes
    all Open reports in the project and closes the project.
5.  If Pending Approval reports exist, project closure is blocked under
    the current rule and the system informs the Admin/HSE Engineer. This
    pending-report rule requires confirmation in OQ-06.
6.  After closure, no Manager or Admin/HSE Engineer may change project
    reports. Admin/HSE Engineers and the Manager may view the project's
    reports read-only. Inspectors may view only their own Closed
    reports.
7.  The project's active and deleted reports are retained for three
    months after closure.
8.  At the end of the three-month period, all reports for the project,
    including Closed and previously deleted reports, are permanently
    removed.

## 10. Edge Cases

  -----------------------------------------------------------------------
  ID                      Case                    Required behavior
  ----------------------- ----------------------- -----------------------
  **EC-01**               Violations of two       Not accepted.
                          incidents in one report 

  **EC-02**               Several violations of   One report is allowed.
                          one incident            

  **EC-03**               Report with no          Not accepted; a message
                          violations              says a violation is
                                                  required.

  **EC-04**               Same incident and       Not accepted, with a
                          identical description   message.
                          as a violation in an    
                          active report           

  **EC-05**               Violation matches a     Accepted by the
                          report currently in the duplicate check;
                          Deleted tab             deleted reports are
                                                  excluded.

  **EC-06**               Violation found after a Filed in a new report.
                          report is submitted     

  **EC-07**               Project name matches no Not accepted; message
                          project                 asks for a proper
                                                  project name.

  **EC-08**               Project belongs to      Not accepted, with a
                          another Manager's team  message.

  **EC-09**               Project has no Manager  Not accepted.
                          email yet, or has been  
                          closed                  

  **EC-10**               Second Manager added to Refused.
                          a project               

  **EC-11**               Session expires         Message shown; nothing
                          mid-form                is saved.

  **EC-12**               Submit triggered twice  Exactly one report is
                                                  created.

  **EC-13**               Manager never acts on a It stays pending
                          pending report          indefinitely; Admin/HSE
                                                  Engineers cannot see or
                                                  act on it.

  **EC-14**               Manager replaced while  New Manager can view
                          reports are pending     and decide on the
                                                  pending reports.

  **EC-15**               Manager rejects a       It is discarded; it
                          report                  cannot be approved,
                                                  opened, closed,
                                                  deleted, or restored.

  **EC-16**               Admin attempts to view  Access is denied.
                          a pending report        

  **EC-17**               Admin attempts to       Action is denied/not
                          delete a pending or     available.
                          rejected report         

  **EC-18**               Admin attempts to       Action is denied;
                          delete an Open report   report must first be
                                                  Closed.

  **EC-19**               Admin deletes a Closed  Report leaves active
                          report while project is lists and appears in
                          open                    the Admin-only Deleted
                                                  tab.

  **EC-20**               Manager or Inspector    Access is denied.
                          attempts to access the  
                          Deleted tab             

  **EC-21**               Admin restores a        Report returns to
                          deleted report while    active Closed reports.
                          project is open         

  **EC-22**               Admin attempts to       Action is denied;
                          restore a report after  Deleted tab is
                          project closure         read-only.

  **EC-23**               Admin attempts to       Action is denied.
                          delete a report after   
                          project closure         

  **EC-24**               Project closure         System warns and
                          attempted while Open    prompts for
                          reports exist           confirmation. On
                                                  confirmation, all Open
                                                  reports are
                                                  automatically Closed;
                                                  on decline, nothing
                                                  changes.

  **EC-25**               Project closure         Closure is blocked
                          attempted while Pending under the current rule;
                          Approval reports exist  see OQ-06.

  **EC-26**               Manager attempts to     Action is denied.
                          change a Closed report  

  **EC-27**               Manager or Admin        Action is denied.
                          attempts to change any  Read-only viewing
                          report after project    remains available
                          closure                 during retention
                                                  according to §4.

  **EC-28**               Inspector views reports Inspector can view only
                          after project closure   their own Closed
                                                  reports.

  **EC-29**               Admin or Manager views  They can view the
                          a project after closure project's reports
                                                  read-only during
                                                  retention.

  **EC-30**               Three-month retention   All reports for the
                          period expires          project, including
                                                  active Closed and
                                                  deleted reports, are
                                                  permanently removed.

  **EC-31**               Two Admins close the    The report ends Closed
                          same report             once; no inconsistent
                          concurrently            state is created.

  **EC-32**               Inspector account       Their reports remain
                          removed by the company  until removed under the
                                                  project retention rule.

  **EC-33**               Forgotten password,     Handled by Account
                          lockout, or role change Administration outside
                                                  the software.
  -----------------------------------------------------------------------

## 11. Acceptance Criteria

  -----------------------------------------------------------------------
  ID                      Covers                  Criterion
  ----------------------- ----------------------- -----------------------
  **AC-01**               FR-01                   Given a form with three
                                                  violations of one
                                                  incident, when
                                                  submitted, then one
                                                  report containing all
                                                  three violations is
                                                  accepted.

  **AC-02**               FR-02                   Given a form with no
                                                  violations, when
                                                  submitted, then it is
                                                  not accepted and a
                                                  message says a
                                                  violation is required.

  **AC-03**               FR-03, FR-04            Given a project name
                                                  matching no project,
                                                  when submitted, then
                                                  the report is not
                                                  accepted, an
                                                  appropriate message is
                                                  shown, and nothing is
                                                  stored.

  **AC-04**               FR-04                   Given a project handled
                                                  by a Manager other than
                                                  the Inspector's own
                                                  Manager, when
                                                  submitted, then the
                                                  report is not accepted
                                                  and a message is shown.

  **AC-05**               FR-04                   Given a project with no
                                                  Manager email or a
                                                  closed project, when a
                                                  report is submitted
                                                  against it, then it is
                                                  not accepted.

  **AC-06**               FR-05                   Given a form combining
                                                  violations of two
                                                  incidents, when
                                                  submitted, then it is
                                                  not accepted. Test
                                                  details depend on OQ-01
                                                  and OQ-02.

  **AC-07**               FR-06                   Given a violation with
                                                  the same incident and
                                                  identical description
                                                  as one in a report that
                                                  has not been deleted,
                                                  when submitted, then it
                                                  is not accepted and a
                                                  message is shown.

  **AC-08**               FR-06                   Given the matching
                                                  report is in the
                                                  Deleted tab, when the
                                                  violation is
                                                  resubmitted, then the
                                                  duplicate check does
                                                  not block it.

  **AC-09**               FR-07                   Given a valid form,
                                                  when submitted, then
                                                  the report is Pending
                                                  Approval with no
                                                  Open/Closed status; it
                                                  is visible to its
                                                  Inspector and project
                                                  Manager, but not to
                                                  Admin/HSE Engineers. No
                                                  draft can be saved.

  **AC-10**               FR-08                   Given an expired
                                                  session, when the
                                                  Inspector submits, then
                                                  a message is shown and
                                                  no report exists. Given
                                                  a double submission,
                                                  then exactly one report
                                                  exists.

  **AC-11**               FR-09, FR-10, FR-17     Given a pending report,
                                                  when its Manager
                                                  approves it, then its
                                                  status becomes Open and
                                                  it becomes visible to
                                                  Admin/HSE Engineers.

  **AC-12**               FR-11, FR-13            Given a pending report,
                                                  when its Manager
                                                  rejects it, then it is
                                                  discarded and cannot be
                                                  approved, opened,
                                                  closed, deleted, or
                                                  restored.

  **AC-13**               FR-12, FR-16, FR-41     Given an Admin/HSE
                                                  Engineer attempts to
                                                  view or act on a
                                                  pending report, then
                                                  access is denied.

  **AC-14**               FR-14                   Given an Inspector
                                                  views reports, then
                                                  they see only their own
                                                  pending, Open, and
                                                  Closed reports, subject
                                                  to post-closure rules.

  **AC-15**               FR-15                   Given a Manager views
                                                  reports, then they see
                                                  pending, Open, and
                                                  Closed reports for
                                                  their own project only.

  **AC-16**               FR-16                   Given an Admin/HSE
                                                  Engineer views active
                                                  reports, then all
                                                  approved, non-deleted
                                                  Open and Closed reports
                                                  across projects are
                                                  shown, and
                                                  pending/rejected
                                                  reports are not shown.

  **AC-17**               FR-17                   Given any approved
                                                  report, then its status
                                                  is Open or Closed and
                                                  no other status value
                                                  is available. Pending
                                                  Approval and Rejected
                                                  are not statuses.

  **AC-18**               FR-18                   Given an Open report in
                                                  an open project, when
                                                  an Admin/HSE Engineer
                                                  closes it, then its
                                                  status becomes Closed
                                                  without requiring
                                                  evidence.

  **AC-19**               FR-19, BR-06            Given a Closed report,
                                                  then no role can edit
                                                  or reopen it; given any
                                                  submitted report, then
                                                  no role has a
                                                  report-edit function.

  **AC-20**               FR-18                   Given two Admin/HSE
                                                  Engineers attempt to
                                                  close the same Open
                                                  report concurrently,
                                                  then the report ends
                                                  Closed once and no
                                                  inconsistent state is
                                                  created.

  **AC-21**               FR-20, FR-21            Given corrective action
                                                  recorded by an
                                                  Admin/HSE Engineer,
                                                  then the project's
                                                  Manager can view it and
                                                  a Manager of another
                                                  project cannot.

  **AC-22**               FR-23, FR-24, FR-25     Given an Admin/HSE
                                                  Engineer attempts to
                                                  delete an Open, Pending
                                                  Approval, or Rejected
                                                  report, then deletion
                                                  is denied.

  **AC-23**               FR-23, FR-25            Given an Admin/HSE
                                                  Engineer deletes a
                                                  Closed report while its
                                                  project is open, then
                                                  it leaves active lists
                                                  and appears in the
                                                  Deleted tab.

  **AC-24**               FR-26, FR-28, FR-41     Given a Manager or
                                                  Inspector attempts to
                                                  open the Deleted tab or
                                                  access a deleted report
                                                  directly, then access
                                                  is denied.

  **AC-25**               FR-27                   Given an Admin/HSE
                                                  Engineer restores a
                                                  deleted report while
                                                  its project is open,
                                                  then it returns to
                                                  active Closed reports
                                                  and is removed from the
                                                  Deleted tab.

  **AC-26**               FR-29, FR-36, FR-37     Given a project is
                                                  closed, then Admin/HSE
                                                  Engineers cannot delete
                                                  or restore reports, and
                                                  the Deleted tab is
                                                  read-only during
                                                  retention.

  **AC-27**               FR-32, FR-33, FR-34     Given a project with
                                                  Open reports, when an
                                                  Admin/HSE Engineer
                                                  initiates closure, then
                                                  a warning and
                                                  confirmation prompt
                                                  appear. If confirmed,
                                                  all Open reports become
                                                  Closed and the project
                                                  closes; if declined,
                                                  the project and reports
                                                  remain unchanged.

  **AC-28**               FR-35                   Given a project with
                                                  Pending Approval
                                                  reports, when an
                                                  Admin/HSE Engineer
                                                  attempts closure, then
                                                  closure is blocked and
                                                  an explanatory message
                                                  is shown, subject to
                                                  confirmation of OQ-06.

  **AC-29**               FR-37, FR-38, FR-41     Given a closed project
                                                  within the retention
                                                  period, then Admin/HSE
                                                  Engineers and the
                                                  Manager can view its
                                                  reports read-only,
                                                  while Inspectors can
                                                  view only their own
                                                  Closed reports.

  **AC-30**               FR-39, NFR-03           Given a project closed
                                                  three months ago, then
                                                  all its reports,
                                                  including active Closed
                                                  and deleted reports,
                                                  are permanently
                                                  removed. Given a
                                                  project closed less
                                                  than three months ago,
                                                  then its reports are
                                                  retained.

  **AC-31**               FR-30, FR-31            Given an Admin/HSE
                                                  Engineer creates a
                                                  project and adds a
                                                  Manager email, then the
                                                  project has that
                                                  Manager; adding a
                                                  second Manager is
                                                  refused.

  **AC-32**               FR-40, NFR-01           Given each role logs
                                                  in, then they see the
                                                  interface for their
                                                  role.

  **AC-33**               FR-41, NFR-01           Given any user attempts
                                                  an operation or record
                                                  access not allowed by
                                                  §4, then the system
                                                  denies the request.
                                                  This includes direct
                                                  access attempts, not
                                                  only hidden or disabled
                                                  interface controls.

  **AC-34**               NFR-02                  Given the volumes
                                                  agreed in OQ-08, then
                                                  at least 95% of
                                                  applicable requests
                                                  complete within 2
                                                  seconds.

  **AC-35**               NFR-04                  Given a release, then
                                                  code review confirms
                                                  documented modules and
                                                  that a feature in one
                                                  module does not require
                                                  changes to unrelated
                                                  modules.

  **AC-36**               CON-01                  Given the product, then
                                                  it contains no
                                                  in-system messaging
                                                  feature between users.
  -----------------------------------------------------------------------

## 12. Traceability to Product Intent

  -----------------------------------------------------------------------
  Intent                              Supporting requirements
  ----------------------------------- -----------------------------------
  I-1 One incident's violations       FR-01, FR-02, FR-05, FR-06, BR-01,
  together, no mixing                 BR-02

  I-2 Manager approval before reports FR-07, FR-09 to FR-13, BR-03 to
  become approved                     BR-05

  I-3 Visibility by role and project  FR-04, FR-14 to FR-16, FR-26,
                                      FR-28, FR-37, FR-38, FR-40, FR-41,
                                      BR-15, BR-16, NFR-01

  I-4 Track resolution to Closed      FR-17 to FR-22, BR-06, BR-07, BR-17

  I-5 Delete, restore, retain, and    FR-23 to FR-29, FR-32 to FR-39,
  permanently remove                  BR-08 to BR-14, NFR-03

  I-6 Stay in scope                   BR-18, BR-19, CON-01 to CON-06,
                                      NFR-04
  -----------------------------------------------------------------------

## 13. Accepted Risks

  -----------------------------------------------------------------------
  ID                                  Risk
  ----------------------------------- -----------------------------------
  **R-01**                            The Manager is the sole approval
                                      gatekeeper. A Manager can leave a
                                      report pending or reject it, and
                                      Admin/HSE Engineers cannot see or
                                      act on it.

  **R-02**                            A pending report can block project
                                      closure, while Admin/HSE Engineers
                                      cannot view or decide on it.

  **R-03**                            The system holds no record that a
                                      report was rejected, by whom, or
                                      why.

  **R-04**                            Duplicate detection is based on
                                      exact description text, so a
                                      rewording or typo may pass the
                                      check.

  **R-05**                            An Admin/HSE Engineer can close or
                                      delete eligible reports, and the
                                      system cannot verify that an
                                      individual matter is resolved.

  **R-06**                            With no draft state, entered data
                                      is lost if a session expires.

  **R-07**                            Confirmed project closure
                                      automatically closes all Open
                                      reports, even if an individual
                                      report has not separately been
                                      confirmed as resolved.
  -----------------------------------------------------------------------

## 14. Clashes Removed or Reconciled

  -----------------------------------------------------------------------
  Previous clash or ambiguity         Resolution in this revision
  ----------------------------------- -----------------------------------
  Rejection was described as          Rejected reports are discarded and
  discarded, but its later lifecycle  cannot be approved, opened, closed,
  was not fully explicit.             deleted, or restored.

  Admin visibility was broad in       Admin/HSE Engineers cannot view or
  places and could be read as         act on Pending Approval or Rejected
  including pending reports.          reports.

  Deletion wording could imply that   Only Closed reports can be deleted,
  any report could be deleted.        and only by Admin/HSE Engineers
                                      while the project is open.

  Open-report deletion and closure    An Open report must first be Closed
  were not clearly ordered.           before it can be deleted.

  Deleted-tab permissions were        Only Admin/HSE Engineers can access
  provisional or unresolved.          the Deleted tab; Managers and
                                      Inspectors cannot.

  Restoration was not included in the Admin/HSE Engineers can restore a
  earlier lifecycle.                  deleted Closed report while its
                                      project is open; it returns to
                                      active Closed reports.

  Project closure previously required Open reports trigger a warning and
  no Open or pending reports,         confirmation; confirming closes
  conflicting with the new            them automatically. Pending reports
  automatic-close confirmation.       remain a closure blocker pending
                                      confirmation in OQ-06.

  Project closure did not clearly     Managers and Admin/HSE Engineers
  define post-closure change          have read-only access during
  permissions.                        retention; no report deletion or
                                      restoration is allowed after
                                      closure.

  Inspector access after project      Inspectors can view only their own
  closure was unclear.                Closed reports during retention.

  Retention wording could be read as  All project reports, including
  applying only to deleted reports.   active Closed and deleted reports,
                                      are permanently removed three
                                      months after project closure.

  Closed reports were described as    Automatic closure is an explicit
  final, while project closure        project-closure operation; Closed
  automatically closes Open reports.  remains final after that operation.
  -----------------------------------------------------------------------

## 15. Remaining Gaps

  -----------------------------------------------------------------------
  ID                      Gap / question          Why it matters
  ----------------------- ----------------------- -----------------------
  **OQ-01**               What exact fields must  Needed to finalize
                          the report form         FR-03, validation, and
                          contain?                acceptance tests.

  **OQ-02**               What identifies the     Needed to implement and
                          "same incident" for an  test incident grouping
                          accident versus an      and duplicate
                          inspection finding?     detection.

  **OQ-03**               Can corrective action   FR-20 and FR-21 define
                          be edited or deleted    recording and Manager
                          while the report is     visibility but not
                          Open, and can           these permissions.
                          Inspectors see it?      

  **OQ-04**               Can an Admin/HSE        Current rule says no
                          Engineer delete a       report changes after
                          Closed report after the project closure;
                          project has closed?     confirm that this
                                                  includes deletion in
                                                  all circumstances.

  **OQ-05**               Should the system       Needed to determine
                          record who closed or    audit-history
                          deleted a report, when, requirements.
                          and why?                

  **OQ-06**               Can a project be closed This revision preserves
                          while Pending Approval  the earlier rule that
                          reports exist?          pending reports block
                                                  closure. The new
                                                  confirmation flow
                                                  explicitly addresses
                                                  Open reports only.
                                                  Confirm whether pending
                                                  reports should continue
                                                  to block closure, or
                                                  whether another
                                                  handling rule is
                                                  intended.

  **OQ-07**               Does project closure    Confirmation currently
                          allow the Admin/HSE     causes all Open reports
                          Engineer to proceed     to be automatically
                          when only Open reports  Closed. This is a
                          exist, even if some are consequential business
                          not individually        decision.
                          resolved?               

  **OQ-08**               What happens to project FR-39/NFR-03 explicitly
                          records other than      cover reports; the
                          reports (for example,   complete project-data
                          project details and     deletion scope should
                          corrective-action       be confirmed.
                          records) at the end of  
                          the three-month         
                          retention period?       

  **OQ-09**               What numbers of users,  Needed to make NFR-02
                          reports, and            measurable.
                          attachments define the  
                          test load for the       
                          2-second response-time  
                          target?                 

  **OQ-10**               Can one Manager handle  Needed to finalize
                          several projects, and   project assignment and
                          can a project's Manager Manager replacement
                          email be changed after  behavior.
                          it is entered?          

  **OQ-11**               What happens to a       Needed to define
                          deleted report's        whether corrective
                          corrective-action data  action is retained and
                          on deletion and         restored with the
                          restoration?            report.

  **OQ-12**               When a deleted report   Needed if auditability
                          is restored, should its of delete/restore
                          original deletion       actions is required.
                          timestamp/history       
                          remain available to     
                          Admin/HSE Engineers?    
  -----------------------------------------------------------------------

## 16. Assumptions to Confirm

  -----------------------------------------------------------------------
  ID                                  Assumption
  ----------------------------------- -----------------------------------
  **A-01**                            An Admin/HSE Engineer who records
                                      corrective action can also view it.

  **A-02**                            Project names are unique, so name
                                      matching in FR-04 is unambiguous.

  **A-03**                            The system sends no notifications;
                                      Managers check their pending list.
                                      The rejection reason is
                                      communicated by the Manager outside
                                      the system.

  **A-04**                            The company's designation data
                                      tells the system which Manager an
                                      Inspector belongs to, so FR-04 can
                                      compare it with the project's
                                      Manager.

  **A-05**                            A restored report returns to the
                                      active Closed list because only
                                      Closed reports can be deleted.

  **A-06**                            The three-month retention period
                                      begins at the date/time the project
                                      is closed.

  **A-07**                            The Deleted tab remains accessible
                                      to Admin/HSE Engineers in read-only
                                      mode during the post-closure
                                      retention period.

  **A-08**                            Project closure does not delete the
                                      project record immediately; report
                                      records remain available for the
                                      retention period under the role
                                      permissions in §4.
  -----------------------------------------------------------------------
