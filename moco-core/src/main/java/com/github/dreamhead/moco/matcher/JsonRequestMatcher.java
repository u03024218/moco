package com.github.dreamhead.moco.matcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dreamhead.moco.*;
import com.github.dreamhead.moco.resource.Resource;
import com.google.common.base.Optional;

import java.io.IOException;

import static com.github.dreamhead.moco.Moco.and;
import static com.google.common.base.Optional.of;

public class JsonRequestMatcher implements RequestMatcher {
    private final RequestExtractor<String> extractor;
    private final Resource resource;
    private final ObjectMapper mapper;

    public JsonRequestMatcher(final RequestExtractor<String> extractor, final Resource resource) {
        this.extractor = extractor;
        this.resource = resource;
        this.mapper = new ObjectMapper();
    }

    @Override
    public boolean match(final Request request) {
        Optional<String> content = extractor.extract(request);
        return content.isPresent() && doMatch(request, content.get());
    }

    private boolean doMatch(final Request request, final String content) {
        try {
            JsonNode requestNode = mapper.readTree(content);
            JsonNode resourceNode = mapper.readTree(resource.readFor(of(request)));
            return requestNode.equals(resourceNode);
        } catch (JsonProcessingException jpe) {
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public RequestMatcher apply(final MocoConfig config) {
        if (config.isFor(MocoConfig.REQUEST_ID)) {
            return (RequestMatcher)config.apply(this);
        }

        Resource appliedResource = this.resource.apply(config);
        if (appliedResource == this.resource) {
            return this;
        }

        return new JsonRequestMatcher(this.extractor, appliedResource);
    }
}
