package net.skycomposer.moviebets.movie.dao.repository;

import net.skycomposer.moviebets.movie.dao.entity.UserExtra;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserExtraRepository extends MongoRepository<UserExtra, String> {
}
