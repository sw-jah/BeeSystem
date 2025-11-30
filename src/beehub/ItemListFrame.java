package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;

public class ItemListFrame extends JFrame {

    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color NAV_BG = new Color(255, 255, 255);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(89, 60, 28);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157);
    private static final Color GREEN_AVAILABLE = new Color(180, 230, 180);
    private static final Color RED_UNAVAILABLE = new Color(255, 200, 200);

    private static Font uiFont;

    static {
        try {
            InputStream is = ItemListFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }

    private JLabel userInfoText;
    private JTextField searchField;
    private JPanel itemListPanel;

    // ===============================
    // 📦 임시 물품 데이터
    // TODO: DB 연동 시 ItemDTO로 변경
    // ===============================
    private String[][] items = {
        // {itemName, stock, status, rentDays, restrictedMajor, imagePath}
        {"C타입 충전기", "3", "available", "1", "전체 학과", null},
        {"노트북", "0", "unavailable", "3", "전체 학과", null},
        {"전공책", "2", "available", "5", "소프트웨어융합학과", null}
    };

    public ItemListFrame() {
        setTitle("서울여대 꿀단지 - 물품대여");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initUI();
        loadItems();

        setVisible(true);
    }

    private void initUI() {
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

        userInfoText = new JLabel("[이름]님 | 보유 꿀 : 100 | 로그아웃");
        userInfoText.setFont(uiFont.deriveFont(14f));
        userInfoText.setForeground(BROWN);

        userInfoPanel.add(profileIcon);
        userInfoPanel.add(userInfoText);
        headerPanel.add(userInfoPanel);

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

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(null);
        contentPanel.setBounds(0, 130, 800, 470);
        contentPanel.setBackground(BG_MAIN);
        add(contentPanel);

        searchField = new JTextField();
        searchField.setFont(uiFont.deriveFont(16f));
        searchField.setBounds(200, 20, 350, 40);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        contentPanel.add(searchField);

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        searchIcon.setBounds(560, 25, 30, 30);
        searchIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchIcon.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                searchItems();
            }
        });
        contentPanel.add(searchIcon);

        itemListPanel = new JPanel();
        itemListPanel.setLayout(null);
        itemListPanel.setBackground(BG_MAIN);
        itemListPanel.setPreferredSize(new Dimension(750, items.length * 140));

        JScrollPane scrollPane = new JScrollPane(itemListPanel);
        scrollPane.setBounds(25, 80, 750, 370);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        contentPanel.add(scrollPane);
    }

    private void loadItems() {
        itemListPanel.removeAll();

        int yPos = 10;
        for (String[] item : items) {
            String itemName = item[0];
            int stock = Integer.parseInt(item[1]);
            String status = item[2];
            String rentDays = item[3];
            String restrictedMajor = item[4];
            String imagePath = item[5]; // 나중에 DB에서 가져올 이미지 경로

            addItemCard(itemName, stock, status, rentDays, restrictedMajor, imagePath, yPos);
            yPos += 130;
        }

        itemListPanel.setPreferredSize(new Dimension(750, yPos));
        itemListPanel.revalidate();
        itemListPanel.repaint();
    }

    private void addItemCard(String itemName, int stock, String status, String rentDays, 
                             String restrictedMajor, String imagePath, int y) {
        JPanel card = new JPanel();
        card.setLayout(null);
        card.setBounds(10, y, 730, 110);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(15, new Color(200, 200, 200), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // ===============================
        // 📷 아이콘 영역 (이미지 or 이모지)
        // ===============================
        JLabel iconLabel = new JLabel();
        iconLabel.setBounds(20, 20, 70, 70);
        iconLabel.setOpaque(true);
        iconLabel.setBackground(new Color(245, 245, 245));
        iconLabel.setBorder(new RoundedBorder(10, new Color(220, 220, 220), 1));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);

        // TODO: DB 연동 시 imagePath가 null이 아니면 이미지 로드
        if (imagePath != null) {
            try {
                ImageIcon icon = new ImageIcon(imagePath);
                Image img = icon.getImage().getScaledInstance(65, 65, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(img));
                iconLabel.setText(""); // 이미지 있으면 텍스트 제거
            } catch (Exception e) {
                // 이미지 로드 실패 시 이모지로 대체
                iconLabel.setIcon(null);
                iconLabel.setText(getEmojiForItem(itemName));
                iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
            }
        } else {
            // 이미지 없으면 이모지 표시
            iconLabel.setText(getEmojiForItem(itemName));
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        }
        
        card.add(iconLabel);

        JLabel statusLabel = new JLabel(status.equals("available") ? "대여 가능" : "대여 불가");
        statusLabel.setFont(uiFont.deriveFont(Font.BOLD, 13f));
        statusLabel.setForeground(BROWN);
        statusLabel.setBounds(110, 20, 90, 25);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(status.equals("available") ? GREEN_AVAILABLE : RED_UNAVAILABLE);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(statusLabel);

        JLabel nameLabel = new JLabel(itemName);
        nameLabel.setFont(uiFont.deriveFont(Font.BOLD, 26f));
        nameLabel.setForeground(Color.BLACK);
        nameLabel.setBounds(110, 50, 250, 40);
        card.add(nameLabel);

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // 상세 화면으로 이동 (이미지 경로 전달)
                new ItemDetailFrame(itemName, stock, status, rentDays, restrictedMajor, imagePath);
                dispose();
            }

            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(250, 250, 250));
            }
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });

        itemListPanel.add(card);
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

    private void searchItems() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadItems();
            return;
        }

        itemListPanel.removeAll();
        int yPos = 10;

        for (String[] item : items) {
            if (item[0].contains(keyword)) {
                String itemName = item[0];
                int stock = Integer.parseInt(item[1]);
                String status = item[2];
                String rentDays = item[3];
                String restrictedMajor = item[4];
                String imagePath = item[5];

                addItemCard(itemName, stock, status, rentDays, restrictedMajor, imagePath, yPos);
                yPos += 130;
            }
        }

        if (yPos == 10) {
            JLabel noResult = new JLabel("검색 결과가 없습니다.", SwingConstants.CENTER);
            noResult.setFont(uiFont.deriveFont(20f));
            noResult.setForeground(new Color(150, 150, 150));
            noResult.setBounds(0, 100, 750, 50);
            itemListPanel.add(noResult);
        }

        itemListPanel.setPreferredSize(new Dimension(750, Math.max(yPos, 350)));
        itemListPanel.revalidate();
        itemListPanel.repaint();
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
                    if (text.equals("물품대여")) return;
                    JOptionPane.showMessageDialog(null, "[" + text + "] 화면으로 이동합니다.");
                }
            });
        }
        return btn;
    }
    
    // ===============================
    // ✨ 모던 스크롤바 UI
    // ===============================
    class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {

        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(200, 200, 200);
            this.trackColor = new Color(245, 245, 245);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            return btn;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (!c.isEnabled()) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 10);
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
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
        SwingUtilities.invokeLater(ItemListFrame::new);
    }
}