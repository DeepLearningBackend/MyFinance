# MyFinance — Online Personal Finance Management Platform

## Complete Build Guide (README + Step-by-Step)

---

## 📌 README — What We Are Building

### Project Name

**MyFinance** — Online Personal Finance Management Platform

### Project Goal

Build a web-based personal finance management platform where:

- **Users** can track expenses, create budgets, and manage financial goals
- **Admins** can manage user accounts, financial data security, and system settings

### Who Is This For?

Anyone who wants to manage their personal finances — track where their money goes, set monthly budgets per category, and work towards financial goals like saving for a vacation, a home, or education.

### Why Are We Building This?

- Mint (the world's biggest finance app, 20M+ users) shut down in 2024
- Existing apps are complex, privacy-invasive, and not built for Indian users
- We are building a simple, private, goal-driven platform from scratch

### What Makes Ours Different (Innovations)

1. Smart budget alerts — warns you at 70%, 90%, 100% BEFORE you overspend
2. Goal motivation engine — deadline countdown + milestone badges
3. Privacy-first — no SMS access, no data selling, transparent policy
4. Plain insight cards — one sentence tip per week, not complex dashboards
5. Category auto-suggest — type "Swiggy" → auto tags as Food
6. Offline expense entry — add expenses without internet, sync when connected

---

## 🛠️ Tech Stack

| Layer | Technology |
| --- | --- |
| Backend | Java Servlets + Apache Tomcat 10 |
| Frontend | JSP + HTML5 + CSS3 (Responsive) |
| Database | MySQL 8.0 + Raw JDBC |
| Security | HttpSession + BCrypt password hashing |
| Build Tool | Maven |
| IDE | IntelliJ IDEA / Eclipse |

---

## 📋 Marking Rubric Map

| Criteria | Marks | Where It Appears In Code |
| --- | --- | --- |
| Problem Understanding & Design | 8 | ER diagram, flow, project report |
| Core Java Concepts | 10 | Model classes, services, threads |
| Database Integration (JDBC) | 8 | DAOImpl classes, raw SQL queries |
| Servlets & Web Integration | 7 | Servlet classes, JSP pages |
| **Total** | **33** |  |

---

## 📁 Final Project Structure

```
MyFinance/
├── src/main/java/com/myfinance/
│   ├── model/
│   │   ├── BaseEntity.java          ← Abstract class (Inheritance)
│   │   ├── User.java                ← Extends BaseEntity
│   │   ├── Expense.java             ← Extends BaseEntity
│   │   ├── Budget.java              ← Extends BaseEntity
│   │   ├── Goal.java                ← Extends BaseEntity
│   │   └── Category.java
│   │
│   ├── dao/
│   │   ├── interfaces/
│   │   │   ├── UserDAO.java         ← Interface (Interfaces rubric)
│   │   │   ├── ExpenseDAO.java
│   │   │   ├── BudgetDAO.java
│   │   │   └── GoalDAO.java
│   │   └── impl/
│   │       ├── UserDAOImpl.java     ← Raw JDBC (8 JDBC marks)
│   │       ├── ExpenseDAOImpl.java
│   │       ├── BudgetDAOImpl.java
│   │       └── GoalDAOImpl.java
│   │
│   ├── service/
│   │   ├── AuthService.java         ← Login/register logic
│   │   ├── ExpenseService.java      ← Collections & Generics here
│   │   ├── BudgetService.java       ← Budget alert logic
│   │   ├── GoalService.java         ← Progress calculation
│   │   └── InsightService.java      ← Weekly tip generation
│   │
│   ├── servlet/
│   │   ├── AuthServlet.java         ← /login, /register (Servlets rubric)
│   │   ├── DashboardServlet.java    ← /dashboard
│   │   ├── ExpenseServlet.java      ← /expenses
│   │   ├── BudgetServlet.java       ← /budgets
│   │   ├── GoalServlet.java         ← /goals
│   │   └── admin/
│   │       ├── AdminDashServlet.java
│   │       ├── UserMgmtServlet.java
│   │       ├── SecurityServlet.java
│   │       └── SettingsServlet.java
│   │
│   ├── thread/
│   │   └── BudgetAlertThread.java   ← implements Runnable (Multithreading)
│   │
│   ├── exception/
│   │   ├── DatabaseException.java   ← Custom exception (Exception Handling)
│   │   └── ValidationException.java
│   │
│   └── util/
│       ├── DBConnection.java        ← JDBC connection
│       ├── PasswordUtil.java        ← BCrypt hashing
│       ├── SessionUtil.java         ← Session helper
│       └── CategoryMapper.java      ← Auto-suggest logic
│
├── src/main/webapp/
│   ├── WEB-INF/
│   │   └── web.xml                  ← Servlet mappings
│   ├── css/
│   │   ├── global.css               ← Base + variables
│   │   ├── auth.css                 ← Login/Register pages
│   │   ├── dashboard.css            ← Dashboard layout
│   │   └── responsive.css           ← Mobile breakpoints
│   ├── js/
│   │   ├── charts.js                ← Pie/bar chart (Canvas API)
│   │   ├── offline.js               ← Offline expense queue
│   │   └── validation.js            ← Frontend form checks
│   └── jsp/
│       ├── auth/
│       │   ├── login.jsp
│       │   └── register.jsp
│       ├── user/
│       │   ├── dashboard.jsp
│       │   ├── expenses.jsp
│       │   ├── budgets.jsp
│       │   └── goals.jsp
│       └── admin/
│           ├── admin-dashboard.jsp
│           ├── user-management.jsp
│           ├── security.jsp
│           └── settings.jsp
│
├── pom.xml
└── database/
    └── schema.sql                   ← All CREATE TABLE statements
```

---

## 🗄️ Database Schema

```sql
-- Users table
CREATE TABLE users (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(100) UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        ENUM('USER','ADMIN') DEFAULT 'USER',
    status      ENUM('ACTIVE','INACTIVE','PENDING') DEFAULT 'ACTIVE',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Expenses table
CREATE TABLE expenses (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    user_id     INT NOT NULL,
    title       VARCHAR(150) NOT NULL,
    amount      DECIMAL(10,2) NOT NULL,
    category    VARCHAR(50) NOT NULL,
    expense_date DATE NOT NULL,
    description TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Budgets table
CREATE TABLE budgets (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    user_id       INT NOT NULL,
    category      VARCHAR(50) NOT NULL,
    limit_amount  DECIMAL(10,2) NOT NULL,
    spent_amount  DECIMAL(10,2) DEFAULT 0.00,
    start_date    DATE NOT NULL,
    end_date      DATE NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Goals table
CREATE TABLE goals (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    user_id       INT NOT NULL,
    title         VARCHAR(150) NOT NULL,
    description   TEXT,
    target_amount DECIMAL(10,2) NOT NULL,
    saved_amount  DECIMAL(10,2) DEFAULT 0.00,
    deadline      DATE NOT NULL,
    status        ENUM('IN_PROGRESS','ACHIEVED','FAILED') DEFAULT 'IN_PROGRESS',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- System settings table
CREATE TABLE system_settings (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    setting_key   VARCHAR(100) UNIQUE NOT NULL,
    setting_value VARCHAR(255) NOT NULL,
    updated_by    INT,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- Audit logs table
CREATE TABLE audit_logs (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    admin_id    INT NOT NULL,
    action      VARCHAR(255) NOT NULL,
    target_user INT,
    timestamp   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id)
);

-- Default admin account (password: admin123)
INSERT INTO users (name, email, password, role, status)
VALUES ('Admin', 'admin@myfinance.com',
'$2a$12$hashedpasswordhere', 'ADMIN', 'ACTIVE');

-- Default system settings
INSERT INTO system_settings (setting_key, setting_value) VALUES
('platform_name', 'MyFinance'),
('default_currency', 'INR'),
('session_timeout', '30'),
('max_login_attempts', '5'),
('budget_alerts_enabled', 'true'),
('email_reports_enabled', 'true');
```

---

# 🚀 STEP-BY-STEP BUILD PROCESS

---

## PHASE 0 — Setup & Environment (Do This First)

### Step 0.1 — Install Required Software

```
1. Install Java JDK 17+         → https://adoptium.net
2. Install Apache Tomcat 10.1   → https://tomcat.apache.org
3. Install MySQL 8.0            → https://dev.mysql.com
4. Install Maven 3.9+           → https://maven.apache.org
5. Install IntelliJ IDEA        → https://jetbrains.com/idea
6. Install MySQL Workbench      → for DB management (optional)
```

### Step 0.2 — Create Maven Project

```
In IntelliJ → New Project → Maven Archetype
GroupId:    com.myfinance
ArtifactId: myfinance
Version:    1.0-SNAPSHOT
Archetype:  maven-archetype-webapp
```

### Step 0.3 — Setup pom.xml

Add these dependencies:

- javax.servlet-api 6.0.0 (scope: provided)
- mysql-connector-java 8.0.33
- jbcrypt 0.4 (password hashing)
- jstl 2.0 (JSP tag library)
- junit 5.9.3 (testing)

### Step 0.4 — Create MySQL Database

```sql
CREATE DATABASE myfinancedb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE myfinancedb;
-- then run schema.sql
```

---

## PHASE 1 — Foundation Layer (Database + Connection)

### Step 1.1 — Create DBConnection.java

**Purpose:** Single class to manage JDBC connection to MySQL **What to write:**

- Static method getConnection() returning Connection
- URL, USER, PASS as private constants
- Load MySQL driver with Class.forName()
- Handle SQLException with custom DatabaseException

**Prompt to use:**

> "Write DBConnection.java in package com.myfinance.util that creates a JDBC connection to MySQL database 'myfinancedb' on localhost:3306. Include getConnection() static method, constants for URL/USER/PASS, and throw DatabaseException on failure."

### Step 1.2 — Create DatabaseException.java and ValidationException.java

**Purpose:** Custom exceptions for rubric marks **What to write:**

- DatabaseException extends RuntimeException with message constructor
- ValidationException extends RuntimeException with message + field name

### Step 1.3 — Run schema.sql

Execute all CREATE TABLE statements in MySQL Workbench or terminal. Verify all 6 tables are created: users, expenses, budgets, goals, system_settings, audit_logs.

---

## PHASE 2 — Model Layer (Java Classes + Inheritance)

### Step 2.1 — Create BaseEntity.java

**Purpose:** Abstract parent class — earns Inheritance marks **What to write:**

- abstract class BaseEntity
- Fields: int id, LocalDateTime createdAt
- Abstract method: public abstract boolean validate()
- Getters and setters for all fields

**Prompt to use:**

> "Write abstract class BaseEntity in package com.myfinance.model with fields id (int) and createdAt (LocalDateTime). Add abstract method validate() returning boolean. Add getters and setters."

### Step 2.2 — Create User.java

**Purpose:** User entity extending BaseEntity **What to write:**

- extends BaseEntity
- Fields: String name, email, password, Role role, Status status
- Enum Role { USER, ADMIN }
- Enum Status { ACTIVE, INACTIVE, PENDING }
- Override validate() — check name/email not null, email contains @

### Step 2.3 — Create Expense.java

**Purpose:** Expense entity extending BaseEntity **What to write:**

- extends BaseEntity
- Fields: int userId, String title, double amount, String category, LocalDate expenseDate, String description
- Override validate() — check amount > 0, title not empty, date not null

### Step 2.4 — Create Budget.java

**Purpose:** Budget entity extending BaseEntity **What to write:**

- extends BaseEntity
- Fields: int userId, String category, double limitAmount, double spentAmount, LocalDate startDate, LocalDate endDate
- Override validate() — check limitAmount > 0, startDate before endDate
- Extra method: getUsagePercent() returns (spentAmount/limitAmount)*100
- Extra method: getRemainingAmount() returns limitAmount - spentAmount

### Step 2.5 — Create Goal.java

**Purpose:** Goal entity extending BaseEntity **What to write:**

- extends BaseEntity
- Fields: int userId, String title, String description, double targetAmount, double savedAmount, LocalDate deadline, GoalStatus status
- Enum GoalStatus { IN_PROGRESS, ACHIEVED, FAILED }
- Override validate() — check targetAmount > 0, deadline in future
- Extra method: getProgressPercent() returns (savedAmount/targetAmount)*100
- Extra method: getRemainingAmount() returns targetAmount - savedAmount

---

## PHASE 3 — DAO Layer (JDBC = 8 Marks)

### Step 3.1 — Create DAO Interfaces

**Purpose:** Interfaces rubric mark **What to write for each (UserDAO, ExpenseDAO, BudgetDAO, GoalDAO):**

- save(Entity e) throws SQLException
- findById(int id) throws SQLException
- findByUserId(int userId) throws SQLException (except UserDAO)
- findAll() throws SQLException
- update(Entity e) throws SQLException
- delete(int id) throws SQLException

**Prompt to use:**

> "Write ExpenseDAO interface in package com.myfinance.dao.interfaces with methods: save(Expense), findById(int), findByUserId(int), findByUserIdAndMonth(int userId, int month, int year), findAll(), update(Expense), delete(int). All throw SQLException."

### Step 3.2 — Create UserDAOImpl.java

**Purpose:** Raw JDBC for user CRUD **What to write:**

- implements UserDAO
- save(): INSERT INTO users with PreparedStatement, use BCrypt for password
- findById(): SELECT * WHERE id=?, map ResultSet to User object
- findByEmail(): SELECT * WHERE email=? (needed for login)
- findAll(): SELECT * FROM users, return List
- update(): UPDATE users SET name=?, email=?, role=?, status=? WHERE id=?
- delete(): DELETE FROM users WHERE id=?
- Always use try-with-resources for Connection and PreparedStatement

### Step 3.3 — Create ExpenseDAOImpl.java

**Purpose:** Raw JDBC for expense CRUD (most used DAO) **What to write:**

- implements ExpenseDAO
- save(): INSERT INTO expenses
- findByUserId(): SELECT WHERE user_id=? ORDER BY expense_date DESC
- findByUserIdAndMonth(): SELECT WHERE user_id=? AND MONTH(expense_date)=?
- findByUserIdAndCategory(): for budget tracking
- getSumByUserIdAndMonth(): SELECT SUM(amount) for dashboard total
- update(): UPDATE expenses SET title=?, amount=?, category=?, date=?
- delete(): DELETE FROM expenses WHERE id=? AND user_id=? (security check)

### Step 3.4 — Create BudgetDAOImpl.java

**What to write:**

- All CRUD with raw JDBC
- updateSpentAmount(int budgetId, double amount): called when expense added
- findByUserIdAndCategory(): check if budget exists for category
- findOverBudget(int userId): SELECT WHERE spent_amount >= limit_amount

### Step 3.5 — Create GoalDAOImpl.java

**What to write:**

- All CRUD with raw JDBC
- updateSavedAmount(int goalId, double amount): add savings to goal
- findByStatus(int userId, String status): filter by IN_PROGRESS/ACHIEVED
- markAsAchieved(int goalId): UPDATE status='ACHIEVED' WHERE id=?

---

## PHASE 4 — Service Layer (Business Logic + Collections)

### Step 4.1 — Create AuthService.java

**Purpose:** Login and registration logic **What to write:**

- register(User user): validate, hash password with BCrypt, call userDAO.save()
- login(String email, String password): findByEmail, BCrypt.checkpw()
- Return User object on success, null on failure

### Step 4.2 — Create ExpenseService.java

**Purpose:** Collections & Generics rubric marks **What to write:**

- addExpense(Expense e): validate, save, then update budget spent amount
- getMonthlyExpenses(int userId): returns List
- getCategoryTotals(int userId): returns Map<String, Double> — category→total (This is the Collections + Generics mark — HashMap usage)
- getMonthlyTotal(int userId): returns double
- ApiResponse generic wrapper used in all return types

**Generic class to add:**

```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    // constructor, getters
}
```

### Step 4.3 — Create BudgetService.java

**Purpose:** Budget alert innovation **What to write:**

- createBudget(Budget b): validate, save
- getBudgetStatus(int userId): returns List with % used
- checkAlerts(int userId): returns List of alert messages -- "Shopping budget is 98% used!" etc. -- This is the INNOVATION — called on every dashboard load
- updateSpentOnExpenseAdd(int userId, String category, double amount)

### Step 4.4 — Create GoalService.java

**What to write:**

- createGoal(Goal g): validate, save
- addSavings(int goalId, double amount): update saved_amount, check if achieved
- getProgressDetails(Goal g): returns Map with percent, remaining, daysLeft -- daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), g.getDeadline()) -- This powers the INNOVATION deadline countdown

