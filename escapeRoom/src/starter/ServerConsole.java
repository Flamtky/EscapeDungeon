package starter;

import contrib.components.InventoryComponent;
import contrib.components.StaminaComponent;
import contrib.item.Item;
import contrib.item.ItemSnapshot;
import core.Component;
import core.Entity;
import core.Game;
import core.components.InputComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.game.GameLoop;
import core.level.loader.DungeonLoader;
import core.network.handler.NettyNetworkHandler;
import core.network.messages.s2c.GameOverEvent;
import core.network.messages.s2c.LevelChangeEvent;
import core.network.server.*;
import core.utils.Point;
import demoDungeon.level.MADungeonRoom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Console command handler for MAServer.
 *
 * <p>Starts a background loop that reads commands from {@code System.in} and dispatches them to a
 * fixed registry of commands. All classes are package-private to keep the surface small; help text
 * is generated from the registry so adding commands does not require manual help updates.
 */
public final class ServerConsole {

  private static final String PROMPT = "MAServer> ";

  private ServerConsole() {}

  /** Starts the server console command loop in a background thread. */
  public static void start(NettyNetworkHandler handler) {
    CommandRegistry registry = new CommandRegistry(handler);
    Thread consoleThread =
        new Thread(
            () -> {
              Scanner scanner = new Scanner(System.in);
              System.out.print(PROMPT);
              while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                  boolean continueLoop = registry.dispatch(line);
                  if (!continueLoop) {
                    break;
                  }
                }
                System.out.print(PROMPT);
              }
              scanner.close();
            });
    consoleThread.setDaemon(true);
    consoleThread.setName("MAServer-Console");
    consoleThread.start();
  }
}

/** Registry and dispatcher for server console commands. */
final class CommandRegistry {
  private final List<ServerCommand> commands;
  private final Map<String, ServerCommand> commandLookup;
  private final CommandContext context;

  CommandRegistry(NettyNetworkHandler handler) {
    this.context = new CommandContext(handler);
    List<ServerCommand> cmds = new ArrayList<>();
    cmds.add(new HelpCommand(this));
    cmds.add(new ListClientsCommand());
    cmds.add(new ChangeLevelCommand());
    cmds.add(new ReloadLevelCommand());
    cmds.add(new CurrentLevelCommand());
    cmds.add(new GameOverCommand());
    cmds.add(new KickCommand());
    cmds.add(new StopCommand());
    cmds.add(new PrintTimingCommand());
    cmds.add(new ServerInfoCommand());
    cmds.add(new CheatCommand());
    cmds.add(new ClearCommand());
    this.commands = Collections.unmodifiableList(cmds);
    this.commandLookup =
        cmds.stream()
            .collect(Collectors.toMap(ServerCommand::name, c -> c, (a, b) -> a, HashMap::new));
    // add aliases
    for (ServerCommand cmd : cmds) {
      for (String alias : cmd.aliases()) {
        commandLookup.putIfAbsent(alias, cmd);
      }
    }
  }

  Collection<String> commandNamesAndAliases() {
    Set<String> names = new HashSet<>();
    for (ServerCommand cmd : commands) {
      names.add(cmd.name());
      names.addAll(cmd.aliases());
    }
    return names;
  }

  boolean dispatch(String line) {
    String[] parts = line.split("\\s+", 2);
    String cmdName = parts[0].toLowerCase();
    String arg = parts.length > 1 ? parts[1].trim() : "";
    ServerCommand cmd = commandLookup.get(cmdName);
    if (cmd == null) {
      System.out.printf("unknown command '%s'. Type 'help'.%n", cmdName);
      return true;
    }

    return cmd.execute(arg, context);
  }

  List<ServerCommand> commands() {
    return commands;
  }
}

/** Shared context for commands, providing access to runtime/transport. */
final class CommandContext {
  private final NettyNetworkHandler handler;

  CommandContext(NettyNetworkHandler handler) {
    this.handler = handler;
  }

  Optional<ServerTransport> transport() {
    Optional<ServerRuntime> runtime = handler.serverRuntime();
    return runtime.map(ServerRuntime::transport);
  }
}

