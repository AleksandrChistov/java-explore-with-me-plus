package ru.practicum.explorewithme.compilation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.compilation.dto.ResponseCompilationDto;
import ru.practicum.explorewithme.compilation.dto.RequestCompilationDto;
import ru.practicum.explorewithme.compilation.service.AdminCompilationService;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = AdminCompilationController.URL, produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminCompilationController {

    public static final String URL = "/admin/compilations";

    private final AdminCompilationService adminCompilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseCompilationDto create(@Valid @RequestBody RequestCompilationDto compilationDto) {
        return adminCompilationService.save(compilationDto);
    }

    @PatchMapping("/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseCompilationDto update(
            @Valid @RequestBody RequestCompilationDto compilationDto,
            @Positive @PathVariable Long compId
    ) {
        return adminCompilationService.update(compId, compilationDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Positive @PathVariable Long compId) {
        adminCompilationService.delete(compId);
    }

}
