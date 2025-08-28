package ru.practicum.explorewithme.compilation.service;

import ru.practicum.explorewithme.compilation.dto.ResponseCompilationDto;

import java.util.List;

public interface CompilationService {

    List<ResponseCompilationDto> getCompilations(Boolean pinned, int from, int size);

    ResponseCompilationDto getCompilation(long compId);

}
