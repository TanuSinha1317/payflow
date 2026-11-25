package com.payflow.ledger;
import org.springframework.data.jpa.repository.*;
import java.util.List;
public interface LedgerRepository extends JpaRepository<LedgerEntry,Long> {
  long countByTransactionId(Long id);
  @Query(value="SELECT w.id, w.reference, w.balance_paise, COALESCE(SUM(CASE WHEN l.direction='CREDIT' THEN l.amount_paise ELSE -l.amount_paise END),0) FROM wallets w LEFT JOIN ledger_entries l ON l.wallet_id=w.id GROUP BY w.id,w.reference,w.balance_paise ORDER BY w.id",nativeQuery=true)
  List<Object[]> balanceComparison();
  @Query(value="SELECT COUNT(*) FROM (SELECT transaction_id FROM ledger_entries GROUP BY transaction_id HAVING SUM(CASE WHEN direction='CREDIT' THEN amount_paise ELSE -amount_paise END)<>0 OR COUNT(*)<>2) x",nativeQuery=true)
  long countUnbalancedTransactions();
}

