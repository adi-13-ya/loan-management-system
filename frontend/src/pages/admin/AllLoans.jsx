import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/axios';
import StatusBadge from '../../components/StatusBadge';
import LoadingSpinner from '../../components/LoadingSpinner';
import { Eye } from 'lucide-react';
import toast from 'react-hot-toast';

const STATUSES = ['', 'DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'FORWARDED_TO_MANAGER', 'APPROVED', 'REJECTED', 'DISBURSED'];

export default function AllLoans() {
  const [loans, setLoans] = useState([]);
  const [branches, setBranches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterBranch, setFilterBranch] = useState('');
  const [filterStatus, setFilterStatus] = useState('');

  useEffect(() => {
    Promise.all([
      api.get('/admin/loans'),
      api.get('/admin/branches'),
    ]).then(([loansRes, branchesRes]) => {
      setLoans(loansRes.data);
      setBranches(branchesRes.data);
    }).catch(() => toast.error('Failed to fetch data'))
      .finally(() => setLoading(false));
  }, []);

  const fetchFiltered = async () => {
    try {
      const params = new URLSearchParams();
      if (filterBranch) params.append('branchId', filterBranch);
      if (filterStatus) params.append('status', filterStatus);
      const res = await api.get(`/admin/loans?${params}`);
      setLoans(res.data);
    } catch (err) {
      toast.error('Failed to filter');
    }
  };

  useEffect(() => { fetchFiltered(); }, [filterBranch, filterStatus]);

  const formatCurrency = (amount) =>
    new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount);

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">All Loans</h1>
        <p className="text-gray-500 mt-1">Cross-branch loan overview</p>
      </div>

      <div className="flex gap-4 mb-4">
        <select value={filterBranch} onChange={(e) => setFilterBranch(e.target.value)} className="input-field w-auto">
          <option value="">All Branches</option>
          {branches.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
        </select>
        <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)} className="input-field w-auto">
          <option value="">All Statuses</option>
          {STATUSES.filter(Boolean).map(s => <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>)}
        </select>
      </div>

      <div className="card p-0 overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-gray-50 border-b">
              <th className="text-left px-4 py-3 font-medium text-gray-500">ID</th>
              <th className="text-left px-4 py-3 font-medium text-gray-500">Customer</th>
              <th className="text-left px-4 py-3 font-medium text-gray-500">Type</th>
              <th className="text-left px-4 py-3 font-medium text-gray-500">Amount</th>
              <th className="text-left px-4 py-3 font-medium text-gray-500">Branch</th>
              <th className="text-left px-4 py-3 font-medium text-gray-500">Status</th>
              <th className="text-left px-4 py-3 font-medium text-gray-500">Date</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody>
            {loans.map(loan => (
              <tr key={loan.id} className="border-b last:border-0 hover:bg-gray-50">
                <td className="px-4 py-3 font-mono text-xs">#{loan.id}</td>
                <td className="px-4 py-3 font-medium">{loan.customerName}</td>
                <td className="px-4 py-3">{loan.loanType}</td>
                <td className="px-4 py-3">{formatCurrency(loan.principalAmount)}</td>
                <td className="px-4 py-3 text-gray-500">{loan.branchName || '-'}</td>
                <td className="px-4 py-3"><StatusBadge status={loan.status} /></td>
                <td className="px-4 py-3 text-gray-500">{new Date(loan.createdAt).toLocaleDateString()}</td>
                <td className="px-4 py-3">
                  <Link to={`/loans/${loan.id}`} className="text-primary-600 hover:text-primary-700">
                    <Eye className="w-4 h-4" />
                  </Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
