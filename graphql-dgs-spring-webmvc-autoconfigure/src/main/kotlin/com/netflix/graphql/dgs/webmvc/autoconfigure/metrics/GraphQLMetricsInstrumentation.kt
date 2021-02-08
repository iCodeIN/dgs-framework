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

import graphql.ExecutionResult;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.SimpleInstrumentationContext;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;

import org.springframework.boot.actuate.metrics.AutoTimer;
import java.util.*

class GraphQLMetricsInstrumentation(
		private val registry: MeterRegistry,
		private val tagsProvider: GraphQLTagsProvider,
		private val autoTimer: AutoTimer
): SimpleInstrumentation() {

	override fun createState(): InstrumentationState {
		return MetricsInstrumentationState(this.registry);
	}

	override fun beginExecution(parameters: InstrumentationExecutionParameters ): InstrumentationContext<ExecutionResult> {
		val state: MetricsInstrumentationState = parameters.getInstrumentationState();
		state.startTimer();

        return object: SimpleInstrumentationContext<ExecutionResult>() {
			override fun onCompleted(result: ExecutionResult , exc: Throwable ) {
				val tags: Iterable<Tag> = tagsProvider.getTags(parameters, result, exc)
				state.stopTimer(autoTimer.builder("graphql.query").tags(tags))
			}
		}
	}

	class MetricsInstrumentationState(private val registry: MeterRegistry) : InstrumentationState {

		private var timerSample: Optional<Timer.Sample> = Optional.empty()

		fun startTimer() {
			this.timerSample = Optional.of(  Timer.start(this.registry) )
		}
		fun stopTimer(timer: Timer.Builder) {
			this.timerSample.map { it.stop(timer.register(this.registry)) }
		}
	}

}
