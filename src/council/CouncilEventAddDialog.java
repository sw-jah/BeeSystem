package council;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*; // [중요] 텍스트 스타일링을 위해 추가
import java.awt.*;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import council.EventManager.FeeType;

public class CouncilEventAddDialog extends JDialog {

    private static final Color BG_WHITE = new Color(255, 255, 255);
    private static final Color BROWN = new Color(139, 90, 43);
    
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
    
    // 주최자 정보
    private String councilId;
    private String councilName;

    private JTextField titleField, dateField, locField, startField, endField, totalField, codeField;
    private JComboBox<String> feeCombo; 
    private JCheckBox codeCheck;
    private JTextArea descArea;

    public CouncilEventAddDialog(CouncilMainFrame parent, EventManager.EventData event, String id, String name) {
        super(parent, event == null ? "새 행사 등록" : "행사 수정", true);
        this.parent = parent;
        this.currentEvent = event;
        this.councilId = id;
        this.councilName = name;

        setSize(500, 750);
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

        // 납부 대상 선택
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
            
            // [중요] 날짜 변환 로직 (행사일, 신청시작일, 신청종료일 모두 변환됨)
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
                // [수정] 양식을 지키지 않았을 때 뜨는 팝업 메시지
                showCustomAlertPopup("날짜 형식이 올바르지 않습니다.\n(2023-12-05 12:00)\n형식으로 다시 입력해주세요.");
                return;
            }

            String code = codeCheck.isSelected() ? codeField.getText().trim() : null;
            int total = Integer.parseInt(totalField.getText().trim());

            String selectedFee = (String) feeCombo.getSelectedItem();
            FeeType feeType = FeeType.NONE;
            if (selectedFee.equals(FeeType.SCHOOL.getLabel())) feeType = FeeType.SCHOOL;
            else if (selectedFee.equals(FeeType.DEPT.getLabel())) feeType = FeeType.DEPT;

            if (currentEvent == null) {
                EventManager.EventData newEvent = new EventManager.EventData(
                    councilId, councilName, title, eventDate, locField.getText(), 
                    startDate, endDate, total, 0, code, "진행중", descArea.getText(), feeType
                );
                EventManager.addEvent(newEvent);
            } else {
                currentEvent.title = title;
                currentEvent.date = eventDate; 
                currentEvent.location = locField.getText();
                currentEvent.startDateTime = startDate;
                currentEvent.endDateTime = endDate;
                currentEvent.totalCount = total;
                currentEvent.secretCode = code;
                currentEvent.description = descArea.getText();
                currentEvent.requiredFee = feeType; 
            }
            
            parent.refreshLists();
            dispose();
            showCustomAlertPopup("저장되었습니다.");
            
        } catch (NumberFormatException ex) {
            showCustomAlertPopup("인원은 숫자만 입력하세요.");
        }
    }

    // [수정] 텍스트 상하좌우 중앙 정렬이 적용된 예쁜 팝업
    private void showCustomAlertPopup(String message) {
        JDialog dialog = new JDialog(this, "알림", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 250, 205)); 
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(BROWN);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 30, 30);
            }
        };
        panel.setLayout(null);
        dialog.add(panel);

        // 텍스트를 담을 투명 패널 (GridBagLayout)
        JPanel textPanel = new JPanel(new GridBagLayout());
        textPanel.setOpaque(false);
        textPanel.setBounds(20, 40, 360, 110); 
        panel.add(textPanel);

        // 메시지 표시 (JTextPane 사용 - 폰트 중앙 정렬)
        JTextPane msgPane = new JTextPane();
        msgPane.setText(message);
        msgPane.setFont(uiFont.deriveFont(18f));
        msgPane.setForeground(BROWN);
        msgPane.setOpaque(false);
        msgPane.setEditable(false);
        msgPane.setFocusable(false);
        
        // 가로 중앙 정렬 스타일
        StyledDocument doc = msgPane.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        textPanel.add(msgPane);

        JButton okBtn = createBtn("확인", BROWN);
        okBtn.setBounds(135, 160, 130, 45);
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);

        dialog.setVisible(true);
    }
}