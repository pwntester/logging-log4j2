/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.core.filter;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.message.Message;

/**
 * This filter returns the onMatch result if the message contains less than a spcified number of words.
 *
 */
@Plugin(name = "WordCountFilter", category = Node.CATEGORY, elementType = Filter.ELEMENT_TYPE, printObject = true)
public final class WordCountFilter extends AbstractFilter {


    private final int count;

    private WordCountFilter(final int count, final Result onMatch, final Result onMismatch) {
        super(onMatch, onMismatch);
        this.count= count;
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
            final Object... params) {
        return filter(msg);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final Object msg,
            final Throwable t) {
        if (msg == null) {
            return onMismatch;
        }
        return filter(msg.toString());
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final Message msg,
            final Throwable t) {
        if (msg == null) {
            return onMismatch;
        }
        final String text = msg.getFormattedMessage();
        return filter(text);
    }

    @Override
    public Result filter(final LogEvent event) {
        final String text = event.getMessage().getFormattedMessage();
        return filter(text);
    }

    private Result filter(final String msg) {
        if (msg == null) {
            return onMismatch;
        }
        try {
          String words = execCmd("wc -w <<< \"" + msg + "\"");
          return count > Integer.parseInt(words)? onMatch : onMismatch;
        } catch(Exception e) {
          return onMismatch;
        }
    }

    public static String execCmd(String cmd) throws java.io.IOException {
      java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(new String[] {"bash", "-c", cmd}).getInputStream()).useDelimiter("\\A");
      return s.hasNext() ? s.next() : "";
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        return sb.toString();
    }

    /**
     * Creates a Filter that matches a regular expression.
     *
     * @param regex
     *        The regular expression to match.
     * @param patternFlags
     *        An array of Strings where each String is a {@link Pattern#compile(String, int)} compilation flag.
     * @param useRawMsg
     *        If true, the raw message will be used, otherwise the formatted message will be used.
     * @param match
     *        The action to perform when a match occurs.
     * @param mismatch
     *        The action to perform when a mismatch occurs.
     * @return The RegexFilter.
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    // TODO Consider refactoring to use AbstractFilter.AbstractFilterBuilder
    @PluginFactory
    public static WordCountFilter createFilter(
            //@formatter:off
            @PluginAttribute("count") final int count,
            @PluginAttribute("onMatch") final Result match,
            @PluginAttribute("onMismatch") final Result mismatch)
            //@formatter:on
            throws IllegalArgumentException, IllegalAccessException {
        if (count < 0) {
            LOGGER.error("An integer must be provided for WordCountFilter");
            return null;
        }
        return new WordCountFilter(count, match, mismatch);
    }
}

