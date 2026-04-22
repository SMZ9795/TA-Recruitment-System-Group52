# TA-Recruitment-System-Group52
Software Engineering Group Project-Group52

## Project Description
This project aims to develop a Teaching Assistant (TA) Recruitment System for BUPT International School.
The system supports TA applications, job posting, and selection processes, with a modern Swing-based user interface and AI-powered features.

## Team Members
- Mengzhe Shi QMUL ID:231226680
- Hanyu Xiao QMUL ID: 231226244
- Xiaowang QMUL ID: 231226510
- Yucheng Liu QMUL ID：231226945
- Zhixing Sun QMUL ID：231226738
- Conghao Li QMUL ID:231225546

## Features

### Core Features
- **TA (Applicant)**: Create profile, upload CV, search jobs, apply for jobs, view application status
- **MO (Course Director)**: Post jobs, screen applicants, manage job listings
- **Admin**: Monitor TA workload, manage user accounts, overview jobs

### AI-Enhanced Features
- **Skill Matching**: Intelligent skill matching between applicants and job requirements
- **Skill Gap Analysis**: Identify missing skills for applicants
- **Workload Balance**: Suggest workload balance for TAs

### UI Features
- **Modern Design**: Clean, professional interface with gradient backgrounds and shadows
- **Responsive Layout**: Well-organized panels and navigation
- **User-Friendly**: Intuitive controls and clear feedback

## Installation

### Prerequisites
- Java 8 or higher
- Git (for cloning the repository)

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/TA-Recruitment-System-Group52.git
   ```
2. Navigate to the project directory:
   ```bash
   cd TA-Recruitment-System-Group52
   ```
3. Compile the project:
   ```bash
   javac -d bin -cp bin src/com/group52/tarecruitment/*.java src/com/group52/tarecruitment/model/*.java src/com/group52/tarecruitment/repository/*.java src/com/group52/tarecruitment/service/*.java src/com/group52/tarecruitment/ui/SwingApp.java src/com/group52/tarecruitment/util/*.java
   ```
4. Run the application:
   ```bash
   java -cp bin com.group52.tarecruitment.Main
   ```

## Usage

### Default Accounts
- **Admin**: email: admin@bupt.local, password: admin123
- **MO**: email: drsmith@bupt.local, password: mo123456

### TA Registration
TAs can register by clicking "Register as TA" on the login page and providing their name, email, and password.

### Job Application Process
1. **TA**: Create profile, upload CV, search for jobs, apply for suitable positions
2. **MO**: Post jobs, review applications, accept/reject applicants
3. **Admin**: Monitor TA workload, manage user accounts

## Project Structure

```
src/
├── com/
│   └── group52/
│       └── tarecruitment/
│           ├── model/          # Data models
│           ├── repository/      # Data persistence
│           ├── service/         # Business logic
│           ├── ui/              # User interface
│           ├── util/            # Utility classes
│           └── Main.java        # Entry point
data/                            # Data files
├── users.csv                     # User data
├── jobs.csv                      # Job listings
└── applications.csv              # Applications
```

## Technologies Used
- Java Swing (GUI)
- CSV (Data storage)
- Java 8+ (Core language)

## Future Enhancements
- Database integration (instead of CSV)
- Web-based interface
- More advanced AI features
- Mobile app support
- Notification system

## License
This project is for educational purposes only.