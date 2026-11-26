# Product Requirements Document --- Safety Incident Reporting System

**Status:** Revised draft v2 — all open questions resolved\
**Date:** 25 November 2026\
**Basis:** Agreed PRD decisions, subsequent clarifications, and the
resolved open questions (§15) and confirmed assumptions (§16) recorded
in this revision.

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
-   **I-7:** Notify the Inspector of a rejection with its reason and
allow the rejected report to be edited and resubmitted,
with version history retained.

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
-   Manager rejection of a pending report with a reason, stored by the
system and notified in-system to the report's Inspector.
-   Editing and resubmitting a rejected report by its Inspector; each
submission is retained as a version in the report's version history.
-   Reassignment of a project's Manager email by an Admin/HSE Engineer
while the project remains open.
-   Audit history recording actor, action, and timestamp for report
closure, deletion, restoration, and Manager reassignment.

### 2.2 Out of Scope

-   General communication between users inside the system (chat,
    comments, free-form messages). The only in-system message is the
    structured rejection notification required by FR-42.
-   Creating or managing user accounts, teams, Inspector-to-Manager
    assignments, or passwords (provided by the company).
-   Draft or saved-incomplete reports.
-   Editing reports in Pending Approval, Open, or Closed states. Only
    Rejected reports may be edited (FR-43).
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
                                      Manager with a reason. The system
                                      notifies the Inspector, who may
                                      edit the report and resubmit it.
                                      While rejected it cannot be
                                      approved, opened, closed, or
                                      deleted.

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

  **Notification**                    An in-system message created by
                                      the system (not composed by
                                      another user) that reports an
                                      event to a user, such as the
                                      rejection reason sent to an
                                      Inspector.

  **Report version**                  An immutable snapshot of a report
                                      taken at each submission or
                                      resubmission, retained in the
                                      report's version history.
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
  and rejected
  reports

  View pending and  No                Yes, own project  **No**
  rejected reports
  of assigned
  project

  Approve or reject No                Yes, own project  **No**
  a pending report                    only

  Edit and resubmit Yes, own rejected No                No
  a rejected report  report only

  View own          Yes               No                No
  rejection
  notification

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

  View corrective   No                Yes, own project  Yes
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
  assign or
  reassign its
  Manager email,
  or close a
  project

  View version      No                Yes, own project  Yes
  history of a
  report

  View audit        No                No                Yes
  history
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
    pending approvals. When the Manager is reassigned, these
    permissions transfer to the newly designated Manager and are
    withdrawn from the previous one.
7.  Only the Inspector who owns a rejected report can edit and
    resubmit it. Rejection notifications are visible only to that
    Inspector.
8.  Inspectors cannot view corrective action on their reports; only
    the project's Manager and Admin/HSE Engineers can.
9.  Only Admin/HSE Engineers can view the audit history.

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
                                      field naming the project to which
                                      the report relates, together with
                                      the incident date and time,
                                      location, and incident type
                                      (accident or inspection finding).
                                      Each violation entry shall include
                                      a description, hazard category,
                                      severity level, immediate action
                                      taken, and zero or more
                                      photograph attachments. (Resolved
                                      OQ-01.)

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
                                      An incident is identified by the
                                      combination of incident date,
                                      location, and incident type as
                                      specified in OQ-02.

  **FR-06**                           The system shall not accept a
                                      violation that has the same
                                      incident and identical description
                                      text as a violation in an existing
                                      report that has not been deleted,
                                      and shall show a message. Deleted
                                      reports are excluded from this
                                      duplicate check, and the report
                                      being edited or resubmitted is
                                      excluded from a check against
                                      itself.

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
                                      pending report with a reason. The
                                      report shall become Rejected, the
                                      reason shall be stored with it,
                                      and the system shall create a
                                      notification of the reason for
                                      the report's Inspector. A rejected
                                      report shall not become Open or
                                      Closed while rejected.

  **FR-12**                           The system shall not show pending
                                      or rejected reports to Admin/HSE
                                      Engineers.

  **FR-13**                           The system shall prevent a rejected
                                      report from being approved, opened,
                                      closed, deleted, or otherwise
                                      transitioned while it remains
                                      rejected. Its only permitted
                                      transition is edit and resubmission
                                      by its Inspector under FR-43,
                                      which returns it to Pending
                                      Approval. A rejected report shall
                                      not appear in the Deleted tab.
  -----------------------------------------------------------------------

