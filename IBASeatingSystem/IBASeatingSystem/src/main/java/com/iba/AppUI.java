package com.iba;

import com.iba.models.Room;
import com.iba.models.Student;
import com.iba.services.CsvService;
import com.iba.services.PdfService;
import com.iba.services.SeatingService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.Desktop;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class AppUI extends Application {

    private final ObservableList<Student> studentList = FXCollections.observableArrayList();
    private final ObservableList<Room> roomList = FXCollections.observableArrayList();

    private final CsvService csvService = new CsvService();
    private final SeatingService seatingService = new SeatingService();
    private final PdfService pdfService = new PdfService();

    private Stage mainStage;
    private StackPane contentArea;
    private ComboBox<Room> roomSelector;
    private GridPane visualGrid;
    private Label statusBanner;
    private final List<Button> navButtons = new ArrayList<>();

    // Seat Swap State (For 2-Click Swapping)
    private Room swapSourceRoom = null;
    private int swapSourceRow = -1;
    private int swapSourceCol = -1;

    // Auto-Save File Name
    private static final String SAVE_FILE = "iba_seating_autosave.dat";

    // Window Dragging Variables
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage stage) {
        this.mainStage = stage;
        stage.initStyle(StageStyle.UNDECORATED);

        // Load saved data or default rooms
        if (!loadStateFromDisk()) {
            roomList.add(new Room("Room-101 (Main Hall)", 4, 5));
            roomList.add(new Room("Room-102 (Lab A)", 3, 4));
        }

        // Start with the sleek Admin Login Screen
        showLoginScreen();
        stage.setTitle("IBA Exam Seating & Student Management System — Pro Edition");
        stage.show();
    }

    // =========================================================================
    // NEW IMPROVEMENT 1: SLEEK IBA ADMIN LOGIN SCREEN
    // =========================================================================
    private void showLoginScreen() {
        BorderPane loginRoot = new BorderPane();
        loginRoot.setStyle("-fx-background-color: #0F172A; -fx-border-color: #334155; -fx-border-width: 1.5;");

        // Top bar with Minimize & Close buttons
        HBox topBar = new HBox(8);
        topBar.setPadding(new Insets(12, 18, 12, 25));
        topBar.setAlignment(Pos.CENTER_RIGHT);
        makeDraggable(topBar);

        Button btnMin = windowControlButton("—", "#1E293B", "#334155", "#E2E8F0", "#FFFFFF");
        btnMin.setOnAction(e -> mainStage.setIconified(true));
        Button btnClose = windowControlButton("✕", "#7F1D1D", "#DC2626", "#FECACA", "#FFFFFF");
        btnClose.setOnAction(e -> { Platform.exit(); System.exit(0); });
        topBar.getChildren().addAll(btnMin, btnClose);
        loginRoot.setTop(topBar);

        // Center Login Card
        VBox card = new VBox(16);
        card.setMaxWidth(400);
        card.setMaxHeight(430);
        card.setPadding(new Insets(35));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 14;");

        Label badge = new Label("IBA EXAMINATION DEPARTMENT");
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        badge.setTextFill(Color.web("#800020"));
        badge.setStyle("-fx-background-color: #FFE4E6; -fx-padding: 5 10 5 10; -fx-background-radius: 5;");

        Label title = new Label("Sign in to IBA Seatly");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#0F172A"));

        Label sub = new Label("Enter your administrator credentials to manage exams.");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web("#64748B"));

        TextField tfUser = new TextField("admin@iba.edu.pk");
        tfUser.setPromptText("Admin Username");
        tfUser.setStyle("-fx-padding: 10; -fx-background-radius: 6; -fx-border-color: #CBD5E1; -fx-border-radius: 6;");

        PasswordField pfPass = new PasswordField();
        pfPass.setText("iba123");
        pfPass.setPromptText("Password");
        pfPass.setStyle("-fx-padding: 10; -fx-background-radius: 6; -fx-border-color: #CBD5E1; -fx-border-radius: 6;");

        Label errLbl = new Label();
        errLbl.setTextFill(Color.web("#DC2626"));
        errLbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));

        Button btnLogin = primaryButton("🔐  Sign In to Dashboard", "#800020");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setPadding(new Insets(12));

        Runnable doLogin = () -> {
            if (tfUser.getText().trim().isEmpty() || pfPass.getText().trim().isEmpty()) {
                errLbl.setText("Please enter both username and password.");
            } else {
                launchMainDashboard();
            }
        };
        btnLogin.setOnAction(e -> doLogin.run());
        pfPass.setOnAction(e -> doLogin.run());

        card.getChildren().addAll(badge, title, sub, new Label("Username:"), tfUser, new Label("Password:"), pfPass, errLbl, btnLogin);
        loginRoot.setCenter(card);

        Scene loginScene = new Scene(loginRoot, 1220, 760);
        mainStage.setScene(loginScene);
    }

    private void launchMainDashboard() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F1F5F9; -fx-border-color: #334155; -fx-border-width: 1.5;");

        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        BorderPane rightLayout = new BorderPane();
        rightLayout.setTop(createTopHeaderBar());

        contentArea = new StackPane();
        contentArea.setPadding(new Insets(22));
        rightLayout.setCenter(contentArea);

        rightLayout.setBottom(createStatusBar());
        root.setCenter(rightLayout);

        showDashboardView();
        setActiveNav(0);

        Scene scene = new Scene(root, 1220, 760);
        mainStage.setScene(scene);
    }

    // =========================================================================
    // TOP HEADER BAR (Global Search + Minimize, Maximize & Exit Buttons)
    // =========================================================================
    private HBox createTopHeaderBar() {
        HBox topBar = new HBox(12);
        topBar.setPadding(new Insets(10, 15, 10, 25));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        makeDraggable(topBar);
        topBar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) mainStage.setMaximized(!mainStage.isMaximized());
        });

        Label badge = new Label("IBA EXAM PORTAL • AUTO-SAVE ACTIVE");
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        badge.setTextFill(Color.web("#800020"));
        badge.setStyle("-fx-background-color: #FFE4E6; -fx-padding: 6 12 6 12; -fx-background-radius: 6;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField quickSearch = new TextField();
        quickSearch.setPromptText("Quick Find Seat by Student ID (e.g. IBA-101) & press Enter...");
        quickSearch.setPrefWidth(320);
        quickSearch.setStyle("-fx-background-radius: 6; -fx-padding: 7 12 7 12; -fx-border-color: #CBD5E1; -fx-border-radius: 6;");

        Button btnQuickFind = primaryButton("Quick Find", "#0F172A");
        Runnable doQuickSearch = () -> {
            String q = quickSearch.getText().trim();
            if (q.isEmpty()) return;
            String res = seatingService.searchStudentSeat(q, roomList);
            showAlert("Quick Seat Lookup", res);
        };
        quickSearch.setOnAction(e -> doQuickSearch.run());
        btnQuickFind.setOnAction(e -> doQuickSearch.run());

        // 3 Window Control Buttons
        HBox windowControls = new HBox(6);
        windowControls.setAlignment(Pos.CENTER_RIGHT);
        windowControls.setPadding(new Insets(0, 0, 0, 15));

        Button btnMin = windowControlButton("—", "#F1F5F9", "#E2E8F0", "#0F172A", "#0F172A");
        btnMin.setOnAction(e -> mainStage.setIconified(true));

        Button btnMax = windowControlButton("☐", "#F1F5F9", "#E2E8F0", "#0F172A", "#0F172A");
        btnMax.setOnAction(e -> mainStage.setMaximized(!mainStage.isMaximized()));

        Button btnClose = windowControlButton("✕", "#FEE2E2", "#DC2626", "#DC2626", "#FFFFFF");
        btnClose.setOnAction(e -> {
            saveStateToDisk();
            Platform.exit();
            System.exit(0);
        });

        windowControls.getChildren().addAll(btnMin, btnMax, btnClose);
        topBar.getChildren().addAll(badge, spacer, new Label("🔍"), quickSearch, btnQuickFind, windowControls);
        return topBar;
    }

    private void makeDraggable(Region region) {
        region.setOnMousePressed(event -> {
            if (!mainStage.isMaximized()) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
        region.setOnMouseDragged(event -> {
            if (!mainStage.isMaximized()) {
                mainStage.setX(event.getScreenX() - xOffset);
                mainStage.setY(event.getScreenY() - yOffset);
            }
        });
    }

    private Button windowControlButton(String symbol, String bgNormal, String bgHover, String textNormal, String textHover) {
        Button btn = new Button(symbol);
        btn.setPrefSize(36, 30);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        String normalStyle = "-fx-background-color: " + bgNormal + "; -fx-text-fill: " + textNormal + "; -fx-background-radius: 6; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: " + bgHover + "; -fx-text-fill: " + textHover + "; -fx-background-radius: 6; -fx-cursor: hand;";
        btn.setStyle(normalStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
        return btn;
    }

    private HBox createStatusBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(8, 25, 8, 25));
        bar.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-width: 1 0 0 0;");
        statusBanner = new Label("Ready — Auto-Save is active. All changes are automatically stored.");
        statusBanner.setFont(Font.font("Segoe UI", 12));
        statusBanner.setTextFill(Color.web("#475569"));
        bar.getChildren().add(statusBanner);
        return bar;
    }

    private void setStatus(String msg) {
        if (statusBanner != null) statusBanner.setText("✅ " + msg);
        saveStateToDisk(); // Auto-save on every action!
    }

    // LEFT SIDEBAR
    private VBox createSidebar() {
        VBox nav = new VBox(8);
        nav.setPrefWidth(255);
        nav.setPadding(new Insets(25, 15, 25, 15));
        nav.setStyle("-fx-background-color: #0F172A;");
        makeDraggable(nav);

        Label brand = new Label("IBA SEATLY");
        brand.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        brand.setTextFill(Color.WHITE);

        Label sub = new Label("Smart Exam Seating Suite");
        sub.setFont(Font.font("Segoe UI", 12));
        sub.setTextFill(Color.web("#94A3B8"));
        VBox.setMargin(sub, new Insets(0, 0, 18, 0));

        navButtons.clear();
        Button b0 = navButton("🏠  1. Main Dashboard", 0, this::showDashboardView);
        Button b1 = navButton("👥  2. Student Data (CSV)", 1, this::showStudentView);
        Button b2 = navButton("🏫  3. Exam Rooms", 2, this::showRoomView);
        Button b3 = navButton("🗺️  4 & 5. Visual Seating Map", 3, this::showSeatingMapView);
        Button b4 = navButton("🔍  6. Student Seat Search", 4, this::showSearchView);
        Button b5 = navButton("📄  7 & 8. PDF & Attendance", 5, this::showReportsView);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button btnReset = new Button("🔄  Reset All Data");
        btnReset.setMaxWidth(Double.MAX_VALUE);
        btnReset.setStyle("-fx-background-color: #334155; -fx-text-fill: #F8FAFC; -fx-background-radius: 8; -fx-padding: 9; -fx-cursor: hand;");
        btnReset.setOnAction(e -> {
            studentList.clear();
            for (Room r : roomList) r.clearSeats();
            showDashboardView();
            setActiveNav(0);
            setStatus("All students and seat assignments have been cleared.");
        });

        Button btnLogout = new Button("🔒  Lock / Logout");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setStyle("-fx-background-color: #1E293B; -fx-text-fill: #CBD5E1; -fx-background-radius: 8; -fx-padding: 9; -fx-cursor: hand;");
        btnLogout.setOnAction(e -> {
            saveStateToDisk();
            showLoginScreen();
        });

        nav.getChildren().addAll(brand, sub, b0, b1, b2, b3, b4, b5, spacer, btnReset, btnLogout);
        return nav;
    }

    private Button navButton(String text, int index, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(12, 15, 12, 15));
        btn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        btn.setOnAction(e -> {
            setActiveNav(index);
            action.run();
        });
        navButtons.add(btn);
        return btn;
    }

    private void setActiveNav(int activeIndex) {
        for (int i = 0; i < navButtons.size(); i++) {
            Button b = navButtons.get(i);
            if (i == activeIndex) {
                b.setStyle("-fx-background-color: #800020; -fx-text-fill: #FFFFFF; -fx-background-radius: 8; -fx-cursor: hand;");
            } else {
                b.setStyle("-fx-background-color: transparent; -fx-text-fill: #CBD5E1; -fx-background-radius: 8; -fx-cursor: hand;");
            }
        }
    }

    // =========================================================================
    // MODULE 1: DASHBOARD + BATCH ANALYTICS BREAKDOWN
    // =========================================================================
    private void showDashboardView() {
        VBox box = new VBox(18);
        Label title = headerLabel("Dashboard & Exam Analytics");

        int totalCap = roomList.stream().mapToInt(Room::getCapacity).sum();
        int totalSeated = roomList.stream().mapToInt(Room::getOccupiedCount).sum();
        int unseatedCount = Math.max(0, studentList.size() - totalSeated);

        HBox cards = new HBox(18);
        cards.getChildren().addAll(
                statCard("Total Students", String.valueOf(studentList.size()), "Loaded in system", "#1D4ED8"),
                statCard("Active Exam Rooms", String.valueOf(roomList.size()), "Configured rooms", "#047857"),
                statCard("Total Seat Capacity", String.valueOf(totalCap), "Available desks", "#800020"),
                statCard("Seated / Unseated", totalSeated + " / " + unseatedCount, "Assignment status", "#7C3AED")
        );

        // Easy 3-Step Guided Wizard
        VBox wizardCard = cardContainer();
        Label wTitle = new Label("🚀 Easy 3-Step Exam Setup (Click in Order)");
        wTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        HBox stepBoxes = new HBox(18);
        VBox s1 = stepBox("Step 1: Load Students", "Load 30 sample IBA students instantly or import your own CSV file.", "1. Load Demo Students", "#1D4ED8", () -> {
            loadSampleData();
            setStatus("Loaded 30 sample students across 3 batches (BCS-1, BBA-2, BAF-3).");
            showDashboardView();
        });

        VBox s2 = stepBox("Step 2: Generate Seating", "Automatically assign seats while separating same-batch neighbors.", "2. Auto-Seat & View Map", "#047857", () -> {
            if (studentList.isEmpty()) loadSampleData();
            int placed = seatingService.generateSeating(studentList, roomList);
            setStatus("Auto-seated " + placed + " students! Opening Visual Map...");
            setActiveNav(3);
            showSeatingMapView();
        });

        VBox s3 = stepBox("Step 3: Print Official PDFs", "Download & auto-open IBA Seating Plan and Attendance Sheets.", "3. Go to PDF Exports", "#800020", () -> {
            setActiveNav(5);
            showReportsView();
        });
        stepBoxes.getChildren().addAll(s1, s2, s3);

        // Batch Distribution Breakdown Card
        VBox batchCard = cardContainer();
        Label bTitle = new Label("📊 Batch / Group Distribution & Room Utilization");
        bTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));

        HBox batchRow = new HBox(25);
        Map<String, Integer> batchCounts = new LinkedHashMap<>();
        for (Student s : studentList) {
            batchCounts.put(s.getBatch(), batchCounts.getOrDefault(s.getBatch(), 0) + 1);
        }
        if (batchCounts.isEmpty()) {
            batchRow.getChildren().add(new Label("No students loaded yet. Click '1. Load Demo Students' above to view batch analytics."));
        } else {
            for (Map.Entry<String, Integer> entry : batchCounts.entrySet()) {
                double ratio = (double) entry.getValue() / studentList.size();
                VBox bBox = new VBox(5);
                Label l = new Label(entry.getKey() + ": " + entry.getValue() + " Students (" + Math.round(ratio * 100) + "%)");
                l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                ProgressBar pb = new ProgressBar(ratio);
                pb.setPrefWidth(180);
                pb.setStyle("-fx-accent: #1D4ED8;");
                bBox.getChildren().addAll(l, pb);
                batchRow.getChildren().add(bBox);
            }
        }

        Button btnMakeCsv = primaryButton("📁 Generate Sample CSV File on PC", "#334155");
        btnMakeCsv.setOnAction(e -> saveSampleCsvFile());

        batchCard.getChildren().addAll(bTitle, batchRow, new Separator(), btnMakeCsv);
        wizardCard.getChildren().addAll(wTitle, stepBoxes);
        box.getChildren().addAll(title, cards, wizardCard, batchCard);
        contentArea.getChildren().setAll(box);
    }

    private VBox stepBox(String stepTitle, String desc, String btnText, String color, Runnable action) {
        VBox v = new VBox(10);
        v.setPadding(new Insets(14));
        v.setPrefWidth(285);
        v.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8;");
        Label l1 = new Label(stepTitle);
        l1.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        Label l2 = new Label(desc);
        l2.setWrapText(true);
        l2.setFont(Font.font("Segoe UI", 12));
        l2.setTextFill(Color.web("#475569"));
        l2.setPrefHeight(36);
        Button b = primaryButton(btnText, color);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> action.run());
        v.getChildren().addAll(l1, l2, b);
        return v;
    }

    // =========================================================================
    // MODULE 2: STUDENT DATA MANAGEMENT
    // =========================================================================
    private void showStudentView() {
        VBox box = new VBox(14);
        Label title = headerLabel("Module 2: Student Data Management (CSV & Manual Entry)");

        HBox topBar = cardContainerHBox();
        Button btnImport = primaryButton("📂 Import CSV File", "#1D4ED8");
        Button btnSample = primaryButton("⚡ Load 30 Sample Students", "#047857");

        TextField tfFilter = new TextField();
        tfFilter.setPromptText("Filter students by ID, Name, or Batch...");
        tfFilter.setPrefWidth(260);

        Button btnDeleteSelected = primaryButton("🗑 Remove Selected", "#DC2626");

        TextArea errorLog = new TextArea();
        errorLog.setPrefHeight(60);
        errorLog.setEditable(false);
        errorLog.setPromptText("CSV Validation & Missing Data logs will appear here...");

        HBox addBar = cardContainerHBox();
        TextField tfId = new TextField(); tfId.setPromptText("ID (e.g. IBA-201)"); tfId.setPrefWidth(130);
        TextField tfName = new TextField(); tfName.setPromptText("Full Name"); tfName.setPrefWidth(170);
        TextField tfBatch = new TextField(); tfBatch.setPromptText("Batch (e.g. BCS-1)"); tfBatch.setPrefWidth(130);
        TextField tfCourse = new TextField(); tfCourse.setPromptText("Course (e.g. OOP)"); tfCourse.setPrefWidth(140);
        Button btnAddManual = primaryButton("➕ Add Student", "#0F172A");

        btnAddManual.setOnAction(e -> {
            if (tfId.getText().trim().isEmpty() || tfName.getText().trim().isEmpty() || tfBatch.getText().trim().isEmpty()) {
                showAlert("Missing Fields", "Please enter Student ID, Full Name, and Batch.");
                return;
            }
            studentList.add(new Student(
                    tfId.getText().trim(),
                    tfName.getText().trim(),
                    tfBatch.getText().trim(),
                    tfCourse.getText().trim().isEmpty() ? "General" : tfCourse.getText().trim()
            ));
            tfId.clear(); tfName.clear(); tfBatch.clear(); tfCourse.clear();
            setStatus("Added student manually. Total students: " + studentList.size());
        });
        addBar.getChildren().addAll(new Label("Quick Add:"), tfId, tfName, tfBatch, tfCourse, btnAddManual);

        FilteredList<Student> filteredData = new FilteredList<>(studentList, p -> true);
        tfFilter.textProperty().addListener((obs, oldV, newV) -> {
            filteredData.setPredicate(s -> {
                if (newV == null || newV.isEmpty()) return true;
                String lower = newV.toLowerCase();
                return s.getStudentId().toLowerCase().contains(lower)
                        || s.getName().toLowerCase().contains(lower)
                        || s.getBatch().toLowerCase().contains(lower);
            });
        });

        TableView<Student> table = new TableView<>(filteredData);
        TableColumn<Student, String> c1 = new TableColumn<>("Student ID");
        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStudentId()));
        TableColumn<Student, String> c2 = new TableColumn<>("Full Name");
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        TableColumn<Student, String> c3 = new TableColumn<>("Batch / Group");
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBatch()));
        TableColumn<Student, String> c4 = new TableColumn<>("Course");
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCourse()));
        table.getColumns().addAll(c1, c2, c3, c4);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        btnImport.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Student CSV File");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File file = fc.showOpenDialog(mainStage);
            if (file != null) {
                CsvService.ImportResult res = csvService.importFromCsv(file);
                studentList.setAll(res.validStudents);
                if (res.errors.isEmpty()) {
                    errorLog.setText("✅ SUCCESS: Imported " + res.validStudents.size() + " valid students. No missing data!");
                } else {
                    errorLog.setText("⚠️ Imported " + res.validStudents.size() + " valid students. Skipped rows with missing data:\n" + String.join("\n", res.errors));
                }
                setStatus("Imported " + res.validStudents.size() + " students from " + file.getName());
            }
        });

        btnSample.setOnAction(e -> {
            loadSampleData();
            errorLog.setText("✅ Loaded 30 sample IBA students (Batches: BCS-1, BBA-2, BAF-3).");
            setStatus("Loaded 30 sample students.");
        });

        btnDeleteSelected.setOnAction(e -> {
            Student sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                studentList.remove(sel);
                setStatus("Removed student: " + sel.getStudentId());
            }
        });

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        topBar.getChildren().addAll(btnImport, btnSample, sp, new Label("🔍"), tfFilter, btnDeleteSelected);

        box.getChildren().addAll(title, topBar, addBar, errorLog, table);
        contentArea.getChildren().setAll(box);
    }

    // =========================================================================
    // MODULE 3: ROOM DISTRIBUTION
    // =========================================================================
    private void showRoomView() {
        VBox box = new VBox(15);
        Label title = headerLabel("Module 3: Exam Room Distribution & Capacity");

        HBox form = cardContainerHBox();
        TextField tfName = new TextField(); tfName.setPromptText("Room Name (e.g. Auditorium)");
        TextField tfRows = new TextField(); tfRows.setPromptText("Rows (e.g. 5)"); tfRows.setPrefWidth(110);
        TextField tfCols = new TextField(); tfCols.setPromptText("Columns (e.g. 6)"); tfCols.setPrefWidth(120);
        Button btnAdd = primaryButton("➕ Add Room", "#047857");
        Button btnRemove = primaryButton("🗑 Delete Selected Room", "#DC2626");

        TableView<Room> table = new TableView<>(roomList);

        btnAdd.setOnAction(e -> {
            try {
                String name = tfName.getText().trim();
                int r = Integer.parseInt(tfRows.getText().trim());
                int c = Integer.parseInt(tfCols.getText().trim());
                if (!name.isEmpty() && r > 0 && c > 0) {
                    roomList.add(new Room(name, r, c));
                    tfName.clear(); tfRows.clear(); tfCols.clear();
                    setStatus("Added new room: " + name + " (Capacity: " + (r * c) + ")");
                }
            } catch (Exception ex) {
                showAlert("Invalid Input", "Please enter a valid Room Name and numeric Rows/Columns.");
            }
        });

        btnRemove.setOnAction(e -> {
            Room sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                roomList.remove(sel);
                setStatus("Deleted room: " + sel.getRoomName());
            }
        });

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        form.getChildren().addAll(new Label("New Room:"), tfName, tfRows, tfCols, btnAdd, sp, btnRemove);

        TableColumn<Room, String> c1 = new TableColumn<>("Room Name");
        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRoomName()));
        TableColumn<Room, String> c2 = new TableColumn<>("Grid Layout (Rows × Cols)");
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRows() + " Rows × " + d.getValue().getCols() + " Columns"));
        TableColumn<Room, String> c3 = new TableColumn<>("Total Capacity");
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCapacity() + " Seats"));
        TableColumn<Room, String> c4 = new TableColumn<>("Occupied Seats");
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOccupiedCount() + " / " + d.getValue().getCapacity()));
        table.getColumns().addAll(c1, c2, c3, c4);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        box.getChildren().addAll(title, form, table);
        contentArea.getChildren().setAll(box);
    }

    // =========================================================================
    // MODULES 4 & 5: VISUAL SEATING MAP WITH WHITEBOARD STAGE & 2-CLICK SWAP
    // =========================================================================
    private void showSeatingMapView() {
        VBox box = new VBox(12);
        Label title = headerLabel("Modules 4 & 5: Interactive Visual Seating Map");

        HBox controls = cardContainerHBox();
        Button btnAuto = primaryButton("⚡ Auto-Generate Anti-Cheating Seating", "#800020");
        Button btnClearRoom = primaryButton("🧹 Clear Seats", "#475569");

        roomSelector = new ComboBox<>(roomList);
        if (!roomList.isEmpty()) roomSelector.getSelectionModel().selectFirst();
        roomSelector.setOnAction(e -> {
            swapSourceRoom = null;
            renderVisualGrid();
        });

        btnAuto.setOnAction(e -> {
            if (studentList.isEmpty()) loadSampleData();
            int placed = seatingService.generateSeating(studentList, roomList);
            swapSourceRoom = null;
            renderVisualGrid();
            setStatus("Seated " + placed + " students! Notice how adjacent desks have different colors/batches.");
        });

        btnClearRoom.setOnAction(e -> {
            for (Room r : roomList) r.clearSeats();
            swapSourceRoom = null;
            renderVisualGrid();
            setStatus("Cleared all room seat assignments.");
        });

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label tip = new Label("💡 Click any desk to Assign, Vacate, or Swap two seats!");
        tip.setTextFill(Color.web("#047857"));
        tip.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        controls.getChildren().addAll(btnAuto, btnClearRoom, new Label("Room:"), roomSelector, sp, tip);

        // Front of Room / Whiteboard Stage Orientation Banner
        HBox stageBanner = new HBox();
        stageBanner.setAlignment(Pos.CENTER);
        stageBanner.setPadding(new Insets(8));
        stageBanner.setStyle("-fx-background-color: #1E293B; -fx-background-radius: 8;");
        Label stageLbl = new Label("⬆️   FRONT OF EXAM ROOM — WHITEBOARD & INVIGILATOR DESK   ⬆️");
        stageLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        stageLbl.setTextFill(Color.web("#F8FAFC"));
        stageBanner.getChildren().add(stageLbl);

        visualGrid = new GridPane();
        visualGrid.setHgap(14);
        visualGrid.setVgap(14);
        visualGrid.setPadding(new Insets(15));
        visualGrid.setAlignment(Pos.TOP_CENTER);

        ScrollPane scroll = new ScrollPane(visualGrid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-radius: 10;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        renderVisualGrid();
        box.getChildren().addAll(title, controls, stageBanner, scroll);
        contentArea.getChildren().setAll(box);
    }

    private void renderVisualGrid() {
        visualGrid.getChildren().clear();
        Room room = roomSelector.getValue();
        if (room == null) return;

        Student[][] seats = room.getSeats();
        for (int r = 0; r < room.getRows(); r++) {
            for (int c = 0; c < room.getCols(); c++) {
                final int rowIdx = r;
                final int colIdx = c;
                Student s = seats[r][c];

                VBox desk = new VBox(5);
                desk.setPrefSize(160, 88);
                desk.setPadding(new Insets(9));
                desk.setAlignment(Pos.CENTER_LEFT);

                boolean isSwapSelected = (swapSourceRoom == room && swapSourceRow == r && swapSourceCol == c);

                Label seatCode = new Label("Row " + (r + 1) + " • Seat " + (c + 1));
                seatCode.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));

                if (isSwapSelected) {
                    desk.setStyle("-fx-background-color: #FEF08A; -fx-border-color: #CA8A04; -fx-border-width: 3; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;");
                    seatCode.setText("🔄 SWAP SELECTED");
                    seatCode.setTextFill(Color.web("#854D0E"));
                } else if (s != null) {
                    String[] theme = getBatchColors(s.getBatch());
                    desk.setStyle("-fx-background-color: " + theme[0] + "; -fx-border-color: " + theme[1] + "; -fx-border-width: 2; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;");
                    seatCode.setTextFill(Color.web(theme[2]));
                } else {
                    desk.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #CBD5E1; -fx-border-style: dashed; -fx-border-width: 1.5; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;");
                    seatCode.setTextFill(Color.web("#64748B"));
                }

                if (s != null) {
                    Label idLbl = new Label(s.getStudentId() + "  [" + s.getBatch() + "]");
                    idLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                    idLbl.setTextFill(Color.web("#0F172A"));

                    Label nameLbl = new Label(s.getName());
                    nameLbl.setFont(Font.font("Segoe UI", 11));
                    nameLbl.setTextFill(Color.web("#334155"));

                    desk.getChildren().addAll(seatCode, idLbl, nameLbl);
                } else {
                    Label empty = new Label(swapSourceRoom != null ? "↪ Click to Move Here" : "+ Click to Assign");
                    empty.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
                    empty.setTextFill(Color.web("#94A3B8"));
                    desk.getChildren().addAll(seatCode, empty);
                }

                desk.setOnMouseClicked(e -> handleDeskClick(room, rowIdx, colIdx));
                visualGrid.add(desk, c, r);
            }
        }
    }

    private void handleDeskClick(Room room, int r, int c) {
        // Check if we are completing a 2-Click Seat Swap!
        if (swapSourceRoom != null) {
            Student first = swapSourceRoom.getSeats()[swapSourceRow][swapSourceCol];
            Student second = room.getSeats()[r][c];
            swapSourceRoom.getSeats()[swapSourceRow][swapSourceCol] = second;
            room.getSeats()[r][c] = first;
            swapSourceRoom = null;
            renderVisualGrid();
            setStatus("Swapped seats successfully!");
            return;
        }

        Student current = room.getSeats()[r][c];
        String seatLabel = room.getRoomName() + " — Row " + (r + 1) + ", Seat " + (c + 1);

        if (current != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Desk Actions — " + seatLabel);
            alert.setHeaderText("Assigned Student: " + current.getName() + " (" + current.getStudentId() + ")");
            alert.setContentText("Batch: " + current.getBatch() + " | Course: " + current.getCourse() +
                    "\n\nChoose an action for this seat:");

            ButtonType btnSwap = new ButtonType("🔄 Swap with Another Seat");
            ButtonType btnVacate = new ButtonType("🗑 Vacate Seat");
            ButtonType btnClose = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(btnSwap, btnVacate, btnClose);

            Optional<ButtonType> res = alert.showAndWait();
            if (res.isPresent()) {
                if (res.get() == btnVacate) {
                    room.getSeats()[r][c] = null;
                    renderVisualGrid();
                    setStatus("Vacated " + seatLabel);
                } else if (res.get() == btnSwap) {
                    swapSourceRoom = room;
                    swapSourceRow = r;
                    swapSourceCol = c;
                    renderVisualGrid();
                    setStatus("Swap Mode: Now click any other desk to swap with " + current.getName() + "!");
                }
            }
        } else {
            List<Student> unseated = getUnseatedStudents();
            if (unseated.isEmpty()) {
                showAlert("No Unseated Students", "All loaded students are already assigned to a seat (or no students loaded yet).");
                return;
            }
            ChoiceDialog<Student> dialog = new ChoiceDialog<>(unseated.get(0), unseated);
            dialog.setTitle("Manual Seat Assignment");
            dialog.setHeaderText("Assign a student to " + seatLabel);
            dialog.setContentText("Select Student:");
            Optional<Student> res = dialog.showAndWait();
            res.ifPresent(chosen -> {
                room.getSeats()[r][c] = chosen;
                renderVisualGrid();
                setStatus("Manually assigned " + chosen.getName() + " to " + seatLabel);
            });
        }
    }

    private List<Student> getUnseatedStudents() {
        List<Student> seated = new ArrayList<>();
        for (Room r : roomList) {
            Student[][] g = r.getSeats();
            for (int i = 0; i < r.getRows(); i++) {
                for (int j = 0; j < r.getCols(); j++) {
                    if (g[i][j] != null) seated.add(g[i][j]);
                }
            }
        }
        List<Student> unseated = new ArrayList<>();
        for (Student s : studentList) {
            if (!seated.contains(s)) unseated.add(s);
        }
        return unseated;
    }

    private String[] getBatchColors(String batch) {
        int hash = Math.abs(batch.toUpperCase().hashCode()) % 4;
        return switch (hash) {
            case 0 -> new String[]{"#EFF6FF", "#3B82F6", "#1D4ED8"};
            case 1 -> new String[]{"#ECFDF5", "#10B981", "#047857"};
            case 2 -> new String[]{"#F5F3FF", "#8B5CF6", "#6D28D9"};
            default -> new String[]{"#FFFBEB", "#F59E0B", "#B45309"};
        };
    }

    // =========================================================================
    // MODULE 6: STUDENT SEAT SEARCH
    // =========================================================================
    private void showSearchView() {
        VBox box = new VBox(18);
        Label title = headerLabel("Module 6: Instant Student Seat Search");

        HBox searchBar = cardContainerHBox();
        TextField tfSearch = new TextField();
        tfSearch.setPromptText("Type Student ID (e.g. IBA-101) and press Enter...");
        tfSearch.setPrefWidth(280);
        Button btnFind = primaryButton("🔍 Search Seat", "#1D4ED8");

        TextArea resultBox = new TextArea();
        resultBox.setEditable(false);
        resultBox.setFont(Font.font("Consolas", FontWeight.BOLD, 15));
        resultBox.setPrefHeight(230);

        Runnable runSearch = () -> {
            String res = seatingService.searchStudentSeat(tfSearch.getText(), roomList);
            resultBox.setText(res);
        };
        tfSearch.setOnAction(e -> runSearch.run());
        btnFind.setOnAction(e -> runSearch.run());

        searchBar.getChildren().addAll(new Label("Student ID:"), tfSearch, btnFind);
        box.getChildren().addAll(title, searchBar, resultBox);
        contentArea.getChildren().setAll(box);
    }

    // =========================================================================
    // MODULES 7 & 8: PDF & ATTENDANCE SHEETS
    // =========================================================================
    private void showReportsView() {
        VBox box = new VBox(20);
        Label title = headerLabel("Modules 7 & 8: Official IBA PDF Reports & Attendance Sheets");

        VBox card = cardContainer();
        Label info = new Label("Configure Exam Details & Generate Ready-to-Print Official IBA PDFs:");
        info.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));

        GridPane metaGrid = new GridPane();
        metaGrid.setHgap(15);
        metaGrid.setVgap(10);
        TextField tfExamName = new TextField("Final Semester Examinations — 2026");
        tfExamName.setPrefWidth(250);
        DatePicker dpDate = new DatePicker(LocalDate.now());
        TextField tfSlot = new TextField("Morning Shift (09:00 AM - 12:00 PM)");
        tfSlot.setPrefWidth(250);

        metaGrid.add(new Label("Exam Title:"), 0, 0); metaGrid.add(tfExamName, 1, 0);
        metaGrid.add(new Label("Exam Date:"), 2, 0); metaGrid.add(dpDate, 3, 0);
        metaGrid.add(new Label("Time Slot:"), 0, 1); metaGrid.add(tfSlot, 1, 1);

        Button btnPdfSeating = primaryButton("📄 Module 7: Export & Open Seating Plan PDF", "#1D4ED8");
        btnPdfSeating.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setInitialFileName("IBA_Seating_Plan.pdf");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File f = fc.showSaveDialog(mainStage);
            if (f != null) {
                try {
                    pdfService.exportSeatingPlanPdf(roomList, f);
                    setStatus("Saved Seating Plan PDF to " + f.getName());
                    openFileAutomatically(f);
                } catch (Exception ex) {
                    showAlert("Error", ex.getMessage());
                }
            }
        });

        Button btnPdfAttendance = primaryButton("✍️ Module 8: Export & Open Attendance Sheets PDF (with Signature Column)", "#800020");
        btnPdfAttendance.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setInitialFileName("IBA_Attendance_Sheets.pdf");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File f = fc.showSaveDialog(mainStage);
            if (f != null) {
                try {
                    pdfService.exportAttendanceSheetPdf(roomList, f);
                    setStatus("Saved Attendance Sheets PDF to " + f.getName());
                    openFileAutomatically(f);
                } catch (Exception ex) {
                    showAlert("Error", ex.getMessage());
                }
            }
        });

        card.getChildren().addAll(info, metaGrid, new Separator(), btnPdfSeating, btnPdfAttendance);
        box.getChildren().addAll(title, card);
        contentArea.getChildren().setAll(box);
    }

    // =========================================================================
    // NEW IMPROVEMENT 2: AUTO-SAVE & AUTO-LOAD DATA PERSISTENCE
    // =========================================================================
    private void saveStateToDisk() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(SAVE_FILE))) {
            for (Student s : studentList) {
                pw.println("STUDENT|" + s.getStudentId() + "|" + s.getName() + "|" + s.getBatch() + "|" + s.getCourse());
            }
            for (Room r : roomList) {
                pw.println("ROOM|" + r.getRoomName() + "|" + r.getRows() + "|" + r.getCols());
            }
            for (int ri = 0; ri < roomList.size(); ri++) {
                Room r = roomList.get(ri);
                Student[][] g = r.getSeats();
                for (int row = 0; row < r.getRows(); row++) {
                    for (int col = 0; col < r.getCols(); col++) {
                        if (g[row][col] != null) {
                            pw.println("SEAT|" + ri + "|" + row + "|" + col + "|" + g[row][col].getStudentId());
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private boolean loadStateFromDisk() {
        File f = new File(SAVE_FILE);
        if (!f.exists()) return false;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            studentList.clear();
            roomList.clear();
            Map<String, Student> map = new HashMap<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p[0].equals("STUDENT") && p.length >= 5) {
                    Student s = new Student(p[1], p[2], p[3], p[4]);
                    studentList.add(s);
                    map.put(s.getStudentId(), s);
                } else if (p[0].equals("ROOM") && p.length >= 4) {
                    roomList.add(new Room(p[1], Integer.parseInt(p[2]), Integer.parseInt(p[3])));
                } else if (p[0].equals("SEAT") && p.length >= 5) {
                    int ri = Integer.parseInt(p[1]);
                    int row = Integer.parseInt(p[2]);
                    int col = Integer.parseInt(p[3]);
                    Student s = map.get(p[4]);
                    if (ri < roomList.size() && s != null) {
                        roomList.get(ri).getSeats()[row][col] = s;
                    }
                }
            }
            return !roomList.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private void openFileAutomatically(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                showAlert("Saved Successfully", "File saved at:\n" + file.getAbsolutePath());
            }
        } catch (Exception ex) {
            showAlert("Saved Successfully", "File saved at:\n" + file.getAbsolutePath());
        }
    }

    private void loadSampleData() {
        studentList.clear();
        String[] batches = {"BCS-1", "BBA-2", "BAF-3"};
        String[] courses = {"OOP Java", "Marketing", "Accounting"};
        String[] names = {
                "Ali Khan", "Sara Ahmed", "Bilal Raza", "Ayesha Noor", "Usman Tariq",
                "Fatima Zahra", "Hamza Sheikh", "Zainab Bibi", "Omar Farooq", "Hira Mani",
                "Saad Afridi", "Mariam Nawaz", "Danish Taimoor", "Sana Javed", "Fahad Mustafa",
                "Mahira Khan", "Atif Aslam", "Momina Mustehsan", "Babar Azam", "Shaheen Shah",
                "Haris Rauf", "Shadab Khan", "Rizwan Ahmed", "Fakhar Zaman", "Naseem Shah",
                "Saim Ayub", "Abrar Ahmed", "Salman Agha", "Saud Shakeel", "Shan Masood"
        };
        for (int i = 0; i < 30; i++) {
            int idx = i % 3;
            studentList.add(new Student("IBA-" + (101 + i), names[i], batches[idx], courses[idx]));
        }
    }

    private void saveSampleCsvFile() {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("sample_iba_students.csv");
        File f = fc.showSaveDialog(mainStage);
        if (f != null) {
            try (PrintWriter pw = new PrintWriter(f)) {
                pw.println("StudentID,Name,Batch,Course");
                pw.println("IBA-101,Ali Khan,BCS-1,OOP Java");
                pw.println("IBA-102,Sara Ahmed,BBA-2,Marketing");
                pw.println("IBA-103,Bilal Raza,BAF-3,Accounting");
                pw.println("IBA-104,Ayesha Noor,BCS-1,OOP Java");
                pw.println("IBA-105,,BBA-2,Marketing");
                pw.println("IBA-106,Usman Tariq,BAF-3,Accounting");
                setStatus("Created sample CSV at " + f.getName());
                showAlert("Sample CSV Created", "Saved! Now go to '2. Student Data (CSV)' and import this file to test missing-data detection.");
            } catch (Exception ex) {
                showAlert("Error", ex.getMessage());
            }
        }
    }

    private Label headerLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 21));
        l.setTextFill(Color.web("#0F172A"));
        return l;
    }

    private VBox statCard(String label, String val, String sub, String colorHex) {
        VBox card = new VBox(4);
        card.setPrefWidth(215);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #E2E8F0; -fx-border-radius: 10;");
        Label t = new Label(label);
        t.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        t.setTextFill(Color.web("#64748B"));
        Label v = new Label(val);
        v.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        v.setTextFill(Color.web(colorHex));
        Label s = new Label(sub);
        s.setFont(Font.font("Segoe UI", 11));
        s.setTextFill(Color.web("#94A3B8"));
        card.getChildren().addAll(t, v, s);
        return card;
    }

    private VBox cardContainer() {
        VBox v = new VBox(15);
        v.setPadding(new Insets(20));
        v.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #E2E8F0; -fx-border-radius: 10;");
        return v;
    }

    private HBox cardContainerHBox() {
        HBox h = new HBox(10);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(12, 16, 12, 16));
        h.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #E2E8F0; -fx-border-radius: 10;");
        return h;
    }

    private Button primaryButton(String text, String hex) {
        Button b = new Button(text);
        b.setPadding(new Insets(9, 16, 9, 16));
        b.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        b.setStyle("-fx-background-color: " + hex + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
        return b;
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}