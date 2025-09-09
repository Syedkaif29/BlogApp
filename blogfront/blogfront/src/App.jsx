import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { ToastProvider } from './contexts/ToastContext';
import Layout from './components/Layout';
import PrivateRoute from './components/PrivateRoute';

// Import pages
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import CreateBlogPage from './pages/CreateBlogPage';
import BlogDetailPage from './pages/BlogDetailPage';
import EditBlogPage from './pages/EditBlogPage';

function App() {
  return (
    <AuthProvider>
      <ToastProvider>
        <Router>
        <Routes>
          <Route path="/" element={<Layout />}>
            {/* Public routes */}
            <Route index element={<HomePage />} />
            <Route path="login" element={<LoginPage />} />
            <Route path="register" element={<RegisterPage />} />
            <Route path="blog/:id" element={<BlogDetailPage />} />
            
            {/* Protected routes */}
            <Route path="create" element={
              <PrivateRoute>
                <CreateBlogPage />
              </PrivateRoute>
            } />
            <Route path="edit/:id" element={
              <PrivateRoute>
                <EditBlogPage />
              </PrivateRoute>
            } />
          </Route>
        </Routes>
        </Router>
      </ToastProvider>
    </AuthProvider>
  );
}

export default App;
