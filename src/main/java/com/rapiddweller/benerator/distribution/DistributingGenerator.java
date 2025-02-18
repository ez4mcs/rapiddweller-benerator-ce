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

package com.rapiddweller.benerator.distribution;

import com.rapiddweller.benerator.Generator;
import com.rapiddweller.benerator.GeneratorContext;
import com.rapiddweller.benerator.wrapper.GeneratorProxy;
import com.rapiddweller.common.ThreadUtil;

/**
 * General purpose generator proxy which is supposed to work with any distribution.
 * The behavior on a reset is up to the generator created by the distribution.<br/><br/>
 * Created: 22.03.2010 10:45:48
 * @param <E> the type parameter
 * @author Volker Bergmann
 * @since 0.6.0
 */
public class DistributingGenerator<E> extends GeneratorProxy<E> {

  private final Generator<E> dataProvider;
  private final Distribution distribution;
  private final boolean unique;

  public DistributingGenerator(Generator<E> dataProvider, Distribution distribution, boolean unique) {
    super(dataProvider.getGeneratedType());
    this.dataProvider = dataProvider;
    this.distribution = distribution;
    this.unique = unique;
  }

  @Override
  public boolean isThreadSafe() {
    // The implementation of this class itself is thread safe.
    // For the combination of this with a distribution and a dataProvider to bes thread-safe,
    // the sequence must be thread-safe and either the dataProvider be thread-safe or the
    // application of the distribution detached.
    return ThreadUtil.isThreadSafe(distribution)
        && (distribution.isApplicationDetached() || dataProvider.isThreadSafe());
  }

  @Override
  public boolean isParallelizable() {
    return ThreadUtil.allParallelizable(dataProvider, distribution);
  }

  @Override
  public void init(GeneratorContext context) {
    dataProvider.init(context);
    setSource(distribution.applyTo(dataProvider, unique));
    super.init(context);
  }

}