### 5.3 Report Visibility and Status

  -----------------------------------------------------------------------
  ID                                  Requirement
  ----------------------------------- -----------------------------------
  **FR-14**                           The system shall show an Inspector
                                      only their own reports: pending,
                                      rejected (with the rejection
                                      reason), and approved reports with
                                      Open or Closed status, subject to
                                      the post-project-closure visibility
                                      rule.

  **FR-15**                           The system shall show a Manager the
                                      pending, rejected, Open, and Closed
                                      reports of the project they
                                      currently handle, and no reports
                                      belonging to other projects.

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
                                      action for a report. Corrective
                                      action shall be editable and
                                      deletable while the report remains
                                      Open, and shall become immutable
                                      once the report is Closed.

  **FR-21**                           The system shall make recorded
                                      corrective action viewable by the
                                      Manager of the report's project.
                                      Inspectors shall not view
                                      corrective action.

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
                                      on the same project at the same
                                      time. The Admin/HSE Engineer may
                                      later change or reassign the
                                      project's Manager email while the
                                      project remains open; the newly
                                      designated Manager shall gain the
                                      project's Manager permissions,
                                      including its pending approvals
                                      and existing reports, and the
                                      previous Manager shall lose them.
                                      Project names shall be unique.

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

  **FR-35**                           When an Admin/HSE Engineer attempts
                                      to close a project that has one or
                                      more Pending Approval reports, the
                                      system shall display an appropriate
                                      warning message stating that the
                                      pending reports will be discarded,
                                      and prompt the Admin/HSE Engineer
                                      to confirm whether to proceed. If
                                      confirmed, the pending reports
                                      shall be discarded as part of the
                                      closure operation; if declined,
                                      nothing changes. (Resolved OQ-06.)

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
                                      all data belonging to a project
                                      when the three-month retention
                                      period after project closure
                                      expires. This includes the project
                                      record itself, all reports in all
                                      states and versions, violations,
                                      corrective actions, photograph
                                      files, notifications, and audit
                                      history. (Resolved OQ-08.)
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

  **FR-42**                           The system shall allow a Manager
                                      to enter a reason when rejecting a
                                      pending report, shall store that
                                      reason with the report, and shall
                                      create an in-system notification
                                      containing the reason for the
                                      report's Inspector. No other
                                      users shall receive this
                                      notification.

  **FR-43**                           The system shall allow an Inspector
                                      to edit a rejected report they own
                                      and resubmit it. Resubmission shall
                                      revalidate the report under
                                      FR-02 to FR-06 and return it to
                                      Pending Approval. No other state
                                      (Pending Approval, Open, Closed)
                                      shall be editable.

  **FR-44**                           The system shall retain an
                                      immutable version snapshot of a
                                      report at every submission and
                                      resubmission. Version history
                                      shall be viewable by the project's
                                      Manager and by Admin/HSE
                                      Engineers. Inspectors shall see
                                      only the current version.

  **FR-45**                           The system shall allow an Admin/HSE
                                      Engineer to change the Manager
                                      email of a project that is still
                                      open, as specified in FR-31, and
                                      shall record the change in the
                                      audit history.

  **FR-46**                           The system shall record an audit
                                      history entry with the acting
                                      user, the action type, and the
                                      timestamp whenever a report is
                                      closed, deleted, or restored, or
                                      whenever a project's Manager is
                                      reassigned. The audit history
                                      shall be viewable only by
                                      Admin/HSE Engineers. (Resolved
                                      OQ-05 and OQ-12.)

  **FR-47**                           Corrective action recorded on a
                                      report shall be retained when the
                                      report is deleted to the Deleted
                                      tab and shall be restored with the
                                      report if it is restored. It shall
                                      be permanently removed with the
                                      report under FR-39. (Resolved
                                      OQ-11.)
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
                                      under a test load of up to 50
                                      concurrent users, 1,000 reports,
                                      50 projects, and 2,000 photographs.
                                      (Resolved OQ-09.)

  **NFR-03 Retention**                All data belonging to a project,
                                      including active and deleted
                                      reports, report versions,
                                      corrective actions, photograph
                                      files, notifications, and audit
                                      history, shall be retained for
                                      three months after project closure
                                      and permanently removed when that
                                      period expires. The three-month
                                      period starts at the exact
                                      project-closure timestamp.
                                      (Resolved OQ-08 and A-06.)

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

  **BR-05**                           Rejection returns the report to
                                      its Inspector with a reason that
                                      the system stores and notifies. The
                                      Inspector may edit the rejected
                                      report and resubmit it. While
                                      rejected, the report cannot be
                                      approved, opened, closed, deleted,
                                      or restored.

  **BR-06**                           Only a rejected report can be
                                      edited, and only by its own
                                      Inspector. Reports in Pending
                                      Approval, Open, or Closed states
                                      cannot be edited by anyone.

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

  **BR-12**                           When project closure is confirmed
                                      while Pending Approval reports
                                      exist, those pending reports are
                                      discarded as part of the closure
                                      operation, after the warning
                                      required by FR-35. (Resolved
                                      OQ-06.)

  **BR-13**                           Once a project is closed, Managers
                                      and Admin/HSE Engineers have
                                      read-only access during the
                                      retention period. Inspectors may
                                      view only their own Closed reports.
                                      No report may be deleted, restored,
                                      or otherwise changed.

  **BR-14**                           All data belonging to a closed
                                      project, including active and
                                      deleted reports, report versions,
                                      corrective actions, photograph
                                      files, notifications, audit
                                      history, and the project record
                                      itself, is permanently removed
                                      three months after project
                                      closure. (Resolved OQ-08.)

  **BR-15**                           Each project has exactly one
                                      Manager at any time. No report can
                                      be filed on a project until its
                                      Manager email is added. An
                                      Admin/HSE Engineer may reassign
                                      the Manager email while the
                                      project is open. Manager access
                                      follows the project's currently
                                      designated Manager, who holds the
                                      access of the previous Manager,
                                      including pending approvals.
                                      Project names are unique.
                                      (Resolved OQ-10 and A-02.)

  **BR-16**                           Admin and HSE Engineer are one role
                                      with higher authority than Manager,
                                      overseeing all projects.

  **BR-17**                           Corrective action is an action
                                      recorded by an Admin/HSE Engineer;
                                      it is not a status.

  **BR-18**                           The following happen outside the
                                      system: notice to the Admin/HSE
                                      Engineer that a matter is resolved,
                                      and account problems raised with
                                      Account Administration. The
                                      rejection reason is communicated
                                      in-system through the notification
                                      required by FR-42.

  **BR-19**                           Roles, teams, accounts, and
                                      Inspector-to-Manager assignments
                                      are decided by the company before
                                      use.

  **BR-20**                           Every submission and resubmission
                                      of a report creates an immutable
                                      version snapshot. Version history
                                      is retained with the report and
                                      purged only under FR-39.

  **BR-21**                           The system automatically records
                                      actor, action type, and timestamp
                                      for report closure, deletion,
                                      restoration, and Manager
                                      reassignment. No text reason is
                                      required from the actor.

  **BR-22**                           Corrective action is retained when
                                      a report is moved to the Deleted
                                      tab and is restored with the
                                      report if it is restored.
  -----------------------------------------------------------------------

