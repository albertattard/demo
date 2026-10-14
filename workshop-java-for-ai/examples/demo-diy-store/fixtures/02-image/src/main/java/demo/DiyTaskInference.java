package demo;

import java.util.List;

public record DiyTaskInference(
        boolean supported,
        List<String> taskChoices) {}
