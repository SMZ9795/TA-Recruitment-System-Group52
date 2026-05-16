# TA Job Recommendation Manual Test

## Scope
This checklist verifies the iteration 4 TA job recommendation feature, including the sorting logic, the recommendation details visibility, and the recommended jobs filter.

## Preconditions
- Run the Swing UI with `java -cp out com.group52.tarecruitment.SwingMain`.
- Log in as a TA account (`TA231226244` or any generic TA).
- Ensure the TA has a populated profile with explicit `Skills` (e.g., `Java, Python, SQL`) and `Available Hours` (e.g., `10`).

## Test Steps
1. **Initial Recommendation Default:** Login as the TA and navigate to the `Job Board`. Verify that the jobs are automatically sorted: highly recommended jobs (highest AI fit score) should appear at the top.
2. **Low-Match Forward Check:** Scroll to the bottom of the `Job Board`. Verify that jobs with `Low Fit` label or $0\%$ score (e.g., missing all required skills or exceeding weekly hours capability) are positioned strictly at the bottom.
3. **Recommendation Details Verification:** Select a `Recommended` job near the top. In the job details panel on the right, verify the following lines accurately reflect the TA's profile:
   - `AI Fit:` Contains the score and the `Recommended` label.
   - `Matched skills:` Lists skills matching the job requirements (e.g., `Java`).
   - `Missing skills:` Lists required skills absent from the TA profile.
   - `Hours check:` Identifies if the TA has enough available hours for the job, indicating "fits current availability" or "needs review".
4. **Filter Recommended Only:** Check the `Recommended only` checkbox at the bottom of the filter panel. Click `Search`. Verify that the table only displays jobs labelled as `Recommended`.
5. **Partial Match Scenario:** Update the TA's skills via `My Profile` so that only one or two skills match a specific job rather than all of them. Go back to `Job Board` and observe the job re-positioned lower in the view, gaining a `Review` label if the score dropped to <80% but >=50%.

## User Manual Screenshots Supplement
For the Day 3 User Manual update, please collect the following screenshots:
1. **Screenshot 1:** The `Job Board` sorted by recommendation. Ensure the `AI Fit` and `Recommendation` columns are clearly visible, illustrating `Recommended` at the top and `Low Fit` at the bottom.
2. **Screenshot 2:** The lower right `Job Details` panel. Highlight the `Why this ranking?` explanation, emphasizing `Matched skills`, `Missing skills`, and `Hours check`.
3. **Screenshot 3:** The filter panel with the `Recommended only` checkbox ticked, showing a fully filtered job list of perfectly matching opportunities.