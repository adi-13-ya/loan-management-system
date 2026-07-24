import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './context/AuthContext';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Register from './pages/Register';
import CustomerDashboard from './pages/customer/CustomerDashboard';
import ApplyLoan from './pages/customer/ApplyLoan';
import OfficerDashboard from './pages/officer/OfficerDashboard';
import ManagerDashboard from './pages/manager/ManagerDashboard';
import BranchPortfolio from './pages/manager/BranchPortfolio';
import AdminDashboard from './pages/admin/AdminDashboard';
import BranchManagement from './pages/admin/BranchManagement';
import UserManagement from './pages/admin/UserManagement';
import AllLoans from './pages/admin/AllLoans';
import AuditLog from './pages/admin/AuditLog';
import LoanDetail from './pages/LoanDetail';

function DashboardRedirect() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" />;

  switch (user.role) {
    case 'CUSTOMER': return <CustomerDashboard />;
    case 'OFFICER': return <OfficerDashboard />;
    case 'MANAGER': return <ManagerDashboard />;
    case 'ADMIN': return <AdminDashboard />;
    default: return <Navigate to="/login" />;
  }
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Toaster position="top-right" toastOptions={{ duration: 3000 }} />
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          <Route element={<ProtectedRoute><Layout /></ProtectedRoute>}>
            <Route path="/dashboard" element={<DashboardRedirect />} />
            <Route path="/loans/:id" element={<LoanDetail />} />

            {/* Customer Routes */}
            <Route path="/apply" element={
              <ProtectedRoute roles={['CUSTOMER']}><ApplyLoan /></ProtectedRoute>
            } />

            {/* Manager Routes */}
            <Route path="/portfolio" element={
              <ProtectedRoute roles={['MANAGER']}><BranchPortfolio /></ProtectedRoute>
            } />

            {/* Admin Routes */}
            <Route path="/admin/branches" element={
              <ProtectedRoute roles={['ADMIN']}><BranchManagement /></ProtectedRoute>
            } />
            <Route path="/admin/users" element={
              <ProtectedRoute roles={['ADMIN']}><UserManagement /></ProtectedRoute>
            } />
            <Route path="/admin/loans" element={
              <ProtectedRoute roles={['ADMIN']}><AllLoans /></ProtectedRoute>
            } />
            <Route path="/admin/audit-log" element={
              <ProtectedRoute roles={['ADMIN']}><AuditLog /></ProtectedRoute>
            } />
          </Route>

          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
