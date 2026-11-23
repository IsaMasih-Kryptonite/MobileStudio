package com.goydevv.studrblx.net;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import com.goydevv.studrblx.model.ContentValue;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kotlin.Unit;
import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import kotlin.text.RegexOption;
import kotlin.text.StringsKt;

/* JADX INFO: compiled from: ImageLoader.kt */
/* JADX INFO: loaded from: classes6.dex */
public final class ImageLoader {
    public static final ImageLoader INSTANCE = new ImageLoader();
    private static Context appCtx;
    private static volatile boolean batchScheduled;
    private static File cacheDir;
    private static volatile int epoch;
    private static final ConcurrentHashMap<String, Long> failed;
    private static final ConcurrentHashMap<String, Long> idMemo;
    private static final ConcurrentHashMap.KeySetView<Long, Boolean> idQueue;
    private static final List<Regex> idRx;
    private static final ConcurrentHashMap<Long, String> keyMemo;
    private static final CopyOnWriteArrayList<Function1<String, Unit>> listeners;
    private static final LruCache<String, Bitmap> mem;
    private static final ConcurrentHashMap.KeySetView<String, Boolean> pending;
    private static final ExecutorService pool;

    private ImageLoader() {
    }

    static {
        final int iMin = (int) Math.min(41943040L, Runtime.getRuntime().maxMemory() / 10);
        mem = new LruCache<String, Bitmap>(iMin) {
            @Override
            public int sizeOf(String key, Bitmap value) {
                Intrinsics.checkNotNullParameter(key, "key");
                Intrinsics.checkNotNullParameter(value, "value");
                return value.getByteCount();
            }
        };
        pending = ConcurrentHashMap.newKeySet();
        failed = new ConcurrentHashMap<>();
        pool = Executors.newFixedThreadPool(4);
        idQueue = ConcurrentHashMap.newKeySet();
        listeners = new CopyOnWriteArrayList<>();
        idRx = CollectionsKt.listOf(new Regex[]{
            new Regex("rbxassetid://(\\d+)", RegexOption.IGNORE_CASE),
            new Regex("assetdelivery\\.roblox\\.com/v\\d+/assetId/(\\d+)", RegexOption.IGNORE_CASE),
            new Regex("rbxthumb://[^\\s]*?[?&]id=(\\d+)", RegexOption.IGNORE_CASE),
            new Regex("rbxthumb://.*id=(\\d+)", RegexOption.IGNORE_CASE),
            new Regex("[?&]id=(\\d+)", RegexOption.IGNORE_CASE),
            new Regex("roblox\\.com/library/(\\d+)", RegexOption.IGNORE_CASE),
            new Regex("create\\.roblox\\.com/store/asset/(\\d+)", RegexOption.IGNORE_CASE),
            new Regex("^\\s*(\\d+)\\s*$")
        });
        idMemo = new ConcurrentHashMap<>();
        keyMemo = new ConcurrentHashMap<>();
    }

    public final CopyOnWriteArrayList<Function1<String, Unit>> getListeners() {
        return listeners;
    }

