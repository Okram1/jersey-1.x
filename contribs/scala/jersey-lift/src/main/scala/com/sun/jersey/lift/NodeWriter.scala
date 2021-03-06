/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.lift

import _root_.java.io.OutputStream
import _root_.java.lang.annotation.Annotation
import _root_.java.lang.reflect.Type

import _root_.net.liftweb.http.{S, LiftServlet}
import _root_.net.liftweb.http.provider.servlet.HTTPRequestServlet

import javax.servlet.ServletContext
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import javax.ws.rs.core.{UriInfo, Context, MultivaluedMap, MediaType}
import javax.ws.rs.ext.{MessageBodyWriter, Provider}

import com.sun.jersey.api.container.ContainerException
import com.sun.jersey.api.core.HttpContext

import javax.ws.rs.Produces
import scala.xml.NodeSeq

/**
 * Converts a Scala   { @link NodeSeq } to a String for rendering nodes as HTML, XML, XHTML using LiftWeb's templates
 *
 * @version $Revision : 1.1 $
 */
@Provider
@Produces(Array("text/html;qs=5", "text/xhtml"))
class NodeWriter extends MessageBodyWriter[NodeSeq] {
  @Context
  var request: HttpServletRequest = null
  @Context
  var uriInfo: UriInfo = null

  def isWriteable(aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = {
    classOf[NodeSeq].isAssignableFrom(aClass)
  }

  def getSize(nodes: NodeSeq, aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = -1L

  def writeTo(template: NodeSeq, aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType, multiMap: MultivaluedMap[String, Object], out: OutputStream): Unit = {
    // Commit the status and headers to the HttpServletResponse
    out.flush

    if (request != null) {
      if (uriInfo != null) {
        val resources = uriInfo.getMatchedResources
        if (!resources.isEmpty) {
          val it = resources.get(resources.size() - 1)
          ResourceBean.set(request, it)
        }
      }
      val nodes = S.render(template, new HTTPRequestServlet(request))
      val text = nodes.toString();
      out.write(text.getBytes());
    }
    else {
      throw new ContainerException("request was not injected properly by the JAXRS runtime!");
    }
  }
}
