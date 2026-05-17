# TA Recruitment System - Screenshots Guide & Collection Instructions

## Overview
This document provides instructions for capturing and organizing screenshots of the TA Recruitment System for the final delivery package. Screenshots demonstrate all major features across the three user roles.

---

## Screenshot Collection Workflow

### Prerequisites
1. Compile the project: `javac -d bin src/com/group52/tarecruitment/**/*.java`
2. Run the GUI: `java -cp bin com.group52.tarecruitment.SwingMain`
3. Use test accounts (see credentials below)
4. Save screenshots in: `screenshots/` directory

### Test Credentials

| User Type | ID | Password | Purpose |
|-----------|----|----|---------|
| TA | TA231226244 | Password1! | Job search & apply |
| TA | TA231226950 | Password1! | Recommendation demo |
| MO | MO001 | Password1! | Job management |
| Admin | ADMIN01 | Password1! | Workload & export |

---

## Screenshots Checklist

### Section 1: Authentication (2 screenshots)

#### 1.1 Login Screen
**Path**: Initial application start screen
**Steps**:
1. Launch application
2. Leave fields empty or partially filled
3. Capture the login form

**Elements to show**:
- User ID input field
- Password input field
- Login button
- Role indicator (if visible)
- Error message (optional - try wrong password once)

**Filename**: `01_login_screen.png`

---

#### 1.2 Login Error & Account Locked
**Path**: After 5 failed login attempts
**Steps**:
1. Try logging in with wrong password 5 times
2. Capture the locked account message
3. Or show password change dialog

**Elements to show**:
- Account locked message
- Retry in X minutes counter
- Account unlock/reset option (if available)

**Filename**: `02_login_locked_account.png`

---

### Section 2: TA Features (6 screenshots)

#### 2.1 TA Dashboard Overview
**Path**: After TA login
**Steps**:
1. Login as TA231226244
2. View dashboard home screen

**Elements to show**:
- Welcome message with TA name
- Application status summary (Pending: X, Accepted: Y, Rejected: Z, Withdrawn: W)
- Notification count badge
- Main navigation menu

**Filename**: `03_ta_dashboard_overview.png`

---

#### 2.2 TA Profile - Personal Information
**Path**: Menu > Profile
**Steps**:
1. Click Profile
2. Show personal details section
3. Show skills input

**Elements to show**:
- Name, Email, ID fields
- Programme dropdown / Year of Study
- Available Hours per Week (editable)
- Skills field with semicolon-separated format
- Save button

**Filename**: `04_ta_profile_personal_info.png`

---

#### 2.3 TA Profile - CV Upload & Password Change
**Path**: Menu > Profile (scroll down)
**Steps**:
1. Show CV upload section
2. Show password change button/modal

**Elements to show**:
- CV upload button
- Current CV filename display (or "No CV uploaded")
- "Change Password" button
- Password change modal showing:
  - Current Password field
  - New Password field
  - Confirm Password field
  - Requirements checklist (uppercase, lowercase, digit, special char, 8+ chars)
- Submit/Cancel buttons

**Filename**: `05_ta_profile_cv_and_password.png`

---

#### 2.4 TA Job Board - Recommended Jobs (Iteration 4)
**Path**: Menu > Job Board
**Steps**:
1. Navigate to Job Board
2. Show top section with Recommended Jobs

**Elements to show**:
- "Recommended Jobs" section header
- Job cards displaying:
  - Job title
  - Match score (e.g., "92% Match" or "8.2/10")
  - Quick match info: "Matched: Java, SQL | Missing: Python | Hours: Yes"
- Jobs sorted by score (highest first)
- Visual differentiation from "All Jobs" section

**Filename**: `06_ta_job_board_recommendations.png`

---

#### 2.5 TA Job Board - Job Details & Application
**Path**: Job Board > Select a job
**Steps**:
1. Scroll down to "All Jobs"
2. Click on a job to view details
3. Show details panel
4. Click "Apply Now"

**Elements to show**:
- Job details panel:
  - Job ID, Title, Description
  - Required Skills
  - Hours per week, Positions, Deadline
  - MO Name
  - Current application status (if already applied)
- "Apply Now" button
- Application confirmation modal/dialog
- Success toast message

**Filename**: `07_ta_job_details_apply.png`

---

