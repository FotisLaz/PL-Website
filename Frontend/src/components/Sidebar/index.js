import './index.scss'
import { Link, NavLink } from "react-router-dom"
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faHome, faSearch, faTshirt, faBars, faClose, faUsers, faFlag, faChartLine } from '@fortawesome/free-solid-svg-icons'
import LogoPL from '../../assets/images/prem.PNG'
import LogoSubtitle from '../../assets/images/sub-logo.png'
import { useState } from 'react'

const Sidebar = () => {
    const [showNav, setShowNav] = useState(false)
    return(
        <div className='nav-bar'> 
            <Link className='logo' to='/'> 
                <img src={LogoPL} alt="logo" />
                <img className="sub-logo" src={LogoSubtitle} alt="PremierZone" />
            </Link>
            <nav className={showNav ? 'mobile-show' : ""}>
                <NavLink exact="true" activeclassname="active" to="/">
                    <FontAwesomeIcon icon={faHome} onClick={() => setShowNav(false)} />
                </NavLink>
                <NavLink exact="true" activeclassname="active" className="teams-link" to="/teams">
                    <FontAwesomeIcon icon={faUsers} onClick={() => setShowNav(false)} />
                </NavLink>
                <NavLink exact="true" activeclassname="active" className="nation-link" to="/nation">
                    <FontAwesomeIcon icon={faFlag} onClick={() => setShowNav(false)} />
                </NavLink>
                <NavLink exact="true" activeclassname="active" className="position-link" to="/position">
                    <FontAwesomeIcon icon={faTshirt} onClick={() => setShowNav(false)} />
                </NavLink>
                <NavLink exact="true" activeclassname="active" className="prediction-link" to="/match-prediction">
                    <FontAwesomeIcon icon={faChartLine} onClick={() => setShowNav(false)} />
                </NavLink>
                <NavLink exact="true" activeclassname="active" className="search-link" to="/search">
                    <FontAwesomeIcon icon={faSearch} onClick={() => setShowNav(false)} />
                </NavLink>
                
                <FontAwesomeIcon icon={faClose} size="3x" className="close-icon" onClick={() => setShowNav(false)} />
            </nav>
        </div>
    )
}

export default Sidebar;
