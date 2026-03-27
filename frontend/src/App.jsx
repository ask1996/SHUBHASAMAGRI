import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import { AuthProvider } from './context/AuthContext'
import { CartProvider } from './context/CartContext'
import Navbar from './components/Navbar'
import FloatingChatbot from './components/FloatingChatbot'
import Footer from './components/Footer'
import ProtectedRoute from './components/ProtectedRoute'
import Home from './pages/Home'
import OccasionKits from './pages/OccasionKits'
import KitDetails from './pages/KitDetails'
import Cart from './pages/Cart'
import Login from './pages/Login'
import Signup from './pages/Signup'
import Orders from './pages/Orders'

function App() {
  return (
    <Router>
      <AuthProvider>
        <CartProvider>
          <div className="app-wrapper">
            <Navbar />
            <main className="main-content">
              <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/occasion/:id" element={<OccasionKits />} />
                <Route path="/kit/:id" element={<KitDetails />} />
                <Route path="/cart" element={
                  <ProtectedRoute>
                    <Cart />
                  </ProtectedRoute>
                } />
                <Route path="/login" element={<Login />} />
                <Route path="/signup" element={<Signup />} />
                <Route path="/orders" element={
                  <ProtectedRoute>
                    <Orders />
                  </ProtectedRoute>
                } />
              </Routes>
            </main>
            <Footer />
            <FloatingChatbot />
          </div>
          <Toaster
            position="top-right"
            toastOptions={{
              duration: 3000,
              style: {
                background: '#FFF8F0',
                color: '#2D1810',
                border: '1px solid #FF6B00',
                borderRadius: '8px',
              },
              success: {
                iconTheme: {
                  primary: '#FF6B00',
                  secondary: '#FFF8F0',
                },
              },
            }}
          />
        </CartProvider>
      </AuthProvider>
    </Router>
  )
}

export default App
