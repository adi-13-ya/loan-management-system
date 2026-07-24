import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import toast from 'react-hot-toast';

const loanTypes = [
  { name: 'Home Loan', minRate: 8.5, maxRate: 9.5, maxTenure: 360 },
  { name: 'Personal Loan', minRate: 11.0, maxRate: 14.0, maxTenure: 60 },
  { name: 'Auto Loan', minRate: 9.0, maxRate: 11.0, maxTenure: 84 },
  { name: 'Education Loan', minRate: 9.0, maxRate: 10.0, maxTenure: 120 },
];

export default function ApplyLoan() {
  const [form, setForm] = useState({
    loanType: 'Home Loan',
    principalAmount: '',
    annualInterestRate: '8.75',
    tenureMonths: 60,
    purpose: '',
    branchId: '',
  });
  const [branches, setBranches] = useState([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    api.get('/branches').then(res => setBranches(res.data)).catch(() => {});
  }, []);

  const selectedType = loanTypes.find(t => t.name === form.loanType);

  const handleTypeChange = (type) => {
    const lt = loanTypes.find(t => t.name === type);
    setForm(prev => ({
      ...prev,
      loanType: type,
      annualInterestRate: ((lt.minRate + lt.maxRate) / 2).toFixed(2),
      tenureMonths: Math.min(prev.tenureMonths, lt.maxTenure),
    }));
  };

  const calculateEmi = () => {
    const P = parseFloat(form.principalAmount);
    const annualRate = parseFloat(form.annualInterestRate);
    const N = form.tenureMonths;
    if (!P || !annualRate || !N) return 0;
    const R = annualRate / 12 / 100;
    if (R === 0) return P / N;
    const emi = (P * R * Math.pow(1 + R, N)) / (Math.pow(1 + R, N) - 1);
    return emi;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const payload = {
        ...form,
        principalAmount: parseFloat(form.principalAmount),
        annualInterestRate: parseFloat(form.annualInterestRate),
        branchId: form.branchId ? parseInt(form.branchId) : null,
      };
      const res = await api.post('/loans', payload);
      toast.success('Loan application created!');
      navigate(`/loans/${res.data.id}`);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create application');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount);
  };

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Apply for a Loan</h1>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="card">
          <h2 className="font-semibold text-gray-900 mb-4">Loan Details</h2>

          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Loan Type</label>
              <select
                value={form.loanType}
                onChange={(e) => handleTypeChange(e.target.value)}
                className="input-field"
              >
                {loanTypes.map(t => (
                  <option key={t.name} value={t.name}>{t.name}</option>
                ))}
              </select>
              {selectedType && (
                <p className="text-xs text-gray-500 mt-1">
                  Rate: {selectedType.minRate}% - {selectedType.maxRate}% | Max tenure: {selectedType.maxTenure} months
                </p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Loan Amount (INR)</label>
              <input
                type="number"
                value={form.principalAmount}
                onChange={(e) => setForm(prev => ({ ...prev, principalAmount: e.target.value }))}
                className="input-field"
                placeholder="e.g. 1000000"
                min="10000"
                required
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Interest Rate (% p.a.)</label>
                <input
                  type="number"
                  value={form.annualInterestRate}
                  onChange={(e) => setForm(prev => ({ ...prev, annualInterestRate: e.target.value }))}
                  className="input-field"
                  step="0.01"
                  min="0"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Tenure (months)</label>
                <input
                  type="range"
                  value={form.tenureMonths}
                  onChange={(e) => setForm(prev => ({ ...prev, tenureMonths: parseInt(e.target.value) }))}
                  min="1"
                  max={selectedType?.maxTenure || 360}
                  className="w-full mt-2"
                />
                <p className="text-sm text-center font-medium text-primary-600">{form.tenureMonths} months ({(form.tenureMonths / 12).toFixed(1)} years)</p>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Branch</label>
              <select
                value={form.branchId}
                onChange={(e) => setForm(prev => ({ ...prev, branchId: e.target.value }))}
                className="input-field"
                required
              >
                <option value="">Select a branch</option>
                {branches.map(b => (
                  <option key={b.id} value={b.id}>{b.name} - {b.city}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Purpose</label>
              <textarea
                value={form.purpose}
                onChange={(e) => setForm(prev => ({ ...prev, purpose: e.target.value }))}
                className="input-field"
                rows={3}
                placeholder="Describe the purpose of the loan..."
              />
            </div>
          </div>
        </div>

        {form.principalAmount && (
          <div className="card bg-primary-50 border-primary-200">
            <h3 className="font-semibold text-primary-900 mb-2">Estimated EMI</h3>
            <p className="text-3xl font-bold text-primary-700">{formatCurrency(calculateEmi())}</p>
            <p className="text-sm text-primary-600 mt-1">per month (approximate)</p>
          </div>
        )}

        <div className="flex gap-3">
          <button type="submit" disabled={loading} className="btn-primary flex-1">
            {loading ? 'Creating...' : 'Create Application'}
          </button>
          <button type="button" onClick={() => navigate('/dashboard')} className="btn-secondary">
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
