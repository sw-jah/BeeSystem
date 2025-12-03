package admin;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class AdminRentManageFrame extends JFrame {

    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(139, 90, 43);
    private static final Color RED_OVERDUE = new Color(255, 80, 80);
    private static final Color GREEN_DONE = new Color(100, 180, 100);
    private static final Color POPUP_BG = new Color(255, 250, 205);

    private static Font uiFont;

    static {
        try {
            InputStream is = AdminRentManageFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }

    private JPanel rentListPanel;
    private ArrayList<RentData> rentList = new ArrayList<>();

    public AdminRentManageFrame() {
        setTitle("관리자 - 대여 관리");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        // 테스트 데이터
        PenaltyManager.increaseRentalCount("20231234");
        PenaltyManager.increaseRentalCount("20210001"); 
        PenaltyManager.increaseRentalCount("20245678");

        rentList.add(new RentData("노트북", "20231234", "김슈니", LocalDate.now().minusDays(1), LocalDate.now().plusDays(2), false));
        rentList.add(new RentData("C타입 충전기", "20210001", "이멋사", LocalDate.now().minusDays(5), LocalDate.now().minusDays(3), false));
        rentList.add(new RentData("우산", "20245678", "박새내", LocalDate.now(), LocalDate.now(), false));
        rentList.add(new RentData("전공책(자바)", "20229999", "최코딩", LocalDate.now().minusDays(10), LocalDate.now().minusDays(5), true));

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

        JLabel titleLabel = new JLabel("대여 관리");
        titleLabel.setFont(uiFont.deriveFont(32f));
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(30, 20, 200, 40);
        headerPanel.add(titleLabel);

        JButton homeBtn = new JButton("<-메인으로");
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

        rentListPanel = new JPanel();
        rentListPanel.setLayout(null);
        rentListPanel.setBackground(BG_MAIN);

        JScrollPane scrollPane = new JScrollPane(rentListPanel);
        scrollPane.setBounds(30, 100, 730, 440);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane);
    }

    private void refreshList() {
        rentListPanel.removeAll();
        int yPos = 10;

        for (RentData data : rentList) {
            JPanel card = createRentCard(data);
            card.setBounds(10, yPos, 690, 100);
            rentListPanel.add(card);
            yPos += 110;
        }

        rentListPanel.setPreferredSize(new Dimension(690, yPos));
        rentListPanel.revalidate();
        rentListPanel.repaint();
    }

    private JPanel createRentCard(RentData data) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new RoundedBorder(15, Color.LIGHT_GRAY));

        JLabel nameLabel = new JLabel(data.itemName);
        nameLabel.setFont(uiFont.deriveFont(20f));
        nameLabel.setForeground(BROWN);
        nameLabel.setBounds(20, 15, 250, 30);
        panel.add(nameLabel);

        JLabel renterLabel = new JLabel("대여자: " + data.renterId + " | " + data.renterName);
        renterLabel.setFont(uiFont.deriveFont(14f));
        renterLabel.setForeground(Color.GRAY);
        renterLabel.setBounds(20, 50, 250, 20);
        panel.add(renterLabel);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yy.MM.dd");
        String dateStr = data.rentDate.format(dtf) + " ~ " + data.dueDate.format(dtf);
        JLabel dateLabel = new JLabel(dateStr);
        dateLabel.setFont(uiFont.deriveFont(14f));
        dateLabel.setForeground(Color.GRAY);
        dateLabel.setBounds(20, 70, 250, 20);
        panel.add(dateLabel);

        long daysDiff = ChronoUnit.DAYS.between(LocalDate.now(), data.dueDate);
        String dDayStr;
        Color dDayColor;

        if (data.isReturned) {
            dDayStr = "반납완료";
            dDayColor = GREEN_DONE;
        } else {
            if (daysDiff >= 0) {
                dDayStr = (daysDiff == 0) ? "D-Day" : "D-" + daysDiff;
                dDayColor = BROWN;
            } else {
                dDayStr = "D+" + Math.abs(daysDiff) + " (연체)";
                dDayColor = RED_OVERDUE;
            }
        }

        JLabel statusLabel = new JLabel(dDayStr, SwingConstants.RIGHT);
        statusLabel.setFont(uiFont.deriveFont(Font.BOLD, 22f));
        statusLabel.setForeground(dDayColor);
        statusLabel.setBounds(300, 35, 200, 30);
        panel.add(statusLabel);

        JButton actionBtn = new JButton();
        if (data.isReturned) {
            actionBtn.setText("완료됨");
            actionBtn.setBackground(new Color(230, 230, 230));
            actionBtn.setForeground(Color.GRAY);
            actionBtn.setEnabled(false);
            actionBtn.setBorder(new RoundedBorder(10, Color.LIGHT_GRAY));
        } else {
            actionBtn.setText("반납확인");
            actionBtn.setBackground(BROWN);
            actionBtn.setForeground(Color.WHITE);
            actionBtn.setBorder(new RoundedBorder(10, BROWN));
            
            // [수정] 이쁜 팝업 적용
            actionBtn.addActionListener(e -> {
                boolean confirm = showConfirmPopup("반납 확인", 
                    "[" + data.itemName + "] 반납 처리를\n하시겠습니까?");
                
                if (confirm) {
                    data.isReturned = true; 
                    PenaltyManager.decreaseRentalCount(data.renterId);

                    long overdueDays = ChronoUnit.DAYS.between(data.dueDate, LocalDate.now());
                    if (overdueDays > 0) {
                        PenaltyManager.setRentalBan(data.renterId, overdueDays);
                        showMsgPopup("연체 확인", "연체 반납 확인되었습니다.\n" + overdueDays + "일간 대여 정지 패널티가 부여됩니다.");
                    } else {
                        showMsgPopup("반납 완료", "정상적으로 반납되었습니다.");
                    }
                    refreshList(); 
                }
            });
        }
        actionBtn.setFont(uiFont.deriveFont(14f));
        actionBtn.setBounds(530, 30, 130, 40);
        actionBtn.setFocusPainted(false);
        panel.add(actionBtn);

        return panel;
    }

    // ==========================================
    // 🎨 이쁜 팝업 메소드 (공통 사용)
    // ==========================================
    private void showMsgPopup(String title, String msg) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
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

        String[] lines = msg.split("\n");
        int yPos = lines.length == 1 ? 80 : 60;
        for(String line : lines) {
            JLabel l = new JLabel(line, SwingConstants.CENTER);
            l.setFont(uiFont.deriveFont(18f));
            l.setForeground(BROWN);
            l.setBounds(20, yPos, 360, 30);
            panel.add(l);
            yPos += 30;
        }

        JButton okBtn = new JButton("확인");
        okBtn.setFont(uiFont.deriveFont(16f));
        okBtn.setBackground(BROWN);
        okBtn.setForeground(Color.WHITE);
        okBtn.setBounds(135, 170, 130, 45);
        okBtn.setBorder(new RoundedBorder(15, BROWN));
        okBtn.setFocusPainted(false);
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);

        dialog.setVisible(true);
    }

    private boolean showConfirmPopup(String title, String msg) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(new Color(0,0,0,0));

        final boolean[] result = {false};

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

        String[] lines = msg.split("\n");
        int yPos = lines.length == 1 ? 80 : 60;
        for (String line : lines) {
            JLabel l = new JLabel(line, SwingConstants.CENTER);
            l.setFont(uiFont.deriveFont(18f));
            l.setForeground(BROWN);
            l.setBounds(20, yPos, 360, 30);
            panel.add(l);
            yPos += 30;
        }

        JButton yesBtn = new JButton("네");
        yesBtn.setBounds(60, 160, 120, 45);
        yesBtn.setBackground(BROWN);
        yesBtn.setForeground(Color.WHITE);
        yesBtn.setFont(uiFont.deriveFont(16f));
        yesBtn.setBorder(new RoundedBorder(15, BROWN));
        yesBtn.setFocusPainted(false);
        yesBtn.addActionListener(e -> { result[0] = true; dialog.dispose(); });
        panel.add(yesBtn);

        JButton noBtn = new JButton("아니오");
        noBtn.setBounds(220, 160, 120, 45);
        noBtn.setBackground(BROWN);
        noBtn.setForeground(Color.WHITE);
        noBtn.setFont(uiFont.deriveFont(16f));
        noBtn.setBorder(new RoundedBorder(15, BROWN));
        noBtn.setFocusPainted(false);
        noBtn.addActionListener(e -> { result[0] = false; dialog.dispose(); });
        panel.add(noBtn);

        dialog.setVisible(true);
        return result[0];
    }

    class RentData {
        String itemName, renterId, renterName;
        LocalDate rentDate, dueDate;
        boolean isReturned;

        public RentData(String item, String id, String name, LocalDate start, LocalDate end, boolean returned) {
            this.itemName = item; this.renterId = id; this.renterName = name;
            this.rentDate = start; this.dueDate = end; this.isReturned = returned;
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