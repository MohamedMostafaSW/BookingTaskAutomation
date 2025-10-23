package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.monte.media.FormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

public class VideoRecorder {

    private static final Logger logger = LogManager.getLogger(VideoRecorder.class);
    private ScreenRecorder screenRecorder;
    private static final String VIDEO_FOLDER = System.getProperty("user.dir") + "/videos/";

    /**
     * Start video recording
     */
    public void startRecording() throws Exception {
        try {
            File videoDir = new File(VIDEO_FOLDER);
            if (!videoDir.exists()) {
                videoDir.mkdirs();
            }

            GraphicsConfiguration gc = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration();

            Format fileFormat = new Format(
                    MediaTypeKey, MediaType.FILE,
                    MimeTypeKey, MIME_AVI
            );

            Format screenFormat = new Format(
                    MediaTypeKey, MediaType.VIDEO,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DepthKey, 24,
                    FrameRateKey, Rational.valueOf(15),
                    QualityKey, 1.0f,
                    KeyFrameIntervalKey, 15 * 60
            );

            Format mouseFormat = new Format(
                    MediaTypeKey, MediaType.VIDEO,
                    EncodingKey, "black",
                    FrameRateKey, Rational.valueOf(30)
            );

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int width = screenSize.width;
            int height = screenSize.height;

            Rectangle captureSize = new Rectangle(0, 0, width, height);
            screenRecorder = new SpecializedScreenRecorder(
                    gc,
                    captureSize,
                    fileFormat,
                    screenFormat,
                    mouseFormat,
                    null,
                    videoDir
            );
            screenRecorder.start();
            logger.info("Video recording started successfully");

        } catch (IOException | AWTException e) {
            logger.error("Failed to start video recording", e);
            throw new Exception("Unable to start video recording: " + e.getMessage());
        }
    }

    /**
     * Stop video recording
     */
    public void stopRecording() throws Exception {
        try {
            if (screenRecorder != null) {
                screenRecorder.stop();
                logger.info("Video recording stopped successfully");
            }
        } catch (IOException e) {
            logger.error("Failed to stop video recording", e);
            throw new Exception("Unable to stop video recording: " + e.getMessage());
        }
    }

    /**
     * Custom ScreenRecorder class to customize video file naming
     */
    private static class SpecializedScreenRecorder extends ScreenRecorder {

        private String name;

        public SpecializedScreenRecorder(GraphicsConfiguration cfg,
                                         Rectangle captureArea,
                                         Format fileFormat,
                                         Format screenFormat,
                                         Format mouseFormat,
                                         Format audioFormat,
                                         File movieFolder) throws IOException, AWTException {
            super(cfg, captureArea, fileFormat, screenFormat, mouseFormat, audioFormat, movieFolder);
        }
    }

}