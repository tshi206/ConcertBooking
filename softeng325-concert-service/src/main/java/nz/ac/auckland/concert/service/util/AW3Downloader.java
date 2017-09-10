package nz.ac.auckland.concert.service.util;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class AW3Downloader {

    // AWS S3 access credentials for concert images.
    private static final String AWS_ACCESS_KEY_ID = "AKIAIDYKYWWUZ65WGNJA";
    private static final String AWS_SECRET_ACCESS_KEY = "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";

    // Name of the S3 bucket that stores images.
    private static final String AWS_BUCKET = "concert.aucklanduni.ac.nz";

    // Download directory - a directory named "images" in the user's home
    // directory.
    private static final String FILE_SEPARATOR = System
            .getProperty("file.separator");
    private static final String USER_DIRECTORY = System
            .getProperty("user.home");
    private static final String DOWNLOAD_DIRECTORY = USER_DIRECTORY
            + FILE_SEPARATOR + "images";

    private static Logger _logger = LoggerFactory.getLogger(AW3Downloader.class);

    private static File downloadDirectory;

    private static BasicAWSCredentials awsCredentials;

    private static AmazonS3 s3;

    private static List<String> imageNames;

    private static AtomicBoolean atomicBoolean = new AtomicBoolean();

    private AW3Downloader(){}

    public static void initAW3Downloader() {

        atomicBoolean.set(true);

        // Create downloadAllImages directory if it doesn't already exist.
        downloadDirectory = new File(DOWNLOAD_DIRECTORY);
        downloadDirectory.mkdir();

        // Create an AmazonS3 object that represents a connection with the
        // remote S3 service.
        awsCredentials = new BasicAWSCredentials(
                AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
        s3 = AmazonS3ClientBuilder
                .standard()
                .withRegion(Regions.AP_SOUTHEAST_2)
                .withCredentials(
                        new AWSStaticCredentialsProvider(awsCredentials))
                .build();

        // Find images names stored in S3.
        imageNames = getImageNames(s3);

    }

    public static void downloadAllImages(){
        if (!atomicBoolean.get()){
            throw new RuntimeException("Downloader not initialized!");
        }
        // Download all images.
        downloadAllImages(s3, imageNames);
    }

    /**
     * Finds image names stored in a bucket named AWS_BUCKET.
     *
     * @param imageName the name of image to be fetched.
     *
     * @return a List of image objects (an empty list if no such image found).
     *
     */
    public static List<Image> fetchPerformerImage(String imageName){
        if (!atomicBoolean.get()){
            throw new RuntimeException("Downloader not initialized!");
        }
        _logger.info(DOWNLOAD_DIRECTORY);
        TransferManager xfer_mgr = TransferManagerBuilder
                .standard()
                .withS3Client(s3)
                .build();
        List<File> imageFiles = new ArrayList<>();
        imageNames.stream().filter(n -> n.equals(imageName)).forEach(n -> {
            try {
                File f = new File(DOWNLOAD_DIRECTORY + FILE_SEPARATOR + n);
                Download xfer = xfer_mgr.download(AWS_BUCKET, n, f);
                // loop with Transfer.isDone()
                // or block with Transfer.waitForCompletion()
                imageFiles.add(f);
            } catch (AmazonServiceException e) {
                _logger.info(e.getErrorMessage());
            } finally{
                _logger.debug("downloaded image with name: " + n);
            }
        });
        xfer_mgr.shutdownNow();
        List<Image> images = new ArrayList<>();
        imageFiles.stream().forEach(file -> {
            BufferedImage img = null;
            try {
                img = ImageIO.read(file);
            } catch (IOException e) {
                _logger.info(e.getMessage());
            }
            if (img != null)
            images.add(img);
        });
        return images;
    }

    /**
     * Finds image names stored in a bucket named AWS_BUCKET.
     *
     * @param s3 the AmazonS3 connection.
     *
     * @return a List of images names.
     *
     */
    private static List<String> getImageNames(AmazonS3 s3) {
        List<String> imageNames = new ArrayList<>();
        if (!(s3.doesBucketExist(AWS_BUCKET))) {
            _logger.info("Bucket %s does not exist.\n", AWS_BUCKET);
            throw new RuntimeException("NO SUCH BUCKET!");
        }
        ObjectListing ol = s3.listObjects(AWS_BUCKET);
        List<S3ObjectSummary> objects = ol.getObjectSummaries();
        for (S3ObjectSummary os: objects) {
            _logger.debug("* " + os.getKey());
            imageNames.add(os.getKey());
        }
        return imageNames;
    }

    public static Bucket getBucket(String bucket_name) {
        if (!atomicBoolean.get()){
            throw new RuntimeException("Downloader not initialized!");
        }
        Bucket named_bucket = null;
        List<Bucket> buckets = s3.listBuckets();
        for (Bucket b : buckets) {
            if (b.getName().equals(bucket_name)) {
                named_bucket = b;
            }
        }
        return named_bucket;
    }

    /**
     * Downloads images in the bucket named AWS_BUCKET.
     *
     * @param s3 the AmazonS3 connection.
     *
     * @param imageNames the named images to downloadAllImages.
     *
     */
    private static void downloadAllImages(AmazonS3 s3, List<String> imageNames) {
        _logger.info(DOWNLOAD_DIRECTORY);
        AtomicLong _idCounter = new AtomicLong();
        TransferManager xfer_mgr = TransferManagerBuilder
                .standard()
                .withS3Client(s3)
                .build();
        imageNames.stream().forEach(n -> {
            try {
                File f = new File(DOWNLOAD_DIRECTORY + FILE_SEPARATOR + n);
                Download xfer = xfer_mgr.download(AWS_BUCKET, n, f);
                _idCounter.incrementAndGet();
                // loop with Transfer.isDone()
                // or block with Transfer.waitForCompletion()
            } catch (AmazonServiceException e) {
                _logger.info(e.getErrorMessage());
            } finally{
                _logger.debug("downloaded image with name: " + n);
            }
        });
        xfer_mgr.shutdownNow();
        _logger.info("downloaded " + _idCounter + " images in total.");
    }
}