#### 2.6 TA Notifications Center
**Path**: Menu > Notifications or click notification bell
**Steps**:
1. Navigate to Notifications
2. Show notification list
3. Show status summary

**Elements to show**:
- Application Status Summary box:
  - Total Applications: X
  - Pending: X | Accepted: X | Rejected: X | Withdrawn: X
- Notification list showing:
  - Notification type badge (Accepted, Rejected, etc.)
  - Message text
  - Timestamp
  - Read/Unread indicator
  - Mark as read/unread button
- Unread count badge
- Filter buttons (if available)

**Filename**: `08_ta_notifications_center.png`

---

### Section 3: MO Features (5 screenshots)

#### 3.1 MO Dashboard Overview
**Path**: After MO login
**Steps**:
1. Login as MO001
2. View MO dashboard

**Elements to show**:
- Welcome message with MO name
- Job management summary (Total Jobs, Open, Closed, Filled)
- Application statistics
- Navigation menu

**Filename**: `09_mo_dashboard_overview.png`

---

#### 3.2 MO Job Creation Form
**Path**: Menu > Jobs > Create New Job
**Steps**:
1. Navigate to Jobs
2. Click "Post New Job" or "Create Job"
3. Show form

**Elements to show**:
- Job Title field
- Job Code field
- Description text area
- Required Skills input
- Hours per Week spinner/input
- Number of Positions spinner
- Deadline date picker (showing future date)
- Status dropdown (default: OPEN)
- Submit/Cancel buttons

**Filename**: `10_mo_job_creation_form.png`

---

#### 3.3 MO Job List & Management
**Path**: Menu > Jobs
**Steps**:
1. Show job list table
2. Show some jobs with different statuses

**Elements to show**:
- Job list table with columns:
  - Job ID
  - Title
  - Status badge (🟢 OPEN, 🔴 CLOSED, 🟡 FILLED)
  - Positions | Filled
  - Deadline
  - Actions (View, Edit, Close/Reopen, Delete)
- Close/Reopen/Delete buttons visible for various jobs
- Confirmation dialogs (if actions taken)

**Filename**: `11_mo_job_list_management.png`

---

#### 3.4 MO Applicant Review with Filters (Iteration 4)
**Path**: Menu > Jobs > Select Job > Applicants
**Steps**:
1. Select a job with applications
2. Show applicant list
3. Show filter buttons
4. Apply filters

**Elements to show**:
- Filter button group:
  - "Pending Only" button
  - "High Match First" button
  - "Needs Decision" button
- Applicant list table:
  - TA Name, ID
  - Application Status badge (PENDING, ACCEPTED, REJECTED, WITHDRAWN)
  - Match Score (if available)
  - Applied Date
  - Actions (View, Accept, Reject, Withdraw)
- Before/After filter states

**Filename**: `12_mo_applicants_filters.png`

---

#### 3.5 MO Applicant Decision
**Path**: Menu > Jobs > Applicants > Select Applicant > Accept/Reject
**Steps**:
1. Show applicant details panel
2. Show match explanation
3. Click Accept or Reject
4. Show confirmation/success

**Elements to show**:
- Applicant details showing:
  - Name, Email, TA ID
  - Programme, Year, Available Hours
  - Skills list
  - Match explanation: "Matched: Java, Python | Missing: SQL | Hours Fit: Yes"
  - CV file indicator
- Accept/Reject buttons
- Confirmation dialog with options
- Success toast message
- Updated application status
- Optional: Job status changed to FILLED (if last position)

**Filename**: `13_mo_applicant_decision.png`

---

### Section 4: Admin Features (6 screenshots)

#### 4.1 Admin Dashboard Overview
**Path**: After Admin login
**Steps**:
1. Login as ADMIN01
2. View dashboard

**Elements to show**:
- Welcome message with Admin name
- Four summary cards:
  - Total Jobs: [number]
  - Filled Jobs: [number]
  - Overloaded TAs: [number]
  - High-Risk TAs: [number]
- Navigation menu
- Quick links or tabs

**Filename**: `14_admin_dashboard_summary.png`

---

#### 4.2 Admin Workload Management (Iteration 4)
**Path**: Menu > Workload or Workload Balancing
**Steps**:
1. Navigate to Workload section
2. Show workload table
3. Show status color coding
4. Optional: Apply search filter

