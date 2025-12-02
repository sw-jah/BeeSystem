package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class SpaceRentFrame extends JFrame {

    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color NAV_BG = new Color(255, 255, 255); // [추가]
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(89, 60, 28);
    private static final Color LIGHT_BROWN = new Color(160, 120, 80);
    private static final Color HIGHLIGHT = new Color(255, 248, 200);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157); // [추가]
    private static final Color BORDER_COLOR = new Color(220, 220, 220);
    private static final Color POPUP_BG = new Color(255, 250, 205);
    
    private static final Color BTN_OFF_BG = new Color(250, 250, 250);
    private static final Color BTN_ON_BG = BROWN;
    private static final Color BTN_ON_FG = Color.WHITE;
    private static final Color BTN_OFF_FG = new Color(100, 100, 100);
    private static final Color BTN_DISABLED_BG = new Color(230, 230, 230);
    private static final Color BTN_DISABLED_FG = new Color(180, 180, 180);

    private static Font uiFont;
    static {
        try {
            InputStream is = SpaceRentFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) { uiFont = new Font("맑은 고딕", Font.PLAIN, 14); }
    }

    private String userName = "사용자";
    private int userPoint = 100;

    private String[] spaces = { "-- 공간을 선택해주세요 --", "=== 세미나실 (2~6인) ===", "세미나실 A", "세미나실 B", "세미나실 C", "세미나실 D", "세미나실 E", "세미나실 F", "=== 실습실 (2~6인) ===", "실습실 A", "실습실 B", "실습실 C", "실습실 D", "실습실 E", "실습실 F" };
    private String[] timeLabels = { "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00" };
    private Map<String, List<String>> bookedDatabase = new HashMap<>();
    private Map<String, Integer> myBookedHoursByDate = new HashMap<>();

    private JComboBox<String> spaceCombo;
    private JComboBox<Integer> yearCombo, monthCombo, dayCombo;
    private JPanel partnerContainer; 
    private ArrayList<JTextField> partnerFields = new ArrayList<>();
    private ArrayList<JToggleButton> timeButtons = new ArrayList<>();
    private int selectedTimeCount = 0;
    
    // 입력 필드
    private JTextField myIdField;
    private JTextField myNameField; 

    public SpaceRentFrame() {
        setTitle("서울여대 꿀단지 - 공간대여");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        // 사용자 정보 로드
        User currentUser = UserManager.getCurrentUser();
        if(currentUser != null) {
            userName = currentUser.getName();
            userPoint = currentUser.getPoints();
        }

        initDummyData();
        initHeaderAndNav();
        initContent();

        setVisible(true);
    }

    private void initDummyData() {
        LocalDate today = LocalDate.now();
        String key = "세미나실 A_" + today.getYear() + "년 " + today.getMonthValue() + "월 " + today.getDayOfMonth() + "일";
        bookedDatabase.put(key, java.util.Arrays.asList("10:00", "14:00"));
    }

    private void initHeaderAndNav() {
        JPanel headerPanel = new JPanel(null);
        headerPanel.setBounds(0, 0, 850, 80);
        headerPanel.setBackground(HEADER_YELLOW);
        add(headerPanel);

        JLabel logoLabel = new JLabel("서울여대 꿀단지");
        logoLabel.setFont(uiFont.deriveFont(32f));
        logoLabel.setForeground(BROWN);
        logoLabel.setBounds(30, 20, 300, 40);
        headerPanel.add(logoLabel);

        JLabel jarIcon = new JLabel("🍯");
        jarIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        jarIcon.setBounds(310, 25, 40, 40);
        headerPanel.add(jarIcon);

        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 25));
        userInfoPanel.setBounds(450, 0, 380, 80);
        userInfoPanel.setOpaque(false);

        JLabel userInfoText = new JLabel("[" + userName + "]님 | 보유 꿀 : " + userPoint + " | 로그아웃");
        userInfoText.setFont(uiFont.deriveFont(14f));
        userInfoText.setForeground(BROWN);
        userInfoText.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userInfoText.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showLogoutPopup(); }
        });

        userInfoPanel.add(userInfoText);
        headerPanel.add(userInfoPanel);

        JPanel navPanel = new JPanel(new GridLayout(1, 6));
        navPanel.setBounds(0, 80, 850, 50);
        navPanel.setBackground(Color.WHITE);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        add(navPanel);

        String[] menus = {"물품대여", "간식행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"};
        for (int i = 0; i < menus.length; i++) {
            JButton menuBtn = createNavButton(menus[i], i == 2);
            navPanel.add(menuBtn);
        }
    }

    private void initContent() {
        JPanel contentPanel = new JPanel(null);
        contentPanel.setBounds(0, 130, 850, 520);
        contentPanel.setBackground(BG_MAIN);
        add(contentPanel);

        JPanel leftPanel = new JPanel(null);
        leftPanel.setBounds(30, 30, 380, 430); 
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new RoundedBorder(15, BORDER_COLOR, 2));
        contentPanel.add(leftPanel);

        JLabel leftTitle = new JLabel("1. 예약 일시 선택");
        leftTitle.setFont(uiFont.deriveFont(Font.BOLD, 18f));
        leftTitle.setForeground(BROWN);
        leftTitle.setBounds(25, 25, 200, 25);
        leftPanel.add(leftTitle);

        addLabel(leftPanel, "공간 선택", 65);
        spaceCombo = new JComboBox<>(spaces);
        spaceCombo.setRenderer(new SpaceListRenderer());
        styleComboBox(spaceCombo);
        spaceCombo.setBounds(25, 90, 330, 40);
        spaceCombo.addActionListener(e -> updateTimeSlotAvailability());
        leftPanel.add(spaceCombo);

        addLabel(leftPanel, "날짜 선택", 145);
        JPanel datePanel = new JPanel(new GridLayout(1, 3, 5, 0));
        datePanel.setOpaque(false);
        datePanel.setBounds(25, 170, 330, 40);

        yearCombo = new JComboBox<>();
        monthCombo = new JComboBox<>();
        dayCombo = new JComboBox<>();
        styleComboBox(yearCombo); styleComboBox(monthCombo); styleComboBox(dayCombo);
        initDateLogic(); 
        datePanel.add(yearCombo); datePanel.add(monthCombo); datePanel.add(dayCombo);
        leftPanel.add(datePanel);

        addLabel(leftPanel, "시간 선택", 225);
        JPanel timeGridPanel = new JPanel(new GridLayout(3, 4, 6, 6)); 
        timeGridPanel.setBounds(25, 255, 330, 120);
        timeGridPanel.setOpaque(false);
        for (String time : timeLabels) {
            JToggleButton btn = createTimeButton(time);
            timeButtons.add(btn);
            timeGridPanel.add(btn);
        }
        leftPanel.add(timeGridPanel);

        JPanel rightPanel = new JPanel(null);
        rightPanel.setBounds(430, 30, 390, 430); 
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new RoundedBorder(15, BORDER_COLOR, 2));
        contentPanel.add(rightPanel);

        JLabel rightTitle = new JLabel("2. 예약자 정보");
        rightTitle.setFont(uiFont.deriveFont(Font.BOLD, 18f));
        rightTitle.setForeground(BROWN);
        rightTitle.setBounds(25, 25, 200, 25);
        rightPanel.add(rightTitle);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBounds(25, 60, 340, 30);
        JLabel info1 = new JLabel("※ "); info1.setForeground(Color.GRAY);
        JLabel info2 = new JLabel("하루 최대 3시간"); info2.setForeground(new Color(220, 50, 50)); 
        JLabel info3 = new JLabel("까지 이용 가능합니다."); info3.setForeground(Color.GRAY);
        infoPanel.add(info1); infoPanel.add(info2); infoPanel.add(info3);
        rightPanel.add(infoPanel);

        // 학번 입력
        addLabel(rightPanel, "신청자 학번 (본인)", 105);
        myIdField = new JTextField();
        if (UserManager.getCurrentUser() != null) myIdField.setText(UserManager.getCurrentUser().getId());
        myIdField.setFont(uiFont.deriveFont(16f));
        myIdField.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(10, BORDER_COLOR, 1), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        myIdField.setBounds(25, 130, 340, 40);
        myIdField.setBackground(new Color(245, 245, 245));
        rightPanel.add(myIdField);

        // 이름 입력
        addLabel(rightPanel, "신청자 이름", 185);
        myNameField = new JTextField();
        if (UserManager.getCurrentUser() != null) myNameField.setText(UserManager.getCurrentUser().getName());
        myNameField.setFont(uiFont.deriveFont(16f));
        myNameField.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(10, BORDER_COLOR, 1), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        myNameField.setBounds(25, 210, 340, 40);
        rightPanel.add(myNameField);

        JLabel partnerLabel = new JLabel("동반인 학번 (최대 5명)");
        partnerLabel.setFont(uiFont.deriveFont(14f));
        partnerLabel.setForeground(LIGHT_BROWN);
        partnerLabel.setBounds(25, 265, 200, 20);
        rightPanel.add(partnerLabel);

        JButton addPartnerBtn = new JButton("+ 추가");
        addPartnerBtn.setFont(uiFont.deriveFont(12f));
        addPartnerBtn.setForeground(BROWN);
        addPartnerBtn.setBackground(Color.WHITE);
        addPartnerBtn.setBorder(new RoundedBorder(10, BORDER_COLOR, 1));
        addPartnerBtn.setBounds(305, 260, 60, 25);
        addPartnerBtn.setFocusPainted(false);
        addPartnerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addPartnerBtn.addActionListener(e -> addPartnerField());
        rightPanel.add(addPartnerBtn);

        partnerContainer = new JPanel();
        partnerContainer.setLayout(new BoxLayout(partnerContainer, BoxLayout.Y_AXIS));
        partnerContainer.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(partnerContainer);
        scrollPane.setBounds(25, 295, 340, 80); 
        scrollPane.setBorder(null);
        rightPanel.add(scrollPane);

        addPartnerField();

        JButton rentBtn = new JButton("예약 완료");
        rentBtn.setFont(uiFont.deriveFont(20f));
        rentBtn.setBackground(BROWN);
        rentBtn.setForeground(Color.WHITE);
        rentBtn.setBounds(25, 390, 340, 45); 
        rentBtn.setFocusPainted(false);
        rentBtn.setBorder(new RoundedBorder(15, BROWN, 1));
        rentBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rentBtn.addActionListener(e -> handleRentAction());
        rightPanel.add(rentBtn);
        
        updateTimeSlotAvailability();
    }

    private void handleRentAction() {
        String inputId = myIdField.getText().trim();
        String inputName = myNameField.getText().trim();
        
        if (inputId.isEmpty() || inputName.isEmpty()) {
            showSimplePopup("알림", "학번과 이름을 모두 입력해주세요.");
            return;
        }

        // 본인 확인
        UserDAO dao = new UserDAO();
        boolean isMatch = dao.checkUserMatch(inputId, inputName);

        if (!isMatch) {
            showSimplePopup("본인 인증 실패", "학번과 이름이 일치하지 않습니다.\n다시 확인해주세요.");
            return;
        }
        
        // 정지 여부 확인 (admin 패키지 연동)
        if (admin.PenaltyManager.isBanned(inputId)) {
            LocalDate banDate = admin.PenaltyManager.getBanDate(inputId);
            showSimplePopup("예약 불가", "🚫 미입실 누적(2회)으로 인해 정지되었습니다.\n해제일: " + banDate);
            return;
        }

        int selectedIndex = spaceCombo.getSelectedIndex();
        String space = (String) spaceCombo.getSelectedItem();
        
        if (selectedIndex == 0 || space.startsWith("===")) {
            showSimplePopup("알림", "유효한 공간을 선택해주세요.");
            return;
        }

        if (selectedTimeCount == 0) {
            showSimplePopup("알림", "시간을 선택해주세요!");
            return;
        }

        String dateStr = yearCombo.getSelectedItem() + "월 " + dayCombo.getSelectedItem() + "일";
        showSimplePopup("예약 성공", space + "\n" + dateStr + " 예약되었습니다!");
    }

    private void initDateLogic() {
        LocalDate today = LocalDate.now();
        yearCombo.addItem(today.getYear());
        updateMonths(today);
        updateDays(today);
        yearCombo.addActionListener(e -> { updateMonths(today); updateTimeSlotAvailability(); });
        monthCombo.addActionListener(e -> { updateDays(today); updateTimeSlotAvailability(); });
        dayCombo.addActionListener(e -> updateTimeSlotAvailability());
    }
    
    private void updateMonths(LocalDate today) {
        monthCombo.removeAllItems();
        for(int i=today.getMonthValue(); i<=12; i++) monthCombo.addItem(i);
    }
    
    private void updateDays(LocalDate today) {
        dayCombo.removeAllItems();
        int m = (Integer) monthCombo.getSelectedItem();
        int startDay = (m == today.getMonthValue()) ? today.getDayOfMonth() : 1;
        for(int i=startDay; i<=31; i++) dayCombo.addItem(i);
    }

    private void updateTimeSlotAvailability() {
        // 더미 로직 (기존 유지)
    }

    private void addPartnerField() {
        if (partnerFields.size() >= 5) return;
        JTextField field = new JTextField("동반인 " + (partnerFields.size() + 1) + " 학번");
        field.setFont(uiFont.deriveFont(14f));
        partnerFields.add(field);
        partnerContainer.add(field);
        partnerContainer.revalidate();
    }

    private JToggleButton createTimeButton(String time) {
        JToggleButton btn = new JToggleButton(time);
        btn.setFont(uiFont.deriveFont(12f));
        btn.setBackground(BTN_OFF_BG);
        btn.addActionListener(e -> {
            if (btn.isSelected()) {
                if (selectedTimeCount >= 3) { btn.setSelected(false); } 
                else { selectedTimeCount++; btn.setBackground(BTN_ON_BG); btn.setForeground(BTN_ON_FG); }
            } else {
                selectedTimeCount--; btn.setBackground(BTN_OFF_BG); btn.setForeground(BTN_OFF_FG);
            }
        });
        return btn;
    }

    private void addLabel(JPanel p, String text, int y) {
        JLabel l = new JLabel(text);
        l.setFont(uiFont.deriveFont(14f));
        l.setForeground(LIGHT_BROWN);
        l.setBounds(25, y, 250, 20);
        p.add(l);
    }

    private void styleComboBox(JComboBox<?> box) {
        box.setFont(uiFont.deriveFont(14f));
        box.setBackground(Color.WHITE);
        box.setForeground(BROWN);
    }

    private void showSimplePopup(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showLogoutPopup() {
        int ans = JOptionPane.showConfirmDialog(this, "로그아웃 하시겠습니까?", "로그아웃", JOptionPane.YES_NO_OPTION);
        if(ans == JOptionPane.YES_OPTION) {
            UserManager.logout();
            new LoginFrame();
            dispose();
        }
    }

    private JButton createNavButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(16f));
        btn.setBackground(isActive ? HIGHLIGHT_YELLOW : NAV_BG);
        if(!isActive) {
            btn.addActionListener(e -> {
                if(text.equals("마이페이지")) { new MyPageFrame(); dispose(); }
                else if (text.equals("공간대여")) { dispose(); }
                else if (text.equals("물품대여")) { new ItemListFrame(); dispose(); }
                else if (text.equals("간식행사") || text.equals("과행사")) { new EventListFrame(); dispose(); }
                else if (text.equals("커뮤니티")) { new CommunityFrame(); dispose(); }
                else if (text.equals("빈 강의실")) { new EmptyClassFrame(); dispose(); }
                else { showSimplePopup("알림", "[" + text + "] 화면은 준비 중입니다."); }
            });
        }
        return btn;
    }
    
    class SpaceListRenderer extends BasicComboBoxRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value.toString().startsWith("--") || value.toString().startsWith("===")) {
                setBackground(Color.LIGHT_GRAY);
            }
            return this;
        }
    }

    private static class RoundedBorder implements Border {
        private int radius; private Color color; private int thickness;
        public RoundedBorder(int r, Color c, int t) { radius = r; color = c; thickness = t; }
        public Insets getBorderInsets(Component c) { return new Insets(radius/2, radius/2, radius/2, radius/2); }
        public boolean isBorderOpaque() { return false; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            g.setColor(color); g.drawRoundRect(x, y, w-1, h-1, radius, radius);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpaceRentFrame::new);
    }
}