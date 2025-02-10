package com.yu.market.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.market.infrastructure.pojo.RuleTree;
import com.yu.market.infrastructure.pojo.RuleTreeNode;

import java.util.List;

/**
* @description 针对表【rule_tree(规则表-树)】的数据库操作Mapper
* @Entity com.yu.market.infrastructure.pojo.RuleTree
*/
public interface RuleTreeMapper extends BaseMapper<RuleTree> {

	List<RuleTreeNode> queryRuleLocks(String[] treeIds);
}