### Step 4.5 — Create InsightService.java

**Purpose:** Weekly plain-language tip (Innovation 4) **What to write:**

- getWeeklyInsight(int userId): returns String
- Compare this month vs last month totals per category
- Return sentence like "You spent ₹2,100 more on Food this month"
- CategoryMapper.java: HashMap<String, String> with 20+ keywords "swiggy"→"Food", "ola"→"Transport", "amazon"→"Shopping", etc.

---

## PHASE 5 — Thread Layer (Multithreading = 4 Marks)

### Step 5.1 — Create BudgetAlertThread.java

**Purpose:** Background thread — earns Multithreading marks **What to write:**

```java
public class BudgetAlertThread implements Runnable {
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                checkAllUserBudgets();      // synchronized method
                Thread.sleep(3600000);      // runs every 1 hour
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private synchronized void checkAllUserBudgets() {
        // fetch all active users
        // for each user, check budget usage
        // log alerts to console / audit_logs table
    }
}
```

- Start it in a ServletContextListener (AppStartupListener.java)
- Mark it as daemon thread so it stops when server stops

---

## PHASE 6 — Servlet Layer (Servlets = 7 Marks)

### Step 6.1 — Create AuthServlet.java (/auth)

**Purpose:** Handle login and register **doGet():** Check if already logged in → redirect to dashboard, else → login.jsp **doPost():** Read action param ("login" or "register")

