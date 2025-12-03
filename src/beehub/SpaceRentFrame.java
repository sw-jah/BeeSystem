package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class SpaceRentFrame extends JFrame {

    // ===============================
    // 🎨 컬러 및 폰트 설정
    // ===============================
    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color NAV_BG = new Color(255, 255, 255);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(89, 60, 28);
    private static final Color LIGHT_BROWN = new Color(160, 120, 80);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157); // [중요] 노란색 통일
    private static final Color BORDER_COLOR = new Color(220, 220, 220);
    private static final Color POPUP_BG = new Color(255, 250, 205);
    
    private static final Color BTN_OFF_BG = new Color(250, 250, 250);
    private static final Color BTN_ON_BG = BROWN;
    private static final Color BTN_ON_FG = Color.WHITE;
    private static final Color BTN_OFF_FG = new Color(100, 100, 100);

    private static Font uiFont;
    static {
        try {
            InputStream is = SpaceRentFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) { uiFont = new Font("맑은 고딕", Font.PLAIN, 14); }
    }

    // 사용자 정보
    private String userName = "게스트";
    private String userId = "";
    private int userPoint = 0;

    // 데이터
    private String[] spaces = { "-- 공간을 선택해주세요 --", "=== 세미나실 (2~6인) ===", "세미나실 A", "세미나실 B", "세미나실 C", "세미나실 D", "세미나실 E", "세미나실 F", "=== 실습실 (2~6인) ===", "실습실 A", "실습실 B", "실습실 C", "실습실 D", "실습실 E", "실습실 F" };
    private String[] timeLabels = { "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00" };
    private Map<String, List<String>> bookedDatabase = new HashMap<>();
    
    // UI 컴포넌트
    private JComboBox<String> spaceCombo;
    private JComboBox<Integer> yearCombo, monthCombo, dayCombo;
    private JPanel partnerContainer; 
    
    // 동반인 관리 리스트 (이름+학번 쌍)
    private List<PartnerEntry> partnerEntries = new ArrayList<>();
    
    private ArrayList<JToggleButton> timeButtons = new ArrayList<>();
    private int selectedTimeCount = 0;
    
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
            userId = currentUser.getId();
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

    // [헤더 및 네비게이션]
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

        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 25));
        userInfoPanel.setBounds(450, 0, 380, 80);
        userInfoPanel.setOpaque(false);

        JLabel userInfoText = new JLabel("[" + userName + "]님" +  " | 로그아웃");
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

        String[] menus = {"물품대여", "과행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"};
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

        // === LEFT PANEL (일시 선택) ===
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

        // === RIGHT PANEL (예약자 정보) ===
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

        // 신청자 정보 (자동 입력 & 수정 불가)
        addLabel(rightPanel, "신청자 학번 (자동 입력)", 105);
        myIdField = new JTextField(userId);
        myIdField.setFont(uiFont.deriveFont(16f));
        myIdField.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(10, BORDER_COLOR, 1), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        myIdField.setBounds(25, 130, 340, 40);
        myIdField.setBackground(new Color(245, 245, 245));
        myIdField.setEditable(false); // 수정 불가
        rightPanel.add(myIdField);

        addLabel(rightPanel, "신청자 이름 (자동 입력)", 185);
        myNameField = new JTextField(userName);
        myNameField.setFont(uiFont.deriveFont(16f));
        myNameField.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(10, BORDER_COLOR, 1), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        myNameField.setBounds(25, 210, 340, 40);
        myNameField.setBackground(new Color(245, 245, 245));
        myNameField.setEditable(false); // 수정 불가
        rightPanel.add(myNameField);

        JLabel partnerLabel = new JLabel("동반인 정보 (최대 5명)");
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
        addPartnerBtn.addActionListener(e -> addPartnerRow());
        rightPanel.add(addPartnerBtn);

        partnerContainer = new JPanel();
        partnerContainer.setLayout(new BoxLayout(partnerContainer, BoxLayout.Y_AXIS));
        partnerContainer.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(partnerContainer);
        scrollPane.setBounds(25, 295, 340, 80); 
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rightPanel.add(scrollPane);

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

    // 동반인 추가 로직
    private void addPartnerRow() {
        if (partnerEntries.size() >= 5) {
            showSimplePopup("알림", "동반인은 최대 5명까지만 가능합니다.");
            return;
        }

        JPanel row = new JPanel(new GridLayout(1, 2, 5, 0));
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(340, 40));
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JTextField nameField = new JTextField("이름");
        styleTextField(nameField);
        addPlaceholderEffect(nameField, "이름");

        JTextField idField = new JTextField("학번");
        styleTextField(idField);
        addPlaceholderEffect(idField, "학번");

        row.add(nameField);
        row.add(idField);

        partnerEntries.add(new PartnerEntry(nameField, idField, row));
        
        partnerContainer.add(row);
        partnerContainer.revalidate();
        partnerContainer.repaint();
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(uiFont.deriveFont(14f));
        tf.setForeground(Color.GRAY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10, BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    private void addPlaceholderEffect(JTextField tf, String placeholder) {
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setText(placeholder);
                    tf.setForeground(Color.GRAY);
                }
            }
        });
    }

    // 예약 처리 로직
    private void handleRentAction() {
        if (admin.PenaltyManager.isBanned(userId)) {
            LocalDate banDate = admin.PenaltyManager.getBanDate(userId);
            showSimplePopup("예약 불가", "🚫 패널티로 인해 예약이 정지되었습니다.\n해제일: " + banDate);
            return;
        }

        if (spaceCombo.getSelectedIndex() == 0 || ((String)spaceCombo.getSelectedItem()).startsWith("===")) {
            showSimplePopup("알림", "유효한 공간을 선택해주세요.");
            return;
        }
        if (selectedTimeCount == 0) {
            showSimplePopup("알림", "시간을 선택해주세요!");
            return;
        }

        List<String> partnerList = new ArrayList<>();
        for (PartnerEntry entry : partnerEntries) {
            String pName = entry.nameField.getText().trim();
            String pId = entry.idField.getText().trim();

            if (!pName.equals("이름") && !pName.isEmpty() && !pId.equals("학번") && !pId.isEmpty()) {
                partnerList.add(pName + "(" + pId + ")");
            } else {
                if (!pName.equals("이름") || !pId.equals("학번")) {
                    showSimplePopup("알림", "동반인의 이름과 학번을\n모두 입력해주세요.");
                    return;
                }
            }
        }

        if (partnerList.isEmpty()) {
            showSimplePopup("알림", "최소 1명 이상의 동반인이 필요합니다.");
            return;
        }

        String dateStr = yearCombo.getSelectedItem() + "월 " + dayCombo.getSelectedItem() + "일";
        String space = (String) spaceCombo.getSelectedItem();
        
        String msg = "예약 완료!\n\n" + 
                     "신청자: " + userName + "\n" +
                     "동반인: " + String.join(", ", partnerList) + "\n" +
                     "공간: " + space + "\n" +
                     "일시: " + dateStr;

        showSimplePopup("예약 성공", msg);
    }

    private class PartnerEntry {
        JTextField nameField;
        JTextField idField;
        JPanel panel;
        public PartnerEntry(JTextField n, JTextField i, JPanel p) {
            this.nameField = n; this.idField = i; this.panel = p;
        }
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

    private void updateTimeSlotAvailability() { /* 더미 로직 */ }

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

    // [수정] 버튼 활성화 색상을 HIGHLIGHT_YELLOW로 통일
    private JButton createNavButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(16f));
        // isActive일 때 HIGHLIGHT_YELLOW 사용 (기존 HIGHLIGHT 제거)
        btn.setBackground(isActive ? HIGHLIGHT_YELLOW : NAV_BG);
        btn.setForeground(BROWN);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        
        if(!isActive) {
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btn.setBackground(HIGHLIGHT_YELLOW); }
                public void mouseExited(MouseEvent e) { btn.setBackground(NAV_BG); }
                public void mouseClicked(MouseEvent e) {
                    if(text.equals("마이페이지")) { new MyPageFrame(); dispose(); }
                    else if (text.equals("공간대여")) return;
                    else if (text.equals("물품대여")) { new ItemListFrame(); dispose(); }
                    else if (text.equals("간식행사") || text.equals("과행사")) { new EventListFrame(); dispose(); }
                    else if (text.equals("커뮤니티")) { new CommunityFrame(); dispose(); }
                    else if (text.equals("빈 강의실")) { new EmptyClassFrame(); dispose(); }
                    else if (text.equals("서울여대 꿀단지")) { new MainFrame(); dispose(); }
                    else { showSimplePopup("알림", "[" + text + "] 화면은 준비 중입니다."); }
                }
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