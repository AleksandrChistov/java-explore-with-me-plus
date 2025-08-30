package ru.practicum.explorewithme.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.explorewithme.request.dto.RequestDto;
import ru.practicum.explorewithme.request.model.Request;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    RequestMapper INSTANCE = Mappers.getMapper(RequestMapper.class);

    @Mapping(source = "event.id", target = "event")
    @Mapping(source = "requester.id", target = "requester")
    @Mapping(source = "status", target = "status")
    RequestDto toRequestDto(Request request);

    List<RequestDto> toRequestDtoList(List<Request> requests);
}