/** Represents a server console command. */
interface ServerCommand {
  /** Primary command name (lowercase). */
  String name();

  /** Command aliases (lowercase); may be empty. */
  List<String> aliases();

  /** Short description for help. */
  String description();

  /** Executes the command. Return false to stop the console loop. */
  boolean execute(String arg, CommandContext ctx);
}

final class HelpCommand implements ServerCommand {
  private final CommandRegistry registry;

  HelpCommand(CommandRegistry registry) {
    this.registry = registry;
  }

  @Override
  public String name() {
    return "help";
  }

  @Override
  public List<String> aliases() {
    return List.of();
  }

  @Override
  public String description() {
    return "Show this help message";
  }

  @Override
  public boolean execute(String arg, CommandContext ctx) {
    System.out.println("Available commands:");
    for (ServerCommand cmd : registry.commands()) {
      String joinedAliases = String.join(",", cmd.aliases());
      String aliasPart = joinedAliases.isEmpty() ? "" : ", " + joinedAliases;
      System.out.printf(" %s%s - %s%n", cmd.name(), aliasPart, cmd.description());
    }
    return true;
  }
}

final class ListClientsCommand implements ServerCommand {
  @Override
  public String name() {
    return "list";
  }

  @Override
  public List<String> aliases() {
    return List.of("clients");
  }

  @Override
  public String description() {
    return "List connected clients";
  }

  @Override
  public boolean execute(String arg, CommandContext ctx) {
    Optional<ServerTransport> transportOpt = ctx.transport();
    if (transportOpt.isEmpty()) {
      System.out.println("transport not ready yet.");
      return true;
    }
    var clients = transportOpt.get().connectedClients();
    if (clients.isEmpty()) {
      System.out.println("No clients connected.");
      return true;
    }
    System.out.printf("%d client(s):%n", clients.size());
    clients.forEach(c -> System.out.printf(" %d %s%n", c.clientId(), c.username()));
    return true;
  }
}

final class ChangeLevelCommand implements ServerCommand {
  @Override
  public String name() {
    return "changelevel";
  }

  @Override
  public List<String> aliases() {
    return List.of();
  }

  @Override
  public String description() {
    return "Change to the given level";
  }

  @Override
  public boolean execute(String arg, CommandContext ctx) {
    if (arg.isBlank()) {
      System.out.println("Usage: changeLevel <levelName>");
      return true;
    }
    try {
      DungeonLoader.loadLevel(arg);
      GameLoop.onLevelLoad.execute();
      Game.network().broadcast(LevelChangeEvent.currentLevel(), true);
      System.out.printf("Changed level to '%s'.%n", arg);
    } catch (Exception e) {
      System.out.printf("Failed to change level: %s%n", e.getMessage());
    }
    return true;
  }
}

final class ReloadLevelCommand implements ServerCommand {
  @Override
  public String name() {
    return "reloadlevel";
  }

  @Override
  public List<String> aliases() {
    return List.of();
  }

  @Override
  public String description() {
    return "Reload the current level";
  }

  @Override
  public boolean execute(String arg, CommandContext ctx) {
    try {
      DungeonLoader.reloadCurrentLevel();
      GameLoop.onLevelLoad.execute();
      Game.network().broadcast(LevelChangeEvent.currentLevel(), true);
      System.out.println("Reloaded current level.");
    } catch (Exception e) {
      System.out.printf("Failed to reload level: %s%n", e.getMessage());
    }
    return true;
  }
}

final class CurrentLevelCommand implements ServerCommand {
  @Override
  public String name() {
    return "currentlevel";
  }

  @Override
  public List<String> aliases() {
    return List.of();
  }

  @Override
  public String description() {
    return "Prints the current level name";
  }

  @Override
  public boolean execute(String arg, CommandContext ctx) {
    String levelName = DungeonLoader.currentLevel();
    System.out.printf("Current level: %s%n", levelName);
    return true;
  }
}

final class GameOverCommand implements ServerCommand {
  @Override
  public String name() {
    return "gameover";
  }

  @Override
  public List<String> aliases() {
    return List.of();
  }

  @Override
  public String description() {
    return "Send game over to all clients";
  }

