package admin;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.InputStream;
import java.net.URL; // URL 추가
import java.util.List;
import beehub.ItemManager; 
import beehub.ItemManager.Item;

public class AdminItemManageFrame extends JFrame {

    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(139, 90, 43);
    private static final Color POPUP_BG = new Color(255, 250, 205);
    
    private static Font uiFont;
    static {
        try {
            InputStream is = AdminItemManageFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) { uiFont = new Font("맑은 고딕", Font.PLAIN, 14); }
    }

    private JPanel itemListPanel;

    public AdminItemManageFrame() {
        setTitle("관리자 - 물품 관리");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initUI();
        refreshList(); 
        setVisible(true);
    }

    private void initUI() {
        JPanel header = new JPanel(null);
        header.setBounds(0, 0, 800, 80);
        header.setBackground(HEADER_YELLOW);
        add(header);

        JLabel title = new JLabel("물품 관리");
        title.setFont(uiFont.deriveFont(32f));
        title.setForeground(BROWN);
        title.setBounds(30, 20, 200, 40);
        header.add(title);

        JButton homeBtn = new JButton("<-메인으로");
        homeBtn.setBounds(650, 25, 110, 35);
        homeBtn.setFont(uiFont.deriveFont(14f));
        homeBtn.setBackground(BROWN);
        homeBtn.setForeground(Color.WHITE);
        homeBtn.setBorder(new RoundedBorder(15, BROWN));
        homeBtn.addActionListener(e -> { new AdminMainFrame(); dispose(); });
        header.add(homeBtn);

        JButton addBtn = new JButton("+ 물품 등록");
        addBtn.setBounds(630, 100, 130, 40);
        addBtn.setFont(uiFont.deriveFont(16f));
        addBtn.setBackground(BROWN);
        addBtn.setForeground(Color.WHITE);
        addBtn.setBorder(new RoundedBorder(15, BROWN));
        addBtn.addActionListener(e -> new AdminItemAddDialog(this, null));
        add(addBtn);

        itemListPanel = new JPanel(null);
        itemListPanel.setBackground(BG_MAIN);
        
        JScrollPane scroll = new JScrollPane(itemListPanel);
        scroll.setBounds(30, 150, 730, 400);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        
        // [수정] 세련된 스크롤바 UI 적용
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        
        add(scroll);
    }

    public void refreshList() {
        itemListPanel.removeAll();
        List<Item> items = ItemManager.getAllItems();
        int y = 0;

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            JPanel card = createItemCard(item, i);
            card.setBounds(10, y, 700, 100);
            itemListPanel.add(card);
            y += 110;
        }

        itemListPanel.setPreferredSize(new Dimension(700, y));
        itemListPanel.revalidate();
        itemListPanel.repaint();
    }

    private JPanel createItemCard(Item item, int index) {
        JPanel p = new JPanel(null);
        p.setBackground(Color.WHITE);
        p.setBorder(new RoundedBorder(15, Color.LIGHT_GRAY));

        JLabel icon = new JLabel("📦", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        icon.setBounds(15, 15, 70, 70);
        icon.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        // [수정] 안전한 이미지 로딩
        boolean imgLoaded = false;
        if(item.imagePath != null && !item.imagePath.isEmpty()) {
            try {
                URL url = getClass().getResource(item.imagePath.startsWith("/") ? item.imagePath : "/" + item.imagePath);
                if (url == null) {
                    ImageIcon ii = new ImageIcon(item.imagePath);
                    if (ii.getIconWidth() > 0) {
                        icon.setIcon(new ImageIcon(ii.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH)));
                        icon.setText("");
                        imgLoaded = true;
                    }
                } else {
                    ImageIcon ii = new ImageIcon(url);
                    icon.setIcon(new ImageIcon(ii.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH)));
                    icon.setText("");
                    imgLoaded = true;
                }
            } catch(Exception e) { }
        }
        if (!imgLoaded) {
             icon.setText("📦");
        }
        
        p.add(icon);

        JLabel name = new JLabel(item.name);
        name.setFont(uiFont.deriveFont(20f));
        name.setForeground(BROWN);
        name.setBounds(100, 15, 300, 25);
        p.add(name);

        JLabel info = new JLabel("재고: " + item.stock + " | 기간: " + item.rentDays + "일 | " + item.targetMajor);
        info.setFont(uiFont.deriveFont(14f));
        info.setForeground(Color.GRAY);
        info.setBounds(100, 50, 400, 20);
        p.add(info);

        JButton edit = new JButton("수정");
        edit.setBounds(530, 30, 70, 40);
        edit.setFont(uiFont.deriveFont(12f));
        edit.setBackground(new Color(255, 238, 140));
        edit.setForeground(BROWN);
        edit.setBorder(new RoundedBorder(10, BROWN));
        edit.addActionListener(e -> new AdminItemAddDialog(this, item));
        p.add(edit);

        JButton del = new JButton("삭제");
        del.setBounds(610, 30, 70, 40);
        del.setFont(uiFont.deriveFont(12f));
        del.setBackground(new Color(255, 100, 100));
        del.setForeground(Color.WHITE);
        del.setBorder(new RoundedBorder(10, new Color(200, 50, 50)));
        del.addActionListener(e -> {
            boolean confirm = showConfirmPopup("삭제 확인", "정말 [" + item.name + "] 항목을\n삭제하시겠습니까?");
            if(confirm) {
                ItemManager.deleteItem(index);
                refreshList();
            }
        });
        p.add(del);

        return p;
    }

    private boolean showConfirmPopup(String title, String msg) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(new Color(0,0,0,0));
        final boolean[] res = {false};

        JPanel p = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(POPUP_BG);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),30,30);
                g2.setColor(BROWN);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,30,30);
            }
        };
        p.setLayout(null);
        dialog.add(p);

        String[] lines = msg.split("\n");
        int y = lines.length == 1 ? 80 : 60;
        for(String line : lines) {
            JLabel l = new JLabel(line, SwingConstants.CENTER);
            l.setFont(uiFont.deriveFont(18f));
            l.setForeground(BROWN);
            l.setBounds(20, y, 360, 30);
            p.add(l);
            y+=30;
        }

        JButton yes = new JButton("네");
        yes.setBounds(60, 160, 120, 45);
        yes.setBackground(BROWN);
        yes.setForeground(Color.WHITE);
        yes.addActionListener(e -> { res[0]=true; dialog.dispose(); });
        p.add(yes);

        JButton no = new JButton("아니오");
        no.setBounds(220, 160, 120, 45);
        no.setBackground(BROWN);
        no.setForeground(Color.WHITE);
        no.addActionListener(e -> { res[0]=false; dialog.dispose(); });
        p.add(no);

        dialog.setVisible(true);
        return res[0];
    }

    // [추가] ModernScrollBarUI
    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(200, 200, 200);
            this.trackColor = new Color(245, 245, 245);
        }
        @Override
        protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override
        protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            return btn;
        }
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (!c.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 8, 8);
        }
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }

    private static class RoundedBorder implements Border {
        private int r; private Color c;
        public RoundedBorder(int r, Color c) { this.r=r; this.c=c; }
        public Insets getBorderInsets(Component c) { return new Insets(r/2,r/2,r/2,r/2); }
        public boolean isBorderOpaque() { return false; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(this.c);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x,y,w-1,h-1,r,r);
        }
    }
}