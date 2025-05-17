import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Notebook {
    private final Map<String, List<String>> data = new ConcurrentHashMap<>();

    public synchronized void loadFromFile(String path) throws Exception {
        try (Scanner scanner = new Scanner(new java.io.File(path))) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(":");
                String name = parts[0].trim();
                List<String> phones = Arrays.asList(parts[1].split(","));
                data.put(name, new ArrayList<>(phones));
            }
        }
    }

    public synchronized void saveToFile(String path) throws Exception {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(path)) {
            for (var entry : data.entrySet()) {
                writer.println(entry.getKey() + ":" + String.join(",", entry.getValue()));
            }
        }
    }

    public Map<String, List<String>> getAll() {
        return data;
    }

    public synchronized void addUser(String name) {
        data.putIfAbsent(name, new ArrayList<>());
    }

    public synchronized void addPhone(String name, String phone) {
        data.computeIfAbsent(name, k -> new ArrayList<>()).add(phone);
    }

    // DOP (POISK KONKRETNOGO POLSOVATELIA)
    public synchronized List<String> getPhonesByName(String name) {
        return data.get(name);
    }
}