  @Override
  public boolean execute(String arg, CommandContext ctx) {
    try {
      Game.network().broadcast(new GameOverEvent("server_console"), true);
      System.out.println("Sent game over to all clients.");
    } catch (Exception e) {
      System.out.printf("Failed to send game over: %s%n", e.getMessage());
    }
    return true;
  }
}

final class KickCommand implements ServerCommand {
  @Override
  public String name() {
    return "kick";
  }

  @Override
  public List<String> aliases() {
    return List.of();
  }

  @Override
  public String description() {
    return "Kick a client by id";
  }

  @Override
  public boolean execute(String arg, CommandContext ctx) {
    if (arg.isBlank()) {
      System.out.println("Usage: kick <clientId>");
      return true;
    }
    short clientId;
    try {
      clientId = Short.parseShort(arg);
    } catch (NumberFormatException e) {
      System.out.println("Invalid client id. Use a number.");
      return true;
    }

    Optional<ServerTransport> transportOpt = ctx.transport();
    if (transportOpt.isEmpty()) {
      System.out.println("transport not ready yet.");
      return true;
    }

    ServerTransport transport = transportOpt.get();
    Session session =
        transport.connectedClients().stream()
            .filter(c -> c.clientId() == clientId)
            .findFirst()
            .flatMap(transport::sessionForClient)
            .orElse(null);
    if (session == null) {
      System.out.printf("No session found for client %d.%n", clientId);
      return true;
    }

    session.sendMessage(new GameOverEvent("kicked_by_server"), true);
    session.close();

    System.out.printf("Kicked client %d.%n", clientId);
    return true;
  }
}

final class StopCommand implements ServerCommand {
  @Override
  public String name() {
    return "stop";
  }

  @Override
  public List<String> aliases() {
    return List.of();
  }

  @Override
  public String description() {
    return "Stop the server";
  }

  @Override
  public boolean execute(String arg, CommandContext ctx) {
    try {
      Game.network().shutdown("console stop");
    } catch (Exception e) {
      System.out.printf("Error while shutting down network: %s%n", e.getMessage());
    }
    Game.exit("console stop");
    return false;
  }
}

final class PrintTimingCommand implements ServerCommand {
  private boolean enabled = AuthoritativeServerLoop.PRINT_TIMING;

  @Override
  public String name() {
    return "printtiming";
  }

  @Override
  public List<String> aliases() {
    return List.of("pt");
  }

  @Override
  public String description() {
    return "Toggle print of server timing information (Currently "
        + (enabled ? "enabled" : "disabled")
        + ")";
  }

  @Override
  public boolean execute(String arg, CommandContext ctx) {
    enabled = !enabled;
    AuthoritativeServerLoop.PRINT_TIMING = enabled;
    System.out.printf("Timing display %s.%n", enabled ? "enabled" : "disabled");
    return true;
  }
}

final class ServerInfoCommand implements ServerCommand {
  @Override
  public String name() {
    return "info";
  }

  @Override
  public List<String> aliases() {
    return List.of("status", "i");
  }

  @Override
  public String description() {
    return "Display server information";
  }

  @Override
  public boolean execute(String arg, CommandContext ctx) {
    Optional<ServerTransport> transportOpt = ctx.transport();
    if (transportOpt.isEmpty()) {
      System.out.println("transport not ready yet.");
      return true;
    }
    ServerTransport transport = transportOpt.get();
    System.out.printf("Connected: %b%n", Game.network().isConnected());
    System.out.printf("Connected clients: %d%n", transport.connectedClients().size());
    System.out.printf("Total sessions: %d%n", transport.clientIdToSessionMap().size());
    System.out.printf("Current level: %s%n", DungeonLoader.currentLevel());
    return true;
  }
}

final class CheatCommand implements ServerCommand {
  @Override
  public String name() {
    return "cheat";
  }

  @Override
  public List<String> aliases() {
    return List.of("c");
  }

  @Override
  public String description() {
    return "Execute a cheat command";
  }

