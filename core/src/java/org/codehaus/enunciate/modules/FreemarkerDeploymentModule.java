/*
 * Copyright 2006 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.modules;

import freemarker.cache.URLTemplateLoader;
import freemarker.template.*;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Basic module that processes a freemarker template when generating, handling the TemplateException that occurs.
 *
 * @author Ryan Heaton
 */
public abstract class FreemarkerDeploymentModule extends BasicDeploymentModule {

  /**
   * Processes the template.  Declared final because we don't ever want to do more than process
   * the template so it can be safe to call the processTemplate() method directly (e.g. in the
   * {@link org.codehaus.enunciate.apt.EnunciateAnnotationProcessor}).
   */
  @Override
  protected final void doGenerate() throws EnunciateException, IOException {
    try {
      doFreemarkerGenerate();
    }
    catch (TemplateException e) {
      throw new EnunciateException(e);
    }
  }

  /**
   * Generate using Freemarker.  Same as {@link #doGenerate} but can throw a TemplateException.
   */
  public abstract void doFreemarkerGenerate() throws EnunciateException, IOException, TemplateException;

  /**
   * Processes the specified template with the given model.
   *
   * @param templateURL The template URL.
   * @param model       The root model.
   */
  public void processTemplate(URL templateURL, Object model) throws IOException, TemplateException {
    processTemplate(templateURL, model, System.out);
  }

  /**
   * Processes the specified template with the given model.
   *
   * @param templateURL The template URL.
   * @param model       The root model.
   * @param out         The output stream to process to.
   */
  public void processTemplate(URL templateURL, Object model, PrintStream out) throws IOException, TemplateException {
    debug("Processing template %s.", templateURL);
    Configuration configuration = getConfiguration();
    configuration.setDefaultEncoding("UTF-8");
    Template template = configuration.getTemplate(templateURL.toString());
    processTemplate(template, model, out);
  }

  /**
   * Processes the specified template with the given model.
   *
   * @param template The template.
   * @param model    The root model.
   */
  public void processTemplate(Template template, Object model) throws TemplateException, IOException {
    processTemplate(template, model, System.out);
  }

  /**
   * Processes the specified template to the specified output stream.
   *
   * @param template The template to process.
   * @param model The model.
   * @param out The output stream.
   */
  public void processTemplate(Template template, Object model, PrintStream out) throws TemplateException, IOException {
    template.process(model, new OutputStreamWriter(out));
  }

  /**
   * Gets the model for processing.
   *
   * @return The model for processing.
   */
  public EnunciateFreemarkerModel getModel() throws IOException {
    EnunciateFreemarkerModel model = (EnunciateFreemarkerModel) FreemarkerModel.get();

    if (model == null) {
      throw new IOException("A model must be established.");
    }

    model.setObjectWrapper(getObjectWrapper());
    model.setFileOutputDirectory(getGenerateDir());
    return model;
  }

  /**
   * The object wrapper to use for the model.
   *
   * @return The object wrapper to use for the model.
   */
  protected ObjectWrapper getObjectWrapper() {
    return new DefaultObjectWrapper();
  }

  /**
   * Get the freemarker configuration.
   *
   * @return the freemarker configuration.
   */
  protected Configuration getConfiguration() {
    Configuration configuration = new Configuration();
    configuration.setTemplateLoader(getTemplateLoader());
    configuration.setLocalizedLookup(false);
    return configuration;
  }

  /**
   * Get the template loader for the freemarker configuration.
   *
   * @return the template loader for the freemarker configuration.
   */
  protected URLTemplateLoader getTemplateLoader() {
    return new URLTemplateLoader() {
      protected URL getURL(String name) {
        try {
          return new URL(name);
        }
        catch (MalformedURLException e) {
          return null;
        }
      }
    };
  }

}