import React from 'react'
import { useKeycloak } from '@react-keycloak/web'
import { NavLink } from 'react-router-dom'
import { Container, Dropdown, Menu } from 'semantic-ui-react'
import { getUsername, isAdmin } from '../misc/Helpers'
import { useNavigate } from 'react-router-dom'

function Navbar() {
  const { keycloak } = useKeycloak()
  const navigate = useNavigate()

  const handleLogInOut = () => {
    if (keycloak.authenticated) {
      navigate('/')
      keycloak.logout()
    } else {
      keycloak.login()
    }
  }

  const username = () => {
    return keycloak.authenticated && getUsername(keycloak)
  }

  const getLogInOutText = () => {
    return keycloak.authenticated ? "Logout" : "Login"
  }

  const getAdminMenuStyle = () => {
    return keycloak.authenticated && isAdmin(keycloak) ? { "display": "block" } : { "display": "none" }
  }

  const getRegularUserMenuStyle = () => {
    return keycloak.authenticated && !isAdmin(keycloak) ? { "display": "block" } : { "display": "none" }
  }

  return (
    <Menu stackable>
      <Container>
        <Menu.Item header>Movies UI</Menu.Item>
        <Menu.Item as={NavLink} end to="/home">Home</Menu.Item>
        <Menu.Item as={NavLink} end to="/wizard" style={getRegularUserMenuStyle()}>Movie Wizard</Menu.Item>
        <Dropdown item text='Admin' style={getAdminMenuStyle()}>
          <Dropdown.Menu>
            <Dropdown.Item as={NavLink} end to="/movies">Movies</Dropdown.Item>
            <Dropdown.Item as={NavLink} end to="/wizard">Movie Wizard</Dropdown.Item>
          </Dropdown.Menu>
        </Dropdown>
        <Menu.Menu position='right'>
          {keycloak.authenticated &&
            <Dropdown text={`Hi ${username()}`} pointing className='link item'>
              <Dropdown.Menu>
                <Dropdown.Item as={NavLink} to="/settings">Settings</Dropdown.Item>
              </Dropdown.Menu>
            </Dropdown>
          }
          <Menu.Item as={NavLink} end to="/login" onClick={handleLogInOut}>{getLogInOutText()}</Menu.Item>
        </Menu.Menu>
      </Container>
    </Menu >
  )
}

export default Navbar