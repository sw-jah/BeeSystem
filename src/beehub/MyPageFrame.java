package beehub;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
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

// CommunityFrame의 Post 클래스를 사용할 수 없으므로, 내부 클래스로 정의
class MyPagePost {
    int no; String title; String writer; String date; int likes; int comments; String content;
    public MyPagePost(int n, String t, String w, String d, int l, int c, String content) {
        this.no = n; this.title = t; this.writer = w; this.date = d; this.likes = l; this.comments = c; this.content = content;
    }
}

// 물품 대여 기록을 위한 데이터 구조
class RentalItem {
    String itemName;
    String returnDate; // YYYY-MM-DD format
    boolean isReturned;

    public RentalItem(String name, String date, boolean returned) {
        this.itemName = name;
        this.returnDate = date;
        this.isReturned = returned;
    }
}

// 예약 상태 Enum
enum ReservationStatus {
    CANCELLABLE, // 취소 가능 (미래 예약)
    COMPLETED,   // 완료 (이미 사용했거나 시간이 지난 예약)
    USER_CANCELLED, // 사용자 취소 완료
    AUTO_CANCELLED  // 시스템 자동 취소 (10분 미입장 등)
}


// 공간 대여 기록을 위한 데이터 구조
class SpaceRentalItem {
    String roomName;
    String reservationDate; // YYYY-MM-DD
    String startTime;       // HH:MM
    String endTime;         // HH:MM
    int headcount;
    ReservationStatus status; 
    
    public SpaceRentalItem(String name, String date, String startTime, String endTime, int count, ReservationStatus status) {
        this.roomName = name;
        this.reservationDate = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.headcount = count;
        this.status = status;
    }
}

// 과 행사 참여 기록을 위한 데이터 구조
class EventParticipationItem {
    String eventTitle;
    String eventDate; // YYYY-MM-DD
    String eventTime; // HH:MM
    boolean requiresSecretCode; // 비밀코드 여부
    ReservationStatus status; 

    public EventParticipationItem(String title, String date, String time, boolean requiresCode, ReservationStatus status) {
        this.eventTitle = title;
        this.eventDate = date;
        this.eventTime = time;
        this.requiresSecretCode = requiresCode;
        this.status = status;
    }
}


public class MyPageFrame extends JFrame {

    // 🎨 컬러 테마
    private static final Color HEADER_YELLOW = new Color(255, 238, 140);
    private static final Color NAV_BG = new Color(255, 255, 255);
    private static final Color BG_MAIN = new Color(255, 255, 255);
    private static final Color BROWN = new Color(89, 60, 28);
    private static final Color HIGHLIGHT_YELLOW = new Color(255, 245, 157);
    private static final Color BORDER_COLOR = new Color(220, 220, 220);
    private static final Color POPUP_BG = new Color(255, 250, 205);
    private static final Color LINK_COLOR = new Color(0, 102, 204);
    private static final Color OVERDUE_RED = new Color(200, 50, 50);
    private static final Color CANCEL_RED = new Color(200, 50, 50);

    private static final String FONT_NAME_HTML = "던파 비트비트체 v2"; // [추가] HTML 폰트명 상수

    private static Font uiFont;

    static {
        try {
            // 폰트 로딩 로직
            InputStream is = MyPageFrame.class.getResourceAsStream("/fonts/DNFBitBitv2.ttf");
            if (is == null) {
                File f = new File("resource/fonts/DNFBitBitv2.ttf");
                if (f.exists()) {
                    uiFont = Font.createFont(Font.TRUETYPE_FONT, f).deriveFont(14f);
                } else {
                    uiFont = new Font("맑은 고딕", Font.PLAIN, 14);
                }
            } else {
                uiFont = Font.createFont(Font.TRUETYPE_FONT, is);
                uiFont = uiFont.deriveFont(14f);
            }
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(uiFont);
        } catch (Exception e) {
            uiFont = new Font("SansSerif", Font.PLAIN, 14);
        }
    }

    // 사용자 정보 (더미 데이터)
    private String userName = "김꿀단지";
    private String userDept = "소프트웨어융합학과";
    private String userId = "202390000";
    private String userNickname = "꿀벌학생";
    private int userPoint = 100;
    
    // UI 컴포넌트
    private JList<String> menuList;
    private CardLayout cardLayout;
    private JPanel detailPanel;
    private JLabel nicknameLabel;
    private ImageIcon beeIcon; 
    private ImageIcon heartIcon; 
    private ImageIcon commentIcon; 

    // 활동 목록 더미 데이터
    private List<MyPagePost> dummyPosts; 
    private List<RentalItem> dummyRentals; 
    private List<SpaceRentalItem> dummySpaceRentals; 
    private List<EventParticipationItem> dummyEvents; 

    // 프레임 크기 및 레이아웃 상수
    private final int FRAME_WIDTH = 800;
    private final int FRAME_HEIGHT = 680; 
    private final int CONTENT_Y = 130;
    private final int CONTENT_HEIGHT = FRAME_HEIGHT - CONTENT_Y - 30; 
    private final int MENU_WIDTH = 170; 
    private final int DETAIL_X = 20 + MENU_WIDTH + 10; 
    private final int DETAIL_WIDTH = FRAME_WIDTH - DETAIL_X - 20; 


