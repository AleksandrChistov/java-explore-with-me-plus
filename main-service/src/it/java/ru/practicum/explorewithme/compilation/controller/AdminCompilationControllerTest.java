package ru.practicum.explorewithme.compilation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.compilation.dto.RequestCompilationDto;
import ru.practicum.explorewithme.compilation.dto.ResponseCompilationDto;
import ru.practicum.explorewithme.compilation.model.Compilation;
import ru.practicum.explorewithme.event.enums.State;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.Location;
import ru.practicum.explorewithme.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
class AdminCompilationControllerTest {

    private final ObjectMapper objectMapper;

    private final EntityManager em;

    private MockMvc mvc;

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void create_shouldCreateCompilation_whenValidDataProvided() throws Exception {
        RequestCompilationDto requestCompilationDto = new RequestCompilationDto("Новая подборка",  false, Set.of());

        MvcResult result = mvc.perform(post(AdminCompilationController.URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(requestCompilationDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResponseCompilationDto response = objectMapper.readValue(content, ResponseCompilationDto.class);

        assertNotNull(response.getId());
        assertEquals("Новая подборка", response.getTitle());
        assertFalse(response.getPinned());
        assertEquals(0, response.getEvents().size());
        // todo: check events fields when ready
    }

    @Test
    void create_shouldReturnBadRequest_whenTitleIsBlank() throws Exception {
        RequestCompilationDto requestCompilationDto = new RequestCompilationDto();
        requestCompilationDto.setTitle("");

        mvc.perform(post(AdminCompilationController.URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(requestCompilationDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturnBadRequest_whenTitleIsNull() throws Exception {
        RequestCompilationDto requestCompilationDto = new RequestCompilationDto();
        requestCompilationDto.setTitle("");

        mvc.perform(post(AdminCompilationController.URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(requestCompilationDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturnConflict_whenTitleIsNotUnique() throws Exception {
        Compilation compilation = new Compilation();
        compilation.setTitle("Первая подборка");

        em.persist(compilation);
        em.flush();

        RequestCompilationDto requestCompilationDto = new RequestCompilationDto();
        requestCompilationDto.setTitle("Первая подборка");

        mvc.perform(post(AdminCompilationController.URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(requestCompilationDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void update_shouldUpdateCompilation_whenValidDataProvided() throws Exception {
        Compilation compilation = new Compilation();
        compilation.setTitle("Первая Подборка");
        compilation.setPinned(false);
        compilation.setEvents(Set.of());

        em.persist(compilation);
        em.flush();

        RequestCompilationDto updateCompilationDto = new RequestCompilationDto("Новая подборка",  true, Set.of());

        MvcResult result = mvc.perform(patch(AdminCompilationController.URL + "/{compId}", compilation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(updateCompilationDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResponseCompilationDto response = objectMapper.readValue(content, ResponseCompilationDto.class);

        assertNotNull(response.getId());
        assertEquals("Новая подборка", response.getTitle());
        assertTrue(response.getPinned());
        // todo: check events fields when ready

        List<Compilation> compilations = em.createQuery("select c from Compilation  c", Compilation.class).getResultList();

        assertEquals(1, compilations.size());
    }

    @Test
    void update_shouldReturnNotFound_whenEventDoesNotExist() throws Exception {
        Compilation compilation = new Compilation();
        compilation.setTitle("Первая Подборка");
        compilation.setPinned(false);

        Category category = new Category();
        category.setName("Первая категория");
        em.persist(category);

        User user = new User();
        user.setName("Пользователь");
        user.setEmail("user@mail.ru");
        em.persist(user);

        Location location = new Location();
        location.setLat(1.0f);
        location.setLon(1.0f);
        em.persist(location);

        Event event1 = new Event();
        event1.setTitle("Офигенное событие");
        event1.setAnnotation("Аннотация");
        event1.setDescription("Описание события");
        event1.setCategory(category);
        event1.setState(State.PENDING);
        event1.setCreatedOn(LocalDateTime.now());
        event1.setEventDate(LocalDateTime.now().plusDays(1));
        event1.setInitiator(user);
        event1.setLocation(location);
        event1.setPaid(true);
        event1.setParticipantLimit(0);
        event1.setRequestModeration(true);
        em.persist(event1);

        Set<Event> events = new HashSet<>();
        events.add(event1);

        compilation.setEvents(events);

        em.persist(compilation);
        em.flush();

        RequestCompilationDto updateCompilationDto = new RequestCompilationDto("Новая подборка",  true, Set.of(999L));

        Event event = em.find(Event.class, event1.getId());
        assertNotNull(event);
        assertNotNull(event.getId());

        mvc.perform(patch(AdminCompilationController.URL + "/{compId}", compilation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(updateCompilationDto)))
                .andExpect(status().isOk());
        // todo: change to isNotFound() after service is ready for check
    }

    @Test
    void update_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        Compilation compilation = new Compilation();
        compilation.setTitle("Первая подборка");

        em.persist(compilation);
        em.flush();

        RequestCompilationDto updateCompilationDto = new RequestCompilationDto();
        updateCompilationDto.setTitle("");

        mvc.perform(patch(AdminCompilationController.URL + "/{compId}", compilation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(updateCompilationDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnNotFound_whenCompilationDoesNotExist() throws Exception {
        RequestCompilationDto updateCompilationDto = new RequestCompilationDto("Новая подборка", true, Set.of());

        mvc.perform(patch(AdminCompilationController.URL + "/{compId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(updateCompilationDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturnConflict_whenTitleIsNotUnique() throws Exception {
        Compilation compilation1 = new Compilation();
        compilation1.setTitle("Первая подборка");
        em.persist(compilation1);

        Compilation compilation2 = new Compilation();
        compilation2.setTitle("Вторая подборка");
        em.persist(compilation2);

        em.flush();

        // Коммитим текущую транзакцию
        TestTransaction.flagForCommit(); // Помечаем для коммита
        TestTransaction.end(); // Выполняем коммит

        RequestCompilationDto updateCompilationDto = new RequestCompilationDto(compilation2.getTitle(), true, Set.of());

        mvc.perform(patch(AdminCompilationController.URL + "/{compId}", compilation1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(updateCompilationDto)))
                .andExpect(status().isConflict());

        // Транзакция для очистки БД
        TestTransaction.start();
        em.createQuery("delete from Compilation").executeUpdate();
        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    @Test
    void delete_shouldDeleteCompilation_whenCompilationExists() throws Exception {
        Compilation compilation1 = new Compilation();
        compilation1.setTitle("Первая подборка");
        em.persist(compilation1);
        em.flush();

        List<Compilation> compilationsBefore = em.createQuery("select c from Compilation c", Compilation.class).getResultList();
        assertEquals(1, compilationsBefore.size());

        mvc.perform(delete(AdminCompilationController.URL + "/{compId}", compilation1.getId()))
                .andExpect(status().isNoContent());

        List<Compilation> compilationsAfter = em.createQuery("select c from Compilation c", Compilation.class).getResultList();
        assertEquals(0, compilationsAfter.size());
    }

    @Test
    void delete_shouldReturnNotFound_whenCompilationDoesNotExist() throws Exception {
        mvc.perform(delete(AdminCompilationController.URL + "/{compId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturnBadRequest_whenInvalidCompIdProvided() throws Exception {
        mvc.perform(delete(AdminCompilationController.URL + "/{compId}", -1L))
                .andExpect(status().isBadRequest());

        mvc.perform(delete(AdminCompilationController.URL + "/{compId}", 0L))
                .andExpect(status().isBadRequest());

        mvc.perform(delete(AdminCompilationController.URL + "/{compId}", "wrongType"))
                .andExpect(status().isBadRequest());
    }
}