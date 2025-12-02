package admin;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;

public class AdminLotteryAddDialog extends JDialog {

    private static final Color BG_YELLOW = new Color(255, 250, 205);
    private static final Color BROWN = new Color(139, 90, 43);
    
    private static Font uiFont;
    static {
        try {
            InputStream is = AdminLotteryAddDialog.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 12);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);
        } catch (Exception e) { uiFont = new Font("맑은 고딕", Font.PLAIN, 12); }
    }

    private AdminLotteryFrame parent;
    
    // 입력 필드들
    private JTextField titleField;
    private JTextField prizeField;
    private JSpinner countSpinner;
    private JTextField annDateField;  // 발표일
    private JTextField appPeriodField;// 응모기간
    private JTextField locField;      // 수령장소
    private JTextField pickPeriodField;// 수령기간

    public AdminLotteryAddDialog(AdminLotteryFrame parent) {
        super(parent, "경품 추첨 등록", true);
        this.parent = parent;

        setSize(450, 550); // 필드가 많아져서 창 크기를 늘림
        setLocationRelativeTo(parent);
        setLayout(null);
        getContentPane().setBackground(BG_YELLOW);

        initUI();
        setVisible(true);
    }

    private void initUI() {
        JLabel titleLabel = new JLabel("새로운 경품 행사 등록");
        titleLabel.setFont(uiFont.deriveFont(18f));
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(30, 20, 300, 30);
        add(titleLabel);

        int yPos = 70;
        int gap = 60; // 간격 조정

        // 1. 제목
        addLabel(yPos, "이벤트 제목 (회차 자동)");
        titleField = createField(yPos + 25);
        add(titleField);
        yPos += gap;

        // 2. 경품명 & 인원 (같은 라인에 배치)
        addLabel(yPos, "경품명");
        prizeField = new JTextField();
        prizeField.setBounds(30, yPos + 25, 250, 30);
        prizeField.setFont(uiFont.deriveFont(14f));
        add(prizeField);

        JLabel countLabel = new JLabel("인원");
        countLabel.setFont(uiFont.deriveFont(14f));
        countLabel.setForeground(BROWN);
        countLabel.setBounds(300, yPos, 50, 20);
        add(countLabel);

        countSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        countSpinner.setBounds(300, yPos + 25, 100, 30);
        add(countSpinner);
        yPos += gap;

        // 3. 당첨자 발표일
        addLabel(yPos, "당첨자 발표 일시 (예: 2024-05-20 14:00)");
        annDateField = createField(yPos + 25);
        add(annDateField);
        yPos += gap;

        // 4. 응모 기간
        addLabel(yPos, "응모 기간 (예: 05.01 ~ 05.15)");
        appPeriodField = createField(yPos + 25);
        add(appPeriodField);
        yPos += gap;

        // 5. 수령 장소
        addLabel(yPos, "수령 장소 (예: 학생회관 2층)");
        locField = createField(yPos + 25);
        add(locField);
        yPos += gap;

        // 6. 수령 기간
        addLabel(yPos, "수령 기간 (예: 05.21 ~ 05.25)");
        pickPeriodField = createField(yPos + 25);
        add(pickPeriodField);
        yPos += gap + 10;

        // 버튼
        JButton cancelBtn = new JButton("취소");
        cancelBtn.setBounds(100, yPos, 100, 40);
        cancelBtn.setBackground(new Color(200, 200, 200));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dispose());
        add(cancelBtn);

        JButton okBtn = new JButton("등록");
        okBtn.setBounds(230, yPos, 100, 40);
        okBtn.setBackground(BROWN);
        okBtn.setForeground(Color.WHITE);
        okBtn.setFocusPainted(false);
        okBtn.addActionListener(e -> saveData());
        add(okBtn);
    }

    private void addLabel(int y, String text) {
        JLabel l = new JLabel(text);
        l.setFont(uiFont.deriveFont(14f));
        l.setForeground(BROWN);
        l.setBounds(30, y, 300, 20);
        add(l);
    }

    private JTextField createField(int y) {
        JTextField f = new JTextField();
        f.setBounds(30, y, 370, 30);
        f.setFont(uiFont.deriveFont(14f));
        return f;
    }

    private void saveData() {
        String title = titleField.getText().trim();
        String prize = prizeField.getText().trim();
        int count = (int) countSpinner.getValue();
        
        // 추가 정보
        String annDate = annDateField.getText().trim();
        String appPeriod = appPeriodField.getText().trim();
        String loc = locField.getText().trim();
        String pickPeriod = pickPeriodField.getText().trim();

        if (title.isEmpty() || prize.isEmpty() || annDate.isEmpty() || 
            appPeriod.isEmpty() || loc.isEmpty() || pickPeriod.isEmpty()) {
            JOptionPane.showMessageDialog(this, "모든 정보를 입력해주세요.");
            return;
        }

        // 부모 창으로 데이터 전달
        parent.addRound(title, prize, count, annDate, appPeriod, loc, pickPeriod);
        JOptionPane.showMessageDialog(this, "등록되었습니다.");
        dispose();
    }
}