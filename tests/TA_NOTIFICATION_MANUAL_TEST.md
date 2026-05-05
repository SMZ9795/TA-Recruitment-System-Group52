# TA Notification Center Manual Test

## Scope
This checklist verifies the iteration 3 TA notification center and dashboard status visualization.

## Preconditions
- Run the Swing UI with `java -cp out com.group52.tarecruitment.SwingMain`.
- Use a TA account with at least one application in `PENDING`, `ACCEPTED`, `REJECTED`, or `WITHDRAWN` state.
- Keep at least one closed job with a pending TA application available for the closed-job alert scenario.

## Test Steps
1. Log in as a TA and open the Dashboard.
2. Verify the Dashboard shows the application status summary with pending, accepted, rejected, and withdrawn counts.
3. Click `View Notifications` and verify the Notifications page opens.
4. Verify the notification table shows application submitted, accepted, rejected, withdrawn, and job closed messages when matching data exists.
5. Switch the filter between `All`, `Unread`, and `Read`.
6. Select a notification and click `Mark Read`, then verify the unread count decreases and the notification moves to the `Read` filter.
7. Select a read notification and click `Mark Unread`, then verify the unread count increases and the notification moves back to the `Unread` filter.
8. Return to Job Board, apply for an open job, and verify the Dashboard feedback says the notification center has refreshed.
9. Withdraw a pending application and verify both the Dashboard summary and notification center update.
10. Try to apply for a closed job and verify the Dashboard shows a clear closed-job warning.

## Screenshots To Capture
- TA Dashboard with application status summary and latest notification line.
- Notification Center with unread count and `All` filter.
- Notification Center after marking one notification as read.
- Closed-job warning after attempting to apply for a closed job.
