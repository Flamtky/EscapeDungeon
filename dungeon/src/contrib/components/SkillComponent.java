package contrib.components;

import contrib.utils.components.skill.Skill;
import core.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * A component that manages a list of {@link Skill} objects for an entity.
 *
 * <p>It supports adding, removing, and retrieving skills, as well as managing which skill is
 * currently active. The active skill can be cycled through the list or set to the last one.
 */
public class SkillComponent implements Component {

  /** All skills currently assigned to this component. */
  private final List<Skill> skills;

  /**
   * The index of the currently active skill in {@link #skills}. If no skill is active, the value is
   * {@code -1}.
   */
  private int activeSkill = -1;

  /**
   * Cached sync data received from the server. Used on clients to display skill state without
   * having the actual Skill objects.
   */
  private SkillComponentData cachedSyncData;

  /**
   * Creates a new {@code SkillComponent} with the given skills.
   *
   * <p>If at least one skill is provided, the first skill will be marked as active.
   *
   * @param skills one or more skills to initialize the component with
   */
  public SkillComponent(Skill... skills) {
    this.skills = new ArrayList<>(Arrays.asList(skills));
    if (skills.length > 0) {
      activeSkill = 0;
    }
  }

  /**
   * Adds a skill to this component.
   *
   * <p>If this is the first skill added, it will automatically become the active skill.
   *
   * @param skill the skill to add (ignored if {@code null})
   */
  public void addSkill(Skill skill) {
    if (skill != null) {
      skills.add(skill);
      if (activeSkill == -1) {
        activeSkill = 0;
      }
    }
  }

  /**
   * Removes a given skill instance from this component.
   *
   * <p>If the active skill is removed, the active index will be adjusted. If no skills remain, the
   * active index is set to {@code -1}.
   *
   * @param skill the skill to remove
   */
  public void removeSkill(Skill skill) {
    if (skills.remove(skill)) {
      activeSkill = skills.size() - 1;
    }
  }

  /** Remove all skills. */
  public void removeAll() {
    skills.clear();
  }

  /**
   * Removes all skills of the given class (or subclasses).
   *
   * <p>If the active skill is removed, the active index will be adjusted. If no skills remain, the
   * active index is set to {@code -1}.
   *
   * @param skillClass the class of the skills to remove
   */
  public void removeSkill(Class<? extends Skill> skillClass) {
    skills.removeIf(s -> skillClass.isAssignableFrom(s.getClass()));
    activeSkill = skills.size() - 1;
  }

  /**
   * Finds the first skill that matches the given class (or subclass).
   *
   * @param skillClass the class of the skill to find
   * @return an {@link Optional} containing the matching skill, or {@link Optional#empty()} if none
   *     exists
   */
  public Optional<Skill> getSkill(Class<? extends Skill> skillClass) {
    return skills.stream().filter(s -> skillClass.isAssignableFrom(s.getClass())).findFirst();
  }

  /**
   * Returns the currently active skill.
   *
   * @return an {@link Optional} containing the active skill, or {@link Optional#empty()} if no
   *     skill is active
   */
  public Optional<Skill> activeSkill() {
    if (activeSkill == -1 || activeSkill >= skills.size()) {
      return Optional.empty();
    }
    return Optional.of(skills.get(activeSkill));
  }

  /**
   * Moves the active skill to the next one in the list.
   *
   * <p>If the end of the list is reached, it wraps around to the first skill. If no skills are
   * present, nothing happens.
   */
  public void nextSkill() {
    if (!skills.isEmpty()) {
      activeSkill = (activeSkill + 1) % skills.size();
    }
  }

  /**
   * Moves the active skill to the previous one in the list.
   *
   * <p>If the beginning of the list is reached, it wraps around to the last skill. If no skills are
   * present, nothing happens.
   */
  public void prevSkill() {
    if (!skills.isEmpty()) {
      activeSkill = (activeSkill - 1 + skills.size()) % skills.size();
    }
  }

  /**
   * Returns an unmodifiable list of all skills currently assigned to this component.
   *
   * @return a copy of the skill list
   */
  public List<Skill> getSkills() {
    return List.copyOf(skills);
  }

  /**
   * Exports the current skill state as sync data for network transmission.
   *
   * <p>This extracts the display-relevant information from all skills.
   *
   * @return a SkillComponentData representing the current state
   */
  public SkillComponentData toSyncData() {
    return SkillComponentData.from(this);
  }

  /**
   * Applies sync data received from the server.
   *
   * <p>This stores the data for client-side display. The actual Skill objects are not modified.
   *
   * @param data the sync data to apply
   */
  public void applySyncData(SkillComponentData data) {
    this.cachedSyncData = data;
    if (data != null) {
      this.activeSkill = data.activeSkillIndex();
    }
  }

  /**
   * Returns the cached sync data, if available.
   *
   * @return the cached SkillComponentData, or null if not set
   */
  public SkillComponentData cachedSyncData() {
    return cachedSyncData;
  }

  /**
   * Returns the active skill's display data.
   *
   * <p>Prefers cached sync data when available for unified single-player/multiplayer behavior. Only
   * falls back to local skill objects when no sync data exists.
   *
   * @return the active skill's display data, or null if no skill is active
   */
  public SkillData activeSkillData() {
    // Prefer cached sync data for unified SP/MP behavior
    if (cachedSyncData != null) {
      return cachedSyncData.activeSkillData();
    }
    // Fall back to local skills if no sync data (e.g., server-side before first sync)
    if (!skills.isEmpty() && activeSkill >= 0 && activeSkill < skills.size()) {
      Skill skill = skills.get(activeSkill);
      return SkillData.now(skill.name(), skill.cooldown(), skill.remainingCooldownMillis());
    }
    return null;
  }

  /**
   * Returns the number of skills.
   *
   * <p>Uses actual skills if available, otherwise falls back to cached sync data.
   *
   * @return the number of skills
   */
  public int skillCount() {
    if (!skills.isEmpty()) {
      return skills.size();
    }
    if (cachedSyncData != null) {
      return cachedSyncData.skillCount();
    }
    return 0;
  }

  /**
   * Returns the index of the currently active skill.
   *
   * @return the active skill index, or -1 if no skill is active
   */
  public int activeSkillIndex() {
    return activeSkill;
  }
}
