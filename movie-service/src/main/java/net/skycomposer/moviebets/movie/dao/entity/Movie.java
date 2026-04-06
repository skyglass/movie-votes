package net.skycomposer.moviebets.movie.dao.entity;

import net.skycomposer.moviebets.common.dto.movie.CreateMovieRequest;
import net.skycomposer.moviebets.common.dto.movie.UpdateMovieRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "movies")
public class Movie {

    @Id
    private String imdbId;
    private String title;
    private String director;
    private String year;
    private String poster;
    private List<Comment> comments = new ArrayList<>();

    @Data
    @AllArgsConstructor
    public static class Comment {
        private String username;
        private String text;
        private Instant timestamp;
    }

    public static Movie from(CreateMovieRequest createMovieRequest) {
        Movie movie = new Movie();
        movie.setImdbId(createMovieRequest.getImdbId());
        movie.setTitle(createMovieRequest.getTitle());
        movie.setDirector(createMovieRequest.getDirector());
        movie.setYear(createMovieRequest.getYear());
        movie.setPoster(createMovieRequest.getPoster());
        return movie;
    }

    public static void updateFrom(UpdateMovieRequest updateMovieRequest, Movie movie) {
        if (updateMovieRequest.getTitle() != null) {
            movie.setTitle(updateMovieRequest.getTitle());
        }
        if (updateMovieRequest.getDirector() != null) {
            movie.setDirector(updateMovieRequest.getDirector());
        }
        if (updateMovieRequest.getYear() != null) {
            movie.setYear(updateMovieRequest.getYear());
        }
        if (updateMovieRequest.getPoster() != null) {
            movie.setPoster(updateMovieRequest.getPoster());
        }
    }
}