const statusConfig = {
  DRAFT: { color: 'bg-gray-100 text-gray-700', label: 'Draft' },
  SUBMITTED: { color: 'bg-blue-100 text-blue-700', label: 'Submitted' },
  UNDER_REVIEW: { color: 'bg-yellow-100 text-yellow-700', label: 'Under Review' },
  FORWARDED_TO_MANAGER: { color: 'bg-purple-100 text-purple-700', label: 'Forwarded' },
  APPROVED: { color: 'bg-green-100 text-green-700', label: 'Approved' },
  REJECTED: { color: 'bg-red-100 text-red-700', label: 'Rejected' },
  DISBURSED: { color: 'bg-emerald-100 text-emerald-700', label: 'Disbursed' },
};

export default function StatusBadge({ status }) {
  const config = statusConfig[status] || { color: 'bg-gray-100 text-gray-700', label: status };
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${config.color}`}>
      {config.label}
    </span>
  );
}