- Login: AuthService.login() → set session → redirect by role
- Register: build User object → AuthService.register() → redirect login
- Set session attributes: userId, userName, userRole

### Step 6.2 — Create DashboardServlet.java (/dashboard)

**doGet():**

- Check session (if null → redirect /auth)
- Check role: if ADMIN → redirect /admin/dashboard
- Fetch: monthly total, expense list (last 5), budget list, goal list
- Fetch: InsightService.getWeeklyInsight()
- Fetch: BudgetService.checkAlerts() → pass alert list to JSP
- Forward to dashboard.jsp with all data as request attributes

### Step 6.3 — Create ExpenseServlet.java (/expenses)

**doGet():**

- Get filter params: month, year, category (from request)
- Fetch filtered expense list via ExpenseService
- Get category totals Map for chart data
- Forward to expenses.jsp

**doPost():**

- Read action: "add", "delete", "edit"
- Add: build Expense, validate, ExpenseService.addExpense()
- Auto-suggest category: CategoryMapper.suggest(title)
- Delete: ExpenseService.delete(id, userId) — security check userId
- Edit: update expense fields
- Redirect to /expenses after each action

### Step 6.4 — Create BudgetServlet.java (/budgets)

**doGet():**

- Fetch all budgets for user
- Calculate usage percent for each
- Get alert list from BudgetService
- Forward to budgets.jsp

