import AdminAuditLogPanel from './AdminAuditLogPanel'
import OperationLogsPanel from './OperationLogsPanel'

export default function AdminLogsPage() {
  return (
    <section className="admin-page-grid">
      <OperationLogsPanel />
      <AdminAuditLogPanel />
    </section>
  )
}
