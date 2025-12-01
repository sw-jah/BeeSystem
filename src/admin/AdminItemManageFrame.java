package admin; // [수정] 패키지 변경

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.util.ArrayList;

public class AdminItemManageFrame extends JFrame {

    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(139, 90, 43);
    
    private static Font uiFont;
    static {
        try {
            InputStream is = AdminItemManageFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) { uiFont = new Font("맑은 고딕", Font.PLAIN, 14); }
    }

    private JPanel itemListPanel;
    private ArrayList<ItemData> itemList = new ArrayList<>();

    public AdminItemManageFrame() {
        setTitle("관리자 - 물품 관리");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        // 테스트 데이터
        itemList.add(new ItemData("C타입 충전기", 3, 1, "전체 학과", null));
        itemList.add(new ItemData("노트북", 5, 3, "소프트웨어융합학과", null));

        initUI();
        refreshList();
        setVisible(true);
    }

    private void initUI() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(null);
        headerPanel.setBounds(0, 0, 800, 80);
        headerPanel.setBackground(HEADER_YELLOW);
        add(headerPanel);

        JLabel titleLabel = new JLabel("물품 관리");
        titleLabel.setFont(uiFont.deriveFont(32f));
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(30, 20, 200, 40);
        headerPanel.add(titleLabel);

        JButton homeBtn = new JButton("<- 메인으로");
        homeBtn.setFont(uiFont.deriveFont(14f));
        homeBtn.setBackground(BROWN);
        homeBtn.setForeground(Color.WHITE);
        homeBtn.setBounds(650, 25, 110, 35);
        homeBtn.setBorder(new RoundedBorder(15, BROWN));
        homeBtn.setFocusPainted(false);
        homeBtn.addActionListener(e -> {
            new AdminMainFrame();
            dispose();
        });
        headerPanel.add(homeBtn);

        JButton addBtn = new JButton("+ 물품 등록");
        addBtn.setFont(uiFont.deriveFont(16f));
        addBtn.setBackground(BROWN);
        addBtn.setForeground(Color.WHITE);
        addBtn.setBounds(630, 100, 130, 40);
        addBtn.setBorder(new RoundedBorder(15, BROWN));
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(e -> {
            new AdminItemAddDialog(this, null); 
        });
        add(addBtn);

        itemListPanel = new JPanel();
        itemListPanel.setLayout(null);
        itemListPanel.setBackground(BG_MAIN);
        
        JScrollPane scrollPane = new JScrollPane(itemListPanel);
        scrollPane.setBounds(30, 150, 730, 400);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane);
    }

    public void refreshList() {
        itemListPanel.removeAll();
        int yPos = 10;

        for (int i = 0; i < itemList.size(); i++) {
            ItemData item = itemList.get(i);
            JPanel card = createItemCard(item, i);
            card.setBounds(10, yPos, 700, 100);
            itemListPanel.add(card);
            yPos += 110;
        }

        itemListPanel.setPreferredSize(new Dimension(700, yPos));
        itemListPanel.revalidate();
        itemListPanel.repaint();
    }

    private JPanel createItemCard(ItemData item, int index) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new RoundedBorder(15, Color.LIGHT_GRAY));

        JLabel imgLabel = new JLabel("📦", SwingConstants.CENTER);
        imgLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        imgLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        imgLabel.setBounds(15, 15, 70, 70);
        panel.add(imgLabel);

        JLabel nameLabel = new JLabel(item.name);
        nameLabel.setFont(uiFont.deriveFont(20f));
        nameLabel.setForeground(BROWN);
        nameLabel.setBounds(100, 15, 300, 25);
        panel.add(nameLabel);

        String info = "재고: " + item.stock + "개 | 대여기간: " + item.rentDays + "일 | 대상: " + item.targetMajor;
        JLabel detailLabel = new JLabel(info);
        detailLabel.setFont(uiFont.deriveFont(14f));
        detailLabel.setForeground(Color.GRAY);
        detailLabel.setBounds(100, 50, 400, 20);
        panel.add(detailLabel);

        JButton editBtn = new JButton("수정");
        editBtn.setFont(uiFont.deriveFont(12f));
        editBtn.setBackground(new Color(255, 238, 140));
        editBtn.setForeground(BROWN);
        editBtn.setBounds(530, 30, 70, 40);
        editBtn.setBorder(new RoundedBorder(10, BROWN));
        editBtn.addActionListener(e -> {
            new AdminItemAddDialog(this, item); 
        });
        panel.add(editBtn);

        JButton delBtn = new JButton("삭제");
        delBtn.setFont(uiFont.deriveFont(12f));
        delBtn.setBackground(new Color(255, 100, 100));
        delBtn.setForeground(Color.WHITE);
        delBtn.setBounds(610, 30, 70, 40);
        delBtn.setBorder(new RoundedBorder(10, new Color(200, 50, 50)));
        delBtn.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, 
                "정말 [" + item.name + "] 항목을 삭제하시겠습니까?", 
                "삭제 확인", JOptionPane.YES_NO_OPTION);
            if(result == JOptionPane.YES_OPTION) {
                itemList.remove(index);
                refreshList();
            }
        });
        panel.add(delBtn);

        return panel;
    }

    public void addItem(ItemData newItem) {
        itemList.add(newItem);
        refreshList();
    }
    
    public static class ItemData {
        String name;
        int stock;
        int rentDays;
        String targetMajor;
        String imagePath;

        public ItemData(String n, int s, int r, String t, String i) {
            name = n; stock = s; rentDays = r; targetMajor = t; imagePath = i;
        }
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
}