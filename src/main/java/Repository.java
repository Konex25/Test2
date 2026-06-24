import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Repository<K, T extends Identifiable<K>> {
    private final Map<K, T> items = new LinkedHashMap<>();

    public Repository() {
    }

    public void add(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null.");
        }
        // Use id as map key
        K id = item.getId();
        if (id == null) {
            throw new IllegalArgumentException("Item id cannot be null.");
        }
        if (items.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate id: " + id);
        }
        items.put(id, item);
    }

    public Optional<T> findById(K id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null.");
        }
        return Optional.ofNullable(items.get(id));
    }

    public boolean removeById(K id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null.");
        }
        return items.remove(id) != null;
    }

    public List<T> findAll() {
        // Return copy
        return new ArrayList<>(items.values());
    }

    public int size() {
        return items.size();
    }
}