**doPost():**

- action = "create": validate, BudgetService.createBudget()
- action = "delete": BudgetDAOImpl.delete()
- action = "edit": update budget fields
- Redirect to /budgets

### Step 6.5 — Create GoalServlet.java (/goals)

**doGet():**

- Fetch all goals, calculate progress % for each
- Get daysLeft for each goal
- Forward to goals.jsp

**doPost():**

- action = "create": GoalService.createGoal()
- action = "add-savings": GoalService.addSavings(goalId, amount)
- action = "delete": GoalDAOImpl.delete()
- Redirect to /goals

### Step 6.6 — Create Admin Servlets

**AdminDashServlet.java (/admin/dashboard):**

- Check role == ADMIN, else redirect /dashboard
- Fetch: total users, active users, expense count, security alerts
- Forward to admin-dashboard.jsp

**UserMgmtServlet.java (/admin/users):**

- doGet(): fetch all users with optional search/filter params
- doPost(): action = "create"/"update"/"delete"/"change-status"
- Log every action to audit_logs table

**SecurityServlet.java (/admin/security):**

- doGet(): fetch security settings from system_settings table
- doPost(): update encryption/2FA/session timeout settings
- Log changes to audit_logs

**SettingsServlet.java (/admin/settings):**

- doGet(): fetch all system_settings rows
- doPost(): update setting by key, log to audit_logs

