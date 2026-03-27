const LoadingSpinner = ({ message = 'Loading...' }) => {
  return (
    <div className="loading-container">
      <div className="spinner">
        <div className="spinner-ring"></div>
        <div className="spinner-om">ॐ</div>
      </div>
      <p className="loading-message">{message}</p>
    </div>
  )
}

export default LoadingSpinner
