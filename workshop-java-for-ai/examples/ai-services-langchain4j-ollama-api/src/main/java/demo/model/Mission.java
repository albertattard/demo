package demo.model;

import java.time.LocalDate;
import java.util.List;

public record Mission(String name, String country, LocalDate launchDate, List<Astronaut> astronauts) {}
