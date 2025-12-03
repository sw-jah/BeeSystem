package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// [중요] 매니저 클래스 임포트
import council.EventManager;
import council.EventManager.EventData;
import beehub.RentManager.RentData;

public class MainFrame extends JFrame {

    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color NAV_BG = new Color(255, 255, 255);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(89, 60, 28);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157);
    private static final Color POPUP_BG = new Color(255, 250, 205);

    private static Font uiFont;
    static {
        try {
            InputStream is = MainFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) { uiFont = new Font("맑은 고딕", Font.PLAIN, 14); }
    }

    private JPanel schedulePanel;
    private JLabel todayHeaderLabel; 
    private JLabel notiText1;
    private JLabel notiText2;
    private User currentUser;

    public MainFrame() {
        // [보안 체크]
        currentUser = UserManager.getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(null, "로그인이 필요한 서비스입니다.", "알림", JOptionPane.WARNING_MESSAGE);
            new LoginFrame();
            dispose();
            return;
        }

        setTitle("서울여대 꿀단지 - 메인");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initUI();
        refreshData(); // 데이터 로드
        setVisible(true);
    }

    private void initUI() {
        // --- 헤더 ---
        JPanel headerPanel = new JPanel(null);
        headerPanel.setBounds(0, 0, 800, 80);
        headerPanel.setBackground(HEADER_YELLOW);
        add(headerPanel);

        JLabel logoLabel = new JLabel("서울여대 꿀단지");
        logoLabel.setFont(uiFont.deriveFont(32f));
        logoLabel.setForeground(BROWN);
        logoLabel.setBounds(30, 20, 300, 40);
        headerPanel.add(logoLabel);

        // 사용자 정보 & 로그아웃
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 25));
        userInfoPanel.setBounds(400, 0, 380, 80);
        userInfoPanel.setOpaque(false);

        JLabel userInfo = new JLabel("[" + currentUser.getName() + "]님 | ");
        userInfo.setFont(uiFont.deriveFont(14f));
        userInfo.setForeground(BROWN);
        userInfoPanel.add(userInfo);

        JLabel logoutBtn = new JLabel("로그아웃");
        logoutBtn.setFont(uiFont.deriveFont(14f));
        logoutBtn.setForeground(BROWN);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showLogoutPopup(); }
        });
        userInfoPanel.add(logoutBtn);
        
        headerPanel.add(userInfoPanel);

        // --- 네비게이션 ---
        JPanel navPanel = new JPanel(new GridLayout(1, 6));
        navPanel.setBounds(0, 80, 800, 50);
        navPanel.setBackground(NAV_BG);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        add(navPanel);

        String[] menus = {"물품대여", "과행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"};
        for (String menu : menus) {
            JButton menuBtn = createNavButton(menu, false); 
            navPanel.add(menuBtn);
        }

        // --- 메인 컨텐츠 ---
        JPanel contentPanel = new JPanel(null);
        contentPanel.setBounds(0, 130, 800, 470);
        contentPanel.setBackground(BG_MAIN);
        add(contentPanel);

        // 벌 아이콘
        JLabel beeIcon = new JLabel("🐝");
        try {
            java.net.URL imgUrl = getClass().getResource("/img/login-bee.png");
            if (imgUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imgUrl);
                Image img = originalIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                beeIcon.setIcon(new ImageIcon(img));
                beeIcon.setText("");
            }
        } catch(Exception e) {}
        beeIcon.setBounds(50, 30, 50, 50); 
        contentPanel.add(beeIcon);

        JLabel notiTitle = new JLabel("일정 알리비");
        notiTitle.setFont(uiFont.deriveFont(24f));
        notiTitle.setForeground(BROWN);
        notiTitle.setBounds(110, 40, 200, 30);
        contentPanel.add(notiTitle);

        // 오늘의 알림 패널
        JPanel todayPanel = new JPanel(null);
        todayPanel.setBounds(50, 90, 700, 150);
        todayPanel.setBackground(Color.WHITE);
        todayPanel.setBorder(new RoundedBorder(20, BROWN, 2));
        contentPanel.add(todayPanel);

        JPanel todayHeader = new JPanel(null);
        todayHeader.setBounds(2, 2, 696, 40);
        todayHeader.setBackground(HIGHLIGHT_YELLOW);
        
        todayHeaderLabel = new JLabel("TODAY");
        todayHeaderLabel.setFont(uiFont.deriveFont(18f));
        todayHeaderLabel.setForeground(BROWN);
        todayHeaderLabel.setBounds(20, 10, 300, 20);
        todayHeader.add(todayHeaderLabel);
        
        todayPanel.add(todayHeader); 

        notiText1 = new JLabel("오늘의 주요 일정이 없습니다.", SwingConstants.CENTER);
        notiText1.setFont(uiFont.deriveFont(20f));
        notiText1.setForeground(BROWN);
        notiText1.setBounds(0, 60, 700, 30);
        todayPanel.add(notiText1);

        notiText2 = new JLabel("", SwingConstants.CENTER);
        notiText2.setFont(uiFont.deriveFont(18f));
        notiText2.setForeground(new Color(150, 150, 150));
        notiText2.setBounds(0, 100, 700, 30);
        todayPanel.add(notiText2);

        // 스크롤 일정 목록
        schedulePanel = new JPanel(null);
        schedulePanel.setBackground(BG_MAIN);

        JScrollPane scrollPane = new JScrollPane(schedulePanel);
        scrollPane.setBounds(50, 260, 700, 190);
        scrollPane.setBorder(null); 
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        // 세련된 스크롤바 적용
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        
        contentPanel.add(scrollPane);
    }

    // ===============================================================
    // 📅 데이터 자동화 및 로직 (핵심 수정)
    // ===============================================================
    private void refreshData() {
        LocalDate today = LocalDate.now();
        String todayStr = today.getMonthValue() + "월 " + today.getDayOfMonth() + "일";
        todayHeaderLabel.setText(todayStr + " TODAY");

        List<ScheduleItem> allSchedules = new ArrayList<>();

        // 1. [물품 반납] RentManager에서 내 대여 기록 가져오기
        List<RentData> myRentals = RentManager.getAllRentals().stream()
                .filter(r -> r.renterId.equals(currentUser.getId()) && !r.isReturned)
                .collect(Collectors.toList());

        for (RentData r : myRentals) {
            allSchedules.add(new ScheduleItem(r.dueDate, r.itemName, "RETURN", 0));
        }

        // 2. [과 행사] EventManager에서 내 학과 or 전체 행사 가져오기
        List<EventData> events = EventManager.getAllEvents().stream()
                .filter(e -> e.targetDept.equals("전체") || 
                             e.targetDept.equals("총학생회") || 
                             e.targetDept.equals(currentUser.getDept()))
                .filter(e -> "진행중".equals(e.status) || "예정".equals(e.status))
                .collect(Collectors.toList());

        for (EventData e : events) {
            String type = e.title.contains("간식") ? "SNACK" : "EVENT";
            // 날짜는 종료일 기준 or 시작일 기준 (여기선 종료일 기준 디데이로 설정)
            allSchedules.add(new ScheduleItem(e.endDateTime.toLocalDate(), e.title, type, e.totalCount - e.currentCount));
        }

        // 3. [공간 예약] (SpaceManager가 없으므로 더미 데이터 1개 시뮬레이션)
        // 실제로는 SpaceManager.getMyReservations(userId) 형태로 가져와야 함
        if ("20231234".equals(currentUser.getId())) { // 특정 학번 테스트용
            allSchedules.add(new ScheduleItem(today.plusDays(1), "50주년기념관 301호 예약", "SPACE", 0));
        }

        // 날짜순 정렬
        Collections.sort(allSchedules, Comparator.comparing(item -> item.rawDate));

        // 오늘의 일정 필터링
        List<ScheduleItem> todayItems = allSchedules.stream()
                .filter(item -> item.rawDate.isEqual(today))
                .collect(Collectors.toList());

        // UI 업데이트: 오늘의 알림판
        if (!todayItems.isEmpty()) {
            // 우선순위: 간식 > 반납 > 예약 > 행사
            ScheduleItem highlight = null;
            for(ScheduleItem item : todayItems) if(item.type.equals("SNACK")) { highlight = item; break; }
            if(highlight == null) for(ScheduleItem item : todayItems) if(item.type.equals("RETURN")) { highlight = item; break; }
            if(highlight == null) highlight = todayItems.get(0);

            if (highlight.type.equals("SNACK")) {
                notiText1.setText("'" + highlight.title + "' 진행 중!");
                notiText2.setText("(선착순 마감 임박)");
            } else if (highlight.type.equals("RETURN")) {
                notiText1.setText("'" + highlight.title + "' 반납일입니다.");
                notiText2.setText("오늘 18:00까지 반납해주세요!");
            } else if (highlight.type.equals("SPACE")) {
                notiText1.setText("오늘 '" + highlight.title + "'이 있습니다.");
                notiText2.setText("잊지 말고 이용해주세요.");
            } else {
                notiText1.setText("오늘 '" + highlight.title + "' 행사가 있습니다.");
                notiText2.setText("");
            }
        } else {
            notiText1.setText("오늘 예정된 주요 일정이 없습니다.");
            notiText2.setText("편안한 하루 보내세요!");
        }

        // UI 업데이트: 하단 스크롤 목록 (오늘 이후의 일정들)
        List<ScheduleItem> futureItems = allSchedules.stream()
                .filter(item -> item.rawDate.isAfter(today))
                .collect(Collectors.toList());

        schedulePanel.removeAll();
        int yPos = 0;
        
        if (futureItems.isEmpty()) {
            JLabel emptyLabel = new JLabel("예정된 일정이 없습니다.", SwingConstants.CENTER);
            emptyLabel.setFont(uiFont.deriveFont(16f));
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setBounds(0, 20, 680, 30);
            schedulePanel.add(emptyLabel);
        } else {
            for (ScheduleItem item : futureItems) {
                String displayTitle = item.title;
                if (item.type.equals("RETURN")) displayTitle = "'" + item.title + "' 반납 예정";
                else if (item.type.equals("SPACE")) displayTitle = item.title;
                
                addScheduleItem(schedulePanel, item.getDateString(), displayTitle, yPos);
                yPos += 45; 
            }
        }
        
        schedulePanel.setPreferredSize(new Dimension(680, Math.max(yPos, 100)));
        schedulePanel.revalidate();
        schedulePanel.repaint();
    }

    class ScheduleItem {
        LocalDate rawDate;
        String title; 
        String type; // RETURN, SNACK, EVENT, SPACE
        int count;  
        
        public ScheduleItem(LocalDate d, String title, String type, int count) {
            this.rawDate = d; this.title = title; this.type = type; this.count = count;
        }
        
        public String getDateString() {
            return rawDate.getMonthValue() + "월 " + rawDate.getDayOfMonth() + "일";
        }
    }

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
                public void mouseEntered(MouseEvent e) { btn.setBackground(HIGHLIGHT_YELLOW); }
                public void mouseExited(MouseEvent e) { btn.setBackground(NAV_BG); }
                public void mouseClicked(MouseEvent e) {
                    if (text.equals("마이페이지")) { new MyPageFrame(); dispose(); }
                    else if (text.equals("공간대여")) { new SpaceRentFrame(); dispose(); }
                    else if (text.equals("과행사")) { new EventListFrame(); dispose(); }
                    else if (text.equals("물품대여")) { new ItemListFrame(); dispose(); }
                    else if (text.equals("커뮤니티")) { new CommunityFrame(); dispose(); }
                    else if (text.equals("빈 강의실")) { new EmptyClassFrame(); dispose(); }
                    else if (text.equals("서울여대 꿀단지")) { new MainFrame(); dispose(); }
                    else { showSimplePopup("알림", "[" + text + "] 화면은 준비 중입니다."); }
                }
            });
        }
        return btn;
    }

    private void addScheduleItem(JPanel panel, String date, String content, int y) {
        JLabel dateLabel = new JLabel(date);
        dateLabel.setFont(uiFont.deriveFont(16f));
        dateLabel.setForeground(BROWN);
        dateLabel.setBounds(10, y, 100, 30);
        
        JLabel barLabel = new JLabel("|");
        barLabel.setFont(uiFont.deriveFont(16f));
        barLabel.setForeground(Color.LIGHT_GRAY);
        barLabel.setBounds(110, y, 20, 30);

        JLabel contentLabel = new JLabel(content);
        contentLabel.setFont(uiFont.deriveFont(18f));
        contentLabel.setForeground(BROWN); 
        contentLabel.setBounds(135, y, 530, 30); 

        panel.add(dateLabel); panel.add(barLabel); panel.add(contentLabel);
    }

    // [수정] 팝업 디자인 통일 (JDialog 사용)
    private void showSimplePopup(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        JLabel msgLabel = new JLabel(message, SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(16f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 80, 360, 30);
        panel.add(msgLabel);

        JButton okBtn = createPopupBtn("확인");
        okBtn.setBounds(135, 160, 130, 45);
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);

        dialog.setVisible(true);
    }

    private void showLogoutPopup() {
        JDialog dialog = new JDialog(this, "로그아웃", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        JLabel msgLabel = new JLabel("로그아웃 하시겠습니까?", SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(18f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 70, 360, 30);
        panel.add(msgLabel);

        JButton yesBtn = createPopupBtn("네");
        yesBtn.setBounds(60, 150, 120, 45);
        yesBtn.addActionListener(e -> {
            dialog.dispose();
            UserManager.logout();
            new LoginFrame(); 
            dispose();
        });
        panel.add(yesBtn);

        JButton noBtn = createPopupBtn("아니오");
        noBtn.setBounds(220, 150, 120, 45);
        noBtn.addActionListener(e -> dialog.dispose());
        panel.add(noBtn);

        dialog.setVisible(true);
    }

    // --- UI Helper Classes ---
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

    private static class RoundedBorder implements Border {
        private int radius; private Color color; private int thickness;
        public RoundedBorder(int r, Color c, int t) { radius = r; color = c; thickness = t; }
        public Insets getBorderInsets(Component c) { return new Insets(radius/2, radius/2, radius/2, radius/2); }
        public boolean isBorderOpaque() { return false; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
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
            JButton btn = new JButton(); btn.setPreferredSize(new Dimension(0, 0)); return btn;
        }
        @Override
        protected JButton createIncreaseButton(int orientation) { 
            JButton btn = new JButton(); btn.setPreferredSize(new Dimension(0, 0)); return btn;
        }
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (!c.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g;
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
}