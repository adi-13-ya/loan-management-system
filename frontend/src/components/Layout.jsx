import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard, FileText, Users, Building2, ClipboardList,
  LogOut, Menu, X, ChevronDown
} from 'lucide-react';
import { useState } from 'react';

const roleNavItems = {
  CUSTOMER: [
    { path: '/dashboard', label: 'My Applications', icon: FileText },
    { path: '/apply', label: 'Apply for Loan', icon: ClipboardList },
  ],
  OFFICER: [
    { path: '/dashboard', label: 'Review Queue', icon: ClipboardList },
  ],
  MANAGER: [
    { path: '/dashboard', label: 'Approval Queue', icon: ClipboardList },
    { path: '/portfolio', label: 'Branch Portfolio', icon: LayoutDashboard },
  ],
  ADMIN: [
    { path: '/dashboard', label: 'Overview', icon: LayoutDashboard },
    { path: '/admin/branches', label: 'Branches', icon: Building2 },
    { path: '/admin/users', label: 'Users', icon: Users },
    { path: '/admin/loans', label: 'All Loans', icon: FileText },
    { path: '/admin/audit-log', label: 'Audit Log', icon: ClipboardList },
  ],
};

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const navItems = roleNavItems[user?.role] || [];

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const roleLabel = {
    CUSTOMER: 'Customer',
    OFFICER: 'Branch Officer',
    MANAGER: 'Branch Manager',
    ADMIN: 'Administrator',
  };

  return (
    <div className="min-h-screen flex">
      {/* Sidebar */}
      <aside className={`fixed inset-y-0 left-0 z-50 w-64 bg-white border-r border-gray-200 transform transition-transform lg:translate-x-0 lg:static lg:inset-auto ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}`}>
        <div className="flex items-center justify-between h-16 px-6 border-b border-gray-200">
          <Link to="/dashboard" className="flex items-center gap-2">
            <Building2 className="w-7 h-7 text-primary-600" />
            <span className="font-bold text-lg text-primary-900">IDFC Bank</span>
          </Link>
          <button onClick={() => setSidebarOpen(false)} className="lg:hidden">
            <X className="w-5 h-5" />
          </button>
        </div>

        <nav className="p-4 space-y-1">
          {navItems.map(item => {
            const Icon = item.icon;
            const isActive = location.pathname === item.path;
            return (
              <Link
                key={item.path}
                to={item.path}
                onClick={() => setSidebarOpen(false)}
                className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-primary-50 text-primary-700'
                    : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
                }`}
              >
                <Icon className="w-5 h-5" />
                {item.label}
              </Link>
            );
          })}
        </nav>

        <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-gray-200">
          <div className="flex items-center gap-3 mb-3">
            <div className="w-9 h-9 rounded-full bg-primary-100 flex items-center justify-center">
              <span className="text-primary-700 font-semibold text-sm">
                {user?.name?.charAt(0)?.toUpperCase()}
              </span>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-gray-900 truncate">{user?.name}</p>
              <p className="text-xs text-gray-500">{roleLabel[user?.role]}</p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="flex items-center gap-2 w-full px-3 py-2 text-sm text-red-600 hover:bg-red-50 rounded-lg transition-colors"
          >
            <LogOut className="w-4 h-4" />
            Sign Out
          </button>
        </div>
      </aside>

      {/* Overlay */}
      {sidebarOpen && (
        <div className="fixed inset-0 bg-black/20 z-40 lg:hidden" onClick={() => setSidebarOpen(false)} />
      )}

      {/* Main content */}
      <div className="flex-1 flex flex-col min-w-0">
        <header className="h-16 bg-white border-b border-gray-200 flex items-center px-4 lg:px-8">
          <button onClick={() => setSidebarOpen(true)} className="lg:hidden mr-4">
            <Menu className="w-6 h-6" />
          </button>
          <div className="flex-1" />
          <div className="flex items-center gap-2 text-sm text-gray-500">
            {user?.branchName && (
              <span className="bg-primary-50 text-primary-700 px-3 py-1 rounded-full text-xs font-medium">
                {user.branchName}
              </span>
            )}
          </div>
        </header>
        <main className="flex-1 p-4 lg:p-8 overflow-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
