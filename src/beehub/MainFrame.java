package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {

    // ===============================
    // 🎨 컬러 테마
    // ===============================
    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color NAV_BG = new Color(255, 255, 255);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(89, 60, 28);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157);
    private static final Color POPUP_BG = new Color(255, 250, 205);

    // ===============================
    // 🔤 폰트 설정
    // ===============================
    private static Font uiFont;

    static {
        try {
            InputStream is = MainFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) {
                uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            } else {
                Font base = Font.createFont(Font.TRUETYPE_FONT, is);
                uiFont = base.deriveFont(14f);
            }
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }

    // 사용자 정보 (추후 DB 연동 시 이 변수 변경)
    private String userName = "사용자";
    private int userPoint = 100;

    private JLabel userInfoText;
    private JLabel notiText1;
    private JLabel notiText2;
    private JPanel schedulePanel;
    private JLabel todayHeaderLabel; 

    public MainFrame() {
        setTitle("서울여대 꿀단지 - 메인");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initUI();
        refreshData();
        
        setVisible(true);
    }

    private void initUI() {
        // --- 상단 헤더 ---
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(null);
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

        // [수정] 프로필 아이콘 제거, 텍스트만 표시
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 25));
        userInfoPanel.setBounds(400, 0, 380, 80);
        userInfoPanel.setOpaque(false);

        userInfoText = new JLabel("[" + userName + "]님 | 보유 꿀 : " + userPoint + " | 로그아웃"); 
        userInfoText.setFont(uiFont.deriveFont(14f));
        userInfoText.setForeground(BROWN);
        userInfoText.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 로그아웃 팝업 연결
        userInfoText.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                showLogoutPopup();
            }
        });

        userInfoPanel.add(userInfoText);
        headerPanel.add(userInfoPanel);

        // --- 네비게이션 ---
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new GridLayout(1, 6));
        navPanel.setBounds(0, 80, 800, 50);
        navPanel.setBackground(NAV_BG);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        add(navPanel);

        String[] menus = {"물품대여", "간식행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"};
        for (String menu : menus) {
            JButton menuBtn = createNavButton(menu);
            navPanel.add(menuBtn);
        }

        // --- 메인 컨텐츠 ---
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(null);
        contentPanel.setBounds(0, 130, 800, 470);
        contentPanel.setBackground(BG_MAIN);
        add(contentPanel);

        JLabel beeIcon = new JLabel();
        String imgPath = "resource/img/login-bee.png"; 
        ImageIcon originalIcon = new ImageIcon(imgPath);
        if (originalIcon.getIconWidth() > 0) {
            Image img = originalIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            beeIcon.setIcon(new ImageIcon(img));
        } else {
            beeIcon.setText("🐝");
            beeIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        }
        beeIcon.setBounds(50, 30, 50, 50);
        contentPanel.add(beeIcon);

        JLabel notiTitle = new JLabel("일정 알리미");
        notiTitle.setFont(uiFont.deriveFont(24f));
        notiTitle.setForeground(BROWN);
        notiTitle.setBounds(110, 40, 200, 30);
        contentPanel.add(notiTitle);

        // [TODAY 알림 박스]
        JPanel todayPanel = new JPanel();
        todayPanel.setLayout(null);
        todayPanel.setBounds(50, 90, 700, 150);
        todayPanel.setBackground(Color.WHITE);
        todayPanel.setBorder(new RoundedBorder(20, BROWN, 2));
        contentPanel.add(todayPanel);

        JPanel todayHeader = new JPanel();
        todayHeader.setBounds(2, 2, 696, 40);
        todayHeader.setBackground(HIGHLIGHT_YELLOW);
        todayHeader.setLayout(null);
        
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

        // [일정 리스트 패널]
        schedulePanel = new JPanel();
        schedulePanel.setLayout(null);
        schedulePanel.setBackground(BG_MAIN);

        JScrollPane scrollPane = new JScrollPane(schedulePanel);
        scrollPane.setBounds(50, 260, 700, 190);
        scrollPane.setBorder(null); 
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); 
        contentPanel.add(scrollPane);
    }

    private void refreshData() {
        userInfoText.setText("[" + userName + "]님 | 보유 꿀 : " + userPoint + " | 로그아웃");

        String todayDate = "12월 5일";
        
        List<ScheduleItem> allSchedules = new ArrayList<>();
        allSchedules.add(new ScheduleItem("12월 5일", "노트북", "RETURN", 0)); 
        allSchedules.add(new ScheduleItem("12월 5일", "총학생회 간식행사", "SNACK", 15));
        
        allSchedules.add(new ScheduleItem("12월 6일", "보조배터리", "RETURN", 0));
        allSchedules.add(new ScheduleItem("12월 6일", "소융의 밤 행사", "EVENT", 50));
        allSchedules.add(new ScheduleItem("12월 20일", "종강 파티", "EVENT", 0));
        allSchedules.add(new ScheduleItem("12월 25일", "크리스마스 행사", "EVENT", 0));
        
        List<ScheduleItem> todayItems = allSchedules.stream()
                .filter(item -> item.date.equals(todayDate))
                .collect(Collectors.toList());

        if (!todayItems.isEmpty()) {
            ScheduleItem highlightItem = null;
            for(ScheduleItem item : todayItems) {
                if(item.type.equals("SNACK")) { highlightItem = item; break; }
            }
            if(highlightItem == null) {
                for(ScheduleItem item : todayItems) {
                    if(item.type.equals("RETURN")) { highlightItem = item; break; }
                }
            }
            if(highlightItem == null) highlightItem = todayItems.get(0);

            todayHeaderLabel.setText(todayDate + " TODAY");
            if (highlightItem.type.equals("SNACK")) {
                notiText1.setText(highlightItem.title + "가 진행 중입니다!");
                notiText2.setText("(남은 수량 : " + highlightItem.count + "개)");
            } else if (highlightItem.type.equals("RETURN")) {
                notiText1.setText("'" + highlightItem.title + "' 반납일입니다.");
                notiText2.setText("잊지 말고 반납해주세요!");
            } else {
                notiText1.setText(highlightItem.title + "가 있습니다.");
                notiText2.setText("");
            }
        } else {
            todayHeaderLabel.setText(todayDate + " TODAY");
            notiText1.setText("오늘 예정된 주요 행사가 없습니다.");
            notiText2.setText("");
        }

        List<ScheduleItem> futureItems = allSchedules.stream()
                .filter(item -> !item.date.equals(todayDate))
                .collect(Collectors.toList());

        schedulePanel.removeAll();
        int yPos = 0;
        
        for (ScheduleItem item : futureItems) {
            String displayContent = "";
            if (item.type.equals("RETURN")) {
                displayContent = "'" + item.title + "' 반납";
            } else {
                displayContent = item.title; 
            }
            addScheduleItem(schedulePanel, item.date, displayContent, yPos);
            yPos += 45; 
        }
        
        schedulePanel.setPreferredSize(new Dimension(680, yPos));
        schedulePanel.revalidate();
        schedulePanel.repaint();
    }

    class ScheduleItem {
        String date;
        String title;
        String type; 
        int count;  

        public ScheduleItem(String date, String title, String type, int count) {
            this.date = date;
            this.title = title;
            this.type = type;
            this.count = count;
        }
    }

    // [수정] 네비게이션 연결 로직
    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(16f));
        btn.setForeground(BROWN);
        btn.setBackground(NAV_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(HIGHLIGHT_YELLOW); }
            public void mouseExited(MouseEvent e) { btn.setBackground(NAV_BG); }
            public void mouseClicked(MouseEvent e) {
                if (text.equals("마이페이지")) {
                    new MyPageFrame(); // [수정] MyPageFrame 열기
                    dispose(); // 현재 창 닫기
                    return;
                }
                
                if (text.equals("공간대여")) {
                    new SpaceRentFrame(); dispose();
                } else if (text.equals("간식행사")) {
                    new EventListFrame(); dispose();
                } else if (text.equals("물품대여")) {
                    new ItemListFrame(); dispose();
                } else if (text.equals("커뮤니티")) {
                    new CommunityFrame(); dispose();
                } else {
                    showSimplePopup("알림", "[" + text + "] 화면은 준비 중입니다.");
                }
            }
        });
        return btn;
    }

    private void addScheduleItem(JPanel panel, String date, String content, int y) {
        JLabel dateLabel = new JLabel(date);
        dateLabel.setFont(uiFont.deriveFont(16f));
        dateLabel.setForeground(BROWN);
        dateLabel.setBounds(10, y, 100, 30);
        dateLabel.setHorizontalAlignment(SwingConstants.LEFT); 
        
        JLabel barLabel = new JLabel("|");
        barLabel.setFont(uiFont.deriveFont(16f));
        barLabel.setForeground(Color.LIGHT_GRAY);
        barLabel.setBounds(110, y, 20, 30);
        barLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel contentLabel = new JLabel(content);
        contentLabel.setFont(uiFont.deriveFont(18f));
        contentLabel.setForeground(BROWN); 
        contentLabel.setBounds(135, y, 530, 30); 

        panel.add(dateLabel);
        panel.add(barLabel);
        panel.add(contentLabel);
    }

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

    // [수정] 로그아웃 팝업 (네/아니오)
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
            // new LoginFrame(); // 로그인 화면으로 이동
            dispose();
        });
        panel.add(yesBtn);

        JButton noBtn = createPopupBtn("아니오");
        noBtn.setBounds(220, 150, 120, 45);
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

    private static class RoundedBorder implements Border {
        private int radius;
        private Color color;
        private int thickness;
        public RoundedBorder(int r, Color c, int t) { 
            radius = r; color = c; thickness = t;
        }
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
        SwingUtilities.invokeLater(MainFrame::new);
    }
}