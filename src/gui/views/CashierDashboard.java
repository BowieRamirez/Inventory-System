package gui.views;

import gui.controllers.CashierDashboardController;
import gui.utils.ThemeManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
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

/**
 * CashierDashboard - Main dashboard for cashier users
 * 
 * Features:
 * - Process payments
 * - View approved reservations
 * - Generate receipts
 */
public class CashierDashboard {
    
    private BorderPane view;
    private VBox sidebar;
    private StackPane contentArea;
    private Label titleLabel;
    private CashierDashboardController controller;
    
    private Button paymentsBtn;
    private Button receiptsBtn;
    private Button helpBtn;
    private Button logoutBtn;
    private Label logoLabel;
    private Label subtitleLabel;
    
    // Smooth toggle fields
    private StackPane toggleSwitch;
    private StackPane toggleCircle;
    private Label toggleIcon;
    
    public CashierDashboard() {
        controller = new CashierDashboardController();
        initializeView();
        // Re-apply theme when ThemeManager changes (fixes cases where theme was toggled earlier)
        javafx.application.Platform.runLater(() -> ThemeManager.addThemeChangeListener(() -> javafx.application.Platform.runLater(this::updateSidebarTheme)));
    }
    
    private void initializeView() {
        view = new BorderPane();
        view.setMaxWidth(Double.MAX_VALUE);
        view.setMaxHeight(Double.MAX_VALUE);
        String bgColor = ThemeManager.isDarkMode() ? "-color-bg-default" : "#F8F9FA";
        view.setStyle("-fx-background-color: " + bgColor + ";");
        
        createSidebar();
        view.setLeft(sidebar);
        view.setTop(createTopBar());
        
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        contentArea.setMaxWidth(Double.MAX_VALUE);
        contentArea.setMaxHeight(Double.MAX_VALUE);
        String contentBg = ThemeManager.isDarkMode() ? "-color-bg-default" : "#F8F9FA";
        contentArea.setStyle("-fx-background-color: " + contentBg + ";");
        view.setCenter(contentArea);
        
        showPayments();
    }
    
    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        String bgGradient = ThemeManager.isDarkMode()
            ? "linear-gradient(to right, #1a2a6c 0%, #0d1b4d 50%, #1a2a6c 100%)"
            : "linear-gradient(to right, #1e3c72 0%, #2a5298 50%, #1e3c72 100%)";
        topBar.setStyle(
            "-fx-background-color: " + bgGradient + ";"
        );
        
