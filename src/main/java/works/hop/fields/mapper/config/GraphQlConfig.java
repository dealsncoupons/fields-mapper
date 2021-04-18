package works.hop.fields.mapper.config;

import graphql.*;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionPath;
import graphql.execution.SubscriptionExecutionStrategy;
import graphql.execution.instrumentation.*;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import graphql.language.SourceLocation;
import graphql.language.StringValue;
import graphql.scalars.ExtendedScalars;
import graphql.schema.*;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import works.hop.fields.mapper.fetcher.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static graphql.scalars.util.Kit.typeName;
import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;
import static works.hop.fields.mapper.config.PerRequestState.REQUEST_STATE;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class GraphQlConfig {

    final PlayerFetcher playerFetcher;
    final SportFetcher sportFetcher;
    final SponsorFetcher sponsorFetcher;
    final PlayerTeamsFetcher playerTeamsFetcher;
    final PrizeSponsorFetcher prizeSponsorFetcher;
    final PrizeSportFetcher prizeSportFetcher;
    final PrizeWinnerFetcher prizeWinnerFetcher;
    final SportTeamsFetcher sportTeamsFetcher;
    //mutations
    final RegisterPlayerFetcher registerPlayerFetcher;
    final AddTeamPlayerFetcher addTeamPlayerFetcher;

    @Bean
    public GraphQL graphQL(@Value("${spring.graphql.schema.location:schema/schema.graphqls}") String location) throws IOException {
        return GraphQL.newGraphQL(buildSchema(location))
                .instrumentation(requestContext())
                .subscriptionExecutionStrategy(new SubscriptionExecutionStrategy())
                .build();
    }

    @Bean
    public DataFetcherExceptionHandler dataFetcherExceptionHandler() {
        return handlerParameters -> {
            Throwable exception = handlerParameters.getException();
            SourceLocation sourceLocation = handlerParameters.getSourceLocation();
            ExecutionPath path = handlerParameters.getPath();

            ExceptionWhileDataFetching error = new ExceptionWhileDataFetching(path, exception, sourceLocation);
            log.warn(error.getMessage(), exception);

            return DataFetcherExceptionHandlerResult.newResult().error(error).build();
        };
    }

    private GraphQLSchema buildSchema(String location) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(location)) {
            TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(inputStream);
            RuntimeWiring runtimeWiring = buildWiring();
            SchemaGenerator schemaGenerator = new SchemaGenerator();
            return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
        }
    }

    private Instrumentation requestContext() {
        return new SimpleInstrumentation() {
            @Override
            public InstrumentationState createState() {
                return new PerRequestState();
            }

            @Override
            public ExecutionInput instrumentExecutionInput(ExecutionInput executionInput, InstrumentationExecutionParameters parameters) {
                return ExecutionInput.newExecutionInput()
                        .variables(executionInput.getVariables())
                        .operationName(executionInput.getOperationName())
                        .query(executionInput.getQuery())
                        .context(GraphQLContext.newContext()
                                .of(REQUEST_STATE, parameters.getInstrumentationState())
                                .build())
                        .dataLoaderRegistry(executionInput.getDataLoaderRegistry())
                        .cacheControl(executionInput.getCacheControl())
                        .executionId(executionInput.getExecutionId())
                        .localContext(executionInput.getLocalContext())
                        .locale(executionInput.getLocale())
                        .root(executionInput.getRoot())
                        .build();
            }

            @Override
            public InstrumentationContext<ExecutionResult> beginExecution(InstrumentationExecutionParameters parameters) {
                return new SimpleInstrumentationContext<>() {
                    @Override
                    public void onCompleted(ExecutionResult result, Throwable t) {
                        PerRequestState state = parameters.getInstrumentationState();
                        state.clear();
                        System.out.println("Request state cleared");
                    }
                };
            }
        };
    }

    private GraphQLCodeRegistry codeRegistry() {
        return GraphQLCodeRegistry.newCodeRegistry()
                .dataFetcher(
                        coordinates("Player", "teams"),
                        playerTeamsFetcher)
                .dataFetcher(
                        coordinates("Prize", "sport"),
                        prizeSportFetcher)
                .dataFetcher(
                        coordinates("Prize", "sponsor"),
                        prizeSponsorFetcher)
                .dataFetcher(
                        coordinates("Prize", "winner"),
                        prizeWinnerFetcher)
                .dataFetcher(
                        coordinates("Sport", "teams"),
                        sportTeamsFetcher)
                .build();
    }

    private RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("Query")
                        .dataFetcher("playerById", playerFetcher)
                        .dataFetcher("sportByName", sportFetcher)
