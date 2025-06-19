package fu.se.myplatform.repository;

import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Staff findByUser(Account user);

    void deleteByUser(Account account);
}

