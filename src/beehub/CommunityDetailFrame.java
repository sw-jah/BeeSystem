package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.InputStream;
import beehub.CommunityFrame.Post;

public class CommunityDetailFrame extends JFrame {

    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color BROWN = new Color(89, 60, 28);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BORDER_COLOR = new Color(220, 220, 220);
    private static final Color POPUP_BG = new Color(255, 250, 205); 
    private static final Color AUTHOR_HIGHLIGHT = new Color(255, 180, 0); 

    private static Font uiFont;
    // [추가] 폰트 적용의 일관성을 위한 상수 정의
    private static final String FONT_NAME_HTML = "DNFBitBitv2"; 

    static {
        try {
            File fontFile = new File("resource/fonts/DNFBitBitv2.ttf");
            if (fontFile.exists()) {
                uiFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(14f);
            } else {
                InputStream is = CommunityDetailFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
                if (is != null) {
                    uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
                } else {
                    uiFont = new Font("SansSerif", Font.PLAIN, 14);
                }
            }
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(uiFont);
        } catch (Exception e) {
            uiFont = new Font("SansSerif", Font.PLAIN, 14);
        }
    }

    private Post currentPost;
    private DefaultListModel<String> commentModel;
    private ImageIcon heartIcon;
    private String currentUser; 
    private boolean isLiked = false; 
    private JLabel commentTitle; 
    private JButton likeBtn;

    public CommunityDetailFrame(Post post, ImageIcon icon, String user) {
        this.currentPost = post;
        this.heartIcon = icon;
        this.currentUser = user;

        setTitle("게시글 상세 - " + post.title);
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        initUI();
        setVisible(true);
    }

    private void initUI() {
        // 1. 헤더
        JPanel header = new JPanel(new BorderLayout());
        header.setBounds(0, 0, 600, 50);
        header.setBackground(HEADER_YELLOW);
        JLabel title = new JLabel(" 커뮤니티 > 게시글 상세", JLabel.LEFT);
        title.setFont(uiFont.deriveFont(18f));
        title.setForeground(BROWN);
        header.add(title, BorderLayout.WEST);
        add(header);

        // 2. 게시글 정보
        JPanel infoPanel = new JPanel(null);
        infoPanel.setBounds(20, 70, 545, 100);
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new RoundedBorder(15, BORDER_COLOR, 2));

        JLabel postTitle = new JLabel(currentPost.title);
        postTitle.setFont(uiFont.deriveFont(Font.BOLD, 22f));
        postTitle.setBounds(20, 15, 500, 30);
        infoPanel.add(postTitle);

        JLabel writerInfo = new JLabel("작성자: " + currentPost.writer + "  |  " + currentPost.date);
        writerInfo.setFont(uiFont.deriveFont(14f));
        writerInfo.setForeground(Color.GRAY);
        writerInfo.setBounds(20, 55, 300, 20);
        infoPanel.add(writerInfo);
        
        // 좋아요 수
        JLabel likeLabel = new JLabel(" " + currentPost.likes);
        if (heartIcon != null) likeLabel.setIcon(heartIcon);
        likeLabel.setFont(uiFont.deriveFont(16f));
        likeLabel.setForeground(new Color(255, 100, 100));
        likeLabel.setBounds(450, 55, 80, 20);
        infoPanel.add(likeLabel);

        // [수정] 중복된 수정/삭제 버튼 블록 제거 완료

        add(infoPanel);

        // 3. 본문
        JTextArea contentArea = new JTextArea(currentPost.content);
        contentArea.setFont(uiFont.deriveFont(16f)); 
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentScroll.setBounds(20, 180, 545, 200);
        contentScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        add(contentScroll);

        // 4. 컨트롤 바 (좋아요 + 수정/삭제)
        JPanel controlBar = new JPanel(new BorderLayout(10, 0)); 
        controlBar.setBounds(20, 395, 545, 45); 
        controlBar.setOpaque(false);
        
        // (A) 좋아요 버튼 (가운데 정렬, 크기 확대)
        JPanel likeWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        likeWrapper.setOpaque(false);
        
        likeBtn = createStyledButton(" 좋아요", 120, 45); 
        if (heartIcon != null) likeBtn.setIcon(heartIcon);
        likeBtn.setBackground(Color.WHITE);
        likeBtn.setForeground(new Color(255, 100, 100));
        likeBtn.addActionListener(e -> handleLikeAction(likeLabel));
        likeWrapper.add(likeBtn);
        controlBar.add(likeWrapper, BorderLayout.CENTER);