    public final void init(Context ctx) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        if (appCtx == null) {
            appCtx = ctx.getApplicationContext();
        }
        if (cacheDir == null) {
            File file = new File(ctx.getCacheDir(), "img");
            file.mkdirs();
            cacheDir = file;
        }
    }

    private final String unwrapContentString(String content) {
        if (content == null) {
            return null;
        }
        String trimmed = content.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.startsWith("ContentValue(") && trimmed.endsWith(")")) {
            int idx = trimmed.indexOf("uri=");
            if (idx >= 0) {
                String inner = trimmed.substring(idx + 4, trimmed.length() - 1).trim();
                int comma = inner.indexOf(',');
                if (comma >= 0) {
                    inner = inner.substring(0, comma).trim();
                }
                if (inner.equals("null") || inner.isEmpty()) {
                    return null;
                }
                return inner;
            }
        }
        return trimmed;
    }

    public final Long assetId(Object content) {
        if (content == null) {
            return null;
        }
        String raw = content instanceof ContentValue ? ((ContentValue) content).getUri() : content.toString();
        return assetId(raw);
    }

    public final Long assetId(String content) {
        String str = unwrapContentString(content);
        if (str == null || StringsKt.isBlank(str)) {
            return null;
        }
        String low = str.toLowerCase(Locale.ROOT);
        if (low.contains("spawnlocation.png")) {
            return Long.valueOf(12224170L);
        }
        if (low.endsWith("/face.png") || low.equals("rbxasset://textures/face.png")) {
            return Long.valueOf(7075502L);
        }
        Long l = idMemo.get(str);
        if (l != null) {
            long jLongValue = l.longValue();
            if (jLongValue > 0L) {
                return Long.valueOf(jLongValue);
            }
            return null;
        }
        Long id = parseId(str);
        if (id != null && id.longValue() <= 0L) {
            id = null;
        }
        if (idMemo.size() > 50000) {
            idMemo.clear();
        }
        idMemo.put(str, Long.valueOf(id != null ? id.longValue() : -1L));
        return id;
    }

    private final Long parseId(String content) {
        if (StringsKt.startsWith(content, "rbxasset://", true) && !StringsKt.startsWith(content, "rbxassetid://", true)) {
            return null;
        }
        for (Regex r : idRx) {
            MatchResult matchResult = r.find(content, 0);
            if (matchResult != null) {
                return StringsKt.toLongOrNull(matchResult.getGroupValues().get(1));
            }
        }
        return null;
    }

    public final String keyFor(String content) {
        String strPutIfAbsent;
        String str = unwrapContentString(content);
        if (str == null || StringsKt.isBlank(str)) {
            return null;
        }
        Long lAssetId = assetId(str);
        if (lAssetId == null) {
            if (StringsKt.startsWith(str, "http://", true) || StringsKt.startsWith(str, "https://", true)) {
                return "url:" + str;
            }
            return null;
        }
        long jLongValue = lAssetId.longValue();
        ConcurrentMap<Long, String> concurrentMap = keyMemo;
        Long lValueOf = Long.valueOf(jLongValue);
        String str2 = concurrentMap.get(lValueOf);
        if (str2 == null && (strPutIfAbsent = concurrentMap.putIfAbsent(lValueOf, (str2 = "asset:" + jLongValue))) != null) {
            str2 = strPutIfAbsent;
        }
        return str2;
    }

    public final Bitmap get(Object content) {
        if (content == null) {
            return null;
        }
        String raw = content instanceof ContentValue ? ((ContentValue) content).getUri() : content.toString();
        return get(raw);
    }

    public final Bitmap get(String content) {
        String key = keyFor(content);
        if (key == null) {
            return null;
        }
        return getKey(key);
    }

    public final Bitmap getKey(final String key) {
        Intrinsics.checkNotNullParameter(key, "key");
        Bitmap bitmap = mem.get(key);
        if (bitmap != null) {
            return bitmap;
        }
        Long f = failed.get(key);
        if ((f != null && System.currentTimeMillis() - f.longValue() < 45000) || !pending.add(key)) {
            return null;
        }
        pool.execute(new Runnable() {
            @Override
            public final void run() {
                try {
                    ImageLoader.INSTANCE.loadFromDisk(key);
                } catch (Exception _e) {
                    ImageLoader.INSTANCE.fail(key);
                }
            }
        });
        return null;
    }

    public final Bitmap peekKey(String key) {
        Intrinsics.checkNotNullParameter(key, "key");
        return mem.get(key);
    }

    public final void notifyAssetLoaded(long id, Bitmap b) {
        if (b == null || id <= 0L) {
            return;
        }
        String key = "asset:" + id;
        keyMemo.putIfAbsent(Long.valueOf(id), key);
        done(key, b);
    }

    private final File diskFile(String key) {
        try {
            File dir = cacheDir;
            if (dir == null) {
                return null;
            }
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = key.getBytes(Charsets.UTF_8);
            Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
            byte[] bArrDigest = messageDigest.digest(bytes);
            Intrinsics.checkNotNullExpressionValue(bArrDigest, "digest(...)");
            String h = ArraysKt.joinToString(bArrDigest, (CharSequence) "", "", "", -1, "...", new Function1() {
                @Override
                public final Object invoke(Object obj) {
                    return ImageLoader.diskFile$lambda$7(((Byte) obj).byteValue());
                }
            });
            return new File(dir, h + ".png");
        } catch (Throwable _ktEx) {
            throw com.goydevv.studrblx.util.KtCompat.sneakyThrow(_ktEx);
        }
    }

    static final CharSequence diskFile$lambda$7(byte it) {
        String str = String.format("%02x", Arrays.copyOf(new Object[]{Byte.valueOf(it)}, 1));
        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
        return str;
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

    private final void saveToDisk(String key, Bitmap bmp, byte[] rawBytes) {
        File f = diskFile(key);
        if (f == null) {
            return;
        }
        try {
            if (rawBytes != null && rawBytes.length > 0) {
                FilesKt.writeBytes(f, rawBytes);
            } else if (bmp != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
                FilesKt.writeBytes(f, baos.toByteArray());
            }
        } catch (Exception ignored) {
        }
    }

    public final void loadFromDisk(String key) {
        try {
            Bitmap b;
            File f = diskFile(key);
            if (f != null && f.isFile()) {
                try {
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    b = BitmapFactory.decodeFile(f.getPath(), opts);
                } catch (Throwable th) {
                    b = null;
                }
                if (b != null) {
                    done(key, b);
                    return;
                }
            }
            if (StringsKt.startsWith(key, "asset:", false)) {
                long id = Long.parseLong(StringsKt.removePrefix(key, (CharSequence) "asset:"));
                // Primary path: authentic Roblox AssetDelivery with Roblox-Place-Id + auth cookie + XML/Decal unwrap
                Bitmap direct = AssetFetcher.INSTANCE.imageBlocking(id);
                if (direct != null) {
                    saveToDisk(key, direct, null);
                    done(key, direct);
                    return;
                }
                // Fallback path: Roblox Thumbnails API batch
                idQueue.add(Long.valueOf(id));
                scheduleBatch();
                return;
            }
            if (StringsKt.startsWith(key, "url:", false)) {
                String rawUrl = StringsKt.removePrefix(key, (CharSequence) "url:");
                Long maybeId = assetId(rawUrl);
                if (maybeId != null) {
                    Bitmap direct = AssetFetcher.INSTANCE.imageBlocking(maybeId.longValue());
                    if (direct != null) {
                        saveToDisk(key, direct, null);
                        done(key, direct);
                        return;
                    }
                }
                fetchUrl(key, rawUrl);
                return;
            }
            fail(key);
        } catch (Throwable _ktEx) {
            fail(key);
        }
    }

    private final void scheduleBatch() {
        if (batchScheduled) {
            return;
        }
        batchScheduled = true;
        pool.execute(new Runnable() {
            @Override
            public final void run() {
                try {
                    ImageLoader.scheduleBatch$lambda$9();
                } catch (Exception ignored) {
                    batchScheduled = false;
                }
            }
        });
    }

    static final void scheduleBatch$lambda$9() {
        try {
            Thread.sleep(80L);
            batchScheduled = false;
            List<Long> listTake = CollectionsKt.take(CollectionsKt.toList(idQueue), 100);
            idQueue.removeAll(CollectionsKt.toSet(listTake));
            if (!idQueue.isEmpty()) {
                INSTANCE.scheduleBatch();
            }
            if (listTake.isEmpty()) {
                return;
            }
            try {
                Map<Long, String> mapThumbnailUrls = RobloxApi.INSTANCE.thumbnailUrls(listTake, "420x420");
                for (final Long id : listTake) {
                    final String u = mapThumbnailUrls.get(id);
                    if (u == null) {
                        INSTANCE.fail("asset:" + id);
                    } else if (u.length() == 0) {
                        pending.remove("asset:" + id);
                        failed.put("asset:" + id, Long.valueOf(System.currentTimeMillis() - 40000));
                    } else {
                        pool.execute(new Runnable() {
                            @Override
                            public final void run() {
                                ImageLoader.INSTANCE.fetchUrl("asset:" + id, u);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                for (Long id : listTake) {
                    INSTANCE.fail("asset:" + id);
                }
            }
        } catch (Throwable _ktEx) {
            batchScheduled = false;
        }
    }

    public final void fetchUrl(String key, String url) {
        String lowerCase;
        try {
            String host = new URL(url).getHost();
            Intrinsics.checkNotNullExpressionValue(host, "getHost(...)");
            lowerCase = host.toLowerCase(Locale.ROOT);
            Intrinsics.checkNotNullExpressionValue(lowerCase, "toLowerCase(...)");
        } catch (Exception e) {
            lowerCase = "";
        }
        try {
            if (!WebPermissions.INSTANCE.isRobloxHost(lowerCase)) {
                throw new IOException("not a Roblox image");
            }
            Map<String, String> headers = new LinkedHashMap<>();
            long placeId = RobloxApi.INSTANCE.getActivePlaceId();
            if (placeId > 0L) {
                headers.put("Roblox-Place-Id", String.valueOf(placeId));
            }
            byte[] bytes = Http.INSTANCE.get(url, headers, 8388608);
            Bitmap b = decodeBitmap(bytes);
            if (b == null) {
                throw new IOException("bad image");
            }
            saveToDisk(key, b, bytes);
            done(key, b);
            return;
        } catch (Exception ignored) {
        }
        fail(key);
    }

    public final int getEpoch() {
        return epoch;
    }

    private final void done(String key, Bitmap b) {
        mem.put(key, b);
        failed.remove(key);
        epoch++;
        pending.remove(key);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((Function1) it.next()).invoke(key);
        }
    }

    private final void fail(String key) {
        pending.remove(key);
        failed.put(key, Long.valueOf(System.currentTimeMillis()));
    }
}
