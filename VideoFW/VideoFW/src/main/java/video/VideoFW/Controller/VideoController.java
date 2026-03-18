package video.VideoFW.Controller;

import video.VideoFW.Model.Video;
import video.VideoFW.Repository.VideoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/videos")
public class VideoController {

    private final VideoRepository videoRepository;

    public VideoController(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @GetMapping
    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }


    @PostMapping
    public Video createVideo(@RequestBody Video video) {
        if (videoRepository.existsByName(video.getName())) {
            throw new RuntimeException("Video with this name already exists");
        }
        return videoRepository.save(video);
    }
}