---

## PHASE 7 — Frontend Layer (JSP + CSS + Responsive)

### Step 7.1 — Create global.css

**CSS Variables to define:**

```css
:root {
  --primary:    #f4a261;
  --primary-dk: #e07b35;
  --dark:       #1a1a2e;
  --surface:    #ffffff;
  --bg:         #f5f7fa;
  --text:       #1e1e2e;
  --text-muted: #888888;
  --border:     #eaedf3;
  --success:    #22c55e;
  --warning:    #f59e0b;
  --danger:     #ef4444;
  --radius:     12px;
  --shadow:     0 2px 8px rgba(0,0,0,0.08);
}
* { margin:0; padding:0; box-sizing:border-box; }
body { font-family: 'Segoe UI', sans-serif; background: var(--bg); color: var(--text); }
```

### Step 7.2 — Create responsive.css

**Breakpoints:**

```css
/* Tablet */
@media (max-width: 1024px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .two-col    { grid-template-columns: 1fr; }
}
/* Mobile */
@media (max-width: 768px) {
  .sidebar         { display: none; }
  .sidebar.open    { display: flex; position: fixed; z-index: 999; }
  .topbar          { padding: 0 16px; }
  .stats-grid      { grid-template-columns: 1fr 1fr; }
  .main-content    { padding: 16px; }
  .hamburger       { display: block; }
}
/* Small mobile */
@media (max-width: 480px) {
  .stats-grid   { grid-template-columns: 1fr; }
  .form-row     { grid-template-columns: 1fr; }
  .table-scroll { overflow-x: auto; }
}
```

