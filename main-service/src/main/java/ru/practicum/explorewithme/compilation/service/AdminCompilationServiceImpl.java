package ru.practicum.explorewithme.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.compilation.dao.CompilationRepository;
import ru.practicum.explorewithme.compilation.dto.RequestCompilationDto;
import ru.practicum.explorewithme.compilation.dto.ResponseCompilationDto;
import ru.practicum.explorewithme.compilation.mapper.CompilationMapper;
import ru.practicum.explorewithme.compilation.model.Compilation;
import ru.practicum.explorewithme.error.exception.NotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCompilationServiceImpl implements AdminCompilationService {

    private final CompilationRepository compilationRepository;

    private final CompilationMapper compilationMapper;

    @Override
    public ResponseCompilationDto save(RequestCompilationDto requestCompilationDto) {
        Compilation newCompilation = compilationMapper.toCompilation(requestCompilationDto);

        // todo: get events by ids (batch) - eventRepository.findAllByIdIn(requestCompilationDto.getEvents())
        // newCompilation.setEvents(events) and then save

        Compilation saved = compilationRepository.save(newCompilation);

        return compilationMapper.toCompilationDto(saved);
    }

    @Override
    public ResponseCompilationDto update(long compId, RequestCompilationDto requestCompilationDto) {
        Compilation fromDb = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        compilationMapper.updateCompilationFromDto(requestCompilationDto, fromDb);

        // todo: get events by ids (batch) - eventRepository.findAllByIdIn(requestCompilationDto.getEvents())
        // check events.size() == requestCompilationDto.getEvents().size()
        // fromDb.setEvents(events) and then save or throw new NotFoundException("Category with id=27 was not found")

        Compilation updated = compilationRepository.save(fromDb);

        return compilationMapper.toCompilationDto(updated);
    }

    @Override
    public void delete(long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }

        compilationRepository.deleteById(compId);
    }
}
