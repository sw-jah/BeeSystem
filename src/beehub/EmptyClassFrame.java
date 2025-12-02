package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class EmptyClassFrame extends JFrame {

    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color NAV_BG = new Color(255, 255, 255); // [추가]
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(89, 60, 28);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157); // [추가]
    private static final Color POPUP_BG = new Color(255, 250, 205); // [추가]
    private static final Color BORDER_COLOR = new Color(220, 220, 220);

    private static Font uiFont;
    static {
        try {
            InputStream is = EmptyClassFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) { uiFont = new Font("맑은 고딕", Font.PLAIN, 14); }
    }

    private DefaultTableModel tableModel;
    private JTable roomTable;
    private List<ClassRoom> allRooms = new ArrayList<>();
    
    // UI 컴포넌트 (검색용)
    private JComboBox<Integer> monthCombo, dayCombo;
    private JComboBox<String> buildingCombo, timeCombo;
    private JButton searchBtn;

    public EmptyClassFrame() {
        setTitle("서울여대 꿀단지 - 빈 강의실 찾기");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initDummyDatabase();
        initHeader();
        initNav();
        initContent();

        setVisible(true);
    }

    private void initDummyDatabase() {
        allRooms.add(new ClassRoom("50주년기념관", "301호", Arrays.asList(9, 10, 11))); 
        allRooms.add(new ClassRoom("50주년기념관", "405호", Arrays.asList(13, 14, 15))); 
        allRooms.add(new ClassRoom("제2과학관", "101호", Arrays.asList(9, 10, 11, 12, 13)));
        allRooms.add(new ClassRoom("인문사회관", "202호", Arrays.asList(11, 12, 13)));
    }

    private void initHeader() {
        JPanel headerPanel = new JPanel(null);
        headerPanel.setBounds(0, 0, 800, 80);
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
        
        // 유저 정보 표시
        User user = UserManager.getCurrentUser();
        String info = (user != null) ? "[" + user.getName() + "]님" : "[게스트]님";
        
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 25));
        userInfoPanel.setBounds(400, 0, 380, 80);
        userInfoPanel.setOpaque(false);
        
        JLabel userInfo = new JLabel(info + " | 로그아웃");
        userInfo.setFont(uiFont.deriveFont(14f));
        userInfo.setForeground(BROWN);
        userInfo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userInfo.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showLogoutPopup(); }
        });
        
        userInfoPanel.add(userInfo);
        headerPanel.add(userInfoPanel);
    }

    private void initNav() {
        JPanel navPanel = new JPanel(new GridLayout(1, 6));
        navPanel.setBounds(0, 80, 800, 50);
        navPanel.setBackground(NAV_BG);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        add(navPanel);

        String[] menus = {"물품대여", "간식행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"};
        for (String menu : menus) {
            JButton menuBtn = createNavButton(menu, menu.equals("빈 강의실"));
            navPanel.add(menuBtn);
        }
    }

    private void initContent() {
        JPanel contentPanel = new JPanel(null);
        contentPanel.setBounds(0, 130, 800, 470);
        contentPanel.setBackground(BG_MAIN);
        add(contentPanel);

        // 검색 필터 패널
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15)); 
        filterPanel.setBounds(25, 20, 750, 70);
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(new RoundedBorder(15, BORDER_COLOR));

        LocalDate today = LocalDate.now();
        
        Vector<Integer> months = new Vector<>();
        for (int i = 1; i <= 12; i++) months.add(i);
        monthCombo = new JComboBox<>(months);
        styleComboBox(monthCombo);
        monthCombo.setSelectedItem(today.getMonthValue());

        Vector<Integer> days = new Vector<>();
        for (int i = 1; i <= 31; i++) days.add(i);
        dayCombo = new JComboBox<>(days);
        styleComboBox(dayCombo);
        dayCombo.setSelectedItem(today.getDayOfMonth());

        String[] buildings = {"전체", "50주년기념관", "제2과학관", "인문사회관"};
        buildingCombo = new JComboBox<>(buildings);
        styleComboBox(buildingCombo);
        buildingCombo.setPreferredSize(new Dimension(130, 35));
        buildingCombo.setSelectedIndex(0); 

        String[] times = new String[9];
        for(int i=0; i<9; i++) {
            int start = 9 + i;
            times[i] = String.format("%02d:00 ~ %02d:00", start, start+1);
        }
        timeCombo = new JComboBox<>(times);
        styleComboBox(timeCombo);
        timeCombo.setPreferredSize(new Dimension(140, 35));

        searchBtn = createStyledButton("조회", 70, 35);
        searchBtn.setBackground(Color.WHITE);
        searchBtn.setForeground(BROWN);
        searchBtn.addActionListener(e -> searchRooms()); 

        filterPanel.add(createLabel("날짜:"));
        filterPanel.add(monthCombo);
        filterPanel.add(createLabel("월"));
        filterPanel.add(dayCombo);
        filterPanel.add(createLabel("일"));
        filterPanel.add(Box.createHorizontalStrut(15)); 
        filterPanel.add(createLabel("건물:"));
        filterPanel.add(buildingCombo);
        filterPanel.add(Box.createHorizontalStrut(5)); 
        filterPanel.add(createLabel("시간:"));
        filterPanel.add(timeCombo);
        filterPanel.add(searchBtn);

        contentPanel.add(filterPanel);

        // 테이블
        String[] headers = {"건물명", "강의실", "상태"};
        tableModel = new DefaultTableModel(headers, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        roomTable = new JTable(tableModel);
        styleTable(roomTable);
        
        JScrollPane scroll = new JScrollPane(roomTable);
        scroll.setBounds(25, 100, 750, 340);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scroll.getViewport().setBackground(Color.WHITE);
        contentPanel.add(scroll);

        // 자동 조회
        searchRooms();
    }

    private void searchRooms() {
        tableModel.setRowCount(0);
        String selectedBuilding = (String) buildingCombo.getSelectedItem();
        String timeStr = (String) timeCombo.getSelectedItem(); 
        
        if (selectedBuilding == null || timeStr == null) return;

        int selectedHour = Integer.parseInt(timeStr.split(":")[0]);
        boolean found = false;

        for (ClassRoom room : allRooms) {
            boolean isMatch = selectedBuilding.equals("전체") || room.buildingName.equals(selectedBuilding);
            if (isMatch) {
                if (!room.occupiedHours.contains(selectedHour)) {
                    tableModel.addRow(new Object[]{
                        room.buildingName, 
                        room.roomNo, 
                        "사용가능"
                    });
                    found = true;
                }
            }
        }

        if (!found) {
            String target = selectedBuilding.equals("전체") ? "모든 건물" : selectedBuilding;
            showSimplePopup("검색 결과 없음", target + " " + selectedHour + "시에는\n사용 가능한 강의실이 없습니다.");
        }
    }

    // --- 헬퍼 메서드들 ---
    
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
                    if (text.equals("빈 강의실")) return; 
                    if (text.equals("공간대여")) { new SpaceRentFrame(); dispose(); } 
                    else if (text.equals("과행사")) { new EventListFrame(); dispose(); } 
                    else if (text.equals("물품대여")) { new ItemListFrame(); dispose(); } 
                    else if (text.equals("커뮤니티")) { new CommunityFrame(); dispose(); } 
                    else if (text.equals("마이페이지")) { new MainFrame(); dispose(); } 
                    else { showSimplePopup("알림", "[" + text + "] 화면은 준비 중입니다."); }
                }
            });
        }
        return btn;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(uiFont.deriveFont(16f));
        label.setForeground(BROWN);
        return label;
    }

    private void styleComboBox(JComboBox<?> box) {
        box.setFont(uiFont.deriveFont(14f));
        box.setBackground(Color.WHITE);
        box.setForeground(BROWN);
        box.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        ((JComponent) box.getRenderer()).setOpaque(true);
    }

    private JButton createStyledButton(String text, int width, int height) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(16f));
        btn.setBackground(BROWN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(15, BROWN));
        btn.setPreferredSize(new Dimension(width, height));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleTable(JTable table) {
        table.setFont(uiFont.deriveFont(14f));
        table.setRowHeight(40);
        table.setSelectionBackground(HIGHLIGHT_YELLOW);
        table.setSelectionForeground(BROWN);
        table.setGridColor(new Color(230, 230, 230));
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(uiFont.deriveFont(16f));
        header.setBackground(HEADER_YELLOW);
        header.setForeground(BROWN);
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BROWN));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    class ClassRoom {
        String buildingName; String roomNo; List<Integer> occupiedHours;
        public ClassRoom(String b, String r, List<Integer> o) { this.buildingName = b; this.roomNo = r; this.occupiedHours = o; }
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
        btn.setBorder(new RoundedBorder(15, BROWN));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static class RoundedBorder implements Border {
        private int radius; private Color color;
        public RoundedBorder(int r, Color c) { radius = r; color = c; }
        public Insets getBorderInsets(Component c) { return new Insets(radius/2, radius/2, radius/2, radius/2); }
        public boolean isBorderOpaque() { return false; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EmptyClassFrame::new);
    }
}