/*
 * Licensed to the University of California, Berkeley under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package tachyon.util.io;

import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import tachyon.Constants;
import tachyon.exception.InvalidPathException;

/**
 * Tests for the {@link PathUtils} class.
 */
public class PathUtilsTest {

  /**
   * The expected exception thrown during a test.
   */
  @Rule
  public final ExpectedException mException = ExpectedException.none();

  /**
   * Tests the {@link PathUtils#cleanPath(String)} method.
   *
   * @throws InvalidPathException thrown if the path is invalid
   */
  @Test
  public void cleanPathNoExceptionTest() throws InvalidPathException {
    // test clean path
    Assert.assertEquals("/foo/bar", PathUtils.cleanPath("/foo/bar"));

    // test trailing slash
    Assert.assertEquals("/foo/bar", PathUtils.cleanPath("/foo/bar/"));

    // test redundant slashes
    Assert.assertEquals("/foo/bar", PathUtils.cleanPath("/foo//bar"));
    Assert.assertEquals("/foo/bar", PathUtils.cleanPath("/foo//bar//"));
    Assert.assertEquals("/foo/bar", PathUtils.cleanPath("/foo///////bar//////"));

    // test dots gets resolved
    Assert.assertEquals("/foo/bar", PathUtils.cleanPath("/foo/./bar"));
    Assert.assertEquals("/foo/bar", PathUtils.cleanPath("/foo/././bar"));
    Assert.assertEquals("/foo", PathUtils.cleanPath("/foo/bar/.."));
    Assert.assertEquals("/bar", PathUtils.cleanPath("/foo/../bar"));
    Assert.assertEquals("/", PathUtils.cleanPath("/foo/bar/../.."));

    // the following seems strange
    // TODO(jiri): Instead of returning null, throw InvalidPathException.
    Assert.assertNull(PathUtils.cleanPath("/foo/bar/../../.."));
  }

  /**
   * Tests the {@link PathUtils#cleanPath(String)} method to thrown an exception in case an invalid
   * path is provided.
   *
   * @throws InvalidPathException thrown if the path is invalid
   */
  @Test
  public void cleanPathExceptionTest() throws InvalidPathException {
    mException.expect(InvalidPathException.class);
    Assert.assertEquals("/foo/bar", PathUtils.cleanPath("/\\   foo / bar"));
  }

  /**
   * Tests the {@link PathUtils#concatPath(Object, Object...)} method.
   */
  @Test
  public void concatPathTest() {
    Assert.assertEquals("/", PathUtils.concatPath("/"));
    Assert.assertEquals("/", PathUtils.concatPath("/", ""));
    Assert.assertEquals("/bar", PathUtils.concatPath("/", "bar"));

    Assert.assertEquals("foo", PathUtils.concatPath("foo"));
    Assert.assertEquals("/foo", PathUtils.concatPath("/foo"));
    Assert.assertEquals("/foo", PathUtils.concatPath("/foo", ""));

    // Join base without trailing "/"
    Assert.assertEquals("/foo/bar", PathUtils.concatPath("/foo", "bar"));
    Assert.assertEquals("/foo/bar", PathUtils.concatPath("/foo", "bar/"));
    Assert.assertEquals("/foo/bar", PathUtils.concatPath("/foo", "/bar"));
    Assert.assertEquals("/foo/bar", PathUtils.concatPath("/foo", "/bar/"));

    // Join base with trailing "/"
    Assert.assertEquals("/foo/bar", PathUtils.concatPath("/foo/", "bar"));
    Assert.assertEquals("/foo/bar", PathUtils.concatPath("/foo/", "bar/"));
    Assert.assertEquals("/foo/bar", PathUtils.concatPath("/foo/", "/bar"));
    Assert.assertEquals("/foo/bar", PathUtils.concatPath("/foo/", "/bar/"));

    // Whitespace must be trimmed.
    Assert.assertEquals("/foo/bar", PathUtils.concatPath("/foo ", "bar  "));
    Assert.assertEquals("/foo/bar", PathUtils.concatPath("/foo ", "  bar"));
    Assert.assertEquals("/foo/bar", PathUtils.concatPath("/foo ", "  bar  "));

    // Redundant separator must be trimmed.
    Assert.assertEquals("/foo/bar", PathUtils.concatPath("/foo/", "bar//"));

    // Multiple components to join.
    Assert.assertEquals("/foo/bar/a/b/c", PathUtils.concatPath("/foo", "bar", "a", "b", "c"));
    Assert.assertEquals("/foo/bar/b/c", PathUtils.concatPath("/foo", "bar", "", "b", "c"));

    // Non-string
    Assert.assertEquals("/foo/bar/1", PathUtils.concatPath("/foo", "bar", 1));
    Assert.assertEquals("/foo/bar/2", PathUtils.concatPath("/foo", "bar", 2L));

    // Header
    Assert.assertEquals(Constants.HEADER + "host:port/foo/bar",
        PathUtils.concatPath(Constants.HEADER + "host:port", "/foo", "bar"));
  }

