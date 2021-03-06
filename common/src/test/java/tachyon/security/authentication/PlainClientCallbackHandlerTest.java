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

package tachyon.security.authentication;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the {@link tachyon.security.authentication.PlainSaslUtils.PlainClientCallbackHandler}
 * class.
 */
public class PlainClientCallbackHandlerTest {

  /**
   * The exception expected to be thrown.
   */
  @Rule
  public ExpectedException mThrown = ExpectedException.none();

  /**
   * Tests that the callback is handled correctly.
   *
   * @throws Exception thrown when the callback fails
   */
  @Test
  public void clientCallbackHandlerTest() throws Exception {
    Callback[] callbacks = new Callback[2];
    callbacks[0] = new NameCallback("Username:");
    callbacks[1] = new PasswordCallback("Password:", true);

    String user = "tachyon-user-1";
    String password = "tachyon-user-1-password";

    CallbackHandler clientCBHandler =
        new PlainSaslUtils.PlainClientCallbackHandler(user, password);
    clientCBHandler.handle(callbacks);

    validateCallbacks(user, password, callbacks);
  }

  /**
   * Tests that an exception is thrown in case an unsupported callback is used.
   *
   * @throws Exception thrown when the callback fails
   */
  @Test
  public void unsupportCallbackTest() throws Exception {
    mThrown.expect(UnsupportedCallbackException.class);
    mThrown.expectMessage(RealmCallback.class + " is unsupported.");

    Callback[] callbacks = new Callback[3];
    callbacks[0] = new NameCallback("Username:");
    callbacks[1] = new PasswordCallback("Password:", true);
    callbacks[2] = new RealmCallback("Realm:");

    String user = "tachyon-user-2";
    String password = "tachyon-user-2-password";
    CallbackHandler clientCBHandler =
        new PlainSaslUtils.PlainClientCallbackHandler(user, password);
    clientCBHandler.handle(callbacks);
  }

  /**
   * Tests that the callback can handle a non-existent user.
   *
   * @throws Exception thrown when the callback fails
   */
  @Test
  public void nullNameCallbackTest() throws Exception {
    Callback[] callbacks = new Callback[2];
    callbacks[0] = new NameCallback("Username:");
    callbacks[1] = new PasswordCallback("Password:", true);

    String user = null;
    String password = "tachyon-user-3-password";
    CallbackHandler clientCBHandler =
        new PlainSaslUtils.PlainClientCallbackHandler(user, password);
    clientCBHandler.handle(callbacks);

    validateCallbacks(user, password, callbacks);
  }

  /**
   * Tests that the callback can handle a non-existent password.
   *
   * @throws Exception thrown when the callback fails
   */
  @Test
  public void nullPasswordCallbackTest() throws Exception {
    Callback[] callbacks = new Callback[2];
    callbacks[0] = new NameCallback("Username:");
    callbacks[1] = new PasswordCallback("Password:", true);

    String user = "tachyon-user-4";
    String password = null;
    CallbackHandler clientCBHandler =
        new PlainSaslUtils.PlainClientCallbackHandler(user, password);
    clientCBHandler.handle(callbacks);

    validateCallbacks(user, password, callbacks);
  }

  /**
   * Tests that an exception is thrown when a callback is {@code null}.
   *
   * @throws Exception thrown when the callback fails
   */
  @Test
  public void nullCallbackTest() throws Exception {
    mThrown.expect(UnsupportedCallbackException.class);
    mThrown.expectMessage(null + " is unsupported.");

    Callback[] callbacks = new Callback[3];
    callbacks[0] = new NameCallback("Username:");
    callbacks[1] = new PasswordCallback("Password:", true);
    callbacks[2] = null;

    String user = "tachyon-user-5";
    String password = "tachyon-user-5-password";
    CallbackHandler clientCBHandler =
        new PlainSaslUtils.PlainClientCallbackHandler(user, password);
    clientCBHandler.handle(callbacks);
  }

  private void validateCallbacks(String user, String passwd, Callback[] callbacks)
      throws IOException, UnsupportedCallbackException {
    for (Callback cb : callbacks) {
      if (cb instanceof NameCallback) {
        Assert.assertEquals(user, ((NameCallback) cb).getName());
      } else if (cb instanceof PasswordCallback) {
        char[] passwordChar = ((PasswordCallback) cb).getPassword();
        Assert.assertEquals(passwd, passwordChar == null ? null : String.copyValueOf(passwordChar));
      }
    }
  }
}
