package com.goydevv.studrblx.ui;

import com.goydevv.studrblx.core.Studio;
import com.goydevv.studrblx.model.ClassDb;
import com.goydevv.studrblx.model.DataModel;
import com.goydevv.studrblx.model.Enums;
import com.goydevv.studrblx.model.Instance;
import com.goydevv.studrblx.model.PropNames;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kotlin.Pair;
import kotlin.Triple;
import kotlin.TuplesKt;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.enums.EnumEntries;
import kotlin.enums.EnumEntriesKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;

public final class Autocomplete {
    public static final Autocomplete INSTANCE = new Autocomplete();

    private static final Pattern RX_CALL_STR_ARG = Pattern.compile("^[\"']([^\"']*)[\"']");
    private static final Pattern RX_BRACKET_STR = Pattern.compile("^[\"']([^\"']*)[\"']$");
    private static final Pattern RX_CTOR_NEW = Pattern.compile("^(\\w+)\\.new\\s*\\(");
    private static final Pattern RX_INSTANCE_NEW = Pattern.compile("Instance\\.new\\s*\\(\\s*[\"'](\\w+)[\"']");
    private static final Pattern RX_DATA_FACTORY = Pattern.compile("^(\\w+)\\.(fromRGB|fromHSV|fromHex|Angles|lookAt|lookAlong|fromEulerAnglesXYZ|fromEulerAnglesYXZ|fromOrientation|fromAxisAngle|fromMatrix|fromScale|fromOffset|fromUnixTimestamp|fromUnixTimestampMillis|fromIsoDate|fromEnum|fromName|fromId|fromExisting)\\b");
    private static final Pattern RX_DECL_PREFIX = Pattern.compile("(\\blocal|\\bfunction|\\bfor)\\s+$");
    private static final Pattern RX_LOCAL_DECL = Pattern.compile("\\blocal\\s+(?:function\\s+)?([A-Za-z_][\\w]*(?:\\s*,\\s*[A-Za-z_][\\w]*)*)");
    private static final Pattern RX_FUNC_DECL = Pattern.compile("\\bfunction\\s+([A-Za-z_][\\w.:]*)");
    private static final Pattern RX_FUNC_PARAMS = Pattern.compile("\\bfunction\\s*[\\w.:]*\\s*\\(([^)]*)\\)");
    private static final Pattern RX_FOR_VARS = Pattern.compile("\\bfor\\s+([\\w\\s,]+?)\\s+(?:in|=)");

    private static final List<String> keywords = CollectionsKt.listOf(new String[]{
            "and", "break", "do", "else", "elseif", "end", "false", "for",
            "function", "if", "in", "local", "nil", "not", "or", "repeat",
            "return", "then", "true", "until", "while", "continue", "export", "type"
    });

    private static final List<String> globals = CollectionsKt.listOf(new String[]{
            "game", "workspace", "script", "plugin", "shared", "_G", "_VERSION",
            "print", "warn", "error", "assert", "pcall", "xpcall", "typeof", "type",
            "tostring", "tonumber", "pairs", "ipairs", "next", "select", "unpack",
            "require", "loadstring", "setmetatable", "getmetatable", "rawget",
            "rawset", "rawequal", "rawlen", "wait", "spawn", "delay", "tick",
            "time", "elapsedTime", "collectgarbage", "gcinfo", "newproxy",
            "version", "settings", "UserSettings"
    });

    private static final Map<String, List<String>> libs = MapsKt.mapOf(
            TuplesKt.to("math", CollectionsKt.listOf(new String[]{"abs", "acos", "asin", "atan", "atan2", "ceil", "clamp", "cos", "cosh", "deg", "exp", "floor", "fmod", "frexp", "huge", "ldexp", "log", "log10", "max", "min", "modf", "noise", "pi", "pow", "rad", "random", "randomseed", "round", "sign", "sin", "sinh", "sqrt", "tan", "tanh", "lerp", "map"})),
            TuplesKt.to("string", CollectionsKt.listOf(new String[]{"byte", "char", "find", "format", "gmatch", "gsub", "len", "lower", "match", "rep", "reverse", "split", "sub", "upper", "pack", "packsize", "unpack"})),
            TuplesKt.to("table", CollectionsKt.listOf(new String[]{"clear", "clone", "concat", "create", "find", "freeze", "insert", "isfrozen", "maxn", "move", "pack", "remove", "sort", "unpack"})),
            TuplesKt.to("task", CollectionsKt.listOf(new String[]{"wait", "spawn", "defer", "delay", "cancel", "synchronize", "desynchronize"})),
            TuplesKt.to("coroutine", CollectionsKt.listOf(new String[]{"create", "resume", "running", "status", "wrap", "yield", "isyieldable", "close"})),
            TuplesKt.to("os", CollectionsKt.listOf(new String[]{"time", "clock", "date", "difftime"})),
            TuplesKt.to("bit32", CollectionsKt.listOf(new String[]{"band", "bor", "bxor", "bnot", "lshift", "rshift", "arshift", "btest", "extract", "replace", "lrotate", "rrotate", "countlz", "countrz", "byteswap"})),
            TuplesKt.to("utf8", CollectionsKt.listOf(new String[]{"char", "charpattern", "codepoint", "codes", "len", "offset", "graphemes", "nfcnormalize", "nfdnormalize"})),
            TuplesKt.to("debug", CollectionsKt.listOf(new String[]{"traceback", "info", "profilebegin", "profileend"})),
            TuplesKt.to("buffer", CollectionsKt.listOf(new String[]{"create", "fromstring", "tostring", "len", "readi8", "readu8", "readi16", "readu16", "readi32", "readu32", "readf32", "readf64", "writei8", "writeu8", "writei16", "writeu16", "writei32", "writeu32", "writef32", "writef64", "readstring", "writestring", "readbits", "writebits", "copy", "fill"})),
            TuplesKt.to("vector", CollectionsKt.listOf(new String[]{"create", "zero", "one", "magnitude", "normalize", "cross", "dot", "angle", "floor", "ceil", "abs", "sign", "clamp", "max", "min", "lerp"}))
    );

    private static final Map<String, List<String>> dataStatics = MapsKt.mapOf(
            TuplesKt.to("Vector3", CollectionsKt.listOf(new String[]{"new", "zero", "one", "xAxis", "yAxis", "zAxis", "FromNormalId", "FromAxis"})),
            TuplesKt.to("Vector2", CollectionsKt.listOf(new String[]{"new", "zero", "one", "xAxis", "yAxis"})),
            TuplesKt.to("CFrame", CollectionsKt.listOf(new String[]{"new", "Angles", "fromEulerAnglesXYZ", "fromEulerAnglesYXZ", "fromOrientation", "fromAxisAngle", "lookAt", "fromMatrix", "identity", "lookAlong"})),
            TuplesKt.to("Color3", CollectionsKt.listOf(new String[]{"new", "fromRGB", "fromHSV", "fromHex"})),
            TuplesKt.to("UDim2", CollectionsKt.listOf(new String[]{"new", "fromScale", "fromOffset"})),
            TuplesKt.to("UDim", CollectionsKt.listOf("new")),
            TuplesKt.to("BrickColor", CollectionsKt.listOf(new String[]{"new", "random", "palette", "White", "Black", "Red", "Blue", "Green", "Yellow", "Gray", "DarkGray"})),
            TuplesKt.to("Instance", CollectionsKt.listOf(new String[]{"new", "fromExisting"})),
            TuplesKt.to("TweenInfo", CollectionsKt.listOf("new")),
            TuplesKt.to("Ray", CollectionsKt.listOf("new")),
            TuplesKt.to("NumberRange", CollectionsKt.listOf("new")),
            TuplesKt.to("NumberSequence", CollectionsKt.listOf("new")),
            TuplesKt.to("ColorSequence", CollectionsKt.listOf("new")),
            TuplesKt.to("NumberSequenceKeypoint", CollectionsKt.listOf("new")),
            TuplesKt.to("ColorSequenceKeypoint", CollectionsKt.listOf("new")),
            TuplesKt.to("Random", CollectionsKt.listOf("new")),
            TuplesKt.to("Rect", CollectionsKt.listOf("new")),
            TuplesKt.to("Region3", CollectionsKt.listOf("new")),
            TuplesKt.to("RaycastParams", CollectionsKt.listOf("new")),
            TuplesKt.to("OverlapParams", CollectionsKt.listOf("new")),
            TuplesKt.to("DateTime", CollectionsKt.listOf(new String[]{"now", "fromUnixTimestamp", "fromUnixTimestampMillis", "fromIsoDate", "fromLocalTime", "fromUniversalTime"})),
            TuplesKt.to("Font", CollectionsKt.listOf(new String[]{"new", "fromEnum", "fromName", "fromId"})),
            TuplesKt.to("PhysicalProperties", CollectionsKt.listOf("new")),
            TuplesKt.to("Axes", CollectionsKt.listOf("new")),
            TuplesKt.to("Faces", CollectionsKt.listOf("new"))
    );

