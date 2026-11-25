# PayFlow QA Report

Audit date: 25 November 2026 (Asia/Kolkata)

## Registration root cause

The reported `Failed to fetch` was reproduced in Chromium. Initially neither port 8080 nor 5173 was listening. After starting both services, the browser sent `OPTIONS http://localhost:8080/api/v1/auth/register`, received `403 Invalid CORS request`, and never sent the registration POST.

`SecurityConfig` exposed the policy as a bean named `cors`; Spring Security did not discover it as `corsConfigurationSource` and used an empty MVC policy. Renaming the bean fixed the preflight. The local allowlist was also made explicit for `localhost` and `127.0.0.1` on ports 5173 and 3000, because Vite advertised `127.0.0.1` while the original policy accepted only `localhost`.

Evidence after repair: preflight 200 with the exact allowed origin, registration POST 201, redirect to `/app`, one database user, one zero-balance personal wallet, BCrypt password, and role `USER`. An untrusted origin still receives 403 with no allow-origin header.

## Environment and runtime

| Item | Verified result | Status |
|---|---|---|
| Java | Temurin 21.0.13.1; Maven compiled with `release 21` | PASS |
| Maven | 3.9.16 | PASS |
| Node/npm | 24.11.1 / 11.6.2 | PASS |
| PostgreSQL | Homebrew PostgreSQL 18.3, `localhost:5432`, database/user `payflow` | PASS |
| Flyway | V1 validated and applied; five required tables present | PASS |
| Backend | Hikari, JPA, Security and Tomcat initialized; port 8080 | PASS |
| Frontend | Vite served port 5173; production build succeeded | PASS |
| Health/OpenAPI | health `UP`; Swagger and `/v3/api-docs` return 200 | PASS |
| Docker/Testcontainers | Docker socket absent; four Testcontainers tests skipped by `mvn clean verify` | BLOCKED |
| k6 | Not installed; load script retained but benchmark not executed | NOT VERIFIED |

## Functional test matrix

| Feature | Expected / observed after repair | Status |
|---|---|---|
| Browser registration | 201, dashboard redirect, ₹0.00 wallet | PASS |
| Duplicate email/phone | Both return 409; no duplicate wallet | PASS |
| Invalid registration | Invalid email, phone and short password return 400 | PASS |
| Login | Valid 200; wrong password and unknown user 401 | PASS |
| JWT protection | Missing/invalid JWT return structured 401; USER on admin API gets structured 403 | PASS |
| Expired JWT | Signature/expiry code exists, but a real expired-token HTTP test was not executed | NOT VERIFIED |
| Wallets | Three synthetic users each received one zero-balance wallet | PASS |
| Top-up | ₹5,000 browser top-up; replay did not duplicate; invalid amounts return 400 | PASS |
| Transfer | ₹800 browser transfer; balances ₹4,200/₹2,800; shared reference and balanced postings | PASS |
| Rejections | Unknown recipient 404, self-transfer 400, insufficient funds 422, missing idempotency header 400 | PASS |
| History | Correct signs, counterparty, timestamps, newest-first order, authorized details, capped size 100 | PASS |
| History filters/UI receipt | Type/date filters, pagination controls and clickable receipt UI are not implemented | FAIL |
| IDOR protection | Unrelated user receives 404 for another user's transaction | PASS |
| Logout/session | Logout clears in-memory token; `/app` redirects to login; refresh requires login | PASS |
| Engineering Lab | All six UI cards executed actual backend work and displayed observed results | PASS |
| Lab isolation | Synthetic Lab accounts used; Lab route returns structured 404 when disabled | PASS |
| Profile page | Not implemented in current frontend | FAIL |
| Frontend pending state | Financial actions disable while pending; uncertain network outcome retains idempotency key | PASS |

## Financial and concurrency results

Executed against isolated PostgreSQL 18.1 on port 55432, not the normal user database:

| Scenario | Actual verified result | Status |
|---|---|---|
| Concurrent credits | 2/2 committed; receiver ₹1,800; 4 transfer postings | PASS |
| 50 incoming transfers | 50/50 committed; receiver ₹6,000; 100 postings; 0 discrepancies | PASS |
| Double spend | 1 committed, 1 insufficient-funds rejection; sender ₹200 | PASS |
| 50 concurrent debits | 10 committed, 40 expected rejections, 0 unexpected; sender ₹0; 20 postings | PASS |
| 50 duplicate requests | 1 transaction reference, one ₹200 debit, 2 postings | PASS |
| Conflict/retry | Changed payload rejected; identical retry returned original reference | PASS |
| Atomic rollback | Injected post-flush failure; sender ₹1,000, receiver ₹0, no new transaction/postings | PASS |
| Opposite directions | 2 completed within timeout; final balances ₹1,100/₹900 | PASS |
| Concurrent top-ups | 20/20 committed; ₹2,000 final balance; 40 postings | PASS |
| Duplicate registration | Exactly one user and wallet committed | PASS |
| Monetary precision | ₹0.10, ₹10.25 and ₹99.99 converted exactly to 10/1025/9999 paise | PASS |
| Reconciliation detection | Intentional isolated 1-paise mismatch detected; restored test data reconciled | PASS |

