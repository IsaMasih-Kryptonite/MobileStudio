package com.goydevv.studrblx.lua;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.collections.SetsKt;
import kotlin.enums.EnumEntries;
import kotlin.enums.EnumEntriesKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;
import kotlin.text.StringsKt;

/* JADX INFO: compiled from: LuauTranspiler.kt */
/* JADX INFO: loaded from: classes4.dex */
public final class LuauTranspiler {
    private static volatile String lastFailure;
    public static final LuauTranspiler INSTANCE = new LuauTranspiler();
    private static final Set<String> keywords = SetsKt.setOf(new String[]{"and", "break", "do", "else", "elseif", "end", "false", "for", "function", "goto", "if", "in", "local", "nil", "not", "or", "repeat", "return", "then", "true", "until", "while"});
    private static final List<String> ops = CollectionsKt.listOf(new String[]{"...", "..=", "//=", "..", "==", "~=", "<=", ">=", "//", "::", "->", "+=", "-=", "*=", "/=", "%=", "^=", "+", "-", "*", "/", "%", "^", "#", "&", "|", "~", "<", ">", "=", "(", ")", "{", "}", "[", "]", ";", ":", ",", ".", "?", "@", "!"});

    /* JADX INFO: compiled from: LuauTranspiler.kt */
    private enum K {
        NAME,
        KEYWORD,
        NUMBER,
        STRING,
        OP,
        EOF;

        private static final /* synthetic */ EnumEntries $ENTRIES = EnumEntriesKt.enumEntries(values());

        public static EnumEntries<K> getEntries() {
            return $ENTRIES;
        }
    }

    /* JADX INFO: compiled from: LuauTranspiler.kt */
    private static final class ParseError extends Exception {
    }

    private LuauTranspiler() {
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: compiled from: LuauTranspiler.kt */
    static final class Tok {
        private final K k;
        private String out;
        private String prefix;
        private String suffix;
        private final String text;
        private final String trivia;

        public Tok(K k, String text, String trivia) {
            Intrinsics.checkNotNullParameter(k, "k");
            Intrinsics.checkNotNullParameter(text, "text");
            Intrinsics.checkNotNullParameter(trivia, "trivia");
            this.k = k;
            this.text = text;
            this.trivia = trivia;
            this.out = this.text;
            this.prefix = "";
            this.suffix = "";
        }

        public final K getK() {
            return this.k;
        }

        public final String getText() {
            return this.text;
        }

        public final String getTrivia() {
            return this.trivia;
        }

        public final String getOut() {
            return this.out;
        }

        public final void setOut(String str) {
            Intrinsics.checkNotNullParameter(str, "<set-?>");
            this.out = str;
        }

        public final String getPrefix() {
            return this.prefix;
        }

        public final void setPrefix(String str) {
            Intrinsics.checkNotNullParameter(str, "<set-?>");
            this.prefix = str;
        }

        public final String getSuffix() {
            return this.suffix;
        }

        public final void setSuffix(String str) {
            Intrinsics.checkNotNullParameter(str, "<set-?>");
            this.suffix = str;
        }

        public final boolean isOp(String s) {
            Intrinsics.checkNotNullParameter(s, "s");
            return this.k == K.OP && Intrinsics.areEqual(this.text, s);
        }

        public final boolean isKw(String s) {
            Intrinsics.checkNotNullParameter(s, "s");
            return this.k == K.KEYWORD && Intrinsics.areEqual(this.text, s);
        }

        public final boolean isName(String s) {
            Intrinsics.checkNotNullParameter(s, "s");
            return this.k == K.NAME && Intrinsics.areEqual(this.text, s);
        }
    }

    public final String getLastFailure() {
        return lastFailure;
    }

    public final void setLastFailure(String str) {
        lastFailure = str;
    }

    public static /* synthetic */ String transpile$default(LuauTranspiler luauTranspiler, String str, boolean z, int i, Object obj) {
        if ((i & 2) != 0) {
            z = false;
        }
        try { return luauTranspiler.transpile(str, z); } catch (Throwable _ktEx) { throw com.goydevv.studrblx.util.KtCompat.sneakyThrow(_ktEx); }
    }

    private static boolean isWordChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_';
    }

    public final String transpile(String src, boolean hoistTopLocals) throws Exception {
        return transpileInternal(src, hoistTopLocals, true);
    }

    private final String transpileInternal(String src, boolean hoistTopLocals, boolean recordFailure) {
        String str;
        Intrinsics.checkNotNullParameter(src, "src");
        if (recordFailure) {
            lastFailure = null;
        }
        Parser parser = null;
        ArrayList<Tok> arrayListLex = null;
        try {
            arrayListLex = lex(src);
            try {
                parser = new Parser(arrayListLex, hoistTopLocals);
                parser.run();
                StringBuilder sb = new StringBuilder(src.length() + 64);
                Iterator<Tok> it = arrayListLex.iterator();
                Intrinsics.checkNotNullExpressionValue(it, "iterator(...)");
                Tok prev = null;
                while (it.hasNext()) {
                    Tok next = it.next();
                    Intrinsics.checkNotNullExpressionValue(next, "next(...)");
                    Tok t = next;
                    if (prev != null && prev.getK() == K.NUMBER && t.isOp("..") && t.getTrivia().isEmpty()) {
                        sb.append(' ');
                    }
                    String trivia = t.getTrivia();
                    String prefix = t.getPrefix();
                    String out = t.getOut();
                    String suffix = t.getSuffix();
                    if (trivia.isEmpty() && sb.length() > 0 && isWordChar(sb.charAt(sb.length() - 1))) {
                        char firstOut = 0;
                        if (!prefix.isEmpty()) {
                            firstOut = prefix.charAt(0);
                        } else if (!out.isEmpty()) {
                            firstOut = out.charAt(0);
                        } else if (!suffix.isEmpty()) {
                            firstOut = suffix.charAt(0);
                        }
                        if (firstOut != 0 && isWordChar(firstOut)) {
                            sb.append(' ');
                        }
                    }
                    sb.append(trivia);
                    sb.append(prefix);
                    sb.append(out);
                    sb.append(suffix);
                    prev = t;
                }
                String string = sb.toString();
                Intrinsics.checkNotNull(string);
                return string;
            } catch (Throwable e2) {
                if (!(e2 instanceof ParseError) && !(e2 instanceof IndexOutOfBoundsException) && !(e2 instanceof StackOverflowError)) {
                    if (recordFailure) {
                        lastFailure = e2.getClass().getSimpleName();
                    }
                    return src;
                }
                ArrayList<Tok> arrayList = arrayListLex;
                int pp = parser != null ? parser.getP() : 0;
                if (arrayList == null) {
                    str = null;
                } else {
                    int safePp = Math.min(Math.max(pp, 0), arrayList.size());
                    int i = 0;
                    for (Tok tok : (List<Tok>) CollectionsKt.take(arrayList, safePp)) {
                        CharSequence trivia = tok.getTrivia();
                        int i2 = 0;
                        for (int i3 = 0; i3 < trivia.length(); i3++) {
                            if (trivia.charAt(i3) == '\n') {
                                i2++;
                            }
                        }
                        CharSequence text = tok.getText();
                        int i4 = 0;
                        for (int i5 = 0; i5 < text.length(); i5++) {
                            if (text.charAt(i5) == '\n') {
                                i4++;
                            }
                        }
                        i += i2 + i4;
                    }
                    int line = i + 1;
                    int fromIdx = Math.min(Math.max(safePp - 6, 0), arrayList.size());
                    int toIdx = Math.min(Math.max(safePp + 4, fromIdx), arrayList.size());
                    List<Tok> listSubList = arrayList.subList(fromIdx, toIdx);
                    Intrinsics.checkNotNullExpressionValue(listSubList, "subList(...)");
                    str = "line " + line + " near: " + CollectionsKt.joinToString(listSubList, " ", "", "", -1, "...", new Function1() { // from class: com.goydevv.studrblx.lua.LuauTranspiler$$ExternalSyntheticLambda0
                        @Override // kotlin.jvm.functions.Function1
                        public final Object invoke(Object obj) {
                            return LuauTranspiler.transpile$lambda$3((LuauTranspiler.Tok) obj);
                        }
                    });
                }
                if (recordFailure) {
                    lastFailure = str;
                }
                return src;
            }
        } catch (Throwable e3) {
            return src;
        }
    }

