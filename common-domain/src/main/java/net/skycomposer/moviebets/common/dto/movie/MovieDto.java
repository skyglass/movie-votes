package net.skycomposer.moviebets.common.dto.movie;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDto {
    private String imdbId;
    private String title;
    private String director;
    private String year;
    private String poster;
    private List<CommentDto> comments;
}