package cn.loblok.upc.common.utils;

public class ImageUtil {

    // 基础缩略图：256宽，webp格式（用于列表页）
    private static final String THUMBNAIL_SUFFIX = "?imageMogr2/thumbnail/256x/format/webp";
    // 详情图：原尺寸，webp格式（用于详情页，省流量）
    private static final String DETAIL_SUFFIX = "?imageMogr2/format/webp";

    /**
     * 获取优化后的图片URL
     * @param originalUrl 原始COS地址
     * @param isThumbnail 是否是缩略图
     * @return 拼接后的URL
     */
    public static String getOptimizedUrl(String originalUrl, boolean isThumbnail) {
        if (originalUrl == null || originalUrl.isEmpty()) {
            return "";
        }
        // 如果已经有参数了，避免重复拼接
        if (originalUrl.contains("?")) {
            return originalUrl;
        }
        return originalUrl + (isThumbnail ? THUMBNAIL_SUFFIX : DETAIL_SUFFIX);
    }
}