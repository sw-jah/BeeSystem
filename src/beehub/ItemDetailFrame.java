package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.util.List;

// [중요] 매니저 클래스 임포트
import admin.PenaltyManager; 
import beehub.ItemManager;   

public class ItemDetailFrame extends JFrame {

    // ===============================
    // 🎨 UI 디자인 상수
    // ===============================
    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color NAV_BG = new Color(255, 255, 255);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(89, 60, 28);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157);
    private static final Color GREEN_AVAILABLE = new Color(180, 230, 180);
    private static final Color RED_UNAVAILABLE = new Color(255, 200, 200);
    private static final Color GRAY_BTN = new Color(180, 180, 180);
    private static final Color POPUP_BG = new Color(255, 250, 205);

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
    // 👤 사용자 및 물품 변수
    // ===============================
    private String userName = "사용자";
    private String userId = ""; // 로그인한 사용자 ID
    private int userPoint = 100;

    private String itemName;
    private int stock;
    private String status;
    private String rentDays;
    private String restrictedMajor;
    private String imagePath; 
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
        
        // 현재 로그인한 사용자 정보 가져오기
        User currentUser = UserManager.getCurrentUser();
        if(currentUser != null) {
            userName = currentUser.getName();
            userId = currentUser.getId();
            userPoint = currentUser.getPoints();
        }
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initUI();

        setVisible(true);
    }

    private void initUI() {
        // --- 헤더 영역 ---
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

        JLabel userInfoText = new JLabel("[" + userName + "]님 | 로그아웃");
        userInfoText.setFont(uiFont.deriveFont(14f));
        userInfoText.setForeground(BROWN);
        userInfoText.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        userInfoText.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                showLogoutPopup();
            }
        });

        userInfoPanel.add(userInfoText);
        headerPanel.add(userInfoPanel);

        // --- 네비게이션 바 ---
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new GridLayout(1, 6));
        navPanel.setBounds(0, 80, 800, 50);
        navPanel.setBackground(NAV_BG);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        add(navPanel);

        String[] menus = {"물품대여", "과행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"};
        for (int i = 0; i < menus.length; i++) {
            JButton menuBtn = createNavButton(menus[i], i == 0);
            navPanel.add(menuBtn);
        }

        // --- 메인 컨텐츠 영역 ---
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(null);
        contentPanel.setBounds(0, 130, 800, 470);
        contentPanel.setBackground(BG_MAIN);
        add(contentPanel);

        // 뒤로가기 버튼
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

        // 아이콘/이미지 표시
        JLabel iconLabel = new JLabel();
        iconLabel.setBounds(70, 80, 230, 250);
        iconLabel.setOpaque(true);
        iconLabel.setBackground(new Color(245, 245, 245));
        iconLabel.setBorder(new RoundedBorder(20, new Color(220, 220, 220), 2));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);

        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(imagePath);
                Image img = icon.getImage().getScaledInstance(220, 240, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(img));
                iconLabel.setText(""); 
            } catch (Exception e) {
                iconLabel.setIcon(null);
                iconLabel.setText(getEmojiForItem(itemName));
                iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 120));
            }
        } else {
            iconLabel.setText(getEmojiForItem(itemName));
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 120));
        }
        contentPanel.add(iconLabel);

        // 상태 라벨
        JLabel statusLabel = new JLabel(status.equals("available") ? "대여 가능" : "대여 불가");
        statusLabel.setFont(uiFont.deriveFont(Font.BOLD, 15f));
        statusLabel.setForeground(BROWN);
        statusLabel.setBounds(330, 85, 110, 35);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(status.equals("available") ? GREEN_AVAILABLE : RED_UNAVAILABLE);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        contentPanel.add(statusLabel);

        // 물품 정보 텍스트
        JLabel nameLabel = new JLabel(itemName);
        nameLabel.setFont(uiFont.deriveFont(Font.BOLD, 40f));
        nameLabel.setForeground(Color.BLACK);
        nameLabel.setBounds(330, 145, 450, 50);
        contentPanel.add(nameLabel);

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

        // 🟢 [핵심] 대여 버튼 로직 (연동 및 제한 기능 포함)
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
                // 1. 이미 이 화면에서 대여를 눌렀는지 확인
                if (isRented) {
                    showSimplePopup("알림", "이미 대여중입니다.");
                    return;
                }

                // 2. 연체 패널티 확인 (PenaltyManager)
                long banDays = PenaltyManager.getRentalBanDaysRemaining(userId);
                if (banDays > 0) {
                    showSimplePopup("대여 불가", "연체 패널티로 인해\n" + banDays + "일 동안 대여할 수 없습니다.");
                    return;
                }

                // 3. 1인당 최대 2개 대여 제한 확인
                int currentCount = PenaltyManager.getCurrentRentalCount(userId);
                if (currentCount >= 2) {
                    showSimplePopup("대여 불가", "물품은 최대 2개까지만\n동시 대여 가능합니다.");
                    return;
                }

                // 4. [중요] ItemManager를 통해 실제 데이터의 재고 차감 (파일 저장됨)
                ItemManager.decreaseStock(itemName);
                
                // 5. 화면 갱신 및 상태 변경
                stock--;
                stockLabel.setText("남은 재고 : " + stock + "개");
                isRented = true;
                
                // 대여 개수 증가 처리
                PenaltyManager.increaseRentalCount(userId);

                showSimplePopup("성공", "대여가 완료되었습니다.\n(현재 대여 중: " + (currentCount + 1) + "개)");

                // 재고 소진 시 UI 변경
                if (stock == 0) {
                    rentButton.setVisible(false);
                    statusLabel.setText("대여 불가");
                    statusLabel.setBackground(RED_UNAVAILABLE);
                }
            });
            contentPanel.add(rentButton);
        }
    }

    private String getEmojiForItem(String itemName) {
        if (itemName.contains("충전기")) return "⚡";
        if (itemName.contains("노트북")) return "💻";
        if (itemName.contains("책")) return "📚";
        if (itemName.contains("우산")) return "☂️";
        if (itemName.contains("배터리")) return "🔋";
        return "📦"; 
    }

    // --- 팝업 디자인 ---
    private void showSimplePopup(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));

        JPanel panel = new JPanel() {
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
        panel.setLayout(null);
        dialog.add(panel);

        String[] lines = message.split("\n");
        int yPos = (lines.length == 1) ? 80 : 60; 

        for (String line : lines) {
            JLabel lbl = new JLabel(line, SwingConstants.CENTER);
            lbl.setFont(uiFont.deriveFont(20f)); 
            lbl.setForeground(BROWN);
            lbl.setBounds(20, yPos, 360, 30);
            panel.add(lbl);
            yPos += 30;
        }

        JButton confirmBtn = new JButton("확인");
        confirmBtn.setFont(uiFont.deriveFont(16f));
        confirmBtn.setBackground(BROWN);
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setBounds(135, 170, 130, 45);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setBorder(new RoundedBorder(15, BROWN, 1));
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmBtn.addActionListener(e -> dialog.dispose());
        panel.add(confirmBtn);

        dialog.setVisible(true);
    }

    private void showLogoutPopup() {
        JDialog dialog = new JDialog(this, "로그아웃", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel() {
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
        panel.setLayout(null);
        dialog.add(panel);

        JLabel msgLabel = new JLabel("로그아웃 하시겠습니까?", SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(18f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 70, 360, 30);
        panel.add(msgLabel);

        JButton yesBtn = new JButton("네");
        yesBtn.setFont(uiFont.deriveFont(16f));
        yesBtn.setBackground(BROWN);
        yesBtn.setForeground(Color.WHITE);
        yesBtn.setFocusPainted(false);
        yesBtn.setBorder(new RoundedBorder(15, BROWN, 1));
        yesBtn.setBounds(60, 150, 120, 45);
        yesBtn.addActionListener(e -> {
            dialog.dispose();
            new LoginFrame();
            dispose();
        });
        panel.add(yesBtn);

        JButton noBtn = new JButton("아니오");
        noBtn.setFont(uiFont.deriveFont(16f));
        noBtn.setBackground(BROWN);
        noBtn.setForeground(Color.WHITE);
        noBtn.setFocusPainted(false);
        noBtn.setBorder(new RoundedBorder(15, BROWN, 1));
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
                    if (text.equals("물품대여")) {
                        new ItemListFrame(); dispose();
                    } else if (text.equals("과행사")) { // 간식행사 -> 과행사로 수정됨
                        new EventListFrame(); dispose();
                    } else if (text.equals("빈 강의실")) {
                        new EmptyClassFrame(); dispose();
                    } else if (text.equals("공간대여")) {
                        new SpaceRentFrame(); dispose();
                    } else if (text.equals("마이페이지")) {
                        new MyPageFrame(); dispose();
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

    // [수정] 메인 메소드에서도 ItemManager 사용하도록 변경
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            List<ItemManager.Item> items = ItemManager.getAllItems();
            if (!items.isEmpty()) {
                ItemManager.Item item = items.get(0);
                new ItemDetailFrame(
                    item.name, 
                    item.stock, 
                    item.stock > 0 ? "available" : "unavailable", 
                    String.valueOf(item.rentDays), 
                    item.targetMajor, 
                    item.imagePath
                );
            } else {
                new ItemDetailFrame("테스트 물품", 5, "available", "3", "전체 학과", null);
            }
        });
    }
}