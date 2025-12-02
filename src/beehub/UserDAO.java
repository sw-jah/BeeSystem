package beehub;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import admin.DBUtil; 

public class UserDAO {

    // 학생회 정보 리턴용 클래스
    public static class CouncilInfo {
        public String id;
        public String name; 
        public CouncilInfo(String id, String name) { this.id = id; this.name = name; }
    }

    /**
     * 👤 일반 사용자 로그인 인증
     */
    public boolean checkUserLogin(String id, String pw) {
        // 1. 가상 DB (UserManager) 우선 확인
        if (UserManager.login(id, pw)) {
            return true;
        }
        
        // 2. 실제 DB 확인 (백업용 / 추후 연결 시 사용)
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean isSuccess = false;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM MEMBER WHERE id = ? AND password = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, pw);
            rs = pstmt.executeQuery();
            if (rs.next()) isSuccess = true;
        } catch (Exception e) { e.printStackTrace(); } 
        finally { close(conn, pstmt, rs); }
        
        return isSuccess;
    }

    /**
     * 👑 관리자 로그인 인증
     */
    public boolean checkAdminLogin(String id, String pw) {
        if (id.equals("admin") && pw.equals("1234")) return true;
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean isSuccess = false;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM ADMIN WHERE id = ? AND password = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id); pstmt.setString(2, pw);
            rs = pstmt.executeQuery();
            if (rs.next()) isSuccess = true;
        } catch (Exception e) { e.printStackTrace(); } 
        finally { close(conn, pstmt, rs); }
        return isSuccess;
    }

    /**
     * 🎓 학생회 로그인 (정보 반환)
     */
    public CouncilInfo getCouncilInfo(String id, String pw) {
        if (id.equals("council_soft") && pw.equals("1234")) return new CouncilInfo("council_soft", "소프트웨어융합학과");
        if (id.equals("council_general") && pw.equals("1234")) return new CouncilInfo("council_general", "총학생회");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        CouncilInfo info = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT id, dept_name FROM COUNCIL WHERE id = ? AND password = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id); pstmt.setString(2, pw);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                info = new CouncilInfo(rs.getString("id"), rs.getString("dept_name"));
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { close(conn, pstmt, rs); }
        return info;
    }

    /**
     * [신규] 본인 확인 (학번과 이름 일치 여부)
     */
    public boolean checkUserMatch(String id, String name) {
        // 1. UserManager에서 확인
        User user = UserManager.getUserById(id);
        if (user != null && user.getName().equals(name)) {
            return true;
        }

        // 2. DB에서 확인
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean isMatch = false;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM MEMBER WHERE id = ? AND name = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            rs = pstmt.executeQuery();
            
            if (rs.next()) isMatch = true;

        } catch (Exception e) { e.printStackTrace(); } 
        finally { close(conn, pstmt, rs); }
        
        return isMatch;
    }

    private void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if(rs != null) rs.close();
            if(pstmt != null) pstmt.close();
            if(conn != null) conn.close();
        } catch(Exception e) {}
    }
}