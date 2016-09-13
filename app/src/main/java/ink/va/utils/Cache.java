package ink.va.utils;

import com.jakewharton.disklrucache.DiskLruCache;

/**
 * Created by USER on 2016-08-20.
 */
public class Cache {
    private DiskLruCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 20; // 20MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";
}
