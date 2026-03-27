import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getOccasionById } from '../api/occasionApi'
import { getKitsByOccasion } from '../api/kitApi'
import KitCard from '../components/KitCard'
import LoadingSpinner from '../components/LoadingSpinner'

const OccasionKits = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const [occasion, setOccasion] = useState(null)
  const [kits, setKits] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    fetchData()
  }, [id])

  const fetchData = async () => {
    try {
      setLoading(true)
      setError(null)
      const [occasionRes, kitsRes] = await Promise.all([
        getOccasionById(id),
        getKitsByOccasion(id)
      ])
      if (occasionRes.success) {
        setOccasion(occasionRes.data)
      }
      if (kitsRes.success) {
        setKits(kitsRes.data)
      }
    } catch (err) {
      setError('Failed to load data. Please try again.')
      console.error('Error fetching occasion kits:', err)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return <LoadingSpinner message="Loading kits for this occasion..." />
  }

  if (error) {
    return (
      <div className="page-container error-page">
        <p>{error}</p>
        <button className="btn btn-primary" onClick={fetchData}>Try Again</button>
        <button className="btn btn-secondary" onClick={() => navigate('/')}>Go Home</button>
      </div>
    )
  }

  return (
    <div className="occasion-kits-page page-container">
      <button className="btn btn-ghost back-btn" onClick={() => navigate('/')}>
        &#8592; Back to Occasions
      </button>

      {occasion && (
        <div className="occasion-header">
          <div className="occasion-header-content">
            <h1 className="page-title">{occasion.name}</h1>
            <p className="occasion-description">{occasion.description}</p>
            <div className="occasion-stats">
              <span className="stat-badge">
                {kits.length} {kits.length === 1 ? 'Kit' : 'Kits'} Available
              </span>
            </div>
          </div>
          {occasion.imageUrl && (
            <div className="occasion-header-image">
              <img
                src={occasion.imageUrl}
                alt={occasion.name}
                onError={(e) => { e.target.style.display = 'none' }}
              />
            </div>
          )}
        </div>
      )}

      {kits.length > 0 ? (
        <div>
          <h2 className="section-subtitle-left">Choose Your Perfect Kit</h2>
          <div className="kits-grid">
            {kits.map(kit => (
              <KitCard key={kit.id} kit={kit} />
            ))}
          </div>
        </div>
      ) : (
        <div className="empty-state">
          <div className="empty-icon">&#128219;</div>
          <h3>No Kits Available</h3>
          <p>We're currently curating kits for this occasion. Please check back soon!</p>
          <button className="btn btn-primary" onClick={() => navigate('/')}>
            Browse Other Occasions
          </button>
        </div>
      )}
    </div>
  )
}

export default OccasionKits