    private static final Map<String, Pair<List<String>, List<String>>> dataMembers = MapsKt.mapOf(
            TuplesKt.to("Vector3", TuplesKt.to(CollectionsKt.listOf(new String[]{"X", "Y", "Z", "Magnitude", "Unit"}), CollectionsKt.listOf(new String[]{"Dot", "Cross", "Lerp", "FuzzyEq", "Abs", "Ceil", "Floor", "Sign", "Angle", "Max", "Min"}))),
            TuplesKt.to("Vector2", TuplesKt.to(CollectionsKt.listOf(new String[]{"X", "Y", "Magnitude", "Unit"}), CollectionsKt.listOf(new String[]{"Dot", "Cross", "Lerp", "FuzzyEq", "Abs", "Ceil", "Floor", "Sign", "Angle", "Max", "Min"}))),
            TuplesKt.to("CFrame", TuplesKt.to(CollectionsKt.listOf(new String[]{"Position", "LookVector", "RightVector", "UpVector", "XVector", "YVector", "ZVector", "X", "Y", "Z", "Rotation"}), CollectionsKt.listOf(new String[]{"Inverse", "Lerp", "Orthonormalize", "ToWorldSpace", "ToObjectSpace", "PointToWorldSpace", "PointToObjectSpace", "VectorToWorldSpace", "VectorToObjectSpace", "GetComponents", "ToEulerAnglesXYZ", "ToEulerAnglesYXZ", "ToOrientation", "ToAxisAngle", "FuzzyEq"}))),
            TuplesKt.to("Color3", TuplesKt.to(CollectionsKt.listOf(new String[]{"R", "G", "B"}), CollectionsKt.listOf(new String[]{"Lerp", "ToHSV", "ToHex"}))),
            TuplesKt.to("UDim2", TuplesKt.to(CollectionsKt.listOf(new String[]{"X", "Y", "Width", "Height"}), CollectionsKt.listOf("Lerp"))),
            TuplesKt.to("UDim", TuplesKt.to(CollectionsKt.listOf(new String[]{"Scale", "Offset"}), CollectionsKt.emptyList())),
            TuplesKt.to("BrickColor", TuplesKt.to(CollectionsKt.listOf(new String[]{"Name", "Number", "Color", "r", "g", "b"}), CollectionsKt.emptyList())),
            TuplesKt.to("Random", TuplesKt.to(CollectionsKt.emptyList(), CollectionsKt.listOf(new String[]{"NextNumber", "NextInteger", "NextUnitVector", "Shuffle", "Clone"}))),
            TuplesKt.to("RaycastParams", TuplesKt.to(CollectionsKt.listOf(new String[]{"FilterDescendantsInstances", "FilterType", "IgnoreWater", "CollisionGroup", "RespectCanCollide", "BruteForceAllSlow"}), CollectionsKt.listOf("AddToFilter"))),
            TuplesKt.to("OverlapParams", TuplesKt.to(CollectionsKt.listOf(new String[]{"FilterDescendantsInstances", "FilterType", "MaxParts", "CollisionGroup", "RespectCanCollide"}), CollectionsKt.listOf("AddToFilter"))),
            TuplesKt.to("Ray", TuplesKt.to(CollectionsKt.listOf(new String[]{"Origin", "Direction", "Unit"}), CollectionsKt.listOf(new String[]{"ClosestPoint", "Distance"}))),
            TuplesKt.to("TweenInfo", TuplesKt.to(CollectionsKt.listOf(new String[]{"Time", "EasingStyle", "EasingDirection", "RepeatCount", "Reverses", "DelayTime"}), CollectionsKt.emptyList())),
            TuplesKt.to("RaycastResult", TuplesKt.to(CollectionsKt.listOf(new String[]{"Instance", "Position", "Normal", "Material", "Distance"}), CollectionsKt.emptyList())),
            TuplesKt.to("Tween", TuplesKt.to(CollectionsKt.listOf(new String[]{"PlaybackState", "TweenInfo", "Instance"}), CollectionsKt.listOf(new String[]{"Play", "Pause", "Cancel"}))),
            TuplesKt.to("RBXScriptSignal", TuplesKt.to(CollectionsKt.emptyList(), CollectionsKt.listOf(new String[]{"Connect", "Once", "Wait", "ConnectParallel"}))),
            TuplesKt.to("RBXScriptConnection", TuplesKt.to(CollectionsKt.listOf("Connected"), CollectionsKt.listOf("Disconnect"))),
            TuplesKt.to("AnimationTrack", TuplesKt.to(CollectionsKt.listOf(new String[]{"IsPlaying", "Length", "Looped", "Priority", "Speed", "TimePosition", "WeightCurrent", "Animation"}), CollectionsKt.listOf(new String[]{"Play", "Stop", "AdjustSpeed", "AdjustWeight", "GetMarkerReachedSignal", "GetTimeOfKeyframe"}))),
            TuplesKt.to("DataStore", TuplesKt.to(CollectionsKt.emptyList(), CollectionsKt.listOf(new String[]{"GetAsync", "SetAsync", "UpdateAsync", "RemoveAsync", "IncrementAsync", "GetSortedAsync", "ListKeysAsync"}))),
            TuplesKt.to("Path", TuplesKt.to(CollectionsKt.listOf("Status"), CollectionsKt.listOf(new String[]{"ComputeAsync", "GetWaypoints", "CheckOcclusionAsync"}))),
            TuplesKt.to("Mouse", TuplesKt.to(CollectionsKt.listOf(new String[]{"Hit", "Target", "X", "Y", "Origin", "UnitRay", "Icon", "TargetFilter", "ViewSizeX", "ViewSizeY"}), CollectionsKt.emptyList())),
            TuplesKt.to("InputObject", TuplesKt.to(CollectionsKt.listOf(new String[]{"UserInputType", "UserInputState", "KeyCode", "Position", "Delta"}), CollectionsKt.listOf("IsModifierKeyDown")))
    );