  @Override
  public boolean execute(String arg, CommandContext ctx) {
    if (arg.isBlank()) {
      System.out.println("Usage: cheat <target> <type> [params]");
      System.out.println("Available cheat types:");
      for (CheatType type : CheatType.values()) {
        System.out.printf(" %s %s - %s%n", type.name(), type.params(), type.description());
      }
      return true;
    }
    String[] parts = arg.split("\\s+", 3);
    Integer target;
    try {
      target = parts.length > 0 ? Integer.valueOf(parts[0]) : null;
    } catch (NumberFormatException e) {
      System.out.printf("Invalid target entity id '%s'.%n", parts[0]);
      return true;
    }
    String typeStr = parts.length > 1 ? parts[1].toUpperCase() : "";
    CheatType type;
    try {
      type = CheatType.fromString(typeStr);
    } catch (IllegalArgumentException e) {
      System.out.printf("Unknown cheat type '%s'.%n", typeStr);
      return true;
    }
    String params = parts.length > 2 ? parts[2] : "";

    executeCheat(target, type, params, ctx);

    return true;
  }

  private void executeCheat(Integer targetId, CheatType type, String params, CommandContext ctx) {
    if (targetId == null) {
      System.out.println("Cheat command requires a valid target entity id.");
      return;
    }

    if (ctx.transport().isEmpty()) {
      System.out.println("Transport not available.");
      return;
    }

    Entity targetEntity =
        ctx.transport().get().connectedClients().stream()
            .filter(c -> c.clientId() == targetId)
            .findFirst()
            .flatMap(ClientState::playerEntity)
            .orElse(null);
    if (targetEntity == null) {
      System.out.println("Target not found.");
      return;
    }

    switch (type) {
      case TELEPORT:
        var targetParams = params.split("\\s+");
        if (targetParams.length != 2) {
          System.out.println("Usage for TELEPORT: cheat <target> TELEPORT <x> <y>");
          return;
        }
        Point targetPos;
        try {
          float x = Float.parseFloat(targetParams[0]);
          float y = Float.parseFloat(targetParams[1]);
          targetPos = new Point(x, y);
        } catch (NumberFormatException e) {
          System.out.println("Invalid coordinates for TELEPORT.");
          return;
        }
        targetEntity
            .fetch(PositionComponent.class)
            .ifPresentOrElse(
                posComp -> {
                  posComp.position(targetPos);
                  System.out.printf("Teleported %s to %s%n", targetId, targetPos);
                },
                () -> System.out.printf("PositionComponent not found on %s%n", targetId));
        break;
      case REMOVE_MODIFIERS:
        targetEntity
            .fetch(VelocityComponent.class)
            .ifPresentOrElse(
                velocityComponent -> {
                  velocityComponent.removeAllModifiers();
                  System.out.printf("Removed all velocity modifiers from %s%n", targetId);
                },
                () -> System.out.printf("VelocityComponent not found on %s%n", targetId));
        break;
      case GIVE_CONTROL:
        targetEntity
            .fetch(InputComponent.class)
            .ifPresentOrElse(
                inputComponent -> {
                  inputComponent.deactivateControls(false);
                  MADungeonRoom.addCallbacks(inputComponent);
                  System.out.printf("Gave control to %s%n", targetId);
                },
                () -> {
                  System.out.printf("InputComponent not found on %s%n", targetId);
                });
        break;
      case GIVE_ITEM:
        var itemName = params.trim();
        if (itemName.isEmpty()) {
          System.out.println("Usage for GIVE_ITEM: cheat <target> GIVE_ITEM <itemName>");
          return;
        }

        ItemSnapshot itemSnapshot = new ItemSnapshot(itemName, (byte) 1);
        Item item = itemSnapshot.toItem();
        if (item == null) {
          System.out.printf("Item '%s' not found.%n", itemName);
          return;
        }
        targetEntity
            .fetch(InventoryComponent.class)
            .ifPresent(
                inventoryComponent -> {
                  var result = inventoryComponent.add(item);
                  if (result) {
                    System.out.printf("Gave item '%s' to %s%n", itemName, targetId);
                  } else {
                    System.out.printf(
                        "Failed to give item '%s' to %s: Inventory full.%n", itemName, targetId);
                  }
                });
        break;
      case STAMINA:
        var staminaParams = params.split("\\s+");
        if (staminaParams.length < 1) {
          System.out.println("Usage for STAMINA: cheat <target> STAMINA <get/set> [value]");
          return;
        }
        String action = staminaParams[0].toLowerCase();
        if (action.equals("get")) {
          targetEntity
              .fetch(StaminaComponent.class)
              .ifPresentOrElse(
                  sc -> System.out.printf("Stamina of %s: %.2f%n", targetId, sc.currentAmount()),
                  () -> System.out.printf("StaminaComponent not found on %s%n", targetId));
        } else if (action.equals("set")) {
          if (staminaParams.length != 2) {
            System.out.println("Usage for STAMINA set: cheat <target> STAMINA set <value>");
            return;
          }
          float value;
          try {
            value = Float.parseFloat(staminaParams[1]);
          } catch (NumberFormatException e) {
            System.out.println("Invalid stamina value.");
            return;
          }
          targetEntity
              .fetch(StaminaComponent.class)
              .ifPresentOrElse(
                  sc -> {
                    sc.currentAmount(value);
                    System.out.printf("Set stamina of %s to %.2f%n", targetId, value);
                  },
                  () -> System.out.printf("StaminaComponent not found on %s%n", targetId));
        } else {
          System.out.println("Unknown action for STAMINA. Use 'get' or 'set'.");
        }
        break;
      case REMOVE:
        var componentName = params.trim();
        if (componentName.isEmpty()) {
          System.out.println("Usage for REMOVE: cheat <target> REMOVE <componentName>");
          return;
        }

        Class<? extends Component> componentClass;
        try {
          @SuppressWarnings("unchecked")
          Class<? extends Component> cast =
              (Class<? extends Component>) Class.forName(componentName);
          componentClass = cast;
        } catch (ClassNotFoundException e) {
          System.out.printf("Component class '%s' not found.%n", componentName);
          return;
        } catch (ClassCastException e) {
          System.out.printf("Class '%s' is not a Component.%n", componentName);
          return;
        }

        boolean removed = targetEntity.remove(componentClass);
        if (removed) {
          System.out.printf("Removed component '%s' from %s%n", componentName, targetId);
        } else {
          System.out.printf("Component '%s' not found on %s%n", componentName, targetId);
        }
        break;
    }
  }