  /**
   * Tests the {@link PathUtils#getParent(String)} method.
   *
   * @throws InvalidPathException thrown if the path is invalid
   */
  @Test
  public void getParentTest() throws InvalidPathException {
    // get a parent that is non-root
    Assert.assertEquals("/foo", PathUtils.getParent("/foo/bar"));
    Assert.assertEquals("/foo", PathUtils.getParent("/foo/bar/"));
    Assert.assertEquals("/foo", PathUtils.getParent("/foo/./bar/"));
    Assert.assertEquals("/foo", PathUtils.getParent("/foo/././bar/"));

    // get a parent that is root
    Assert.assertEquals("/", PathUtils.getParent("/foo"));
    Assert.assertEquals("/", PathUtils.getParent("/foo/bar/../"));
    Assert.assertEquals("/", PathUtils.getParent("/foo/../bar/"));

    // get parent of root
    Assert.assertEquals("/", PathUtils.getParent("/"));
    Assert.assertEquals("/", PathUtils.getParent("/foo/bar/../../"));
    Assert.assertEquals("/", PathUtils.getParent("/foo/../bar/../"));
  }

  /**
   * Tests the {@link PathUtils#getPathComponents(String)} method.
   *
   * @throws InvalidPathException thrown if the path is invalid
   */
  @Test
  public void getPathComponentsNoExceptionTest() throws InvalidPathException {
    Assert.assertArrayEquals(new String[] {""}, PathUtils.getPathComponents("/"));
    Assert.assertArrayEquals(new String[] {"", "bar"}, PathUtils.getPathComponents("/bar"));
    Assert.assertArrayEquals(new String[] {"", "foo", "bar"},
        PathUtils.getPathComponents("/foo/bar"));
    Assert.assertArrayEquals(new String[] {"", "foo", "bar"},
        PathUtils.getPathComponents("/foo/bar/"));
    Assert.assertArrayEquals(new String[] {"", "bar"},
        PathUtils.getPathComponents("/foo/../bar"));
    Assert.assertArrayEquals(new String[] {"", "foo", "bar", "a", "b", "c"},
        PathUtils.getPathComponents("/foo//bar/a/b/c"));
  }

  /**
   * Tests the {@link PathUtils#getPathComponents(String)} method to thrown an exception in case the
   * path is invalid.
   *
   * @throws InvalidPathException thrown if the path is invalid
   */
  @Test
  public void getPathComponentsExceptionTest() throws InvalidPathException {
    mException.expect(InvalidPathException.class);
    PathUtils.getPathComponents("/\\   foo / bar");
  }

