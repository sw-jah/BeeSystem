package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;

public class EventDetailFrame extends JFrame {

    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color NAV_BG = new Color(255, 255, 255);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(89, 60, 28);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157);
    private static final Color GREEN_PROGRESS = new Color(180, 230, 180);
    private static final Color ORANGE_CLOSED = new Color(255, 200, 180);
    private static final Color GRAY_BTN = new Color(180, 180, 180);
    private static final Color POPUP_BG = new Color(255, 250, 205);

    private static Font uiFont;

    static {
        try {
            InputStream is = EventDetailFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
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

    // [수정] 사용자 변수
    private String userName = "사용자";
    private int userPoint = 100;

    private String eventId;
    private String councilName;
    private String eventName;
    private String eventType;
    private String status;
    private int remainingSlots;
    private int totalSlots;
    private boolean isApplied = false;
    
    private String eventDescription = "소프트웨어융합학과 A+을 위한 간식 행사에 초대합니다!";
    private String eventDate = "12월 1일 오전 10시 ~ 오후 4시";
    private String eventPlace = "누리관 지하 1층";

    public EventDetailFrame(String eventId, String councilName, String eventName, 
                           String eventType, String status, int remainingSlots, int totalSlots) {
        this.eventId = eventId;
        this.councilName = councilName;
        this.eventName = eventName;
        this.eventType = eventType;
        this.status = status;
        this.remainingSlots = remainingSlots;
        this.totalSlots = totalSlots;

        setTitle("서울여대 꿀단지 - " + eventName);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initUI();

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

        // [수정] 프로필 아이콘 제거
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 25));
        userInfoPanel.setBounds(400, 0, 380, 80);
        userInfoPanel.setOpaque(false);

        JLabel userInfoText = new JLabel("[" + userName + "]님 | 보유 꿀 : " + userPoint + " | 로그아웃");
        userInfoText.setFont(uiFont.deriveFont(14f));
        userInfoText.setForeground(BROWN);
        userInfoText.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // [수정] 로그아웃 연결
        userInfoText.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                showLogoutPopup();
            }
        });

        userInfoPanel.add(userInfoText);
        headerPanel.add(userInfoPanel);

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new GridLayout(1, 6));
        navPanel.setBounds(0, 80, 800, 50);
        navPanel.setBackground(NAV_BG);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        add(navPanel);

        String[] menus = {"물품대여", "과행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"};
        for (int i = 0; i < menus.length; i++) {
            JButton menuBtn = createNavButton(menus[i], i == 1);
            navPanel.add(menuBtn);
        }

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(null);
        contentPanel.setBounds(0, 130, 800, 470);
        contentPanel.setBackground(BG_MAIN);
        add(contentPanel);

        JButton backButton = new JButton("이전 화면");
        backButton.setFont(uiFont.deriveFont(14f));
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(GRAY_BTN);
        backButton.setBounds(680, 20, 90, 30);
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> {
            new EventListFrame();
            dispose();
        });
        contentPanel.add(backButton);

        JLabel statusLabel = new JLabel(status);
        statusLabel.setFont(uiFont.deriveFont(Font.BOLD, 15f));
        statusLabel.setForeground(BROWN);
        statusLabel.setBounds(50, 70, 110, 35);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(status.equals("신청마감") ? ORANGE_CLOSED : GREEN_PROGRESS);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        contentPanel.add(statusLabel);

        JLabel nameLabel = new JLabel(eventName);
        nameLabel.setFont(uiFont.deriveFont(Font.BOLD, 32f));
        nameLabel.setForeground(Color.BLACK);
        nameLabel.setBounds(50, 115, 600, 40);
        contentPanel.add(nameLabel);

        JTextArea descArea = new JTextArea(eventDescription);
        descArea.setFont(uiFont.deriveFont(16f));
        descArea.setForeground(new Color(100, 100, 100));
        descArea.setBackground(BG_MAIN);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setBorder(null);
        descArea.setBounds(50, 165, 650, 40);
        contentPanel.add(descArea);

        JLabel dateLabel = new JLabel("일시 : " + eventDate);
        dateLabel.setFont(uiFont.deriveFont(17f));
        dateLabel.setForeground(new Color(80, 80, 80));
        dateLabel.setBounds(50, 220, 600, 25);
        contentPanel.add(dateLabel);

        JLabel targetLabel = new JLabel("대상 : " + getEventTarget());
        targetLabel.setFont(uiFont.deriveFont(17f));
        targetLabel.setForeground(new Color(80, 80, 80));
        targetLabel.setBounds(50, 250, 600, 25);
        contentPanel.add(targetLabel);

        JLabel placeLabel = new JLabel("장소 : " + eventPlace);
        placeLabel.setFont(uiFont.deriveFont(17f));
        placeLabel.setForeground(new Color(80, 80, 80));
        placeLabel.setBounds(50, 280, 600, 25);
        contentPanel.add(placeLabel);

        JLabel slotsLabel = new JLabel("남은 인원 : " + remainingSlots + "명");
        slotsLabel.setFont(uiFont.deriveFont(17f));
        slotsLabel.setForeground(new Color(80, 80, 80));
        slotsLabel.setBounds(50, 310, 600, 25);
        contentPanel.add(slotsLabel);

        if (!status.equals("신청마감") && remainingSlots > 0) {
            JButton applyButton = new JButton("신청하기");
            applyButton.setFont(uiFont.deriveFont(Font.BOLD, 18f));
            applyButton.setForeground(Color.WHITE);
            applyButton.setBackground(BROWN);
            applyButton.setBounds(570, 340, 180, 50);
            applyButton.setFocusPainted(false);
            applyButton.setBorderPainted(false);
            applyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            applyButton.addActionListener(e -> {
                if (isApplied) {
                    showSimplePopup("알림", "이미 신청하셨습니다.");
                } else {
                    if (canApply()) {
                        if (eventType.equals("간식")) {
                            showSecretCodeDialog(slotsLabel, statusLabel, applyButton);
                        } else {
                            applyEvent(slotsLabel, statusLabel, applyButton);
                        }
                    } else {
                        showSimplePopup("알림", "해당 행사에 참여할 수 없습니다.\n학생회비를 확인해주세요.");
                    }
                }
            });
            contentPanel.add(applyButton);
        }
    }

    private boolean canApply() {
        return true;
    }

    private String getEventTarget() {
        if (councilName.equals("총학생회")) {
            return "학교 학생회비 납부자 (전체 학과)";
        } else if (councilName.contains("대학")) {
            return councilName + " 소속 학생 + 학교 학생회비 납부자";
        } else {
            return councilName + " 학생 + 과 학생회비 납부자";
        }
    }

    private void showSecretCodeDialog(JLabel slotsLabel, JLabel statusLabel, JButton applyButton) {
        JDialog dialog = new JDialog(this, "비밀코드 입력", true);
        dialog.setSize(450, 300);
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

        JLabel closeBtn = new JLabel("X");
        closeBtn.setFont(uiFont.deriveFont(20f));
        closeBtn.setForeground(BROWN);
        closeBtn.setBounds(410, 20, 20, 20);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dialog.dispose(); }
        });
        panel.add(closeBtn);

        JLabel msgLabel = new JLabel("비밀코드를 입력해주세요", SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(20f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(50, 60, 350, 30);
        panel.add(msgLabel);

        JPanel codePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        codePanel.setBounds(90, 110, 270, 50);
        codePanel.setOpaque(false);

        JPasswordField[] codeFields = new JPasswordField[4];
        for (int i = 0; i < 4; i++) {
            JPasswordField field = new JPasswordField(1);
            field.setFont(uiFont.deriveFont(24f));
            field.setHorizontalAlignment(SwingConstants.CENTER);
            field.setPreferredSize(new Dimension(50, 50));
            field.setBackground(Color.WHITE);
            field.setBorder(BorderFactory.createLineBorder(BROWN, 2));
            field.setForeground(BROWN);
            
            final int index = i;
            field.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    if (field.getPassword().length >= 1) {
                        e.consume();
                        if (index < 3) codeFields[index + 1].requestFocus();
                    }
                }
            });
            codeFields[i] = field;
            codePanel.add(field);
        }
        panel.add(codePanel);

        JButton confirmBtn = new JButton("확인");
        confirmBtn.setFont(uiFont.deriveFont(16f));
        confirmBtn.setBackground(BROWN);
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setBounds(150, 200, 150, 45);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setBorder(new RoundedBorder(15, BROWN, 1));
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmBtn.addActionListener(e -> {
            String inputCode = "";
            for (JPasswordField field : codeFields) inputCode += new String(field.getPassword());
            
            if (inputCode.equals("1234")) {
                dialog.dispose();
                applyEvent(slotsLabel, statusLabel, applyButton);
            } else {
                showSimplePopup("오류", "비밀코드가 일치하지 않습니다.");
            }
        });
        panel.add(confirmBtn);

        dialog.setVisible(true);
    }

    private void applyEvent(JLabel slotsLabel, JLabel statusLabel, JButton applyButton) {
        remainingSlots--;
        slotsLabel.setText("남은 인원 : " + remainingSlots + "명");
        isApplied = true;
        showSimplePopup("성공", "신청이 완료되었습니다.");

        if (remainingSlots == 0) {
            applyButton.setVisible(false);
            statusLabel.setText("신청마감");
            statusLabel.setBackground(ORANGE_CLOSED);
        }
    }

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

    // [수정] 로그아웃 팝업
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

    // [수정] 네비게이션 연결
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
                    if (text.equals("과행사")) return;
                    
                    if (text.equals("물품대여")) {
                        new ItemListFrame(); dispose();
                    } else if (text.equals("공간대여")) {
                        new SpaceRentFrame(); dispose();
                    } else if (text.equals("마이페이지")) {
                        new MainFrame(); dispose();
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new EventDetailFrame("1", "소프트웨어융합학과", "기말 간식 행사", 
                               "간식", "진행중", 15, 20)
        );
    }
}