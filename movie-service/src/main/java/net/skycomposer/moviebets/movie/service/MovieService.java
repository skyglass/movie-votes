package net.skycomposer.moviebets.movie.service;

import net.skycomposer.moviebets.movie.dao.repository.MovieRepository;
import net.skycomposer.moviebets.movie.exception.MovieNotFoundException;
import net.skycomposer.moviebets.movie.dao.entity.Movie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public Movie validateAndGetMovie(String imdbId) {
        return movieRepository.findById(imdbId).orElseThrow(() -> new MovieNotFoundException(imdbId));
    }

    public List<Movie> getMovies() {
        return movieRepository.findAll();
    }

    public List<Movie> getMoviesByIds(List<String> ids) {
        return movieRepository.findAllById(ids);
    }

    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    public void deleteMovie(Movie movie) {
        movieRepository.delete(movie);
    }
}