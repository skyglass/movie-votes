package net.skycomposer.moviebets.movie.service;

import net.skycomposer.moviebets.movie.dao.repository.UserExtraRepository;
import net.skycomposer.moviebets.movie.exception.UserExtraNotFoundException;
import net.skycomposer.moviebets.movie.dao.entity.UserExtra;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserExtraService {

    private final UserExtraRepository userExtraRepository;

    public UserExtra validateAndGetUserExtra(String username) {
        return getUserExtra(username).orElseThrow(() -> new UserExtraNotFoundException(username));
    }

    public Optional<UserExtra> getUserExtra(String username) {
        return userExtraRepository.findById(username);
    }

    public UserExtra saveUserExtra(UserExtra userExtra) {
        return userExtraRepository.save(userExtra);
    }
}
