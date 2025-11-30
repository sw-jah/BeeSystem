package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;

public class ItemDetailFrame extends JFrame {

    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color NAV_BG = new Color(255, 255, 255);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(89, 60, 28);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157);
    private static final Color GREEN_AVAILABLE = new Color(180, 230, 180);
    private static final Color RED_UNAVAILABLE = new Color(255, 200, 200);
    private static final Color GRAY_BTN = new Color(180, 180, 180);

    private static Font uiFont;

    static {
        try {
            InputStream is = ItemDetailFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
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
    // 📦 물품 정보
    // TODO: DB 연동 시 ItemDTO 객체로 변경
    // ===============================
    private String itemName;
    private int stock;
    private String status;
    private String rentDays;
    private String restrictedMajor;
    private String imagePath; // 관리자가 등록한 이미지 경로
    private boolean isRented = false;

    public ItemDetailFrame(String itemName, int stock, String status, String rentDays, 
                          String restrictedMajor, String imagePath) {
        this.itemName = itemName;
        this.stock = stock;
        this.status = status;
        this.rentDays = rentDays;
        this.restrictedMajor = restrictedMajor;
        this.imagePath = imagePath;

        setTitle("서울여대 꿀단지 - " + itemName);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

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
        userInfoPanel.setBounds(400, 0, 380, 80);
        userInfoPanel.setOpaque(false);

        JLabel profileIcon = new JLabel("👤");
        profileIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JLabel userInfoText = new JLabel("[이름]님 | 보유 꿀 : 100 | 로그아웃");
        userInfoText.setFont(uiFont.deriveFont(14f));
        userInfoText.setForeground(BROWN);
        userInfoText.setCursor(new Cursor(Cursor.HAND_CURSOR));

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
        for (int i = 0; i < menus.length; i++) {
            JButton menuBtn = createNavButton(menus[i], i == 0);
            navPanel.add(menuBtn);
        }

        // --- 메인 컨텐츠 ---
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(null);
        contentPanel.setBounds(0, 130, 800, 470);
        contentPanel.setBackground(BG_MAIN);
        add(contentPanel);

        // 우측 상단 "이전 화면" 버튼
        JButton backButton = new JButton("이전 화면");
        backButton.setFont(uiFont.deriveFont(14f));
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(GRAY_BTN);
        backButton.setBounds(680, 20, 90, 30);
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> {
            new ItemListFrame();
            dispose();
        });
        contentPanel.add(backButton);

        // ===============================
        // 📷 아이콘/이미지 영역
        // ===============================
        JLabel iconLabel = new JLabel();
        iconLabel.setBounds(70, 80, 230, 250);
        iconLabel.setOpaque(true);
        iconLabel.setBackground(new Color(245, 245, 245));
        iconLabel.setBorder(new RoundedBorder(20, new Color(220, 220, 220), 2));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);

        // TODO: DB 연동 시 imagePath로 이미지 로드
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(imagePath);
                Image img = icon.getImage().getScaledInstance(220, 240, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(img));
                iconLabel.setText(""); // 이미지 있으면 텍스트 제거
            } catch (Exception e) {
                // 이미지 로드 실패 시 이모지로 대체
                iconLabel.setIcon(null);
                iconLabel.setText(getEmojiForItem(itemName));
                iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 120));
            }
        } else {
            // 이미지 없으면 이모지 표시
            iconLabel.setText(getEmojiForItem(itemName));
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 120));
        }

        contentPanel.add(iconLabel);

        // 대여 가능 여부 라벨
        JLabel statusLabel = new JLabel(status.equals("available") ? "대여 가능" : "대여 불가");
        statusLabel.setFont(uiFont.deriveFont(Font.BOLD, 15f));
        statusLabel.setForeground(BROWN);
        statusLabel.setBounds(330, 85, 110, 35);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(status.equals("available") ? GREEN_AVAILABLE : RED_UNAVAILABLE);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        contentPanel.add(statusLabel);

        // 물품 이름
        JLabel nameLabel = new JLabel(itemName);
        nameLabel.setFont(uiFont.deriveFont(Font.BOLD, 40f));
        nameLabel.setForeground(Color.BLACK);
        nameLabel.setBounds(330, 145, 450, 50);
        contentPanel.add(nameLabel);

        // 정보 라벨들
        JLabel stockLabel = new JLabel("남은 재고 : " + stock + "개");
        stockLabel.setFont(uiFont.deriveFont(20f));
        stockLabel.setForeground(new Color(80, 80, 80));
        stockLabel.setBounds(330, 210, 400, 30);
        contentPanel.add(stockLabel);

        JLabel daysLabel = new JLabel("대여 가능 일 수 : " + rentDays + "일");
        daysLabel.setFont(uiFont.deriveFont(20f));
        daysLabel.setForeground(new Color(80, 80, 80));
        daysLabel.setBounds(330, 245, 400, 30);
        contentPanel.add(daysLabel);

        JLabel majorLabel = new JLabel("대상 학과 : " + restrictedMajor);
        majorLabel.setFont(uiFont.deriveFont(20f));
        majorLabel.setForeground(new Color(80, 80, 80));
        majorLabel.setBounds(330, 280, 400, 30);
        contentPanel.add(majorLabel);

        // 대여하기 버튼 (재고 있고 대여 가능일 때만)
        if (status.equals("available") && stock > 0) {
            JButton rentButton = new JButton("대여하기");
            rentButton.setFont(uiFont.deriveFont(Font.BOLD, 20f));
            rentButton.setForeground(Color.WHITE);
            rentButton.setBackground(BROWN);
            rentButton.setBounds(550, 350, 200, 60);
            rentButton.setFocusPainted(false);
            rentButton.setBorderPainted(false);
            rentButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            rentButton.addActionListener(e -> {
                if (isRented) {
                    showAlreadyRentedDialog();
                } else {
                    // TODO: DB 연동 시 실제 재고 감소 쿼리 실행
                    stock--;
                    stockLabel.setText("남은 재고 : " + stock + "개");
                    isRented = true;
                    showRentalCompleteDialog();

                    // 재고가 0이 되면 대여 불가로 변경
                    if (stock == 0) {
                        rentButton.setVisible(false);
                        statusLabel.setText("대여 불가");
                        statusLabel.setBackground(RED_UNAVAILABLE);
                    }
                }
            });
            contentPanel.add(rentButton);
        }
    }

    // ===============================
    // 🎨 물품별 이모지 반환
    // ===============================
    private String getEmojiForItem(String itemName) {
        if (itemName.contains("충전기")) return "⚡";
        if (itemName.contains("노트북")) return "💻";
        if (itemName.contains("책")) return "📚";
        if (itemName.contains("우산")) return "☂️";
        if (itemName.contains("배터리")) return "🔋";
        return "📦"; // 기본 아이콘
    }

    // 대여 완료 다이얼로그
    private void showRentalCompleteDialog() {
        showMessageDialog("대여가 완료되었습니다.");
    }

    // 이미 대여중 다이얼로그
    private void showAlreadyRentedDialog() {
        showMessageDialog("이미 대여중입니다.");
    }

    // 공통 메시지 다이얼로그
    private void showMessageDialog(String message) {
        JDialog dialog = new JDialog(this, "", true);
        dialog.setSize(450, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new RoundedBorder(20, BROWN, 3));

        // 헤더 (노란색)
        JPanel headerPanel = new JPanel();
        headerPanel.setBounds(0, 0, 450, 50);
        headerPanel.setBackground(HIGHLIGHT_YELLOW);
        headerPanel.setLayout(null);

        JLabel headerLabel = new JLabel("알림");
        headerLabel.setFont(uiFont.deriveFont(18f));
        headerLabel.setForeground(BROWN);
        headerLabel.setBounds(20, 15, 100, 20);
        headerPanel.add(headerLabel);

        JLabel closeBtn = new JLabel("✕");
        closeBtn.setFont(uiFont.deriveFont(20f));
        closeBtn.setForeground(BROWN);
        closeBtn.setBounds(415, 15, 20, 20);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dialog.dispose();
            }
        });
        headerPanel.add(closeBtn);
        panel.add(headerPanel);

        JLabel msgLabel = new JLabel(message, SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(22f));
        msgLabel.setForeground(new Color(100, 100, 100));
        msgLabel.setBounds(50, 90, 350, 50);
        panel.add(msgLabel);

        JButton confirmBtn = new JButton("확인");
        confirmBtn.setFont(uiFont.deriveFont(16f));
        confirmBtn.setBackground(BROWN);
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setBounds(150, 170, 150, 45);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setBorderPainted(false);
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmBtn.addActionListener(e -> dialog.dispose());
        panel.add(confirmBtn);

        dialog.add(panel);
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
                    if (text.equals("물품대여")) {
                        new ItemListFrame();
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(null, "[" + text + "] 화면으로 이동합니다.");
                    }
                }
            });
        }
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
        // 테스트용 (이미지 없을 때 이모지로 표시)
        SwingUtilities.invokeLater(() ->
            new ItemDetailFrame("C타입 충전기", 3, "available", "1", "전체 학과", null)
        );
    }
}