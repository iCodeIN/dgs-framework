/*
 * Copyright 2021 Netflix, Inc.
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

package com.netflix.graphql.dgs.webmvc.autoconfigure.metrics

import graphql.ErrorType
import graphql.ExecutionResult
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags

class DefaultGraphQLTagsProvider : GraphQLTagsProvider {

	companion object {
		val OUTCOME_SUCCESS: Tag  = Tag.of("outcome", "SUCCESS")
		val OUTCOME_ERROR: Tag = Tag.of("outcome", "ERROR")
	}

	@Override
	override fun getTags(parameters: InstrumentationExecutionParameters,
						 result: ExecutionResult ,
						 exception: Throwable ): Iterable<Tag> {
		var tags = Tags.of(Tag.of("query", parameters.query))
		if (result.isDataPresent) {
			tags = tags.and(OUTCOME_SUCCESS)
		}
		else {
			tags = tags.and(OUTCOME_ERROR)
			if (result.errors.isNotEmpty()) {
				val errorClassification = result.errors[0]?.errorType
				if (errorClassification is ErrorType) {
					tags = tags.and(Tag.of("errorType", errorClassification.name))
				}
			}
		}
		return tags
	}
}
