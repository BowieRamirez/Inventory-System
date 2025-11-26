package gui.views;

import java.io.File;

import gui.controllers.StudentDashboardController;
import gui.utils.ThemeManager;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import student.Student;

/**
 * StudentDashboard - Main dashboard for student users
 * 
 * Features:
 * - Browse available items
 * - Create reservations
 * - View my reservations
 * - Track order status
 * - Request returns
 */
public class StudentDashboard {
    
    private BorderPane view;
    private StackPane contentArea;
    private StudentDashboardController controller;
    private Student student;
    
    // Navigation buttons
    private Button homeBtn;
    private Button shopBtn;
    private Button cartBtn;
    private Button requestPickupBtn;
    private Button claimItemsBtn;
    private Button myReservationsBtn;
    private Button helpBtn;
    
    // Top bar elements
    private StackPane toggleSwitch;
    private StackPane toggleCircle;
    private Label toggleIcon;
    private Button accountBtn;
    private Button notificationBtn;
    
    // Track current view
    private Runnable currentViewRefresher;
    // Track active tab name so recreating the top bar preserves selection
    private String activeTabName;
    
    public StudentDashboard(Student student) {
        this.student = student;
        controller = new StudentDashboardController(student);
        controller.setNotificationUpdateCallback(() -> updateNotificationBadge());
        controller.setRefreshCallback(() -> refreshCurrentView());
        controller.setNavigateToShopCallback(() -> {
            setActiveTab(shopBtn);
            showShop();
        });
        initializeView();
    }
    
