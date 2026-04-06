package net.skycomposer.moviebets.common.dto.movie;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMovieRequest {
    private @NotBlank String imdbId;
    private @NotBlank String title;
    private @NotBlank String director;
    private @NotBlank String year;
    private String poster;
}