package com.fuint.common.util;

import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.apache.commons.lang.StringUtils;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成工具类
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public class QRCodeUtil {
    public static final Logger logger = LoggerFactory.getLogger(QRCodeUtil.class);

    /**
     * 生成包含字符串信息的二维码图片
     *
     * @param outputStream 文件输出流路径
     * @param content      二维码携带信息
     * @param width        宽度
     * @param height       高度
     * @param imageFormat  二维码的格式
     * @param resource     原图
     */
    public static boolean createQrCode(OutputStream outputStream, String content, int width, int height, String imageFormat, String resource) {
        // 设置二维码纠错级别
        HashMap<EncodeHintType, String> hints = new HashMap<EncodeHintType, String>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        width = width + 200;
        height = height + 200;
        try {
            // 创建比特矩阵(位矩阵)的QR码编码的字符串
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            // 使BufferedImage勾画QRCode  (matrixWidth 是行二维码像素点)
            int matrixWidth = byteMatrix.getWidth();
            BufferedImage image = new BufferedImage(matrixWidth - 200, matrixWidth - 200, BufferedImage.TYPE_INT_RGB);

            // 使用比特矩阵画并保存图像
            image.createGraphics();
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, matrixWidth, matrixWidth);
            graphics.setColor(Color.BLACK);
            for (int i = 0; i < matrixWidth; i++) {
                for (int j = 0; j < matrixWidth; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i - 100, j - 100, 1, 1);
                    }
                }
            }

            if (StringUtils.isNotEmpty(resource)) {
                BufferedImage big = getRemoteBufferedImage(resource);

                BufferedImage small = image;
                Graphics2D g = big.createGraphics();

                // 二维码坐标（默认在右上角）
                int x = big.getWidth() - small.getWidth() - 2;
                int y = 2;

                g.drawImage(small, x, y, small.getWidth(), small.getHeight(), null);
                g.dispose();

                return ImageIO.write(big, imageFormat, outputStream);
            } else {
                return ImageIO.write(image, imageFormat, outputStream);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 生成条码
     *
     * @param content 条码内容
     * @param width 宽度
     * @param height 高度
     * */
    public static boolean createBarCode(OutputStream outputStream, String content, int width, int height) {
        width = width + 200;
        height = height + 200;
        // 设置编码类型为 Code 128
        BarcodeFormat barcodeFormat = BarcodeFormat.CODE_128;
        // 设置编码参数，例如字符集等
        Map<EncodeHintType, Object> encodeHints = new HashMap<>();
        encodeHints.put(EncodeHintType.CHARACTER_SET, "UTF-8"); // 可以根据需要设置字符集
        encodeHints.put(EncodeHintType.MARGIN, 1); // 设置边距大小，默认为10px
        // 生成条码矩阵
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, barcodeFormat, width, height, encodeHints);

            int matrixWidth = bitMatrix.getWidth();
            BufferedImage image = new BufferedImage(matrixWidth - 200, matrixWidth - 200, BufferedImage.TYPE_INT_RGB);

            return ImageIO.write(image, "PNG", outputStream);
        } catch (WriterException e) {
            System.err.println("Could not generate barcode, WriterException :: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Could not generate barcode due to other IOException :: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 读二维码并输出携带的信息
     */
    public static void readQrCode(InputStream inputStream) throws IOException {
        //设置二维码纠错级别
        HashMap<DecodeHintType, String> hints = new HashMap<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        //从输入流中获取字符串信息
        BufferedImage image = ImageIO.read(inputStream);
        //将图像转换为二进制位图源
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        Result result = null;
        try {
            result = reader.decode(bitmap, hints);
        } catch (ReaderException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info(result.getText());
    }

    /**
     * 获取远程网络图片信息
     * @param imageURL
     * @return
     */
    public static BufferedImage getRemoteBufferedImage(String imageURL) {
        URL url;
        InputStream is = null;
        BufferedImage bufferedImage = null;
        try {
            url = new URL(imageURL);
            is = url.openStream();
            bufferedImage = ImageIO.read(is);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println("imageURL: " + imageURL + ",无效!");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("imageURL: " + imageURL + ",读取失败!");
            return null;
        } finally {
            try {
                if (is!=null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("imageURL: " + imageURL + ",流关闭异常!");
                return null;
            }
        }
        return bufferedImage;
    }
}
