package com.goydevv.studrblx.net;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import com.goydevv.studrblx.format.RbxDocument;
import com.goydevv.studrblx.format.RbxFile;
import com.goydevv.studrblx.model.Instance;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

/* JADX INFO: compiled from: AssetFetcher.kt */
/* JADX INFO: loaded from: classes6.dex */
public final class AssetFetcher {
    private static Context app;
    private static final LruCache<Long, Bitmap> bitmaps;
    private static File dir;
    private static volatile int imageEpoch;
    private static final ConcurrentHashMap<Long, Long> imageFailed;
    private static final ConcurrentHashMap.KeySetView<Long, Boolean> imagePending;
    public static final AssetFetcher INSTANCE = new AssetFetcher();
    public static final String[] IMAGE_WRAPPER_KEYS = new String[]{
        "ShirtTemplate", "PantsTemplate", "Graphic",
        "Texture", "TextureId", "TextureID", "TextureContent", "OverlayTextureId",
        "Image", "ImageContent",
        "ColorMap", "ColorMapContent",
        "SunTextureId", "MoonTextureId",
        "SkyboxBk", "SkyboxDn", "SkyboxFt", "SkyboxLf", "SkyboxRt", "SkyboxUp",
        "SkyboxBkContent", "SkyboxDnContent", "SkyboxFtContent", "SkyboxLfContent", "SkyboxRtContent", "SkyboxUpContent"
    };
    private static final ExecutorService pool = Executors.newFixedThreadPool(4);
    private static final ConcurrentHashMap.KeySetView<Long, Boolean> pending = ConcurrentHashMap.newKeySet();
    private static final ConcurrentHashMap<Long, Long> failed = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, String> errors = new ConcurrentHashMap<>();
    private static final CopyOnWriteArrayList<Function1<Long, Unit>> listeners = new CopyOnWriteArrayList<>();

    private AssetFetcher() {
    }

    static {
        final int iMin = (int) Math.min(50331648L, Runtime.getRuntime().maxMemory() / 8);
        bitmaps = new LruCache<Long, Bitmap>(iMin) {
            @Override
            public /* bridge */ /* synthetic */ int sizeOf(Long l, Bitmap bitmap) {
                return sizeOf(l.longValue(), bitmap);
            }

            protected int sizeOf(long key, Bitmap value) {
                Intrinsics.checkNotNullParameter(value, "value");
                return value.getByteCount();
            }
        };
        imagePending = ConcurrentHashMap.newKeySet();
        imageFailed = new ConcurrentHashMap<>();
    }

    public final CopyOnWriteArrayList<Function1<Long, Unit>> getListeners() {
        return listeners;
    }

