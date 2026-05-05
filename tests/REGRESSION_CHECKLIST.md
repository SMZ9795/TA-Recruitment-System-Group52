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

## Manual GUI regression (SwingApp)
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