    public MyPageFrame() {
        setTitle("서울여대 꿀단지 - 마이페이지");
        setSize(FRAME_WIDTH, FRAME_HEIGHT); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
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
    
    private void loadImages() {
         try {
            // 꿀벌 이미지
            ImageIcon originalBeeIcon = new ImageIcon("resource/img/login-bee.png");
            if (originalBeeIcon.getIconWidth() > 0) {
                Image img = originalBeeIcon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH); 
                beeIcon = new ImageIcon(img);
            }
            // 좋아요 이미지
            ImageIcon originalHeartIcon = new ImageIcon("resource/img/heart.png");
            if (originalHeartIcon.getIconWidth() > 0) {
                Image img = originalHeartIcon.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
                heartIcon = new ImageIcon(img);
            }
            // 댓글 이미지
            ImageIcon originalCommentIcon = new ImageIcon("resource/img/comment.png");
            if (originalCommentIcon.getIconWidth() > 0) {
                Image img = originalCommentIcon.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
                commentIcon = new ImageIcon(img);
            }

        } catch (Exception e) {
            System.err.println("Failed to load images.");
        }
    }
    
    // 더미 데이터 생성
    private void initDummyData() {
        LocalDate today = LocalDate.of(2025, 12, 1); // 기준 날짜 2025-12-01
        
        dummyPosts = new ArrayList<>();
        // 작성 글 (본인 글)
        dummyPosts.add(new MyPagePost(1, "커뮤니티 기능 완성! (내 글)", userNickname, today.toString(), 15, 5, "완성해서 너무 기뻐요!"));
        dummyPosts.add(new MyPagePost(2, "Spring 강의 자료 요청해요", userNickname, today.minusDays(2).toString(), 8, 3, "혹시 자료 공유 가능하신 분?"));
        // 댓글/좋아요 글 (다른 사람 글)
        dummyPosts.add(new MyPagePost(3, "점심 메뉴 추천 받습니다", "다른학생1", today.minusDays(5).toString(), 20, 10, "오늘 뭐 먹지..."));
        dummyPosts.add(new MyPagePost(4, "시험 기간 힘내세요!", "다른학생2", today.minusDays(10).toString(), 50, 2, "모두 A+ 받기를 기원합니다."));
        
        // 대여 물품 더미 데이터 
        dummyRentals = new ArrayList<>();
        dummyRentals.add(new RentalItem("노트북 3", "2025-12-04", false)); // D-3
        dummyRentals.add(new RentalItem("보조배터리 5", "2025-11-28", false)); // D+3 (연체)
        dummyRentals.add(new RentalItem("빔 프로젝터", "2025-12-10", false)); // D-9
        dummyRentals.add(new RentalItem("무선 마우스", "2025-11-20", true)); // 반납 완료
        dummyRentals.add(new RentalItem("삼각대", "2025-10-01", true)); // 반납 완료

        // 공간 대여 더미 데이터 (기준 날짜: 2025-12-01)
        dummySpaceRentals = new ArrayList<>();
        // CANCELLABLE (미래 예약)
        dummySpaceRentals.add(new SpaceRentalItem("세미나실 1", "2025-12-05", "14:00", "16:00", 8, ReservationStatus.CANCELLABLE));
        // COMPLETED (과거 완료)
        dummySpaceRentals.add(new SpaceRentalItem("실습실 F", "2025-11-25", "18:00", "20:00", 12, ReservationStatus.COMPLETED)); 
        // USER_CANCELLED (사용자 취소)
        dummySpaceRentals.add(new SpaceRentalItem("세미나실 2", "2025-12-03", "09:00", "11:00", 5, ReservationStatus.USER_CANCELLED));
        // AUTO_CANCELLED (시스템 자동 취소)
        dummySpaceRentals.add(new SpaceRentalItem("실습실 B", "2025-11-30", "11:00", "13:00", 6, ReservationStatus.AUTO_CANCELLED));
        // COMPLETED (오늘 예약, 시간이 이미 지남)
        dummySpaceRentals.add(new SpaceRentalItem("실습실 A", "2025-12-01", "10:00", "12:00", 4, ReservationStatus.COMPLETED)); 
        
        // 과 행사 참여 더미 데이터 (기준 날짜: 2025-12-01)
        dummyEvents = new ArrayList<>();
        // CANCELLABLE (미래, 비밀코드 없음) -> 참여 취소 가능
        dummyEvents.add(new EventParticipationItem("SW 멘토링 특강", "2025-12-10", "15:00", false, ReservationStatus.CANCELLABLE));
        // COMPLETED (과거)
        dummyEvents.add(new EventParticipationItem("개강총회", "2025-09-01", "18:00", false, ReservationStatus.COMPLETED)); 
        // COMPLETED (비밀코드 행사) -> 취소 불가
        dummyEvents.add(new EventParticipationItem("총학생회 간식 배부", "2025-12-05", "12:00", true, ReservationStatus.COMPLETED)); 
        // USER_CANCELLED (사용자 취소)
        dummyEvents.add(new EventParticipationItem("캡스톤 디자인 발표회", "2025-12-20", "13:00", false, ReservationStatus.USER_CANCELLED)); 
    }

    // 꿀 포인트에 따른 등급 계산
    private String getRank(int point) {
        if (point >= 200) return "여왕벌 👑";
        if (point >= 100) return "꿀벌";
        return "일벌";
    }

