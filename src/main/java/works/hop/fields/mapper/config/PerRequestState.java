package works.hop.fields.mapper.config;

import graphql.execution.instrumentation.InstrumentationState;

/**
 * Composition of map for caching request-scoped data
 */
public class PerRequestState extends PerRequestCache implements InstrumentationState {

    public static final String REQUEST_STATE = PerRequestState.class.getSimpleName();
}
