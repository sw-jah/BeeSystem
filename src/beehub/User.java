package beehub;

public class User {
    private String id;       // 학번
    private String password;
    private String name;     // 이름
    private String dept;     // 학과
    private int points;      // 꿀 포인트

    public User(String id, String password, String name, String dept, int points) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.dept = dept;
        this.points = points;
    }

    public String getId() { return id; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getDept() { return dept; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}