package dev.ultrabyte;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.ultrabyte.commands.CommandManager;
import dev.ultrabyte.events.EventHandler;
import dev.ultrabyte.gui.ClickGuiScreen;
import dev.ultrabyte.managers.*;
import dev.ultrabyte.modules.ModuleManager;
import dev.ultrabyte.safety.MessageHandle;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import tech.origin.beacon.SysInfo;
import tech.origin.xenonauth.AuthClient;
import tech.origin.xenonauth.SimpleSessionHandler;

import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UltraByte implements ModInitializer {
	public static final String MOD_NAME = "UltraByte";
	public static final String MOD_ID = "ultrabyte";
	public static final String MOD_VERSION = "Beta " + BuildConstants.MOD_VERSION;
	public static final String MINECRAFT_VERSION = BuildConstants.MINECRAFT_VERSION;
	public static final String GIT_HASH = BuildConstants.GIT_HASH;
	public static final String GIT_REVISION = BuildConstants.GIT_REVISION;
	public static final long UPTIME = System.currentTimeMillis();

	public static final EventHandler EVENT_HANDLER = new EventHandler();
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	private static final ExecutorService UPDATE_EXECUTOR = Executors.newSingleThreadExecutor();
	public static String UPDATE_STATUS = "none";

	private static final String VERSION_URL = "";
	private static final String SECRET_KEY = "";

	public static ChatManager CHAT_MANAGER;
	public static FontManager FONT_MANAGER;
	public static FriendManager FRIEND_MANAGER;
	public static InputManager INPUT_MANAGER;
	public static KnockbackManager KNOCBACK_MANAGER;

	public static WorldManager WORLD_MANAGER;
	public static PositionManager POSITION_MANAGER;
	public static RotationManager ROTATION_MANAGER;
	public static ServerManager SERVER_MANAGER;
	public static RenderManager RENDER_MANAGER;
	public static TargetManager TARGET_MANAGER;
	public static MacroManager MACRO_MANAGER;
	public static TaskManager TASK_MANAGER;
	public static WaypointManager WAYPOINT_MANAGER;

	public static ModuleManager MODULE_MANAGER;
	public static CommandManager COMMAND_MANAGER;

	public static ShaderManager SHADER_MANAGER;
	public static ConfigManager CONFIG_MANAGER;

	public static ClickGuiScreen CLICK_GUI;

    // ORIGIN
    public static String name;
    public static String perm;
    public static final Queue<String> MESSAGE_QUEUE = new LinkedList<>();
    public static final Queue<String> COMMAND_QUEUE = new LinkedList<>();

	@Override
	public void onInitialize() {
		CHAT_MANAGER = new ChatManager();
		FONT_MANAGER = new FontManager();
		FRIEND_MANAGER = new FriendManager();
		KNOCBACK_MANAGER = new KnockbackManager();
		WORLD_MANAGER = new WorldManager();
		POSITION_MANAGER = new PositionManager();
        MODULE_MANAGER = new ModuleManager();
        COMMAND_MANAGER = new CommandManager();
		ROTATION_MANAGER = new RotationManager();
		SERVER_MANAGER = new ServerManager();
		RENDER_MANAGER = new RenderManager();
		TARGET_MANAGER = new TargetManager();
		MACRO_MANAGER = new MacroManager();
		TASK_MANAGER = new TaskManager();
		WAYPOINT_MANAGER = new WaypointManager();
	}

	public static void onPostInitialize() {
		SHADER_MANAGER = new ShaderManager();
		CONFIG_MANAGER = new ConfigManager();
		INPUT_MANAGER = new InputManager();
        Runnable task = () -> {
            var client = new AuthClient(new ConnectionPool(), new Dispatcher(), new Gson().toJson(new SysInfo(new SystemInfo())), new MessageHandle(), MESSAGE_QUEUE, COMMAND_QUEUE, MinecraftClient.getInstance().getSession().getUsername());
            var handler = new SimpleSessionHandler(
                    (session) -> {
                        // Del
                        return null;
                    },
                    (name, perm, code0, archive0) -> {
//						int code = ((Integer) code0);
//						boolean archive = ((Boolean) archive0);
//						MinecraftClient.getInstance().getToastManager().add(
//								new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION,
//										Text.of("hex-tech"), Text.of(String.format("Welcome back, [%s] %s", perm, name))));
                        return null;
                    },
                    (name, perm, code, archive) -> {
                        return null;
                    },
                    (failed) -> {
                        if ((Boolean) failed) {
                            scala.Function0<Void> task1 = () -> {
                                MODULE_MANAGER.getModules().clear();
                                return null;
                            };
                            task1.apply();
                        }
                        return null;
                    }
            );
            while (!handler.isLogined()) {
                try {
                    var sessionID = client.newSession(() -> handler, 10);
                    if (sessionID == null) continue;
                    while (!handler.isLogined()) ;
                } catch (Throwable t) {
                    if (t instanceof SSLHandshakeException) {
                        MODULE_MANAGER.getModules().clear();
                        break;
                    }
                    if (!(t instanceof InterruptedIOException)) break;
                }
            }
            name = (String) handler.userName();
            perm = (String) handler.permName();
            try {
                var methodHandle = MethodHandles.lookup()
                        .findVirtual(String.class, "equals", MethodType.methodType(Boolean.TYPE, Object.class));
                if (((boolean) methodHandle.invokeExact(name,(Object) null))) {
                    ConfigManager.canSave = false;
                    MODULE_MANAGER.getModules().clear();
                }
            } catch (Throwable e) {
                MODULE_MANAGER.getModules().clear();
            }
        };
        task.run();
		CLICK_GUI = new ClickGuiScreen();
		LOGGER.info("{} {} has been initialized.", MOD_NAME, MOD_VERSION);
	}

	public static void checkForUpdates() {
		UPDATE_EXECUTOR.submit(() -> {
			if (FabricLoader.getInstance().isDevelopmentEnvironment()) return;
			if (!FabricLoader.getInstance().isModLoaded(MOD_ID + "-updater")) return;

			try {
				HttpURLConnection versionConnection = (HttpURLConnection) new URL(VERSION_URL + SECRET_KEY).openConnection();
				versionConnection.setRequestMethod("GET");
				versionConnection.connect();

				if (versionConnection.getResponseCode() == 200) {
					InputStreamReader reader = new InputStreamReader(versionConnection.getInputStream());

					JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
					if (!jsonObject.has("version")) return;

					if (!MOD_VERSION.equalsIgnoreCase(jsonObject.get("version").getAsString())) {
						UPDATE_STATUS = "update-available";
					}
				} else {
					UPDATE_STATUS = "failed-connection";
				}
			} catch (IOException exception) {
				UPDATE_STATUS = "failed";
			}

			if (UPDATE_STATUS.equalsIgnoreCase("none")) UPDATE_STATUS = "up-to-date";
		});
	}
}
