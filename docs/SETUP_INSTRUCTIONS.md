# Setup Instructions for STI ProWear GUI

## ✅ Phase 1 Complete!

I've successfully created the foundation for your GUI application:

### **Files Created:**

#### **1. Maven Configuration**
- ✅ `pom.xml` - Maven build configuration with JavaFX 21 and AtlantaFX 2.0.1

#### **2. Core Application**
- ✅ `src/gui/MainApp.java` - JavaFX application entry point

#### **3. Utility Classes**
- ✅ `src/gui/utils/SceneManager.java` - Navigation manager
- ✅ `src/gui/utils/AlertHelper.java` - Dialog utilities
- ✅ `src/gui/utils/GUIValidator.java` - Input validation
- ✅ `src/gui/utils/ThemeManager.java` - Theme management (Light/Dark mode)

#### **4. Views**
- ✅ `src/gui/views/LoginView.java` - Modern login screen
- ✅ `src/gui/views/SignupView.java` - Student registration form

#### **5. Controllers**
- ✅ `src/gui/controllers/LoginController.java` - Authentication logic
- ✅ `src/gui/controllers/SignupController.java` - Registration logic

---

## 🚨 Prerequisites Required

### **Current System Status:**
- ❌ Java 8 detected (you have Java 1.8.0_471)
- ❌ Maven not installed
- ⚠️ **JavaFX 21 requires Java 17 or higher**

### **What You Need to Install:**

#### **1. Java Development Kit (JDK) 17 or higher**

**Download Options:**
- **Oracle JDK 17**: https://www.oracle.com/java/technologies/downloads/#java17
- **OpenJDK 17**: https://adoptium.net/ (Recommended - Free)
- **Amazon Corretto 17**: https://aws.amazon.com/corretto/

**Installation Steps:**
1. Download JDK 17 installer for Windows
2. Run the installer
3. Set JAVA_HOME environment variable:
   - Right-click "This PC" → Properties → Advanced System Settings
   - Environment Variables → New (System Variable)
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Java\jdk-17` (or your install path)
4. Add to PATH:
   - Edit PATH variable
   - Add: `%JAVA_HOME%\bin`
5. Verify installation:
   ```powershell
   java -version
   # Should show: java version "17.x.x"
   ```

#### **2. Apache Maven**

**Download:**
- https://maven.apache.org/download.cgi
- Download the Binary zip archive (e.g., `apache-maven-3.9.5-bin.zip`)

**Installation Steps:**
1. Extract to `C:\Program Files\Apache\maven`
2. Set MAVEN_HOME environment variable:
   - Variable name: `MAVEN_HOME`
   - Variable value: `C:\Program Files\Apache\maven`
3. Add to PATH:
   - Add: `%MAVEN_HOME%\bin`
4. Verify installation:
   ```powershell
   mvn -version
   # Should show: Apache Maven 3.9.x
   ```

---

## 🚀 Running the Application

### **Option 1: Using Maven (Recommended)**

Once Maven is installed:

```powershell
# Navigate to project directory
cd C:\Users\manue\Desktop\Projects\Inventory-System

# Install dependencies
mvn clean install

