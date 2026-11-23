package com.goydevv.studrblx.ui;

import android.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import androidx.core.view.ViewCompat;
import com.goydevv.studrblx.core.PluginWidget;
import com.goydevv.studrblx.core.Settings;
import com.goydevv.studrblx.core.Studio;
import com.goydevv.studrblx.engine.PlaySession;
import com.goydevv.studrblx.lua.EnumItemV;
import com.goydevv.studrblx.lua.LuaRuntime;
import com.goydevv.studrblx.lua.LuaTypes;
import com.goydevv.studrblx.model.CFrame;
import com.goydevv.studrblx.model.Color3;
import com.goydevv.studrblx.model.ColorKey;
import com.goydevv.studrblx.model.ColorSequence;
import com.goydevv.studrblx.model.DataModel;
import com.goydevv.studrblx.model.TreeListener;
import com.goydevv.studrblx.model.EnumValue;
import com.goydevv.studrblx.model.Instance;
import com.goydevv.studrblx.model.NumberKey;
import com.goydevv.studrblx.model.NumberSequence;
import com.goydevv.studrblx.model.PartData;
import com.goydevv.studrblx.model.UDim;
import com.goydevv.studrblx.model.UDim2;
import com.goydevv.studrblx.model.Vector2;
import com.goydevv.studrblx.model.Vector3;
import com.goydevv.studrblx.net.ImageLoader;
import com.goydevv.studrblx.render.FlyCamera;
import com.goydevv.studrblx.render.ViewState;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import kotlin.Pair;
import kotlin.Triple;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.comparisons.ComparisonsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlin.ranges.RangesKt;
import kotlin.sequences.SequencesKt;
import kotlin.text.Regex;
import kotlin.text.StringsKt;
import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaNumber;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

/* JADX INFO: compiled from: GuiOverlay.kt */
/* JADX INFO: loaded from: classes5.dex */
public final class GuiOverlay extends View {
    private static final Regex WS_REGEX = new Regex("\\s+");
    public static final int BUTTON = 0;
    public static final int CORE_MENU = 3;
    public static final int SINK = 2;
    public static final int TEXTBOX = 1;
    public static final float TOPBAR_INSET = 58.0f;
    public static final int WIDGET_CLOSE = 4;
    private final ArrayList<Pair<Instance, String>> absChanged;
    private float absInset;
    private int absRounds;
    private float absUnit;
    private long billboardScan;
    private List<? extends Instance> billboards;
    private IdentityHashMap<Instance, EditBox> boxes;
    private final float d;
    private final Paint fill;
    private volatile List<float[]> guides;
    private volatile boolean hasContent;
    private ArrayList<Hit> hits;
    private final Paint imgPaint;
    private Vector3 lastPointer;
    private final LinkedHashMap<TKey, StaticLayout> layouts;
    private IdentityHashMap<Instance, EditBox> newBoxes;
    private ArrayList<Hit> newHits;
    private ArrayList<Pair<Instance, RectF>> newPicks;
    private ArrayList<Scroll> newScrolls;
    private Function2<? super Float, ? super Float, Unit> onCoreMenu;
    private ArrayList<Pair<Instance, RectF>> picks;
    private final IdentityHashMap<Hit, List<Hit>> pressExtras;
    private Instance pressed;
    private LuaRuntime rt;
    private final LinkedHashMap<SKey, Float> scaledSizes;
    private ArrayList<Scroll> scrolls;
    private final Paint stroke;
    private final Studio studio;
    private final TextPaint textPaint;
    private int safeL;
    private int safeT;
    private int safeR;
    private int safeB;
    private Hit hoveredHit;
    private boolean registerHits = true;
    private volatile boolean dirty = true;
    private DataModel attachedModel;
    private final TreeListener dmListener;
    private final Studio.Listener studioListener;

    /* JADX INFO: compiled from: GuiOverlay.kt */
    public static final class Hit {
        private final Instance inst;
        private final int kind;
        private final RectF rect;
        private final LuaRuntime rt;

        public Hit(Instance inst, RectF rect, LuaRuntime rt, int kind) {
            Intrinsics.checkNotNullParameter(inst, "inst");
            Intrinsics.checkNotNullParameter(rect, "rect");
            this.inst = inst;
            this.rect = rect;
            this.rt = rt;
            this.kind = kind;
        }

        public final Instance getInst() {
            return this.inst;
        }

        public final int getKind() {
            return this.kind;
        }

        public final RectF getRect() {
            return this.rect;
        }

