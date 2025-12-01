package admin; // [수정] 패키지 변경

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AdminItemAddDialog extends JDialog {

    private static final Color BG_YELLOW = new Color(255, 250, 205);
    private static final Color BROWN = new Color(139, 90, 43);
    // [수정] GRAY 색상 정의 추가
    private static final Color GRAY = new Color(200, 200, 200);
    
    private static Font uiFont;
    static {
        try {
            InputStream is = AdminItemAddDialog.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 12);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);
        } catch (Exception e) { uiFont = new Font("맑은 고딕", Font.PLAIN, 12); }
    }

    private AdminItemManageFrame parent;
    private AdminItemManageFrame.ItemData currentItem; 

    private JTextField nameField;
    private JSpinner stockSpinner, daySpinner;
    private JLabel imagePreview;
    private List<JCheckBox> majorCheckBoxes = new ArrayList<>();

    public AdminItemAddDialog(AdminItemManageFrame parent, AdminItemManageFrame.ItemData item) {
        super(parent, item == null ? "물품 등록" : "물품 수정", true);
        this.parent = parent;
        this.currentItem = item;

        setSize(500, 650);
        setLocationRelativeTo(parent);
        setLayout(null);
        getContentPane().setBackground(BG_YELLOW);

        initUI();
        if (item != null) loadData(item);
        
        setVisible(true);
    }

    private void initUI() {
        JLabel imgLabel = new JLabel("물품 사진");
        imgLabel.setFont(uiFont.deriveFont(16f));
        imgLabel.setForeground(BROWN);
        imgLabel.setBounds(30, 30, 100, 30);
        add(imgLabel);

        imagePreview = new JLabel("이미지 없음", SwingConstants.CENTER);
        imagePreview.setBorder(BorderFactory.createLineBorder(BROWN));
        imagePreview.setOpaque(true);
        imagePreview.setBackground(Color.WHITE);
        imagePreview.setBounds(30, 65, 100, 100);
        add(imagePreview);

        JButton uploadBtn = new JButton("사진 찾기");
        uploadBtn.setFont(uiFont.deriveFont(12f));
        uploadBtn.setBounds(140, 100, 100, 30);
        uploadBtn.setBackground(Color.WHITE);
        uploadBtn.setForeground(BROWN);
        uploadBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                imagePreview.setText(chooser.getSelectedFile().getName());
            }
        });
        add(uploadBtn);

        addLabelAndField("물품명 :", 30, 200, 160);
        nameField = new JTextField();
        nameField.setBounds(140, 195, 300, 35);
        add(nameField);

        addLabelAndField("재고(개) :", 30, 250, 80);
        stockSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 1000, 1));
        stockSpinner.setBounds(140, 245, 80, 35);
        add(stockSpinner);

        addLabelAndField("대여기간(일):", 250, 250, 100);
        daySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));
        daySpinner.setBounds(360, 245, 80, 35);
        add(daySpinner);

        JLabel majorLabel = new JLabel("대상 학과 선택 (중복 가능)");
        majorLabel.setFont(uiFont.deriveFont(16f));
        majorLabel.setForeground(BROWN);
        majorLabel.setBounds(30, 300, 300, 30);
        add(majorLabel);

        JPanel majorPanel = new JPanel();
        majorPanel.setLayout(new BoxLayout(majorPanel, BoxLayout.Y_AXIS));
        majorPanel.setBackground(Color.WHITE);

        JCheckBox allCheck = new JCheckBox("전체 학과");
        allCheck.setFont(uiFont.deriveFont(Font.BOLD, 14f));
        allCheck.setBackground(Color.WHITE);
        allCheck.setForeground(BROWN);
        allCheck.addActionListener(e -> {
            boolean sel = allCheck.isSelected();
            for(JCheckBox cb : majorCheckBoxes) cb.setSelected(sel);
        });
        majorPanel.add(allCheck);
        majorPanel.add(Box.createVerticalStrut(10));

        addCollegeGroup(majorPanel, "인문대학", 
            new String[]{"글로벌ICT인문융합학부", "국어국문학과", "영어영문학과", "중어중문학과", "일어일문학과", "사학과", "기독교학과"});
        addCollegeGroup(majorPanel, "사회과학대학", 
            new String[]{"경제학과", "문헌정보학과", "사회복지학과", "아동학과", "행정학과", "언론영상학부", "심리.인지과학학부", "스포츠운동과학과"});
        addCollegeGroup(majorPanel, "과학기술융합대학", 
            new String[]{"수학과", "화학과", "생명환경공학과", "바이오헬스융합학과", "원예생명조경학과", "식품공학과", "식품영양학과"});
        addCollegeGroup(majorPanel, "미래산업융합대학", 
            new String[]{"경영학과", "패션산업학과", "디지털미디어학과", "지능정보보호학부", "소프트웨어융합학과", "데이터사이언스학과", "산업디자인학과"});

        JScrollPane scrollPane = new JScrollPane(majorPanel);
        scrollPane.setBounds(30, 335, 420, 180);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane);

        JButton cancelBtn = createBtn("취소", GRAY); // 이제 GRAY 변수를 찾을 수 있습니다.
        cancelBtn.setBounds(100, 540, 120, 50);
        cancelBtn.addActionListener(e -> dispose());
        add(cancelBtn);

        JButton okBtn = createBtn(currentItem == null ? "등록" : "수정", BROWN);
        okBtn.setBounds(260, 540, 120, 50);
        okBtn.addActionListener(e -> saveData());
        add(okBtn);
    }

    private void addCollegeGroup(JPanel p, String collegeName, String[] depts) {
        JLabel cLabel = new JLabel("■ " + collegeName);
        cLabel.setFont(uiFont.deriveFont(Font.BOLD, 14f));
        cLabel.setForeground(BROWN);
        cLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 0));
        p.add(cLabel);

        for(String dept : depts) {
            JCheckBox cb = new JCheckBox(dept);
            cb.setFont(uiFont.deriveFont(13f));
            cb.setBackground(Color.WHITE);
            cb.setBorder(BorderFactory.createEmptyBorder(0, 20, 5, 0));
            majorCheckBoxes.add(cb);
            p.add(cb);
        }
    }

    private void addLabelAndField(String text, int x, int y, int w) {
        JLabel l = new JLabel(text);
        l.setFont(uiFont.deriveFont(16f));
        l.setForeground(BROWN);
        l.setBounds(x, y, w, 30);
        add(l);
    }

    private JButton createBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(uiFont.deriveFont(16f));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    private void loadData(AdminItemManageFrame.ItemData item) {
        nameField.setText(item.name);
        stockSpinner.setValue(item.stock);
        daySpinner.setValue(item.rentDays);
    }

    private void saveData() {
        String name = nameField.getText().trim();
        if(name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "물품명을 입력해주세요.");
            return;
        }
        int stock = (int) stockSpinner.getValue();
        int days = (int) daySpinner.getValue();

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for(JCheckBox cb : majorCheckBoxes) {
            if(cb.isSelected()) {
                if(sb.length() > 0) sb.append(", ");
                sb.append(cb.getText());
                count++;
            }
        }
        String majors = count == majorCheckBoxes.size() ? "전체 학과" : sb.toString();
        if(majors.isEmpty()) majors = "대상 없음";

        if(currentItem == null) {
            parent.addItem(new AdminItemManageFrame.ItemData(name, stock, days, majors, null));
        } else {
            currentItem.name = name;
            currentItem.stock = stock;
            currentItem.rentDays = days;
            currentItem.targetMajor = majors;
            parent.refreshList();
        }
        dispose();
    }
}