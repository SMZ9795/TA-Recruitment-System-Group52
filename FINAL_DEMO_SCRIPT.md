# TA Recruitment System - Final Demo Script (10 minutes)

## Demo Overview
**Total Duration**: 10 minutes  
**Presenters**: 6 team members (Conghao Li, Hanyu Xiao, Wang Xiao, Yucheng Liu, Mengzhe Shi, Zhixing Sun)  
**Audience**: Module lead, project assessors  
**Goal**: Demonstrate all Iteration 4 features in a complete end-to-end workflow

---

## Pre-Demo Checklist

### Setup Requirements
- [ ] Application compiled: `javac -d bin src/com/group52/tarecruitment/**/*.java`
- [ ] Test data prepared in `data/` directory (users.csv, jobs.csv, applications.csv, notifications.csv)
- [ ] Application window ready to launch: `java -cp bin com.group52.tarecruitment.SwingMain`
- [ ] Projector/screen share tested
- [ ] All demo accounts created and accessible
- [ ] Test credentials printed and available

### Demo Environment Setup
```bash
# Navigate to project directory
cd c:\Users\Lenovo\TA-Recruitment-System-Group52

# Compile if needed
javac -d bin src/com/group52/tarecruitment/**/*.java

# Run application
java -cp bin com.group52.tarecruitment.SwingMain
```

### Test Credentials
| User | ID | Password | Role |
|------|----|----|------|
| Hanyu Xiao | TA231226244 | Password1! | TA (Demo Lead) |
| Module Organizer | MO001 | Password1! | MO |
| System Admin | ADMIN01 | Password1! | Admin |

---

## Demo Script Breakdown

### **Segment 0: Introduction (1 minute) — Conghao Li**

**Purpose**: Set context, explain system overview, introduce team

**Script:**
```
"Good [morning/afternoon] everyone. I'm Conghao Li, and I'm leading the 
final demo of our TA Recruitment System.

This is a comprehensive platform designed to streamline Teaching Assistant 
recruitment at BUPT International School. Today, you'll see how our system 
brings together:
- Teaching Assistants seeking positions
- Module Organizers managing recruitment
- Administrators overseeing workload and reporting

Our system demonstrates modern software engineering practices including:
- Service-oriented architecture
- User role-based access control
- Data persistence with CSV storage
- End-to-end testing coverage

In this 10-minute demo, we'll walk through the complete recruitment lifecycle—
from job posting to workload analysis. Let me introduce my teammates who will 
guide you through each section."

*[Point to each team member as introduced]*

"Hanyu Xiao will show TA-side job recommendations, Wang Xiao will demonstrate 
export functionality, Yucheng Liu will cover workload balancing, Mengzhe Shi 
will show MO review features, and Zhixing Sun will demonstrate password security.

Let's begin by launching the application."

**Action**:
- Launch application
- Show login screen
- Pause for questions to be ready
```

**Timing**: 1 minute (0:00 - 1:00)

---

### **Segment 1: TA Features & Job Recommendations (2 minutes) — Hanyu Xiao**

**Purpose**: Demonstrate job recommendation algorithm and TA features

