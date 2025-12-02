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

    // ===============================
    // 🎨 컬러 테마
    // ===============================
    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(139, 90, 43);
    private static final Color RED_OVERDUE = new Color(255, 80, 80); // 연체 색상
    private static final Color GREEN_DONE = new Color(100, 180, 100); // 완료 색상

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

        // --- 테스트 데이터 생성 ---
        // 1. 정상 대여 (반납 2일 남음)
        rentList.add(new RentData("노트북", "20231234", "김슈니", LocalDate.now().minusDays(1), LocalDate.now().plusDays(2), false));
        // 2. 연체된 대여 (반납일 3일 지남) -> 빨간색 떠야 함
        rentList.add(new RentData("C타입 충전기", "20210001", "이멋사", LocalDate.now().minusDays(5), LocalDate.now().minusDays(3), false));
        // 3. 당일 반납 (D-Day)
        rentList.add(new RentData("우산", "20245678", "박새내", LocalDate.now(), LocalDate.now(), false));
        // 4. 이미 반납 완료된 항목
        rentList.add(new RentData("전공책(자바)", "20229999", "최코딩", LocalDate.now().minusDays(10), LocalDate.now().minusDays(5), true));

        initUI();
        refreshList();
        setVisible(true);
    }

    private void initUI() {
        // --- 헤더 영역 ---
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

        // 메인으로 가기 버튼
        JButton homeBtn = new JButton("🏠 메인으로");
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

        // --- 리스트 영역 ---
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
            card.setBounds(10, yPos, 690, 100); // 카드 크기
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

        // 1. 물품명
        JLabel nameLabel = new JLabel(data.itemName);
        nameLabel.setFont(uiFont.deriveFont(20f));
        nameLabel.setForeground(BROWN);
        nameLabel.setBounds(20, 15, 250, 30);
        panel.add(nameLabel);

        // 2. 대여자 정보 (학번 | 이름)
        JLabel renterLabel = new JLabel("대여자: " + data.renterId + " | " + data.renterName);
        renterLabel.setFont(uiFont.deriveFont(14f));
        renterLabel.setForeground(Color.GRAY);
        renterLabel.setBounds(20, 50, 250, 20);
        panel.add(renterLabel);

        // 3. 날짜 정보
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yy.MM.dd");
        String dateStr = data.rentDate.format(dtf) + " ~ " + data.dueDate.format(dtf);
        JLabel dateLabel = new JLabel(dateStr);
        dateLabel.setFont(uiFont.deriveFont(14f));
        dateLabel.setForeground(Color.GRAY);
        dateLabel.setBounds(20, 70, 250, 20);
        panel.add(dateLabel);

        // 4. D-Day 및 상태 계산
        long daysDiff = ChronoUnit.DAYS.between(LocalDate.now(), data.dueDate);
        String dDayStr;
        Color dDayColor;

        if (data.isReturned) {
            dDayStr = "반납완료";
            dDayColor = GREEN_DONE;
        } else {
            if (daysDiff > 0) {
                dDayStr = "D-" + daysDiff;
                dDayColor = BROWN;
            } else if (daysDiff == 0) {
                dDayStr = "D-Day";
                dDayColor = BROWN;
            } else {
                dDayStr = "D+" + Math.abs(daysDiff) + " (연체)";
                dDayColor = RED_OVERDUE; // 연체 시 빨간색
            }
        }

        JLabel statusLabel = new JLabel(dDayStr, SwingConstants.RIGHT);
        statusLabel.setFont(uiFont.deriveFont(Font.BOLD, 22f));
        statusLabel.setForeground(dDayColor);
        statusLabel.setBounds(300, 35, 200, 30);
        panel.add(statusLabel);

        // 5. 반납 확인 버튼
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
            
            // 버튼 클릭 이벤트
            actionBtn.addActionListener(e -> {
                int result = JOptionPane.showConfirmDialog(this, 
                    "[" + data.itemName + "] 반납 처리를 하시겠습니까?", 
                    "반납 확인", JOptionPane.YES_NO_OPTION);
                
                if (result == JOptionPane.YES_OPTION) {
                    data.isReturned = true; // 상태 변경
                    refreshList(); // 새로고침
                }
            });
        }
        actionBtn.setFont(uiFont.deriveFont(14f));
        actionBtn.setBounds(530, 30, 130, 40);
        actionBtn.setFocusPainted(false);
        panel.add(actionBtn);

        return panel;
    }

    // --- 데이터 클래스 ---
    class RentData {
        String itemName;
        String renterId;
        String renterName;
        LocalDate rentDate;
        LocalDate dueDate;
        boolean isReturned;

        public RentData(String item, String id, String name, LocalDate start, LocalDate end, boolean returned) {
            this.itemName = item;
            this.renterId = id;
            this.renterName = name;
            this.rentDate = start;
            this.dueDate = end;
            this.isReturned = returned;
        }
    }

    // --- 둥근 테두리 클래스 ---
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