package com.naver.hackday.android_extract_gif.extractor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by hanseungbeom on 2018. 5. 14..
 */

public class ExtractorUtils {

    public static final String GIF_EXTRACT_VIDEO_URI = "gif_extract_video_uri";
    public static final String GIF_EXTRACT_START = "gif_extract_start";
    public static final String GIF_EXTRACT_END = "gif_extract_end";
    public static final String GIF_EXTRACT_FPS = "gif_extract_fps";
    public static final String GIF_URI_EXTRA = "gif_uri";
    public static final int THUMBNAIL_SIZE = 64;

    private static final String SEPERATOR = "/";
    private static final String PREFIX = "out";
    private static final String SAVE_THUMBNAIL_NAME = "thumbnail";
    private static final String SAVE_IMAGE_NAME = "frame%04d";
    private static final String FRAME_SAVE_FOLDER_NAME = "frame";
    private static final String SAVE_IMAGE_FORMAT = ".jpg";
    private static final String SAVE_GIF_FORMAT = ".gif";


    public static boolean isRange(long nextPos, long presentationTimeUs, long addRate) {
        long presentationTime = presentationTimeUs / 1000;
        return ((nextPos - (double) addRate * 0.75) <= presentationTime) &&
                (presentationTime < (nextPos + (double) addRate * 0.75));
    }


    public static String getInternalStoragePath(Context context) {
        return context.getApplicationContext().getFilesDir().getPath().toString();
    }

