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
- End-to-end integration (TA profile -> apply -> MO review -> admin workload).
- **[iteration3]** AdminService risk level uses TA's own `availableHours` (not hardcoded 20h).
- **[iteration3]** `getRecruitmentSnapshot()` correctly counts filled jobs and overloaded TAs.

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

