package devdragons.yiuServer.repository;

import devdragons.yiuServer.domain.Curriculum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurriculumRepository extends JpaRepository<Curriculum, Integer> {
    Optional<Curriculum> findByTitle(String title);
}