    private static final Map<String, Members> classMembers = MapsKt.mapOf(
            TuplesKt.to("Instance", INSTANCE.m("Name Parent ClassName Archivable", "FindFirstChild WaitForChild GetChildren GetDescendants Destroy Clone IsA FindFirstChildOfClass FindFirstChildWhichIsA FindFirstAncestor FindFirstAncestorOfClass FindFirstAncestorWhichIsA FindFirstDescendant GetAttribute SetAttribute GetAttributes GetAttributeChangedSignal GetPropertyChangedSignal ClearAllChildren GetFullName IsDescendantOf IsAncestorOf AddTag RemoveTag HasTag GetTags", "Changed ChildAdded ChildRemoved DescendantAdded DescendantRemoving AncestryChanged Destroying AttributeChanged")),
            TuplesKt.to("PVInstance", m$default(INSTANCE, "", "GetPivot PivotTo", null, 4, null)),
            TuplesKt.to("BasePart", INSTANCE.m("Position CFrame Size Orientation Rotation Anchored CanCollide CanTouch CanQuery Transparency Color BrickColor Material MaterialVariant Reflectance Locked Massless CastShadow CollisionGroup AssemblyLinearVelocity AssemblyAngularVelocity AssemblyMass AssemblyRootPart TopSurface BottomSurface FrontSurface BackSurface LeftSurface RightSurface", "GetMass ApplyImpulse ApplyAngularImpulse GetTouchingParts GetConnectedParts GetJoints BreakJoints SetNetworkOwner GetNetworkOwner GetRootPart GetVelocityAtPosition CanSetNetworkOwnership", "Touched TouchEnded")),
            TuplesKt.to("Part", m$default(INSTANCE, "Shape", null, null, 6, null)),
            TuplesKt.to("MeshPart", m$default(INSTANCE, "MeshId TextureID DoubleSided", null, null, 6, null)),
            TuplesKt.to("SpawnLocation", m$default(INSTANCE, "Enabled Neutral TeamColor Duration AllowTeamChangeOnTouch", null, null, 6, null)),
            TuplesKt.to("Seat", m$default(INSTANCE, "Disabled Occupant", "Sit", null, 4, null)),
            TuplesKt.to("VehicleSeat", m$default(INSTANCE, "Disabled Occupant MaxSpeed Torque Throttle Steer TurnSpeed", "Sit", null, 4, null)),
            TuplesKt.to("Model", m$default(INSTANCE, "PrimaryPart WorldPivot", "MoveTo GetBoundingBox GetExtentsSize ScaleTo GetScale TranslateBy SetPrimaryPartCFrame GetPrimaryPartCFrame", null, 4, null)),
            TuplesKt.to("Workspace", m$default(INSTANCE, "Gravity CurrentCamera FallenPartsDestroyHeight DistributedGameTime Terrain StreamingEnabled", "Raycast Blockcast Spherecast Shapecast GetPartBoundsInBox GetPartBoundsInRadius GetPartsInPart GetServerTimeNow GetRealPhysicsFPS FindPartOnRay", null, 4, null)),
            TuplesKt.to("Humanoid", INSTANCE.m("Health MaxHealth WalkSpeed JumpPower JumpHeight UseJumpPower HipHeight AutoRotate Sit Jump MoveDirection RootPart SeatPart DisplayName RigType FloorMaterial PlatformStand WalkToPoint NameDisplayDistance HealthDisplayType BreakJointsOnDeath", "TakeDamage MoveTo Move ChangeState GetState LoadAnimation EquipTool UnequipTools GetPlayingAnimationTracks SetStateEnabled GetStateEnabled ApplyDescription GetAppliedDescription AddAccessory GetAccessories", "Died HealthChanged Running Jumping StateChanged MoveToFinished FreeFalling Seated Climbing Touched")),
            TuplesKt.to("Players", INSTANCE.m("LocalPlayer MaxPlayers RespawnTime CharacterAutoLoads", "GetPlayers GetPlayerFromCharacter GetPlayerByUserId GetUserIdFromNameAsync GetNameFromUserIdAsync GetUserThumbnailAsync GetHumanoidDescriptionFromUserId", "PlayerAdded PlayerRemoving")),
            TuplesKt.to("Player", INSTANCE.m("Character UserId DisplayName Team TeamColor Neutral AccountAge MembershipType CameraMaxZoomDistance CameraMinZoomDistance CameraMode RespawnLocation PlayerGui Backpack PlayerScripts", "Kick LoadCharacter GetMouse IsInGroup GetRankInGroup GetRoleInGroup DistanceFromCharacter GetNetworkPing", "CharacterAdded CharacterRemoving CharacterAppearanceLoaded Chatted Idled")),
            TuplesKt.to("GuiObject", INSTANCE.m("Position Size AnchorPoint BackgroundColor3 BackgroundTransparency BorderColor3 BorderSizePixel Visible ZIndex LayoutOrder Rotation AbsolutePosition AbsoluteSize ClipsDescendants Active AutomaticSize SizeConstraint Selectable Interactable", "TweenPosition TweenSize TweenSizeAndPosition", "MouseEnter MouseLeave InputBegan InputEnded InputChanged")),
            TuplesKt.to("GuiButton", INSTANCE.m("AutoButtonColor Modal Selected", "", "MouseButton1Click MouseButton1Down MouseButton1Up MouseButton2Click Activated")),
            TuplesKt.to("TextLabel", m$default(INSTANCE, "Text TextColor3 TextSize TextScaled TextWrapped Font FontFace RichText TextXAlignment TextYAlignment TextTransparency TextStrokeColor3 TextStrokeTransparency TextTruncate LineHeight MaxVisibleGraphemes TextBounds", null, null, 6, null)),
            TuplesKt.to("TextButton", m$default(INSTANCE, "Text TextColor3 TextSize TextScaled TextWrapped Font FontFace RichText TextXAlignment TextYAlignment TextTransparency TextStrokeColor3 TextStrokeTransparency", null, null, 6, null)),
            TuplesKt.to("TextBox", INSTANCE.m("Text PlaceholderText PlaceholderColor3 ClearTextOnFocus MultiLine TextEditable TextColor3 TextSize TextScaled Font FontFace", "CaptureFocus ReleaseFocus IsFocused", "FocusLost Focused ReturnPressedFromOnScreenKeyboard")),
            TuplesKt.to("ImageLabel", m$default(INSTANCE, "Image ImageColor3 ImageTransparency ScaleType SliceCenter TileSize ImageRectOffset ImageRectSize ResampleMode", null, null, 6, null)),
            TuplesKt.to("ImageButton", m$default(INSTANCE, "Image ImageColor3 ImageTransparency HoverImage PressedImage ScaleType SliceCenter", null, null, 6, null)),
            TuplesKt.to("ScrollingFrame", m$default(INSTANCE, "CanvasSize CanvasPosition AutomaticCanvasSize ScrollBarThickness ScrollingEnabled ScrollingDirection ScrollBarImageColor3 ElasticBehavior", null, null, 6, null)),
            TuplesKt.to("ScreenGui", m$default(INSTANCE, "Enabled ResetOnSpawn IgnoreGuiInset DisplayOrder ZIndexBehavior ScreenInsets", null, null, 6, null)),
            TuplesKt.to("BillboardGui", m$default(INSTANCE, "Adornee Size StudsOffset StudsOffsetWorldSpace MaxDistance AlwaysOnTop Enabled LightInfluence", null, null, 6, null)),
            TuplesKt.to("SurfaceGui", m$default(INSTANCE, "Adornee Face CanvasSize PixelsPerStud SizingMode AlwaysOnTop LightInfluence Enabled", null, null, 6, null)),
            TuplesKt.to("UICorner", m$default(INSTANCE, "CornerRadius", null, null, 6, null)),
            TuplesKt.to("UIStroke", m$default(INSTANCE, "Color Thickness Transparency ApplyStrokeMode LineJoinMode Enabled", null, null, 6, null)),
            TuplesKt.to("UIListLayout", m$default(INSTANCE, "FillDirection HorizontalAlignment VerticalAlignment Padding SortOrder Wraps", null, null, 6, null)),
            TuplesKt.to("UIGridLayout", m$default(INSTANCE, "CellSize CellPadding FillDirection SortOrder FillDirectionMaxCells", null, null, 6, null)),
            TuplesKt.to("UIPadding", m$default(INSTANCE, "PaddingTop PaddingBottom PaddingLeft PaddingRight", null, null, 6, null)),
            TuplesKt.to("UIGradient", m$default(INSTANCE, "Color Transparency Rotation Offset Enabled", null, null, 6, null)),
            TuplesKt.to("UIScale", m$default(INSTANCE, "Scale", null, null, 6, null)),
            TuplesKt.to("RunService", INSTANCE.m("", "IsStudio IsServer IsClient IsRunning IsEdit IsRunMode BindToRenderStep UnbindFromRenderStep", "Heartbeat RenderStepped Stepped PreSimulation PostSimulation PreRender PreAnimation")),
            TuplesKt.to("TweenService", m$default(INSTANCE, "", "Create GetValue", null, 4, null)),
            TuplesKt.to("UserInputService", INSTANCE.m("TouchEnabled KeyboardEnabled MouseEnabled GamepadEnabled MouseBehavior MouseIconEnabled ModalEnabled", "IsKeyDown IsMouseButtonPressed GetMouseLocation GetFocusedTextBox GetKeysPressed GetLastInputType GetConnectedGamepads", "InputBegan InputEnded InputChanged TouchTap TouchStarted TouchEnded TouchMoved JumpRequest LastInputTypeChanged TouchTapInWorld")),
            TuplesKt.to("ContextActionService", m$default(INSTANCE, "", "BindAction BindActionAtPriority UnbindAction UnbindAllActions SetTitle SetImage SetPosition SetDescription GetButton", null, 4, null)),
            TuplesKt.to("DataStoreService", m$default(INSTANCE, "", "GetDataStore GetOrderedDataStore GetGlobalDataStore", null, 4, null)),
            TuplesKt.to("HttpService", m$default(INSTANCE, "HttpEnabled", "JSONEncode JSONDecode GenerateGUID GetAsync PostAsync RequestAsync UrlEncode", null, 4, null)),
            TuplesKt.to("MarketplaceService", INSTANCE.m("ProcessReceipt", "PromptPurchase PromptGamePassPurchase PromptProductPurchase UserOwnsGamePassAsync PlayerOwnsAsset GetProductInfo", "PromptGamePassPurchaseFinished PromptPurchaseFinished PromptProductPurchaseFinished")),
            TuplesKt.to("CollectionService", m$default(INSTANCE, "", "GetTagged AddTag RemoveTag HasTag GetTags GetAllTags GetInstanceAddedSignal GetInstanceRemovedSignal", null, 4, null)),
            TuplesKt.to("Debris", m$default(INSTANCE, "", "AddItem", null, 4, null)),
            TuplesKt.to("PathfindingService", m$default(INSTANCE, "", "CreatePath FindPathAsync", null, 4, null)),
            TuplesKt.to("StarterGui", m$default(INSTANCE, "ShowDevelopmentGui", "SetCore GetCore SetCoreGuiEnabled GetCoreGuiEnabled", null, 4, null)),
            TuplesKt.to("Lighting", m$default(INSTANCE, "ClockTime TimeOfDay Brightness Ambient OutdoorAmbient FogEnd FogStart FogColor GlobalShadows ExposureCompensation GeographicLatitude Technology", "GetMinutesAfterMidnight SetMinutesAfterMidnight GetSunDirection GetMoonDirection", null, 4, null)),
            TuplesKt.to("Camera", m$default(INSTANCE, "CFrame Focus FieldOfView CameraType CameraSubject ViewportSize NearPlaneZ", "WorldToScreenPoint WorldToViewportPoint ScreenPointToRay ViewportPointToRay GetPartsObscuringTarget", null, 4, null)),
            TuplesKt.to("Tool", INSTANCE.m("Handle RequiresHandle CanBeDropped ToolTip Grip Enabled ManualActivationOnly TextureId", "Activate Deactivate", "Activated Deactivated Equipped Unequipped")),
            TuplesKt.to("ValueBase", INSTANCE.m("Value", "", "Changed")),
            TuplesKt.to("Sound", INSTANCE.m("SoundId Volume Looped Playing PlaybackSpeed TimePosition TimeLength IsPlaying RollOffMaxDistance SoundGroup", "Play Stop Pause Resume", "Ended Played Paused Resumed Loaded DidLoop")),
            TuplesKt.to("ClickDetector", INSTANCE.m("MaxActivationDistance CursorIcon", "", "MouseClick RightMouseClick MouseHoverEnter MouseHoverLeave")),
            TuplesKt.to("ProximityPrompt", INSTANCE.m("ActionText ObjectText HoldDuration MaxActivationDistance KeyboardKeyCode GamepadKeyCode Enabled RequiresLineOfSight Style", "InputHoldBegin InputHoldEnd", "Triggered TriggerEnded PromptShown PromptHidden PromptButtonHoldBegan PromptButtonHoldEnded")),
            TuplesKt.to("RemoteEvent", INSTANCE.m("", "FireServer FireClient FireAllClients", "OnServerEvent OnClientEvent")),
            TuplesKt.to("UnreliableRemoteEvent", INSTANCE.m("", "FireServer FireClient FireAllClients", "OnServerEvent OnClientEvent")),
            TuplesKt.to("RemoteFunction", m$default(INSTANCE, "OnServerInvoke OnClientInvoke", "InvokeServer InvokeClient", null, 4, null)),
            TuplesKt.to("BindableEvent", INSTANCE.m("", "Fire", "Event")),
            TuplesKt.to("BindableFunction", m$default(INSTANCE, "OnInvoke", "Invoke", null, 4, null)),
            TuplesKt.to("Animator", m$default(INSTANCE, "", "LoadAnimation GetPlayingAnimationTracks", null, 4, null)),
            TuplesKt.to("Animation", m$default(INSTANCE, "AnimationId", null, null, 6, null)),
            TuplesKt.to("Teams", m$default(INSTANCE, "", "GetTeams", null, 4, null)),
            TuplesKt.to("Team", INSTANCE.m("TeamColor AutoAssignable", "GetPlayers", "PlayerAdded PlayerRemoved")),
            TuplesKt.to("TextChatService", INSTANCE.m("ChatVersion", "", "MessageReceived OnIncomingMessage SendingMessage")),
            TuplesKt.to("TextChannel", INSTANCE.m("", "SendAsync DisplaySystemMessage", "MessageReceived")),
            TuplesKt.to("Light", m$default(INSTANCE, "Brightness Color Enabled Range Shadows", null, null, 6, null)),
            TuplesKt.to("SpotLight", m$default(INSTANCE, "Angle Face", null, null, 6, null)),
            TuplesKt.to("ParticleEmitter", m$default(INSTANCE, "Rate Enabled Lifetime Speed Color Size Transparency Texture SpreadAngle Acceleration Drag LightEmission", "Emit Clear", null, 4, null)),
            TuplesKt.to("Attachment", m$default(INSTANCE, "CFrame Position Orientation WorldCFrame WorldPosition Axis SecondaryAxis Visible", null, null, 6, null)),
            TuplesKt.to("Weld", m$default(INSTANCE, "Part0 Part1 C0 C1 Enabled", null, null, 6, null)),
            TuplesKt.to("Motor6D", m$default(INSTANCE, "Part0 Part1 C0 C1 Transform CurrentAngle DesiredAngle MaxVelocity", null, null, 6, null)),
            TuplesKt.to("WeldConstraint", m$default(INSTANCE, "Part0 Part1 Enabled", null, null, 6, null)),
            TuplesKt.to("Decal", m$default(INSTANCE, "Texture Color3 Transparency Face ZIndex", null, null, 6, null)),
            TuplesKt.to("Texture", m$default(INSTANCE, "StudsPerTileU StudsPerTileV OffsetStudsU OffsetStudsV", null, null, 6, null)),
            TuplesKt.to("SpecialMesh", m$default(INSTANCE, "MeshType MeshId TextureId Scale Offset VertexColor", null, null, 6, null)),
            TuplesKt.to("Highlight", m$default(INSTANCE, "Adornee FillColor FillTransparency OutlineColor OutlineTransparency DepthMode Enabled", null, null, 6, null)),
            TuplesKt.to("BadgeService", m$default(INSTANCE, "", "AwardBadge UserHasBadgeAsync GetBadgeInfoAsync", null, 4, null)),
            TuplesKt.to("TeleportService", m$default(INSTANCE, "", "Teleport TeleportAsync TeleportToPlaceInstance", null, 4, null)),
            TuplesKt.to("SoundService", m$default(INSTANCE, "AmbientReverb RespectFilteringEnabled", "PlayLocalSound", null, 4, null)),
            TuplesKt.to("PlayerGui", m$default(INSTANCE, "ScreenOrientation SelectionImageObject", "SetTopbarTransparency GetGuiObjectsAtPosition", null, 4, null)),
            TuplesKt.to("Backpack", m$default(INSTANCE, "", null, null, 6, null)),
            TuplesKt.to("StarterPlayer", m$default(INSTANCE, "CameraMaxZoomDistance CameraMinZoomDistance CameraMode EnableMouseLockOption CharacterWalkSpeed CharacterJumpPower CharacterUseJumpPower DevTouchMovementMode", null, null, 6, null)),
            TuplesKt.to("GuiService", m$default(INSTANCE, "MenuIsOpen", "GetGuiInset IsTenFootInterface", null, 4, null))
    );

