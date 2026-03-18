package video.VideoFW.Controller;

import video.VideoFW.Model.Video;
import video.VideoFW.Repository.VideoRepository;
import video.VideoFW.Service.VideoPlayerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/player")
public class PlayerController {

    private final VideoPlayerService playerService;
    private final VideoRepository videoRepository;

    public PlayerController(VideoPlayerService playerService,
                            VideoRepository videoRepository) {
        this.playerService = playerService;
        this.videoRepository = videoRepository;
    }

    @PostMapping("/play/{id}")
    public String playById(@PathVariable Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        playerService.playVideo(video.getPath());
        return "Playing video with id: " + id;
    }

    @PostMapping("/play")
    public String playByName(@RequestParam String name) {
        Video video = videoRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        playerService.playVideo(video.getPath());
        return "Playing video: " + name;
    }

    @GetMapping("/status")
    public String getStatus() {
        return playerService.getState().name();
    }

    @GetMapping("/queue")
    public Object getQueue() {
        return playerService.getQueueSnapshot();
    }

    @GetMapping("/queue/size")
    public int getQueueSize() {
        return playerService.getQueueSize();
    }
}