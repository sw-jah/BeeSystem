package admin;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime; 
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class AdminSpaceManageFrame extends JFrame {

    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(139, 90, 43);
    private static final Color RED_CANCEL = new Color(255, 100, 100);
    private static final Color GRAY_TEXT = new Color(150, 150, 150);
    private static final Color POPUP_BG = new Color(255, 250, 205);

    private static Font uiFont;
    static {
        try {
            InputStream is = AdminSpaceManageFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) {
            uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }

    private JPanel listPanel;
    private ArrayList<SpaceData> reserveList = new ArrayList<>();

    public AdminSpaceManageFrame() {
        setTitle("관리자 - 장소 대여 관리");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        reserveList.add(new SpaceData("스터디룸 A", "20231234", "김슈니", LocalDate.now().plusDays(1), LocalTime.of(14, 0), LocalTime.of(16, 0), 4, "예약중"));
        reserveList.add(new SpaceData("세미나실 B", "20210001", "이멋사", LocalDate.now(), LocalTime.now().minusMinutes(20), LocalTime.now().plusHours(1), 6, "예약중"));
        reserveList.add(new SpaceData("세미나실 C", "20240099", "이빠름", LocalDate.now(), LocalTime.now().minusMinutes(5), LocalTime.now().plusHours(2), 4, "예약중"));

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

        JLabel titleLabel = new JLabel("장소 대여 관리");
        titleLabel.setFont(uiFont.deriveFont(32f));
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(30, 20, 300, 40);
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

        listPanel = new JPanel();
        listPanel.setLayout(null);
        listPanel.setBackground(BG_MAIN);

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBounds(30, 100, 730, 440);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane);
    }

    private void refreshList() {
        listPanel.removeAll();
        int yPos = 10;

        for (SpaceData data : reserveList) {
            JPanel card = createSpaceCard(data);
            card.setBounds(10, yPos, 690, 110);
            listPanel.add(card);
            yPos += 120;
        }

        listPanel.setPreferredSize(new Dimension(690, yPos));
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createSpaceCard(SpaceData data) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new RoundedBorder(15, Color.LIGHT_GRAY));

        JLabel roomLabel = new JLabel(data.roomName);
        roomLabel.setFont(uiFont.deriveFont(20f));
        roomLabel.setForeground(BROWN);
        roomLabel.setBounds(20, 15, 200, 30);
        panel.add(roomLabel);

        int warn = PenaltyManager.getWarningCount(data.userId);
        String statusText = data.status;
        if(warn > 0) statusText += " (경고 " + warn + "회)";
        
        JLabel statusLabel = new JLabel(statusText);
        statusLabel.setFont(uiFont.deriveFont(14f));
        statusLabel.setForeground(data.status.equals("취소됨") ? RED_CANCEL : new Color(100, 180, 100));
        statusLabel.setBounds(230, 20, 200, 20);
        panel.add(statusLabel);

        JLabel userLabel = new JLabel("예약자: " + data.userId + " | " + data.userName + " (" + data.peopleCount + "명)");
        userLabel.setFont(uiFont.deriveFont(14f));
        userLabel.setForeground(GRAY_TEXT);
        userLabel.setBounds(20, 50, 300, 20);
        panel.add(userLabel);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        String timeStr = data.date.format(dtf) + "  " + data.startTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " ~ " + data.endTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        JLabel timeLabel = new JLabel(timeStr);
        timeLabel.setFont(uiFont.deriveFont(16f));
        timeLabel.setForeground(BROWN);
        timeLabel.setBounds(20, 75, 400, 25);
        panel.add(timeLabel);

        JButton cancelBtn = new JButton();
        
        if (data.status.equals("취소됨")) {
            cancelBtn.setText("취소완료");
            cancelBtn.setEnabled(false);
            cancelBtn.setBackground(new Color(240, 240, 240));
            cancelBtn.setBorder(new RoundedBorder(10, Color.LIGHT_GRAY));
        } else {
            cancelBtn.setText("미입실 취소");
            cancelBtn.setBackground(RED_CANCEL);
            cancelBtn.setForeground(Color.WHITE);
            cancelBtn.setBorder(new RoundedBorder(10, RED_CANCEL));
            
            // [수정] 이쁜 팝업 적용
            cancelBtn.addActionListener(e -> {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime reserveStart = LocalDateTime.of(data.date, data.startTime);
                LocalDateTime cancelAllowedTime = reserveStart.plusMinutes(10);

                if (now.isBefore(cancelAllowedTime)) {
                    String msg = "아직 미입실 처리를 할 수 없습니다.\n" +
                                 "입장 시간 10분 후 (" + cancelAllowedTime.format(DateTimeFormatter.ofPattern("HH:mm")) + ") 부터\n취소 가능합니다.";
                    showMsgPopup("취소 불가", msg);
                    return; 
                }

                boolean confirm = showConfirmPopup("패널티 부여", 
                    "[" + data.userName + "]님 미입실로 취소하시겠습니까?\n(누적 시 패널티 부여)");
                
                if (confirm) {
                    data.status = "취소됨";
                    PenaltyManager.addWarning(data.userId);
                    
                    if(PenaltyManager.isBanned(data.userId)) {
                        showMsgPopup("예약 정지", "🚫 경고 2회 누적!\n해당 회원은 7일간 예약이 정지되었습니다.");
                    } else {
                        int currentWarn = PenaltyManager.getWarningCount(data.userId);
                        showMsgPopup("경고 부여", "경고가 부여되었습니다.\n(현재 누적: " + currentWarn + "회)");
                    }
                    
                    refreshList();
                }
            });
        }
        
        cancelBtn.setFont(uiFont.deriveFont(14f));
        cancelBtn.setBounds(530, 35, 130, 40);
        cancelBtn.setFocusPainted(false);
        panel.add(cancelBtn);

        return panel;
    }

    // ==========================================
    // 🎨 이쁜 팝업 메소드
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
        yesBtn.addActionListener(e -> { result[0] = true; dialog.dispose(); });
        panel.add(yesBtn);

        JButton noBtn = new JButton("아니오");
        noBtn.setBounds(220, 160, 120, 45);
        noBtn.setBackground(BROWN);
        noBtn.setForeground(Color.WHITE);
        noBtn.setFont(uiFont.deriveFont(16f));
        noBtn.setBorder(new RoundedBorder(15, BROWN));
        noBtn.addActionListener(e -> { result[0] = false; dialog.dispose(); });
        panel.add(noBtn);

        dialog.setVisible(true);
        return result[0];
    }

    class SpaceData {
        String roomName; String userId; String userName;
        LocalDate date; LocalTime startTime; LocalTime endTime;
        int peopleCount; String status;

        public SpaceData(String r, String i, String n, LocalDate d, LocalTime s, LocalTime e, int p, String st) {
            this.roomName = r; userId = i; userName = n; date = d; startTime = s; endTime = e; peopleCount = p; status = st;
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