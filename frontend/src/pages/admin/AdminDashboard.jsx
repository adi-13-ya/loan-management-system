import { useState, useEffect } from 'react';
import api from '../../api/axios';
import LoadingSpinner from '../../components/LoadingSpinner';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { Building2, Users, FileText, IndianRupee } from 'lucide-react';
import toast from 'react-hot-toast';

export default function AdminDashboard() {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAnalytics();
  }, []);

  const fetchAnalytics = async () => {
    try {
      const res = await api.get('/admin/analytics');
      setAnalytics(res.data);
    } catch (err) {
      toast.error('Failed to fetch analytics');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) =>
    new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount || 0);

  if (loading) return <LoadingSpinner />;

  const stats = [
    { label: 'Total Branches', value: analytics?.totalBranches || 0, icon: Building2, color: 'bg-blue-100 text-blue-700' },
    { label: 'Total Users', value: analytics?.totalUsers || 0, icon: Users, color: 'bg-green-100 text-green-700' },
    { label: 'Total Loans', value: analytics?.totalLoans || 0, icon: FileText, color: 'bg-purple-100 text-purple-700' },
  ];

  const chartData = (analytics?.branchStats || []).map(s => ({
    name: s.branchName?.replace('IDFC ', '') || 'Unknown',
    disbursed: Number(s.totalDisbursed || 0),
    active: Number(s.activeLoans || 0),
    pending: Number(s.pendingLoans || 0),
    rejected: Number(s.rejectedLoans || 0),
  }));

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Admin Dashboard</h1>
        <p className="text-gray-500 mt-1">System-wide overview</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
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

      {chartData.length > 0 && (
        <div className="grid md:grid-cols-2 gap-6">
          <div className="card">
            <h3 className="font-semibold mb-4">Total Disbursed by Branch</h3>
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" />
                  <YAxis tickFormatter={(v) => `${(v / 100000).toFixed(0)}L`} />
                  <Tooltip formatter={(v) => formatCurrency(v)} />
                  <Bar dataKey="disbursed" fill="#3b82f6" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="card">
            <h3 className="font-semibold mb-4">Loan Status by Branch</h3>
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" />
                  <YAxis />
                  <Tooltip />
                  <Bar dataKey="active" fill="#10b981" name="Active" stackId="a" />
                  <Bar dataKey="pending" fill="#f59e0b" name="Pending" stackId="a" />
                  <Bar dataKey="rejected" fill="#ef4444" name="Rejected" stackId="a" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
