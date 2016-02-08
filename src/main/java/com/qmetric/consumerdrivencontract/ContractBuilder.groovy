package com.qmetric.consumerdrivencontract

import au.com.dius.pact.provider.ProviderInfo

class ContractBuilder {

    private String contractsDirectory = "target/pacts-dependents"
    private Map<String, String> consumerContractFiles = [:]
    private String basicAuthorization;
    private String providerName;
    private int port;

    static ContractBuilder contract() { new ContractBuilder() }
    ContractBuilder withContractsDirectory(String contractsDirectory) { this.contractsDirectory = contractsDirectory; this }
    ContractBuilder withProviderListeningOnPort(final String providerName, final Integer port) { this.providerName = providerName; this.port = port; this }
    ContractBuilder andConsumer(String consumerName) { this.consumerContractFiles[consumerName] = pathToContract(consumerName); this }
    ContractBuilder andBasicAuthorization(String basicAuthorization) { this.basicAuthorization = basicAuthorization; this }

    Contract build()
    {
        new Contract(configuredProviderInfo(), consumerContractFiles)
    }

    ProviderInfo configuredProviderInfo() {
        ProviderInfo providerInfo = new ProviderInfo()
        configureProviderListeningOnPort(providerInfo)
        configureBasicAuthorization(providerInfo)

        providerInfo
    }

    private void configureProviderListeningOnPort(ProviderInfo providerInfo) {
        providerInfo.name = providerName
        providerInfo.port = port
    }

    private void configureBasicAuthorization(ProviderInfo providerInfo) {
        // e.g. 'Basic dXNlcjpwYXNz' -> user:pass
        if (basicAuthorization) {
            providerInfo.requestFilter = {
                req -> req.addHeader('Authorization', "Basic $basicAuthorization")
            }
        }
    }

    private String pathToContract(String consumerName) {
        contractsDirectory + File.separator + consumerName + "-" + providerName + ".json"
    }
}