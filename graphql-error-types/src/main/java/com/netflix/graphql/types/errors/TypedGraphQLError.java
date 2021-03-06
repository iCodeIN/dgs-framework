/*
 * Copyright 2020 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.graphql.types.errors;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static graphql.Assert.assertNotNull;

public class TypedGraphQLError implements GraphQLError {

    private final String message;
    private final List<SourceLocation> locations;
    private final ErrorClassification classification;
    private final List<Object> path;
    private final Map<String, Object> extensions;

    public TypedGraphQLError(String message, List<SourceLocation> locations, ErrorClassification classification, List<Object> path, Map<String, Object> extensions) {
        this.message = message;
        this.locations = locations;
        this.classification = classification;
        this.path = path;
        this.extensions = extensions;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return locations;
    }

    @Override
    // We return null here because we don't want graphql-java to write classification field
    public ErrorClassification getErrorType() {
        return null;
    }

    @Override
    public List<Object> getPath() {
        return path;
    }

    @Override
    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public static Builder UNKNOWN = newBuilder();
    public static Builder INTERNAL = newBuilder().errorType(ErrorType.INTERNAL);
    public static Builder NOT_FOUND = newBuilder().errorType(ErrorType.NOT_FOUND);
    public static Builder UNAUTHENTICATED = newBuilder().errorType(ErrorType.UNAUTHENTICATED);
    public static Builder PERMISSION_DENIED = newBuilder().errorType(ErrorType.PERMISSION_DENIED);
    public static Builder BAD_REQUEST = newBuilder().errorType(ErrorType.BAD_REQUEST);
    public static Builder UNAVAILABLE = newBuilder().errorType(ErrorType.UNAVAILABLE);
    public static Builder FAILED_PRECONDITION = newBuilder().errorType(ErrorType.FAILED_PRECONDITION);

    public static Builder FIELD_NOT_FOUND = newBuilder().errorDetail(ErrorDetail.Common.FIELD_NOT_FOUND);
    public static Builder INVALID_CURSOR = newBuilder().errorDetail(ErrorDetail.Common.INVALID_CURSOR);
    public static Builder UNIMPLEMENTED = newBuilder().errorDetail(ErrorDetail.Common.UNIMPLEMENTED);
    public static Builder INVALID_ARGUMENT = newBuilder().errorDetail(ErrorDetail.Common.INVALID_ARGUMENT);
    public static Builder DEADLINE_EXCEEDED = newBuilder().errorDetail(ErrorDetail.Common.DEADLINE_EXCEEDED);
    public static Builder SERVICE_ERROR = newBuilder().errorDetail(ErrorDetail.Common.SERVICE_ERROR);
    public static Builder ENHANCE_YOUR_CALM = newBuilder().errorDetail(ErrorDetail.Common.ENHANCE_YOUR_CALM);
    public static Builder THROTTLED_CPU = newBuilder().errorDetail(ErrorDetail.Common.THROTTLED_CPU);
    public static Builder THROTTLED_CONCURRENCY = newBuilder().errorDetail(ErrorDetail.Common.THROTTLED_CONCURRENCY);
    public static Builder MISSING_RESOURCE = newBuilder().errorDetail(ErrorDetail.Common.MISSING_RESOURCE);

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "TypedGraphQLError{" +
                "message='" + message + '\'' +
                ", locations=" + locations +
                ", path=" + path +
                ", extensions=" + extensions +
                '}';
    }

    public static class Builder {
        private String message;
        private List<Object> path;
        private List<SourceLocation> locations = new ArrayList<>();
        private ErrorClassification errorClassification = ErrorType.UNKNOWN;
        private Map<String, Object> extensions;
        private String origin;
        private String debugUri;
        private Map<String, Object> debugInfo;

        private Builder() {
        }

        private String defaultMessage() {
            return errorClassification.toString();
        }

        private Map<String, Object> getExtensions() {
            HashMap<String, Object> extensionsMap = new HashMap<>();
            if (extensions != null) extensionsMap.putAll(extensions);
            if (errorClassification instanceof ErrorType) {
                extensionsMap.put("errorType", String.valueOf(errorClassification));
            } else if (errorClassification instanceof ErrorDetail) {
                extensionsMap.put("errorType", String.valueOf(((ErrorDetail) errorClassification).getErrorType()));
                extensionsMap.put("errorDetail", String.valueOf(errorClassification));
            }
            if (origin != null) extensionsMap.put("origin", origin);
            if (debugUri != null) extensionsMap.put("debugUri", debugUri);
            if (debugInfo != null) extensionsMap.put("debugInfo", debugInfo);
            return extensionsMap;
        }

        public Builder message(String message, Object... formatArgs) {
            this.message = String.format(assertNotNull(message), formatArgs);
            return this;
        }

        public Builder locations(List<SourceLocation> locations) {
            this.locations.addAll(assertNotNull(locations));
            return this;
        }

        public Builder location(SourceLocation location) {
            this.locations.add(assertNotNull(location));
            return this;
        }

        public Builder path(ExecutionPath path) {
            this.path = assertNotNull(path).toList();
            return this;
        }

        public Builder path(List<Object> path) {
            this.path = assertNotNull(path);
            return this;
        }

        public Builder errorType(ErrorType errorType) {
            this.errorClassification = assertNotNull(errorType);
            return this;
        }

        public Builder errorDetail(ErrorDetail errorDetail) {
            this.errorClassification = assertNotNull(errorDetail);
            return this;
        }

        public Builder origin(String origin) {
            this.origin = assertNotNull(origin);
            return this;
        }

        public Builder debugUri(String debugUri) {
            this.debugUri = assertNotNull(debugUri);
            return this;
        }

        public Builder debugInfo(Map<String, Object> debugInfo) {
            this.debugInfo = assertNotNull(debugInfo);
            return this;
        }

        public Builder extensions(Map<String, Object> extensions) {
            this.extensions = assertNotNull(extensions);
            return this;
        }

        /**
         * @return a newly built GraphQLError
         */
        public TypedGraphQLError build() {
            if (message == null) message = defaultMessage();
            return new TypedGraphQLError(message, locations, errorClassification, path, getExtensions());
        }
    }

}