    static final CharSequence transpile$lambda$3(Tok it) {
        Intrinsics.checkNotNullParameter(it, "it");
        return it.getText();
    }

    private final String relevel(String text, String prefix) {
        String body0 = StringsKt.removePrefix(text, (CharSequence) prefix);
        if (!StringsKt.startsWith(body0, "[[", false) || !StringsKt.endsWith(body0, "]]", false)) {
            return text;
        }
        String inner = body0.substring(2, body0.length() - 2);
        Intrinsics.checkNotNullExpressionValue(inner, "substring(...)");
        if (!StringsKt.contains((CharSequence) inner, (CharSequence) "[[", false)) {
            return text;
        }
        int lvl = 1;
        while (StringsKt.contains((CharSequence) inner, (CharSequence) ("]" + StringsKt.repeat("=", lvl) + "]"), false)
                || StringsKt.endsWith(inner, "]" + StringsKt.repeat("=", lvl), false)) {
            lvl++;
        }
        String eq = StringsKt.repeat("=", lvl);
        return prefix + "[" + eq + "[" + inner + "]" + eq + "]";
    }

    /* JADX WARN: Code duplicated, block: B:145:0x02c7  */
    /* JADX WARN: Code duplicated, block: B:146:0x02ca  */
    /* JADX WARN: Code duplicated, block: B:148:0x007a A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:32:0x008c  */
    /* JADX WARN: Code duplicated, block: B:66:0x0158  */
    private static String rewriteStringEscapes(String lit) {
        int len = lit.length();
        StringBuilder sb = new StringBuilder(len);
        int i = 0;
        while (i < len) {
            char ch = lit.charAt(i);
            if (ch == '\\' && i + 1 < len) {
                char nxt = lit.charAt(i + 1);
                if (nxt == '\\') {
                    sb.append("\\\\");
                    i += 2;
                } else if (nxt == 'u' && i + 2 < len && lit.charAt(i + 2) == '{') {
                    int close = lit.indexOf('}', i + 3);
                    if (close > i + 3) {
                        String hex = lit.substring(i + 3, close);
                        try {
                            int cp = Integer.parseInt(hex, 16);
                            byte[] utf8Bytes = new String(Character.toChars(cp)).getBytes(kotlin.text.Charsets.UTF_8);
                            for (byte b : utf8Bytes) {
                                sb.append(String.format(java.util.Locale.US, "\\%03d", b & 0xFF));
                            }
                            i = close + 1;
                            continue;
                        } catch (Exception ignored) {
                        }
                    }
                    sb.append(ch).append(nxt);
                    i += 2;
                } else {
                    sb.append(ch).append(nxt);
                    i += 2;
                }
            } else {
                sb.append(ch);
                i++;
            }
        }
        return sb.toString();
    }

    private final ArrayList<Tok> lex(String s) {
        ArrayList<Tok> arrayList = new ArrayList<>();
        int lvl = (s.length() > 0 && s.charAt(0) == '\uFEFF') ? 1 : 0;
        int n = s.length();
        StringBuilder triv = new StringBuilder();
        while (true) {
            int i2 = lvl;
            while (i2 < n) {
                char c2 = s.charAt(i2);
                if (c2 == '\t' || c2 == '\n' || c2 == '\f' || c2 == '\r' || c2 == ' ') {
                    triv.append(c2);
                    i2++;
                } else if (c2 == '-' && i2 + 1 < n && s.charAt(i2 + 1) == '-') {
                    int lvl2 = lex$longBracketLevel(n, s, i2 + 2);
                    int e;
                    if (lvl2 >= 0) {
                        e = lex$skipLong(s, n, i2 + 2, lvl2);
                    } else {
                        int idx = StringsKt.indexOf((CharSequence) s, '\n', i2, false);
                        e = idx < 0 ? n : idx;
                    }
                    String comment = s.substring(i2, e);
                    Intrinsics.checkNotNullExpressionValue(comment, "substring(...)");
                    triv.append(lvl2 == 0 ? relevel(comment, "--") : comment);
                    i2 = e;
                } else {
                    break;
                }
            }
            if (i2 >= n) {
                String trailing = triv.toString();
                Intrinsics.checkNotNullExpressionValue(trailing, "toString(...)");
                arrayList.add(new Tok(K.EOF, "", trailing));
                return arrayList;
            }
            String tr = triv.toString();
            Intrinsics.checkNotNullExpressionValue(tr, "toString(...)");
            triv.setLength(0);
            char c = s.charAt(i2);
            if (Character.isLetter(c) || c == '_') {
                lvl = i2;
                while (lvl < n && (Character.isLetterOrDigit(s.charAt(lvl)) || s.charAt(lvl) == '_')) {
                    lvl++;
                }
                String w = s.substring(i2, lvl);
                Intrinsics.checkNotNullExpressionValue(w, "substring(...)");
                arrayList.add(new Tok(keywords.contains(w) ? K.KEYWORD : K.NAME, w, tr));
            } else if (Character.isDigit(c) || (c == '.' && i2 + 1 < n && Character.isDigit(s.charAt(i2 + 1)))) {
                int j = i2;
                if (c == '0' && i2 + 1 < n && (s.charAt(i2 + 1) == 'x' || s.charAt(i2 + 1) == 'X' || s.charAt(i2 + 1) == 'b' || s.charAt(i2 + 1) == 'B')) {
                    j += 2;
                    while (j < n && (Character.isLetterOrDigit(s.charAt(j)) || s.charAt(j) == '_')) {
                        j++;
                    }
                } else {
                    boolean seenDot = false;
                    while (j < n) {
                        char ch = s.charAt(j);
                        if (Character.isDigit(ch) || ch == '_') {
                            j++;
                        } else if (ch == '.' && !seenDot && !(j + 1 < n && s.charAt(j + 1) == '.')) {
                            seenDot = true;
                            j++;
                        } else {
                            break;
                        }
                    }
                    if (j < n && (s.charAt(j) == 'e' || s.charAt(j) == 'E')) {
                        j++;
                        if (j < n && (s.charAt(j) == '+' || s.charAt(j) == '-')) {
                            j++;
                        }
                        while (j < n && (Character.isDigit(s.charAt(j)) || s.charAt(j) == '_')) {
                            j++;
                        }
                    }
                }
                String rawNum = s.substring(i2, j);
                Intrinsics.checkNotNullExpressionValue(rawNum, "substring(...)");
                String num = StringsKt.replace(rawNum, "_", "", false);
                if (StringsKt.startsWith(num, "0b", false) || StringsKt.startsWith(num, "0B", false)) {
                    String binPart = num.substring(2);
                    Intrinsics.checkNotNullExpressionValue(binPart, "substring(...)");
                    try {
                        num = new java.math.BigInteger(binPart, 2).toString();
                    } catch (Exception ignored) {
                        num = "0";
                    }
                }
                Tok t = new Tok(K.NUMBER, rawNum, tr);
                t.setOut(num);
                arrayList.add(t);
                lvl = j;
            } else if (c == '"' || c == '\'') {
                int j2 = i2 + 1;
                while (j2 < n && s.charAt(j2) != c) {
                    if (s.charAt(j2) == '\\') {
                        if (j2 + 1 < n && s.charAt(j2 + 1) == 'z') {
                            j2 += 2;
                            while (j2 < n) {
                                char wc = s.charAt(j2);
                                if (wc == ' ' || wc == '\t' || wc == '\n' || wc == '\r' || wc == '\f' || wc == 0x0B) {
                                    j2++;
                                } else {
                                    break;
                                }
                            }
                        } else if (j2 + 2 < n && s.charAt(j2 + 1) == '\r' && s.charAt(j2 + 2) == '\n') {
                            j2 += 3;
                        } else {
                            j2 += 2;
                        }
                    } else if (s.charAt(j2) == '\n') {
                        break;
                    } else {
                        j2++;
                    }
                }
                lvl = Math.min(j2 + 1, n);
                String strLit = s.substring(i2, lvl);
                Intrinsics.checkNotNullExpressionValue(strLit, "substring(...)");
                Tok strTok = new Tok(K.STRING, strLit, tr);
                if (strLit.indexOf("\\u{") >= 0) {
                    strTok.setOut(rewriteStringEscapes(strLit));
                }
                arrayList.add(strTok);
            } else if (c == '[' && lex$longBracketLevel(n, s, i2) >= 0) {
                int lvl3 = lex$longBracketLevel(n, s, i2);
                int e2 = lex$skipLong(s, n, i2, lvl3);
                String longStr = s.substring(i2, e2);
                Intrinsics.checkNotNullExpressionValue(longStr, "substring(...)");
                Tok t2 = new Tok(K.STRING, longStr, tr);
                if (lvl3 == 0) {
                    t2.setOut(relevel(t2.getText(), ""));
                }
                arrayList.add(t2);
                lvl = e2;
            } else if (c == '`') {
                Pair<Integer, String> pairInterpolated = interpolated(s, i2);
                int e3 = pairInterpolated.component1().intValue();
                String converted = pairInterpolated.component2();
                String interpStr = s.substring(i2, e3);
                Intrinsics.checkNotNullExpressionValue(interpStr, "substring(...)");
                Tok t3 = new Tok(K.STRING, interpStr, tr);
                t3.setOut(converted);
                arrayList.add(t3);
                lvl = e3;
            } else {
                String op = null;
                for (Object obj2 : ops) {
                    if (StringsKt.startsWith(s, (String) obj2, i2, false)) {
                        op = (String) obj2;
                        break;
                    }
                }
                if (op == null) {
                    op = String.valueOf(c);
                }
                arrayList.add(new Tok(K.OP, op, tr));
                lvl = i2 + Math.max(op.length(), 1);
            }
        }
    }

