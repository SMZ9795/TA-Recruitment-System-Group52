# TA Recruitment Regression Checklist

## Automated test coverage (`RecruitmentSystemTestRunner`)
- CV field persistence in `users.csv` after restart.
- Backward compatibility for legacy 10-column `users.csv` rows.
- TA profile update persistence, including CV path.
- CV validation rules (`.pdf/.txt`, max 5MB).
- Multi-condition job filter matching logic.
- Register/login happy path and common failures.
- Job validation (deadline not in past, positions vs accepted applications).
- Application authorization and status-transition constraints.
- Job deletion guard when related applications exist.
- TA notification filtering, unread count, status summary, and closed-job alerts.
- End-to-end integration (TA profile -> apply -> MO review -> admin workload).
- **[iteration3]** AdminService risk level uses TA's own `availableHours` (not hardcoded 20h).
- **[iteration3]** `getRecruitmentSnapshot()` correctly counts filled jobs and overloaded TAs.
- **[iteration3]** Job lifecycle: closing an OPEN job blocks new TA applications.
- **[iteration3]** Job lifecycle: reopening a CLOSED job restores apply, but is rejected when the deadline has passed or the job is already FILLED.
- **[iteration3]** Job lifecycle: `autoCloseExpiredJobs` sweeps all OPEN jobs past deadline; an apply attempt against a stale OPEN job self-heals to CLOSED.
- **[iteration4]** `ExportService` writes CSV files to `data/exports/` with timestamped filenames, header-only output for empty datasets, and unique filenames on repeated exports.
- **[iteration4]** `ExportService` CSV content carries the documented headers and per-row field values (all applications, TA workload summary, job filling status, MO applicant list).

## Manual GUI regression (SwingApp)

### Admin panel — iteration 3 checks
1. **Summary bar visible**: After logging in as Admin, the top of the dashboard shows four cards:
   "Total Jobs", "Filled Jobs", "Overloaded TAs", "High-Risk TAs" with numeric values.
2. **Summary updates on refresh**: Click Refresh in the Workload tab; card numbers reflect current data.
3. **Workload table columns**: Verify columns are
   `TA ID | TA Name | Available h/week | Assigned h/week | Remaining h | Risk`.
   The old "Alert" column and `> 20h` logic should be gone.
4. **Risk levels correct**:
   - A TA with 10 available h and 9 assigned h shows **At Risk**.
   - A TA with 10 available h and 11 assigned h shows **Overloaded**.
   - A TA with 10 available h and 5 assigned h shows **OK**.
5. **Remaining hours column**: Verify value is `max(0, availableHours - assignedHours)`.

### MO panel — iteration 3 job lifecycle
1. **Manual close**: Open MO Dashboard, select an OPEN job, click `Close Job`, confirm.
   - Status badge turns red and shows `CLOSED`.
   - From a TA account, attempting to apply to that job shows the closed-job warning.
2. **Manual reopen**: Select the CLOSED job, click `Reopen Job`, confirm.
   - Status flips back to `OPEN`; TA applies succeed again.
3. **Reopen guard**: For a CLOSED job whose deadline has already passed, `Reopen Job` shows
   "Cannot reopen a job whose deadline has passed."
4. **Filled-job guard**: Reopening a job that already has all positions filled is rejected
   with "filled" in the error message.
5. **Auto-close on entry**: With the dashboard open, advance the system clock past a job's
   deadline (or edit `data/jobs.csv` to a past date) and click `Refresh`.
   - The job becomes `CLOSED` automatically and a toast shows the count of auto-closed jobs.
6. **Edit preserves CLOSED**: Editing a CLOSED job's fields keeps it CLOSED; reopening must be
   done via the explicit `Reopen Job` button.

### Admin & MO panels — iteration 4 CSV exports
1. **Admin Workload Overview** → click `Export Workload CSV`. A success dialog shows the path
   under `data/exports/ta_workload_summary_<yyyyMMdd_HHmmss>.csv`. The file opens in a spreadsheet
   with the documented header row and one row per TA that has accepted positions.
