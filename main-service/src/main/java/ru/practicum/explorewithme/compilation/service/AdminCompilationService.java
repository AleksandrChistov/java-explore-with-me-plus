package ru.practicum.explorewithme.compilation.service;

import ru.practicum.explorewithme.compilation.dto.ResponseCompilationDto;
import ru.practicum.explorewithme.compilation.dto.RequestCompilationDto;

public interface AdminCompilationService {

    ResponseCompilationDto save(RequestCompilationDto requestCompilationDto);

    ResponseCompilationDto update(long compId, RequestCompilationDto requestCompilationDto);

    void delete(long compId);

}
