package admin;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class PenaltyManager {
    // ==========================================
    // 🏢 공간 대여(Space) 관련 데이터
    // ==========================================
    // 아이디 : 경고 횟수
    private static Map<String, Integer> warningCounts = new HashMap<>();
    // 아이디 : 공간 대여 정지 해제 날짜
    private static Map<String, LocalDate> banEndDates = new HashMap<>();

    // ==========================================
    // 📦 물품 대여(Item) 관련 데이터
    // ==========================================
    // 아이디 : 물품 대여 정지 해제 날짜 (연체 패널티)
    private static Map<String, LocalDate> rentalBanEndDates = new HashMap<>();
    // 아이디 : 현재 대여 중인 물품 개수
    private static Map<String, Integer> currentRentalCounts = new HashMap<>();


    // ==========================================
    // [신규] 물품 대여 관련 메소드
    // ==========================================

    /**
     * 🚨 물품 연체 패널티 부여 (관리자 반납 처리 시 호출)
     * @param userId 학번
     * @param overdueDays 연체된 일수 (이 기간만큼 대여 정지)
     */
    public static void setRentalBan(String userId, long overdueDays) {
        if (overdueDays <= 0) return;

        LocalDate releaseDate = LocalDate.now().plusDays(overdueDays);
        rentalBanEndDates.put(userId, releaseDate);
        System.out.println("[시스템] " + userId + "님은 연체로 인해 " + releaseDate + "까지 대여가 금지됩니다.");
    }

    /**
     * 🚫 대여 정지 남은 일수 확인
     * @return 0이면 대여 가능, 1 이상이면 남은 정지 일수
     */
    public static long getRentalBanDaysRemaining(String userId) {
        if (!rentalBanEndDates.containsKey(userId)) return 0;

        LocalDate banUntil = rentalBanEndDates.get(userId);
        LocalDate today = LocalDate.now();

        if (today.isAfter(banUntil)) {
            rentalBanEndDates.remove(userId); // 기한 지났으면 해제
            return 0;
        }

        // 남은 일수 계산 (오늘부터 정지 해제일까지)
        return ChronoUnit.DAYS.between(today, banUntil);
    }

    /**
     * 📦 현재 대여 중인 물품 개수 증가 (대여 시)
     */
    public static void increaseRentalCount(String userId) {
        currentRentalCounts.put(userId, currentRentalCounts.getOrDefault(userId, 0) + 1);
    }

    /**
     * 📦 현재 대여 중인 물품 개수 감소 (반납 시)
     */
    public static void decreaseRentalCount(String userId) {
        int count = currentRentalCounts.getOrDefault(userId, 0);
        if (count > 0) {
            currentRentalCounts.put(userId, count - 1);
        }
    }

    /**
     * 🔢 현재 대여 중인 개수 조회
     */
    public static int getCurrentRentalCount(String userId) {
        return currentRentalCounts.getOrDefault(userId, 0);
    }


    // ==========================================
    // [기존] 공간 대여 경고 관련 메소드
    // ==========================================

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

    // ⚠️ 현재 경고 횟수 (관리자 화면 표시용) - [이 부분이 누락되어 에러 발생했음]
    public static int getWarningCount(String userId) {
        return warningCounts.getOrDefault(userId, 0);
    }
}