**Elements to show**:
- Workload Balancing Suggestions table:
  - Columns: TA ID, Name, Status, Assigned h/week, Available h/week, Remaining h, Suggestion
  - Color-coded Status:
    - 🟢 Balanced: Assigned <= Available and > 50% utilized
    - 🔴 Overloaded: Assigned > Available
    - 🟡 Underused: Assigned < 50% of Available
  - Example suggestions: "Move 4h from TA A to TA B", "TA C has 6h capacity"
- Search by TA name/ID (optional)
- Refresh button
- Status distribution (pie chart or summary if available)

**Filename**: `15_admin_workload_balancing.png`

---

#### 4.3 Admin Job Management
**Path**: Menu > Jobs (Admin view)
**Steps**:
1. Show admin global job list
2. Show job actions

**Elements to show**:
- Global job list showing all MO jobs
- Columns: Job ID, Title, MO Name, Status, Filled/Positions, Deadline
- Application count per job (if available)
- Admin actions (View, Edit, Delete if necessary)
- Different status badges

**Filename**: `16_admin_job_management.png`

---

#### 4.4 Admin Export - All Applications (Iteration 4)
**Path**: Menu > Reports/Export or Dashboard > Export Section
**Steps**:
1. Navigate to Export section
2. Show export button options
3. Click "Export All Applications"
4. Show success message

**Elements to show**:
- Three export buttons:
  - "Export All Applications"
  - "Export TA Workload Summary"
  - "Export Job Filling Status"
- Export process (button pressed state)
- Success toast message showing:
  - "Export successful: applications_2026-05-17_143025.csv"
  - File location: "data/exports/"

**Filename**: `17_admin_export_all_applications.png`

---

#### 4.5 Admin Export - Workload Summary (Iteration 4)
**Path**: Menu > Reports/Export
**Steps**:
1. Click "Export TA Workload Summary"
2. Show success message

**Elements to show**:
- Button in active/pressed state
- Success notification showing:
  - Filename: "workload_summary_2026-05-17_143025.csv"
  - Columns listed: TA ID, Name, Available h/week, Assigned h/week, Remaining h, Status
  - File saved location

**Filename**: `18_admin_export_workload.png`

---

#### 4.6 Admin User Management
**Path**: Menu > Users or User Management
**Steps**:
1. Navigate to User Management
2. Show user list
3. Show user actions

**Elements to show**:
- User list table:
  - User ID, Name, Role (TA/MO/Admin), Email, Status (Active/Inactive)
- User actions menu:
  - View Details
  - Reset Password
  - Deactivate/Activate
  - Change Role (if applicable)
- Optional: Create New User button
- Optional: User details panel showing full information

**Filename**: `19_admin_user_management.png`

---

### Section 5: End-to-End Flow (3 screenshots - Sequential Flow)

#### 5.1 E2E Step 1: TA Receives & Accepts Recommendation
**Path**: TA dashboard > Job Board
**Combine elements from**:
- Screenshot 06 (Recommended Jobs)
- Screenshot 07 (Apply action)

**Elements to show**:
- TA viewing recommended job with high match score
- Clicking Apply
- Clear indication of the application being made

**Filename**: `20_e2e_01_ta_recommendation_apply.png`

---

#### 5.2 E2E Step 2: MO Reviews & Accepts
**Path**: MO dashboard > Jobs > Applicants
**Combine elements from**:
- Screenshot 12 (Applicant list with filters)
- Screenshot 13 (Accept decision)

**Elements to show**:
- MO viewing applicant with match reason
- Using "High Match First" filter
- Accepting the application
- Success confirmation

**Filename**: `21_e2e_02_mo_review_accept.png`

---

