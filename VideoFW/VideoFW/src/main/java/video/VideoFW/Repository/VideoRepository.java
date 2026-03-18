package video.VideoFW.Repository;

import video.VideoFW.Model.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {

    Optional<Video> findByName(String name);

    boolean existsByName(String name);
}