### Step 7.3 — Create login.jsp and register.jsp

**login.jsp layout:**

- Split screen: left = branding (dark bg, logo, tagline)
- Right = form card (email, password, submit, link to register)
- Show error message if session has "error" attribute
- Responsive: on mobile = single column (form only)

### Step 7.4 — Create dashboard.jsp (User)

**Layout:**

- Topbar: logo, nav links, notification bell, avatar
- Sidebar: nav items (Home, Expenses, Budgets, Goals, Profile)
- Main: welcome banner → stats row → spending donut + budget bars → recent expenses → goal cards
- Alert banner at top if BudgetService.checkAlerts() returns non-empty list
- Mobile: hamburger menu opens sidebar as overlay

### Step 7.5 — Create expenses.jsp

**Layout:**

- Month pill selector (Jan–Dec row)
- Filter row: search, category dropdown, export button
- Two-column: left = add expense form, right = expense list
- Each row has edit and delete icon buttons
- Category icon shown based on category name
- Mobile: stack form above list

### Step 7.6 — Create budgets.jsp

**Layout:**

- Stats row: total budget, spent, remaining
- Two-column: left = create budget form, right = budget progress cards
- Each budget card shows progress bar with colour: green < 70%, amber 70–90%, red > 90%
- Warning box shown when > 90% used (Innovation 1)
- Mobile: single column

