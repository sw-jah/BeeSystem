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

    // ===============================
    // 📅 행사 정보
    // TODO: DB 연동 시 EventDTO 객체로 변경
    // ===============================
    private String eventId;
    private String councilName;
    private String eventName;
    private String eventType; // "간식" or "참여"
    private String status;
    private int remainingSlots;
    private int totalSlots;
    private boolean isApplied = false;
    
    // TODO: DB에서 가져올 추가 정보
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

        String[] menus = {"물품대여", "과행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"};
        for (int i = 0; i < menus.length; i++) {
            JButton menuBtn = createNavButton(menus[i], i == 1);
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
            new EventListFrame();
            dispose();
        });
        contentPanel.add(backButton);

        // 상태 라벨 (진행중/사전신청/신청마감)
        JLabel statusLabel = new JLabel(status);
        statusLabel.setFont(uiFont.deriveFont(Font.BOLD, 15f));
        statusLabel.setForeground(BROWN);
        statusLabel.setBounds(50, 70, 110, 35);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(status.equals("신청마감") ? ORANGE_CLOSED : GREEN_PROGRESS);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        contentPanel.add(statusLabel);

        // 행사명
        JLabel nameLabel = new JLabel(eventName);
        nameLabel.setFont(uiFont.deriveFont(Font.BOLD, 32f));
        nameLabel.setForeground(Color.BLACK);
        nameLabel.setBounds(50, 115, 600, 40);
        contentPanel.add(nameLabel);

        // 행사 상세 설명
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

        // 행사 정보
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

        // 신청하기 버튼 (신청 가능할 때만)
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
                    showMessageDialog("이미 신청하셨습니다.");
                } else {
                    // TODO: DB에서 학생의 학생회비 납부 여부 확인
                    if (canApply()) {
                        // 간식 행사면 비밀코드 입력, 참여 행사면 바로 신청
                        if (eventType.equals("간식")) {
                            showSecretCodeDialog(slotsLabel, statusLabel, applyButton);
                        } else {
                            applyEvent(slotsLabel, statusLabel, applyButton);
                        }
                    } else {
                        showMessageDialog("해당 행사에 참여할 수 없습니다.\n학생회비를 확인해주세요.");
                    }
                }
            });
            contentPanel.add(applyButton);
        }
    }

    // ===============================
    // 🎯 참여 가능 여부 확인
    // TODO: DB 연동 시 실제 학생회비 확인
    // ===============================
    private boolean canApply() {
        // 총학생회 -> 학교 학생회비 납부자만
        // 단과대학 -> 해당 단과대학 학생 + 학교 학생회비
        // 학과 -> 해당 학과 학생 + 과 학생회비
        
        // 임시: 모두 참여 가능으로 설정
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

    // 비밀코드 입력 다이얼로그 (간식 행사용)
    private void showSecretCodeDialog(JLabel slotsLabel, JLabel statusLabel, JButton applyButton) {
        JDialog dialog = new JDialog(this, "", true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new RoundedBorder(20, BROWN, 3));

        // 헤더
        JPanel headerPanel = new JPanel();
        headerPanel.setBounds(0, 0, 450, 50);
        headerPanel.setBackground(HIGHLIGHT_YELLOW);
        headerPanel.setLayout(null);

        JLabel headerLabel = new JLabel("○○○");
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

        // 비밀코드 입력 영역
        JLabel msgLabel = new JLabel("비밀코드를 입력해주세요", SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(16f));
        msgLabel.setForeground(new Color(100, 100, 100));
        msgLabel.setBounds(50, 80, 350, 30);
        panel.add(msgLabel);

        // 4자리 비밀코드 입력 필드
        JPanel codePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        codePanel.setBounds(90, 120, 270, 50);
        codePanel.setOpaque(false);

        JPasswordField[] codeFields = new JPasswordField[4];
        for (int i = 0; i < 4; i++) {
            JPasswordField field = new JPasswordField(1);
            field.setFont(uiFont.deriveFont(24f));
            field.setHorizontalAlignment(SwingConstants.CENTER);
            field.setPreferredSize(new Dimension(50, 50));
            field.setBackground(new Color(220, 220, 230));
            field.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 2));
            
            final int index = i;
            field.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    if (field.getPassword().length >= 1) {
                        e.consume();
                        if (index < 3) {
                            codeFields[index + 1].requestFocus();
                        }
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
        confirmBtn.setBounds(150, 210, 150, 45);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setBorderPainted(false);
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmBtn.addActionListener(e -> {
            // TODO: DB에서 비밀코드 확인
            String inputCode = "";
            for (JPasswordField field : codeFields) {
                inputCode += new String(field.getPassword());
            }
            
            // 임시 비밀코드: 1234
            if (inputCode.equals("1234")) {
                dialog.dispose();
                applyEvent(slotsLabel, statusLabel, applyButton);
            } else {
                JOptionPane.showMessageDialog(dialog, "비밀코드가 일치하지 않습니다.");
            }
        });
        panel.add(confirmBtn);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    // 행사 신청 처리
    private void applyEvent(JLabel slotsLabel, JLabel statusLabel, JButton applyButton) {
        // TODO: DB에 신청 정보 저장
        remainingSlots--;
        slotsLabel.setText("남은 인원 : " + remainingSlots + "명");
        isApplied = true;
        showMessageDialog("신청이 완료되었습니다.");

        // 남은 인원이 0이면 신청마감으로 변경
        if (remainingSlots == 0) {
            applyButton.setVisible(false);
            statusLabel.setText("신청마감");
            statusLabel.setBackground(ORANGE_CLOSED);
        }
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

        JLabel msgLabel = new JLabel("<html><center>" + message.replace("\n", "<br>") + "</center></html>", 
                                     SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(20f));
        msgLabel.setForeground(new Color(100, 100, 100));
        msgLabel.setBounds(50, 80, 350, 70);
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
                    if (text.equals("과행사")) {
                        new EventListFrame();
                        dispose();
                    } else if (text.equals("물품대여")) {
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
        SwingUtilities.invokeLater(() ->
            new EventDetailFrame("1", "소프트웨어융합학과", "기말 간식 행사", 
                               "간식", "진행중", 15, 20)
        );
    }
}