package com.payflow.idempotency;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord,Long> {
  @Query(value="INSERT INTO idempotency_records(owner_id,operation,idempotency_key,request_fingerprint) VALUES (:owner,:operation,:key,:fingerprint) ON CONFLICT (owner_id,operation,idempotency_key) DO NOTHING RETURNING id",nativeQuery=true)
  Optional<Long> claim(@Param("owner") Long owner,@Param("operation") String operation,@Param("key") String key,@Param("fingerprint") String fingerprint);
  @Query(value="SELECT * FROM idempotency_records WHERE owner_id=:owner AND operation=:operation AND idempotency_key=:key FOR UPDATE",nativeQuery=true)
  Optional<IdempotencyRecord> lockExisting(@Param("owner") Long owner,@Param("operation") String operation,@Param("key") String key);
}