//                        .dataFetcher("playerSports", userFolloweesFetcher)
//                        .dataFetcher("teamPlayers", findUserFetcher)
                )
                .type(newTypeWiring("Mutation")
                        .dataFetcher("registerPlayer", registerPlayerFetcher)
                        .dataFetcher("addTeamPlayer", addTeamPlayerFetcher)
                )
                .codeRegistry(codeRegistry())
                .scalar(extendsUUIdScalarType())
                .scalar(extendedScalarsBigDecimal())
                .scalar(extendedScalarsDate())
                .scalar(extendedScalarsDateTime())
//                .directive("auth", new AuthRoleDirective())
                .build();
    }

    @Bean
    public GraphQLScalarType extendedScalarsDateTime() {
        return ExtendedScalars.DateTime;
    }

    @Bean
    public GraphQLScalarType extendedScalarsDate() {
        return ExtendedScalars.Date;
    }

    @Bean
    public GraphQLScalarType extendedScalarsBigDecimal() {
        return ExtendedScalars.GraphQLBigDecimal;
    }

    @Bean
    public GraphQLScalarType extendsUUIdScalarType() {
        return GraphQLScalarType.newScalar()
                .name("UUID")
                .description("UUID value as a string")
                .coercing(new Coercing<UUID, String>() {
                    @Override
                    public String serialize(Object input) throws CoercingSerializeException {
                        if (input instanceof String) {
                            try {
                                return (UUID.fromString((String) input)).toString();
                            } catch (IllegalArgumentException ex) {
                                throw new CoercingSerializeException(
                                        "Expected a UUID value that can be converted : '" + ex.getMessage() + "'."
                                );
                            }
                        } else if (input instanceof UUID) {
                            return input.toString();
                        } else {
                            throw new CoercingSerializeException(
                                    "Expected something we can convert to 'java.util.UUID' but was '" + typeName(input) + "'."
                            );
                        }
                    }

                    @Override
                    public UUID parseValue(Object input) throws CoercingParseValueException {
                        if (input instanceof String) {
                            try {
                                return UUID.fromString((String) input);
                            } catch (IllegalArgumentException ex) {
                                throw new CoercingParseValueException(
                                        "Expected a 'String' of UUID type but was '" + typeName(input) + "'."
                                );
                            }
                        } else if (input instanceof UUID) {
                            return (UUID) input;
                        } else {
                            throw new CoercingParseValueException(
                                    "Expected a 'String' or 'UUID' type but was '" + typeName(input) + "'."
                            );
                        }
                    }

                    @Override
                    public UUID parseLiteral(Object input) throws CoercingParseLiteralException {
                        if (!(input instanceof StringValue)) {
                            throw new CoercingParseLiteralException(
                                    "Expected a 'java.util.UUID' AST type object but was '" + typeName(input) + "'."
                            );
                        }
                        try {
                            return UUID.fromString(((StringValue) input).getValue());
                        } catch (IllegalArgumentException ex) {
                            throw new CoercingParseLiteralException(
                                    "Expected something that we can convert to a UUID but was invalid"
                            );
                        }
                    }
                }).build();
    }
}
