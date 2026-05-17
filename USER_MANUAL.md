# TA Recruitment System - User Manual

## System Overview
The TA Recruitment System is a comprehensive platform for managing Teaching Assistant recruitment at BUPT International School. The system has three main user roles:
- **TA (Teaching Assistant)**: Browse job postings and apply for positions
- **MO (Module Organizer)**: Post jobs and review applications
- **Admin**: Manage system, view workload balancing, and export reports

---

## 1. Getting Started

### System Requirements
- Java Runtime Environment (JRE) 11 or higher
- Windows, macOS, or Linux
- Minimum 512MB RAM

### How to Run the Application

#### Option 1: Run with GUI (Swing Interface)
```bash
cd TA-Recruitment-System-Group52
java -cp bin com.group52.tarecruitment.SwingMain
```

#### Option 2: Run with Console Interface
```bash
cd TA-Recruitment-System-Group52
java -cp bin com.group52.tarecruitment.ConsoleApp
```

---

## 2. Login Screen

**Location**: First screen when launching the application

### Features:
- **User ID Field**: Enter your assigned ID (e.g., TA231226244, MO001, ADMIN01)
- **Password Field**: Enter your password (case-sensitive)
- **Role Display**: System identifies your role automatically based on user ID prefix
- **Error Handling**: Clear messages for wrong credentials or locked accounts

### Login Credentials (Test Account Examples):
| Role | User ID | Password | Notes |
|------|---------|----------|-------|
| TA | TA231226244 | Password1! | Full profile example |
| MO | MO001 | Password1! | Job management example |
| Admin | ADMIN01 | Password1! | System administration |

**Security Features:**
- Account locks after 5 failed login attempts
- Lock expires after 15 minutes
- Passwords are hashed and never stored in plain text

---

## 3. TA Dashboard

**Location**: Main view after TA login
**Screenshot Checklist:**
- [ ] Dashboard header showing "Welcome, [TA Name]"
- [ ] Application status summary (Pending/Accepted/Rejected/Withdrawn counts)
- [ ] Notification center with unread count
- [ ] Navigation menu (Job Board, Profile, Notifications, Settings)

### 3.1 TA Profile Page

**Navigation**: Click "Profile" from main menu

**Features:**
1. **Personal Information**
   - Name, Email, Year of Study, Programme
   - Available Hours per Week (editable)
   - Skills (comma/semicolon separated)

2. **CV Upload**
   - Upload resume in PDF or TXT format
   - Maximum file size: 5MB
   - Current CV filename displayed

