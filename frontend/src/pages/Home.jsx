import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getOccasions } from '../api/occasionApi'
import OccasionCard from '../components/OccasionCard'
import LoadingSpinner from '../components/LoadingSpinner'

const Home = () => {
  const [occasions, setOccasions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const navigate = useNavigate()

  useEffect(() => {
    fetchOccasions()
  }, [])

  const fetchOccasions = async () => {
    try {
      setLoading(true)
      const response = await getOccasions()
      if (response.success) {
        setOccasions(response.data)
      }
    } catch (err) {
      setError('Failed to load occasions. Please try again.')
      console.error('Error fetching occasions:', err)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="home-page">
      {/* Hero Section */}
      <section className="hero-section">
        <div className="hero-content">
          <div className="hero-om">ॐ</div>
          <h1 className="hero-title">
            Welcome to <span className="brand-highlight">ShubhaSamagri</span>
          </h1>
          <p className="hero-subtitle">
            Authentic Pooja Essentials & Ritual Kits for Sacred Hindu Occasions
          </p>
          <p className="hero-description">
            From Marriage to Gruha Pravesh, Satyanarayana Vratham to Upanayanam —
            we bring divine blessings to your doorstep with carefully curated ritual kits.
          </p>
          <div className="hero-buttons">
            <button
              className="btn btn-primary btn-lg"
              onClick={() => document.getElementById('occasions-section').scrollIntoView({ behavior: 'smooth' })}
            >
              Explore Occasions
            </button>
            <button
              className="btn btn-outline btn-lg"
              onClick={() => navigate('/signup')}
            >
              Get Started
            </button>
          </div>
        </div>
        <div className="hero-decoration">
          <div className="diya-animation">
            <span className="diya">&#127774;</span>
            <span className="diya">&#127774;</span>
            <span className="diya">&#127774;</span>
          </div>
          <div className="flower-decoration">
            <span>&#127800;</span>
            <span>&#127801;</span>
            <span>&#127800;</span>
          </div>
        </div>
      </section>

      {/* Features Strip */}
      <section className="features-strip">
        <div className="features-container">
          <div className="feature-item">
            <span className="feature-icon">&#127775;</span>
            <span>100% Authentic Items</span>
          </div>
          <div className="feature-item">
            <span className="feature-icon">&#128666;</span>
            <span>Pan India Delivery</span>
          </div>
          <div className="feature-item">
            <span className="feature-icon">&#128219;</span>
            <span>Expert-Curated Kits</span>
          </div>
          <div className="feature-item">
            <span className="feature-icon">&#9997;</span>
            <span>Personalisation Available</span>
          </div>
          <div className="feature-item">
            <span className="feature-icon">&#128081;</span>
            <span>Blessed by Pandits</span>
          </div>
        </div>
      </section>

      {/* Occasions Section */}
      <section id="occasions-section" className="occasions-section">
        <div className="section-header">
          <h2 className="section-title">Sacred Occasions</h2>
          <p className="section-subtitle">
            Select your occasion and find the perfect pooja kit for every ritual
          </p>
        </div>

        {loading ? (
          <LoadingSpinner message="Loading sacred occasions..." />
        ) : error ? (
          <div className="error-state">
            <p>{error}</p>
            <button className="btn btn-primary" onClick={fetchOccasions}>Try Again</button>
          </div>
        ) : (
          <div className="occasions-grid">
            {occasions.map(occasion => (
              <OccasionCard key={occasion.id} occasion={occasion} />
            ))}
          </div>
        )}
      </section>

      {/* Why Choose Us Section */}
      <section className="why-us-section">
        <div className="section-header">
          <h2 className="section-title">Why ShubhaSamagri?</h2>
        </div>
        <div className="why-us-grid">
          <div className="why-us-card">
            <div className="why-us-icon">&#127757;</div>
            <h3>Sourced from Sacred Places</h3>
            <p>Our items are sourced from renowned temples and traditional markets across India — Varanasi, Tirupati, Madurai and more.</p>
          </div>
          <div className="why-us-card">
            <div className="why-us-icon">&#128218;</div>
            <h3>Curated by Experts</h3>
            <p>Each kit is carefully assembled by experienced pandits ensuring every ritual item is present and in the correct quantity.</p>
          </div>
          <div className="why-us-card">
            <div className="why-us-icon">&#128666;</div>
            <h3>Doorstep Delivery</h3>
            <p>Fresh flowers, quality incense, pure ghee — everything delivered fresh to your home before your auspicious occasion.</p>
          </div>
          <div className="why-us-card">
            <div className="why-us-icon">&#128140;</div>
            <h3>Occasion Specialists</h3>
            <p>Dedicated kits for every Hindu occasion — from intimate ceremonies to grand celebrations with hundreds of guests.</p>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="cta-section">
        <div className="cta-content">
          <h2>Ready for Your Sacred Occasion?</h2>
          <p>Create your account and order your pooja kit today!</p>
          <button
            className="btn btn-primary btn-lg"
            onClick={() => navigate('/signup')}
          >
            Get Started — It's Free
          </button>
        </div>
      </section>
    </div>
  )
}

export default Home
