# PayFlow Interview Guide

## Thirty-second explanation

PayFlow is a simulated INR wallet that focuses on correctness under concurrent requests. PostgreSQL transactions, ordered pessimistic row locks, atomic database idempotency claims, and balanced ledger postings ensure a transfer is all-or-nothing and never spends the same balance twice.

## Source files to study

- `TransactionEngine.java`: the complete money-movement transaction boundary.
- `V1__initial_schema.sql`: database invariants and indexes.
- `PayFlowPostgresTest.java`: real concurrent PostgreSQL verification.
- `ReconciliationService.java`: stored-versus-ledger integrity checks.
- `LabScenarioService.java`: controlled, measured demonstrations.

## Transfer flow

1. JWT identifies the sender; request validation parses exact paise.
2. `INSERT ... ON CONFLICT DO NOTHING` claims `(owner, operation, key)`.
3. A matching committed claim replays its stored result; a different fingerprint returns 409.
4. Only wallet IDs are resolved before locking—no balance is read early.
5. Both rows are locked in ascending ID order and fresh balances are checked.
6. Debit, credit, transaction, two ledger entries, and idempotent response commit together.
7. Any unchecked failure rolls back every write.

## Why these choices

- Pessimistic locking makes contention behavior explicit and easy to demonstrate; it trades throughput for predictable correctness on hot wallets.
- The database, not `synchronized`, coordinates multiple application instances.
- A modular monolith keeps one ACID boundary and avoids distributed transaction complexity.
- Integer paise and checked arithmetic avoid binary rounding and overflow.
- The funding account uses a signed balance: top-ups debit it and credit personal wallets, so aggregate balance remains zero.

## Likely questions

**Why not optimistic locking?** It performs well at low contention but requires retry policy and user-visible conflict semantics. Wallet contention is a deliberate focus here, so row locks are clearer.

**Can two opposite transfers deadlock?** Both code paths lock the same wallet IDs in ascending order, removing that particular circular wait. Other database resources could still deadlock; production code should classify and bound retries.

**What happens after a lost HTTP response?** The client retries with the same key. The committed JSON response is returned without another debit.

**Is the ledger tamper-proof?** No. It is append-only through the API and reconciled, but it is not cryptographically immutable.

## Demonstration

Enable `LAB_ENABLED=true` and `VITE_LAB_ENABLED=true`, log in with a demo user, then run all six cards. Explain expected values before revealing observed results. The rollback card uses a server-side fault hook that is not registered unless the lab is enabled.