Browser Lab observations: credits ₹1,800; debits 1/1 with ₹200 remaining; 50 duplicate requests/1 transaction; rollback balances ₹1,000/₹0; opposite balances ₹1,100/₹900; final reconciliation 0 discrepancies and 0 unbalanced transactions. Repeating credits and reconciliation remained consistent.

## API contract checked

| Feature | Contract |
|---|---|
| Register/login | `POST /api/v1/auth/register|login`, JSON, public, 201/200 |
| Wallet | `GET /api/v1/wallet`, Bearer JWT, 200 |
| Top-up | `POST /api/v1/wallet/top-ups`, JWT + `Idempotency-Key`, `{amount}`, 200 |
| Recipient | `GET /api/v1/recipients/lookup?phone=`, JWT, 200/404 |
| Transfer | `POST /api/v1/transfers`, JWT + idempotency header, `{recipientId,amount,note}`, 200 |
| History/detail | `GET /api/v1/transactions[/{reference}]`, JWT, 200/404 |
| Reconciliation | `GET|POST /api/v1/admin/reconciliation[/run]`, ADMIN only |
| Lab | `POST /api/v1/lab/scenarios/{name}`, authenticated and configuration-gated |

## Repairs made

- Corrected Spring Security CORS bean discovery and added a finite local-origin allowlist.
- Added structured 401/403 security responses.
- Added 400 handling for missing headers and malformed JSON, and 404 for unmapped resources.
- Added pending/disabled financial UI states and safe uncertain-outcome retry messaging.
- Expanded real-PostgreSQL tests for stress credits/debits, retry, rollback, lock order, duplicate registration, concurrent top-ups, precision and discrepancy detection.
- Added two frontend utility/session tests and reproducible local health/start scripts.

## Builds and counts

- Focused real-PostgreSQL backend suite: **18 passed, 0 failed, 0 skipped** (14 integration + 4 money tests).
- Frontend Vitest: **2 passed, 0 failed**.
- `mvn clean verify`: build passed; 4 unit tests ran, 12 opt-in local-PostgreSQL tests and 4 Testcontainers tests were skipped in that invocation. The 14 local-PostgreSQL tests were then executed explicitly and passed.
- Manual API matrix: **32 checks passed after repairs**.
- Browser: registration, login, top-up, transfer, balance/history, logout/protection, and six Lab cards passed.

## Remaining issues and limitations

- Docker is unavailable, so the Testcontainers implementation was not executed.
- k6 is unavailable; no throughput or latency benchmark is claimed.
- Expired-JWT HTTP behavior and scheduled reconciliation timing were not executed.
- Transaction type/date filters, frontend pagination controls, receipt modal and profile screen remain unimplemented.
- Mixed top-up/incoming/outgoing stress in one scenario was not executed; each operation class was tested concurrently and reconciled separately.
- Recipient lookup has no production rate limiter. The in-memory access token intentionally requires login after refresh.
- PostgreSQL 18 works, but the bundled Flyway version logs that its formally tested maximum is PostgreSQL 17.

## Startup instructions

Terminal 1:

```bash
brew services start postgresql@18
cd /Users/tanurakeshsinha/Documents/ChatGPT/payflow
./scripts/start-backend-local.sh
```

Terminal 2:

```bash
cd /Users/tanurakeshsinha/Documents/ChatGPT/payflow/frontend
npm ci
npm run dev -- --host 127.0.0.1
```

Optional local Lab demo: run `./scripts/start-demo-local.sh`, then start the frontend normally. The authenticated frontend discovers Lab availability from the backend; the Lab remains disabled by default in normal backend startup.

Verification:

```bash
cd /Users/tanurakeshsinha/Documents/ChatGPT/payflow
./scripts/check-local.sh
```

Application URLs: frontend `http://localhost:5173`, backend `http://localhost:8080`, health `/actuator/health`, Swagger `/swagger-ui.html`.

