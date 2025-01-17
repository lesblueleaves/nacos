/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.core.paramcheck;

import com.alibaba.nacos.common.spi.NacosServiceLoader;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HttpParamExtractor Manager.
 *
 * @author zhuoguang
 */
public class HttpParamExtractorManager {
    
    private static final String SPLITTER = "@@";
    
    private static final HttpParamExtractorManager INSTANCE = new HttpParamExtractorManager();
    
    private static final AbstractHttpParamExtractor DEFAULT_EXTRACTOR = new AbstractHttpParamExtractor() {
        @Override
        public void init() {
        }
        
        @Override
        public void extractParamAndCheck(HttpServletRequest request) throws Exception {
        }
    };
    
    private final Map<String, AbstractHttpParamExtractor> extractorMap = new ConcurrentHashMap<>(32);
    
    private HttpParamExtractorManager() {
        Collection<AbstractHttpParamExtractor> extractors = NacosServiceLoader.load(AbstractHttpParamExtractor.class);
        for (AbstractHttpParamExtractor extractor : extractors) {
            List<String> targetrequestlist = extractor.getTargetRequestList();
            for (String targetrequest : targetrequestlist) {
                extractorMap.put(targetrequest, extractor);
            }
        }
    }
    
    public static HttpParamExtractorManager getInstance() {
        return INSTANCE;
    }
    
    public AbstractHttpParamExtractor getExtractor(String uri, String method, String module) {
        AbstractHttpParamExtractor extractor = extractorMap.get(uri + SPLITTER + method);
        if (extractor == null) {
            extractor = extractorMap.get("default" + SPLITTER + module);
        }
        return extractor == null ? DEFAULT_EXTRACTOR : extractor;
    }
    
}
