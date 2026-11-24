package com.goydevv.studrblx.lua;

import com.goydevv.studrblx.core.OutputLog;
import com.goydevv.studrblx.model.ClassDb;
import com.goydevv.studrblx.model.EnumValue;
import com.goydevv.studrblx.model.Instance;
import com.goydevv.studrblx.model.InstanceFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import kotlin.Unit;
import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.collections.SetsKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaNumber;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.UpValue;
import org.luaj.vm2.Upvaldesc;
import org.luaj.vm2.Varargs;

/* JADX INFO: compiled from: RobloxGlobals.kt */
/* JADX INFO: loaded from: classes4.dex */
public final class RobloxGlobals {
    public static final RobloxGlobals INSTANCE = new RobloxGlobals();
    private static final java.util.Set<LuaTable> FROZEN_TABLES = java.util.Collections.synchronizedSet(
            java.util.Collections.newSetFromMap(new java.util.WeakHashMap<LuaTable, Boolean>()));
    private static final java.util.Map<LuaTable, LuaValue> FROZEN_ORIG_MT = java.util.Collections.synchronizedMap(
            new java.util.WeakHashMap<LuaTable, LuaValue>());

    private static int toU32(LuaValue v) {
        return (int) (long) v.checkdouble();
    }

    private static LuaValue fromU32(int n) {
        return LuaValue.valueOf(((long) n) & 0xFFFFFFFFL);
    }

    public static boolean isFrozenTable(LuaValue v) {
        return (v instanceof LuaTable) && FROZEN_TABLES.contains((LuaTable) v);
    }

    public static void checkNotFrozen(LuaValue v) {
        if (isFrozenTable(v)) {
            throw new LuaError("attempt to modify a readonly table");
        }
    }

