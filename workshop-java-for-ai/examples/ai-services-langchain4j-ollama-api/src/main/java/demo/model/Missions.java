package demo.model;

import java.util.List;
import java.util.function.Consumer;

public record Missions(int year, List<Mission> missions) {

    public int size() {
        return missions.size();
    }

    public void forEach(final Consumer<? super Mission> consumer) {
        missions.forEach(consumer);
    }
}
