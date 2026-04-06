import React, { useState } from 'react'
import { Button, Card, Image, Icon } from 'semantic-ui-react'
import { Link } from 'react-router-dom'
import { useKeycloak } from '@react-keycloak/web'
import { moviesApi } from '../misc/MoviesApi'
import { getUsername, handleLogError } from '../misc/Helpers'

function MovieCard({ movie, link, canVote }) {
    const { keycloak } = useKeycloak()
    const [voted, setVoted] = useState(false)
    const [loading, setLoading] = useState(false)

    const handleVote = async (e) => {
        e.stopPropagation()
        e.preventDefault()

        if (!keycloak.authenticated) {
            console.error('User not authenticated')
            return
        }

        const userId = getUsername(keycloak)
        if (!userId) {
            console.error('No user ID found in token')
            return
        }

        const voteRequest = {
            userId,
            itemId: movie.imdbId,
            itemType: 'MOVIE',
            itemName: movie.title,
            userItemStatus: 'VOTED'
        }

        try {
            setLoading(true)
            await moviesApi.placeVote(keycloak.token, voteRequest)
            setVoted(true)
        } catch (error) {
            handleLogError(error)
        } finally {
            setLoading(false)
        }
    }

    const clickableContent = (
        <>
            <Image src={movie.poster || '/images/movie-poster.jpg'} wrapped ui={false} />
            <Card.Content textAlign="center">
                <Card.Header>{movie.title}</Card.Header>
            </Card.Content>
            <Card.Content textAlign="center" style={{ paddingTop: '0.5em', paddingBottom: '0.5em' }}>
                <Card.Description>imdbID: <strong>{movie.imdbId}</strong></Card.Description>
                <Card.Description>Author: <strong>{movie.director}</strong></Card.Description>
                <Card.Description>Year: <strong>{movie.year}</strong></Card.Description>
            </Card.Content>
        </>
    )

    return (
        <Card>
            <div style={{ cursor: 'pointer' }}>
                {link ? (
                    <Link
                        to={`/movies/${movie.imdbId}/${canVote && !voted}`}
                        style={{ color: 'inherit', textDecoration: 'none', display: 'block' }}
                        onClick={(e) => {
                            if (loading) {
                                e.preventDefault()
                            }
                        }}
                    >
                        {clickableContent}
                    </Link>
                ) : (
                    clickableContent
                )}
            </div>

            {keycloak.authenticated && (
                <Card.Content extra textAlign="center">
                    <Button
                        color={!canVote || voted ? 'red' : 'green'}
                        disabled={!canVote || voted || loading}
                        loading={loading}
                        onClick={handleVote}
                    >
                        <Icon name="star" />
                        {!canVote || voted ? 'Voted' : 'Vote'}
                    </Button>
                </Card.Content>
            )}
        </Card>
    )
}

export default MovieCard