    public final void init(Context ctx) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        if (app == null) {
            app = ctx.getApplicationContext();
            File file = new File(ctx.getCacheDir(), "assets");
            file.mkdirs();
            dir = file;
        }
    }

    private final File file(long id) {
        File file = dir;
        if (file != null) {
            return new File(file, id + ".bin");
        }
        return null;
    }

    public final byte[] request(final long id) {
        File file = file(id);
        if (file != null && file.isFile()) {
            try {
                byte[] cached = FilesKt.readBytes(file);
                if (!RobloxApi.isJsonErrorPayload(cached)) {
                    return cached;
                }
                file.delete();
            } catch (Exception e) {
                return null;
            }
        }
        Long f = failed.get(Long.valueOf(id));
        if ((f != null && System.currentTimeMillis() - f.longValue() < 45000) || !pending.add(Long.valueOf(id))) {
            return null;
        }
        pool.execute(new Runnable() {
            @Override
            public final void run() {
                AssetFetcher.request$lambda$5(id);
            }
        });
        return null;
    }

    static final void request$lambda$5(long $id) {
        try {
            INSTANCE.load($id);
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                ((Function1) it.next()).invoke(Long.valueOf($id));
            }
        } catch (Exception e) {
            failed.put(Long.valueOf($id), Long.valueOf(System.currentTimeMillis()));
            String message = e.getMessage();
            if (message == null) {
                message = e.toString();
            }
            errors.put(Long.valueOf($id), message);
        } finally {
            pending.remove(Long.valueOf($id));
        }
    }

    public final boolean isCached(long id) {
        File file = file(id);
        return file != null && file.isFile();
    }

    public final File cachedFile(long id) {
        File f = file(id);
        return (f != null && f.isFile()) ? f : null;
    }

    public final void prefetch(final long id) {
        File f = file(id);
        if (f != null && f.isFile()) {
            return;
        }
        Long failTime = failed.get(Long.valueOf(id));
        if ((failTime != null && System.currentTimeMillis() - failTime.longValue() < 45000) || !pending.add(Long.valueOf(id))) {
            return;
        }
        pool.execute(new Runnable() {
            @Override
            public final void run() {
                AssetFetcher.request$lambda$5(id);
            }
        });
    }

    public final String cacheFile(long id) {
        File file = file(id);
        if (file != null && file.isFile()) {
            return file.getPath();
        }
        return null;
    }

    public final String lastError(long id) {
        return errors.get(Long.valueOf(id));
    }

    public final byte[] load(long id) {
        File f = file(id);
        if (f != null && f.isFile()) {
            byte[] cached = FilesKt.readBytes(f);
            if (cached != null && cached.length > 0 && !RobloxApi.isJsonErrorPayload(cached)) {
                return cached;
            }
            try {
                f.delete();
            } catch (Exception ignored) {
            }
        }
        Context ctx = app;
        if (ctx == null) {
            throw new IllegalStateException("AssetFetcher.init not called");
        }
        byte[] bytes = RobloxApi.downloadAsset$default(RobloxApi.INSTANCE, ctx, id, null, 4, null);
        if (bytes == null || bytes.length == 0 || RobloxApi.isJsonErrorPayload(bytes)) {
            throw com.goydevv.studrblx.util.KtCompat.sneakyThrow(new IOException("Invalid or JSON error payload for asset " + id));
        }
        if (f != null) {
            try {
                File tmp = new File(f.getPath() + ".tmp");
                FilesKt.writeBytes(tmp, bytes);
                tmp.renameTo(f);
            } catch (Exception ignored) {
            }
        }
        failed.remove(Long.valueOf(id));
        errors.remove(Long.valueOf(id));
        return bytes;
    }

    public final int getImageEpoch() {
        return imageEpoch;
    }

    public final Bitmap image(final long id) {
        Bitmap bitmap = bitmaps.get(Long.valueOf(id));
        if (bitmap != null) {
            return bitmap;
        }
        Long f = imageFailed.get(Long.valueOf(id));
        if ((f != null && System.currentTimeMillis() - f.longValue() < 45000) || !imagePending.add(Long.valueOf(id))) {
            return null;
        }
        pool.execute(new Runnable() {
            @Override
            public final void run() {
                AssetFetcher.image$lambda$9(id);
            }
        });
        return null;
    }

    static final void image$lambda$9(long $id) {
        try {
            try {
                Bitmap b = INSTANCE.loadImage($id, 0);
                if (b == null) {
                    imageFailed.put(Long.valueOf($id), Long.valueOf(System.currentTimeMillis()));
                } else {
                    bitmaps.put(Long.valueOf($id), b);
                    imageFailed.remove(Long.valueOf($id));
                    imageEpoch = imageEpoch + 1;
                    ImageLoader.INSTANCE.notifyAssetLoaded($id, b);
                    Iterator it = listeners.iterator();
                    while (it.hasNext()) {
                        ((Function1) it.next()).invoke(Long.valueOf($id));
                    }
                }
            } catch (Exception e) {
                imageFailed.put(Long.valueOf($id), Long.valueOf(System.currentTimeMillis()));
            }
        } finally {
            imagePending.remove(Long.valueOf($id));
        }
    }

    public final boolean imageFailed(long id) {
        return imageFailed.containsKey(Long.valueOf(id)) && bitmaps.get(Long.valueOf(id)) == null;
    }

    public final Bitmap imageBlocking(long id) {
        Bitmap bitmap = bitmaps.get(Long.valueOf(id));
        if (bitmap != null) {
            return bitmap;
        }
        try {
            Bitmap bitmapLoadImage = loadImage(id, 0);
            if (bitmapLoadImage == null) {
                return null;
            }
            bitmaps.put(Long.valueOf(id), bitmapLoadImage);
            imageFailed.remove(Long.valueOf(id));
            imageEpoch = imageEpoch + 1;
            return bitmapLoadImage;
        } catch (Exception e) {
            return null;
        }
    }

    private final Bitmap decodeBitmap(byte[] raw) {
        if (raw == null || raw.length == 0) {
            return null;
        }
        byte[] data = raw;
        try {
            data = RobloxApi.INSTANCE.ungzip(raw);
        } catch (Exception ignored) {
        }
        if (data == null || data.length == 0 || RobloxApi.isJsonErrorPayload(data)) {
            return null;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        if (bmp == null) {
            bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return bmp;
    }

    private final Bitmap fetchRobloxThumbnail(long id) {
        try {
            org.json.JSONObject j = Http.INSTANCE.json(
                "https://thumbnails.roblox.com/v1/assets?assetIds=" + id + "&size=420x420&format=Png&isCircular=false",
                Collections.<String, String>emptyMap()
            );
            org.json.JSONArray arr = j.optJSONArray("data");
            if (arr != null && arr.length() > 0) {
                org.json.JSONObject item = arr.optJSONObject(0);
                if (item != null && "Completed".equals(item.optString("state"))) {
                    String url = item.optString("imageUrl");
                    if (url != null && url.length() > 0) {
                        byte[] imgBytes = Http.get$default(Http.INSTANCE, url, null, 8388608, 2, null);
                        return decodeBitmap(imgBytes);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private final Bitmap loadImage(long id, int depth) {
        byte[] bytes = null;
        try {
            bytes = load(id);
        } catch (Exception e) {
            return fetchRobloxThumbnail(id);
        }
        if (bytes == null || bytes.length == 0) {
            return fetchRobloxThumbnail(id);
        }
        Bitmap bitmapDecodeByteArray = decodeBitmap(bytes);
        if (bitmapDecodeByteArray == null) {
            if (depth > 2) {
                return null;
            }
            try {
                RbxDocument doc = RbxFile.INSTANCE.read(bytes);
                for (Instance r : doc.getRoots()) {
                    for (Instance i : (List<Instance>) CollectionsKt.plus((Collection) CollectionsKt.listOf(r), (Iterable) Instance.descendants$default(r, null, 1, null))) {
                        for (String k : IMAGE_WRAPPER_KEYS) {
                            Object obj = i.get(k);
                            String rawVal = obj instanceof com.goydevv.studrblx.model.ContentValue
                                ? ((com.goydevv.studrblx.model.ContentValue) obj).getUri()
                                : (obj != null ? obj.toString() : null);
                            Long lAssetId = ImageLoader.INSTANCE.assetId(rawVal);
                            if (lAssetId != null) {
                                long inner = lAssetId.longValue();
                                if (inner != id && inner > 0L) {
                                    Bitmap nested = loadImage(inner, depth + 1);
                                    if (nested != null) {
                                        return nested;
                                    }
                                }
                            }
                        }
                    }
                }
                return fetchRobloxThumbnail(id);
            } catch (Exception e) {
                return fetchRobloxThumbnail(id);
            }
        }
        return bitmapDecodeByteArray;
    }

    public final Long idOf(Object content) {
        String raw = content instanceof com.goydevv.studrblx.model.ContentValue
                ? ((com.goydevv.studrblx.model.ContentValue) content).getUri()
                : (content != null ? content.toString() : null);
        return ImageLoader.INSTANCE.assetId(raw);
    }
}
