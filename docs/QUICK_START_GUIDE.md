# Quick Start Guide - GUI Implementation

## 🚀 Getting Started

This guide will help you quickly set up and start building the GUI for the STI ProWear System.

---

## 📋 Prerequisites

### **Required Software**
- ✅ **Java JDK 17 or higher** (JavaFX requires Java 11+, recommend 17+)
- ✅ **Maven 3.6+** or **Gradle 7.0+**
- ✅ **IDE**: IntelliJ IDEA (recommended) or Eclipse with JavaFX plugin
- ✅ **Git** (for version control)

### **Check Your Java Version**
```bash
java -version
# Should show: java version "17.x.x" or higher
```

---

## 🛠️ Step 1: Setup Maven Project

### **Option A: Using Maven**

Create `pom.xml` in your project root:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.sti</groupId>
    <artifactId>prowear-system</artifactId>
    <version>2.0.0</version>
    <packaging>jar</packaging>

    <name>STI ProWear System</name>
    <description>Inventory Management System with GUI</description>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <javafx.version>21.0.1</javafx.version>
        <atlantafx.version>2.0.1</atlantafx.version>
    </properties>

    <dependencies>
        <!-- JavaFX Controls -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>

        <!-- JavaFX FXML (optional, for FXML-based UI) -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>${javafx.version}</version>
        </dependency>

        <!-- AtlantaFX Theme Library -->
        <dependency>
            <groupId>io.github.mkpaz</groupId>
            <artifactId>atlantafx-base</artifactId>
            <version>${atlantafx.version}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Maven Compiler Plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>

            <!-- JavaFX Maven Plugin -->
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <mainClass>gui.MainApp</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### **Install Dependencies**
```bash
mvn clean install
```

---

## 📁 Step 2: Create Package Structure

Create the following directory structure:

```
src/
├── main/
│   └── MerchSystem.java (existing - keep)
├── gui/
│   ├── MainApp.java (NEW)
│   ├── controllers/
│   ├── views/
│   ├── components/
│   └── utils/
├── inventory/ (existing - keep)
├── admin/ (existing - keep)
├── student/ (existing - keep)
├── user/ (existing - keep)
└── utils/ (existing - keep)
```

---

## 💻 Step 3: Create Your First GUI Window

### **Create `src/gui/MainApp.java`**

```java
package gui;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Set AtlantaFX theme
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        // Create UI components
        Label titleLabel = new Label("STI ProWear System");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label("Modern Inventory Management");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");

        Button testButton = new Button("Test Button");
        testButton.setOnAction(e -> {
            System.out.println("Button clicked! JavaFX is working!");
        });

        // Layout
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(titleLabel, subtitleLabel, testButton);
        root.setStyle("-fx-padding: 50px;");

        // Scene and Stage
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("STI ProWear System - GUI");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

---

## ▶️ Step 4: Run Your Application

### **Using Maven**
```bash
mvn javafx:run
```

### **Using IDE (IntelliJ IDEA)**
1. Right-click `MainApp.java`
2. Select "Run 'MainApp.main()'"
3. If you get module errors, add VM options:
   ```
   --module-path "path/to/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml
   ```

### **Expected Result**
You should see a window with:
- Title: "STI ProWear System"
- Subtitle: "Modern Inventory Management"
- A button that prints to console when clicked

---

## 🎨 Step 5: Test AtlantaFX Themes

Modify `MainApp.java` to test different themes:

```java
// Try different themes:
Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
// Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
// Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
// Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
```

Available themes:
- `PrimerLight` / `PrimerDark` - GitHub-inspired
- `NordLight` / `NordDark` - Nordic color palette
- `CupertinoLight` / `CupertinoDark` - macOS-inspired
- `Dracula` - Popular dark theme

---

## 🔧 Step 6: Create Helper Utilities

### **Create `src/gui/utils/AlertHelper.java`**

```java
package gui.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class AlertHelper {
    
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("Success!");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
```

### **Test AlertHelper**

Add to your `MainApp.java`:

```java
testButton.setOnAction(e -> {
    AlertHelper.showInfo("Test", "JavaFX and AtlantaFX are working!");
});
```

---

## 📚 Next Steps

Once you have the basic setup working:

1. ✅ **Read `GUI_IMPLEMENTATION_PLAN.md`** - Detailed roadmap
2. ✅ **Read `CODEBASE_ANALYSIS.md`** - Understand existing code
3. ✅ **Start Phase 1** - Build SceneManager and GUIValidator
4. ✅ **Build Login Screen** - First real feature
5. ✅ **Iterate through phases** - Follow the plan

---

## 🐛 Troubleshooting

### **Problem: "Error: JavaFX runtime components are missing"**
**Solution**: Add VM options:
```
--module-path "C:/path/to/javafx-sdk-21/lib" --add-modules javafx.controls,javafx.fxml
```

### **Problem: "Module not found: atlantafx.base"**
**Solution**: Run `mvn clean install` to download dependencies

### **Problem: "UnsupportedClassVersionError"**
**Solution**: Ensure you're using Java 17+. Check with `java -version`

### **Problem: Window appears but is blank**
**Solution**: Check console for errors. Ensure JavaFX modules are loaded.

---

## 📖 Useful Resources

- **JavaFX Documentation**: https://openjfx.io/
- **AtlantaFX GitHub**: https://github.com/mkpaz/atlantafx
- **AtlantaFX Sampler** (live demo): https://mkpaz.github.io/atlantafx/
- **JavaFX Tutorial**: https://jenkov.com/tutorials/javafx/index.html
- **Scene Builder** (visual designer): https://gluonhq.com/products/scene-builder/

---

## 💡 Tips

1. **Use Scene Builder** for visual layout design (optional)
2. **Test frequently** - Run after each small change
3. **Use AtlantaFX Sampler** to see component examples
4. **Keep console version** - Don't delete existing code
5. **Commit often** - Use Git to track changes

---

## ✅ Checklist

- [ ] Java 17+ installed
- [ ] Maven/Gradle configured
- [ ] pom.xml created with dependencies
- [ ] Package structure created
- [ ] MainApp.java created and runs successfully
- [ ] AtlantaFX theme displays correctly
- [ ] AlertHelper utility created and tested
- [ ] Ready to start Phase 1 of implementation

---

**Good luck with your GUI implementation! 🚀**

If you encounter issues, refer to the troubleshooting section or check the official documentation.