        // (B) 수정/삭제 링크 (우측 정렬, 폰트 적용)
        if (currentPost.writer.equals(currentUser)) {
            JPanel editDeletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15)); 
            editDeletePanel.setOpaque(false);
            
            JLabel editLink = createTextLink("수정"); 
            editLink.addMouseListener(new MouseAdapter() { 
                public void mouseClicked(MouseEvent e) { 
                    showCustomAlertPopup("수정 기능", "게시글 수정을 시작합니다.");
                }
            });
            
            JLabel deleteLink = createTextLink("삭제"); 
            deleteLink.setForeground(new Color(200, 50, 50)); 
            deleteLink.addMouseListener(new MouseAdapter() { 
                public void mouseClicked(MouseEvent e) { 
                    showCustomConfirmPopup("게시글을 삭제하시겠습니까?", () -> dispose());
                }
            });

            editDeletePanel.add(editLink);
            editDeletePanel.add(new JLabel(" ")); 
            editDeletePanel.add(deleteLink);
            controlBar.add(editDeletePanel, BorderLayout.EAST);
        }
        
        add(controlBar);

        // 5. 댓글 영역 
        commentTitle = new JLabel(" 댓글 (" + currentPost.comments + ")"); 
        commentTitle.setFont(uiFont.deriveFont(16f));
        commentTitle.setForeground(BROWN);
        commentTitle.setBounds(25, 450, 150, 25);
        add(commentTitle);

        commentModel = new DefaultListModel<>();
        commentModel.addElement(currentPost.writer + ": 이 글이 첫 댓글입니다."); 
        
        JList<String> commentList = new JList<>(commentModel);
        commentList.setFont(uiFont.deriveFont(14f)); // [수정] 폰트 적용
        commentList.setCellRenderer(new CommentListRenderer(currentPost.writer));
        
        JScrollPane commentScroll = new JScrollPane(commentList);
        commentScroll.setBounds(20, 480, 545, 100);
        commentScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        add(commentScroll);

        commentTitle.setText(" 댓글 (" + commentModel.getSize() + ")"); 

        // 댓글 입력
        JTextField commentInput = new JTextField();
        commentInput.setBounds(20, 590, 430, 40);
        commentInput.setFont(uiFont.deriveFont(14f)); // [수정] 폰트 적용
        add(commentInput);

        JButton addCommentBtn = createStyledButton("등록", 100, 40);
        addCommentBtn.setBounds(465, 590, 100, 40);
        addCommentBtn.addActionListener(e -> {
            String text = commentInput.getText().trim();
            if (!text.isEmpty()) {
                commentModel.addElement(currentUser + ":" + text);
                
                commentInput.setText("");
                currentPost.comments++;
                commentTitle.setText(" 댓글 (" + commentModel.getSize() + ")"); 
            }
        });
        add(addCommentBtn);
    }
    
    private void handleLikeAction(JLabel likeLabel) {
        if (!isLiked) {
            currentPost.likes++;
            isLiked = true;
            likeBtn.setBackground(new Color(255, 240, 240)); 
            showCustomAlertPopup("좋아요", "이 글을 좋아합니다!"); 
        } else {
            showCustomAlertPopup("알림", "이미 좋아요를 눌렀습니다.");
            return;
        }
        likeLabel.setText(" " + currentPost.likes);
    }

    // [최종] 텍스트 링크 생성 헬퍼 (폰트 적용)
    private JLabel createTextLink(String text) {
        String underlineColor = toHexString(BROWN);
        
        JLabel label = new JLabel("<html><body style='font-size:14px; color:" + toHexString(BROWN) + ";'>" +
                                 "<u style='text-decoration-color: " + underlineColor + ";'>" + text + "</u></body></html>");
        label.setFont(uiFont.deriveFont(14f)); // 폰트 적용
        label.setForeground(BROWN);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return label;
    }
    
    private String toHexString(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    // [최종] 댓글 렌더러 (글자색 강조 및 폰트 적용)
    class CommentListRenderer extends DefaultListCellRenderer {
        String postWriter;
        
        public CommentListRenderer(String writer) {
            this.postWriter = writer;
            setFont(uiFont.deriveFont(14f)); 
        }
        
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // 폰트 적용을 위해 super 호출 후 setFont를 다시 호출
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setFont(uiFont.deriveFont(14f)); 
            
            String text = (String) value;
            String[] parts = text.split(":", 2); 
            
            if (parts.length > 0) {
                String name = parts[0].trim();
                String content = parts.length > 1 ? parts[1] : "";
                
                // HTML로 텍스트/색상 설정 (font-family 제거)
                String html = "<html><body style='font-size:14px;'>"; 
                
                if (name.equals(currentPost.writer)) { 
                    html += "<span style='color:" + toHexString(AUTHOR_HIGHLIGHT) + "; font-weight:bold;'>작성자</span>"; 
                } else {
                    html += "<b>" + name + "</b>"; 
                }
                
                html += " : " + content + "</body></html>";
                
                setText(html);
            }
            
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            return this;
        }
    }

    private JButton createStyledButton(String text, int w, int h) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(14f));
        btn.setBackground(BROWN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(15, BROWN, 1));
        return btn;
    }
    
    private JPanel createPopupPanel() {
        return new JPanel() {
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
    }

    private JButton createPopupBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(16f));
        btn.setBackground(BROWN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(15, BROWN, 1));
        return btn;
    }

    private void showCustomAlertPopup(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        JLabel msgLabel = new JLabel(message, SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(16f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 80, 360, 30);
        panel.add(msgLabel);

        JButton okBtn = createPopupBtn("확인");
        okBtn.setBounds(135, 160, 130, 45);
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);

        dialog.setVisible(true);
    }
    
    private void showCustomConfirmPopup(String message, Runnable onConfirm) {
        JDialog dialog = new JDialog(this, "확인", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        JLabel msgLabel = new JLabel(message, SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(18f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 70, 360, 30);
        panel.add(msgLabel);

        JButton yesBtn = createPopupBtn("네");
        yesBtn.setBounds(60, 150, 120, 45);
        yesBtn.addActionListener(e -> {
            dialog.dispose();
            onConfirm.run();
        });
        panel.add(yesBtn);

        JButton noBtn = createPopupBtn("아니오");
        noBtn.setBounds(220, 150, 120, 45);
        noBtn.addActionListener(e -> dialog.dispose());
        panel.add(noBtn);

        dialog.setVisible(true);
    }

    private static class RoundedBorder implements Border {
        private int radius; private Color color; private int thickness;
        public RoundedBorder(int r, Color c, int t) { radius = r; color = c; thickness = t; }
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
}