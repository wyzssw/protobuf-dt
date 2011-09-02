/*
 * Copyright (c) 2011 Google Inc.
 *
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.google.eclipse.protobuf.junit.core;

import static com.google.eclipse.protobuf.junit.util.SystemProperties.lineSeparator;
import static org.eclipse.emf.common.util.URI.createURI;
import static org.eclipse.emf.ecore.util.EcoreUtil.resolveAll;
import static org.eclipse.xtext.util.CancelIndicator.NullImpl;

import com.google.eclipse.protobuf.junit.util.MultiLineTextBuilder;
import com.google.eclipse.protobuf.protobuf.Protobuf;
import com.google.inject.Injector;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.linking.lazy.LazyLinkingResource;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.resource.*;
import org.eclipse.xtext.util.StringInputStream;
import org.junit.rules.MethodRule;
import org.junit.runners.model.*;

import java.io.*;

/**
 * Rule that performs configuration of a standalone Xtext environment.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
public class XtextRule implements MethodRule {

  private Injector injector;

  public Statement apply(Statement base, FrameworkMethod method, Object target) {
    return new XtextStatement(base);
  }

  public Injector injector() {
    return injector;
  }

  public Protobuf parse(MultiLineTextBuilder text) {
    return parse(text.toString());
  }

  public Protobuf parse(String text) {
    XtextResource resource = resourceFrom(new StringInputStream(text));
    IParseResult parseResult = resource.getParseResult();
    if (!parseResult.hasSyntaxErrors()) return (Protobuf) parseResult.getRootASTElement();
    StringBuilder builder = new StringBuilder();
    builder.append("Syntax errors:");
    for (INode error : parseResult.getSyntaxErrors()) 
      builder.append(lineSeparator()).append("- ").append(error.getSyntaxErrorMessage());
    throw new IllegalStateException(builder.toString());
  }

  private XtextResource resourceFrom(InputStream input) {
    return resourceFrom(input, createURI("mytestmodel.proto")); //$NON-NLS-1$
  }

  private XtextResource resourceFrom(InputStream input, URI uri) {
    XtextResourceSet set = getInstanceOf(XtextResourceSet.class);
    set.setClasspathURIContext(getClass());
    XtextResource resource = (XtextResource) getInstanceOf(IResourceFactory.class).createResource(uri);
    set.getResources().add(resource);
    try {
      resource.load(input, null);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    if (resource instanceof LazyLinkingResource) {
      ((LazyLinkingResource) resource).resolveLazyCrossReferences(NullImpl);
      return resource;
    }
    resolveAll(resource);
    return resource;
  }

  public <T> T getInstanceOf(Class<T> type) {
    return injector.getInstance(type);
  }

  private class XtextStatement extends Statement {
    private final Statement base;

    public XtextStatement(Statement base) {
      this.base = base;
    }

    @Override public void evaluate() throws Throwable {
      setUpInjector();
      base.evaluate();
    }

    private void setUpInjector() {
      injector = new TestingStandaloneSetup().createInjectorAndDoEMFRegistration();
    }
  }
}