2. **Admin Jobs Overview** → click `Export Job Filling CSV`. The exported file lists every job
   with `positions`, `filledPositions`, `remainingPositions`, and a `filled/total` ratio.
3. **Admin Audit Log** → click `Export All Applications CSV`. Every application row contains
   the joined `moduleCode`, `moduleName`, `taName`, and `taEmail` columns.
4. **MO Applicants tab** → open a job's applicants from Dashboard, click `Export Applicants CSV`.
   The file contains the TA profile snapshot (programme, year, skills, available hours) plus the
   application status. Trying to export without first opening a job shows a friendly warning.
5. **Empty-data export** → with no applications in the system, the All-Applications export still
   writes a header-only file (no error). The success dialog still reports the file path.
6. **Repeated export** → click an export button twice in the same second. Two distinct files
   appear in `data/exports/` (the second one gets a numeric suffix appended).

### Pre-existing GUI regression
1. TA profile CV upload:
   - Select `.pdf` and `.txt` files and save profile.
   - Re-open profile page and verify uploaded filename is shown.
   - Restart app and verify CV filename/path still available.
2. TA CV upload validation:
   - Try `.docx` file and confirm clear validation message.
   - Try file larger than 5MB and confirm clear validation message.
3. MO applicant details:
   - In applicant list, click `View Applicant Details`.
   - Verify name/year/programme/skills/available hours/CV info are shown.
4. Job board filters:
   - Apply keyword + skills + max hours + MO + status together.
   - Verify clear/reset and refresh behavior.
5. Error handling:
   - Invalid login, duplicate registration, past deadline job creation,
     duplicate application, unauthorized review, non-pending withdrawal.
6. TA notification center:
   - Verify Dashboard status summary counts pending/accepted/rejected/withdrawn applications.
   - Verify notification filters, unread count, Mark Read, and Mark Unread.
   - Verify closed-job application attempts show a clear TA warning.

## Manual GUI regression (SwingApp) — Iteration 4

### TA panel — Job Recommendation (Hanyu Xiao)
1. **Recommended Jobs section visible**:
   - After logging in as TA, the Job Board shows both "All Jobs" and "Recommended Jobs" sections.
   - Recommended jobs are sorted by match score (highest first).
2. **Recommendation scores displayed**:
   - Each recommended job shows a score badge (e.g., "92% Match", "Score: 8.2/10").
3. **Match reason details**:
   - Click or hover on a recommended job to view:
     - "Matched skills: Java, SQL"
     - "Missing skills: Python, C++"
     - "Hours fit: Yes" or "Hours fit: No (requires 20h, available: 10h)"
     - "Programme match: Yes/No"
4. **Recommendation ordering**:
   - Verify high-match jobs appear before low-match jobs.
   - After updating profile (skills/hours), refresh and verify re-ranking.
5. **Low-match filtering**:
   - Jobs with <50% match are shown in separate "Low Match" section or marked clearly.

### MO panel — Applicant Filtering (Mengzhe Shi)
1. **Filter buttons visible**:
   - Applicant list shows three filter buttons:
     - "Pending Only" (shows only PENDING applications)
     - "High Match First" (sorts by recommendation score, highest first)
     - "Needs Decision" (shows only PENDING/WITHDRAWN applications)
2. **Filter behavior**:
   - Clicking "Pending Only" hides ACCEPTED/REJECTED applications.
   - Clicking "High Match First" reorders by score (if score not available, show default order).
   - Clicking "Needs Decision" shows mixed PENDING and WITHDRAWN.
3. **Filter reset**:
   - Clicking filter button again deactivates it; applicant list returns to original order.
4. **Notification refresh after action**:
   - After accepting an applicant: notification bar updates, job status shows filled if all positions taken.
   - After rejecting an applicant: notification bar updates.

### Admin panel — Workload Balancing Suggestions (Yucheng Liu)
1. **Workload Balancing tab visible**:
   - Admin dashboard shows a new "Workload Balancing" tab/section.
2. **Suggestions table columns**:
   - `TA ID | TA Name | Status (Overloaded/Balanced/Underused) | Assigned h/week | Capacity | Suggestion`.
