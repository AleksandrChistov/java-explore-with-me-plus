package ru.practicum.explorewithme.mapper;

import org.mapstruct.Mapper;
import ru.practicum.StatsDto;
import ru.practicum.explorewithme.model.Stats;

@Mapper(componentModel = "spring")
public abstract class StatsMapper {
    public abstract Stats toStats(StatsDto statsDto);
}