    private void initHeader() {
        JPanel headerPanel = new JPanel(null);
        headerPanel.setBounds(0, 0, FRAME_WIDTH, 80);
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

        JLabel logoutText = new JLabel("| 로그아웃"); 
        logoutText.setFont(uiFont.deriveFont(14f));
        logoutText.setForeground(BROWN);
        logoutText.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        logoutText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showLogoutPopup();
            }
        });

        userInfoPanel.add(logoutText);
        headerPanel.add(userInfoPanel);
    }

    private void initNav() {
        JPanel navPanel = new JPanel(new GridLayout(1, 6));
        navPanel.setBounds(0, 80, FRAME_WIDTH, 50);
        navPanel.setBackground(NAV_BG);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        add(navPanel);

        String[] menus = {"물품대여", "과행사", "공간대여", "빈 강의실", "커뮤니티", "마이페이지"}; 
        for (String menu : menus) {
            JButton menuBtn = createNavButton(menu, menu.equals("마이페이지")); 
            navPanel.add(menuBtn);
        }
    }

    private void initContent() {
        JPanel contentPanel = new JPanel(null);
        contentPanel.setBounds(0, CONTENT_Y, FRAME_WIDTH, CONTENT_HEIGHT);
        contentPanel.setBackground(BG_MAIN);
        add(contentPanel);
        
        // 좌측 메뉴 영역 (170px)
        JPanel leftPanel = new JPanel(null);
        leftPanel.setBounds(20, 20, MENU_WIDTH, CONTENT_HEIGHT - 40); 
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new RoundedBorder(20, BORDER_COLOR, 1));
        contentPanel.add(leftPanel);

        // 1-1. 메뉴 목록
        String[] menuItems = {
            "나의 활동", "회원 정보", "작성 게시글", "댓글 단 게시글", "좋아요 누른 게시글", 
            "이용 기록", "물품 대여 기록", "공간 대여 기록", "과 행사 참여 기록", 
            "--- 분리선 ---", 
            "응모함" 
        };
        menuList = new JList<>(menuItems);
        menuList.setFont(uiFont.deriveFont(16f));
        menuList.setForeground(BROWN);
        menuList.setSelectionBackground(HIGHLIGHT_YELLOW);
        menuList.setSelectionForeground(BROWN);
        menuList.setCellRenderer(new MyPageListRenderer()); 
        
        JScrollPane menuScroll = new JScrollPane(menuList);
        menuScroll.setBounds(10, 10, MENU_WIDTH - 20, CONTENT_HEIGHT - 60); 
        menuScroll.setBorder(BorderFactory.createEmptyBorder());
        menuScroll.getViewport().setBackground(Color.WHITE);
        
        // 좌우 스크롤바 제거
        menuScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        // 세련된 스크롤바 디자인 적용
        menuScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        menuScroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        menuScroll.getVerticalScrollBar().setUnitIncrement(16);
        
        leftPanel.add(menuScroll);
        
        // 2. 우측 상세 내용 영역 (580px)
        cardLayout = new CardLayout();
        detailPanel = new JPanel(cardLayout);
        detailPanel.setBounds(DETAIL_X, 20, DETAIL_WIDTH, CONTENT_HEIGHT - 40); 
        detailPanel.setBackground(Color.WHITE);
        detailPanel.setBorder(new RoundedBorder(20, BORDER_COLOR, 1));
        contentPanel.add(detailPanel);

        // 3. 상세 내용 카드 추가
        addDetailCards();
        
        // 리스너 연결 및 기본 화면 설정
        menuList.setSelectedIndex(1); // 기본 화면: 회원 정보
        cardLayout.show(detailPanel, "회원 정보");
        
        menuList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedItem = menuList.getSelectedValue();
                if (selectedItem != null) {
                    if (!selectedItem.equals("나의 활동") && !selectedItem.equals("이용 기록") && !selectedItem.equals("--- 분리선 ---")) {
                         cardLayout.show(detailPanel, selectedItem);
                    }
                }
            }
        });
    }

    private void addDetailCards() {
        // 1. 회원 정보 (첫 화면)
        JPanel infoPanel = createUserInfoPanel();
        detailPanel.add(infoPanel, "회원 정보");
        
        // 2. 작성 게시글
        detailPanel.add(createActivityListPanel("작성 게시글"), "작성 게시글");
        
        // 3. 댓글 단 게시글
        detailPanel.add(createActivityListPanel("댓글 단 게시글"), "댓글 단 게시글");
        
        // 4. 좋아요 누른 게시글
        detailPanel.add(createActivityListPanel("좋아요 누른 게시글"), "좋아요 누른 게시글");
        
        // 5. 대여 기록
        detailPanel.add(createRentalListPanel(), "물품 대여 기록"); 
        
        // 6. 공간 대여 기록
        detailPanel.add(createSpaceRentalListPanel(), "공간 대여 기록"); 
        
        // 7. 과 행사 참여 기록
        detailPanel.add(createEventListPanel(), "과 행사 참여 기록"); 

        // 8. 응모함
        JPanel applicationPanel = createPlaceholderPanel("응모함", "참여한 행사 응모 현황 및 결과가 표시됩니다.");
        detailPanel.add(applicationPanel, "응모함");
        
        // 9. 초기 화면 (카테고리 헤더용)
        JPanel welcomePanel = createPlaceholderPanel("환영합니다!", userName + "님의 마이페이지입니다.");
        detailPanel.add(welcomePanel, "나의 활동");
        detailPanel.add(welcomePanel, "이용 기록");
    }
    
    // 물품 이름의 중앙 정렬 렌더러
    class CenterRenderer extends DefaultTableCellRenderer {
        public CenterRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setFont(uiFont.deriveFont(16f)); 
            return c;
        }
    }


    // 물품 대여 기록 패널 생성
    private JPanel createRentalListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 제목 크기 확대
        JLabel titleLabel = new JLabel("물품 대여 기록", SwingConstants.LEFT);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f)); 
        titleLabel.setForeground(BROWN);
        panel.add(titleLabel, BorderLayout.NORTH);

        // 테이블 모델 및 데이터 준비
        String[] headers = {"물품 이름", "반납 기한/상태"};
        DefaultTableModel tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        for (RentalItem item : dummyRentals) {
            String status;
            if (item.isReturned) {
                status = "반납 완료";
            } else {
                status = item.returnDate; // D-Day 처리는 렌더러가 담당
            }
            tableModel.addRow(new Object[]{item.itemName, status});
        }
        
        JTable rentalTable = new JTable(tableModel);
        styleTable(rentalTable);
        
        // 컬럼 너비 설정
        rentalTable.getColumnModel().getColumn(0).setPreferredWidth(300); 
        rentalTable.getColumnModel().getColumn(1).setPreferredWidth(250); 

        // 물품 이름 컬럼에 CenterRenderer 적용
        rentalTable.getColumnModel().getColumn(0).setCellRenderer(new CenterRenderer());

        // 반납 기한/상태 컬럼에 커스텀 렌더러 적용
        rentalTable.getColumnModel().getColumn(1).setCellRenderer(new RentalStatusRenderer());

        JScrollPane scrollPane = new JScrollPane(rentalTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        // 취소 액션 리스너
        rentalTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = rentalTable.rowAtPoint(e.getPoint());
                int col = rentalTable.columnAtPoint(e.getPoint());

                if (col == 1 && row >= 0 && row < rentalTable.getRowCount()) {
                    String statusText = rentalTable.getValueAt(row, col).toString();
                    if (!statusText.equals("반납 완료") && statusText.contains("D-")) {
                         showCustomAlertPopup("알림", "대여 물품은 시스템상 취소가 불가능하며, 반납 후 자동 기록됩니다.");
                    }
                }
            }
        });


        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    // 공간 대여 기록 패널 생성
    private JPanel createSpaceRentalListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("공간 대여 기록", SwingConstants.LEFT);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f)); 
        titleLabel.setForeground(BROWN);
        panel.add(titleLabel, BorderLayout.NORTH);

        // 테이블 모델 및 데이터 준비
        String[] headers = {"빌린 방", "대여 일자", "인원", "상태/취소"}; 
        DefaultTableModel tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        for (SpaceRentalItem item : dummySpaceRentals) {
            // SpaceRentalItem 객체 자체를 테이블에 저장하여 렌더러가 참조하도록 함
            tableModel.addRow(new Object[]{item.roomName, item, item.headcount, item.status});
        }
        
        JTable spaceRentalTable = new JTable(tableModel);
        styleTable(spaceRentalTable);
        
        // 컬럼 너비 설정
        spaceRentalTable.getColumnModel().getColumn(0).setPreferredWidth(100); // 빌린 방
        spaceRentalTable.getColumnModel().getColumn(1).setPreferredWidth(200); // 대여 일자 (시간 포함)
        spaceRentalTable.getColumnModel().getColumn(2).setPreferredWidth(50);  // 인원
        spaceRentalTable.getColumnModel().getColumn(3).setPreferredWidth(120); // 상태/취소
        
        // 컬럼 렌더러 적용
        spaceRentalTable.getColumnModel().getColumn(0).setCellRenderer(new CenterRenderer());
        
        // 날짜/시간 정보를 보기 좋게 표시하는 렌더러 적용
        spaceRentalTable.getColumnModel().getColumn(1).setCellRenderer(new SpaceDateTimeRenderer()); 
        
        spaceRentalTable.getColumnModel().getColumn(2).setCellRenderer(new CenterRenderer());

        // '상태/취소' 컬럼에 커스텀 렌더러 적용
        spaceRentalTable.getColumnModel().getColumn(3).setCellRenderer(new SpaceActionRenderer());
        
        // 예약 취소 액션 리스너
        JTable finalSpaceRentalTable = spaceRentalTable;
        DefaultTableModel finalSpaceTableModel = tableModel;
        spaceRentalTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = finalSpaceRentalTable.rowAtPoint(e.getPoint());
                int col = finalSpaceRentalTable.columnAtPoint(e.getPoint());
                
                // 상태/취소 컬럼 클릭 시
                if (col == 3 && row >= 0 && row < dummySpaceRentals.size()) {
                    SpaceRentalItem item = dummySpaceRentals.get(row);
                    
                    if (item.status == ReservationStatus.CANCELLABLE) {
                        showCustomConfirmPopup(
                            "'" + item.roomName + " (" + item.reservationDate + ")' 예약을 취소하시겠습니까?", // 팝업 메시지
                            () -> {
                                // 상태 변경 및 테이블 새로고침
                                item.status = ReservationStatus.USER_CANCELLED;
                                finalSpaceTableModel.fireTableDataChanged(); // [수정] 모델에 변경 사항 알림
                                showCustomAlertPopup("취소 완료", item.roomName + " 예약이 취소 완료되었습니다.");
                            }
                        );
                    }
                }
            }
        });


        JScrollPane scrollPane = new JScrollPane(spaceRentalTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    // 과 행사 참여 기록 패널 생성
    private JPanel createEventListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("과 행사 참여 기록", SwingConstants.LEFT);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f)); 
        titleLabel.setForeground(BROWN);
        panel.add(titleLabel, BorderLayout.NORTH);

        // 테이블 모델 및 데이터 준비
        String[] headers = {"행사 제목", "행사 일정", "상태/취소"};
        DefaultTableModel tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        // 데이터 로딩
        for (EventParticipationItem item : dummyEvents) {
            // item 객체 자체를 테이블에 넣어 렌더러가 상태를 확인하도록 함
            tableModel.addRow(new Object[]{item.eventTitle, item, item.status}); 
        }
        
        JTable eventTable = new JTable(tableModel);
        styleTable(eventTable);
        
        // 컬럼 너비 설정
        eventTable.getColumnModel().getColumn(0).setPreferredWidth(250); // 행사 제목
        eventTable.getColumnModel().getColumn(1).setPreferredWidth(180); // 행사 일정
        eventTable.getColumnModel().getColumn(2).setPreferredWidth(100); // 상태/취소
        
        // 컬럼 렌더러 적용
        eventTable.getColumnModel().getColumn(0).setCellRenderer(new CenterRenderer()); // 제목 중앙 정렬
        eventTable.getColumnModel().getColumn(1).setCellRenderer(new EventScheduleRenderer()); // 일정 렌더러
        eventTable.getColumnModel().getColumn(2).setCellRenderer(new EventActionRenderer()); // 상태/취소 렌더러
        
        // 예약 취소 액션 리스너
        JTable finalEventTable = eventTable;
        DefaultTableModel finalEventTableModel = tableModel;
        eventTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = finalEventTable.rowAtPoint(e.getPoint());
                int col = finalEventTable.columnAtPoint(e.getPoint());
                
                if (col == 2 && row >= 0 && row < dummyEvents.size()) {
                    EventParticipationItem item = dummyEvents.get(row);
                    
                    if (item.status == ReservationStatus.CANCELLABLE) {
                        showCustomConfirmPopup(
                            "'" + item.eventTitle + " (" + item.eventDate + ")' 참여를 취소하시겠습니까?",
                            () -> {
                                // 상태 변경 및 테이블 새로고침
                                item.status = ReservationStatus.USER_CANCELLED;
                                finalEventTableModel.fireTableDataChanged(); // [수정] 모델에 변경 사항 알림
                                showCustomAlertPopup("참여 취소 완료", item.eventTitle + " 참여가 취소 완료되었습니다.");
                            }
                        );
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(eventTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }


    // 대여 일자(날짜 + 시간)를 표시하는 렌더러 (공간 대여용)
    class SpaceDateTimeRenderer extends DefaultTableCellRenderer {
        public SpaceDateTimeRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel label = (JLabel) c;
            
            // value는 SpaceRentalItem 객체임
            if (value instanceof SpaceRentalItem) {
                SpaceRentalItem item = (SpaceRentalItem) value;
                label.setText(item.reservationDate + " " + item.startTime + "~" + item.endTime); // 날짜와 시간 결합
            }
            label.setFont(uiFont.deriveFont(16f));
            return c;
        }
    }

    // 과 행사 일정 렌더러
    class EventScheduleRenderer extends DefaultTableCellRenderer {
        public EventScheduleRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel label = (JLabel) c;
            
            if (value instanceof EventParticipationItem) {
                EventParticipationItem item = (EventParticipationItem) value;
                label.setText(item.eventDate + " (" + item.eventTime + ")"); // 날짜와 시간 결합
            }
            label.setFont(uiFont.deriveFont(16f));
            return c;
        }
    }


    // 공간 대여 '상태/취소' 액션 렌더러
    class SpaceActionRenderer extends DefaultTableCellRenderer {
        public SpaceActionRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel label = (JLabel) c;
            
            label.setFont(uiFont.deriveFont(16f)); 

            if (isSelected) {
                label.setBackground(HIGHLIGHT_YELLOW);
            } else {
                label.setBackground(Color.WHITE);
            }
            
            ReservationStatus status = (ReservationStatus) value;
            label.setForeground(BROWN); // 기본 글자색
            
            switch (status) {
                case CANCELLABLE:
                    label.setText("<html><u>취소</u></html>");
                    label.setForeground(CANCEL_RED); 
                    break;
                case COMPLETED:
                    label.setText("완료"); 
                    break;
                case USER_CANCELLED:
                    label.setText("취소 완료");
                    break;
                case AUTO_CANCELLED:
                    label.setText("예약 취소"); 
                    label.setForeground(OVERDUE_RED);
                    label.setFont(uiFont.deriveFont(Font.BOLD, 16f));
                    break;
            }
            
            return label;
        }
    }
    
    // 과 행사 '상태/취소' 액션 렌더러
    class EventActionRenderer extends DefaultTableCellRenderer {
        public EventActionRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel label = (JLabel) c;
            
            label.setFont(uiFont.deriveFont(16f)); 
            label.setForeground(BROWN); // 기본 글자색

            if (isSelected) {
                label.setBackground(HIGHLIGHT_YELLOW);
            } else {
                label.setBackground(Color.WHITE);
            }
            
            ReservationStatus status = (ReservationStatus) value;
            
            switch (status) {
                case CANCELLABLE:
                    label.setText("<html><u>참여 취소</u></html>");
                    label.setForeground(CANCEL_RED); 
                    break;
                case COMPLETED:
                    label.setText("완료"); 
                    break;
                case USER_CANCELLED:
                    label.setText("취소 완료");
                    break;
                default:
                    label.setText(""); 
                    break; 
            }
            
            return label;
        }
    }


    
    // D-Day 계산 및 연체 색상 처리 렌더러 (물품 대여용)
    class RentalStatusRenderer extends DefaultTableCellRenderer {
        
        public RentalStatusRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel label = (JLabel) c;
            
            label.setFont(uiFont.deriveFont(16f)); 

            if (isSelected) {
                label.setBackground(HIGHLIGHT_YELLOW);
            } else {
                label.setBackground(Color.WHITE);
            }
            
            String statusText = value.toString();
            
            if (statusText.equals("반납 완료")) {
                label.setText(statusText);
                label.setForeground(BROWN); // 일반 색상
            } else {
                // 반납 기한 날짜인 경우 D-Day 계산
                String dDayStatus = formatDDay(statusText);
                label.setText(statusText + " (" + dDayStatus + ")");
                
                if (dDayStatus.startsWith("D+")) {
                    label.setForeground(OVERDUE_RED); // 연체 시 빨간색
                    label.setFont(uiFont.deriveFont(Font.BOLD, 16f));
                } else {
                    label.setForeground(BROWN); // 일반 D-Day
                }
            }
            
            return label;
        }
    }
    
    // D-Day 계산 유틸리티
    private String formatDDay(String dateStr) {
        try {
            // 현재 날짜를 2025-12-01로 고정하여 계산 (더미 데이터 기준)
            LocalDate today = LocalDate.of(2025, 12, 1);
            LocalDate returnDate = LocalDate.parse(dateStr);
            
            long daysDiff = ChronoUnit.DAYS.between(today, returnDate);

            if (daysDiff == 0) {
                return "D-DAY";
            } else if (daysDiff > 0) {
                return "D-" + daysDiff;
            } else {
                return "D+" + Math.abs(daysDiff);
            }
        } catch (java.time.format.DateTimeParseException e) {
            return "날짜 오류";
        }
    }
    
    // 활동 목록을 표시하는 JTable 패널 생성 (제목만 표시)
    private JPanel createActivityListPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 제목 크기 확대
        JLabel titleLabel = new JLabel(title, SwingConstants.LEFT);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f)); 
        titleLabel.setForeground(BROWN);
        panel.add(titleLabel, BorderLayout.NORTH);

        // 테이블 모델 및 데이터 준비
        String[] headers = {"제목"}; 
        DefaultTableModel tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        // 더미 데이터 로딩 (필터링)
        List<MyPagePost> filteredList = dummyPosts.stream()
            .filter(post -> {
                if (title.equals("작성 게시글")) return post.writer.equals(userNickname);
                if (title.equals("댓글 단 게시글")) return post.comments > 0; 
                if (title.equals("좋아요 누른 게시글")) return post.likes > 10; 
                return false;
            }).collect(Collectors.toList());

        for (MyPagePost post : filteredList) {
            tableModel.addRow(new Object[]{post.title}); 
        }
        
        JTable activityTable = new JTable(tableModel);
        styleTable(activityTable);
        
        // 컬럼 너비 설정: 제목만 전체 너비 사용 (580 - 30 padding = 550)
        activityTable.getColumnModel().getColumn(0).setPreferredWidth(550); 
        
        // 활동 목록 테이블 제목에 CenterRenderer 적용
        activityTable.getColumnModel().getColumn(0).setCellRenderer(new CenterRenderer());


        // 더블 클릭 이벤트: 게시글 상세로 이동
        activityTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = activityTable.getSelectedRow();
                    if (row != -1) {
                        String selectedTitle = (String) activityTable.getValueAt(row, 0); 
                        MyPagePost selectedPost = filteredList.stream()
                            .filter(p -> p.title.equals(selectedTitle))
                            .findFirst().orElse(null);
                        
                        if (selectedPost != null) {
                            showCustomAlertPopup("게시글 이동", selectedPost.title + " 글 상세 화면으로 이동합니다.");
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(activityTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    // 회원 정보 상세 패널
    private JPanel createUserInfoPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 제목 크기 확대
        JLabel titleLabel = new JLabel("회원 정보", SwingConstants.LEFT);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(BROWN);
        titleLabel.setBounds(20, 10, 200, 30);
        panel.add(titleLabel);
        
        // 구분선
        JSeparator separator = new JSeparator();
        separator.setBounds(20, 45, 520, 1);
        panel.add(separator);

        // 정보 레이아웃
        int y = 70;
        
        // 이름
        y = addInfoRow(panel, y, "이름", userName, 400, false, null);
        
        // 학과/학번 (전체 표시, valueWidth 380으로 수정)
        y = addInfoRow(panel, y, "학과/학번", userDept + " / " + userId, 380, false, null); 
        
        // 닉네임 수정 가능 영역
        y = addInfoRow(panel, y, "닉네임", userNickname, 250, true, new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showNicknameEditPopup();
            }
        });

        // 빈 줄 추가 (간격 확보)
        y += 20; 
        
        // 보유 꿀
        y = addInfoRow(panel, y, "보유 꿀", userPoint + "꿀", 400, false, null);
        
        // 등급 (이미지 처리)
        // 등급은 addInfoRow 대신 직접 배치하여 이미지 아이콘을 사용
        JLabel rankTitleLabel = createLabel("등급");
        rankTitleLabel.setFont(uiFont.deriveFont(16f));
        rankTitleLabel.setBounds(20, y, 100, 30);
        panel.add(rankTitleLabel);
        
        JLabel rankValueLabel = createLabel(""); 
        
        String rank = getRank(userPoint);
        rankValueLabel.setText(rank + " (" + userPoint + "/200)");
        
        // 꿀벌 이미지 적용
        if (rank.startsWith("꿀벌") && beeIcon != null) {
            rankValueLabel.setText(rankValueLabel.getText().replace("꿀벌", "꿀벌")); 
            rankValueLabel.setIcon(beeIcon);
            rankValueLabel.setHorizontalTextPosition(SwingConstants.RIGHT); 
            rankValueLabel.setIconTextGap(5);
        }
        
        rankValueLabel.setBounds(150, y, 400, 30);
        panel.add(rankValueLabel);
        y += 40; // Increment y
        
        // 비밀번호 수정 버튼 (Y축 간격 추가)
        y += 50; 
        JButton passwordBtn = createStyledButton("비밀번호 수정", 150, 40);
        passwordBtn.setBounds(20, y, 150, 40);
        passwordBtn.addActionListener(e -> showCustomAlertPopup("비밀번호 수정", "비밀번호 수정 팝업을 띄웁니다."));
        panel.add(passwordBtn);


        return panel;
    }
    
    // 정보 행 추가 헬퍼
    private int addInfoRow(JPanel panel, int y, String title, String value, int valueWidth, boolean isEditable, MouseAdapter adapter) {
        JLabel titleLabel = createLabel(title);
        titleLabel.setFont(uiFont.deriveFont(16f));
        titleLabel.setBounds(20, y, 100, 30);
        panel.add(titleLabel);

        JLabel valueLabel = createLabel(value);
        valueLabel.setFont(uiFont.deriveFont(16f));
        valueLabel.setBounds(150, y, valueWidth, 30);
        panel.add(valueLabel);
        
        if (title.equals("닉네임")) {
            this.nicknameLabel = valueLabel; // 닉네임 라벨 저장
        }

        if (isEditable && adapter != null) {
            JLabel editLink = new JLabel("<html><u>[수정]</u></html>");
            editLink.setFont(uiFont.deriveFont(14f));
            editLink.setForeground(LINK_COLOR);
            editLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
            // 닉네임 옆에 바로 위치
            editLink.setBounds(150 + valueWidth + 10, y, 50, 30); 
            editLink.addMouseListener(adapter); 
            panel.add(editLink);
        }

        return y + 40;
    }
    
    // 테이블 스타일링 (CommunityFrame.java 참고)
    private void styleTable(JTable table) {
        table.setFont(uiFont.deriveFont(16f)); // [수정] 폰트 크기 확대
        table.setRowHeight(30);
        table.setSelectionBackground(HIGHLIGHT_YELLOW);
        table.setSelectionForeground(BROWN);
        table.setGridColor(new Color(230, 230, 230));
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(uiFont.deriveFont(18f)); // [수정] 폰트 크기 확대
        header.setBackground(HEADER_YELLOW);
        header.setForeground(BROWN);
        header.setPreferredSize(new Dimension(0, 35));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BROWN));

        // 모든 셀 기본 좌측 정렬 (제목에 적합)
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
        }
    }


    // 닉네임 수정 팝업
    private void showNicknameEditPopup() {
        JDialog dialog = new JDialog(this, "닉네임 수정", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 350); // [수정] 팝업 크기 조정
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        JLabel msgLabel = new JLabel("새 닉네임을 입력하세요.", SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(18f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 70, 360, 60); // [수정] Y 위치와 높이 확대 (클리핑 방지)
        panel.add(msgLabel);
        
        JTextField inputField = new JTextField(userNickname);
        inputField.setFont(uiFont.deriveFont(16f));
        inputField.setBounds(50, 140, 300, 40);
        panel.add(inputField);

        JButton saveBtn = createPopupBtn("저장");
        saveBtn.setBounds(60, 220, 120, 45); // [수정] Y 위치 조정
        saveBtn.addActionListener(e -> {
            String newNickname = inputField.getText().trim();
            if (newNickname.isEmpty() || newNickname.length() > 10) {
                 JOptionPane.showMessageDialog(dialog, "닉네임은 1자 이상 10자 이내로 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                 return;
            }
            userNickname = newNickname; 
            nicknameLabel.setText(userNickname);
            dialog.dispose();
            showCustomAlertPopup("성공", "닉네임이 성공적으로 변경되었습니다.");
        });
        panel.add(saveBtn);

        JButton cancelBtn = createPopupBtn("취소");
        cancelBtn.setBounds(220, 220, 120, 45); // [수정] Y 위치 조정
        cancelBtn.addActionListener(e -> dialog.dispose());
        panel.add(cancelBtn);

        dialog.setVisible(true);
    }
    
    private JPanel createPlaceholderPanel(String title, String message) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 제목 크기 확대
        JLabel titleLabel = new JLabel(title, SwingConstants.LEFT);
        titleLabel.setFont(uiFont.deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(BROWN);
        panel.add(titleLabel, BorderLayout.NORTH);

        JLabel msgLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>", SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(18f));
        msgLabel.setForeground(Color.GRAY);
        panel.add(msgLabel, BorderLayout.CENTER);

        return panel;
    }
    
    // [수정] 이쁜 확인 팝업 (Runnable Callback 포함) - 폰트 적용 및 클리핑 해결
    private void showCustomConfirmPopup(String message, Runnable onConfirm) {
        JDialog dialog = new JDialog(this, "확인", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 350); // 팝업 크기 유지
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);
        
        // [수정] 폰트 적용 및 HTML/높이 수정: 팝업 메시지 폰트 적용 (CommunityDetailFrame.java의 createTextLink 참고)
        String htmlMessage = "<html><body style='text-align:center; padding: 10px;'>" +
                             "<font face='" + FONT_NAME_HTML + "'>" + 
                             message + 
                             "</font></body></html>";
                             
        JLabel msgLabel = new JLabel(htmlMessage, SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(18f)); // Fallback size for better layout calculation
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 50, 360, 90); // Y 위치와 높이 확대 (클리핑 방지)
        panel.add(msgLabel);

        JButton yesBtn = createPopupBtn("확인"); 
        yesBtn.setBounds(60, 220, 120, 45); 
        yesBtn.addActionListener(e -> {
            dialog.dispose();
            onConfirm.run();
        });
        panel.add(yesBtn);

        JButton noBtn = createPopupBtn("취소"); 
        noBtn.setBounds(220, 220, 120, 45); 
        noBtn.addActionListener(e -> dialog.dispose());
        panel.add(noBtn);

        dialog.setVisible(true);
    }


    // --- 헬퍼 및 UI 스타일링 ---
    
    // 커뮤니티 프레임에서 가져온 세련된 스크롤바 UI 클래스
    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(200, 200, 200);
            this.trackColor = new Color(245, 245, 245);
        }
        
        @Override
        protected JButton createDecreaseButton(int orientation) { 
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            return btn;
        }
        
        @Override
        protected JButton createIncreaseButton(int orientation) { 
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            return btn;
        }
        
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g;
            if (!c.isEnabled()) return;
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
    
    // 네비게이션 버튼 (MainFrame.java 참고)
    private JButton createNavButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(16f));
        btn.setForeground(BROWN);
        btn.setBackground(isActive ? HIGHLIGHT_YELLOW : NAV_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 마이페이지 외 메뉴 클릭 시 해당 프레임으로 이동 (임시)
        if (!isActive) {
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { btn.setBackground(HIGHLIGHT_YELLOW); }
                @Override
                public void mouseExited(MouseEvent e) { btn.setBackground(NAV_BG); }
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (text.equals("마이페이지")) return; 
                    
                    if (text.equals("공간대여")) { /* new SpaceRentFrame(); */ dispose();
                    } else if (text.equals("과행사")) { /* new EventListFrame(); */ dispose(); 
                    } else if (text.equals("물품대여")) { /* new ItemListFrame(); */ dispose();
                    } else if (text.equals("커뮤니티")) { /* new CommunityFrame(); */ dispose();
                    } else if (text.equals("빈 강의실")) { /* new EmptyClassFrame(); */ dispose();
                    } else {
                        showSimplePopup("알림", "[" + text + "] 화면은 준비 중입니다.");
                    }
                }
            });
        }
        return btn;
    }

    // 리스트 렌더러 (구분선 처리 및 폰트 적용)
    class MyPageListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            String text = (String) value;
            label.setFont(uiFont.deriveFont(16f));
            label.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            
            if (text.equals("나의 활동") || text.equals("이용 기록")) {
                // 카테고리 제목 강조
                label.setFont(uiFont.deriveFont(Font.BOLD, 18f));
                label.setBackground(new Color(240, 240, 240)); 
                label.setForeground(BROWN);
                label.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, BORDER_COLOR));
            } else if (text.equals("--- 분리선 ---")) {
                 // 응모함 분리선 처리
                label.setText("");
                label.setBackground(Color.WHITE);
                label.setBorder(BorderFactory.createMatteBorder(5, 0, 0, 0, BG_MAIN)); 
                label.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            } else {
                label.setForeground(BROWN);
                if (isSelected) {
                    label.setBackground(HIGHLIGHT_YELLOW);
                } else {
                    label.setBackground(Color.WHITE);
                }
            }
            return label;
        }
    }

    private void showLogoutPopup() {
        JDialog dialog = new JDialog(this, "로그아웃", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 300); 
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        JLabel msgLabel = new JLabel("로그아웃 하시겠습니까?", SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(18f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 70, 360, 60); 
        panel.add(msgLabel);

        JButton yesBtn = createPopupBtn("네");
        yesBtn.setBounds(60, 180, 120, 45); 
        yesBtn.addActionListener(e -> {
            dialog.dispose();
            // new LoginFrame(); // 로그인 화면으로 이동
            dispose();
        });
        panel.add(yesBtn);

        JButton noBtn = createPopupBtn("아니오");
        noBtn.setBounds(220, 180, 120, 45); 
        noBtn.addActionListener(e -> dialog.dispose());
        panel.add(noBtn);

        dialog.setVisible(true);
    }
    
    private void showSimplePopup(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 350); 
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        JLabel msgLabel = new JLabel(message, SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(16f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 50, 360, 90); 
        panel.add(msgLabel);

        JButton okBtn = createPopupBtn("확인");
        okBtn.setBounds(135, 220, 130, 45); 
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);

        dialog.setVisible(true);
    }
    
    private void showCustomAlertPopup(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setSize(400, 350); 
        dialog.setLocationRelativeTo(this);

        JPanel panel = createPopupPanel();
        panel.setLayout(null);
        dialog.add(panel);

        JLabel msgLabel = new JLabel(message, SwingConstants.CENTER);
        msgLabel.setFont(uiFont.deriveFont(16f));
        msgLabel.setForeground(BROWN);
        msgLabel.setBounds(20, 50, 360, 90); 
        panel.add(msgLabel);

        JButton okBtn = createPopupBtn("확인");
        okBtn.setBounds(135, 220, 130, 45); 
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);

        dialog.setVisible(true);
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
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private JButton createStyledButton(String text, int w, int h) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont.deriveFont(14f));
        btn.setBackground(BROWN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(15, BROWN, 1));
        btn.setPreferredSize(new Dimension(w, h)); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }


    private static class RoundedBorder implements Border {
        private int radius; private Color color; private int thickness;
        public RoundedBorder(int r, Color c, int t) { radius = r; color = c; thickness = t; }
        public Insets getBorderInsets(Component c) { return new Insets(radius/2, radius/2, radius/2, radius/2); }
        public boolean isBorderOpaque() { return false; }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g; 
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MyPageFrame::new);
    }
}