    private static final int lex$longBracketLevel(int n, String $s, int at) {
        if (at >= n || $s.charAt(at) != '[') {
            return -1;
        }
        int j = at + 1;
        int lvl = 0;
        while (j < n && $s.charAt(j) == '=') {
            lvl++;
            j++;
        }
        if (j >= n || $s.charAt(j) != '[') {
            return -1;
        }
        return lvl;
    }

    private static final int lex$skipLong(String $s, int n, int at, int lvl) {
        String close = "]" + StringsKt.repeat("=", lvl) + "]";
        int e = StringsKt.indexOf((CharSequence) $s, close, at + lvl + 2, false);
        return e < 0 ? n : close.length() + e;
    }

    private final Pair<Integer, String> interpolated(String s, int start) {
        ArrayList parts = new ArrayList();
        StringBuilder lit = new StringBuilder();
        int i = start + 1;
        while (i < s.length() && s.charAt(i) != '`') {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char nx = s.charAt(i + 1);
                if (nx == '\r' && i + 2 < s.length() && s.charAt(i + 2) == '\n') {
                    lit.append("\\\n");
                    i += 3;
                } else if (nx == 'z') {
                    lit.append("\\z");
                    i += 2;
                    while (i < s.length()) {
                        char wc = s.charAt(i);
                        if (wc == ' ' || wc == '\t' || wc == '\n' || wc == '\r' || wc == '\f' || wc == 0x0B) {
                            lit.append(wc);
                            i++;
                        } else {
                            break;
                        }
                    }
                } else if (nx == 'u' && i + 2 < s.length() && s.charAt(i + 2) == '{') {
                    int close = s.indexOf('}', i + 3);
                    if (close > i + 3) {
                        try {
                            int cp = Integer.parseInt(s.substring(i + 3, close), 16);
                            byte[] utf8Bytes = new String(Character.toChars(cp)).getBytes(kotlin.text.Charsets.UTF_8);
                            for (byte b : utf8Bytes) {
                                lit.append(String.format(java.util.Locale.US, "\\%03d", b & 0xFF));
                            }
                            i = close + 1;
                            continue;
                        } catch (Exception ignored) {
                        }
                    }
                    lit.append('\\').append(nx);
                    i += 2;
                } else {
                    switch (nx) {
                        case '\n':
                            lit.append("\\\n");
                            break;
                        case '\r':
                            lit.append("\\r");
                            break;
                        case '`':
                        case '{':
                        case '}':
                            lit.append(nx);
                            break;
                        default:
                            lit.append('\\');
                            lit.append(nx);
                            break;
                    }
                    i += 2;
                }
            } else if (c == '\\') {
                lit.append("\\\\");
                i++;
            } else if (c == '{') {
                interpolated$flush(lit, parts);
                int j = skipInterpBrace(s, i + 1);
                String expr = s.substring(i + 1, Math.min(j, s.length()));
                Intrinsics.checkNotNullExpressionValue(expr, "substring(...)");
                String trimmed = expr.trim();
                if (trimmed.isEmpty()) {
                    parts.add("tostring(nil)");
                } else {
                    parts.add("tostring((" + transpileExpr(expr) + "))");
                }
                i = j + 1;
            } else if (c == '\"') {
                lit.append("\\\"");
                i++;
            } else if (c == '\r') {
                if (i + 1 < s.length() && s.charAt(i + 1) == '\n') {
                    lit.append("\\\n");
                    i += 2;
                } else {
                    lit.append("\\r");
                    i++;
                }
            } else if (c == '\n') {
                lit.append("\\\n");
                i++;
            } else {
                lit.append(c);
                i++;
            }
        }
        interpolated$flush(lit, parts);
        int end = Math.min(i + 1, s.length());
        String body = parts.isEmpty() ? "\"\"" : CollectionsKt.joinToString(parts, " .. ", "", "", -1, "...", null);
        return TuplesKt.to(Integer.valueOf(end), "(" + body + ")");
    }

    private static final void interpolated$flush(StringBuilder lit, ArrayList<String> arrayList) {
        if (lit.length() > 0) {
            arrayList.add("\"" + ((Object) lit) + "\"");
            lit.setLength(0);
        }
    }

    private static int skipInterpBrace(String s, int start) {
        int n = s.length();
        int depth = 1;
        int j = start;
        while (j < n && depth > 0) {
            char cj = s.charAt(j);
            if (cj == '"' || cj == '\'') {
                char q = cj;
                j++;
                while (j < n && s.charAt(j) != q) {
                    if (s.charAt(j) == '\\') {
                        j += 2;
                    } else if (s.charAt(j) == '\n') {
                        break;
                    } else {
                        j++;
                    }
                }
                if (j < n) {
                    j++;
                }
            } else if (cj == '`') {
                j++;
                while (j < n && s.charAt(j) != '`') {
                    char cb = s.charAt(j);
                    if (cb == '\\') {
                        j += 2;
                    } else if (cb == '{') {
                        j = skipInterpBrace(s, j + 1) + 1;
                    } else {
                        j++;
                    }
                }
                if (j < n) {
                    j++;
                }
            } else if (cj == '-' && j + 1 < n && s.charAt(j + 1) == '-') {
                int lvl = lex$longBracketLevel(n, s, j + 2);
                if (lvl >= 0) {
                    j = lex$skipLong(s, n, j + 2, lvl);
                } else {
                    int nl = s.indexOf('\n', j + 2);
                    j = nl < 0 ? n : nl + 1;
                }
            } else if (cj == '[' && lex$longBracketLevel(n, s, j) >= 0) {
                int lvl = lex$longBracketLevel(n, s, j);
                j = lex$skipLong(s, n, j, lvl);
            } else if (cj == '{') {
                depth++;
                j++;
            } else if (cj == '}') {
                depth--;
                if (depth > 0) {
                    j++;
                }
            } else {
                j++;
            }
        }
        return j;
    }

    private final String transpileExpr(String e) {
        String r = transpileInternal("return " + e, false, false);
        if (!StringsKt.startsWith(r, "return ", false)) {
            return e;
        }
        String strSubstring = r.substring(7);
        Intrinsics.checkNotNullExpressionValue(strSubstring, "substring(...)");
        return strSubstring;
    }

    /* JADX INFO: compiled from: LuauTranspiler.kt */
    private static final class LoopCtx {
        private final int id;
        private boolean usesContinue;
        private int currentTopStmtStart = -1;
        private int firstContinueStmtStart = -1;

        public LoopCtx(int id) {
            this.id = id;
        }

        public final int getCurrentTopStmtStart() {
            return this.currentTopStmtStart;
        }

        public final void setCurrentTopStmtStart(int v) {
            this.currentTopStmtStart = v;
        }

        public final int getFirstContinueStmtStart() {
            return this.firstContinueStmtStart;
        }

        public final void markContinue() {
            this.usesContinue = true;
            if (this.firstContinueStmtStart < 0 && this.currentTopStmtStart >= 0) {
                this.firstContinueStmtStart = this.currentTopStmtStart;
            }
        }

        public final int getId() {
            return this.id;
        }

        public final boolean getUsesContinue() {
            return this.usesContinue;
        }

        public final void setUsesContinue(boolean z) {
            this.usesContinue = z;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: compiled from: LuauTranspiler.kt */
    static final class Parser {
        private static final int MAX_DEPTH = 256;
        private int castDepth;
        private int depth;
        private int exprDepth;
        private boolean lastSuffixHadCall;
        private int lastSuffixKind;
        private int lastSuffixStart;
        private int lastSuffixEnd;
        private final boolean hoist;
        private int labelCounter;
        private final ArrayList<LoopCtx> loops;
        private int p;
        private final List<Tok> t;

        public Parser(List<Tok> t, boolean hoist) {
            Intrinsics.checkNotNullParameter(t, "t");
            this.t = t;
            this.hoist = hoist;
            this.loops = new ArrayList<>();
        }

        public /* synthetic */ Parser(List list, boolean z, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this(list, (i & 2) != 0 ? false : z);
        }

        public final boolean getHoist() {
            return this.hoist;
        }

        public final List<Tok> getT() {
            return this.t;
        }

        public final int getP() {
            return this.p;
        }

        public final void setP(int i) {
            this.p = i;
        }

        public final int getLabelCounter() {
            return this.labelCounter;
        }

        public final void setLabelCounter(int i) {
            this.labelCounter = i;
        }

        public final int getDepth() {
            return this.depth;
        }

        public final void setDepth(int i) {
            this.depth = i;
        }

        public final ArrayList<LoopCtx> getLoops() {
            return this.loops;
        }

        public static /* synthetic */ Tok peek$default(Parser parser, int i, int i2, Object obj) {
            if ((i2 & 1) != 0) {
                i = 0;
            }
            return parser.peek(i);
        }

        public final Tok peek(int o) {
            return this.t.get(Math.min(this.p + o, this.t.size() - 1));
        }

        public final Tok next() {
            Tok tok = this.t.get(this.p);
            if (this.p < this.t.size() - 1) {
                this.p++;
            }
            return tok;
        }

        public final boolean atEof() {
            return peek$default(this, 0, 1, null).getK() == K.EOF;
        }

        public final Tok expectOp(String s) throws ParseError {
            Intrinsics.checkNotNullParameter(s, "s");
            if (peek$default(this, 0, 1, null).isOp(s)) {
                return next();
            }
            throw new ParseError();
        }

        public final Tok expectKw(String s) throws ParseError {
            Intrinsics.checkNotNullParameter(s, "s");
            if (peek$default(this, 0, 1, null).isKw(s)) {
                return next();
            }
            throw new ParseError();
        }

        public final Tok expectName() throws ParseError {
            if (peek$default(this, 0, 1, null).getK() == K.NAME) {
                return next();
            }
            throw new ParseError();
        }

        public final void drop(int from, int to) throws IOException {
            for (int i = from; i < to; i++) {
                Tok tk = this.t.get(i);
                CharSequence text = tk.getText();
                Appendable sb = new StringBuilder();
                int length = text.length();
                for (int i2 = 0; i2 < length; i2++) {
                    char cCharAt = text.charAt(i2);
                    if (cCharAt == '\n') {
                        sb.append(cCharAt);
                    }
                }
                tk.setOut(((StringBuilder) sb).toString());
                tk.setPrefix("");
                tk.setSuffix("");
            }
        }

        public final void run() throws IOException, ParseError {
            block();
            if (!atEof()) {
                throw new ParseError();
            }
        }

        public final boolean blockEnd() {
            Tok k = peek$default(this, 0, 1, null);
            return k.getK() == K.EOF || k.isKw("end") || k.isKw("else") || k.isKw("elseif") || k.isKw("until");
        }

        public final void block() throws IOException, ParseError {
            this.depth++;
            if (this.depth > MAX_DEPTH) {
                throw new ParseError();
            }
            try {
                while (!blockEnd()) {
                    int before = this.p;
                    if (peek$default(this, 0, 1, null).isKw("return")) {
                        next();
                        if (!blockEnd() && !peek$default(this, 0, 1, null).isOp(";")) {
                            exprList();
                        }
                        if (peek$default(this, 0, 1, null).isOp(";")) {
                            next();
                        }
                    } else {
                        statement();
                    }
                    if (this.p == before) {
                        throw new ParseError();
                    }
                }
            } finally {
                this.depth--;
            }
        }

        public final Tok loopBody(String endKw) throws IOException, ParseError {
            Intrinsics.checkNotNullParameter(endKw, "endKw");
            this.labelCounter++;
            LoopCtx ctx = new LoopCtx(this.labelCounter);
            this.loops.add(ctx);
            int bodyStart = this.p;
            try {
                if (Intrinsics.areEqual(endKw, "until")) {
                    this.depth++;
                    if (this.depth > MAX_DEPTH) {
                        throw new ParseError();
                    }
                    try {
                        while (!blockEnd()) {
                            int before = this.p;
                            ctx.setCurrentTopStmtStart(before);
                            if (peek$default(this, 0, 1, null).isKw("return")) {
                                next();
                                if (!blockEnd() && !peek$default(this, 0, 1, null).isOp(";")) {
                                    exprList();
                                }
                                if (peek$default(this, 0, 1, null).isOp(";")) {
                                    next();
                                }
                            } else {
                                statement();
                            }
                            if (this.p == before) {
                                throw new ParseError();
                            }
                        }
                    } finally {
                        this.depth--;
                    }
                } else {
                    block();
                }
            } finally {
                this.loops.remove(this.loops.size() - 1);
            }
            Tok e = expectKw(endKw);
            if (ctx.getUsesContinue()) {
                if (Intrinsics.areEqual(endKw, "end")) {
                    this.t.get(bodyStart).setPrefix("do " + this.t.get(bodyStart).getPrefix());
                    e.setPrefix(e.getPrefix() + " end ::__continue" + ctx.getId() + ":: ");
                } else {
                    int wrapStart = ctx.getFirstContinueStmtStart() >= 0 ? ctx.getFirstContinueStmtStart() : bodyStart;
                    this.t.get(wrapStart).setPrefix("do " + this.t.get(wrapStart).getPrefix());
                    e.setPrefix(e.getPrefix() + " end ::__continue" + ctx.getId() + ":: ");
                }
            }
            return e;
        }

        public final void statement() throws IOException, ParseError {
            while (peek$default(this, 0, 1, null).isOp("@")) {
                int attrStart = this.p;
                next();
                if (peek$default(this, 0, 1, null).isOp("[")) {
                    skipBalanced("[", "]");
                } else if (peek$default(this, 0, 1, null).getK() == K.NAME || peek$default(this, 0, 1, null).getK() == K.KEYWORD) {
                    next();
                    if (peek$default(this, 0, 1, null).isOp("(")) {
                        skipBalanced("(", ")");
                    }
                }
                drop(attrStart, this.p);
            }
            if (blockEnd()) {
                return;
            }
            Tok k = peek$default(this, 0, 1, null);
            if (!k.isOp(";")) {
                if (k.isKw("if")) {
                    next();
                    expr();
                    expectKw("then");
                    block();
                    while (peek$default(this, 0, 1, null).isKw("elseif")) {
                        next();
                        expr();
                        expectKw("then");
                        block();
                    }
                    if (peek$default(this, 0, 1, null).isKw("else")) {
                        next();
                        block();
                    }
                    expectKw("end");
                    return;
                }
                if (!k.isKw("while")) {
                    if (!k.isKw("do")) {
                        if (k.isKw("for")) {
                            next();
                            nameWithType();
                            if (peek$default(this, 0, 1, null).isOp("=")) {
                                next();
                                exprList();
                            } else {
                                while (peek$default(this, 0, 1, null).isOp(",")) {
                                    next();
                                    nameWithType();
                                }
                                expectKw("in");
                                int first = this.p;
                                expr();
                                if (peek$default(this, 0, 1, null).isOp(",")) {
                                    while (peek$default(this, 0, 1, null).isOp(",")) {
                                        next();
                                        expr();
                                    }
                                } else {
                                    this.t.get(first).setPrefix("__iter(" + this.t.get(first).getPrefix());
                                    Tok tok = this.t.get(this.p - 1);
                                    tok.setSuffix(tok.getSuffix() + ")");
                                }
                            }
                            expectKw("do");
                            loopBody("end");
                            return;
                        }
                        if (k.isKw("repeat")) {
                            next();
                            loopBody("until");
                            expr();
                            Unit unit = Unit.INSTANCE;
                            return;
                        }
                        if (k.isKw("function")) {
                            next();
                            expectName();
                            while (true) {
                                if (peek$default(this, 0, 1, null).isOp(".") || peek$default(this, 0, 1, null).isOp(":")) {
                                    next();
                                    expectName();
                                } else {
                                    funcBody();
                                    Unit unit2 = Unit.INSTANCE;
                                    return;
                                }
                            }
                        } else {
                            if (k.isKw("local") || (k.isName("const") && (peek(1).getK() == K.NAME || peek(1).isKw("function")))) {
                                Tok localTok = next();
                                boolean hoistThis = this.hoist && this.depth == 1;
                                if (hoistThis) {
                                    localTok.setOut("");
                                } else if (localTok.isName("const")) {
                                    localTok.setOut("local");
                                }
                                if (peek$default(this, 0, 1, null).isKw("function")) {
                                    next();
                                    expectName();
                                    funcBody();
                                } else {
                                    localName();
                                    while (peek$default(this, 0, 1, null).isOp(",")) {
                                        next();
                                        localName();
                                    }
                                    if (peek$default(this, 0, 1, null).isOp("=")) {
                                        next();
                                        exprList();
                                    } else if (hoistThis) {
                                        Tok tok2 = this.t.get(this.p - 1);
                                        tok2.setSuffix(tok2.getSuffix() + " = nil");
                                    }
                                }
                                Unit unit3 = Unit.INSTANCE;
                                return;
                            }
                            if (!k.isKw("break")) {
                                if (!k.isKw("goto")) {
                                    if (k.isOp("::")) {
                                        next();
                                        expectName();
                                        expectOp("::");
                                        return;
                                    }
                                    if (k.isName("continue") && isStatementWord()) {
                                        Tok tok3 = next();
                                        LoopCtx ctx = (LoopCtx) CollectionsKt.lastOrNull((List) this.loops);
                                        if (ctx == null) {
                                            throw new ParseError();
                                        }
                                        ctx.markContinue();
                                        tok3.setOut("goto __continue" + ctx.getId());
                                        Unit unit4 = Unit.INSTANCE;
                                        return;
                                    }
                                    if (k.isName("type") && peek(1).isKw("function")) {
                                        typeFuncDecl(this.p);
                                        return;
                                    }
                                    if (k.isName("export") && peek(1).isName("type") && peek(2).isKw("function")) {
                                        typeFuncDecl(this.p);
                                        return;
                                    }
                                    if (!k.isName("type") || peek(1).getK() != K.NAME || (!peek(2).isOp("=") && !peek(2).isOp("<"))) {
                                        if (!k.isName("export") || !peek(1).isName("type")) {
                                            exprStatement();
                                            Unit unit5 = Unit.INSTANCE;
                                            return;
                                        } else {
                                            typeDecl(this.p);
                                            Unit unit6 = Unit.INSTANCE;
                                            return;
                                        }
                                    }
                                    typeDecl(this.p);
                                    Unit unit7 = Unit.INSTANCE;
                                    return;
                                }
                                next();
                                expectName();
                                return;
                            }
                            next();
                        }
                    } else {
                        next();
                        block();
                        expectKw("end");
                    }
                } else {
                    next();
                    expr();
                    expectKw("do");
                    loopBody("end");
                }
            } else {
                next();
            }
        }

        public final boolean isStatementWord() {
            Tok n = peek(1);
            return !(n.isOp("(") || n.isOp("=") || n.isOp(".") || n.isOp("[") || n.isOp(":") || n.isOp(",") || n.getK() == K.STRING || n.isOp("{") || n.isOp("+=") || n.isOp("-=") || n.isOp("*=") || n.isOp("/=") || n.isOp("//=") || n.isOp("%=") || n.isOp("^=") || n.isOp("..="));
        }

        public final void typeDecl(int start) throws IOException, ParseError {
            if (peek$default(this, 0, 1, null).isName("export")) {
                next();
            }
            next();
            expectName();
            if (peek$default(this, 0, 1, null).isOp("<")) {
                skipAngle();
            }
            expectOp("=");
            type();
            drop(start, this.p);
        }

        public final void typeFuncDecl(int start) throws IOException, ParseError {
            if (peek$default(this, 0, 1, null).isName("export")) {
                next();
            }
            next();
            expectKw("function");
            expectName();
            funcBody();
            drop(start, this.p);
        }

        public final void nameWithType() throws IOException, ParseError {
            expectName();
            if (peek$default(this, 0, 1, null).isOp(":")) {
                int s = this.p;
                next();
                type();
                drop(s, this.p);
            }
        }

        public final void localName() throws IOException, ParseError {
            expectName();
            if (peek$default(this, 0, 1, null).isOp("<") && looksLikeGenericArgs()) {
                int s = this.p;
                skipAngle();
                drop(s, this.p);
            }
            if (peek$default(this, 0, 1, null).isOp(":")) {
                int s2 = this.p;
                next();
                type();
                drop(s2, this.p);
            }
        }

        public final void funcBody() throws IOException, ParseError {
            if (peek$default(this, 0, 1, null).isOp("<")) {
                int s = this.p;
                skipAngle();
                drop(s, this.p);
            }
            expectOp("(");
            while (!peek$default(this, 0, 1, null).isOp(")")) {
                if (peek$default(this, 0, 1, null).isOp("...")) {
                    next();
                    if (peek$default(this, 0, 1, null).isOp(":")) {
                        int s2 = this.p;
                        next();
                        type();
                        drop(s2, this.p);
                    }
                } else {
                    nameWithType();
                }
                if (!peek$default(this, 0, 1, null).isOp(",")) {
                    break;
                } else {
                    next();
                }
            }
            expectOp(")");
            if (peek$default(this, 0, 1, null).isOp(":")) {
                int s3 = this.p;
                next();
                returnType();
                drop(s3, this.p);
            }
            this.loops.add(null);
            block();
            this.loops.remove(this.loops.size() - 1);
            expectKw("end");
        }

        private final boolean looksLikeGenericArgs() {
            int n = this.t.size();
            if (this.p >= n || !this.t.get(this.p).isOp("<")) {
                return false;
            }
            int angleDepth = 1;
            int parenDepth = 0;
            int braceDepth = 0;
            int bracketDepth = 0;
            boolean prevWasAtom = false;
            int i = this.p + 1;
            while (i < n) {
                Tok tk = this.t.get(i);
                K kind = tk.getK();
                if (kind == K.EOF) {
                    return false;
                }
                if (parenDepth == 0 && braceDepth == 0 && bracketDepth == 0) {
                    if (kind == K.NUMBER) {
                        return false;
                    }
                    if (kind == K.KEYWORD && !tk.isKw("nil") && !tk.isKw("true") && !tk.isKw("false")) {
                        return false;
                    }
                    boolean isAtom = (kind == K.NAME || kind == K.STRING || tk.isKw("nil") || tk.isKw("true") || tk.isKw("false"));
                    if (isAtom) {
                        if (prevWasAtom) {
                            return false;
                        }
                        prevWasAtom = true;
                    } else if (kind == K.OP) {
                        String op = tk.getText();
                        prevWasAtom = false;
                        if ("<".equals(op)) {
                            angleDepth++;
                        } else if (">".equals(op) || ">=".equals(op)) {
                            if (">=".equals(op) && this.castDepth > 0) {
                                return false;
                            }
                            angleDepth--;
                            if (angleDepth == 0) {
                                if (i <= this.p + 1) {
                                    return false;
                                }
                                if (this.castDepth > 0 && ">".equals(op) && i + 1 < n) {
                                    Tok nxt = this.t.get(i + 1);
                                    if (nxt.getTrivia().indexOf('\n') < 0) {
                                        K nk = nxt.getK();
                                        if (nk == K.NAME || nk == K.NUMBER || nk == K.STRING
                                                || nxt.isKw("true") || nxt.isKw("false") || nxt.isKw("nil")
                                                || nxt.isKw("not") || nxt.isOp("(") || nxt.isOp("{")
                                                || nxt.isOp("[") || nxt.isOp("#") || nxt.isOp("-")) {
                                            return false;
                                        }
                                    }
                                }
                                return true;
                            }
                        } else if ("(".equals(op)) {
                            parenDepth++;
                        } else if ("{".equals(op)) {
                            braceDepth++;
                        } else if ("[".equals(op)) {
                            bracketDepth++;
                        } else if (")".equals(op) || "}".equals(op) || "]".equals(op)) {
                            return false;
                        } else if ("=".equals(op) || "==".equals(op) || "~=".equals(op) || "<=".equals(op)
                                || "+".equals(op) || "-".equals(op) || "*".equals(op) || "/".equals(op)
                                || "//".equals(op) || "%".equals(op) || "^".equals(op) || "#".equals(op)
                                || "..".equals(op) || ";".equals(op) || "::".equals(op)
                                || "+=".equals(op) || "-=".equals(op) || "*=".equals(op) || "/=".equals(op)
                                || "%=".equals(op) || "^=".equals(op) || "..=".equals(op) || "//=".equals(op)) {
                            return false;
                        }
                    }
                } else if (kind == K.OP) {
                    String op = tk.getText();
                    if ("(".equals(op)) parenDepth++;
                    else if (")".equals(op)) { if (parenDepth > 0) parenDepth--; else return false; }
                    else if ("{".equals(op)) braceDepth++;
                    else if ("}".equals(op)) { if (braceDepth > 0) braceDepth--; else return false; }
                    else if ("[".equals(op)) bracketDepth++;
                    else if ("]".equals(op)) { if (bracketDepth > 0) bracketDepth--; else return false; }
                }
                i++;
            }
            return false;
        }

        public final void skipAngle() throws ParseError {
            expectOp("<");
            int depth = 1;
            while (depth > 0 && !atEof()) {
                Tok k = next();
                if (k.isOp("<")) {
                    depth++;
                } else if (k.isOp(">")) {
                    depth--;
                } else if (k.isOp(">=")) {
                    depth--;
                    if (depth == 0) {
                        int idx = this.p - 1;
                        Tok gt = new Tok(K.OP, ">", k.getTrivia());
                        Tok eq = new Tok(K.OP, "=", "");
                        this.t.set(idx, gt);
                        this.t.add(idx + 1, eq);
                    }
                }
            }
        }

        public final void skipBalanced(String open, String close) throws ParseError {
            Intrinsics.checkNotNullParameter(open, "open");
            Intrinsics.checkNotNullParameter(close, "close");
            expectOp(open);
            int depth = 1;
            while (depth > 0 && !atEof()) {
                Tok k = next();
                if (k.isOp(open)) {
                    depth++;
                } else if (k.isOp(close)) {
                    depth--;
                }
            }
        }

        public final void returnType() throws IOException, ParseError {
            if (peek$default(this, 0, 1, null).isOp("(")) {
                skipBalanced("(", ")");
                if (peek$default(this, 0, 1, null).isOp("->")) {
                    next();
                    type();
                }
                while (peek$default(this, 0, 1, null).isOp("?")) {
                    next();
                }
                while (true) {
                    if (!peek$default(this, 0, 1, null).isOp("|") && !peek$default(this, 0, 1, null).isOp("&")) {
                        return;
                    }
                    next();
                    simpleType();
                }
            } else {
                type();
            }
        }

        public final void type() throws IOException, ParseError {
            if (peek$default(this, 0, 1, null).isOp("|") || peek$default(this, 0, 1, null).isOp("&")) {
                next();
            }
            simpleType();
            while (true) {
                if (peek$default(this, 0, 1, null).isOp("|") || peek$default(this, 0, 1, null).isOp("&")) {
                    next();
                    simpleType();
                } else {
                    return;
                }
            }
        }

        public final void simpleType() throws IOException, ParseError {
            this.exprDepth++;
            if (this.exprDepth > MAX_DEPTH) {
                throw new ParseError();
            }
            try {
                simpleTypeBody();
            } finally {
                this.exprDepth--;
            }
        }

        private final void simpleTypeBody() throws IOException, ParseError {
            Tok k = peek$default(this, 0, 1, null);
            if (k.isOp("<")) {
                skipAngle();
                simpleType();
            } else if (k.isOp("(")) {
                skipBalanced("(", ")");
                if (peek$default(this, 0, 1, null).isOp("->")) {
                    next();
                    type();
                }
            } else if (k.isOp("{")) {
                skipBalanced("{", "}");
            } else if (k.isName("typeof") && peek(1).isOp("(")) {
                next();
                skipBalanced("(", ")");
                while (true) {
                    if (peek$default(this, 0, 1, null).isOp(".")) {
                        next();
                        expectName();
                    } else if (peek$default(this, 0, 1, null).isOp("<") && looksLikeGenericArgs()) {
                        skipAngle();
                    } else {
                        break;
                    }
                }
            } else if (k.isOp("...")) {
                next();
                simpleType();
            } else if (k.getK() == K.NAME || k.isKw("nil") || k.isKw("true") || k.isKw("false") || k.getK() == K.STRING) {
                next();
                while (true) {
                    if (peek$default(this, 0, 1, null).isOp(".")) {
                        next();
                        expectName();
                    } else if (peek$default(this, 0, 1, null).isOp("<") && looksLikeGenericArgs()) {
                        skipAngle();
                    } else {
                        break;
                    }
                }
                if (peek$default(this, 0, 1, null).isOp("...")) {
                    next();
                }
            } else {
                throw new ParseError();
            }
            while (peek$default(this, 0, 1, null).isOp("?")) {
                next();
            }
        }

        public final void exprList() throws IOException, ParseError {
            while (true) {
                expr();
                if (!peek$default(this, 0, 1, null).isOp(",")) {
                    return;
                } else {
                    next();
                }
            }
        }

        public final void exprStatement() throws IOException, ParseError {
            int start = this.p;
            suffixedExpr();
            Tok k = peek$default(this, 0, 1, null);
            if (k.isOp("=") || k.isOp(",")) {
                while (peek$default(this, 0, 1, null).isOp(",")) {
                    next();
                    suffixedExpr();
                }
                expectOp("=");
                exprList();
                return;
            }
            if (k.getK() == K.OP && k.getText().length() >= 2 && StringsKt.endsWith(k.getText(), "=", false) && !SetsKt.setOf(new String[]{"==", "~=", "<=", ">="}).contains(k.getText())) {
                boolean hadCall = this.lastSuffixHadCall;
                int sufKind = this.lastSuffixKind;
                int sufStart = this.lastSuffixStart;
                int sufEnd = this.lastSuffixEnd;
                Tok opTok = next();
                int opIdx = this.p - 1;
                String base = StringsKt.dropLast(opTok.getText(), 1);
                boolean lhsHasNewline = false;
                for (int li = start; li < opIdx; li++) {
                    if (this.t.get(li).getOut().indexOf('\n') >= 0) {
                        lhsHasNewline = true;
                        break;
                    }
                }
                int rhsStart = this.p;
                expr();
                if (rhsStart == this.p) {
                    throw new ParseError();
                }
                Tok last = this.t.get(this.p - 1);
                if ((hadCall || lhsHasNewline) && sufKind == 1 && sufStart > start && sufEnd < opIdx) {
                    String fieldName = this.t.get(sufEnd).getOut();
                    this.t.get(start).setPrefix("do local __t = (" + this.t.get(start).getPrefix());
                    this.t.get(sufStart).setOut("); __t.");
                    if (Intrinsics.areEqual(base, "//")) {
                        opTok.setOut("= math.floor(__t." + fieldName + " / (");
                        last.setSuffix(last.getSuffix() + ")) end");
                    } else {
                        opTok.setOut("= __t." + fieldName + " " + base + " (");
                        last.setSuffix(last.getSuffix() + ") end");
                    }
                } else if ((hadCall || lhsHasNewline) && sufKind == 2 && sufStart > start && sufEnd < opIdx) {
                    this.t.get(start).setPrefix("do local __t, __k = (" + this.t.get(start).getPrefix());
                    this.t.get(sufStart).setOut("), (");
                    this.t.get(sufEnd).setOut("); __t[__k]");
                    if (Intrinsics.areEqual(base, "//")) {
                        opTok.setOut("= math.floor(__t[__k] / (");
                        last.setSuffix(last.getSuffix() + ")) end");
                    } else {
                        opTok.setOut("= __t[__k] " + base + " (");
                        last.setSuffix(last.getSuffix() + ") end");
                    }
                } else {
                    String lhs = CollectionsKt.joinToString(RangesKt.until(start, opIdx), " ", "", "", -1, "...", new Function1() { // from class: com.goydevv.studrblx.lua.LuauTranspiler$Parser$$ExternalSyntheticLambda0
                        @Override // kotlin.jvm.functions.Function1
                        public final Object invoke(Object obj) {
                            Tok tok = Parser.this.t.get(((Integer) obj).intValue());
                            return tok.getPrefix() + tok.getOut() + tok.getSuffix();
                        }
                    });
                    if (Intrinsics.areEqual(base, "//")) {
                        opTok.setOut("= math.floor(" + lhs + " / (");
                        last.setSuffix(last.getSuffix() + "))");
                    } else {
                        opTok.setOut("= " + lhs + " " + base + " (");
                        last.setSuffix(last.getSuffix() + ")");
                    }
                }
            }
        }

        public final void primaryExpr() throws IOException, ParseError {
            Tok k = peek$default(this, 0, 1, null);
            if (k.getK() != K.NAME) {
                if (!k.isOp("(")) {
                    throw new ParseError();
                }
                next();
                expr();
                expectOp(")");
                return;
            }
            next();
        }

        public final void suffixedExpr() throws IOException, ParseError {
            boolean savedHadCall = this.lastSuffixHadCall;
            boolean hadCall = false;
            int kind = 0;
            int sufStart = -1;
            int sufEnd = -1;
            this.lastSuffixHadCall = false;
            primaryExpr();
            hadCall |= this.lastSuffixHadCall;
            while (true) {
                Tok k = peek$default(this, 0, 1, null);
                if (k.isOp(".")) {
                    int dotIdx = this.p;
                    next();
                    if (peek$default(this, 0, 1, null).getK() != K.NAME && peek$default(this, 0, 1, null).getK() != K.KEYWORD) {
                        throw new ParseError();
                    }
                    int fieldIdx = this.p;
                    next();
                    kind = 1;
                    sufStart = dotIdx;
                    sufEnd = fieldIdx;
                } else if (k.isOp("[")) {
                    int openIdx = this.p;
                    next();
                    this.lastSuffixHadCall = false;
                    expr();
                    hadCall |= this.lastSuffixHadCall;
                    int closeIdx = this.p;
                    expectOp("]");
                    kind = 2;
                    sufStart = openIdx;
                    sufEnd = closeIdx;
                } else if (k.isOp(":")) {
                    next();
                    expectName();
                    callArgs();
                    hadCall = true;
                    kind = 0;
                } else if (k.isOp("(") || k.isOp("{") || k.getK() == K.STRING) {
                    callArgs();
                    hadCall = true;
                    kind = 0;
                } else {
                    this.lastSuffixHadCall = savedHadCall || hadCall;
                    this.lastSuffixKind = kind;
                    this.lastSuffixStart = sufStart;
                    this.lastSuffixEnd = sufEnd;
                    return;
                }
            }
        }

        public final void callArgs() throws IOException, ParseError {
            Tok k = peek$default(this, 0, 1, null);
            if (k.getK() != K.STRING) {
                if (!k.isOp("{")) {
                    if (k.isOp("(")) {
                        next();
                        if (!peek$default(this, 0, 1, null).isOp(")")) {
                            exprList();
                        }
                        expectOp(")");
                        return;
                    }
                    throw new ParseError();
                }
                table();
                Unit unit = Unit.INSTANCE;
                return;
            }
            next();
        }

        public final void table() throws IOException, ParseError {
            expectOp("{");
            while (!peek$default(this, 0, 1, null).isOp("}")) {
                if (peek$default(this, 0, 1, null).isOp("[")) {
                    next();
                    expr();
                    expectOp("]");
                    expectOp("=");
                    expr();
                } else if (peek$default(this, 0, 1, null).getK() == K.NAME && peek(1).isOp("=")) {
                    next();
                    next();
                    expr();
                } else {
                    expr();
                }
                if (!peek$default(this, 0, 1, null).isOp(",") && !peek$default(this, 0, 1, null).isOp(";")) {
                    break;
                } else {
                    next();
                }
            }
            expectOp("}");
        }

        public final void simpleExpr() throws IOException, ParseError {
            while (peek$default(this, 0, 1, null).isOp("@")) {
                int attrStart = this.p;
                next();
                if (peek$default(this, 0, 1, null).isOp("[")) {
                    skipBalanced("[", "]");
                } else if (peek$default(this, 0, 1, null).getK() == K.NAME || peek$default(this, 0, 1, null).getK() == K.KEYWORD) {
                    next();
                    if (peek$default(this, 0, 1, null).isOp("(")) {
                        skipBalanced("(", ")");
                    }
                }
                drop(attrStart, this.p);
            }
            Tok k = peek$default(this, 0, 1, null);
            if (k.getK() == K.NUMBER || k.getK() == K.STRING || k.isKw("nil") || k.isKw("true") || k.isKw("false") || k.isOp("...")) {
                next();
            } else if (k.isOp("{")) {
                table();
            } else if (k.isKw("function")) {
                next();
                funcBody();
            } else if (k.isKw("if")) {
                ifExpr();
            } else {
                suffixedExpr();
            }
            while (peek$default(this, 0, 1, null).isOp("::")) {
                int s = this.p;
                next();
                this.castDepth++;
                try {
                    type();
                } finally {
                    this.castDepth--;
                }
                drop(s, this.p);
            }
        }

        public final void ifExpr() throws IOException, ParseError {
            int ifStart = this.p;
            Tok ifTok = next();
            expr();
            expectKw("then").setOut("then return");
            expr();
            while (peek$default(this, 0, 1, null).isKw("elseif")) {
                next();
                expr();
                expectKw("then").setOut("then return");
                expr();
            }
            expectKw("else").setOut("else return");
            expr();
            boolean hasVararg = false;
            int fnBlockDepth = 0;
            for (int i = ifStart + 1; i < this.p; i++) {
                Tok tk = this.t.get(i);
                if (fnBlockDepth == 0) {
                    if (tk.isKw("function")) {
                        fnBlockDepth = 1;
                    } else if (tk.isOp("...") && !tk.getOut().isEmpty()) {
                        hasVararg = true;
                        break;
                    }
                } else {
                    if (tk.isKw("function") || tk.isKw("do") || (tk.isKw("if") && !tk.getOut().startsWith("(function"))) {
                        fnBlockDepth++;
                    } else if (tk.isKw("end")) {
                        fnBlockDepth--;
                    }
                }
            }
            ifTok.setOut(hasVararg ? "(function(...) if" : "(function() if");
            Tok tok = this.t.get(this.p - 1);
            tok.setSuffix(tok.getSuffix() + (hasVararg ? " end end)(...)" : " end end)()"));
        }

        /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
        private final Pair<Integer, Integer> binPrio(Tok k) {
            int i;
            if (k.getK() == K.KEYWORD) {
                String text = k.getText();
                if (Intrinsics.areEqual(text, "or")) {
                    i = 1;
                } else {
                    if (!Intrinsics.areEqual(text, "and")) {
                        return null;
                    }
                    i = 2;
                }
                return TuplesKt.to(Integer.valueOf(i), Integer.valueOf(i));
            }
            if (k.getK() != K.OP) {
                return null;
            }
            String text2 = k.getText();
            switch (text2.hashCode()) {
                case 37:
                    if (!text2.equals("%")) {
                        return null;
                    }
                    return TuplesKt.to(11, 11);
                case 42:
                    if (!text2.equals("*")) {
                        return null;
                    }
                    return TuplesKt.to(11, 11);
                case 43:
                    if (!text2.equals("+")) {
                        return null;
                    }
                    return TuplesKt.to(10, 10);
                case 45:
                    if (!text2.equals("-")) {
                        return null;
                    }
                    return TuplesKt.to(10, 10);
                case 47:
                    if (!text2.equals("/")) {
                        return null;
                    }
                    return TuplesKt.to(11, 11);
                case 60:
                    if (!text2.equals("<")) {
                        return null;
                    }
                    return TuplesKt.to(3, 3);
                case 62:
                    if (!text2.equals(">")) {
                        return null;
                    }
                    return TuplesKt.to(3, 3);
                case 94:
                    if (text2.equals("^")) {
                        return TuplesKt.to(14, 13);
                    }
                    return null;
                case 1472:
                    if (text2.equals("..")) {
                        return TuplesKt.to(9, 8);
                    }
                    return null;
                case 1504:
                    if (!text2.equals("//")) {
                        return null;
                    }
                    return TuplesKt.to(11, 11);
                case 1921:
                    if (!text2.equals("<=")) {
                        return null;
                    }
                    return TuplesKt.to(3, 3);
                case 1952:
                    if (!text2.equals("==")) {
                        return null;
                    }
                    return TuplesKt.to(3, 3);
                case 1983:
                    if (!text2.equals(">=")) {
                        return null;
                    }
                    return TuplesKt.to(3, 3);
                case 3967:
                    if (!text2.equals("~=")) {
                        return null;
                    }
                    return TuplesKt.to(3, 3);
                default:
                    return null;
            }
        }

        public final void expr() throws IOException, ParseError {
            subExpr(0);
        }

        public final void subExpr(int limit) throws IOException, ParseError {
            this.exprDepth++;
            if (this.exprDepth > MAX_DEPTH) {
                throw new ParseError();
            }
            try {
                subExprBody(limit);
            } finally {
                this.exprDepth--;
            }
        }

        private final void subExprBody(int limit) throws IOException, ParseError {
            int start = this.p;
            Tok k = peek$default(this, 0, 1, null);
            if (k.isKw("not") || k.isOp("-") || k.isOp("#")) {
                next();
                subExpr(12);
            } else {
                simpleExpr();
            }
            while (true) {
                Tok op = peek$default(this, 0, 1, null);
                Pair<Integer, Integer> pairBinPrio = binPrio(op);
                if (pairBinPrio != null && pairBinPrio.getFirst().intValue() > limit) {
                    next();
                    subExpr(pairBinPrio.getSecond().intValue());
                    if (op.isOp("//")) {
                        op.setOut("/");
                        this.t.get(start).setPrefix("math.floor(" + this.t.get(start).getPrefix());
                        Tok tok = this.t.get(this.p - 1);
                        tok.setSuffix(tok.getSuffix() + ")");
                    }
                } else {
                    return;
                }
            }
        }
    }
}