**Script:**
```
"Thank you, Conghao. I'm Hanyu Xiao, responsible for TA-side job 
recommendations.

[LOGIN STEP]
Let me log in as a TA who's looking for suitable positions. I'll use the 
credentials for TA231226244.

[AFTER LOGIN]
As you can see, after logging in, the TA is immediately presented with 
application statistics. Here we can see:
- 2 pending applications
- 1 accepted position
- 0 rejections
- 0 withdrawals

[NAVIGATE TO JOB BOARD]
Now let's navigate to the Job Board. Notice that we have TWO distinct 
sections:

[SHOW RECOMMENDED JOBS SECTION]
First, the RECOMMENDED JOBS section at the top. This is powered by our 
AI matching service, which analyzes:
1. Skill alignment with required skills
2. Available hours vs. job hours
3. Programme/department match
4. Historical application success

These recommended jobs are automatically sorted by match score, highest first. 
For example, this 'AI Lab Assistant' job shows 92% match because:
- Matched skills: Java, Python, SQL
- Missing skills: None
- Hours fit: Yes (available 20h, job needs 10h)
- Programme match: Yes

[SCROLL TO ALL JOBS]
Below, we have the complete job board with search filters. Let me apply to 
this recommended job as an example.

[CLICK 'APPLY NOW']
I'll click 'Apply Now' to demonstrate the application flow.

[SHOW CONFIRMATION DIALOG]
The system confirms my application details. Notice the system clearly shows 
which job I'm applying for and tracks the application status.

[CLICK SUBMIT]
After submission, we see a success notification confirming the application 
was received.

This recommendation system significantly improves the TA experience by:
- Reducing time spent searching
- Prioritizing relevant opportunities
- Explaining why certain jobs are recommended
- Incorporating multiple matching criteria

Next, I'll hand over to Wang Xiao to show the export functionality."

**Actions**:
1. Login with TA231226244
2. Show dashboard with application summary
3. Navigate to Job Board
4. Show Recommended Jobs section with match scores
5. Point out match explanation (skills, hours, programme)
6. Scroll down to All Jobs section
7. Click on a recommended job
8. Click "Apply Now"
9. Show confirmation dialog
10. Show success toast
11. Return to Job Board to show updated application status

**Timing**: 2 minutes (1:00 - 3:00)

---

### **Segment 2: Export Functionality (1.5 minutes) — Wang Xiao**

**Purpose**: Demonstrate export capabilities for reporting

**Script:**
```
"Hello, I'm Wang Xiao. I'm responsible for the export and reporting features 
that help administrators monitor system activity.

[LOGOUT FROM TA]
Let me logout from the TA account and log in as an administrator so we can 
access the export features.

[LOGIN AS ADMIN]
Logging in as ADMIN01...

[SHOW ADMIN DASHBOARD]
Welcome to the Admin Dashboard. At the top, we see summary cards showing:
- Total Jobs: 8
- Filled Jobs: 3
- Overloaded TAs: 2
- High-Risk TAs: 1

[NAVIGATE TO EXPORT SECTION]
Now let's look at the export features. I'll navigate to the Reports/Export 
section. Here we have three main export options:

[SHOW EXPORT BUTTONS]
1. 'Export All Applications' - generates a comprehensive CSV of all applications
2. 'Export TA Workload Summary' - shows current workload distribution
3. 'Export Job Filling Status' - tracks recruitment progress

Let me click on 'Export All Applications' to demonstrate.

[CLICK EXPORT BUTTON]
[WAIT FOR SUCCESS MESSAGE]

Perfect! The system shows: 'Export successful: applications_2026-05-17_143025.csv'

Notice the filename includes:
- Descriptive name (applications)
- Timestamp with date and time (2026-05-17_143025)
- File location: data/exports/

This timestamp ensures:
- No file overwriting with duplicate exports
- Clear identification of when the export was created
- Audit trail for compliance purposes

[CLICK EXPORT WORKLOAD SUMMARY]
Let me export the Workload Summary as well. This helps admins track TA 
capacity allocation.

[SHOW SUCCESS MESSAGE]
'Export successful: workload_summary_2026-05-17_143026.csv'

[CLICK EXPORT JOB FILLING STATUS]
And finally, Job Filling Status to monitor recruitment progress across all jobs.

[SHOW SUCCESS MESSAGE]
'Export successful: job_status_2026-05-17_143027.csv'

The exported CSV files contain:
- **Applications**: App ID, TA ID, Job ID, Status, Dates
- **Workload**: TA ID, Name, Available h/week, Assigned h/week, Remaining h, Status
- **Job Status**: Job ID, Title, Total Positions, Filled, Status, Deadline

These exports are critical for:
- Compliance and audit requirements
- Data analysis and planning
- Integration with external systems
- Backup and disaster recovery

Next, Yucheng Liu will show how the system balances workload intelligently."

