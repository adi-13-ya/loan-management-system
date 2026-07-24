import { useState, useEffect } from 'react';
import api from '../../api/axios';
import LoadingSpinner from '../../components/LoadingSpinner';
import { Building2, Plus } from 'lucide-react';
import toast from 'react-hot-toast';

export default function BranchManagement() {
  const [branches, setBranches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ name: '', code: '', city: '', managerId: '' });

  useEffect(() => { fetchBranches(); }, []);

  const fetchBranches = async () => {
    try {
      const res = await api.get('/admin/branches');
      setBranches(res.data);
    } catch (err) {
      toast.error('Failed to fetch branches');
    } finally { setLoading(false); }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await api.post('/admin/branches', {
        ...form,
        managerId: form.managerId ? parseInt(form.managerId) : null,
      });
      toast.success('Branch created!');
      setShowForm(false);
      setForm({ name: '', code: '', city: '', managerId: '' });
      fetchBranches();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create branch');
    }
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Branch Management</h1>
          <p className="text-gray-500 mt-1">Manage all bank branches</p>
        </div>
        <button onClick={() => setShowForm(!showForm)} className="btn-primary flex items-center gap-2">
          <Plus className="w-4 h-4" /> Add Branch
        </button>
      </div>

      {showForm && (
        <div className="card mb-6">
          <h3 className="font-semibold mb-4">Create New Branch</h3>
          <form onSubmit={handleCreate} className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
              <input value={form.name} onChange={(e) => setForm(p => ({ ...p, name: e.target.value }))} className="input-field" required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Code</label>
              <input value={form.code} onChange={(e) => setForm(p => ({ ...p, code: e.target.value }))} className="input-field" required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">City</label>
              <input value={form.city} onChange={(e) => setForm(p => ({ ...p, city: e.target.value }))} className="input-field" required />
            </div>
            <div className="flex items-end">
              <button type="submit" className="btn-primary">Create</button>
            </div>
          </form>
        </div>
      )}

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {branches.map(branch => (
          <div key={branch.id} className="card">
            <div className="flex items-start gap-3">
              <div className="w-10 h-10 rounded-lg bg-primary-100 flex items-center justify-center">
                <Building2 className="w-5 h-5 text-primary-600" />
              </div>
              <div>
                <h3 className="font-semibold text-gray-900">{branch.name}</h3>
                <p className="text-sm text-gray-500">{branch.city}</p>
                <p className="text-xs text-gray-400 mt-1">Code: {branch.code}</p>
                {branch.managerName && (
                  <p className="text-xs text-primary-600 mt-1">Manager: {branch.managerName}</p>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
