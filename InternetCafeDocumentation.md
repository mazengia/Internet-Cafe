Internet Cafe Spring Boot Application
=====================================

Overview
--------
This project is a backend system for managing an internet cafe. It allows registration of computers, management of user sessions, billing, and reporting. It features a web dashboard, REST API, and optional agent software for desktop session tracking.

Main Features
-------------
- Register computers and assign them to branches.
- Start/stop user sessions and track usage time.
- Generate daily billing reports.
- JWT-based authentication with roles: ADMIN, AGENT, USER.
- Web dashboard for live monitoring and management.
- Optional agent for desktop lock/unlock detection.

Main Modules/Packages
---------------------
- **auth**: Authentication controllers and login logic.
- **authority**: User roles and authorities.
- **branch**: Branch entity and repository.
- **computer**: Computer entity, controller, and service for managing computers.
- **session**: Session entity, controller, and service for managing user sessions.
- **report**: Billing and reporting logic.
- **security**: JWT, security configuration, and filters.
- **service**: Core services (billing, agent, websocket, etc.).
- **users**: User management, controllers, and repositories.
- **utils**: Utility classes and helpers.
- **exception**: Custom exception handling.

Prerequisites & Dependencies
---------------------------
- Java 17+
- Maven (or use the included `mvnw` wrapper)
- Database: Microsoft SQL Server (default, configurable in `application.yaml`)
- Spring Boot 4.0.0 and related dependencies (see `pom.xml`)

Configuration
-------------
- Main config: `src/main/resources/application.yaml`
- Set database connection, server port (default: 8052), and JWT secret here.

Running the Application
----------------------
1. **Build:**
   ```bash
   ./mvnw -DskipTests clean package
   ```
2. **Run:**
   ```bash
   ./mvnw spring-boot:run
   # or
   java -jar target/internet-cafe-0.0.1-SNAPSHOT.jar
   ```
3. **Test:**
   ```bash
   ./mvnw test
   ```

Accessing the Application
------------------------
- **Dashboard UI:**  
  [http://localhost:8052/dashboard](http://localhost:8052/dashboard)  
  Live monitoring of computers, sessions, and revenue.

- **Login Page:**  
  [http://localhost:8052/login](http://localhost:8052/login)  
  Default admin:  
  - Username: `admin`  
  - Password: `admin`

User Manual
-----------

### 1. Logging In
- Go to `/login`, enter your credentials.
- On first run, use the default admin credentials.

### 2. Using the Dashboard
- View all registered computers and their statuses.
- See running sessions and live revenue.
- Start/stop sessions, lock/unlock computers (if authorized).

### 3. API Usage
- Authenticate:  
  `POST /api/auth/login` with JSON body `{ "username": "...", "password": "..." }`
- Use the returned JWT for subsequent API requests in the `Authorization` header.

### 4. Managing Computers & Sessions
- Register a computer:  
  `POST /computers` (ADMIN/AGENT)
- Start a session:  
  `POST /sessions/{computerId}/start`
- Stop a session:  
  `POST /sessions/{computerId}/stop-running`

### 5. Billing Reports
- Get daily report:  
  `GET /api/v1/billing/reports/daily?from=YYYY-MM-DD&to=YYYY-MM-DD` (ADMIN)

### 6. Agent Setup (Optional)
- **Windows:**
- Task Scheduler (Recommended)
  Place the JAR in `C:\apps\internet-cafe`, add a startup script to run minimized.
  Great 👍
  Since it **works correctly now**, here are the **FINAL, CLEAN TUTORIAL NOTES** you can keep or share.

---

# ✅ Run Spring Boot JAR at Windows Startup (Task Scheduler)

This is the **Windows equivalent of Ubuntu systemd / Startup Applications**.

---

## 📌 Prerequisites

* JDK installed

  ```
  C:\Program Files\Java\jdk-17\bin
  ```
* JAR located in a system path

  ```
  C:\apps\internet-cafe\internet-cafe-0.0.1-SNAPSHOT.jar
  ```

---

## 🛠 Step-by-Step Configuration

### 1️⃣   Open Task Scheduler

* Press **Win + R**
* Type:

  ```
  taskschd.msc
  ```

---

### 2️⃣   Create Task

Click **Create Task** (not *Create Basic Task*)

---

### 3️⃣   General Tab

* **Name**: `Internet Cafe Service`
* ✅ check box Run whether user is logged on or not
* ✅ check box Run with highest privileges
* **Configure for**: Windows 10 / 11

---

### 4️⃣   Triggers Tab

* Click **New**  
* **Begin the task**: ` choes At startup` 
* Click **OK**

---

### 5️⃣   Actions Tab (IMPORTANT)

Click **New** and use **exactly this**:

#### Program/script

```
cmd.exe
```

#### Add arguments

```bat
/c ""C:\Program Files\Java\jdk-17\bin\java.exe" -jar C:\apps\internet-cafe\internet-cafe-0.0.1-SNAPSHOT.jar >> C:\apps\internet-cafe\startup.log 2>&1"
```

#### Start in

```
C:\apps\internet-cafe
```

✔ Click **OK**

---

### 6️⃣   Conditions Tab

* ❌ Uncheck **Start the task only if the computer is on AC power**
* Leave others as needed

---

### 7️⃣  Settings Tab

Recommended:

* ✅ Allow task to be run on demand
* ✅ Restart task if it fails
* Restart every: **1 minute**
* Attempt restart: **3 times**

---

### 8️⃣   Save

* Click **OK**
* Enter Windows admin password

---

## ▶️ Test Without Reboot

* Right-click **Internet Cafe Service**
* Click **Run**

---

## 🔍 Verify It Is Running

### Check Java process

```bat
tasklist | findstr java
```

### Check application port (example 8080)

```bat
netstat -ano | findstr :8080
```

---

## 📄 Logs Location

Application logs are written to:

```
C:\apps\internet-cafe\startup.log
```

Use this file to debug:

* Startup errors
* DB connection issues
* Port conflicts
* Spring profiles
 
## ✅ Final Result

✔ JAR starts at Windows boot
✔ No user login required
✔ Logs available
✔ Stable and reproducible
 

- **Linux:**  
  Place the JAR in `/opt/internet-cafe/`, ensure `dbus-monitor` is installed, add to startup applications.

Troubleshooting
---------------
- Check `application.yaml` for correct database and JWT settings.
- Ensure Java 17+ is installed.
- For agent issues, verify required dependencies (`dbus-monitor` on Linux, `LogonUI.exe` on Windows).

---

This documentation provides a clear overview, module summary, and user manual for both developers and end users. If you need more detailed API documentation or developer setup instructions, let me know!