### Step 7.7 — Create goals.jsp

**Layout:**

- Filter pills: All / In Progress / Achieved / Failed
- Two-column: left = set goal form, right = goal cards
- Each goal card: emoji icon, title, progress bar, saved/target amounts
- Deadline countdown badge ("48 days left")
- Milestone badge shown at 25%, 50%, 75%, 100% (Innovation 2)
- Achieved goals show green celebration banner
- Mobile: single column

### Step 7.8 — Create admin-dashboard.jsp

**Layout:**

- Same topbar + sidebar (admin nav items)
- Stats: total users, active users, security alerts, transactions
- Recent registrations table
- User growth bar chart (drawn with Canvas API)
- Active security alerts table with action buttons

### Step 7.9 — Create user-management.jsp

**Layout:**

- Search bar + role filter + status filter + Add User button
- Full users table: avatar, name, email, role badge, status badge, joined date, actions
- Action buttons: edit (pencil icon), delete (trash icon)
- Responsive: table has horizontal scroll on mobile

### Step 7.10 — Create security.jsp

**Layout:**

- Stats row: security score, open incidents, encryption status
- Two-column: security controls (toggles) + audit log
- Toggle switches for: AES-256, 2FA, session timeout, IP whitelist, RBAC

### Step 7.11 — Create settings.jsp

**Layout:**

- Two-column: platform config form + feature flag toggles
- Change log table at bottom

---

## PHASE 8 — JavaScript Layer

### Step 8.1 — Create charts.js

**Purpose:** Draw spending charts using HTML5 Canvas API (no external library) **What to write:**

- drawDonutChart(canvasId, data, colors): draws category pie chart
- drawBarChart(canvasId, labels, values): draws monthly spending bar chart
- Data passed from JSP as JSON via request attributes

### Step 8.2 — Create offline.js

**Purpose:** Offline expense entry (Innovation 6) **What to write:**

- Listen for online/offline events
- On offline: show "Offline Mode" banner, store form submissions in localStorage
- On online: sync pending expenses by submitting stored data to /expenses
- Show "Syncing X pending expenses..." message

### Step 8.3 — Create validation.js

**Purpose:** Frontend form validation before submit **What to write:**

- validateExpenseForm(): amount > 0, title not empty, date not empty
- validateBudgetForm(): limit > 0, start before end
- validateGoalForm(): target > 0, deadline in future
- Show inline error messages below each field

---

## PHASE 9 — Web Configuration

### Step 9.1 — Configure web.xml

```xml
<!-- Map all servlets -->
<servlet>
  <servlet-name>AuthServlet</servlet-name>
  <servlet-class>com.myfinance.servlet.AuthServlet</servlet-class>
</servlet>
<servlet-mapping>
  <servlet-name>AuthServlet</servlet-name>
  <url-pattern>/auth</url-pattern>
</servlet-mapping>
<!-- Repeat for all servlets -->
<!-- Default welcome page -->
<welcome-file-list>
  <welcome-file>auth</welcome-file>
</welcome-file-list>
<!-- Session timeout: 30 mins -->
<session-config>
  <session-timeout>30</session-timeout>
</session-config>
<!-- Error pages -->
<error-page>
  <error-code>404</error-code>
  <location>/jsp/error/404.jsp</location>
</error-page>
<error-page>
  <error-code>403</error-code>
  <location>/jsp/error/403.jsp</location>
</error-page>
```

### Step 9.2 — Create AppStartupListener.java

**Purpose:** Start background thread on server startup

```java
@WebListener
public class AppStartupListener implements ServletContextListener {
    private Thread alertThread;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        BudgetAlertThread task = new BudgetAlertThread();
        alertThread = new Thread(task, "BudgetAlertThread");
        alertThread.setDaemon(true);
        alertThread.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (alertThread != null) alertThread.interrupt();
    }
}
```

---

## PHASE 10 — Testing & Deployment

### Step 10.1 — Unit Testing with JUnit 5

