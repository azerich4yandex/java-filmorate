package ru.yandex.practicum.filmorate.storage.memory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

@Slf4j
@Component
public class InMemoryMpaStorage implements MpaStorage {

    public final HashMap<Long, Mpa> ratings = new HashMap<>();
    private long generatedId;

    @Override
    public Collection<Mpa> findAll(Integer size, Integer from) {
        return ratings.values().stream()
                .skip(from)
                .limit(size)
                .toList();
    }

    @Override
    public Optional<Mpa> findById(Long ratingId) {
        return Optional.ofNullable(ratings.get(ratingId));
    }

    @Override
    public Mpa createRating(Mpa rating) {
        rating.setId(getNextId());
        save(rating);

        return rating;
    }

    @Override
    public Mpa updateRating(Mpa newRating) {
        save(newRating);
        return newRating;
    }

    public void save(Mpa rating) {
        ratings.put(rating.getId(), rating);
    }

    @Override
    public void deleteRating(Long ratingId) {
        ratings.remove(ratingId);
    }

    @Override
    public void clearRatings() {
        ratings.clear();
    }

    @Override
    public boolean isNameAlreadyUser(Mpa rating) {
        return ratings.values().stream().anyMatch(
                existingRating -> !existingRating.getId().equals(rating.getId()) && existingRating.getName()
                        .equalsIgnoreCase(rating.getName()));
    }

    private long getNextId() {
        return ++generatedId;
    }
}
