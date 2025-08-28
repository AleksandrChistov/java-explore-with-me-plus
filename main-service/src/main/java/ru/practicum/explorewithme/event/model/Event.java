package ru.practicum.explorewithme.event.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.event.enums.State;
import ru.practicum.explorewithme.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@ToString
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 2000)
    @Column(name = "annotation", nullable = false)
    private String annotation;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @ToString.Exclude
    private Category category;

    @NotNull
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @NotBlank
    @Size(max = 7000)
    @Column(name = "description", nullable = false)
    private String description;

    @NotNull
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    @ToString.Exclude
    private User initiator;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    @ToString.Exclude
    private Location location;

    @Column(name = "paid", nullable = false)
    private Boolean paid;

    @Column(name = "participant_limit", nullable = false)
    private Long participantLimit;

    @Column(name = "request_moderation", nullable = false)
    private Boolean requestModeration = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private State state;

    @NotBlank
    @Size(max = 120)
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;
        return id.equals(event.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
