package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommunityFrame extends JFrame {

    // 🎨 컬러 테마
    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color NAV_BG = new Color(255, 255, 255);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(89, 60, 28);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157);
    private static final Color BORDER_COLOR = new Color(220, 220, 220);
    private static final Color POPUP_BG = new Color(255, 250, 205); // 팝업 배경색

    private static Font uiFont;
    private ImageIcon heartIcon; 

    // 폰트 로드 및 등록
    static {
        try {
            InputStream is = CommunityFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) {
                File f = new File("resource/fonts/DNFBitBitv2.ttf");
                if (f.exists()) {
                    uiFont = Font.createFont(Font.TRUETYPE_FONT, f).deriveFont(14f);
                } else {
                    uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
                }
            } else {
                uiFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
            }
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(uiFont);
        } catch (Exception e) {
            uiFont = new Font("SansSerif", Font.PLAIN, 14);
        }
    }

    private String userName = "사용자"; 
    
    // UI 컴포넌트
    private JTextField searchField;
    private JTable postTable;
    private DefaultTableModel tableModel;
    private JPanel pagePanel; 

    // 데이터 및 페이지네이션 변수
    private List<Post> allPosts = new ArrayList<>(); 
    private List<Post> filteredPosts = new ArrayList<>(); 
    private int currentPage = 1;
    private final int itemsPerPage = 8; 

    public CommunityFrame() {
        setTitle("서울여대 꿀단지 - 커뮤니티");
        setSize(800, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(BG_MAIN);

        loadImages();
        initDummyData();
        
        initHeader();
        initNav();
        initContent();

        setVisible(true);
    }
    
    // [추가] 새 Post를 목록에 추가하고 UI를 새로고침하는 공개 메서드
    public void addPost(Post newPost) {
        // 새 글의 No를 현재 목록의 최대 No + 1로 설정 (내림차순 정렬을 위해)
        int maxNo = allPosts.isEmpty() ? 0 : allPosts.stream().mapToInt(p -> p.no).max().orElse(0);
        newPost.no = maxNo + 1;
        
        allPosts.add(0, newPost); // 가장 위에 추가
        searchPosts(); // 필터링/검색 로직을 다시 실행하고 renderTable()을 호출하여 목록을 새로고침
    }

    private void loadImages() {
        try {
            ImageIcon origin = new ImageIcon("resource/img/heart.png");
            if (origin.getIconWidth() > 0) {
                Image img = origin.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
                heartIcon = new ImageIcon(img);
            }
        } catch (Exception e) {}
    }

    private void initDummyData() {
        LocalDate today = LocalDate.now();
        for (int i = 1; i <= 30; i++) {
            String date = today.minusDays(i).toString();
            // 모든 게시글의 초기 댓글 수를 최소 1개(첫 댓글) 이상으로 설정
            int initialComments = 1; 
            if (i == 1) {
                // 더미 데이터의 no는 역순으로 30부터 1까지 할당
                allPosts.add(new Post(30, "제가 쓴 글입니다 (테스트)", "사용자", today.toString(), 0, initialComments, "내용"));
            } else {
                allPosts.add(new Post(30 - i + 1, "게시글 테스트 " + i, "글쓴이" + i, date, i * 2, initialComments + (i % 3), "내용입니다."));
            }
        }
    }

    private void initHeader() {
        JPanel headerPanel = new JPanel(null);
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

        JLabel userInfoText = new JLabel("[" + userName + "]님 | 보유 꿀 : 100 | 로그아웃");
        userInfoText.setFont(uiFont.deriveFont(14f));
        userInfoText.setForeground(BROWN);
        userInfoText.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // [수정] 로그아웃 팝업 호출
        userInfoText.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { 
                showLogoutPopup(); 
            }
        });
        
        userInfoPanel.add(userInfoText);
        headerPanel.add(userInfoPanel);
    }

    private void initNav() {
        JPanel navPanel = new JPanel(new GridLayout(1, 6));
        navPanel.setBounds(0, 80, 800, 50);
        navPanel.setBackground(NAV_BG);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        add(navPanel);

        String[] menus = {"물품대여", "간식행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"};
        for (String menu : menus) {
            JButton menuBtn = createNavButton(menu, menu.equals("커뮤니티"));
            navPanel.add(menuBtn);
        }
    }

    private void initContent() {
        JPanel contentPanel = new JPanel(null);
        contentPanel.setBounds(0, 130, 800, 520);
        contentPanel.setBackground(BG_MAIN);
        add(contentPanel);

        // 1. 상단 컨트롤 영역
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBounds(25, 20, 750, 60);
        topContainer.setBackground(BG_MAIN);
        topContainer.setOpaque(false);

        // 검색 패널 (왼쪽)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(new RoundedBorder(15, BORDER_COLOR, 2));
        
        searchField = new JTextField(20);
        searchField.setFont(uiFont.deriveFont(14f));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10, BORDER_COLOR, 1), 
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        searchField.setPreferredSize(new Dimension(220, 35));

        JButton searchBtn = createStyledButton("검색", 70, 35);
        searchBtn.setBackground(Color.WHITE);
        searchBtn.setForeground(BROWN);
        searchBtn.addActionListener(e -> searchPosts());

        searchPanel.add(createLabel("검색 :"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        // 글쓰기 버튼 패널 (오른쪽)
        JPanel writePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        writePanel.setOpaque(false);
        
        JButton writeBtn = createStyledButton("글쓰기", 90, 40);
        writeBtn.setBackground(Color.WHITE); 
        writeBtn.setForeground(BROWN);
        
        // [수정] CommunityWriteFrame을 열고 현재 프레임(this)을 전달
        writeBtn.addActionListener(e -> {
            // 사용자 이름과 현재 CommunityFrame 객체(this)를 전달
            new CommunityWriteFrame(userName, this);
        });
        
        writePanel.add(writeBtn);

        topContainer.add(searchPanel, BorderLayout.WEST);
        topContainer.add(writePanel, BorderLayout.EAST);

        contentPanel.add(topContainer);

        // 2. 게시글 목록 테이블
        String[] headers = {"제목", "작성자", "작성일", "좋아요"};
        tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        postTable = new JTable(tableModel);
        styleTable(postTable);
        
        // 컬럼 너비
        postTable.getColumnModel().getColumn(0).setPreferredWidth(450); 
        postTable.getColumnModel().getColumn(1).setPreferredWidth(100); 
        postTable.getColumnModel().getColumn(2).setPreferredWidth(120); 
        postTable.getColumnModel().getColumn(3).setPreferredWidth(80);  

        // 렌더러 적용 
        postTable.getColumnModel().getColumn(0).setCellRenderer(new TitleCommentRenderer()); 
        if (heartIcon != null) {
            postTable.getColumnModel().getColumn(3).setCellRenderer(new IconTextRenderer(heartIcon)); 
        }

        // 상세 페이지 이동 이벤트
        postTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = postTable.getSelectedRow();
                    if (row != -1) {
                        TitleWithCommentCount tc = (TitleWithCommentCount) postTable.getValueAt(row, 0);
                        Post selectedPost = filteredPosts.get(row); // 현재 필터링된 목록에서 Post 객체 가져옴
                        
                        if (selectedPost != null) {
                            // CommunityDetailFrame이 CommunityFrame의 Post 클래스를 사용하도록 수정 필요
                            // 현재 Post 클래스는 public static이므로 외부에서 접근 가능합니다.
                            CommunityDetailFrame detailFrame = new CommunityDetailFrame(selectedPost, heartIcon, userName); 
                            
                            // 상세 프레임이 닫힐 때 목록을 새로고침하도록 WindowListener를 추가
                            detailFrame.addWindowListener(new WindowAdapter() {
                                @Override
                                public void windowClosed(WindowEvent e) {
                                    renderTable(); // 상세 프레임이 닫힌 후 목록을 새로고침
                                }
                            });
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(postTable);
        scrollPane.setBounds(25, 90, 750, 310);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        // 스크롤바 디자인 적용
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        contentPanel.add(scrollPane);

        // 3. 페이지네이션 패널
        pagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        pagePanel.setBounds(25, 410, 750, 40);
        pagePanel.setBackground(BG_MAIN);
        contentPanel.add(pagePanel);

        searchPosts();
    }
    
    // --- 기능 로직 ---

    private void searchPosts() {
        String keyword = searchField.getText().trim();
        filteredPosts.clear();

        if (keyword.isEmpty()) {
            filteredPosts.addAll(allPosts);
        } else {
            filteredPosts = allPosts.stream()
                .filter(p -> p.title.contains(keyword) || p.writer.contains(keyword))
                .collect(Collectors.toList());
        }
        
        // 내림차순 정렬 (최신 글이 위로)
        filteredPosts.sort((p1, p2) -> Integer.compare(p2.no, p1.no));

        currentPage = 1;
        renderTable();
    }

    private void renderTable() {
        tableModel.setRowCount(0);

        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredPosts.size());

        for (int i = start; i < end; i++) {
            Post post = filteredPosts.get(i);
            tableModel.addRow(new Object[]{
                new TitleWithCommentCount(post.title, post.comments), 
                post.writer, 
                formatDate(post.date), 
                post.likes
            });
        }
        
        updatePaginationPanel();
    }

    private void updatePaginationPanel() {
        pagePanel.removeAll();
        
        int calcPages = (int) Math.ceil((double) filteredPosts.size() / itemsPerPage);
        if (calcPages == 0) calcPages = 1;
        final int totalPages = calcPages; 

        // < 이전
        JButton prevBtn = createPageButton("<", false);
        prevBtn.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                renderTable();
            }
        });
        pagePanel.add(prevBtn);

        // 숫자
        for (int i = 1; i <= totalPages; i++) {
            JButton numBtn = createPageButton(String.valueOf(i), i == currentPage);
            final int pageNum = i;
            numBtn.addActionListener(e -> {
                currentPage = pageNum;
                renderTable();
            });
            pagePanel.add(numBtn);
        }

        // > 다음
        JButton nextBtn = createPageButton(">", false);
        nextBtn.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                renderTable();
            }
        });
        pagePanel.add(nextBtn);

        pagePanel.revalidate();
        pagePanel.repaint();
    }

    private String formatDate(String dateStr) {
        LocalDate postDate = LocalDate.parse(dateStr);
        LocalDate today = LocalDate.now();
        long daysDiff = ChronoUnit.DAYS.between(postDate, today);

        if (daysDiff == 0) return "오늘";
        else if (daysDiff <= 30) return daysDiff + "일 전";
        else if (postDate.getYear() == today.getYear()) 
            return postDate.getMonthValue() + "월 " + postDate.getDayOfMonth() + "일";
        else return postDate.getYear() + "." + postDate.getMonthValue() + "." + postDate.getDayOfMonth();
    }

    private Post findPostByTitle(String title) {
        for (Post p : allPosts) {
            if (p.title.equals(title)) return p;
        }
        return null;
    }
    
    // [추가] 로그아웃 팝업 메서드 (MainFrame 스타일 참고)
    private void showLogoutPopup() {
        JDialog dialog = new JDialog(this, "로그아웃", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        JLabel msgLabel = new JLabel("로그아웃 하시겠습니까?", SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(18f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 70, 360, 30);
        panel.add(msgLabel);

        JButton yesBtn = createPopupBtn("네");
        yesBtn.setBounds(60, 150, 120, 45);
        yesBtn.addActionListener(e -> {
            dialog.dispose();
            // new LoginFrame(); // LoginFrame이 정의되어 있다면 주석 해제
            dispose();
        });
        panel.add(yesBtn);

        JButton noBtn = createPopupBtn("아니오");
        noBtn.setBounds(220, 150, 120, 45);
        noBtn.addActionListener(e -> dialog.dispose());
        panel.add(noBtn);

        dialog.setVisible(true);
    }
    
    // [추가] 팝업 스타일 메서드
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
    
    // [추가] 팝업 버튼 스타일 메서드
    private JButton createPopupBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(16f));
        btn.setBackground(BROWN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(15, BROWN, 1));
        return btn;
    }


    // --- 데이터 클래스 ---
    public static class Post {
        int no; String title; String writer; String date; int likes; int comments; String content;
        public Post(int n, String t, String w, String d, int l, int c, String content) {
            no = n; this.title = t; this.writer = w; this.date = d; this.likes = l; this.comments = c; this.content = content;
        }
    }

    class TitleWithCommentCount {
        String title; int commentCount;
        public TitleWithCommentCount(String t, int c) { title = t; commentCount = c; }
        @Override public String toString() { return title; }
    }

    class TitleCommentRenderer extends JPanel implements TableCellRenderer {
        private JLabel titleLabel = new JLabel();
        private JLabel countLabel = new JLabel();

        public TitleCommentRenderer() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
            setOpaque(true);
            
            titleLabel.setFont(uiFont.deriveFont(16f)); 
            titleLabel.setForeground(BROWN);
            
            countLabel.setFont(uiFont.deriveFont(14f));
            countLabel.setForeground(Color.GRAY);
            
            add(titleLabel);
            add(countLabel);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (isSelected) {
                setBackground(HIGHLIGHT_YELLOW);
                titleLabel.setForeground(BROWN); 
                countLabel.setForeground(Color.GRAY);
            } else {
                setBackground(Color.WHITE);
                titleLabel.setForeground(BROWN);
                countLabel.setForeground(Color.GRAY);
            }

            if (value instanceof TitleWithCommentCount) {
                TitleWithCommentCount tc = (TitleWithCommentCount) value;
                titleLabel.setText(tc.title);
                
                // 댓글 수가 1 이상이면 표시합니다. (첫 댓글 포함)
                if (tc.commentCount > 0) {
                    countLabel.setText("[" + tc.commentCount + "]");
                } else {
                    countLabel.setText(""); 
                }
            }
            return this;
        }
    }

    class IconTextRenderer extends DefaultTableCellRenderer {
        private Icon icon;
        public IconTextRenderer(Icon icon) { this.icon = icon; }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setFont(uiFont.deriveFont(14f)); 
            c.setIcon(icon);
            c.setText(value != null ? " " + value.toString() : "");
            c.setHorizontalAlignment(CENTER);
            return c;
        }
    }

    // [수정 완료] ModernScrollBarUI: createZeroButton() 메서드를 제거하고 로직을 직접 포함하여 오류 해결
    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(200, 200, 200);
            this.trackColor = new Color(245, 245, 245);
        }
        
        @Override
        protected JButton createDecreaseButton(int orientation) { 
            // 0 크기의 버튼을 반환하여 스크롤바 화살표를 숨깁니다.
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            return btn;
        }
        
        @Override
        protected JButton createIncreaseButton(int orientation) { 
            // 0 크기의 버튼을 반환하여 스크롤바 화살표를 숨깁니다.
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            return btn;
        }
        
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (!c.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 8, 8);
        }
        
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(uiFont.deriveFont(16f));
        label.setForeground(BROWN);
        return label;
    }

    private JButton createStyledButton(String text, int w, int h) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(14f));
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(15, BROWN, 1));
        btn.setPreferredSize(new Dimension(w, h));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createPageButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(14f));
        btn.setPreferredSize(new Dimension(35, 35));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (isActive) {
            btn.setBackground(BROWN);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new RoundedBorder(10, BROWN, 1));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(BROWN);
            btn.setBorder(new RoundedBorder(10, BORDER_COLOR, 1));
        }
        return btn;
    }

    private void styleTable(JTable table) {
        table.setFont(uiFont.deriveFont(14f)); 
        table.setRowHeight(40);
        table.setSelectionBackground(HIGHLIGHT_YELLOW);
        table.setSelectionForeground(BROWN);
        table.setGridColor(new Color(230, 230, 230));
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(uiFont.deriveFont(16f));
        header.setBackground(HEADER_YELLOW);
        header.setForeground(BROWN);
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BROWN));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 1 || i == 2) { 
                table.getColumnModel().getColumn(i).setCellRenderer(center);
            }
        }
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
                    if (text.equals("커뮤니티")) return;
                    if (text.equals("빈 강의실")) { /* new EmptyClassFrame(); dispose(); */ }
                    else if (text.equals("공간대여")) { /* new SpaceRentFrame(); dispose(); */ }
                    else if (text.equals("물품대여")) { /* new ItemListFrame(); dispose(); */ }
                    else if (text.equals("간식행사") || text.equals("과행사")) { /* new EventListFrame(); dispose(); */ }
                    else if (text.equals("마이페이지")) { /* new MainFrame(); dispose(); */ }
                    else JOptionPane.showMessageDialog(null, "준비중입니다.");
                }
            });
        }
        return btn;
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CommunityFrame::new);
    }
}