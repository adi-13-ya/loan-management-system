import { useState, useEffect } from 'react';
import api from '../../api/axios';
import LoadingSpinner from '../../components/LoadingSpinner';
import { Users, ToggleLeft, ToggleRight } from 'lucide-react';
import toast from 'react-hot-toast';

const roleBadge = {
  CUSTOMER: 'bg-blue-100 text-blue-700',
  OFFICER: 'bg-yellow-100 text-yellow-700',
  MANAGER: 'bg-purple-100 text-purple-700',
  ADMIN: 'bg-red-100 text-red-700',
};

export default function UserManagement() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => { fetchUsers(); }, []);

  const fetchUsers = async () => {
    try {
      const res = await api.get('/admin/users');
      setUsers(res.data);
    } catch (err) {
      toast.error('Failed to fetch users');
    } finally { setLoading(false); }
  };

  const toggleActive = async (userId) => {
    try {
      await api.put(`/admin/users/${userId}/toggle-active`);
      toast.success('User status updated');
      fetchUsers();
    } catch (err) {
      toast.error('Failed to update user');
    }
  };

  const filtered = filter === 'ALL' ? users : users.filter(u => u.role === filter);

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">User Management</h1>
          <p className="text-gray-500 mt-1">{users.length} total users</p>
        </div>
        <div className="flex gap-1 bg-gray-100 p-1 rounded-lg">
          {['ALL', 'CUSTOMER', 'OFFICER', 'MANAGER', 'ADMIN'].map(role => (
            <button
              key={role}
              onClick={() => setFilter(role)}
              className={`px-3 py-1.5 text-xs font-medium rounded-md transition-colors ${
                filter === role ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              {role === 'ALL' ? 'All' : role.charAt(0) + role.slice(1).toLowerCase()}
            </button>
          ))}
        </div>
      </div>

      <div className="card p-0 overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-gray-50 border-b">
              <th className="text-left px-4 py-3 font-medium text-gray-500">Name</th>
              <th className="text-left px-4 py-3 font-medium text-gray-500">Email</th>
              <th className="text-left px-4 py-3 font-medium text-gray-500">Role</th>
              <th className="text-left px-4 py-3 font-medium text-gray-500">Branch</th>
              <th className="text-left px-4 py-3 font-medium text-gray-500">Status</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody>
            {filtered.map(u => (
              <tr key={u.id} className="border-b last:border-0 hover:bg-gray-50">
                <td className="px-4 py-3 font-medium">{u.name}</td>
                <td className="px-4 py-3 text-gray-500">{u.email}</td>
                <td className="px-4 py-3">
                  <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${roleBadge[u.role]}`}>
                    {u.role}
                  </span>
                </td>
                <td className="px-4 py-3 text-gray-500">{u.branchName || '-'}</td>
                <td className="px-4 py-3">
                  <span className={`text-xs font-medium ${u.active ? 'text-green-600' : 'text-red-600'}`}>
                    {u.active ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <button
                    onClick={() => toggleActive(u.id)}
                    className="text-gray-400 hover:text-gray-600"
                    title={u.active ? 'Deactivate' : 'Activate'}
                  >
                    {u.active ? <ToggleRight className="w-5 h-5 text-green-500" /> : <ToggleLeft className="w-5 h-5" />}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
