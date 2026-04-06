import React from 'react'
import { Card, Header, Segment } from 'semantic-ui-react'
import MovieCard from './MovieCard'
import { useKeycloak } from '@react-keycloak/web'

function MovieList({ movies, voteStatuses = {} }) {
    const { keycloak } = useKeycloak()

    const movieList = movies.map(movie => {
        const canVote = keycloak.authenticated && voteStatuses[movie.imdbId] // assume `false` means not voted
        return (
            <MovieCard
                key={movie.imdbId}
                movie={movie}
                link={true}
                canVote={canVote}
            />
        )
    })

    return (
        movies.length > 0 ? (
            <Card.Group doubling centered>
                {movieList}
            </Card.Group>
        ) : (
            <Segment padded color='blue'>
                <Header textAlign='center' as='h4'>No movies</Header>
            </Segment>
        )
    )
}

export default MovieList