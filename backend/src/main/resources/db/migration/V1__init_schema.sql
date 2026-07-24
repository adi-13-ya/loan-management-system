-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('CUSTOMER', 'OFFICER', 'MANAGER', 'ADMIN')),
    branch_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Branches table
CREATE TABLE branches (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    city VARCHAR(255) NOT NULL,
    manager_id BIGINT REFERENCES users(id)
);

-- Add FK from users to branches (circular ref)
ALTER TABLE users ADD CONSTRAINT fk_users_branch FOREIGN KEY (branch_id) REFERENCES branches(id);

-- Loan types (reference data)
CREATE TABLE loan_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    min_interest_rate NUMERIC(5,2) NOT NULL,
    max_interest_rate NUMERIC(5,2) NOT NULL,
    max_tenure_months INTEGER NOT NULL
);

-- Loan applications
CREATE TABLE loan_applications (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES users(id),
    branch_id BIGINT REFERENCES branches(id),
    loan_type VARCHAR(100) NOT NULL,
    principal_amount NUMERIC(15,2) NOT NULL,
    annual_interest_rate NUMERIC(5,2) NOT NULL,
    tenure_months INTEGER NOT NULL,
    purpose TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT','SUBMITTED','UNDER_REVIEW','FORWARDED_TO_MANAGER','APPROVED','REJECTED','DISBURSED')),
    current_officer_id BIGINT REFERENCES users(id),
    current_manager_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Documents
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    loan_application_id BIGINT NOT NULL REFERENCES loan_applications(id),
    doc_type VARCHAR(30) NOT NULL CHECK (doc_type IN ('ID_PROOF','INCOME_PROOF','OTHER')),
    file_name VARCHAR(500) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Approval audit log
CREATE TABLE approval_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    loan_application_id BIGINT NOT NULL REFERENCES loan_applications(id),
    actor_id BIGINT NOT NULL REFERENCES users(id),
    from_status VARCHAR(30) NOT NULL,
    to_status VARCHAR(30) NOT NULL,
    remarks TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT NOW()
);

-- EMI schedule
CREATE TABLE emi_schedules (
    id BIGSERIAL PRIMARY KEY,
    loan_application_id BIGINT NOT NULL REFERENCES loan_applications(id),
    installment_number INTEGER NOT NULL,
    due_date DATE NOT NULL,
    emi_amount NUMERIC(15,2) NOT NULL,
    principal_component NUMERIC(15,2) NOT NULL,
    interest_component NUMERIC(15,2) NOT NULL,
    outstanding_balance NUMERIC(15,2) NOT NULL,
    is_paid BOOLEAN NOT NULL DEFAULT false
);

-- Indexes
CREATE INDEX idx_loan_apps_customer ON loan_applications(customer_id);
CREATE INDEX idx_loan_apps_branch ON loan_applications(branch_id);
CREATE INDEX idx_loan_apps_status ON loan_applications(status);
CREATE INDEX idx_emi_loan ON emi_schedules(loan_application_id);
CREATE INDEX idx_audit_loan ON approval_audit_logs(loan_application_id);
CREATE INDEX idx_docs_loan ON documents(loan_application_id);
