import { useState, useEffect } from 'react';
import api from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import LoadingSpinner from '../../components/LoadingSpinner';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import { IndianRupee, FileCheck, FileX, Clock } from 'lucide-react';
import toast from 'react-hot-toast';

const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444'];

export default function BranchPortfolio() {
  const { user } = useAuth();
  const [portfolio, setPortfolio] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (user?.branchId) {
      fetchPortfolio();
    }
  }, [user]);

  const fetchPortfolio = async () => {
    try {
      const res = await api.get(`/admin/branch-portfolio/${user.branchId}`);
      setPortfolio(res.data);
    } catch (err) {
      toast.error('Failed to fetch portfolio');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) =>
    new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount || 0);

  if (loading) return <LoadingSpinner />;

  const pieData = [
    { name: 'Active', value: Number(portfolio?.activeLoans || 0) },
    { name: 'Approved', value: Number(portfolio?.approvedLoans || 0) },
    { name: 'Pending', value: Number(portfolio?.pendingLoans || 0) },
    { name: 'Rejected', value: Number(portfolio?.rejectedLoans || 0) },
  ].filter(d => d.value > 0);

  const stats = [
    { label: 'Total Disbursed', value: formatCurrency(portfolio?.totalDisbursed), icon: IndianRupee, color: 'bg-green-100 text-green-700' },
    { label: 'Active Loans', value: portfolio?.activeLoans || 0, icon: FileCheck, color: 'bg-blue-100 text-blue-700' },
    { label: 'Pending', value: portfolio?.pendingLoans || 0, icon: Clock, color: 'bg-yellow-100 text-yellow-700' },
    { label: 'Rejected', value: portfolio?.rejectedLoans || 0, icon: FileX, color: 'bg-red-100 text-red-700' },
  ];

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Branch Portfolio</h1>
        <p className="text-gray-500 mt-1">{user?.branchName} - Overview</p>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        {stats.map(stat => {
          const Icon = stat.icon;
          return (
            <div key={stat.label} className="card">
              <div className={`w-10 h-10 rounded-lg ${stat.color} flex items-center justify-center mb-3`}>
                <Icon className="w-5 h-5" />
              </div>
              <p className="text-2xl font-bold">{stat.value}</p>
              <p className="text-sm text-gray-500">{stat.label}</p>
            </div>
          );
        })}
      </div>

      {pieData.length > 0 && (
        <div className="card">
          <h3 className="font-semibold mb-4">Loan Distribution</h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={pieData} cx="50%" cy="50%" outerRadius={80} dataKey="value" label={({ name, value }) => `${name}: ${value}`}>
                  {pieData.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}
    </div>
  );
}
