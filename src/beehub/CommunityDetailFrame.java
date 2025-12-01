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
    // [수정 완료] 실제 시스템 폰트명("던파 비트비트체 v2")으로 설정되었습니다.
    private static final String FONT_NAME_HTML = "던파 비트비트체 v2"; 

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
            System.out.println("DEBUG: Loaded font family name for uiFont: " + uiFont.getFamily());
        } catch (Exception e) {
            uiFont = new Font("SansSerif", Font.PLAIN, 14);
            System.err.println("ERROR: Failed to load DNFBitBitv2 font. Using fallback 'SansSerif'.");
            e.printStackTrace();
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

        // [추가] 윈도우 닫기 리스너를 추가하여 Post 객체의 댓글 수를 업데이트
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 프레임이 닫힐 때, 현재 댓글 모델의 크기를 Post 객체에 저장 (X 버튼 클릭 시 실행됨)
                currentPost.comments = commentModel.getSize();
            }
        });

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
        JPanel controlBar = new JPanel(null); 
        controlBar.setBounds(20, 395, 545, 55); 
        controlBar.setOpaque(false);
        
        // (A) 좋아요 버튼 (강제 중앙 정렬, 크기 140x50)
        int likeBtnWidth = 140;
        int likeBtnHeight = 50;
        int likeBtnX = (545 - likeBtnWidth) / 2;
        int likeBtnY = (55 - likeBtnHeight) / 2;
        
        likeBtn = createStyledButton(" 좋아요", likeBtnWidth, likeBtnHeight); 
        if (heartIcon != null) likeBtn.setIcon(heartIcon);
        likeBtn.setBackground(Color.WHITE);
        likeBtn.setForeground(new Color(255, 100, 100));
        likeBtn.addActionListener(e -> handleLikeAction(likeLabel));
        
        likeBtn.setBounds(likeBtnX, likeBtnY, likeBtnWidth, likeBtnHeight);
        controlBar.add(likeBtn);

        // (B) 수정/삭제 링크 (우측 정렬, 폰트 적용)
        if (currentPost.writer.equals(currentUser)) {
            JPanel editDeletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); 
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
            
            editDeletePanel.setBounds(400, 15, 145, 30);
            controlBar.add(editDeletePanel);
        }
        
        add(controlBar);

        // 5. 댓글 영역 
        commentTitle = new JLabel(" 댓글 (" + currentPost.comments + ")"); 
        commentTitle.setFont(uiFont.deriveFont(16f));
        commentTitle.setForeground(BROWN);
        commentTitle.setBounds(25, 460, 150, 25);
        add(commentTitle);

        commentModel = new DefaultListModel<>();
        
        // Post 객체의 기존 댓글 수(currentPost.comments)를 기반으로 commentModel을 초기화
        if (currentPost.comments > 0) {
            for (int i = 0; i < currentPost.comments; i++) {
                if (i == 0) {
                    commentModel.addElement(currentPost.writer + ": 이 글이 첫 댓글입니다.");
                } else {
                    // 저장된 댓글 수에 맞춰 단순 플레이스 홀더 사용
                    commentModel.addElement("Guest" + i + ": 저장된 댓글 #" + i); 
                }
            }
        } else {
             // 댓글이 0인 경우에도 시뮬레이션의 일관성을 위해 '첫 댓글'을 추가
             commentModel.addElement(currentPost.writer + ": 이 글이 첫 댓글입니다."); 
        }

        
        JList<String> commentList = new JList<>(commentModel);
        commentList.setFont(uiFont.deriveFont(14f)); 
        commentList.setCellRenderer(new CommentListRenderer(currentPost.writer)); 
        
        JScrollPane commentScroll = new JScrollPane(commentList);
        commentScroll.setBounds(20, 490, 545, 100);
        commentScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        add(commentScroll);

        commentTitle.setText(" 댓글 (" + commentModel.getSize() + ")"); 

        // 댓글 입력
        JTextField commentInput = new JTextField();
        commentInput.setBounds(20, 600, 430, 40);
        commentInput.setFont(uiFont.deriveFont(14f)); 
        add(commentInput);

        JButton addCommentBtn = createStyledButton("등록", 100, 40);
        addCommentBtn.setBounds(465, 600, 100, 40);
        addCommentBtn.addActionListener(e -> {
            String text = commentInput.getText().trim();
            if (!text.isEmpty()) {
                commentModel.addElement(currentUser + ":" + text);
                
                commentInput.setText("");
                commentTitle.setText(" 댓글 (" + commentModel.getSize() + ")"); 
            }
        });
        add(addCommentBtn);
    }
    
    // [수정] 좋아요 액션 핸들러: 50개 초과 시 20꿀 지급 로직 추가
    private void handleLikeAction(JLabel likeLabel) {
        if (!isLiked) {
            currentPost.likes++;
            isLiked = true;
            likeBtn.setBackground(new Color(255, 240, 240)); 
            showCustomAlertPopup("좋아요", "이 글을 좋아합니다!"); 
            
            // [추가된 로직] 내 글(currentUser)이고, 좋아요가 50을 초과했으며, 아직 보상이 지급되지 않았다면 꿀 지급
            if (currentPost.writer.equals(currentUser) && 
                currentPost.likes > 50 && 
                !currentPost.isRewardGiven) {
                
                // 20꿀 지급 시뮬레이션
                currentPost.isRewardGiven = true;
                // 실제 사용자에게 20꿀을 지급했다는 팝업을 표시합니다.
                showCustomAlertPopup("🎉 꿀 보상 지급! 🎉", "축하합니다! 좋아요 50개 초과로 20꿀이 지급되었습니다!");
            }
            
        } else {
            showCustomAlertPopup("알림", "이미 좋아요를 눌렀습니다.");
            return;
        }
        likeLabel.setText(" " + currentPost.likes);
    }

    // [최종 수정] 텍스트 링크 생성 헬퍼 (폰트 적용: <font face> 태그 강제 사용)
    private JLabel createTextLink(String text) {
        String underlineColor = toHexString(BROWN);
        
        // <font face> 태그를 사용하여 폰트 적용을 강제했습니다.
        JLabel label = new JLabel("<html><body style='color:" + toHexString(BROWN) + ";'>" +
                                 "<font face='" + FONT_NAME_HTML + "'>" + 
                                 "<u style='text-decoration-color: " + underlineColor + ";'>" + text + "</u>" +
                                 "</font>" + 
                                 "</body></html>");
        
        label.setFont(uiFont.deriveFont(14f)); 
        label.setForeground(BROWN);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return label;
    }
    
    private String toHexString(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    // [댓글 적용 성공] 댓글 렌더러: JPanel과 JLabel을 사용하여 HTML을 제거하고 폰트를 직접 적용합니다.
    class CommentListRenderer extends JPanel implements ListCellRenderer<String> {
        String postWriter;
        private JLabel nameLabel = new JLabel();
        private JLabel contentLabel = new JLabel();
        
        public CommentListRenderer(String writer) {
            this.postWriter = writer;
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0)); 
            setOpaque(true);
            
            // 폰트 직접 적용 (HTML 회피)
            nameLabel.setFont(uiFont.deriveFont(Font.BOLD, 14f));
            contentLabel.setFont(uiFont.deriveFont(14f));
            
            add(nameLabel);
            add(contentLabel);
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        }
        
        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            
            // 배경색 처리
            Color bg = isSelected ? list.getSelectionBackground() : list.getBackground();
            setBackground(bg);
            nameLabel.setBackground(bg);
            contentLabel.setBackground(bg);
            nameLabel.setOpaque(true); 
            contentLabel.setOpaque(true);
            
            // 텍스트 분리 및 색상/내용 설정
            String[] parts = value.split(":", 2); 
            String name = parts[0].trim();
            String content = parts.length > 1 ? parts[1].trim() : "";
            
            if (name.equals(currentPost.writer)) { 
                nameLabel.setText("작성자"); 
                nameLabel.setForeground(AUTHOR_HIGHLIGHT);
            } else {
                nameLabel.setText(name); 
                nameLabel.setForeground(BROWN); 
            }
            
            contentLabel.setText(" : " + content);
            contentLabel.setForeground(BROWN); 
            
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
        btn.setPreferredSize(new Dimension(w, h)); 
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