  /**
   * Tests the {@link PathUtils#hasPrefix(String, String)} method.
   *
   * @throws InvalidPathException thrown if the path is invalid
   */
  @Test
  public void hasPrefixTest() throws InvalidPathException {
    Assert.assertTrue(PathUtils.hasPrefix("/", "/"));
    Assert.assertTrue(PathUtils.hasPrefix("/a", "/a"));
    Assert.assertTrue(PathUtils.hasPrefix("/a", "/a/"));
    Assert.assertTrue(PathUtils.hasPrefix("/a/b/c", "/a"));
    Assert.assertTrue(PathUtils.hasPrefix("/a/b/c", "/a/b"));
    Assert.assertTrue(PathUtils.hasPrefix("/a/b/c", "/a/b/c"));
    Assert.assertFalse(PathUtils.hasPrefix("/", "/a"));
    Assert.assertFalse(PathUtils.hasPrefix("/", "/a/b/c"));
    Assert.assertFalse(PathUtils.hasPrefix("/a", "/a/b/c"));
    Assert.assertFalse(PathUtils.hasPrefix("/a/b", "/a/b/c"));
    Assert.assertFalse(PathUtils.hasPrefix("/a/b/c", "/aa"));
    Assert.assertFalse(PathUtils.hasPrefix("/a/b/c", "/a/bb"));
    Assert.assertFalse(PathUtils.hasPrefix("/a/b/c", "/a/b/cc"));
    Assert.assertFalse(PathUtils.hasPrefix("/aa/b/c", "/a"));
    Assert.assertFalse(PathUtils.hasPrefix("/a/bb/c", "/a/b"));
    Assert.assertFalse(PathUtils.hasPrefix("/a/b/cc", "/a/b/c"));
  }

  /**
   * Tests the {@link PathUtils#isRoot(String)} method.
   *
   * @throws InvalidPathException thrown if the path is invalid
   */
  @Test
  public void isRootTest() throws InvalidPathException {
    // check a path that is non-root
    Assert.assertFalse(PathUtils.isRoot("/foo/bar"));
    Assert.assertFalse(PathUtils.isRoot("/foo/bar/"));
    Assert.assertFalse(PathUtils.isRoot("/foo/./bar/"));
    Assert.assertFalse(PathUtils.isRoot("/foo/././bar/"));
    Assert.assertFalse(PathUtils.isRoot("/foo/../bar"));
    Assert.assertFalse(PathUtils.isRoot("/foo/../bar/"));

    // check a path that is root
    Assert.assertTrue(PathUtils.isRoot("/"));
    Assert.assertTrue(PathUtils.isRoot("/"));
    Assert.assertTrue(PathUtils.isRoot("/."));
    Assert.assertTrue(PathUtils.isRoot("/./"));
    Assert.assertTrue(PathUtils.isRoot("/foo/.."));
    Assert.assertTrue(PathUtils.isRoot("/foo/../"));
  }

  /**
   * Tests the {@link PathUtils#temporaryFileName(long, long, String)} method.
   */
  @Test
  public void temporaryFileNameTest() {
    Assert.assertEquals(PathUtils.temporaryFileName(1, 1, "/"),
        PathUtils.temporaryFileName(1, 1, "/"));
    Assert.assertNotEquals(PathUtils.temporaryFileName(1, 1, "/"),
        PathUtils.temporaryFileName(1, 2, "/"));
    Assert.assertNotEquals(PathUtils.temporaryFileName(2, 1, "/"),
        PathUtils.temporaryFileName(1, 1, "/"));
    Assert.assertNotEquals(PathUtils.temporaryFileName(1, 1, "/"),
        PathUtils.temporaryFileName(1, 1, "/a"));
  }

  /**
   * Tests the {@link PathUtils#uniqPath()} method.
   */
  @Test
  public void uniqPathTest() {
    Assert.assertNotEquals(PathUtils.uniqPath(), PathUtils.uniqPath());
  }

  /**
   * Tests the {@link PathUtils#validatePath(String)} method.
   *
   * @throws InvalidPathException thrown if the path is invalid
   */
  @Test
  public void validatePathTest() throws InvalidPathException {
    // check valid paths
    PathUtils.validatePath("/foo/bar");
    PathUtils.validatePath("/foo/bar/");
    PathUtils.validatePath("/foo/./bar/");
    PathUtils.validatePath("/foo/././bar/");
    PathUtils.validatePath("/foo/../bar");
    PathUtils.validatePath("/foo/../bar/");

    // check invalid paths
    LinkedList<String> invalidPaths = new LinkedList<String>();
    invalidPaths.add(null);
    invalidPaths.add("");
    invalidPaths.add(" /");
    invalidPaths.add("/ ");
    for (String invalidPath : invalidPaths) {
      try {
        PathUtils.validatePath(invalidPath);
        Assert.fail("validatePath(" + invalidPath + ") did not fail");
      } catch (InvalidPathException ipe) {
        // this is expected
      }
    }
  }
}