**Actions**:
1. Logout from TA
2. Login as ADMIN01
3. Show Admin Dashboard with summary cards
4. Scroll to Export section
5. Click "Export All Applications"
6. Show success toast with filename and path
7. Click "Export TA Workload Summary"
8. Show success toast
9. Click "Export Job Filling Status"
10. Show success toast

**Timing**: 1.5 minutes (3:00 - 4:30)

---

### **Segment 3: AI Workload Balancing (1.5 minutes) — Yucheng Liu**

**Purpose**: Demonstrate intelligent workload analysis and suggestions

**Script:**
```
"Good day, I'm Yucheng Liu. I manage the workload balancing features that 
help admins ensure fair distribution of TA responsibilities.

[SHOW WORKLOAD TAB]
Looking at the Workload Balancing section on the Admin Dashboard, we see a 
comprehensive table analyzing each TA's capacity.

[POINT OUT TABLE COLUMNS]
The system displays:
- TA ID and Name
- Available hours per week (set by TA)
- Assigned hours per week (from accepted jobs)
- Remaining capacity
- Current status classification
- Personalized suggestions

[EXPLAIN STATUS CLASSIFICATION]
The system classifies workload into three categories:

[SHOW BALANCED STATUS - GREEN]
'BALANCED' TAs (shown in green) have:
- Assigned hours ≤ available hours
- Utilizing more than 50% of capacity
- Example: TA A has 20h available, 12h assigned = 60% utilization ✓

[SHOW OVERLOADED STATUS - RED]
'OVERLOADED' TAs (shown in red) have:
- Assigned hours > available hours
- At risk of burnout
- Example: TA B has 15h available but 18h assigned
- System flags this immediately

[SHOW UNDERUSED STATUS - YELLOW]
'UNDERUSED' TAs (shown in yellow) have:
- Assigned hours < 50% of available capacity
- Significant untapped potential
- Example: TA C has 20h available but only 5h assigned = 25% utilization

[POINT OUT SUGGESTIONS]
For each TA, we have explainable suggestions:
- 'Move 4h/week from TA A to TA B' - specific rebalancing action
- 'TA C has 6h/week available capacity' - identifies underutilized resources
- These are rule-based suggestions, no black-box AI

[EXPLAIN ALGORITHM]
Our workload algorithm uses:
1. **Capacity Analysis**: Actual hours vs. available hours
2. **Utilization Metric**: Percentage of capacity in use
3. **Risk Assessment**: Burnout prediction
4. **Matching Engine**: Skills-aware load balancing recommendations

[SHOW REFRESH CAPABILITY]
The system updates in real-time. When MO makes decisions (accept/reject 
applications), workload status refreshes automatically.

This approach helps admins:
- Proactively prevent TA burnout
- Identify underutilized resources
- Make data-driven scheduling decisions
- Ensure fair workload distribution

Next, Mengzhe Shi will demonstrate how MO reviewers use our enhanced 
notification and filtering system."

**Actions**:
1. Show Workload Balancing table
2. Point out column headers (TA ID, Name, Available, Assigned, Remaining, Status, Suggestion)
3. Highlight color-coded status (Green/Balanced, Red/Overloaded, Yellow/Underused)
4. Point out specific TA examples with different statuses
5. Read a suggestion example
6. Click Refresh to show real-time update capability
7. Show summary statistics (e.g., "2 Overloaded TAs, 3 Balanced, 1 Underused")

**Timing**: 1.5 minutes (4:30 - 6:00)

---

### **Segment 4: MO Notification & Review Polish (2 minutes) — Mengzhe Shi**

**Purpose**: Show MO review features and notifications

**Script:**
```
"Hello, I'm Mengzhe Shi, responsible for Module Organizer (MO) notification 
and review features.

[LOGOUT AND LOGIN AS MO]
Let me switch to the MO account to show the review interface.

Logging in as MO001...

[SHOW MO DASHBOARD]
The MO Dashboard shows:
- Total jobs posted
- Applications awaiting review
- Recent notifications
- Job status overview

