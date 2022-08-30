package com.luck.picture.lib.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.entity.LocalMediaFolder;
import com.luck.picture.lib.interfaces.OnQueryAlbumListener;
import com.luck.picture.lib.interfaces.OnQueryAllAlbumListener;
import com.luck.picture.lib.interfaces.OnQueryDataResultListener;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * @author：luck
 * @date：2021/11/11 12:53 下午
 * @describe：IBridgeMediaLoader
 */
public abstract class IBridgeMediaLoader {
    protected static final String TAG = IBridgeMediaLoader.class.getSimpleName();
    protected static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");
    protected static final String ORDER_BY = MediaStore.MediaColumns.DATE_MODIFIED + " DESC";
    protected static final String NOT_GIF = " AND (" + MediaStore.MediaColumns.MIME_TYPE + "!='image/gif')";
    protected static final String GROUP_BY_BUCKET_Id = " GROUP BY (bucket_id";
    protected static final String COLUMN_COUNT = "count";
    protected static final String COLUMN_BUCKET_ID = "bucket_id";
    protected static final String COLUMN_DURATION = "duration";
    protected static final String COLUMN_BUCKET_DISPLAY_NAME = "bucket_display_name";
    protected static final String COLUMN_ORIENTATION = "orientation";
    protected static final int MAX_SORT_SIZE = 60;
    private Context mContext;
    private PictureSelectionConfig mConfig;
    protected static final String text = " (" + MediaStore.MediaColumns.MIME_TYPE + " like 'text/%')";
    protected static final String[][] MIME_MAP_TABLE_DOCUMENT = {{"application/pdf", "pdf"},
            {"application/msword", "doc"},
//            {"application/msword", "dot"},
            {"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"},
            {"application/vnd.openxmlformats-officedocument.wordprocessingml.template", "dotx"},
            {"application/vnd.ms-excel", "xls"},
//            {"application/vnd.ms-excel", "xlt"},
            {"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"},
            {"application/vnd.openxmlformats-officedocument.spreadsheetml.template", "xltx"},
            {"application/vnd.ms-powerpoint", "ppt"},
//            {"application/vnd.ms-powerpoint", "pot"},
//            {"application/vnd.ms-powerpoint", "pps"},
            {"application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx"},
            {"application/vnd.openxmlformats-officedocument.presentationml.template", "potx"},
            {"application/vnd.openxmlformats-officedocument.presentationml.slideshow", "ppsx"}};

    protected static final Integer[] MIME_MAP_TABLE_PDF = {0};
    protected static final Integer[] MIME_MAP_TABLE_DOC = {1, 2, 3};
    protected static final Integer[] MIME_MAP_TABLE_XLS = {4, 5, 6};
    protected static final Integer[] MIME_MAP_TABLE_PPT = {7, 8, 9, 10};


    protected static final String PNG = "png";
    protected static final String JPG = "jpg";
    protected static final String JPEG = "jpeg";
    protected static final String GIF = "gif";
    protected static final String MP3 = "mp3";
    protected static final String AAC = "aac";
    protected static final String WAV = "wav";
    protected static final String M4A = "m4a";
    protected static final String MP4 = "mp4";
    protected static final String _3GP = "3gp";
    protected static final String TXT = "txt";
    protected static final String XML = "xml";
    protected static final String JSON = "json";
    protected static final String DOC = "doc";
    protected static final String DOT = "dot";
    protected static final String DOCX = "docx";
    protected static final String DOTX = "dotx";
    protected static final String XLS = "xls";
    protected static final String XLSX = "xlsx";
    protected static final String PPT = "ppt";
    protected static final String PPTX = "pptx";
    protected static final String PDF = "pdf";
    protected static final String ZIP = "zip";
    protected static final String[] MIME_MAP_TABLE_PDF_R = {PDF};
    protected static final String[] MIME_MAP_TABLE_DOC_R = {DOC, DOT, DOCX, DOTX};
    protected static final String[] MIME_MAP_TABLE_XLS_R = {XLS, XLSX};
    protected static final String[] MIME_MAP_TABLE_PPT_R = {PPT, PPTX};
    protected static final String[] MIME_MAP_TABLE_TXT_R = {TXT, XML, JSON};
    protected static final String[] MIME_MAP_TABLE_ALL_R = {TXT, XML, JSON, PDF, DOC, DOT, DOCX, DOTX, XLS, XLSX, PPT, PPTX};

    /**
     * init config
     *
     * @param context
     * @param config  {@link PictureSelectionConfig}
     */
    public void initConfig(Context context, PictureSelectionConfig config) {
        this.mContext = context;
        this.mConfig = config;
    }

    protected Context getContext() {
        return mContext;
    }

    protected PictureSelectionConfig getConfig() {
        return mConfig;
    }