    private static final Map<String, String> propTypes = MapsKt.mapOf(
            TuplesKt.to("LocalPlayer", "Player"),
            TuplesKt.to("Character", "Model"),
            TuplesKt.to("PlayerGui", "PlayerGui"),
            TuplesKt.to("Backpack", "Backpack"),
            TuplesKt.to("PlayerScripts", "PlayerScripts"),
            TuplesKt.to("CurrentCamera", "Camera"),
            TuplesKt.to("Terrain", "Terrain"),
            TuplesKt.to("PrimaryPart", "BasePart"),
            TuplesKt.to("RootPart", "BasePart"),
            TuplesKt.to("Humanoid", "Humanoid"),
            TuplesKt.to("HumanoidRootPart", "BasePart"),
            TuplesKt.to("Head", "BasePart"),
            TuplesKt.to("Torso", "BasePart"),
            TuplesKt.to("Handle", "BasePart"),
            TuplesKt.to("Adornee", "Instance"),
            TuplesKt.to("Team", "Team"),
            TuplesKt.to("Position", "#Vector3"),
            TuplesKt.to("Size", "#Vector3"),
            TuplesKt.to("Orientation", "#Vector3"),
            TuplesKt.to("CFrame", "#CFrame"),
            TuplesKt.to("Color", "#Color3"),
            TuplesKt.to("Velocity", "#Vector3"),
            TuplesKt.to("AssemblyLinearVelocity", "#Vector3"),
            TuplesKt.to("LookVector", "#Vector3"),
            TuplesKt.to("RightVector", "#Vector3"),
            TuplesKt.to("UpVector", "#Vector3"),
            TuplesKt.to("Unit", "#Vector3"),
            TuplesKt.to("BackgroundColor3", "#Color3"),
            TuplesKt.to("TextColor3", "#Color3"),
            TuplesKt.to("AbsolutePosition", "#Vector2"),
            TuplesKt.to("AbsoluteSize", "#Vector2"),
            TuplesKt.to("MoveDirection", "#Vector3"),
            TuplesKt.to("Focus", "#CFrame"),
            TuplesKt.to("WorldPivot", "#CFrame"),
            TuplesKt.to("C0", "#CFrame"),
            TuplesKt.to("C1", "#CFrame"),
            TuplesKt.to("BrickColor", "#BrickColor"),
            TuplesKt.to("Hit", "#CFrame"),
            TuplesKt.to("Target", "BasePart"),
            TuplesKt.to("Occupant", "Humanoid"),
            TuplesKt.to("SeatPart", "BasePart"),
            TuplesKt.to("Parent", "Instance")
    );

    private static final Map<String, String> methodReturns = MapsKt.mapOf(
            TuplesKt.to("GetMouse", "#Mouse"),
            TuplesKt.to("LoadAnimation", "#AnimationTrack"),
            TuplesKt.to("Create", "#Tween"),
            TuplesKt.to("GetDataStore", "#DataStore"),
            TuplesKt.to("GetOrderedDataStore", "#DataStore"),
            TuplesKt.to("CreatePath", "#Path"),
            TuplesKt.to("Raycast", "#RaycastResult"),
            TuplesKt.to("GetPivot", "#CFrame"),
            TuplesKt.to("Clone", "@self"),
            TuplesKt.to("GetPlayerFromCharacter", "Player"),
            TuplesKt.to("FindFirstAncestorOfClass", "@arg"),
            TuplesKt.to("FindFirstChildOfClass", "@arg"),
            TuplesKt.to("FindFirstChildWhichIsA", "@arg"),
            TuplesKt.to("FindFirstAncestorWhichIsA", "@arg"),
            TuplesKt.to("GetPropertyChangedSignal", "#RBXScriptSignal"),
            TuplesKt.to("GetAttributeChangedSignal", "#RBXScriptSignal"),
            TuplesKt.to("GetInstanceAddedSignal", "#RBXScriptSignal"),
            TuplesKt.to("Connect", "#RBXScriptConnection"),
            TuplesKt.to("Once", "#RBXScriptConnection")
    );

    private static final Map<String, String> nameHints = MapsKt.mapOf(
            TuplesKt.to("player", "Player"),
            TuplesKt.to("plr", "Player"),
            TuplesKt.to("localplayer", "Player"),
            TuplesKt.to("character", "Model"),
            TuplesKt.to("char", "Model"),
            TuplesKt.to("humanoid", "Humanoid"),
            TuplesKt.to("hum", "Humanoid"),
            TuplesKt.to("hit", "BasePart"),
            TuplesKt.to("part", "BasePart"),
            TuplesKt.to("otherpart", "BasePart"),
            TuplesKt.to("root", "BasePart"),
            TuplesKt.to("hrp", "BasePart"),
            TuplesKt.to("rootpart", "BasePart"),
            TuplesKt.to("tool", "Tool"),
            TuplesKt.to("camera", "Camera"),
            TuplesKt.to("cam", "Camera"),
            TuplesKt.to("mouse", "#Mouse"),
            TuplesKt.to("input", "#InputObject"),
            TuplesKt.to("gui", "ScreenGui"),
            TuplesKt.to("button", "TextButton"),
            TuplesKt.to("frame", "Frame"),
            TuplesKt.to("label", "TextLabel"),
            TuplesKt.to("textbox", "TextBox"),
            TuplesKt.to("sound", "Sound"),
            TuplesKt.to("remote", "RemoteEvent"),
            TuplesKt.to("event", "RemoteEvent"),
            TuplesKt.to("animator", "Animator"),
            TuplesKt.to("track", "#AnimationTrack"),
            TuplesKt.to("tween", "#Tween"),
            TuplesKt.to("model", "Model"),
            TuplesKt.to("folder", "Folder"),
            TuplesKt.to("leaderstats", "Folder")
    );

