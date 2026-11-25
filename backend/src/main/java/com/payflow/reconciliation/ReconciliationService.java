package com.payflow.reconciliation;
import com.payflow.ledger.LedgerRepository;import com.payflow.transaction.TransactionRepository;import com.payflow.wallet.WalletRepository;
import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Isolation;import org.springframework.transaction.annotation.Transactional;
import java.time.*;import java.util.*;
@Service
public class ReconciliationService {
  public record Discrepancy(String walletReference,long storedBalancePaise,long ledgerBalancePaise){}
  public record Report(Instant executedAt,long accountsInspected,long transactionsInspected,long ledgerEntriesInspected,long unbalancedTransactions,List<Discrepancy> discrepancies,long durationMillis,boolean passed){}
  private final WalletRepository wallets;private final TransactionRepository txs;private final LedgerRepository ledger;
  public ReconciliationService(WalletRepository w,TransactionRepository t,LedgerRepository l){wallets=w;txs=t;ledger=l;}
  @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ) public Report run(){long start=System.nanoTime();List<Discrepancy> d=new ArrayList<>();List<Object[]> rows=ledger.balanceComparison();for(Object[] r:rows){long stored=((Number)r[2]).longValue(),derived=((Number)r[3]).longValue();if(stored!=derived)d.add(new Discrepancy(r[1].toString(),stored,derived));}long unbalanced=ledger.countUnbalancedTransactions();long ms=(System.nanoTime()-start)/1_000_000;return new Report(Instant.now(),rows.size(),txs.count(),ledger.count(),unbalanced,d,ms,d.isEmpty()&&unbalanced==0);}
}

