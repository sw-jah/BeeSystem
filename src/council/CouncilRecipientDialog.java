package council;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.InputStream;
import council.EventManager.EventData;

public class CouncilRecipientDialog extends JDialog {

    private static final Color POPUP_BG = new Color(255, 250, 205); // 연한 노랑
    private static final Color BROWN = new Color(139, 90, 43);
    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color TABLE_BG = new Color(255, 255, 255);

    private static Font uiFont;
    static {
        try {
            InputStream is = CouncilRecipientDialog.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) uiFont = new Font("맑은 고딕", Font.PLAIN, 12);
            else uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);
        } catch (Exception e) { uiFont = new Font("맑은 고딕", Font.PLAIN, 12); }
    }

    public CouncilRecipientDialog(Frame parent, EventData event) {
        super(parent, "수령 명단 확인", true);
        setSize(550, 500);
        setLocationRelativeTo(parent);
        setUndecorated(true); // 테두리 제거 (커스텀 디자인)
        setBackground(new Color(0,0,0,0)); // 투명 배경

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
        add(panel);

        // 닫기 버튼 (X)
        JLabel closeBtn = new JLabel("X", SwingConstants.CENTER);
        closeBtn.setFont(uiFont.deriveFont(Font.BOLD, 18f));
        closeBtn.setForeground(BROWN);
        closeBtn.setBounds(500, 15, 30, 30);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { dispose(); }
        });
        panel.add(closeBtn);

        // 타이틀
        JLabel titleLabel = new JLabel("[" + event.title + "] 수령 명단", SwingConstants.CENTER);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 20f));
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(20, 20, 510, 30);
        panel.add(titleLabel);

        // 테이블 모델 설정
        String[] headers = {"순번", "이름", "학번", "학생회비 납부"};
        DefaultTableModel model = new DefaultTableModel(headers, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // [중요] EventData의 수령자 리스트를 불러와 테이블에 추가
        if (event.recipients.isEmpty()) {
            // 명단이 없을 경우 안내 메시지는 테이블에 표시되지 않지만, 테이블이 비어있음
        } else {
            for (String[] row : event.recipients) {
                model.addRow(row);
            }
        }

        JTable table = new JTable(model);
        styleTable(table);

        // 스크롤 패널
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(TABLE_BG);
        scroll.setBorder(BorderFactory.createLineBorder(BROWN, 1));
        scroll.setBounds(30, 70, 490, 350);
        panel.add(scroll);

        // 하단 닫기 버튼
        JButton closeButton = new JButton("닫기");
        closeButton.setFont(uiFont.deriveFont(16f));
        closeButton.setBackground(BROWN);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(new RoundedBorder(15, BROWN));
        closeButton.setBounds(200, 440, 150, 40);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose());
        panel.add(closeButton);

        setVisible(true);
    }

    private void styleTable(JTable table) {
        table.setFont(uiFont.deriveFont(14f));
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(255, 245, 157));
        table.setSelectionForeground(BROWN);
        table.setGridColor(new Color(230, 230, 230));
        
        // 헤더 스타일링
        JTableHeader header = table.getTableHeader();
        header.setFont(uiFont.deriveFont(16f));
        header.setBackground(HEADER_YELLOW);
        header.setForeground(BROWN);
        header.setPreferredSize(new Dimension(0, 35));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BROWN));

        // 셀 가운데 정렬
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // 컬럼 너비 조정
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // 순번
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // 이름
        table.getColumnModel().getColumn(2).setPreferredWidth(150); // 학번
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // 회비
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
            g2.drawRoundRect(x, y, w-1, h-1, radius, radius);
        }
    }
}