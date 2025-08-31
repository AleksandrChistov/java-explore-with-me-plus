package ru.practicum.explorewithme.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explorewithme.event.dto.LocationDto;
import ru.practicum.explorewithme.event.model.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    @Mapping(target = "id", ignore = true)
    Location toEntity(LocationDto dto);

    LocationDto toDto(Location location);

}
