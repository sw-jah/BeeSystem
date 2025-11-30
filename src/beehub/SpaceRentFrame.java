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
import java.util.Arrays;

public class SpaceRentFrame extends JFrame {

    // ===============================
    // 🎨 테마 컬러
    // ===============================
    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(89, 60, 28);
    private static final Color LIGHT_BROWN = new Color(160, 120, 80);
    private static final Color HIGHLIGHT = new Color(255, 248, 200);
    private static final Color BORDER_COLOR = new Color(220, 220, 220);
    private static final Color POPUP_BG = new Color(255, 250, 205);

    private static final Color BTN_OFF_BG = new Color(250, 250, 250);
    private static final Color BTN_ON_BG = BROWN;
    private static final Color BTN_ON_FG = Color.WHITE;
    private static final Color BTN_OFF_FG = new Color(100, 100, 100);
    private static final Color BTN_DISABLED_BG = new Color(230, 230, 230);
    private static final Color BTN_DISABLED_FG = new Color(180, 180, 180);

    // 폰트
    private static Font uiFont;
    static {
        try {
            InputStream is = SpaceRentFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
            
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(uiFont);
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }

    // ===============================
    // 🏠 데이터 설정
    // ===============================
    private String[] spaces = {
        "-- 공간을 선택해주세요 --", 
        "=== 세미나실 (2~6인) ===", 
        "세미나실 A", "세미나실 B", "세미나실 C", "세미나실 D", "세미나실 E", "세미나실 F",
        "=== 실습실 (2~6인) ===", 
        "실습실 A", "실습실 B", "실습실 C", "실습실 D", "실습실 E", "실습실 F"
    };

    private String[] timeLabels = {
        "09:00", "10:00", "11:00", "12:00", 
        "13:00", "14:00", "15:00", "16:00",
        "17:00", "18:00", "19:00", "20:00"
    };

    // [중요] 예약된 시간 데이터 (공간+날짜 기준)
    private Map<String, List<String>> bookedDatabase = new HashMap<>();
    
    // [수정] 날짜별 내 예약 시간 합계 (날짜 기준)
    private Map<String, Integer> myBookedHoursByDate = new HashMap<>();

    // UI 컴포넌트
    private JComboBox<String> spaceCombo;
    private JComboBox<Integer> yearCombo, monthCombo, dayCombo;
    private JPanel partnerContainer; 
    private ArrayList<JTextField> partnerFields = new ArrayList<>();
    private ArrayList<JToggleButton> timeButtons = new ArrayList<>();
    private int selectedTimeCount = 0;

    public SpaceRentFrame() {
        setTitle("서울여대 꿀단지 - 공간대여");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        // 테스트 데이터 (오늘 날짜 세미나실A 10시, 14시 예약됨)
        initDummyData();

        initHeaderAndNav();
        initContent();

        setVisible(true);
    }

    private void initDummyData() {
        LocalDate today = LocalDate.now();
        String key = "세미나실 A_" + today.getYear() + "년 " + today.getMonthValue() + "월 " + today.getDayOfMonth() + "일";
        bookedDatabase.put(key, Arrays.asList("10:00", "14:00"));
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

        JLabel userInfoText = new JLabel("[이름]님 | 보유 꿀 : 100 | 로그아웃");
        userInfoText.setFont(uiFont.deriveFont(14f));
        userInfoText.setForeground(BROWN);
        userInfoText.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userInfoText.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new LoginFrame(); dispose();
            }
        });

        userInfoPanel.add(new JLabel("👤"));
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

        // ==========================================
        // [LEFT] 예약 설정 (위아래 여백 줄임)
        // ==========================================
        JPanel leftPanel = new JPanel(null);
        leftPanel.setBounds(30, 30, 380, 430); // Y위치 조정, 높이 조정
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new RoundedBorder(15, BORDER_COLOR, 2));
        contentPanel.add(leftPanel);

        // 타이틀 위치 상단으로 붙임
        JLabel leftTitle = new JLabel("1. 예약 일시 선택");
        leftTitle.setFont(uiFont.deriveFont(Font.BOLD, 18f));
        leftTitle.setForeground(BROWN);
        leftTitle.setBounds(25, 25, 200, 25);
        leftPanel.add(leftTitle);

        // 간격 좁힘 (기존 60 -> 65)
        addLabel(leftPanel, "공간 선택", 65);
        spaceCombo = new JComboBox<>(spaces);
        spaceCombo.setRenderer(new SpaceListRenderer());
        styleComboBox(spaceCombo);
        spaceCombo.setBounds(25, 90, 330, 40);
        spaceCombo.addActionListener(e -> updateTimeSlotAvailability());
        leftPanel.add(spaceCombo);

        addLabel(leftPanel, "날짜 선택 (3개월 이내)", 145);
        JPanel datePanel = new JPanel(new GridLayout(1, 3, 5, 0));
        datePanel.setOpaque(false);
        datePanel.setBounds(25, 170, 330, 40);

        yearCombo = new JComboBox<>();
        monthCombo = new JComboBox<>();
        dayCombo = new JComboBox<>();
        
        styleComboBox(yearCombo);
        styleComboBox(monthCombo);
        styleComboBox(dayCombo);
        
        initDateLogic(); 

        datePanel.add(yearCombo);
        datePanel.add(monthCombo);
        datePanel.add(dayCombo);
        leftPanel.add(datePanel);

        addLabel(leftPanel, "시간 선택", 225);
        
        JPanel timeGridPanel = new JPanel(new GridLayout(3, 4, 6, 6)); // 간격 살짝 줄임
        timeGridPanel.setBounds(25, 255, 330, 120);
        timeGridPanel.setOpaque(false);

        for (String time : timeLabels) {
            JToggleButton btn = createTimeButton(time);
            timeButtons.add(btn);
            timeGridPanel.add(btn);
        }
        leftPanel.add(timeGridPanel);

        // ==========================================
        // [RIGHT] 사용자 정보 (위아래 여백 줄임)
        // ==========================================
        JPanel rightPanel = new JPanel(null);
        rightPanel.setBounds(430, 30, 390, 430); // Y위치 조정, 높이 조정
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new RoundedBorder(15, BORDER_COLOR, 2));
        contentPanel.add(rightPanel);

        JLabel rightTitle = new JLabel("2. 예약자 정보");
        rightTitle.setFont(uiFont.deriveFont(Font.BOLD, 18f));
        rightTitle.setForeground(BROWN);
        rightTitle.setBounds(25, 25, 200, 25);
        rightPanel.add(rightTitle);

        // 안내 문구 (위치 조정)
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBounds(25, 60, 340, 30);
        
        JLabel info1 = new JLabel("※ ");
        info1.setFont(uiFont.deriveFont(13f));
        info1.setForeground(Color.GRAY);
        
        JLabel info2 = new JLabel("하루 최대 3시간");
        info2.setFont(uiFont.deriveFont(13f));
        info2.setForeground(new Color(220, 50, 50)); // Red
        
        JLabel info3 = new JLabel("까지 이용 가능합니다.");
        info3.setFont(uiFont.deriveFont(13f));
        info3.setForeground(Color.GRAY);
        
        infoPanel.add(info1);
        infoPanel.add(info2);
        infoPanel.add(info3);
        rightPanel.add(infoPanel);

        addLabel(rightPanel, "신청자 학번 (본인)", 105);
        JTextField myIdField = new JTextField("20231234");
        myIdField.setFont(uiFont.deriveFont(16f));
        myIdField.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10, BORDER_COLOR, 1), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        myIdField.setBounds(25, 130, 340, 40);
        myIdField.setEditable(false);
        myIdField.setBackground(new Color(245, 245, 245));
        rightPanel.add(myIdField);

        JLabel partnerLabel = new JLabel("동반인 학번 (최대 5명)");
        partnerLabel.setFont(uiFont.deriveFont(14f));
        partnerLabel.setForeground(LIGHT_BROWN);
        partnerLabel.setBounds(25, 185, 200, 20);
        rightPanel.add(partnerLabel);

        JButton addPartnerBtn = new JButton("+ 추가");
        addPartnerBtn.setFont(uiFont.deriveFont(12f));
        addPartnerBtn.setForeground(BROWN);
        addPartnerBtn.setBackground(Color.WHITE);
        addPartnerBtn.setBorder(new RoundedBorder(10, BORDER_COLOR, 1));
        addPartnerBtn.setBounds(305, 180, 60, 25);
        addPartnerBtn.setFocusPainted(false);
        addPartnerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addPartnerBtn.addActionListener(e -> addPartnerField());
        rightPanel.add(addPartnerBtn);

        partnerContainer = new JPanel();
        partnerContainer.setLayout(new BoxLayout(partnerContainer, BoxLayout.Y_AXIS));
        partnerContainer.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(partnerContainer);
        scrollPane.setBounds(25, 215, 340, 130); // 높이 살짝 조정
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rightPanel.add(scrollPane);

        addPartnerField();

        JButton rentBtn = new JButton("예약 완료");
        rentBtn.setFont(uiFont.deriveFont(20f));
        rentBtn.setBackground(BROWN);
        rentBtn.setForeground(Color.WHITE);
        rentBtn.setBounds(25, 365, 340, 45); // 버튼 위치 올림
        rentBtn.setFocusPainted(false);
        rentBtn.setBorder(new RoundedBorder(15, BROWN, 1));
        rentBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rentBtn.addActionListener(e -> handleRentAction());
        rightPanel.add(rentBtn);
        
        updateTimeSlotAvailability();
    }

    // ===============================
    // 📅 날짜 로직
    // ===============================
    private void initDateLogic() {
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusMonths(3); 

        yearCombo.addItem(today.getYear());
        if (maxDate.getYear() > today.getYear()) {
            yearCombo.addItem(maxDate.getYear());
        }

        ActionListener dateUpdateListener = e -> updateTimeSlotAvailability();
        yearCombo.addActionListener(e -> { updateMonths(today, maxDate); updateTimeSlotAvailability(); });
        monthCombo.addActionListener(e -> { updateDays(today, maxDate); updateTimeSlotAvailability(); });
        dayCombo.addActionListener(dateUpdateListener);

        updateMonths(today, maxDate);
        updateDays(today, maxDate);
    }

    // ✨ 핵심 기능: 예약된 시간 회색 처리 (날짜별 로직 반영)
    private void updateTimeSlotAvailability() {
        String selectedSpace = (String) spaceCombo.getSelectedItem();
        Object y = yearCombo.getSelectedItem();
        Object m = monthCombo.getSelectedItem();
        Object d = dayCombo.getSelectedItem();

        if (selectedSpace == null || y == null || m == null || d == null) return;

        String key = selectedSpace + "_" + y + "년 " + m + "월 " + d + "일";
        List<String> bookedTimes = bookedDatabase.getOrDefault(key, Collections.emptyList());

        for (JToggleButton btn : timeButtons) {
            String time = btn.getText();
            if (bookedTimes.contains(time)) {
                btn.setEnabled(false);
                btn.setBackground(BTN_DISABLED_BG);
                btn.setForeground(BTN_DISABLED_FG);
                btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                if (btn.isSelected()) {
                    btn.setSelected(false);
                    if (selectedTimeCount > 0) selectedTimeCount--;
                }
            } else {
                btn.setEnabled(true);
                if (!btn.isSelected()) {
                    btn.setBackground(BTN_OFF_BG);
                    btn.setForeground(BTN_OFF_FG);
                    btn.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
                }
            }
        }
    }

    private void updateMonths(LocalDate today, LocalDate maxDate) {
        monthCombo.removeAllItems();
        int selectedYear = (Integer) yearCombo.getSelectedItem();
        int startMonth = (selectedYear == today.getYear()) ? today.getMonthValue() : 1;
        int endMonth = (selectedYear == maxDate.getYear()) ? maxDate.getMonthValue() : 12;
        for (int i = startMonth; i <= endMonth; i++) {
            monthCombo.addItem(i);
        }
    }

    private void updateDays(LocalDate today, LocalDate maxDate) {
        if (monthCombo.getSelectedItem() == null) return;
        dayCombo.removeAllItems();
        int year = (Integer) yearCombo.getSelectedItem();
        int month = (Integer) monthCombo.getSelectedItem();
        
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1);
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int startDay = 1;
        if (year == today.getYear() && month == today.getMonthValue()) {
            startDay = today.getDayOfMonth();
        }
        if (year == maxDate.getYear() && month == maxDate.getMonthValue()) {
            lastDay = Math.min(lastDay, maxDate.getDayOfMonth());
        }
        for (int i = startDay; i <= lastDay; i++) {
            dayCombo.addItem(i);
        }
    }

    private void addPartnerField() {
        if (partnerFields.size() >= 5) {
            showSimplePopup("알림", "동반인은 최대 5명까지만 가능합니다.");
            return;
        }
        
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(340, 45));
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JTextField field = new JTextField();
        field.setFont(uiFont.deriveFont(14f));
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10, BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        field.setText("동반인 " + (partnerFields.size() + 1) + " 학번");
        field.setForeground(Color.GRAY);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().startsWith("동반인")) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText("동반인 학번 입력");
                    field.setForeground(Color.GRAY);
                }
            }
        });

        partnerFields.add(field);
        row.add(field, BorderLayout.CENTER);
        partnerContainer.add(row);
        partnerContainer.revalidate();
        partnerContainer.repaint();
    }

    private JToggleButton createTimeButton(String time) {
        JToggleButton btn = new JToggleButton(time);
        btn.setFont(uiFont.deriveFont(12f));
        btn.setBackground(BTN_OFF_BG);
        btn.setForeground(BTN_OFF_FG);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            if (btn.isSelected()) {
                if (selectedTimeCount >= 3) {
                    btn.setSelected(false);
                    showSimplePopup("경고", "하루 최대 3시간까지만\n예약 가능합니다.");
                } else {
                    selectedTimeCount++;
                    btn.setBackground(BTN_ON_BG);
                    btn.setForeground(BTN_ON_FG);
                    btn.setBorder(BorderFactory.createLineBorder(BROWN));
                }
            } else {
                selectedTimeCount--;
                btn.setBackground(BTN_OFF_BG);
                btn.setForeground(BTN_OFF_FG);
                btn.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            }
        });
        return btn;
    }

    private void handleRentAction() {
        int selectedIndex = spaceCombo.getSelectedIndex();
        String space = (String) spaceCombo.getSelectedItem();
        
        if (selectedIndex == 0 || space.startsWith("===")) {
            showSimplePopup("알림", "유효한 공간을 선택해주세요.");
            return;
        }

        // [수정] 날짜별 예약 시간 합계 체크
        String dateKey = yearCombo.getSelectedItem() + "-" + monthCombo.getSelectedItem() + "-" + dayCombo.getSelectedItem();
        int usedHours = myBookedHoursByDate.getOrDefault(dateKey, 0);

        if (usedHours + selectedTimeCount > 3) {
            showSimplePopup("이용 한도 초과", 
                "선택하신 날짜에 이미 " + usedHours + "시간을 예약하셨습니다.\n" +
                "하루 최대 3시간 규정에 의해\n추가 예약이 불가능합니다.");
            return;
        }

        if (selectedTimeCount == 0) {
            showSimplePopup("알림", "시간을 선택해주세요!");
            return;
        }

        boolean hasPartner = false;
        StringBuilder partners = new StringBuilder();
        int partnerCount = 0;
        for (JTextField f : partnerFields) {
            String val = f.getText();
            if (!val.startsWith("동반인") && !val.trim().isEmpty()) {
                partners.append(val).append(", ");
                hasPartner = true;
                partnerCount++;
            }
        }
        
        if (!hasPartner) {
            showSimplePopup("예약 불가", "최소 2인 이상(동반인 필수)\n부터 예약 가능합니다.");
            return;
        }
        if (partners.length() > 0) partners.setLength(partners.length() - 2);

        String dateStr = yearCombo.getSelectedItem() + "월 " + dayCombo.getSelectedItem() + "일";
        
        ArrayList<Integer> selectedHours = new ArrayList<>();
        for (JToggleButton btn : timeButtons) {
            if (btn.isSelected()) {
                String t = btn.getText().split(":")[0];
                selectedHours.add(Integer.parseInt(t));
            }
        }
        Collections.sort(selectedHours);
        
        StringBuilder timeStrBuilder = new StringBuilder();
        if (!selectedHours.isEmpty()) {
            int startH = selectedHours.get(0);
            int prevH = startH;
            
            for (int i = 1; i < selectedHours.size(); i++) {
                int currentH = selectedHours.get(i);
                if (currentH > prevH + 1) {
                    timeStrBuilder.append(formatTime(startH)).append(" ~ ").append(formatTime(prevH + 1)).append(" / ");
                    startH = currentH;
                }
                prevH = currentH;
            }
            timeStrBuilder.append(formatTime(startH)).append(" ~ ").append(formatTime(prevH + 1));
        }
        String timeStr = timeStrBuilder.toString();

        showSuccessPopup(space, dateStr, timeStr, (partnerCount + 1), selectedHours, dateKey);
    }

    private String formatTime(int hour) {
        String ampm = (hour < 12) ? "오전" : "오후";
        int h = (hour > 12) ? hour - 12 : hour;
        if (h == 0) h = 12; 
        return ampm + " " + h + "시";
    }

    // ===============================
    // 🎨 [수정] 단순 팝업 (JLabel 조립)
    // ===============================
    private void showSimplePopup(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(400, 250); 
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));

        JPanel panel = createPopupPanel();
        dialog.add(panel);
        panel.setLayout(null);

        String[] lines = message.split("\n");
        int yPos = 40;
        if(lines.length == 1) yPos = 60; 

        for (String line : lines) {
            JLabel lbl = new JLabel(line, SwingConstants.CENTER);
            lbl.setFont(uiFont.deriveFont(15f));
            lbl.setForeground(BROWN);
            lbl.setBounds(20, yPos, 360, 25);
            panel.add(lbl);
            yPos += 25;
        }

        JButton okBtn = createPopupBtn("확인");
        okBtn.setBounds(135, 170, 130, 40);
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);

        dialog.setVisible(true);
    }

    // ===============================
    // 🎨 [수정] 예약 완료 팝업 (JLabel 조립 + 예약 확정 로직)
    // ===============================
    private void showSuccessPopup(String space, String date, String timeRange, int totalPeople, List<Integer> hours, String dateKey) {
        JDialog dialog = new JDialog(this, "예약 완료", true);
        dialog.setSize(420, 350); 
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));

        JPanel panel = createPopupPanel();
        dialog.add(panel);
        panel.setLayout(null);

        JLabel label1 = new JLabel("예약 일자 : " + date, SwingConstants.CENTER);
        label1.setFont(uiFont.deriveFont(15f));
        label1.setForeground(BROWN);
        label1.setBounds(20, 40, 380, 25);
        panel.add(label1);

        JLabel labelTime = new JLabel(timeRange, SwingConstants.CENTER);
        labelTime.setFont(uiFont.deriveFont(15f));
        labelTime.setForeground(BROWN);
        labelTime.setBounds(20, 65, 380, 25);
        panel.add(labelTime);

        JLabel label2 = new JLabel("[" + space + "], 인원 " + totalPeople + "명 예약되었습니다.", SwingConstants.CENTER);
        label2.setFont(uiFont.deriveFont(15f));
        label2.setForeground(BROWN);
        label2.setBounds(20, 95, 380, 25);
        panel.add(label2);

        JLabel label3 = new JLabel("10분 간 입장하지 않을 시 자동 입실 취소 되며", SwingConstants.CENTER);
        label3.setFont(uiFont.deriveFont(13f));
        label3.setForeground(new Color(220, 50, 50)); 
        label3.setBounds(20, 140, 380, 20);
        panel.add(label3);

        JLabel label4 = new JLabel("경고 2회 누적 시 일주일 간 대여 불가합니다.", SwingConstants.CENTER);
        label4.setFont(uiFont.deriveFont(13f));
        label4.setForeground(new Color(220, 50, 50));
        label4.setBounds(20, 165, 380, 20);
        panel.add(label4);

        JButton okBtn = createPopupBtn("확인");
        okBtn.setBounds(135, 240, 150, 50);
        okBtn.addActionListener(e -> {
            // [중요] 예약 확정 시 데이터 저장
            Object y = yearCombo.getSelectedItem();
            Object m = monthCombo.getSelectedItem();
            Object d = dayCombo.getSelectedItem();
            String key = space + "_" + y + "년 " + m + "월 " + d + "일";
            
            // 1. 해당 공간/날짜 예약 DB 업데이트 (회색 처리용)
            List<String> bookedList = bookedDatabase.getOrDefault(key, new ArrayList<>());
            for(int h : hours) {
                String t = String.format("%02d:00", h);
                if(!bookedList.contains(t)) bookedList.add(t);
            }
            bookedDatabase.put(key, bookedList);
            
            // 2. 내 예약 시간 합계 업데이트 (3시간 제한용)
            int current = myBookedHoursByDate.getOrDefault(dateKey, 0);
            myBookedHoursByDate.put(dateKey, current + selectedTimeCount);

            dialog.dispose();
            updateTimeSlotAvailability(); // 화면 갱신
        });
        panel.add(okBtn);

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
        return btn;
    }

    class SpaceListRenderer extends BasicComboBoxRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String text = (String) value;
            if (text.startsWith("--") || text.startsWith("===")) {
                setFont(uiFont.deriveFont(Font.BOLD, 14f));
                setBackground(new Color(240, 240, 240));
                setForeground(Color.GRAY);
                setHorizontalAlignment(CENTER);
            } else {
                setFont(uiFont.deriveFont(Font.PLAIN, 14f));
                if (isSelected) {
                    setBackground(new Color(255, 248, 220));
                    setForeground(BROWN);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(Color.BLACK);
                }
                setHorizontalAlignment(LEFT);
                setText("  " + text);
            }
            return this;
        }
    }

    private void addLabel(JPanel p, String text, int y) {
        JLabel l = new JLabel(text);
        l.setFont(uiFont.deriveFont(14f));
        l.setForeground(LIGHT_BROWN);
        l.setBounds(25, y, 250, 20);
        p.add(l);
    }

    private void styleComboBox(JComboBox box) {
        box.setFont(uiFont.deriveFont(14f));
        box.setBackground(Color.WHITE);
        box.setForeground(BROWN);
        box.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        ((JComponent) box.getRenderer()).setOpaque(true);
    }

    private JButton createNavButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(16f));
        btn.setForeground(BROWN);
        btn.setBackground(isActive ? HIGHLIGHT : Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (!isActive) {
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btn.setBackground(HIGHLIGHT); }
                public void mouseExited(MouseEvent e) { btn.setBackground(Color.WHITE); }
                public void mouseClicked(MouseEvent e) {
                    if (text.equals("공간대여")) return;
                    if (text.equals("물품대여")) { new ItemListFrame(); dispose(); }
                    else if (text.equals("과행사")) { new EventListFrame(); dispose(); }
                    else if (text.equals("마이페이지")) { new MainFrame(); dispose(); }
                    else showSimplePopup("알림", "준비 중입니다.");
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
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpaceRentFrame::new);
    }
}