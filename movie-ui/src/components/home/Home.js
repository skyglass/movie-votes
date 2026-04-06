import React, { useEffect, useState } from 'react'
import { Container } from 'semantic-ui-react'
import { handleLogError } from '../misc/Helpers'
import { moviesApi } from '../misc/MoviesApi'
import MovieList from './MovieList'
import { useKeycloak } from '@react-keycloak/web'

function Home() {
  const [isLoading, setIsLoading] = useState(false)
  const [movies, setMovies] = useState([])
  const [voteStatuses, setVoteStatuses] = useState({})

  const { keycloak } = useKeycloak()

  useEffect(() => {
    const fetchMoviesAndVotes = async () => {
      setIsLoading(true)
      try {
        const response = await moviesApi.getMovies()
        const movies = response.data
        setMovies(movies)

        const imdbIds = movies.map(movie => movie.imdbId)
        const voteResp = await moviesApi.getMovieVoteStatuses(keycloak.token, imdbIds)
        const statusList = voteResp.data.result

        const statusMap = {}
        statusList.forEach(status => {
          statusMap[status.itemId] = !status.alreadyVoted
        })
        setVoteStatuses(statusMap)
      } catch (error) {
        handleLogError(error)
      } finally {
        setIsLoading(false)
      }
    }

    fetchMoviesAndVotes()
  }, [])

  return isLoading ? <></> : (
      <Container>
        <MovieList movies={movies} voteStatuses={voteStatuses} />
      </Container>
  )
}

export default Home