#### 5.3 E2E Step 3: Admin Sees Updated Workload
**Path**: Admin dashboard > Workload
**Combine elements from**:
- Screenshot 14 (Dashboard summary - updated counts)
- Screenshot 15 (Workload table showing TA's new assignment)

**Elements to show**:
- Admin dashboard showing updated summary cards
- Workload table showing TA now has assigned hours
- Status changed (e.g., from "Underused" to "Balanced")
- Suggestion for potential rebalancing

**Filename**: `22_e2e_03_admin_workload_updated.png`

---

## Screenshot Organization

### Directory Structure
```
screenshots/
├── 01_login_screen.png
├── 02_login_locked_account.png
├── 03_ta_dashboard_overview.png
├── 04_ta_profile_personal_info.png
├── 05_ta_profile_cv_and_password.png
├── 06_ta_job_board_recommendations.png
├── 07_ta_job_details_apply.png
├── 08_ta_notifications_center.png
├── 09_mo_dashboard_overview.png
├── 10_mo_job_creation_form.png
├── 11_mo_job_list_management.png
├── 12_mo_applicants_filters.png
├── 13_mo_applicant_decision.png
├── 14_admin_dashboard_summary.png
├── 15_admin_workload_balancing.png
├── 16_admin_job_management.png
├── 17_admin_export_all_applications.png
├── 18_admin_export_workload.png
├── 19_admin_user_management.png
├── 20_e2e_01_ta_recommendation_apply.png
├── 21_e2e_02_mo_review_accept.png
├── 22_e2e_03_admin_workload_updated.png
└── SCREENSHOTS_README.md (this file)
```

---

## Capturing Screenshots

### On Windows:
1. Use **Print Screen** to capture full screen
2. Use **Alt+Print Screen** for active window only
3. Use **Snip & Sketch** (Win+Shift+S) for custom region
4. Paste into Paint and save as PNG

### On macOS:
1. Use **Cmd+Shift+3** for full screen
2. Use **Cmd+Shift+4** for selection
3. Use **Cmd+Shift+5** for video/region selection

### On Linux:
1. Use **Print Screen** or **Shift+Print Screen**
2. Use **gnome-screenshot** or similar tool
3. Use **shutter** for advanced capture

### Best Practices:
- ✅ Resize window to ~1024x768 for consistency
- ✅ Ensure all UI elements are visible and readable
- ✅ Use clear, distinct test data (meaningful job titles, names)
- ✅ Show confirmation messages and success toasts
- ✅ Include error messages for edge cases (if relevant)
- ✅ Maintain consistent window positioning across screenshots
- ✅ Save as PNG format for quality

---

## Screenshot Naming Convention

Format: `##_section_description.png`

Where:
- `##`: Sequential number (01-22)
- `section`: TA, MO, Admin, or E2E
- `description`: Brief description of content (lowercase, hyphenated)

Examples:
- `03_ta_dashboard_overview.png` ✅ Good
- `ta_dashboard.png` ❌ Not specific enough
- `03TA_Dashboard_Overview.PNG` ❌ Wrong naming convention

---

## Screenshot Verification Checklist

Before finalizing screenshots, verify:

- [ ] Resolution is readable (minimum 800x600)
- [ ] All relevant UI elements are visible
- [ ] No personal sensitive data visible (or properly masked)
- [ ] Window title bar visible (shows application name)
- [ ] Status messages/toasts are captured (if applicable)
- [ ] Color coding is clear and visible
- [ ] Text is legible in the screenshot
- [ ] Filename follows naming convention
- [ ] Saved in correct directory with PNG format
- [ ] File size is reasonable (<2MB each)

---

## Integration with Documentation

### User Manual Integration:
- Link screenshots in USER_MANUAL.md as reference images
- Use screenshots in "Screenshot Elements" checklist sections
- Provide quick reference to screenshot files for feature-specific sections

### Demo Script Integration:
- Reference specific screenshots in demo narration
- Use screenshots for transitions between demo sections
- Provide visual proof of feature completeness

### Final Delivery Package:
- Include `screenshots/` directory in project delivery
- Create index.md listing all screenshots with descriptions
- Provide presentation slides with annotated screenshots

---

## Tips for Better Screenshots

1. **Consistency**: Use same resolution and font size across all screenshots
2. **Clarity**: Ensure text is readable at 100% zoom
3. **Completeness**: Show all relevant buttons and fields
4. **Context**: Include window title and menu bar for context
5. **Documentation**: Add captions or annotations if needed (using image editor)
6. **Version Control**: Version screenshots if system changes frequently

---

## File Format Guidelines

- **Format**: PNG (lossless, good compression)
- **Resolution**: 1024x768 or 1280x1024 (consistent)
- **File Size**: Target <1MB per screenshot (use compression)
- **Naming**: Follow convention strictly
- **Backup**: Keep source images in version control

---

For questions or clarifications, refer to the USER_MANUAL.md or contact the project lead.
