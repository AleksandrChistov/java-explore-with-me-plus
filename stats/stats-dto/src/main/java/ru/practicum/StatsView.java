package ru.practicum;

import lombok.Data;

@Data
public class StatsView {
    private final String app;
    private final String uri;
    private final Long hits;
}
