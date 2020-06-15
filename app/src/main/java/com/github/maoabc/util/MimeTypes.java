package com.github.maoabc.util;

import android.webkit.MimeTypeMap;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.regex.Pattern;

public final class MimeTypes {

    public static final String SCHEME_ARCHIVE = "archive";

    public static final Pattern ARCHIVE_PATTERN = Pattern.compile("^.*\\.(?i)(zip|Z|rpm|deb|cpio|lzma|arj|xar|lzh|zst|cab|z[0-9]+|rar|xz|tar|tgz|tbz2|txz|jar|gz|bz2|7z|7z.001)$");
    public static final Pattern MUSIC_PATTERN = Pattern.compile("^.*\\.(?i)(mp3|wma|wav|aac|ogg|m4a|flac|ra|rm|amr|mid)$");
    //    public static final Pattern docP = Pattern.compile("^.*\\.(?i)(doc|docx|rtf|odt)$");
    public static final Pattern MOVIE_PATTERN = Pattern.compile("^.*\\.(?i)(3gp|3gpp|mp4|avi|flv|mkv|m4v|rmvb|mpg|mpeg|wmv|mov|vob|ts|divx|asf|avchd|mts|m2ts|webm|wtv)$");
    public static final Pattern IMAGE_PATTERN = Pattern.compile("^.*\\.(?i)(jpg|jpeg|bmp|png|gif|tiff|arw|dng|raw|rw2|srw|webp)$");
    //    public static final Pattern MARKUP_PATTERN = Pattern.compile("^.*\\.(?i)(html|xhtml|xml|css|tex|json)$");
    public static final Pattern SCRIPT_PATTERN = Pattern.compile("^.*\\.(?i)(sh|py|bat|awk|sed|bash|zsh|csh)$");
    //    public static final Pattern excelP = Pattern.compile("^.*\\.(?i)(xls|xlsx|ods)$");
//    public static final Pattern pwrpointP = Pattern.compile("^.*\\.(?i)(ppt|pptx|pps|ppsx|odp)$");
    public static final Pattern TEXT_PATTERN = Pattern.compile("^.*\\.(?i)(txt|log|cfg|ini|rc|prop|csv|conf|java|h|hpp|c|cc|cpp|cxx|js|py|lua|diff|md|html|json|lisp|php|rb|rs|pl|go|awk|sed|hs)$");
    public static final Pattern APK_PATTERN = Pattern.compile("^.*\\.(?i)(apk)$");

    public static final Pattern SHELL_SCRIPT_PATTERN = Pattern.compile("^.*\\.(?i)(sh|bash|zsh)$");

    @IntDef({OTHER, APK, IMAGE, TEXT, MUSIC, MOVIE, ARCHIVE, SCRIPT})
    @interface MimeType {
    }

    public static final int OTHER = 0;
    public static final int APK = 1;
    public static final int IMAGE = 2;
    public static final int TEXT = 3;
    public static final int MUSIC = 4;
    public static final int MOVIE = 5;
    public static final int ARCHIVE = 6;
    public static final int SCRIPT = 7;

    public static final String ALL_MIME_TYPES = "*/*";
    private static final String ALL_TEXT_TYPES = "text/*";

    public static final String MIME_APK = "application/vnd.android.package-archive";
    public static final String MIME_ZIP = "application/zip";
    public static final String MIME_RAR = "application/rar";

    public static final String MIME_7ZIP = "application/x-7z-compressed";

    public static final String MIME_BZIP2 = "application/x-bzip2";

    public static final String MIME_GZIP = "application/x-gzip";

    public static final String MIME_TAR = "application/x-tar";

    public static final String MIME_XZ = "application/x-xz";

    public static final String MIME_ZSTD = "application/zstd";

    public static final String MIME_LZMA = "application/x-lzma";

    public static final String MIME_CPIO = "application/x-cpio";

    public static final String MIME_Z = "application/x-compress";

    // construct a with an approximation of the capacity
    private static final HashMap<String, String> MIME_TYPES = new HashMap<>(1 + (int) (66 / 0.75));

