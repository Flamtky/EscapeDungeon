package contrib.entities.deco;

import core.utils.Rectangle;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.animation.SpritesheetConfig;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/**
 * An enumeration of predefined decorative objects (Decos) that can be placed in the game world.
 *
 * <p>Each {@code Deco} entry defines its own sprite or spritesheet path, animation configuration,
 * and optional default collider and depth layer. Decorations are typically static or animated world
 * props such as trees, bushes, furniture, or architectural elements.
 *
 * <p>The configuration data provided by this enum is used by the {@link
 * contrib.entities.deco.DecoFactory} to instantiate corresponding decorative entities.
 */
public enum Deco {
  /** A decoration. */
  Tileset1(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig().spriteWidth(512).spriteHeight(384))
          .scaleX(384 / 16f)),
  /** A decoration. */
  Tileset2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig().spriteWidth(512).spriteHeight(512))),

  /** A decoration. */
  BookshelfLarge(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(0, 16 * 16, 1, 1, 32, 32)).scaleX(2),
      Vector2.of(2, 1)),
  /** A decoration. */
  Chains0("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(17 * 16, 5 * 16)),
  /** A decoration. */
  Chains1("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(18 * 16, 5 * 16)),
  /** A decoration. */
  Chains2("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(19 * 16, 5 * 16)),
  /** A decoration. */
  Chains3("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(20 * 16, 5 * 16)),
  /** A decoration. */
  Chains4("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(16 * 16, 6 * 16)),
  /** A decoration. */
  Chains5("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(17 * 16, 6 * 16)),
  /** A decoration. */
  Chains6("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(18 * 16, 6 * 16)),
  /** A decoration. */
  Chains7("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(19 * 16, 6 * 16)),
  /** A decoration. */
  Chains8("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(20 * 16, 6 * 16)),

  /** A decoration. */
  FloorBarsSquare("spritesheets/FG_Cellar.png", new SpritesheetConfig(16 * 16, 4 * 16)),
  /** A decoration. */
  FloorBarsRound("spritesheets/FG_Cellar.png", new SpritesheetConfig(17 * 16, 4 * 16)),
  /** A decoration. */
  FloorBarsSmall("spritesheets/FG_Cellar.png", new SpritesheetConfig(18 * 16, 4 * 16)),

  /** A decoration. */
  WindowBarred("spritesheets/FG_Cellar.png", new SpritesheetConfig(16 * 16, 3 * 16)),

  /** A decoration. */
  TreeBig(
      "objects/nature/big_tree_tile.png",
      new AnimationConfig().scaleX(3).scaleY(0),
      new Rectangle(2.10f, 1.50f, 0.55f, 0.60f)),

  /** A decoration. */
  TreeBigChainL(
      "objects/nature/big_trees_sheet.png",
      new AnimationConfig(new SpritesheetConfig(0, 0, 1, 1, 40, 64)).scaleX((float) 40 / 16),
      new Rectangle(1.75f, 1.5f, 0.75f, 0.6f)),
  /** A decoration. */
  TreeBigChainM(
      "objects/nature/big_trees_sheet.png",
      new AnimationConfig(new SpritesheetConfig(40, 0, 1, 1, 32, 64)).scaleX(2),
      new Rectangle(2.00f, 1.50f, 0.00f, 0.60f)),
  /** A decoration. */
  TreeBigChainR(
      "objects/nature/big_trees_sheet.png",
      new AnimationConfig(new SpritesheetConfig(40 + 32, 0, 1, 1, 40, 64)).scaleX((float) 40 / 16),
      new Rectangle(2.20f, 1.50f, 0.00f, 0.60f)),

  /** A decoration. */
  TreeMedium(
      "objects/nature/medium_tree.png",
      new AnimationConfig().scaleX(2),
      new Rectangle(0.8f, 1.2f, 0.6f, 0.2f)),
  /** A decoration. */
  TreeSmall(
      "objects/nature/small_arbust.png",
      new AnimationConfig(),
      new Rectangle(0.5f, 0.8f, 0.25f, 0.2f)),
  /** A decoration. */
  StumpSmall(
      "objects/nature/stump.png", new AnimationConfig(), new Rectangle(0.50f, 0.65f, 0.30f, 0.30f)),
  /** A decoration. */
  Reeds("objects/nature/reeds.png", new AnimationConfig()),

  /** A decoration. */
  Bush1("objects/nature/bushes_sheet.png", new SpritesheetConfig(0, 0)),
  /** A decoration. */
  Bush2("objects/nature/bushes_sheet.png", new SpritesheetConfig(16, 0)),
  /** A decoration. */
  Bush3("objects/nature/bushes_sheet.png", new SpritesheetConfig(0, 16)),
  /** A decoration. */
  Bush4("objects/nature/bushes_sheet.png", new SpritesheetConfig(16, 16)),
  /** A decoration. */
  ChairRed(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(6, 41, 1, 1, 11, 22)).scaleX(0.8f),
      new Rectangle(1.00f, 1.05f, -0.1f, 0.00f)),
  /** A decoration. */
  ChairYellow(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(19, 41, 1, 1, 11, 22)).scaleX(0.8f),
      new Rectangle(1.00f, 1.05f, -0.1f, 0.00f)),
  /** A decoration. */
  ChairGreen(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(32, 41, 1, 1, 11, 22)).scaleX(0.8f),
      new Rectangle(1.00f, 1.05f, -0.1f, 0.00f)),
  /** A decoration. */
  ChairBlue(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(45, 41, 1, 1, 11, 22)).scaleX(0.8f),
      new Rectangle(1.00f, 1.05f, -0.1f, 0.00f)),
  /** A decoration. */
  ChairWhite(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(58, 41, 1, 1, 11, 22)).scaleX(0.8f),
      new Rectangle(1.00f, 1.05f, -0.1f, 0.00f)),
  /** A decoration. */
  ChairGrey(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(71, 41, 1, 1, 11, 22)).scaleX(0.8f),
      new Rectangle(1.00f, 1.05f, -0.1f, 0.00f)),
  /** A decoration. */
  TableShort(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(85, 47, 1, 1, 26, 16)).scaleX(1.3f),
      new Rectangle(2.30f, 1.30f, -0.10f, 0.00f)),
  /** A decoration. */
  TableLong(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(115, 47, 1, 1, 40, 16)).scaleX(1.3f),
      new Rectangle(3.45f, 1.30f, -0.10f, 0.00f)),
  /** A decoration. */
  CouchWhite(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(119, 65, 1, 1, 33, 16)).scaleX(1.2f),
      new Rectangle(2.60f, 1.10f, -0.05f, 0.00f)),
  /** A decoration. */
  CouchBlue(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(120, 83, 1, 1, 33, 16)).scaleX(1.2f),
      new Rectangle(2.60f, 1.10f, -0.05f, 0.00f)),
  /** A decoration. */
  CouchGreen(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(120, 102, 1, 1, 33, 16)).scaleX(1.2f),
      new Rectangle(2.60f, 1.10f, -0.05f, 0.00f)),
  /** A decoration. */
  CouchOrange(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(120, 121, 1, 1, 33, 16)).scaleX(1.2f),
      new Rectangle(2.60f, 1.10f, -0.05f, 0.00f)),
  /** A decoration. */
  PottedTree(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(170, 65, 1, 1, 14, 19)).scaleX(1.2f),
      new Rectangle(1.20f, 0.85f, 0.00f, 0.00f)),
  /** A decoration. */
  TrashCanGreen(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(116, 143, 1, 1, 9, 14)).scaleX(0.7f),
      new Rectangle(0.90f, 0.85f, -0.10f, 0.00f)),
  /** A decoration. */
  TrashCanRed(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(126, 143, 1, 1, 9, 14)).scaleX(0.7f),
      new Rectangle(0.90f, 0.85f, -0.10f, 0.00f)),
  /** A decoration. */
  TrashCanBlue(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(136, 143, 1, 1, 9, 14)).scaleX(0.7f),
      new Rectangle(0.90f, 0.85f, -0.10f, 0.00f)),
  /** A decoration. */
  WaterDispenser(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(147, 140, 1, 1, 9, 17)).scaleX(0.7f),
      new Rectangle(0.90f, 0.85f, -0.10f, 0.00f)),
  /** A decoration. */
  SnackDispenser(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(159, 123, 1, 1, 24, 34)).scaleX(1.7f),
      new Rectangle(1.90f, 1.40f, -0.10f, 0.00f)),
  /** A decoration. */
  BookShelf(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(184, 126, 1, 1, 24, 31)).scaleX(1.7f),
      new Rectangle(1.90f, 1.40f, -0.10f, 0.00f)),
  /** A decoration. */
  FolderRed(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(211, 119, 1, 1, 11, 8)).scaleX(0.6f),
      new Rectangle(0.85f, 0.70f, 0.00f, 0.00f)),
  /** A decoration. */
  FolderBlue(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(211, 129, 1, 1, 11, 8)).scaleX(0.6f),
      new Rectangle(0.85f, 0.70f, 0.00f, 0.00f)),
  /** A decoration. */
  FolderGreen(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(211, 140, 1, 1, 11, 8)).scaleX(0.6f),
      new Rectangle(0.85f, 0.70f, 0.00f, 0.00f)),
  /** A decoration. */
  SheetBlank(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(211, 151, 1, 1, 10, 6)).scaleX(0.5f),
      new Rectangle(0.95f, 0.50f, -0.05f, 0.00f)),
  /** A decoration. */
  Clock(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(159, 108, 1, 1, 19, 6)).scaleX(0.5f),
      new Rectangle(1.60f, 0.50f, 0.00f, 0.00f)),
  /** A decoration. */
  SheetWritten1(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(183, 107, 1, 1, 6, 8)).scaleX(0.5f),
      new Rectangle(0.50f, 0.70f, 0.00f, 0.00f)),
  /** A decoration. */
  SheetWritten2(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(192, 107, 1, 1, 6, 9)).scaleX(0.5f),
      new Rectangle(0.50f, 0.75f, 0.00f, 0.00f)),
  /** A decoration. */
  Printer(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(200, 107, 1, 1, 9, 9)).scaleX(0.65f),
      new Rectangle(0.65f, 0.65f, 0.00f, 0.00f)),
  /** A decoration. */
  PCThick(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(233, 106, 1, 1, 15, 19)).scaleX(1.0f),
      new Rectangle(1.00f, 1.05f, 0.00f, 0.00f)),
  /** A decoration. */
  PCFlat(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(233, 128, 1, 1, 13, 23)).scaleX(1.0f),
      new Rectangle(1.00f, 1.45f, 0.00f, 0.00f)),
  /** A decoration. */
  CoffeeMachine(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(159, 91, 1, 1, 7, 11)).scaleX(0.55f),
      new Rectangle(0.55f, 0.65f, 0.00f, 0.00f)),
  /** A decoration. */
  Cup(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(169, 95, 1, 1, 8, 7)).scaleX(0.55f),
      new Rectangle(0.65f, 0.55f, 0.00f, 0.00f)),
  /** A decoration. */
  FlagIndia(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(179, 94, 1, 1, 12, 9)).scaleX(0.75f),
      new Rectangle(1.00f, 0.75f, 0.00f, 0.00f)),
  /** A decoration. */
  FlagUK(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(193, 94, 1, 1, 12, 9)).scaleX(0.75f),
      new Rectangle(1.00f, 0.75f, 0.00f, 0.00f)),
  /** A decoration. */
  FlagUSA(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(207, 94, 1, 1, 12, 9)).scaleX(0.75f),
      new Rectangle(1.00f, 0.75f, 0.00f, 0.00f)),
  /** A decoration. */
  Painting1(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(222, 94, 1, 1, 6, 8)).scaleX(0.5f),
      new Rectangle(0.50f, 0.65f, 0.00f, 0.00f)),
  /** A decoration. */
  Painting2(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(230, 94, 1, 1, 6, 8)).scaleX(0.5f),
      new Rectangle(0.50f, 0.65f, 0.00f, 0.00f)),
  /** A decoration. */
  Painting3(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(239, 94, 1, 1, 11, 8)).scaleX(0.5f),
      new Rectangle(0.70f, 0.50f, 0.00f, 0.00f)),
  /** A decoration. */
  Board(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(234, 81, 1, 1, 17, 11)).scaleX(0.9f),
      new Rectangle(1.40f, 0.90f, 0.00f, 0.00f)),
  /** A decoration. */
  Stickies(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(217, 107, 1, 1, 6, 8)).scaleX(0.5f),
      new Rectangle(0.50f, 0.65f, 0.00f, 0.00f)),
  /** A decoration. */
  ThinWallHorizontal(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(171, 44, 1, 1, 79, 17)).scaleX(1.3f),
      new Rectangle(6.05f, 0.50f, 0.00f, 0.00f)),
  /** A decoration. */
  ThinWallVertical(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(207, 63, 1, 1, 4, 27)).scaleX(0.37f),
      new Rectangle(0.40f, 1.45f, 0.00f, 0.00f)),
  /** A decoration. */
  Furniture(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(3, 68, 1, 1, 73, 24)).scaleX(2.15f)),
  /** A decoration. */
  WallWhite(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(84, 70, 1, 1, 26, 20)).scaleX(1.55f),
      new Rectangle(2.05f, 1.55f, 0.00f, 0.00f)),
  /** A decoration. */
  Desk1(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(188, 63, 1, 1, 17, 19)).scaleX(1.4f),
      new Rectangle(1.40f, 1.40f, 0.00f, 0.00f)),
  /** A decoration. */
  Desk2(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(213, 63, 1, 1, 17, 19)).scaleX(1.4f),
      new Rectangle(1.40f, 1.40f, 0.00f, 0.00f)),
  /** A decoration. */
  Window1(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(59, 96, 1, 1, 26, 21)).scaleX(1.55f),
      new Rectangle(1.95f, 1.55f, 0.00f, 0.00f)),
  /** A decoration. */
  Window2(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(88, 96, 1, 1, 26, 21)).scaleX(1.55f),
      new Rectangle(1.95f, 1.55f, 0.00f, 0.00f)),
  /** A decoration. */
  Elevator(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(98, 120, 1, 1, 16, 31)).scaleX(1.25f),
      new Rectangle(1.25f, 2.40f, 0.00f, 0.00f)),
  /** A decoration. */
  Cat(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(65, 129, 1, 1, 16, 14)).scaleX(1.1f),
      new Rectangle(0.95f, 0.65f, 0.15f, 0.00f)),
  /** A decoration. */
  Dog(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(59, 147, 1, 1, 24, 11)).scaleX(0.9f),
      new Rectangle(1.55f, 0.70f, 0.20f, 0.00f)),
  /** A decoration. */
  Sky(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(0, 0, 1, 1, 256, 38)).scaleX(2.7f)),
  /** A decoration. */
  Tom(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(2, 105, 1, 1, 15, 23)).scaleX(1.15f),
      new Rectangle(0.95f, 0.95f, 0.10f, 0.00f)),
  /** A decoration. */
  Joe(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(19, 104, 1, 1, 19, 24)).scaleX(1.15f),
      new Rectangle(0.95f, 0.95f, 0.10f, 0.00f)),
  /** A decoration. */
  Bill(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(40, 107, 1, 1, 13, 21)).scaleX(1.15f),
      new Rectangle(0.95f, 0.95f, 0.10f, 0.00f)),
  /** A decoration. */
  Sarah(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(3, 132, 1, 1, 17, 23)).scaleX(1.15f),
      new Rectangle(0.95f, 0.95f, 0.10f, 0.00f)),
  /** A decoration. */
  Laura(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(22, 132, 1, 1, 17, 23)).scaleX(1.15f),
      new Rectangle(0.95f, 0.95f, 0.10f, 0.00f)),
  /** A decoration. */
  BossAtDesk(
      "office/boss.png",
      new AnimationConfig(new SpritesheetConfig(13, 13, 1, 1, 51, 48)).scaleX(2.30f),
      new Rectangle(2.45f, 1.87f, 0.00f, 0.00f)),
  /** A decoration. */
  Worker1(
      "office/worker1.png",
      new AnimationConfig(new SpritesheetConfig(13, 13, 1, 1, 51, 48)).scaleX(2.30f),
      new Rectangle(1.80f, 1.70f, 0.00f, 0.00f)),
  /** A decoration. */
  Worker2(
      "office/worker2.png",
      new AnimationConfig(new SpritesheetConfig(13, 13, 1, 1, 51, 48)).scaleX(2.30f),
      new Rectangle(1.80f, 1.70f, 0.00f, 0.00f)),
  /** A decoration. */
  Worker3(
      "office/worker4.png",
      new AnimationConfig(new SpritesheetConfig(13, 13, 1, 1, 51, 48)).scaleX(2.30f),
      new Rectangle(1.80f, 1.70f, 0.00f, 0.00f)),
  /** A decoration. */
  Cabinet(
      "office/cabinet.png",
      new AnimationConfig(new SpritesheetConfig(18, 6, 1, 1, 25, 43)).scaleX(1.40f),
      new Rectangle(1.40f, 1.35f, 0.00f, 0.00f)),
  /** A decoration. */
  Chair(
      "office/chair.png",
      new AnimationConfig(new SpritesheetConfig(2, 0, 1, 1, 12, 16)).scaleX(0.55f),
      new Rectangle(0.55f, 0.45f, 0.00f, 0.00f)),
  /** A decoration. */
  CoffeeMaker(
      "office/coffee-maker.png",
      new AnimationConfig(new SpritesheetConfig(1, 8, 1, 1, 62, 43)).scaleX(2.35f),
      new Rectangle(3.35f, 1.20f, 0.00f, 0.00f)),
  /** A decoration. */
  Desk(
      "office/desk.png",
      new AnimationConfig(new SpritesheetConfig(13, 3, 1, 1, 38, 26)).scaleX(1.40f),
      new Rectangle(2.05f, 1.20f, 0.00f, 0.00f)),
  /** A decoration. */
  DeskWithPC1(
      "office/desk-with-pc.png",
      new AnimationConfig(new SpritesheetConfig(13, 13, 1, 1, 38, 35)).scaleX(1.95f),
      new Rectangle(2.15f, 1.30f, 0.00f, 0.00f)),
  /** A decoration. */
  DeskWithPC2(
      "office/Julia_PC.png",
      new AnimationConfig(new SpritesheetConfig(13, 13, 1, 1, 38, 41)).scaleX(2.05f),
      new Rectangle(2.05f, 1.55f, 0.00f, 0.00f)),
  /** A decoration. */
  WallCorner(
      "office/office-partitions-1.png",
      new AnimationConfig(new SpritesheetConfig(0, 4, 1, 1, 64, 60)).scaleX(3.20f),
      new Rectangle(3.40f, 2.20f, 0.00f, 0.00f)),
  /** A decoration. */
  WallVertical(
      "office/office-partitions-2.png",
      new AnimationConfig(new SpritesheetConfig(30, 4, 1, 1, 3, 60)).scaleX(0.16f),
      new Rectangle(0.15f, 2.15f, 0.00f, 0.00f)),
  /** A decoration. */
  PCOn(
      "office/PC1.png",
      new AnimationConfig(new SpritesheetConfig(4, 4, 1, 1, 25, 22)).scaleX(1.20f),
      new Rectangle(1.40f, 0.90f, 0.00f, 0.00f)),
  /** A decoration. */
  PCOff(
      "office/PC2.png",
      new AnimationConfig(new SpritesheetConfig(4, 4, 1, 1, 25, 22)).scaleX(1.20f),
      new Rectangle(1.40f, 0.90f, 0.00f, 0.00f)),
  /** A decoration. */
  Plant(
      "office/plant.png",
      new AnimationConfig(new SpritesheetConfig(9, 9, 1, 1, 11, 23)).scaleX(0.50f),
      new Rectangle(0.50f, 0.50f, 0.00f, 0.00f)),
  /** A decoration. */
  Printer1(
      "office/printer.png",
      new AnimationConfig(new SpritesheetConfig(2, 2, 1, 1, 29, 30)).scaleX(1.40f),
      new Rectangle(1.40f, 1.00f, 0.00f, 0.00f)),
  /** A decoration. */
  Printer2(
      "office/printer.png",
      new AnimationConfig(new SpritesheetConfig(34, 2, 1, 1, 29, 30)).scaleX(1.40f),
      new Rectangle(1.40f, 1.00f, 0.00f, 0.00f)),
  /** A decoration. */
  StampingTable(
      "office/stamping-table.png",
      new AnimationConfig(new SpritesheetConfig(5, 3, 1, 1, 46, 26)).scaleX(1.50f),
      new Rectangle(2.65f, 1.20f, 0.00f, 0.00f)),
  /** A decoration. */
  WritingTable(
      "office/writing-table.png",
      new AnimationConfig(new SpritesheetConfig(13, 13, 1, 1, 38, 36)).scaleX(2.10f),
      new Rectangle(2.25f, 1.80f, 0.00f, 0.00f)),
  /** A decoration. */
  TrashBin(
      "office/trash.png",
      new AnimationConfig(new SpritesheetConfig(3, 3, 1, 1, 9, 10)).scaleX(0.60f),
      new Rectangle(0.60f, 0.50f, 0.00f, 0.00f)),
  /** A decoration. */
  WaterCooler(
      "office/water-cooler.png",
      new AnimationConfig(new SpritesheetConfig(0, 4, 1, 1, 14, 27)).scaleX(0.70f),
      new Rectangle(0.70f, 0.70f, 0.00f, 0.00f)),
  /** A decoration. */
  Sink(
      "office/sink.png",
      new AnimationConfig(new SpritesheetConfig(15, 22, 1, 1, 34, 26)).scaleX(1.50f),
      new Rectangle(2.00f, 1.10f, 0.00f, 0.00f)),
  /** A decoration. */
  JuliaFront(
      "office/Julia.png",
      new AnimationConfig(new SpritesheetConfig(4, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaRight(
      "office/Julia.png",
      new AnimationConfig(new SpritesheetConfig(33, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaBack(
      "office/Julia.png",
      new AnimationConfig(new SpritesheetConfig(68, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaLeft(
      "office/Julia.png",
      new AnimationConfig(new SpritesheetConfig(103, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaDrinkingCoffee1(
      "office/Julia_Drinking_Coffee.png",
      new AnimationConfig(new SpritesheetConfig(4, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaDrinkingCoffee2(
      "office/Julia_Drinking_Coffee.png",
      new AnimationConfig(new SpritesheetConfig(36, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaDrinkingCoffee3(
      "office/Julia_Drinking_Coffee.png",
      new AnimationConfig(new SpritesheetConfig(68, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaIdle1(
      "office/Julia-Idle.png",
      new AnimationConfig(new SpritesheetConfig(4, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaIdle2(
      "office/Julia-Idle.png",
      new AnimationConfig(new SpritesheetConfig(36, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaIdle3(
      "office/Julia-Idle.png",
      new AnimationConfig(new SpritesheetConfig(68, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaIdle4(
      "office/Julia-Idle.png",
      new AnimationConfig(new SpritesheetConfig(100, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkForward1(
      "office/Julia_walk_Foward.png",
      new AnimationConfig(new SpritesheetConfig(20, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkForward2(
      "office/Julia_walk_Foward.png",
      new AnimationConfig(new SpritesheetConfig(84, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkForward3(
      "office/Julia_walk_Foward.png",
      new AnimationConfig(new SpritesheetConfig(148, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkForward4(
      "office/Julia_walk_Foward.png",
      new AnimationConfig(new SpritesheetConfig(212, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkLeft1(
      "office/Julia_walk_Left.png",
      new AnimationConfig(new SpritesheetConfig(18, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkLeft2(
      "office/Julia_walk_Left.png",
      new AnimationConfig(new SpritesheetConfig(82, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkLeft3(
      "office/Julia_walk_Left.png",
      new AnimationConfig(new SpritesheetConfig(146, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkLeft4(
      "office/Julia_walk_Left.png",
      new AnimationConfig(new SpritesheetConfig(210, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkRight1(
      "office/Julia_walk_Rigth.png",
      new AnimationConfig(new SpritesheetConfig(17, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkRight2(
      "office/Julia_walk_Rigth.png",
      new AnimationConfig(new SpritesheetConfig(81, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkRight3(
      "office/Julia_walk_Rigth.png",
      new AnimationConfig(new SpritesheetConfig(145, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkRight4(
      "office/Julia_walk_Rigth.png",
      new AnimationConfig(new SpritesheetConfig(209, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkUp1(
      "office/Julia_walk_Up.png",
      new AnimationConfig(new SpritesheetConfig(20, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkUp2(
      "office/Julia_walk_Up.png",
      new AnimationConfig(new SpritesheetConfig(84, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkUp3(
      "office/Julia_walk_Up.png",
      new AnimationConfig(new SpritesheetConfig(148, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkUp4(
      "office/Julia_walk_Up.png",
      new AnimationConfig(new SpritesheetConfig(212, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),

  /** A decoration. */
  LogBig(
      "objects/nature/big_log.png",
      new AnimationConfig().scaleX(2),
      new Rectangle(1.65f, 1.20f, 0.20f, 0.20f)),

  /** A decoration. */
  WallBackOrnamentBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(0, 0, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallCornerTopLeftOutsideBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(16, 0, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallTopOutsideBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(32, 0, 1, 1, 16, 16)),
      null,
      DepthLayer.Player.depth() + 1),
  /** A decoration. */
  WallCornerTopRightOutsideBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(48, 0, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallCornerTopLeftInsideBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(80, 0, 1, 1, 16, 32)),
      new Rectangle(1f, 2f, 0f, 0f)),
  /** A decoration. */
  WallFrontBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(96, 0, 1, 1, 16, 32)),
      new Rectangle(1f, 2f, 0f, 0f)),
  /** A decoration. */
  WallCornerTopRightInsideBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(112, 0, 1, 1, 16, 32)),
      new Rectangle(1f, 2f, 0f, 0f)),
  /** A decoration. */
  WallLeftBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(16, 16, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallRightBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(48, 16, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallFrontOrnamentBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(0, 32, 1, 1, 16, 32)),
      new Rectangle(1f, 2f, 0f, 0f)),
  /** A decoration. */
  WallCornerFrontLeftOutsideBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(16, 32, 1, 1, 16, 32)),
      new Rectangle(1f, 2f, 0f, 0f)),
  /** A decoration. */
  WallCornerFrontRightOutsideBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(48, 32, 1, 1, 16, 32)),
      new Rectangle(1f, 2f, 0f, 0f)),
  /** A decoration. */
  WallCornerFrontLeftInsideBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(80, 48, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallCornerFrontRightInsideBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(112, 48, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallFrontAngledUpBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(0, 64, 1, 1, 16, 48)),
      new Rectangle(1f, 2f, 0f, 1f)),
  /** A decoration. */
  WallFrontAngledDownBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(16, 64, 1, 1, 16, 48)),
      new Rectangle(1f, 2f, 0f, 1f)),
  /** A decoration. */
  WallCornerFrontRightAngledBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(64, 64, 1, 1, 16, 48)),
      new Rectangle(1f, 3f, 0f, 0f)),
  /** A decoration. */
  WallCornerFrontLeftAngledBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(80, 64, 1, 1, 16, 48)),
      new Rectangle(1f, 3f, 0f, 0f)),
  /** A decoration. */
  WallTopAngledDown1Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(32, 80, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallTopAngledUp1Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(48, 80, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallTopAngledDown2Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(32, 96, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallTopAngledUp2Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(48, 96, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallLeftArchBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(96, 64, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ArchMiddleBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(112, 64, 1, 1, 16, 16))),
  /** A decoration. */
  WallRightArchBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(128, 64, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  FloorTileArrowDownBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 0, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileArrowLeftBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(176, 0, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileArrowRightBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(192, 0, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileArrowUpBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(208, 0, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 16, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileCornerBottomLeftBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(176, 16, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileCornerTopLeftBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(192, 16, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileCornerTopRightBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(208, 16, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileCornerBottomRightBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(224, 16, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileCornerCenterBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(240, 16, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileArrowDownDarkBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 32, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileArrowLeftDarkBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(176, 32, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileArrowRightDarkBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(192, 32, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileArrowUpDarkBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(208, 32, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileDarkBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 48, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileCornerBottomLeftDarkBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(176, 48, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileCornerTopLeftDarkBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(192, 48, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileCornerTopRightDarkBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(208, 48, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileCornerBottomRightDarkBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(224, 48, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileCornerCenterDarkBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(240, 48, 1, 1, 16, 16))),
  /** A decoration. */
  HoleTileDryGrassBeige1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 64, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  HoleTileDryGrassBeige2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(176, 64, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  HoleTileDryGrassBeige3(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(192, 64, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  HoleTileDryGrassBeige4(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(208, 64, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  HoleTileDryGrassBeige5(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(224, 64, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  HoleTileDryGrassBeige6(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(240, 64, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ArchShadow(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(96, 80, 1, 1, 48, 16))),
  /** A decoration. */
  WallBackOrnamentGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(0, 112, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallCornerTopLeftOutsideGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(16, 112, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallTopOutsideGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(32, 112, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallCornerTopRightOutsideGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(48, 112, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallCornerTopLeftInsideGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(80, 112, 1, 1, 16, 32)),
      new Rectangle(1f, 2f, 0f, 0f)),
  /** A decoration. */
  WallFrontGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(96, 112, 1, 1, 16, 32)),
      new Rectangle(1f, 2f, 0f, 0f)),
  /** A decoration. */
  WallCornerTopRightInsideGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(112, 112, 1, 1, 16, 32)),
      new Rectangle(1f, 2f, 0f, 0f)),
  /** A decoration. */
  WallLeftGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(16, 128, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallRightGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(48, 128, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallFrontOrnamentGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(0, 144, 1, 1, 16, 32)),
      new Rectangle(1f, 2f, 0f, 0f)),
  /** A decoration. */
  WallCornerFrontLeftOutsideGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(16, 144, 1, 1, 16, 32)),
      new Rectangle(1f, 2f, 0f, 0f)),
  /** A decoration. */
  WallCornerFrontRightOutsideGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(48, 144, 1, 1, 16, 32)),
      new Rectangle(1f, 2f, 0f, 0f)),
  /** A decoration. */
  WallCornerFrontLeftInsideGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(80, 160, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallCornerFrontRightInsideGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(112, 160, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallFrontAngledUpGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(0, 176, 1, 1, 16, 48)),
      new Rectangle(1f, 2f, 0f, 1f)),
  /** A decoration. */
  WallFrontAngledDownGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(16, 176, 1, 1, 16, 48)),
      new Rectangle(1f, 2f, 0f, 1f)),
  /** A decoration. */
  WallCornerFrontRightAngledGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(32, 176, 1, 1, 16, 48)),
      new Rectangle(1f, 3f, 0f, 0f)),
  /** A decoration. */
  WallCornerFrontLeftAngledGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(48, 176, 1, 1, 16, 48)),
      new Rectangle(1f, 3f, 0f, 0f)),
  /** A decoration. */
  WallTopAngledDown1Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(0, 224, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallTopAngledUp1Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(16, 224, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallTopAngledDown2Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(0, 240, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallTopAngledUp2Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(16, 240, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallLeftArchGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(64, 192, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ArchMiddleGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(80, 192, 1, 1, 16, 16))),
  /** A decoration. */
  WallRightArchGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(96, 192, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  FloorTileArrowDownGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 128, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileArrowLeftGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(176, 128, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileArrowRightGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(192, 128, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileArrowUpGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(208, 128, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 144, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileCornerBottomLeftGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(176, 144, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileCornerTopLeftGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(192, 144, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileCornerTopRightGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(208, 144, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileCornerBottomRightGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(224, 144, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileCornerCenterGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(240, 144, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileArrowDownDarkGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 160, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileArrowLeftDarkGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(176, 160, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileArrowRightDarkGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(192, 160, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileArrowUpDarkGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(208, 160, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileDarkGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 176, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileCornerBottomLeftDarkGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(176, 176, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileCornerTopLeftDarkGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(192, 176, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileCornerTopRightDarkGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(208, 176, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileCornerBottomRightDarkGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(224, 176, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileCornerCenterDarkGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(240, 176, 1, 1, 16, 16))),
  /** A decoration. */
  HoleTileDryGrassGray1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 192, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  HoleTileDryGrassGray2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(176, 192, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  HoleTileDryGrassGray3(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(192, 192, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  HoleTileDryGrassGray4(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(208, 192, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  HoleTileDryGrassGray5(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(224, 192, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  HoleTileDryGrassGray6(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(240, 192, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  FloorTileDropBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 208, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileDropCrossBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(208, 208, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileDropCornerTopLeftBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(224, 208, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileDropCornerBottomLeftBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(208, 208, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileDropCornerBottomRightBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(224, 208, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileDropCornerTopRightBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(240, 208, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileDropGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 224, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileDropCrossGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(208, 224, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileDropCornerTopLeftGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(224, 224, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileDropCornerBottomLeftGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(208, 224, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileDropCornerBottomRightGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(224, 224, 1, 1, 16, 16))),
  /** A decoration. */
  FloorTileDropCornerTopRightGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(240, 224, 1, 1, 16, 16))),
  /** A decoration. */
  Pillar1GrayWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(0, 256, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Pillar2GrayWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(16, 256, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Pillar3GrayWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(32, 256, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform1GrayWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(48, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform2Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(64, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform3GrayWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(48, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament1GrayWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(64, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament2GrayWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(80, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament3GrayWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(96, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform4GrayWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(0, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform5GrayWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(32, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform6GrayWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(48, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament1GlowingGrayWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(64, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament2GlowingGrayWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(80, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament3GlowingGrayWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(96, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Pillar1BeigeWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(0, 304, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Pillar2BeigeWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(16, 304, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Pillar3BeigeWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(32, 304, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform1BeigeWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(48, 304, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform2Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(64, 304, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform3BeigeWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(48, 320, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament1BeigeWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(64, 320, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament2BeigeWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(80, 320, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament3BeigeWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(96, 320, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform4BeigeWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(0, 336, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform5BeigeWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(32, 336, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform6BeigeWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(48, 336, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament1GlowingBeigeWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(64, 336, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament2GlowingBeigeWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(80, 336, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament3GlowingBeigeWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(96, 336, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform7GrayWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(0, 352, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform8GrayWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(16, 352, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform7BeigeWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(32, 352, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform8BeigeWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(48, 352, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Vase1Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(112, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Vase2Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(128, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Vase3Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(144, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Vase4Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Chest1ClosedWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(176, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Chest1OpenWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(192, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Chest2ClosedWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(208, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Chest2OpenWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(224, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairLeftDamaged1Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(112, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairRightDamaged1Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(128, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairTopDamaged1Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(144, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairBottomDamaged1Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk1Damaged1Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(176, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk2Damaged1Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(192, 272, 1, 1, 32, 16)),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk3Damaged1Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(224, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairLeftWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(112, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairRightWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(128, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairTopWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(144, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairBottomWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk1Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(176, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk2Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(192, 288, 1, 1, 32, 16)),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk3Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(224, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairLeftDamaged2Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(112, 304, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairRightDamaged2Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(128, 304, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairTopDamaged2Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(144, 304, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairBottomDamaged2Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 304, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk1Damaged2Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(176, 304, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk2Damaged2Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(192, 304, 1, 1, 32, 16)),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk3Damaged2Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(224, 304, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box1Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(128, 320, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box2Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(144, 320, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box5StackWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 320, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box4StackWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(176, 320, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box3StackWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(192, 320, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box1Broken1Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(224, 320, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box1Broken2Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(240, 320, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box3Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(112, 336, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box4Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(128, 336, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box5Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(144, 336, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box1Broken3Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(224, 336, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box1Broken4Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(112, 336, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Vase1SetWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(80, 352, 1, 1, 32, 16)),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Box3PyramidWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(112, 352, 1, 1, 32, 32)).scaleX(2f).scaleY(2f),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Box4PyramidWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(144, 352, 1, 1, 32, 32)).scaleX(2f).scaleY(2f),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Box5PyramidWater(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(176, 352, 1, 1, 32, 32)).scaleX(2f).scaleY(2f),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Box2Broken1Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(208, 352, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box2Broken2Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(224, 352, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box2Broken3Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(208, 368, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box2Broken4Water(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(224, 368, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  TorchGrayAnimated(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(0, 416, 1, 4, 16, 16)).framesPerSprite(10)),
  /** A decoration. */
  TorchBeigeAnimated(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(80, 416, 1, 4, 16, 16)).framesPerSprite(10)),
  /** A decoration. */
  FireboxGrayAnimated(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(0, 432, 1, 4, 16, 16)).framesPerSprite(10)),
  /** A decoration. */
  FireboxBeigeAnimated(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(80, 432, 1, 4, 16, 16)).framesPerSprite(10)),
  /** A decoration. */
  SpikeSingleGrayAnimated(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(0, 448, 1, 4, 16, 16)).framesPerSprite(10)),
  /** A decoration. */
  SpikeTriple1GrayAnimated(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(0, 464, 1, 4, 16, 16)).framesPerSprite(10)),
  /** A decoration. */
  SpikeTriple2GrayAnimated(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(0, 480, 1, 4, 16, 16)).framesPerSprite(10)),
  /** A decoration. */
  SpikeSingleBeigeAnimated(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(80, 448, 1, 4, 16, 16)).framesPerSprite(10)),
  /** A decoration. */
  SpikeTriple1BeigeAnimated(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(80, 464, 1, 4, 16, 16)).framesPerSprite(10)),
  /** A decoration. */
  SpikeTriple2BeigeAnimated(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(80, 480, 1, 4, 16, 16)).framesPerSprite(10)),
  /** A decoration. */
  PressurePlate1GrayAnimated(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 416, 1, 3, 16, 16)).framesPerSprite(10)),
  /** A decoration. */
  PressurePlate1BeigeAnimated(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 432, 1, 3, 16, 16)).framesPerSprite(10)),
  /** A decoration. */
  PressurePlate2GrayAnimated(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 448, 1, 3, 16, 16)).framesPerSprite(10)),
  /** A decoration. */
  PressurePlate2BeigeAnimated(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(160, 464, 1, 3, 16, 16)).framesPerSprite(10)),
  /** A decoration. */
  BannerRed1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 0, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BannerRed2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(272, 0, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BannerRed1Damaged(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(288, 0, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BannerRed2Damaged(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(304, 0, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BannerRedLarge(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 0, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BannerRedLargeDamaged(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(464, 0, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BannerBlackLarge(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(480, 0, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BannerBlackLargeDamaged(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(496, 0, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BannerBlack1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 16, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BannerBlack2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(272, 16, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BannerBlack1Damaged(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(288, 16, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BannerBlack2Damaged(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(304, 16, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Window1Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 32, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Window2Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(272, 32, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Window3Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(288, 32, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Window4TopGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(304, 32, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Window4MiddleGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(320, 32, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Window5Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 48, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Window4BottomGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(304, 48, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Drain1Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 64, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Drain2Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(272, 64, 1, 1, 16, 64)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Drain3Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(288, 64, 1, 1, 16, 64)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Drain4Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(304, 64, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Drain5Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(320, 64, 1, 1, 16, 64)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Window1Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 80, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Window2Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(272, 80, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Window3Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(288, 80, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Window4TopBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(304, 80, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Window4MiddleBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(320, 80, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Window5Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 96, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Window4BottomBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(304, 96, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Drain1Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 112, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Drain2Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(272, 112, 1, 1, 16, 64)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Drain3Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(288, 112, 1, 1, 16, 64)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Drain4Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(304, 112, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Drain5Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(320, 112, 1, 1, 16, 64)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Pillar4Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(336, 32, 1, 1, 16, 48)),
      new Rectangle(1f, 1f, 0f, 0.5f)),
  /** A decoration. */
  Pillar1Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 32, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Pillar5Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(368, 32, 1, 1, 16, 48)),
      new Rectangle(1f, 1f, 0f, 0.5f)),
  /** A decoration. */
  Pillar2Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(384, 32, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Pillar3Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(400, 32, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform1Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 32, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform3Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 48, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament1Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 48, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament2Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 48, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament3Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(464, 48, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform4Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 64, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform5Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(400, 64, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform6Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 64, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament1GlowingGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 64, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament2GlowingGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 64, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament3GlowingGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(464, 64, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Pillar4Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(336, 80, 1, 1, 16, 48)),
      new Rectangle(1f, 1f, 0f, 0.5f)),
  /** A decoration. */
  Pillar1Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 80, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Pillar5Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(368, 80, 1, 1, 16, 48)),
      new Rectangle(1f, 1f, 0f, 0.5f)),
  /** A decoration. */
  Pillar2Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(384, 80, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Pillar3Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(400, 80, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform1Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 80, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform3Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 96, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament1Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 96, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament2Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 96, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament3Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(464, 96, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform4Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 112, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform5Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(400, 112, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform6Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 112, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament1GlowingBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 112, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament2GlowingBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 112, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Ornament3GlowingBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(464, 112, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform7Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 128, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform8Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(384, 128, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform7Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(400, 128, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Plattform8Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 128, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  TorchOff1Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 32, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  TorchOff2Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 32, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  FireboxOff1Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 48, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  FireboxOff2Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 48, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SpikeSingle1EmptyGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 64, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SpikeTripple1EmptyGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 64, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  TorchOff1Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 80, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  TorchOff2Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 80, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  FireboxOff1Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 96, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  FireboxOff2Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 96, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SpikeSingle1EmptyBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 112, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SpikeTripple1EmptyBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 112, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SpikeSingle2EmptyGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 144, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SpikeTripple2EmptyGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(368, 144, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SpikeTripple3EmptyGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 160, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SpikeTripple2EmptyBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(368, 160, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SpikeSingle2EmptyBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 176, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SpikeTripple3EmptyBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(368, 176, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Vase1Set(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 128, 1, 1, 32, 16)),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Vase1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(384, 128, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Vase2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(400, 128, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Vase3(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 128, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Vase4(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 128, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Chest1Closed(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 128, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Chest1Open(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(464, 128, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Chest2Closed(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(480, 128, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Chest2Open(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(496, 128, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairLeftDamaged1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(384, 144, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairRightDamaged1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(400, 144, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairTopDamaged1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 144, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairBottomDamaged1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 144, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk1Damaged1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 144, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk2Damaged1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(464, 144, 1, 1, 32, 16)),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk3Damaged1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(496, 144, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairLeft(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(384, 160, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairRight(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(400, 160, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairTop(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 160, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairBottom(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 160, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk1Flat(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 160, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk2Flat(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(464, 160, 1, 1, 32, 16)),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk3Flat(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(496, 160, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairLeftDamaged2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(384, 176, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairRightDamaged2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(400, 176, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairTopDamaged2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 176, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  ChairBottomDamaged2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 176, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk1Damaged2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 176, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk2Damaged2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(464, 176, 1, 1, 32, 16)),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk3Damaged2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(496, 176, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(400, 192, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 192, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box5Stack(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 192, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box4Stack(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 192, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box3Stack(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(464, 192, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box1Broken1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(496, 192, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box1Broken2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(240, 192, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box3(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(384, 208, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box4(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(400, 208, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box5(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 208, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box1Broken3(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(496, 208, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box1Broken4(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(384, 208, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box3Pyramid(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(384, 224, 1, 1, 32, 32)).scaleX(2f).scaleY(2f),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Box4Pyramid(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 224, 1, 1, 32, 32)).scaleX(2f).scaleY(2f),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Box5Pyramid(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 224, 1, 1, 32, 32)).scaleX(2f).scaleY(2f),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Box2Broken1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(480, 224, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box2Broken2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(496, 224, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box2Broken3(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(480, 240, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box2Broken4(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(496, 240, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallDecor1Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 128, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallDecor2Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(272, 128, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallDecor3Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(288, 128, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallDecor1Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(304, 128, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallDecor2Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(320, 128, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WallDecor3Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(336, 128, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Doorway1Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 160, 1, 1, 48, 32)).scaleX(2f).scaleY(2f),
      new Rectangle(3f, 2f, 0f, 0f)),
  /** A decoration. */
  Doorway1Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(304, 160, 1, 1, 48, 32)).scaleX(2f).scaleY(2f),
      new Rectangle(3f, 2f, 0f, 0f)),
  /** A decoration. */
  Doorway2Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 192, 1, 1, 48, 32)).scaleX(2f).scaleY(2f),
      new Rectangle(3f, 2f, 0f, 0f)),
  /** A decoration. */
  Doorway2Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(304, 192, 1, 1, 48, 32)).scaleX(2f).scaleY(2f),
      new Rectangle(3f, 2f, 0f, 0f)),
  /** A decoration. */
  DoorGreen1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 192, 1, 1, 16, 32)),
      new Rectangle(1f, 2f, 0f, 0f)),
  /** A decoration. */
  DoorGreen2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(368, 192, 1, 1, 16, 32)),
      new Rectangle(1f, 2f, 0f, 0f)),
  /** A decoration. */
  Doorway3Gray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 224, 1, 1, 48, 32)).scaleX(2f).scaleY(2f),
      new Rectangle(3f, 2f, 0f, 0f)),
  /** A decoration. */
  Doorway3Beige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(304, 224, 1, 1, 48, 32)).scaleX(2f).scaleY(2f),
      new Rectangle(3f, 2f, 0f, 0f)),
  /** A decoration. */
  DoorBrown1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 224, 1, 1, 16, 32)),
      new Rectangle(1f, 2f, 0f, 0f)),
  /** A decoration. */
  DoorBrown2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(368, 224, 1, 1, 16, 32)),
      new Rectangle(1f, 2f, 0f, 0f)),

  /** A decoration. */
  WaterDeep(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(272, 320, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepTopLeftInnerRoundGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepBackRoundGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(272, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepTopRightInnerRoundGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(288, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepSide1RoundGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepSide2RoundGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(288, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepBottomLeftInnerRoundGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepFrontRoundGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(272, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepBottomRightInnerRoundGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(288, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepTopLeftOuterRoundGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 304, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepTopRightOuterRoundGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(288, 304, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepBottomLeftOuterRoundGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 336, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepBottomRightOuterRoundGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(288, 336, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepTopLeftInnerStraightGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(320, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepBackStraightGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(336, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepTopRightInnerStraightGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepSide1StraightGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(320, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepSide2StraightGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepBottomLeftInnerStraightGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(320, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepFrontStraightGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(336, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepBottomRightInnerStraightGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepTopLeftOuterStraightGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(320, 304, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepTopRightOuterStraightGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 304, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepBottomLeftOuterStraightGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(320, 336, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepBottomRightOuterStraightGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 336, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepTopLeftInnerRoundBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 368, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepBackRoundBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(272, 368, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepTopRightInnerRoundBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(288, 368, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepSide1RoundBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 384, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepSide2RoundBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(288, 384, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepBottomLeftInnerRoundBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 400, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepFrontRoundBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(272, 400, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepBottomRightInnerRoundBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(288, 400, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepTopLeftOuterRoundBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 416, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepTopRightOuterRoundBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(288, 416, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepBottomLeftOuterRoundBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 448, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepBottomRightOuterRoundBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(288, 448, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepTopLeftInnerStraightBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(320, 368, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepBackStraightBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(336, 368, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepTopRightInnerStraightBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 368, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepSide1StraightBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(320, 384, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepSide2StraightBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 384, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepBottomLeftInnerStraightBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(320, 400, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepFrontStraightBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(336, 400, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepBottomRightInnerStraightBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 400, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepTopLeftOuterStraightBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(320, 416, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepTopRightOuterStraightBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 416, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepBottomLeftOuterStraightBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(320, 448, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterDeepBottomRightOuterStraightBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(352, 448, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHigh(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(400, 320, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHighTopLeftInnerRound(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(384, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHighBackRound(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(400, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHighTopRightInnerRound(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHighSide1Round(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(384, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHighSide2Round(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHighBottomLeftInnerRound(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(384, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHighFrontRound(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(400, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHighBottomRightInnerRound(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHighTopLeftOuterRound(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(384, 304, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHighTopRightOuterRound(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 304, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHighBottomLeftOuterRound(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(384, 336, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHighBottomRightOuterRound(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 336, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHighAngledUpFront1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHighAngledUpFront2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHighAngledDownFront1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(480, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHighAngledDownFront2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(480, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHighAngledUpBack1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 320, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHighAngledUpBack2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 304, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHighAngledDownBack1(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(480, 320, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  WaterHighAngledDownBack2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(480, 304, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeHorizontalGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(272, 464, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeHorizontalBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(336, 464, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeSmallTopBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(384, 464, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeHorizontalTopLeftBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(400, 464, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeHorizontalTopBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 464, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeHorizontalTopRightBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 464, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeSmallTopGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 464, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeHorizontalTopLeftGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(464, 464, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeHorizontalTopGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(480, 464, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeHorizontalTopRightGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(496, 464, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeVerticalGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(256, 480, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeMiddleGray(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(272, 480, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeVerticalBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(320, 480, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeMiddleBeige(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(336, 480, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeSmallMiddleBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(384, 480, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeHorizontalLeftBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(400, 480, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeHorizontalMiddleBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 480, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeHorizontalRightBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 480, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeSmallMiddleGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 480, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeHorizontalLeftGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(464, 480, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeHorizontalMiddleGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(480, 480, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeHorizontalRightGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(496, 480, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeSmallBottomBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(384, 496, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeHorizontalLeftBottomBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(400, 496, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeHorizontalBottomBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(416, 496, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeHorizontalRightBottomBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 496, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeSmallBottomGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 496, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeHorizontalLeftBottomGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(464, 496, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeHorizontalBottomGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(480, 496, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeHorizontalRightBottomGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(496, 496, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeVerticalTopLeftGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 368, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeVerticalTopGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 368, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeVerticalTopRightGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(464, 368, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeVerticalLeftGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 384, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeVerticalMiddleGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 384, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeVerticalRightGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(464, 384, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeVerticalBottomLeftGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 400, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeVerticalBottomGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 400, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeVerticalBottomRightGreen(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(464, 400, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeVerticalTopLeftBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 416, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeVerticalTopBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 416, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeVerticalTopRightBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(464, 416, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeVerticalLeftBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 432, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeVerticalMiddleBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 432, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeVerticalRightBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(464, 432, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeVerticalBottomLeftBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(432, 448, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeVerticalBottomBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(448, 448, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BridgeVerticalBottomRightBrown(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig(464, 448, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  DoorGrayGray1Animated(
      "spritesheets/FG_Cellar_Doors.png",
      new AnimationConfig(new SpritesheetConfig(16, 16, 1, 4, 16, 32)).framesPerSprite(3)),
  /** A decoration. */
  DoorGrayGray2Animated(
      "spritesheets/FG_Cellar_Doors.png",
      new AnimationConfig(new SpritesheetConfig(96, 16, 1, 4, 16, 32)).framesPerSprite(3)),
  /** A decoration. */
  DoorGrayGray3Animated(
      "spritesheets/FG_Cellar_Doors.png",
      new AnimationConfig(new SpritesheetConfig(176, 16, 1, 4, 16, 32)).framesPerSprite(3)),
  /** A decoration. */
  DoorGrayBeige1Animated(
      "spritesheets/FG_Cellar_Doors.png",
      new AnimationConfig(new SpritesheetConfig(16, 48, 1, 4, 16, 32)).framesPerSprite(3)),
  /** A decoration. */
  DoorGrayBeige2Animated(
      "spritesheets/FG_Cellar_Doors.png",
      new AnimationConfig(new SpritesheetConfig(96, 48, 1, 4, 16, 32)).framesPerSprite(3)),
  /** A decoration. */
  DoorGrayBeige3Animated(
      "spritesheets/FG_Cellar_Doors.png",
      new AnimationConfig(new SpritesheetConfig(176, 48, 1, 4, 16, 32)).framesPerSprite(3)),
  /** A decoration. */
  DoorBeigeGray1Animated(
      "spritesheets/FG_Cellar_Doors.png",
      new AnimationConfig(new SpritesheetConfig(16, 80, 1, 4, 16, 32)).framesPerSprite(3)),
  /** A decoration. */
  DoorBeigeGray2Animated(
      "spritesheets/FG_Cellar_Doors.png",
      new AnimationConfig(new SpritesheetConfig(96, 80, 1, 4, 16, 32)).framesPerSprite(3)),
  /** A decoration. */
  DoorBeigeGray3Animated(
      "spritesheets/FG_Cellar_Doors.png",
      new AnimationConfig(new SpritesheetConfig(176, 80, 1, 4, 16, 32)).framesPerSprite(3)),
  /** A decoration. */
  DoorBeigeBeige1Animated(
      "spritesheets/FG_Cellar_Doors.png",
      new AnimationConfig(new SpritesheetConfig(16, 112, 1, 4, 16, 32)).framesPerSprite(3)),
  /** A decoration. */
  DoorBeigeBeige2Animated(
      "spritesheets/FG_Cellar_Doors.png",
      new AnimationConfig(new SpritesheetConfig(96, 112, 1, 4, 16, 32)).framesPerSprite(3)),
  /** A decoration. */
  DoorBeigeBeige3Animated(
      "spritesheets/FG_Cellar_Doors.png",
      new AnimationConfig(new SpritesheetConfig(176, 112, 1, 4, 16, 32)).framesPerSprite(3)),
  /** A decoration. */
  DoorGreen1Animated(
      "spritesheets/FG_Cellar_Doors.png",
      new AnimationConfig(new SpritesheetConfig(16, 144, 1, 4, 16, 32)).framesPerSprite(3)),
  /** A decoration. */
  DoorGreen2Animated(
      "spritesheets/FG_Cellar_Doors.png",
      new AnimationConfig(new SpritesheetConfig(96, 144, 1, 4, 16, 32)).framesPerSprite(3)),
  /** A decoration. */
  DoorBrown1Animated(
      "spritesheets/FG_Cellar_Doors.png",
      new AnimationConfig(new SpritesheetConfig(16, 176, 1, 4, 16, 32)).framesPerSprite(3)),
  /** A decoration. */
  DoorBrown2Animated(
      "spritesheets/FG_Cellar_Doors.png",
      new AnimationConfig(new SpritesheetConfig(96, 176, 1, 4, 16, 32)).framesPerSprite(3)),
  /** A decoration. */
  Bookshelf1(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(0, 256, 1, 1, 32, 32)).scaleX(2).scaleY(2),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Bookshelf2(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(32, 256, 1, 1, 32, 32)).scaleX(2).scaleY(2),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Bookshelf3(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(64, 256, 1, 1, 32, 32)).scaleX(2).scaleY(2),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Bookshelf4(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(96, 256, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Bookshelf5(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(112, 256, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Bookshelf6(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(128, 256, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Bookshelf7(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(144, 256, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SwordStand1(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(160, 256, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SwordStand2(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(176, 256, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SwordStand3(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(192, 256, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SwordStand2Broken(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(224, 256, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BallGreen(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(256, 256, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  PillarDarkGrayBlue(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(272, 256, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  PillarDarkGrayRed(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(288, 256, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SocketDarkGrayBlue(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(304, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BallRed(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(320, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  BallBlue(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(384, 256, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SwordStand4(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(208, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SwordStand4Broken(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(240, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SocketDarkGrayRed(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(304, 272, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Bookshelf1Broken(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(0, 288, 1, 1, 32, 32)).scaleX(2).scaleY(2),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Bookshelf2Broken(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(32, 288, 1, 1, 32, 32)).scaleX(2).scaleY(2),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Bookshelf3Broken(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(64, 288, 1, 1, 32, 32)).scaleX(2).scaleY(2),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Bookshelf4Broken(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(96, 288, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Bookshelf5Broken(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(112, 288, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Bookshelf6Broken(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(128, 288, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Bookshelf7Broken(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(144, 288, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SwordStand5(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(160, 288, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SwordStand6(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(176, 288, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SwordStand7(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(192, 288, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SwordStand7Broken(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(224, 288, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  PillarDarkGrayEmpty(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(272, 288, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  PillarDarkGrayGreen(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(288, 288, 1, 1, 16, 32)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SocketDarkGrayGreen(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(304, 288, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SwordStand8(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(208, 304, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SwordStand8Broken(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(240, 304, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  SocketDarkGrayEmpty(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(304, 304, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box6Pyramid(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(0, 320, 1, 1, 32, 32)).scaleX(2).scaleY(2),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Box6BrokenPyramid(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(32, 320, 1, 1, 32, 32)).scaleX(2).scaleY(2),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Box7Pyramid(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(64, 320, 1, 1, 32, 32)).scaleX(2).scaleY(2),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Box7BrokenPyramid(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(96, 320, 1, 1, 32, 32)).scaleX(2).scaleY(2),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Box8OpenPyramid(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(128, 320, 1, 1, 32, 32)).scaleX(2).scaleY(2),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Box8BrokenPyramid(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(160, 320, 1, 1, 32, 32)).scaleX(2).scaleY(2),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk2x2(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(192, 320, 1, 1, 32, 32)).scaleX(2).scaleY(2),
      new Rectangle(2f, 2f, 0f, 0f)),
  /** A decoration. */
  Desk2x2Broken(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(224, 320, 1, 1, 32, 32)).scaleX(2).scaleY(2),
      new Rectangle(2f, 2f, 0f, 0f)),
  /** A decoration. */
  Box6OpenPyramid(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(0, 352, 1, 1, 32, 32)).scaleX(2).scaleY(2),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Box6Broken1(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(32, 352, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box6Broken2(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(48, 352, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box7OpenPyramid(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(64, 352, 1, 1, 32, 32)).scaleX(2).scaleY(2),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Box7Broken1(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(96, 352, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box7Broken2(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(112, 352, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box8Pyramid(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(128, 352, 1, 1, 32, 32)).scaleX(2).scaleY(2),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Box8Broken1(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(160, 352, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box8Broken2(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(176, 352, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk2x1(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(192, 352, 1, 1, 32, 16)),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk1x1(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(224, 352, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  DeskSmall(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(240, 352, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box6Open(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(32, 368, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box6(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(48, 368, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box7Open(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(96, 368, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box7(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(112, 368, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box8(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(160, 368, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Box8Open(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(176, 368, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk2x1Broken(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(192, 368, 1, 1, 32, 16)),
      new Rectangle(2f, 1f, 0f, 0f)),
  /** A decoration. */
  Desk1x1Broken(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(224, 368, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  DeskSmallBroken(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(240, 368, 1, 1, 16, 16)),
      new Rectangle(1f, 1f, 0f, 0f)),
  /** A decoration. */
  Spikes5xAnimated(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(256, 336, 1, 4, 16, 16)).framesPerSprite(10)),
  /** A decoration. */
  Spikes5xMirroredAnimated(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(320, 336, 1, 4, 16, 16)).framesPerSprite(10)),
  ;

  private IPath path;
  private AnimationConfig config;
  private Rectangle defaultCollider = null;
  private int defaultDepth;

  Deco(String path, AnimationConfig config) {
    this.path = new SimpleIPath(path);
    this.config = config;
  }

  Deco(String path, SpritesheetConfig config) {
    this(path, config, (Rectangle) null);
  }

  Deco(String path, SpritesheetConfig config, int defaultDepth) {
    this(path, config, (Rectangle) null, defaultDepth);
  }

  Deco(String path, SpritesheetConfig config, Vector2 defaultCollider) {
    this(path, config, new Rectangle(defaultCollider), DepthLayer.Player.depth());
  }

  Deco(String path, SpritesheetConfig config, Vector2 defaultCollider, int defaultDepth) {
    this(path, config, new Rectangle(defaultCollider), defaultDepth);
  }

  Deco(String path, SpritesheetConfig config, Rectangle defaultCollider) {
    this(
        path,
        config,
        defaultCollider,
        defaultCollider == null ? DepthLayer.BackgroundDeco.depth() : DepthLayer.Player.depth());
  }

  Deco(String path, SpritesheetConfig config, Rectangle defaultCollider, int defaultDepth) {
    this(path, new AnimationConfig(config), defaultCollider, defaultDepth);
  }

  Deco(String path, AnimationConfig config, Vector2 defaultColliderSize) {
    this(path, config, new Rectangle(defaultColliderSize));
  }

  Deco(String path, AnimationConfig config, Rectangle defaultCollider) {
    this(
        path,
        config,
        defaultCollider,
        defaultCollider == null ? DepthLayer.BackgroundDeco.depth() : DepthLayer.Player.depth());
  }

  Deco(String path, AnimationConfig config, Rectangle defaultCollider, int defaultDepth) {
    this.path = new SimpleIPath(path);
    this.config = config;
    this.defaultCollider = defaultCollider;
    this.defaultDepth = defaultDepth;
  }

  /**
   * Returns the {@link IPath} representing the file path to the sprite or spritesheet used by this
   * decoration.
   *
   * @return the {@link IPath} to the decoration’s sprite or spritesheet
   */
  public IPath path() {
    return path;
  }

  /**
   * Returns the {@link AnimationConfig} associated with this decoration.
   *
   * <p>This configuration defines how the sprite or spritesheet is rendered and scaled.
   *
   * @return the {@link AnimationConfig} of this decoration
   */
  public AnimationConfig config() {
    return config;
  }

  /**
   * Returns the default collider for this decoration, if any.
   *
   * <p>The collider defines the solid area occupied by the object and is used for player or
   * environment collision. If the decoration is purely visual, this may return {@code null}.
   *
   * @return the {@link Rectangle} representing the default collider, or {@code null} if none exists
   */
  public Rectangle defaultCollider() {
    return defaultCollider;
  }

  /**
   * Returns the default rendering depth for this decoration.
   *
   * <p>The depth determines the draw order relative to other entities (e.g., background, player,
   * foreground).
   *
   * @return the default rendering depth value
   */
  public int defaultDepth() {
    return defaultDepth;
  }
}
