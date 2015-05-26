SELECT _SkillTableID,            -- Skill.id
       _SkillLevel,              -- Skill.level
       _NeedJob,                 -- SkillTree.jobID
       _EnglishName,             -- SkillTree.jobName
       _JobNumber,               -- SkillTree.advancement
       _NameID,                  -- Skill.nameID
       _SkillType,               -- Skill.type
       _NeedWeaponType1,         -- Skill.weapon1
       _NeedWeaponType2,         -- Skill.weapon2
       _ParentSkillID1,          -- Skill.parentID1
       _NeedParentSkillLevel1,   -- Skill.parentLevel1
       _ParentSkillID2,          -- Skill.parentID2
       _NeedParentSkillLevel2,   -- Skill.parentLevel2
       _NeedBasicSP1,            -- Skill.basicSP
       _NeedFirstSP1,            -- Skill.firstSP
       _TreeSlotIndex,           -- Skill.slot
       _LevelLimit,              -- Level.limit
       _NeedSkillPoint,          -- Level.sp
       _SkillExplanationID,      -- Level.Mode.explanationID
       _SkillExplanationIDParam, -- Level.Mode.explanationParams
       _DecreaseSP,              -- Level.Mode.mp
       _DelayTime                -- Level.Mode.cd
FROM skilltreetable t
INNER JOIN skilltable_character s
ON(_SkillTableID = s._ID)
INNER JOIN jobtable j
ON(s._NeedJob = j._ID)
RIGHT OUTER JOIN %s c
ON(_SkillTableID = c._SkillIndex)
WHERE _SkillTableID IS NOT NULL
  AND _SkillLevel <= _MaxLevel
  AND _ApplyType = %d