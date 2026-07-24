import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import StatusBadge from '../../components/StatusBadge';
import LoadingSpinner from '../../components/LoadingSpinner';
import EmptyState from '../../components/EmptyState';
import { ClipboardList, Eye } from 'lucide-react';
import toast from 'react-hot-toast';

export default function OfficerDashboard() {
  const { user } = useAuth();
  const [loans, setLoans] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (user?.branchId) {
      fetchQueue();
    }
  }, [user]);

  const fetchQueue = async () => {
    try {
      const res = await api.get(`/loans/branch/${user.branchId}/queue`);
      setLoans(res.data);
    } catch (err) {
      toast.error('Failed to fetch review queue');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) =>
    new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount);

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Review Queue</h1>
        <p className="text-gray-500 mt-1">Applications pending your review at {user?.branchName}</p>
      </div>

      {loans.length === 0 ? (
        <EmptyState title="Queue is empty" description="No applications pending review" icon={ClipboardList} />
      ) : (
        <div className="overflow-x-auto card p-0">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-gray-50 border-b">
                <th className="text-left px-4 py-3 font-medium text-gray-500">ID</th>
                <th className="text-left px-4 py-3 font-medium text-gray-500">Customer</th>
                <th className="text-left px-4 py-3 font-medium text-gray-500">Loan Type</th>
                <th className="text-left px-4 py-3 font-medium text-gray-500">Amount</th>
                <th className="text-left px-4 py-3 font-medium text-gray-500">Status</th>
                <th className="text-left px-4 py-3 font-medium text-gray-500">Submitted</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody>
              {loans.map(loan => (
                <tr key={loan.id} className="border-b last:border-0 hover:bg-gray-50">
                  <td className="px-4 py-3 font-mono text-xs">#{loan.id}</td>
                  <td className="px-4 py-3">
                    <div>
                      <p className="font-medium">{loan.customerName}</p>
                      <p className="text-xs text-gray-500">{loan.customerEmail}</p>
                    </div>
                  </td>
                  <td className="px-4 py-3">{loan.loanType}</td>
                  <td className="px-4 py-3 font-medium">{formatCurrency(loan.principalAmount)}</td>
                  <td className="px-4 py-3"><StatusBadge status={loan.status} /></td>
                  <td className="px-4 py-3 text-gray-500">{new Date(loan.createdAt).toLocaleDateString()}</td>
                  <td className="px-4 py-3">
                    <Link to={`/loans/${loan.id}`} className="btn-secondary text-xs flex items-center gap-1 w-fit">
                      <Eye className="w-3 h-3" /> Review
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
