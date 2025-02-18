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

package com.rapiddweller.benerator.dataset;

import com.rapiddweller.common.ArrayBuilder;
import com.rapiddweller.common.IOUtil;
import com.rapiddweller.common.StringUtil;
import com.rapiddweller.common.exception.ExceptionFactory;
import com.rapiddweller.domain.address.Country;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Creates and manages {@link Dataset}s.<br/><br/>
 * Created: 21.03.2008 13:46:54
 * @author Volker Bergmann
 * @since 0.5.0
 */
public class DatasetUtil {

  private static final Logger logger = LoggerFactory.getLogger(DatasetUtil.class);

  public static final String REGION_NESTING = "/com/rapiddweller/dataset/region";

  private static String defaultRegionName;
  private static Dataset defaultRegion;

  protected static final Map<String, Map<String, Dataset>> types = new HashMap<>();

  private DatasetUtil() {
    // private constructor to prevent instantiation
  }

  public static Dataset defaultRegion() {
    if (defaultRegion == null) {
      defaultRegion = DatasetUtil.getDataset("region", defaultRegionName());
    }
    return defaultRegion;
  }

  public static String defaultRegionName() {
    if (defaultRegionName == null) {
      defaultRegionName = Country.getDefault().getIsoCode();
    }
    return defaultRegionName;
  }

  public static String fallbackRegionName() {
    return Country.getFallback().getIsoCode();
  }

  public static Dataset getDataset(String nesting, String name) {
    Map<String, Dataset> sets = types.get(nesting);
    if (sets == null) {
      sets = parseDatasetTypeConfig(nesting);
    }
    return getDataset(nesting, name, sets);
  }

  public static String[] getDataFiles(String filenamePattern, String datasetName, String nesting) {
    Dataset dataset = getDataset(nesting, datasetName);
    ArrayBuilder<String> builder = new ArrayBuilder<>(String.class);
    if (dataset.allAtomicSubSets().isEmpty()) {
      String filename = filenameOfDataset(datasetName, filenamePattern);
      if (IOUtil.isURIAvailable(filename)) {
        builder.add(filename);
      } else {
        throw ExceptionFactory.getInstance().configurationError("File not found: " + filename);
      }
    } else {
      for (Dataset atomicSet : dataset.allAtomicSubSets()) {
        String filename = MessageFormat.format(filenamePattern, atomicSet);
        if (IOUtil.isURIAvailable(filename)) {
          builder.add(filename);
        } else {
          logger.warn("Data file not found: {}", filename);
        }
      }
    }
    return builder.toArray();
  }

  public static String filenameOfDataset(String datasetName, String filenamePattern) {
    return MessageFormat.format(filenamePattern, datasetName);
  }

  public static void runInRegion(String regionName, Runnable task) {
    String realDefaultRegionName = defaultRegionName;
    defaultRegionName = regionName;
    defaultRegion = null;
    try {
      task.run();
    } finally {
      defaultRegionName = realDefaultRegionName;
      defaultRegion = null;
    }
  }

  public static <T> T callInRegion(String regionName, Callable<T> task) throws Exception {
    String realDefaultRegionName = defaultRegionName;
    defaultRegionName = regionName;
    defaultRegion = null;
    try {
      return task.call();
    } finally {
      defaultRegionName = realDefaultRegionName;
      defaultRegion = null;
    }
  }

  // private helpers -------------------------------------------------------------------------------------------------

  private static Dataset getDataset(String type, String name, Map<String, Dataset> sets) {
    return sets.computeIfAbsent(name, k -> new Dataset(type, name));
  }

  private static synchronized Map<String, Dataset> parseDatasetTypeConfig(String nesting) {
    try {
      Map<String, Dataset> sets = new HashMap<>();
      Map<String, String> properties = IOUtil.readProperties(nesting + ".set.properties");
      for (Map.Entry<String, String> entry : properties.entrySet()) {
        String name = entry.getKey();
        Dataset dataset = getDataset(nesting, name, sets);
        String[] subsetNames = StringUtil.tokenize(entry.getValue(), ',');
        for (String subsetName : subsetNames) {
          Dataset subset = getDataset(nesting, subsetName, sets);
          dataset.addSubSet(subset);
        }
      }
      types.put(nesting, sets);
      return sets;
    } catch (Exception e) {
      throw ExceptionFactory.getInstance().configurationError("Setup for Dataset type failed: " + nesting, e);
    }
  }

  public static Locale defaultLanguageForRegion(String datasetName) {
    if (StringUtil.isEmpty(datasetName)) {
      return Locale.getDefault();
    }
    if ("dach".equals(datasetName)) {
      return Locale.GERMAN;
    }
    if (datasetName.length() == 2) {
      Country country = Country.getInstance(datasetName, false);
      if (country != null) {
        return country.getDefaultLanguageLocale();
      }
    }
    return Locale.ENGLISH;
  }

}
