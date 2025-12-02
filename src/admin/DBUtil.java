package admin;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBUtil {
    // [중요] 친구분이 설정한 DB 정보로 꼭 수정해야 합니다!
    // 아래는 예시 정보입니다.
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver"; 
    private static final String URL = "jdbc:mysql://localhost:3306/beehub_db?serverTimezone=UTC"; 
    private static final String USER = "root"; 
    private static final String PASS = "1234"; 

    public static Connection getConnection() {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ DB 연결 실패! (DBUtil.java의 아이디/비번/주소를 확인하세요)");
            return null;
        }
    }
}