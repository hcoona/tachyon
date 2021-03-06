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

package tachyon.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tachyon.Constants;
import tachyon.conf.TachyonConf;
import tachyon.util.network.NetworkAddressUtils;
import tachyon.util.network.NetworkAddressUtils.ServiceType;

/**
 * Servlet that provides data for the header navigation bar.
 */
public final class WebInterfaceHeaderServlet extends HttpServlet {
  private static final long serialVersionUID = -2466055439220042703L;

  private final transient TachyonConf mTachyonConf;

  /**
   * Creates a new instance of {@link WebInterfaceHeaderServlet}.
   *
   * @param conf Tachyon configuration
   */
  public WebInterfaceHeaderServlet(TachyonConf conf) {
    mTachyonConf = conf;
  }

  /**
   * Populate the header with information about master. So we can return to
   * the master from any page.
   *
   * @param request the {@link HttpServletRequest} object
   * @param response the {@link HttpServletResponse} object
   * @throws ServletException if the target resource throws this exception
   * @throws IOException if the target resource throws this exception
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    int masterWebPort = mTachyonConf.getInt(Constants.MASTER_WEB_PORT);
    String masterHostName =
        NetworkAddressUtils.getConnectHost(ServiceType.MASTER_RPC, mTachyonConf);
    request.setAttribute("masterHost", masterHostName);
    request.setAttribute("masterPort", masterWebPort);
    getServletContext().getRequestDispatcher("/header.jsp").include(request, response);
  }
}
