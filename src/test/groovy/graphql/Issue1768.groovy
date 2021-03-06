package graphql

import graphql.schema.DataFetcher
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLSchema
import graphql.schema.GraphQLSchemaElement
import graphql.schema.GraphQLTypeVisitorStub
import graphql.schema.SchemaTransformer
import graphql.schema.idl.NaturalEnumValuesProvider
import graphql.util.TraversalControl
import graphql.util.TraverserContext
import graphql.util.TreeTransformerUtil
import spock.lang.Specification

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring
import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring

class Issue1768 extends Specification {

    static enum ThreadSort {
        NEWEST_FIRST,
        OLDEST_FIRST,
        MOST_COMMENTS_FIRST
    }

    def "#1768 check if the old behavior is not broken" () {
        def spec = '''
            type Query {
                dummy: String                
            }
            '''
        GraphQL graphql = TestUtil.graphQL(spec, [Query: [dummy: (DataFetcher<String>) { null }]]).build()

        when:
        ExecutionResult result = graphql.execute {
            it.query(" { dummy } ")
        }

        then:
        result.data.dummy == null

    }
    def "#1768 test if local context is set for top level"() {
        def spec = '''
            type Query {
                dummy: String                
            }
            '''
        GraphQL graphql = TestUtil.graphQL(spec, [Query: [dummy: (DataFetcher<String>) { (String) it.localContext }]]).build()

        when:
        ExecutionResult result = graphql.execute {
            it.localContext("test").query(" { dummy } ")
        }

        then:
        result.data.dummy == "test"
    }
    def "#1768 test if the top local context gets transferred to the next level"() {
        def spec = '''
            type Query {
                dummy: DummyType                
            }
            type DummyType {
                dummy: String
            }
            '''
        GraphQL graphql = TestUtil.graphQL(spec, [
                Query: [dummy: (DataFetcher<Map>) { [ : ]}],
                DummyType: [dummy: (DataFetcher<String>) { (String) it.localContext }]]).build()

        when:
        ExecutionResult result = graphql.execute {
            it.localContext("test").query(" { dummy { dummy }} ")
        }

        then:
        result.data.dummy.dummy == "test"
    }

}
