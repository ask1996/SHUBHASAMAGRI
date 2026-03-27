import { Link } from 'react-router-dom'

const Footer = () => {
  return (
    <footer className="footer">
      <div className="footer-container">
        <div className="footer-brand">
          <div className="footer-logo">
            <span className="om-symbol">ॐ</span>
            <span className="brand-name">ShubhaSamagri</span>
          </div>
          <p className="footer-tagline">
            Bringing divine blessings to your doorstep.<br />
            Authentic pooja essentials for every sacred occasion.
          </p>
        </div>

        <div className="footer-links">
          <h4>Quick Links</h4>
          <ul>
            <li><Link to="/">Home</Link></li>
            <li><Link to="/cart">My Cart</Link></li>
            <li><Link to="/orders">My Orders</Link></li>
            <li><Link to="/login">Login</Link></li>
            <li><Link to="/signup">Sign Up</Link></li>
          </ul>
        </div>

        <div className="footer-occasions">
          <h4>Occasions</h4>
          <ul>
            <li>Marriage Ceremony</li>
            <li>Gruha Pravesh</li>
            <li>Satyanarayana Vratham</li>
            <li>Naming Ceremony</li>
            <li>Upanayanam</li>
          </ul>
        </div>

        <div className="footer-contact">
          <h4>Connect With Us</h4>
          <p>support@shubhasamagri.com</p>
          <p>+91 9999999999</p>
          <p>Mon-Sat: 9AM - 6PM IST</p>
          <div className="footer-values">
            <span className="badge">100% Authentic</span>
            <span className="badge">Same Day Dispatch</span>
          </div>
        </div>
      </div>

      <div className="footer-bottom">
        <p>&copy; 2024 ShubhaSamagri. All rights reserved. | Made with devotion</p>
        <p className="footer-disclaimer">
          ॐ सर्वे भवन्तु सुखिनः | May all beings be happy
        </p>
      </div>
    </footer>
  )
}

export default Footer
