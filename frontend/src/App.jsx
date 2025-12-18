import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { AuthProvider } from './contexts/AuthContext';
import { FlightSearchProvider } from './contexts/FlightSearchContext';
import { BookingProvider } from './contexts/BookingContext';
import ProtectedRoute from './components/ProtectedRoute';
import Header from './components/Header';
import Footer from './components/Footer';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import FlightSelection from './pages/FlightSelection';
import SeatSelection from './pages/SeatSelection';
import TravellerInfo from './pages/TravellerInfo';
import ExtraServices from './pages/ExtraServices';
import Payment from './pages/Payment';
import Confirmation from './pages/Confirmation';
import MyBookings from './pages/MyBookings';
import AirlineInformation from './pages/AirlineInformation';
// Admin Pages
import AdminLayout from './components/admin/AdminLayout';
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminBookings from './pages/admin/AdminBookings';
import AdminUsers from './pages/admin/AdminUsers';
import AdminFlights from './pages/admin/AdminFlights';
import './styles/App.css';

function App() {
  return (
    <AuthProvider>
      <FlightSearchProvider>
        <BookingProvider>
          <Router>
            <div className="app">
              <Header />
              <main className="main-content">
                <Routes>
                  {/* Public Routes */}
                  <Route path="/" element={<Home />} />
                  <Route path="/login" element={<Login />} />
                  <Route path="/register" element={<Register />} />
                  <Route path="/flights" element={<FlightSelection />} />
                  <Route path="/airline-info" element={<AirlineInformation />} />
                  
                  {/* Booking Flow */}
                  <Route path="/booking/flight-selection" element={<FlightSelection />} />
                  <Route path="/booking/seat-selection" element={<SeatSelection />} />
                  <Route path="/booking/traveller-info" element={<TravellerInfo />} />
                  <Route path="/booking/extra-services" element={<ExtraServices />} />
                  <Route path="/booking/payment" element={<Payment />} />
                  <Route path="/booking/confirmation/:bookingId" element={<Confirmation />} />
                  <Route path="/my-bookings" element={<MyBookings />} />
                  
                  {/* Admin Routes - Protected with ADMIN role */}
                  <Route 
                    path="/admin" 
                    element={
                      <ProtectedRoute allowedRoles={['ADMIN']}>
                        <AdminLayout />
                      </ProtectedRoute>
                    }
                  >
                    <Route index element={<Navigate to="/admin/dashboard" replace />} />
                    <Route path="dashboard" element={<AdminDashboard />} />
                    <Route path="bookings" element={<AdminBookings />} />
                    <Route path="users" element={<AdminUsers />} />
                    <Route path="flights" element={<AdminFlights />} />
                  </Route>
                  
                  {/* Catch-all route */}
                  <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
              </main>
              <Footer />
              
              {/* Toast Notifications */}
              <ToastContainer
                position="top-right"
                autoClose={3000}
                hideProgressBar={false}
                newestOnTop
                closeOnClick
                rtl={false}
                pauseOnFocusLoss
                draggable
                pauseOnHover
                theme="light"
              />
            </div>
          </Router>
        </BookingProvider>
      </FlightSearchProvider>
    </AuthProvider>
  );
}

export default App;