  private enum CheatType {
    TELEPORT("Teleport to coordinates", "<x> <y>", List.of("tp")),
    REMOVE_MODIFIERS("Remove all velocity modifiers", "", List.of("rm")),
    GIVE_CONTROL("Give control back to player", "", List.of("gc")),
    GIVE_ITEM("Give an item to the player", "<itemName>", List.of("give")),
    STAMINA("Get/Set stamina", "<get/set> [value]", List.of("s")),
    REMOVE("Remove a given component from the entity", "<componentName>", List.of());

    private final String description;
    private final String params;
    private final List<String> aliases;

    CheatType(String description, String params, List<String> aliases) {
      this.description = description;
      this.params = params;
      this.aliases = aliases;
    }

    public String description() {
      return description;
    }

    public String params() {
      return params;
    }

    public List<String> aliases() {
      return aliases;
    }

    public static CheatType fromString(String name) {
      for (CheatType type : CheatType.values()) {
        if (type.name().equalsIgnoreCase(name) || type.aliases().contains(name.toLowerCase())) {
          return type;
        }
      }
      throw new IllegalArgumentException("No enum constant for name: " + name);
    }
  }
}

final class ClearCommand implements ServerCommand {
  private static final String ANSI_CLEAR = "\033[H\033[2J";
  private static final String FALLBACK_CLEAR;

  static {
    FALLBACK_CLEAR = "\n".repeat(80);
  }

  @Override
  public String name() {
    return "clear";
  }

  @Override
  public List<String> aliases() {
    return List.of("cls");
  }

  @Override
  public String description() {
    return "Clear the console screen";
  }

  @Override
  public boolean execute(String arg, CommandContext ctx) {
    try {
      System.out.print(ANSI_CLEAR);
      System.out.flush();
    } catch (Exception e) {
      System.out.print(FALLBACK_CLEAR);
    }
    return true;
  }
}
