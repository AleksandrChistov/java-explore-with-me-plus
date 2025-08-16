package ru.practicum.client;

import ru.practicum.StatsDto;
import ru.practicum.StatsParams;
import ru.practicum.StatsView;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface StatsClient {

    CompletableFuture<Void> hit(StatsDto statsDto);

    CompletableFuture<List<StatsView>> getStats(StatsParams statsParams);

}