[NAVIGATE TO JOB/APPLICANTS]
Let me navigate to view applicants for one of our open positions.

[SHOW APPLICANT LIST]
Here we see the applicant list for this job. But notice something important—
we have three new filtering options that significantly improve review workflow:

[HIGHLIGHT FILTER BUTTONS]
1. **'Pending Only'** - Shows only applications awaiting decision
   Click to toggle: now we only see applications that need our attention

2. **'High Match First'** - Sorts by recommendation score
   This reorders applicants so the best matches appear first
   Helps MOs quickly identify strongest candidates

3. **'Needs Decision'** - Shows pending AND withdrawn applications
   Useful for finding applications that need action

[SHOW BEFORE/AFTER FILTERING]
Without filters: Mixed statuses, random order
With 'High Match First': Highest scored applicants appear first
With 'Pending Only': Only actionable applications shown

[CLICK ON AN APPLICANT]
Let me view details for this top-matched applicant.

[SHOW APPLICANT DETAILS]
We see comprehensive information:
- TA name, email, ID
- Year of study, programme
- Available hours per week
- Skills list
- CV file reference
- Match explanation: 'Matched: Java, Python | Missing: SQL | Hours Fit: Yes'

This match explanation directly comes from our AI matching service and helps 
MOs understand recommendation reasoning.

[CLICK ACCEPT]
I'll accept this application to demonstrate the decision flow.

[SHOW CONFIRMATION]
Confirmation dialog appears with application details.

[CONFIRM ACCEPTANCE]
[SHOW SUCCESS TOAST]
'Application accepted. TA notified.'

[WATCH FOR STATUS UPDATE]
Notice:
- Applicant status changes to 'ACCEPTED'
- Notification sent to TA
- If this was the last available position, job status automatically changes to 'FILLED'

[SHOW NOTIFICATIONS]
In the TA's notification center, they receive:
- 'Your application to [Job Title] was ACCEPTED'
- Timestamp of acceptance
- Option to view more details

[SHOW MO DASHBOARD UPDATE]
The MO dashboard automatically reflects:
- Updated application count
- Changed job status (if filled)
- Fresh notification count

This seamless notification system ensures:
- Timely communication to all stakeholders
- Automatic status synchronization
- Clear audit trail of all decisions

Next, Zhixing Sun will demonstrate the password security and account management 
features."

**Actions**:
1. Logout from Admin
2. Login as MO001
3. Show MO Dashboard
4. Navigate to Jobs
5. Select a job with pending applications
6. Show applicant list
7. Demonstrate filter buttons:
   - Click "Pending Only" to filter
   - Click "High Match First" to sort
   - Click "Needs Decision" to show mixed statuses
8. Show filter toggles reverting
9. Click on an applicant to view details
10. Show match explanation
11. Click "Accept" button
12. Show confirmation dialog
13. Confirm acceptance
14. Show success notification
15. Show applicant status changed to "ACCEPTED"
16. (Optional) Show job status changed to "FILLED" if last position

**Timing**: 2 minutes (6:00 - 8:00)

---

### **Segment 5: Password Security & Account Management (1 minute) — Zhixing Sun**

**Purpose**: Demonstrate account security features