        public final LuaRuntime getRt() {
            return this.rt;
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    /* JADX WARN: Type inference failed for: r0v21, types: [com.goydevv.studrblx.ui.GuiOverlay$layouts$1] */
    /* JADX WARN: Type inference failed for: r0v22, types: [com.goydevv.studrblx.ui.GuiOverlay$scaledSizes$1] */
    public GuiOverlay(Context ctx, Studio studio) {
        super(ctx);
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        Intrinsics.checkNotNullParameter(studio, "studio");
        this.studio = studio;
        this.fill = new Paint(1);
        Paint paint = new Paint(1);
        paint.setStyle(Paint.Style.STROKE);
        this.stroke = paint;
        this.imgPaint = new Paint(3);
        this.textPaint = new TextPaint(1);
        this.d = getResources().getDisplayMetrics().density;
        this.hits = new ArrayList<>();
        this.scrolls = new ArrayList<>();
        this.picks = new ArrayList<>();
        this.newHits = new ArrayList<>();
        this.newScrolls = new ArrayList<>();
        this.newPicks = new ArrayList<>();
        this.boxes = new IdentityHashMap<>();
        this.newBoxes = new IdentityHashMap<>();
        this.guides = CollectionsKt.emptyList();
        this.billboards = CollectionsKt.emptyList();
        ImageLoader.INSTANCE.getListeners().add(new Function1() { // from class: com.goydevv.studrblx.ui.GuiOverlay$$ExternalSyntheticLambda2
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return GuiOverlay._init_$lambda$2(GuiOverlay.this, (String) obj);
            }
        });
        this.layouts = new LinkedHashMap<TKey, StaticLayout>() { // from class: com.goydevv.studrblx.ui.GuiOverlay$layouts$1
            @Override // java.util.LinkedHashMap
            protected boolean removeEldestEntry(Map.Entry<GuiOverlay.TKey, StaticLayout> eldest) {
                return size() > 500;
            }
        };
        this.scaledSizes = new LinkedHashMap<SKey, Float>() { // from class: com.goydevv.studrblx.ui.GuiOverlay$scaledSizes$1
            @Override // java.util.LinkedHashMap
            protected boolean removeEldestEntry(Map.Entry<GuiOverlay.SKey, Float> eldest) {
                return size() > 400;
            }
        };
        this.absInset = 58.0f;
        this.absChanged = new ArrayList<>();
        this.pressExtras = new IdentityHashMap<>();
        setWillNotDraw(false);
        setVisibility(View.VISIBLE);
        this.dmListener = new TreeListener() {
            @Override
            public void childAdded(Instance parent, Instance child) {
                if (GuiOverlay.isGuiRelevant(parent) || GuiOverlay.isGuiRelevant(child)) {
                    if (child != null && ("BillboardGui".equals(child.getClassName()) || "SurfaceGui".equals(child.getClassName()))) {
                        GuiOverlay.this.billboardScan = 0L;
                    }
                    GuiOverlay.this.dirty = true;
                    GuiOverlay.this.postInvalidate();
                }
            }

            @Override
            public void childRemoved(Instance parent, Instance child) {
                if (GuiOverlay.isGuiRelevant(parent) || GuiOverlay.isGuiRelevant(child)) {
                    if (child != null && ("BillboardGui".equals(child.getClassName()) || "SurfaceGui".equals(child.getClassName()))) {
                        GuiOverlay.this.billboardScan = 0L;
                    }
                    GuiOverlay.this.dirty = true;
                    GuiOverlay.this.postInvalidate();
                }
            }

            @Override
            public void propChanged(Instance inst, String name) {
                if (GuiOverlay.isGuiRelevant(inst)) {
                    GuiOverlay.this.dirty = true;
                    GuiOverlay.this.postInvalidate();
                }
            }
        };
        this.studioListener = new Studio.Listener() {
            @Override
            public void onTreeChanged() {
                GuiOverlay.this.billboardScan = 0L;
                GuiOverlay.this.dirty = true;
                GuiOverlay.this.postInvalidate();
            }

            @Override
            public void onPropsChanged() {
                GuiOverlay.this.dirty = true;
                GuiOverlay.this.postInvalidate();
            }

            @Override
            public void onModelReplaced() {
                GuiOverlay.this.attachModelListener();
                GuiOverlay.this.billboardScan = 0L;
                GuiOverlay.this.dirty = true;
                GuiOverlay.this.postInvalidate();
            }

            @Override
            public void onPlayStateChanged() {
                GuiOverlay.this.attachModelListener();
                GuiOverlay.this.billboardScan = 0L;
                GuiOverlay.this.hoveredHit = null;
                GuiOverlay.this.pressed = null;
                GuiOverlay.this.dirty = true;
                GuiOverlay.this.postInvalidate();
            }

            @Override
            public void onSelectionChanged() {
                GuiOverlay.this.dirty = true;
                GuiOverlay.this.postInvalidate();
            }

            @Override
            public void onPluginsChanged() {
                GuiOverlay.this.dirty = true;
                GuiOverlay.this.postInvalidate();
            }
        };
        this.studio.getListeners().add(this.studioListener);
        attachModelListener();
        setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public android.view.WindowInsets onApplyWindowInsets(View v, android.view.WindowInsets insets) {
                int cL = 0, cT = 0, cR = 0, cB = 0;
                if (android.os.Build.VERSION.SDK_INT >= 28 && insets != null && insets.getDisplayCutout() != null) {
                    cL = insets.getDisplayCutout().getSafeInsetLeft();
                    cT = insets.getDisplayCutout().getSafeInsetTop();
                    cR = insets.getDisplayCutout().getSafeInsetRight();
                    cB = insets.getDisplayCutout().getSafeInsetBottom();
                }
                if (cL != GuiOverlay.this.safeL || cT != GuiOverlay.this.safeT || cR != GuiOverlay.this.safeR || cB != GuiOverlay.this.safeB) {
                    GuiOverlay.this.safeL = cL;
                    GuiOverlay.this.safeT = cT;
                    GuiOverlay.this.safeR = cR;
                    GuiOverlay.this.safeB = cB;
                    GuiOverlay.this.dirty = true;
                    GuiOverlay.this.postInvalidate();
                }
                return insets;
            }
        });
    }

    private static boolean isGuiRelevant(Instance inst) {
        if (inst == null) {
            return false;
        }
        String cn = inst.getClassName();
        return inst.isA("GuiBase")
                || inst.isA("UIBase")
                || "StarterGui".equals(cn)
                || "PlayerGui".equals(cn)
                || "CoreGui".equals(cn)
                || "Folder".equals(cn)
                || "GuiService".equals(cn);
    }

    private void attachModelListener() {
        DataModel current = this.studio.getModel();
        if (this.attachedModel == current) {
            return;
        }
        if (this.attachedModel != null) {
            this.attachedModel.getListeners().remove(this.dmListener);
        }
        this.attachedModel = current;
        if (current != null && !current.getListeners().contains(this.dmListener)) {
            current.getListeners().add(this.dmListener);
        }
    }

    public final boolean isDirty() {
        return this.dirty;
    }

    public final void markDirty() {
        this.dirty = true;
        postInvalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.studio.getListeners().contains(this.studioListener)) {
            this.studio.getListeners().add(this.studioListener);
        }
        attachModelListener();
        this.dirty = true;
        postInvalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        this.studio.getListeners().remove(this.studioListener);
        if (this.attachedModel != null) {
            this.attachedModel.getListeners().remove(this.dmListener);
            this.attachedModel = null;
        }
        super.onDetachedFromWindow();
    }

    /* JADX INFO: compiled from: GuiOverlay.kt */
    public static final class Scroll {
        private final Instance inst;
        private final float maxX;
        private final float maxY;
        private final RectF rect;
        private final float unit;

        public Scroll(Instance inst, RectF rect, float maxX, float maxY, float unit) {
            Intrinsics.checkNotNullParameter(inst, "inst");
            Intrinsics.checkNotNullParameter(rect, "rect");
            this.inst = inst;
            this.rect = rect;
            this.maxX = maxX;
            this.maxY = maxY;
            this.unit = unit;
        }

        public final Instance getInst() {
            return this.inst;
        }

        public final float getMaxX() {
            return this.maxX;
        }

        public final float getMaxY() {
            return this.maxY;
        }

        public final RectF getRect() {
            return this.rect;
        }

        public final float getUnit() {
            return this.unit;
        }
    }

    public final boolean getHasContent() {
        return this.hasContent;
    }

    public final void setHasContent(boolean z) {
        this.hasContent = z;
    }

    /* JADX INFO: compiled from: GuiOverlay.kt */
    public static final class EditBox {
        private final RectF area;
        private final boolean inLayout;
        private final RectF rect;
        private final float scale;

        public EditBox(RectF rect, RectF area, float scale, boolean inLayout) {
            Intrinsics.checkNotNullParameter(rect, "rect");
            Intrinsics.checkNotNullParameter(area, "area");
            this.rect = rect;
            this.area = area;
            this.scale = scale;
            this.inLayout = inLayout;
        }

        public final RectF getArea() {
            return this.area;
        }

        public final boolean getInLayout() {
            return this.inLayout;
        }

        public final RectF getRect() {
            return this.rect;
        }

        public final float getScale() {
            return this.scale;
        }
    }

    public final List<float[]> getGuides() {
        return this.guides;
    }

    public final void setGuides(List<float[]> list) {
        Intrinsics.checkNotNullParameter(list, "<set-?>");
        this.guides = list;
    }

    public final Instance getSelectedGui() {
        Object obj = null;
        if (this.studio.isPlaying()) {
            return null;
        }
        for (Object obj2 : this.studio.getSelection()) {
            if (((Instance) obj2).isA("GuiObject")) {
                obj = obj2;
                break;
            }
        }
        return (Instance) obj;
    }

    public final void setSelectedGui(Instance instance) {
    }

    public final Instance getPressed() {
        return this.pressed;
    }

    public final void setPressed(Instance instance) {
        this.pressed = instance;
    }

    public final Function2<? super Float, ? super Float, Unit> getOnCoreMenu() {
        return this.onCoreMenu;
    }

    public final void setOnCoreMenu(Function2<? super Float, ? super Float, Unit> function2) {
        this.onCoreMenu = function2;
    }

    static final Unit _init_$lambda$2(GuiOverlay this$0, String it) {
        Intrinsics.checkNotNullParameter(it, "it");
        this$0.postInvalidate();
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: compiled from: GuiOverlay.kt */
    static final /* data */ class TKey {
        private final Layout.Alignment align;
        private final boolean bold;
        private final float lineHeight;
        private final boolean rich;
        private final float size;
        private final String text;
        private final Typeface tf;
        private final boolean truncate;
        private final int width;

        public static /* synthetic */ TKey copy$default(TKey tKey, String str, boolean z, Typeface typeface, boolean z2, float f, int i, Layout.Alignment alignment, float f2, boolean z3, int i2, Object obj) {
            if ((i2 & 1) != 0) {
                str = tKey.text;
            }
            if ((i2 & 2) != 0) {
                z = tKey.rich;
            }
            if ((i2 & 4) != 0) {
                typeface = tKey.tf;
            }
            if ((i2 & 8) != 0) {
                z2 = tKey.bold;
            }
            if ((i2 & 16) != 0) {
                f = tKey.size;
            }
            if ((i2 & 32) != 0) {
                i = tKey.width;
            }
            if ((i2 & 64) != 0) {
                alignment = tKey.align;
            }
            if ((i2 & 128) != 0) {
                f2 = tKey.lineHeight;
            }
            if ((i2 & 256) != 0) {
                z3 = tKey.truncate;
            }
            float f3 = f2;
            boolean z4 = z3;
            int i3 = i;
            Layout.Alignment alignment2 = alignment;
            float f4 = f;
            Typeface typeface2 = typeface;
            return tKey.copy(str, z, typeface2, z2, f4, i3, alignment2, f3, z4);
        }

        /* JADX INFO: renamed from: component1, reason: from getter */
        public final String component1() {
            return this.text;
        }

        /* JADX INFO: renamed from: component2, reason: from getter */
        public final boolean component2() {
            return this.rich;
        }

        /* JADX INFO: renamed from: component3, reason: from getter */
        public final Typeface component3() {
            return this.tf;
        }

        /* JADX INFO: renamed from: component4, reason: from getter */
        public final boolean component4() {
            return this.bold;
        }

        /* JADX INFO: renamed from: component5, reason: from getter */
        public final float component5() {
            return this.size;
        }

        /* JADX INFO: renamed from: component6, reason: from getter */
        public final int component6() {
            return this.width;
        }

        /* JADX INFO: renamed from: component7, reason: from getter */
        public final Layout.Alignment component7() {
            return this.align;
        }

        /* JADX INFO: renamed from: component8, reason: from getter */
        public final float component8() {
            return this.lineHeight;
        }

        /* JADX INFO: renamed from: component9, reason: from getter */
        public final boolean component9() {
            return this.truncate;
        }

        public final TKey copy(String text, boolean rich, Typeface tf, boolean bold, float size, int width, Layout.Alignment align, float lineHeight, boolean truncate) {
            Intrinsics.checkNotNullParameter(text, "text");
            Intrinsics.checkNotNullParameter(tf, "tf");
            Intrinsics.checkNotNullParameter(align, "align");
            return new TKey(text, rich, tf, bold, size, width, align, lineHeight, truncate);
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof TKey)) {
                return false;
            }
            TKey tKey = (TKey) other;
            return Intrinsics.areEqual(this.text, tKey.text) && this.rich == tKey.rich && Intrinsics.areEqual(this.tf, tKey.tf) && this.bold == tKey.bold && Float.compare(this.size, tKey.size) == 0 && this.width == tKey.width && this.align == tKey.align && Float.compare(this.lineHeight, tKey.lineHeight) == 0 && this.truncate == tKey.truncate;
        }

        public int hashCode() {
            return (((((((((((((((this.text.hashCode() * 31) + Boolean.hashCode(this.rich)) * 31) + this.tf.hashCode()) * 31) + Boolean.hashCode(this.bold)) * 31) + Float.hashCode(this.size)) * 31) + Integer.hashCode(this.width)) * 31) + this.align.hashCode()) * 31) + Float.hashCode(this.lineHeight)) * 31) + Boolean.hashCode(this.truncate);
        }

        public String toString() {
            return "TKey(text=" + this.text + ", rich=" + this.rich + ", tf=" + this.tf + ", bold=" + this.bold + ", size=" + this.size + ", width=" + this.width + ", align=" + this.align + ", lineHeight=" + this.lineHeight + ", truncate=" + this.truncate + ")";
        }

        public TKey(String text, boolean rich, Typeface tf, boolean bold, float size, int width, Layout.Alignment align, float lineHeight, boolean truncate) {
            Intrinsics.checkNotNullParameter(text, "text");
            Intrinsics.checkNotNullParameter(tf, "tf");
            Intrinsics.checkNotNullParameter(align, "align");
            this.text = text;
            this.rich = rich;
            this.tf = tf;
            this.bold = bold;
            this.size = size;
            this.width = width;
            this.align = align;
            this.lineHeight = lineHeight;
            this.truncate = truncate;
        }

        public final boolean getBold() {
            return this.bold;
        }

        public final boolean getRich() {
            return this.rich;
        }

        public final float getSize() {
            return this.size;
        }

        public final String getText() {
            return this.text;
        }

        public final Typeface getTf() {
            return this.tf;
        }

        public final int getWidth() {
            return this.width;
        }

        public final Layout.Alignment getAlign() {
            return this.align;
        }

        public final float getLineHeight() {
            return this.lineHeight;
        }

        public final boolean getTruncate() {
            return this.truncate;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: compiled from: GuiOverlay.kt */
    static final /* data */ class SKey {
        private final boolean bold;
        private final int h;
        private final float lineHeight;
        private final float max;
        private final float min;
        private final boolean rich;
        private final String text;
        private final Typeface tf;
        private final int w;

        public static /* synthetic */ SKey copy$default(SKey sKey, String str, boolean z, Typeface typeface, boolean z2, int i, int i2, float f, float f2, float f3, int i3, Object obj) {
            if ((i3 & 1) != 0) {
                str = sKey.text;
            }
            if ((i3 & 2) != 0) {
                z = sKey.rich;
            }
            if ((i3 & 4) != 0) {
                typeface = sKey.tf;
            }
            if ((i3 & 8) != 0) {
                z2 = sKey.bold;
            }
            if ((i3 & 16) != 0) {
                i = sKey.w;
            }
            if ((i3 & 32) != 0) {
                i2 = sKey.h;
            }
            if ((i3 & 64) != 0) {
                f = sKey.max;
            }
            if ((i3 & 128) != 0) {
                f2 = sKey.min;
            }
            if ((i3 & 256) != 0) {
                f3 = sKey.lineHeight;
            }
            float f4 = f2;
            float f5 = f3;
            int i4 = i2;
            float f6 = f;
            int i5 = i;
            Typeface typeface2 = typeface;
            return sKey.copy(str, z, typeface2, z2, i5, i4, f6, f4, f5);
        }

        /* JADX INFO: renamed from: component1, reason: from getter */
        public final String component1() {
            return this.text;
        }

        /* JADX INFO: renamed from: component2, reason: from getter */
        public final boolean component2() {
            return this.rich;
        }

        /* JADX INFO: renamed from: component3, reason: from getter */
        public final Typeface component3() {
            return this.tf;
        }

        /* JADX INFO: renamed from: component4, reason: from getter */
        public final boolean component4() {
            return this.bold;
        }

        /* JADX INFO: renamed from: component5, reason: from getter */
        public final int component5() {
            return this.w;
        }

        /* JADX INFO: renamed from: component6, reason: from getter */
        public final int component6() {
            return this.h;
        }

        /* JADX INFO: renamed from: component7, reason: from getter */
        public final float component7() {
            return this.max;
        }

        /* JADX INFO: renamed from: component8, reason: from getter */
        public final float component8() {
            return this.min;
        }

        /* JADX INFO: renamed from: component9, reason: from getter */
        public final float component9() {
            return this.lineHeight;
        }

        public final SKey copy(String text, boolean rich, Typeface tf, boolean bold, int w, int h, float max, float min, float lineHeight) {
            Intrinsics.checkNotNullParameter(text, "text");
            Intrinsics.checkNotNullParameter(tf, "tf");
            return new SKey(text, rich, tf, bold, w, h, max, min, lineHeight);
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof SKey)) {
                return false;
            }
            SKey sKey = (SKey) other;
            return Intrinsics.areEqual(this.text, sKey.text) && this.rich == sKey.rich && Intrinsics.areEqual(this.tf, sKey.tf) && this.bold == sKey.bold && this.w == sKey.w && this.h == sKey.h && Float.compare(this.max, sKey.max) == 0 && Float.compare(this.min, sKey.min) == 0 && Float.compare(this.lineHeight, sKey.lineHeight) == 0;
        }

        public int hashCode() {
            return (((((((((((((((this.text.hashCode() * 31) + Boolean.hashCode(this.rich)) * 31) + this.tf.hashCode()) * 31) + Boolean.hashCode(this.bold)) * 31) + Integer.hashCode(this.w)) * 31) + Integer.hashCode(this.h)) * 31) + Float.hashCode(this.max)) * 31) + Float.hashCode(this.min)) * 31) + Float.hashCode(this.lineHeight);
        }

        public String toString() {
            return "SKey(text=" + this.text + ", rich=" + this.rich + ", tf=" + this.tf + ", bold=" + this.bold + ", w=" + this.w + ", h=" + this.h + ", max=" + this.max + ", min=" + this.min + ", lineHeight=" + this.lineHeight + ")";
        }

        public SKey(String text, boolean rich, Typeface tf, boolean bold, int w, int h, float max, float min, float lineHeight) {
            Intrinsics.checkNotNullParameter(text, "text");
            Intrinsics.checkNotNullParameter(tf, "tf");
            this.text = text;
            this.rich = rich;
            this.tf = tf;
            this.bold = bold;
            this.w = w;
            this.h = h;
            this.max = max;
            this.min = min;
            this.lineHeight = lineHeight;
        }

        public final boolean getBold() {
            return this.bold;
        }

        public final int getH() {
            return this.h;
        }

        public final float getLineHeight() {
            return this.lineHeight;
        }

        public final float getMax() {
            return this.max;
        }

        public final float getMin() {
            return this.min;
        }

        public final boolean getRich() {
            return this.rich;
        }

        public final String getText() {
            return this.text;
        }

        public final Typeface getTf() {
            return this.tf;
        }

        public final int getW() {
            return this.w;
        }
    }

    private final CharSequence content(String text, boolean rich) {
        return rich ? RichText.INSTANCE.parse(text) : text;
    }

    private final StaticLayout layoutFor(TKey k) {
        StaticLayout staticLayoutBuild;
        Map map = this.layouts;
        StaticLayout staticLayout = (StaticLayout) map.get(k);
        if (staticLayout == null) {
            TextPaint textPaint = new TextPaint(1);
            textPaint.setTypeface(k.getTf());
            textPaint.setTextSize(k.getSize());
            textPaint.setFakeBoldText(k.getBold());
            CharSequence charSequenceContent = content(k.getText(), k.getRich());
            if (k.getTruncate()) {
                CharSequence charSequenceEllipsize = TextUtils.ellipsize(charSequenceContent, textPaint, k.getWidth(), TextUtils.TruncateAt.END);
                Intrinsics.checkNotNullExpressionValue(charSequenceEllipsize, "ellipsize(...)");
                charSequenceContent = charSequenceEllipsize;
            }
            staticLayoutBuild = StaticLayout.Builder.obtain(charSequenceContent, 0, charSequenceContent.length(), textPaint, RangesKt.coerceAtLeast(k.getWidth(), 1)).setAlignment(k.getAlign()).setIncludePad(false).setLineSpacing(0.0f, k.getLineHeight()).build();
            Intrinsics.checkNotNullExpressionValue(staticLayoutBuild, "build(...)");
            map.put(k, staticLayoutBuild);
        } else {
            staticLayoutBuild = staticLayout;
        }
        return staticLayoutBuild;
    }

    private final float scaledSize(SKey k) {
        Float fValueOf;
        Map map = this.scaledSizes;
        Float f = (Float) map.get(k);
        if (f == null) {
            TextPaint textPaint = new TextPaint(1);
            textPaint.setTypeface(k.getTf());
            textPaint.setFakeBoldText(k.getBold());
            CharSequence charSequenceContent = content(k.getText(), k.getRich());
            Iterable iterableSplit = WS_REGEX.split(charSequenceContent.toString(), 0);
            Collection arrayList = new ArrayList();
            for (Object obj : iterableSplit) {
                if (((String) obj).length() > 0) {
                    arrayList.add(obj);
                }
            }
            List list = (List) arrayList;
            float fCoerceAtLeast = RangesKt.coerceAtLeast(k.getMin(), 1.0f);
            float fCoerceAtLeast2 = RangesKt.coerceAtLeast(k.getMax(), fCoerceAtLeast);
            if (!scaledSize$lambda$9$fits(textPaint, list, charSequenceContent, k, fCoerceAtLeast2)) {
                for (int i = 0; i < 12; i++) {
                    float f2 = (fCoerceAtLeast + fCoerceAtLeast2) / 2.0f;
                    if (scaledSize$lambda$9$fits(textPaint, list, charSequenceContent, k, f2)) {
                        fCoerceAtLeast = f2;
                    } else {
                        fCoerceAtLeast2 = f2;
                    }
                }
                fCoerceAtLeast2 = fCoerceAtLeast;
            }
            fValueOf = Float.valueOf(fCoerceAtLeast2);
            map.put(k, fValueOf);
        } else {
            fValueOf = f;
        }
        return fValueOf.floatValue();
    }

    private static final boolean scaledSize$lambda$9$fits(TextPaint p, List<String> list, CharSequence cs, SKey $k, float s) {
        boolean z;
        p.setTextSize(s);
        List<String> list2 = list;
        if (!(list2 instanceof Collection) || !list2.isEmpty()) {
            Iterator it = list2.iterator();
            while (true) {
                if (it.hasNext()) {
                    if (p.measureText((String) it.next()) > ((float) $k.getW())) {
                        z = true;
                        break;
                    }
                } else {
                    z = false;
                    break;
                }
            }
        } else {
            z = false;
        }
        if (z) {
            return false;
        }
        StaticLayout l = StaticLayout.Builder.obtain(cs, 0, cs.length(), p, RangesKt.coerceAtLeast($k.getW(), 1)).setIncludePad(false).setLineSpacing(0.0f, $k.getLineHeight()).build();
        Intrinsics.checkNotNullExpressionValue(l, "build(...)");
        return l.getHeight() <= $k.getH();
    }

    private final Object surfaceRenderLock = new Object();

    public final void renderSurface(Instance gui, float cw, float ch, Bitmap bmp) {
        synchronized (this.surfaceRenderLock) {
            renderSurfaceLocked(gui, cw, ch, bmp);
        }
    }

    private final void renderSurfaceLocked(Instance gui, float cw, float ch, Bitmap bmp) {
        Intrinsics.checkNotNullParameter(gui, "gui");
        Intrinsics.checkNotNullParameter(bmp, "bmp");
        bmp.eraseColor(0);
        if (Boolean.FALSE.equals(gui.get("Enabled"))) {
            return;
        }
        float canvasW = cw;
        float canvasH = ch;
        if (canvasW <= 0.0f || canvasH <= 0.0f) {
            Object csObj = gui.get("CanvasSize");
            Vector2 cs = csObj instanceof Vector2 ? (Vector2) csObj : null;
            canvasW = (cs != null && cs.getX() > 0.0f) ? cs.getX() : 800.0f;
            canvasH = (cs != null && cs.getY() > 0.0f) ? cs.getY() : 600.0f;
        }
        Canvas c = new Canvas(bmp);
        c.scale(bmp.getWidth() / canvasW, bmp.getHeight() / canvasH);
        ArrayList<Hit> savedHits = this.newHits;
        ArrayList<Scroll> savedScrolls = this.newScrolls;
        ArrayList<Pair<Instance, RectF>> savedPicks = this.newPicks;
        LuaRuntime sr = this.rt;
        boolean savedRegHits = this.registerHits;
        this.newHits = new ArrayList<>();
        this.newScrolls = new ArrayList<>();
        this.newPicks = new ArrayList<>();
        this.registerHits = false;
        RectF area = new RectF(0.0f, 0.0f, canvasW, canvasH);
        float su = this.absUnit;
        float si = this.absInset;
        this.absUnit = 1.0f;
        this.absInset = 0.0f;
        try {
            setAbs(gui, area);
            float baseScale = this.d > 0.0f ? (1.0f / this.d) : 1.0f;
            drawLayerCollector(c, gui, area, baseScale, area);
        } finally {
            this.newHits = savedHits;
            this.newScrolls = savedScrolls;
            this.newPicks = savedPicks;
            this.rt = sr;
            this.absUnit = su;
            this.absInset = si;
            this.registerHits = savedRegHits;
        }
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        Intrinsics.checkNotNullParameter(canvas, "canvas");
        this.dirty = false;
        float width = getWidth();
        float height = getHeight();
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }
        synchronized (this.studio.getLock()) {
            attachModelListener();
            this.newHits = new ArrayList<>();
            this.newScrolls = new ArrayList<>();
            this.newPicks = new ArrayList<>();
            this.newBoxes = new IdentityHashMap<>();
            this.registerHits = true;
            PlaySession play = this.studio.getPlay();
            boolean playing = play != null;

            List<Instance> primaryChildren;
            if (playing) {
                Instance player = play.getPlayer();
                Instance playerGui = player != null ? Instance.findFirstChild$default(player, "PlayerGui", false, 2, null) : null;
                primaryChildren = (playerGui != null && playerGui.getChildren() != null) ? playerGui.getChildren() : CollectionsKt.emptyList();
            } else {
                Instance starterGui = this.studio.getModel().findService("StarterGui");
                boolean showDevGui = starterGui != null && !Boolean.FALSE.equals(starterGui.get("ShowDevelopmentGui"));
                primaryChildren = (showDevGui && starterGui.getChildren() != null) ? starterGui.getChildren() : CollectionsKt.emptyList();
            }

            LuaRuntime activeRt = (playing && play.getRt() != null) ? play.getRt() : this.studio.getEditRuntime();
            this.rt = activeRt;

            boolean hasAny = drawBillboards(canvas, playing);

            Instance coreGui = this.studio.getModel().findService("CoreGui");
            List<Instance> coreChildren = (coreGui != null && coreGui.getChildren() != null) ? coreGui.getChildren() : CollectionsKt.emptyList();

            List<Instance> screenGuis = GuiMath.filterAndSortScreenGuis(primaryChildren, coreChildren);
            RectF fullScreenClip = new RectF(0.0f, 0.0f, width, height);

            for (Instance sg : screenGuis) {
                boolean isCoreGui = false;
                for (Instance anc = sg.getParent(); anc != null; anc = anc.getParent()) {
                    if ("CoreGui".equals(anc.getClassName())) {
                        isCoreGui = true;
                        break;
                    }
                }
                boolean isPluginCore = isCoreGui && !playing;

                float[] rArr = GuiMath.computeScreenGuiRect(
                        sg, width, height, this.d,
                        this.safeL, this.safeT, this.safeR, this.safeB,
                        isCoreGui, playing
                );
                RectF rectF = new RectF(rArr[0], rArr[1], rArr[2], rArr[3]);

                if (isPluginCore) {
                    usePluginUnits();
                } else {
                    useGameUnits();
                }
                setAbs(sg, rectF);

                LuaRuntime savedRt = this.rt;
                if (isPluginCore) {
                    this.rt = this.studio.getEditRuntime();
                }
                float sgScale = isPluginCore ? pluginScale(height) : 1.0f;
                drawLayerCollector(canvas, sg, rectF, sgScale, fullScreenClip);
                this.rt = savedRt;
                hasAny = true;
            }

            useGameUnits();
            if (playing) {
                hasAny = true;
            }
            if (!playing) {
                hasAny = drawPluginWidgets(canvas, width, height) | hasAny;
            }

            Instance selectedGui = getSelectedGui();
            if (selectedGui != null) {
                RectF selRect = null;
                for (Pair<Instance, RectF> pair : this.newPicks) {
                    if (pair.getFirst() == selectedGui) {
                        selRect = pair.getSecond();
                        break;
                    }
                }
                if (selRect != null) {
                    this.stroke.setColor(-15096065);
                    this.stroke.setStrokeWidth(this.d * 2.0f);
                    this.stroke.setShader(null);
                    canvas.drawRect(selRect, this.stroke);
                    float hs = this.d * 4.0f;
                    for (float[] pt : handlePoints(selRect)) {
                        this.fill.setShader(null);
                        this.fill.setColor(-1);
                        canvas.drawRect(pt[0] - hs, pt[1] - hs, pt[0] + hs, pt[1] + hs, this.fill);
                        canvas.drawRect(pt[0] - hs, pt[1] - hs, pt[0] + hs, pt[1] + hs, this.stroke);
                    }
                    hasAny = true;
                }
            }

            List<float[]> gList = this.guides;
            if (!gList.isEmpty()) {
                this.stroke.setColor(-49766);
                this.stroke.setStrokeWidth(this.d * 1.5f);
                this.stroke.setShader(null);
                for (float[] g : gList) {
                    canvas.drawLine(g[0], g[1], g[2], g[3], this.stroke);
                }
            }

            this.hits = this.newHits;
            this.scrolls = this.newScrolls;
            this.picks = this.newPicks;
            this.boxes = this.newBoxes;
            fireAbsChanges();
            this.hasContent = hasAny;
        }
    }

    public final List<float[]> handlePoints(RectF r) {
        Intrinsics.checkNotNullParameter(r, "r");
        return CollectionsKt.listOf(new float[][]{new float[]{r.left, r.top}, new float[]{r.centerX(), r.top}, new float[]{r.right, r.top}, new float[]{r.right, r.centerY()}, new float[]{r.right, r.bottom}, new float[]{r.centerX(), r.bottom}, new float[]{r.left, r.bottom}, new float[]{r.left, r.centerY()}});
    }

    public final EditBox editBox(Instance i) {
        Intrinsics.checkNotNullParameter(i, "i");
        return this.boxes.get(i);
    }

    public final Integer handleAt(Instance i, float x, float y) {
        Intrinsics.checkNotNullParameter(i, "i");
        EditBox b = this.boxes.get(i);
        if (b == null) {
            return null;
        }
        float tol = this.d * 16.0f;
        int i2 = 0;
        for (Object obj : handlePoints(b.getRect())) {
            int i3 = i2 + 1;
            if (i2 < 0) {
                CollectionsKt.throwIndexOverflow();
            }
            float[] fArr = (float[]) obj;
            if (Math.abs(fArr[0] - x) <= tol && Math.abs(fArr[1] - y) <= tol) {
                return Integer.valueOf(i2);
            }
            i2 = i3;
        }
        return (!b.getRect().contains(x, y) || seeThrough(i)) ? null : -1;
    }

    public final boolean seeThrough(Instance c) {
        String string;
        boolean z;
        Intrinsics.checkNotNullParameter(c, "c");
        if (Intrinsics.areEqual(c.get("Visible"), (Object) false)) {
            return true;
        }
        if (num(c, "BackgroundTransparency", 0.0f) < 0.999f) {
            return false;
        }
        String img = "";
        if (c.isA("TextLabel") || c.isA("TextButton") || Intrinsics.areEqual(c.getClassName(), "TextBox")) {
            Object obj = c.get("Text");
            String text = obj instanceof String ? (String) obj : null;
            if (text == null) {
                text = "";
            }
            if (!StringsKt.isBlank(text) && num(c, "TextTransparency", 0.0f) < 0.999f) {
                return false;
            }
        }
        if (Intrinsics.areEqual(c.getClassName(), "ImageLabel") || Intrinsics.areEqual(c.getClassName(), "ImageButton")) {
            Object obj2 = c.get("Image");
            if (obj2 != null && (string = obj2.toString()) != null) {
                img = string;
            }
            if (!StringsKt.isBlank(img) && num(c, "ImageTransparency", 0.0f) < 0.999f) {
                return false;
            }
        }
        for (Instance instance : c.getChildren()) {
            if ("UIStroke".equals(instance.getClassName())
                    && !Boolean.FALSE.equals(instance.get("Enabled"))
                    && num(instance, "Transparency", 0.0f) < 0.999f) {
                return false;
            }
        }
        return true;
    }

    /* JADX INFO: compiled from: GuiOverlay.kt */
    public static final class DragState {
        private final Vector2 anchor;
        private final RectF area;
        private final int handle;
        private final boolean inLayout;
        private final Instance inst;
        private final UDim2 pos;
        private final UDim2 size;
        private final RectF start;
        private final float unit;

        public DragState(Instance inst, int handle, RectF start, RectF area, float unit, UDim2 pos, UDim2 size, Vector2 anchor, boolean inLayout) {
            Intrinsics.checkNotNullParameter(inst, "inst");
            Intrinsics.checkNotNullParameter(start, "start");
            Intrinsics.checkNotNullParameter(area, "area");
            Intrinsics.checkNotNullParameter(pos, "pos");
            Intrinsics.checkNotNullParameter(size, "size");
            Intrinsics.checkNotNullParameter(anchor, "anchor");
            this.inst = inst;
            this.handle = handle;
            this.start = start;
            this.area = area;
            this.unit = unit;
            this.pos = pos;
            this.size = size;
            this.anchor = anchor;
            this.inLayout = inLayout;
        }

        public final RectF getArea() {
            return this.area;
        }

        public final int getHandle() {
            return this.handle;
        }

        public final Instance getInst() {
            return this.inst;
        }

        public final RectF getStart() {
            return this.start;
        }

        public final float getUnit() {
            return this.unit;
        }

        public final Vector2 getAnchor() {
            return this.anchor;
        }

        public final boolean getInLayout() {
            return this.inLayout;
        }

        public final UDim2 getPos() {
            return this.pos;
        }

        public final UDim2 getSize() {
            return this.size;
        }
    }

    public final DragState beginDrag(Instance i, int handle) {
        Intrinsics.checkNotNullParameter(i, "i");
        synchronized (this.studio.getLock()) {
            EditBox editBox = this.boxes.get(i);
            if (editBox == null) {
                return null;
            }
            RectF rectF = new RectF(editBox.getRect());
            RectF rectF2 = new RectF(editBox.getArea());
            float scale = this.d * editBox.getScale();
            UDim2 uDim2Udim = udim(i.get("Position"));
            UDim2 uDim2Udim2 = udim(i.get("Size"));
            Object obj = i.get("AnchorPoint");
            Vector2 vector2 = obj instanceof Vector2 ? (Vector2) obj : null;
            if (vector2 == null) {
                vector2 = new Vector2(0.0f, 0.0f);
            }
            return new DragState(i, handle, rectF, rectF2, scale, uDim2Udim, uDim2Udim2, vector2, editBox.getInLayout());
        }
    }

    public final void dragTo(DragState s, float dx, float dy) {
        Intrinsics.checkNotNullParameter(s, "s");
        int h = s.getHandle();
        if (h == -1 && s.getInLayout()) {
            return;
        }
        float l = s.getStart().left;
        float t = s.getStart().top;
        float r = s.getStart().right;
        float b = s.getStart().bottom;
        if (h == -1) {
            l += dx;
            r += dx;
            t += dy;
            b += dy;
        } else {
            if (h == 0 || h == 6 || h == 7) l += dx;
            if (h == 2 || h == 3 || h == 4) r += dx;
            if (h == 0 || h == 1 || h == 2) t += dy;
            if (h == 4 || h == 5 || h == 6) b += dy;
            if (r - l < 1.0f) {
                if (h == 0 || h == 6 || h == 7) l = r - 1.0f;
                else r = l + 1.0f;
            }
            if (b - t < 1.0f) {
                if (h == 0 || h == 1 || h == 2) t = b - 1.0f;
                else b = t + 1.0f;
            }
        }

        ArrayList<float[]> gl = new ArrayList<>();
        RectF area = s.getArea();
        boolean moveX = (h == -1 || h == 0 || h == 2 || h == 3 || h == 4 || h == 6 || h == 7);
        boolean moveY = (h == -1 || h == 0 || h == 1 || h == 2 || h == 4 || h == 5 || h == 6);
        boolean snappedX = false;
        boolean snappedY = false;

        if (Settings.INSTANCE.getGuiSmartGuides()) {
            synchronized (this.studio.getLock()) {
                ArrayList<Float> targetsX = new ArrayList<>();
                targetsX.add(area.left);
                targetsX.add(area.centerX());
                targetsX.add(area.right);
                ArrayList<Float> targetsY = new ArrayList<>();
                targetsY.add(area.top);
                targetsY.add(area.centerY());
                targetsY.add(area.bottom);
                for (Map.Entry<Instance, EditBox> entry : this.boxes.entrySet()) {
                    Instance key = entry.getKey();
                    EditBox value = entry.getValue();
                    if (key != s.getInst() && key.getParent() == s.getInst().getParent()) {
                        targetsX.add(value.getRect().left);
                        targetsX.add(value.getRect().centerX());
                        targetsX.add(value.getRect().right);
                        targetsY.add(value.getRect().top);
                        targetsY.add(value.getRect().centerY());
                        targetsY.add(value.getRect().bottom);
                    }
                }
                float tol = this.d * 6.0f;
                if (moveX) {
                    List<Float> probesX;
                    if (h == -1) {
                        probesX = CollectionsKt.listOf(new Float[]{l, (l + r) * 0.5f, r});
                    } else if (h == 0 || h == 6 || h == 7) {
                        probesX = CollectionsKt.listOf(l);
                    } else {
                        probesX = CollectionsKt.listOf(r);
                    }
                    Pair<Float, Float> bestX = dragTo$lambda$23$best(tol, probesX, targetsX);
                    if (bestX != null) {
                        float diff = bestX.getFirst();
                        float guideX = bestX.getSecond();
                        if (h == -1) {
                            l += diff;
                            r += diff;
                        } else if (h == 0 || h == 6 || h == 7) {
                            l += diff;
                        } else {
                            r += diff;
                        }
                        gl.add(new float[]{guideX, area.top, guideX, area.bottom});
                        snappedX = true;
                    }
                }
                if (moveY) {
                    List<Float> probesY;
                    if (h == -1) {
                        probesY = CollectionsKt.listOf(new Float[]{t, (t + b) * 0.5f, b});
                    } else if (h == 0 || h == 1 || h == 2) {
                        probesY = CollectionsKt.listOf(t);
                    } else {
                        probesY = CollectionsKt.listOf(b);
                    }
                    Pair<Float, Float> bestY = dragTo$lambda$23$best(tol, probesY, targetsY);
                    if (bestY != null) {
                        float diff = bestY.getFirst();
                        float guideY = bestY.getSecond();
                        if (h == -1) {
                            t += diff;
                            b += diff;
                        } else if (h == 0 || h == 1 || h == 2) {
                            t += diff;
                        } else {
                            b += diff;
                        }
                        gl.add(new float[]{area.left, guideY, area.right, guideY});
                        snappedY = true;
                    }
                }
            }
        }

        int grid = Settings.INSTANCE.getGuiGrid();
        if (grid > 0) {
            float g = grid * s.getUnit();
            if (moveX && !snappedX) {
                if (h == -1) {
                    float nl = dragTo$snapV(g, l, area.left);
                    r += nl - l;
                    l = nl;
                } else if (h == 0 || h == 6 || h == 7) {
                    l = dragTo$snapV(g, l, area.left);
                } else {
                    r = dragTo$snapV(g, r, area.left);
                }
            }
            if (moveY && !snappedY) {
                if (h == -1) {
                    float nt = dragTo$snapV(g, t, area.top);
                    b += nt - t;
                    t = nt;
                } else if (h == 0 || h == 1 || h == 2) {
                    t = dragTo$snapV(g, t, area.top);
                } else {
                    b = dragTo$snapV(g, b, area.top);
                }
            }
        }

        this.guides = gl;
        float w = r - l;
        float hh = b - t;
        float aw = area.width();
        float ah = area.height();
        float u = s.getUnit();
        synchronized (this.studio.getLock()) {
            if (h != -1) {
                UDim2 newSize = new UDim2(
                        s.getSize().getXs(),
                        Math.round((w - (s.getSize().getXs() * aw)) / u),
                        s.getSize().getYs(),
                        Math.round((hh - (s.getSize().getYs() * ah)) / u)
                );
                if (!Intrinsics.areEqual(newSize, s.getInst().get("Size"))) {
                    Instance.set$default(s.getInst(), "Size", newSize, false, 4, null);
                }
            }
            if (!s.getInLayout()) {
                UDim2 newPos = new UDim2(
                        s.getPos().getXs(),
                        Math.round((((l + (s.getAnchor().getX() * w)) - area.left) - (s.getPos().getXs() * aw)) / u),
                        s.getPos().getYs(),
                        Math.round((((t + (s.getAnchor().getY() * hh)) - area.top) - (s.getPos().getYs() * ah)) / u)
                );
                if (!Intrinsics.areEqual(newPos, s.getInst().get("Position"))) {
                    Instance.set$default(s.getInst(), "Position", newPos, false, 4, null);
                }
            }
            postInvalidate();
        }
    }

    private static final Pair<Float, Float> dragTo$lambda$23$best(float tol, List<Float> list, List<Float> list2) {
        float bd = tol;
        Pair<Float, Float> pair = null;
        Iterator<Float> it = list.iterator();
        while (it.hasNext()) {
            float e = it.next().floatValue();
            Iterator<Float> it2 = list2.iterator();
            while (it2.hasNext()) {
                float c = it2.next().floatValue();
                float diff = c - e;
                if (Math.abs(diff) < bd) {
                    bd = Math.abs(diff);
                    pair = TuplesKt.to(Float.valueOf(diff), Float.valueOf(c));
                }
            }
        }
        return pair;
    }

    private static final float dragTo$snapV(float g, float v, float origin) {
        return (Math.round((v - origin) / g) * g) + origin;
    }

    public final void endDrag() {
        this.guides = CollectionsKt.emptyList();
        postInvalidate();
    }

    private final void useGameUnits() {
        this.absUnit = this.d;
        this.absInset = 58.0f;
    }

    private final void usePluginUnits() {
        this.absUnit = this.d * this.studio.editGuiScale();
        this.absInset = 0.0f;
    }

    private final void setAbs(Instance c, RectF r) {
        if (this.absUnit <= 0.0f) {
            useGameUnits();
        }
        float u = this.absUnit;
        float x = r.left / u;
        float y = (r.top / u) - this.absInset;
        float w = r.width() / u;
        float h = r.height() / u;
        boolean sizeChanged = Math.abs(c.getAbsW() - w) > 0.25f || Math.abs(c.getAbsH() - h) > 0.25f;
        boolean posChanged = Math.abs(c.getAbsX() - x) > 0.25f || Math.abs(c.getAbsY() - y) > 0.25f;
        c.setAbsX(x);
        c.setAbsY(y);
        c.setAbsW(w);
        c.setAbsH(h);
        if (sizeChanged) {
            this.absChanged.add(TuplesKt.to(c, "AbsoluteSize"));
        }
        if (posChanged) {
            this.absChanged.add(TuplesKt.to(c, "AbsolutePosition"));
        }
    }

    private final void setContentSize(Instance layout, float wPx, float hPx) {
        if (this.absUnit <= 0.0f) {
            useGameUnits();
        }
        float w = wPx / this.absUnit;
        float h = hPx / this.absUnit;
        if (Math.abs(layout.getAbsW() - w) > 0.25f || Math.abs(layout.getAbsH() - h) > 0.25f) {
            this.absChanged.add(TuplesKt.to(layout, "AbsoluteContentSize"));
        }
        layout.setAbsW(w);
        layout.setAbsH(h);
    }

    private final void fireAbsChanges() {
        if (this.absChanged.isEmpty()) {
            this.absRounds = 0;
            return;
        }
        ArrayList list = new ArrayList(this.absChanged);
        this.absChanged.clear();
        LuaRuntime[] luaRuntimeArr = new LuaRuntime[2];
        luaRuntimeArr[0] = this.studio.getEditRuntime();
        PlaySession play = this.studio.getPlay();
        luaRuntimeArr[1] = play != null ? play.getRt() : null;
        List<LuaRuntime> runtimes = CollectionsKt.distinct(CollectionsKt.listOfNotNull(luaRuntimeArr));
        Iterator it = list.iterator();
        Intrinsics.checkNotNullExpressionValue(it, "iterator(...)");
        while (it.hasNext()) {
            Pair pair = (Pair) it.next();
            Instance inst = (Instance) pair.component1();
            String prop = (String) pair.component2();
            for (LuaRuntime r : runtimes) {
                if (r.hasConn(inst, "Changed:" + prop)) {
                    r.fire(inst, "Changed:" + prop, new LuaValue[0]);
                }
                if (r.hasConn(inst, "Changed")) {
                    LuaString luaStringValueOf = LuaValue.valueOf(prop);
                    Intrinsics.checkNotNullExpressionValue(luaStringValueOf, "valueOf(...)");
                    r.fire(inst, "Changed", luaStringValueOf);
                }
            }
        }
        this.absRounds++;
        if (this.absRounds <= 30) {
            postInvalidate();
        }
    }

    private final void drawTopBar(Canvas canvas) {
        float r = this.d * 20.0f;
        float cx = (this.d * 12.0f) + r;
        float cy = (this.d * 8.0f) + r;
        this.fill.setShader(null);
        this.fill.setColor(-1946157056);
        canvas.drawCircle(cx, cy, r, this.fill);
        this.stroke.setColor(-1);
        this.stroke.setStrokeWidth(this.d * 2.6f);
        this.stroke.setShader(null);
        canvas.save();
        canvas.rotate(15.0f, cx, cy);
        float s = this.d * 8.0f;
        canvas.drawRoundRect(cx - s, cy - s, cx + s, cy + s, this.d * 2.0f, this.d * 2.0f, this.stroke);
        this.fill.setColor(-1);
        canvas.drawRect(cx - (this.d * 2.2f), cy - (this.d * 2.2f), (this.d * 2.2f) + cx, (this.d * 2.2f) + cy, this.fill);
        canvas.restore();
        this.newHits.add(new Hit(this.studio.getModel(), new RectF(cx - r, cy - r, cx + r, cy + r), null, 3));
        float cx2 = cx + (2.0f * r) + (this.d * 8.0f);
        this.fill.setColor(-1946157056);
        canvas.drawCircle(cx2, cy, r, this.fill);
        this.fill.setColor(-1);
        canvas.drawRoundRect(cx2 - (this.d * 9.0f), cy - (this.d * 7.0f), (this.d * 9.0f) + cx2, (this.d * 5.0f) + cy, this.d * 4.0f, this.d * 4.0f, this.fill);
        Path tail = new Path();
        tail.moveTo(cx2 - (this.d * 4.0f), (this.d * 4.0f) + cy);
        tail.lineTo(cx2 - (this.d * 6.0f), (this.d * 10.0f) + cy);
        tail.lineTo((this.d * 1.0f) + cx2, (this.d * 4.0f) + cy);
        tail.close();
        canvas.drawPath(tail, this.fill);
    }

    private final float pluginScale(float viewH) {
        return this.studio.editGuiScale();
    }

    private final boolean drawPluginWidgets(Canvas canvas, float w, float h) {
        boolean any = false;
        float x = 12.0f;
        float y = this.d * 12.0f;
        float ps = pluginScale(h);
        usePluginUnits();
        Iterator<PluginWidget> it = this.studio.getPluginWidgets().iterator();
        Intrinsics.checkNotNullExpressionValue(it, "iterator(...)");
        float y2 = y;
        while (it.hasNext()) {
            PluginWidget pw = it.next();
            Instance wi = pw.getInst();
            if (Intrinsics.areEqual(wi.get("Enabled"), (Object) true)) {
                Object obj = wi.getAttributes().get("__w");
                Number number = obj instanceof Number ? (Number) obj : null;
                float ww = RangesKt.coerceAtMost((number != null ? number.floatValue() : 300.0f) * ps, (w / this.d) * 0.6f) * this.d;
                Object obj2 = wi.getAttributes().get("__h");
                Number number2 = obj2 instanceof Number ? (Number) obj2 : null;
                float wh = RangesKt.coerceAtMost((number2 != null ? number2.floatValue() : 300.0f) * ps, (h / this.d) * 0.8f) * this.d;
                float x2 = (w - ww) - (this.d * x);
                this.fill.setShader(null);
                this.fill.setColor(-13750738);
                canvas.drawRect(x2, y2, x2 + ww, (this.d * 24.0f) + y2 + wh, this.fill);
                this.fill.setColor(Ui.INSTANCE.getHEADER());
                canvas.drawRect(x2, y2, x2 + ww, y2 + (this.d * 24.0f), this.fill);
                this.textPaint.setColor(Ui.INSTANCE.getTEXT());
                this.textPaint.setTextSize(this.d * 13.0f);
                this.textPaint.setTypeface(Typeface.DEFAULT);
                this.textPaint.setShader(null);
                Object obj3 = wi.get("Title");
                String name = obj3 instanceof String ? (String) obj3 : null;
                if (name == null) {
                    name = wi.getName();
                }
                float f = x;
                canvas.drawText(name, x2 + (this.d * 8.0f), (this.d * 17.0f) + y2, this.textPaint);
                this.stroke.setColor(Ui.INSTANCE.getTEXT());
                this.stroke.setStrokeWidth(this.d * 1.6f);
                this.stroke.setShader(null);
                float cx = (x2 + ww) - (this.d * 14.0f);
                float cy = y2 + (this.d * f);
                float k = this.d * 4.5f;
                float x3 = cx - k;
                float y3 = y2;
                float y4 = cy - k;
                float x4 = cx + k;
                float y5 = cy + k;
                canvas.drawLine(x3, y4, x4, y5, this.stroke);
                canvas.drawLine(cx + k, cy - k, cx - k, cy + k, this.stroke);
                ArrayList<Hit> arrayList = this.newHits;
                float ps2 = ps;
                float ps3 = this.d;
                arrayList.add(new Hit(wi, new RectF((x2 + ww) - (this.d * 28.0f), y3, x2 + ww, (ps3 * 24.0f) + y3), pw.getRt(), 4));
                RectF content = new RectF(x2, (this.d * 24.0f) + y3, x2 + ww, (this.d * 24.0f) + y3 + wh);
                canvas.save();
                canvas.clipRect(content);
                LuaRuntime saved = this.rt;
                this.rt = pw.getRt();
                setAbs(wi, new RectF(content));
                drawChildren(canvas, wi, content, ps2, content);
                this.rt = saved;
                canvas.restore();
                y2 = y3 + wh + (this.d * 36.0f);
                ps = ps2;
                any = true;
                x = f;
            }
        }
        useGameUnits();
        return any;
    }

    private final boolean drawBillboards(Canvas canvas, boolean playing) {
        long now = System.currentTimeMillis();
        if (now - this.billboardScan > 500) {
            this.billboardScan = now;
            ArrayList<Instance> found = new ArrayList<>();
            Instance ws = this.studio.getModel().findService("Workspace");
            if (ws != null) {
                for (Instance inst : Instance.descendants$default(ws, null, 1, null)) {
                    if ("BillboardGui".equals(inst.getClassName())) {
                        found.add(inst);
                    }
                }
            }
            PlaySession play = playing ? this.studio.getPlay() : null;
            Instance player = play != null ? play.getPlayer() : null;
            Instance pg = player != null ? Instance.findFirstChild$default(player, "PlayerGui", false, 2, null) : null;
            if (pg != null) {
                for (Instance inst : Instance.descendants$default(pg, null, 1, null)) {
                    if ("BillboardGui".equals(inst.getClassName()) && (inst.get("Adornee") instanceof Instance)) {
                        found.add(inst);
                    }
                }
            }
            this.billboards = found;
        }
        if (this.billboards.isEmpty()) {
            return false;
        }
        ViewState view = this.studio.getView();
        FlyCamera camera = this.studio.getCamera();
        float fTan = (float) Math.tan(Math.toRadians(((double) camera.getFov()) / 2.0d));
        ArrayList<Triple<Instance, float[], Float>> visibleBillboards = new ArrayList<>();
        for (Instance bb : this.billboards) {
            if (bb.getParent() == null || Boolean.FALSE.equals(bb.get("Enabled"))) {
                continue;
            }
            Object adorneeObj = bb.get("Adornee");
            Instance target = adorneeObj instanceof Instance ? (Instance) adorneeObj : bb.getParent();
            if (target == null) {
                continue;
            }
            Vector3 anchorPos = null;
            if (target.isA("BasePart")) {
                CFrame cf = PartData.INSTANCE.cframe(target);
                Vector3 sz = PartData.INSTANCE.size(target);
                Object eoObj = bb.get("ExtentsOffset");
                Vector3 eo = eoObj instanceof Vector3 ? (Vector3) eoObj : Vector3.INSTANCE.getZERO();
                Object eowObj = bb.get("ExtentsOffsetWorldSpace");
                Vector3 eow = eowObj instanceof Vector3 ? (Vector3) eowObj : Vector3.INSTANCE.getZERO();
                Vector3 localExt = new Vector3(eo.getX() * sz.getX() * 0.5f, eo.getY() * sz.getY() * 0.5f, eo.getZ() * sz.getZ() * 0.5f);
                Vector3 worldExt = new Vector3(eow.getX() * sz.getX() * 0.5f, eow.getY() * sz.getY() * 0.5f, eow.getZ() * sz.getZ() * 0.5f);
                anchorPos = cf.getPosition().plus(cf.rotationOnly().times(localExt)).plus(worldExt);
            } else if ("Attachment".equals(target.getClassName())) {
                Instance part = target.getParent();
                if (part != null && part.isA("BasePart")) {
                    CFrame cf = PartData.INSTANCE.cframe(part);
                    Object acfObj = target.get("CFrame");
                    CFrame acf = acfObj instanceof CFrame ? (CFrame) acfObj : CFrame.INSTANCE.getIDENTITY();
                    anchorPos = cf.times(acf.getPosition());
                }
            } else if (target.isA("Model")) {
                Object ppObj = target.get("PrimaryPart");
                Instance part = ppObj instanceof Instance ? (Instance) ppObj : target.findFirstChildWhichIsA("BasePart", true);
                if (part != null) {
                    anchorPos = PartData.INSTANCE.cframe(part).getPosition();
                }
            }
            if (anchorPos == null) {
                continue;
            }
            Object soObj = bb.get("StudsOffset");
            Vector3 so = soObj instanceof Vector3 ? (Vector3) soObj : Vector3.INSTANCE.getZERO();
            Object sowObj = bb.get("StudsOffsetWorldSpace");
            Vector3 sow = sowObj instanceof Vector3 ? (Vector3) sowObj : Vector3.INSTANCE.getZERO();
            Vector3 worldPos = anchorPos.plus(sow)
                    .plus(camera.getRight().times(so.getX()))
                    .plus(camera.getUp().times(so.getY()))
                    .minus(camera.getForward().times(so.getZ()));
            float dist = view.distTo(worldPos);
            Object mdObj = bb.get("MaxDistance");
            float maxDist = (mdObj instanceof Number) ? ((Number) mdObj).floatValue() : Float.POSITIVE_INFINITY;
            if (maxDist <= 0.0f) {
                maxDist = Float.POSITIVE_INFINITY;
            }
            if (dist > maxDist || dist < 0.1f) {
                continue;
            }
            float[] proj = view.project(worldPos);
            if (proj == null || proj[2] > 1.0f) {
                continue;
            }
            visibleBillboards.add(new Triple<>(bb, proj, Float.valueOf(dist)));
        }
        if (visibleBillboards.isEmpty()) {
            return false;
        }
        if (visibleBillboards.size() > 1) {
            java.util.Collections.sort(visibleBillboards, new Comparator<Triple<Instance, float[], Float>>() {
                @Override
                public int compare(Triple<Instance, float[], Float> a, Triple<Instance, float[], Float> b) {
                    boolean aTop = Boolean.TRUE.equals(a.getFirst().get("AlwaysOnTop"));
                    boolean bTop = Boolean.TRUE.equals(b.getFirst().get("AlwaysOnTop"));
                    if (aTop != bTop) {
                        return aTop ? 1 : -1;
                    }
                    return Float.compare(b.getThird(), a.getThird());
                }
            });
        }
        RectF screenRect = new RectF(0.0f, 0.0f, getWidth(), getHeight());
        for (Triple<Instance, float[], Float> item : visibleBillboards) {
            Instance bb = item.getFirst();
            float[] proj = item.getSecond();
            float dist = item.getThird();
            float pxPerStud = getHeight() / ((dist * 2.0f) * fTan);
            UDim2 sz = udim(bb.get("Size"));
            float xs = (sz.getXs() * pxPerStud) + (sz.getXo() * this.d);
            float ys = (sz.getYs() * pxPerStud) + (sz.getYo() * this.d);
            if (xs < 1.0f || ys < 1.0f) {
                continue;
            }
            Object offObj = bb.get("SizeOffset");
            Vector2 sizeOff = offObj instanceof Vector2 ? (Vector2) offObj : new Vector2(0.0f, 0.0f);
            float x = (proj[0] - (xs * 0.5f)) + (sizeOff.getX() * xs);
            float y = (proj[1] - (ys * 0.5f)) - (sizeOff.getY() * ys);
            RectF rectF = new RectF(x, y, x + xs, y + ys);
            if (!RectF.intersects(rectF, screenRect)) {
                continue;
            }
            boolean clips = Boolean.TRUE.equals(bb.get("ClipsDescendants"));
            boolean savedRegHits = this.registerHits;
            this.registerHits = Boolean.TRUE.equals(bb.get("Active"));
            if (clips) {
                canvas.save();
                canvas.clipRect(rectF);
            }
            setAbs(bb, rectF);
            drawLayerCollector(canvas, bb, rectF, 1.0f, clips ? rectF : screenRect);
            if (clips) {
                canvas.restore();
            }
            this.registerHits = savedRegHits;
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: compiled from: GuiOverlay.kt */
    static final class Node {
        private final RectF area;
        private final boolean inLayout;
        private final Instance inst;
        private final RectF rect;
        private final float scale;

        public Node(Instance inst, RectF rect, float scale, RectF area, boolean inLayout) {
            Intrinsics.checkNotNullParameter(inst, "inst");
            Intrinsics.checkNotNullParameter(rect, "rect");
            this.inst = inst;
            this.rect = rect;
            this.scale = scale;
            this.area = area;
            this.inLayout = inLayout;
        }

        public /* synthetic */ Node(Instance instance, RectF rectF, float f, RectF rectF2, boolean z, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this(instance, rectF, f, (i & 8) != 0 ? null : rectF2, (i & 16) != 0 ? false : z);
        }

        public final RectF getArea() {
            return this.area;
        }

        public final boolean getInLayout() {
            return this.inLayout;
        }

        public final Instance getInst() {
            return this.inst;
        }

        public final RectF getRect() {
            return this.rect;
        }

        public final float getScale() {
            return this.scale;
        }
    }

    private final UDim2 udim(Object v) {
        UDim2 uDim2 = v instanceof UDim2 ? (UDim2) v : null;
        return uDim2 == null ? new UDim2(0.0f, 0, 0.0f, 0) : uDim2;
    }

    private final int enumOf(Instance i, String prop, int def) {
        Object obj = i != null ? i.get(prop) : null;
        if (obj instanceof EnumValue) return ((EnumValue) obj).getValue();
        if (obj instanceof EnumItemV) return ((EnumItemV) obj).getValue();
        if (obj instanceof Number) return ((Number) obj).intValue();
        return def;
    }

    private final float num(Instance i, String prop, float def) {
        Object obj = i != null ? i.get(prop) : null;
        Number number = obj instanceof Number ? (Number) obj : null;
        return number != null ? number.floatValue() : def;
    }

    private final Instance child(Instance p, String cls) {
        Object next;
        Iterator it = p.getChildren().iterator();
        while (it.hasNext()) {
            next = it.next();
            if (Intrinsics.areEqual(((Instance) next).getClassName(), cls)) {
                return (Instance) next;
            }
        }
        next = null;
        return (Instance) next;
    }

    private final boolean isText(Instance c) {
        return Intrinsics.areEqual(c.getClassName(), "TextLabel") || Intrinsics.areEqual(c.getClassName(), "TextButton") || Intrinsics.areEqual(c.getClassName(), "TextBox");
    }

    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
    private final String textOf(Instance c) {
        String className = c.getClassName();
        switch (className.hashCode()) {
            case -935338273:
                if (!className.equals("TextButton")) {
                    return null;
                }
                break;
            case 246787390:
                if (!className.equals("TextBox")) {
                    return null;
                }
                Object obj = c.get("Text");
                String str2 = obj instanceof String ? (String) obj : "";
                if (str2.length() == 0) {
                    Object obj2 = c.get("PlaceholderText");
                    str2 = obj2 instanceof String ? (String) obj2 : null;
                }
                return str2;
            case 948280743:
                if (!className.equals("TextLabel")) {
                    return null;
                }
                break;
            default:
                return null;
        }
        Object obj3 = c.get("Text");
        if (obj3 instanceof String) {
            return (String) obj3;
        }
        return null;
    }

    private final RectF padded(Instance p, RectF r, float scale) {
        Instance pad = child(p, "UIPadding");
        return pad == null ? r : new RectF(r.left + padded$u(pad, this, scale, "PaddingLeft", r.width()), r.top + padded$u(pad, this, scale, "PaddingTop", r.height()), r.right - padded$u(pad, this, scale, "PaddingRight", r.width()), r.bottom - padded$u(pad, this, scale, "PaddingBottom", r.height()));
    }

    private static final float padded$u(Instance pad, GuiOverlay this$0, float $scale, String n, float size) {
        Object obj = pad.get(n);
        UDim uDim = obj instanceof UDim ? (UDim) obj : null;
        if (uDim != null) {
            return (uDim.getScale() * size) + (uDim.getOffset() * this$0.d * $scale);
        }
        return 0.0f;
    }

    private final float[] constrain(Instance c, float w0, float h0, float scale) {
        return GuiMath.applyConstraints(c, w0, h0, this.d, scale);
    }

    private final float[] sizeOf(Instance c, RectF area, float scale) {
        UDim2 s = udim(c.get("Size"));
        float aw = area.width();
        float ah = area.height();
        float w = (s.getXs() * aw) + (s.getXo() * this.d * scale);
        float h = (s.getYs() * ah) + (s.getYo() * this.d * scale);
        switch (enumOf(c, "SizeConstraint", 0)) {
            case 1:
                h = (s.getYs() * aw) + (s.getYo() * this.d * scale);
                break;
            case 2:
                w = (s.getXs() * ah) + (s.getXo() * this.d * scale);
                break;
        }
        int auto = enumOf(c, "AutomaticSize", 0);
        if (auto != 0 && isText(c)) {
            String rawText = textOf(c);
            if (rawText == null) {
                rawText = "";
            }
            boolean rich = Boolean.TRUE.equals(c.get("RichText"));
            String plainText = rich ? RichText.INSTANCE.parse(rawText).toString() : rawText;
            Fonts.Face face = Fonts.INSTANCE.of(c);
            this.textPaint.setTypeface(face.getTypeface());
            this.textPaint.setFakeBoldText(face.getFakeBold());
            this.textPaint.setTextSize(num(c, "TextSize", 14.0f) * this.d * scale);
            List<String> lines = StringsKt.split((CharSequence) plainText, new char[]{'\n'}, false, 0);
            float maxLine = 0.0f;
            for (String line : lines) {
                maxLine = Math.max(maxLine, this.textPaint.measureText(line));
            }
            float padH = 0.0f;
            float padV = 0.0f;
            Instance padInst = child(c, "UIPadding");
            if (padInst != null) {
                float[] pad = GuiMath.computePadding(c, Math.max(w, 0.0f), Math.max(h, 0.0f), this.d, scale);
                padH = pad[0] + pad[2];
                padV = pad[1] + pad[3];
            }
            float tw = maxLine + (this.d * 2.0f) + padH;
            float th = (lines.size() * this.textPaint.getFontSpacing()) + padV;
            if (auto == 1 || auto == 3) {
                w = Math.max(w, tw);
            }
            if (auto == 2 || auto == 3) {
                int wrapW = (int) Math.max(w - padH, 1.0f);
                StaticLayout l = layoutFor(new TKey(rawText, rich, face.getTypeface(), face.getFakeBold(), this.textPaint.getTextSize(), wrapW, Layout.Alignment.ALIGN_NORMAL, 1.0f, false));
                float measuredH = Boolean.TRUE.equals(c.get("TextWrapped")) ? (l.getHeight() + padV) : th;
                h = Math.max(h, measuredH);
            }
        }
        float[] cs = constrain(c, w, h, scale);
        float w2 = cs[0];
        float h2 = cs[1];
        float ownScale = GuiMath.ownScaleOf(c);
        w2 *= ownScale;
        h2 *= ownScale;
        float effScale = scale * ownScale;
        if (auto != 0 && !"ScrollingFrame".equals(c.getClassName())) {
            boolean hasLayout = false;
            for (Instance ch : c.getChildren()) {
                String cn = ch.getClassName();
                if ("UIListLayout".equals(cn) || "UIGridLayout".equals(cn) || "UITableLayout".equals(cn) || "UIPageLayout".equals(cn)) {
                    hasLayout = true;
                    break;
                }
            }
            RectF tmp;
            if (hasLayout) {
                tmp = new RectF(0.0f, 0.0f, w2, h2);
            } else {
                float tw = (auto == 1 || auto == 3) ? Math.max(w2, aw) : w2;
                float th = (auto == 2 || auto == 3) ? Math.max(h2, ah) : h2;
                tmp = new RectF(0.0f, 0.0f, tw, th);
            }
            float mx = w2;
            float my = h2;
            for (Node n : layoutChildren(c, padded(c, tmp, effScale), effScale)) {
                mx = Math.max(mx, n.getRect().right);
                my = Math.max(my, n.getRect().bottom);
            }
            Instance padInst = child(c, "UIPadding");
            if (padInst != null) {
                Object prObj = padInst.get("PaddingRight");
                if (prObj instanceof UDim) {
                    mx += ((UDim) prObj).getOffset() * this.d * effScale;
                }
                Object pbObj = padInst.get("PaddingBottom");
                if (pbObj instanceof UDim) {
                    my += ((UDim) pbObj).getOffset() * this.d * effScale;
                }
            }
            if (auto == 1 || auto == 3) {
                w2 = mx;
            }
            if (auto == 2 || auto == 3) {
                h2 = my;
            }
        }
        return new float[]{Math.max(w2, 0.0f), Math.max(h2, 0.0f)};
    }

    private final float scaleFor(Instance c, float parentScale) {
        Instance instanceChild = child(c, "UIScale");
        return (instanceChild != null ? num(instanceChild, "Scale", 1.0f) : 1.0f) * parentScale;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @SuppressWarnings("unchecked")
    private final List<Instance> sortKids(Instance parent, List<? extends Instance> kids, Instance layout) {
        if (layout == null) {
            return (List<Instance>) (List<?>) kids;
        }
        switch (enumOf(layout, "SortOrder", 2)) {
            case 0:
                return CollectionsKt.sortedWith(kids, new Comparator() { // from class: com.goydevv.studrblx.ui.GuiOverlay$sortKids$$inlined$sortedBy$1
                    /* JADX WARN: Multi-variable type inference failed */
                    @Override // java.util.Comparator
                    public final int compare(Object t, Object t2) {
                        return ComparisonsKt.compareValues(((Instance) t).getName(), ((Instance) t2).getName());
                    }
                });
            case 1:
            default:
                return (List<Instance>) (List<?>) kids;
            case 2:
                return CollectionsKt.sortedWith(kids, new Comparator() { // from class: com.goydevv.studrblx.ui.GuiOverlay$sortKids$$inlined$sortedBy$2
                    /* JADX WARN: Multi-variable type inference failed */
                    @Override // java.util.Comparator
                    public final int compare(Object t, Object t2) {
                        Object obj = ((Instance) t).get("LayoutOrder");
                        Number number = obj instanceof Number ? (Number) obj : null;
                        Integer numValueOf = Integer.valueOf(number != null ? number.intValue() : 0);
                        Object obj2 = ((Instance) t2).get("LayoutOrder");
                        Number number2 = obj2 instanceof Number ? (Number) obj2 : null;
                        return ComparisonsKt.compareValues(numValueOf, Integer.valueOf(number2 != null ? number2.intValue() : 0));
                    }
                });
        }
    }

    private final List<Node> layoutChildren(Instance parent, RectF area, float scale) {
        boolean isLayerCollector = !parent.isA("GuiObject");
        float childScale = isLayerCollector ? (scale * GuiMath.ownScaleOf(parent)) : scale;
        GuiMath.TextMeasurer measurer = new GuiMath.TextMeasurer() {
            @Override
            public float[] measureTextSize(Instance inst, float baseW, float baseH, float sc) {
                return GuiOverlay.this.sizeOf(inst, new RectF(0.0f, 0.0f, baseW, baseH), sc);
            }
        };
        GuiMath.LayoutResult res = GuiMath.computeLayout(
                parent, area.left, area.top, area.right, area.bottom,
                this.d, childScale,
                new GuiMath.SizeResolver() {
                    @Override
                    public float[] resolveSize(Instance child, float aw, float ah, float sc) {
                        return GuiOverlay.this.sizeOf(child, new RectF(0.0f, 0.0f, aw, ah), sc);
                    }
                }
        );
        if (res.layoutInstance != null) {
            setContentSize(res.layoutInstance, res.contentW, res.contentH);
            if ("UIGridLayout".equals(res.layoutInstance.getClassName())) {
                float au = this.absUnit > 0.0f ? this.absUnit : (this.d > 0.0f ? this.d : 1.0f);
                if (res.cellW > 0.0f || res.cellH > 0.0f) {
                    res.layoutInstance.getAttributes().put("__absCellSize", new Vector2(res.cellW / au, res.cellH / au));
                }
                res.layoutInstance.getAttributes().put("__absCellCount", new Vector2((float) res.usedCols, (float) res.usedRows));
            }
        }
        ArrayList<Node> out = new ArrayList<>(res.items.size());
        for (GuiMath.LayoutBox box : res.items) {
            float nodeScale = childScale * GuiMath.ownScaleOf(box.inst);
            out.add(new Node(box.inst, new RectF(box.left, box.top, box.right, box.bottom), nodeScale, area, box.inLayout));
        }
        return out;
    }

    private static final class GlobalDrawItem {
        final Node node;
        final RectF clip;
        final int zIndex;
        final int order;

        GlobalDrawItem(Node node, RectF clip, int zIndex, int order) {
            this.node = node;
            this.clip = clip;
            this.zIndex = zIndex;
            this.order = order;
        }
    }

    private final void drawLayerCollector(Canvas canvas, Instance collector, RectF area, float scale, RectF clip) {
        if (GuiMath.isZIndexGlobal(collector)) {
            ArrayList<GlobalDrawItem> items = new ArrayList<>();
            collectGlobalNodes(collector, area, scale, clip, items);
            java.util.Collections.sort(items, new Comparator<GlobalDrawItem>() {
                @Override
                public int compare(GlobalDrawItem a, GlobalDrawItem b) {
                    int c = Integer.compare(a.zIndex, b.zIndex);
                    return c != 0 ? c : Integer.compare(a.order, b.order);
                }
            });
            for (GlobalDrawItem item : items) {
                drawSingleNodeVisual(canvas, item.node, item.clip);
            }
        } else {
            drawChildren(canvas, collector, area, scale, clip);
        }
    }

    private final void collectGlobalNodes(Instance parent, RectF area, float scale, RectF clip, List<GlobalDrawItem> out) {
        RectF content = padded(parent, area, scale);
        List<Node> nodes = layoutChildren(parent, content, scale);
        for (Node n : nodes) {
            Instance c = n.getInst();
            RectF r = n.getRect();
            setAbs(c, r);
            if ((r.width() <= 0.0f || r.height() <= 0.0f) && !"ScrollingFrame".equals(c.getClassName()) && Boolean.TRUE.equals(c.get("ClipsDescendants"))) {
                continue;
            }
            float groupT = "CanvasGroup".equals(c.getClassName()) ? num(c, "GroupTransparency", 0.0f) : 0.0f;
            if (groupT >= 0.999f) {
                continue;
            }
            int zIdx = (int) num(c, "ZIndex", 1.0f);
            out.add(new GlobalDrawItem(n, new RectF(clip), zIdx, out.size()));
            if ("ScrollingFrame".equals(c.getClassName())) {
                // ScrollingFrame manages its own clipped canvas children
                continue;
            }
            boolean clips = Boolean.TRUE.equals(c.get("ClipsDescendants")) || "CanvasGroup".equals(c.getClassName());
            RectF childClip;
            if (clips) {
                childClip = new RectF(r);
                if (!childClip.intersect(clip)) {
                    childClip.setEmpty();
                }
            } else {
                childClip = clip;
            }
            collectGlobalNodes(c, r, n.getScale(), childClip, out);
        }
    }

    private final void drawSingleNodeVisual(Canvas canvas, Node n, RectF clip) {
        Instance c = n.getInst();
        RectF r = n.getRect();
        float rot = num(c, "Rotation", 0.0f);
        float parentAbsRot = 0.0f;
        Instance parInst = c.getParent();
        if (parInst != null) {
            Object prObj = parInst.getAttributes().get("__absRotation");
            if (prObj instanceof Number) {
                parentAbsRot = ((Number) prObj).floatValue();
            }
        }
        c.getAttributes().put("__absRotation", Float.valueOf(parentAbsRot + rot));
        int saveCount = canvas.save();
        canvas.clipRect(clip);
        if (rot != 0.0f) {
            canvas.rotate(rot, r.centerX(), r.centerY());
        }
        drawObject(canvas, c, r, n.getScale());
        recordNodeInteraction(n, clip);
        if ("ScrollingFrame".equals(c.getClassName())) {
            drawScrolling(canvas, c, r, n.getScale(), clip);
        }
        canvas.restoreToCount(saveCount);
    }

    private final void recordNodeInteraction(Node n, RectF clip) {
        Instance c = n.getInst();
        RectF r = n.getRect();
        RectF rectF = new RectF(r);
        if (!rectF.intersect(clip)) {
            rectF.setEmpty();
        }
        if (n.getArea() != null && !this.studio.isPlaying()) {
            this.newBoxes.put(c, new EditBox(new RectF(r), new RectF(n.getArea()), n.getScale(), n.getInLayout()));
        }
        if (!rectF.isEmpty()) {
            this.newPicks.add(TuplesKt.to(c, new RectF(r)));
            if (this.registerHits && isInteractable(c)) {
                if ("TextBox".equals(c.getClassName())) {
                    this.newHits.add(new Hit(c, rectF, this.rt, 1));
                } else if (c.isA("GuiButton")) {
                    this.newHits.add(new Hit(c, rectF, this.rt, 0));
                } else if (Boolean.TRUE.equals(c.get("Active"))) {
                    this.newHits.add(new Hit(c, rectF, this.rt, 2));
                }
            }
        }
    }

    private final void drawChildren(Canvas canvas, Instance parent, RectF area, float scale, RectF clip) {
        RectF content = padded(parent, area, scale);
        List<Node> nodes = layoutChildren(parent, content, scale);
        if (nodes.size() > 1) {
            ArrayList<Node> drawOrder = new ArrayList<>(nodes);
            java.util.Collections.sort(drawOrder, new Comparator<Node>() {
                @Override
                public int compare(Node a, Node b) {
                    int za = (int) num(a.getInst(), "ZIndex", 1.0f);
                    int zb = (int) num(b.getInst(), "ZIndex", 1.0f);
                    return Integer.compare(za, zb);
                }
            });
            nodes = drawOrder;
        }
        for (Node n : nodes) {
            drawNode(canvas, n, clip);
        }
    }

    private final void drawNode(Canvas canvas, Node n, RectF clip) {
        Instance c = n.getInst();
        RectF r = n.getRect();
        setAbs(c, r);
        if ((r.width() <= 0.0f || r.height() <= 0.0f) && !"ScrollingFrame".equals(c.getClassName()) && Boolean.TRUE.equals(c.get("ClipsDescendants"))) {
            return;
        }
        float rot = num(c, "Rotation", 0.0f);
        float parentAbsRot = 0.0f;
        Instance parInst = c.getParent();
        if (parInst != null) {
            Object prObj = parInst.getAttributes().get("__absRotation");
            if (prObj instanceof Number) {
                parentAbsRot = ((Number) prObj).floatValue();
            }
        }
        c.getAttributes().put("__absRotation", Float.valueOf(parentAbsRot + rot));
        float groupT = "CanvasGroup".equals(c.getClassName()) ? num(c, "GroupTransparency", 0.0f) : 0.0f;
        if (groupT >= 0.999f) {
            return;
        }
        int saveCount = canvas.save();
        if (rot != 0.0f) {
            canvas.rotate(rot, r.centerX(), r.centerY());
        }
        if ("CanvasGroup".equals(c.getClassName())) {
            Object gcObj = c.get("GroupColor3");
            Color3 gc = gcObj instanceof Color3 ? (Color3) gcObj : null;
            boolean hasTint = gc != null && (gc.getR() < 0.995f || gc.getG() < 0.995f || gc.getB() < 0.995f);
            if (groupT > 0.001f || hasTint) {
                android.graphics.Paint lp = new android.graphics.Paint();
                lp.setAlpha((int) ((1.0f - groupT) * 255.0f));
                if (hasTint) {
                    lp.setColorFilter(new android.graphics.PorterDuffColorFilter(gc.toArgb(), android.graphics.PorterDuff.Mode.MULTIPLY));
                }
                canvas.saveLayer(r, lp);
            }
            canvas.clipRect(r);
        }
        drawObject(canvas, c, r, n.getScale());
        recordNodeInteraction(n, clip);
        if ("ScrollingFrame".equals(c.getClassName())) {
            drawScrolling(canvas, c, r, n.getScale(), clip);
        } else {
            boolean clips = Boolean.TRUE.equals(c.get("ClipsDescendants")) || "CanvasGroup".equals(c.getClassName());
            RectF childClip;
            if (clips) {
                childClip = new RectF(r);
                if (!childClip.intersect(clip)) {
                    childClip.setEmpty();
                }
                canvas.clipRect(r);
            } else {
                childClip = clip;
            }
            drawChildren(canvas, c, r, n.getScale(), childClip);
        }
        canvas.restoreToCount(saveCount);
    }

    /* JADX WARN: Code duplicated, block: B:36:0x0118  */
    /* JADX WARN: Code duplicated, block: B:39:0x012d A[LOOP:1: B:37:0x0127->B:39:0x012d, LOOP_END] */
    /* JADX WARN: Code duplicated, block: B:53:0x0166  */
    /* JADX WARN: Code duplicated, block: B:54:0x0175  */
    /* JADX WARN: Code duplicated, block: B:62:0x01b1  */
    /* JADX WARN: Code duplicated, block: B:65:0x01ba  */
    /* JADX WARN: Code duplicated, block: B:73:0x01fb  */
    /* JADX WARN: Code duplicated, block: B:77:0x0212 A[LOOP:2: B:75:0x020c->B:77:0x0212, LOOP_END] */
    /* JADX WARN: Code duplicated, block: B:80:0x0238  */
    private final boolean isInteractable(Instance c) {
        Instance cur = c;
        while (cur != null) {
            if (cur.isA("GuiObject") && Boolean.FALSE.equals(cur.get("Interactable"))) {
                return false;
            }
            cur = cur.getParent();
        }
        return true;
    }

    private final void drawScrolling(Canvas canvas, Instance c, RectF r, float scale, RectF clip) {
        float unit = this.d * scale;
        Object uDim2 = c.get("CanvasSize");
        if (uDim2 == null) {
            uDim2 = new UDim2(0.0f, 0, 2.0f, 0);
        }
        UDim2 cs = udim(uDim2);
        Ref.FloatRef cw = new Ref.FloatRef();
        cw.element = Math.max((cs.getXs() * r.width()) + (cs.getXo() * unit), r.width());
        Ref.FloatRef ch = new Ref.FloatRef();
        ch.element = Math.max((cs.getYs() * r.height()) + (cs.getYo() * unit), r.height());
        int auto = enumOf(c, "AutomaticCanvasSize", 0);
        Object obj = c.get("CanvasPosition");
        Vector2 pos1 = obj instanceof Vector2 ? (Vector2) obj : new Vector2(0.0f, 0.0f);
        RectF area = drawScrolling$canvasRect(r, pos1, unit, cw, ch);
        List<Node> nodes = layoutChildren(c, padded(c, area, scale), scale);
        if (auto != 0 && !nodes.isEmpty()) {
            float maxBottom = nodes.get(0).getRect().bottom;
            float maxRight = nodes.get(0).getRect().right;
            for (int i = 1; i < nodes.size(); i++) {
                RectF nr = nodes.get(i).getRect();
                if (nr.bottom > maxBottom) maxBottom = nr.bottom;
                if (nr.right > maxRight) maxRight = nr.right;
            }
            float bottom = maxBottom - area.top;
            float right = maxRight - area.left;
            Instance padInst = child(c, "UIPadding");
            if (padInst != null) {
                Object pbObj = padInst.get("PaddingBottom");
                if (pbObj instanceof UDim) {
                    UDim pb = (UDim) pbObj;
                    bottom += (pb.getScale() * area.height()) + (pb.getOffset() * unit);
                }
                Object prObj = padInst.get("PaddingRight");
                if (prObj instanceof UDim) {
                    UDim pr = (UDim) prObj;
                    right += (pr.getScale() * area.width()) + (pr.getOffset() * unit);
                }
            }
            boolean changed = false;
            if ((auto == 2 || auto == 3) && bottom > ch.element) {
                ch.element = bottom;
                changed = true;
            }
            if ((auto == 1 || auto == 3) && right > cw.element) {
                cw.element = right;
                changed = true;
            }
            if (changed) {
                nodes = layoutChildren(c, padded(c, drawScrolling$canvasRect(r, pos1, unit, cw, ch), scale), scale);
            }
        }
        if (this.absUnit <= 0.0f) {
            useGameUnits();
        }
        float au = this.absUnit;
        c.getAttributes().put("__absCanvasSize", new Vector2(cw.element / au, ch.element / au));
        c.getAttributes().put("__absWindowSize", new Vector2(r.width() / au, r.height() / au));
        float maxX = Math.max(0.0f, cw.element - r.width());
        float maxY = Math.max(0.0f, ch.element - r.height());
        float clampedPosX = RangesKt.coerceIn(pos1.getX(), 0.0f, maxX / Math.max(unit, 0.001f));
        float clampedPosY = RangesKt.coerceIn(pos1.getY(), 0.0f, maxY / Math.max(unit, 0.001f));
        if (Math.abs(clampedPosX - pos1.getX()) > 0.5f || Math.abs(clampedPosY - pos1.getY()) > 0.5f) {
            pos1 = new Vector2(clampedPosX, clampedPosY);
            nodes = layoutChildren(c, padded(c, drawScrolling$canvasRect(r, pos1, unit, cw, ch), scale), scale);
        }
        boolean clips = !Boolean.FALSE.equals(c.get("ClipsDescendants"));
        RectF vis = new RectF(r);
        if (!vis.intersect(clip)) {
            vis.setEmpty();
        }
        if (!vis.isEmpty() && !Boolean.FALSE.equals(c.get("ScrollingEnabled")) && (maxX > 0.0f || maxY > 0.0f)) {
            this.newScrolls.add(new Scroll(c, vis, maxX / unit, maxY / unit, unit));
        }
        canvas.save();
        if (clips) {
            canvas.clipRect(r);
        }
        RectF childClip = clips ? vis : clip;
        for (Node n : nodes) {
            drawNode(canvas, n, childClip);
        }
        canvas.restore();
        float th = num(c, "ScrollBarThickness", 12.0f) * unit * 0.5f;
        float bt = num(c, "ScrollBarImageTransparency", 0.0f);
        if (th > 0.0f && bt < 1.0f) {
            this.fill.setShader(null);
            this.fill.setColor(alpha(colorOf(c.get("ScrollBarImageColor3"), ViewCompat.MEASURED_STATE_MASK), (0.7f * bt) + 0.3f));
            if (maxY > 0.0f) {
                float barH = Math.max(th, r.height() * (r.height() / ch.element));
                float y = r.top + ((r.height() - barH) * RangesKt.coerceIn((pos1.getY() * unit) / maxY, 0.0f, 1.0f));
                canvas.drawRoundRect((r.right - th) - (this.d * 2.0f), y, r.right - (this.d * 2.0f), y + barH, th / 2.0f, th / 2.0f, this.fill);
            }
            if (maxX > 0.0f) {
                float barW = Math.max(th, r.width() * (r.width() / cw.element));
                float x = r.left + ((r.width() - barW) * RangesKt.coerceIn((pos1.getX() * unit) / maxX, 0.0f, 1.0f));
                canvas.drawRoundRect(x, (r.bottom - th) - (this.d * 2.0f), x + barW, r.bottom - (this.d * 2.0f), th / 2.0f, th / 2.0f, this.fill);
            }
        }
    }

    private static final RectF drawScrolling$canvasRect(RectF $r, Vector2 pos0, float unit, Ref.FloatRef cw, Ref.FloatRef ch) {
        return new RectF($r.left - (pos0.getX() * unit), $r.top - (pos0.getY() * unit), ($r.left - (pos0.getX() * unit)) + cw.element, ($r.top - (pos0.getY() * unit)) + ch.element);
    }

    private final int colorOf(Object v, int i) {
        Color3 color3 = v instanceof Color3 ? (Color3) v : null;
        return color3 != null ? color3.toArgb() : i;
    }

    private final int alpha(int c, float transparency) {
        int a = (int) ((1.0f - RangesKt.coerceIn(transparency, 0.0f, 1.0f)) * ((c >>> 24) & 255));
        return (16777215 & c) | (a << 24);
    }

    private final float cornerOf(Instance c, RectF r, float scale) {
        Instance instanceChild = child(c, "UICorner");
        if (instanceChild == null) {
            return 0.0f;
        }
        Object obj = instanceChild.get("CornerRadius");
        UDim uDim = obj instanceof UDim ? (UDim) obj : null;
        return RangesKt.coerceIn(uDim != null ? (uDim.getScale() * Math.min(r.width(), r.height())) + (uDim.getOffset() * this.d * scale) : this.d * 8.0f * scale, 0.0f, Math.min(r.width(), r.height()) / 2.0f);
    }

    private final Shader gradientShader(Instance g, RectF r, float baseTransparency) {
        return gradientShader(g, r, baseTransparency, 0xFFFFFFFF);
    }

    private final Shader gradientShader(Instance g, RectF r, float baseTransparency, int baseColorArgb) {
        float baseR = ((baseColorArgb >>> 16) & 255) / 255.0f;
        float baseG = ((baseColorArgb >>> 8) & 255) / 255.0f;
        float baseB = (baseColorArgb & 255) / 255.0f;
        List listEmptyList;
        List listEmptyList2;
        Iterable keys;
        Iterable keys2;
        Object obj = g.get("Color");
        ColorSequence cseq = obj instanceof ColorSequence ? (ColorSequence) obj : null;
        Object obj2 = g.get("Transparency");
        NumberSequence tseq = obj2 instanceof NumberSequence ? (NumberSequence) obj2 : null;
        float f = 1.0f;
        List listListOf = CollectionsKt.listOf(new Float[]{Float.valueOf(0.0f), Float.valueOf(1.0f)});
        if (cseq == null || (keys2 = cseq.getKeys()) == null) {
            listEmptyList = CollectionsKt.emptyList();
        } else {
            Iterable iterable = keys2;
            Collection arrayList = new ArrayList(CollectionsKt.collectionSizeOrDefault(iterable, 10));
            Iterator it = iterable.iterator();
            while (it.hasNext()) {
                arrayList.add(Float.valueOf(((ColorKey) it.next()).getTime()));
            }
            listEmptyList = (List) arrayList;
        }
        List listPlus = CollectionsKt.plus((Collection) listListOf, (Iterable) listEmptyList);
        if (tseq == null || (keys = tseq.getKeys()) == null) {
            listEmptyList2 = CollectionsKt.emptyList();
        } else {
            Iterable iterable2 = keys;
            Collection arrayList2 = new ArrayList(CollectionsKt.collectionSizeOrDefault(iterable2, 10));
            Iterator it2 = iterable2.iterator();
            while (it2.hasNext()) {
                arrayList2.add(Float.valueOf(((NumberKey) it2.next()).getTime()));
            }
            listEmptyList2 = (List) arrayList2;
        }
        Iterable times = CollectionsKt.sorted(CollectionsKt.distinct(CollectionsKt.plus((Collection) listPlus, listEmptyList2)));
        Iterable iterable3 = times;
        Collection arrayList3 = new ArrayList(CollectionsKt.collectionSizeOrDefault(iterable3, 10));
        Iterator it3 = iterable3.iterator();
        while (it3.hasNext()) {
            float fFloatValue = ((Number) it3.next()).floatValue();
            Color3 gc = gradientShader$colorAt(cseq, fFloatValue);
            Color3 mul = new Color3(gc.getR() * baseR, gc.getG() * baseG, gc.getB() * baseB);
            float combinedT = 1.0f - ((1.0f - baseTransparency) * (1.0f - gradientShader$transAt(tseq, fFloatValue)));
            arrayList3.add(Integer.valueOf(alpha(mul.toArgb(), combinedT)));
        }
        int[] colors = CollectionsKt.toIntArray((List) arrayList3);
        double rot = Math.toRadians(num(g, "Rotation", 0.0f));
        float dx = (float) Math.cos(rot);
        float dy = (float) Math.sin(rot);
        Object obj3 = g.get("Offset");
        Vector2 off = obj3 instanceof Vector2 ? (Vector2) obj3 : null;
        if (off == null) {
            off = new Vector2(0.0f, 0.0f);
        }
        float half = (Math.abs(r.width() * dx) + Math.abs(r.height() * dy)) / 2.0f;
        float cx = r.centerX() + (off.getX() * r.width());
        float cy = r.centerY() + (off.getY() * r.height());
        return new LinearGradient(cx - (dx * half), cy - (dy * half), cx + (dx * half), cy + (dy * half), colors, CollectionsKt.toFloatArray((Collection) times), Shader.TileMode.CLAMP);
    }

    private static final Color3 gradientShader$colorAt(ColorSequence cseq, float t) {
        List<ColorKey> keys;
        if (cseq == null || (keys = cseq.getKeys()) == null) {
            return new Color3(1.0f, 1.0f, 1.0f);
        }
        if (keys.isEmpty()) {
            return new Color3(1.0f, 1.0f, 1.0f);
        }
        int size = keys.size() - 1;
        for (int i = 0; i < size; i++) {
            if (t <= keys.get(i + 1).getTime()) {
                ColorKey a = keys.get(i);
                ColorKey b = keys.get(i + 1);
                float f = b.getTime() > a.getTime() ? (t - a.getTime()) / (b.getTime() - a.getTime()) : 0.0f;
                return a.getColor().lerp(b.getColor(), RangesKt.coerceIn(f, 0.0f, 1.0f));
            }
        }
        return ((ColorKey) CollectionsKt.last((List) keys)).getColor();
    }

    private static final float gradientShader$transAt(NumberSequence tseq, float t) {
        List<NumberKey> keys;
        if (tseq == null || (keys = tseq.getKeys()) == null || keys.isEmpty()) {
            return 0.0f;
        }
        int size = keys.size() - 1;
        for (int i = 0; i < size; i++) {
            if (t <= keys.get(i + 1).getTime()) {
                NumberKey a = keys.get(i);
                NumberKey b = keys.get(i + 1);
                float f = b.getTime() > a.getTime() ? (t - a.getTime()) / (b.getTime() - a.getTime()) : 0.0f;
                return a.getValue() + ((b.getValue() - a.getValue()) * RangesKt.coerceIn(f, 0.0f, 1.0f));
            }
        }
        return ((NumberKey) CollectionsKt.last((List) keys)).getValue();
    }

    /* JADX WARN: Code duplicated, block: B:67:0x019c  */
    private final void drawObject(Canvas canvas, Instance c, RectF r, float scale) {
        boolean z;
        float f;
        Object next;
        boolean z2;
        Object next2;
        float f2;
        Instance instance;
        float f3;
        float f4;
        float f5;
        float f6 = 0.0f;
        float fNum = num(c, "BackgroundTransparency", 0.0f);
        float fCornerOf = cornerOf(c, r, scale);
        Iterator it = c.getChildren().iterator();
        while (true) {
            z = false;
            if (it.hasNext()) {
                next = it.next();
                Instance instance2 = (Instance) next;
                f = f6;
                if (Intrinsics.areEqual(instance2.getClassName(), "UIGradient") && !Intrinsics.areEqual(instance2.get("Enabled"), (Object) false)) {
                    break;
                } else {
                    f6 = f;
                }
            } else {
                f = f6;
                next = null;
                break;
            }
        }
        Instance instance3 = (Instance) next;
        ArrayList<Instance> children = c.getChildren();
        Iterator it2 = children.iterator();
        while (true) {
            if (!it2.hasNext()) {
                z2 = z;
                next2 = null;
                break;
            }
            next2 = it2.next();
            Instance instance4 = (Instance) next2;
            z2 = z;
            ArrayList<Instance> arrayList = children;
            if ((!Intrinsics.areEqual(instance4.getClassName(), "UIStroke") || Intrinsics.areEqual(instance4.get("Enabled"), Boolean.valueOf(z2))) ? z2 : true) {
                break;
            }
            z = z2;
            children = arrayList;
        }
        Instance instance5 = (Instance) next2;
        boolean z3 = instance5 != null && isText(c) && enumOf(instance5, "ApplyStrokeMode", z2 ? 1 : 0) == 0;
        if (fNum < 1.0f) {
            this.fill.setColor(alpha(colorOf(c.get("BackgroundColor3"), -6053211), fNum));
            this.fill.setShader(instance3 != null ? gradientShader(instance3, r, fNum, colorOf(c.get("BackgroundColor3"), -6053211)) : null);
            if (fCornerOf > f) {
                canvas.drawRoundRect(r, fCornerOf, fCornerOf, this.fill);
            } else {
                canvas.drawRect(r, this.fill);
            }
            this.fill.setShader(null);
            if (this.pressed == c && !Intrinsics.areEqual(c.get("AutoButtonColor"), (Object) false)) {
                this.fill.setColor(1073741824);
                if (fCornerOf > f) {
                    canvas.drawRoundRect(r, fCornerOf, fCornerOf, this.fill);
                } else {
                    canvas.drawRect(r, this.fill);
                }
            }
            float f7 = f;
            float fNum2 = num(c, "BorderSizePixel", f7) * scale;
            if (fNum2 > f7) {
                if ((fCornerOf == f7) && instance5 == null) {
                    this.stroke.setShader(null);
                    this.stroke.setColor(alpha(colorOf(c.get("BorderColor3"), -14996939), fNum));
                    this.stroke.setStrokeWidth(this.d * fNum2);
                    switch (enumOf(c, "BorderMode", 0)) {
                        case 0:
                            f4 = this.d * fNum2;
                            f5 = f4 / 2.0f;
                            break;
                        case 1:
                            f5 = 0.0f;
                            break;
                        default:
                            f4 = (-fNum2) * this.d;
                            f5 = f4 / 2.0f;
                            break;
                    }
                    float f8 = f5;
                    canvas.drawRect(r.left - f8, r.top - f8, r.right + f8, r.bottom + f8, this.stroke);
                }
            }
        }
        if (instance5 != null && !z3) {
            float strokeT = num(instance5, "Transparency", 0.0f);
            float fNum3 = num(instance5, "Thickness", 1.0f) * this.d * scale;
            Instance strokeGrad = child(instance5, "UIGradient");
            if (strokeGrad != null && Boolean.FALSE.equals(strokeGrad.get("Enabled"))) {
                strokeGrad = null;
            }
            this.stroke.setColor(alpha(colorOf(instance5.get("Color"), ViewCompat.MEASURED_STATE_MASK), strokeT));
            this.stroke.setShader(strokeGrad != null ? gradientShader(strokeGrad, r, strokeT, colorOf(instance5.get("Color"), ViewCompat.MEASURED_STATE_MASK)) : null);
            this.stroke.setStrokeWidth(fNum3);
            this.stroke.setStrokeJoin(Paint.Join.ROUND);
            f2 = 1.0f;
            RectF rectF = new RectF(r.left - (fNum3 / 2.0f), r.top - (fNum3 / 2.0f), r.right + (fNum3 / 2.0f), r.bottom + (fNum3 / 2.0f));
            if (fCornerOf > 0.0f) {
                canvas.drawRoundRect(rectF, (fNum3 / 2.0f) + fCornerOf, (fNum3 / 2.0f) + fCornerOf, this.stroke);
            } else {
                canvas.drawRect(rectF, this.stroke);
            }
            this.stroke.setShader(null);
        } else {
            f2 = 1.0f;
        }
        if (Intrinsics.areEqual(c.getClassName(), "ImageLabel") || Intrinsics.areEqual(c.getClassName(), "ImageButton")) {
            instance = c;
            drawImage(canvas, instance, r, fCornerOf, scale);
            f3 = scale;
        } else {
            instance = c;
            f3 = scale;
        }
        if (isText(instance)) {
            drawText(canvas, instance, padded(instance, r, f3), f3, z3 ? instance5 : null, fNum >= f2 ? instance3 : null);
        }
    }

    private final void drawImage(Canvas canvas, Instance c, RectF r, float corner, float scale) {
        Object obj;
        String content;
        Bitmap bmp;
        float it = num(c, "ImageTransparency", 0.0f);
        if (it >= 1.0f) {
            return;
        }
        obj = null;
        if ("ImageButton".equals(c.getClassName())) {
            if (this.pressed == c) {
                Object pi = c.get("PressedImage");
                if (pi != null && !pi.toString().isEmpty()) {
                    obj = pi;
                }
            } else if (this.hoveredHit != null && this.hoveredHit.getInst() == c) {
                Object hi = c.get("HoverImage");
                if (hi != null && !hi.toString().isEmpty()) {
                    obj = hi;
                }
            }
        }
        if (obj == null) {
            obj = c.get("Image");
        }
        if (obj == null || (content = obj.toString()) == null || content.isEmpty() || (bmp = ImageLoader.INSTANCE.get(content)) == null) {
            return;
        }
        this.imgPaint.setAlpha((int) ((1.0f - it) * 255.0f));
        int tint = colorOf(c.get("ImageColor3"), -1);
        this.imgPaint.setColorFilter((tint & ViewCompat.MEASURED_SIZE_MASK) != 16777215 ? new LightingColorFilter(16777215 & tint, 0) : null);
        Rect src = sourceRect(c, bmp);
        canvas.save();
        if (corner > 0.0f) {
            Path path = new Path();
            path.addRoundRect(r, corner, corner, Path.Direction.CW);
            canvas.clipPath(path);
        }
        switch (enumOf(c, "ScaleType", 0)) {
            case 1:
                drawSliced(canvas, c, bmp, src, r, scale);
                break;
            case 2:
                Object uDim2 = c.get("TileSize");
                if (uDim2 == null) {
                    uDim2 = new UDim2(1.0f, 0, 1.0f, 0);
                }
                UDim2 ts = udim(uDim2);
                float tw = RangesKt.coerceAtLeast((ts.getXs() * r.width()) + (ts.getXo() * this.d * scale), 2.0f);
                float th = RangesKt.coerceAtLeast((ts.getYs() * r.height()) + (ts.getYo() * this.d * scale), 2.0f);
                Bitmap sub = Bitmap.createBitmap(bmp, src.left, src.top, RangesKt.coerceAtLeast(src.width(), 1), RangesKt.coerceAtLeast(src.height(), 1));
                Intrinsics.checkNotNullExpressionValue(sub, "createBitmap(...)");
                BitmapShader sh = new BitmapShader(sub, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                Matrix matrix = new Matrix();
                matrix.setScale(tw / sub.getWidth(), th / sub.getHeight());
                matrix.postTranslate(r.left, r.top);
                sh.setLocalMatrix(matrix);
                this.imgPaint.setShader(sh);
                canvas.drawRect(r, this.imgPaint);
                this.imgPaint.setShader(null);
                break;
            case 3:
                float k = Math.min(r.width() / src.width(), r.height() / src.height());
                float w = src.width() * k;
                float h = src.height() * k;
                canvas.drawBitmap(bmp, src, new RectF(r.centerX() - (w / 2.0f), r.centerY() - (h / 2.0f), r.centerX() + (w / 2.0f), r.centerY() + (h / 2.0f)), this.imgPaint);
                break;
            case 4:
                float k2 = Math.max(r.width() / src.width(), r.height() / src.height());
                float sw = r.width() / k2;
                float sh2 = r.height() / k2;
                Rect s2 = new Rect((int) (src.exactCenterX() - (sw / 2.0f)), (int) (src.exactCenterY() - (sh2 / 2.0f)), (int) (src.exactCenterX() + (sw / 2.0f)), (int) (src.exactCenterY() + (sh2 / 2.0f)));
                canvas.drawBitmap(bmp, s2, r, this.imgPaint);
                break;
            default:
                canvas.drawBitmap(bmp, src, r, this.imgPaint);
                break;
        }
        canvas.restore();
    }

    private final Rect sourceRect(Instance c, Bitmap bmp) {
        Object obj = c.get("ImageRectSize");
        Vector2 size = obj instanceof Vector2 ? (Vector2) obj : null;
        Object obj2 = c.get("ImageRectOffset");
        Vector2 off = obj2 instanceof Vector2 ? (Vector2) obj2 : null;
        if (off == null) {
            off = new Vector2(0.0f, 0.0f);
        }
        if (size == null || size.getX() <= 0.0f || size.getY() <= 0.0f) {
            return new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
        }
        float need = Math.max(off.getX() + size.getX(), off.getY() + size.getY());
        float sheet = 64.0f;
        while (sheet < need) {
            sheet *= 2.0f;
        }
        float k = bmp.getWidth() / sheet;
        return new Rect((int) (off.getX() * k), (int) (off.getY() * k), RangesKt.coerceAtMost((int) ((off.getX() + size.getX()) * k), bmp.getWidth()), RangesKt.coerceAtMost((int) ((off.getY() + size.getY()) * k), bmp.getHeight()));
    }

    private final void drawSliced(Canvas canvas, Instance c, Bitmap bmp, Rect src, RectF r, float scale) {
        int j;
        Object obj = c.get("SliceCenter");
        com.goydevv.studrblx.model.Rect sc = obj instanceof com.goydevv.studrblx.model.Rect ? (com.goydevv.studrblx.model.Rect) obj : null;
        if (sc == null || sc.getMaxX() <= sc.getMinX() || sc.getMaxY() <= sc.getMinY()) {
            canvas.drawBitmap(bmp, src, r, this.imgPaint);
            return;
        }
        float ow = RangesKt.coerceAtLeast(sc.getMinX() + sc.getMaxX(), 1.0f);
        float oh = RangesKt.coerceAtLeast(sc.getMinY() + sc.getMaxY(), 1.0f);
        float kx = src.width() / ow;
        float ky = src.height() / oh;
        float ss = num(c, "SliceScale", 1.0f) * this.d * scale;
        float sl = sc.getMinX() * kx;
        float st = sc.getMinY() * ky;
        float sr = (ow - sc.getMaxX()) * kx;
        float sb = (oh - sc.getMaxY()) * ky;
        float dl = sc.getMinX() * ss;
        float dt = sc.getMinY() * ss;
        float dr = (ow - sc.getMaxX()) * ss;
        float db = (oh - sc.getMaxY()) * ss;
        if (dl + dr > r.width()) {
            float f = r.width() / (dl + dr);
            dl *= f;
            dr *= f;
        }
        if (dt + db > r.height()) {
            float f2 = r.height() / (dt + db);
            dt *= f2;
            db *= f2;
        }
        float[] xs = {src.left, src.left + sl, src.right - sr, src.right};
        int i = 3;
        float[] xs2 = {src.top, src.top + st, src.bottom - sb, src.bottom};
        float[] xd = {r.left, r.left + dl, r.right - dr, r.right};
        float[] yd = {r.top, r.top + dt, r.bottom - db, r.bottom};
        int j2 = 0;
        while (true) {
            float[] xd2 = xd;
            int i2 = i;
            if (j2 >= i2) {
                return;
            }
            int i3 = j2;
            int j3 = 0;
            while (j3 < i2) {
                if (xs[i3 + 1] <= xs[i3] || xs2[j3 + 1] <= xs2[j3] || xd2[i3 + 1] <= xd2[i3]) {
                    j = j3;
                } else if (yd[j3 + 1] <= yd[j3]) {
                    j = j3;
                } else {
                    j = j3;
                    canvas.drawBitmap(bmp, new Rect((int) xs[i3], (int) xs2[j], (int) xs[i3 + 1], (int) xs2[j + 1]), new RectF(xd2[i3], yd[j], xd2[i3 + 1], yd[j + 1]), this.imgPaint);
                }
                j3 = j + 1;
                i2 = 3;
            }
            j2 = i3 + 1;
            xd = xd2;
            i = 3;
        }
    }

    /* JADX WARN: Code duplicated, block: B:43:0x0094  */
    private final void drawText(Canvas canvas, Instance c, RectF r, float scale, Instance uiStroke, Instance gradient) {
        boolean placeholder;
        Object color3;
        String str;
        int b;
        Layout.Alignment alignment;
        int wrapW;
        float tx;
        float ty;
        float f;
        float strokeT;
        Shader shader;
        Shader shaderGradientShader;
        int i;
        Object obj;
        float fWidth;
        String text = textOf(c);
        if (text == null || text.length() == 0) {
            c.getAttributes().put("__textBounds", new Vector2(0.0f, 0.0f));
            c.getAttributes().put("__textFits", Boolean.TRUE);
            return;
        }
        float tt = num(c, "TextTransparency", 0.0f);
        if (tt >= 1.0f) {
            return;
        }
        Object obj2 = c.get("MaxVisibleGraphemes");
        Number number = obj2 instanceof Number ? (Number) obj2 : null;
        int maxG = number != null ? number.intValue() : -1;
        if (maxG >= 0) {
            text = StringsKt.take(text, maxG);
        }
        String text2 = text;
        if (text2.length() == 0) {
            return;
        }
        boolean rich = Intrinsics.areEqual(c.get("RichText"), (Object) true);
        if (Intrinsics.areEqual(c.getClassName(), "TextBox")) {
            Object obj3 = c.get("Text");
            String str2 = obj3 instanceof String ? (String) obj3 : null;
            if (str2 == null || str2.length() == 0) {
                placeholder = true;
            } else {
                placeholder = false;
            }
        } else {
            placeholder = false;
        }
        if (placeholder) {
            color3 = c.get("PlaceholderColor3");
            if (color3 == null) {
                color3 = new Color3(0.7f, 0.7f, 0.7f);
            }
        } else {
            color3 = c.get("TextColor3");
        }
        int color = alpha(colorOf(color3, ViewCompat.MEASURED_STATE_MASK), tt);
        Fonts.Face face = Fonts.INSTANCE.of(c);
        float minS = RangesKt.coerceIn(num(c, "LineHeight", 1.0f), 0.5f, 3.0f);
        Instance sizeLimit = child(c, "UITextSizeConstraint");
        float size = num(c, "TextSize", 14.0f) * this.d * scale;
        boolean wrapped = Intrinsics.areEqual(c.get("TextWrapped"), (Object) true);
        if (Intrinsics.areEqual(c.get("TextScaled"), (Object) true)) {
            float maxS = Math.min(100.0f, sizeLimit != null ? num(sizeLimit, "MaxTextSize", 100.0f) : 100.0f) * this.d * scale;
            str = "TextScaled";
            SKey sKey = new SKey(text2, rich, face.getTypeface(), face.getFakeBold(), (int) r.width(), (int) r.height(), maxS, (sizeLimit != null ? num(sizeLimit, "MinTextSize", 1.0f) : 1.0f) * this.d * scale, minS);
            size = scaledSize(sKey);
            b = -16777216;
        } else {
            str = "TextScaled";
            b = 0;
            if (sizeLimit != null) {
                size = RangesKt.coerceIn(size, num(sizeLimit, "MinTextSize", 1.0f) * this.d * scale, num(sizeLimit, "MaxTextSize", 100.0f) * this.d * scale);
            }
        }
        if (size < 1.0f) {
            return;
        }
        int xAlign = enumOf(c, "TextXAlignment", 2);
        int yAlign = enumOf(c, "TextYAlignment", 1);
        switch (xAlign) {
            case 0:
                alignment = Layout.Alignment.ALIGN_NORMAL;
                break;
            case 1:
                alignment = Layout.Alignment.ALIGN_OPPOSITE;
                break;
            default:
                alignment = Layout.Alignment.ALIGN_CENTER;
                break;
        }
        Layout.Alignment align = alignment;
        boolean wrap = wrapped || Intrinsics.areEqual(c.get(str), (Object) true);
        boolean truncate = (wrap || enumOf(c, "TextTruncate", 0) == 0) ? false : true;
        int rw = RangesKt.coerceAtLeast((int) r.width(), 1);
        if (wrap || truncate) {
            wrapW = rw;
        } else {
            this.textPaint.setTypeface(face.getTypeface());
            this.textPaint.setTextSize(size);
            this.textPaint.setFakeBoldText(face.getFakeBold());
            String plain = rich ? RichText.INSTANCE.parse(text2).toString() : text2;
            Iterator it = StringsKt.split((CharSequence) plain, new char[]{'\n'}, false, 0).iterator();
            if (!it.hasNext()) {
                throw new NoSuchElementException();
            }
            float fMeasureText = this.textPaint.measureText((String) it.next());
            while (it.hasNext()) {
                fMeasureText = Math.max(fMeasureText, this.textPaint.measureText((String) it.next()));
            }
            wrapW = Math.max(rw, ((int) fMeasureText) + 2);
        }
        int wrapW2 = wrapW;
        StaticLayout sl = layoutFor(new TKey(text2, rich, face.getTypeface(), face.getFakeBold(), size, wrapW, align, minS, truncate));
        float auText = this.absUnit > 0.0f ? this.absUnit : (this.d > 0.0f ? this.d : 1.0f);
        float maxLineW = 0.0f;
        int slLines = sl.getLineCount();
        for (int li = 0; li < slLines; li++) {
            float lw = sl.getLineWidth(li);
            if (lw > maxLineW) maxLineW = lw;
        }
        float slH = sl.getHeight();
        c.getAttributes().put("__textBounds", new Vector2(maxLineW / auText, slH / auText));
        c.getAttributes().put("__textFits", Boolean.valueOf((maxLineW <= r.width() + 0.5f) && (slH <= r.height() + 0.5f)));
        if (wrapW2 > r.width()) {
            float f2 = r.left;
            switch (xAlign) {
                case 0:
                    fWidth = 0.0f;
                    break;
                case 1:
                    fWidth = wrapW2 - r.width();
                    break;
                default:
                    fWidth = (wrapW2 - r.width()) / 2.0f;
                    break;
            }
            tx = f2 - fWidth;
        } else {
            tx = r.left;
        }
        switch (yAlign) {
            case 0:
                ty = r.top;
                break;
            case 1:
            default:
                ty = r.top + ((r.height() - sl.getHeight()) / 2.0f);
                break;
            case 2:
                ty = r.bottom - sl.getHeight();
                break;
        }
        TextPaint p = sl.getPaint();
        Intrinsics.checkNotNullExpressionValue(p, "getPaint(...)");
        canvas.save();
        if (Intrinsics.areEqual(c.get("ClipsDescendants"), (Object) true)) {
            canvas.clipRect(r);
        }
        canvas.translate(tx, ty);
        if (uiStroke != null) {
            strokeT = num(uiStroke, "Transparency", 0.0f);
            f = 1.0f;
        } else {
            f = 1.0f;
            strokeT = num(c, "TextStrokeTransparency", 1.0f);
        }
        if (strokeT < f) {
            if (uiStroke != null) {
                obj = uiStroke.get("Color");
                i = ViewCompat.MEASURED_STATE_MASK;
            } else {
                i = ViewCompat.MEASURED_STATE_MASK;
                obj = c.get("TextStrokeColor3");
            }
            int sc = colorOf(obj, i);
            float sw = uiStroke != null ? num(uiStroke, "Thickness", 1.0f) * this.d * scale * 2.0f : this.d * 1.6f * scale;
            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(sw);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setColor(alpha(sc, 1.0f - ((1.0f - strokeT) * (1.0f - tt))));
            if (rich) {
                drawRichStroke(canvas, sl, p);
            } else {
                sl.draw(canvas);
            }
            p.setStyle(Paint.Style.FILL);
        }
        p.setColor(color);
        if (gradient == null || (shaderGradientShader = gradientShader(gradient, r, tt, color)) == null) {
            shader = null;
        } else {
            Matrix matrix = new Matrix();
            matrix.setTranslate(-tx, -ty);
            shaderGradientShader.setLocalMatrix(matrix);
            shader = shaderGradientShader;
        }
        p.setShader(shader);
        sl.draw(canvas);
        p.setShader(null);
        canvas.restore();
    }

    private final void drawRichStroke(Canvas canvas, StaticLayout sl, TextPaint p) {
        String plain = sl.getText().toString();
        StaticLayout.Builder.obtain(plain, 0, plain.length(), p, sl.getWidth()).setAlignment(sl.getAlignment()).setIncludePad(false).setLineSpacing(0.0f, sl.getSpacingMultiplier()).build().draw(canvas);
    }

    public final Hit buttonAt(float x, float y) {
        List<Hit> list = this.hits;
        for (int idx = list.size() - 1; idx >= 0; idx--) {
            Hit hit = list.get(idx);
            if (!hit.getRect().contains(x, y)) {
                continue;
            }
            if (hit.getKind() != 2) {
                return hit;
            }
            if (!seeThrough(hit.getInst())) {
                return null;
            }
        }
        return null;
    }

    public final void hoverAt(float x, float y) {
        Hit cur = buttonAt(x, y);
        Hit prev = this.hoveredHit;
        if (prev != null && (cur == null || prev.getInst() != cur.getInst())) {
            synchronized (this.studio.getLock()) {
                hover(prev, x, y, false);
            }
        }
        if (cur != null && (prev == null || prev.getInst() != cur.getInst())) {
            synchronized (this.studio.getLock()) {
                hover(cur, x, y, true);
            }
        }
        this.hoveredHit = cur;
    }

    public final boolean isPluginUi(Instance i) {
        boolean z;
        Intrinsics.checkNotNullParameter(i, "i");
        Iterable pluginWidgets = this.studio.getPluginWidgets();
        if (!(pluginWidgets instanceof Collection) || !((Collection) pluginWidgets).isEmpty()) {
            Iterator it = pluginWidgets.iterator();
            while (true) {
                if (it.hasNext()) {
                    PluginWidget pluginWidget = (PluginWidget) it.next();
                    if (i == pluginWidget.getInst() || i.isDescendantOf(pluginWidget.getInst())) {
                        z = true;
                        break;
                    }
                } else {
                    z = false;
                    break;
                }
            }
        } else {
            z = false;
        }
        if (z) {
            return true;
        }
        for (Instance p = i.getParent(); p != null; p = p.getParent()) {
            if (Intrinsics.areEqual(p.getClassName(), "CoreGui")) {
                return true;
            }
        }
        return false;
    }

    public final boolean pluginUiAt(float x, float y) {
        Iterable<Hit> iterable = this.hits;
        if ((iterable instanceof Collection) && ((Collection) iterable).isEmpty()) {
            return false;
        }
        for (Hit hit : iterable) {
            if (hit.getRect().contains(x, y) && (hit.getKind() == 4 || (isPluginUi(hit.getInst()) && !(hit.getKind() == 2 && seeThrough(hit.getInst()))))) {
                return true;
            }
        }
        return false;
    }

    public final boolean blocksAt(float x, float y) {
        Iterable<Hit> iterable = this.hits;
        if ((iterable instanceof Collection) && ((Collection) iterable).isEmpty()) {
            return false;
        }
        for (Hit hit : iterable) {
            if (hit.getRect().contains(x, y) && !(hit.getKind() == 2 && seeThrough(hit.getInst()))) {
                return true;
            }
        }
        return false;
    }

    public final Scroll scrollAt(float x, float y) {
        Object objPrevious;
        List list = this.scrolls;
        ListIterator<Scroll> listIterator = list.listIterator(list.size());
        while (listIterator.hasPrevious()) {
            objPrevious = listIterator.previous();
            if (((Scroll) objPrevious).getRect().contains(x, y)) {
                return (Scroll) objPrevious;
            }
        }
        objPrevious = null;
        return (Scroll) objPrevious;
    }

    /* JADX WARN: Code duplicated, block: B:21:0x0067 A[Catch: all -> 0x0080, TryCatch #0 {, blocks: (B:5:0x000d, B:7:0x001b, B:11:0x0022, B:12:0x0027, B:17:0x005c, B:22:0x0078, B:21:0x0067), top: B:29:0x000d }] */
    public final void scrollBy(Scroll s, float dxPx, float dyPx) {
        Intrinsics.checkNotNullParameter(s, "s");
        synchronized (this.studio.getLock()) {
            Object obj = s.getInst().get("CanvasPosition");
            Vector2 vector2 = obj instanceof Vector2 ? (Vector2) obj : null;
            if (vector2 == null) {
                vector2 = new Vector2(0.0f, 0.0f);
            }
            float fCoerceIn = RangesKt.coerceIn(vector2.getX() - (dxPx / s.getUnit()), 0.0f, s.getMaxX());
            float fCoerceIn2 = RangesKt.coerceIn(vector2.getY() - (dyPx / s.getUnit()), 0.0f, s.getMaxY());
            if (fCoerceIn == vector2.getX()) {
                if (!(fCoerceIn2 == vector2.getY())) {
                    Instance.set$default(s.getInst(), "CanvasPosition", new Vector2(fCoerceIn, fCoerceIn2), false, 4, null);
                }
            } else {
                Instance.set$default(s.getInst(), "CanvasPosition", new Vector2(fCoerceIn, fCoerceIn2), false, 4, null);
            }
            Unit unit = Unit.INSTANCE;
        }
        invalidate();
    }

    private final boolean mouseFor(Instance i) {
        return this.studio.getPlay() == null && isPluginUi(i);
    }

    private final void uisFire(String event, LuaValue input) {
        Instance uis = this.studio.getEditModel().findService("UserInputService");
        if (uis == null) {
            return;
        }
        LuaRuntime editRuntime = this.studio.getEditRuntime();
        LuaBoolean TRUE = LuaValue.TRUE;
        Intrinsics.checkNotNullExpressionValue(TRUE, "TRUE");
        editRuntime.fire(uis, event, input, TRUE);
    }

    public final Hit pluginFrameAt(float x, float y) {
        Object obj = null;
        if (this.studio.getPlay() != null) {
            return null;
        }
        List list = this.hits;
        ListIterator<Hit> listIterator = list.listIterator(list.size());
        while (listIterator.hasPrevious()) {
            Object objPrevious = listIterator.previous();
            Hit hit = (Hit) objPrevious;
            if (hit.getKind() == 2 && hit.getRt() != null && hit.getRect().contains(x, y) && isPluginUi(hit.getInst())) {
                obj = objPrevious;
                break;
            }
        }
        return (Hit) obj;
    }

    private final List<Hit> extrasFor(Hit h, float x, float y) {
        int i = this.hits.indexOf(h);
        if (i < 0) {
            return CollectionsKt.emptyList();
        }
        Iterable iterableSubList = this.hits.subList(i + 1, this.hits.size());
        Intrinsics.checkNotNullExpressionValue(iterableSubList, "subList(...)");
        Collection arrayList = new ArrayList();
        for (Object obj : iterableSubList) {
            Hit hit = (Hit) obj;
            if (hit.getKind() == 2 && hit.getRt() != null && hit.getRect().contains(x, y) && isPluginUi(hit.getInst())) {
                arrayList.add(obj);
            }
        }
        return CollectionsKt.asReversed((List) arrayList);
    }

    public final void press(Hit h, float x, float y) {
        Intrinsics.checkNotNullParameter(h, "h");
        if (h.getKind() != 0 && h.getKind() != 2) {
            return;
        }
        if (h.getKind() == 0) {
            this.pressed = h.getInst();
        }
        LuaRuntime r = h.getRt();
        if (r == null) {
            return;
        }
        boolean mouse = mouseFor(h.getInst());
        synchronized (this.studio.getLock()) {
            List<Hit> extras = mouse ? extrasFor(h, x, y) : CollectionsKt.emptyList();
            if (extras.isEmpty()) {
                this.pressExtras.remove(h);
            } else {
                this.pressExtras.put(h, extras);
            }
            if (mouse) {
                for (Hit extra : extras) {
                    hover(extra, x, y, true);
                }
            }
            if (this.hoveredHit == null || this.hoveredHit.getInst() != h.getInst()) {
                hover(h, x, y, true);
                this.hoveredHit = h;
            }
            for (Hit extra : extras) {
                LuaRuntime ert = extra.getRt();
                if (ert != null) {
                    ert.fire(extra.getInst(), "InputBegan", touchInput(x, y, 0, true, false));
                }
            }
            if (h.getKind() == 0) {
                Vector2 gp = guiPoint(x, y);
                r.fire(h.getInst(), "MouseButton1Down", LuaValue.valueOf(gp.getX()), LuaValue.valueOf(gp.getY()));
            }
            r.fire(h.getInst(), "InputBegan", touchInput(x, y, 0, mouse, false));
            if (mouse) {
                uisFire("InputBegan", touchInput(x, y, 0, true, false));
            }
            invalidate();
        }
    }

    private final void releaseExtras(Hit h, float x, float y) {
        List<Hit> listRemove = this.pressExtras.remove(h);
        if (listRemove == null) {
            return;
        }
        for (Hit hit : listRemove) {
            Intrinsics.checkNotNullExpressionValue(hit, "next(...)");
            Hit e = hit;
            LuaRuntime rt = e.getRt();
            if (rt != null) {
                rt.fire(e.getInst(), "InputEnded", touchInput$default(this, x, y, 2, true, false, 16, null));
            }
            hover(e, x, y, false);
        }
    }

    private final void hover(Hit h, float x, float y, boolean enter) {
        LuaRuntime r = h.getRt();
        if (r == null) {
            return;
        }
        Vector2 p = guiPoint(x, y);
        Instance inst = h.getInst();
        String str = enter ? "MouseEnter" : "MouseLeave";
        LuaNumber luaNumberValueOf = LuaValue.valueOf(p.getX());
        Intrinsics.checkNotNullExpressionValue(luaNumberValueOf, "valueOf(...)");
        LuaNumber luaNumberValueOf2 = LuaValue.valueOf(p.getY());
        Intrinsics.checkNotNullExpressionValue(luaNumberValueOf2, "valueOf(...)");
        r.fire(inst, str, luaNumberValueOf, luaNumberValueOf2);
        Instance inst2 = h.getInst();
        String str2 = enter ? "InputBegan" : "InputEnded";
        LuaValue[] luaValueArr = new LuaValue[1];
        luaValueArr[0] = touchInput(x, y, enter ? 0 : 2, true, true);
        r.fire(inst2, str2, luaValueArr);
    }

    public final boolean canWheel(Hit h, float x, float y) {
        Intrinsics.checkNotNullParameter(h, "h");
        LuaRuntime r = h.getRt();
        if (this.studio.getPlay() != null || !isPluginUi(h.getInst()) || r == null) {
            return false;
        }
        synchronized (this.studio.getLock()) {
            if (r.hasConn(h.getInst(), "InputChanged")) {
                return true;
            }
            List<Hit> extras = this.pressExtras.get(h);
            if (extras != null) {
                for (Hit hit : extras) {
                    LuaRuntime ert = hit.getRt();
                    if (ert != null && ert.hasConn(hit.getInst(), "InputChanged")) {
                        return true;
                    }
                }
            }
            Instance top = pluginObjectAt(x, y);
            for (Instance cur = top; cur != null && !"CoreGui".equals(cur.getClassName()); cur = cur.getParent()) {
                if (r.hasConn(cur, "InputChanged")) {
                    return true;
                }
            }
            return false;
        }
    }

    static final Instance canWheel$lambda$92$lambda$89(Instance it) {
        Intrinsics.checkNotNullParameter(it, "it");
        return it.getParent();
    }

    static final boolean canWheel$lambda$92$lambda$90(Instance it) {
        Intrinsics.checkNotNullParameter(it, "it");
        return !Intrinsics.areEqual(it.getClassName(), "CoreGui");
    }

    private final Instance pluginObjectAt(float x, float y) {
        Object objPrevious;
        Pair<Instance, RectF> pair;
        if (this.studio.getPlay() != null) {
            return null;
        }
        List list = this.picks;
        ListIterator<Pair<Instance, RectF>> listIterator = list.listIterator(list.size());
        do {
            if (listIterator.hasPrevious()) {
                objPrevious = listIterator.previous();
                pair = (Pair) objPrevious;
            } else {
                objPrevious = null;
                break;
            }
        } while (!(pair.component2().contains(x, y) && isPluginUi(pair.component1())));
        Pair<Instance, RectF> pair2 = (Pair) objPrevious;
        if (pair2 != null) {
            return pair2.getFirst();
        }
        return null;
    }

    private final LuaRuntime pluginRt(Instance i) {
        Object next;
        LuaRuntime rt;
        PluginWidget pluginWidget;
        Iterator it = this.studio.getPluginWidgets().iterator();
        do {
            if (!it.hasNext()) {
                next = null;
                break;
            }
            next = it.next();
            pluginWidget = (PluginWidget) next;
        } while (!(i == pluginWidget.getInst() || i.isDescendantOf(pluginWidget.getInst())));
        PluginWidget pluginWidget2 = (PluginWidget) next;
        return (pluginWidget2 == null || (rt = pluginWidget2.getRt()) == null) ? this.studio.getEditRuntime() : rt;
    }

    public final void pluginWheel(float x, float y, int z) {
        Instance top = pluginObjectAt(x, y);
        if (top == null) {
            return;
        }
        LuaRuntime r = pluginRt(top);
        synchronized (this.studio.getLock()) {
            LuaTypes luaTypes = LuaTypes.INSTANCE;
            for (Instance parent = top; parent != null && !Intrinsics.areEqual(parent.getClassName(), "CoreGui"); parent = parent.getParent()) {
                if (r.hasConn(parent, "InputChanged")) {
                    r.fire(parent, "InputChanged", pluginWheel$lambda$95$wheel(luaTypes, this, x, y, z));
                }
            }
            uisFire("InputChanged", pluginWheel$lambda$95$wheel(luaTypes, this, x, y, z));
            Unit unit = Unit.INSTANCE;
        }
        postInvalidate();
    }

    private static final LuaValue pluginWheel$lambda$95$wheel(LuaTypes lt, GuiOverlay this$0, float $x, float $y, int $z) {
        LuaTable t = new LuaTable();
        t.set("UserInputType", lt.wrap(new EnumItemV("UserInputType", "MouseWheel", 3)));
        t.set("UserInputState", lt.wrap(new EnumItemV("UserInputState", "Change", 1)));
        Vector2 p = this$0.guiPoint($x, $y);
        t.set("Position", lt.wrap(new Vector3(p.getX(), p.getY(), $z)));
        t.set("Delta", lt.wrap(new Vector3(0.0f, 0.0f, $z)));
        t.set("KeyCode", lt.wrap(new EnumItemV("KeyCode", "Unknown", 0)));
        return t;
    }

    public final void pluginPointerMove(Hit h, float x, float y) {
        char c;
        int i;
        char c2;
        Intrinsics.checkNotNullParameter(h, "h");
        if (mouseFor(h.getInst())) {
            synchronized (this.studio.getLock()) {
                int i2 = 0;
                LuaValue luaValue = touchInput(x, y, 1, true, true);
                LuaRuntime rt = h.getRt();
                char c3 = 0;
                int i3 = 1;
                if (rt != null) {
                    rt.fire(h.getInst(), "InputChanged", luaValue);
                }
                Iterable<Hit> iterable = (List) this.pressExtras.get(h);
                if (iterable != null) {
                    for (Hit hit : iterable) {
                        LuaRuntime rt2 = hit.getRt();
                        if (rt2 != null) {
                            Instance inst = hit.getInst();
                            LuaValue[] luaValueArr = new LuaValue[i3];
                            c2 = c3;
                            luaValueArr[c2] = touchInput(x, y, 1, true, true);
                            rt2.fire(inst, "InputChanged", luaValueArr);
                        } else {
                            c2 = c3;
                        }
                        c3 = c2;
                    }
                    c = c3;
                    i = i3;
                } else {
                    c = 0;
                    i = 1;
                }
                Vector2 vector2GuiPoint = guiPoint(x, y);
                LuaRuntime rt3 = h.getRt();
                if (rt3 != null) {
                    Instance inst2 = h.getInst();
                    LuaValue[] luaValueArr2 = new LuaValue[2];
                    LuaNumber luaNumberValueOf = LuaValue.valueOf(vector2GuiPoint.getX());
                    Intrinsics.checkNotNullExpressionValue(luaNumberValueOf, "valueOf(...)");
                    luaValueArr2[c] = luaNumberValueOf;
                    LuaNumber luaNumberValueOf2 = LuaValue.valueOf(vector2GuiPoint.getY());
                    Intrinsics.checkNotNullExpressionValue(luaNumberValueOf2, "valueOf(...)");
                    luaValueArr2[i] = luaNumberValueOf2;
                    rt3.fire(inst2, "MouseMoved", luaValueArr2);
                }
                uisFire("InputChanged", touchInput(x, y, 1, true, true));
                Unit unit = Unit.INSTANCE;
            }
            postInvalidate();
        }
    }

    public final void release(Hit h, float x, float y, boolean click) {
        Intrinsics.checkNotNullParameter(h, "h");
        if (this.pressed == h.getInst()) {
            this.pressed = null;
        }
        invalidate();
        switch (h.getKind()) {
            case 0: {
                LuaRuntime r = h.getRt();
                if (r == null) {
                    return;
                }
                boolean mouse = mouseFor(h.getInst());
                synchronized (this.studio.getLock()) {
                    releaseExtras(h, x, y);
                    Vector2 gp = guiPoint(x, y);
                    r.fire(h.getInst(), "MouseButton1Up", LuaValue.valueOf(gp.getX()), LuaValue.valueOf(gp.getY()));
                    r.fire(h.getInst(), "InputEnded", touchInput(x, y, 2, mouse, false));
                    if (mouse) {
                        uisFire("InputEnded", touchInput(x, y, 2, true, false));
                    }
                    if (click) {
                        r.fire(h.getInst(), "MouseButton1Click");
                        r.fire(h.getInst(), "Activated", touchInput(x, y, 2, mouse, false), LuaValue.valueOf(mouse ? 1 : 0));
                    }
                    if (this.hoveredHit != null && this.hoveredHit.getInst() == h.getInst()) {
                        hover(h, x, y, false);
                        this.hoveredHit = null;
                    } else if (mouse) {
                        hover(h, x, y, false);
                    }
                }
                return;
            }
            case 1:
                if (click) {
                    editTextBox(h);
                }
                return;
            case 2: {
                LuaRuntime r2 = h.getRt();
                if (r2 == null) {
                    return;
                }
                boolean mouse2 = mouseFor(h.getInst());
                synchronized (this.studio.getLock()) {
                    releaseExtras(h, x, y);
                    r2.fire(h.getInst(), "InputEnded", touchInput(x, y, 2, mouse2, false));
                    if (mouse2) {
                        uisFire("InputEnded", touchInput(x, y, 2, true, false));
                    }
                    if (this.hoveredHit != null && this.hoveredHit.getInst() == h.getInst()) {
                        hover(h, x, y, false);
                        this.hoveredHit = null;
                    } else if (mouse2) {
                        hover(h, x, y, false);
                    }
                }
                return;
            }
            case 3:
                if (click && this.onCoreMenu != null) {
                    this.onCoreMenu.invoke(Float.valueOf(x), Float.valueOf(y));
                }
                return;
            case 4:
                if (click) {
                    synchronized (this.studio.getLock()) {
                        Instance.set$default(h.getInst(), "Enabled", false, false, 4, null);
                    }
                    postInvalidate();
                }
                return;
            default:
                return;
        }
    }

    private final Vector2 guiPoint(float x, float y) {
        boolean edit = this.studio.getPlay() == null;
        float u = this.d;
        if (edit) {
            u *= this.studio.editGuiScale();
        }
        Vector2 p = new Vector2(x / u, (y / u) - (edit ? 0.0f : 58.0f));
        if (edit) {
            this.studio.setLastEditPointer(p);
        }
        return p;
    }

    static /* synthetic */ LuaValue touchInput$default(GuiOverlay guiOverlay, float f, float f2, int i, boolean z, boolean z2, int i2, Object obj) {
        if ((i2 & 8) != 0) {
            z = false;
        }
        if ((i2 & 16) != 0) {
            z2 = false;
        }
        return guiOverlay.touchInput(f, f2, i, z, z2);
    }

    private final LuaValue touchInput(float x, float y, int state, boolean mouse, boolean movement) {
        EnumItemV type;
        String stateName;
        LuaTypes lt = LuaTypes.INSTANCE;
        LuaTable t = new LuaTable();
        if (movement) {
            type = new EnumItemV("UserInputType", "MouseMovement", 4);
        } else if (mouse) {
            type = new EnumItemV("UserInputType", "MouseButton1", 0);
        } else {
            type = new EnumItemV("UserInputType", "Touch", 7);
        }
        t.set("UserInputType", lt.wrap(type));
        switch (state) {
            case 0:
                stateName = "Begin";
                break;
            case 1:
                stateName = "Change";
                break;
            default:
                stateName = "End";
                break;
        }
        t.set("UserInputState", lt.wrap(new EnumItemV("UserInputState", stateName, state)));
        Vector2 gp = guiPoint(x, y);
        Vector3 pos = new Vector3(gp.getX(), gp.getY(), 0.0f);
        t.set("Position", lt.wrap(pos));
        Vector3 prev = this.lastPointer;
        t.set("Delta", lt.wrap((state != 1 || prev == null) ? Vector3.INSTANCE.getZERO() : pos.minus(prev)));
        t.set("KeyCode", lt.wrap(new EnumItemV("KeyCode", "Unknown", 0)));
        this.lastPointer = pos;
        return t;
    }

    /* JADX WARN: Code duplicated, block: B:36:0x007a  */
    private final void editTextBox(Hit h) {
        String str = null;
        String str2;
        String str3;
        String str4;
        boolean multiLine;
        final Instance inst = h.getInst();
        final LuaRuntime r = h.getRt();
        if (r == null) {
            return;
        }
        Ui ui = Ui.INSTANCE;
        Context context = getContext();
        Intrinsics.checkNotNullExpressionValue(context, "getContext(...)");
        synchronized (this.studio.getLock()) {
            if (Boolean.FALSE.equals(inst.get("TextEditable"))) {
                return;
            }
            boolean clearOnFocus = !Boolean.FALSE.equals(inst.get("ClearTextOnFocus"));
            multiLine = Boolean.TRUE.equals(inst.get("MultiLine"));
            if (clearOnFocus) {
                str2 = "";
                Instance.set$default(inst, "Text", "", false, 4, null);
            } else {
                Object obj = inst.get("Text");
                str2 = obj instanceof String ? (String) obj : "";
            }
        }
        final EditText e = Ui.input$default(ui, context, str2, null, false, false, 28, null);
        e.setSingleLine(!multiLine);
        if (multiLine) {
            e.setMinLines(3);
            e.setMaxLines(8);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        synchronized (this.studio.getLock()) {
            Object obj2 = inst.get("PlaceholderText");
            str3 = obj2 instanceof String ? (String) obj2 : null;
        }
        if (str3 != null) {
            if (!(str3.length() == 0)) {
                str = str3;
            }
            str4 = str != null ? str : "Enter text";
        } else {
            str4 = "Enter text";
        }
        builder.setTitle(str4).setView(e).setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.goydevv.studrblx.ui.GuiOverlay$$ExternalSyntheticLambda0
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                try {
                    GuiOverlay.editTextBox$lambda$107(GuiOverlay.this, inst, e, r, dialogInterface, i);
                } catch (Throwable _t) {
                    if (_t instanceof RuntimeException) throw (RuntimeException) _t;
                    if (_t instanceof Error) throw (Error) _t;
                    throw new RuntimeException(_t);
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.goydevv.studrblx.ui.GuiOverlay$$ExternalSyntheticLambda1
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                GuiOverlay.editTextBox$lambda$109(GuiOverlay.this, r, inst, dialogInterface, i);
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public final void onCancel(DialogInterface dialogInterface) {
                GuiOverlay.editTextBox$lambda$109(GuiOverlay.this, r, inst, dialogInterface, 0);
            }
        }).show();
        synchronized (this.studio.getLock()) {
            r.setFocusedTextBox(inst);
            r.fire(inst, "Focused", new LuaValue[0]);
            Instance uis = r.getDm().findService("UserInputService");
            if (uis != null) {
                r.fire(uis, "TextBoxFocused", r.wrap(inst));
            }
            Unit unit = Unit.INSTANCE;
        }
        postInvalidate();
    }

    static final void editTextBox$lambda$107(GuiOverlay this$0, Instance $inst, EditText $e, LuaRuntime $r, DialogInterface dialogInterface, int i) {
        try {
            synchronized (this$0.studio.getLock()) {
                Instance.set$default($inst, "Text", $e.getText().toString(), false, 4, null);
                if ($r.getFocusedTextBox() == $inst) {
                    $r.setFocusedTextBox(null);
                }
                LuaBoolean TRUE = LuaValue.TRUE;
                Intrinsics.checkNotNullExpressionValue(TRUE, "TRUE");
                LuaValue NIL = LuaValue.NIL;
                Intrinsics.checkNotNullExpressionValue(NIL, "NIL");
                $r.fire($inst, "FocusLost", TRUE, NIL);
                Instance uis = $r.getDm().findService("UserInputService");
                if (uis != null) {
                    $r.fire(uis, "TextBoxFocusReleased", $r.wrap($inst));
                }
                Unit unit = Unit.INSTANCE;
            }
            this$0.postInvalidate();
        } catch (Throwable _ktEx) {
            throw com.goydevv.studrblx.util.KtCompat.sneakyThrow(_ktEx);
        }
    }

    static final void editTextBox$lambda$109(GuiOverlay this$0, LuaRuntime $r, Instance $inst, DialogInterface dialogInterface, int i) {
        synchronized (this$0.studio.getLock()) {
            if ($r.getFocusedTextBox() == $inst) {
                $r.setFocusedTextBox(null);
            }
            LuaBoolean FALSE = LuaValue.FALSE;
            Intrinsics.checkNotNullExpressionValue(FALSE, "FALSE");
            LuaValue NIL = LuaValue.NIL;
            Intrinsics.checkNotNullExpressionValue(NIL, "NIL");
            $r.fire($inst, "FocusLost", FALSE, NIL);
            Instance uis = $r.getDm().findService("UserInputService");
            if (uis != null) {
                $r.fire(uis, "TextBoxFocusReleased", $r.wrap($inst));
            }
            Unit unit = Unit.INSTANCE;
        }
        this$0.postInvalidate();
    }

    public final boolean tap(float x, float y) {
        try {
        Hit h = buttonAt(x, y);
        if (h == null) {
            return false;
        }
        if (this.studio.getPlay() == null && h.getRt() == this.studio.getEditRuntime() && h.getKind() != 4 && !isPluginUi(h.getInst())) {
            return false;
        }
        press(h, x, y);
        release(h, x, y, true);
        return true;
    }
        catch (Throwable _ktEx) {
            throw com.goydevv.studrblx.util.KtCompat.sneakyThrow(_ktEx);
        }
    }

    public final Instance pick(float x, float y) {
        Instance sg;
        Object objPrevious;
        Pair<Instance, RectF> pair;
        Instance instanceComponent1;
        if (this.studio.getPlay() != null || (sg = this.studio.getModel().findService("StarterGui")) == null || Boolean.FALSE.equals(sg.get("ShowDevelopmentGui"))) {
            return null;
        }
        List list = this.picks;
        ListIterator<Pair<Instance, RectF>> listIterator = list.listIterator(list.size());
        do {
            if (listIterator.hasPrevious()) {
                objPrevious = listIterator.previous();
                pair = (Pair) objPrevious;
                instanceComponent1 = pair.component1();
            } else {
                objPrevious = null;
                break;
            }
        } while (!(pair.component2().contains(x, y) && instanceComponent1.isDescendantOf(sg) && !seeThrough(instanceComponent1)));
        Pair<Instance, RectF> pair2 = (Pair) objPrevious;
        if (pair2 != null) {
            return pair2.getFirst();
        }
        return null;
    }

    /**
     * Pure-Java Roblox GUI layout, inset, constraint, ZIndexBehavior, and hit-testing math.
     * Shared by GuiOverlay at runtime and verified directly by the JVM test suite.
     */
    public static final class GuiMath {
        public interface TextMeasurer {
            float[] measureTextSize(Instance inst, float baseW, float baseH, float scale);
        }

        public interface SizeResolver {
            float[] resolveSize(Instance child, float aw, float ah, float scale);
        }

        public static final class LayoutBox {
            public final Instance inst;
            public final float left, top, right, bottom;
            public final boolean inLayout;

            public LayoutBox(Instance inst, float left, float top, float right, float bottom, boolean inLayout) {
                this.inst = inst;
                this.left = left;
                this.top = top;
                this.right = right;
                this.bottom = bottom;
                this.inLayout = inLayout;
            }

            public float width() { return this.right - this.left; }
            public float height() { return this.bottom - this.top; }
        }

        public static final class LayoutResult {
            public final List<LayoutBox> items;
            public final Instance layoutInstance;
            public final float contentW;
            public final float contentH;
            public final float cellW;
            public final float cellH;
            public final int usedCols;
            public final int usedRows;

            public LayoutResult(List<LayoutBox> items, Instance layoutInstance, float contentW, float contentH, float cellW, float cellH, int usedCols, int usedRows) {
                this.items = items;
                this.layoutInstance = layoutInstance;
                this.contentW = contentW;
                this.contentH = contentH;
                this.cellW = cellW;
                this.cellH = cellH;
                this.usedCols = usedCols;
                this.usedRows = usedRows;
            }
        }

        public static final class HitBox {
            public final Instance inst;
            public final float left, top, right, bottom;
            public final int kind;
            public final boolean seeThrough;

            public HitBox(Instance inst, float left, float top, float right, float bottom, int kind, boolean seeThrough) {
                this.inst = inst;
                this.left = left;
                this.top = top;
                this.right = right;
                this.bottom = bottom;
                this.kind = kind;
                this.seeThrough = seeThrough;
            }

            public boolean contains(float x, float y) {
                return x >= this.left && x < this.right && y >= this.top && y < this.bottom;
            }
        }

        public static UDim2 udimOf(Object v, UDim2 def) {
            return v instanceof UDim2 ? (UDim2) v : def;
        }

        public static int enumVal(Instance i, String prop, int def) {
            Object obj = i != null ? i.get(prop) : null;
            if (obj instanceof EnumValue) return ((EnumValue) obj).getValue();
            if (obj instanceof EnumItemV) return ((EnumItemV) obj).getValue();
            if (obj instanceof Number) return ((Number) obj).intValue();
            return def;
        }

        public static float numVal(Instance i, String prop, float def) {
            Object obj = i != null ? i.get(prop) : null;
            return obj instanceof Number ? ((Number) obj).floatValue() : def;
        }

        public static Instance findChildByClass(Instance p, String cls) {
            if (p == null) return null;
            for (Instance ch : p.getChildren()) {
                if (cls.equals(ch.getClassName())) return ch;
            }
            return null;
        }

        public static float ownScaleOf(Instance c) {
            Instance sc = findChildByClass(c, "UIScale");
            if (sc == null) return 1.0f;
            float v = numVal(sc, "Scale", 1.0f);
            return v > 0.001f ? v : 1.0f;
        }

        public static boolean isScreenGuiEnabled(Instance inst) {
            return inst != null
                    && "ScreenGui".equals(inst.getClassName())
                    && !Boolean.FALSE.equals(inst.get("Enabled"));
        }

        private static void collectEnabledScreenGuis(List<Instance> list, List<Instance> out) {
            if (list == null) return;
            for (Instance inst : list) {
                if (inst == null) continue;
                if (isScreenGuiEnabled(inst)) {
                    out.add(inst);
                } else if ("Folder".equals(inst.getClassName()) || "Configuration".equals(inst.getClassName())) {
                    collectEnabledScreenGuis(inst.getChildren(), out);
                }
            }
        }

        public static List<Instance> filterAndSortScreenGuis(List<Instance> primary, List<Instance> core) {
            ArrayList<Instance> pList = new ArrayList<>();
            collectEnabledScreenGuis(primary, pList);
            Comparator<Instance> byOrder = new Comparator<Instance>() {
                @Override
                public int compare(Instance a, Instance b) {
                    int da = (int) numVal(a, "DisplayOrder", 0.0f);
                    int db = (int) numVal(b, "DisplayOrder", 0.0f);
                    return Integer.compare(da, db);
                }
            };
            java.util.Collections.sort(pList, byOrder);

            ArrayList<Instance> cList = new ArrayList<>();
            collectEnabledScreenGuis(core, cList);
            java.util.Collections.sort(cList, byOrder);

            ArrayList<Instance> combined = new ArrayList<>(pList.size() + cList.size());
            combined.addAll(pList);
            combined.addAll(cList);
            return combined;
        }

        public static boolean isZIndexGlobal(Instance layerCollector) {
            return enumVal(layerCollector, "ZIndexBehavior", 1) == 0;
        }

        /**
         * Computes ScreenGui content bounds [left, top, right, bottom] in pixels.
         * ScreenInsets: 0 = None, 1 = DeviceSafeInsets, 2 = CoreUISafeInsets, 3 = TopbarSafeInsets.
         */
        public static float[] computeScreenGuiRect(
                Instance sg, float width, float height, float density,
                float safeL, float safeT, float safeR, float safeB,
                boolean isCoreGui, boolean playing
        ) {
            boolean ignoreInset = Boolean.TRUE.equals(sg.get("IgnoreGuiInset"));
            Object siObj = sg.get("ScreenInsets");
            Integer screenInsets = null;
            if (siObj instanceof EnumValue) {
                screenInsets = Integer.valueOf(((EnumValue) siObj).getValue());
            } else if (siObj instanceof EnumItemV) {
                screenInsets = Integer.valueOf(((EnumItemV) siObj).getValue());
            } else if (siObj instanceof Number) {
                screenInsets = Integer.valueOf(((Number) siObj).intValue());
            }
            float topBarPx = density * TOPBAR_INSET;

            if (isCoreGui && !playing) {
                return new float[]{0.0f, 0.0f, width, height};
            }
            if (ignoreInset || (screenInsets != null && screenInsets.intValue() == 0)) {
                return new float[]{0.0f, 0.0f, width, height};
            }
            if (screenInsets != null && screenInsets.intValue() == 1) {
                // DeviceSafeInsets: respect hardware display cutout only, no topbar inset
                return new float[]{safeL, safeT, Math.max(safeL, width - safeR), Math.max(safeT, height - safeB)};
            }
            // CoreUISafeInsets (2), TopbarSafeInsets (3), or unset default: apply topbar inset
            float top = Math.max(topBarPx, safeT);
            return new float[]{safeL, top, Math.max(safeL, width - safeR), Math.max(top, height - safeB)};
        }

        public static float[] computePadding(Instance parent, float areaW, float areaH, float density, float scale) {
            Instance pad = findChildByClass(parent, "UIPadding");
            if (pad == null) {
                return new float[]{0.0f, 0.0f, 0.0f, 0.0f};
            }
            float l = udim1D(pad.get("PaddingLeft"), areaW, density, scale);
            float t = udim1D(pad.get("PaddingTop"), areaH, density, scale);
            float r = udim1D(pad.get("PaddingRight"), areaW, density, scale);
            float b = udim1D(pad.get("PaddingBottom"), areaH, density, scale);
            return new float[]{l, t, r, b};
        }

        public static float udim1D(Object udimObj, float parentSize, float density, float scale) {
            if (!(udimObj instanceof UDim)) return 0.0f;
            UDim u = (UDim) udimObj;
            return (u.getScale() * parentSize) + (u.getOffset() * density * scale);
        }

        public static float[] applyConstraints(Instance c, float w0, float h0, float density, float scale) {
            float w = w0;
            float h = h0;
            for (Instance ch : c.getChildren()) {
                String cn = ch.getClassName();
                if ("UIAspectRatioConstraint".equals(cn)) {
                    float aspect = numVal(ch, "AspectRatio", 1.0f);
                    if (aspect <= 0.0f) aspect = 1.0f;
                    int aspectType = enumVal(ch, "AspectType", 0);
                    if (aspectType == 0) {
                        // FitWithinMaxSize
                        if (w <= 0.0f && h > 0.0f) {
                            w = h * aspect;
                        } else if (h <= 0.0f || (w / h) <= aspect) {
                            h = w / aspect;
                        } else {
                            w = h * aspect;
                        }
                    } else {
                        // ScaleWithParentSize
                        if (enumVal(ch, "DominantAxis", 0) == 0) {
                            h = w / aspect;
                        } else {
                            w = h * aspect;
                        }
                    }
                } else if ("UISizeConstraint".equals(cn)) {
                    Object minObj = ch.get("MinSize");
                    Vector2 minV = minObj instanceof Vector2 ? (Vector2) minObj : new Vector2(0.0f, 0.0f);
                    Object maxObj = ch.get("MaxSize");
                    Vector2 maxV = maxObj instanceof Vector2 ? (Vector2) maxObj : new Vector2(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
                    float minW = minV.getX() * density * scale;
                    float maxW = Math.max(minV.getX(), maxV.getX()) * density * scale;
                    float minH = minV.getY() * density * scale;
                    float maxH = Math.max(minV.getY(), maxV.getY()) * density * scale;
                    w = Math.max(minW, Math.min(w, maxW));
                    h = Math.max(minH, Math.min(h, maxH));
                }
            }
            return new float[]{w, h};
        }

        /**
         * Pure-Java size calculation (for JVM tests without Android TextPaint).
         */
        public static float[] resolveSizePure(Instance c, float aw, float ah, float density, float scale) {
            UDim2 s = udimOf(c.get("Size"), new UDim2(0.0f, 0, 0.0f, 0));
            float w = (s.getXs() * aw) + (s.getXo() * density * scale);
            float h = (s.getYs() * ah) + (s.getYo() * density * scale);
            int sc = enumVal(c, "SizeConstraint", 0);
            if (sc == 1) {
                h = (s.getYs() * aw) + (s.getYo() * density * scale);
            } else if (sc == 2) {
                w = (s.getXs() * ah) + (s.getXo() * density * scale);
            }
            float[] cs = applyConstraints(c, w, h, density, scale);
            float own = ownScaleOf(c);
            float w2 = cs[0] * own;
            float h2 = cs[1] * own;
            float effScale = scale * own;
            int auto = enumVal(c, "AutomaticSize", 0);
            if (auto != 0 && !"ScrollingFrame".equals(c.getClassName())) {
                float[] pad = computePadding(c, Math.max(w2, aw), Math.max(h2, ah), density, effScale);
                LayoutResult lr = computeLayout(
                        c, pad[0], pad[1], Math.max(pad[0], Math.max(w2, aw) - pad[2]), Math.max(pad[1], Math.max(h2, ah) - pad[3]),
                        density, effScale,
                        new SizeResolver() {
                            @Override
                            public float[] resolveSize(Instance child, float caw, float cah, float csc) {
                                return resolveSizePure(child, caw, cah, density, csc);
                            }
                        }
                );
                float mx = w2;
                float my = h2;
                for (LayoutBox b : lr.items) {
                    mx = Math.max(mx, b.right);
                    my = Math.max(my, b.bottom);
                }
                mx += pad[2];
                my += pad[3];
                if (auto == 1 || auto == 3) w2 = mx;
                if (auto == 2 || auto == 3) h2 = my;
            }
            return new float[]{Math.max(w2, 0.0f), Math.max(h2, 0.0f)};
        }

        private static void collectFolderGuiObjects(List<Instance> folders, List<Instance> out) {
            for (Instance f : folders) {
                if (f == null) continue;
                for (Instance ch : f.getChildren()) {
                    if (ch == null) continue;
                    if (ch.isA("GuiObject") && !Boolean.FALSE.equals(ch.get("Visible"))) {
                        out.add(ch);
                    } else if ("Folder".equals(ch.getClassName())) {
                        collectFolderGuiObjects(java.util.Collections.singletonList(ch), out);
                    }
                }
            }
        }

        public static LayoutResult computeLayout(
                Instance parent,
                float areaL, float areaT, float areaR, float areaB,
                float density, float childScale,
                SizeResolver sizeResolver
        ) {
            ArrayList<Instance> kids = new ArrayList<>();
            ArrayList<Instance> folders = null;
            Instance listLayout = null;
            Instance gridLayout = null;
            Instance pageLayout = null;
            Instance tableLayout = null;
            for (Instance ch : parent.getChildren()) {
                String cn = ch.getClassName();
                if ("UIListLayout".equals(cn)) {
                    listLayout = ch;
                } else if ("UIGridLayout".equals(cn)) {
                    gridLayout = ch;
                } else if ("UIPageLayout".equals(cn)) {
                    pageLayout = ch;
                } else if ("UITableLayout".equals(cn)) {
                    tableLayout = ch;
                } else if (ch.isA("GuiObject") && !Boolean.FALSE.equals(ch.get("Visible"))) {
                    kids.add(ch);
                } else if ("Folder".equals(cn)) {
                    if (folders == null) folders = new ArrayList<>();
                    folders.add(ch);
                }
            }
            Instance activeLayout = listLayout != null ? listLayout : (gridLayout != null ? gridLayout : (pageLayout != null ? pageLayout : tableLayout));
            if (activeLayout == null && folders != null) {
                collectFolderGuiObjects(folders, kids);
            }
            if (kids.isEmpty()) {
                return new LayoutResult(java.util.Collections.<LayoutBox>emptyList(), activeLayout, 0.0f, 0.0f, 0.0f, 0.0f, 0, 0);
            }
            if (activeLayout != null) {
                final int sortOrder = enumVal(activeLayout, "SortOrder", 2);
                if (sortOrder == 0) {
                    java.util.Collections.sort(kids, new Comparator<Instance>() {
                        @Override
                        public int compare(Instance a, Instance b) {
                            return a.getName().compareTo(b.getName());
                        }
                    });
                } else if (sortOrder == 2) {
                    java.util.Collections.sort(kids, new Comparator<Instance>() {
                        @Override
                        public int compare(Instance a, Instance b) {
                            int la = (int) numVal(a, "LayoutOrder", 0.0f);
                            int lb = (int) numVal(b, "LayoutOrder", 0.0f);
                            return Integer.compare(la, lb);
                        }
                    });
                }
            } else {
                java.util.Collections.sort(kids, new Comparator<Instance>() {
                    @Override
                    public int compare(Instance a, Instance b) {
                        int za = (int) numVal(a, "ZIndex", 1.0f);
                        int zb = (int) numVal(b, "ZIndex", 1.0f);
                        return Integer.compare(za, zb);
                    }
                });
            }

            ArrayList<LayoutBox> out = new ArrayList<>(kids.size());
            float aw = areaR - areaL;
            float ah = areaB - areaT;

            if (listLayout != null) {
                boolean horiz = enumVal(listLayout, "FillDirection", 1) == 0;
                int hAlign = enumVal(listLayout, "HorizontalAlignment", 1);
                int vAlign = enumVal(listLayout, "VerticalAlignment", 1);
                float padPx = udim1D(listLayout.get("Padding"), horiz ? aw : ah, density, childScale);
                int nKids = kids.size();
                float[][] sizes = new float[nKids][];
                float[] flexGrow = new float[nKids];
                float totalGrow = 0.0f;
                float totalPrimary = 0.0f;
                for (int i = 0; i < nKids; i++) {
                    Instance kid = kids.get(i);
                    sizes[i] = sizeResolver.resolveSize(kid, aw, ah, childScale);
                    Instance flex = findChildByClass(kid, "UIFlexItem");
                    if (flex != null) {
                        int fm = enumVal(flex, "FlexMode", 0);
                        float gr = (fm == 1 || fm == 2) ? 1.0f : numVal(flex, "GrowRatio", 0.0f);
                        if (gr > 0.0f) {
                            flexGrow[i] = gr;
                            totalGrow += gr;
                        }
                    }
                    totalPrimary += horiz ? sizes[i][0] : sizes[i][1];
                    if (i > 0) totalPrimary += padPx;
                }
                boolean wraps = Boolean.TRUE.equals(listLayout.get("Wraps"));
                float availPrimary = horiz ? aw : ah;
                if (!wraps && totalGrow > 0.0f && availPrimary > totalPrimary) {
                    float extra = availPrimary - totalPrimary;
                    for (int i = 0; i < nKids; i++) {
                        if (flexGrow[i] > 0.0f) {
                            float add = extra * (flexGrow[i] / totalGrow);
                            if (horiz) sizes[i][0] += add;
                            else sizes[i][1] += add;
                        }
                    }
                    totalPrimary = availPrimary;
                }

                if (wraps) {
                    // Break into wrapped lines first so per-line alignment works accurately
                    ArrayList<int[]> lines = new ArrayList<>();
                    ArrayList<Float> linePrim = new ArrayList<>();
                    ArrayList<Float> lineCross = new ArrayList<>();
                    int lineStart = 0;
                    float curP = 0.0f;
                    float curC = 0.0f;
                    for (int i = 0; i < nKids; i++) {
                        float pSz = horiz ? sizes[i][0] : sizes[i][1];
                        float cSz = horiz ? sizes[i][1] : sizes[i][0];
                        if (i > lineStart && (curP + padPx + pSz) > availPrimary + 0.5f) {
                            lines.add(new int[]{lineStart, i});
                            linePrim.add(curP);
                            lineCross.add(curC);
                            lineStart = i;
                            curP = pSz;
                            curC = cSz;
                        } else {
                            curP = (i == lineStart) ? pSz : (curP + padPx + pSz);
                            if (cSz > curC) curC = cSz;
                        }
                    }
                    lines.add(new int[]{lineStart, nKids});
                    linePrim.add(curP);
                    lineCross.add(curC);

                    float totalCross = 0.0f;
                    for (int li = 0; li < lineCross.size(); li++) {
                        totalCross += lineCross.get(li);
                        if (li > 0) totalCross += padPx;
                    }
                    float crossCursor;
                    if (horiz) {
                        if (vAlign == 0) crossCursor = areaT + ((ah - totalCross) * 0.5f);
                        else if (vAlign == 2) crossCursor = areaB - totalCross;
                        else crossCursor = areaT;
                    } else {
                        if (hAlign == 0) crossCursor = areaL + ((aw - totalCross) * 0.5f);
                        else if (hAlign == 2) crossCursor = areaR - totalCross;
                        else crossCursor = areaL;
                    }

                    for (int li = 0; li < lines.size(); li++) {
                        int sIdx = lines.get(li)[0];
                        int eIdx = lines.get(li)[1];
                        float lp = linePrim.get(li);
                        float lc = lineCross.get(li);
                        float pCursor;
                        if (horiz) {
                            if (hAlign == 0) pCursor = areaL + ((aw - lp) * 0.5f);
                            else if (hAlign == 2) pCursor = areaR - lp;
                            else pCursor = areaL;
                        } else {
                            if (vAlign == 0) pCursor = areaT + ((ah - lp) * 0.5f);
                            else if (vAlign == 2) pCursor = areaB - lp;
                            else pCursor = areaT;
                        }
                        for (int i = sIdx; i < eIdx; i++) {
                            float cw = sizes[i][0];
                            float chh = sizes[i][1];
                            float x = horiz ? pCursor : crossCursor;
                            float y = horiz ? crossCursor : pCursor;
                            out.add(new LayoutBox(kids.get(i), x, y, x + cw, y + chh, true));
                            pCursor += (horiz ? cw : chh) + padPx;
                        }
                        crossCursor += lc + padPx;
                    }
                } else {
                    float cursor;
                    if (horiz) {
                        if (hAlign == 0) cursor = areaL + ((aw - totalPrimary) * 0.5f);
                        else if (hAlign == 2) cursor = areaR - totalPrimary;
                        else cursor = areaL;
                    } else {
                        if (vAlign == 0) cursor = areaT + ((ah - totalPrimary) * 0.5f);
                        else if (vAlign == 2) cursor = areaB - totalPrimary;
                        else cursor = areaT;
                    }
                    for (int i = 0; i < nKids; i++) {
                        float cw = sizes[i][0];
                        float chh = sizes[i][1];
                        float x, y;
                        if (horiz) {
                            x = cursor;
                            if (vAlign == 0) y = areaT + ((ah - chh) * 0.5f);
                            else if (vAlign == 2) y = areaB - chh;
                            else y = areaT;
                            cursor += cw + padPx;
                        } else {
                            if (hAlign == 0) x = areaL + ((aw - cw) * 0.5f);
                            else if (hAlign == 2) x = areaR - cw;
                            else x = areaL;
                            y = cursor;
                            cursor += chh + padPx;
                        }
                        out.add(new LayoutBox(kids.get(i), x, y, x + cw, y + chh, true));
                    }
                }

                float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
                float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
                for (LayoutBox b : out) {
                    if (b.left < minX) minX = b.left;
                    if (b.top < minY) minY = b.top;
                    if (b.right > maxX) maxX = b.right;
                    if (b.bottom > maxY) maxY = b.bottom;
                }
                return new LayoutResult(out, listLayout, Math.max(0.0f, maxX - minX), Math.max(0.0f, maxY - minY), 0.0f, 0.0f, 0, 0);
            }

            if (gridLayout != null) {
                UDim2 cs = udimOf(gridLayout.get("CellSize"), new UDim2(0.0f, 100, 0.0f, 100));
                UDim2 cp = udimOf(gridLayout.get("CellPadding"), new UDim2(0.0f, 5, 0.0f, 5));
                float cw = Math.max(1.0f, (cs.getXs() * aw) + (cs.getXo() * density * childScale));
                float chh = Math.max(1.0f, (cs.getYs() * ah) + (cs.getYo() * density * childScale));
                float[] constrainedCell = applyConstraints(gridLayout, cw, chh, density, childScale);
                cw = Math.max(1.0f, constrainedCell[0]);
                chh = Math.max(1.0f, constrainedCell[1]);
                float px = (cp.getXs() * aw) + (cp.getXo() * density * childScale);
                float py = (cp.getYs() * ah) + (cp.getYo() * density * childScale);
                boolean horiz = enumVal(gridLayout, "FillDirection", 0) == 0;
                int maxCells = (int) numVal(gridLayout, "FillDirectionMaxCells", 0.0f);
                int hAlign = enumVal(gridLayout, "HorizontalAlignment", 1);
                int vAlign = enumVal(gridLayout, "VerticalAlignment", 1);
                int startCorner = enumVal(gridLayout, "StartCorner", 0);
                int nKids = kids.size();
                int cols, rows;
                if (horiz) {
                    int fitCols = Math.max(1, (int) Math.floor((aw + px) / Math.max(cw + px, 1.0f)));
                    if (maxCells > 0) fitCols = Math.min(fitCols, maxCells);
                    cols = Math.max(1, fitCols);
                    rows = (nKids + cols - 1) / cols;
                } else {
                    int fitRows = Math.max(1, (int) Math.floor((ah + py) / Math.max(chh + py, 1.0f)));
                    if (maxCells > 0) fitRows = Math.min(fitRows, maxCells);
                    rows = Math.max(1, fitRows);
                    cols = (nKids + rows - 1) / rows;
                }
                int usedCols = horiz ? Math.min(nKids, cols) : cols;
                int usedRows = horiz ? rows : Math.min(nKids, rows);
                float contentW = usedCols > 0 ? (usedCols * cw) + (Math.max(0, usedCols - 1) * px) : 0.0f;
                float contentH = usedRows > 0 ? (usedRows * chh) + (Math.max(0, usedRows - 1) * py) : 0.0f;
                float originX = (hAlign == 0) ? (areaL + ((aw - contentW) * 0.5f)) : (hAlign == 2 ? (areaR - contentW) : areaL);
                float originY = (vAlign == 0) ? (areaT + ((ah - contentH) * 0.5f)) : (vAlign == 2 ? (areaB - contentH) : areaT);
                boolean flipX = (startCorner == 1 || startCorner == 3);
                boolean flipY = (startCorner == 2 || startCorner == 3);
                for (int i = 0; i < nKids; i++) {
                    int col = horiz ? (i % cols) : (i / rows);
                    int row = horiz ? (i / cols) : (i % rows);
                    if (flipX) col = (usedCols - 1) - col;
                    if (flipY) row = (usedRows - 1) - row;
                    float x = originX + (col * (cw + px));
                    float y = originY + (row * (chh + py));
                    out.add(new LayoutBox(kids.get(i), x, y, x + cw, y + chh, true));
                }
                return new LayoutResult(out, gridLayout, contentW, contentH, cw, chh, usedCols, usedRows);
            }

            if (pageLayout != null) {
                boolean horiz = enumVal(pageLayout, "FillDirection", 0) == 0;
                float padPx = udim1D(pageLayout.get("Padding"), horiz ? aw : ah, density, childScale);
                Object curPageObj = pageLayout.get("CurrentPage");
                int curIdx = 0;
                if (curPageObj instanceof Instance) {
                    int found = kids.indexOf(curPageObj);
                    if (found >= 0) curIdx = found;
                }
                for (int i = 0; i < kids.size(); i++) {
                    int rel = i - curIdx;
                    float x = horiz ? (areaL + rel * (aw + padPx)) : areaL;
                    float y = horiz ? areaT : (areaT + rel * (ah + padPx));
                    out.add(new LayoutBox(kids.get(i), x, y, x + aw, y + ah, true));
                }
                return new LayoutResult(out, pageLayout, aw, ah, 0.0f, 0.0f, 0, 0);
            }

            if (tableLayout != null) {
                boolean horiz = enumVal(tableLayout, "FillDirection", 1) == 0;
                UDim2 pad = udimOf(tableLayout.get("Padding"), new UDim2(0.0f, 0, 0.0f, 0));
                float px = (pad.getXs() * aw) + (pad.getXo() * density * childScale);
                float py = (pad.getYs() * ah) + (pad.getYo() * density * childScale);
                int nKids = kids.size();
                float cellW = horiz ? Math.max(1.0f, (aw - px * Math.max(0, nKids - 1)) / Math.max(1, nKids)) : aw;
                float cellH = horiz ? ah : Math.max(1.0f, (ah - py * Math.max(0, nKids - 1)) / Math.max(1, nKids));
                for (int i = 0; i < nKids; i++) {
                    float x = horiz ? (areaL + i * (cellW + px)) : areaL;
                    float y = horiz ? areaT : (areaT + i * (cellH + py));
                    out.add(new LayoutBox(kids.get(i), x, y, x + cellW, y + cellH, true));
                }
                return new LayoutResult(out, tableLayout, aw, ah, 0.0f, 0.0f, 0, 0);
            }

            for (Instance c : kids) {
                float[] sz = sizeResolver.resolveSize(c, aw, ah, childScale);
                float cw = sz[0];
                float chh = sz[1];
                UDim2 pos = udimOf(c.get("Position"), new UDim2(0.0f, 0, 0.0f, 0));
                Object apObj = c.get("AnchorPoint");
                Vector2 ap = apObj instanceof Vector2 ? (Vector2) apObj : new Vector2(0.0f, 0.0f);
                float px = areaL + (pos.getXs() * aw) + (pos.getXo() * density * childScale) - (ap.getX() * cw);
                float py = areaT + (pos.getYs() * ah) + (pos.getYo() * density * childScale) - (ap.getY() * chh);
                out.add(new LayoutBox(c, px, py, px + cw, py + chh, false));
            }
            return new LayoutResult(out, null, 0.0f, 0.0f, 0.0f, 0.0f, 0, 0);
        }

        public static HitBox hitTestButton(List<HitBox> hits, float x, float y) {
            for (int idx = hits.size() - 1; idx >= 0; idx--) {
                HitBox h = hits.get(idx);
                if (!h.contains(x, y)) continue;
                if (h.kind != SINK) return h;
                if (!h.seeThrough) return null;
            }
            return null;
        }
    }
}
