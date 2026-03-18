# WinnowPlaybackSystem
My solution for a playback system to help monitor the food waste using Java Spring Initializr.

API calls:

POST http://localhost:8080/videos -> give a JSON with name and path, it will add the video with the name and unique ID in the database. Trying to add another video (exactly same path) but with the same name parameter won't work. It is unique as the videos in the folder would be unique too.
{
  "name": "prajitura1",
  "path": "C:/videos/food3.mp4"
}

GET http://localhost:8080/videos -> returns data about all the videos currently added in the database

POST http://localhost:8080/play/{id} -> send a play request to the backend to play the video with ID = {id}
POST http://localhost:8080/player/play?name={name} -> sends a play request to the backend to play the video with name = {name}. The name between the brackets should be the name you sent in the post above, like "prajitura1".

GET http://localhost:8080/player/status -> returns the status of the application, it can be: IDLE (no video playing for more than 3 seconds), PLAYING (video is ongoing), ERROR and FINISHED.
GET http://localhost:8080/player/queue -> returns the queue with the paths of the next videos that will be played.
GET http://localhost:8080/player/queue/SIZE -> returns the number of videos that will be played after the current video.

HOW TO RUN:

1. Run Application.java and wait for the application to start.
2.1. Ensure there are videos in your computer and copy the path to send POST requests to populate the database with records.
2.2. You can also log in with H2 in localhost: http://localhost:8080/h2-console with the details below to use SQL querries.

spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=

3. Once you have stored the videos, send multiple POST requestes for play to see the functionality. Repeated POST, even for the same ID/Name will be added in queue, ensuring they are played in order.

Assumptions:
- the monitor on which the application's window will be opened is supposed to have the sleep option disabled, ensuring the monitor's display will never close due to inactivity.
- the videos are added manually by the user.
- this only does the playback system, it doesn't take feedback from the camera yet.

Improvements:
- add logs for the videos which status returned ERROR, number of retries and timestamps for all videos to ensure the whole video is running, letting the camera do its image processing behind.
- due to some videos resolution, the application might display only on 1/3 of the monitor; either create an algorithm that would make the video of a good resolution to let the camera understand what it's there OR all the videos should specifically be filmed with a specific resolution which can be hard coded.
- no need to add the videos yourself in the database, simply create a function to read all the mp4 files from a folder and automatically index them in the database with ID and name in the alphabetical order.

Dependencies can be found in pom.xml file, some were provided by the Spring Initializr website, some were added manually.

Q: Repeated requests: What should happen if a test triggers playback while a video is already playing?
R: It shouldn't play over it, it should wait for the current video to end, and once it is ended, out of a queue where the video was added, play it.

Q: System state: How could an automated test determine whether the system is idle, playing, or has encountered an error?
R: The applications works like a state machine, for each important step the state updates, corresponding to the current situation: IDLE - a video wasn't played for more than 3 seconds, neither there is one in queue, or the application just started; FINISHED - the video finished playing, it will stay up to 3 seconds before being turned into IDLE; PLAYING - the video is currently running so there is way to play another video at the same time; ERROR - there was an error either at the start of a video or during it, the application will try to run 2 times before deciding to proceed to the next video in queue.

Q: Video selection: How might the test specify which scenario video should be played?
R: We could know that based on the feedback from the camera or based on the ERROR logs and could implement further based on it.

Q: Error handling: How should the system behave if a video file cannot be found or playback fails?
R: The system shouldn't stop just because one video didn't work. I have made the algorithm so it runs 2 times in case it fails, maybe first time was due to a uncommon error and 2nd time worked. If both times fail, it will simply go to the next video in queue. For example: if current video X is playing and in queue there is Y and Z, and the current video X is failing for the first time, it will try to play X again (despite Y being in queue) and if it fails again, go forward to Y and so on.

Q: Test repeatability: How would you ensure that tests can run reliably and consistently across multiple runs?
R: I would implement a log for all the videos that were ran and see if there were errors. Based on the log I would check if the device with the camera got it right.
