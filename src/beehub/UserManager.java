package beehub;

import java.util.HashMap;
import java.util.Map;

public class UserManager {
    // 가상 DB (ID : User 객체)
    private static Map<String, User> users = new HashMap<>();
    
    // 현재 로그인한 사용자 (세션)
    private static User currentUser = null;

    static {
        // [더미 데이터] 테스트용 계정들
        users.put("20231234", new User("20231234", "1234", "김슈니", "소프트웨어융합학과", 250));
        users.put("20210001", new User("20210001", "1234", "이멋사", "시각디자인학과", 100));
        users.put("20205678", new User("20205678", "1234", "박새내", "경영학과", 50));
    }

    // 로그인 처리 (성공 시 세션에 저장)
    public static boolean login(String id, String pw) {
        User user = users.get(id);
        if (user != null && user.getPassword().equals(pw)) {
            currentUser = user; 
            return true;
        }
        return false;
    }

    // 로그아웃
    public static void logout() {
        currentUser = null;
    }

    // 현재 로그인한 사용자 정보 가져오기
    public static User getCurrentUser() {
        return currentUser;
    }
    
    // ID로 사용자 찾기 (검증용)
    public static User getUserById(String id) {
        return users.get(id);
    }
}