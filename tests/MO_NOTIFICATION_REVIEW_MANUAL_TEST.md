# MO Notification & Review Manual Test

1. Start the Swing application and login with an MO account from `data/users.csv`.
2. On the MO Dashboard, check the **MO Notifications** card above the posted jobs table.
   It should show readable reminders with job title, applicant name, application status, match score when available, pending count, and filled-job reminders.
   If the MO has no pending applications or filled jobs, it should show a clear empty-state message.
3. Select a job in **My Posted Jobs** and click **View Applicants**.
4. In the Applicants page, try these controls:
   - **Pending only**: shows only applications with `PENDING` status.
   - **Needs decision**: shows applications that can still be accepted or rejected (`APPLIED`, `REVIEWING`, or `PENDING`).
     Selecting it clears **Pending only** so the broader decision queue is visible.
   - **High match first**: sorts applicants by match score from high to low.
5. Select a pending applicant and click **Accept** or **Reject**.
6. Verify immediately, without logging out:
   - the applicants table refreshes and the reviewed applicant leaves the pending-only view,
   - the MO notification pending count changes,
   - the MO notification card refreshes,
   - the dashboard job row refreshes,
   - if accepted applicants now equal the number of positions, the job status becomes `FILLED`.