    /**
     * A list of which columns to return. Passing null will return all columns, which is inefficient.
     */
    protected static final String[] PROJECTION = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            COLUMN_DURATION,
            MediaStore.MediaColumns.SIZE,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DISPLAY_NAME,
            COLUMN_BUCKET_ID,
            MediaStore.MediaColumns.DATE_ADDED,
            COLUMN_ORIENTATION};

    /**
     * A list of which columns to return. Passing null will return all columns, which is inefficient.
     */
    protected static final String[] ALL_PROJECTION = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            COLUMN_DURATION,
            MediaStore.MediaColumns.SIZE,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DISPLAY_NAME,
            COLUMN_BUCKET_ID,
            MediaStore.MediaColumns.DATE_ADDED,
            COLUMN_ORIENTATION,
            "COUNT(*) AS " + COLUMN_COUNT};

    /**
     * query album cover
     *
     * @param bucketId
     */
    public abstract String getAlbumFirstCover(long bucketId);


    /**
     * query album list
     */
    public abstract void loadAllAlbum(OnQueryAllAlbumListener<LocalMediaFolder> query);

    /**
     * page query specified contents
     *
     * @param bucketId
     * @param page
     * @param pageSize
     */
    public abstract void loadPageMediaData(long bucketId, int page, int pageSize, OnQueryDataResultListener<LocalMedia> query);


    /**
     * query specified contents
     */
    public abstract void loadOnlyInAppDirAllMedia(OnQueryAlbumListener<LocalMediaFolder> query);


    /**
     * A filter declaring which rows to return,
     * formatted as an SQL WHERE clause (excluding the WHERE itself).
     * Passing null will return all rows for the given URI.
     */
    protected abstract String getSelection();

    /**
     * You may include ?s in selection, which will be replaced by the values from selectionArgs,
     * in the order that they appear in the selection. The values will be bound as Strings.
     */
    protected abstract String[] getSelectionArgs();

    /**
     * How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself).
     * Passing null will use the default sort order, which may be unordered.
     */
    protected abstract String getSortOrder();

    /**
     * parse LocalMedia
     *
     * @param data      Cursor
     * @param isUsePool object pool
     */
    protected abstract LocalMedia parseLocalMedia(Cursor data, boolean isUsePool);

    /**
     * Get video (maximum or minimum time)
     *
     * @return
     */
    protected String getDurationCondition() {
        long maxS = getConfig().filterVideoMaxSecond == 0 ? Long.MAX_VALUE : getConfig().filterVideoMaxSecond;
        return String.format(Locale.CHINA, "%d <%s " + COLUMN_DURATION + " and " + COLUMN_DURATION + " <= %d",
                Math.max((long) 0, getConfig().filterVideoMinSecond), "=", maxS);
    }

    /**
     * Get media size (maxFileSize or miniFileSize)
     *
     * @return
     */
    protected String getFileSizeCondition() {
        long maxS = getConfig().filterMaxFileSize == 0 ? Long.MAX_VALUE : getConfig().filterMaxFileSize;
        return String.format(Locale.CHINA, "%d <%s " + MediaStore.MediaColumns.SIZE + " and " + MediaStore.MediaColumns.SIZE + " <= %d",
                Math.max(0, getConfig().filterMinFileSize), "=", maxS);
    }

    /**
     * getQueryMimeCondition
     *
     * @return
     */
    protected String getQueryMimeCondition() {
        List<String> filters = getConfig().queryOnlyList;
        HashSet<String> filterSet = new HashSet<>(filters);
        Iterator<String> iterator = filterSet.iterator();
        StringBuilder stringBuilder = new StringBuilder();
        int index = -1;
        while (iterator.hasNext()) {
            String value = iterator.next();
            if (TextUtils.isEmpty(value)) {
                continue;
            }

            if (getConfig().chooseMode == SelectMimeType.ofVideo()) {
                if (value.startsWith(PictureMimeType.MIME_TYPE_PREFIX_IMAGE) || value.startsWith(PictureMimeType.MIME_TYPE_PREFIX_AUDIO)) {
                    continue;
                }
            } else if (getConfig().chooseMode == SelectMimeType.ofImage()) {
                if (value.startsWith(PictureMimeType.MIME_TYPE_PREFIX_AUDIO) || value.startsWith(PictureMimeType.MIME_TYPE_PREFIX_VIDEO)) {
                    continue;
                }
            } else if (getConfig().chooseMode == SelectMimeType.ofAudio()) {
                if (value.startsWith(PictureMimeType.MIME_TYPE_PREFIX_VIDEO) || value.startsWith(PictureMimeType.MIME_TYPE_PREFIX_IMAGE)) {
                    continue;
                }
            }
            if (getConfig().chooseMode == SelectMimeType.ofDocument()) {
                if (value.startsWith(PictureMimeType.MIME_TYPE_PREFIX_VIDEO)
                        || value.startsWith(PictureMimeType.MIME_TYPE_PREFIX_IMAGE)
                        || value.startsWith(PictureMimeType.MIME_TYPE_PREFIX_AUDIO)) {
                    continue;
                }
            }

            if (getConfig().chooseMode == SelectMimeType.ofDocument()) {
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    if (Objects.equals(value, PictureMimeType.MIME_TYPE_PREFIX_DOCUMENT_DOC)) {
                        index = getIndex(stringBuilder, index, MIME_MAP_TABLE_DOC);
                    } else if (Objects.equals(value, PictureMimeType.MIME_TYPE_PREFIX_DOCUMENT_XLS)) {
                        index = getIndex(stringBuilder, index, MIME_MAP_TABLE_XLS);
                    } else if (Objects.equals(value, PictureMimeType.MIME_TYPE_PREFIX_DOCUMENT_PPT)) {
                        index = getIndex(stringBuilder, index, MIME_MAP_TABLE_PPT);
                    } else if (Objects.equals(value, PictureMimeType.MIME_TYPE_PREFIX_DOCUMENT_PDF)) {
                        index = getIndex(stringBuilder, index, MIME_MAP_TABLE_PDF);
                    } else if (Objects.equals(value, PictureMimeType.MIME_TYPE_PREFIX_DOCUMENT_TXT)) {
                        index++;
                        stringBuilder.append(index == 0 ? " " : " OR ").append(text);
                    }
//                } else {
//                    if (Objects.equals(value, PictureMimeType.MIME_TYPE_PREFIX_DOCUMENT_DOC)) {
//                        index = getIndexR(stringBuilder, index, MIME_MAP_TABLE_DOC_R);
//                    } else if (Objects.equals(value, PictureMimeType.MIME_TYPE_PREFIX_DOCUMENT_XLS)) {
//                        index = getIndexR(stringBuilder, index, MIME_MAP_TABLE_XLS_R);
//                    } else if (Objects.equals(value, PictureMimeType.MIME_TYPE_PREFIX_DOCUMENT_PPT)) {
//                        index = getIndexR(stringBuilder, index, MIME_MAP_TABLE_PPT_R);
//                    } else if (Objects.equals(value, PictureMimeType.MIME_TYPE_PREFIX_DOCUMENT_PDF)) {
//                        index = getIndexR(stringBuilder, index, MIME_MAP_TABLE_PDF_R);
//                    } else if (Objects.equals(value, PictureMimeType.MIME_TYPE_PREFIX_DOCUMENT_TXT)) {
//                        index = getIndexR(stringBuilder, index, MIME_MAP_TABLE_TXT_R);
//                    }
//                }
            } else {
                index++;
                stringBuilder.append(index == 0 ? " AND " : " OR ").append(MediaStore.MediaColumns.MIME_TYPE).append("='").append(value).append("'");
            }

        }
        if (getConfig().chooseMode != SelectMimeType.ofVideo() && getConfig().chooseMode != SelectMimeType.ofDocument()) {
            if (!getConfig().isGif && !filterSet.contains(PictureMimeType.ofGIF())) {
                stringBuilder.append(NOT_GIF);
            }
        }

        if (getConfig().chooseMode == SelectMimeType.ofDocument() &&
                stringBuilder.toString().isEmpty()) {
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                stringBuilder.append(text);
                for (String[] strings : MIME_MAP_TABLE_DOCUMENT) {
                    stringBuilder.append(" OR (")
                            .append(MediaStore.MediaColumns.MIME_TYPE)
                            .append("='")
                            .append(strings[0]).append("')");
                }
//            } else {
//                for (int i = 0; i < MIME_MAP_TABLE_ALL_R.length; i++) {
//                    if (i == MIME_MAP_TABLE_ALL_R.length - 1) {
//                        stringBuilder.append(MediaStore.Files.FileColumns.DATA)
//                                .append(" LIKE '%.")
//                                .append(MIME_MAP_TABLE_ALL_R[i])
//                                .append("'");
//                    } else {
//                        stringBuilder.append(MediaStore.Files.FileColumns.DATA)
//                                .append(" LIKE '%.")
//                                .append(MIME_MAP_TABLE_ALL_R[i])
//                                .append("'")
//                                .append(" OR ");
//                    }
//                }
//            }

        }

        return stringBuilder.toString();
    }

    private int getIndexR(StringBuilder stringBuilder, int index, String[] mimeMapTablePdfR) {
        for (String s : mimeMapTablePdfR) {
            index++;
            if (index > 0) {
                stringBuilder.append(" OR ");
            }
            stringBuilder.append(MediaStore.Files.FileColumns.DATA)
                    .append(" LIKE '%.")
                    .append(s)
                    .append("'");
        }
        return index;
    }

    private int getIndex(StringBuilder stringBuilder, int index, Integer[] integers) {
        for (Integer integer : integers) {
            index++;
            String[] strings = MIME_MAP_TABLE_DOCUMENT[integer];
            stringBuilder.append(
                    index == 0 ? "(" : " OR (")
                    .append(MediaStore.MediaColumns.MIME_TYPE)
                    .append("='")
                    .append(strings[0]).append("')");
        }
        return index;
    }


}