    private static final List<String> extraEnums = CollectionsKt.listOf(new String[]{
            "KeyCode", "UserInputType", "Material", "EasingStyle", "EasingDirection",
            "HumanoidStateType", "PartType", "SurfaceType", "Font", "NormalId",
            "CameraType", "RaycastFilterType", "CoreGuiType", "SortOrder",
            "FillDirection", "HorizontalAlignment", "VerticalAlignment",
            "TextXAlignment", "TextYAlignment", "AutomaticSize", "ScaleType",
            "ZIndexBehavior", "PlaybackState", "AnimationPriority", "ContextActionResult"
    });

    public enum Kind {
        KEYWORD("kw", -496260),
        GLOBAL("g", -8071433),
        LIBRARY("lib", -8071433),
        FUNCTION("fn", -132195),
        METHOD("m", -132195),
        PROPERTY("p", -6497026),
        EVENT("ev", -14848),
        CLASS("c", -11613776),
        ENUM("e", -4862296),
        SERVICE("s", -11613776),
        VARIABLE("v", -3355444),
        CHILD("ch", -2302806);

        private final String badge;
        private final int color;
        private static final EnumEntries<Kind> $ENTRIES = EnumEntriesKt.enumEntries(values());

        public static EnumEntries<Kind> getEntries() {
            return $ENTRIES;
        }

        Kind(String badge, int color) {
            this.badge = badge;
            this.color = color;
        }

        public final String getBadge() {
            return this.badge;
        }

        public final int getColor() {
            return this.color;
        }
    }

    private Autocomplete() {
    }

    public static final class Item {
        private final String detail;
        private final String insert;
        private final Kind kind;
        private final String label;

        public Item(String label, Kind kind, String detail, String insert) {
            this.label = label != null ? label : "";
            this.kind = kind != null ? kind : Kind.VARIABLE;
            this.detail = detail != null ? detail : "";
            this.insert = insert != null ? insert : this.label;
        }

        public Item(String str, Kind kind, String str2, String str3, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this(str, kind, (i & 4) != 0 ? "" : str2, (i & 8) != 0 ? str : str3);
        }

        public final String getDetail() {
            return this.detail;
        }

        public final String getInsert() {
            return this.insert;
        }

        public final Kind getKind() {
            return this.kind;
        }

        public final String getLabel() {
            return this.label;
        }
    }

    public static final class Result {
        private final int end;
        private final List<Item> items;
        private final String prefix;
        private final int start;

        public Result(int start, int end, String prefix, List<Item> items) {
            this.start = start;
            this.end = end;
            this.prefix = prefix != null ? prefix : "";
            this.items = items != null ? items : Collections.<Item>emptyList();
        }

        public final int getEnd() {
            return this.end;
        }

        public final List<Item> getItems() {
            return this.items;
        }

        public final String getPrefix() {
            return this.prefix;
        }

        public final int getStart() {
            return this.start;
        }
    }

    private static final class Members {
        private final List<String> events;
        private final List<String> methods;
        private final List<String> props;

        public Members(List<String> props, List<String> methods, List<String> events) {
            this.props = props;
            this.methods = methods;
            this.events = events;
        }

        public final List<String> getEvents() {
            return this.events;
        }

        public final List<String> getMethods() {
            return this.methods;
        }

        public final List<String> getProps() {
            return this.props;
        }
    }

    static Members m$default(Autocomplete autocomplete, String str, String str2, String str3, int i, Object obj) {
        if ((i & 2) != 0) {
            str2 = "";
        }
        if ((i & 4) != 0) {
            str3 = "";
        }
        return autocomplete.m(str, str2, str3);
    }

    private final Members m(String props, String methods, String events) {
        return new Members(splitWords(props), splitWords(methods), splitWords(events));
    }

