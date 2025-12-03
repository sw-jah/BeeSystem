package beehub;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ItemManager {

    // 물품 데이터 구조 (직렬화 가능)
    public static class Item implements Serializable {
        private static final long serialVersionUID = 1L;
        public String name;
        public int stock;
        public int rentDays;
        public String targetMajor;
        public String imagePath;

        public Item(String n, int s, int r, String t, String i) {
            name = n; stock = s; rentDays = r; targetMajor = t; imagePath = i;
        }
    }

    private static List<Item> items = new ArrayList<>();
    private static final String FILE_PATH = "bee_items.dat";

    // 클래스 로딩 시 파일에서 데이터 불러옴
    static {
        loadData();
    }

    public static List<Item> getAllItems() {
        return items;
    }

    public static void addItem(Item item) {
        items.add(item);
        saveData(); // 저장
    }

    public static void deleteItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            saveData(); // 저장
        }
    }

    // 수정 후 저장
    public static void save() {
        saveData();
    }

    // 재고 감소
    public static void decreaseStock(String itemName) {
        for (Item item : items) {
            if (item.name.equals(itemName)) {
                if (item.stock > 0) item.stock--;
                saveData(); // 저장
                break;
            }
        }
    }

    private static void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(items);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadData() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            initDefaultData();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            items = (List<Item>) ois.readObject();
        } catch (Exception e) {
            initDefaultData();
        }
    }

    private static void initDefaultData() {
        items = new ArrayList<>();
        items.add(new Item("C타입 충전기", 3, 1, "전체 학과", null));
        items.add(new Item("노트북", 5, 3, "소프트웨어융합학과", null));
        items.add(new Item("전공책(자바)", 2, 7, "소프트웨어융합학과", null));
        saveData();
    }
}