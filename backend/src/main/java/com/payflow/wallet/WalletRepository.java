package com.payflow.wallet;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface WalletRepository extends JpaRepository<Wallet,Long> {
  Optional<Wallet> findByOwnerId(Long ownerId);
  @Query("select w.id from Wallet w where w.owner.id=:ownerId") Optional<Long> findIdByOwnerId(@Param("ownerId") Long ownerId);
  @Query("select w.id from Wallet w where w.accountType='SYSTEM_FUNDING'") Optional<Long> findSystemFundingAccountId();
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select w from Wallet w where w.id in :ids order by w.id")
  List<Wallet> lockAllByIdOrdered(@Param("ids") Collection<Long> ids);
  @Query("select w from Wallet w where w.accountType='SYSTEM_FUNDING'") Optional<Wallet> findSystemFundingAccount();
}

