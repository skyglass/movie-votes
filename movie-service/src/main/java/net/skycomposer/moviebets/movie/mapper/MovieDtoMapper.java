package net.skycomposer.moviebets.movie.mapper;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.common.dto.movie.CommentDto;
import net.skycomposer.moviebets.common.dto.movie.MovieDto;
import net.skycomposer.moviebets.movie.dao.entity.Movie;
import net.skycomposer.moviebets.movie.dao.entity.UserExtra;
import net.skycomposer.moviebets.movie.service.UserExtraService;

@RequiredArgsConstructor
@Component
public class MovieDtoMapper {

    private final UserExtraService userExtraService;

    public MovieDto toMovieDto(Movie movie) {
        List<CommentDto> comments = movie.getComments().stream()
                .map(this::toMovieDtoCommentDto)
                .toList();

        return new MovieDto(
                movie.getImdbId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getYear(),
                movie.getPoster(),
                comments
        );
    }

    public CommentDto toMovieDtoCommentDto(Movie.Comment comment) {
        String username = comment.getUsername();
        String avatar = getAvatarForUser(username);
        String text = comment.getText();
        Instant timestamp = comment.getTimestamp();

        return new CommentDto(username, avatar, text, timestamp);
    }

    private String getAvatarForUser(String username) {
        return userExtraService.getUserExtra(username)
                .map(UserExtra::getAvatar)
                .orElse(username);
    }
}
