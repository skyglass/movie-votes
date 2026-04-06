package net.skycomposer.moviebets.movie.dao.repository;

import net.skycomposer.moviebets.movie.dao.entity.Movie;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends MongoRepository<Movie, String> {
}