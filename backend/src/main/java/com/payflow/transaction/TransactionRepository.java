package com.payflow.transaction;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface TransactionRepository extends JpaRepository<FinancialTransaction,Long> {
  Optional<FinancialTransaction> findByReference(UUID reference);
  @Query("select t from FinancialTransaction t where t.sourceWallet.id=:walletId or t.destinationWallet.id=:walletId")
  Page<FinancialTransaction> history(@Param("walletId") Long walletId,Pageable pageable);
}