        titleLabel = new Label("Process Payments");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        String titleColor = ThemeManager.isDarkMode() ? "-color-fg-default" : "white";
        titleLabel.setStyle("-fx-text-fill: " + titleColor + ";");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Theme toggle switch (smooth animation)
        toggleSwitch = new StackPane();
        toggleSwitch.setPrefWidth(70);
        toggleSwitch.setPrefHeight(32);
        toggleSwitch.setMaxWidth(70);
        toggleSwitch.setMaxHeight(32);
        
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
            "-fx-background-radius: 17px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 3);"
        );
        
        toggleIcon = new Label(ThemeManager.isDarkMode() ? "🌙" : "☀");
        toggleIcon.setFont(Font.font("System", FontWeight.BOLD, 12));
        toggleIcon.setStyle("-fx-text-fill: #000000;");
        toggleCircle.getChildren().add(toggleIcon);
        
        StackPane.setAlignment(toggleCircle, ThemeManager.isDarkMode() ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        StackPane.setMargin(toggleCircle, new Insets(0, 2, 0, 2));
        
        toggleSwitch.getChildren().addAll(toggleBg, toggleCircle);
        toggleSwitch.setOnMouseClicked(e -> toggleTheme());
        toggleSwitch.setStyle("-fx-cursor: hand;");
        
        Label cashierLabel = new Label("👤 Cashier");
        String labelColor = ThemeManager.isDarkMode() ? "-color-fg-muted" : "rgba(255,255,255,0.9)";
        cashierLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-size: 14px;");
        
        topBar.getChildren().addAll(titleLabel, spacer, toggleSwitch, cashierLabel);
        return topBar;
    }
    
    private void createSidebar() {
        sidebar = new VBox(10);
        sidebar.setPrefWidth(250);
        sidebar.setPadding(new Insets(20));
        
        // Gradient blue sidebar in light mode, gradient dark blue in dark mode
        String bgGradient = ThemeManager.isDarkMode()
            ? "linear-gradient(to bottom, #1a2a6c 0%, #0d1b4d 50%, #1a2a6c 100%)"
            : "linear-gradient(to bottom, #1e3c72 0%, #2a5298 50%, #1e3c72 100%)";
        sidebar.setStyle(
            "-fx-background-color: " + bgGradient + ";"
        );
        
        // Logo image
        ImageView logoImage = new ImageView();
        try {
            javafx.scene.image.Image img = new javafx.scene.image.Image(
                new java.io.FileInputStream("src/database/data/images/NewLogo.png")
            );
            logoImage.setImage(img);
            logoImage.setFitWidth(50);
            logoImage.setFitHeight(50);
            logoImage.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("Failed to load logo: " + e.getMessage());
        }
        
        logoLabel = new Label("STI ProWear Novaliches");
        logoLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        String logoColor = ThemeManager.isDarkMode() ? "-color-accent-fg" : "white";
        logoLabel.setStyle("-fx-text-fill: " + logoColor + ";");
        
        subtitleLabel = new Label("Cashier Panel");
        String subtitleColor = ThemeManager.isDarkMode() ? "-color-fg-muted" : "rgba(255,255,255,0.8)";
        subtitleLabel.setStyle("-fx-text-fill: " + subtitleColor + "; -fx-font-size: 12px;");
        
        VBox header = new VBox(10, logoImage, logoLabel, subtitleLabel);
        header.setAlignment(Pos.TOP_CENTER);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        paymentsBtn = createNavButton("💳 Process Payments", true);
        receiptsBtn = createNavButton("🧾 Receipts", false);
        helpBtn = createNavButton("❓ Help", false);
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        logoutBtn = createNavButton("🚪 Logout", false);
        String logoutColor = ThemeManager.isDarkMode() ? "white" : "rgba(255,255,255,0.9)";
        // Use explicit danger color for background to avoid CSS variable resolution issues
        String logoutBg = ThemeManager.isDarkMode() ? "#CF222E" : "rgba(255,255,255,0.15)";
        logoutBtn.setStyle(
            "-fx-background-color: " + logoutBg + ";" +
            "-fx-text-fill: " + logoutColor + ";" +
            "-fx-font-size: 14px;" +
            "-fx-alignment: center;" +
            "-fx-padding: 12px;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;" +
            (ThemeManager.isDarkMode() ? "-fx-font-weight: bold;" : "")
        );
        
        paymentsBtn.setOnAction(e -> {
            setActiveButton(paymentsBtn);
            showPayments();
        });

        receiptsBtn.setOnAction(e -> {
            setActiveButton(receiptsBtn);
            showReceipts();
        });

        helpBtn.setOnAction(e -> {
            setActiveButton(helpBtn);
            showHelp();
        });
        
        logoutBtn.setOnAction(e -> controller.handleLogout());
        
        sidebar.getChildren().addAll(
            header,
            new Separator(),
            paymentsBtn,
            receiptsBtn,
            helpBtn,
            spacer,
            new Separator(),
            logoutBtn
        );
    }
    
    private Button createNavButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPrefHeight(40);
        
        if (active) {
            String activeBg = ThemeManager.isDarkMode() ? "-color-accent-subtle" : "rgba(255,255,255,0.2)";
            String activeText = ThemeManager.isDarkMode() ? "-color-accent-fg" : "white";
            btn.setStyle(
                "-fx-background-color: " + activeBg + ";" +
                "-fx-text-fill: " + activeText + ";" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6px;" +
                "-fx-cursor: hand;"
            );
        } else {
            String inactiveText = ThemeManager.isDarkMode() ? "-color-fg-default" : "rgba(255,255,255,0.9)";
            btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + inactiveText + ";" +
                "-fx-font-size: 14px;" +
                "-fx-cursor: hand;"
            );
        }
        
        return btn;
    }
    
    private void setActiveButton(Button activeBtn) {
        Button[] buttons = {paymentsBtn, receiptsBtn, helpBtn};
        
        String activeBg = ThemeManager.isDarkMode() ? "-color-accent-subtle" : "rgba(255,255,255,0.2)";
        String activeText = ThemeManager.isDarkMode() ? "-color-accent-fg" : "white";
        String inactiveText = ThemeManager.isDarkMode() ? "-color-fg-default" : "rgba(255,255,255,0.9)";
        
        for (Button btn : buttons) {
            if (btn == activeBtn) {
                btn.setStyle(
                    "-fx-background-color: " + activeBg + ";" +
                    "-fx-text-fill: " + activeText + ";" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-cursor: hand;"
                );
            } else {
                btn.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: " + inactiveText + ";" +
                    "-fx-font-size: 14px;" +
                    "-fx-cursor: hand;"
                );
            }
        }
    }
    
    private void showPayments() {
        titleLabel.setText("Process Payments");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createPaymentsView());
    }
    
    private void showReceipts() {
        titleLabel.setText("Receipts");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createReceiptsView());
    }
    
    

    private void showHelp() {
        titleLabel.setText("Help & Documentation");
        contentArea.getChildren().clear();
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-control-inner-background: transparent; -fx-padding: 10;");
        
        VBox mainBox = new VBox(20);
        mainBox.setPadding(new Insets(20));
        mainBox.setStyle("-fx-border-color: transparent;");
        mainBox.setId("help-content");
        
        // ====== WELCOME SECTION ======
        VBox welcomeSection = createHelpCard(
            "👋 Welcome to Cashier Station",
            "STI ProWear System - Payment Processing",
            "As a cashier, you handle customer payments, generate receipts, and process transactions. This guide covers all payment procedures and best practices."
        );
        
        // ====== NAVIGATION BASICS ======
        VBox navSection = createHelpCard(
            "🧭 Navigation Basics",
            "",
            ""
        );
        navSection.getChildren().add(createBulletPoint("Dashboard: View today's transactions and pending payments"));
        navSection.getChildren().add(createBulletPoint("Payments: Process customer payments securely"));
        navSection.getChildren().add(createBulletPoint("Receipts: Generate and manage payment receipts"));
        navSection.getChildren().add(createBulletPoint("Help: Access this documentation anytime"));
        
        // ====== PAYMENT PROCESSING ======
        VBox paymentSection = createHelpCard(
            "💳 Payment Processing",
            "Handling Customer Transactions",
            ""
        );
        paymentSection.getChildren().add(createBulletPoint("Receive customer with approved reservation ID"));
        paymentSection.getChildren().add(createBulletPoint("Verify customer identity with valid student ID"));
        paymentSection.getChildren().add(createBulletPoint("Confirm reservation details and total amount"));
        paymentSection.getChildren().add(createBulletPoint("Accept payment (cash currently supported)"));
        paymentSection.getChildren().add(createBulletPoint("Calculate and provide change accurately"));
        paymentSection.getChildren().add(createBulletPoint("Generate and provide receipt to customer"));
        paymentSection.getChildren().add(createBulletPoint("Record transaction in system with receipt number"));
        
        // ====== CASH HANDLING ======
        VBox cashSection = createHelpCard(
            "💰 Cash Handling & Accuracy",
            "Critical for Security & Trust",
            ""
        );
        cashSection.getChildren().add(createBulletPoint("Count cash drawer at START of shift"));
        cashSection.getChildren().add(createBulletPoint("Record opening balance in cash register"));
        cashSection.getChildren().add(createBulletPoint("Use calculator for change calculations"));
        cashSection.getChildren().add(createBulletPoint("Double-check all transactions for accuracy"));
        cashSection.getChildren().add(createBulletPoint("Count cash drawer at END of shift"));
        cashSection.getChildren().add(createBulletPoint("Report any discrepancies to supervisor immediately"));
        cashSection.getChildren().add(createBulletPoint("Never leave cash drawer unattended"));
        cashSection.getChildren().add(createBulletPoint("Secure all cash in designated safe/lockbox"));
        
        // ====== RECEIPT MANAGEMENT ======
        VBox receiptSection = createHelpCard(
            "🧾 Receipt Management",
            "Documentation & Records",
            ""
        );
        receiptSection.getChildren().add(createBulletPoint("Generate receipt for every payment transaction"));
        receiptSection.getChildren().add(createBulletPoint("Include: Customer name, items, amount paid, change, date/time"));
        receiptSection.getChildren().add(createBulletPoint("Keep receipt numbers sequential"));
        receiptSection.getChildren().add(createBulletPoint("Provide receipt to customer - always keep copy"));
        receiptSection.getChildren().add(createBulletPoint("Store receipts in designated file/folder"));
        receiptSection.getChildren().add(createBulletPoint("Never discard or destroy original receipts"));
        receiptSection.getChildren().add(createBulletPoint("Report lost receipts to supervisor"));
        
        // ====== CUSTOMER SERVICE ======
        VBox csSection = createHelpCard(
            "🤝 Customer Service Excellence",
            "Professional & Courteous Service",
            ""
        );
        csSection.getChildren().add(createBulletPoint("Greet every customer with a smile and warm greeting"));
        csSection.getChildren().add(createBulletPoint("Speak clearly and confirm payment details"));
        csSection.getChildren().add(createBulletPoint("Handle complaints professionally and empathetically"));
        csSection.getChildren().add(createBulletPoint("Never rush customers - take time for accuracy"));
        csSection.getChildren().add(createBulletPoint("Thank customers and encourage them to return"));
        csSection.getChildren().add(createBulletPoint("Maintain clean and organized work area"));
        csSection.getChildren().add(createBulletPoint("Escalate complex issues to supervisor"));
        
        // ====== PAYMENT METHODS ======
        VBox methodSection = createHelpCard(
            "📋 Supported Payment Methods",
            "Current Options",
            ""
        );
        methodSection.getChildren().add(createBulletPoint("Cash: Primary payment method"));
        methodSection.getChildren().add(createBulletPoint("Verify cash denominations: ₱1, ₱5, ₱10, ₱20, ₱50, ₱100, ₱500, ₱1000"));
        methodSection.getChildren().add(createBulletPoint("Check for counterfeit or damaged bills"));
        methodSection.getChildren().add(createBulletPoint("Future updates may include card/digital payments"));
        
        // ====== REJECTED PAYMENTS ======
        VBox rejectSection = createHelpCard(
            "❌ Handling Payment Issues",
            "When Payment Can't Be Processed",
            ""
        );
        rejectSection.getChildren().add(createBulletPoint("Insufficient funds: Politely inform customer and inform staff"));
        rejectSection.getChildren().add(createBulletPoint("Reservation expired: Check with staff before accepting"));
        rejectSection.getChildren().add(createBulletPoint("Customer disputes amount: Verify reservation and confirm with customer"));
        rejectSection.getChildren().add(createBulletPoint("Missing documentation: Request valid student ID"));
        rejectSection.getChildren().add(createBulletPoint("System error: Call supervisor - never guess or force transaction"));
        
        // ====== TIPS & BEST PRACTICES ======
        VBox tipsSection = createHelpCard(
            "💡 Tips & Best Practices",
            "Work Efficiently & Safely",
            ""
        );
        tipsSection.getChildren().add(createTipBullet("Always use calculator - mental math causes errors"));
        tipsSection.getChildren().add(createTipBullet("Count change back to customer - never rush"));
        tipsSection.getChildren().add(createTipBullet("Verify receipts print correctly before handing to customer"));
        tipsSection.getChildren().add(createTipBullet("Keep payment window visible - transparency builds trust"));
        tipsSection.getChildren().add(createTipBullet("Regular cash counts prevent discrepancies"));
        tipsSection.getChildren().add(createTipBullet("Good appearance and attitude improve customer experience"));
        
        // ====== TROUBLESHOOTING ======
        VBox troubleshootSection = createHelpCard(
            "🔧 Troubleshooting",
            "Common Issues & Solutions",
            ""
        );
        troubleshootSection.getChildren().add(createFAQItem("Q: Customer provided exact amount?", "A: Count carefully and process payment. Generate receipt confirming zero change."));
        troubleshootSection.getChildren().add(createFAQItem("Q: Receipt printer not working?", "A: Contact IT or supervisor. Use manual receipt form as backup."));
        troubleshootSection.getChildren().add(createFAQItem("Q: Cash drawer won't open?", "A: Call supervisor - do not force. May need key override."));
        troubleshootSection.getChildren().add(createFAQItem("Q: Customer claims wrong change given?", "A: Review receipt and transaction. Report to supervisor if discrepancy found."));
        troubleshootSection.getChildren().add(createFAQItem("Q: Reservation not found in system?", "A: Verify reservation ID and customer name. Consult with staff."));
        
        // ====== SAFETY & SECURITY ======
        VBox securitySection = createHelpCard(
            "🔒 Safety & Security",
            "Protecting Assets & Data",
            ""
        );
        securitySection.getChildren().add(createBulletPoint("Never share drawer keys or access codes"));
        securitySection.getChildren().add(createBulletPoint("Report suspicious transactions immediately"));
        securitySection.getChildren().add(createBulletPoint("Keep password confidential - don't write it down"));
        securitySection.getChildren().add(createBulletPoint("Log out when stepping away from system"));
        securitySection.getChildren().add(createBulletPoint("Report any system breaches or unauthorized access"));
        securitySection.getChildren().add(createBulletPoint("Handle customer data confidentially"));
        
        // ====== SHIFT PROCEDURES ======
        VBox shiftSection = createHelpCard(
            "⏰ Shift Opening & Closing",
            "Daily Procedures",
            ""
        );
        shiftSection.getChildren().add(createBulletPoint("OPENING: Count drawer, record starting amount, log in"));
        shiftSection.getChildren().add(createBulletPoint("DURING: Process payments, maintain records, help customers"));
        shiftSection.getChildren().add(createBulletPoint("CLOSING: Count drawer, calculate balance, report discrepancies"));
        shiftSection.getChildren().add(createBulletPoint("RECONCILIATION: Match receipts with cash and system records"));
        shiftSection.getChildren().add(createBulletPoint("Document any issues on daily report"));
        
        mainBox.getChildren().addAll(
            welcomeSection, navSection, paymentSection, cashSection, receiptSection, 
            csSection, methodSection, rejectSection, tipsSection, troubleshootSection, 
            securitySection, shiftSection
        );
        
        scrollPane.setContent(mainBox);
        contentArea.getChildren().add(scrollPane);
    }
    
    private void updateSidebarTheme() {
        boolean isDark = ThemeManager.isDarkMode();
        
        // Update main background
        String bgColor = isDark ? "-color-bg-default" : "#F8F9FA";
        view.setStyle("-fx-background-color: " + bgColor + ";");
        
        // Update content area background
        String contentBg = isDark ? "-color-bg-default" : "#F8F9FA";
        contentArea.setStyle("-fx-background-color: " + contentBg + "; -fx-padding: 20;");
        
        // Update top bar
        HBox topBar = (HBox) view.getTop();
        String bgGradient = isDark
            ? "linear-gradient(to right, #1a2a6c 0%, #0d1b4d 50%, #1a2a6c 100%)"
            : "linear-gradient(to right, #1e3c72 0%, #2a5298 50%, #1e3c72 100%)";
        topBar.setStyle(
            "-fx-background-color: " + bgGradient + ";"
        );
        
        // Update title color
        String titleColor = isDark ? "-color-fg-default" : "white";
        titleLabel.setStyle("-fx-text-fill: " + titleColor + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Update top bar buttons and labels
        for (javafx.scene.Node node : topBar.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                String themeBtnColor = isDark ? "-color-fg-default" : "white";
                btn.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: " + themeBtnColor + ";" +
                    "-fx-font-size: 18px;" +
                    "-fx-cursor: hand;"
                );
            } else if (node instanceof Label && !node.equals(titleLabel)) {
                Label lbl = (Label) node;
                String labelColor = isDark ? "-color-fg-muted" : "rgba(255,255,255,0.9)";
                lbl.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-size: 14px;");
            }
        }
        
        // Update sidebar background
        String sidebarGradient = isDark
            ? "linear-gradient(to bottom, #1a2a6c 0%, #0d1b4d 50%, #1a2a6c 100%)"
            : "linear-gradient(to bottom, #1e3c72 0%, #2a5298 50%, #1e3c72 100%)";
        sidebar.setStyle("-fx-background-color: " + sidebarGradient + "; -fx-padding: 20;");
        
        // Update logo and subtitle
        String logoColor = isDark ? "-color-accent-fg" : "white";
        String subtitleColor = isDark ? "-color-fg-muted" : "rgba(255, 255, 255, 0.8)";
        logoLabel.setStyle("-fx-text-fill: " + logoColor + "; -fx-font-size: 20px; -fx-font-weight: bold;");
        subtitleLabel.setStyle("-fx-text-fill: " + subtitleColor + "; -fx-font-size: 12px;");
        
        // Update navigation buttons
        Button[] buttons = {paymentsBtn, receiptsBtn};
        for (Button btn : buttons) {
            String currentStyle = btn.getStyle();
            
            // Check if button is currently active (bold text)
            boolean isActive = currentStyle.contains("-fx-font-weight: bold");
            
            if (isDark) {
                // Dark mode styling - use darker subtle color for active state
                if (isActive) {
                    btn.setStyle("-fx-background-color: -color-accent-subtle; -fx-text-fill: -color-accent-fg; " +
                               "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 6; " +
                               "-fx-cursor: hand; -fx-alignment: center-left; -fx-font-size: 14px;");
                } else {
                    btn.setStyle("-fx-background-color: transparent; -fx-text-fill: -color-fg-default; " +
                               "-fx-padding: 12; -fx-background-radius: 6; -fx-cursor: hand; " +
                               "-fx-alignment: center-left; -fx-font-size: 14px;");
                }
            } else {
                // Light mode styling (blue sidebar)
                if (isActive) {
                    btn.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2); -fx-text-fill: white; " +
                               "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 6; " +
                               "-fx-cursor: hand; -fx-alignment: center-left; -fx-font-size: 14px;");
                } else {
                    btn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255, 255, 255, 0.9); " +
                               "-fx-padding: 12; -fx-background-radius: 6; -fx-cursor: hand; " +
                               "-fx-alignment: center-left; -fx-font-size: 14px;");
                }
            }
        }
        
        // Update logout button
        if (isDark) {
            // Use explicit danger color for dark mode
            logoutBtn.setStyle("-fx-background-color: #CF222E; -fx-text-fill: white; " +
                             "-fx-padding: 12; -fx-background-radius: 6; -fx-cursor: hand; " +
                             "-fx-alignment: center; -fx-font-size: 14px; -fx-font-weight: bold;");
        } else {
            logoutBtn.setStyle("-fx-background-color: rgba(255, 255, 255, 0.15); -fx-text-fill: rgba(255, 255, 255, 0.9); " +
                             "-fx-padding: 12; -fx-background-radius: 6; -fx-cursor: hand; " +
                             "-fx-alignment: center; -fx-font-size: 14px;");
        }
        
        // Refresh Help content if currently displayed
        if (contentArea.lookup("#help-content") != null) {
            showHelp();
        }
    }
    
    /**
     * Toggle theme with smooth animation
     */
    private void toggleTheme() {
        ThemeManager.toggleLightDark();
        
        // Update toggle switch appearance with smooth animation
        toggleIcon.setText(ThemeManager.isDarkMode() ? "🌙" : "☀");
        
        // Animate circle position smoothly
        double targetX = ThemeManager.isDarkMode() ? 44 : 2; // Right: 44, Left: 2
        Timeline slideAnimation = new Timeline(
            new KeyFrame(Duration.millis(400), 
                new KeyValue(toggleCircle.translateXProperty(), targetX - toggleCircle.getLayoutX())
            )
        );
        slideAnimation.setCycleCount(1);
        slideAnimation.play();
        
        // Update circle color
        String circleColor = ThemeManager.isDarkMode() 
            ? "linear-gradient(from 0% 0% to 100% 100%, #6bb6ff 0%, #2a7fd9 50%, #1a5fa0 100%)"
            : "linear-gradient(from 0% 0% to 100% 100%, #ffd700 0%, #ffed4e 50%, #f5b542 100%)";
        toggleCircle.setStyle(
            "-fx-background-color: " + circleColor + ";" +
            "-fx-background-radius: 13px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 3);"
        );
        
        // Update background color
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
        
        // Update the full UI theme
        updateSidebarTheme();
    }
    
    public BorderPane getView() {
        return view;
    }
    
    // ===== HELP SECTION HELPER METHODS =====
    
    private VBox createHelpCard(String title, String subtitle, String description) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16));
        card.setStyle(
            "-fx-background-color: " + (ThemeManager.isDarkMode() ? "#2a2a3e" : "#f5f5f5") + ";" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: " + (ThemeManager.isDarkMode() ? "#404054" : "#e0e0e0") + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 1);"
        );
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#e0e0ff" : "#1e3c72") + ";");
        card.getChildren().add(titleLabel);
        
        if (subtitle != null && !subtitle.isEmpty()) {
            Label subtitleLabel = new Label(subtitle);
            subtitleLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
            subtitleLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#a0a0c0" : "#4a5f8f") + ";");
            card.getChildren().add(subtitleLabel);
        }
        
        if (description != null && !description.isEmpty()) {
            Label descLabel = new Label(description);
            descLabel.setWrapText(true);
            descLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#c0c0d0" : "#555555") + ";");
            card.getChildren().add(descLabel);
        }
        
        return card;
    }
    
    private VBox createBulletPoint(String text) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(4, 0, 4, 20));
        Label bullet = new Label("• " + text);
        bullet.setWrapText(true);
        bullet.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#d0d0e0" : "#333333") + "; -fx-font-size: 11px;");
        box.getChildren().add(bullet);
        return box;
    }
    
    private VBox createSubBullet(String text) {
        VBox box = new VBox(2);
        box.setPadding(new Insets(2, 0, 2, 40));
        Label bullet = new Label("  ◦ " + text);
        bullet.setWrapText(true);
        bullet.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#c0c0d0" : "#444444") + "; -fx-font-size: 10px;");
        box.getChildren().add(bullet);
        return box;
    }
    
    private VBox createTipBullet(String text) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(4, 0, 4, 20));
        Label bullet = new Label("✓ " + text);
        bullet.setWrapText(true);
        bullet.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#90ee90" : "#2d6a2d") + "; -fx-font-size: 11px;");
        box.getChildren().add(bullet);
        return box;
    }
    
    private HBox createFAQItem(String question, String answer) {
        HBox faqBox = new HBox(12);
        faqBox.setPadding(new Insets(8, 0, 8, 0));
        
        VBox qnaBox = new VBox(4);
        Label qLabel = new Label(question);
        qLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 11));
        qLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#b0d0ff" : "#1e3c72") + ";");
        qLabel.setWrapText(true);
        
        Label aLabel = new Label(answer);
        aLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#c0c0d0" : "#555555") + "; -fx-font-size: 10px;");
        aLabel.setWrapText(true);
        
        qnaBox.getChildren().addAll(qLabel, aLabel);
        faqBox.getChildren().add(qnaBox);
        HBox.setHgrow(qnaBox, Priority.ALWAYS);
        
        return faqBox;
    }
}

