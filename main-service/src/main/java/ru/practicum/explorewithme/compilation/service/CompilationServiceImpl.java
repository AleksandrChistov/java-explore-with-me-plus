package ru.practicum.explorewithme.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsParams;
import ru.practicum.StatsView;
import ru.practicum.client.StatsClient;
import ru.practicum.explorewithme.compilation.dao.CompilationRepository;
import ru.practicum.explorewithme.compilation.dto.ResponseCompilationDto;
import ru.practicum.explorewithme.compilation.mapper.CompilationMapper;
import ru.practicum.explorewithme.compilation.model.Compilation;
import ru.practicum.explorewithme.error.exception.NotFoundException;
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
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final RequestRepository requestRepository;
    private final StatsClient statClient;
    private final EventMapper eventMapper;

    @Override
    public List<ResponseCompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        List<Compilation> compilations;

        if (pinned == null) {
            compilations = compilationRepository
                    .findAll(pageable)
                    .toList();
        } else {
            compilations = compilationRepository
                    .findAllByPinned(pinned, pageable)
                    .toList();
        }

        List<Event> events = compilations.stream()
                .map(Compilation::getEvents)
                .flatMap(Set::stream)
                .toList();

        if (events.isEmpty()) {
            return compilations.stream()
                    .map(compilation -> compilationMapper.toCompilationDto(compilation, Collections.emptySet()))
                    .collect(Collectors.toList());
        }

        List<Long> eventIds = events.stream().map(Event::getId).toList();

        return compilations.stream()
                .map(c -> {
                    Set<EventShortDto> compilationEventDtos = getEventShortDtos(
                            c.getEvents(),
                            getConfirmedRequests(eventIds),
                            getViews(eventIds)
                    );
                    return compilationMapper.toCompilationDto(c, compilationEventDtos);
                })
                .toList();
    }

    @Override
    public ResponseCompilationDto getCompilation(long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        if (compilation.getEvents().isEmpty()) {
            return compilationMapper.toCompilationDto(compilation, Collections.emptySet());
        }

        List<Long> eventIds = compilation.getEvents().stream().map(Event::getId).toList();

        Set<EventShortDto> eventShortDtos = getEventShortDtos(
                compilation.getEvents(),
                getConfirmedRequests(eventIds),
                getViews(eventIds)
        );

        return compilationMapper.toCompilationDto(compilation, eventShortDtos);
    }

    private Map<Long, Long> getConfirmedRequests(List<Long> eventIds) {
        return requestRepository.getConfirmedRequestsByEventIds(eventIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));
    }

    private Map<Long, Long> getViews(List<Long> eventIds) {
        StatsParams statsParams = new StatsParams();
        statsParams.setStart(EPOCH_LOCAL_DATE_TIME);
        statsParams.setEnd(LocalDateTime.now());
        statsParams.setUris(eventIds.stream()
                .map(id -> "/events/" + id)
                .toList());
        statsParams.setUnique(false);

        return statClient.getStats(statsParams)
                .stream()
                .collect(Collectors.toMap(
                        sv -> Long.parseLong(sv.getUri().split("/")[2]),
                        StatsView::getHits
                ));
    }

    private Set<EventShortDto> getEventShortDtos(Set<Event> events, Map<Long, Long> confirmedRequests, Map<Long, Long> views) {
        return events.stream()
                .map(event -> eventMapper.toEventShortDto(event,
                        Optional.ofNullable(confirmedRequests.get(event.getId())).orElse(0L),
                        Optional.ofNullable(views.get(event.getId())).orElse(0L)))
                .collect(Collectors.toSet());
    }

}
