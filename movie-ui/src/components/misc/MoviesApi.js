import axios from 'axios'
import { config } from '../../Constants'

export const moviesApi = {
  getMovies,
  getMovie,
  saveMovie,
  deleteMovie,
  addMovieComment,
  getUserExtrasMe,
  saveUserExtrasMe,
  getMovieVoteStatuses,
  placeVote
}

function getMovies() {
  return instance.get('/api/movie/')
}

function getMovie(imdbId) {
  return instance.get(`/api/movie/${imdbId}`)
}

function saveMovie(movie, token) {
  return instance.post('/api/movie/', movie, {
    headers: { 'Authorization': bearerAuth(token) }
  })
}

function updateMovie(movie, token) {
  return instance.post('/api/movie/admin', movie, {
    headers: { 'Authorization': bearerAuth(token) }
  })
}

function deleteMovie(imdbId, token) {
  return instance.delete(`/api/movie/admin/${imdbId}`, {
    headers: { 'Authorization': bearerAuth(token) }
  })
}

function addMovieComment(imdbId, comment, token) {
  return instance.post(`/api/movie/${imdbId}/comments`, comment, {
    headers: { 'Authorization': bearerAuth(token) }
  })
}

function getUserExtrasMe(token) {
  return instance.get(`/api/movie/userextras/me`, {
    headers: { 'Authorization': bearerAuth(token) }
  })
}

function saveUserExtrasMe(token, userExtra) {
  return instance.post(`/api/movie/userextras/me`, userExtra, {
    headers: { 'Authorization': bearerAuth(token) }
  })
}

function getMovieVoteStatuses(token, imdbIds) {
  return instance.post(`/api/bet/get-movie-vote-statuses`, { ids: imdbIds }, {
    headers: { 'Authorization': bearerAuth(token) }
  })
}

function placeVote(token, voteRequest) {
  return instance.post(`/api/bet/place-request`, voteRequest, {
    headers: { 'Authorization': bearerAuth(token) }
  })
}

// -- Axios

const instance = axios.create({
  baseURL: config.url.BASE_URL
})

instance.interceptors.response.use(response => {
  return response
}, function (error) {
  if (error.response.status === 404) {
    return { status: error.response.status }
  }
  return Promise.reject(error.response)
})

// -- Helper functions

function bearerAuth(token) {
  return `Bearer ${token}`
}