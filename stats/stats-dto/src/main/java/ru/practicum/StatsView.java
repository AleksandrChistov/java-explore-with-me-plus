package ru.practicum;

import lombok.*;


@Data
public class StatsView {
    private final String app;
    private final String uri;
    private final Integer hits;

    public StatsView(String app, String uri, Integer hits) {
        this.app = app;
        this.uri = uri;
        this.hits = hits;
    }
}