    private void initializeView() {
        view = new BorderPane();
        view.setMaxWidth(Double.MAX_VALUE);
        view.setMaxHeight(Double.MAX_VALUE);
        String bgColor = ThemeManager.isDarkMode() ? "-color-bg-default" : "#F8F9FA";
        view.setStyle("-fx-background-color: " + bgColor + ";");
        
        // Create top bar
        view.setTop(createTopBar());

        // Initialize notification badge
        updateNotificationBadge();
        
        // Create content area
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20, 20, 20, 20));
        contentArea.setMaxWidth(Double.MAX_VALUE);
        contentArea.setMaxHeight(Double.MAX_VALUE);
        String contentBg = ThemeManager.isDarkMode() ? "-color-bg-default" : "#F8F9FA";
        contentArea.setStyle("-fx-background-color: " + contentBg + ";");
        view.setCenter(contentArea);
        
        // Show home by default
        showHome();
    }
    
    /**
     * Create top navigation bar with logo and tabs
     */
    private VBox createTopBar() {
        VBox topContainer = new VBox(0);
        String bgGradient = ThemeManager.isDarkMode()
            ? "linear-gradient(to right, #1a2a6c 0%, #0d1b4d 50%, #1a2a6c 100%)"
            : "linear-gradient(to right, #1e3c72 0%, #2a5298 50%, #1e3c72 100%)";
        topContainer.setStyle("-fx-background-color: " + bgGradient + ";");
        
        // Single unified row: Logo, Navigation (centered), and Account controls
        HBox headerRow = new HBox(20);
        headerRow.setPadding(new Insets(15, 20, 15, 20));
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setStyle("-fx-background-color: transparent;");
        
        // Logo container with name
        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPrefWidth(280);
        logoBox.setStyle("-fx-background-color: transparent;");
        
        // Try to load logo image
        try {
            File logoFile = new File("src/database/data/images/NewLogo.png");
            if (logoFile.exists()) {
                Image logoImage = new Image(logoFile.toURI().toString());
                ImageView logoView = new ImageView(logoImage);
                logoView.setFitHeight(45);
                logoView.setFitWidth(80);
                logoView.setPreserveRatio(true);
                logoBox.getChildren().add(logoView);
            } else {
                // Fallback to text logo
                Label logoText = new Label("STI ProWear");
                logoText.setFont(Font.font("System", FontWeight.BOLD, 18));
                logoText.setStyle("-fx-text-fill: #1a1a1a;");
                logoBox.getChildren().add(logoText);
            }
        } catch (Exception e) {
            // Fallback to text logo
            Label logoText = new Label("STI ProWear");
            logoText.setFont(Font.font("System", FontWeight.BOLD, 18));
            logoText.setStyle("-fx-text-fill: #1a1a1a;");
            logoBox.getChildren().add(logoText);
        }
        
        // Add company name and student name
        VBox namesBox = new VBox(2);
        namesBox.setAlignment(Pos.CENTER_LEFT);
        
        Label companyNameLabel = new Label("STI ProWare Novaliches");
        String textColor = "#ffffff";
        companyNameLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        companyNameLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
        
        Label studentNameLabel = new Label(student.getFirstName() + " " + student.getLastName());
        studentNameLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        studentNameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        namesBox.getChildren().addAll(companyNameLabel, studentNameLabel);
        logoBox.getChildren().add(namesBox);
        
        // Create navigation buttons/tabs
        homeBtn = createNavTab("Home", false);
        shopBtn = createNavTab("Shop", false);
        cartBtn = createNavTab("Cart", false);
        requestPickupBtn = createNavTab("Request Pickup", false);
        claimItemsBtn = createNavTab("Claim Items", false);
        myReservationsBtn = createNavTab("My Reservations", false);
        helpBtn = createNavTab("Help", false);
        
        // Preserve previously active tab if set, otherwise default to Home
        if (activeTabName == null) {
            activeTabName = "Home";
        }
        
        // Button actions
        homeBtn.setOnAction(e -> {
            setActiveTab(homeBtn);
            showHome();
        });
        
        shopBtn.setOnAction(e -> {
            setActiveTab(shopBtn);
            showShop();
        });
        
        cartBtn.setOnAction(e -> {
            setActiveTab(cartBtn);
            showCart();
        });
        
        requestPickupBtn.setOnAction(e -> {
            setActiveTab(requestPickupBtn);
            showRequestPickup();
        });
        
        claimItemsBtn.setOnAction(e -> {
            setActiveTab(claimItemsBtn);
            showClaimItems();
        });
        
        myReservationsBtn.setOnAction(e -> {
            setActiveTab(myReservationsBtn);
            showMyReservations();
        });

        helpBtn.setOnAction(e -> {
            setActiveTab(helpBtn);
            showHelp();
        });
        
        // Set up cart update callback
        controller.setCartUpdateCallback(this::updateCartBadge);
        
        // Navigation container (centered)
        HBox navContainer = new HBox(30);
        navContainer.setAlignment(Pos.CENTER);
        navContainer.getChildren().addAll(homeBtn, shopBtn, cartBtn, requestPickupBtn, claimItemsBtn, myReservationsBtn, helpBtn);
        HBox.setHgrow(navContainer, Priority.ALWAYS);

        // Apply the active tab based on last selection
        switch (activeTabName) {
            case "Shop": setActiveTab(shopBtn); break;
            case "Cart": setActiveTab(cartBtn); break;
            case "Request Pickup": setActiveTab(requestPickupBtn); break;
            case "Claim Items": setActiveTab(claimItemsBtn); break;
            case "My Reservations": setActiveTab(myReservationsBtn); break;
            case "Help": setActiveTab(helpBtn); break;
            default: setActiveTab(homeBtn); break;
        }
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Theme toggle switch - reuse LoginView implementation
        // Toggle switch button (pill shape with sliding circle)
        toggleSwitch = new StackPane();
        toggleSwitch.setPrefWidth(70);
        toggleSwitch.setPrefHeight(32);
        toggleSwitch.setMaxWidth(70);
        toggleSwitch.setMaxHeight(32);
        
        // Background pill
        Region toggleBg = new Region();
        toggleBg.setPrefWidth(70);
        toggleBg.setPrefHeight(32);
        String toggleBgColor = ThemeManager.isDarkMode() 
            ? "linear-gradient(to right, #1e3a5f 0%, #0f2a4a 50%, #1e3a5f 100%)"
            : "linear-gradient(to right, #fff9e6 0%, #ffefb3 50%, #fff9e6 100%)";
        toggleBg.setStyle(
            "-fx-background-color: " + toggleBgColor + ";" +
            "-fx-background-radius: 16px;" +
            "-fx-border-color: " + (ThemeManager.isDarkMode() ? "rgba(77, 163, 255, 0.3)" : "rgba(245, 197, 66, 0.3)") + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 16px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0, 0, 2);"
        );
        
        // Sliding circle with icon
        toggleCircle = new StackPane();
        toggleCircle.setPrefWidth(26);
        toggleCircle.setPrefHeight(26);
        toggleCircle.setMaxWidth(26);
        toggleCircle.setMaxHeight(26);
        
        String circleColor = ThemeManager.isDarkMode() 
            ? "linear-gradient(from 0% 0% to 100% 100%, #6bb6ff 0%, #2a7fd9 50%, #1a5fa0 100%)"
            : "linear-gradient(from 0% 0% to 100% 100%, #ffd700 0%, #ffed4e 50%, #f5b542 100%)";
        toggleCircle.setStyle(
            "-fx-background-color: " + circleColor + ";" +
            "-fx-background-radius: 13px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 3);"
        );
        
        // Icon inside circle
        toggleIcon = new Label(ThemeManager.isDarkMode() ? "🌙" : "☀");
        toggleIcon.setFont(Font.font("System", FontWeight.BOLD, 12));
        toggleIcon.setStyle("-fx-text-fill: #000000;");
        toggleCircle.getChildren().add(toggleIcon);
        
        // Position circle based on theme using translateX (for smooth animation)
        StackPane.setAlignment(toggleCircle, Pos.CENTER_LEFT);
        StackPane.setMargin(toggleCircle, new Insets(0, 2, 0, 2));
        double initialX = ThemeManager.isDarkMode() ? 42 : 2; // 70 - 26 - 2 = 42
        toggleCircle.setTranslateX(initialX);
        
        toggleSwitch.getChildren().addAll(toggleBg, toggleCircle);
        toggleSwitch.setOnMouseClicked(e -> toggleTheme());
        toggleSwitch.setStyle("-fx-cursor: hand;");
        
        // My account dropdown button
        accountBtn = new Button("👤 My account ▼");
        accountBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.2);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;" +
            "-fx-padding: 8 12;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;"
        );
        accountBtn.setOnMouseEntered(e -> accountBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.3);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;" +
            "-fx-padding: 8 12;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;"
        ));
        accountBtn.setOnMouseExited(e -> accountBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.2);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;" +
            "-fx-padding: 8 12;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;"
        ));
        
        // Create context menu for account dropdown
        ContextMenu accountMenu = new ContextMenu();
        
        // Account Info menu item
        MenuItem accountInfoItem = new MenuItem("Account Info");
        accountInfoItem.setStyle("-fx-font-size: 13px;");
        accountInfoItem.setOnAction(e -> showAccountDetails());

        // Change Password menu item
        MenuItem changePasswordItem = new MenuItem("Change Password");
        changePasswordItem.setStyle("-fx-font-size: 13px;");
        changePasswordItem.setOnAction(e -> controller.openChangePasswordDialog());

        // Logout menu item
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setStyle("-fx-font-size: 13px; -fx-text-fill: #CF222E;");
        logoutItem.setOnAction(e -> controller.handleLogout());
        
        accountMenu.getItems().addAll(accountInfoItem, changePasswordItem, logoutItem);
        
        // Show menu when account button is clicked
        accountBtn.setOnAction(e -> {
            accountMenu.show(accountBtn, Side.BOTTOM, 0, 5);
        });
        
        // Notification button (bell) - clickable to open notifications dialog
        notificationBtn = new Button("🔔");
        notificationBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.12);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;" +
            "-fx-padding: 8 10;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;"
        );
        notificationBtn.setOnAction(e -> {
            controller.showNotificationsDialog();
            // update badge after user views
            updateNotificationBadge();
        });

        headerRow.getChildren().addAll(logoBox, navContainer, toggleSwitch, notificationBtn, accountBtn);
        
        // Add thicker separator line under header for stronger visual weight
        Separator separator = new Separator();
        separator.setPrefHeight(2);
        separator.setMinHeight(2);
        separator.setMaxHeight(2);
        separator.setStyle(
            "-fx-background-color: rgba(0,0,0,0.35);" +
            "-fx-padding: 0;"
        );
        
        topContainer.getChildren().addAll(headerRow, separator);
        topContainer.setPadding(new Insets(0));
        return topContainer;
    }
    
    /**
     * Create navigation tab button
     */
    private Button createNavTab(String text, boolean active) {
        Button btn = new Button(text);
        btn.setPadding(new Insets(12, 16, 12, 16));
        btn.setAlignment(Pos.CENTER);
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: rgba(255,255,255,0.9);" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: 600;" +
            "-fx-border-width: 0;" +
            "-fx-cursor: hand;" +
            "-fx-font-family: System;"
        );
        
        // Hover effect
        // Remove hover-based font weight changes to avoid sticky bold effect.
        // Nav buttons remain consistently styled; only active state is bolded.
        btn.setOnMouseEntered(e -> { /* no-op to keep style stable on hover */ });
        btn.setOnMouseExited(e -> { /* no-op to keep style stable on hover */ });
        
        return btn;
    }
    
    /**
     * Set active navigation tab
     */
    private void setActiveTab(Button activeBtn) {
        // remember active tab name so recreating top bar keeps selection
        if (activeBtn != null) {
            activeTabName = activeBtn.getText();
        }
        Button[] buttons = {homeBtn, shopBtn, cartBtn, requestPickupBtn, claimItemsBtn, myReservationsBtn, helpBtn};
        
        for (Button btn : buttons) {
            if (btn == activeBtn) {
                // Active button - white, solid
                btn.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 16px;" +
                    "-fx-font-weight: 700;" +
                    "-fx-border-width: 0;" +
                    "-fx-cursor: hand;" +
                    "-fx-font-family: System;"
                );
            } else {
                // Inactive button - semi-transparent white
                btn.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: rgba(255,255,255,0.9);" +
                    "-fx-font-size: 16px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-border-width: 0;" +
                    "-fx-cursor: hand;" +
                    "-fx-font-family: System;"
                );
            }
        }
    }

    private void showHelp() {
        contentArea.getChildren().clear();
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 10;");
        
        VBox mainBox = new VBox(12);
        mainBox.setPadding(new Insets(20));
        mainBox.setStyle("-fx-border-color: transparent;");
        mainBox.setId("help-content");
        
        // Header
        Label headerTitle = new Label("📚 Help & User Guide");
        headerTitle.setFont(Font.font("System", FontWeight.BOLD, 22));
        headerTitle.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#e0e0ff" : "#1e3c72") + ";");
        
        Label headerSubtitle = new Label("Click on any section below to expand and view details");
        headerSubtitle.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#a0a0c0" : "#666666") + "; -fx-font-size: 12px;");
        
        VBox headerBox = new VBox(4, headerTitle, headerSubtitle);
        headerBox.setPadding(new Insets(0, 0, 10, 0));
        
        // Create Accordion with TitledPanes
        javafx.scene.control.Accordion accordion = new javafx.scene.control.Accordion();
        
        // ====== WELCOME SECTION ======
        VBox welcomeContent = new VBox(8);
        welcomeContent.setPadding(new Insets(12));
        welcomeContent.getChildren().add(createHelpDescription("This is your one-stop shop for all official STI merchandise including uniforms, jerseys, and special items. Follow this guide to browse, reserve, and pickup your merchandise."));
        javafx.scene.control.TitledPane welcomePane = createAccordionPane("👋 Welcome to STI ProWear Shop", welcomeContent);
        
        // ====== GETTING STARTED ======
        VBox startedContent = new VBox(4);
        startedContent.setPadding(new Insets(12));
        startedContent.getChildren().addAll(
            createBulletPoint("Create your account with email and password"),
            createBulletPoint("Verify your student course (only items for your course available)"),
            createBulletPoint("Review Terms and Conditions before proceeding"),
            createBulletPoint("Browse items, select sizes, and make reservations"),
            createBulletPoint("Pay at cashier and pickup your items")
        );
        javafx.scene.control.TitledPane startedPane = createAccordionPane("🚀 Getting Started", startedContent);
        
        // ====== NAVIGATION GUIDE ======
        VBox navContent = new VBox(4);
        navContent.setPadding(new Insets(12));
        navContent.getChildren().addAll(
            createBulletPoint("Home: Dashboard with quick access to key features"),
            createBulletPoint("Shop: Browse items by category or search by code"),
            createBulletPoint("My Reservations: Track your orders and their status"),
            createBulletPoint("Account: Update profile, change password, view history"),
            createBulletPoint("Help: Access this guide anytime")
        );
        javafx.scene.control.TitledPane navPane = createAccordionPane("🧭 Navigation Guide", navContent);
        
        // ====== SHOPPING BASICS ======
        VBox shoppingContent = new VBox(4);
        shoppingContent.setPadding(new Insets(12));
        shoppingContent.getChildren().addAll(
            createBulletPoint("Tap 'Shop' to browse available merchandise"),
            createBulletPoint("Filter by category: Course Items or STI Special Items"),
            createSubBullet("Course Items: Available only for your enrolled course"),
            createSubBullet("STI Special Items: Available to ALL students"),
            createBulletPoint("Select item to view details: Price, sizes, available stock"),
            createBulletPoint("Choose your size: XS, S, M, L, XL, XXL, One Size"),
            createBulletPoint("Enter quantity desired (check stock availability first)"),
            createBulletPoint("Review total price before confirming reservation")
        );
        javafx.scene.control.TitledPane shoppingPane = createAccordionPane("🛍️ Shopping Basics", shoppingContent);
        
        // ====== RESERVATION STATUS ======
        VBox statusContent = new VBox(4);
        statusContent.setPadding(new Insets(12));
        statusContent.getChildren().addAll(
            createStatusBullet("🔵 PENDING", "Admin is reviewing your request"),
            createStatusBullet("✅ APPROVED - READY FOR PICKUP", "Items are ready! Go to cashier to pay and collect"),
            createStatusBullet("✓ COMPLETED", "You have successfully picked up your items"),
            createStatusBullet("❌ CANCELLED", "Your reservation was cancelled (check reason)"),
            createStatusBullet("🔄 RETURN REQUEST", "You've requested to return/exchange items")
        );
        javafx.scene.control.TitledPane statusPane = createAccordionPane("📊 Understanding Reservation Status", statusContent);
        
        // ====== PAYMENT PROCESS ======
        VBox paymentContent = new VBox(4);
        paymentContent.setPadding(new Insets(12));
        paymentContent.getChildren().addAll(
            createBulletPoint("Wait for admin approval of your reservation"),
            createBulletPoint("Once approved, go to cashier desk with your student ID"),
            createBulletPoint("Provide your Reservation ID to cashier"),
            createBulletPoint("Cashier will verify items and total amount"),
            createBulletPoint("Make payment (currently cash only) to cashier"),
            createBulletPoint("Receive receipt and pickup your merchandise"),
            createBulletPoint("Verify items match your reservation before leaving")
        );
        javafx.scene.control.TitledPane paymentPane = createAccordionPane("💳 Payment Process", paymentContent);
        
        // ====== RETURNS & REPLACEMENTS ======
        VBox returnContent = new VBox(4);
        returnContent.setPadding(new Insets(12));
        returnContent.getChildren().addAll(
            createBulletPoint("Item damaged or defective? Request replacement immediately"),
            createBulletPoint("Wrong size received? Initiate exchange within 7 days"),
            createBulletPoint("Go to 'Request Pickup' in your account"),
            createBulletPoint("Select reason: Damage, Wrong Size, Incorrect Item, Other"),
            createBulletPoint("Upload clear photos of the issue (required for damage claims)"),
            createBulletPoint("Provide detailed explanation in text area"),
            createBulletPoint("Select replacement item (if applicable)"),
            createBulletPoint("Wait for staff approval - usually within 24-48 hours")
        );
        javafx.scene.control.TitledPane returnPane = createAccordionPane("🔄 Returns & Replacements", returnContent);
        
        // ====== TIPS FOR SUCCESS ======
        VBox tipsContent = new VBox(4);
        tipsContent.setPadding(new Insets(12));
        tipsContent.getChildren().addAll(
            createTipBullet("Check available stock BEFORE reserving - avoid disappointment"),
            createTipBullet("Double-check your size - sizes run differently per item"),
            createTipBullet("Save your Reservation ID - you'll need it for payment"),
            createTipBullet("Pickup within 7 days - items are held for limited time"),
            createTipBullet("Bring student ID - required to claim merchandise"),
            createTipBullet("Check item condition before leaving cashier"),
            createTipBullet("Report issues immediately - don't wait days to complain")
        );
        javafx.scene.control.TitledPane tipsPane = createAccordionPane("💡 Tips for Success", tipsContent);
        
        // ====== SEARCH & CATEGORIES ======
        VBox searchContent = new VBox(4);
        searchContent.setPadding(new Insets(12));
        searchContent.getChildren().addAll(
            createBulletPoint("Search by item code: 1000-9999 range"),
            createBulletPoint("Browse by category: Course, Gender, Type"),
            createBulletPoint("Filter by size to see current stock"),
            createBulletPoint("Sort by price, newest, or popularity"),
            createBulletPoint("Use 'View Similar' to find related items")
        );
        javafx.scene.control.TitledPane searchPane = createAccordionPane("🔍 Search & Categories", searchContent);
        
        // ====== ACCOUNT MANAGEMENT ======
        VBox accountContent = new VBox(4);
        accountContent.setPadding(new Insets(12));
        accountContent.getChildren().addAll(
            createBulletPoint("View your profile information and course"),
            createBulletPoint("Change your password from Account tab"),
            createBulletPoint("Update contact information as needed"),
            createBulletPoint("View order history and past purchases"),
            createBulletPoint("Check reservation activity log")
        );
        javafx.scene.control.TitledPane accountPane = createAccordionPane("👤 Account Management", accountContent);
        
        // ====== FAQ ======
        VBox faqContent = new VBox(8);
        faqContent.setPadding(new Insets(12));
        faqContent.getChildren().addAll(
            createFAQItem("Q: Can I cancel my reservation?", "A: Yes, but only if it's PENDING. Contact admin if already APPROVED."),
            createFAQItem("Q: My reservation was rejected. Why?", "A: Check the rejection reason. Could be: out of stock, invalid size, or course mismatch."),
            createFAQItem("Q: How long can I hold my items?", "A: Once approved, items are held for 7 days. Pick them up before expiration."),
            createFAQItem("Q: Can I reserve items for another student?", "A: No, items are personal. Each student must create their own account."),
            createFAQItem("Q: What payment methods do you accept?", "A: Currently cash only. Card/digital payments coming soon."),
            createFAQItem("Q: Can I change my order after reserving?", "A: No, you must cancel and create a new reservation.")
        );
        javafx.scene.control.TitledPane faqPane = createAccordionPane("❓ Frequently Asked Questions", faqContent);
        
        // ====== IMPORTANT POLICIES ======
        VBox policiesContent = new VBox(4);
        policiesContent.setPadding(new Insets(12));
        policiesContent.getChildren().addAll(
            createBulletPoint("Course-restricted items available ONLY to enrolled students"),
            createBulletPoint("One account per student - duplicates will be deactivated"),
            createBulletPoint("No refunds for completed purchases"),
            createBulletPoint("Items must be collected within 7 days of approval"),
            createBulletPoint("Damage claims require photo evidence"),
            createBulletPoint("Misuse of system may result in account termination")
        );
        javafx.scene.control.TitledPane policiesPane = createAccordionPane("📜 Important Policies", policiesContent);
        
        // ====== NEED MORE HELP ======
        VBox supportContent = new VBox(4);
        supportContent.setPadding(new Insets(12));
        supportContent.getChildren().addAll(
            createBulletPoint("Visit Cashier Desk: Located at Student Services Center"),
            createBulletPoint("Email: merch-support@sti.edu.ph"),
            createBulletPoint("Chat with Staff: Message from your account"),
            createBulletPoint("Phone: 1-800-STI-SHOP"),
            createBulletPoint("Hours: Monday-Friday, 9 AM - 5 PM")
        );
        javafx.scene.control.TitledPane supportPane = createAccordionPane("📞 Need More Help?", supportContent);
        
        // Add all panes to accordion
        accordion.getPanes().addAll(
            welcomePane, startedPane, navPane, shoppingPane, statusPane,
            paymentPane, returnPane, tipsPane, searchPane, accountPane,
            faqPane, policiesPane, supportPane
        );
        
        // Expand the first pane by default
        accordion.setExpandedPane(welcomePane);
        
        mainBox.getChildren().addAll(headerBox, accordion);
        
        scrollPane.setContent(mainBox);
        contentArea.getChildren().add(scrollPane);
        currentViewRefresher = this::showHelp;
    }
    
    /**
     * Create a styled TitledPane for the accordion
     */
    private javafx.scene.control.TitledPane createAccordionPane(String title, VBox content) {
        javafx.scene.control.TitledPane pane = new javafx.scene.control.TitledPane(title, content);
        pane.setAnimated(false);
        pane.setCollapsible(true);
        
        // Style the titled pane header - larger font
        String textColor = ThemeManager.isDarkMode() ? "#e0e0ff" : "#1e3c72";
        
        pane.setStyle(
            "-fx-font-weight: bold; " +
            "-fx-font-size: 15px; " +
            "-fx-text-fill: " + textColor + ";"
        );
        
        // Clean content - no border/background container
        content.setStyle("-fx-background-color: transparent;");
        
        return pane;
    }
    
    /**
     * Create a description label for help sections
     */
    private Label createHelpDescription(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#c0c0d0" : "#555555") + "; -fx-font-size: 14px;");
        return label;
    }
    
    /**
     * Show home view
     */
    private void showHome() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createHomeView());
        currentViewRefresher = this::showHome;
    }
    
    /**
     * Show shop view
     */
    private void showShop() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createShopView());
        currentViewRefresher = this::showShop;
    }
    
    /**
     * Show cart view
     */
    private void showCart() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createCartView());
        currentViewRefresher = this::showCart;
    }
    
    /**
     * Update cart badge with current item count
     */
    private void updateCartBadge() {
        int cartSize = controller.getCartSize();
        cartBtn.setText("Cart (" + cartSize + ")");
    }

    /**
     * Update notification badge with unread count
     */
    private void updateNotificationBadge() {
        if (notificationBtn == null) return;
        int count = controller.getUnreadNotificationCount();
        if (count > 0) {
            notificationBtn.setText("🔔 (" + count + ")");
        } else {
            notificationBtn.setText("🔔");
        }
    }
    
    /**
     * Refresh the current view
     */
    private void refreshCurrentView() {
        if (currentViewRefresher != null) {
            currentViewRefresher.run();
        }
    }
    
    /**
     * Show my reservations view
     */
    private void showMyReservations() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createMyReservationsView());
        currentViewRefresher = this::showMyReservations;
    }
    
    /**
     * Show request pickup view
     */
    private void showRequestPickup() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createRequestPickupView());
        currentViewRefresher = this::showRequestPickup;
    }
    
    /**
     * Show claim items view
     */
    private void showClaimItems() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createClaimItemsView());
        currentViewRefresher = this::showClaimItems;
    }
    
    /**
     * Toggle between light and dark theme
     */
    private void toggleTheme() {
        // Prevent double-clicks during animation
        toggleSwitch.setDisable(true);

        boolean willBeDark = !ThemeManager.isDarkMode();
        double toX = willBeDark ? 42 : 2; // slide right for dark, left for light

        Timeline slideAnimation = new Timeline(
            new KeyFrame(
                Duration.millis(220),
                new KeyValue(toggleCircle.translateXProperty(), toX, Interpolator.EASE_BOTH)
            )
        );

        slideAnimation.setOnFinished(evt -> {
            // Apply theme after the slider finishes for non-instant feel
            ThemeManager.toggleLightDark();

            // Update icon
            toggleIcon.setText(ThemeManager.isDarkMode() ? "🌙" : "☀");

            // Update circle style based on final theme
            String circleColor = ThemeManager.isDarkMode()
                ? "linear-gradient(from 0% 0% to 100% 100%, #6bb6ff 0%, #2a7fd9 50%, #1a5fa0 100%)"
                : "linear-gradient(from 0% 0% to 100% 100%, #ffd700 0%, #ffed4e 50%, #f5b542 100%)";
            toggleCircle.setStyle(
                "-fx-background-color: " + circleColor + ";" +
                "-fx-background-radius: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 3);"
            );

            // Update background style
            Region toggleBg = (Region) toggleSwitch.getChildren().get(0);
            String toggleBgColor = ThemeManager.isDarkMode()
                ? "linear-gradient(to right, #1e3a5f 0%, #0f2a4a 50%, #1e3a5f 100%)"
                : "linear-gradient(to right, #fff9e6 0%, #ffefb3 50%, #fff9e6 100%)";
            toggleBg.setStyle(
                "-fx-background-color: " + toggleBgColor + ";" +
                "-fx-background-radius: 16px;" +
                "-fx-border-color: " + (ThemeManager.isDarkMode() ? "rgba(77, 163, 255, 0.3)" : "rgba(245, 197, 66, 0.3)") + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 16px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0, 0, 2);"
            );

            // Refresh the themed UI last
            updateTheme();

            toggleSwitch.setDisable(false);
        });

        slideAnimation.play();
    }
    
    /**
     * Update theme colors when theme changes
     */
    private void updateTheme() {
        // Update main background
        String bgColor = ThemeManager.isDarkMode() ? "-color-bg-default" : "#F8F9FA";
        view.setStyle("-fx-background-color: " + bgColor + ";");
        
        // Update content area background
        String contentBg = ThemeManager.isDarkMode() ? "-color-bg-default" : "#F8F9FA";
        contentArea.setStyle("-fx-background-color: " + contentBg + "; -fx-padding: 20;");
        
        // Recreate top bar to update all colors
        view.setTop(createTopBar());
        // Re-apply notification badge after recreating top bar
        updateNotificationBadge();
        
        // Refresh help if displayed, or re-apply current view
        if (contentArea.lookup("#help-content") != null) {
            showHelp();
        } else if (currentViewRefresher != null) {
            currentViewRefresher.run();
        }
    }
    
    /**
     * Show account details dialog
     */
    private void showAccountDetails() {
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Account Details");
        dialog.setHeaderText("Your Account Information");
        
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-min-width: 400px;");
        
        // Account info
        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-border-color: -color-border-default;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 6px;" +
            "-fx-background-radius: 6px;"
        );
        
        Label nameLabel = new Label("Full Name: " + student.getFullName());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: -color-fg-default;");
        
        Label idLabel = new Label("Student ID: " + student.getStudentId());
        idLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: -color-fg-default;");
        
        Label courseLabel = new Label("Course: " + student.getCourse());
        courseLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: -color-fg-default;");
        
        Label genderLabel = new Label("Gender: " + student.getGender());
        genderLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: -color-fg-default;");
        
        Label statusLabel = new Label("Status: " + student.getAccountStatus());
        statusLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: -color-fg-default;");
        
        infoBox.getChildren().addAll(nameLabel, idLabel, courseLabel, genderLabel, statusLabel);
        
        content.getChildren().addAll(infoBox);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }
    
    /**
     * Get the view node
     */
    public BorderPane getView() {
        return view;
    }
    
    // ===== HELP SECTION HELPER METHODS =====
    
    private VBox createBulletPoint(String text) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(4, 0, 4, 20));
        Label bullet = new Label("• " + text);
        bullet.setWrapText(true);
        bullet.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#d0d0e0" : "#333333") + "; -fx-font-size: 14px;");
        box.getChildren().add(bullet);
        return box;
    }
    
    private VBox createSubBullet(String text) {
        VBox box = new VBox(2);
        box.setPadding(new Insets(2, 0, 2, 40));
        Label bullet = new Label("  ◦ " + text);
        bullet.setWrapText(true);
        bullet.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#c0c0d0" : "#444444") + "; -fx-font-size: 13px;");
        box.getChildren().add(bullet);
        return box;
    }
    
    private VBox createTipBullet(String text) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(4, 0, 4, 20));
        Label bullet = new Label("✓ " + text);
        bullet.setWrapText(true);
        bullet.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#90ee90" : "#2d6a2d") + "; -fx-font-size: 14px;");
        box.getChildren().add(bullet);
        return box;
    }
    
    private VBox createStatusBullet(String status, String description) {
        VBox box = new VBox(3);
        box.setPadding(new Insets(6, 0, 6, 20));
        Label statusLabel = new Label(status);
        statusLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        statusLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#b0e0ff" : "#0d5fa8") + ";");
        
        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#c0c0d0" : "#555555") + "; -fx-font-size: 13px;");
        
        box.getChildren().addAll(statusLabel, descLabel);
        return box;
    }
    
    private HBox createFAQItem(String question, String answer) {
        HBox faqBox = new HBox(12);
        faqBox.setPadding(new Insets(8, 0, 8, 0));
        
        VBox qnaBox = new VBox(4);
        Label qLabel = new Label(question);
        qLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        qLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#b0d0ff" : "#1e3c72") + ";");
        qLabel.setWrapText(true);
        
        Label aLabel = new Label(answer);
        aLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#c0c0d0" : "#555555") + "; -fx-font-size: 13px;");
        aLabel.setWrapText(true);
        
        qnaBox.getChildren().addAll(qLabel, aLabel);
        faqBox.getChildren().add(qnaBox);
        HBox.setHgrow(qnaBox, Priority.ALWAYS);
        
        return faqBox;
    }
}