## 8. Constraints

  -----------------------------------------------------------------------
  ID                                  Constraint
  ----------------------------------- -----------------------------------
  **CON-01**                          The product shall not include
                                      general in-system communication
                                      between users (chat, comments, or
                                      free-form messages). The only
                                      in-system message is the
                                      structured rejection notification
                                      required by FR-42.

  **CON-02**                          Admin and HSE Engineer shall be
                                      treated as one role.

  **CON-03**                          The technology stack is fixed by
                                      ADR-08: Java Spring Boot with
                                      Thymeleaf server-rendered HTML/CSS/
                                      JavaScript, MySQL, Spring Security,
                                      and local filesystem photograph
                                      storage, packaged as a single
                                      deployable application.

  **CON-04**                          The software relies on
                                      company-provided accounts and
                                      designations; it has no user or
                                      team management.

  **CON-05**                          The software does not verify that
                                      an entered Manager email belongs to
                                      a real Manager. The Manager email
                                      may be changed or reassigned by an
                                      Admin/HSE Engineer while the
                                      project remains open, as specified
                                      in FR-31 and FR-45.

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
2.  The Inspector fills in the project name, incident date/time,
    location, incident type, and one or more violations belonging to
    one incident (each with description, hazard category, severity,
    immediate action taken, and optional photographs).
