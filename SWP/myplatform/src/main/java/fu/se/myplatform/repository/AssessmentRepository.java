// src/main/java/fu/se/myplatform/repository/AssessmentRepository.java
package fu.se.myplatform.repository;

import fu.se.myplatform.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
}