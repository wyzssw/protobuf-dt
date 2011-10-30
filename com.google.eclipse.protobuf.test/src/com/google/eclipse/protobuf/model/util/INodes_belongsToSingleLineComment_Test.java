/*
 * Copyright (c) 2011 Google Inc.
 *
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.google.eclipse.protobuf.model.util;

import static com.google.eclipse.protobuf.junit.core.Setups.unitTestSetup;
import static com.google.eclipse.protobuf.junit.core.XtextRule.createWith;
import static org.eclipse.xtext.nodemodel.util.NodeModelUtils.getNode;
import static org.junit.Assert.*;

import org.eclipse.xtext.nodemodel.*;
import org.junit.*;

import com.google.eclipse.protobuf.junit.core.XtextRule;

/**
 * Test for <code>{@link INodes#belongsToSingleLineComment(INode)}</code>
 * 
 * @author alruiz@google.com (Alex Ruiz)
 */
public class INodes_belongsToSingleLineComment_Test {

  @Rule public XtextRule xtext = createWith(unitTestSetup());
  
  private INodes nodes;
  
  @Before public void setUp() {
    nodes = xtext.getInstanceOf(INodes.class);
  }
  
  // // This is a test.
  // message Person {}
  @Test public void should_return_true_if_node_belongs_to_single_line_comment() {
    ILeafNode commentNode = xtext.find("// This is a test.");
    assertTrue(nodes.belongsToSingleLineComment(commentNode));
  }

  // message Person {}
  @Test public void should_return_false_if_node_does_not_belong_to_single_line_comment() {
    ICompositeNode node = getNode(xtext.root());
    assertFalse(nodes.belongsToSingleLineComment(node));
  }
}