    public static Uri saveThumbnail(Context context, Bitmap origin, String createdTime) {
        Bitmap thumbnail = origin.copy(origin.getConfig(), true);
        int height = thumbnail.getHeight();
        int width = thumbnail.getWidth();
        while (height > THUMBNAIL_SIZE) {
            thumbnail = Bitmap.createScaledBitmap(thumbnail, (width * THUMBNAIL_SIZE) / height, THUMBNAIL_SIZE, true);
            height = thumbnail.getHeight();
            width = thumbnail.getWidth();
        }

        String folderName = getInternalStoragePath(context)
                + SEPERATOR
                + createdTime;

        String fileName = SAVE_THUMBNAIL_NAME + SAVE_IMAGE_FORMAT;

        File file = new File(folderName);
        if (!file.exists())
            file.mkdirs();

        File fileCacheItem = new File(folderName + SEPERATOR + fileName);
        OutputStream out = null;

        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, out);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        thumbnail.recycle();
        return Uri.fromFile(fileCacheItem);
    }

    public static void saveBitmaptoJpeg(Context context, Bitmap bitmap, String createdTime, int index) {

        String folderName = getInternalStoragePath(context)
                + SEPERATOR
                + createdTime
                + SEPERATOR
                + FRAME_SAVE_FOLDER_NAME;

        String fileName = String.format(SAVE_IMAGE_NAME, index) + SAVE_IMAGE_FORMAT;

        File file = new File(folderName);
        if (!file.exists())
            file.mkdirs();

        File fileCacheItem = new File(folderName + SEPERATOR + fileName);
        OutputStream out = null;

        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bitmap.recycle();
    }

    public static int deleteDir(String path) {
        int totalDelNum = 0;
        try {
            File file = new File(path);
            File[] childFileList = file.listFiles();
            for (File childFile : childFileList) {
                if (childFile.isDirectory()) {
                    totalDelNum += deleteDir(childFile.getAbsolutePath());
                } else {
                    childFile.delete();
                    totalDelNum++;
                }
            }
            file.delete();
            totalDelNum++;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalDelNum;
    }

    public static String getDeletePath(Context context, long createdTime) {
        return getInternalStoragePath(context)
                + SEPERATOR
                + createdTime
                + SEPERATOR + FRAME_SAVE_FOLDER_NAME;
    }


    public static Bitmap outputBufferToFrame(ByteBuffer outputBuffer, MediaFormat format) {
       byte[] byteArray = new byte[outputBuffer.remaining()];
        outputBuffer.get(byteArray);
        int width = format.getInteger(MediaFormat.KEY_WIDTH);
        int height = format.getInteger(MediaFormat.KEY_HEIGHT);
        int[] rgbArray = decodeYUV420SP(byteArray, width, height);
        return Bitmap.createBitmap(rgbArray, width, height, Bitmap.Config.ARGB_8888);
    }


    public static int[] decodeYUV420SP(byte[] yuv420sp, int width, int height) {

        final int frameSize = width * height;

        int rgb[] = new int[width * height];
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0;
                else if (r > 262143) r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143) g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143) b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) &
                        0xff00) | ((b >> 10) & 0xff);
            }
        }
        return rgb;
    }

    public static String getImagePath(Context context, long createdTime) {
        return getInternalStoragePath(context)
                + SEPERATOR
                + createdTime
                + SEPERATOR
                + FRAME_SAVE_FOLDER_NAME
                + SEPERATOR
                + SAVE_IMAGE_NAME
                + SAVE_IMAGE_FORMAT;
    }

    public static String getOutputPath(long createdTime) {
        return Environment.getExternalStorageDirectory().getPath()
                + SEPERATOR
                + PREFIX
                + createdTime
                + SAVE_GIF_FORMAT;
    }

    public static int getSecFromMs(long milliseconds) {
        return (int) (milliseconds / 1000);
    }

    public static long msToUs(long timeMs) {
        return (timeMs * 1000);
    }


    private static boolean isImageFormatSupported(Image image) {
        int format = image.getFormat();
        switch (format) {
            case ImageFormat.YUV_420_888:
            case ImageFormat.NV21:
            case ImageFormat.YV12:
                return true;
        }
        return false;
    }



    public static byte[] convertYUV420_888ToNV21(Image image) {

        int targetFormat = 2; //save to NV21

        if (!isImageFormatSupported(image)) {
            throw new RuntimeException("can't convert Image to byte array, format " + image.getFormat());
        }
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    if (targetFormat == 1) {//YUV_420_888
                        channelOffset = width * height;
                        outputStride = 1;
                    } else if (targetFormat == 2) {//NV21
                        channelOffset = width * height + 1;
                        outputStride = 2;
                    }
                    break;
                case 2:
                    if (targetFormat == 1) {//YUV_420_888
                        channelOffset = (int) (width * height * 1.25);
                        outputStride = 1;
                    } else if (targetFormat == 2) {//NV21
                        channelOffset = width * height;
                        outputStride = 2;
                    }
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();

            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return data;
    }

    public static Bitmap outputBufferToFrame(Image image, MediaFormat format) {
        byte[] decodedByte = convertYUV420_888ToNV21(image);
        //int width = format.getInteger(MediaFormat.KEY_WIDTH);
        int width = format.getInteger("crop-right") + 1 - format.getInteger("crop-left");
        //int height = format.getInteger(MediaFormat.KEY_HEIGHT);
        int height = format.getInteger("crop-bottom") + 1 - format.getInteger("crop-top");
        return yuv2Img(decodedByte, ImageFormat.NV21 ,width,height,100);
    }

    public static int getWidthFromFormat(MediaFormat format){
        return format.getInteger("crop-right") + 1 - format.getInteger("crop-left");
    }
    public static int getHeightFromFormat(MediaFormat format){
        return format.getInteger("crop-bottom") + 1 - format.getInteger("crop-top");
    }
    public static int[] bitmapToIntArray(Bitmap bitmap){
        int x = bitmap.getWidth();
        int y = bitmap.getHeight();
        int[] intArray = new int[x * y];
        bitmap.getPixels(intArray, 0, x, 0, 0, x, y);
        return intArray;
    }

    public static int getDelayOfFrame(int fps){
        return (int) (((double) 1 / fps) * 1000);
    }

    private static Bitmap yuv2Img(byte[] frameData, int yuvFormat, int prevWidth, int prevHeight, int quality) {
        Bitmap img = null;
        try {
            YuvImage image = new YuvImage(frameData, yuvFormat, prevWidth, prevHeight, null);
            if (image != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0, prevWidth, prevHeight), quality, stream);
                img = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                stream.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return img;
    }

    public static Uri makeGifFile(ByteArrayOutputStream baos,String createdTime) {
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/gif");
        if (!dir.exists())
            dir.mkdir();
        File outFile = new File(dir, "out"+createdTime+SAVE_GIF_FORMAT);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outFile);
            // Put data in your baos
            baos.writeTo(fos);
        } catch (IOException ioe) {
            // Handle exception here
            ioe.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return Uri.fromFile(outFile);
    }

}