**Script:**
```
"Hello, I'm Zhixing Sun. I'm responsible for account security and password 
management features that protect our system.

[SHOW ACCOUNT MENU]
All users—whether TA, MO, or Admin—have access to the 'Change Password' feature 
through their account menu or settings.

Let me click on 'Change Password' to demonstrate.

[SHOW PASSWORD CHANGE DIALOG]
The password change interface includes three fields:
1. Current Password - Must match the account's actual password
2. New Password - Must meet strength requirements
3. Confirm Password - Must match the new password

[EXPLAIN PASSWORD REQUIREMENTS]
New passwords must contain:
✓ At least 8 characters
✓ At least one uppercase letter (A-Z)
✓ At least one lowercase letter (a-z)
✓ At least one digit (0-9)
✓ At least one special character (!@#$%^&*)
✓ Must be different from current password

[DEMONSTRATE VALIDATION]
Let me try entering a weak password...

[TYPE WEAK PASSWORD]
[SHOW ERROR MESSAGE]
'Password must contain uppercase, lowercase, digit, and special character'

The system provides clear feedback about what's missing.

[ENTER STRONG PASSWORD]
Let me enter a properly formatted password: 'NewSecure123!'

[CONFIRM PASSWORD]
I'll confirm with the same password.

[CLICK CHANGE]
[SHOW SUCCESS MESSAGE]
'Password changed successfully'

[EXPLAIN SECURITY FEATURES]
Our password system includes:
1. **Strength Validation**: Enforced at registration AND password change
2. **Failed Login Locking**: 5 failed attempts locks account for 15 minutes
3. **Login Attempt Tracking**: Prevents brute force attacks
4. **Password Hashing**: Passwords never stored in plain text
5. **Admin Reset**: Admins can reset passwords, but users can also self-serve

[SHOW LOGIN WITH NEW PASSWORD]
After logging out and back in with the new password, we can verify it works:

[LOGOUT]
[LOGIN WITH NEW PASSWORD]
[SUCCESS LOGIN]

This demonstrates both the password security AND the successful credential update.

[EXPLAIN DESIGN CHOICE]
Why this approach:
- Empowers users to maintain their own account security
- Reduces support burden on administrators
- Clear error messages guide proper password selection
- Account lockout prevents brute force attacks
- Transparent process maintains user trust

This concludes the security demonstration. Thank you all for attending this demo."

**Actions**:
1. Navigate to account/settings menu
2. Click "Change Password"
3. Show password change dialog
4. Attempt entering weak password (e.g., "weak")
5. Show validation error message
6. Enter strong password (e.g., "NewSecure123!")
7. Confirm password
8. Click "Change Password"
9. Show success toast message
10. Logout from current session
11. Login with new password
12. Show successful login
13. Verify user is logged in with new credentials

**Timing**: 1 minute (8:00 - 9:00)

---

### **Segment 6: Closing & Q&A (1 minute) — Conghao Li**

**Purpose**: Summarize demo, invite questions, thank audience

**Script:**
```
"Thank you to all our teammates for demonstrating our features in action.

In the past 9 minutes, we've showcased the complete TA Recruitment System 
including:

✓ **Iteration 3 Features** (Foundation):
  - User authentication and role-based access
  - Job posting and management
  - Application lifecycle
  - Notifications and communication
  - Workload tracking

✓ **Iteration 4 Features** (Enhanced):
  - TA Job Recommendations with match explanations
  - Advanced applicant filtering (Pending Only, High Match First, Needs Decision)
  - Intelligent Workload Balancing with suggestions
  - Export functionality for reporting (Applications, Workload, Job Status)
  - Password change and account security
  - Seamless notification synchronization

**Technical Achievements**:
- 43 Java source files across 6 packages
- Service-oriented architecture with clear separation of concerns
- Comprehensive automated test suite (30+ tests)
- CSV-based data persistence
- GUI and command-line interfaces

**Test Coverage**:
- Unit tests: All core services
- Integration tests: End-to-end workflows
- Manual tests: All user-facing features
- Regression tests: Documented checklist

**Deliverables**:
- Complete source code with documentation
- User manual with screenshots
- Comprehensive regression checklist
- Final demo script
- Test runners with passing test cases

We believe this system effectively demonstrates modern software engineering 
practices including:
- Iterative development
- Role-based design
- Clear API boundaries
- Comprehensive testing
- User-centric features

[PAUSE FOR QUESTIONS]

Thank you very much for your time today. We look forward to your feedback. 
Are there any questions about the system or specific features you'd like us 
to elaborate on?

[WAIT FOR Q&A - approximately 30-60 seconds]

Thank you again. This concludes our demonstration."

**Actions**:
1. Summarize all features demonstrated
2. Highlight Iteration 4 enhancements
3. Mention technical architecture
4. Reference test coverage
5. List deliverables
6. Open floor for questions
7. Thank audience

