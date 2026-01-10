package contrib.components;

import contrib.utils.components.skill.Skill;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializable data class representing a SkillComponent's state for network synchronization.
 *
 * <p>This class contains the active skill index and a list of {@link SkillData} objects
 * representing each skill's display state. It is used to sync skill state from server to clients.
 *
 * @param activeSkillIndex the index of the currently active skill (-1 if none)
 * @param skills the list of skill display data
 */
public record SkillComponentData(int activeSkillIndex, List<SkillData> skills)
    implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /**
   * Creates a SkillComponentData from a SkillComponent.
   *
   * <p>Extracts the current display state from all skills in the component.
   *
   * @param component the SkillComponent to extract state from
   * @return a new SkillComponentData representing the current state
   */
  public static SkillComponentData from(SkillComponent component) {
    List<Skill> skillList = component.getSkills();
    List<SkillData> skillDataList = new ArrayList<>(skillList.size());

    for (Skill skill : skillList) {
      skillDataList.add(
          new SkillData(skill.name(), skill.cooldown(), skill.remainingCooldownMillis()));
    }

    // Find active skill index
    int activeIndex = -1;
    if (component.activeSkill().isPresent()) {
      Skill activeSkill = component.activeSkill().get();
      activeIndex = skillList.indexOf(activeSkill);
    }

    return new SkillComponentData(activeIndex, skillDataList);
  }

  /**
   * Returns the number of skills.
   *
   * @return the skill count
   */
  public int skillCount() {
    return skills != null ? skills.size() : 0;
  }

  /**
   * Returns the active skill's display data.
   *
   * @return the active SkillData, or null if no skill is active
   */
  public SkillData activeSkillData() {
    if (activeSkillIndex < 0 || skills == null || activeSkillIndex >= skills.size()) {
      return null;
    }
    return skills.get(activeSkillIndex);
  }
}
