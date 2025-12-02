package admin;

import java.util.ArrayList;
import java.util.List;

public class LotteryManager {
    
    // 모든 회차 정보를 저장하는 리스트 (DB 대용)
    private static List<LotteryRound> rounds = new ArrayList<>();

    // 초기 더미 데이터 (프로그램 시작 시 자동 로드)
    static {
        addRound("1회차: 기말고사 간식 이벤트", "치킨 기프티콘", 2, "2023-12-20", "2023-12-01 ~ 2023-12-15", "학생회관 2층", "2023-12-21 ~ 2023-12-24");
        addRound("2회차: 방학 맞이 이벤트", "백화점 상품권", 1, "2024-01-10", "2024-01-01 ~ 2024-01-08", "행정관 1층", "2024-01-11 ~ 2024-01-15");
        
        // 더미 응모자 추가
        if (!rounds.isEmpty()) {
            rounds.get(0).addApplicant("김슈니", "20231234", 1);
            rounds.get(0).addApplicant("이멋사", "20210001", 3);
        }
    }

    // 회차 추가 메서드
    public static void addRound(String name, String prize, int count, String annDate, String appPeriod, String loc, String pickPeriod) {
        rounds.add(new LotteryRound(name, prize, count, annDate, appPeriod, loc, pickPeriod));
    }

    // 전체 회차 가져오기
    public static List<LotteryRound> getAllRounds() {
        return rounds;
    }

    // ==========================================
    // 📦 공통 데이터 클래스 (관리자/사용자 모두 사용)
    // ==========================================
    public static class LotteryRound {
        public String name;           // 회차 제목 (예: "1회차: ...")
        public String prizeName;      // 경품명
        public int winnerCount;       // 당첨 인원
        public String announcementDate; // 발표일 (YYYY-MM-DD)
        public String applicationPeriod;// 응모 기간
        public String pickupLocation;   // 수령 장소
        public String pickupPeriod;     // 수령 기간
        public boolean isDrawn = false; // 추첨 여부
        
        public List<Applicant> applicants = new ArrayList<>(); // 응모자 명단

        public LotteryRound(String name, String prize, int count, String annDate, String appPeriod, String loc, String pickPeriod) {
            this.name = name;
            this.prizeName = prize;
            this.winnerCount = count;
            this.announcementDate = annDate;
            this.applicationPeriod = appPeriod;
            this.pickupLocation = loc;
            this.pickupPeriod = pickPeriod;
        }

        public void addApplicant(String name, String id, int count) {
            // 이미 응모한 사람이면 횟수만 증가
            for (Applicant app : applicants) {
                if (app.hakbun.equals(id)) {
                    app.count += count;
                    return;
                }
            }
            // 신규 응모
            applicants.add(new Applicant(name, id, count));
        }
    }

    public static class Applicant {
        public String name;
        public String hakbun;
        public int count;
        public String status = "추첨 전"; // "당첨", "미당첨"

        public Applicant(String name, String id, int count) {
            this.name = name; this.hakbun = id; this.count = count;
        }
    }
}