    static {

        /*
         * ================= MIME TYPES ====================
         */
        MIME_TYPES.put("asm", "text/x-asm");
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("js", "application/javascript");

        MIME_TYPES.put("def", "text/plain");
        MIME_TYPES.put("in", "text/plain");
        MIME_TYPES.put("list", "text/plain");
        MIME_TYPES.put("log", "text/plain");
        MIME_TYPES.put("pl", "text/plain");
        MIME_TYPES.put("prop", "text/plain");
        MIME_TYPES.put("properties", "text/plain");
        MIME_TYPES.put("rc", "text/plain");
        MIME_TYPES.put("ini", "text/plain");
        MIME_TYPES.put("md", "text/markdown");

        MIME_TYPES.put("epub", "application/epub+zip");
        MIME_TYPES.put("ibooks", "application/x-ibooks+zip");

        MIME_TYPES.put("ifb", "text/calendar");
        MIME_TYPES.put("eml", "message/rfc822");
        MIME_TYPES.put("msg", "application/vnd.ms-outlook");

        MIME_TYPES.put("ace", "application/x-ace-compressed");
        MIME_TYPES.put("7z", "application/x-7z-compressed");
        MIME_TYPES.put("bz", "application/x-bzip");
        MIME_TYPES.put("bz2", "application/x-bzip2");
        MIME_TYPES.put("cab", "application/vnd.ms-cab-compressed");
        MIME_TYPES.put("gz", "application/x-gzip");
        MIME_TYPES.put("lrf", "application/octet-stream");
        MIME_TYPES.put("jar", "application/java-archive");
        MIME_TYPES.put("xz", "application/x-xz");
        MIME_TYPES.put("tar", "application/x-tar");
        MIME_TYPES.put("Z", "application/x-compress");
        MIME_TYPES.put("lzma", "application/x-lzma");

        MIME_TYPES.put("bat", "application/x-bat");
        MIME_TYPES.put("ksh", "text/plain");
        MIME_TYPES.put("sh", "application/x-sh");
        MIME_TYPES.put("csh", "application/x-csh");
        MIME_TYPES.put("php", "text/x-php");
        MIME_TYPES.put("lisp", "text/x-script.lisp");

        MIME_TYPES.put("db", "application/octet-stream");
        MIME_TYPES.put("db3", "application/octet-stream");

        MIME_TYPES.put("otf", "application/x-font-otf");
        MIME_TYPES.put("ttf", "application/x-font-ttf");
        MIME_TYPES.put("psf", "application/x-font-linux-psf");

        MIME_TYPES.put("cgm", "image/cgm");
        MIME_TYPES.put("btif", "image/prs.btif");
        MIME_TYPES.put("dwg", "image/vnd.dwg");
        MIME_TYPES.put("dxf", "image/vnd.dxf");
        MIME_TYPES.put("fbs", "image/vnd.fastbidsheet");
        MIME_TYPES.put("fpx", "image/vnd.fpx");
        MIME_TYPES.put("fst", "image/vnd.fst");
        MIME_TYPES.put("mdi", "image/vnd.ms-mdi");
        MIME_TYPES.put("npx", "image/vnd.net-fpx");
        MIME_TYPES.put("xif", "image/vnd.xiff");
        MIME_TYPES.put("pct", "image/x-pict");
        MIME_TYPES.put("pic", "image/x-pict");

        MIME_TYPES.put("adp", "audio/adpcm");
        MIME_TYPES.put("au", "audio/basic");
        MIME_TYPES.put("snd", "audio/basic");
        MIME_TYPES.put("m2a", "audio/mpeg");
        MIME_TYPES.put("m3a", "audio/mpeg");
        MIME_TYPES.put("oga", "audio/ogg");
        MIME_TYPES.put("spx", "audio/ogg");
        MIME_TYPES.put("aac", "audio/x-aac");
        MIME_TYPES.put("mka", "audio/x-matroska");

        MIME_TYPES.put("jpgv", "video/jpeg");
        MIME_TYPES.put("jpgm", "video/jpm");
        MIME_TYPES.put("jpm", "video/jpm");
        MIME_TYPES.put("mj2", "video/mj2");
        MIME_TYPES.put("mjp2", "video/mj2");
        MIME_TYPES.put("mpa", "video/mpeg");
        MIME_TYPES.put("ogv", "video/ogg");
        MIME_TYPES.put("flv", "video/x-flv");
        MIME_TYPES.put("mkv", "video/x-matroska");

    }


    /**
     * Get Mime Type of a file
     *
     * @param name the file of which mime type to get
     * @return Mime type in form of String
     */
    @NonNull
    public static String getMimeType(String name) {

        final String extension = getExtension(name);

        String type = getMimeTypeFromExtension(extension);
//        if (type == null) {
//            return "application/octet-stream";
//        }
        return type == null ? "application/octet-stream" : type;
    }

    public static String getMimeTypeFromExtension(String extension) {
        String mimeType = null;
        // mapping extension to system mime types
        if (!extension.isEmpty()) {
            final String extensionLowerCase = extension.toLowerCase();
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extensionLowerCase);
            if (mimeType == null) {
                mimeType = MIME_TYPES.get(extensionLowerCase);
            }
        }
        return mimeType;
    }

    @MimeType
    public static int getSupportMimeType(String name) {
        if (isAPK(name)) {
            return APK;
        } else if (isImage(name)) {
            return IMAGE;
        } else if (isMusic(name)) {
            return MUSIC;
        } else if (isMovie(name)) {
            return MOVIE;
        } else if (isText(name)) {
            return TEXT;
        } else if (isArchive(name)) {
            return ARCHIVE;
        } else if (isScript(name)) {
            return SCRIPT;
        } else {
            return OTHER;
        }
    }

    public static boolean isAPK(String name) {
        return APK_PATTERN.matcher(name).matches();
    }

    public static boolean isImage(String name) {
        return IMAGE_PATTERN.matcher(name).matches();
    }

    public static boolean isText(String name) {
        return TEXT_PATTERN.matcher(name).matches();
    }

    public static boolean isMusic(String name) {
        return MUSIC_PATTERN.matcher(name).matches();
    }

    public static boolean isMovie(String name) {
        return MOVIE_PATTERN.matcher(name).matches();
    }

    public static boolean isArchive(String name) {
        return ARCHIVE_PATTERN.matcher(name).matches();
    }

    public static boolean isScript(String name) {
        return SCRIPT_PATTERN.matcher(name).matches();
    }

    public static boolean isShellScript(String name) {
        return SHELL_SCRIPT_PATTERN.matcher(name).matches();
    }


    /**
     * Helper method for {@link #getMimeType(String)}
     * to calculate the last '.' extension of files
     *
     * @param name the path of file
     * @return extension extracted from name in lowercase
     */
    @NonNull
    public static String getExtension(@NonNull String name) {
        int index = name.lastIndexOf('.');
        /*.开头的文件不算后缀*/
        return index > 0 ? name.substring(index + 1).toLowerCase() : "";
    }

}