**What to test:**

- UserDAOImplTest: test save(), findByEmail(), delete()
- ExpenseServiceTest: test addExpense(), getCategoryTotals()
- BudgetServiceTest: test checkAlerts() at 70%, 90%, 100%
- GoalServiceTest: test getProgressPercent(), addSavings()

### Step 10.2 — Manual Testing Checklist

```
AUTH:
  [ ] Register new user
  [ ] Login as user → goes to user dashboard
  [ ] Login as admin → goes to admin dashboard
  [ ] Invalid login shows error
  [ ] Session expires after 30 mins

USER FLOWS:
  [ ] Add expense → appears in list
  [ ] Type "Swiggy" → category auto-suggests "Food"
  [ ] Add expense that pushes budget to 90% → alert appears
  [ ] Create budget with start/end date
  [ ] Budget progress bar shows correct %
  [ ] Create goal → progress starts at 0%
  [ ] Add savings to goal → progress updates
  [ ] Goal at 100% → marks as Achieved
  [ ] Offline: add expense with no internet → syncs when online

ADMIN FLOWS:
  [ ] View all users in table
  [ ] Search user by name
  [ ] Change user status to Inactive
  [ ] Delete user
  [ ] Toggle security setting
  [ ] Change system setting → appears in change log

RESPONSIVE:
  [ ] Mobile 375px: sidebar hidden, hamburger works
  [ ] Tablet 768px: 2-column stats grid
  [ ] Desktop 1440px: full layout with sidebar
```

### Step 10.3 — Deploy to Tomcat

```
1. Build WAR: mvn clean package
2. Copy myfinance.war to Tomcat/webapps/
3. Start Tomcat: bin/startup.sh (or startup.bat on Windows)
4. Open: http://localhost:8080/myfinance
5. Login with admin@myfinance.com / admin123
```

---

## 📊 Mark-by-Mark Final Checklist

### Problem Understanding & Solution Design (8 marks)

- [ ] Project report written with problem statement
- [ ] ER diagram (all 6 tables with relationships)
- [ ] Flow diagram (user registers → logs expense → checks budget)
- [ ] All input/output defined per module
- [ ] Innovation points documented

### Core Java Concepts (10 marks)

- [ ] BaseEntity abstract class with validate() — Inheritance
- [ ] User/Expense/Budget/Goal extend BaseEntity — Polymorphism
- [ ] FinanceOperation interface used — Interfaces
- [ ] DatabaseException + ValidationException — Exception Handling
- [ ] List<>, Map<>, ApiResponse used — Collections & Generics
- [ ] BudgetAlertThread implements Runnable + synchronized — Multithreading

### Database Integration JDBC (8 marks)

- [ ] DBConnection.java with raw JDBC
- [ ] All 4 DAOImpl classes using PreparedStatement
- [ ] ResultSet mapped to model objects
- [ ] Try-with-resources used for all connections
- [ ] All 6 tables created and populated

### Servlets & Web Integration (7 marks)

- [ ] 8+ HttpServlet subclasses written
- [ ] doGet() and doPost() implemented in each
- [ ] HttpSession used for auth (no JWT)
- [ ] RequestDispatcher.forward() to JSP pages
- [ ] All JSP pages use JSTL tags
- [ ] web.xml has all servlet mappings

---

## 🔑 Key Rules to Always Follow While Coding

1. **Security** — Always check session in every servlet before processing
2. **Security** — Always check userId matches resource owner before edit/delete
3. **Validation** — Always call entity.validate() before saving to DB
4. **JDBC** — Always use PreparedStatement (never String concatenation in SQL)
5. **JDBC** — Always use try-with-resources so connections auto-close
6. **Passwords** — Always hash with BCrypt, never store plain text
7. **Responsive** — Always test at 375px, 768px, and 1280px widths
8. **Error handling** — Every servlet has try-catch and shows user-friendly error

---

*MyFinance — Built with Java Servlets + JDBC + MySQL + JSP* *GUVI Project — Online Personal Finance Management Platform*