3.  The system validates the form, project, and duplicate-violation
    rules.
4.  The Inspector submits the completed report. A version snapshot is
    saved.
5.  The report enters Pending Approval and has no Open/Closed status.
6.  The project's Manager sees the pending report. Admin/HSE Engineers
    do not.
7.  The Manager either:
    -   **Approves:** the report becomes Open; or
    -   **Rejects with a reason:** the report becomes Rejected, the
        reason is stored, and the system notifies the Inspector. The
        Inspector may edit the rejected report and resubmit it, which
        saves a new version snapshot and returns it to Pending
        Approval.
8.  Approved reports become visible according to the permission
    matrix.

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
3.  If Pending Approval reports exist, the system additionally warns
    that those pending reports will be discarded on confirmation.
4.  If the Admin/HSE Engineer declines, the project remains open and no
    reports change.
5.  If the Admin/HSE Engineer confirms, the system automatically closes
    all Open reports, discards all Pending Approval reports, and then
    closes the project.
6.  After closure, no Manager or Admin/HSE Engineer may change project
    reports. Admin/HSE Engineers and the Manager may view the project's
    reports read-only. Inspectors may view only their own Closed
    reports.
7.  The project's data is retained for three months after closure,
    starting from the exact project-closure timestamp.
8.  At the end of the three-month period, all data for the project,
    including the project record, reports in all states and versions,
    corrective actions, photograph files, notifications, and audit
    history, is permanently removed.

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

  **EC-15**               Manager rejects a       Report becomes
                          report                  Rejected with the
                                                  stored reason; the
                                                  system notifies the
                                                  Inspector, who may
                                                  edit and resubmit it.
                                                  While rejected it
                                                  cannot be approved,
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

  **EC-25**               Project closure         System warns that
                          attempted while Pending pending reports will
                          Approval reports exist  be discarded and
                                                  prompts for
                                                  confirmation. On
                                                  confirm, pending
                                                  reports are
                                                  discarded; on
                                                  decline, nothing
                                                  changes.

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

  **EC-30**               Three-month retention   All project data for
                          period expires          the project,
                                                  including the project
                                                  record, all reports
                                                  and versions,
                                                  corrective actions,
                                                  photo files,
                                                  notifications, and
                                                  audit history, is
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

  **EC-34**               Inspector edits and     New version snapshot
                          resubmits a rejected    saved; report returns
                          report                  to Pending Approval
                                                  and is revalidated
                                                  under FR-02 to FR-06.

  **EC-35**               Admin reassigns the     New Manager gains
                          Manager email while     pending approvals and
                          reports are pending     existing reports;
                                                  previous Manager
                                                  loses project access.
                                                  Audit entry recorded.

  **EC-36**               Admin creates a         Not accepted; message
                          project with a name     says the project name
                          that already exists     must be unique.

  **EC-37**               Another Inspector       Duplicate check
                          submits a violation     blocks it unless the
                          identical to one in a   matching report is in
                          rejected report         the Deleted tab or is
                                                  the report being
                                                  resubmitted.

  **EC-38**               Admin attempts to       Action is denied.
                          delete or restore a
                          rejected report
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

  **AC-12**               FR-11, FR-13, FR-42     Given a pending report,
                                                  when its Manager
                                                  rejects it with a
                                                  reason, then it
                                                  becomes Rejected, the
                                                  reason is stored, the
                                                  Inspector is notified
                                                  in-system, and the
                                                  report cannot be
                                                  approved, opened,
                                                  closed, deleted, or
                                                  restored while
                                                  rejected.

  **AC-13**               FR-12, FR-16, FR-41     Given an Admin/HSE
                                                  Engineer attempts to
                                                  view or act on a
                                                  pending report, then
                                                  access is denied.

  **AC-14**               FR-14                   Given an Inspector
                                                  views reports, then
                                                  they see only their own
                                                  pending, rejected (with
                                                  reason), Open, and
                                                  Closed reports, subject
                                                  to post-closure rules.

  **AC-15**               FR-15                   Given a Manager views
                                                  reports, then they see
                                                  pending, rejected,
                                                  Open, and Closed
                                                  reports for
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

  **AC-28**               FR-32, FR-33, FR-35     Given a project with
                                                  Pending Approval
                                                  reports, when an
                                                  Admin/HSE Engineer
                                                  attempts closure, then
                                                  a warning states the
                                                  pending reports will
                                                  be discarded and a
                                                  confirmation prompt
                                                  appears. If confirmed,
                                                  pending reports are
                                                  discarded and the
                                                  project closes; if
                                                  declined, nothing
                                                  changes.

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
                                                  all its data,
                                                  including the project
                                                  record, reports in all
                                                  states and versions,
                                                  corrective actions,
                                                  photo files,
                                                  notifications, and
                                                  audit history, is
                                                  permanently removed.
                                                  Given a project closed
                                                  less than three months
                                                  ago, then its data is
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

  **AC-34**               NFR-02                  Given a test load of
                                                  50 concurrent users,
                                                  1,000 reports, 50
                                                  projects, and 2,000
                                                  photographs, then at
                                                  least 95% of
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
                                                  general in-system
                                                  messaging feature
                                                  between users; the
                                                  only in-system message
                                                  is the structured
                                                  rejection notification
                                                  of FR-42.

  **AC-37**               FR-43, FR-44            Given a rejected
                                                  report, when its
                                                  Inspector edits and
                                                  resubmits it, then a
                                                  new version snapshot
                                                  is saved and the
                                                  report returns to
                                                  Pending Approval.

  **AC-38**               FR-31, FR-45, FR-46     Given an Admin/HSE
                                                  Engineer reassigns a
                                                  project's Manager
                                                  email while reports
                                                  are pending, then the
                                                  new Manager can view
                                                  and decide on the
                                                  pending reports, the
                                                  previous Manager loses
                                                  project access, and an
                                                  audit entry is
                                                  recorded.

  **AC-39**               FR-31                   Given an Admin/HSE
                                                  Engineer creates a
                                                  project with a name
                                                  that already exists,
                                                  then it is not
                                                  accepted and a message
                                                  says the name must be
                                                  unique.

  **AC-40**               FR-46, FR-47            Given an Admin/HSE
                                                  Engineer closes,
                                                  deletes, or restores a
                                                  report, then the audit
                                                  history records the
                                                  actor, action type,
                                                  and timestamp. Given a
                                                  deleted report with
                                                  corrective action is
                                                  restored, then the
                                                  corrective action is
                                                  restored with it.

  **AC-41**               FR-42, FR-43            Given a Manager
                                                  rejects a report with
                                                  a reason, then only
                                                  the owning Inspector
                                                  receives the
                                                  notification and only
                                                  that Inspector can
                                                  edit and resubmit the
                                                  report.
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

  I-7 Notify rejection and allow      FR-11, FR-13, FR-42 to FR-44,
  resubmission with version history   BR-05, BR-06, BR-20
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

  **R-02**                            On confirmed project closure,
                                      Pending Approval reports are
                                      discarded without any Admin/HSE
                                      Engineer having viewed them,
                                      because Admin/HSE Engineers cannot
                                      see pending reports.

  **R-03**                            RESOLVED. The system now records
                                      the rejection reason with the
                                      report, notifies the Inspector
                                      in-system, and records actor,
                                      action, and timestamp in the audit
                                      history (FR-42, FR-46).

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
                                      reports and discards all Pending
                                      Approval reports, even if an
                                      individual report has not
                                      separately been confirmed as
                                      resolved.
  -----------------------------------------------------------------------

