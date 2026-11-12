package demo;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class Worker implements Runnable {

    private final Map<Integer, MapValue> map = createMap();

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            final Collection<MapValue> values = map.values();
            for (final MapValue value : values) {
                if (!map.containsKey(value.id())) {
                    throw new IllegalStateException("Now this is strange!!");
                }
            }
            Thread.yield();
        }
    }

    private static Map<Integer, MapValue> createMap() {
        return IntStream.range(0, 10_000)
                .mapToObj(MapValue::new)
                .collect(Collectors.toMap(MapValue::id, Function.identity()));
    }
}