3. **Password Change**
   - Click "Change Password" button
   - Enter current password, new password, confirm new password
   - Password must contain: uppercase, lowercase, digit, special character (!@#$%^&*)
   - Minimum 8 characters

4. **Save and Validation**
   - Clear error messages for validation failures
   - Success toast after saving

**Screenshot Elements:**
- [ ] Profile form with all fields visible
- [ ] Skills input field showing semicolon-separated format
- [ ] CV upload button and current file display
- [ ] "Change Password" modal dialog
- [ ] Validation error message example

### 3.2 TA Job Board

**Navigation**: Click "Job Board" or "Jobs" from main menu

**Sections:**

#### A. Recommended Jobs (Iteration 4 Feature)
- **Displayed At Top**: Jobs matching TA's skills, programme, and available hours
- **Sort Order**: Highest match score first
- **Information Shown**:
  - Job Title
  - Module Name
  - Match Score (e.g., "92% Match" or "8.2/10")
  - Quick View: "Matched Skills: Java, SQL | Missing: Python | Hours Fit: Yes"

#### B. All Jobs Section
- **Filtering Options**:
  - Keyword search (job title, description)
  - Skills filter
  - Maximum hours per week
  - Module Organizer
  - Job Status (OPEN, CLOSED, FILLED)

- **Job Card Information**:
  - Job ID, Title, Description
  - Required Skills
  - Hours per week, Positions available
  - Deadline
  - Application status (Not Applied / Pending / Accepted / Rejected / Withdrawn)

#### C. Apply to Job
- **Button**: "Apply Now" on each job card
- **Confirmation**: Modal showing job details and confirmation
- **Success Message**: Toast notification with job ID and application status

**Screenshot Elements:**
- [ ] Recommended Jobs section with match scores
- [ ] Recommendation reason panel (skills, hours, programme fit)
- [ ] All Jobs list with filters applied
- [ ] Job application modal
- [ ] Success confirmation toast

### 3.3 TA Notifications

**Navigation**: Click "Notifications" or notification bell icon

**Features:**
1. **Notification Types**:
   - New application received
   - Application accepted
   - Application rejected
   - Application withdrawn
   - Job closed while application pending
   - Job status changed to FILLED

2. **Notification List**:
   - Unread count badge
   - Time/date of notification
   - Mark as read/unread
   - Filter options:
     - Show all / Unread only
     - By type

3. **Application Status Dashboard**:
   - Total Applications: X
   - Pending: X
   - Accepted: X
   - Rejected: X
   - Withdrawn: X

**Screenshot Elements:**
- [ ] Notification center with unread badge
- [ ] Notification list with various types
- [ ] Application status summary panel
- [ ] "Mark as read" action
- [ ] Notification filters

---

## 4. MO (Module Organizer) Dashboard

**Location**: Main view after MO login
**Screenshot Checklist:**
- [ ] Dashboard header showing "Welcome, [MO Name]"
- [ ] Job count summary
- [ ] Application statistics
- [ ] Navigation menu

### 4.1 MO Job Management

**Navigation**: Click "Jobs" or "My Jobs"

**Features:**
1. **Post New Job**
   - Click "Post New Job" button
   - Fill form:
     - Job Title (required)
     - Job Code/ID (auto-generated or manual)
     - Description
     - Required Skills (semicolon-separated)
     - Hours per week
     - Number of positions
     - Deadline (YYYY-MM-DD format, must be future date)
     - Status (OPEN by default)

2. **View Jobs List**
   - Table with columns:
     - Job ID, Title, Status (badge), Positions/Filled, Deadline
     - Actions: View Details, Edit, Close/Reopen, Delete

3. **Job Lifecycle Management**
   - **Close Job**: Mark as CLOSED, prevents new applications
   - **Reopen Job**: Restore to OPEN (only if deadline hasn't passed)
   - **Edit Job**: Modify details (except status via edit)
   - **Delete Job**: Only if no applications exist

4. **Job Status Badge Colors**:
   - 🟢 **OPEN**: Green background
   - 🔴 **CLOSED**: Red background
   - 🟡 **FILLED**: Yellow background

**Screenshot Elements:**
- [ ] Job creation form with all fields
- [ ] Job list table with status badges
- [ ] Close Job confirmation dialog
- [ ] Reopen Job success message

### 4.2 MO Applicant Review

**Navigation**: Click "Applicants" or select job > "View Applicants"

**Features:**
1. **Applicant Filtering** (Iteration 4 Features):
   - **"Pending Only"** button: Show only PENDING applications
   - **"High Match First"** button: Sort by recommendation score
   - **"Needs Decision"** button: Show PENDING and WITHDRAWN

2. **Applicant List Columns**:
   - Applicant Name, TA ID
   - Application Status (badge)
   - Match Score (if available)
   - Applied Date
   - Actions: View Details, Accept, Reject, Withdraw

3. **Applicant Details View**
   - TA Name, Email, ID
   - Programme, Year of Study
   - Available Hours per Week
   - Skills, CV File
   - Match explanation: "Matched: X | Missing: Y | Fit: Z"

4. **Application Decision**
   - **Accept**: 
     - Confirmation modal
     - Success toast
     - Job status updates to FILLED if all positions filled
     - Notifications sent to TA
   - **Reject**: 
     - Optional rejection reason
     - Confirmation
     - Notification sent to TA
   - **Withdraw**: Withdraw application

5. **Notification Updates**
   - After accepting/rejecting, applicant receives notification
   - Job status badge updates immediately

**Screenshot Elements:**
- [ ] Applicant list with filters
- [ ] Filter buttons (Pending Only, High Match First, Needs Decision)
- [ ] Applicant details panel
- [ ] Accept/Reject decision dialog
- [ ] Job FILLED status indicator

### 4.3 MO Export (Iteration 4 Feature)

**Navigation**: Dashboard > "Export" or Job Details > "Export Applicants"

**Features:**
1. **Export Buttons**:
   - "Export Applicant List": Creates CSV for current job

2. **Export File Details**:
   - Location: `data/exports/`
   - Filename format: `applicants_<job_id>_<timestamp>.csv`
   - Example: `applicants_JOB001_2026-05-17_143025.csv`

3. **CSV Content**:
   - Columns: Applicant ID, TA Name, Email, Status, Match Score, Applied Date
   - All PENDING and ACCEPTED applicants included

4. **Success Notification**:
   - Toast message: "Export successful: [filename]"

**Screenshot Elements:**
- [ ] Export button on job details
- [ ] Success message with filename
- [ ] Exported CSV preview (if possible)

---

## 5. Admin Dashboard

**Location**: Main view after Admin login
**Screenshot Checklist:**
- [ ] Summary cards (Total Jobs, Filled Jobs, Overloaded TAs, High-Risk TAs)
- [ ] Workload summary table
- [ ] Job management interface
- [ ] Export functions

### 5.1 Admin Summary Cards

**Location**: Top of Admin Dashboard

**Cards Displayed**:
1. **Total Jobs**: Count of all jobs in system
2. **Filled Jobs**: Count of jobs with all positions filled
3. **Overloaded TAs**: Count of TAs with assigned hours > available hours
4. **High-Risk TAs**: Count of TAs with assigned hours > 80% of available hours

**Update Behavior**:
- Refresh on dashboard load
- Updates when "Refresh" button clicked in Workload tab
- Shows real-time data

**Screenshot Elements:**
- [ ] Four summary cards with numbers
- [ ] Card titles and icons
- [ ] Refresh button

### 5.2 Workload Management (Iteration 4 Feature)

**Navigation**: Dashboard > "Workload" or "Workload Balancing"

**Features:**
1. **Workload Balancing Suggestions Table**
   - Columns: TA ID, TA Name, Status, Assigned h/week, Available h/week, Remaining h, Suggestion

2. **Status Colors**:
   - 🟢 **Balanced** (Green): Assigned < Available, and > 50% utilized
   - 🔴 **Overloaded** (Red): Assigned > Available
   - 🟡 **Underused** (Yellow): Assigned < 50% of Available

3. **Suggestions Examples**:
   - "Move 4h/week from TA A to TA B"
   - "TA C has 6h/week available capacity"
   - "Recommend reassigning 2h/week jobs"

4. **Search/Filter**:
   - Search by TA Name or ID
   - Filter by Status

5. **Refresh Data**:
   - Click "Refresh" to recalculate based on accepted applications

**Screenshot Elements:**
- [ ] Workload table with all columns
- [ ] Status color coding
- [ ] Suggestion examples
- [ ] Search filter
- [ ] Refresh button

### 5.3 Admin Job Management

**Navigation**: Dashboard > "Jobs" or "Job Management"

**Features**:
- Create, view, edit, close, reopen, delete jobs (same as MO)
- Global view of all jobs across all MOs
- See application count per job
- Monitor job status across system

**Screenshot Elements:**
- [ ] Global job list
- [ ] Job creation form
- [ ] Job actions menu

### 5.4 Admin Export Functions (Iteration 4 Feature)

**Navigation**: Dashboard > "Reports" or "Export"

**Export Options:**

1. **Export All Applications**
   - Button: "Export All Applications"
   - File: `applications_<timestamp>.csv`
   - Columns: Application ID, TA ID, Job ID, Status, Applied Date, Decision Date
   - Includes all applications across all jobs

2. **Export TA Workload Summary**
   - Button: "Export TA Workload Summary"
   - File: `workload_summary_<timestamp>.csv`
   - Columns: TA ID, TA Name, Available h/week, Assigned h/week, Remaining h, Status
   - Snapshot of current workload

3. **Export Job Filling Status**
   - Button: "Export Job Filling Status"
   - File: `job_status_<timestamp>.csv`
   - Columns: Job ID, Job Title, Total Positions, Filled Positions, Status, Deadline
   - Shows job filling progress

**Export Behavior:**
- Files saved to: `data/exports/`
- Timestamp format: `YYYY-MM-DD_HHMMSS`
- Empty data results in CSV with headers only
- Success toast shows exact filename and location
- Multiple exports create separate files

**Screenshot Elements:**
- [ ] Export button group
- [ ] Export options/menu
- [ ] Success notification with filename
- [ ] File location indicator

### 5.5 Admin User Management

**Navigation**: Dashboard > "Users" or "User Management"

**Features:**
1. **User List**
   - Columns: User ID, Name, Role, Email, Status (Active/Inactive)

2. **User Actions**:
   - View user details
   - Reset password (generates temporary password)
   - Deactivate/Activate account
   - View application history

3. **Create New User**
   - Set user role (TA, MO, Admin)
   - Assign unique ID
   - Initial password (shown once, must change on first login)

**Screenshot Elements:**
- [ ] User list table
- [ ] User details panel
- [ ] Reset password modal
- [ ] Create user form

---

## 6. Common Features & Workflows

### 6.1 Password Change (All Roles)

**Access**: Account Menu > "Change Password" or Settings

**Steps**:
1. Click "Change Password"
2. Enter current password
3. Enter new password
4. Confirm new password
5. Click "Change"
6. Success: "Password changed successfully"

**Password Requirements**:
- Minimum 8 characters
- At least one uppercase letter (A-Z)
- At least one lowercase letter (a-z)
- At least one digit (0-9)
- At least one special character (!@#$%^&*)
- Cannot reuse current password

**Error Cases**:
- "Current password is incorrect"
- "Passwords do not match"
- "Password too short"
- "Password does not meet requirements"

### 6.2 End-to-End Application Flow

**Complete User Journey:**

1. **TA Receives Recommendation**
   - Views Job Board
   - Sees "Recommended Jobs" section
   - Job has 85% match score for skills/hours/programme

2. **TA Applies to Recommended Job**
   - Clicks "Apply Now"
   - Confirms application
   - Receives success notification

3. **MO Reviews Application**
   - Views applicant details
   - Sees match score: "Matched: Java, Python | Missing: SQL | Hours Fit: Yes"
   - Uses "High Match First" filter to prioritize
   - Decides to accept

4. **TA Receives Notification**
   - Gets acceptance notification
   - Updates notification count
   - Application shown as ACCEPTED

5. **Job Status Updates**
   - If last position filled, job status changes to FILLED
   - MO sees FILLED badge
   - No more applications accepted for this job

6. **Admin Sees Workload Changes**
   - TA now has assigned hours from job
   - Workload status updates (Balanced/Overloaded/Underused)
   - Admin can see suggestion: "Move X hours from TA A to TA B"

7. **Admin Generates Report**
   - Exports workload summary including new TA assignment
   - CSV includes updated assigned hours

**Screenshot Elements for Complete Flow:**
- [ ] Recommended job with match score
- [ ] Application confirmation modal
- [ ] MO applicant details with match reason
- [ ] Accept decision modal
- [ ] TA notification of acceptance
- [ ] Job FILLED status
- [ ] Admin workload table with new assignment
- [ ] Export success message

---

## 7. Error Handling & Edge Cases

### Common Error Messages

| Scenario | Error Message | Resolution |
|----------|---------------|-----------|
| Wrong password | "Invalid credentials" | Check caps lock, re-enter carefully |
| Account locked | "Account locked due to multiple failed attempts" | Wait 15 minutes or contact admin |
| Expired deadline | "Cannot create job with past deadline" | Select future date |
| Duplicate application | "Already applied to this job" | Withdraw previous application first |
| File too large | "CV file exceeds 5MB limit" | Compress or reduce file size |
| Wrong file format | "Only .pdf or .txt files supported" | Convert to PDF or TXT format |
| No positions available | "Job is closed/filled, cannot apply" | Contact MO for reopening |
| Insufficient data | "Cannot export empty dataset" | Ensure system has data to export |

---

## 8. Tips & Best Practices

### For TAs:
- ✅ Complete profile with accurate skills for better recommendations
- ✅ Set available hours correctly to avoid overload
- ✅ Check notifications regularly for application updates
- ✅ Apply to recommended jobs first (highest match)
- ✅ Keep CV updated with latest experience

### For MOs:
- ✅ Use "High Match First" filter to see best candidates first
- ✅ Use "Pending Only" to focus on decisions needed
- ✅ Provide feedback when rejecting applications
- ✅ Set realistic deadlines (2-3 weeks in future)
- ✅ Review applicants within 48 hours

### For Admins:
- ✅ Monitor workload regularly (at least weekly)
- ✅ Review "Overloaded TAs" list proactively
- ✅ Export reports for records and planning
- ✅ Check job filling status to identify bottlenecks
- ✅ Respond to manual rebalancing requests

---

## 9. Technical Support

### Troubleshooting

**Application won't start**
- Ensure Java 11+ is installed: `java -version`
- Check Java is in PATH: `java -cp bin com.group52.tarecruitment.SwingMain`
- Verify `bin` directory exists after compilation

**Data not saving**
- Check `data/` directory exists and is writable
- Verify CSV files (users.csv, jobs.csv, etc.) are not corrupted
- Try restarting the application

**Slow performance**
- Close other applications to free RAM
- Check CSV file sizes (if > 10MB, consider archiving old data)
- Restart application

**Lost password**
- Admin can reset password via User Management
- Temporary password will be provided
- Change on first login

---

## 10. System Requirements & Limitations

**System Requirements:**
- Java Runtime Environment 11 or later
- 512MB minimum RAM
- 100MB free disk space
- Windows, macOS, or Linux with Java support

**Known Limitations:**
- GUI is single-window (no multi-window support)
- Data stored in CSV files (not ideal for 10,000+ records)
- No email integration (notifications in-app only)
- No batch import of jobs/users (manual creation only)
- CV files stored as path reference only (not actual upload)

**Data Backup:**
- Recommended: Backup `data/` directory weekly
- Export reports before major changes
- Keep audit logs for compliance

---

## Appendix: Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| Save | Ctrl+S |
| Refresh | F5 or Ctrl+R |
| Clear/Cancel | Esc |
| Exit application | Alt+F4 or Ctrl+Q |
| Logout | Ctrl+L |

---

## Version Information
- **System Version**: Iteration 4
- **Release Date**: May 2026
- **Last Updated**: 2026-05-17

---

For further assistance, contact the system administrator.
