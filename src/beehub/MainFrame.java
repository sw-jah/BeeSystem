package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;

public class MainFrame extends JFrame {

    // ===============================
    // 🎨 컬러 테마
    // ===============================
    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color NAV_BG = new Color(255, 255, 255);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(89, 60, 28);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157); // 메뉴바용 하이라이트

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

    // ===============================
    // 🔄 데이터 라벨 (전역 변수)
    // ===============================
    private JLabel userInfoText;
    private JLabel notiText1;
    private JLabel notiText2;
    private JPanel schedulePanel;

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

    // 1️⃣ UI 초기화
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

        JLabel jarIcon = new JLabel();
        String jarPath = "resource/img/honey-jar.png";
        ImageIcon jarImg = new ImageIcon(jarPath);
        if (jarImg.getIconWidth() > 0) {
            Image img = jarImg.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            jarIcon.setIcon(new ImageIcon(img));
            jarIcon.setBounds(310, 20, 40, 40);
        } else {
            jarIcon.setText("🍯");
            jarIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
            jarIcon.setBounds(310, 25, 40, 40);
        }
        headerPanel.add(jarIcon);

        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 25));
        userInfoPanel.setBounds(400, 0, 380, 80);
        userInfoPanel.setOpaque(false);

        JLabel profileIcon = new JLabel("👤");
        profileIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        
        userInfoText = new JLabel("로딩중..."); 
        userInfoText.setFont(uiFont.deriveFont(14f));
        userInfoText.setForeground(BROWN);
        userInfoText.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userInfoText.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int ans = JOptionPane.showConfirmDialog(null, "로그아웃 하시겠습니까?", "로그아웃", JOptionPane.YES_NO_OPTION);
                if(ans == JOptionPane.YES_OPTION) {
                    new LoginFrame();
                    dispose();
                }
            }
        });

        userInfoPanel.add(profileIcon);
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
        String beePath = "resource/img/login-bee.png";
        ImageIcon beeImg = new ImageIcon(beePath);
        if (beeImg.getIconWidth() > 0) {
            Image img = beeImg.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
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
        
        JLabel todayLabel = new JLabel("○○○ TODAY");
        todayLabel.setFont(uiFont.deriveFont(18f));
        todayLabel.setForeground(BROWN);
        todayLabel.setBounds(20, 10, 200, 20);
        todayHeader.add(todayLabel);

        JLabel closeBtn = new JLabel("X");
        closeBtn.setFont(uiFont.deriveFont(18f));
        closeBtn.setForeground(BROWN);
        closeBtn.setBounds(660, 10, 20, 20);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                todayPanel.setVisible(false);
            }
        });
        todayHeader.add(closeBtn);
        todayPanel.add(todayHeader);

        notiText1 = new JLabel("", SwingConstants.CENTER);
        notiText1.setFont(uiFont.deriveFont(20f));
        notiText1.setForeground(new Color(100, 100, 100));
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
        schedulePanel.setBounds(0, 270, 800, 200);
        schedulePanel.setBackground(BG_MAIN);
        contentPanel.add(schedulePanel);
    }

    // 2️⃣ 데이터 갱신 (수정됨)
    private void refreshData() {
        String userName = "백소연"; 
        int honeyPoint = 100;
        userInfoText.setText("[" + userName + "]님 | 보유 꿀 : " + honeyPoint + " | 로그아웃");

        String eventTitle = "소프트웨어융합학과 간식 행사가 있습니다.";
        int remainQty = 0;
        notiText1.setText(eventTitle);
        notiText2.setText("(남은 수량 : " + remainQty + "개)");

        schedulePanel.removeAll();
        
        // [수정] 종강파티 삭제, 리스트 간소화
        String[][] schedules = {
            {"12월 2일", "보조배터리 반납"},
            {"12월 5일", "총학생회 간식 행사"}
        };

        int yPos = 0;
        for (String[] sch : schedules) {
            addScheduleItem(schedulePanel, sch[0], sch[1], yPos);
            yPos += 40;
        }
        
        schedulePanel.revalidate();
        schedulePanel.repaint();
    }

    // 🛠️ 메뉴 버튼 생성 헬퍼
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
                JOptionPane.showMessageDialog(null, "[" + text + "] 화면으로 이동합니다.");
            }
        });
        return btn;
    }

    // 🛠️ 일정 아이템 추가 (수정됨: 강조 색상 제거)
    private void addScheduleItem(JPanel panel, String date, String content, int y) {
        JLabel dateLabel = new JLabel(date);
        dateLabel.setFont(uiFont.deriveFont(18f));
        dateLabel.setForeground(BROWN);
        dateLabel.setBounds(50, y, 100, 30);
        
        JLabel barLabel = new JLabel("|");
        barLabel.setFont(uiFont.deriveFont(18f));
        barLabel.setForeground(Color.LIGHT_GRAY);
        barLabel.setBounds(150, y, 20, 30);

        JLabel contentLabel = new JLabel(content);
        contentLabel.setFont(uiFont.deriveFont(18f));
        contentLabel.setForeground(BROWN);
        
        // [삭제됨] 배경색 강조 로직 제거 (투명하게)
        contentLabel.setOpaque(false); 
        contentLabel.setBounds(170, y, 400, 30);

        panel.add(dateLabel);
        panel.add(barLabel);
        panel.add(contentLabel);
    }

    // 🛠️ 둥근 테두리 클래스
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
        new MainFrame();
    }
}