## 14. Clashes Removed or Reconciled

  -----------------------------------------------------------------------
  Previous clash or ambiguity         Resolution in this revision
  ----------------------------------- -----------------------------------
  Rejection was described as          Rejected reports now carry a
  discarded, but its later lifecycle  stored reason, notify the
  was not fully explicit.             Inspector in-system, and may be
                                      edited and resubmitted by their
                                      Inspector, with version history
                                      retained (FR-42 to FR-44).

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
  automatic-close confirmation.       trigger their own warning;
                                      confirming discards them (FR-35,
                                      resolved OQ-06).

  Project closure did not clearly     Managers and Admin/HSE Engineers
  define post-closure change          have read-only access during
  permissions.                        retention; no report deletion or
                                      restoration is allowed after
                                      closure.

  Inspector access after project      Inspectors can view only their own
  closure was unclear.                Closed reports during retention.

  Retention wording could be read as  All project data, including the
  applying only to deleted reports.   project record, all reports and
                                      versions, corrective actions,
                                      photo files, notifications, and
                                      audit history, is permanently
                                      removed three months after
                                      project closure (resolved OQ-08).

  Closed reports were described as    Automatic closure is an explicit
  final, while project closure        project-closure operation; Closed
  automatically closes Open reports.  remains final after that operation.
  -----------------------------------------------------------------------