3. **Status classification**:
   - "Overloaded": assigned hours > available hours (shown in red).
   - "Balanced": assigned hours ≤ available hours (shown in green).
   - "Underused": assigned hours < 50% of available hours (shown in yellow).
4. **Suggestion content**:
   - Example: "Move 4h/week from TA A to TA B" or "TA C has 6h/week available capacity".
   - Suggestions are explainable and rule-based (no external AI calls).
5. **Refresh updates**:
   - Clicking Refresh recalculates all workload statuses and suggestions based on current accepted applications.

### Admin panel — Export Functionality (Wang Xiao)
1. **Export buttons visible**:
   - Admin dashboard shows three export buttons:
     - "Export All Applications"
     - "Export TA Workload Summary"
     - "Export Job Filling Status"
2. **CSV file generation**:
   - Each export creates a timestamped CSV file (e.g., `applications_2026-05-17_143025.csv`).
   - Files are saved to `data/exports/` directory.
3. **Export content verification**:
   - **Applications CSV**: columns are `Application ID | TA ID | Job ID | Status | Applied Date | Decision Date`.
   - **Workload Summary CSV**: columns are `TA ID | TA Name | Available h/week | Assigned h/week | Remaining h | Status`.
   - **Job Filling Status CSV**: columns are `Job ID | Job Title | Total Positions | Filled Positions | Status | Deadline`.
4. **Empty data handling**:
   - Export with no applications/TAs/jobs still creates valid CSV with headers only.
5. **File write success toast**:
   - After export, a toast message shows "Export successful: applications_2026-05-17_143025.csv".
6. **Duplicate export guard**:
   - Exporting twice in quick succession creates two separate files with different timestamps.

### MO panel — Export Applicant List (Wang Xiao)
1. **Export button on job detail**:
   - MO opens a job detail view and sees an "Export Applicants" button.
2. **Applicant list CSV**:
   - Clicking export creates `applicants_<job_id>_<timestamp>.csv` in `data/exports/`.
   - Columns: `Applicant ID | TA Name | TA Email | Status | Match Score | Applied Date`.
3. **Export toast**:
   - Success message shows filename and location.

### Account Security — Password Change (Zhixing Sun)
1. **Change Password modal**:
   - All users (TA/MO/Admin) see a "Change Password" option in menu or account settings.
   - Modal shows three input fields:
     - "Current Password" (masked)
     - "New Password" (masked)
     - "Confirm New Password" (masked)
2. **Validation checks**:
   - Current password is verified against stored hash; incorrect password shows error.
   - New password must be ≥ 8 characters; show error if < 8.
   - New password and confirm must match; show error if different.
   - Fields cannot be empty; show error for blank input.
3. **Success flow**:
   - After correct password change, modal closes and toast shows "Password changed successfully".
4. **Failure cases**:
   - Incorrect current password: "Current password is incorrect".
   - Password mismatch: "Passwords do not match".
   - Password too short: "Password must be at least 8 characters".
5. **Login with new password**:
   - Log out and log back in with new password; verify successful login.
6. **Admin reset password preserved**:
   - Admin can still reset other users' passwords via user management panel.

## Automated test coverage — Iteration 4 (Automated integration tests)
- TA Job Recommendation algorithm: high-match jobs ranked before low-match.
- Recommendation score calculation: skills match, hours fit, programme match.
- Low-match jobs (<50% score) properly flagged or separated.
- Export functionality: CSV files created with correct headers and content.
- Export file timestamps and directory creation (`data/exports/`).
- Workload status classification: Overloaded/Balanced/Underused.
- Workload suggestions are generated for each status.
- Password change: correct old password allows change; incorrect blocks.
- Password validation: length, confirmation, empty input checks.
- MO notification refresh after accept/reject decision.
- Job status updates to FILLED when all positions accepted.
- Filter behavior: "Pending Only", "High Match First", "Needs Decision" toggle correctly.
- End-to-end Iteration 4 flow: TA views recommendation → applies → MO reviews with filters → Admin reviews workload/export suggestions.

