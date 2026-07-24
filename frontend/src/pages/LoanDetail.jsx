import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import StatusBadge from '../components/StatusBadge';
import LoadingSpinner from '../components/LoadingSpinner';
import { ArrowLeft, Upload, CheckCircle, XCircle, ArrowRight, FileText } from 'lucide-react';
import toast from 'react-hot-toast';

export default function LoanDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [loan, setLoan] = useState(null);
  const [emiSchedule, setEmiSchedule] = useState([]);
  const [documents, setDocuments] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [remarks, setRemarks] = useState('');
  const [activeTab, setActiveTab] = useState('details');
  const [uploading, setUploading] = useState(false);
  const [uploadDocType, setUploadDocType] = useState('ID_PROOF');

  useEffect(() => {
    fetchAll();
  }, [id]);

  const fetchAll = async () => {
    try {
      const [loanRes, docsRes] = await Promise.all([
        api.get(`/loans/${id}`),
        api.get(`/loans/${id}/documents`),
      ]);
      setLoan(loanRes.data);
      setDocuments(docsRes.data);

      if (['DISBURSED', 'APPROVED'].includes(loanRes.data.status)) {
        const emiRes = await api.get(`/loans/${id}/emi-schedule`);
        setEmiSchedule(emiRes.data);
      }
    } catch (err) {
      toast.error('Failed to load loan details');
    } finally {
      setLoading(false);
    }
  };

  const handleAction = async (action) => {
    try {
      const payload = remarks ? { remarks } : {};
      await api.post(`/loans/${id}/${action}`, payload);
      toast.success(`Loan ${action}ed successfully!`);
      setRemarks('');
      fetchAll();
    } catch (err) {
      toast.error(err.response?.data?.message || `Failed to ${action}`);
    }
  };

  const handleUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setUploading(true);
    const formData = new FormData();
    formData.append('file', file);
    formData.append('docType', uploadDocType);
    try {
      await api.post(`/loans/${id}/documents`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      toast.success('Document uploaded!');
      fetchAll();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  const handleMarkPaid = async (emiId) => {
    try {
      await api.post(`/emi/${emiId}/mark-paid`);
      toast.success('EMI marked as paid');
      fetchAll();
    } catch (err) {
      toast.error('Failed to mark EMI as paid');
    }
  };

  const formatCurrency = (amount) =>
    new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 }).format(amount);

  if (loading) return <LoadingSpinner />;
  if (!loan) return <div>Loan not found</div>;

  const paidCount = emiSchedule.filter(e => e.isPaid).length;
  const totalCount = emiSchedule.length;
  const progressPct = totalCount > 0 ? (paidCount / totalCount) * 100 : 0;

  const tabs = ['details', 'documents'];
  if (loan.status === 'DISBURSED') tabs.push('emi-schedule');

  return (
    <div>
      <button onClick={() => navigate(-1)} className="flex items-center gap-1 text-gray-500 hover:text-gray-700 mb-4">
        <ArrowLeft className="w-4 h-4" /> Back
      </button>

      <div className="card mb-6">
        <div className="flex items-start justify-between mb-4">
          <div>
            <div className="flex items-center gap-3">
              <h1 className="text-xl font-bold text-gray-900">{loan.loanType}</h1>
              <StatusBadge status={loan.status} />
            </div>
            <p className="text-sm text-gray-500 mt-1">Application #{loan.id}</p>
          </div>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
          <div><span className="text-gray-500">Customer</span><p className="font-medium">{loan.customerName}</p></div>
          <div><span className="text-gray-500">Amount</span><p className="font-medium">{formatCurrency(loan.principalAmount)}</p></div>
          <div><span className="text-gray-500">Interest Rate</span><p className="font-medium">{loan.annualInterestRate}% p.a.</p></div>
          <div><span className="text-gray-500">Tenure</span><p className="font-medium">{loan.tenureMonths} months</p></div>
          <div><span className="text-gray-500">Branch</span><p className="font-medium">{loan.branchName || '-'}</p></div>
          <div><span className="text-gray-500">Purpose</span><p className="font-medium">{loan.purpose || '-'}</p></div>
          <div><span className="text-gray-500">Officer</span><p className="font-medium">{loan.currentOfficerName || '-'}</p></div>
          <div><span className="text-gray-500">Manager</span><p className="font-medium">{loan.currentManagerName || '-'}</p></div>
        </div>

        {/* Action Buttons */}
        {user?.role === 'CUSTOMER' && loan.status === 'DRAFT' && (
          <div className="mt-4 pt-4 border-t">
            <button onClick={() => handleAction('submit')} className="btn-primary">Submit Application</button>
          </div>
        )}

        {user?.role === 'OFFICER' && loan.status === 'SUBMITTED' && (
          <div className="mt-4 pt-4 border-t">
            <button onClick={() => handleAction('review')} className="btn-primary">Pick Up for Review</button>
          </div>
        )}

        {user?.role === 'OFFICER' && loan.status === 'UNDER_REVIEW' && (
          <div className="mt-4 pt-4 border-t space-y-3">
            <textarea
              value={remarks}
              onChange={(e) => setRemarks(e.target.value)}
              className="input-field"
              placeholder="Add remarks..."
              rows={2}
            />
            <div className="flex gap-2">
              <button onClick={() => handleAction('forward')} className="btn-primary flex items-center gap-1">
                <ArrowRight className="w-4 h-4" /> Forward to Manager
              </button>
              <button onClick={() => handleAction('reject')} className="btn-danger flex items-center gap-1">
                <XCircle className="w-4 h-4" /> Reject
              </button>
            </div>
          </div>
        )}

        {user?.role === 'MANAGER' && loan.status === 'FORWARDED_TO_MANAGER' && (
          <div className="mt-4 pt-4 border-t space-y-3">
            <textarea
              value={remarks}
              onChange={(e) => setRemarks(e.target.value)}
              className="input-field"
              placeholder="Add remarks..."
              rows={2}
            />
            <div className="flex gap-2">
              <button onClick={() => handleAction('approve')} className="btn-success flex items-center gap-1">
                <CheckCircle className="w-4 h-4" /> Approve & Disburse
              </button>
              <button onClick={() => handleAction('reject')} className="btn-danger flex items-center gap-1">
                <XCircle className="w-4 h-4" /> Reject
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Tabs */}
      <div className="flex gap-1 mb-4 bg-gray-100 p-1 rounded-lg w-fit">
        {tabs.map(tab => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`px-4 py-2 text-sm font-medium rounded-md transition-colors ${
              activeTab === tab ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            {tab === 'emi-schedule' ? 'EMI Schedule' : tab.charAt(0).toUpperCase() + tab.slice(1)}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      {activeTab === 'details' && (
        <div className="card">
          <h3 className="font-semibold mb-4">Application Timeline</h3>
          <div className="text-sm text-gray-500">
            <p>Created: {new Date(loan.createdAt).toLocaleString()}</p>
            <p>Last Updated: {new Date(loan.updatedAt).toLocaleString()}</p>
          </div>
        </div>
      )}

      {activeTab === 'documents' && (
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-semibold">Documents</h3>
            {user?.role === 'CUSTOMER' && !['REJECTED', 'DISBURSED'].includes(loan.status) && (
              <div className="flex items-center gap-2">
                <select value={uploadDocType} onChange={(e) => setUploadDocType(e.target.value)} className="input-field text-sm w-auto">
                  <option value="ID_PROOF">ID Proof</option>
                  <option value="INCOME_PROOF">Income Proof</option>
                  <option value="OTHER">Other</option>
                </select>
                <label className="btn-secondary text-sm cursor-pointer flex items-center gap-1">
                  <Upload className="w-4 h-4" />
                  {uploading ? 'Uploading...' : 'Upload'}
                  <input type="file" className="hidden" onChange={handleUpload} accept=".pdf,.jpg,.jpeg,.png" />
                </label>
              </div>
            )}
          </div>
          {documents.length === 0 ? (
            <p className="text-gray-500 text-sm">No documents uploaded yet</p>
          ) : (
            <div className="space-y-2">
              {documents.map(doc => (
                <div key={doc.id} className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                  <FileText className="w-5 h-5 text-gray-400" />
                  <div className="flex-1">
                    <p className="text-sm font-medium">{doc.fileName}</p>
                    <p className="text-xs text-gray-500">{doc.docType} - {new Date(doc.uploadedAt).toLocaleDateString()}</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {activeTab === 'emi-schedule' && (
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-semibold">EMI Schedule</h3>
            <span className="text-sm text-gray-500">{paidCount}/{totalCount} paid</span>
          </div>

          {totalCount > 0 && (
            <div className="mb-4">
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div className="bg-green-500 h-2 rounded-full transition-all" style={{ width: `${progressPct}%` }} />
              </div>
            </div>
          )}

          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b text-left">
                  <th className="pb-2 font-medium text-gray-500">#</th>
                  <th className="pb-2 font-medium text-gray-500">Due Date</th>
                  <th className="pb-2 font-medium text-gray-500">EMI</th>
                  <th className="pb-2 font-medium text-gray-500">Principal</th>
                  <th className="pb-2 font-medium text-gray-500">Interest</th>
                  <th className="pb-2 font-medium text-gray-500">Balance</th>
                  <th className="pb-2 font-medium text-gray-500">Status</th>
                </tr>
              </thead>
              <tbody>
                {emiSchedule.map(emi => (
                  <tr key={emi.id} className="border-b last:border-0">
                    <td className="py-2">{emi.installmentNumber}</td>
                    <td className="py-2">{new Date(emi.dueDate).toLocaleDateString()}</td>
                    <td className="py-2">{formatCurrency(emi.emiAmount)}</td>
                    <td className="py-2">{formatCurrency(emi.principalComponent)}</td>
                    <td className="py-2">{formatCurrency(emi.interestComponent)}</td>
                    <td className="py-2">{formatCurrency(emi.outstandingBalance)}</td>
                    <td className="py-2">
                      {emi.isPaid ? (
                        <span className="text-green-600 flex items-center gap-1"><CheckCircle className="w-4 h-4" /> Paid</span>
                      ) : user?.role === 'CUSTOMER' ? (
                        <button onClick={() => handleMarkPaid(emi.id)} className="text-primary-600 hover:text-primary-700 text-xs font-medium">
                          Mark Paid
                        </button>
                      ) : (
                        <span className="text-gray-400">Pending</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
