/*
 * Copyright 2025 Chris Ainsley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ainslec.picopica;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Public-facing fluent API entry point for running the Picopica preprocessor.
 * <p>
 * Allows configuration of the source text, active keys, resource URL,
 * license mapping, and optional automatic license header insertion
 * before executing the preprocessing run.
 * </p>
 *
 * <p>
 * Instances are created via {@link PicoPica#input(String)} and then
 * configured using fluent method chaining before calling {@link #exec()}.
 * </p>
 */
public final class Api {
	
    private String resourceUrl;
    private String input;
    private Set<String> keys = Collections.emptySet();
    private Map<String, String> licenseMap = Collections.emptyMap();
    private boolean executed = false;
    private boolean autoAddLicenseToHeader = false;
    private LanguageMode languageMode = LanguageMode.DEFAULT; // Default is c-like by default (but only supporting multi line style comments */
    

    Api() {}

    /** Set (or replace) the source input. */
    public Api load(String src, String ... keys) {
        this.input = src;
        
        if (keys != null && keys.length> 0) {
        	this.keys = Set.of(keys);
        }
        
        return this;
    }
    
    public Api autoAddLicenseToHeader() {
        this.autoAddLicenseToHeader = true;
        return this;
    }

    /** Optional: tag the input with a resource URL (used in errors/positions). */
    public Api resourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
        return this;
    }

    /** Provide the active keys. Null becomes empty. */
    public Api keys(Set<String> keys) {
        this.keys = (keys == null) ? Collections.emptySet() : Set.copyOf(keys);
        return this;
    }
    
    /** Provide the active keys. Null becomes empty. */
    public Api languageMode(LanguageMode languageMode) {
        this.languageMode = languageMode == null ? this.languageMode : languageMode;
        return this;
    }
    
    public Api licenseMap(Map<String, String> licenseMap) {
        this.licenseMap = (licenseMap == null) ? Collections.emptyMap() : Map.copyOf(licenseMap);
        return this;
    }

    /** Convenience varargs: .keys("FOO","BAR") */
    public Api keys(String... keys) {
        if (keys == null) {
            this.keys = Collections.emptySet();
        } else {
            this.keys = Arrays.stream(keys).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
        }
        return this;
    }

    /**
     * Run the preprocessor.
     * @throws IllegalStateException if input(...) was not provided.
     */
    public String exec() {
    	
        if (executed) {
            // optional guard; remove if you want to allow re-use
            throw new IllegalStateException("This Picopica exec() has already been executed.");
        }
        
        if (input == null) {
            throw new IllegalStateException("Missing input: call Picopica.input(\"...\") before exec().");
        }
        
        executed = true;
        return PicoPica.choose(resourceUrl, input, keys == null ? Collections.emptySet() : keys, licenseMap, autoAddLicenseToHeader, languageMode);
    }
}