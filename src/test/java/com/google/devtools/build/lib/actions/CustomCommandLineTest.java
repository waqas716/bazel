// Copyright 2015 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.lib.actions;


import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.devtools.build.lib.analysis.actions.CustomCommandLine;
import com.google.devtools.build.lib.analysis.actions.CustomCommandLine.CustomArgv;
import com.google.devtools.build.lib.analysis.actions.CustomCommandLine.CustomMultiArgv;
import com.google.devtools.build.lib.collect.nestedset.NestedSetBuilder;
import com.google.devtools.build.lib.syntax.Label;
import com.google.devtools.build.lib.syntax.Label.SyntaxException;
import com.google.devtools.build.lib.testutil.Scratch;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for CustomCommandLine.
 */
@RunWith(JUnit4.class)
public class CustomCommandLineTest {

  private Scratch scratch;
  private Root rootDir;
  private Artifact artifact1;
  private Artifact artifact2;

  @Before
  public void setUp() throws Exception {
    scratch = new Scratch();
    rootDir = Root.asDerivedRoot(scratch.dir("/exec/root"));
    artifact1 = new Artifact(scratch.file("/exec/root/dir/file1.txt"), rootDir);
    artifact2 = new Artifact(scratch.file("/exec/root/dir/file2.txt"), rootDir);
  }

  @Test
  public void testStringArgs() {
    CustomCommandLine cl = CustomCommandLine.builder().add("--arg1").add("--arg2").build();
    assertEquals(ImmutableList.of("--arg1", "--arg2"), cl.arguments());
  }

  @Test
  public void testLabelArgs() throws SyntaxException {
    CustomCommandLine cl = CustomCommandLine.builder().add(Label.parseAbsolute("//a:b")).build();
    assertEquals(ImmutableList.of("//a:b"), cl.arguments());
  }

  @Test
  public void testStringsArgs() {
    CustomCommandLine cl = CustomCommandLine.builder().add("--arg",
        ImmutableList.of("a", "b")).build();
    assertEquals(ImmutableList.of("--arg", "a", "b"), cl.arguments());
  }

  @Test
  public void testArtifactExecPathArgs() {
    CustomCommandLine cl = CustomCommandLine.builder().addExecPath("--path", artifact1).build();
    assertEquals(ImmutableList.of("--path", "dir/file1.txt"), cl.arguments());
  }

  @Test
  public void testArtifactExecPathsArgs() {
    CustomCommandLine cl = CustomCommandLine.builder().addExecPaths("--path",
        ImmutableList.of(artifact1, artifact2)).build();
    assertEquals(ImmutableList.of("--path", "dir/file1.txt", "dir/file2.txt"), cl.arguments());
  }

  @Test
  public void testNestedSetArtifactExecPathsArgs() {
    CustomCommandLine cl = CustomCommandLine.builder().addExecPaths(
        NestedSetBuilder.<Artifact>stableOrder().add(artifact1).add(artifact2).build()).build();
    assertEquals(ImmutableList.of("dir/file1.txt", "dir/file2.txt"), cl.arguments());
  }

  @Test
  public void testArtifactJoinExecPathArgs() {
    CustomCommandLine cl = CustomCommandLine.builder().addJoinExecPaths("--path", ":",
        ImmutableList.of(artifact1, artifact2)).build();
    assertEquals(ImmutableList.of("--path", "dir/file1.txt:dir/file2.txt"), cl.arguments());
  }

  @Test
  public void testPathArgs() {
    CustomCommandLine cl = CustomCommandLine.builder().addPath(artifact1.getExecPath()).build();
    assertEquals(ImmutableList.of("dir/file1.txt"), cl.arguments());
  }

  @Test
  public void testJoinPathArgs() {
    CustomCommandLine cl = CustomCommandLine.builder().addJoinPaths(":",
        ImmutableList.of(artifact1.getExecPath(), artifact2.getExecPath())).build();
    assertEquals(ImmutableList.of("dir/file1.txt:dir/file2.txt"), cl.arguments());
  }

  @Test
  public void testPathsArgs() {
    CustomCommandLine cl = CustomCommandLine.builder().addPaths("%s:%s",
        artifact1.getExecPath(), artifact1.getRootRelativePath()).build();
    assertEquals(ImmutableList.of("dir/file1.txt:dir/file1.txt"), cl.arguments());
  }

  @Test
  public void testCustomArgs() {
    CustomCommandLine cl = CustomCommandLine.builder().add(new CustomArgv() {
      @Override
      public String argv() {
        return "--arg";
      }
    }).build();
    assertEquals(ImmutableList.of("--arg"), cl.arguments());
  }

  @Test
  public void testCustomMultiArgs() {
    CustomCommandLine cl = CustomCommandLine.builder().add(new CustomMultiArgv() {
      @Override
      public ImmutableList<String> argv() {
        return ImmutableList.of("--arg1", "--arg2");
      }
    }).build();
    assertEquals(ImmutableList.of("--arg1", "--arg2"), cl.arguments());
  }

  @Test
  public void testCombinedArgs() {
    CustomCommandLine cl = CustomCommandLine.builder()
        .add("--arg")
        .add("--args", ImmutableList.of("abc"))
        .addExecPaths("--path1", ImmutableList.of(artifact1))
        .addExecPath("--path2", artifact2)
        .build();
    assertEquals(ImmutableList.of("--arg", "--args", "abc", "--path1", "dir/file1.txt", "--path2",
        "dir/file2.txt"), cl.arguments());
  }

  @Test
  public void testAddNulls() {
    CustomCommandLine cl = CustomCommandLine.builder()
        .add("--args", null)
        .addExecPaths(null, ImmutableList.of(artifact1))
        .addExecPath(null, null)
        .build();
    assertEquals(ImmutableList.of(), cl.arguments());
  }
}
