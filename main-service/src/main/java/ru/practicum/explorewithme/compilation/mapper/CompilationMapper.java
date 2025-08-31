package ru.practicum.explorewithme.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.explorewithme.compilation.dto.ResponseCompilationDto;
import ru.practicum.explorewithme.compilation.dto.RequestCompilationDto;
import ru.practicum.explorewithme.compilation.model.Compilation;
import ru.practicum.explorewithme.event.mapper.EventMapper;

@Mapper(componentModel = "spring",
        uses = {EventMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompilationMapper {

    ResponseCompilationDto toCompilationDto(Compilation compilation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Compilation toCompilation(RequestCompilationDto requestCompilationDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    void updateCompilationFromDto(RequestCompilationDto requestCompilationDto, @MappingTarget Compilation compilation);

}
