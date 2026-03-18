package video.VideoFW.Service;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class VideoPlayerService {

    public enum State {IDLE, PLAYING, FINISHED, ERROR}

    private volatile State state = State.IDLE;

    private Stage stage;
    private MediaView mediaView;
    private MediaPlayer mediaPlayer;

    private final Queue<String> videoQueue = new LinkedList<>();
    private final int MAX_RETRIES = 2;
    private int retryCount = 0;

    private static final AtomicBoolean javafxInitialized = new AtomicBoolean(false);

    public VideoPlayerService() {
        initJavaFX();
        Platform.runLater(this::initStage); // creează Stage persistent
    }

    private void initJavaFX() {
        if (javafxInitialized.compareAndSet(false, true)) {
            new JFXPanel(); // initializează JavaFX
        }
    }

    private void initStage() {
        mediaView = new MediaView();

        Group root = new Group(mediaView);
        root.setStyle("-fx-background-color: white");

        Scene scene = new Scene(root, 1280, 720);

        stage = new Stage();
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setAlwaysOnTop(true);

        stage.setOnCloseRequest((WindowEvent event) -> {
            cleanupMediaPlayer();
            state = State.FINISHED;
            playNext();
        });

        stage.show();
    }

    public synchronized State getState() {
        return state;
    }

    public synchronized void playVideo(String path) {
        File file = new File(path);
        if (!file.exists()) {
            state = State.ERROR;
            throw new RuntimeException("Video file not found: " + path);
        }

        if (mediaPlayer != null) {
            videoQueue.add(path);
            System.out.println("Added to queue: " + path);
            return;
        }

        startVideo(path);
    }

    private void startVideo(String path) {
        Platform.runLater(() -> {
            try {
                Media media = new Media(new File(path).toURI().toString());
                mediaPlayer = new MediaPlayer(media);

                mediaView.setPreserveRatio(true);
                mediaView.setFitWidth(1980);  // sau orice lățime maximă
                mediaView.setFitHeight(1080);  // sau orice înălțime maximă

                mediaPlayer.setOnPlaying(() -> {
                    retryCount = 0;
                    state = State.PLAYING;
                });

                mediaPlayer.setOnEndOfMedia(() -> {
                    System.out.println("Finished: " + path);
                    cleanupMediaPlayer();
                    playNext();
                });

                mediaPlayer.setOnError(() -> {
                    System.out.println("Error playing: " + path);
                    cleanupMediaPlayer();

                    if (retryCount < MAX_RETRIES) {
                        retryCount++;
                        showWhiteScreenAndRetry(path);
                    } else {
                        state = State.ERROR;
                        retryCount = 0;
                        playNext();
                    }
                });

                mediaView.setMediaPlayer(mediaPlayer);
                state = State.PLAYING;
                mediaPlayer.play();

            } catch (Exception e) {
                state = State.ERROR;
                e.printStackTrace();
            }
        });
    }

    private synchronized void playNext() {
        if (!videoQueue.isEmpty()) {
            String next = videoQueue.poll();
            System.out.println("Playing next: " + next);
            startVideo(next);
        } else {
            state = State.FINISHED;
            Platform.runLater(() -> mediaView.setMediaPlayer(null));
            scheduleIdleCheck();
        }
    }

    private synchronized void cleanupMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
            Platform.runLater(() -> mediaView.setMediaPlayer(null)); // rămâne alb
        }
    }

    public synchronized int getQueueSize() {
        return videoQueue.size();
    }

    public synchronized Queue<String> getQueueSnapshot() {
        return new LinkedList<>(videoQueue);
    }

    private void showWhiteScreenAndRetry(String path) {
        Platform.runLater(() -> {
            mediaView.setMediaPlayer(null);

            new Thread(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> startVideo(path));
            }).start();
        });
    }

    private void scheduleIdleCheck() {
        new Thread(() -> {
            try {
                Thread.sleep(3000); // 3 secunde
                synchronized (VideoPlayerService.this) {
                    if (state == State.FINISHED && videoQueue.isEmpty()) {
                        state = State.IDLE;
                    }
                }
            } catch (InterruptedException ignored) {}
        }).start();
    }

}