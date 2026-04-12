package springbootproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springbootproject.entity.FileMetadata;
import springbootproject.entity.User;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    List<FileMetadata> findByUser(User user);

    Optional<FileMetadata> findByIdAndUser(Long id, User user);
}