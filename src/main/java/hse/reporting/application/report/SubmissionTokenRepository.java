package hse.reporting.application.report;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubmissionTokenRepository extends JpaRepository<SubmissionToken, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from SubmissionToken t where t.token = :token")
    Optional<SubmissionToken> findForUpdate(@Param("token") String token);
}
