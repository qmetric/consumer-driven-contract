package com.qmetric.consumerdrivencontract
import au.com.dius.pact.model.Interaction
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static com.qmetric.consumerdrivencontract.Contract.prepareProvider
import static com.qmetric.consumerdrivencontract.ContractBuilder.contract
import static DummyService.createExampleFooProviderServiceListeningOnPort

// this annotation can be used to configure test runner to ignore it while running isolated unit tests
@ConsumerDrivenContractTest
class ProviderSideVerificationTest extends Specification {

    // shared because we use it in the where clause
    @Shared Contract contract

    // Used to start provider service under test and
    // to inform pact which port it should use to connect to that service.
    private final static int FOO_PROVIDER_PORT = 12309

    // this will be replaced by you real provider service
    private DummyService yourProviderAppUnderTest

    def setupSpec() {
        contract = contract()
                // Optional, falls back to the default pact directory if omitted.
                // Here we have to specify it as we use test resource directory
                // for testing purposes instead of one created by pact.
                // Unless you test the library itself, as we do here,
                // you may not need to customize the location of the pact files
                .withContractsDirectory(directoryWithPreGeneratedPacts())
                // provider name is used to find the contract file
                .withProviderListeningOnPort("fooProvider", FOO_PROVIDER_PORT)
                .andBasicAuthorization("dXNlcjpwYXNz") // user:pass
                // consumer name is also used to find the contract file
                // consumer should be specified after the provider as at this stage the file name is constructed
                .andConsumer("barConsumer")
                .build()
    }

    def setup() {
        // Here you will set up you real provider service under test.
        // Try use the real app and stub dependencies only if necessary (e.g. endpoints to other services)
        yourProviderAppUnderTest = createExampleFooProviderServiceListeningOnPort(FOO_PROVIDER_PORT)
    }

    def cleanup() {
        // here you will clean up the mess, shut down the real provider service you tested etc.
        yourProviderAppUnderTest?.shutdown();
    }

    // This is the main contract verification
    @Unroll
    def "should honour the contract with barConsumer (#interaction)" () {
        given:  providerStatePreparedFor(interaction)
        expect: contract.isFulfilledWhenOccurs(interaction)
        where:  interaction << contract.getInteractionsWith("barConsumer")
    }

    // The state name is extracted from the interaction and used to find and execute the appropriate closure.
    // Go to json contract description to find the state mentioned below.
    private void providerStatePreparedFor(Interaction interaction) {
        prepareProvider(interaction, [
                "bar is 5": {
                    yourProviderAppUnderTest.configureBarToBeFive()
                }
        ])
    }

    private String directoryWithPreGeneratedPacts() {
        getClass().classLoader.getResource("pacts-dependents").getFile()
    }

}