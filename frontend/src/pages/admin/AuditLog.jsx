import { useState, useEffect } from 'react';
import api from '../../api/axios';
import LoadingSpinner from '../../components/LoadingSpinner';
import StatusBadge from '../../components/StatusBadge';
import { Search } from 'lucide-react';
import toast from 'react-hot-toast';

export default function AuditLog() {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    api.get('/admin/audit-log')
      .then(res => setLogs(res.data))
      .catch(() => toast.error('Failed to fetch audit log'))
      .finally(() => setLoading(false));
  }, []);

  const filtered = logs.filter(log =>
    !search ||
    log.actorName?.toLowerCase().includes(search.toLowerCase()) ||
    log.remarks?.toLowerCase().includes(search.toLowerCase()) ||
    String(log.loanApplicationId).includes(search)
  );

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Audit Log</h1>
        <p className="text-gray-500 mt-1">Complete history of all loan state changes</p>
      </div>

      <div className="relative mb-4 max-w-sm">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="input-field pl-9"
          placeholder="Search by actor, loan ID, or remarks..."
        />
      </div>

      <div className="card p-0 overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-gray-50 border-b">
              <th className="text-left px-4 py-3 font-medium text-gray-500">Loan</th>
              <th className="text-left px-4 py-3 font-medium text-gray-500">Actor</th>
              <th className="text-left px-4 py-3 font-medium text-gray-500">Role</th>
              <th className="text-left px-4 py-3 font-medium text-gray-500">From</th>
              <th className="text-left px-4 py-3 font-medium text-gray-500">To</th>
              <th className="text-left px-4 py-3 font-medium text-gray-500">Remarks</th>
              <th className="text-left px-4 py-3 font-medium text-gray-500">Timestamp</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map(log => (
              <tr key={log.id} className="border-b last:border-0 hover:bg-gray-50">
                <td className="px-4 py-3 font-mono text-xs">#{log.loanApplicationId}</td>
                <td className="px-4 py-3 font-medium">{log.actorName}</td>
                <td className="px-4 py-3 text-gray-500">{log.actorRole}</td>
                <td className="px-4 py-3"><StatusBadge status={log.fromStatus} /></td>
                <td className="px-4 py-3"><StatusBadge status={log.toStatus} /></td>
                <td className="px-4 py-3 text-gray-500 max-w-xs truncate">{log.remarks || '-'}</td>
                <td className="px-4 py-3 text-gray-500 text-xs">{new Date(log.timestamp).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
