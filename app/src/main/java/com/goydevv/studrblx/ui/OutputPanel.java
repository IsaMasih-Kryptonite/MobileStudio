package com.goydevv.studrblx.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.goydevv.studrblx.core.OutputLog;
import com.goydevv.studrblx.core.Studio;
import com.goydevv.studrblx.model.DataModel;
import com.goydevv.studrblx.model.Instance;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;
import kotlin.text.StringsKt;

public final class OutputPanel extends LinearLayout {
    private static final int MAX_VISIBLE_ENTRIES = 1000;
    private static final int MAX_LINE_CHARS = 2000;
    private static final int MAX_COPY_CHARS = 500000;
    private static final int MAX_HISTORY = 100;
    private static final Pattern SCRIPT_LINE_PATTERN = Pattern.compile("([A-Za-z_][\\w.]*):(\\d+)");

    private final Adapter adapter;
    private final ArrayList<OutputLog.Entry> entries;
    private final SimpleDateFormat fmt;
    private final ArrayList<String> history;
    private int historyIdx;
    private final RecyclerView list;
    private final Function1<OutputLog.Entry, Unit> listener;
    private final Handler mainHandler;
    private final AtomicBoolean pending;
    private final Runnable reloadRunnable;
    private int filterMode = 0;
    private final Studio studio;

