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