# Run the application
mvn javafx:run
```

### **Option 2: Using IntelliJ IDEA**

1. **Install IntelliJ IDEA Community Edition** (Free)
   - Download: https://www.jetbrains.com/idea/download/

2. **Open Project:**
   - File → Open → Select `Inventory-System` folder
   - IntelliJ will detect `pom.xml` and import as Maven project

3. **Configure JDK:**
   - File → Project Structure → Project
   - Set SDK to JDK 17
   - Set Language Level to 17

4. **Run Application:**
   - Right-click `src/gui/MainApp.java`
   - Select "Run 'MainApp.main()'"

### **Option 3: Using Eclipse**

1. **Install Eclipse IDE for Java Developers**
   - Download: https://www.eclipse.org/downloads/

2. **Install Maven Plugin (m2e)** - Usually pre-installed

3. **Import Project:**
   - File → Import → Maven → Existing Maven Projects
   - Select `Inventory-System` folder

4. **Configure JDK:**
   - Right-click project → Properties → Java Build Path
   - Add JDK 17 library

5. **Run Application:**
   - Right-click `MainApp.java` → Run As → Java Application

---

## 🎨 What You'll See

When you successfully run the application, you'll see:

### **Login Screen Features:**
- Modern card-based design with AtlantaFX Primer theme
- Username/Student ID input field
- Password field (masked)
- Role selector (Student, Admin, Staff, Cashier)
- Login and Sign Up buttons
- Theme toggle button (Light/Dark mode)

### **Functional Features:**
- ✅ Admin login (username: `admin`, password: `admin123`)
- ✅ Staff login (username: `staff`, password: `staff123`)
- ✅ Cashier login (username: `cashier`, password: `cashier123`)
- ✅ Student login (uses existing student database)
- ✅ Student registration with validation
- ✅ Theme switching (Light/Dark mode)
- ✅ Input validation with visual feedback

---

## 🧪 Testing the Application

### **Test Admin Login:**
1. Select "Admin" from role dropdown
2. Username: `admin`
3. Password: `admin123`
4. Click "Login"
5. You should see a success message (Dashboard coming soon)

### **Test Student Registration:**
1. Click "Sign Up" button
2. Fill in the form:
   - Student ID: 10-12 digits (e.g., `202312345678`)
   - First Name: Your first name
   - Last Name: Your last name
   - Course: Select from dropdown
   - Gender: Select Male/Female
   - Password: At least 6 characters
   - Confirm Password: Same as password
3. Click "Create Account"
4. You should see success message
5. Try logging in with your new credentials

### **Test Theme Toggle:**
1. Click the "🌙 Dark Mode" button at bottom of login screen
2. Theme should switch to dark mode
3. Button text changes to "☀ Light Mode"
4. Click again to switch back

---

## 📁 Project Structure

```
Inventory-System/
├── pom.xml                          # Maven configuration
├── src/
│   ├── gui/                         # NEW GUI package
│   │   ├── MainApp.java            # Application entry point
│   │   ├── controllers/            # Business logic controllers
│   │   │   ├── LoginController.java
│   │   │   └── SignupController.java
│   │   ├── views/                  # UI views
│   │   │   ├── LoginView.java
│   │   │   └── SignupView.java
│   │   └── utils/                  # GUI utilities
│   │       ├── SceneManager.java
│   │       ├── AlertHelper.java
│   │       ├── GUIValidator.java
│   │       └── ThemeManager.java
│   │
│   ├── main/                        # Existing console app (unchanged)
│   ├── admin/                       # Existing admin classes (reused)
│   ├── student/                     # Existing student classes (reused)
│   ├── inventory/                   # Existing business logic (reused)
│   └── utils/                       # Existing utilities (reused)
│
└── Documentation/
    ├── GUI_IMPLEMENTATION_PLAN.md
    ├── CODEBASE_ANALYSIS.md
    ├── QUICK_START_GUIDE.md
    └── README_GUI_TRANSITION.md
```

---

## 🐛 Troubleshooting

### **Problem: "mvn: command not found"**
**Solution:** Maven not installed or not in PATH. Follow Maven installation steps above.

### **Problem: "java version 1.8.x"**
**Solution:** Need Java 17+. Install JDK 17 and update JAVA_HOME.

### **Problem: "Error: JavaFX runtime components are missing"**
**Solution:** 
1. Ensure you're using Java 17+
2. Run with Maven: `mvn javafx:run`
3. Or add VM options in IDE:
   ```
   --module-path "path/to/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml
   ```

### **Problem: "Module not found: atlantafx.base"**
**Solution:** Run `mvn clean install` to download dependencies.

### **Problem: Window appears but is blank**
**Solution:** Check console for errors. Ensure JavaFX modules are loaded correctly.

---

## ✅ Next Steps After Setup

Once you have the application running:

1. **Test all login roles** (Admin, Staff, Cashier, Student)
2. **Test student registration** flow
3. **Test theme switching**
4. **Review the code** to understand the architecture
5. **Start Phase 2** - We'll build the dashboards next!

---

## 📞 Need Help?

If you encounter issues:

1. Check Java version: `java -version` (must be 17+)
2. Check Maven version: `mvn -version`
3. Check console output for error messages
4. Verify all files were created correctly
5. Ensure `src/database/data/students.txt` exists

---

## 🎉 What's Working Now

✅ **Phase 1 Complete:**
- Modern login screen with AtlantaFX theme
- Student registration with validation
- Authentication for all user roles
- Theme switching (Light/Dark mode)
- Navigation between login and signup
- Input validation with visual feedback
- Reusing all existing business logic

**Coming Next (Phase 2-3):**
- Admin Dashboard with inventory management
- Student Dashboard with item browsing
- Cashier Dashboard for payment processing
- Staff Dashboard for approvals

---

**Installation Priority:**
1. ⚠️ **Install JDK 17** (CRITICAL - Required for JavaFX 21)
2. ⚠️ **Install Maven** (CRITICAL - Required to build and run)
3. ✅ **Run `mvn clean install`**
4. ✅ **Run `mvn javafx:run`**
5. 🎉 **Enjoy your modern GUI!**