    public OutputPanel(Context ctx, Studio studio) {
        super(ctx);
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        Intrinsics.checkNotNullParameter(studio, "studio");
        this.studio = studio;
        this.entries = new ArrayList<>();
        this.list = new RecyclerView(ctx);
        this.adapter = new Adapter();
        this.fmt = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
        this.history = new ArrayList<>();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.pending = new AtomicBoolean(false);
        this.reloadRunnable = new Runnable() {
            @Override
            public void run() {
                OutputPanel.this.pending.set(false);
                OutputPanel.this.reload();
            }
        };
        this.listener = new Function1<OutputLog.Entry, Unit>() {
            @Override
            public Unit invoke(OutputLog.Entry entry) {
                OutputPanel.this.schedule();
                return Unit.INSTANCE;
            }
        };

        setOrientation(LinearLayout.VERTICAL);
        setBackgroundColor(Ui.INSTANCE.getPANEL());
        LinearLayout h = Ui.INSTANCE.header(ctx, "Output", Icons.general$default(Icons.INSTANCE, ctx, "Output", null, 4, null));
        h.addView(Ui.iconButton$default(Ui.INSTANCE, ctx, Icons.INSTANCE.general(ctx, "Cleanup", "Clr"), 0.0f, new Function1<View, Unit>() {
            @Override
            public Unit invoke(View v) {
                OutputLog.INSTANCE.clear();
                return Unit.INSTANCE;
            }
        }, 4, null));

        final TextView filterBtn = new TextView(ctx);
        filterBtn.setText("All");
        filterBtn.setTextSize(11.0f);
        filterBtn.setTextColor(Ui.INSTANCE.getACCENT());
        filterBtn.setPadding(Ui.INSTANCE.dp(ctx, 8.0f), Ui.INSTANCE.dp(ctx, 2.0f), Ui.INSTANCE.dp(ctx, 8.0f), Ui.INSTANCE.dp(ctx, 2.0f));
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OutputPanel.this.filterMode = (OutputPanel.this.filterMode + 1) % 3;
                filterBtn.setText(OutputPanel.this.filterMode == 0 ? "All" : (OutputPanel.this.filterMode == 1 ? "Warn+" : "Errors"));
                OutputPanel.this.reload();
            }
        });
        h.addView(filterBtn, Ui.INSTANCE.lp(-2, -2, 0.0f));

        final TextView copyBtn = new TextView(ctx);
        copyBtn.setText("Copy");
        copyBtn.setTextSize(11.0f);
        copyBtn.setTextColor(Ui.INSTANCE.getTEXT());
        copyBtn.setPadding(Ui.INSTANCE.dp(ctx, 8.0f), Ui.INSTANCE.dp(ctx, 2.0f), Ui.INSTANCE.dp(ctx, 8.0f), Ui.INSTANCE.dp(ctx, 2.0f));
        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pm = new PopupMenu(v.getContext(), v);
                pm.getMenu().add(0, 1, 0, "Copy Unique Errors (No Loop)");
                pm.getMenu().add(0, 2, 1, "Copy All Errors");
                pm.getMenu().add(0, 3, 2, "Copy Unique Warn + Errors");
                pm.getMenu().add(0, 4, 3, "Copy Full Output");
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        OutputPanel.this.copyOutput(item.getItemId());
                        return true;
                    }
                });
                pm.show();
            }
        });
        h.addView(copyBtn, Ui.INSTANCE.lp(-2, -2, 0.0f));
        addView(h, Ui.lp$default(Ui.INSTANCE, -1, -2, 0.0f, 4, null));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ctx);
        linearLayoutManager.setStackFromEnd(true);
        this.list.setLayoutManager(linearLayoutManager);
        this.list.setAdapter(this.adapter);
        this.list.setItemAnimator(null);
        addView(this.list, Ui.INSTANCE.lp(-1, 0, 1.0f));

        LinearLayout bar = new LinearLayout(ctx);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setBackgroundColor(Ui.INSTANCE.getHEADER());
        final EditText cmd = Ui.input$default(Ui.INSTANCE, ctx, "", "Run a command (Lua)", false, false, 24, null);
        cmd.setTypeface(Typeface.MONOSPACE);
        cmd.setTextSize(12.0f);
        cmd.setImeOptions(2);
        cmd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent ev) {
                if (id != 2) {
                    if (!(ev != null && ev.getKeyCode() == KeyEvent.KEYCODE_ENTER && ev.getAction() == KeyEvent.ACTION_DOWN)) {
                        return false;
                    }
                }
                OutputPanel.this.runCommandBar(cmd);
                return true;
            }
        });
        bar.addView(cmd, Ui.INSTANCE.lp(0, -2, 1.0f));
        bar.addView(Ui.iconButton$default(Ui.INSTANCE, ctx, Icons.INSTANCE.general(ctx, "Back", "Prev"), 0.0f, new Function1<View, Unit>() {
            @Override
            public Unit invoke(View v) {
                if (!OutputPanel.this.history.isEmpty()) {
                    OutputPanel.this.historyIdx = RangesKt.coerceAtLeast(OutputPanel.this.historyIdx - 1, 0);
                    cmd.setText(OutputPanel.this.history.get(OutputPanel.this.historyIdx));
                    cmd.setSelection(cmd.getText().length());
                }
                return Unit.INSTANCE;
            }
        }, 4, null));
        bar.addView(Ui.iconButton$default(Ui.INSTANCE, ctx, Icons.INSTANCE.general(ctx, "RunScript", "Run"), 0.0f, new Function1<View, Unit>() {
            @Override
            public Unit invoke(View v) {
                OutputPanel.this.runCommandBar(cmd);
                return Unit.INSTANCE;
            }
        }, 4, null));
        addView(bar, Ui.lp$default(Ui.INSTANCE, -1, -2, 0.0f, 4, null));
        reload();
    }

    private void runCommandBar(EditText cmd) {
        if (cmd == null || cmd.getText() == null) {
            return;
        }
        String code = cmd.getText().toString();
        if (StringsKt.isBlank(code)) {
            return;
        }
        this.history.add(code);
        if (this.history.size() > MAX_HISTORY) {
            this.history.remove(0);
        }
        this.historyIdx = this.history.size();
        cmd.setText("");
        try {
            this.studio.runCommand(code);
        } catch (Throwable t) {
            String msg = t.getMessage();
            OutputLog.INSTANCE.error("Command error: " + (msg != null && !msg.isEmpty() ? msg : t.getClass().getSimpleName()));
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        OutputLog.INSTANCE.getListeners().addIfAbsent(this.listener);
        reload();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        OutputLog.INSTANCE.getListeners().remove(this.listener);
        this.mainHandler.removeCallbacks(this.reloadRunnable);
        this.pending.set(false);
    }

    private void schedule() {
        if (this.pending.compareAndSet(false, true)) {
            this.mainHandler.postDelayed(this.reloadRunnable, 100L);
        }
    }

    private void copyOutput(int mode) {
        List<OutputLog.Entry> snap = OutputLog.INSTANCE.snapshot();
        StringBuilder sb = new StringBuilder();
        int count = 0;
        boolean truncated = false;
        if (mode == 1 || mode == 3) {
            LinkedHashMap<String, Integer> dedup = new LinkedHashMap<>();
            for (OutputLog.Entry e : snap) {
                if (e == null) {
                    continue;
                }
                OutputLog.Level lv = e.getLevel();
                if (mode == 1 && lv != OutputLog.Level.ERROR) continue;
                if (mode == 3 && lv != OutputLog.Level.ERROR && lv != OutputLog.Level.WARN) continue;
                String raw = e.getText() != null ? e.getText().trim() : "";
                String key = (mode == 3 && lv != null ? "[" + lv.name() + "] " : "") + raw;
                Integer prev = dedup.get(key);
                dedup.put(key, prev == null ? 1 : prev + 1);
            }
            for (Map.Entry<String, Integer> it : dedup.entrySet()) {
                if (sb.length() >= MAX_COPY_CHARS) {
                    truncated = true;
                    break;
                }
                if (sb.length() > 0) sb.append("\n");
                sb.append(it.getKey());
                if (it.getValue() > 1) {
                    sb.append(" (x").append(it.getValue()).append(")");
                }
                count++;
            }
        } else {
            for (OutputLog.Entry e : snap) {
                if (e == null) {
                    continue;
                }
                OutputLog.Level lv = e.getLevel();
                if (mode == 2 && lv != OutputLog.Level.ERROR) continue;
                if (sb.length() >= MAX_COPY_CHARS) {
                    truncated = true;
                    break;
                }
                if (sb.length() > 0) sb.append("\n");
                String lvlName = lv != null ? lv.name() : "INFO";
                String txt = e.getText() != null ? e.getText() : "";
                sb.append(formatTime(e.getTime())).append("  [").append(lvlName).append("] ").append(txt);
                count++;
            }
        }
        if (count == 0) {
            Toast.makeText(getContext(), "Nothing matching to copy", Toast.LENGTH_SHORT).show();
            return;
        }
        if (truncated) {
            sb.append("\n... [output truncated to fit clipboard]");
        }
        try {
            ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("StudioRblx Output", sb.toString()));
            }
            String label = mode == 1 ? " unique error(s)" : (mode == 2 ? " error(s)" : (mode == 3 ? " unique warn/error(s)" : " output line(s)"));
            Toast.makeText(getContext(), "Copied " + count + label, Toast.LENGTH_SHORT).show();
        } catch (Throwable t) {
            Toast.makeText(getContext(), "Clipboard copy failed (output too large)", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatTime(long timeMs) {
        synchronized (this.fmt) {
            return this.fmt.format(new Date(timeMs));
        }
    }

    private void reload() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            schedule();
            return;
        }
        boolean wasEmpty = this.entries.isEmpty();
        boolean atBottom = wasEmpty || !this.list.canScrollVertically(1);

        List<OutputLog.Entry> snap = OutputLog.INSTANCE.snapshot();
        ArrayList<OutputLog.Entry> filtered = new ArrayList<>();
        for (OutputLog.Entry e : snap) {
            if (e == null) {
                continue;
            }
            OutputLog.Level lv = e.getLevel();
            if (this.filterMode == 1 && lv != OutputLog.Level.WARN && lv != OutputLog.Level.ERROR) continue;
            if (this.filterMode == 2 && lv != OutputLog.Level.ERROR) continue;
            filtered.add(e);
        }

        this.entries.clear();
        int total = filtered.size();
        int startIdx = Math.max(0, total - MAX_VISIBLE_ENTRIES);
        for (int i = startIdx; i < total; i++) {
            this.entries.add(filtered.get(i));
        }
        this.adapter.notifyDataSetChanged();
        if (atBottom && !this.entries.isEmpty()) {
            this.list.scrollToPosition(this.entries.size() - 1);
        }
    }

    private Instance findScriptByRef(String ref) {
        if (ref == null || ref.isEmpty() || this.studio == null) {
            return null;
        }
        DataModel dm = this.studio.getModel();
        Object lock = this.studio.getLock();
        if (dm == null) {
            return null;
        }
        if (lock != null) {
            synchronized (lock) {
                return findScriptLocked(dm, ref);
            }
        }
        return findScriptLocked(dm, ref);
    }

    private Instance findScriptLocked(DataModel dm, String ref) {
        String[] parts = ref.split("\\.");
        if (parts.length > 1) {
            Instance cur = dm;
            int start = "game".equalsIgnoreCase(parts[0]) ? 1 : 0;
            for (int i = start; i < parts.length && cur != null; i++) {
                cur = cur.findFirstChild(parts[i], false);
            }
            if (cur != null && cur.isA("LuaSourceContainer")) {
                return cur;
            }
        }
        String leaf = parts[parts.length - 1];
        return findScriptRecursive(dm, ref, leaf, 0);
    }

    private Instance findScriptRecursive(Instance node, String fullRef, String leafName, int depth) {
        if (node == null || depth > 24) {
            return null;
        }
        if (node.isA("LuaSourceContainer")) {
            String fn = node.fullName();
            if (fullRef.equals(fn) || (fn != null && fn.endsWith("." + fullRef))) {
                return node;
            }
            if (leafName.equals(node.getName())) {
                return node;
            }
        }
        List<Instance> children = node.getChildren();
        if (children != null) {
            for (Instance ch : children) {
                Instance found = findScriptRecursive(ch, fullRef, leafName, depth + 1);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private final class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public Adapter() {
        }

        @Override
        public int getItemCount() {
            return OutputPanel.this.entries.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Intrinsics.checkNotNullParameter(parent, "parent");
            Context context = parent.getContext();
            TextView tv = Ui.text$default(Ui.INSTANCE, context, "", 12.0f, 0, false, 24, null);
            tv.setSingleLine(false);
            tv.setTypeface(Typeface.MONOSPACE);
            int padH = Ui.INSTANCE.dp(context, 8.0f);
            int padV = Ui.INSTANCE.dp(context, 1.0f);
            tv.setPadding(padH, padV, padH, padV);
            tv.setTextIsSelectable(true);
            tv.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
            return new RecyclerView.ViewHolder(tv) {
            };
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder h, int position) {
            Intrinsics.checkNotNullParameter(h, "h");
            if (position < 0 || position >= OutputPanel.this.entries.size()) {
                return;
            }
            OutputLog.Entry e = OutputPanel.this.entries.get(position);
            if (e == null || !(h.itemView instanceof TextView)) {
                return;
            }
            TextView t = (TextView) h.itemView;
            String rawText = e.getText() != null ? e.getText() : "";
            if (rawText.length() > MAX_LINE_CHARS) {
                rawText = rawText.substring(0, MAX_LINE_CHARS) + "… [truncated]";
            }
            String prefix = OutputPanel.this.formatTime(e.getTime()) + "  ";
            String full = prefix + rawText;

            int color = resolveColor(e.getLevel());
            t.setTextColor(color);

            OutputLog.Level lv = e.getLevel();
            if (lv == OutputLog.Level.ERROR || lv == OutputLog.Level.WARN) {
                SpannableStringBuilder ssb = buildLinkedText(full, prefix.length(), color);
                if (ssb != null) {
                    t.setTextIsSelectable(false);
                    t.setText(ssb);
                    t.setMovementMethod(LinkMovementMethod.getInstance());
                    t.setHighlightColor(0x334080FF);
                    return;
                }
            }
            t.setMovementMethod(null);
            t.setTextIsSelectable(true);
            t.setText(full);
        }

        private int resolveColor(OutputLog.Level level) {
            if (level == null) {
                return Ui.INSTANCE.getTEXT();
            }
            switch (level) {
                case PRINT:
                    return Ui.INSTANCE.getTEXT();
                case INFO:
                    return Ui.INFO;
                case WARN:
                    return Ui.WARN;
                case ERROR:
                    return Ui.ERROR;
                default:
                    return Ui.INSTANCE.getTEXT();
            }
        }

        private SpannableStringBuilder buildLinkedText(String full, int bodyStart, final int baseColor) {
            Matcher m = SCRIPT_LINE_PATTERN.matcher(full);
            m.region(bodyStart, full.length());
            SpannableStringBuilder ssb = null;
            int matchCount = 0;
            while (m.find() && matchCount < 8) {
                final String scriptRef = m.group(1);
                final String lineStr = m.group(2);
                final Instance target = OutputPanel.this.findScriptByRef(scriptRef);
                if (target != null) {
                    if (ssb == null) {
                        ssb = new SpannableStringBuilder(full);
                    }
                    matchCount++;
                    ssb.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            try {
                                OutputPanel.this.studio.openScript(target);
                                Toast.makeText(widget.getContext(), "Opened " + target.getName() + " (line " + lineStr + ")", Toast.LENGTH_SHORT).show();
                            } catch (Throwable ignored) {
                            }
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            ds.setColor(baseColor);
                            ds.setUnderlineText(true);
                        }
                    }, m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            return ssb;
        }
    }
}
