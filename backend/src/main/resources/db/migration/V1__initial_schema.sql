CREATE TABLE app_users (
  id BIGSERIAL PRIMARY KEY,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(254) NOT NULL UNIQUE,
  phone VARCHAR(10) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  role VARCHAR(10) NOT NULL CHECK (role IN ('USER','ADMIN')),
  status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','DISABLED')),
  lab_account BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE wallets (
  id BIGSERIAL PRIMARY KEY,
  reference UUID NOT NULL UNIQUE,
  owner_id BIGINT UNIQUE REFERENCES app_users(id),
  currency VARCHAR(3) NOT NULL DEFAULT 'INR' CHECK (currency='INR'),
  account_type VARCHAR(20) NOT NULL CHECK (account_type IN ('PERSONAL','SYSTEM_FUNDING','LAB')),
  balance_paise BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT personal_owner_required CHECK ((account_type IN ('PERSONAL','LAB') AND owner_id IS NOT NULL) OR (account_type='SYSTEM_FUNDING' AND owner_id IS NULL)),
  CONSTRAINT personal_nonnegative CHECK (account_type='SYSTEM_FUNDING' OR balance_paise >= 0)
);
CREATE TABLE financial_transactions (
  id BIGSERIAL PRIMARY KEY,
  reference UUID NOT NULL UNIQUE,
  type VARCHAR(20) NOT NULL CHECK (type IN ('TOP_UP','TRANSFER')),
  source_wallet_id BIGINT NOT NULL REFERENCES wallets(id),
  destination_wallet_id BIGINT NOT NULL REFERENCES wallets(id),
  amount_paise BIGINT NOT NULL CHECK (amount_paise > 0),
  status VARCHAR(20) NOT NULL CHECK (status='COMPLETED'),
  note VARCHAR(140),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (source_wallet_id <> destination_wallet_id)
);
CREATE TABLE ledger_entries (
  id BIGSERIAL PRIMARY KEY,
  transaction_id BIGINT NOT NULL REFERENCES financial_transactions(id),
  wallet_id BIGINT NOT NULL REFERENCES wallets(id),
  direction VARCHAR(6) NOT NULL CHECK (direction IN ('DEBIT','CREDIT')),
  amount_paise BIGINT NOT NULL CHECK (amount_paise > 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(transaction_id, wallet_id, direction)
);
CREATE TABLE idempotency_records (
  id BIGSERIAL PRIMARY KEY,
  owner_id BIGINT NOT NULL REFERENCES app_users(id),
  operation VARCHAR(30) NOT NULL,
  idempotency_key VARCHAR(100) NOT NULL,
  request_fingerprint VARCHAR(64) NOT NULL,
  transaction_id BIGINT REFERENCES financial_transactions(id),
  response_json TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(owner_id, operation, idempotency_key)
);
CREATE INDEX idx_tx_source_created ON financial_transactions(source_wallet_id, created_at DESC, id DESC);
CREATE INDEX idx_tx_destination_created ON financial_transactions(destination_wallet_id, created_at DESC, id DESC);
CREATE INDEX idx_ledger_wallet ON ledger_entries(wallet_id);
INSERT INTO wallets(reference, owner_id, account_type, balance_paise) VALUES ('00000000-0000-0000-0000-000000000001', NULL, 'SYSTEM_FUNDING', 0);

