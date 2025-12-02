package admin; // 패키지명 확인

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class PenaltyManager {
    // 💾 메모리에 임시 저장 (DB 대신 사용)
    // 아이디 : 경고 횟수
    private static Map<String, Integer> warningCounts = new HashMap<>();
    
    // 아이디 : 정지 해제 날짜
    private static Map<String, LocalDate> banEndDates = new HashMap<>();

    /**
     * 🚨 경고 부여 (관리자가 '미입실 취소' 누를 때 호출)
     */
    public static void addWarning(String userId) {
        // 1. 기존 경고 횟수 가져와서 +1
        int count = warningCounts.getOrDefault(userId, 0) + 1;
        warningCounts.put(userId, count);

        System.out.println("[시스템] " + userId + "님 경고 누적: " + count + "회");

        // 2. 경고가 2회 이상이면 -> 7일간 정지 처리
        if (count >= 2) {
            LocalDate releaseDate = LocalDate.now().plusDays(7); // 오늘 + 7일
            banEndDates.put(userId, releaseDate);
            System.out.println("⛔ " + userId + "님은 " + releaseDate + "까지 예약이 금지됩니다.");
        }
    }

    /**
     * 🚫 예약 가능 여부 확인 (사용자가 예약 시도할 때 호출)
     * @return true면 정지 상태(예약 불가), false면 정상
     */
    public static boolean isBanned(String userId) {
        // 1. 정지 명단에 없으면 통과
        if (!banEndDates.containsKey(userId)) {
            return false;
        }

        // 2. 정지 기간 확인
        LocalDate banUntil = banEndDates.get(userId);
        LocalDate today = LocalDate.now();

        if (today.isAfter(banUntil)) {
            // 정지 기간(1주일)이 지났다면? -> 해제 및 초기화!
            banEndDates.remove(userId);
            warningCounts.remove(userId); // 경고 횟수도 0으로 리셋
            System.out.println("✅ " + userId + "님의 정지가 해제되었습니다.");
            return false; // 예약 가능
        }

        return true; // 아직 정지 기간임
    }

    // 📅 정지 해제일 날짜 가져오기 (알림창용)
    public static LocalDate getBanDate(String userId) {
        return banEndDates.get(userId);
    }
    
    // 현재 경고 횟수 (관리자 화면 표시용)
    public static int getWarningCount(String userId) {
        return warningCounts.getOrDefault(userId, 0);
    }
}