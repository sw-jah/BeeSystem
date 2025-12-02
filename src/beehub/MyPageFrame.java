package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// [중요] 관리자 데이터 매니저 임포트
import admin.LotteryManager;
import admin.LotteryManager.LotteryRound;
import admin.LotteryManager.Applicant;

public class MyPageFrame extends JFrame {

    // 🎨 컬러 테마
    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color NAV_BG = new Color(255, 255, 255);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(89, 60, 28);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157);
    private static final Color BORDER_COLOR = new Color(220, 220, 220);
    private static final Color POPUP_BG = new Color(255, 250, 205);
    private static final Color LINK_COLOR = new Color(0, 102, 204);
    private static final Color OVERDUE_RED = new Color(200, 50, 50);
    private static final Color CANCEL_RED = new Color(200, 50, 50);
    private static final Color WINNER_GREEN = new Color(0, 150, 0);

    private static Font uiFont;

    static {
        try {
            InputStream is = MyPageFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) {
                File f = new File("resource/fonts/DNFBitBitv2.ttf");
                if (f.exists()) {
                    uiFont = Font.createFont(Font.TRUETYPE_FONT, f).deriveFont(14f);
                } else {
                    uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
                }
            } else {
                uiFont = Font.createFont(Font.TRUETYPE_FONT, is);
                uiFont = uiFont.deriveFont(14f);
            }
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(uiFont);
        } catch (Exception e) {
            uiFont = new Font("SansSerif", Font.PLAIN, 14);
        }
    }
    
    // ==========================================================
    // 📊 데이터 구조
    // ==========================================================

    public static class MyPagePost {
        int no; String title; String writer; String date; int likes; int comments; String content;
        public MyPagePost(int n, String t, String w, String d, int l, int c, String content) {
            this.no = n; this.title = t; this.writer = w; this.date = d; this.likes = l; this.comments = c; this.content = content;
        }
    }

    public static class RentalItem {
        String itemName;
        String returnDate; 
        boolean isReturned;

        public RentalItem(String name, String date, boolean returned) {
            this.itemName = name;
            this.returnDate = date;
            this.isReturned = returned;
        }
    }

    public static enum ReservationStatus {
        CANCELLABLE, COMPLETED, USER_CANCELLED, AUTO_CANCELLED
    }

    public static class SpaceRentalItem {
        String roomName; String reservationDate; String startTime; String endTime; int headcount; ReservationStatus status; 
        public SpaceRentalItem(String name, String date, String startTime, String endTime, int count, ReservationStatus status) {
            this.roomName = name; this.reservationDate = date; this.startTime = startTime; this.endTime = endTime; this.headcount = count; this.status = status;
        }
    }

    public static class EventParticipationItem {
        String eventTitle; String eventDate; String eventTime; boolean requiresSecretCode; ReservationStatus status; 
        public EventParticipationItem(String title, String date, String time, boolean requiresCode, ReservationStatus status) {
            this.eventTitle = title; this.eventDate = date; this.eventTime = time; this.requiresSecretCode = requiresCode; this.status = status;
        }
    }
    
    // [수정] 사용자 응모 기록 (내 응모함용) - admin.LotteryManager의 LotteryRound 사용
    public static class UserApplication {
        LotteryRound round;
        String applicationDate; 
        int entryCount; // 응모 횟수
        
        public UserApplication(LotteryRound round, String appDate, int count) {
            this.round = round; this.applicationDate = appDate; this.entryCount = count;
        }
    }

    // 사용자 정보 (더미 데이터)
    private String userName = "김꿀단지";
    private String userDept = "소프트웨어융합학과";
    private String userId = "202390000";
    private String userNickname = "꿀벌학생";
    private String userPassword = "password123";
    private int userPoint = 250; 
    
    // UI 컴포넌트
    private JList<String> menuList;
    private CardLayout cardLayout;
    private JPanel detailPanel;
    private JLabel nicknameLabel;
    private ImageIcon beeIcon; 
    
    // 활동 목록 더미 데이터
    private List<MyPagePost> dummyPosts; 
    private List<RentalItem> dummyRentals; 
    private List<SpaceRentalItem> dummySpaceRentals; 
    private List<EventParticipationItem> dummyEvents; 
    
    // [수정] 내 응모 기록 리스트
    private List<UserApplication> myApplications;

    // 프레임 크기 및 레이아웃 상수
    private final int FRAME_WIDTH = 800;
    private final int FRAME_HEIGHT = 680; 
    private final int CONTENT_Y = 130;
    private final int CONTENT_HEIGHT = FRAME_HEIGHT - CONTENT_Y - 30; 
    private final int MENU_WIDTH = 170; 
    private final int DETAIL_X = 20 + MENU_WIDTH + 10; 
    private final int DETAIL_WIDTH = FRAME_WIDTH - DETAIL_X - 20; 

    public MyPageFrame() {
        setTitle("서울여대 꿀단지 - 마이페이지");
        setSize(FRAME_WIDTH, FRAME_HEIGHT); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        loadImages(); 
        initDummyData(); 
        initHeader();
        initNav();
        initContent();

        setVisible(true);
    }
    
    private void loadImages() {
         try {
            ImageIcon originalBeeIcon = new ImageIcon("resource/img/login-bee.png");
            if (originalBeeIcon.getIconWidth() > 0) {
                Image img = originalBeeIcon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH); 
                beeIcon = new ImageIcon(img);
            }
        } catch (Exception e) {
            System.err.println("Failed to load images.");
        }
    }
    
    // 더미 데이터 생성
    private void initDummyData() {
        LocalDate today = LocalDate.of(2025, 12, 1); 
        
        dummyPosts = new ArrayList<>();
        dummyPosts.add(new MyPagePost(1, "커뮤니티 기능 완성! (내 글)", userNickname, today.toString(), 15, 5, "완성해서 너무 기뻐요!"));
        dummyPosts.add(new MyPagePost(2, "Spring 강의 자료 요청해요", userNickname, today.minusDays(2).toString(), 8, 3, "혹시 자료 공유 가능하신 분?"));
        dummyPosts.add(new MyPagePost(3, "점심 메뉴 추천 받습니다", "다른학생1", today.minusDays(5).toString(), 20, 10, "오늘 뭐 먹지..."));
        dummyPosts.add(new MyPagePost(4, "시험 기간 힘내세요!", "다른학생2", today.minusDays(10).toString(), 50, 2, "모두 A+ 받기를 기원합니다."));
        
        dummyRentals = new ArrayList<>();
        dummyRentals.add(new RentalItem("노트북 3", "2025-12-04", false)); 
        dummyRentals.add(new RentalItem("보조배터리 5", "2025-11-28", false)); 
        dummyRentals.add(new RentalItem("빔 프로젝터", "2025-12-10", false)); 
        dummyRentals.add(new RentalItem("무선 마우스", "2025-11-20", true)); 
        dummyRentals.add(new RentalItem("삼각대", "2025-10-01", true)); 

        dummySpaceRentals = new ArrayList<>();
        dummySpaceRentals.add(new SpaceRentalItem("세미나실 1", "2025-12-05", "14:00", "16:00", 8, ReservationStatus.CANCELLABLE));
        dummySpaceRentals.add(new SpaceRentalItem("실습실 F", "2025-11-25", "18:00", "20:00", 12, ReservationStatus.COMPLETED)); 
        dummySpaceRentals.add(new SpaceRentalItem("세미나실 2", "2025-12-03", "09:00", "11:00", 5, ReservationStatus.USER_CANCELLED));
        dummySpaceRentals.add(new SpaceRentalItem("실습실 B", "2025-11-30", "11:00", "13:00", 6, ReservationStatus.AUTO_CANCELLED));
        dummySpaceRentals.add(new SpaceRentalItem("실습실 A", "2025-12-01", "10:00", "12:00", 4, ReservationStatus.COMPLETED)); 
        
        dummyEvents = new ArrayList<>();
        dummyEvents.add(new EventParticipationItem("SW 멘토링 특강", "2025-12-10", "15:00", false, ReservationStatus.CANCELLABLE));
        dummyEvents.add(new EventParticipationItem("개강총회", "2025-09-01", "18:00", false, ReservationStatus.COMPLETED)); 
        dummyEvents.add(new EventParticipationItem("총학생회 간식 배부", "2025-12-05", "12:00", true, ReservationStatus.COMPLETED)); 
        dummyEvents.add(new EventParticipationItem("캡스톤 디자인 발표회", "2025-12-20", "13:00", false, ReservationStatus.USER_CANCELLED)); 

        // [수정] 나의 응모 기록 초기화 (관리자 매니저와 연동)
        myApplications = new ArrayList<>();
        List<LotteryRound> allRounds = LotteryManager.getAllRounds();
        
        // 테스트용: 1회차에 내가 이미 5번 응모했다고 가정
        if (!allRounds.isEmpty()) {
            LotteryRound r1 = allRounds.get(0);
            myApplications.add(new UserApplication(r1, "2023-12-02", 5));
            r1.addApplicant(userName, userId, 5); // 관리자 쪽 명단에도 추가
        }
    }

    private String getRank(int point) {
        if (point >= 200) return "여왕벌";
        if (point >= 100) return "꿀벌";
        return "일벌";
    }

    private void initHeader() {
        JPanel headerPanel = new JPanel(null);
        headerPanel.setBounds(0, 0, FRAME_WIDTH, 80);
        headerPanel.setBackground(HEADER_YELLOW);
        add(headerPanel);

        JLabel logoLabel = new JLabel("서울여대 꿀단지");
        logoLabel.setFont(uiFont.deriveFont(32f));
        logoLabel.setForeground(BROWN);
        logoLabel.setBounds(30, 20, 300, 40);
        headerPanel.add(logoLabel);

        JLabel jarIcon = new JLabel("");
        jarIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        jarIcon.setBounds(310, 25, 40, 40);
        headerPanel.add(jarIcon);
        
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 25));
        userInfoPanel.setBounds(400, 0, 380, 80);
        userInfoPanel.setOpaque(false);

        JLabel logoutText = new JLabel("| 로그아웃"); 
        logoutText.setFont(uiFont.deriveFont(14f));
        logoutText.setForeground(BROWN);
        logoutText.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutText.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showLogoutPopup(); }
        });
        userInfoPanel.add(logoutText);
        headerPanel.add(userInfoPanel);
    }

    private void initNav() {
        JPanel navPanel = new JPanel(new GridLayout(1, 6));
        navPanel.setBounds(0, 80, FRAME_WIDTH, 50);
        navPanel.setBackground(NAV_BG);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        add(navPanel);

        String[] menus = {"물품대여", "과행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"}; 
        for (String menu : menus) {
            JButton menuBtn = createNavButton(menu, menu.equals("마이페이지")); 
            navPanel.add(menuBtn);
        }
    }

    private void initContent() {
        JPanel contentPanel = new JPanel(null);
        contentPanel.setBounds(0, CONTENT_Y, FRAME_WIDTH, CONTENT_HEIGHT);
        contentPanel.setBackground(BG_MAIN);
        add(contentPanel);
        
        JPanel leftPanel = new JPanel(null);
        leftPanel.setBounds(20, 20, MENU_WIDTH, CONTENT_HEIGHT - 40); 
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new RoundedBorder(20, BORDER_COLOR, 1));
        contentPanel.add(leftPanel);

        String[] menuItems = {
            "나의 활동", "회원 정보", "작성 게시글", "댓글 단 게시글", "좋아요 누른 게시글", 
            "이용 기록", "물품 대여 기록", "공간 대여 기록", "과 행사 참여 기록", 
            "--- 분리선 ---", 
            "응모함" 
        };
        menuList = new JList<>(menuItems);
        menuList.setFont(uiFont.deriveFont(16f));
        menuList.setForeground(BROWN);
        menuList.setSelectionBackground(HIGHLIGHT_YELLOW);
        menuList.setSelectionForeground(BROWN);
        menuList.setCellRenderer(new MyPageListRenderer()); 
        
        JScrollPane menuScroll = new JScrollPane(menuList);
        menuScroll.setBounds(10, 10, MENU_WIDTH - 20, CONTENT_HEIGHT - 60); 
        menuScroll.setBorder(BorderFactory.createEmptyBorder());
        menuScroll.getViewport().setBackground(Color.WHITE);
        menuScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        menuScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        menuScroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        menuScroll.getVerticalScrollBar().setUnitIncrement(16);
        leftPanel.add(menuScroll);
        
        cardLayout = new CardLayout();
        detailPanel = new JPanel(cardLayout);
        detailPanel.setBounds(DETAIL_X, 20, DETAIL_WIDTH, CONTENT_HEIGHT - 40); 
        detailPanel.setBackground(Color.WHITE);
        detailPanel.setBorder(new RoundedBorder(20, BORDER_COLOR, 1));
        contentPanel.add(detailPanel);

        addDetailCards();
        
        menuList.setSelectedIndex(1);
        cardLayout.show(detailPanel, "회원 정보");
        
        menuList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedItem = menuList.getSelectedValue();
                if (selectedItem != null) {
                    if (!selectedItem.equals("나의 활동") && !selectedItem.equals("이용 기록") && !selectedItem.equals("--- 분리선 ---")) {
                         cardLayout.show(detailPanel, selectedItem);
                    }
                }
            }
        });
    }

    private void addDetailCards() {
        detailPanel.add(createUserInfoPanel(), "회원 정보");
        detailPanel.add(createActivityListPanel("작성 게시글"), "작성 게시글");
        detailPanel.add(createActivityListPanel("댓글 단 게시글"), "댓글 단 게시글");
        detailPanel.add(createActivityListPanel("좋아요 누른 게시글"), "좋아요 누른 게시글");
        detailPanel.add(createRentalListPanel(), "물품 대여 기록"); 
        detailPanel.add(createSpaceRentalListPanel(), "공간 대여 기록"); 
        detailPanel.add(createEventListPanel(), "과 행사 참여 기록"); 
        detailPanel.add(createApplicationPanel(), "응모함");
        
        JPanel welcomePanel = createPlaceholderPanel("환영합니다!", userName + "님의 마이페이지입니다.");
        detailPanel.add(welcomePanel, "나의 활동");
        detailPanel.add(welcomePanel, "이용 기록");
    }
    
    // [수정] 응모함 패널
    private JPanel createApplicationPanel() {
        JPanel panel = new JPanel(null);
        panel.setName("응모함");
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("꿀단지 응모함", SwingConstants.LEFT);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f)); 
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(20, 10, 500, 30);
        panel.add(titleLabel);

        int y = 50;

        JPanel pointStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pointStatusPanel.setBounds(20, y, DETAIL_WIDTH - 40, 40);
        pointStatusPanel.setOpaque(false);
        
        JLabel pointTitle = createLabel("나의 보유 꿀:");
        pointTitle.setFont(uiFont.deriveFont(Font.BOLD, 18f));
        pointStatusPanel.add(pointTitle);
        
        JLabel currentPointLabel = createLabel(userPoint + "꿀");
        currentPointLabel.setFont(uiFont.deriveFont(Font.BOLD, 18f));
        currentPointLabel.setForeground(WINNER_GREEN);
        pointStatusPanel.add(currentPointLabel);
        
        JLabel ruleLabel = createLabel(" (응모 방식: 100꿀 당 1회 응모)");
        ruleLabel.setFont(uiFont.deriveFont(14f));
        pointStatusPanel.add(ruleLabel);
        
        panel.add(pointStatusPanel);
        y += 50;

        JButton applyBtn = createStyledButton("응모하기", 150, 45);
        applyBtn.setBounds(100, y, 150, 45);
        applyBtn.addActionListener(e -> showApplyPopup(currentPointLabel));
        panel.add(applyBtn);

        JButton checkBtn = createStyledButton("당첨확인하기", 150, 45);
        checkBtn.setBounds(270, y, 150, 45);
        checkBtn.addActionListener(e -> showCheckWinningPopup());
        panel.add(checkBtn);
        y += 60;
        
        JLabel historyTitle = new JLabel("나의 응모 기록", SwingConstants.LEFT);
        historyTitle.setFont(uiFont.deriveFont(Font.BOLD, 20f));
        historyTitle.setForeground(BROWN);
        historyTitle.setBounds(20, y, 500, 30);
        panel.add(historyTitle);
        y += 40;

        String[] headers = {"회차", "경품 항목", "응모 횟수", "응모 일자"};
        DefaultTableModel tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // [수정] 테이블 데이터 채우기 (myApplications 사용)
        for (UserApplication item : myApplications) {
            tableModel.addRow(new Object[]{
                item.round.name.split(":")[0], 
                item.round.prizeName, 
                item.entryCount, 
                item.applicationDate
            });
        }

        JTable applicationTable = new JTable(tableModel);
        styleTable(applicationTable);
        
        applicationTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        applicationTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        applicationTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        applicationTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        
        applicationTable.getColumnModel().getColumn(0).setCellRenderer(new CenterRenderer());
        applicationTable.getColumnModel().getColumn(1).setCellRenderer(new CenterRenderer());
        applicationTable.getColumnModel().getColumn(2).setCellRenderer(new CenterRenderer());
        applicationTable.getColumnModel().getColumn(3).setCellRenderer(new CenterRenderer());
        
        JScrollPane scrollPane = new JScrollPane(applicationTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.setBounds(20, y, DETAIL_WIDTH - 40, CONTENT_HEIGHT - y - 60); 

        panel.add(scrollPane);

        return panel;
    }

    // [수정] 응모 팝업
    private void showApplyPopup(JLabel pointLabel) {
        LocalDate today = LocalDate.of(2025, 12, 1);
        
        List<LotteryRound> allRounds = LotteryManager.getAllRounds();
        
        List<LotteryRound> availableRounds = allRounds.stream()
            .filter(r -> {
                try {
                    return !LocalDate.parse(r.announcementDate).isBefore(today);
                } catch (Exception e) { return false; }
            })
            .collect(Collectors.toList());

        if (availableRounds.isEmpty()) {
            showCustomAlertPopup("응모 불가", "현재 응모 가능한 경품 회차가 없습니다.");
            return;
        }

        JDialog dialog = new JDialog(this, "경품 응모하기", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(500, 500); 
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        int y = 30;
        JLabel title = new JLabel("꿀단지 경품 응모", SwingConstants.CENTER);
        title.setFont(uiFont.deriveFont(Font.BOLD, 22f));
        title.setForeground(BROWN);
        title.setBounds(10, y, 480, 30);
        panel.add(title);
        y += 50;
        
        JLabel currentPointInfo = createLabel("나의 보유 꿀: " + userPoint + "꿀");
        currentPointInfo.setFont(uiFont.deriveFont(18f));
        currentPointInfo.setBounds(30, y, 440, 30);
        panel.add(currentPointInfo);
        y += 40;

        JLabel roundSelectLabel = createLabel("응모할 회차 선택:");
        roundSelectLabel.setBounds(30, y, 200, 30);
        panel.add(roundSelectLabel);
        
        String[] roundTitles = availableRounds.stream()
            .map(r -> r.name + ": " + r.prizeName)
            .toArray(String[]::new);
        
        JComboBox<String> roundCombo = new JComboBox<>(roundTitles);
        roundCombo.setFont(uiFont.deriveFont(16f));
        roundCombo.setBounds(200, y, 250, 30);
        panel.add(roundCombo);
        y += 40;
        
        JTextArea infoArea = new JTextArea();
        infoArea.setFont(uiFont.deriveFont(16f));
        infoArea.setEditable(false);
        infoArea.setOpaque(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBounds(30, y, 440, 100);
        panel.add(infoArea);
        
        Runnable updateInfo = () -> {
            int idx = roundCombo.getSelectedIndex();
            if(idx >= 0) {
                LotteryRound r = availableRounds.get(idx);
                infoArea.setText(String.format("당첨 발표: %s\n당첨 인원: %d명\n응모 기간: %s", 
                    r.announcementDate, r.winnerCount, r.applicationPeriod));
            }
        };
        updateInfo.run();
        roundCombo.addActionListener(e -> updateInfo.run());
        
        y += 110;

        JLabel countLabel = createLabel("응모 횟수 (1회당 100꿀):");
        countLabel.setBounds(30, y, 200, 30);
        panel.add(countLabel);
        
        JTextField countField = new JTextField("1");
        countField.setFont(uiFont.deriveFont(16f));
        countField.setBounds(250, y, 100, 30);
        panel.add(countField);
        y += 60;

        JButton applyFinalBtn = createPopupBtn("응모하기");
        applyFinalBtn.setBounds(100, y, 150, 45);
        applyFinalBtn.addActionListener(e -> {
            try {
                int count = Integer.parseInt(countField.getText().trim());
                if (count <= 0) {
                     showCustomAlertPopup("경고", "응모 횟수는 1회 이상이어야 합니다.");
                     return;
                }
                int requiredPoints = count * 100;
                if (userPoint < requiredPoints) {
                    showCustomAlertPopup("오류", "꿀이 부족합니다!");
                    return;
                }
                
                showCustomConfirmPopup(count + "회 응모하시겠습니까?", () -> {
                    userPoint -= requiredPoints;
                    pointLabel.setText(userPoint + "꿀");
                    
                    LotteryRound r = availableRounds.get(roundCombo.getSelectedIndex());
                    
                    // 1. 관리자 데이터 연동
                    r.addApplicant(userName, userId, count);
                    
                    // 2. 내 응모 기록 연동
                    UserApplication existingApp = myApplications.stream()
                        .filter(app -> app.round == r)
                        .findFirst().orElse(null);
                    
                    if (existingApp != null) existingApp.entryCount += count;
                    else myApplications.add(new UserApplication(r, LocalDate.now().toString(), count));
                    
                    dialog.dispose();
                    showCustomAlertPopup("성공", "응모가 완료되었습니다!");
                    refreshApplicationPanel();
                });
                
            } catch (NumberFormatException ex) {
                showCustomAlertPopup("오류", "숫자만 입력하세요.");
            }
        });
        panel.add(applyFinalBtn);
            
        JButton cancelBtn = createPopupBtn("취소");
        cancelBtn.setBounds(260, y, 120, 45);
        cancelBtn.addActionListener(e -> dialog.dispose());
        panel.add(cancelBtn);

        dialog.setVisible(true);
    }

    // [수정] 당첨 확인 팝업
    private void showCheckWinningPopup() {
        JDialog dialog = new JDialog(this, "당첨 확인", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(450, 450); 
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        int y = 30;
        JLabel title = new JLabel("경품 응모 당첨 확인", SwingConstants.CENTER);
        title.setFont(uiFont.deriveFont(Font.BOLD, 22f));
        title.setForeground(BROWN);
        title.setBounds(10, y, 430, 30);
        panel.add(title);
        y += 50;

        JLabel roundSelectLabel = createLabel("회차 선택:");
        roundSelectLabel.setBounds(30, y, 100, 30);
        panel.add(roundSelectLabel);
        
        List<LotteryRound> allRounds = LotteryManager.getAllRounds();
        String[] roundTitles = allRounds.stream()
            .map(r -> r.name + ": " + r.prizeName)
            .toArray(String[]::new);
        
        JComboBox<String> roundCombo = new JComboBox<>(roundTitles);
        roundCombo.setFont(uiFont.deriveFont(16f));
        roundCombo.setBounds(140, y, 280, 30);
        panel.add(roundCombo);
        y += 50;
        
        JTextArea resultArea = new JTextArea("확인 버튼을 눌러주세요.");
        resultArea.setFont(uiFont.deriveFont(18f));
        resultArea.setForeground(BROWN);
        resultArea.setEditable(false);
        resultArea.setOpaque(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setBounds(30, y, 390, 120);
        panel.add(resultArea);
        y += 140;

        JButton confirmBtn = createPopupBtn("확인");
        confirmBtn.setBounds(100, y, 110, 45);
        confirmBtn.addActionListener(e -> {
            int idx = roundCombo.getSelectedIndex();
            if(idx < 0) return;
            LotteryRound r = allRounds.get(idx);
            
            Applicant myRecord = null;
            for(Applicant app : r.applicants) {
                if(app.hakbun.equals(userId)) {
                    myRecord = app;
                    break;
                }
            }
            
            String resultText;
            Color color;
            LocalDate today = LocalDate.of(2025, 12, 1);
            LocalDate annDate = LocalDate.parse(r.announcementDate);
            
            if (annDate.isAfter(today)) {
                resultText = "아직 발표일이 아닙니다.\n(" + r.announcementDate + " 발표)";
                color = BROWN;
            } else if (myRecord == null) {
                resultText = "응모 기록이 없습니다.";
                color = BROWN;
            } else if ("당첨".equals(myRecord.status)) {
                resultText = "🎉 축하합니다! 당첨되셨습니다!\n수령: " + r.pickupLocation + "\n기간: " + r.pickupPeriod;
                color = WINNER_GREEN;
            } else {
                resultText = "아쉽게도 미당첨되었습니다.";
                color = OVERDUE_RED;
            }
            
            resultArea.setText(resultText);
            resultArea.setForeground(color);
        });
        panel.add(confirmBtn);
            
        JButton closeBtn = createPopupBtn("닫기");
        closeBtn.setBounds(230, y, 110, 45);
        closeBtn.addActionListener(e -> dialog.dispose());
        panel.add(closeBtn);

        dialog.setVisible(true);
    }

    // [추가] 새로고침
    private void refreshApplicationPanel() {
        Component[] components = detailPanel.getComponents();
        for (Component comp : components) {
            if ("응모함".equals(comp.getName())) {
                detailPanel.remove(comp);
                break;
            }
        }
        detailPanel.add(createApplicationPanel(), "응모함");
        cardLayout.show(detailPanel, "응모함");
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    // --- 기타 렌더러 및 패널 생성 코드 ---

    class CenterRenderer extends DefaultTableCellRenderer {
        public CenterRenderer() { setHorizontalAlignment(JLabel.CENTER); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setFont(uiFont.deriveFont(16f)); 
            return c;
        }
    }

    class SpaceDateTimeRenderer extends DefaultTableCellRenderer {
        public SpaceDateTimeRenderer() { setHorizontalAlignment(JLabel.CENTER); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel label = (JLabel) c;
            if (value instanceof SpaceRentalItem) {
                SpaceRentalItem item = (SpaceRentalItem) value;
                label.setText(item.reservationDate + " " + item.startTime + "~" + item.endTime); 
            }
            label.setFont(uiFont.deriveFont(16f));
            return c;
        }
    }

    class EventScheduleRenderer extends DefaultTableCellRenderer {
        public EventScheduleRenderer() { setHorizontalAlignment(JLabel.CENTER); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel label = (JLabel) c;
            if (value instanceof EventParticipationItem) {
                EventParticipationItem item = (EventParticipationItem) value;
                label.setText(item.eventDate + " (" + item.eventTime + ")"); 
            }
            label.setFont(uiFont.deriveFont(16f));
            return c;
        }
    }

    class SpaceActionRenderer extends DefaultTableCellRenderer {
        public SpaceActionRenderer() { setHorizontalAlignment(JLabel.CENTER); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel label = (JLabel) c;
            label.setFont(uiFont.deriveFont(16f)); 
            if (isSelected) label.setBackground(HIGHLIGHT_YELLOW); else label.setBackground(Color.WHITE);
            ReservationStatus status = (ReservationStatus) value;
            label.setForeground(BROWN); 
            switch (status) {
                case CANCELLABLE: label.setText("<html><u>취소</u></html>"); label.setForeground(CANCEL_RED); break;
                case COMPLETED: label.setText("완료"); break;
                case USER_CANCELLED: label.setText("취소 완료"); break;
                case AUTO_CANCELLED: label.setText("예약 취소"); label.setForeground(OVERDUE_RED); label.setFont(uiFont.deriveFont(Font.BOLD, 16f)); break;
            }
            return label;
        }
    }
    
    class EventActionRenderer extends DefaultTableCellRenderer {
        public EventActionRenderer() { setHorizontalAlignment(JLabel.CENTER); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel label = (JLabel) c;
            label.setFont(uiFont.deriveFont(16f)); 
            label.setForeground(BROWN); 
            if (isSelected) label.setBackground(HIGHLIGHT_YELLOW); else label.setBackground(Color.WHITE);
            ReservationStatus status = (ReservationStatus) value;
            switch (status) {
                case CANCELLABLE: label.setText("<html><u>참여 취소</u></html>"); label.setForeground(CANCEL_RED); break;
                case COMPLETED: label.setText("완료"); break;
                case USER_CANCELLED: label.setText("취소 완료"); break;
                default: label.setText(""); break; 
            }
            return label;
        }
    }

    class RentalStatusRenderer extends DefaultTableCellRenderer {
        public RentalStatusRenderer() { setHorizontalAlignment(JLabel.CENTER); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel label = (JLabel) c;
            label.setFont(uiFont.deriveFont(16f)); 
            if (isSelected) label.setBackground(HIGHLIGHT_YELLOW); else label.setBackground(Color.WHITE);
            String statusText = value.toString();
            if (statusText.equals("반납 완료")) {
                label.setText(statusText); label.setForeground(BROWN); 
            } else {
                String dDayStatus = formatDDay(statusText);
                label.setText(statusText + " (" + dDayStatus + ")");
                if (dDayStatus.startsWith("D+")) {
                    label.setForeground(OVERDUE_RED); label.setFont(uiFont.deriveFont(Font.BOLD, 16f));
                } else {
                    label.setForeground(BROWN); 
                }
            }
            return label;
        }
    }
    
    private String formatDDay(String dateStr) {
        try {
            LocalDate today = LocalDate.of(2025, 12, 1);
            LocalDate returnDate = LocalDate.parse(dateStr);
            long daysDiff = ChronoUnit.DAYS.between(today, returnDate);
            if (daysDiff == 0) return "D-DAY";
            else if (daysDiff > 0) return "D-" + daysDiff;
            else return "D+" + Math.abs(daysDiff);
        } catch (Exception e) { return "날짜 오류"; }
    }

    // (기존 코드들: ActivityList, RentalList, SpaceRentalList, EventList Panel 생성)
    // 아래 패널 생성 로직들은 기존 코드와 동일합니다.

    private JPanel createUserInfoPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel titleLabel = new JLabel("회원 정보", SwingConstants.LEFT);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(20, 10, 200, 30);
        panel.add(titleLabel);
        JSeparator separator = new JSeparator();
        separator.setBounds(20, 45, 520, 1);
        panel.add(separator);
        int y = 70;
        y = addInfoRow(panel, y, "이름", userName, 400, false, null);
        y = addInfoRow(panel, y, "학과/학번", userDept + " / " + userId, 380, false, null); 
        y = addInfoRow(panel, y, "닉네임", userNickname, 250, true, new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showNicknameEditPopup(); }
        });
        y += 20; 
        y = addInfoRow(panel, y, "보유 꿀", userPoint + "꿀", 400, false, null);
        JLabel rankTitleLabel = createLabel("등급");
        rankTitleLabel.setFont(uiFont.deriveFont(16f));
        rankTitleLabel.setBounds(20, y, 100, 30);
        panel.add(rankTitleLabel);
        JLabel rankValueLabel = createLabel(""); 
        String rank = getRank(userPoint);
        rankValueLabel.setText(rank + " (" + userPoint + "/200)");
        if (rank.startsWith("꿀벌") && beeIcon != null) {
            rankValueLabel.setText(rankValueLabel.getText().replace("꿀벌", "꿀벌")); 
            rankValueLabel.setIcon(beeIcon);
            rankValueLabel.setHorizontalTextPosition(SwingConstants.RIGHT); 
            rankValueLabel.setIconTextGap(5);
        }
        rankValueLabel.setBounds(150, y, 400, 30);
        panel.add(rankValueLabel);
        y += 90; 
        JButton passwordBtn = createStyledButton("비밀번호 수정", 150, 40);
        passwordBtn.setBounds(20, y, 150, 40);
        passwordBtn.addActionListener(e -> showPasswordChangePopup());
        panel.add(passwordBtn);
        return panel;
    }
    
    private int addInfoRow(JPanel panel, int y, String title, String value, int valueWidth, boolean isEditable, MouseAdapter adapter) {
        JLabel titleLabel = createLabel(title);
        titleLabel.setFont(uiFont.deriveFont(16f));
        titleLabel.setBounds(20, y, 100, 30);
        panel.add(titleLabel);
        JLabel valueLabel = createLabel(value);
        valueLabel.setFont(uiFont.deriveFont(16f));
        valueLabel.setBounds(150, y, valueWidth, 30);
        panel.add(valueLabel);
        if (title.equals("닉네임")) this.nicknameLabel = valueLabel;
        if (isEditable && adapter != null) {
            JLabel editLink = new JLabel("<html><u>[수정]</u></html>");
            editLink.setFont(uiFont.deriveFont(14f));
            editLink.setForeground(LINK_COLOR);
            editLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
            editLink.setBounds(150 + valueWidth + 10, y, 50, 30); 
            editLink.addMouseListener(adapter); 
            panel.add(editLink);
        }
        return y + 40;
    }

    private JPanel createActivityListPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel titleLabel = new JLabel(title, SwingConstants.LEFT);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f)); 
        titleLabel.setForeground(BROWN);
        panel.add(titleLabel, BorderLayout.NORTH);
        String[] headers = {"제목"}; 
        DefaultTableModel tableModel = new DefaultTableModel(headers, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        List<MyPagePost> filteredList = dummyPosts.stream()
            .filter(post -> {
                if (title.equals("작성 게시글")) return post.writer.equals(userNickname);
                if (title.equals("댓글 단 게시글")) return post.comments > 0; 
                if (title.equals("좋아요 누른 게시글")) return post.likes > 10; 
                return false;
            }).collect(Collectors.toList());
        for (MyPagePost post : filteredList) { tableModel.addRow(new Object[]{post.title}); }
        JTable activityTable = new JTable(tableModel);
        styleTable(activityTable);
        activityTable.getColumnModel().getColumn(0).setPreferredWidth(550); 
        activityTable.getColumnModel().getColumn(0).setCellRenderer(new CenterRenderer());
        activityTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = activityTable.getSelectedRow();
                    if (row != -1) showCustomAlertPopup("게시글 이동", "글 상세 화면으로 이동합니다.");
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(activityTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRentalListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel titleLabel = new JLabel("물품 대여 기록", SwingConstants.LEFT);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f)); 
        titleLabel.setForeground(BROWN);
        panel.add(titleLabel, BorderLayout.NORTH);
        String[] headers = {"물품 이름", "반납 기한/상태"};
        DefaultTableModel tableModel = new DefaultTableModel(headers, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        for (RentalItem item : dummyRentals) {
            String status = item.isReturned ? "반납 완료" : item.returnDate;
            tableModel.addRow(new Object[]{item.itemName, status});
        }
        JTable rentalTable = new JTable(tableModel);
        styleTable(rentalTable);
        rentalTable.getColumnModel().getColumn(0).setPreferredWidth(300); 
        rentalTable.getColumnModel().getColumn(1).setPreferredWidth(250); 
        rentalTable.getColumnModel().getColumn(0).setCellRenderer(new CenterRenderer());
        rentalTable.getColumnModel().getColumn(1).setCellRenderer(new RentalStatusRenderer());
        JScrollPane scrollPane = new JScrollPane(rentalTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSpaceRentalListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel titleLabel = new JLabel("공간 대여 기록", SwingConstants.LEFT);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f)); 
        titleLabel.setForeground(BROWN);
        panel.add(titleLabel, BorderLayout.NORTH);
        String[] headers = {"빌린 방", "대여 일자", "인원", "상태/취소"}; 
        DefaultTableModel tableModel = new DefaultTableModel(headers, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        for (SpaceRentalItem item : dummySpaceRentals) { tableModel.addRow(new Object[]{item.roomName, item, item.headcount, item.status}); }
        JTable spaceRentalTable = new JTable(tableModel);
        styleTable(spaceRentalTable);
        spaceRentalTable.getColumnModel().getColumn(0).setPreferredWidth(100); 
        spaceRentalTable.getColumnModel().getColumn(1).setPreferredWidth(200); 
        spaceRentalTable.getColumnModel().getColumn(2).setPreferredWidth(50);  
        spaceRentalTable.getColumnModel().getColumn(3).setPreferredWidth(120); 
        spaceRentalTable.getColumnModel().getColumn(0).setCellRenderer(new CenterRenderer());
        spaceRentalTable.getColumnModel().getColumn(1).setCellRenderer(new SpaceDateTimeRenderer()); 
        spaceRentalTable.getColumnModel().getColumn(2).setCellRenderer(new CenterRenderer());
        spaceRentalTable.getColumnModel().getColumn(3).setCellRenderer(new SpaceActionRenderer());
        setupSpaceRentalCancelListener(spaceRentalTable, tableModel);
        JScrollPane scrollPane = new JScrollPane(spaceRentalTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createEventListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel titleLabel = new JLabel("과 행사 참여 기록", SwingConstants.LEFT);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f)); 
        titleLabel.setForeground(BROWN);
        panel.add(titleLabel, BorderLayout.NORTH);
        String[] headers = {"행사 제목", "행사 일정", "상태/취소"};
        DefaultTableModel tableModel = new DefaultTableModel(headers, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        for (EventParticipationItem item : dummyEvents) { tableModel.addRow(new Object[]{item.eventTitle, item, item.status}); }
        JTable eventTable = new JTable(tableModel);
        styleTable(eventTable);
        eventTable.getColumnModel().getColumn(0).setPreferredWidth(250); 
        eventTable.getColumnModel().getColumn(1).setPreferredWidth(180); 
        eventTable.getColumnModel().getColumn(2).setPreferredWidth(100); 
        eventTable.getColumnModel().getColumn(0).setCellRenderer(new CenterRenderer()); 
        eventTable.getColumnModel().getColumn(1).setCellRenderer(new EventScheduleRenderer()); 
        eventTable.getColumnModel().getColumn(2).setCellRenderer(new EventActionRenderer()); 
        setupEventCancelListener(eventTable, tableModel);
        JScrollPane scrollPane = new JScrollPane(eventTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void setupSpaceRentalCancelListener(JTable table, DefaultTableModel tableModel) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col == 3 && row >= 0 && row < dummySpaceRentals.size()) {
                    SpaceRentalItem item = dummySpaceRentals.get(row);
                    if (item.status == ReservationStatus.CANCELLABLE) {
                        String confirmMsg = "'" + item.roomName + " (" + item.reservationDate + ")' 예약을 취소하시겠습니까?";
                        showCustomConfirmPopup(confirmMsg, () -> {
                            item.status = ReservationStatus.USER_CANCELLED;
                            tableModel.setValueAt(item.status, row, 3);
                            tableModel.fireTableDataChanged(); 
                            showCustomAlertPopup("취소 완료", item.roomName + " 예약이\n취소 완료되었습니다.");
                        });
                    }
                }
            }
        });
    }

    private void setupEventCancelListener(JTable table, DefaultTableModel tableModel) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col == 2 && row >= 0 && row < dummyEvents.size()) {
                    EventParticipationItem item = dummyEvents.get(row);
                    if (item.status == ReservationStatus.CANCELLABLE) {
                        String confirmMsg = "'" + item.eventTitle + " (" + item.eventDate + ")' 참여를 취소하시겠습니까?";
                        showCustomConfirmPopup(confirmMsg, () -> {
                            item.status = ReservationStatus.USER_CANCELLED;
                            tableModel.setValueAt(item.status, row, 2);
                            tableModel.fireTableDataChanged();
                            showCustomAlertPopup("참여 취소 완료", item.eventTitle + " 참여가\n취소 완료되었습니다.");
                        });
                    }
                }
            }
        });
    }

    private void showPasswordChangePopup() {
        JDialog dialog = new JDialog(this, "비밀번호 수정", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(500, 450); 
        dialog.setLocationRelativeTo(this);
        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);
        int y = 30;
        JLabel title = new JLabel("비밀번호 수정", SwingConstants.CENTER);
        title.setFont(uiFont.deriveFont(Font.BOLD, 20f));
        title.setForeground(BROWN);
        title.setBounds(10, y, 480, 30);
        panel.add(title);
        y += 50;
        JPasswordField currentPwdField = createPasswordField(panel, "현재 비밀번호:", y);
        y += 60;
        JPasswordField newPwdField = createPasswordField(panel, "수정할 비밀번호:", y);
        y += 60;
        JPasswordField confirmPwdField = createPasswordField(panel, "비밀번호 확인:", y);
        y += 80;
        JButton saveBtn = createPopupBtn("비밀번호 변경");
        saveBtn.setBounds(100, y, 150, 45);
        saveBtn.addActionListener(e -> {
            String current = new String(currentPwdField.getPassword());
            String newPwd = new String(newPwdField.getPassword());
            String confirmPwd = new String(confirmPwdField.getPassword());
            if (!current.equals(userPassword)) showCustomAlertPopup("오류", "현재 비밀번호가 일치하지 않습니다.");
            else if (newPwd.isEmpty() || confirmPwd.isEmpty()) showCustomAlertPopup("오류", "새 비밀번호를 모두 입력해주세요.");
            else if (!newPwd.equals(confirmPwd)) showCustomAlertPopup("오류", "새 비밀번호와 확인이 일치하지 않습니다.");
            else if (newPwd.length() < 6) showCustomAlertPopup("오류", "비밀번호는 6자 이상이어야 합니다.");
            else { userPassword = newPwd; dialog.dispose(); showCustomAlertPopup("변경 완료", "비밀번호가 성공적으로 변경되었습니다."); }
        });
        panel.add(saveBtn);
        JButton cancelBtn = createPopupBtn("취소");
        cancelBtn.setBounds(260, y, 120, 45);
        cancelBtn.addActionListener(e -> dialog.dispose());
        panel.add(cancelBtn);
        dialog.setVisible(true);
    }

    private JPasswordField createPasswordField(JPanel panel, String labelText, int y) {
        JLabel label = new JLabel(labelText, SwingConstants.LEFT);
        label.setFont(uiFont.deriveFont(16f));
        label.setForeground(BROWN);
        label.setBounds(50, y, 150, 30);
        panel.add(label);
        JPasswordField field = new JPasswordField(15);
        field.setFont(uiFont.deriveFont(16f));
        field.setBounds(200, y, 200, 30);
        panel.add(field);
        return field;
    }

    private void showNicknameEditPopup() {
        JDialog dialog = new JDialog(this, "닉네임 수정", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 350); 
        dialog.setLocationRelativeTo(this);
        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);
        JLabel msgLabel = new JLabel("새 닉네임을 입력하세요.", SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(18f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 70, 360, 60); 
        panel.add(msgLabel);
        JTextField inputField = new JTextField(userNickname);
        inputField.setFont(uiFont.deriveFont(16f));
        inputField.setBounds(50, 140, 300, 40);
        panel.add(inputField);
        JButton saveBtn = createPopupBtn("저장");
        saveBtn.setBounds(60, 220, 120, 45); 
        saveBtn.addActionListener(e -> {
            String newNickname = inputField.getText().trim();
            if (newNickname.isEmpty() || newNickname.length() > 10) { JOptionPane.showMessageDialog(dialog, "닉네임은 1자 이상 10자 이내로 입력해주세요."); return; }
            userNickname = newNickname; nicknameLabel.setText(userNickname);
            dialog.dispose(); showCustomAlertPopup("성공", "닉네임이 성공적으로 변경되었습니다.");
        });
        panel.add(saveBtn);
        JButton cancelBtn = createPopupBtn("취소");
        cancelBtn.setBounds(220, 220, 120, 45); 
        cancelBtn.addActionListener(e -> dialog.dispose());
        panel.add(cancelBtn);
        dialog.setVisible(true);
    }

    private JPanel createPlaceholderPanel(String title, String message) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel titleLabel = new JLabel(title, SwingConstants.LEFT);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(BROWN);
        panel.add(titleLabel, BorderLayout.NORTH);
        JLabel msgLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>", SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(18f));
        msgLabel.setForeground(Color.GRAY);
        panel.add(msgLabel, BorderLayout.CENTER);
        return panel;
    }

    private void showLogoutPopup() {
        JDialog dialog = new JDialog(this, "로그아웃", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 300); 
        dialog.setLocationRelativeTo(this);
        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);
        JLabel msgLabel = new JLabel("로그아웃 하시겠습니까?", SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(18f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 70, 360, 60); 
        panel.add(msgLabel);
        JButton yesBtn = createPopupBtn("네");
        yesBtn.setBounds(60, 180, 120, 45); 
        yesBtn.addActionListener(e -> {
            dialog.dispose();
            
            // [수정] 로그인 화면으로 이동
            new LoginFrame();
            
            dispose();
        });
        panel.add(yesBtn);
        JButton noBtn = createPopupBtn("아니오");
        noBtn.setBounds(220, 180, 120, 45); 
        noBtn.addActionListener(e -> dialog.dispose());
        panel.add(noBtn);
        dialog.setVisible(true);
    }

    private void showCustomAlertPopup(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 350); 
        dialog.setLocationRelativeTo(this);
        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);
        JTextArea msgArea = new JTextArea(message);
        msgArea.setFont(uiFont.deriveFont(18f));
        msgArea.setForeground(BROWN);
        msgArea.setOpaque(false);
        msgArea.setEditable(false);
        msgArea.setHighlighter(null);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setBounds(30, 60, 340, 80);
        panel.add(msgArea);
        JButton okBtn = createPopupBtn("확인");
        okBtn.setBounds(135, 220, 130, 45); 
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);
        dialog.setVisible(true);
    }

    private void showSimplePopup(String title, String message) {
        showCustomAlertPopup(title, message);
    }

    private void showCustomConfirmPopup(String message, Runnable onConfirm) {
        JDialog dialog = new JDialog(this, "확인", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 350); 
        dialog.setLocationRelativeTo(this);
        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);
        JTextArea msgArea = new JTextArea(message);
        msgArea.setFont(uiFont.deriveFont(18f));
        msgArea.setForeground(BROWN);
        msgArea.setOpaque(false);
        msgArea.setEditable(false);
        msgArea.setHighlighter(null);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setBounds(30, 60, 340, 80); 
        panel.add(msgArea);
        JButton yesBtn = createPopupBtn("확인"); 
        yesBtn.setBounds(60, 220, 120, 45); 
        yesBtn.addActionListener(e -> {
            dialog.dispose();
            onConfirm.run();
        });
        panel.add(yesBtn);
        JButton noBtn = createPopupBtn("취소"); 
        noBtn.setBounds(220, 220, 120, 45); 
        noBtn.addActionListener(e -> dialog.dispose());
        panel.add(noBtn);
        dialog.setVisible(true);
    }

    private JPanel createPopupPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(POPUP_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(BROWN);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 30, 30);
            }
        };
    }

    private JButton createPopupBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(16f));
        btn.setBackground(BROWN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(15, BROWN, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createStyledButton(String text, int w, int h) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(14f));
        btn.setBackground(BROWN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(15, BROWN, 1));
        btn.setPreferredSize(new Dimension(w, h)); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(uiFont.deriveFont(16f));
        label.setForeground(BROWN);
        return label;
    }

    class MyPageListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String text = (String) value;
            label.setFont(uiFont.deriveFont(16f));
            label.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            if (text.equals("나의 활동") || text.equals("이용 기록")) {
                label.setFont(uiFont.deriveFont(Font.BOLD, 18f));
                label.setBackground(new Color(240, 240, 240)); 
                label.setForeground(BROWN);
                label.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, BORDER_COLOR));
            } else if (text.equals("--- 분리선 ---")) {
                label.setText("");
                label.setBackground(Color.WHITE);
                label.setBorder(BorderFactory.createMatteBorder(5, 0, 0, 0, BG_MAIN)); 
                label.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            } else {
                label.setForeground(BROWN);
                if (isSelected) label.setBackground(HIGHLIGHT_YELLOW); else label.setBackground(Color.WHITE);
            }
            return label;
        }
    }

    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(200, 200, 200);
            this.trackColor = new Color(245, 245, 245);
        }
        @Override
        protected JButton createDecreaseButton(int orientation) { 
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            return btn;
        }
        @Override
        protected JButton createIncreaseButton(int orientation) { 
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            return btn;
        }
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g;
            if (!c.isEnabled()) return;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 8, 8);
        }
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }

    // [추가] 테이블 스타일링 메서드 (누락분 추가)
    private void styleTable(JTable table) {
        table.setFont(uiFont.deriveFont(16f)); 
        table.setRowHeight(30);
        table.setSelectionBackground(HIGHLIGHT_YELLOW);
        table.setSelectionForeground(BROWN);
        table.setGridColor(new Color(230, 230, 230));
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(uiFont.deriveFont(18f)); 
        header.setBackground(HEADER_YELLOW);
        header.setForeground(BROWN);
        header.setPreferredSize(new Dimension(0, 35));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BROWN));

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
        }
    }

    // [추가] 네비게이션 버튼 생성 메서드 (누락분 추가)
    private JButton createNavButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(16f));
        btn.setForeground(BROWN);
        btn.setBackground(isActive ? HIGHLIGHT_YELLOW : NAV_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (!isActive) {
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { btn.setBackground(HIGHLIGHT_YELLOW); }
                @Override
                public void mouseExited(MouseEvent e) { btn.setBackground(NAV_BG); }
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (text.equals("마이페이지")) return; 
                    
                    if (text.equals("과행사")) {
                        new EventListFrame(); dispose();
                    } else if (text.equals("물품대여")) { new ItemListFrame(); dispose(); }
                    else if (text.equals("공간대여")) {
                        new SpaceRentFrame(); dispose();
                    } else if (text.equals("빈 강의실")) {
                        new EmptyClassFrame(); dispose();
                    } else if (text.equals("커뮤니티")) {
                        new CommunityFrame(); dispose();
                    } else {
                        showSimplePopup("알림", "[" + text + "] 화면은 준비 중입니다.");
                    }
                }
            });
        }
        return btn;
    }

    private static class RoundedBorder implements Border {
        private int radius; private Color color; private int thickness;
        public RoundedBorder(int r, Color c, int t) { radius = r; color = c; thickness = t; }
        public Insets getBorderInsets(Component c) { return new Insets(radius/2, radius/2, radius/2, radius/2); }
        public boolean isBorderOpaque() { return false; }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g; 
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MyPageFrame::new);
    }
}