package council;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import council.EventManager.FeeType; // [중요] FeeType 임포트

public class CouncilEventAddDialog extends JDialog {

    private static final Color BG_WHITE = new Color(255, 255, 255);
    private static final Color BROWN = new Color(139, 90, 43);
    private static final Color POPUP_BG = new Color(255, 250, 205);
    
    private static Font uiFont;
    static {
        try {
            InputStream is = CouncilEventAddDialog.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 12);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);
        } catch (Exception e) { uiFont = new Font("맑은 고딕", Font.PLAIN, 12); }
    }

    private CouncilMainFrame parent;
    private EventManager.EventData currentEvent;
    
    // [수정] 주최자 정보 저장을 위한 변수 추가
    private String councilId;
    private String councilName;

    private JTextField titleField, dateField, locField, startField, endField, totalField, codeField;
    private JComboBox<String> feeCombo; // [추가] 회비 선택 콤보박스
    private JCheckBox codeCheck;
    private JTextArea descArea;

    // [수정] 생성자: 학생회 ID와 이름(id, name)을 받는 파라미터 추가
    public CouncilEventAddDialog(CouncilMainFrame parent, EventManager.EventData event, String id, String name) {
        super(parent, event == null ? "새 행사 등록" : "행사 수정", true);
        this.parent = parent;
        this.currentEvent = event;
        this.councilId = id;
        this.councilName = name;

        setSize(500, 750); // 높이 약간 증가
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_WHITE);

        initUI();
        if (event != null) loadData();
        setVisible(true);
    }

    private void initUI() {
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 15));
        formPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        formPanel.setBackground(BG_WHITE);

        titleField = addInput(formPanel, "행사명");
        dateField = addInput(formPanel, "행사 일시 (yyyy-MM-dd HH:mm)");
        locField = addInput(formPanel, "장소 (예: 50주년 기념관)");
        startField = addInput(formPanel, "신청 시작 일시 (yyyy-MM-dd HH:mm)");
        endField = addInput(formPanel, "신청 종료 일시 (yyyy-MM-dd HH:mm)");
        totalField = addInput(formPanel, "총 모집 인원 (숫자만)");

        // [추가] 납부 대상 선택 (회비 종류)
        JPanel feePanel = new JPanel(new BorderLayout(0, 5));
        feePanel.setBackground(BG_WHITE);
        JLabel feeLabel = new JLabel("납부 대상 (참여 조건)");
        feeLabel.setFont(uiFont.deriveFont(14f));
        feeLabel.setForeground(BROWN);
        
        String[] feeOptions = { FeeType.NONE.getLabel(), FeeType.SCHOOL.getLabel(), FeeType.DEPT.getLabel() };
        feeCombo = new JComboBox<>(feeOptions);
        feeCombo.setFont(uiFont.deriveFont(14f));
        feeCombo.setBackground(Color.WHITE);
        
        feePanel.add(feeLabel, BorderLayout.NORTH);
        feePanel.add(feeCombo, BorderLayout.CENTER);
        formPanel.add(feePanel);

        // 비밀코드 섹션
        JPanel codePanel = new JPanel(new BorderLayout(10, 0));
        codePanel.setBackground(BG_WHITE);
        codeCheck = new JCheckBox("비밀코드 사용");
        codeCheck.setFont(uiFont.deriveFont(14f));
        codeCheck.setBackground(BG_WHITE);
        codeCheck.setForeground(BROWN);
        codePanel.add(codeCheck, BorderLayout.WEST);
        
        codeField = new JTextField();
        codeField.setFont(uiFont.deriveFont(14f));
        codeField.setEnabled(false);
        codePanel.add(codeField, BorderLayout.CENTER);
        
        codeCheck.addActionListener(e -> codeField.setEnabled(codeCheck.isSelected()));
        formPanel.add(codePanel);

        // 상세설명
        JLabel descLabel = new JLabel("상세 설명");
        descLabel.setFont(uiFont.deriveFont(14f));
        descLabel.setForeground(BROWN);
        formPanel.add(descLabel);
        
        descArea = new JTextArea(4, 20);
        descArea.setFont(uiFont.deriveFont(14f));
        descArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        descArea.setLineWrap(true);
        formPanel.add(new JScrollPane(descArea));

        add(new JScrollPane(formPanel), BorderLayout.CENTER);

        // 버튼 영역
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        btnPanel.setBackground(BG_WHITE);
        
        JButton cancelBtn = createBtn("취소", Color.LIGHT_GRAY);
        cancelBtn.addActionListener(e -> dispose());
        btnPanel.add(cancelBtn);

        JButton saveBtn = createBtn("저장", BROWN);
        saveBtn.addActionListener(e -> saveData());
        btnPanel.add(saveBtn);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private JTextField addInput(JPanel p, String labelText) {
        JPanel row = new JPanel(new BorderLayout(0, 5));
        row.setBackground(BG_WHITE);
        JLabel l = new JLabel(labelText);
        l.setFont(uiFont.deriveFont(14f));
        l.setForeground(BROWN);
        JTextField tf = new JTextField();
        tf.setFont(uiFont.deriveFont(14f));
        tf.setPreferredSize(new Dimension(0, 35));
        row.add(l, BorderLayout.NORTH);
        row.add(tf, BorderLayout.CENTER);
        p.add(row);
        return tf;
    }

    private JButton createBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(uiFont.deriveFont(16f));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setPreferredSize(new Dimension(120, 45));
        b.setFocusPainted(false);
        return b;
    }

    private void loadData() {
        titleField.setText(currentEvent.title);
        dateField.setText(currentEvent.date.format(EventManager.DATE_FMT));
        locField.setText(currentEvent.location);
        startField.setText(currentEvent.startDateTime.format(EventManager.DATE_FMT));
        endField.setText(currentEvent.endDateTime.format(EventManager.DATE_FMT));
        totalField.setText(String.valueOf(currentEvent.totalCount));
        
        // [추가] 회비 정보 로드
        if (currentEvent.requiredFee != null) {
            feeCombo.setSelectedItem(currentEvent.requiredFee.getLabel());
        }

        if (currentEvent.secretCode != null) {
            codeCheck.setSelected(true);
            codeField.setEnabled(true);
            codeField.setText(currentEvent.secretCode);
        }
        descArea.setText(currentEvent.description);
    }

    private void saveData() {
        try {
            String title = titleField.getText().trim();
            if(title.isEmpty()) {
                showCustomAlertPopup("행사명을 입력하세요.");
                return;
            }
            
            LocalDateTime eventDate, startDate, endDate;
            try {
                eventDate = LocalDateTime.parse(dateField.getText().trim(), EventManager.DATE_FMT);
                startDate = LocalDateTime.parse(startField.getText().trim(), EventManager.DATE_FMT);
                endDate = LocalDateTime.parse(endField.getText().trim(), EventManager.DATE_FMT);
                
                if (endDate.isBefore(startDate)) {
                    showCustomAlertPopup("신청 종료일이 시작일보다\n빠를 수 없습니다.");
                    return;
                }
                
                if (startDate.isAfter(eventDate) || endDate.isAfter(eventDate)) {
                    showCustomAlertPopup("신청 기간은 행사 일시보다\n이전이어야 합니다.");
                    return;
                }

            } catch (DateTimeParseException e) {
                showCustomAlertPopup("날짜 형식이 올바르지 않습니다.\n(yyyy-MM-dd HH:mm)");
                return;
            }

            String code = codeCheck.isSelected() ? codeField.getText().trim() : null;
            int total = Integer.parseInt(totalField.getText().trim());

            // [추가] 선택된 회비 타입 변환
            String selectedFee = (String) feeCombo.getSelectedItem();
            FeeType feeType = FeeType.NONE;
            if (selectedFee.equals(FeeType.SCHOOL.getLabel())) feeType = FeeType.SCHOOL;
            else if (selectedFee.equals(FeeType.DEPT.getLabel())) feeType = FeeType.DEPT;

            if (currentEvent == null) {
                // [수정] 새 EventData 생성 (주최자 ID, 학과명, 회비 정보 포함 13개 인자)
                EventManager.EventData newEvent = new EventManager.EventData(
                    councilId, councilName, title, eventDate, locField.getText(), 
                    startDate, endDate, total, 0, code, "진행중", descArea.getText(), feeType
                );
                EventManager.addEvent(newEvent);
            } else {
                // 수정
                currentEvent.title = title;
                currentEvent.date = eventDate; 
                currentEvent.location = locField.getText();
                currentEvent.startDateTime = startDate;
                currentEvent.endDateTime = endDate;
                currentEvent.totalCount = total;
                currentEvent.secretCode = code;
                currentEvent.description = descArea.getText();
                currentEvent.requiredFee = feeType; // [추가] 회비 정보 업데이트
            }
            
            parent.refreshLists();
            dispose();
            showCustomAlertPopup("저장되었습니다.");
            
        } catch (NumberFormatException ex) {
            showCustomAlertPopup("인원은 숫자만 입력하세요.");
        }
    }

    private void showCustomAlertPopup(String message) {
        JDialog dialog = new JDialog(this, "알림", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 200);
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

        JTextArea msgArea = new JTextArea(message);
        msgArea.setFont(uiFont.deriveFont(18f));
        msgArea.setForeground(BROWN);
        msgArea.setOpaque(false);
        msgArea.setEditable(false);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setBounds(30, 50, 340, 70);
        panel.add(msgArea);

        JButton okBtn = createBtn("확인", BROWN);
        okBtn.setBounds(135, 130, 130, 45);
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);

        dialog.setVisible(true);
    }
}