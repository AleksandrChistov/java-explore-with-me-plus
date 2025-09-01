package ru.practicum.explorewithme.compilation.service;

import ru.practicum.explorewithme.compilation.dto.CreateCompilationDto;
import ru.practicum.explorewithme.compilation.dto.ResponseCompilationDto;
import ru.practicum.explorewithme.compilation.dto.UpdateCompilationDto;

public interface AdminCompilationService {

    ResponseCompilationDto save(CreateCompilationDto requestCompilationDto);

    ResponseCompilationDto update(long compId, UpdateCompilationDto updateCompilationDto);

    void delete(long compId);

}
