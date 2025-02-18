/*
 * (c) Copyright 2006-2020 by rapiddweller GmbH & Volker Bergmann. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, is permitted under the terms of the
 * GNU General Public License.
 *
 * For redistributing this software or a derivative work under a license other
 * than the GPL-compatible Free Software License as defined by the Free
 * Software Foundation or approved by OSI, you must first obtain a commercial
 * license to this software product from rapiddweller GmbH & Volker Bergmann.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED CONDITIONS,
 * REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE
 * HEREBY EXCLUDED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.rapiddweller.benerator.engine.statement;

import com.rapiddweller.benerator.engine.BeneratorContext;
import com.rapiddweller.benerator.engine.Statement;
import com.rapiddweller.jdbacl.identity.IdentityProvider;
import com.rapiddweller.jdbacl.identity.KeyMapper;
import com.rapiddweller.model.data.ComplexTypeDescriptor;
import com.rapiddweller.model.data.Entity;
import com.rapiddweller.platform.db.AbstractDBSystem;

import java.util.List;

/**
 * Interface for transcoding classes that can be parents of cascade operations.<br/><br/>
 * Created: 18.04.2011 08:35:04
 *
 * @author Volker Bergmann
 * @since 0.6.6
 */
public interface CascadeParent extends Statement {
  /**
   * Add sub statement.
   *
   * @param subStatement the sub statement
   */
  void addSubStatement(Statement subStatement);

  /**
   * Gets source.
   *
   * @param context the context
   * @return the source
   */
  AbstractDBSystem getSource(BeneratorContext context);

  /**
   * Gets target.
   *
   * @param context the context
   * @return the target
   */
  AbstractDBSystem getTarget(BeneratorContext context);

  /**
   * Current entity entity.
   *
   * @return the entity
   */
  Entity currentEntity();

  /**
   * Gets key mapper.
   *
   * @return the key mapper
   */
  KeyMapper getKeyMapper();

  /**
   * Gets identity provider.
   *
   * @return the identity provider
   */
  IdentityProvider getIdentityProvider();

  /**
   * Needs nk mapping boolean.
   *
   * @param type the type
   * @return the boolean
   */
  boolean needsNkMapping(String type);

  /**
   * Gets type.
   *
   * @param db      the db
   * @param context the context
   * @return the type
   */
  ComplexTypeDescriptor getType(AbstractDBSystem db, BeneratorContext context);

  /**
   * Gets sub statements.
   *
   * @return the sub statements
   */
  List<Statement> getSubStatements();
}
