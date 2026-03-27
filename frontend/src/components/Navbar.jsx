import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'
import toast from 'react-hot-toast'

const Navbar = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const { user, isAuthenticated, logout } = useAuth()
  const { cartCount } = useCart()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    toast.success('Logged out successfully')
    navigate('/')
    setMobileMenuOpen(false)
  }

  const closeMobileMenu = () => setMobileMenuOpen(false)

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-brand" onClick={closeMobileMenu}>
          <span className="om-symbol">ॐ</span>
          <span className="brand-name">ShubhaSamagri</span>
        </Link>

        <button
          className="mobile-menu-btn"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          aria-label="Toggle menu"
        >
          <span className={`hamburger ${mobileMenuOpen ? 'open' : ''}`}>
            <span></span>
            <span></span>
            <span></span>
          </span>
        </button>

        <div className={`navbar-links ${mobileMenuOpen ? 'mobile-open' : ''}`}>
          <Link to="/" className="nav-link" onClick={closeMobileMenu}>
            Home
          </Link>
          <Link to="/" className="nav-link" onClick={closeMobileMenu}>
            Occasions
          </Link>

          {isAuthenticated && (
            <>
              <Link to="/cart" className="nav-link cart-link" onClick={closeMobileMenu}>
                <span className="cart-icon">&#128722;</span>
                Cart
                {cartCount > 0 && (
                  <span className="cart-badge">{cartCount}</span>
                )}
              </Link>
              <Link to="/orders" className="nav-link" onClick={closeMobileMenu}>
                My Orders
              </Link>
            </>
          )}

          {isAuthenticated ? (
            <div className="nav-user-section">
              <span className="nav-user-name">Namaste, {user?.name?.split(' ')[0]}!</span>
              <button className="btn btn-secondary btn-sm" onClick={handleLogout}>
                Logout
              </button>
            </div>
          ) : (
            <div className="nav-auth-section">
              <Link to="/login" className="btn btn-secondary btn-sm" onClick={closeMobileMenu}>
                Login
              </Link>
              <Link to="/signup" className="btn btn-primary btn-sm" onClick={closeMobileMenu}>
                Sign Up
              </Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  )
}

export default Navbar
