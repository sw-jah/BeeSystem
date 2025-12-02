package admin;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// [중요] LotteryManager 사용
import admin.LotteryManager.Applicant;
import admin.LotteryManager.LotteryRound;

public class AdminLotteryFrame extends JFrame {

    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(139, 90, 43);
    private static final Color BLUE_BTN = new Color(100, 150, 255);
    private static final Color RED_WIN = new Color(255, 100, 100);
    private static final Color GRAY_LOSE = new Color(150, 150, 150);

    private static Font uiFont;
    static {
        try {
            InputStream is = AdminLotteryFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
        } catch (Exception e) { uiFont = new Font("맑은 고딕", Font.PLAIN, 14); }
    }

    private JComboBox<String> roundCombo;
    private JPanel listPanel;
    private JButton drawBtn;
    private JLabel infoLabel;

    public AdminLotteryFrame() {
        setTitle("관리자 - 경품 추첨");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initUI();
        refreshList();
        setVisible(true);
    }

    private void initUI() {
        // 헤더
        JPanel headerPanel = new JPanel(null);
        headerPanel.setBounds(0, 0, 850, 80);
        headerPanel.setBackground(HEADER_YELLOW);
        add(headerPanel);

        JLabel titleLabel = new JLabel("경품 추첨 관리");
        titleLabel.setFont(uiFont.deriveFont(32f));
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(30, 20, 300, 40);
        headerPanel.add(titleLabel);

        JButton homeBtn = new JButton("🏠 메인으로");
        homeBtn.setFont(uiFont.deriveFont(14f));
        homeBtn.setBackground(BROWN);
        homeBtn.setForeground(Color.WHITE);
        homeBtn.setBounds(700, 25, 110, 35);
        homeBtn.setBorder(new RoundedBorder(15, BROWN));
        homeBtn.setFocusPainted(false);
        homeBtn.addActionListener(e -> {
            new AdminMainFrame();
            dispose();
        });
        headerPanel.add(homeBtn);

        // 컨트롤 패널
        JPanel controlPanel = new JPanel(null);
        controlPanel.setBounds(30, 90, 780, 60);
        controlPanel.setBackground(BG_MAIN);
        add(controlPanel);

        JLabel comboLabel = new JLabel("진행 회차 :");
        comboLabel.setFont(uiFont.deriveFont(16f));
        comboLabel.setForeground(BROWN);
        comboLabel.setBounds(0, 15, 90, 30);
        controlPanel.add(comboLabel);

        // [수정] LotteryManager에서 데이터 가져오기
        List<LotteryRound> rounds = LotteryManager.getAllRounds();
        String[] roundNames = rounds.stream().map(r -> r.name).toArray(String[]::new);
        
        roundCombo = new JComboBox<>(roundNames);
        roundCombo.setFont(uiFont.deriveFont(14f));
        roundCombo.setBounds(90, 15, 300, 35);
        roundCombo.setBackground(Color.WHITE);
        roundCombo.addActionListener(e -> refreshList());
        controlPanel.add(roundCombo);

        JButton regBtn = new JButton("+ 추첨 등록");
        regBtn.setFont(uiFont.deriveFont(14f));
        regBtn.setBackground(BROWN);
        regBtn.setForeground(Color.WHITE);
        regBtn.setBounds(400, 15, 120, 35);
        regBtn.setBorder(new RoundedBorder(15, BROWN));
        regBtn.setFocusPainted(false);
        regBtn.addActionListener(e -> new AdminLotteryAddDialog(this));
        controlPanel.add(regBtn);

        drawBtn = new JButton("🎲 추첨 시작");
        drawBtn.setFont(uiFont.deriveFont(14f));
        drawBtn.setBackground(BLUE_BTN);
        drawBtn.setForeground(Color.WHITE);
        drawBtn.setBounds(530, 15, 120, 35);
        drawBtn.setBorder(new RoundedBorder(15, BLUE_BTN));
        drawBtn.setFocusPainted(false);
        drawBtn.addActionListener(e -> runLottery());
        controlPanel.add(drawBtn);

        // 정보 라벨
        infoLabel = new JLabel("");
        infoLabel.setFont(uiFont.deriveFont(13f));
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setVerticalAlignment(SwingConstants.TOP);
        infoLabel.setBounds(30, 155, 780, 60);
        add(infoLabel);

        // 리스트 헤더
        JPanel listHeader = new JPanel(new GridLayout(1, 4));
        listHeader.setBounds(30, 220, 780, 30);
        listHeader.setBackground(new Color(240, 240, 240));
        listHeader.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        String[] columns = {"응모자", "학번", "응모 횟수", "당첨 여부"};
        for (String col : columns) {
            JLabel l = new JLabel(col, SwingConstants.CENTER);
            l.setFont(uiFont.deriveFont(Font.BOLD, 14f));
            l.setForeground(BROWN);
            listHeader.add(l);
        }
        add(listHeader);

        listPanel = new JPanel(null);
        listPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBounds(30, 250, 780, 330);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane);
    }

    public void refreshList() {
        listPanel.removeAll();
        int selectedIdx = roundCombo.getSelectedIndex();
        if (selectedIdx < 0) {
            listPanel.repaint();
            return;
        }

        // [수정] LotteryManager 사용
        LotteryRound round = LotteryManager.getAllRounds().get(selectedIdx);
        
        infoLabel.setText("<html>" +
                "<span style='color:#8B5A2B; font-weight:bold;'>경품: " + round.prizeName + " (" + round.winnerCount + "명)</span> | " +
                "발표: " + round.announcementDate + " | 응모기간: " + round.applicationPeriod + "<br>" +
                "수령장소: " + round.pickupLocation + " | 수령기간: " + round.pickupPeriod +
                "</html>");

        if (round.isDrawn) {
            drawBtn.setText("추첨 완료");
            drawBtn.setEnabled(false);
            drawBtn.setBackground(Color.GRAY);
            drawBtn.setBorder(new RoundedBorder(15, Color.GRAY));
        } else {
            drawBtn.setText("🎲 추첨 시작");
            drawBtn.setEnabled(true);
            drawBtn.setBackground(BLUE_BTN);
            drawBtn.setBorder(new RoundedBorder(15, BLUE_BTN));
        }

        int yPos = 0;
        int rowHeight = 40;

        for (Applicant app : round.applicants) {
            JPanel row = new JPanel(new GridLayout(1, 4));
            row.setBounds(0, yPos, 780, rowHeight);
            row.setBackground(Color.WHITE);
            row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

            addCell(row, app.name, Color.BLACK);
            addCell(row, app.hakbun, Color.BLACK);
            addCell(row, app.count + "회", Color.BLACK);

            JLabel statusLabel = new JLabel(app.status, SwingConstants.CENTER);
            statusLabel.setFont(uiFont.deriveFont(14f));
            if (app.status.equals("당첨")) {
                statusLabel.setForeground(RED_WIN);
                statusLabel.setFont(uiFont.deriveFont(Font.BOLD, 14f));
            } else if (app.status.equals("미당첨")) {
                statusLabel.setForeground(GRAY_LOSE);
            } else {
                statusLabel.setForeground(BROWN);
            }
            row.add(statusLabel);

            listPanel.add(row);
            yPos += rowHeight;
        }

        listPanel.setPreferredSize(new Dimension(760, yPos));
        listPanel.revalidate();
        listPanel.repaint();
    }

    private void addCell(JPanel p, String text, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(uiFont.deriveFont(14f));
        l.setForeground(color);
        p.add(l);
    }

    private void runLottery() {
        int selectedIdx = roundCombo.getSelectedIndex();
        if (selectedIdx < 0) return;
        LotteryRound round = LotteryManager.getAllRounds().get(selectedIdx);

        int confirm = JOptionPane.showConfirmDialog(this, 
            "[" + round.name + "] 추첨을 시작하시겠습니까?\n" +
            "총 " + round.winnerCount + "명을 랜덤으로 선정합니다.",
            "추첨 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            List<Applicant> candidates = new ArrayList<>(round.applicants);
            Collections.shuffle(candidates);

            for (Applicant app : round.applicants) app.status = "미당첨";

            int pickCount = 0;
            Random random = new Random();

            while (pickCount < round.winnerCount && !candidates.isEmpty()) {
                int idx = random.nextInt(candidates.size());
                Applicant winner = candidates.get(idx);
                winner.status = "당첨";
                candidates.remove(idx);
                pickCount++;
            }

            round.isDrawn = true;
            refreshList();
            JOptionPane.showMessageDialog(this, "추첨이 완료되었습니다!");
        }
    }

    // [수정] 매니저에 추가하고 콤보박스 갱신
    public void addRound(String titleOnly, String prize, int count, 
                         String annDate, String appPeriod, String loc, String pickPeriod) {
        
        int nextRoundNum = LotteryManager.getAllRounds().size() + 1;
        String fullName = nextRoundNum + "회차: " + titleOnly;

        // 매니저에 저장
        LotteryManager.addRound(fullName, prize, count, annDate, appPeriod, loc, pickPeriod);
        
        // UI 갱신
        roundCombo.addItem(fullName);
        roundCombo.setSelectedItem(fullName);
        refreshList();
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
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
        }
    }
}