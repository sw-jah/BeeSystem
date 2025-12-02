package beehub;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import admin.DBUtil; 

public class UserDAO {

    // 학생회 정보 리턴용 클래스
    public static class CouncilInfo {
        public String id;
        public String name; // 학과명 (예: 소프트웨어융합학과)
        public CouncilInfo(String id, String name) { this.id = id; this.name = name; }
    }

    public boolean checkUserLogin(String id, String pw) {
        if (id.equals("202390000") && pw.equals("1234")) return true;
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean isSuccess = false;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM MEMBER WHERE id = ? AND password = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id); pstmt.setString(2, pw);
            rs = pstmt.executeQuery();
            if (rs.next()) isSuccess = true;
        } catch (Exception e) { e.printStackTrace(); } 
        finally { close(conn, pstmt, rs); }
        return isSuccess;
    }

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
     * 🎓 학생회 로그인 (수정됨: 정보 반환)
     */
    public CouncilInfo getCouncilInfo(String id, String pw) {
        // [테스트 계정]
        if (id.equals("council_soft") && pw.equals("1234")) return new CouncilInfo("council_soft", "소프트웨어융합학과");
        if (id.equals("council_general") && pw.equals("1234")) return new CouncilInfo("council_general", "총학생회");
        if (id.equals("council_chem") && pw.equals("1234")) return new CouncilInfo("council_chem", "화학과");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        CouncilInfo info = null;

        try {
            conn = DBUtil.getConnection();
            // DB 테이블 컬럼: id, password, dept_name
            String sql = "SELECT id, dept_name FROM COUNCIL WHERE id = ? AND password = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, pw);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                info = new CouncilInfo(rs.getString("id"), rs.getString("dept_name"));
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { close(conn, pstmt, rs); }
        return info;
    }

    private void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if(rs != null) rs.close();
            if(pstmt != null) pstmt.close();
            if(conn != null) conn.close();
        } catch(Exception e) {}
    }
}