## 15. Open Questions — Resolved

All twelve open questions were resolved on 25 November 2026. The
answers are incorporated in the requirements above; this section is
retained for traceability.

  -----------------------------------------------------------------------
  ID      Resolution
  -------- --------------------------------------------------------------
  OQ-01    Standard HSE form. Report fields: project, incident date/time,
           location, incident type (accident or inspection finding).
           Violation fields: description, hazard category, severity
           level, immediate action taken, photograph(s). See FR-03.

  OQ-02    An incident is identified by the combination of incident
           date, location, and incident type. See FR-05.

  OQ-03    Corrective action is editable and deletable while the report
           is Open, and becomes immutable once Closed. Inspectors cannot
           view corrective action. See FR-20, FR-21.

  OQ-04    Confirmed: no report changes of any kind after project
           closure, including deletion. See FR-22, FR-29.

  OQ-05    Yes. The system records actor, action type, and timestamp for
           closure, deletion, restoration, and Manager reassignment. No
           text reason is required. See FR-46, BR-21.

  OQ-06    Pending Approval reports no longer block closure. On
           confirmed closure they are discarded after an explicit
           warning. See FR-35, BR-12.

  OQ-07    Confirmed: Admin may proceed with closure when Open reports
           exist; confirmation auto-closes them. See FR-32, FR-33.

  OQ-08    At retention expiry ALL project data is permanently purged:
           project record, reports in all states and versions,
           violations, corrective actions, photo files, notifications,
           and audit history. See FR-39, NFR-03, BR-14.

  OQ-09    Test load: 50 concurrent users, 1,000 reports, 50 projects,
           2,000 photographs; 95% of requests under 2 seconds. See
           NFR-02.

  OQ-10    One Manager may handle several projects (already established
           by ADR/architecture). The Manager email may be reassigned by
           an Admin while the project is open; the new Manager gains
           pending approvals and existing reports. Project names must
           be unique. See FR-31, FR-45, BR-15.

  OQ-11    Corrective action is retained when a report is deleted and
           restored with it if restored; purged with the report under
           FR-39. See FR-47, BR-22.

  OQ-12    Deletion, restoration, and other audited actions leave an
           audit-history entry (actor, action, timestamp) visible to
           Admin/HSE Engineers. See FR-46.
  -----------------------------------------------------------------------

## 16. Assumptions — Confirmed

All assumptions were confirmed on 25 November 2026, except A-03 which
was revised as noted.

  -----------------------------------------------------------------------
  ID      Status
  -------- --------------------------------------------------------------
  A-01     CONFIRMED. An Admin/HSE Engineer who records corrective
           action can also view it.

  A-02     CONFIRMED. Project names are unique, so name matching in
           FR-04 is unambiguous. Refusal of duplicate names is in
           FR-31.

  A-03     REVISED. The system DOES send an in-system notification
           carrying the rejection reason to the Inspector. The
           Inspector may edit the rejected report and resubmit it, and
           the version history of the report is saved. (Supersedes the
           earlier "no notifications" assumption; see FR-42, FR-43,
           FR-44.)

  A-04     CONFIRMED. The company's designation data tells the system
           which Manager an Inspector belongs to, so FR-04 can compare
           it with the project's Manager.

  A-05     CONFIRMED. A restored report returns to the active Closed
           list because only Closed reports can be deleted.

  A-06     CONFIRMED. The three-month retention period begins at the
           exact date/time the project is closed.

  A-07     CONFIRMED. The Deleted tab remains accessible to Admin/HSE
           Engineers in read-only mode during the post-closure
           retention period.

  A-08     CONFIRMED. Project closure does not delete the project
           record immediately; project data remains available for the
           retention period under the role permissions in §4, then is
           purged entirely under FR-39.
  -----------------------------------------------------------------------
