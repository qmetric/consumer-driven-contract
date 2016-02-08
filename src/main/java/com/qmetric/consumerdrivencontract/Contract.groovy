package com.qmetric.consumerdrivencontract

import au.com.dius.pact.model.Interaction
import au.com.dius.pact.model.Pact
import au.com.dius.pact.model.PactReader
import au.com.dius.pact.provider.ProviderClient
import au.com.dius.pact.provider.ProviderInfo
import au.com.dius.pact.provider.ResponseComparison
import scala.collection.JavaConversions

class Contract {

    private final ProviderInfo providerInfo
    private final Map<String, String> consumerContractFiles

    Contract(ProviderInfo providerInfo, Map<String,String> consumerContractFiles) {
        this.providerInfo = providerInfo
        this.consumerContractFiles = consumerContractFiles.asImmutable()
    }

    static void prepareProvider(Interaction interaction, Map<String, Closure> fixtures) {
        String state = interaction.providerState().nonEmpty() ? interaction.providerState().get() : interaction.description()
        if (fixtures.containsKey(state)) {
            fixtures[state]()
        } else {
            throw new IllegalArgumentException("State not found in fixtures: $state")
        }
    }

    List<Interaction> getInteractionsWith(String consumerName) {
        if (! consumerContractFiles.containsKey(consumerName)) {
            throw new IllegalArgumentException("'$consumerName' consumer name not found - have you added it while building the contract?")
        }
        Pact pact = createPact(providerInfo, consumerName, consumerContractFiles[consumerName])

        JavaConversions.seqAsJavaList(pact.interactions())
    }

    void isFulfilledWhenOccurs(Interaction interaction) {
        def result = perform(interaction)
        verifyCorrectResult(result)
    }



    private Map perform(Interaction interaction) {
        ProviderClient client = new ProviderClient(provider: providerInfo, request: interaction.request())
        Map clientResponse = (Map) client.makeRequest()
        ResponseComparison.compareResponse(
                interaction.response(),
                clientResponse as Map,
                clientResponse.statusCode as int,
                clientResponse.headers as Map,
                clientResponse.data as String
        ) as Map
    }

    private static void verifyCorrectResult(Map result) {
        assert result.method == true
        if (result.headers.size() > 0) {
            result.headers.each { k, v ->
                assert v == true
            }
        }
        // empty list of body mismatches
        assert result.body.size() == 0
    }

    private static Pact createPact(ProviderInfo providerInfo, String consumer, String contractFile) {
        (Pact) new PactReader().loadPact(
                providerInfo.hasPactWith(consumer, { pactFile = new File(contractFile) }).getPactFile()
        );
    }
}