    private static List<String> splitWords(String s) {
        if (s == null || s.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> out = new ArrayList<>();
        for (String tok : s.split(" ")) {
            if (tok != null && !tok.trim().isEmpty()) {
                out.add(tok.trim());
            }
        }
        return out;
    }

    private static abstract class T {
        private T() {
        }

        public static final class Inst extends T {
            private final Instance i;

            public Inst(Instance i) {
                this.i = i;
            }

            public final Instance getI() {
                return this.i;
            }
        }

        public static final class Cls extends T {
            private final String name;

            public Cls(String name) {
                this.name = name;
            }

            public final String getName() {
                return this.name;
            }
        }

        public static final class Lib extends T {
            private final String name;

            public Lib(String name) {
                this.name = name;
            }

            public final String getName() {
                return this.name;
            }
        }

        public static final class DataStatic extends T {
            private final String name;

            public DataStatic(String name) {
                this.name = name;
            }

            public final String getName() {
                return this.name;
            }
        }

        public static final class Data extends T {
            private final String name;

            public Data(String name) {
                this.name = name;
            }

            public final String getName() {
                return this.name;
            }
        }

        public static final class EnumRoot extends T {
            public static final EnumRoot INSTANCE = new EnumRoot();

            private EnumRoot() {
            }
        }

        public static final class EnumType extends T {
            private final String name;

            public EnumType(String name) {
                this.name = name;
            }

            public final String getName() {
                return this.name;
            }
        }

        public static final class Signal extends T {
            public static final Signal INSTANCE = new Signal();

            private Signal() {
            }
        }
    }

    private final String clsOf(T t) {
        if (t instanceof T.Inst) {
            Instance inst = ((T.Inst) t).getI();
            return inst != null ? inst.getClassName() : null;
        }
        if (t instanceof T.Cls) {
            return ((T.Cls) t).getName();
        }
        return null;
    }

    private final List<String> classChain(String cls) {
        ArrayList<String> out = new ArrayList<>();
        if (cls == null || cls.isEmpty()) {
            out.add("Instance");
            return out;
        }
        String c = cls;
        int guard = 0;
        while (c != null && guard < 15) {
            guard++;
            out.add(c);
            if ("Instance".equals(c)) {
                break;
            }
            c = ClassDb.INSTANCE.superclass(c);
        }
        if (!out.contains("Instance")) {
            out.add("Instance");
        }
        return out;
    }

    private final Triple<Set<String>, Set<String>, Set<String>> membersFor(String cls) {
        LinkedHashSet<String> p = new LinkedHashSet<>();
        LinkedHashSet<String> me = new LinkedHashSet<>();
        LinkedHashSet<String> ev = new LinkedHashSet<>();
        for (String c : classChain(cls)) {
            Members members = classMembers.get(c);
            if (members != null) {
                p.addAll(members.getProps());
                me.addAll(members.getMethods());
                ev.addAll(members.getEvents());
            }
        }
        return new Triple<Set<String>, Set<String>, Set<String>>(p, me, ev);
    }

    private final T typeFromHint(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        if (s.startsWith("#")) {
            return s.length() > 1 ? new T.Data(s.substring(1)) : null;
        }
        return new T.Cls(s);
    }

    private final boolean isId(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private final Pair<Integer, Integer> lexState(String text, int pos) {
        int n = Math.max(0, Math.min(pos, text.length()));
        int i = 0;
        while (i < n) {
            char c = text.charAt(i);
            if (c == '-' && i + 1 < n && text.charAt(i + 1) == '-') {
                int lvl = longLevel(text, i + 2);
                if (lvl >= 0) {
                    String close = "]" + StringsKt.repeat("=", lvl) + "]";
                    int e = text.indexOf(close, i + 4 + lvl);
                    if (e < 0 || e + close.length() > n) {
                        return TuplesKt.to(2, Integer.valueOf(i));
                    }
                    i = e + close.length();
                } else {
                    int e2 = text.indexOf('\n', i);
                    if (e2 < 0 || e2 >= n) {
                        return TuplesKt.to(2, Integer.valueOf(i));
                    }
                    i = e2 + 1;
                }
            } else if (c == '"' || c == '\'' || c == '`') {
                int j = i + 1;
                while (j < n && text.charAt(j) != c && text.charAt(j) != '\n') {
                    if (text.charAt(j) == '\\') {
                        j++;
                    }
                    j++;
                }
                if (j >= n) {
                    return TuplesKt.to(1, Integer.valueOf(i));
                }
                i = j + 1;
            } else if (c == '[') {
                int lvl = longLevel(text, i);
                if (lvl >= 0) {
                    String close2 = "]" + StringsKt.repeat("=", lvl) + "]";
                    int e3 = text.indexOf(close2, i + 2 + lvl);
                    if (e3 < 0 || e3 + close2.length() > n) {
                        return TuplesKt.to(2, Integer.valueOf(i));
                    }
                    i = e3 + close2.length();
                } else {
                    i++;
                }
            } else {
                i++;
            }
        }
        return TuplesKt.to(0, -1);
    }

    private final int longLevel(String text, int at) {
        int len = text.length();
        if (at < 0 || at >= len || text.charAt(at) != '[') {
            return -1;
        }
        int j = at + 1;
        int l = 0;
        while (j < len && text.charAt(j) == '=') {
            l++;
            j++;
        }
        if (j >= len || text.charAt(j) != '[') {
            return -1;
        }
        return l;
    }

    private static final class Step {
        private final char acc;
        private final String arg;
        private final boolean call;
        private final String name;

        public Step(String name, char acc, String arg, boolean call) {
            this.name = name != null ? name : "";
            this.acc = acc;
            this.arg = arg;
            this.call = call;
        }

        public final char getAcc() {
            return this.acc;
        }

        public final String getArg() {
            return this.arg;
        }

        public final boolean getCall() {
            return this.call;
        }

        public final String getName() {
            return this.name;
        }
    }

    private final List<Step> chainBefore(String text, int end) {
        if (text == null || end <= 0) {
            return null;
        }
        int clampedEnd = Math.min(end, text.length());
        ArrayList<Step> steps = new ArrayList<>();
        int i = clampedEnd - 1;
        int guard = 0;
        while (i >= 0 && guard < 24) {
            guard++;
            while (i >= 0 && (text.charAt(i) == ' ' || text.charAt(i) == '\t')) {
                i--;
            }
            if (i < 0) {
                break;
            }
            String arg = null;
            boolean call = false;
            if (text.charAt(i) == ')') {
                int depth = 0;
                int j = i;
                while (j >= 0) {
                    char ch = text.charAt(j);
                    if (ch == ')') {
                        depth++;
                    } else if (ch == '(') {
                        depth--;
                        if (depth == 0) {
                            break;
                        }
                    }
                    j--;
                }
                if (j < 0) {
                    return null;
                }
                String inside = text.substring(j + 1, i).trim();
                Matcher m = RX_CALL_STR_ARG.matcher(inside);
                if (m.find()) {
                    arg = m.group(1);
                }
                call = true;
                i = j - 1;
            } else if (text.charAt(i) == ']') {
                int j = i;
                while (j >= 0 && text.charAt(j) != '[') {
                    j--;
                }
                if (j < 0) {
                    return null;
                }
                String inside = text.substring(j + 1, i).trim();
                Matcher m = RX_BRACKET_STR.matcher(inside);
                if (!m.find()) {
                    return null;
                }
                String key = m.group(1);
                if (key == null) {
                    return null;
                }
                steps.add(0, new Step(key, '.', null, false));
                i = j - 1;
                continue;
            }
            while (i >= 0 && (text.charAt(i) == ' ' || text.charAt(i) == '\t')) {
                i--;
            }
            if (i < 0 || !isId(text.charAt(i))) {
                break;
            }
            int e = i + 1;
            while (i >= 0 && isId(text.charAt(i))) {
                i--;
            }
            String name = text.substring(i + 1, e);
            if (name.isEmpty() || Character.isDigit(name.charAt(0))) {
                return null;
            }
            char acc = (i >= 0 && (text.charAt(i) == '.' || text.charAt(i) == ':')) ? text.charAt(i) : ' ';
            steps.add(0, new Step(name, acc, arg, call));
            if (acc == ' ') {
                return steps;
            }
            i--;
        }
        return steps.isEmpty() ? null : steps;
    }

    private final T resolveRoot(String name, Studio studio, Instance script, String text, int before, int depth) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        DataModel dm = studio != null ? studio.getModel() : null;
        switch (name) {
            case "game":
            case "Game":
                return dm != null ? new T.Inst(dm) : new T.Cls("DataModel");
            case "workspace":
            case "Workspace":
                Instance ws = dm != null ? dm.getWorkspace() : null;
                return ws != null ? new T.Inst(ws) : new T.Cls("Workspace");
            case "script":
                return script != null ? new T.Inst(script) : new T.Cls("Script");
            case "Enum":
                return T.EnumRoot.INSTANCE;
            case "plugin":
                return new T.Cls("Plugin");
            default:
                if (libs.containsKey(name)) {
                    return new T.Lib(name);
                }
                if (dataStatics.containsKey(name)) {
                    return new T.DataStatic(name);
                }
                return resolveLocal(name, studio, script, text, before, depth);
        }
    }

    private final T resolveLocal(String name, Studio studio, Instance script, String text, int before, int depth) {
        if (depth > 3 || name == null || name.isEmpty()) {
            return null;
        }
        if (text != null && before > 0) {
            int limit = Math.max(0, Math.min(before, text.length()));
            String prefixText = text.substring(0, limit);
            Pattern rx = Pattern.compile("(?:^|[\\s;(])local\\s+" + Pattern.quote(name) + "\\s*(?::\\s*[\\w.<>]+\\s*)?=\\s*([^\\n;]+)");
            Matcher matcher = rx.matcher(prefixText);
            String lastExpr = null;
            int lastStart = 0;
            while (matcher.find()) {
                lastExpr = matcher.group(1);
                lastStart = matcher.start();
            }
            if (lastExpr != null) {
                String expr = lastExpr.trim();
                if (expr.endsWith(",")) {
                    expr = expr.substring(0, expr.length() - 1).trim();
                }
                T t = resolveExpr(expr, studio, script, text, lastStart, depth + 1);
                if (t != null) {
                    return t;
                }
            }
        }
        String hint = nameHints.get(name.toLowerCase(Locale.ROOT));
        if (hint != null) {
            return typeFromHint(hint);
        }
        if (!ClassDb.INSTANCE.isServiceName(name)) {
            return null;
        }
        DataModel dm = studio != null ? studio.getModel() : null;
        Instance svc = dm != null ? dm.findService(name) : null;
        return svc != null ? new T.Inst(svc) : new T.Cls(name);
    }

    private final T resolveExpr(String expr, Studio studio, Instance script, String fullText, int before, int depth) {
        if (expr == null) {
            return null;
        }
        String e = expr.trim();
        if (e.isEmpty()) {
            return null;
        }
        Matcher mNew = RX_CTOR_NEW.matcher(e);
        if (mNew.find()) {
            String typeName = mNew.group(1);
            if ("Instance".equals(typeName)) {
                Matcher mInst = RX_INSTANCE_NEW.matcher(e);
                if (mInst.find()) {
                    return new T.Cls(mInst.group(1));
                }
            } else if (typeName != null && dataStatics.containsKey(typeName)) {
                return new T.Data(typeName);
            }
        }
        Matcher mFac = RX_DATA_FACTORY.matcher(e);
        if (mFac.find()) {
            String typeName = mFac.group(1);
            if (typeName != null && dataStatics.containsKey(typeName)) {
                return new T.Data(typeName);
            }
        }
        List<Step> steps = chainBefore(e + " ", e.length());
        if (steps == null) {
            return null;
        }
        return resolveSteps(steps, studio, script, fullText, before, depth);
    }

    private final T resolveSteps(List<Step> steps, Studio studio, Instance script, String text, int before, int depth) {
        if (steps == null || steps.isEmpty()) {
            return null;
        }
        T t = resolveRoot(steps.get(0).getName(), studio, script, text, before, depth);
        if (t == null) {
            return null;
        }
        for (int idx = 1; idx < steps.size(); idx++) {
            T next = member(t, steps.get(idx), studio);
            if (next == null) {
                return null;
            }
            t = next;
        }
        return t;
    }

    private final T member(T receiver, Step step, Studio studio) {
        if (receiver == null || step == null) {
            return null;
        }
        String name = step.getName();
        String arg = step.getArg();
        if (receiver instanceof T.EnumRoot) {
            return new T.EnumType(name);
        }
        if (receiver instanceof T.DataStatic) {
            String dn = ((T.DataStatic) receiver).getName();
            if ("Instance".equals(dn) && "new".equals(name) && arg != null && !arg.isEmpty()) {
                return new T.Cls(arg);
            }
            return new T.Data(dn);
        }
        DataModel dm = studio != null ? studio.getModel() : null;
        if (receiver instanceof T.Inst) {
            Instance inst = ((T.Inst) receiver).getI();
            if (inst != null) {
                if ("Parent".equals(name)) {
                    Instance p = inst.getParent();
                    return p != null ? new T.Inst(p) : new T.Cls("Instance");
                }
                if (step.getCall()) {
                    if (("GetService".equals(name) || "FindService".equals(name)) && arg != null && !arg.isEmpty()) {
                        Instance svc = dm != null ? dm.findService(arg) : null;
                        return svc != null ? new T.Inst(svc) : new T.Cls(arg);
                    }
                    if ("FindFirstChild".equals(name) || "WaitForChild".equals(name) || "FindFirstDescendant".equals(name)) {
                        if (arg == null || arg.isEmpty()) {
                            return new T.Cls("Instance");
                        }
                        boolean recursive = "FindFirstDescendant".equals(name);
                        Instance ch = inst.findFirstChild(arg, recursive);
                        if (ch != null) {
                            return new T.Inst(ch);
                        }
                        String hint = propTypes.get(arg);
                        if (hint != null) {
                            return typeFromHint(hint);
                        }
                        return new T.Cls("Instance");
                    }
                } else {
                    Instance ch = inst.findFirstChild(name, false);
                    if (ch != null) {
                        return new T.Inst(ch);
                    }
                }
            }
        }
        if (step.getCall()) {
            String ret = methodReturns.get(name);
            if (ret != null) {
                if ("@self".equals(ret)) {
                    return receiver;
                }
                if ("@arg".equals(ret)) {
                    return (arg != null && !arg.isEmpty()) ? new T.Cls(arg) : new T.Cls("Instance");
                }
                return typeFromHint(ret);
            }
            if (("GetService".equals(name) || "FindService".equals(name)) && arg != null && !arg.isEmpty()) {
                Instance svc = dm != null ? dm.findService(arg) : null;
                return svc != null ? new T.Inst(svc) : new T.Cls(arg);
            }
            if ("FindFirstChild".equals(name) || "WaitForChild".equals(name) || "FindFirstDescendant".equals(name)) {
                if (arg != null && !arg.isEmpty()) {
                    String hint = propTypes.get(arg);
                    if (hint != null) {
                        return typeFromHint(hint);
                    }
                }
                return new T.Cls("Instance");
            }
        } else {
            String cls = clsOf(receiver);
            if (cls != null) {
                Set<String> events = membersFor(cls).component3();
                if (events.contains(name)) {
                    return T.Signal.INSTANCE;
                }
            }
        }
        String pt = propTypes.get(name);
        if (pt != null) {
            return typeFromHint(pt);
        }
        return null;
    }

    private final Set<String> localNames(String text, int upTo) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        if (text == null || upTo <= 0) {
            return out;
        }
        int limit = Math.max(0, Math.min(upTo, text.length()));
        String src = text.substring(0, limit);

        Matcher mLocal = RX_LOCAL_DECL.matcher(src);
        while (mLocal.find()) {
            String grp = mLocal.group(1);
            if (grp != null) {
                for (String part : grp.split(",")) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        out.add(trimmed);
                    }
                }
            }
        }

        Matcher mFunc = RX_FUNC_DECL.matcher(src);
        while (mFunc.find()) {
            String grp = mFunc.group(1);
            if (grp != null) {
                int dot = grp.indexOf('.');
                if (dot >= 0) {
                    grp = grp.substring(0, dot);
                }
                int colon = grp.indexOf(':');
                if (colon >= 0) {
                    grp = grp.substring(0, colon);
                }
                if (!grp.isEmpty()) {
                    out.add(grp);
                }
            }
        }

        Matcher mParams = RX_FUNC_PARAMS.matcher(src);
        while (mParams.find()) {
            String grp = mParams.group(1);
            if (grp != null) {
                for (String part : grp.split(",")) {
                    String trimmed = part.trim();
                    int colon = trimmed.indexOf(':');
                    if (colon >= 0) {
                        trimmed = trimmed.substring(0, colon).trim();
                    }
                    if (!trimmed.isEmpty() && !"...".equals(trimmed)) {
                        out.add(trimmed);
                    }
                }
            }
        }

        Matcher mFor = RX_FOR_VARS.matcher(src);
        while (mFor.find()) {
            String grp = mFor.group(1);
            if (grp != null) {
                for (String part : grp.split(",")) {
                    String trimmed = part.trim();
                    int colon = trimmed.indexOf(':');
                    if (colon >= 0) {
                        trimmed = trimmed.substring(0, colon).trim();
                    }
                    if (!trimmed.isEmpty()) {
                        out.add(trimmed);
                    }
                }
            }
        }
        return out;
    }

    private final List<Item> rank(List<Item> items, final String prefix) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        if (prefix == null || prefix.isEmpty()) {
            HashSet<String> seen = new HashSet<>();
            ArrayList<Item> out = new ArrayList<>();
            for (Item it : items) {
                if (it != null && seen.add(it.getLabel())) {
                    out.add(it);
                    if (out.size() >= 60) {
                        break;
                    }
                }
            }
            return out;
        }
        final String p = prefix.toLowerCase(Locale.ROOT);
        HashSet<String> seen = new HashSet<>();
        ArrayList<Item> filtered = new ArrayList<>();
        for (Item it : items) {
            if (it == null) {
                continue;
            }
            String label = it.getLabel();
            if (label.equals(prefix)) {
                continue;
            }
            String low = label.toLowerCase(Locale.ROOT);
            if (low.contains(p) && seen.add(label)) {
                filtered.add(it);
            }
        }
        Collections.sort(filtered, new Comparator<Item>() {
            @Override
            public int compare(Item a, Item b) {
                int ra = rankScore(a.getLabel(), prefix, p);
                int rb = rankScore(b.getLabel(), prefix, p);
                if (ra != rb) {
                    return Integer.compare(ra, rb);
                }
                int la = a.getLabel().length();
                int lb = b.getLabel().length();
                if (la != lb) {
                    return Integer.compare(la, lb);
                }
                return a.getLabel().compareTo(b.getLabel());
            }
        });
        if (filtered.size() > 40) {
            return new ArrayList<>(filtered.subList(0, 40));
        }
        return filtered;
    }

    private static int rankScore(String label, String prefix, String lowerPrefix) {
        if (label.startsWith(prefix)) {
            return 0;
        }
        if (label.toLowerCase(Locale.ROOT).startsWith(lowerPrefix)) {
            return 1;
        }
        return 2;
    }

    public static Result complete$default(Autocomplete autocomplete, Studio studio, Instance instance, String str, int i, boolean z, int i2, Object obj) {
        if ((i2 & 16) != 0) {
            z = false;
        }
        return autocomplete.complete(studio, instance, str, i, z);
    }

    public final Result complete(Studio studio, Instance script, String text, int cursor, boolean explicit) {
        if (text == null || cursor < 0 || cursor > text.length()) {
            return null;
        }
        try {
            Pair<Integer, Integer> pairLexState = lexState(text, cursor);
            int state = pairLexState.component1().intValue();
            int at = pairLexState.component2().intValue();
            if (state == 2) {
                return null;
            }
            if (state == 1) {
                return completeString(studio, script, text, cursor, at);
            }
            int start = cursor;
            while (start > 0 && isId(text.charAt(start - 1))) {
                start--;
            }
            String prefix = text.substring(start, cursor);
            if (!prefix.isEmpty() && Character.isDigit(prefix.charAt(0))) {
                return null;
            }
            int accIdx = start - 1;
            char acc = accIdx >= 0 ? text.charAt(accIdx) : ' ';
            if (acc == '.' && accIdx > 0 && text.charAt(accIdx - 1) == '.') {
                return null;
            }
            if (acc == '.' || acc == ':') {
                List<Step> steps = chainBefore(text, accIdx);
                if (steps == null) {
                    return null;
                }
                T t;
                List<Item> memberItems;
                Object lock = studio != null ? studio.getLock() : null;
                if (lock != null) {
                    synchronized (lock) {
                        t = resolveSteps(steps, studio, script, text, accIdx, 0);
                        memberItems = t != null ? membersOf(t, acc) : null;
                    }
                } else {
                    t = resolveSteps(steps, studio, script, text, accIdx, 0);
                    memberItems = t != null ? membersOf(t, acc) : null;
                }
                if (memberItems == null) {
                    return null;
                }
                List<Item> ranked = rank(memberItems, prefix);
                if (ranked.isEmpty()) {
                    return null;
                }
                return new Result(start, cursor, prefix, ranked);
            }

            if (prefix.isEmpty() && !explicit) {
                return null;
            }
            String before = text.substring(Math.max(0, start - 16), start);
            if (RX_DECL_PREFIX.matcher(before).find() && !explicit) {
                return null;
            }
            ArrayList<Item> items = new ArrayList<>();
            for (String local : localNames(text, start)) {
                if (!local.equals(prefix)) {
                    items.add(new Item(local, Kind.VARIABLE, "local", local));
                }
            }
            for (String kw : keywords) {
                items.add(new Item(kw, Kind.KEYWORD, "", kw));
            }
            for (String gl : globals) {
                items.add(new Item(gl, Kind.GLOBAL, "", gl));
            }
            for (String lib : libs.keySet()) {
                items.add(new Item(lib, Kind.LIBRARY, "library", lib));
            }
            for (String ds : dataStatics.keySet()) {
                items.add(new Item(ds, Kind.CLASS, "datatype", ds));
            }
            items.add(new Item("Enum", Kind.ENUM, "", "Enum"));
            List<Item> ranked = rank(items, prefix);
            return ranked.isEmpty() ? null : new Result(start, cursor, prefix, ranked);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private final List<Item> membersOf(T t, char acc) {
        ArrayList<Item> out = new ArrayList<>();
        if (t == null) {
            return out;
        }
        if (t instanceof T.Lib) {
            String libName = ((T.Lib) t).getName();
            List<String> entries = libs.get(libName);
            if (entries != null) {
                for (String str : entries) {
                    Kind kind;
                    if ("pi".equals(str) || "huge".equals(str) || "charpattern".equals(str) || "zero".equals(str) || "one".equals(str)) {
                        kind = Kind.PROPERTY;
                    } else {
                        kind = Kind.FUNCTION;
                    }
                    out.add(new Item(str, kind, libName + "." + str, str));
                }
            }
        } else if (t instanceof T.DataStatic) {
            String dsName = ((T.DataStatic) t).getName();
            List<String> entries = dataStatics.get(dsName);
            if (entries != null) {
                for (String str : entries) {
                    if (str == null || str.isEmpty()) {
                        continue;
                    }
                    Kind kind = (!Character.isUpperCase(str.charAt(0)) || "Angles".equals(str) || "FromNormalId".equals(str) || "FromAxis".equals(str))
                            ? Kind.FUNCTION
                            : Kind.PROPERTY;
                    out.add(new Item(str, kind, dsName + "." + str, str));
                }
            }
        } else if (t instanceof T.Data) {
            String dataName = ((T.Data) t).getName();
            Pair<List<String>, List<String>> pair = dataMembers.get(dataName);
            if (pair != null) {
                if (acc == ':') {
                    for (String mName : pair.component2()) {
                        out.add(new Item(mName, Kind.METHOD, dataName, mName));
                    }
                } else {
                    for (String pName : pair.component1()) {
                        out.add(new Item(pName, Kind.PROPERTY, dataName, pName));
                    }
                }
            }
        } else if (t instanceof T.EnumRoot) {
            out.add(new Item("GetEnums", Kind.FUNCTION, "", "GetEnums"));
            LinkedHashSet<String> allEnums = new LinkedHashSet<>(extraEnums);
            Map<String, LinkedHashMap<String, Integer>> enumTypes = Enums.INSTANCE.getTypes();
            if (enumTypes != null) {
                allEnums.addAll(enumTypes.keySet());
            }
            ArrayList<String> sortedEnums = new ArrayList<>(allEnums);
            Collections.sort(sortedEnums);
            for (String eName : sortedEnums) {
                if (eName != null) {
                    out.add(new Item(eName, Kind.ENUM, "Enum", eName));
                }
            }
        } else if (t instanceof T.EnumType) {
            String enumName = ((T.EnumType) t).getName();
            if (acc == ':') {
                out.add(new Item("GetEnumItems", Kind.METHOD, "", "GetEnumItems"));
            } else {
                Map<String, LinkedHashMap<String, Integer>> enumTypes = Enums.INSTANCE.getTypes();
                LinkedHashMap<String, Integer> vals = enumTypes != null ? enumTypes.get(enumName) : null;
                if (vals != null) {
                    for (String valName : vals.keySet()) {
                        if (valName != null) {
                            out.add(new Item(valName, Kind.ENUM, "Enum." + enumName, valName));
                        }
                    }
                }
            }
        } else if (t instanceof T.Signal) {
            if (acc == ':') {
                for (String mName : new String[]{"Connect", "Once", "Wait", "ConnectParallel"}) {
                    out.add(new Item(mName, Kind.METHOD, "RBXScriptSignal", mName));
                }
            }
        } else if (t instanceof T.Inst || t instanceof T.Cls) {
            String cls = clsOf(t);
            if (cls == null) {
                return out;
            }
            Triple<Set<String>, Set<String>, Set<String>> members = membersFor(cls);
            Set<String> props = members.component1();
            Set<String> methods = members.component2();
            Set<String> events = members.component3();
            if (acc == ':') {
                for (String mName : methods) {
                    out.add(new Item(mName, Kind.METHOD, cls, mName));
                }
            } else {
                if (t instanceof T.Inst) {
                    Instance inst = ((T.Inst) t).getI();
                    if (inst != null) {
                        List<Instance> children = inst.getChildren();
                        if (children != null) {
                            for (Instance ch : children) {
                                if (ch != null && ch.getName() != null) {
                                    String chName = ch.getName();
                                    out.add(new Item(chName, Kind.CHILD, ch.getClassName() != null ? ch.getClassName() : "Instance", isSimpleName(chName) ? chName : ""));
                                }
                            }
                        }
                        if (inst instanceof DataModel) {
                            for (String svc : ClassDb.INSTANCE.getServiceNames()) {
                                if (svc != null) {
                                    out.add(new Item(svc, Kind.SERVICE, "service", svc));
                                }
                            }
                        }
                        Map<String, ?> rawProps = inst.getProps();
                        if (rawProps != null) {
                            for (String rawKey : rawProps.keySet()) {
                                if (rawKey == null) {
                                    continue;
                                }
                                String disp = PropNames.INSTANCE.display(rawKey);
                                if (disp != null && isSimpleName(disp) && !disp.startsWith("xml")) {
                                    out.add(new Item(disp, Kind.PROPERTY, cls, disp));
                                }
                            }
                        }
                    }
                }
                for (String pName : props) {
                    out.add(new Item(pName, Kind.PROPERTY, cls, pName));
                }
                for (String evName : events) {
                    out.add(new Item(evName, Kind.EVENT, cls, evName));
                }
            }
        }
        ArrayList<Item> nonEmpty = new ArrayList<>();
        for (Item item : out) {
            if (item != null && !item.getInsert().isEmpty()) {
                nonEmpty.add(item);
            }
        }
        return nonEmpty;
    }

    private final boolean isSimpleName(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        char first = s.charAt(0);
        if (!Character.isLetter(first) && first != '_') {
            return false;
        }
        for (int i = 1; i < s.length(); i++) {
            if (!isId(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private final List<String> getServiceList() {
        LinkedHashSet<String> all = new LinkedHashSet<>();
        Collection<String> dbServices = ClassDb.INSTANCE.getServiceNames();
        if (dbServices != null) {
            all.addAll(dbServices);
        }
        Collections.addAll(all,
                "RunService", "TweenService", "Debris", "HttpService", "UserInputService",
                "ContextActionService", "CollectionService", "MarketplaceService",
                "DataStoreService", "PathfindingService", "TextService", "GuiService",
                "BadgeService", "PhysicsService", "InsertService", "TeleportService",
                "ProximityPromptService", "MessagingService", "MemoryStoreService",
                "AssetService", "SocialService", "PolicyService", "HapticService",
                "VRService", "GroupService", "Selection", "ChangeHistoryService", "StudioService"
        );
        ArrayList<String> sorted = new ArrayList<>(all);
        Collections.sort(sorted);
        return sorted;
    }

    private final Result completeString(Studio studio, Instance script, String text, int cursor, int quoteIdx) {
        if (text == null || quoteIdx < 0 || cursor < quoteIdx + 1 || cursor > text.length()) {
            return null;
        }
        int start = quoteIdx + 1;
        String prefix = text.substring(start, cursor);
        int p = quoteIdx - 1;
        while (p >= 0 && Character.isWhitespace(text.charAt(p))) {
            p--;
        }
        if (p < 0 || text.charAt(p) != '(') {
            return null;
        }
        int endFn = p;
        int startFn = endFn;
        while (startFn > 0 && isId(text.charAt(startFn - 1))) {
            startFn--;
        }
        String fnName = text.substring(startFn, endFn);
        if (fnName.isEmpty()) {
            return null;
        }
        int accIdx = startFn - 1;
        char acc = accIdx >= 0 ? text.charAt(accIdx) : ' ';

        ArrayList<Item> items = new ArrayList<>();
        if ("GetService".equals(fnName) || "FindService".equals(fnName)) {
            LinkedHashSet<String> seen = new LinkedHashSet<>(getServiceList());
            Object lock = studio != null ? studio.getLock() : null;
            DataModel dm = studio != null ? studio.getModel() : null;
            if (dm != null) {
                if (lock != null) {
                    synchronized (lock) {
                        for (Instance ch : dm.getChildren()) {
                            if (ch != null && ch.getClassName() != null) {
                                seen.add(ch.getClassName());
                            }
                        }
                    }
                } else {
                    for (Instance ch : dm.getChildren()) {
                        if (ch != null && ch.getClassName() != null) {
                            seen.add(ch.getClassName());
                        }
                    }
                }
            }
            for (String svc : seen) {
                items.add(new Item(svc, Kind.SERVICE, "service", svc));
            }
        } else if ("new".equals(fnName) || "IsA".equals(fnName)
                || "FindFirstChildOfClass".equals(fnName) || "FindFirstChildWhichIsA".equals(fnName)
                || "FindFirstAncestorOfClass".equals(fnName) || "FindFirstAncestorWhichIsA".equals(fnName)) {
            for (String cn : classMembers.keySet()) {
                items.add(new Item(cn, Kind.CLASS, "Class", cn));
            }
        } else if ("FindFirstChild".equals(fnName) || "WaitForChild".equals(fnName) || "FindFirstDescendant".equals(fnName)) {
            Instance base = null;
            Object lock = studio != null ? studio.getLock() : null;
            if (acc == ':' && accIdx > 0) {
                List<Step> chain = chainBefore(text, accIdx);
                if (chain != null) {
                    T resolved;
                    if (lock != null) {
                        synchronized (lock) {
                            resolved = resolveSteps(chain, studio, script, text, accIdx, 0);
                        }
                    } else {
                        resolved = resolveSteps(chain, studio, script, text, accIdx, 0);
                    }
                    if (resolved instanceof T.Inst) {
                        base = ((T.Inst) resolved).getI();
                    }
                }
            }
            if (base == null && studio != null && studio.getModel() != null) {
                if (lock != null) {
                    synchronized (lock) {
                        base = script != null ? script.getParent() : studio.getModel().findService("Workspace");
                    }
                } else {
                    base = script != null ? script.getParent() : studio.getModel().findService("Workspace");
                }
            }
            if (base != null) {
                if (lock != null) {
                    synchronized (lock) {
                        for (Instance ch : base.getChildren()) {
                            if (ch != null && ch.getName() != null) {
                                items.add(new Item(ch.getName(), Kind.CHILD, ch.getClassName() != null ? ch.getClassName() : "Instance", ch.getName()));
                            }
                        }
                    }
                } else {
                    for (Instance ch : base.getChildren()) {
                        if (ch != null && ch.getName() != null) {
                            items.add(new Item(ch.getName(), Kind.CHILD, ch.getClassName() != null ? ch.getClassName() : "Instance", ch.getName()));
                        }
                    }
                }
            }
        } else if (("GetAttribute".equals(fnName) || "SetAttribute".equals(fnName)) && acc == ':' && accIdx > 0) {
            List<Step> chain = chainBefore(text, accIdx);
            if (chain != null) {
                Object lock = studio != null ? studio.getLock() : null;
                if (lock != null) {
                    synchronized (lock) {
                        T resolved = resolveSteps(chain, studio, script, text, accIdx, 0);
                        if (resolved instanceof T.Inst && ((T.Inst) resolved).getI() != null) {
                            Map<String, ?> attrs = ((T.Inst) resolved).getI().getAttributes();
                            if (attrs != null) {
                                for (String attrName : attrs.keySet()) {
                                    if (attrName != null) {
                                        items.add(new Item(attrName, Kind.PROPERTY, "attribute", attrName));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        List<Item> ranked = rank(items, prefix);
        if (ranked.isEmpty()) {
            return null;
        }
        return new Result(start, cursor, prefix, ranked);
    }
}