**Timing**: 1 minute (9:00 - 10:00)

---

## Demo Timing Breakdown

| Segment | Presenter | Content | Duration | End Time |
|---------|-----------|---------|----------|----------|
| 0 | Conghao Li | Introduction & System Overview | 1:00 | 1:00 |
| 1 | Hanyu Xiao | TA Job Recommendations | 2:00 | 3:00 |
| 2 | Wang Xiao | Export Functionality | 1:30 | 4:30 |
| 3 | Yucheng Liu | Workload Balancing | 1:30 | 6:00 |
| 4 | Mengzhe Shi | MO Review & Notifications | 2:00 | 8:00 |
| 5 | Zhixing Sun | Password Security | 1:00 | 9:00 |
| 6 | Conghao Li | Closing & Q&A | 1:00 | 10:00 |

**Total: 10 minutes**

---

## Demo Contingency Plans

### If Application Crashes:
- Have backup compiled binary ready
- Restart from most recent segment
- Continue with next segment
- Acknowledge the issue professionally

### If Feature Not Working:
- "This feature requires additional setup that we'll verify post-demo"
- Show code implementation as backup
- Move to next feature
- Note for post-demo fix

### If Projector/Screen Issues:
- Live code review as fallback
- Display screenshots from SCREENSHOTS_GUIDE.md
- Narrate functionality while showing code
- Ensure audio is clear

### If Running Out of Time:
- Skip "Job Board Filters" if needed (covered in narrative)
- Reduce Admin section to export summary only
- Keep core flow: TA → MO → Admin
- Ensure closing is still included

---

## Demo Success Criteria

✓ **Technical**:
- [ ] All features execute without crashes
- [ ] No compilation errors
- [ ] Data persists correctly
- [ ] All 6 presenters speak clearly
- [ ] Screen is visible and readable

✓ **Content**:
- [ ] All Iteration 4 features demonstrated
- [ ] End-to-end flow clearly shown
- [ ] Explainability emphasized (match scores, suggestions)
- [ ] User experience benefits highlighted
- [ ] Technical architecture mentioned

✓ **Delivery**:
- [ ] Demo completes in 10 minutes ±30 seconds
- [ ] Smooth transitions between presenters
- [ ] Clear narration throughout
- [ ] Professional demeanor
- [ ] Confidence in explaining features

---

## Post-Demo Action Items

1. **Submit Code to Repository**
   - Create branch: `conghao/iteration4-final-delivery`
   - Push all updates and fixes
   - Tag with version: `v1.4-final`

2. **Gather Feedback**
   - Document assessor questions
   - Record suggestions for future iterations
   - Note any confusion points

3. **Prepare Documentation**
   - Ensure all README files are up to date
   - Verify user manual has all features
   - Confirm test documentation is complete

4. **Archive Results**
   - Save all demo screenshots
   - Record demo if possible
   - Archive test results
   - Create final submission package

---

## Notes & Tips

- **Pacing**: Give assessors time to absorb each feature (pause for ~5 seconds after key points)
- **Emphasis**: Highlight Iteration 4 improvements (recommendations, export, workload balancing)
- **Clarity**: Use simple language when explaining technical concepts
- **Engagement**: Make eye contact, speak clearly, show enthusiasm
- **Preparation**: Practice transitions between presenters beforehand
- **Backup**: Have printed slides or speaker notes for reference
- **Equipment**: Test all equipment 10 minutes before demo starts

---

## References

- USER_MANUAL.md - Complete user documentation with all features
- REGRESSION_CHECKLIST.md - Testing checklist for all features
- SCREENSHOTS_GUIDE.md - Guide for capturing and organizing screenshots
- Code: src/com/group52/tarecruitment/ - All source code implementations

---

**Demo prepared by**: Conghao Li (Final Testing & Integration Lead)  
**Last updated**: 2026-05-17  
**Status**: Ready for final delivery demonstration

---

Good luck with the demo! 🎉
