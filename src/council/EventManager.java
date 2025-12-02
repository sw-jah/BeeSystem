package council;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventManager {
    
    private static List<EventData> events = new ArrayList<>();
    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // 회비 납부 조건 Enum
    public enum FeeType {
        NONE("제한없음"), SCHOOL("학교 학생회비"), DEPT("과 학생회비");
        private String label;
        FeeType(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    static {
        // 더미 데이터: 총학생회 행사 (학교 회비 필요)
        addEvent(new EventData("council_general", "총학생회", 
                "기말고사 간식행사", 
                LocalDateTime.parse("2023-12-05 12:00", DATE_FMT), 
                "학생회관", 
                LocalDateTime.parse("2023-12-01 09:00", DATE_FMT), 
                LocalDateTime.parse("2023-12-04 18:00", DATE_FMT), 
                100, 0, "1234", "진행중", "시험기간 응원 간식 배부합니다!", FeeType.SCHOOL));
        
        // 더미 데이터: 소프트웨어학과 행사 (과 회비 필요)
        addEvent(new EventData("council_soft", "소프트웨어융합학과", 
                "SW 페스티벌", 
                LocalDateTime.parse("2023-11-20 10:00", DATE_FMT), 
                "50주년 기념관", 
                LocalDateTime.parse("2023-11-10 00:00", DATE_FMT), 
                LocalDateTime.parse("2023-11-18 23:59", DATE_FMT), 
                200, 0, null, "종료", "다양한 SW 전시 및 체험", FeeType.DEPT));
    }

    public static void addEvent(EventData e) { events.add(e); }
    public static List<EventData> getAllEvents() { return events; }
    
    // 특정 학생회가 주최한 행사만 가져오기
    public static List<EventData> getEventsByOwner(String ownerId) {
        return events.stream().filter(e -> e.ownerId.equals(ownerId)).collect(Collectors.toList());
    }
    
    public static List<EventData> getOngoingEvents() {
        return events.stream().filter(e -> "진행중".equals(e.status)).collect(Collectors.toList());
    }

    public static class EventData {
        public String ownerId;   // 주최자 ID (예: council_soft)
        public String targetDept; // 대상 학과 (예: 소프트웨어융합학과, 총학생회)
        public String title;
        public LocalDateTime date;      
        public String location;
        public LocalDateTime startDateTime; 
        public LocalDateTime endDateTime;   
        public int totalCount;
        public int currentCount;
        public String secretCode;
        public String status;
        public String description;
        public FeeType requiredFee; // [추가] 필요 회비
        public List<String[]> recipients = new ArrayList<>();

        public EventData(String ownerId, String targetDept, String t, LocalDateTime d, String l, LocalDateTime start, LocalDateTime end, 
                         int total, int cur, String code, String stat, String desc, FeeType fee) {
            this.ownerId = ownerId; this.targetDept = targetDept;
            this.title = t; this.date = d; this.location = l; 
            this.startDateTime = start; this.endDateTime = end;
            this.totalCount = total; this.currentCount = cur; this.secretCode = code;
            this.status = stat; this.description = desc;
            this.requiredFee = fee;
            this.currentCount = 0; 
        }
        
        public void addRecipient(String name, String id, String paid) {
            recipients.add(new String[]{ String.valueOf(recipients.size() + 1), name, id, paid });
            currentCount = recipients.size();
        }
        
        public String getPeriodString() {
            return startDateTime.format(DATE_FMT) + " ~ " + endDateTime.format(DATE_FMT);
        }
    }
}