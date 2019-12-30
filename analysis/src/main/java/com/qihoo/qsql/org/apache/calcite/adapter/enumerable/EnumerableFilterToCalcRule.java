/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qihoo.qsql.org.apache.calcite.adapter.enumerable;

import com.qihoo.qsql.org.apache.calcite.plan.RelOptRule;
import com.qihoo.qsql.org.apache.calcite.plan.RelOptRuleCall;
import com.qihoo.qsql.org.apache.calcite.rel.RelNode;
import com.qihoo.qsql.org.apache.calcite.rel.type.RelDataType;
import com.qihoo.qsql.org.apache.calcite.rex.RexBuilder;
import com.qihoo.qsql.org.apache.calcite.rex.RexProgram;
import com.qihoo.qsql.org.apache.calcite.rex.RexProgramBuilder;
import com.qihoo.qsql.org.apache.calcite.tools.RelBuilderFactory;

/** Variant of {@link com.qihoo.qsql.org.apache.calcite.rel.rules.FilterToCalcRule} for
 * {@link com.qihoo.qsql.org.apache.calcite.adapter.enumerable.EnumerableConvention enumerable calling convention}. */
public class EnumerableFilterToCalcRule extends RelOptRule {
  /**
   * Creates an EnumerableFilterToCalcRule.
   *
   * @param relBuilderFactory Builder for relational expressions
   */
  public EnumerableFilterToCalcRule(RelBuilderFactory relBuilderFactory) {
    super(operand(EnumerableFilter.class, any()), relBuilderFactory, null);
  }

  public void onMatch(RelOptRuleCall call) {
    final EnumerableFilter filter = call.rel(0);
    final RelNode input = filter.getInput();

    // Create a program containing a filter.
    final RexBuilder rexBuilder = filter.getCluster().getRexBuilder();
    final RelDataType inputRowType = input.getRowType();
    final RexProgramBuilder programBuilder =
        new RexProgramBuilder(inputRowType, rexBuilder);
    programBuilder.addIdentity();
    programBuilder.addCondition(filter.getCondition());
    final RexProgram program = programBuilder.getProgram();

    final EnumerableCalc calc = EnumerableCalc.create(input, program);
    call.transformTo(calc);
  }
}

// End EnumerableFilterToCalcRule.java