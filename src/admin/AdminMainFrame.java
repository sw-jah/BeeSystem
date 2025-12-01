package admin; // [수정] 패키지 변경

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import beehub.LoginFrame; // [수정] LoginFrame import 추가

public class AdminMainFrame extends JFrame {

    // ===============================
    // 🎨 컬러 테마
    // ===============================
    private static final Color BG_YELLOW = new Color(255, 250, 205);
    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color BROWN = new Color(139, 90, 43);
    
    // 폰트 설정
    private static Font uiFont;

    static {
        try {
            // 리소스 경로는 src/admin이 아니라 classpath 기준이므로 그대로 둡니다.
            InputStream is = AdminMainFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.BOLD, 12);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.BOLD, 12);
        }
    }

    public AdminMainFrame() {
        setTitle("서울여대 꿀단지 - 총 관리자");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_YELLOW);

        initUI();
        setVisible(true);
    }

    private void initUI() {
        // --- 상단 헤더 ---
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(null);
        headerPanel.setBounds(0, 0, 800, 80);
        headerPanel.setBackground(HEADER_YELLOW);
        add(headerPanel);

        JLabel logoLabel = new JLabel("서울여대 꿀단지 [관리자]");
        logoLabel.setFont(uiFont.deriveFont(32f));
        logoLabel.setForeground(BROWN);
        logoLabel.setBounds(30, 20, 400, 40);
        headerPanel.add(logoLabel);

        // 로그아웃 버튼
        JButton logoutBtn = new JButton("로그아웃");
        logoutBtn.setFont(uiFont.deriveFont(14f));
        logoutBtn.setBackground(BROWN);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBounds(680, 25, 90, 35);
        logoutBtn.setBorder(new RoundedBorder(15, BROWN));
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> {
            new LoginFrame(); // 로그인 화면으로 이동
            dispose();
        });
        headerPanel.add(logoutBtn);

        // --- 메인 메뉴 버튼들 ---
        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new GridLayout(2, 2, 20, 20)); 
        menuContainer.setBounds(100, 130, 600, 400);
        menuContainer.setOpaque(false);
        add(menuContainer);

        menuContainer.add(createMenuButton("물품 관리", e -> {
            new AdminItemManageFrame(); // 물품 관리 화면 열기
            dispose();
        }));
        menuContainer.add(createMenuButton("대여 관리", e -> showMsg("준비 중입니다.")));
        menuContainer.add(createMenuButton("장소 대여", e -> showMsg("준비 중입니다.")));
        menuContainer.add(createMenuButton("경품 추첨", e -> showMsg("준비 중입니다.")));
    }

    private JButton createMenuButton(String text, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(24f));
        btn.setBackground(Color.WHITE);
        btn.setForeground(BROWN);
        btn.setBorder(new RoundedBorder(30, BROWN));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(255, 245, 220)); }
            public void mouseExited(MouseEvent e) { btn.setBackground(Color.WHITE); }
        });
        
        return btn;
    }

    private void showMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg);
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
            g2.drawRoundRect(x, y, w-1, h-1, radius, radius);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminMainFrame::new);
    }
}