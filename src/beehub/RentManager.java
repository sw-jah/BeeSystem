package beehub;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RentManager {

    public static class RentData implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public String itemId;   // 물품 고유 ID (재고 관리용)
        public String itemName; // 대여 당시 물품명 (기록 보존용)
        public String renterId;
        public String renterName;
        public LocalDate rentDate;
        public LocalDate dueDate;
        public boolean isReturned;

        public RentData(String itemId, String itemName, String id, String name, LocalDate start, LocalDate end, boolean returned) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.renterId = id;
            this.renterName = name;
            this.rentDate = start;
            this.dueDate = end;
            this.isReturned = returned;
        }
    }

    private static List<RentData> rentList = new ArrayList<>();
    private static final String FILE_PATH = "bee_rentals.dat";

    static {
        loadData();
    }

    public static List<RentData> getAllRentals() {
        return rentList;
    }

    public static void addRental(RentData data) {
        rentList.add(data);
        saveData();
    }

    // [핵심] 해당 물품 ID로 현재 대여 중(미반납)인 기록이 있는지 확인
    public static boolean isItemCurrentlyRented(String itemId) {
        for (RentData data : rentList) {
            if (data.itemId.equals(itemId) && !data.isReturned) {
                return true; 
            }
        }
        return false;
    }

    public static void save() {
        saveData();
    }

    private static void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(rentList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadData() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            rentList = (List<RentData>) ois.readObject();
        } catch (Exception e) {
            rentList = new ArrayList<>();
        }
    }
}