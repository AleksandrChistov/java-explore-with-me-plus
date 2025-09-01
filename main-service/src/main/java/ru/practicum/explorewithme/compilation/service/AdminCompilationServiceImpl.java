package ru.practicum.explorewithme.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsParams;
import ru.practicum.StatsView;
import ru.practicum.client.StatsClient;
import ru.practicum.explorewithme.compilation.dao.CompilationRepository;
import ru.practicum.explorewithme.compilation.dto.CreateCompilationDto;
import ru.practicum.explorewithme.compilation.dto.UpdateCompilationDto;
import ru.practicum.explorewithme.compilation.dto.ResponseCompilationDto;
import ru.practicum.explorewithme.compilation.mapper.CompilationMapper;
import ru.practicum.explorewithme.compilation.model.Compilation;
import ru.practicum.explorewithme.error.exception.NotFoundException;
import ru.practicum.explorewithme.event.dao.EventRepository;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.mapper.EventMapper;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.request.dao.RequestRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.explorewithme.consts.ConstantUtil.EPOCH_LOCAL_DATE_TIME;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCompilationServiceImpl implements AdminCompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;
    private final StatsClient statClient;

    @Override
    public ResponseCompilationDto save(CreateCompilationDto requestCompilationDto) {
        Compilation newCompilation = compilationMapper.toCompilation(requestCompilationDto);

        if (requestCompilationDto.getEvents() == null || requestCompilationDto.getEvents().isEmpty()) {
            Compilation saved = compilationRepository.save(newCompilation);
            return compilationMapper.toCompilationDto(saved, Collections.emptySet());
        }

        Set<Event> events = eventRepository.findAllByIdIn(requestCompilationDto.getEvents());

        newCompilation.setEvents(events);

        Compilation saved = compilationRepository.save(newCompilation);

        List<Long> eventIds = events.stream().map(Event::getId).toList();

        Map<Long, Long> confirmedRequests = requestRepository.getConfirmedRequestsByEventIds(eventIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));

        StatsParams statsParams = new StatsParams();
        statsParams.setStart(EPOCH_LOCAL_DATE_TIME);
        statsParams.setEnd(LocalDateTime.now());
        statsParams.setUris(eventIds.stream()
                .map(id -> "/events/" + id)
                .toList());
        statsParams.setUnique(false);

        Map<Long, Long> views = statClient.getStats(statsParams)
                .stream()
                .collect(Collectors.toMap(
                        sv -> Long.parseLong(sv.getUri().split("/")[2]),
                        StatsView::getHits
                ));

        Set<EventShortDto> eventShortDtos = events.stream()
                .map(event -> eventMapper.toEventShortDto(event,
                        Optional.ofNullable(confirmedRequests.get(event.getId())).orElse(0L),
                        Optional.ofNullable(views.get(event.getId())).orElse(0L)))
                .collect(Collectors.toSet());

        return compilationMapper.toCompilationDto(saved, eventShortDtos);
    }

    @Override
    public ResponseCompilationDto update(long compId, UpdateCompilationDto updateCompilationDto) {
        Compilation fromDb = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        compilationMapper.updateCompilationFromDto(updateCompilationDto, fromDb);

        if (updateCompilationDto.getEvents() == null || updateCompilationDto.getEvents().isEmpty()) {
            Compilation updated = compilationRepository.save(fromDb);
            return compilationMapper.toCompilationDto(updated, Collections.emptySet());
        }

        Set<Event> events = eventRepository.findAllByIdIn(updateCompilationDto.getEvents());

        fromDb.setEvents(events);

        Compilation updated = compilationRepository.save(fromDb);

        List<Long> eventIds = events.stream().map(Event::getId).toList();

        Map<Long, Long> confirmedRequests = requestRepository.getConfirmedRequestsByEventIds(eventIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));

        StatsParams statsParams = new StatsParams();
        statsParams.setStart(EPOCH_LOCAL_DATE_TIME);
        statsParams.setEnd(LocalDateTime.now());
        statsParams.setUris(eventIds.stream()
                .map(id -> "/events/" + id)
                .toList());
        statsParams.setUnique(false);

        Map<Long, Long> views = statClient.getStats(statsParams)
                .stream()
                .collect(Collectors.toMap(
                        sv -> Long.parseLong(sv.getUri().split("/")[2]),
                        StatsView::getHits
                ));

        Set<EventShortDto> eventShortDtos = events.stream()
                .map(event -> eventMapper.toEventShortDto(event,
                        Optional.ofNullable(confirmedRequests.get(event.getId())).orElse(0L),
                        Optional.ofNullable(views.get(event.getId())).orElse(0L)))
                .collect(Collectors.toSet());

        return compilationMapper.toCompilationDto(updated, eventShortDtos);
    }

    @Override
    public void delete(long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }

        compilationRepository.deleteById(compId);
    }
}