    private RobloxGlobals() {
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code duplicated, block: B:44:0x00dd  */
    /* JADX WARN: Code duplicated, block: B:45:0x00e2  */
    /* JADX WARN: Code duplicated, block: B:49:0x00f7  */
    /* JADX WARN: Code duplicated, block: B:54:0x0104  */
    /* JADX WARN: Code duplicated, block: B:57:0x010b  */
    /* JADX WARN: Code duplicated, block: B:74:0x00d5 A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:75:0x00e5 A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:76:0x00db A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:77:0x0100 A[ADDED_TO_REGION, REMOVE, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:80:0x00c1 A[SYNTHETIC] */
    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
    public final Instance resolveRequirePath(Instance caller, String path) {
        Intrinsics.checkNotNullParameter(caller, "caller");
        Intrinsics.checkNotNullParameter(path, "path");
        Iterable iterableSplit = StringsKt.split((CharSequence) path, new char[]{'/'}, false, 0);
        ArrayList<String> parts = new ArrayList<>();
        for (Object obj : iterableSplit) {
            String s = (String) obj;
            if (s.length() > 0) {
                parts.add(s);
            }
        }
        if (parts.isEmpty()) {
            throw new LuaError("require path \"" + path + "\" is empty");
        }
        String first = parts.get(0);
        Instance cur;
        if (".".equals(first)) {
            cur = caller.getParent();
            parts.remove(0);
        } else if ("..".equals(first)) {
            Instance p = caller.getParent();
            cur = p != null ? p.getParent() : null;
            parts.remove(0);
        } else if ("@self".equals(first)) {
            cur = caller;
            parts.remove(0);
        } else {
            throw new LuaError("require path \"" + path + "\" must start with ./, ../ or @self (aliases aren't supported)");
        }
        for (String name : parts) {
            if (".".equals(name)) {
                continue;
            }
            if ("..".equals(name)) {
                cur = cur != null ? cur.getParent() : null;
                continue;
            }
            String clean = StringsKt.removeSuffix(StringsKt.removeSuffix(name, (CharSequence) ".luau"), (CharSequence) ".lua");
            Instance child = cur != null ? Instance.findFirstChild$default(cur, clean, false, 2, null) : null;
            if (child == null) {
                String parentName = (cur != null && cur.fullName() != null) ? cur.fullName() : "nil";
                throw new LuaError("require(\"" + path + "\"): " + clean + " is not a child of " + parentName);
            }
            cur = child;
        }
        if (cur == null) {
            throw new LuaError("require(\"" + path + "\") went above the game");
        }
        return cur;
    }

    public final void install(final LuaRuntime rt) {
        Intrinsics.checkNotNullParameter(rt, "rt");
        final Globals g = rt.getGlobals();
        final long start = System.nanoTime();
        installBit32(g);
        g.set("print", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda35
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$6(g, (Varargs) obj);
            }
        }));
        g.set("warn", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda46
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$7(g, (Varargs) obj);
            }
        }));
        g.set("wait", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda49
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$8(rt, (Varargs) obj);
            }
        }));
        g.set("Wait", g.get("wait"));
        g.set("delay", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda50
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$9(rt, (Varargs) obj);
            }
        }));
        g.set("spawn", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda51
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$10(rt, (Varargs) obj);
            }
        }));
        g.set("tick", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda52
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$11((Varargs) obj);
            }
        }));
        g.set("time", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda53
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$12(rt, (Varargs) obj);
            }
        }));
        g.set("elapsedTime", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda3
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$13(start, (Varargs) obj);
            }
        }));
        g.set("typeof", LuaTypesKt.fn1(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda4
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$14((LuaValue) obj);
            }
        }));
        g.set("game", rt.wrap(rt.getDm()));
        g.set("Game", rt.wrap(rt.getDm()));
        g.set("workspace", rt.wrap(rt.getDm().getWorkspace()));
        g.set("Workspace", rt.wrap(rt.getDm().getWorkspace()));
        g.set("shared", rt.getSharedTable());
        g.set("__iter", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda5
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$15(g, (Varargs) obj);
            }
        }));
        g.set("unpack", g.get("table").get("unpack"));
        installSafePcall(g);
        g.set("require", LuaTypesKt.fn1(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda6
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$16(rt, (LuaValue) obj);
            }
        }));
        g.set("getfenv", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda7
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$19(rt, g, (Varargs) obj);
            }
        }));
        g.set("setfenv", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda8
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$20(rt, (Varargs) obj);
            }
        }));
        g.set("newproxy", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda9
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$21((Varargs) obj);
            }
        }));
        g.set("loadstring", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda10
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$22(g, rt, (Varargs) obj);
            }
        }));
        final LuaTable settingsRoot = StudioSettings.INSTANCE.create(rt);
        g.set("settings", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda11
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$23(settingsRoot, (Varargs) obj);
            }
        }));
        final Instance instanceCreate = InstanceFactory.INSTANCE.create("UserSettings");
        Instance instanceCreate2 = InstanceFactory.INSTANCE.create("UserGameSettings");
        instanceCreate2.setName("GameSettings");
        instanceCreate2.set("ControlMode", new EnumValue(0), false);
        instanceCreate2.set("ComputerMovementMode", new EnumValue(0), false);
        instanceCreate2.set("TouchMovementMode", new EnumValue(0), false);
        instanceCreate2.set("MasterVolume", Float.valueOf(1.0f), false);
        instanceCreate2.set("SavedQualityLevel", new EnumValue(0), false);
        instanceCreate2.set("RotationType", new EnumValue(0), false);
        instanceCreate2.attachRaw(instanceCreate);
        g.set("UserSettings", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda12
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$26(rt, instanceCreate, (Varargs) obj);
            }
        }));
        g.set("version", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda14
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$27((Varargs) obj);
            }
        }));
        g.set("Version", g.get("version"));
        g.set("Delay", g.get("delay"));
        g.set("Spawn", g.get("spawn"));
        g.set("ElapsedTime", g.get("elapsedTime"));
        g.set("printidentity", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda15
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$28((Varargs) obj);
            }
        }));
        g.set("gcinfo", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda16
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$29((Varargs) obj);
            }
        }));
        LuaTable task = new LuaTable();
        task.set("wait", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda17
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$31(rt, (Varargs) obj);
            }
        }));
        task.set("spawn", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda18
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$32(rt, (Varargs) obj);
            }
        }));
        task.set("defer", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda19
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$33(rt, (Varargs) obj);
            }
        }));
        task.set("delay", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda20
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$35(rt, (Varargs) obj);
            }
        }));
        task.set("cancel", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda21
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$37(rt, (Varargs) obj);
            }
        }));
        task.set("synchronize", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda22
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$38((Varargs) obj);
            }
        }));
        task.set("desynchronize", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda23
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$39((Varargs) obj);
            }
        }));
        g.set("task", task);
        LuaTable instLib = new LuaTable();
        instLib.set("new", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda25
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$41(rt, (Varargs) obj);
            }
        }));
        instLib.set("fromExisting", LuaTypesKt.fn1(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda26
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$44(rt, (LuaValue) obj);
            }
        }));
        g.set("Instance", instLib);
        LuaTable luaTable = new LuaTable();
        luaTable.set("new", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda27
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$48$lambda$47((Varargs) obj);
            }
        }));
        Unit unit = Unit.INSTANCE;
        g.set("RaycastParams", luaTable);
        LuaTable luaTable2 = new LuaTable();
        luaTable2.set("new", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda28
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$51$lambda$50((Varargs) obj);
            }
        }));
        Unit unit2 = Unit.INSTANCE;
        g.set("OverlapParams", luaTable2);
        LuaTable luaTable3 = new LuaTable();
        luaTable3.set("new", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda29
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$54$lambda$53((Varargs) obj);
            }
        }));
        Unit unit3 = Unit.INSTANCE;
        g.set("DockWidgetPluginGuiInfo", luaTable3);
        LuaValue math = g.get("math");
        math.set("clamp", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda30
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$55((Varargs) obj);
            }
        }));
        math.set("sign", LuaTypesKt.fn1(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda31
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$56((LuaValue) obj);
            }
        }));
        math.set("round", LuaTypesKt.fn1(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda32
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$57((LuaValue) obj);
            }
        }));
        math.set("noise", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda33
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$58((Varargs) obj);
            }
        }));
        math.set("lerp", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda34
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$59((Varargs) obj);
            }
        }));
        math.set("map", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda36
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$60((Varargs) obj);
            }
        }));
        math.set("log", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                double x = a.arg(1).checkdouble();
                if (a.arg(2).isnil()) {
                    return LuaValue.valueOf(Math.log(x));
                }
                double base = a.arg(2).checkdouble();
                if (base == 2.0d) return LuaValue.valueOf(Math.log(x) / Math.log(2.0d));
                if (base == 10.0d) return LuaValue.valueOf(Math.log10(x));
                return LuaValue.valueOf(Math.log(x) / Math.log(base));
            }
        }));
        LuaValue string = g.get("string");
        string.set("split", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda37
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$63((Varargs) obj);
            }
        }));
        LuaValue table = g.get("table");
        table.set("find", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda38
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$64((Varargs) obj);
            }
        }));
        table.set("clear", LuaTypesKt.fn1(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda39
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$66((LuaValue) obj);
            }
        }));
        table.set("create", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda40
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$67((Varargs) obj);
            }
        }));
        table.set("freeze", LuaTypesKt.fn1(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda41
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$68((LuaValue) obj);
            }
        }));
        table.set("isfrozen", LuaTypesKt.fn1(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda42
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$69((LuaValue) obj);
            }
        }));
        table.set("clone", LuaTypesKt.fn1(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda43
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$70((LuaValue) obj);
            }
        }));
        table.set("move", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda44
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$71((Varargs) obj);
            }
        }));
        table.set("getn", LuaTypesKt.fn1(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda45
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$72((LuaValue) obj);
            }
        }));
        LuaValue tblUnpack = LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                LuaTable t = a.arg(1).checktable();
                int i = a.arg(2).optint(1);
                int j = a.arg(3).isnil() ? t.length() : a.arg(3).checkint();
                if (i > j) {
                    return LuaValue.NONE;
                }
                long lcount = ((long) j - (long) i) + 1L;
                if (lcount > 1000000L) {
                    throw new LuaError("too many results to unpack");
                }
                int count = (int) lcount;
                LuaValue[] out = new LuaValue[count];
                for (int idx = 0; idx < count; idx++) {
                    out[idx] = t.get(i + idx);
                }
                return LuaValue.varargsOf(out);
            }
        });
        table.set("unpack", tblUnpack);
        g.set("unpack", tblUnpack);
        final LuaValue origInsert = table.get("insert");
        if (origInsert.isfunction()) {
            table.set("insert", LuaTypesKt.fnv(new Function1() {
                @Override
                public final Object invoke(Object obj) {
                    Varargs a = (Varargs) obj;
                    checkNotFrozen(a.arg(1));
                    return origInsert.invoke(a);
                }
            }));
        }
        final LuaValue origRemove = table.get("remove");
        if (origRemove.isfunction()) {
            table.set("remove", LuaTypesKt.fnv(new Function1() {
                @Override
                public final Object invoke(Object obj) {
                    Varargs a = (Varargs) obj;
                    checkNotFrozen(a.arg(1));
                    return origRemove.invoke(a);
                }
            }));
        }
        final LuaValue origSort = table.get("sort");
        if (origSort.isfunction()) {
            table.set("sort", LuaTypesKt.fnv(new Function1() {
                @Override
                public final Object invoke(Object obj) {
                    Varargs a = (Varargs) obj;
                    checkNotFrozen(a.arg(1));
                    return origSort.invoke(a);
                }
            }));
        }
        final LuaValue origRawset = g.get("rawset");
        if (origRawset.isfunction()) {
            g.set("rawset", LuaTypesKt.fnv(new Function1() {
                @Override
                public final Object invoke(Object obj) {
                    Varargs a = (Varargs) obj;
                    checkNotFrozen(a.arg(1));
                    return origRawset.invoke(a);
                }
            }));
        }
        final LuaValue origSetmetatable = g.get("setmetatable");
        if (origSetmetatable.isfunction()) {
            g.set("setmetatable", LuaTypesKt.fnv(new Function1() {
                @Override
                public final Object invoke(Object obj) {
                    Varargs a = (Varargs) obj;
                    checkNotFrozen(a.arg(1));
                    return origSetmetatable.invoke(a);
                }
            }));
        }
        LuaTable utf8 = new LuaTable();
        utf8.set("char", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda47
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$73((Varargs) obj);
            }
        }));
        utf8.set("len", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                return utf8Len((Varargs) obj);
            }
        }));
        utf8.set("charpattern", LuaString.valueOf(new byte[]{
            '[', 0x00, '-', 0x7F, (byte) 0xC2, '-', (byte) 0xFD, ']',
            '[', (byte) 0x80, '-', (byte) 0xBF, ']', '*'
        }));
        utf8.set("nfdnormalize", LuaTypesKt.fn1(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                String s = ((LuaValue) obj).checkjstring();
                return LuaValue.valueOf(java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD));
            }
        }));
        utf8.set("nfcnormalize", LuaTypesKt.fn1(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                String s = ((LuaValue) obj).checkjstring();
                return LuaValue.valueOf(java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFC));
            }
        }));
        utf8.set("codepoint", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                return utf8Codepoint((Varargs) obj);
            }
        }));
        utf8.set("offset", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                return utf8Offset((Varargs) obj);
            }
        }));
        utf8.set("codes", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                LuaString ls = a.arg(1).checkstring();
                byte[] raw = new byte[ls.m_length];
                System.arraycopy(ls.m_bytes, ls.m_offset, raw, 0, ls.m_length);
                String decoded = new String(raw, Charsets.UTF_8);
                final ArrayList<int[]> entries = new ArrayList<>();
                int byteOff = 1;
                int ci = 0;
                while (ci < decoded.length()) {
                    int cp = decoded.codePointAt(ci);
                    entries.add(new int[]{byteOff, cp});
                    byteOff += (cp <= 0x7F) ? 1 : (cp <= 0x7FF) ? 2 : (cp <= 0xFFFF) ? 3 : 4;
                    ci += Character.charCount(cp);
                }
                final int[] cursor = new int[]{0};
                LuaValue iter = LuaTypesKt.fnv(new Function1() {
                    @Override
                    public final Object invoke(Object iterObj) {
                        if (cursor[0] >= entries.size()) {
                            return LuaValue.NIL;
                        }
                        int[] pair = entries.get(cursor[0]++);
                        return LuaValue.varargsOf(LuaValue.valueOf(pair[0]), LuaValue.valueOf(pair[1]));
                    }
                });
                return LuaValue.varargsOf(iter, ls, LuaValue.valueOf(0));
            }
        }));
        utf8.set("graphemes", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                LuaString ls = a.arg(1).checkstring();
                int byteLen = ls.m_length;
                int iPos = a.arg(2).optint(1);
                int jPos = a.arg(3).optint(byteLen);
                if (iPos < 0) iPos = byteLen + iPos + 1;
                if (jPos < 0) jPos = byteLen + jPos + 1;
                if (iPos < 1) iPos = 1;
                if (jPos > byteLen) jPos = byteLen;
                String full = new String(ls.m_bytes, ls.m_offset, ls.m_length, Charsets.UTF_8);
                int[] charToByte = new int[full.length() + 1];
                int bOff = 1;
                int ci = 0;
                while (ci < full.length()) {
                    int cp = full.codePointAt(ci);
                    int cc = Character.charCount(cp);
                    int bl = (cp <= 0x7F) ? 1 : (cp <= 0x7FF) ? 2 : (cp <= 0xFFFF) ? 3 : 4;
                    charToByte[ci] = bOff;
                    if (cc == 2 && ci + 1 < full.length()) {
                        charToByte[ci + 1] = bOff;
                    }
                    bOff += bl;
                    ci += cc;
                }
                charToByte[full.length()] = bOff;
                final ArrayList<int[]> spans = new ArrayList<>();
                if (iPos <= jPos && !full.isEmpty()) {
                    java.text.BreakIterator bi = java.text.BreakIterator.getCharacterInstance(java.util.Locale.ROOT);
                    bi.setText(full);
                    int start = bi.first();
                    for (int end = bi.next(); end != java.text.BreakIterator.DONE; start = end, end = bi.next()) {
                        int firstB = charToByte[start];
                        int lastB = charToByte[end] - 1;
                        if (lastB >= iPos && firstB <= jPos) {
                            spans.add(new int[]{Math.max(firstB, iPos), Math.min(lastB, jPos)});
                        }
                    }
                }
                final int[] cursor = new int[]{0};
                LuaValue iter = LuaTypesKt.fnv(new Function1() {
                    @Override
                    public final Object invoke(Object iterObj) {
                        if (cursor[0] >= spans.size()) {
                            return LuaValue.NIL;
                        }
                        int[] span = spans.get(cursor[0]++);
                        return LuaValue.varargsOf(LuaValue.valueOf(span[0]), LuaValue.valueOf(span[1]));
                    }
                });
                return LuaValue.varargsOf(iter, ls, LuaValue.NIL);
            }
        }));
        g.set("utf8", utf8);
        math.set("isfinite", LuaTypesKt.fn1(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                double d = ((LuaValue) obj).checkdouble();
                return LuaValue.valueOf(!Double.isNaN(d) && !Double.isInfinite(d));
            }
        }));
        math.set("isnan", LuaTypesKt.fn1(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                return LuaValue.valueOf(Double.isNaN(((LuaValue) obj).checkdouble()));
            }
        }));
        math.set("isinf", LuaTypesKt.fn1(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                return LuaValue.valueOf(Double.isInfinite(((LuaValue) obj).checkdouble()));
            }
        }));
        math.set("log10", LuaTypesKt.fn1(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                return LuaValue.valueOf(Math.log10(((LuaValue) obj).checkdouble()));
            }
        }));
        table.set("foreach", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                LuaTable t = a.arg(1).checktable();
                LuaFunction f = a.arg(2).checkfunction();
                LuaValue k = LuaValue.NIL;
                while (true) {
                    Varargs n = t.next(k);
                    k = n.arg1();
                    if (k.isnil()) {
                        return LuaValue.NIL;
                    }
                    LuaValue r = f.call(k, n.arg(2));
                    if (!r.isnil()) {
                        return r;
                    }
                }
            }
        }));
        table.set("foreachi", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                LuaTable t = a.arg(1).checktable();
                LuaFunction f = a.arg(2).checkfunction();
                int len = t.length();
                for (int i = 1; i <= len; i++) {
                    LuaValue r = f.call(LuaValue.valueOf(i), t.get(i));
                    if (!r.isnil()) {
                        return r;
                    }
                }
                return LuaValue.NIL;
            }
        }));
        table.set("maxn", LuaTypesKt.fn1(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                LuaTable t = ((LuaValue) obj).checktable();
                double max = 0.0d;
                LuaValue k = LuaValue.NIL;
                while (true) {
                    Varargs n = t.next(k);
                    k = n.arg1();
                    if (k.isnil()) {
                        return LuaValue.valueOf(max);
                    }
                    if (k.isnumber()) {
                        double d = k.todouble();
                        if (d > max) {
                            max = d;
                        }
                    }
                }
            }
        }));
        installCoroutineExtras(g, rt);
        installBuffer(g);
        installDateTime(g);
        installDebugAndOs(g, rt, table, string);
    }

    private final void installBit32(Globals g) {
        LuaTable b = new LuaTable();
        b.set("band", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                int acc = -1;
                int n = a.narg();
                for (int i = 1; i <= n; i++) {
                    acc &= toU32(a.arg(i));
                }
                return fromU32(acc);
            }
        }));
        b.set("bor", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                int acc = 0;
                int n = a.narg();
                for (int i = 1; i <= n; i++) {
                    acc |= toU32(a.arg(i));
                }
                return fromU32(acc);
            }
        }));
        b.set("bxor", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                int acc = 0;
                int n = a.narg();
                for (int i = 1; i <= n; i++) {
                    acc ^= toU32(a.arg(i));
                }
                return fromU32(acc);
            }
        }));
        b.set("bnot", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                return fromU32(~toU32(a.arg(1)));
            }
        }));
        b.set("btest", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                int acc = -1;
                int n = a.narg();
                for (int i = 1; i <= n; i++) {
                    acc &= toU32(a.arg(i));
                }
                return LuaValue.valueOf(acc != 0);
            }
        }));
        b.set("lshift", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                int x = toU32(a.arg(1));
                int disp = a.arg(2).checkint();
                if (disp >= 32 || disp <= -32) return LuaValue.ZERO;
                return fromU32(disp >= 0 ? (x << disp) : (x >>> (-disp)));
            }
        }));
        b.set("rshift", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                int x = toU32(a.arg(1));
                int disp = a.arg(2).checkint();
                if (disp >= 32 || disp <= -32) return LuaValue.ZERO;
                return fromU32(disp >= 0 ? (x >>> disp) : (x << (-disp)));
            }
        }));
        b.set("arshift", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                int x = toU32(a.arg(1));
                int disp = a.arg(2).checkint();
                if (disp >= 32) return fromU32(x < 0 ? -1 : 0);
                if (disp <= -32) return LuaValue.ZERO;
                return fromU32(disp >= 0 ? (x >> disp) : (x << (-disp)));
            }
        }));
        b.set("lrotate", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                int x = toU32(a.arg(1));
                int disp = a.arg(2).checkint() & 31;
                return fromU32(Integer.rotateLeft(x, disp));
            }
        }));
        b.set("rrotate", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                int x = toU32(a.arg(1));
                int disp = a.arg(2).checkint() & 31;
                return fromU32(Integer.rotateRight(x, disp));
            }
        }));
        b.set("extract", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                int x = toU32(a.arg(1));
                int field = a.arg(2).checkint();
                int width = a.arg(3).optint(1);
                if (field < 0) throw new LuaError("field cannot be negative");
                if (width <= 0) throw new LuaError("width must be positive");
                if (field + width > 32) throw new LuaError("trying to access non-existent bits");
                int mask = (width == 32) ? -1 : ((1 << width) - 1);
                return fromU32((x >>> field) & mask);
            }
        }));
        b.set("replace", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                int x = toU32(a.arg(1));
                int v = toU32(a.arg(2));
                int field = a.arg(3).checkint();
                int width = a.arg(4).optint(1);
                if (field < 0) throw new LuaError("field cannot be negative");
                if (width <= 0) throw new LuaError("width must be positive");
                if (field + width > 32) throw new LuaError("trying to access non-existent bits");
                int mask = (width == 32) ? -1 : ((1 << width) - 1);
                return fromU32((x & ~(mask << field)) | ((v & mask) << field));
            }
        }));
        b.set("countlz", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                return LuaValue.valueOf(Integer.numberOfLeadingZeros(toU32(((Varargs) obj).arg(1))));
            }
        }));
        b.set("countrz", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                return LuaValue.valueOf(Integer.numberOfTrailingZeros(toU32(((Varargs) obj).arg(1))));
            }
        }));
        b.set("byteswap", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                return fromU32(Integer.reverseBytes(toU32(((Varargs) obj).arg(1))));
            }
        }));
        g.set("bit32", b);
    }

    private final void installSafePcall(Globals g) {
        final LuaValue origPcall = g.get("pcall");
        if (origPcall.isfunction()) {
            LuaValue safePcall = LuaTypesKt.fnv(new Function1() {
                @Override
                public final Object invoke(Object obj) {
                    Varargs a = (Varargs) obj;
                    try {
                        return origPcall.invoke(a);
                    } catch (org.luaj.vm2.OrphanedThread ot) {
                        throw ot;
                    } catch (StackOverflowError soe) {
                        return LuaValue.varargsOf(LuaValue.FALSE, LuaValue.valueOf("stack overflow"));
                    } catch (Throwable t) {
                        String msg = t.getMessage();
                        return LuaValue.varargsOf(LuaValue.FALSE, LuaValue.valueOf(msg != null && !msg.isEmpty() ? msg : t.toString()));
                    }
                }
            });
            g.set("pcall", safePcall);
            g.set("ypcall", safePcall);
        } else {
            g.set("ypcall", origPcall);
        }
        final LuaValue origXpcall = g.get("xpcall");
        if (origXpcall.isfunction()) {
            g.set("xpcall", LuaTypesKt.fnv(new Function1() {
                @Override
                public final Object invoke(Object obj) {
                    Varargs a = (Varargs) obj;
                    try {
                        return origXpcall.invoke(a);
                    } catch (org.luaj.vm2.OrphanedThread ot) {
                        throw ot;
                    } catch (StackOverflowError soe) {
                        LuaValue errObj = LuaValue.valueOf("stack overflow");
                        LuaValue errh = a.arg(2);
                        if (errh.isfunction()) {
                            try {
                                errObj = errh.call(errObj);
                            } catch (Throwable ignored) {
                                errObj = LuaValue.valueOf("error in error handling");
                            }
                        }
                        return LuaValue.varargsOf(LuaValue.FALSE, errObj);
                    } catch (Throwable t) {
                        String msg = t.getMessage();
                        LuaValue errObj = LuaValue.valueOf(msg != null && !msg.isEmpty() ? msg : t.toString());
                        LuaValue errh = a.arg(2);
                        if (errh.isfunction()) {
                            try {
                                errObj = errh.call(errObj);
                            } catch (Throwable ignored) {
                                errObj = LuaValue.valueOf("error in error handling");
                            }
                        }
                        return LuaValue.varargsOf(LuaValue.FALSE, errObj);
                    }
                }
            }));
        }
    }

    private final void installCoroutineExtras(final Globals g, final LuaRuntime rt) {
        LuaValue coroVal = g.get("coroutine");
        if (!coroVal.istable()) {
            return;
        }
        final LuaValue origCreate = coroVal.get("create");
        if (origCreate.isfunction()) {
            coroVal.set("create", LuaTypesKt.fnv(new Function1() {
                @Override
                public final Object invoke(Object obj) {
                    Varargs res = origCreate.invoke((Varargs) obj);
                    LuaValue th = res.arg1();
                    if (th instanceof LuaThread) {
                        rt.setHook((LuaThread) th);
                    }
                    return res;
                }
            }));
        }
        final LuaValue origWrap = coroVal.get("wrap");
        if (origWrap.isfunction()) {
            coroVal.set("wrap", LuaTypesKt.fnv(new Function1() {
                @Override
                public final Object invoke(Object obj) {
                    LuaFunction fn = ((Varargs) obj).arg(1).checkfunction();
                    final LuaThread co = rt.newThread(fn);
                    return LuaTypesKt.fnv(new Function1() {
                        @Override
                        public final Object invoke(Object callArgs) {
                            if (rt.isCancelled(co)) {
                                throw new LuaError("cannot resume dead coroutine");
                            }
                            try {
                                Varargs r = co.resume((Varargs) callArgs);
                                if (r.arg1().toboolean()) {
                                    return r.subargs(2);
                                }
                                LuaValue err = r.arg(2);
                                throw new LuaError(err.isnil() ? "error in coroutine" : err.tojstring());
                            } catch (LuaError le) {
                                throw le;
                            } catch (org.luaj.vm2.OrphanedThread ot) {
                                throw ot;
                            } catch (StackOverflowError soe) {
                                throw new LuaError("stack overflow");
                            } catch (Throwable t) {
                                String msg = t.getMessage();
                                throw new LuaError(msg != null && !msg.isEmpty() ? msg : t.toString());
                            }
                        }
                    });
                }
            }));
        }
        final LuaValue origStatus = coroVal.get("status");
        if (origStatus.isfunction()) {
            coroVal.set("status", LuaTypesKt.fnv(new Function1() {
                @Override
                public final Object invoke(Object obj) {
                    Varargs a = (Varargs) obj;
                    LuaValue th = a.arg(1);
                    if (th instanceof LuaThread && rt.isCancelled((LuaThread) th)) {
                        return LuaValue.valueOf("dead");
                    }
                    return origStatus.invoke(a);
                }
            }));
        }
        final LuaValue origResume = coroVal.get("resume");
        if (origResume.isfunction()) {
            coroVal.set("resume", LuaTypesKt.fnv(new Function1() {
                @Override
                public final Object invoke(Object obj) {
                    Varargs a = (Varargs) obj;
                    LuaValue th = a.arg(1);
                    if (th instanceof LuaThread && rt.isCancelled((LuaThread) th)) {
                        return LuaValue.varargsOf(LuaValue.FALSE, LuaValue.valueOf("cannot resume dead coroutine"));
                    }
                    try {
                        return origResume.invoke(a);
                    } catch (org.luaj.vm2.OrphanedThread ot) {
                        throw ot;
                    } catch (StackOverflowError soe) {
                        return LuaValue.varargsOf(LuaValue.FALSE, LuaValue.valueOf("stack overflow"));
                    } catch (Throwable t) {
                        String msg = t.getMessage();
                        return LuaValue.varargsOf(LuaValue.FALSE, LuaValue.valueOf(msg != null && !msg.isEmpty() ? msg : t.toString()));
                    }
                }
            }));
        }
        coroVal.set("isyieldable", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                LuaThread running = g.running;
                return LuaValue.valueOf(running != null && !running.isMainThread());
            }
        }));
        coroVal.set("close", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                LuaValue th = a.arg(1);
                if (!(th instanceof LuaThread)) {
                    throw new LuaError("bad argument #1 to 'close' (thread expected, got " + LuaTypes.INSTANCE.typeOf(th) + ")");
                }
                LuaThread co = (LuaThread) th;
                if (co == g.running) {
                    return LuaValue.varargsOf(LuaValue.FALSE, LuaValue.valueOf("cannot close a running coroutine"));
                }
                rt.cancel(co);
                return LuaValue.TRUE;
            }
        }));
    }

    private static int decodeUtf8(byte[] bytes, int baseOff, int byteLen, int idx, int[] outCp) {
        int b0 = bytes[baseOff + idx] & 0xFF;
        if (b0 < 0x80) {
            outCp[0] = b0;
            return 1;
        }
        if ((b0 & 0xE0) == 0xC0) {
            if (idx + 1 >= byteLen) return -1;
            int b1 = bytes[baseOff + idx + 1] & 0xFF;
            if ((b1 & 0xC0) != 0x80) return -1;
            int cp = ((b0 & 0x1F) << 6) | (b1 & 0x3F);
            if (cp < 0x80) return -1;
            outCp[0] = cp;
            return 2;
        }
        if ((b0 & 0xF0) == 0xE0) {
            if (idx + 2 >= byteLen) return -1;
            int b1 = bytes[baseOff + idx + 1] & 0xFF;
            int b2 = bytes[baseOff + idx + 2] & 0xFF;
            if ((b1 & 0xC0) != 0x80 || (b2 & 0xC0) != 0x80) return -1;
            int cp = ((b0 & 0x0F) << 12) | ((b1 & 0x3F) << 6) | (b2 & 0x3F);
            if (cp < 0x800 || (cp >= 0xD800 && cp <= 0xDFFF)) return -1;
            outCp[0] = cp;
            return 3;
        }
        if ((b0 & 0xF8) == 0xF0) {
            if (idx + 3 >= byteLen) return -1;
            int b1 = bytes[baseOff + idx + 1] & 0xFF;
            int b2 = bytes[baseOff + idx + 2] & 0xFF;
            int b3 = bytes[baseOff + idx + 3] & 0xFF;
            if ((b1 & 0xC0) != 0x80 || (b2 & 0xC0) != 0x80 || (b3 & 0xC0) != 0x80) return -1;
            int cp = ((b0 & 0x07) << 18) | ((b1 & 0x3F) << 12) | ((b2 & 0x3F) << 6) | (b3 & 0x3F);
            if (cp < 0x10000 || cp > 0x10FFFF) return -1;
            outCp[0] = cp;
            return 4;
        }
        return -1;
    }

    private static Varargs utf8Len(Varargs a) {
        LuaString ls = a.arg(1).checkstring();
        int byteLen = ls.m_length;
        int i = a.arg(2).optint(1);
        int j = a.arg(3).optint(-1);
        if (i < 0) i = byteLen + i + 1;
        if (j < 0) j = byteLen + j + 1;
        if (i < 1) throw new LuaError("initial position out of string");
        if (j > byteLen) throw new LuaError("final position out of string");
        int count = 0;
        int pos = i - 1;
        int end = j;
        int[] cp = new int[1];
        while (pos < end) {
            int step = decodeUtf8(ls.m_bytes, ls.m_offset, end, pos, cp);
            if (step < 0) {
                return LuaValue.varargsOf(LuaValue.NIL, LuaValue.valueOf(pos + 1));
            }
            count++;
            pos += step;
        }
        return LuaValue.valueOf(count);
    }

    private static Varargs utf8Codepoint(Varargs a) {
        LuaString ls = a.arg(1).checkstring();
        int byteLen = ls.m_length;
        int i = a.arg(2).optint(1);
        int j = a.arg(3).optint(i);
        if (i < 0) i = byteLen + i + 1;
        if (j < 0) j = byteLen + j + 1;
        if (i < 1) throw new LuaError("out of range");
        if (j > byteLen) throw new LuaError("out of range");
        if (i > j) return LuaValue.NONE;
        ArrayList<LuaValue> cps = new ArrayList<>();
        int pos = i - 1;
        int[] cp = new int[1];
        while (pos < j) {
            int step = decodeUtf8(ls.m_bytes, ls.m_offset, byteLen, pos, cp);
            if (step < 0) {
                throw new LuaError("invalid UTF-8 code");
            }
            cps.add(LuaValue.valueOf(cp[0]));
            pos += step;
        }
        return LuaValue.varargsOf(cps.toArray(new LuaValue[0]));
    }

    private static Varargs utf8Offset(Varargs a) {
        LuaString ls = a.arg(1).checkstring();
        int byteLen = ls.m_length;
        int n = a.arg(2).checkint();
        int pos = a.arg(3).optint(n >= 0 ? 1 : byteLen + 1);
        if (pos < 0) pos = byteLen + pos + 1;
        pos--;
        if (pos < 0 || pos > byteLen) {
            throw new LuaError("position out of range");
        }
        if (n == 0) {
            while (pos > 0 && pos < byteLen && (ls.m_bytes[ls.m_offset + pos] & 0xC0) == 0x80) {
                pos--;
            }
            return LuaValue.valueOf(pos + 1);
        }
        if (pos < byteLen && (ls.m_bytes[ls.m_offset + pos] & 0xC0) == 0x80) {
            throw new LuaError("initial position is a continuation byte");
        }
        if (n < 0) {
            while (n < 0 && pos > 0) {
                do {
                    pos--;
                } while (pos > 0 && (ls.m_bytes[ls.m_offset + pos] & 0xC0) == 0x80);
                n++;
            }
            return n == 0 ? LuaValue.valueOf(pos + 1) : LuaValue.NIL;
        } else {
            n--;
            while (n > 0 && pos < byteLen) {
                do {
                    pos++;
                } while (pos < byteLen && (ls.m_bytes[ls.m_offset + pos] & 0xC0) == 0x80);
                n--;
            }
            return n == 0 ? LuaValue.valueOf(pos + 1) : LuaValue.NIL;
        }
    }

    private static final java.nio.ByteBuffer checkBuf(LuaValue v) {
        Object ud = v.isuserdata() ? v.touserdata() : null;
        if (ud instanceof java.nio.ByteBuffer) {
            return (java.nio.ByteBuffer) ud;
        }
        throw new org.luaj.vm2.LuaError("buffer expected, got " + LuaTypes.INSTANCE.typeOf(v));
    }

    private static void checkBufRange(java.nio.ByteBuffer bb, int off, int len) {
        if (off < 0 || len < 0 || ((long) off + (long) len) > (long) bb.capacity()) {
            throw new org.luaj.vm2.LuaError("buffer access out of bounds");
        }
    }

    private static void checkBufBits(java.nio.ByteBuffer bb, int bitOff, int bitCnt) {
        if (bitOff < 0 || bitCnt < 0 || bitCnt > 32) {
            throw new org.luaj.vm2.LuaError("buffer bit access out of bounds");
        }
        if (bitCnt > 0) {
            long lastByte = ((long) bitOff + (long) bitCnt - 1L) >>> 3;
            if (lastByte >= (long) bb.capacity()) {
                throw new org.luaj.vm2.LuaError("buffer access out of bounds");
            }
        }
    }

    private final void installBuffer(Globals g) {
        LuaTable buf = new LuaTable();
        buf.set("create", LuaTypesKt.fn1(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                int sz = ((LuaValue) obj).checkint();
                if (sz < 0 || sz > 268435456) throw new org.luaj.vm2.LuaError("buffer size out of range");
                java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocate(sz).order(java.nio.ByteOrder.LITTLE_ENDIAN);
                return new org.luaj.vm2.LuaUserdata(bb);
            }
        }));
        buf.set("fromstring", LuaTypesKt.fn1(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                org.luaj.vm2.LuaString ls = ((LuaValue) obj).checkstring();
                byte[] arr = new byte[ls.m_length];
                System.arraycopy(ls.m_bytes, ls.m_offset, arr, 0, ls.m_length);
                java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap(arr).order(java.nio.ByteOrder.LITTLE_ENDIAN);
                return new org.luaj.vm2.LuaUserdata(bb);
            }
        }));
        buf.set("tostring", LuaTypesKt.fn1(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                java.nio.ByteBuffer bb = checkBuf((LuaValue) obj);
                return org.luaj.vm2.LuaString.valueOf(bb.array(), 0, bb.capacity());
            }
        }));
        buf.set("len", LuaTypesKt.fn1(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                return LuaValue.valueOf(checkBuf((LuaValue) obj).capacity());
            }
        }));
        buf.set("readi8", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int off = a.arg(2).checkint();
                checkBufRange(bb, off, 1);
                return LuaValue.valueOf((int) bb.get(off));
            }
        }));
        buf.set("readu8", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int off = a.arg(2).checkint();
                checkBufRange(bb, off, 1);
                return LuaValue.valueOf(bb.get(off) & 0xFF);
            }
        }));
        buf.set("readi16", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int off = a.arg(2).checkint();
                checkBufRange(bb, off, 2);
                return LuaValue.valueOf((int) bb.getShort(off));
            }
        }));
        buf.set("readu16", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int off = a.arg(2).checkint();
                checkBufRange(bb, off, 2);
                return LuaValue.valueOf(bb.getShort(off) & 0xFFFF);
            }
        }));
        buf.set("readi32", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int off = a.arg(2).checkint();
                checkBufRange(bb, off, 4);
                return LuaValue.valueOf(bb.getInt(off));
            }
        }));
        buf.set("readu32", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int off = a.arg(2).checkint();
                checkBufRange(bb, off, 4);
                return LuaValue.valueOf(((long) bb.getInt(off)) & 0xFFFFFFFFL);
            }
        }));
        buf.set("readf32", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int off = a.arg(2).checkint();
                checkBufRange(bb, off, 4);
                return LuaValue.valueOf((double) bb.getFloat(off));
            }
        }));
        buf.set("readf64", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int off = a.arg(2).checkint();
                checkBufRange(bb, off, 8);
                return LuaValue.valueOf(bb.getDouble(off));
            }
        }));
        buf.set("writei8", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int off = a.arg(2).checkint();
                checkBufRange(bb, off, 1);
                bb.put(off, (byte) (long) a.arg(3).checkdouble());
                return LuaValue.NONE;
            }
        }));
        buf.set("writeu8", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int off = a.arg(2).checkint();
                checkBufRange(bb, off, 1);
                bb.put(off, (byte) (((long) a.arg(3).checkdouble()) & 0xFFL));
                return LuaValue.NONE;
            }
        }));
        buf.set("writei16", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int off = a.arg(2).checkint();
                checkBufRange(bb, off, 2);
                bb.putShort(off, (short) (long) a.arg(3).checkdouble());
                return LuaValue.NONE;
            }
        }));
        buf.set("writeu16", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int off = a.arg(2).checkint();
                checkBufRange(bb, off, 2);
                bb.putShort(off, (short) (((long) a.arg(3).checkdouble()) & 0xFFFFL));
                return LuaValue.NONE;
            }
        }));
        buf.set("writei32", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int off = a.arg(2).checkint();
                checkBufRange(bb, off, 4);
                bb.putInt(off, (int) (long) a.arg(3).checkdouble());
                return LuaValue.NONE;
            }
        }));
        buf.set("writeu32", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int off = a.arg(2).checkint();
                checkBufRange(bb, off, 4);
                bb.putInt(off, (int) (long) a.arg(3).checkdouble());
                return LuaValue.NONE;
            }
        }));
        buf.set("writef32", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int off = a.arg(2).checkint();
                checkBufRange(bb, off, 4);
                bb.putFloat(off, (float) a.arg(3).checkdouble());
                return LuaValue.NONE;
            }
        }));
        buf.set("writef64", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int off = a.arg(2).checkint();
                checkBufRange(bb, off, 8);
                bb.putDouble(off, a.arg(3).checkdouble());
                return LuaValue.NONE;
            }
        }));
        buf.set("readstring", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int off = a.arg(2).checkint();
                int cnt = a.arg(3).checkint();
                checkBufRange(bb, off, cnt);
                return org.luaj.vm2.LuaString.valueOf(bb.array(), off, cnt);
            }
        }));
        buf.set("writestring", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int off = a.arg(2).checkint();
                org.luaj.vm2.LuaString ls = a.arg(3).checkstring();
                int cnt = a.arg(4).isnil() ? ls.m_length : a.arg(4).checkint();
                if (cnt < 0 || cnt > ls.m_length) {
                    throw new org.luaj.vm2.LuaError("buffer access out of bounds");
                }
                checkBufRange(bb, off, cnt);
                System.arraycopy(ls.m_bytes, ls.m_offset, bb.array(), off, cnt);
                return LuaValue.NONE;
            }
        }));
        buf.set("copy", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer dst = checkBuf(a.arg(1));
                int dstOff = a.arg(2).checkint();
                java.nio.ByteBuffer src = checkBuf(a.arg(3));
                int srcOff = a.arg(4).optint(0);
                int cnt = a.arg(5).isnil() ? (src.capacity() - srcOff) : a.arg(5).checkint();
                checkBufRange(src, srcOff, cnt);
                checkBufRange(dst, dstOff, cnt);
                System.arraycopy(src.array(), srcOff, dst.array(), dstOff, cnt);
                return LuaValue.NONE;
            }
        }));
        buf.set("fill", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int off = a.arg(2).checkint();
                byte val = (byte) (((long) a.arg(3).checkdouble()) & 0xFFL);
                int cnt = a.arg(4).isnil() ? (bb.capacity() - off) : a.arg(4).checkint();
                checkBufRange(bb, off, cnt);
                java.util.Arrays.fill(bb.array(), off, off + cnt, val);
                return LuaValue.NONE;
            }
        }));
        buf.set("readbits", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int bitOff = a.arg(2).checkint();
                int bitCnt = a.arg(3).checkint();
                checkBufBits(bb, bitOff, bitCnt);
                byte[] arr = bb.array();
                long res = 0L;
                for (int i = 0; i < bitCnt; i++) {
                    int bIdx = (bitOff + i) >>> 3;
                    int bBit = (bitOff + i) & 7;
                    if (((arr[bIdx] >>> bBit) & 1) != 0) {
                        res |= (1L << i);
                    }
                }
                return LuaValue.valueOf(res);
            }
        }));
        buf.set("writebits", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                java.nio.ByteBuffer bb = checkBuf(a.arg(1));
                int bitOff = a.arg(2).checkint();
                int bitCnt = a.arg(3).checkint();
                long val = (long) a.arg(4).checkdouble();
                checkBufBits(bb, bitOff, bitCnt);
                byte[] arr = bb.array();
                for (int i = 0; i < bitCnt; i++) {
                    int bIdx = (bitOff + i) >>> 3;
                    int bBit = (bitOff + i) & 7;
                    if (((val >>> i) & 1L) != 0L) {
                        arr[bIdx] = (byte) (arr[bIdx] | (1 << bBit));
                    } else {
                        arr[bIdx] = (byte) (arr[bIdx] & ~(1 << bBit));
                    }
                }
                return LuaValue.NONE;
            }
        }));
        g.set("buffer", buf);
    }

    private static final LuaTable makeDateTime(final long ms) {
        LuaTable t = new LuaTable();
        t.set("UnixTimestamp", LuaValue.valueOf(ms / 1000L));
        t.set("UnixTimestampMillis", LuaValue.valueOf(ms));
        t.set("ToIsoDate", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US);
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                return LuaValue.valueOf(sdf.format(new java.util.Date(ms)));
            }
        }));
        t.set("ToUniversalTime", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
                cal.setTimeInMillis(ms);
                LuaTable r = new LuaTable();
                r.set("Year", cal.get(java.util.Calendar.YEAR));
                r.set("Month", cal.get(java.util.Calendar.MONTH) + 1);
                r.set("Day", cal.get(java.util.Calendar.DAY_OF_MONTH));
                r.set("Hour", cal.get(java.util.Calendar.HOUR_OF_DAY));
                r.set("Minute", cal.get(java.util.Calendar.MINUTE));
                r.set("Second", cal.get(java.util.Calendar.SECOND));
                r.set("Millisecond", cal.get(java.util.Calendar.MILLISECOND));
                return r;
            }
        }));
        t.set("ToLocalTime", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTimeInMillis(ms);
                LuaTable r = new LuaTable();
                r.set("Year", cal.get(java.util.Calendar.YEAR));
                r.set("Month", cal.get(java.util.Calendar.MONTH) + 1);
                r.set("Day", cal.get(java.util.Calendar.DAY_OF_MONTH));
                r.set("Hour", cal.get(java.util.Calendar.HOUR_OF_DAY));
                r.set("Minute", cal.get(java.util.Calendar.MINUTE));
                r.set("Second", cal.get(java.util.Calendar.SECOND));
                r.set("Millisecond", cal.get(java.util.Calendar.MILLISECOND));
                return r;
            }
        }));
        t.set("FormatUniversalTime", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                String fmt = a.arg(2).optjstring("YYYY-MM-DD HH:mm:ss");
                String loc = a.arg(3).optjstring("en-us");
                return LuaValue.valueOf(formatDateTime(ms, fmt, loc, java.util.TimeZone.getTimeZone("UTC")));
            }
        }));
        t.set("FormatLocalTime", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                String fmt = a.arg(2).optjstring("YYYY-MM-DD HH:mm:ss");
                String loc = a.arg(3).optjstring("en-us");
                return LuaValue.valueOf(formatDateTime(ms, fmt, loc, java.util.TimeZone.getDefault()));
            }
        }));
        LuaTable dtMt = new LuaTable();
        dtMt.set("__type", "DateTime");
        dtMt.set(LuaValue.EQ, LuaTypesKt.fn2(new Function2() {
            @Override
            public final Object invoke(Object o1, Object o2) {
                LuaValue a = (LuaValue) o1;
                LuaValue b = (LuaValue) o2;
                if (!a.istable() || !b.istable()) {
                    return LuaValue.FALSE;
                }
                LuaValue ma = a.get("UnixTimestampMillis");
                LuaValue mb = b.get("UnixTimestampMillis");
                if (!ma.isnumber() || !mb.isnumber()) {
                    return LuaValue.FALSE;
                }
                return LuaValue.valueOf(ma.tolong() == mb.tolong());
            }
        }));
        dtMt.set(LuaValue.TOSTRING, LuaTypesKt.fn1(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US);
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                return LuaValue.valueOf(sdf.format(new java.util.Date(ms)));
            }
        }));
        t.setmetatable(dtMt);
        return t;
    }

    private static String formatDateTime(long ms, String format, String localeStr, java.util.TimeZone tz) {
        java.util.Locale locale;
        try {
            locale = (localeStr != null && !localeStr.isEmpty())
                    ? java.util.Locale.forLanguageTag(localeStr.replace('_', '-'))
                    : java.util.Locale.US;
            if (locale == null || locale.getLanguage().isEmpty()) {
                locale = java.util.Locale.US;
            }
        } catch (Exception e) {
            locale = java.util.Locale.US;
        }
        String f = format != null ? format : "YYYY-MM-DD HH:mm:ss";
        if ("LTS".equals(f)) f = "h:mm:ss A";
        else if ("LT".equals(f)) f = "h:mm A";
        else if ("LLLL".equals(f)) f = "dddd, MMMM D, YYYY h:mm A";
        else if ("LLL".equals(f)) f = "MMMM D, YYYY h:mm A";
        else if ("LL".equals(f)) f = "MMMM D, YYYY";
        else if ("L".equals(f)) f = "MM/DD/YYYY";
        else if ("llll".equals(f)) f = "ddd, MMM D, YYYY h:mm A";
        else if ("lll".equals(f)) f = "MMM D, YYYY h:mm A";
        else if ("ll".equals(f)) f = "MMM D, YYYY";
        else if ("l".equals(f)) f = "M/D/YYYY";
        StringBuilder pat = new StringBuilder();
        int idx = 0;
        int len = f.length();
        while (idx < len) {
            if (f.startsWith("YYYY", idx)) { pat.append("yyyy"); idx += 4; }
            else if (f.startsWith("YY", idx)) { pat.append("yy"); idx += 2; }
            else if (f.startsWith("MMMM", idx)) { pat.append("MMMM"); idx += 4; }
            else if (f.startsWith("MMM", idx)) { pat.append("MMM"); idx += 3; }
            else if (f.startsWith("MM", idx)) { pat.append("MM"); idx += 2; }
            else if (f.startsWith("M", idx)) { pat.append("M"); idx += 1; }
            else if (f.startsWith("DD", idx)) { pat.append("dd"); idx += 2; }
            else if (f.startsWith("D", idx)) { pat.append("d"); idx += 1; }
            else if (f.startsWith("dddd", idx)) { pat.append("EEEE"); idx += 4; }
            else if (f.startsWith("ddd", idx)) { pat.append("EEE"); idx += 3; }
            else if (f.startsWith("HH", idx)) { pat.append("HH"); idx += 2; }
            else if (f.startsWith("H", idx)) { pat.append("H"); idx += 1; }
            else if (f.startsWith("hh", idx)) { pat.append("hh"); idx += 2; }
            else if (f.startsWith("h", idx)) { pat.append("h"); idx += 1; }
            else if (f.startsWith("mm", idx)) { pat.append("mm"); idx += 2; }
            else if (f.startsWith("m", idx)) { pat.append("m"); idx += 1; }
            else if (f.startsWith("SSS", idx)) { pat.append("SSS"); idx += 3; }
            else if (f.startsWith("ss", idx)) { pat.append("ss"); idx += 2; }
            else if (f.startsWith("s", idx)) { pat.append("s"); idx += 1; }
            else if (f.startsWith("A", idx) || f.startsWith("a", idx)) { pat.append("a"); idx += 1; }
            else {
                char ch = f.charAt(idx);
                if (ch == '\'') {
                    pat.append("''");
                } else if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')) {
                    pat.append('\'').append(ch).append('\'');
                } else {
                    pat.append(ch);
                }
                idx++;
            }
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(pat.toString(), locale);
        sdf.setTimeZone(tz);
        return sdf.format(new java.util.Date(ms));
    }


    private static String formatTzOffset(java.util.TimeZone tz, long ms) {
        int offMin = tz.getOffset(ms) / 60000;
        char sign = offMin < 0 ? '-' : '+';
        int absMin = Math.abs(offMin);
        return String.format(java.util.Locale.US, "%c%02d%02d", sign, absMin / 60, absMin % 60);
    }

    private static int readPackIntWidth(String fmt, int[] idxRef, int defWidth) {
        int i = idxRef[0];
        if (i + 1 < fmt.length() && Character.isDigit(fmt.charAt(i + 1))) {
            int num = 0;
            while (i + 1 < fmt.length() && Character.isDigit(fmt.charAt(i + 1))) {
                num = num * 10 + (fmt.charAt(++i) - '0');
            }
            if (num < 1 || num > 16) {
                throw new LuaError("integral size (" + num + ") out of limits [1,16]");
            }
            idxRef[0] = i;
            return num;
        }
        return defWidth;
    }

    private static void writeSizedInt(java.io.ByteArrayOutputStream baos, long val, int size, java.nio.ByteOrder ord) {
        if (ord == java.nio.ByteOrder.LITTLE_ENDIAN) {
            for (int k = 0; k < size; k++) {
                baos.write((int) ((val >>> (k * 8)) & 0xFFL));
            }
        } else {
            for (int k = size - 1; k >= 0; k--) {
                baos.write((int) ((val >>> (k * 8)) & 0xFFL));
            }
        }
    }

    private static long readSizedInt(byte[] buf, int off, int size, boolean signed, java.nio.ByteOrder ord) {
        long res = 0L;
        if (ord == java.nio.ByteOrder.LITTLE_ENDIAN) {
            for (int k = size - 1; k >= 0; k--) {
                res = (res << 8) | (buf[off + k] & 0xFFL);
            }
        } else {
            for (int k = 0; k < size; k++) {
                res = (res << 8) | (buf[off + k] & 0xFFL);
            }
        }
        if (signed && size < 8) {
            long signBit = 1L << (size * 8 - 1);
            if ((res & signBit) != 0L) {
                res |= (~0L << (size * 8));
            }
        }
        return res;
    }

    private final void installDebugAndOs(Globals g, final LuaRuntime rt, LuaValue table, LuaValue string) {
        table.set("pack", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                int n = a.narg();
                LuaTable t = new LuaTable();
                for (int i = 1; i <= n; i++) {
                    t.set(i, a.arg(i));
                }
                t.set("n", n);
                return t;
            }
        }));
        LuaValue existingDbg = g.get("debug");
        LuaTable dbg = existingDbg.istable() ? existingDbg.checktable() : new LuaTable();
        dbg.set("profilebegin", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) { return LuaValue.NONE; }
        }));
        dbg.set("profileend", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) { return LuaValue.NONE; }
        }));
        dbg.set("setmemorycategory", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) { return LuaValue.NONE; }
        }));
        dbg.set("resetmemorycategory", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) { return LuaValue.NONE; }
        }));
        if (!dbg.get("traceback").isfunction()) {
            dbg.set("traceback", LuaTypesKt.fnv(new Function1() {
                @Override
                public final Object invoke(Object obj) {
                    Varargs a = (Varargs) obj;
                    String msg = a.arg(1).isstring() ? a.arg(1).tojstring() : "";
                    return LuaValue.valueOf(msg.length() > 0 ? msg + "\nstack traceback:" : "stack traceback:");
                }
            }));
        }
        dbg.set("info", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                LuaValue target;
                String opts;
                LuaThread threadArg = null;
                if (a.arg(1).isthread()) {
                    threadArg = (LuaThread) a.arg(1);
                    target = a.arg(2);
                    opts = a.arg(3).checkjstring();
                } else {
                    target = a.arg(1);
                    opts = a.arg(2).checkjstring();
                }
                LuaValue infoTbl = LuaValue.NIL;
                try {
                    LuaValue getinfo = rt.getDebugGetinfo();
                    if (getinfo != null && getinfo.isfunction()) {
                        LuaValue queryTarget = target;
                        if (target.isnumber() && threadArg == null) {
                            queryTarget = LuaValue.valueOf(target.checkint() + 1);
                        }
                        if (threadArg != null) {
                            infoTbl = getinfo.invoke(LuaValue.varargsOf(new LuaValue[]{threadArg, queryTarget, LuaValue.valueOf("Slnf")})).arg1();
                        } else {
                            infoTbl = getinfo.call(queryTarget, LuaValue.valueOf("Slnf"));
                        }
                    }
                } catch (Throwable ignored) {
                    infoTbl = LuaValue.NIL;
                }
                if (infoTbl.isnil() && target.isnumber()) {
                    return LuaValue.NIL;
                }
                LuaValue funcVal = infoTbl.istable() ? infoTbl.get("func") : (target.isfunction() ? target : LuaValue.NIL);
                ArrayList<LuaValue> out = new ArrayList<>();
                for (int i = 0; i < opts.length(); i++) {
                    char ch = opts.charAt(i);
                    if (ch == 's') {
                        LuaValue srcV = infoTbl.istable() ? infoTbl.get("short_src") : LuaValue.NIL;
                        if (srcV.isnil() && infoTbl.istable()) srcV = infoTbl.get("source");
                        String s = srcV.isstring() ? rt.cleanError(srcV.tojstring()) : "[Script]";
                        out.add(LuaValue.valueOf(s));
                    } else if (ch == 'l') {
                        LuaValue lV = infoTbl.istable() ? infoTbl.get("currentline") : LuaValue.NIL;
                        if ((!lV.isnumber() || lV.toint() < 0) && infoTbl.istable()) {
                            lV = infoTbl.get("linedefined");
                        }
                        out.add(lV.isnumber() ? lV : LuaValue.valueOf(-1));
                    } else if (ch == 'n') {
                        LuaValue nV = infoTbl.istable() ? infoTbl.get("name") : LuaValue.NIL;
                        out.add(nV.isstring() ? nV : LuaValue.valueOf(""));
                    } else if (ch == 'a') {
                        if (funcVal instanceof LuaClosure) {
                            LuaClosure lc = (LuaClosure) funcVal;
                            out.add(LuaValue.valueOf(lc.p != null ? lc.p.numparams : 0));
                            out.add(LuaValue.valueOf(lc.p != null && lc.p.is_vararg != 0));
                        } else {
                            out.add(LuaValue.valueOf(0));
                            out.add(LuaValue.TRUE);
                        }
                    } else if (ch == 'f') {
                        out.add(funcVal != null ? funcVal : LuaValue.NIL);
                    } else {
                        throw new LuaError("invalid option '" + ch + "'");
                    }
                }
                return LuaValue.varargsOf(out.toArray(new LuaValue[0]));
            }
        }));
        g.set("debug", dbg);

        LuaValue osVal = g.get("os");
        LuaTable osTbl = osVal.istable() ? osVal.checktable() : new LuaTable();
        osTbl.set("clock", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                return LuaValue.valueOf(((double) System.nanoTime()) / 1.0E9d);
            }
        }));
        osTbl.set("difftime", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                return LuaValue.valueOf(a.arg(1).checkdouble() - a.arg(2).checkdouble());
            }
        }));
        osTbl.set("time", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                if (a.arg(1).istable()) {
                    LuaTable t = a.arg(1).checktable();
                    java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
                    cal.set(java.util.Calendar.YEAR, t.get("year").optint(1970));
                    cal.set(java.util.Calendar.MONTH, t.get("month").optint(1) - 1);
                    cal.set(java.util.Calendar.DAY_OF_MONTH, t.get("day").optint(1));
                    cal.set(java.util.Calendar.HOUR_OF_DAY, t.get("hour").optint(12));
                    cal.set(java.util.Calendar.MINUTE, t.get("min").optint(0));
                    cal.set(java.util.Calendar.SECOND, t.get("sec").optint(0));
                    cal.set(java.util.Calendar.MILLISECOND, 0);
                    return LuaValue.valueOf(cal.getTimeInMillis() / 1000L);
                }
                return LuaValue.valueOf(System.currentTimeMillis() / 1000L);
            }
        }));
        osTbl.set("date", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                String fmt = a.arg(1).optjstring("%c");
                long ms = a.arg(2).isnumber() ? ((long) (a.arg(2).checkdouble() * 1000.0d)) : System.currentTimeMillis();
                boolean utc = fmt.startsWith("!");
                String f = utc ? fmt.substring(1) : fmt;
                java.util.TimeZone tz = utc ? java.util.TimeZone.getTimeZone("UTC") : java.util.TimeZone.getDefault();
                java.util.Calendar cal = java.util.Calendar.getInstance(tz);
                cal.setTimeInMillis(ms);
                java.util.Date dateObj = new java.util.Date(ms);
                if ("*t".equals(f)) {
                    LuaTable r = new LuaTable();
                    r.set("year", cal.get(java.util.Calendar.YEAR));
                    r.set("month", cal.get(java.util.Calendar.MONTH) + 1);
                    r.set("day", cal.get(java.util.Calendar.DAY_OF_MONTH));
                    r.set("yday", cal.get(java.util.Calendar.DAY_OF_YEAR));
                    r.set("wday", cal.get(java.util.Calendar.DAY_OF_WEEK));
                    r.set("hour", cal.get(java.util.Calendar.HOUR_OF_DAY));
                    r.set("min", cal.get(java.util.Calendar.MINUTE));
                    r.set("sec", cal.get(java.util.Calendar.SECOND));
                    r.set("isdst", LuaValue.valueOf(tz.inDaylightTime(dateObj)));
                    return r;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < f.length(); i++) {
                    char c = f.charAt(i);
                    if (c == '%' && i + 1 < f.length()) {
                        char nc = f.charAt(++i);
                        switch (nc) {
                            case 'Y': sb.append(String.format(java.util.Locale.US, "%04d", cal.get(java.util.Calendar.YEAR))); break;
                            case 'y': sb.append(String.format(java.util.Locale.US, "%02d", cal.get(java.util.Calendar.YEAR) % 100)); break;
                            case 'm': sb.append(String.format(java.util.Locale.US, "%02d", cal.get(java.util.Calendar.MONTH) + 1)); break;
                            case 'd': sb.append(String.format(java.util.Locale.US, "%02d", cal.get(java.util.Calendar.DAY_OF_MONTH))); break;
                            case 'e': sb.append(String.format(java.util.Locale.US, "%2d", cal.get(java.util.Calendar.DAY_OF_MONTH))); break;
                            case 'H': sb.append(String.format(java.util.Locale.US, "%02d", cal.get(java.util.Calendar.HOUR_OF_DAY))); break;
                            case 'I': {
                                int h12 = cal.get(java.util.Calendar.HOUR);
                                sb.append(String.format(java.util.Locale.US, "%02d", h12 == 0 ? 12 : h12));
                                break;
                            }
                            case 'M': sb.append(String.format(java.util.Locale.US, "%02d", cal.get(java.util.Calendar.MINUTE))); break;
                            case 'S': sb.append(String.format(java.util.Locale.US, "%02d", cal.get(java.util.Calendar.SECOND))); break;
                            case 'p': sb.append(cal.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM ? "AM" : "PM"); break;
                            case 'j': sb.append(String.format(java.util.Locale.US, "%03d", cal.get(java.util.Calendar.DAY_OF_YEAR))); break;
                            case 'w': sb.append(cal.get(java.util.Calendar.DAY_OF_WEEK) - 1); break;
                            case 'a': {
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE", java.util.Locale.US);
                                sdf.setTimeZone(tz);
                                sb.append(sdf.format(dateObj));
                                break;
                            }
                            case 'A': {
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE", java.util.Locale.US);
                                sdf.setTimeZone(tz);
                                sb.append(sdf.format(dateObj));
                                break;
                            }
                            case 'b': case 'h': {
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM", java.util.Locale.US);
                                sdf.setTimeZone(tz);
                                sb.append(sdf.format(dateObj));
                                break;
                            }
                            case 'B': {
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM", java.util.Locale.US);
                                sdf.setTimeZone(tz);
                                sb.append(sdf.format(dateObj));
                                break;
                            }
                            case 'x': {
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yy", java.util.Locale.US);
                                sdf.setTimeZone(tz);
                                sb.append(sdf.format(dateObj));
                                break;
                            }
                            case 'X': case 'T': {
                                sb.append(String.format(java.util.Locale.US, "%02d:%02d:%02d",
                                        cal.get(java.util.Calendar.HOUR_OF_DAY),
                                        cal.get(java.util.Calendar.MINUTE),
                                        cal.get(java.util.Calendar.SECOND)));
                                break;
                            }
                            case 'F': {
                                sb.append(String.format(java.util.Locale.US, "%04d-%02d-%02d",
                                        cal.get(java.util.Calendar.YEAR),
                                        cal.get(java.util.Calendar.MONTH) + 1,
                                        cal.get(java.util.Calendar.DAY_OF_MONTH)));
                                break;
                            }
                            case 'R': {
                                sb.append(String.format(java.util.Locale.US, "%02d:%02d",
                                        cal.get(java.util.Calendar.HOUR_OF_DAY),
                                        cal.get(java.util.Calendar.MINUTE)));
                                break;
                            }
                            case 'Z': sb.append(tz.getDisplayName(tz.inDaylightTime(dateObj), java.util.TimeZone.SHORT, java.util.Locale.US)); break;
                            case 'z': sb.append(formatTzOffset(tz, ms)); break;
                            case 'U': case 'W': sb.append(String.format(java.util.Locale.US, "%02d", cal.get(java.util.Calendar.WEEK_OF_YEAR))); break;
                            case 'c': {
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", java.util.Locale.US);
                                sdf.setTimeZone(tz);
                                sb.append(sdf.format(dateObj));
                                break;
                            }
                            case '%': sb.append('%'); break;
                            default: {
                                sb.append('%').append(nc);
                                break;
                            }
                        }
                    } else {
                        sb.append(c);
                    }
                }
                return LuaValue.valueOf(sb.toString());
            }
        }));
        g.set("os", osTbl);

        final LuaValue origFmt = string.get("format");
        final LuaValue tostringFn = g.get("tostring");
        if (origFmt.isfunction()) {
            string.set("format", LuaTypesKt.fnv(new Function1() {
                @Override
                public final Object invoke(Object obj) {
                    Varargs a = (Varargs) obj;
                    String fmt = a.arg(1).checkjstring();
                    if (!fmt.contains("%*")) {
                        return origFmt.invoke(a);
                    }
                    StringBuilder sb = new StringBuilder();
                    ArrayList<LuaValue> newArgs = new ArrayList<>();
                    newArgs.add(LuaValue.NIL);
                    int argIdx = 2;
                    for (int i = 0; i < fmt.length(); i++) {
                        char c = fmt.charAt(i);
                        if (c == '%' && i + 1 < fmt.length()) {
                            char nc = fmt.charAt(i + 1);
                            if (nc == '%') {
                                sb.append("%%");
                                i++;
                            } else if (nc == '*') {
                                sb.append("%s");
                                LuaValue v = a.arg(argIdx++);
                                newArgs.add(tostringFn.isfunction() ? tostringFn.call(v) : LuaValue.valueOf(v.tojstring()));
                                i++;
                            } else {
                                sb.append(c);
                                int j = i + 1;
                                while (j < fmt.length() && " -+#0123456789.".indexOf(fmt.charAt(j)) >= 0) {
                                    sb.append(fmt.charAt(j++));
                                }
                                if (j < fmt.length()) {
                                    sb.append(fmt.charAt(j));
                                    newArgs.add(a.arg(argIdx++));
                                    i = j;
                                }
                            }
                        } else {
                            sb.append(c);
                        }
                    }
                    newArgs.set(0, LuaValue.valueOf(sb.toString()));
                    return origFmt.invoke(LuaValue.varargsOf(newArgs.toArray(new LuaValue[0])));
                }
            }));
        }
        string.set("packsize", LuaTypesKt.fn1(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                String fmt = ((LuaValue) obj).checkjstring();
                int sz = 0;
                int[] idxRef = new int[1];
                for (int i = 0; i < fmt.length(); i++) {
                    char c = fmt.charAt(i);
                    switch (c) {
                        case '<': case '>': case '=': case ' ': break;
                        case '!': {
                            idxRef[0] = i;
                            readPackIntWidth(fmt, idxRef, 1);
                            i = idxRef[0];
                            break;
                        }
                        case 'b': case 'B': case 'x': sz += 1; break;
                        case 'h': case 'H': sz += 2; break;
                        case 'l': case 'L': case 'j': case 'J': case 'T': case 'f': sz += 4; break;
                        case 'i': case 'I': {
                            idxRef[0] = i;
                            sz += readPackIntWidth(fmt, idxRef, 4);
                            i = idxRef[0];
                            break;
                        }
                        case 'd': case 'n': sz += 8; break;
                        case 'c': {
                            int num = 0;
                            while (i + 1 < fmt.length() && Character.isDigit(fmt.charAt(i + 1))) {
                                num = num * 10 + (fmt.charAt(++i) - '0');
                            }
                            sz += num;
                            break;
                        }
                        case 's': case 'z':
                            throw new LuaError("variable-length format");
                        default: break;
                    }
                }
                return LuaValue.valueOf(sz);
            }
        }));
        string.set("pack", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                String fmt = a.arg(1).checkjstring();
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                java.nio.ByteOrder ord = java.nio.ByteOrder.LITTLE_ENDIAN;
                int argIdx = 2;
                byte[] tmp8 = new byte[8];
                int[] idxRef = new int[1];
                for (int i = 0; i < fmt.length(); i++) {
                    char c = fmt.charAt(i);
                    switch (c) {
                        case '<': ord = java.nio.ByteOrder.LITTLE_ENDIAN; break;
                        case '>': ord = java.nio.ByteOrder.BIG_ENDIAN; break;
                        case '=': case ' ': break;
                        case '!': {
                            idxRef[0] = i;
                            readPackIntWidth(fmt, idxRef, 1);
                            i = idxRef[0];
                            break;
                        }
                        case 'x': baos.write(0); break;
                        case 'b': case 'B': baos.write((int) (((long) a.arg(argIdx++).checkdouble()) & 0xFFL)); break;
                        case 'h': case 'H': {
                            writeSizedInt(baos, (long) a.arg(argIdx++).checkdouble(), 2, ord);
                            break;
                        }
                        case 'l': case 'L': case 'j': case 'J': case 'T': {
                            writeSizedInt(baos, (long) a.arg(argIdx++).checkdouble(), 4, ord);
                            break;
                        }
                        case 'i': case 'I': {
                            idxRef[0] = i;
                            int w = readPackIntWidth(fmt, idxRef, 4);
                            i = idxRef[0];
                            writeSizedInt(baos, (long) a.arg(argIdx++).checkdouble(), w, ord);
                            break;
                        }
                        case 'f': {
                            java.nio.ByteBuffer.wrap(tmp8).order(ord).putFloat(0, (float) a.arg(argIdx++).checkdouble());
                            baos.write(tmp8, 0, 4);
                            break;
                        }
                        case 'd': case 'n': {
                            java.nio.ByteBuffer.wrap(tmp8).order(ord).putDouble(0, a.arg(argIdx++).checkdouble());
                            baos.write(tmp8, 0, 8);
                            break;
                        }
                        case 'z': {
                            org.luaj.vm2.LuaString ls = a.arg(argIdx++).checkstring();
                            baos.write(ls.m_bytes, ls.m_offset, ls.m_length);
                            baos.write(0);
                            break;
                        }
                        case 'c': {
                            int num = 0;
                            while (i + 1 < fmt.length() && Character.isDigit(fmt.charAt(i + 1))) {
                                num = num * 10 + (fmt.charAt(++i) - '0');
                            }
                            org.luaj.vm2.LuaString ls = a.arg(argIdx++).checkstring();
                            if (ls.m_length > num) {
                                throw new LuaError("string longer than given size");
                            }
                            baos.write(ls.m_bytes, ls.m_offset, ls.m_length);
                            for (int p = ls.m_length; p < num; p++) baos.write(0);
                            break;
                        }
                        case 's': {
                            idxRef[0] = i;
                            int w = readPackIntWidth(fmt, idxRef, 4);
                            i = idxRef[0];
                            org.luaj.vm2.LuaString ls = a.arg(argIdx++).checkstring();
                            writeSizedInt(baos, ls.m_length, w, ord);
                            baos.write(ls.m_bytes, ls.m_offset, ls.m_length);
                            break;
                        }
                        default: break;
                    }
                }
                byte[] res = baos.toByteArray();
                return org.luaj.vm2.LuaString.valueOf(res, 0, res.length);
            }
        }));
        string.set("unpack", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                String fmt = a.arg(1).checkjstring();
                org.luaj.vm2.LuaString data = a.arg(2).checkstring();
                int init = a.arg(3).optint(1);
                if (init < 0) init = data.m_length + init + 1;
                int pos = init - 1;
                if (pos < 0 || pos > data.m_length) {
                    throw new LuaError("initial position out of string");
                }
                java.nio.ByteOrder ord = java.nio.ByteOrder.LITTLE_ENDIAN;
                java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap(data.m_bytes, data.m_offset, data.m_length);
                ArrayList<LuaValue> out = new ArrayList<>();
                int[] idxRef = new int[1];
                for (int i = 0; i < fmt.length(); i++) {
                    char c = fmt.charAt(i);
                    bb.order(ord);
                    switch (c) {
                        case '<': ord = java.nio.ByteOrder.LITTLE_ENDIAN; break;
                        case '>': ord = java.nio.ByteOrder.BIG_ENDIAN; break;
                        case '=': case ' ': break;
                        case '!': {
                            idxRef[0] = i;
                            readPackIntWidth(fmt, idxRef, 1);
                            i = idxRef[0];
                            break;
                        }
                        case 'x': {
                            if (pos + 1 > data.m_length) throw new LuaError("data string too short");
                            pos += 1;
                            break;
                        }
                        case 'b': {
                            if (pos + 1 > data.m_length) throw new LuaError("data string too short");
                            out.add(LuaValue.valueOf((int) bb.get(data.m_offset + pos)));
                            pos += 1;
                            break;
                        }
                        case 'B': {
                            if (pos + 1 > data.m_length) throw new LuaError("data string too short");
                            out.add(LuaValue.valueOf(bb.get(data.m_offset + pos) & 0xFF));
                            pos += 1;
                            break;
                        }
                        case 'h': {
                            if (pos + 2 > data.m_length) throw new LuaError("data string too short");
                            out.add(LuaValue.valueOf((int) bb.getShort(data.m_offset + pos)));
                            pos += 2;
                            break;
                        }
                        case 'H': {
                            if (pos + 2 > data.m_length) throw new LuaError("data string too short");
                            out.add(LuaValue.valueOf(bb.getShort(data.m_offset + pos) & 0xFFFF));
                            pos += 2;
                            break;
                        }
                        case 'l': case 'j': {
                            if (pos + 4 > data.m_length) throw new LuaError("data string too short");
                            out.add(LuaValue.valueOf(bb.getInt(data.m_offset + pos)));
                            pos += 4;
                            break;
                        }
                        case 'L': case 'J': case 'T': {
                            if (pos + 4 > data.m_length) throw new LuaError("data string too short");
                            out.add(LuaValue.valueOf(((long) bb.getInt(data.m_offset + pos)) & 0xFFFFFFFFL));
                            pos += 4;
                            break;
                        }
                        case 'i': case 'I': {
                            idxRef[0] = i;
                            int w = readPackIntWidth(fmt, idxRef, 4);
                            i = idxRef[0];
                            if (pos + w > data.m_length) throw new LuaError("data string too short");
                            long val = readSizedInt(data.m_bytes, data.m_offset + pos, w, c == 'i', ord);
                            out.add(LuaValue.valueOf((double) val));
                            pos += w;
                            break;
                        }
                        case 'f': {
                            if (pos + 4 > data.m_length) throw new LuaError("data string too short");
                            out.add(LuaValue.valueOf((double) bb.getFloat(data.m_offset + pos)));
                            pos += 4;
                            break;
                        }
                        case 'd': case 'n': {
                            if (pos + 8 > data.m_length) throw new LuaError("data string too short");
                            out.add(LuaValue.valueOf(bb.getDouble(data.m_offset + pos)));
                            pos += 8;
                            break;
                        }
                        case 'z': {
                            int start = pos;
                            while (pos < data.m_length && data.m_bytes[data.m_offset + pos] != 0) pos++;
                            if (pos >= data.m_length) throw new LuaError("unfinished string for format 'z'");
                            out.add(org.luaj.vm2.LuaString.valueOf(data.m_bytes, data.m_offset + start, pos - start));
                            pos++;
                            break;
                        }
                        case 'c': {
                            int num = 0;
                            while (i + 1 < fmt.length() && Character.isDigit(fmt.charAt(i + 1))) {
                                num = num * 10 + (fmt.charAt(++i) - '0');
                            }
                            if (pos + num > data.m_length) throw new LuaError("data string too short");
                            out.add(org.luaj.vm2.LuaString.valueOf(data.m_bytes, data.m_offset + pos, num));
                            pos += num;
                            break;
                        }
                        case 's': {
                            idxRef[0] = i;
                            int w = readPackIntWidth(fmt, idxRef, 4);
                            i = idxRef[0];
                            if (pos + w > data.m_length) throw new LuaError("data string too short");
                            long slenL = readSizedInt(data.m_bytes, data.m_offset + pos, w, false, ord);
                            pos += w;
                            if (slenL < 0 || slenL > (long) (data.m_length - pos)) {
                                throw new LuaError("data string too short");
                            }
                            int slen = (int) slenL;
                            out.add(org.luaj.vm2.LuaString.valueOf(data.m_bytes, data.m_offset + pos, slen));
                            pos += slen;
                            break;
                        }
                        default: break;
                    }
                }
                out.add(LuaValue.valueOf(pos + 1));
                return LuaValue.varargsOf(out.toArray(new LuaValue[0]));
            }
        }));
        if (LuaString.s_metatable == null) {
            LuaString.s_metatable = LuaValue.tableOf(new LuaValue[]{LuaValue.INDEX, string});
        } else {
            LuaString.s_metatable.set(LuaValue.INDEX, string);
        }
    }

    private final void installDateTime(Globals g) {
        LuaTable dt = new LuaTable();
        dt.set("now", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                return makeDateTime(System.currentTimeMillis());
            }
        }));
        dt.set("fromUnixTimestamp", LuaTypesKt.fn1(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                return makeDateTime((long) (((LuaValue) obj).checkdouble() * 1000.0d));
            }
        }));
        dt.set("fromUnixTimestampMillis", LuaTypesKt.fn1(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                return makeDateTime(((LuaValue) obj).checklong());
            }
        }));
        dt.set("fromIsoDate", LuaTypesKt.fn1(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                String iso = ((LuaValue) obj).checkjstring().trim();
                String[] patterns = new String[]{
                    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                    "yyyy-MM-dd'T'HH:mm:ssXXX",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd"
                };
                for (String pat : patterns) {
                    try {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(pat, java.util.Locale.US);
                        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                        sdf.setLenient(false);
                        java.util.Date d = sdf.parse(iso);
                        if (d != null) {
                            return makeDateTime(d.getTime());
                        }
                    } catch (Exception ignored) {
                    }
                }
                return LuaValue.NIL;
            }
        }));
        dt.set("fromUniversalTime", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                int year = a.arg(1).optint(1970);
                int month = a.arg(2).optint(1);
                int day = a.arg(3).optint(1);
                int hour = a.arg(4).optint(0);
                int minute = a.arg(5).optint(0);
                int second = a.arg(6).optint(0);
                int ms = a.arg(7).optint(0);
                java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
                cal.clear();
                cal.set(year, month - 1, day, hour, minute, second);
                cal.set(java.util.Calendar.MILLISECOND, ms);
                return makeDateTime(cal.getTimeInMillis());
            }
        }));
        dt.set("fromLocalTime", LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                Varargs a = (Varargs) obj;
                int year = a.arg(1).optint(1970);
                int month = a.arg(2).optint(1);
                int day = a.arg(3).optint(1);
                int hour = a.arg(4).optint(0);
                int minute = a.arg(5).optint(0);
                int second = a.arg(6).optint(0);
                int ms = a.arg(7).optint(0);
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.clear();
                cal.set(year, month - 1, day, hour, minute, second);
                cal.set(java.util.Calendar.MILLISECOND, ms);
                return makeDateTime(cal.getTimeInMillis());
            }
        }));
        g.set("DateTime", dt);
    }

    private static final String install$joinArgs(Globals g, Varargs a) {
        StringBuilder sb = new StringBuilder();
        LuaValue tostring = g.get("tostring");
        int i = 1;
        int iNarg = a.narg();
        if (1 <= iNarg) {
            while (true) {
                if (i > 1) {
                    sb.append(' ');
                }
                sb.append(tostring.call(a.arg(i)).tojstring());
                if (i == iNarg) {
                    break;
                }
                i++;
            }
        }
        String string = sb.toString();
        Intrinsics.checkNotNullExpressionValue(string, "toString(...)");
        return string;
    }

    static final Varargs install$lambda$5$lambda$2(Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        LuaInteger luaIntegerValueOf = LuaValue.valueOf(Integer.numberOfLeadingZeros((int) a.arg(1).checklong()));
        Intrinsics.checkNotNullExpressionValue(luaIntegerValueOf, "valueOf(...)");
        return luaIntegerValueOf;
    }

    static final Varargs install$lambda$5$lambda$3(Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        LuaInteger luaIntegerValueOf = LuaValue.valueOf(Integer.numberOfTrailingZeros((int) a.arg(1).checklong()));
        Intrinsics.checkNotNullExpressionValue(luaIntegerValueOf, "valueOf(...)");
        return luaIntegerValueOf;
    }

    static final Varargs install$lambda$5$lambda$4(Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        LuaNumber luaNumberValueOf = LuaValue.valueOf(((long) Integer.reverseBytes((int) a.arg(1).checklong())) & 4294967295L);
        Intrinsics.checkNotNullExpressionValue(luaNumberValueOf, "valueOf(...)");
        return luaNumberValueOf;
    }

    static final Varargs install$lambda$6(Globals $g, Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        OutputLog.INSTANCE.print(install$joinArgs($g, a));
        LuaValue NONE = LuaValue.NONE;
        Intrinsics.checkNotNullExpressionValue(NONE, "NONE");
        return NONE;
    }

    static final Varargs install$lambda$7(Globals $g, Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        OutputLog.INSTANCE.warn(install$joinArgs($g, a));
        LuaValue NONE = LuaValue.NONE;
        Intrinsics.checkNotNullExpressionValue(NONE, "NONE");
        return NONE;
    }

    static final Varargs install$lambda$8(LuaRuntime $rt, Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        return $rt.yieldFor(RangesKt.coerceAtLeast(a.arg(1).optdouble(0.03d), 0.029d));
    }

    static final Varargs install$lambda$9(LuaRuntime $rt, Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        LuaFunction luaFunctionCheckfunction = a.arg(2).checkfunction();
        Intrinsics.checkNotNullExpressionValue(luaFunctionCheckfunction, "checkfunction(...)");
        LuaThread co = $rt.newThread(luaFunctionCheckfunction);
        $rt.delay(a.arg(1).optdouble(0.0d), co);
        LuaValue NONE = LuaValue.NONE;
        Intrinsics.checkNotNullExpressionValue(NONE, "NONE");
        return NONE;
    }

    static final Varargs install$lambda$10(LuaRuntime $rt, Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        LuaFunction luaFunctionCheckfunction = a.arg(1).checkfunction();
        Intrinsics.checkNotNullExpressionValue(luaFunctionCheckfunction, "checkfunction(...)");
        LuaThread co = $rt.newThread(luaFunctionCheckfunction);
        $rt.delay(0.0d, co);
        LuaValue NONE = LuaValue.NONE;
        Intrinsics.checkNotNullExpressionValue(NONE, "NONE");
        return NONE;
    }

    static final Varargs install$lambda$11(Varargs it) {
        Intrinsics.checkNotNullParameter(it, "it");
        LuaNumber luaNumberValueOf = LuaValue.valueOf(System.currentTimeMillis() / 1000.0d);
        Intrinsics.checkNotNullExpressionValue(luaNumberValueOf, "valueOf(...)");
        return luaNumberValueOf;
    }

    static final Varargs install$lambda$12(LuaRuntime $rt, Varargs it) {
        Intrinsics.checkNotNullParameter(it, "it");
        LuaNumber luaNumberValueOf = LuaValue.valueOf($rt.getTime());
        Intrinsics.checkNotNullExpressionValue(luaNumberValueOf, "valueOf(...)");
        return luaNumberValueOf;
    }

    static final Varargs install$lambda$13(long $start, Varargs it) {
        Intrinsics.checkNotNullParameter(it, "it");
        LuaNumber luaNumberValueOf = LuaValue.valueOf((System.nanoTime() - $start) / 1.0E9d);
        Intrinsics.checkNotNullExpressionValue(luaNumberValueOf, "valueOf(...)");
        return luaNumberValueOf;
    }

    static final LuaValue install$lambda$14(LuaValue v) {
        Intrinsics.checkNotNullParameter(v, "v");
        LuaString luaStringValueOf = LuaValue.valueOf(LuaTypes.INSTANCE.typeOf(v));
        Intrinsics.checkNotNullExpressionValue(luaStringValueOf, "valueOf(...)");
        return luaStringValueOf;
    }

    /* JADX WARN: Code restructure failed: missing block: B:27:0x006f, code lost:
    
        if (((r1 == null || (r1 = r1.get(org.luaj.vm2.LuaValue.CALL)) == null || !r1.isfunction()) ? false : true) != false) goto L28;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    static final org.luaj.vm2.Varargs install$lambda$15(org.luaj.vm2.Globals r7, org.luaj.vm2.Varargs r8) {
        Intrinsics.checkNotNullParameter(r8, "a");
        LuaValue v = r8.arg1();
        if (v.isfunction()) {
            return r8;
        }
        if (v.istable()) {
            LuaValue mt = v.getmetatable();
            LuaValue iter = mt != null ? mt.get("__iter") : null;
            if (iter != null && iter.isfunction()) {
                return iter.invoke(v);
            }
            return LuaValue.varargsOf(new LuaValue[]{r7.get("next"), v, LuaValue.NIL});
        }
        if (v.isuserdata()) {
            LuaValue mt = v.getmetatable();
            LuaValue call = mt != null ? mt.get(LuaValue.CALL) : null;
            if (call != null && call.isfunction()) {
                return r8;
            }
        }
        throw new LuaError("attempt to iterate over a " + LuaTypes.INSTANCE.typeOf(v) + " value");
    }

    private static final UpValue install$envSlot(LuaValue f) {
        Upvaldesc[] ups;
        LuaClosure c = f instanceof LuaClosure ? (LuaClosure) f : null;
        if (c == null || (ups = c.p.upvalues) == null) {
            return null;
        }
        int length = ups.length;
        for (int i = 0; i < length; i++) {
            LuaString luaString = ups[i].name;
            if (Intrinsics.areEqual(luaString != null ? luaString.tojstring() : null, "_ENV")) {
                UpValue[] upValues = c.upValues;
                Intrinsics.checkNotNullExpressionValue(upValues, "upValues");
                return (UpValue) ArraysKt.getOrNull(upValues, i);
            }
        }
        return null;
    }

    private static final LuaValue install$funcAt(LuaRuntime $rt, int level) {
        try {
            LuaValue luaValue = $rt.getDebugGetinfo().call(LuaValue.valueOf(level), LuaValue.valueOf("f")).get("func");
            Intrinsics.checkNotNull(luaValue);
            return luaValue;
        } catch (Exception e) {
            LuaValue luaValue2 = LuaValue.NIL;
            Intrinsics.checkNotNull(luaValue2);
            return luaValue2;
        }
    }

    static final LuaValue install$lambda$16(LuaRuntime $rt, LuaValue m) {
        LuaValue luaValue;
        Intrinsics.checkNotNullParameter(m, "m");
        Object obj = m.isuserdata() ? m.touserdata() : null;
        if (!(obj instanceof Instance)) {
            obj = null;
        }
        Instance inst = (Instance) obj;
        if (inst == null) {
            if (m.isnumber()) {
                OutputLog.INSTANCE.warn("require(" + m.tolong() + "): external asset modules are stubbed offline");
                LuaTable stub = new LuaTable();
                LuaTable smt = new LuaTable();
                smt.set(LuaValue.CALL, LuaTypesKt.fnv(new Function1() {
                    @Override
                    public final Object invoke(Object a) {
                        return LuaValue.NIL;
                    }
                }));
                smt.set(LuaValue.INDEX, LuaTypesKt.fn2(new Function2() {
                    @Override
                    public final Object invoke(Object t, Object k) {
                        return LuaTypesKt.fnv(new Function1() {
                            @Override
                            public final Object invoke(Object a) {
                                return LuaValue.NIL;
                            }
                        });
                    }
                }));
                stub.setmetatable(smt);
                return stub;
            }
            if (m.isstring()) {
                LuaTable callerEnv = null;
                for (int lvl = 1; lvl <= 3; lvl++) {
                    UpValue up = install$envSlot(install$funcAt($rt, lvl));
                    LuaValue val = up != null ? up.getValue() : null;
                    if (val instanceof LuaTable && !((LuaTable) val).get("script").isnil()) {
                        callerEnv = (LuaTable) val;
                        break;
                    }
                }
                if (callerEnv == null) {
                    callerEnv = $rt.loadingModuleEnv();
                }
                if (callerEnv != null && (luaValue = callerEnv.get("script")) != null) {
                    Object obj2 = luaValue.isuserdata() ? luaValue.touserdata() : null;
                    Instance caller = (Instance) (obj2 instanceof Instance ? obj2 : null);
                    if (caller != null) {
                        RobloxGlobals robloxGlobals = INSTANCE;
                        String str = m.tojstring();
                        Intrinsics.checkNotNullExpressionValue(str, "tojstring(...)");
                        inst = robloxGlobals.resolveRequirePath(caller, str);
                    }
                }
                if (inst == null) {
                    throw new LuaError("require(\"" + m.tojstring() + "\") needs to be called from a script");
                }
            } else {
                throw new LuaError("Attempted to call require with invalid argument(s).");
            }
        }
        try { return $rt.require(inst); } catch (Throwable _ktEx) { throw com.goydevv.studrblx.util.KtCompat.sneakyThrow(_ktEx); }
    }

    static final Varargs install$lambda$19(LuaRuntime $rt, Globals $g, Varargs a) {
        LuaValue f;
        Object value;
        LuaValue value2;
        Intrinsics.checkNotNullParameter(a, "a");
        LuaValue x = a.arg1();
        if (x.isfunction()) {
            f = x;
        } else if (x.isnil()) {
            f = install$funcAt($rt, 1);
        } else {
            if (!x.isnumber()) {
                throw new LuaError("bad argument #1 to 'getfenv' (number expected)");
            }
            if (x.toint() == 0) {
                LuaTable luaTableLoadingModuleEnv = $rt.loadingModuleEnv();
                if (luaTableLoadingModuleEnv != null) {
                    return luaTableLoadingModuleEnv;
                }
                Object bottom = null;
                for (int lvl = 1; lvl < 251; lvl++) {
                    LuaValue f2 = install$funcAt($rt, lvl);
                    if (f2.isnil()) {
                        break;
                    }
                    UpValue upValueInstall$envSlot = install$envSlot(f2);
                    if (upValueInstall$envSlot != null && (value = upValueInstall$envSlot.getValue()) != null) {
                        bottom = value;
                    }
                }
                return bottom != null ? (Varargs) bottom : $g;
            }
            f = install$funcAt($rt, x.toint());
        }
        Intrinsics.checkNotNull(f);
        UpValue upValueInstall$envSlot2 = install$envSlot(f);
        return (upValueInstall$envSlot2 == null || (value2 = upValueInstall$envSlot2.getValue()) == null) ? $g : value2;
    }

    static final Varargs install$lambda$20(LuaRuntime $rt, Varargs a) {
        LuaValue f;
        Intrinsics.checkNotNullParameter(a, "a");
        LuaValue x = a.arg1();
        LuaTable t = a.arg(2).checktable();
        if (x.isfunction()) {
            f = x;
        } else {
            f = install$funcAt($rt, x.isnil() ? 1 : x.checkint());
        }
        Intrinsics.checkNotNull(f);
        UpValue upValueInstall$envSlot = install$envSlot(f);
        if (upValueInstall$envSlot == null) {
            throw new LuaError("'setfenv' cannot change environment of given object");
        }
        upValueInstall$envSlot.setValue(t);
        return f;
    }

    static final Varargs install$lambda$21(Varargs a) {
        LuaValue mt;
        Intrinsics.checkNotNullParameter(a, "a");
        LuaValue x = a.arg1();
        if (x.isuserdata()) {
            mt = x.getmetatable();
        } else {
            mt = x.toboolean() ? new LuaTable() : null;
        }
        return new LuaUserdata(new Object(), mt);
    }

    static final Varargs install$lambda$22(Globals $g, LuaRuntime $rt, Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        LuaSandbox luaSandbox = LuaSandbox.INSTANCE;
        LuaValue luaValueArg1 = a.arg1();
        Intrinsics.checkNotNullExpressionValue(luaValueArg1, "arg1(...)");
        if (luaSandbox.isBytecode(luaValueArg1)) {
            Varargs varargsVarargsOf = LuaValue.varargsOf(LuaValue.NIL, LuaValue.valueOf("bytecode is not allowed"));
            Intrinsics.checkNotNullExpressionValue(varargsVarargsOf, "varargsOf(...)");
            return varargsVarargsOf;
        }
        String src = a.arg1().checkjstring();
        String name = a.arg(2).optjstring("loadstring");
        try {
            LuaTable env = null;
            for (int lvl = 1; lvl <= 3; lvl++) {
                UpValue up = install$envSlot(install$funcAt($rt, lvl));
                LuaValue val = up != null ? up.getValue() : null;
                if (val instanceof LuaTable) {
                    env = (LuaTable) val;
                    break;
                }
            }
            if (env == null) {
                env = $rt.loadingModuleEnv();
            }
            if (env == null) {
                env = $g;
            }
            Intrinsics.checkNotNull(src);
            return $rt.compile(src, name, env);
        } catch (Throwable e) {
            String message = e.getMessage();
            if (message == null || message.isEmpty()) {
                message = "syntax error";
            }
            return LuaValue.varargsOf(LuaValue.NIL, LuaValue.valueOf($rt.cleanError(message)));
        }
    }

    static final Varargs install$lambda$23(LuaTable $settingsRoot, Varargs it) {
        Intrinsics.checkNotNullParameter(it, "it");
        return $settingsRoot;
    }

    static final Varargs install$lambda$26(LuaRuntime $rt, Instance $userSettings, Varargs it) {
        Intrinsics.checkNotNullParameter(it, "it");
        return $rt.wrap($userSettings);
    }

    static final Varargs install$lambda$27(Varargs it) {
        Intrinsics.checkNotNullParameter(it, "it");
        LuaString luaStringValueOf = LuaValue.valueOf("0.600.0");
        Intrinsics.checkNotNullExpressionValue(luaStringValueOf, "valueOf(...)");
        return luaStringValueOf;
    }

    static final Varargs install$lambda$28(Varargs it) {
        Intrinsics.checkNotNullParameter(it, "it");
        OutputLog.INSTANCE.print("Current identity is 2");
        LuaValue NONE = LuaValue.NONE;
        Intrinsics.checkNotNullExpressionValue(NONE, "NONE");
        return NONE;
    }

    static final Varargs install$lambda$29(Varargs it) {
        Intrinsics.checkNotNullParameter(it, "it");
        LuaNumber luaNumberValueOf = LuaValue.valueOf((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0d);
        Intrinsics.checkNotNullExpressionValue(luaNumberValueOf, "valueOf(...)");
        return luaNumberValueOf;
    }

    static final Varargs install$lambda$31(LuaRuntime $rt, Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        Varargs varargsVarargsOf = LuaValue.varargsOf($rt.yieldFor(a.arg(1).optdouble(0.0d)).arg1(), LuaValue.NONE);
        Intrinsics.checkNotNullExpressionValue(varargsVarargsOf, "let(...)");
        return varargsVarargsOf;
    }

    static final Varargs install$lambda$32(LuaRuntime $rt, Varargs a) {
        LuaThread co;
        Intrinsics.checkNotNullParameter(a, "a");
        LuaValue f = a.arg(1);
        if (f.isthread()) {
            Intrinsics.checkNotNull(f, "null cannot be cast to non-null type org.luaj.vm2.LuaThread");
            co = (LuaThread) f;
        } else {
            LuaFunction luaFunctionCheckfunction = f.checkfunction();
            Intrinsics.checkNotNullExpressionValue(luaFunctionCheckfunction, "checkfunction(...)");
            co = $rt.newThread(luaFunctionCheckfunction);
        }
        Varargs varargsSubargs = a.subargs(2);
        Intrinsics.checkNotNullExpressionValue(varargsSubargs, "subargs(...)");
        $rt.resume(co, varargsSubargs);
        return co;
    }

    static final Varargs install$lambda$33(LuaRuntime $rt, Varargs a) {
        LuaThread co;
        Intrinsics.checkNotNullParameter(a, "a");
        LuaValue f = a.arg(1);
        if (f.isthread()) {
            Intrinsics.checkNotNull(f, "null cannot be cast to non-null type org.luaj.vm2.LuaThread");
            co = (LuaThread) f;
        } else {
            LuaFunction luaFunctionCheckfunction = f.checkfunction();
            Intrinsics.checkNotNullExpressionValue(luaFunctionCheckfunction, "checkfunction(...)");
            co = $rt.newThread(luaFunctionCheckfunction);
        }
        Varargs varargsSubargs = a.subargs(2);
        Intrinsics.checkNotNullExpressionValue(varargsSubargs, "subargs(...)");
        $rt.defer(co, varargsSubargs);
        return co;
    }

    static final Varargs install$lambda$35(final LuaRuntime $rt, Varargs a) {
        final LuaThread co;
        Intrinsics.checkNotNullParameter(a, "a");
        LuaValue f = a.arg(2);
        if (f.isthread()) {
            Intrinsics.checkNotNull(f, "null cannot be cast to non-null type org.luaj.vm2.LuaThread");
            co = (LuaThread) f;
        } else {
            LuaFunction luaFunctionCheckfunction = f.checkfunction();
            Intrinsics.checkNotNullExpressionValue(luaFunctionCheckfunction, "checkfunction(...)");
            co = $rt.newThread(luaFunctionCheckfunction);
        }
        final Varargs args = a.subargs(3);
        $rt.delayWithArgs(a.arg(1).optdouble(0.0d), co, args);
        return co;
    }

    static final Unit install$lambda$35$lambda$34(LuaRuntime $rt, LuaThread $co, Varargs $args) {
        Intrinsics.checkNotNull($args);
        $rt.resume($co, $args);
        return Unit.INSTANCE;
    }

    static final Varargs install$lambda$37(LuaRuntime $rt, Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        LuaValue luaValueArg = a.arg(1);
        LuaThread luaThread = luaValueArg instanceof LuaThread ? (LuaThread) luaValueArg : null;
        if (luaThread != null) {
            $rt.cancel(luaThread);
        }
        LuaValue NONE = LuaValue.NONE;
        Intrinsics.checkNotNullExpressionValue(NONE, "NONE");
        return NONE;
    }

    static final Varargs install$lambda$38(Varargs it) {
        Intrinsics.checkNotNullParameter(it, "it");
        LuaValue NONE = LuaValue.NONE;
        Intrinsics.checkNotNullExpressionValue(NONE, "NONE");
        return NONE;
    }

    static final Varargs install$lambda$39(Varargs it) {
        Intrinsics.checkNotNullParameter(it, "it");
        LuaValue NONE = LuaValue.NONE;
        Intrinsics.checkNotNullExpressionValue(NONE, "NONE");
        return NONE;
    }

    static final Varargs install$lambda$41(LuaRuntime $rt, Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        String cls = a.arg(1).checkjstring();
        ClassDb classDb = ClassDb.INSTANCE;
        Intrinsics.checkNotNull(cls);
        if (classDb.isServiceName(cls) || SetsKt.setOf(new String[]{"DataModel", "Player", "PlayerGui"}).contains(cls)) {
            throw new LuaError("Unable to create an Instance of type \"" + cls + "\"");
        }
        Instance i = InstanceFactory.INSTANCE.create(cls);
        LuaValue luaValueArg = a.arg(2);
        Intrinsics.checkNotNullExpressionValue(luaValueArg, "arg(...)");
        Object obj = luaValueArg.isuserdata() ? luaValueArg.touserdata() : null;
        if (!(obj instanceof Instance)) {
            obj = null;
        }
        Instance instance = (Instance) obj;
        if (instance != null) {
            Instance.setParent$default(i, instance, 0, 2, null);
        }
        return $rt.wrap(i);
    }

    static final LuaValue install$lambda$44(LuaRuntime $rt, LuaValue v) {
        Intrinsics.checkNotNullParameter(v, "v");
        Object obj = v.isuserdata() ? v.touserdata() : null;
        Instance instance = (Instance) (obj instanceof Instance ? obj : null);
        if (instance == null) {
            throw new LuaError("Instance expected, got " + LuaTypes.INSTANCE.typeOf(v));
        }
        Instance instanceCloneTree = instance.cloneTree();
        Iterator it = CollectionsKt.toList(instanceCloneTree.getChildren()).iterator();
        while (it.hasNext()) {
            ((Instance) it.next()).destroy();
        }
        return $rt.wrap(instanceCloneTree);
    }

    static final Varargs install$lambda$48$lambda$47(Varargs varargs) {
        Intrinsics.checkNotNullParameter(varargs, "<unused var>");
        LuaTable luaTable = new LuaTable();
        luaTable.set("FilterDescendantsInstances", new LuaTable());
        luaTable.set("FilterType", LuaTypes.INSTANCE.wrap(new EnumItemV("RaycastFilterType", "Exclude", 0)));
        luaTable.set("IgnoreWater", LuaValue.FALSE);
        luaTable.set("CollisionGroup", "Default");
        luaTable.set("RespectCanCollide", LuaValue.FALSE);
        luaTable.set("AddToFilter", LuaTypesKt.fnv(new Function1() { // from class: com.goydevv.studrblx.lua.RobloxGlobals$$ExternalSyntheticLambda1
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RobloxGlobals.install$lambda$48$lambda$47$lambda$46$lambda$45((Varargs) obj);
            }
        }));
        return luaTable;
    }

    static final Varargs install$lambda$48$lambda$47$lambda$46$lambda$45(Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        LuaValue list = a.arg(1).get("FilterDescendantsInstances");
        LuaValue v = a.arg(2);
        if (v.istable()) {
            int i = 1;
            int length = v.length();
            if (1 <= length) {
                while (true) {
                    list.set(list.length() + 1, v.get(i));
                    if (i == length) {
                        break;
                    }
                    i++;
                }
            }
        } else {
            int i2 = list.length();
            list.set(i2 + 1, v);
        }
        LuaValue NONE = LuaValue.NONE;
        Intrinsics.checkNotNullExpressionValue(NONE, "NONE");
        return NONE;
    }

    static final Varargs install$lambda$51$lambda$50(Varargs varargs) {
        Intrinsics.checkNotNullParameter(varargs, "<unused var>");
        LuaTable luaTable = new LuaTable();
        luaTable.set("FilterDescendantsInstances", new LuaTable());
        luaTable.set("FilterType", LuaTypes.INSTANCE.wrap(new EnumItemV("RaycastFilterType", "Exclude", 0)));
        luaTable.set("MaxParts", 0);
        luaTable.set("CollisionGroup", "Default");
        luaTable.set("RespectCanCollide", LuaValue.FALSE);
        return luaTable;
    }

    static final Varargs install$lambda$54$lambda$53(Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        LuaTable luaTable = new LuaTable();
        luaTable.set("InitialDockState", a.arg(1));
        luaTable.set("InitialEnabled", LuaValue.valueOf(a.arg(2).optboolean(false)));
        luaTable.set("FloatingXSize", LuaValue.valueOf(a.arg(4).optint(300)));
        luaTable.set("FloatingYSize", LuaValue.valueOf(a.arg(5).optint(300)));
        luaTable.set("MinWidth", LuaValue.valueOf(a.arg(6).optint(150)));
        luaTable.set("MinHeight", LuaValue.valueOf(a.arg(7).optint(150)));
        return luaTable;
    }

    static final Varargs install$lambda$55(Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        double x = a.arg(1).checkdouble();
        double mn = a.arg(2).checkdouble();
        double mx = a.arg(3).checkdouble();
        if (mn > mx) {
            throw new LuaError("max must be greater than or equal to min");
        }
        double clamped = Double.isNaN(x) ? Double.NaN : (x < mn ? mn : (x > mx ? mx : x));
        LuaNumber luaNumberValueOf = LuaValue.valueOf(clamped);
        Intrinsics.checkNotNullExpressionValue(luaNumberValueOf, "valueOf(...)");
        return luaNumberValueOf;
    }

    static final LuaValue install$lambda$56(LuaValue a) {
        int i;
        Intrinsics.checkNotNullParameter(a, "a");
        double x = a.checkdouble();
        if (x > 0.0d) {
            i = 1;
        } else {
            i = x < 0.0d ? -1 : 0;
        }
        LuaInteger luaIntegerValueOf = LuaValue.valueOf(i);
        Intrinsics.checkNotNullExpressionValue(luaIntegerValueOf, "valueOf(...)");
        return luaIntegerValueOf;
    }

    static final LuaValue install$lambda$57(LuaValue a) {
        Intrinsics.checkNotNullParameter(a, "a");
        double x = a.checkdouble();
        LuaNumber luaNumberValueOf = LuaValue.valueOf(x >= 0.0d ? Math.floor(0.5d + x) : -Math.floor((-x) + 0.5d));
        Intrinsics.checkNotNullExpressionValue(luaNumberValueOf, "valueOf(...)");
        return luaNumberValueOf;
    }

    static final Varargs install$lambda$58(Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        LuaNumber luaNumberValueOf = LuaValue.valueOf(Noise.INSTANCE.perlin(a.arg(1).optdouble(0.0d), a.arg(2).optdouble(0.0d), a.arg(3).optdouble(0.0d)));
        Intrinsics.checkNotNullExpressionValue(luaNumberValueOf, "valueOf(...)");
        return luaNumberValueOf;
    }

    static final Varargs install$lambda$59(Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        double x = a.arg(1).checkdouble();
        double y = a.arg(2).checkdouble();
        double t = a.arg(3).checkdouble();
        double res = (t == 1.0d) ? y : (x + ((y - x) * t));
        return LuaValue.valueOf(res);
    }

    static final Varargs install$lambda$60(Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        double x = a.arg(1).checkdouble();
        double i0 = a.arg(2).checkdouble();
        double i1 = a.arg(3).checkdouble();
        double o0 = a.arg(4).checkdouble();
        double o1 = a.arg(5).checkdouble();
        LuaNumber luaNumberValueOf = LuaValue.valueOf((((x - i0) * (o1 - o0)) / (i1 - i0)) + o0);
        Intrinsics.checkNotNullExpressionValue(luaNumberValueOf, "valueOf(...)");
        return luaNumberValueOf;
    }

    static final Varargs install$lambda$63(Varargs a) {
        Collection parts;
        Intrinsics.checkNotNullParameter(a, "a");
        CharSequence s = a.arg(1).checkjstring();
        String sep = a.arg(2).optjstring(",");
        LuaTable t = new LuaTable();
        Intrinsics.checkNotNull(sep);
        boolean z = sep.length() == 0;
        Intrinsics.checkNotNull(s);
        if (!z) {
            parts = StringsKt.split(s, new String[]{sep}, false, 0);
        } else {
            CharSequence charSequence = s;
            Collection arrayList = new ArrayList(charSequence.length());
            for (int i = 0; i < charSequence.length(); i++) {
                arrayList.add(String.valueOf(charSequence.charAt(i)));
            }
            parts = (List) arrayList;
        }
        int idx = 1;
        Iterator it = parts.iterator();
        while (it.hasNext()) {
            t.set(idx++, LuaValue.valueOf((String) it.next()));
        }
        return t;
    }

    static final Varargs install$lambda$64(Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        LuaTable t = a.arg(1).checktable();
        LuaValue v = a.arg(2);
        int init = a.arg(3).optint(1);
        int length = t.length();
        for (int i = Math.max(init, 1); i <= length; i++) {
            if (t.get(i).eq_b(v)) {
                return LuaValue.valueOf(i);
            }
        }
        return LuaValue.NIL;
    }

    static final LuaValue install$lambda$66(LuaValue a) {
        Intrinsics.checkNotNullParameter(a, "a");
        LuaTable t = a.checktable();
        checkNotFrozen(t);
        ArrayList keys = new ArrayList();
        LuaValue k = LuaValue.NIL;
        while (true) {
            Varargs n = t.next(k);
            k = n.arg1();
            if (k.isnil()) {
                break;
            }
            keys.add(k);
        }
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            t.set((LuaValue) it.next(), LuaValue.NIL);
        }
        LuaValue NONE = LuaValue.NONE;
        Intrinsics.checkNotNullExpressionValue(NONE, "NONE");
        return NONE;
    }

    static final Varargs install$lambda$67(Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        LuaTable t = new LuaTable();
        int n = a.arg(1).checkint();
        if (n < 0 || n > 10000000) {
            throw new LuaError("invalid argument #1 to 'create' (size out of range)");
        }
        LuaValue v = a.arg(2);
        if (!v.isnil()) {
            int i = 1;
            if (1 <= n) {
                while (true) {
                    t.set(i, v);
                    if (i == n) {
                        break;
                    }
                    i++;
                }
            }
        }
        return t;
    }

    static final LuaValue install$lambda$68(LuaValue it) {
        Intrinsics.checkNotNullParameter(it, "it");
        LuaTable t = it.checktable();
        if (FROZEN_TABLES.contains(t)) {
            return t;
        }
        LuaTable newMt = new LuaTable();
        LuaValue oldMt = t.getmetatable();
        if (oldMt != null && !oldMt.isnil()) {
            FROZEN_ORIG_MT.put(t, oldMt);
        }
        if (oldMt != null && oldMt.istable()) {
            LuaTable omt = (LuaTable) oldMt;
            LuaValue k = LuaValue.NIL;
            while (true) {
                Varargs n = omt.next(k);
                k = n.arg1();
                if (k.isnil()) {
                    break;
                }
                newMt.set(k, n.arg(2));
            }
        }
        newMt.set(LuaValue.NEWINDEX, LuaTypesKt.fnv(new Function1() {
            @Override
            public final Object invoke(Object obj) {
                throw new LuaError("attempt to modify a readonly table");
            }
        }));
        t.setmetatable(newMt);
        FROZEN_TABLES.add(t);
        return t;
    }

    static final LuaValue install$lambda$69(LuaValue it) {
        Intrinsics.checkNotNullParameter(it, "it");
        it.checktable();
        return LuaValue.valueOf(isFrozenTable(it));
    }

    static final LuaValue install$lambda$70(LuaValue a) {
        Intrinsics.checkNotNullParameter(a, "a");
        LuaTable src = a.checktable();
        LuaTable t = new LuaTable();
        LuaValue k = LuaValue.NIL;
        while (true) {
            Varargs n = src.next(k);
            k = n.arg1();
            if (k.isnil()) {
                break;
            }
            t.set(k, n.arg(2));
        }
        LuaValue mt = isFrozenTable(src) ? FROZEN_ORIG_MT.get(src) : src.getmetatable();
        if (mt != null && !mt.isnil()) {
            t.setmetatable(mt);
        }
        return t;
    }

    static final Varargs install$lambda$71(Varargs a) {
        LuaTable dst;
        Intrinsics.checkNotNullParameter(a, "a");
        LuaTable src = a.arg(1).checktable();
        int f = a.arg(2).checkint();
        int e = a.arg(3).checkint();
        int tpos = a.arg(4).checkint();
        dst = a.arg(5).isnil() ? src : a.arg(5).checktable();
        checkNotFrozen(dst);
        if (tpos <= f) {
            int i = f;
            if (i <= e) {
                while (true) {
                    dst.set((tpos + i) - f, src.get(i));
                    if (i == e) {
                        break;
                    }
                    i++;
                }
            }
        } else {
            int i2 = e;
            if (f <= i2) {
                while (true) {
                    dst.set((tpos + i2) - f, src.get(i2));
                    if (i2 == f) {
                        break;
                    }
                    i2--;
                }
            }
        }
        Intrinsics.checkNotNull(dst);
        return dst;
    }

    static final LuaValue install$lambda$72(LuaValue a) {
        Intrinsics.checkNotNullParameter(a, "a");
        return LuaValue.valueOf(a.checktable().length());
    }

    static final Varargs install$lambda$73(Varargs a) {
        Intrinsics.checkNotNullParameter(a, "a");
        StringBuilder sb = new StringBuilder();
        int i = 1;
        int iNarg = a.narg();
        if (1 <= iNarg) {
            while (true) {
                int cp = a.arg(i).checkint();
                if (cp < 0 || cp > 0x10FFFF) {
                    throw new LuaError("value out of range");
                }
                sb.appendCodePoint(cp);
                if (i == iNarg) {
                    break;
                }
                i++;
            }
        }
        LuaString luaStringValueOf = LuaValue.valueOf(sb.toString());
        Intrinsics.checkNotNullExpressionValue(luaStringValueOf, "valueOf(...)");
        return luaStringValueOf;
    }

    static final LuaValue install$lambda$74(LuaValue a) {
        Intrinsics.checkNotNullParameter(a, "a");
        byte[] m_bytes = a.checkstring().m_bytes;
        Intrinsics.checkNotNullExpressionValue(m_bytes, "m_bytes");
        String s = new String(m_bytes, a.checkstring().m_offset, a.checkstring().m_length, Charsets.UTF_8);
        LuaInteger luaIntegerValueOf = LuaValue.valueOf(s.codePointCount(0, s.length()));
        Intrinsics.checkNotNullExpressionValue(luaIntegerValueOf, "valueOf(...)");
        return luaIntegerValueOf;
    }
}
