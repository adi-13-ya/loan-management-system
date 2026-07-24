import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/axios';
import StatusBadge from '../../components/StatusBadge';
import LoadingSpinner from '../../components/LoadingSpinner';
import EmptyState from '../../components/EmptyState';
import { PlusCircle, Eye, FileText } from 'lucide-react';
import toast from 'react-hot-toast';

export default function CustomerDashboard() {
  const [loans, setLoans] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchLoans();
  }, []);

  const fetchLoans = async () => {
    try {
      const res = await api.get('/loans/my');
      setLoans(res.data);
    } catch (err) {
      toast.error('Failed to fetch applications');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount);
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">My Loan Applications</h1>
          <p className="text-gray-500 mt-1">Track and manage your loan applications</p>
        </div>
        <Link to="/apply" className="btn-primary flex items-center gap-2">
          <PlusCircle className="w-4 h-4" />
          Apply for Loan
        </Link>
      </div>

      {loans.length === 0 ? (
        <EmptyState
          title="No applications yet"
          description="Apply for your first loan to get started"
          icon={FileText}
        />
      ) : (
        <div className="grid gap-4">
          {loans.map(loan => (
            <div key={loan.id} className="card hover:shadow-md transition-shadow">
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    <h3 className="font-semibold text-gray-900">{loan.loanType}</h3>
                    <StatusBadge status={loan.status} />
                  </div>
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                    <div>
                      <span className="text-gray-500">Amount</span>
                      <p className="font-medium">{formatCurrency(loan.principalAmount)}</p>
                    </div>
                    <div>
                      <span className="text-gray-500">Interest Rate</span>
                      <p className="font-medium">{loan.annualInterestRate}% p.a.</p>
                    </div>
                    <div>
                      <span className="text-gray-500">Tenure</span>
                      <p className="font-medium">{loan.tenureMonths} months</p>
                    </div>
                    <div>
                      <span className="text-gray-500">Branch</span>
                      <p className="font-medium">{loan.branchName || 'Not assigned'}</p>
                    </div>
                  </div>
                </div>
                <div className="flex gap-2 ml-4">
                  {loan.status === 'DRAFT' && (
                    <button
                      onClick={async () => {
                        try {
                          await api.post(`/loans/${loan.id}/submit`);
                          toast.success('Application submitted!');
                          fetchLoans();
                        } catch (err) {
                          toast.error(err.response?.data?.message || 'Submit failed');
                        }
                      }}
                      className="btn-primary text-sm"
                    >
                      Submit
                    </button>
                  )}
                  <Link to={`/loans/${loan.id}`} className="btn-secondary text-sm flex items-center gap-1">
                    <Eye className="w-4 h-4" />
                    View
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
